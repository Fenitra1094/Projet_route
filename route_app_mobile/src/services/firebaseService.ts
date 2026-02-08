// src/services/firebaseService.ts
import { initializeApp } from "firebase/app";
import { 
  getAuth, 
  signInWithEmailAndPassword,
  signOut,
  onAuthStateChanged,
  User
} from "firebase/auth";
import {
  getFirestore,
  collection,
  addDoc,
  getDocs,
  query,
  where,
  Timestamp,
  updateDoc,
  setDoc,
  doc,
  onSnapshot,
  getDoc
} from "firebase/firestore";
import {
  getStorage,
  ref as storageRef,
  uploadBytes,
  getDownloadURL
} from "firebase/storage";

// Configuration Firebase
const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID
};

// Initialiser Firebase
const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
export const storage = getStorage(app);

// ==================== AUTHENTIFICATION ====================

const SESSION_KEY = "session";
let userStatusUnsub: (() => void) | null = null;
let userStatusUserId: string | null = null;

type SessionInfo = {
  userId: string;
  createdAt: number;
  expiresAt: number;
};

type SecurityConfig = {
  maxAttempts: number;
  sessionDurationSec: number;
};

const normalizeTimestamp = (value: any): Date | null => {
  if (!value) return null;
  if (value instanceof Timestamp) return value.toDate();
  if (value instanceof Date) return value;
  return null;
};

const normalizeStatus = (value: any): string => {
  if (!value) return "";
  return String(value)
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "");
};

const getStatusInfo = (data: Record<string, any>) => {
  const statusBlocageId =
    data.Id_status_blocage || data.statusBlocageId || data.status_blocage;
  const normalized = normalizeStatus(statusBlocageId);
  const isDebloque = normalized === "debloque";
  const isBlockedByStatus = normalized === "bloque";
  const rawIsBlocked = Boolean(data.isBlocked);

  return {
    statusBlocageId: normalized || statusBlocageId,
    isDebloque,
    isBlockedByStatus,
    rawIsBlocked
  };
};

const computeBlockedState = (data: Record<string, any>) => {
  const statusInfo = getStatusInfo(data);
  const isBlocked = statusInfo.isDebloque
    ? false
    : statusInfo.rawIsBlocked || statusInfo.isBlockedByStatus;

  return { ...statusInfo, isBlocked };
};

export const getSecurityConfig = async (): Promise<SecurityConfig> => {
  const configRef = doc(db, "config", "securite");
  const snapshot = await getDoc(configRef);
  const data = snapshot.exists() ? snapshot.data() : {};

  return {
    maxAttempts: Number(data?.nombre_tentative ?? 3),
    sessionDurationSec: Number(data?.duree_session ?? 3600)
  };
};

export const findUserByEmail = async (email: string) => {
  const q = query(collection(db, "users"), where("email", "==", email));
  const snapshot = await getDocs(q);

  if (snapshot.empty) return null;

  const first = snapshot.docs[0];
  return { id: first.id, ...first.data() } as any;
};

/**
 * Connexion avec email et mot de passe
 */
export const loginUser = async (email: string, password: string) => {
  try {
    const userCredential = await signInWithEmailAndPassword(auth, email, password);
    return {
      user: userCredential.user,
      token: await userCredential.user.getIdToken()
    };
  } catch (error) {
    throw error;
  }
};

/**
 * Déconnexion
 */
export const logoutUser = async () => {
  try {
    await signOut(auth);
  } catch (error) {
    throw error;
  }
};

/**
 * Observer les changements d'authentification
 */
export const onAuthChange = (callback: (user: User | null) => void) => {
  return onAuthStateChanged(auth, callback);
};

export const setSession = (userId: string, durationSec: number) => {
  const createdAt = Date.now();
  const expiresAt = createdAt + durationSec * 1000;
  const payload: SessionInfo = { userId, createdAt, expiresAt };
  localStorage.setItem(SESSION_KEY, JSON.stringify(payload));
};

export const getSession = (): SessionInfo | null => {
  const raw = localStorage.getItem(SESSION_KEY);
  if (!raw) return null;

  try {
    return JSON.parse(raw) as SessionInfo;
  } catch {
    return null;
  }
};

