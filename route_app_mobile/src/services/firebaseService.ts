// src/services/firebaseService.ts
import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import {
  getFirestore,
  collection,
  addDoc,
  getDocs,
  query,
  where,
  Timestamp,
  updateDoc,
  doc,
  onSnapshot,
  getDoc
} from "firebase/firestore";
import {
  getStorage,
  connectStorageEmulator,
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

if (import.meta.env.DEV) {
  connectStorageEmulator(storage, 'localhost', 9199);
}


// ==================== AUTHENTIFICATION ====================
// Login logic moved to LoginService.ts.

// ==================== SIGNALEMENTS ====================

/**
 * Ajouter un nouveau signalement
 */
export type SignalementPayload = {
  latitude: number;
  longitude: number;
  quartier?: string;
  province?: string;
  id_entreprise?: number | null;
  id_status?: number | null;
  surface: string;
  budget: string;
  date_?: string;
  id_user: number;
  user_email?: string;
};

export const addSignalement = async (signalementData: SignalementPayload) => {
  try {
    const idSignalement = Date.now();
    const docRef = await addDoc(collection(db, "signalement"), {
      id_signalement: idSignalement,
      ...signalementData,
      source: 'mobile',
      last_sync: Timestamp.now(),
      dateCreation: Timestamp.now(),
      dateModification: Timestamp.now()
    });
    await updateDoc(docRef, { firebase_doc_id: docRef.id });
    return { docId: docRef.id, idSignalement };
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
    const querySnapshot = await getDocs(collection(db, "signalement"));
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
      collection(db, "signalement"),
      where("id_user", "==", Number(userId))
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
    const docRef = doc(db, "signalement", signalementId);
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
  const docRef = doc(db, "signalement", signalementId);
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
  return onSnapshot(collection(db, "signalement"), (snapshot) => {
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

/**
 * Récupérer les photos d'un signalement depuis photo_signalement
 */
export const getPhotosForSignalement = async (idSignalement: number): Promise<string[]> => {
  try {
    const q = query(
      collection(db, 'photo_signalement'),
      where('id_signalement', '==', idSignalement)
    );
    const snapshot = await getDocs(q);
    return snapshot.docs
      .map(d => d.data().photo_signalement_url as string)
      .filter(Boolean);
  } catch (error) {
    console.error('Erreur récupération photos:', error);
    return [];
  }
};

// ==================== PHOTOS ==

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
  signalementDocId: string,
  idSignalement: number,
  photoURL: string
) => {
  try {
    // Stocker uniquement dans la collection photo_signalement
    await addDoc(collection(db, 'photo_signalement'), {
      id_photo_signalement: Date.now(),
      id_signalement: idSignalement,
      photo_signalement_url: photoURL
    });
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
