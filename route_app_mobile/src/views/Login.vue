<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Connexion</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-item>
        <ion-label position="stacked">Email</ion-label>
        <ion-input v-model="email" type="email" placeholder="email@exemple.com" />
      </ion-item>

      <ion-item class="ion-margin-top">
        <ion-label position="stacked">Mot de passe</ion-label>
        <ion-input
          v-model="password"
          type="password"
          placeholder="Votre mot de passe"
          @keyup.enter="handleLogin"
        />
      </ion-item>

      <ion-button
        expand="block"
        class="ion-margin-top"
        @click="handleLogin"
      >
        Se connecter
      </ion-button>

      <ion-text color="medium">
        <p class="ion-margin-top">
          Connexion requise pour acceder a la carte.
        </p>
      </ion-text>

      <ion-loading :is-open="loading" message="Connexion..." />
      <ion-toast
        :is-open="toastOpen"
        :message="toastMessage"
        :color="toastColor"
        duration="2500"
        @didDismiss="toastOpen = false"
      />
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import {
  IonButton,
  IonContent,
  IonHeader,
  IonInput,
  IonItem,
  IonLabel,
  IonLoading,
  IonPage,
  IonText,
  IonTitle,
  IonToast,
  IonToolbar
} from '@ionic/vue';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import {
  checkUserBlockStatus,
  getSecurityConfig,
  logoutUser,
  loginUser,
  setSession,
  syncUserProfile
} from '@/services/firebaseService';

const router = useRouter();
const email = ref('bemaso@gmail.com');
const password = ref('bemasooo');
const loading = ref(false);
const toastOpen = ref(false);
const toastMessage = ref('');
const toastColor = ref<'success' | 'danger'>('danger');

const showToast = (message: string, color: 'success' | 'danger' = 'danger') => {
  toastMessage.value = message;
  toastColor.value = color;
  toastOpen.value = true;
};

const handleLogin = async () => {
  if (!email.value || !password.value) {
    showToast('Email et mot de passe requis.');
    return;
  }

  loading.value = true;

  try {
    const { user } = await loginUser(email.value, password.value);
    await syncUserProfile(user);
    const status = await checkUserBlockStatus(user.uid);

    if (status.isBlocked) {
      await logoutUser();
      showToast('Compte bloque. Contactez un administrateur.');
      return;
    }

    const securityConfig = await getSecurityConfig();
    setSession(user.uid, securityConfig.sessionDurationSec);

    localStorage.setItem('user', JSON.stringify({
      uid: user.uid,
      email: user.email
    }));

    showToast('Connexion reussie.', 'success');
    await router.replace('/map');
  } catch (error) {
    showToast('Connexion echouee. Verifiez vos identifiants.');
  } finally {
    loading.value = false;
  }
};
</script>
