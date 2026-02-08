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
          :type="passwordType"
          placeholder="Votre mot de passe"
          @keyup.enter="handleLogin"
        />
        <ion-button
          fill="clear"
          slot="end"
          type="button"
          aria-label="Afficher ou masquer le mot de passe"
          @click="showPassword = !showPassword"
        >
          <ion-icon :icon="showPassword ? eyeOffOutline : eyeOutline" />
        </ion-button>
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
  IonIcon,
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
import { computed, ref } from 'vue';
import { eyeOffOutline, eyeOutline } from 'ionicons/icons';
import { useRouter } from 'vue-router';
import {
  checkUserBlockStatus,
  checkUserBlockStatusByEmail,
  getSecurityConfig,
  incrementLoginAttemptsByEmail,
  logoutUser,
  loginUser,
  setSession,
  startUserStatusListener,
  syncUserProfile
} from '@/services/firebaseService';

const router = useRouter();
const email = ref('bemaso@gmail.com');
const password = ref('bemasooo');
const showPassword = ref(false);
const passwordType = computed(() => (showPassword.value ? 'text' : 'password'));
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
    const preStatus = await checkUserBlockStatusByEmail(email.value);
    if (preStatus.isBlocked) {
      showToast('Compte bloque. Contactez un administrateur.');
      return;
    }

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
    startUserStatusListener(user.uid);

    localStorage.setItem('user', JSON.stringify({
      uid: user.uid,
      email: user.email
    }));

    showToast('Connexion reussie.', 'success');
    await router.replace('/map');
  } catch (error) {
    try {
      const result = await incrementLoginAttemptsByEmail(email.value);
      const remaining = result.maxAttempts - result.attempts;

      if (result.isBlocked) {
        showToast('Compte bloque. Trop de tentatives.');
        return;
      }

      if (remaining > 0 && result.attempts > 0) {
        showToast(`Connexion echouee. Tentatives restantes: ${remaining}.`);
        return;
      }
    } catch {
      // Ignore attempt tracking errors and show generic message.
    }

    showToast('Connexion echouee. Verifiez vos identifiants.');
  } finally {
    loading.value = false;
  }
};
</script>
