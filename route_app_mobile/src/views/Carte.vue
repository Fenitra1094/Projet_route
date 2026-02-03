<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Carte</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="logout">
            <ion-icon :icon="logOutOutline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <div class="welcome-message">
        <h2>Bienvenue, {{ userNom }} !</h2>
        <p>Vous êtes connecté en tant que {{ userRole }}</p>
      </div>
      
      <!-- Votre contenu carte ici -->
      <div id="map-container"></div>
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
  IonButton,
  IonIcon,
  IonButtons
} from '@ionic/vue';
import { logOutOutline } from 'ionicons/icons';
import { auth } from '@/config/firebase';
import { signOut } from 'firebase/auth';

const router = useRouter();
const userNom = ref('');
const userRole = ref('');

onMounted(() => {
  const userData = JSON.parse(localStorage.getItem('user') || '{}');
  userNom.value = userData.nom || '';
  userRole.value = userData.role || '';
});

const logout = async () => {
  try {
    await signOut(auth);
    localStorage.clear();
    router.push('/login');
  } catch (error) {
    console.error('Logout error:', error);
  }
};
</script>