export const isSessionValid = () => {
  const session = getSession();
  if (!session) return false;
  return Date.now() < session.expiresAt;
};

export const clearSession = () => {
  stopUserStatusListener();
  localStorage.removeItem(SESSION_KEY);
  localStorage.removeItem("user");
};

export const startUserStatusListener = (userId: string) => {
  if (userStatusUnsub && userStatusUserId === userId) return;

  stopUserStatusListener();
  userStatusUserId = userId;

  const userDocRef = doc(db, "users", userId);
  userStatusUnsub = onSnapshot(userDocRef, async (snapshot) => {
    if (!snapshot.exists()) return;

    const data = snapshot.data();
    const { isBlocked, isDebloque, rawIsBlocked } = computeBlockedState(data);
    const loginAttempts = data.loginAttempts || 0;

    if (isDebloque && (rawIsBlocked || loginAttempts > 0)) {
      await updateDoc(userDocRef, {
        isBlocked: false,
        loginAttempts: 0,
        blockedUntil: null,
        updatedAt: Timestamp.now()
      });
      window.dispatchEvent(new CustomEvent("user-unblocked"));
    }
  });
};

export const stopUserStatusListener = () => {
  if (userStatusUnsub) {
    userStatusUnsub();
    userStatusUnsub = null;
    userStatusUserId = null;
  }
};

export const syncUserProfile = async (user: User) => {
  const userDocRef = doc(db, "users", user.uid);
  const snapshot = await getDoc(userDocRef);
  const now = Timestamp.now();
  const email = user.email || "";
  const nom = user.displayName || (email ? email.split("@")[0] : "utilisateur");

  if (!snapshot.exists()) {
    await setDoc(userDocRef, {
      uid: user.uid,
      email,
      nom,
      Id_role: "manager",
      Id_status_blocage: "debloque",
      loginAttempts: 0,
      isBlocked: false,
      blockedUntil: null,
      createdAt: now,
      updatedAt: now
    });
    return;
  }

  await updateDoc(userDocRef, {
    email,
    nom,
    loginAttempts: 0,
    isBlocked: false,
    blockedUntil: null,
    updatedAt: now
  });
};

/**
 * Vérifier et gérer le blocage de l'utilisateur
 */
export const checkUserBlockStatus = async (userId: string) => {
  try {
    const userDocRef = doc(db, "users", userId);
    const userDoc = await getDoc(userDocRef);
    
    if (userDoc.exists()) {
      const userData = userDoc.data();
      const { isBlocked } = computeBlockedState(userData);
      return {
        isBlocked,
        blockedUntil: userData.blockedUntil || null,
        loginAttempts: userData.loginAttempts || 0
      };
    }
    
    return {
      isBlocked: false,
      blockedUntil: null,
      loginAttempts: 0
    };
  } catch (error) {
    console.error("Erreur vérification blocage:", error);
    throw error;
  }
};

export const checkUserBlockStatusByEmail = async (email: string) => {
  const userData = await findUserByEmail(email);

  if (!userData) {
    return {
      exists: false,
      userId: null,
      isBlocked: false,
      blockedUntil: null,
      loginAttempts: 0
    };
  }

  const { isBlocked } = computeBlockedState(userData);
  const loginAttempts = userData.loginAttempts || 0;

  return {
    exists: true,
    userId: userData.id,
    isBlocked,
    blockedUntil: normalizeTimestamp(userData.blockedUntil),
    loginAttempts: loginAttempts
  };
};

/**
 * Incrémenter le compteur de tentatives échouées
 */
export const incrementLoginAttempts = async (userId: string) => {
  try {
    const { maxAttempts } = await getSecurityConfig();
    const userDocRef = doc(db, "users", userId);
    const userDoc = await getDoc(userDocRef);
    
    let attempts = 1;
    if (userDoc.exists()) {
      attempts = (userDoc.data().loginAttempts || 0) + 1;
    }
    
    const isBlocked = attempts >= maxAttempts;
    
    await setDoc(userDocRef, {
      loginAttempts: attempts,
      isBlocked: isBlocked,
      blockedUntil: isBlocked ? Timestamp.now().toDate() : null,
      lastFailedLogin: Timestamp.now()
    }, { merge: true });
    
    return { attempts, isBlocked };
  } catch (error) {
    console.error("Erreur incrémentation tentatives:", error);
    throw error;
  }
};

