<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Carte</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-text>
        <p>Carte en construction. Connexion reussie.</p>
      </ion-text>

      <ion-button class="ion-margin-top" expand="block" @click="handleLogout">
        Deconnexion
      </ion-button>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import {
  IonButton,
  IonContent,
  IonHeader,
  IonPage,
  IonText,
  IonTitle,
  IonToolbar
} from '@ionic/vue';
import { useRouter } from 'vue-router';
import { logoutUser } from '@/services/firebaseService';

const router = useRouter();

const handleLogout = async () => {
  await logoutUser();
  localStorage.removeItem('user');
  await router.replace('/login');
};
</script>
