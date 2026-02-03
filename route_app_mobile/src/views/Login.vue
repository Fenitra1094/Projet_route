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
import { ref, onMounted } from 'vue';
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
  loginUser, 
  checkUserBlockStatus, 
  resetLoginAttempts, 
  incrementLoginAttempts 
} from '@/services/firebaseService';

const router = useRouter();

// Data
const email = ref('bemaso@gmail.com'); // Pré-rempli pour test
const password = ref('bemasooo'); // Pré-rempli pour test
const loading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

// Login function - Firebase uniquement
const login = async () => {
  try {
    loading.value = true;
    errorMessage.value = '';
    successMessage.value = '';

    console.log('🔐 Tentative de connexion avec:', email.value);

    // 1. Vérifier le blocage de l'utilisateur
    // (Nous vérifierons d'abord en essayant de se connecter)
    
    // 2. Appel Firebase Auth via firebaseService
    const result = await loginUser(email.value.trim(), password.value);
    
    if (!result.user || !result.token) {
      throw new Error('Authentification Firebase échouée');
    }

    console.log('✅ Firebase Auth réussie!');
    console.log('UID:', result.user.uid);
    console.log('Email:', result.user.email);
    
    // 3. Vérifier le blocage après login réussi
    const blockStatus = await checkUserBlockStatus(result.user.uid);
    
    if (blockStatus.isBlocked) {
      errorMessage.value = `Compte bloqué jusqu'à ${blockStatus.blockedUntil}. Réessayez plus tard.`;
      await showToast(errorMessage.value, 'warning');
      loading.value = false;
      return;
    }
    
    // 4. Login réussi - réinitialiser les tentatives
    await resetLoginAttempts(result.user.uid);
    
    // 5. Stocker les informations
    localStorage.setItem('firebaseUid', result.user.uid);
    localStorage.setItem('userEmail', result.user.email || '');
    localStorage.setItem('token', result.token);
    
    // 6. Afficher message de succès
    successMessage.value = 'Connexion réussie !';
    await showToast('Connexion réussie ! Redirection...', 'success');
    
    // 7. Rediriger vers la carte
    setTimeout(() => {
      router.push('/map');
    }, 500);
    
  } catch (error: any) {
    console.error('❌ Erreur de connexion:', error);
    loading.value = false;
    
    // Gestion des erreurs spécifiques Firebase
    if (error.code === 'auth/invalid-credential') {
      errorMessage.value = 'Email ou mot de passe incorrect.';
      await incrementLoginAttempts(email.value);
      
    } else if (error.code === 'auth/user-not-found') {
      errorMessage.value = 'Utilisateur non trouvé. Créez un compte d\'abord.';
      
    } else if (error.code === 'auth/wrong-password') {
      errorMessage.value = 'Mot de passe incorrect.';
      await incrementLoginAttempts(email.value);
      
    } else if (error.code === 'auth/too-many-requests') {
      errorMessage.value = 'Trop de tentatives. Réessayez dans quelques minutes.';
      
    } else if (error.code === 'auth/network-request-failed') {
      errorMessage.value = 'Erreur réseau. Vérifiez votre connexion.';
      
    } else if (error.message?.includes('bloqué')) {
      errorMessage.value = error.message;
      
    } else {
      errorMessage.value = 'Erreur de connexion: ' + (error.message || error);
    }
    
    await showToast(errorMessage.value, 'danger');
    
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

onMounted(() => {
  console.log('🚀 Page Login montée');
  console.log('Firebase prêt à l\'emploi');
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