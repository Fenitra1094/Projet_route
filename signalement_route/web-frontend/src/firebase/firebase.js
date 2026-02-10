import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";

const firebaseConfig = {
  apiKey: "AIzaSyBWbprADDZy5yThZlvP3sBMe5a3x-xrxa8",
  authDomain: "signalement-3b41e.firebaseapp.com",
  projectId: "signalement-3b41e",
  storageBucket: "signalement-3b41e.firebasestorage.app",
  messagingSenderId: "656546569338",
  appId: "1:656546569338:web:09e44a705aedcf48a26fc3"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
