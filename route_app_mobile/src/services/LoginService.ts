import { signInWithEmailAndPassword, signOut, onAuthStateChanged, User } from 'firebase/auth';
import {
  collection,
  doc,
  getDoc,
  getDocs,
  onSnapshot,
  query,
  setDoc,
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
  if (!value) return '';
  return String(value)
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '');
};

const getStatusInfo = (data: Record<string, any>) => {
  const statusBlocageId =
    data.Id_status_blocage || data.statusBlocageId || data.status_blocage;
  const normalized = normalizeStatus(statusBlocageId);
  const isDebloque = normalized === 'debloque';
  const isBlockedByStatus = normalized === 'bloque';
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
  const configRef = doc(db, 'config', 'securite');
  const snapshot = await getDoc(configRef);
  const data = snapshot.exists() ? snapshot.data() : {};

  return {
    maxAttempts: Number(data?.nombre_tentative ?? 3),
    sessionDurationSec: Number(data?.duree_session ?? 3600)
  };
};

export const findUserByEmail = async (email: string) => {
  const q = query(collection(db, 'users'), where('email', '==', email));
  const snapshot = await getDocs(q);

  if (snapshot.empty) return null;

  const first = snapshot.docs[0];
  return { id: first.id, ...first.data() } as any;
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
  localStorage.removeItem('user');
};

export const startUserStatusListener = (userId: string) => {
  if (userStatusUnsub && userStatusUserId === userId) return;

  stopUserStatusListener();
  userStatusUserId = userId;

  const userDocRef = doc(db, 'users', userId);
  userStatusUnsub = onSnapshot(userDocRef, async (snapshot) => {
    if (!snapshot.exists()) return;

    const data = snapshot.data();
    const { isDebloque, rawIsBlocked } = computeBlockedState(data);
    const loginAttempts = data.loginAttempts || 0;

    if (isDebloque && (rawIsBlocked || loginAttempts > 0)) {
      await updateDoc(userDocRef, {
        isBlocked: false,
        loginAttempts: 0,
        blockedUntil: null,
        updatedAt: Timestamp.now()
      });
      window.dispatchEvent(new CustomEvent('user-unblocked'));
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
  const userDocRef = doc(db, 'users', user.uid);
  const snapshot = await getDoc(userDocRef);
  const now = Timestamp.now();
  const email = user.email || '';
  const nom = user.displayName || (email ? email.split('@')[0] : 'utilisateur');

  if (!snapshot.exists()) {
    await setDoc(userDocRef, {
      uid: user.uid,
      email,
      nom,
      Id_role: 'manager',
      Id_status_blocage: 'debloque',
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

export const checkUserBlockStatus = async (userId: string) => {
  const userDocRef = doc(db, 'users', userId);
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

  const { isBlocked, isDebloque, rawIsBlocked } = computeBlockedState(userData);
  const loginAttempts = userData.loginAttempts || 0;

  if (isDebloque && (rawIsBlocked || userData.blockedUntil)) {
    try {
      await updateDoc(doc(db, 'users', userData.id), {
        isBlocked: false,
        loginAttempts: 0,
        blockedUntil: null,
        updatedAt: Timestamp.now()
      });
      window.dispatchEvent(new CustomEvent('user-unblocked'));
    } catch {
      // Ignore reset failures during pre-check.
    }
  }

  return {
    exists: true,
    userId: userData.id,
    isBlocked,
    blockedUntil: normalizeTimestamp(userData.blockedUntil),
    loginAttempts: loginAttempts
  };
};

export const incrementLoginAttempts = async (userId: string) => {
  const { maxAttempts } = await getSecurityConfig();
  const userDocRef = doc(db, 'users', userId);
  const userDoc = await getDoc(userDocRef);

  let attempts = 1;
  if (userDoc.exists()) {
    attempts = (userDoc.data().loginAttempts || 0) + 1;
  }

  const isBlocked = attempts >= maxAttempts;

  await setDoc(
    userDocRef,
    {
      loginAttempts: attempts,
      isBlocked: isBlocked,
      blockedUntil: isBlocked ? Timestamp.now().toDate() : null,
      lastFailedLogin: Timestamp.now()
    },
    { merge: true }
  );

  return { attempts, isBlocked };
};

export const incrementLoginAttemptsByEmail = async (email: string) => {
  const { maxAttempts } = await getSecurityConfig();
  const userData = await findUserByEmail(email);

  if (!userData) {
    return { attempts: 0, isBlocked: false, maxAttempts };
  }

  const attempts = (userData.loginAttempts || 0) + 1;
  const isBlocked = attempts >= maxAttempts;
  const userDocRef = doc(db, 'users', userData.id);

  await updateDoc(userDocRef, {
    loginAttempts: attempts,
    isBlocked: isBlocked,
    Id_status_blocage: isBlocked ? 'bloque' : userData.Id_status_blocage || 'debloque',
    blockedUntil: isBlocked ? Timestamp.now().toDate() : null,
    lastFailedLogin: Timestamp.now()
  });

  return { attempts, isBlocked, maxAttempts };
};

export const resetLoginAttempts = async (userId: string) => {
  const userDocRef = doc(db, 'users', userId);
  await setDoc(
    userDocRef,
    {
      loginAttempts: 0,
      isBlocked: false,
      blockedUntil: null,
      lastSuccessfulLogin: Timestamp.now()
    },
    { merge: true }
  );
};

export const loginWithEmail = async (
  email: string,
  password: string
): Promise<LoginResult> => {
  const preStatus = await checkUserBlockStatusByEmail(email);
  if (preStatus.isBlocked) {
    return {
      status: 'blocked',
      message: 'Compte bloque. Contactez un administrateur.'
    };
  }

  try {
    const { user } = await loginUser(email, password);
    await syncUserProfile(user);
    const status = await checkUserBlockStatus(user.uid);

    if (status.isBlocked) {
      await logoutUser();
      return {
        status: 'blocked',
        message: 'Compte bloque. Contactez un administrateur.'
      };
    }

    const securityConfig = await getSecurityConfig();
    setSession(user.uid, securityConfig.sessionDurationSec);
    startUserStatusListener(user.uid);

    localStorage.setItem('user', JSON.stringify({
      uid: user.uid,
      email: user.email
    }));

    return { status: 'success', message: 'Connexion reussie.' };
  } catch (error) {
    try {
      const result = await incrementLoginAttemptsByEmail(email);
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
    } catch {
      // Ignore attempt tracking errors and show generic message.
    }

    return {
      status: 'failed',
      message: 'Connexion echouee. Verifiez vos identifiants.'
    };
  }
};
