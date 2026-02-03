<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Connexion</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <div class="login-container">
        <ion-img 
          src="/assets/logo.png" 
          alt="Logo"
          class="logo"
        ></ion-img>

        <ion-card>
          <ion-card-content>
            <form @submit.prevent="login">
              <ion-list>
                <!-- Email Input -->
                <ion-item>
                  <ion-label position="floating">Email</ion-label>
                  <ion-input
                    v-model="email"
                    type="email"
                    required
                    :disabled="loading"
                  ></ion-input>
                </ion-item>

                <!-- Password Input -->
                <ion-item>
                  <ion-label position="floating">Mot de passe</ion-label>
                  <ion-input
                    v-model="password"
                    type="password"
                    required
                    :disabled="loading"
                  ></ion-input>
                </ion-item>
              </ion-list>

              <!-- Error Message -->
              <ion-text v-if="errorMessage" color="danger" class="error-message">
                <p>{{ errorMessage }}</p>
              </ion-text>

              <!-- Success Message -->
              <ion-text v-if="successMessage" color="success" class="success-message">
                <p>{{ successMessage }}</p>
              </ion-text>

              <!-- Login Button -->
              <ion-button
                expand="block"
                type="submit"
                :disabled="loading || !email || !password"
                class="ion-margin-top"
              >
                <ion-spinner v-if="loading" name="crescent"></ion-spinner>
                <span v-else>Se connecter</span>
              </ion-button>
            
            </form>
          </ion-card-content>
        </ion-card>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonCard,
  IonCardContent,
  IonList,
  IonItem,
  IonLabel,
  IonInput,
  IonButton,
  IonSpinner,
  IonText,
  IonImg,
  toastController
} from '@ionic/vue';
import { 
  auth, 
  signInWithEmailAndPassword 
} from '@/config/firebase';
import axios from 'axios';

const router = useRouter();

// Data
const email = ref('test@example.com'); // Pré-rempli pour test
const password = ref('Test123!'); // Pré-rempli pour test
const loading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

// API configuration
const API_BASE_URL = 'http://localhost:8081/api';

// Fonction pour vérifier la config Firebase
const checkFirebaseConfig = () => {
  console.log('🔧 Vérification config Firebase:');
  console.log('API Key:', import.meta.env.VITE_FIREBASE_API_KEY?.substring(0, 10) + '...');
  console.log('Auth Domain:', import.meta.env.VITE_FIREBASE_AUTH_DOMAIN);
  console.log('Project ID:', import.meta.env.VITE_FIREBASE_PROJECT_ID);
  
  // Vérifier si la config est présente
  const hasConfig = import.meta.env.VITE_FIREBASE_API_KEY && 
                   import.meta.env.VITE_FIREBASE_API_KEY !== 'AIzaSyA...votre_clé';
  
  if (!hasConfig) {
    errorMessage.value = 'Configuration Firebase manquante. Vérifiez le fichier .env';
    return false;
  }
  
  return true;
};

