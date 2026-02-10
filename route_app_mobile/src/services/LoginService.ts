import { signInWithEmailAndPassword, signOut, onAuthStateChanged, User } from 'firebase/auth';
import {
  addDoc,
  collection,
  doc,
  getDoc,
  getDocs,
  onSnapshot,
  query,
  Timestamp,
  updateDoc,
  where
} from 'firebase/firestore';
import { auth, db } from './firebaseService';

type LoginResult = {
  status: 'success' | 'blocked' | 'failed';
  message: string;
};

const SESSION_KEY = 'session';
let userStatusUnsub: (() => void) | null = null;
let userStatusUserId: number | null = null;

type SessionInfo = {
  userId: string;
  idUser?: number;
  createdAt: number;
  expiresAt: number;
};

type SecurityConfig = {
  maxAttempts: number;
  sessionDurationSec: number;
};

type UserRecord = {
  docId: string;
  idUser: number;
  email: string;
  data: Record<string, any>;
};

type StatusBlocageCache = {
  byId: Map<number, string>;
  byLabel: Map<string, number>;
};

let statusBlocageCache: StatusBlocageCache | null = null;

const normalizeTimestamp = (value: any): Date | null => {
  if (!value) return null;
  if (value instanceof Timestamp) return value.toDate();
  if (value instanceof Date) return value;
  return null;
};

const normalizeStatus = (value: any): string => {
  if (!value) return '';
  return String(value)
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '');
};

const getLoginAttempts = (data: Record<string, any>) => {
  const raw =
    data?.tentatives_echouees ??
    data?.loginAttempts ??
    data?.login_attempts ??
    data?.tentatives ??
    0;
  return Number(raw) || 0;
};

const normalizeUserRecord = (docSnap: any): UserRecord => {
  const data = docSnap.data() || {};
  const idUser = Number(data.id_user ?? data.Id_user ?? data.idUser ?? 0);
  return {
    docId: docSnap.id,
    idUser,
    email: String(data.email || ''),
    data
  };
};

const getStatusBlocageCache = async (): Promise<StatusBlocageCache> => {
  if (statusBlocageCache) return statusBlocageCache;

  const snapshot = await getDocs(collection(db, 'status_blocage'));
  const byId = new Map<number, string>();
  const byLabel = new Map<string, number>();

  snapshot.forEach((docSnap) => {
    const data = docSnap.data() as any;
    const rawId = data.id_status_blocage ?? data.Id_status_blocage ?? data.id_status;
    const id = Number(rawId);
    if (!Number.isFinite(id)) return;

    const label = data.status || data.libelle || data.label || docSnap.id;
    const normalized = normalizeStatus(label);
    byId.set(id, normalized);
    if (normalized) {
      byLabel.set(normalized, id);
    }
  });

  statusBlocageCache = { byId, byLabel };
  return statusBlocageCache;
};

const getStatusBlocageId = async (label: string) => {
  const cache = await getStatusBlocageCache();
  const normalized = normalizeStatus(label);
  return cache.byLabel.get(normalized) ?? null;
};

const getStatusBlocageLabel = async (idStatus: number) => {
  const cache = await getStatusBlocageCache();
  return cache.byId.get(idStatus) || '';
};

const getLatestBlocageForUser = async (idUser: number) => {
  const q = query(
    collection(db, 'historique_blocage'),
    where('id_user', '==', idUser)
  );
  const snapshot = await getDocs(q);
  if (snapshot.empty) return null;

  // Sort client-side to avoid composite index requirement
  const sorted = snapshot.docs
    .map((d) => d.data() as any)
    .sort((a, b) => {
      const da = normalizeTimestamp(a.date_ || a.date);
      const db2 = normalizeTimestamp(b.date_ || b.date);
      return (db2?.getTime() ?? 0) - (da?.getTime() ?? 0);
    });

  const data = sorted[0];
  const idStatus = Number(
    data.id_status_blocage ?? data.Id_status_blocage ?? data.status_blocage ?? 0
  );

  return {
    idStatus,
    date: normalizeTimestamp(data.date_ || data.date)
  };
};

