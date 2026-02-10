// src/firebase.js
import { initializeApp } from "firebase/app";
import { 
  getAuth, 
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signOut,
  onAuthStateChanged
} from "firebase/auth";

// Configuration Firebase
// Note: Vite uses VITE_ prefix for public environment variables
const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID
};

// Vérifier que la configuration est complète
console.log('Firebase Configuration Debug:');
console.log('API Key:', firebaseConfig.apiKey ? '✓ Loaded' : '✗ Missing');
console.log('Auth Domain:', firebaseConfig.authDomain ? '✓ Loaded' : '✗ Missing');
console.log('Project ID:', firebaseConfig.projectId ? '✓ Loaded' : '✗ Missing');
console.log('Storage Bucket:', firebaseConfig.storageBucket ? '✓ Loaded' : '✗ Missing');
console.log('Messaging Sender ID:', firebaseConfig.messagingSenderId ? '✓ Loaded' : '✗ Missing');
console.log('App ID:', firebaseConfig.appId ? '✓ Loaded' : '✗ Missing');

if (!firebaseConfig.apiKey || !firebaseConfig.authDomain || !firebaseConfig.projectId) {
  console.error('Firebase configuration incomplete! Check your .env file.');
}

// Initialiser Firebase
let app;
let auth;

try {
  app = initializeApp(firebaseConfig);
  auth = getAuth(app);
  console.log('Firebase initialized successfully');
} catch (error) {
  console.error('Firebase initialization error:', error);
}

// Export des fonctions
export { 
  auth, 
  signInWithEmailAndPassword, 
  createUserWithEmailAndPassword,
  signOut,
  onAuthStateChanged
};

// Fonction utilitaire pour vérifier la configuration
export const checkFirebaseConfig = () => {
  console.log('Firebase config vérifiée:');
  console.log('Projet:', firebaseConfig.projectId);
  console.log('Auth Domain:', firebaseConfig.authDomain);
  return firebaseConfig.projectId !== 'votre-projet';
};