// Login function - Version corrigée
const login = async () => {
  try {
    loading.value = true;
    errorMessage.value = '';
    successMessage.value = '';

    // 1. Vérifier la configuration Firebase
    if (!checkFirebaseConfig()) {
      loading.value = false;
      return;
    }

    console.log('🔐 Tentative de connexion avec:', {
      email: email.value,
      passwordLength: password.value.length
    });

    // 2. Firebase Authentication
    console.log('🔥 Tentative Firebase Auth...');
    const firebaseUser = await signInWithEmailAndPassword(
      auth, 
      email.value.trim(), // Trim pour enlever les espaces
      password.value
    );
    
    console.log('✅ Firebase Auth réussie!');
    console.log('UID:', firebaseUser.user.uid);
    console.log('Email:', firebaseUser.user.email);
    
    // 3. Récupérer le token
    const firebaseToken = await firebaseUser.user.getIdToken();
    console.log('🔑 Token obtenu (début):', firebaseToken.substring(0, 20) + '...');
    
    // 4. Vérifier l'utilisateur dans notre API
    console.log('🌐 Appel API backend...');
    const response = await axios.post(`${API_BASE_URL}/auth/login`, {
      email: email.value,
      password: firebaseToken // On envoie le token comme "password"
    });

    console.log('✅ Réponse API:', response.status, response.data);

    if (response.status === 200) {
      // 5. Stocker les informations
      const userData = response.data;
      
      localStorage.setItem('user', JSON.stringify(userData));
      localStorage.setItem('token', firebaseToken);
      localStorage.setItem('firebaseUid', firebaseUser.user.uid);
      
      // 6. Afficher message de succès
      await showToast('Connexion réussie ! Redirection...', 'success');
      
      // 7. Rediriger vers la carte
      setTimeout(() => {
        router.push('/map');
      }, 1000);
      
    } else {
      errorMessage.value = 'Échec de la connexion avec le backend';
    }
    
  } catch (error: any) {
    console.error('❌ ERREUR DÉTAILLÉE:', error);
    
    // Log supplémentaire pour Firebase
    if (error.code) {
      console.error('Code erreur Firebase:', error.code);
      console.error('Message Firebase:', error.message);
    }
    
    // Gestion des erreurs spécifiques
    if (error.code === 'auth/invalid-credential') {
      errorMessage.value = 'Email ou mot de passe incorrect. ';
      errorMessage.value += 'Vérifiez que l\'utilisateur existe dans Firebase Console.';
      
      // Aide supplémentaire
      console.warn('💡 ASTUCE: Vérifiez dans Firebase Console:');
      console.warn('1. Allez dans Authentication → Users');
      console.warn('2. Vérifiez si ' + email.value + ' existe');
      console.warn('3. Si non, créez-le manuellement');
      
    } else if (error.code === 'auth/user-not-found') {
      errorMessage.value = 'Aucun utilisateur trouvé avec cet email. ';
      errorMessage.value += 'Créez d\'abord l\'utilisateur dans Firebase Console.';
      
    } else if (error.code === 'auth/wrong-password') {
      errorMessage.value = 'Mot de passe incorrect. ';
      errorMessage.value += 'Réinitialisez-le dans Firebase Console si nécessaire.';
      
    } else if (error.code === 'auth/too-many-requests') {
      errorMessage.value = 'Trop de tentatives. Réessayez dans quelques minutes.';
      
    } else if (error.code === 'auth/network-request-failed') {
      errorMessage.value = 'Erreur réseau. Vérifiez votre connexion internet.';
      
    } else if (error.response) {
      // Erreur backend
      const status = error.response.status;
      const data = error.response.data;
      
      if (status === 404) {
        errorMessage.value = 'Utilisateur non trouvé dans notre base de données. ';
        errorMessage.value += 'Assurez-vous qu\'il est aussi dans PostgreSQL.';
      } else if (status === 403) {
        errorMessage.value = 'Compte bloqué: ' + data;
      } else {
        errorMessage.value = 'Erreur serveur: ' + (data || 'Veuillez réessayer');
      }
      
    } else {
      errorMessage.value = 'Erreur inattendue: ' + (error.message || 'Veuillez réessayer');
    }
  } finally {
    loading.value = false;
  }
};

// Toast helper
const showToast = async (message: string, color: string = 'success') => {
  const toast = await toastController.create({
    message,
    duration: 3000,
    color,
    position: 'top'
  });
  await toast.present();
};

// Test simple pour vérifier Firebase
const testFirebaseConnection = async () => {
  console.log('🧪 Test Firebase...');
  
  try {
    // Tentative avec des identifiants connus
    const testEmail = 'test@example.com';
    const testPassword = 'Test123!';
    
    console.log('Test avec:', testEmail);
    
    const result = await signInWithEmailAndPassword(auth, testEmail, testPassword);
    console.log('✅ Test Firebase réussi!');
    console.log('UID:', result.user.uid);
    
    return true;
  } catch (error: any) {
    console.error('❌ Test Firebase échoué:', error.code, error.message);
    return false;
  }
};



// Au chargement de la page
import { onMounted } from 'vue';
onMounted(() => {
  console.log('🚀 Page Login montée');
  
  // Vérifier la config Firebase
  checkFirebaseConfig();
  
  // Optionnel: Tester la connexion Firebase
  // testFirebaseConnection();
});
</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 0 auto;
  padding-top: 20px;
}

.logo {
  max-width: 150px;
  margin: 0 auto 30px;
  display: block;
}

.error-message {
  display: block;
  text-align: center;
  margin-top: 15px;
  padding: 10px;
  background-color: rgba(255, 0, 0, 0.1);
  border-radius: 5px;
}

.success-message {
  display: block;
  text-align: center;
  margin-top: 15px;
  padding: 10px;
  background-color: rgba(0, 255, 0, 0.1);
  border-radius: 5px;
}

ion-button {
  --border-radius: 8px;
  height: 50px;
  font-weight: bold;
}

ion-input {
  --padding-start: 0;
}
</style>