export const incrementLoginAttemptsByEmail = async (email: string) => {
  const { maxAttempts } = await getSecurityConfig();
  const userData = await findUserByEmail(email);

  if (!userData) {
    return { attempts: 0, isBlocked: false, maxAttempts };
  }

  const attempts = (userData.loginAttempts || 0) + 1;
  const isBlocked = attempts >= maxAttempts;
  const userDocRef = doc(db, "users", userData.id);

  await updateDoc(userDocRef, {
    loginAttempts: attempts,
    isBlocked: isBlocked,
    Id_status_blocage: isBlocked ? "bloque" : userData.Id_status_blocage || "debloque",
    blockedUntil: isBlocked ? Timestamp.now().toDate() : null,
    lastFailedLogin: Timestamp.now()
  });

  return { attempts, isBlocked, maxAttempts };
};

/**
 * Réinitialiser le compteur de tentatives
 */
export const resetLoginAttempts = async (userId: string) => {
  try {
    const userDocRef = doc(db, "users", userId);
    await setDoc(userDocRef, {
      loginAttempts: 0,
      isBlocked: false,
      blockedUntil: null,
      lastSuccessfulLogin: Timestamp.now()
    }, { merge: true });
  } catch (error) {
    console.error("Erreur réinitialisation tentatives:", error);
    throw error;
  }
};

// ==================== SIGNALEMENTS ====================

/**
 * Ajouter un nouveau signalement
 */
export const addSignalement = async (signalementData: {
  latitude: number;
  longitude: number;
  quartier: string;
  entreprise: string;
  surface: string;
  budget: string;
  description: string;
  userId: string;
  userEmail: string;
}) => {
  try {
    const docRef = await addDoc(collection(db, "signalements"), {
      ...signalementData,
      status: "nouveau",
      photos: [],
      dateCreation: Timestamp.now(),
      dateModification: Timestamp.now()
    });
    return docRef.id;
  } catch (error) {
    console.error("Erreur lors de l'ajout du signalement:", error);
    throw error;
  }
};

/**
 * Récupérer tous les signalements
 */
export const getAllSignalements = async () => {
  try {
    const querySnapshot = await getDocs(collection(db, "signalements"));
    const signalements: any[] = [];
    
    querySnapshot.forEach((doc) => {
      signalements.push({
        id: doc.id,
        ...doc.data()
      });
    });
    
    return signalements;
  } catch (error) {
    console.error("Erreur lors de la récupération des signalements:", error);
    throw error;
  }
};

/**
 * Récupérer les signalements de l'utilisateur
 */
export const getUserSignalements = async (userId: string) => {
  try {
    const q = query(
      collection(db, "signalements"),
      where("userId", "==", userId)
    );
    
    const querySnapshot = await getDocs(q);
    const signalements: any[] = [];
    
    querySnapshot.forEach((doc) => {
      signalements.push({
        id: doc.id,
        ...doc.data()
      });
    });
    
    return signalements;
  } catch (error) {
    console.error("Erreur lors de la récupération des signalements utilisateur:", error);
    throw error;
  }
};

/**
 * Mettre à jour le statut d'un signalement
 */
export const updateSignalementStatus = async (
  signalementId: string,
  newStatus: string
) => {
  try {
    const docRef = doc(db, "signalements", signalementId);
    await updateDoc(docRef, {
      status: newStatus,
      dateModification: Timestamp.now()
    });
  } catch (error) {
    console.error("Erreur lors de la mise à jour du signalement:", error);
    throw error;
  }
};

/**
 * Écouter les changements d'un signalement en temps réel
 */
export const onSignalementChange = (
  signalementId: string,
  callback: (data: any) => void,
  onError?: (error: any) => void
) => {
  const docRef = doc(db, "signalements", signalementId);
  return onSnapshot(docRef, (doc) => {
    if (doc.exists()) {
      callback({
        id: doc.id,
        ...doc.data()
      });
    }
  }, (error) => {
    if (onError) onError(error);
  });
};

/**
 * Écouter tous les signalements en temps réel
 */