const getUserBlockState = async (idUser: number) => {
  const latest = await getLatestBlocageForUser(idUser);
  if (!latest?.idStatus) {
    return {
      isBlocked: false,
      isDebloque: true,
      statusId: null,
      statusLabel: 'debloque',
      blockedUntil: null
    };
  }

  const statusLabel = await getStatusBlocageLabel(latest.idStatus);
  const normalized = normalizeStatus(statusLabel);

  return {
    isBlocked: normalized === 'bloque',
    isDebloque: normalized === 'debloque',
    statusId: latest.idStatus,
    statusLabel: normalized,
    blockedUntil: latest.date
  };
};

export const getSecurityConfig = async (): Promise<SecurityConfig> => {
  const configRef = doc(db, 'parametre', 'parametre');
  const snapshot = await getDoc(configRef);
  const data = snapshot.exists() ? snapshot.data() : {};

  return {
    maxAttempts: Number(data?.nombre_tentative ?? 3),
    sessionDurationSec: Number(data?.session_vie ?? 3600)
  };
};

export const findUserByEmail = async (email: string) => {
  const q = query(collection(db, 'user_'), where('email', '==', email));
  const snapshot = await getDocs(q);

  if (snapshot.empty) return null;

  return normalizeUserRecord(snapshot.docs[0]);
};

const findUserByIdUser = async (idUser: number) => {
  const q = query(collection(db, 'user_'), where('id_user', '==', idUser));
  const snapshot = await getDocs(q);

  if (snapshot.empty) return null;

  return normalizeUserRecord(snapshot.docs[0]);
};

export const loginUser = async (email: string, password: string) => {
  const userCredential = await signInWithEmailAndPassword(auth, email, password);
  return {
    user: userCredential.user,
    token: await userCredential.user.getIdToken()
  };
};

export const logoutUser = async () => {
  await signOut(auth);
};

export const onAuthChange = (callback: (user: User | null) => void) => {
  return onAuthStateChanged(auth, callback);
};

export const setSession = (userId: string, durationSec: number, idUser?: number) => {
  const createdAt = Date.now();
  const expiresAt = createdAt + durationSec * 1000;
  const payload: SessionInfo = { userId, createdAt, expiresAt, idUser };
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
  localStorage.removeItem('user');
};