export const onAllSignalementsChange = (
  callback: (signalements: any[]) => void,
  onError?: (error: any) => void
) => {
  return onSnapshot(collection(db, "signalements"), (snapshot) => {
    const signalements: any[] = [];
    snapshot.forEach((doc) => {
      signalements.push({
        id: doc.id,
        ...doc.data()
      });
    });
    callback(signalements);
  }, (error) => {
    if (onError) onError(error);
  });
};

// ==================== PHOTOS ====================

/**
 * Uploader une photo vers Firebase Storage
 */
export const uploadPhoto = async (
  signalementId: string,
  photoFile: File,
  userId: string
): Promise<string> => {
  try {
    const fileName = `signalements/${userId}/${signalementId}/${Date.now()}_${photoFile.name}`;
    const fileRef = storageRef(storage, fileName);
    
    // Uploader le fichier
    await uploadBytes(fileRef, photoFile);
    
    // Récupérer l'URL de téléchargement
    const downloadURL = await getDownloadURL(fileRef);
    return downloadURL;
  } catch (error) {
    console.error("Erreur lors du téléchargement de la photo:", error);
    throw error;
  }
};

/**
 * Ajouter une URL de photo à un signalement
 */
export const addPhotoToSignalement = async (
  signalementId: string,
  photoURL: string
) => {
  try {
    const docRef = doc(db, "signalements", signalementId);
    const docSnapshot = await getDoc(docRef);
    
    if (docSnapshot.exists()) {
      const currentPhotos = docSnapshot.data().photos || [];
      await updateDoc(docRef, {
        photos: [...currentPhotos, {
          url: photoURL,
          dateUpload: Timestamp.now()
        }],
        dateModification: Timestamp.now()
      });
    }
  } catch (error) {
    console.error("Erreur lors de l'ajout de la photo:", error);
    throw error;
  }
};

// ==================== DONNÉES DE RÉFÉRENCE ====================

/**
 * Récupérer les quartiers
 */
export const getQuartiers = async () => {
  try {
    const querySnapshot = await getDocs(collection(db, "quartiers"));
    const quartiers: any[] = [];
    
    querySnapshot.forEach((doc) => {
      quartiers.push({
        id: doc.id,
        ...doc.data()
      });
    });
    
    return quartiers;
  } catch (error) {
    console.error("Erreur lors de la récupération des quartiers:", error);
    throw error;
  }
};

/**
 * Récupérer les entreprises
 */
export const getEntreprises = async () => {
  try {
    const querySnapshot = await getDocs(collection(db, "entreprises"));
    const entreprises: any[] = [];
    
    querySnapshot.forEach((doc) => {
      entreprises.push({
        id: doc.id,
        ...doc.data()
      });
    });
    
    return entreprises;
  } catch (error) {
    console.error("Erreur lors de la récupération des entreprises:", error);
    throw error;
  }
};

/**
 * Récupérer les statuts
 */
export const getStatuses = async () => {
  try {
    const querySnapshot = await getDocs(collection(db, "statuses"));
    const statuses: any[] = [];
    
    querySnapshot.forEach((doc) => {
      statuses.push({
        id: doc.id,
        ...doc.data()
      });
    });
    
    return statuses;
  } catch (error) {
    console.error("Erreur lors de la récupération des statuts:", error);
    throw error;
  }
};

// ==================== UTILITAIRES ====================

/**
 * Calculer les statistiques des signalements
 */
export const calculateStatistics = (signalements: any[]) => {
  return {
    totalPoints: signalements.length,
    totalSurface: signalements.reduce((sum, s) => sum + (parseFloat(s.surface) || 0), 0),
    totalBudget: signalements.reduce((sum, s) => sum + (parseFloat(s.budget) || 0), 0),
    byStatus: signalements.reduce((acc, s) => {
      const status = s.status || 'inconnu';
      acc[status] = (acc[status] || 0) + 1;
      return acc;
    }, {} as Record<string, number>),
    avancement: calculateAvancement(signalements)
  };
};

/**
 * Calculer le pourcentage d'avancement
 */
export const calculateAvancement = (signalements: any[]): number => {
  if (signalements.length === 0) return 0;
  
  const finished = signalements.filter(s => s.status === 'terminé').length;
  return Math.round((finished / signalements.length) * 100);
};