export const startUserStatusListener = (idUser: number) => {
  if (!idUser) return;
  if (userStatusUnsub && userStatusUserId === idUser) return;

  stopUserStatusListener();
  userStatusUserId = idUser;

  let isFirstSnapshot = true;
  const q = query(collection(db, 'historique_blocage'), where('id_user', '==', idUser));
  userStatusUnsub = onSnapshot(q, async () => {
    // Skip the initial snapshot to avoid false "unblocked" events
    if (isFirstSnapshot) {
      isFirstSnapshot = false;
      return;
    }

    const status = await getUserBlockState(idUser);
    // Only reset if there's in actual debloque entry (statusId !== null)
    if (status.isDebloque && status.statusId !== null) {
      const userDoc = await findUserByIdUser(idUser);
      if (userDoc && getLoginAttempts(userDoc.data) > 0) {
        await updateDoc(doc(db, 'user_', userDoc.docId), {
          tentatives_echouees: 0,
          last_sync: Timestamp.now()
        });
        window.dispatchEvent(new CustomEvent('user-unblocked'));
      }
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
  const email = user.email || '';
  const nom = user.displayName || (email ? email.split('@')[0] : 'utilisateur');
  const existing = await findUserByEmail(email);
  const now = Timestamp.now();

  if (!existing) {
    const rolesSnap = await getDocs(query(collection(db, 'role'), where('libelle', '==', 'utilisateur')));
    const roleDoc = rolesSnap.docs[0];
    const roleData = roleDoc?.data() as any;
    const roleId = Number(roleData?.id_role ?? roleData?.Id_role ?? 0) || 0;

    await addDoc(collection(db, 'user_'), {
      id_user: Date.now(),
      email,
      firebase_uid: user.uid,
      id_role: roleId,
      nom,
      prenom: '',
      source: 'mobile',
      synced: true,
      last_sync: now,
      tentatives_echouees: 0
    });

    return;
  }

  await updateDoc(doc(db, 'user_', existing.docId), {
    email,
    firebase_uid: user.uid,
    nom,
    last_sync: now
  });
};

export const checkUserBlockStatus = async (idUser: number) => {
  if (!idUser) {
    return {
      isBlocked: false,
      blockedUntil: null,
      loginAttempts: 0
    };
  }

  const status = await getUserBlockState(idUser);
  const userDoc = await findUserByIdUser(idUser);

  return {
    isBlocked: status.isBlocked,
    blockedUntil: status.blockedUntil,
    loginAttempts: userDoc ? getLoginAttempts(userDoc.data) : 0
  };
};

export const checkUserBlockStatusByEmail = async (email: string) => {
  const userData = await findUserByEmail(email);

  if (!userData) {
    return {
      exists: false,
      userId: null as number | null,
      docId: null as string | null,
      isBlocked: false,
      blockedUntil: null,
      loginAttempts: 0
    };
  }

  const status = await getUserBlockState(userData.idUser);
  // Re-read fresh data for accurate attempt count
  const freshUserDoc = await getDoc(doc(db, 'user_', userData.docId));
  const freshData = freshUserDoc.exists() ? freshUserDoc.data() : userData.data;
  const attempts = getLoginAttempts(freshData as Record<string, any>);

  const { maxAttempts } = await getSecurityConfig();

  console.log(`[LoginService] Pre-check: attempts=${attempts}, isBlocked=${status.isBlocked}, isDebloque=${status.isDebloque}, statusId=${status.statusId}, maxAttempts=${maxAttempts}`);

  // Only reset attempts if user was ACTUALLY blocked (attempts >= maxAttempts)
  // AND an admin has inserted a "debloque" entry in historique_blocage.
  // This prevents the debloque entry from resetting attempts on every login.
  if (status.isDebloque && status.statusId !== null && attempts >= maxAttempts) {
    try {
      await updateDoc(doc(db, 'user_', userData.docId), {
        tentatives_echouees: 0,
        last_sync: Timestamp.now()
      });
      console.log(`[LoginService] Pre-check: reset attempts from ${attempts} to 0 (user was unblocked)`);
      window.dispatchEvent(new CustomEvent('user-unblocked'));
    } catch {
      // Ignore reset failures during pre-check.
    }

    return {
      exists: true,
      userId: userData.idUser,
      docId: userData.docId,
      isBlocked: false,
      blockedUntil: null,
      loginAttempts: 0
    };
  }

  return {
    exists: true,
    userId: userData.idUser,
    docId: userData.docId,
    isBlocked: status.isBlocked,
    blockedUntil: status.blockedUntil,
    loginAttempts: attempts
  };
};

export const incrementLoginAttemptsByEmail = async (email: string) => {
  const { maxAttempts } = await getSecurityConfig();
  const userData = await findUserByEmail(email);

  if (!userData) {
    return { attempts: 0, isBlocked: false, maxAttempts };
  }

  // Re-read fresh data to avoid stale counts
  const freshDoc = await getDoc(doc(db, 'user_', userData.docId));
  const freshData = freshDoc.exists() ? freshDoc.data() : userData.data;
  const currentAttempts = getLoginAttempts(freshData);
  const attempts = currentAttempts + 1;
  const isBlocked = attempts >= maxAttempts;
  const userDocRef = doc(db, 'user_', userData.docId);

  console.log(`[LoginService] Increment: ${currentAttempts} -> ${attempts} / max=${maxAttempts}, blocked=${isBlocked}`);

  await updateDoc(userDocRef, {
    tentatives_echouees: attempts,
    last_sync: Timestamp.now()
  });

  if (isBlocked) {
    const bloqueId = await getStatusBlocageId('bloque');
    await addDoc(collection(db, 'historique_blocage'), {
      id_historique_blocage: Date.now(),
      id_status_blocage: bloqueId ?? 0,
      id_user: userData.idUser,
      date_: Timestamp.now()
    });
  }

  return { attempts, isBlocked, maxAttempts };
};

export const resetLoginAttempts = async (docId: string) => {
  const userDocRef = doc(db, 'user_', docId);
  await updateDoc(userDocRef, {
    tentatives_echouees: 0,
    last_sync: Timestamp.now()
  });
};

export const loginWithEmail = async (
  email: string,
  password: string
): Promise<LoginResult> => {
  let preStatus: {
    exists: boolean;
    userId: number | null;
    docId: string | null;
    isBlocked: boolean;
    blockedUntil: Date | null;
    loginAttempts: number;
  };

  try {
    preStatus = await checkUserBlockStatusByEmail(email);
    if (preStatus.isBlocked) {
      return {
        status: 'blocked',
        message: 'Compte bloque. Contactez un administrateur.'
      };
    }
  } catch (error) {
    const errorCode = (error as any)?.code || '';
    if (errorCode === 'permission-denied') {
      return {
        status: 'failed',
        message: 'Acces Firestore refuse. Verifiez les rules.'
      };
    }
    throw error;
  }

  try {
    const { user } = await loginUser(email, password);
    await syncUserProfile(user);

    const refreshed = await findUserByEmail(email);
    const idUser = refreshed?.idUser || preStatus.userId || 0;
    const status = await checkUserBlockStatus(idUser);

    if (status.isBlocked) {
      await logoutUser();
      return {
        status: 'blocked',
        message: 'Compte bloque. Contactez un administrateur.'
      };
    }

    const securityConfig = await getSecurityConfig();
    setSession(user.uid, securityConfig.sessionDurationSec, idUser || undefined);
    if (idUser) {
      startUserStatusListener(idUser);
    }

    localStorage.setItem('user', JSON.stringify({
      uid: user.uid,
      email: user.email,
      id_user: idUser
    }));

    if (refreshed?.docId) {
      await resetLoginAttempts(refreshed.docId);
    }

    return { status: 'success', message: 'Connexion reussie.' };
  } catch (error) {
    console.error('Erreur Firebase Auth:', error);
    const errorCode = (error as any)?.code || '';

    // auth/too-many-requests = Firebase Auth server-side rate limiting
    // This is NOT a wrong password — do NOT increment attempts
    if (errorCode === 'auth/too-many-requests') {
      console.warn('[LoginService] Firebase Auth rate limit active — not counting as failed attempt');
      return {
        status: 'failed',
        message: 'Trop de tentatives sur ce compte. Veuillez patienter quelques minutes avant de reessayer.'
      };
    }

    if (errorCode === 'permission-denied') {
      return {
        status: 'failed',
        message: 'Acces Firestore refuse. Verifiez les rules.'
      };
    }

    // Only increment attempts for actual auth failures (wrong password, etc.)
    try {
      const result = await incrementLoginAttemptsByEmail(email);
      console.log('Tentatives:', result.attempts, '/', result.maxAttempts, 'Bloque:', result.isBlocked);
      const remaining = result.maxAttempts - result.attempts;

      if (result.isBlocked) {
        return {
          status: 'blocked',
          message: 'Compte bloque. Trop de tentatives.'
        };
      }

      if (remaining > 0 && result.attempts > 0) {
        return {
          status: 'failed',
          message: `Connexion echouee. Tentatives restantes: ${remaining}.`
        };
      }
    } catch (incrementError) {
      console.error('Erreur increment tentatives:', incrementError);
    }

    const suffix = errorCode ? ` (${errorCode})` : '';
    return {
      status: 'failed',
      message: `Connexion echouee. Verifiez vos identifiants.${suffix}`
    };
  }
};
