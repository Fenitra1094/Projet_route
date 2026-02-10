<template>
  <ion-page>
    <ion-content class="login-content" :fullscreen="true">
      <div class="login-container">
        <!-- Logo / titre principal -->
        <div class="login-header">
          <div class="logo-circle">
            <ion-icon :icon="navigateOutline" class="logo-icon" />
          </div>
          <h1>RouteSignal</h1>
          <p class="subtitle">Système de signalement routier</p>
        </div>

        <!-- Formulaire de connexion -->
        <div class="login-form">
          <h2>Connexion</h2>
          
          <div class="form-group">
            <label class="form-label">Email</label>
            <ion-input
              v-model="email"
              type="email"
              placeholder="email@exemple.com"
              class="custom-input"
              fill="outline"
              inputmode="email"
              autocomplete="email"
              enterkeyhint="next"
            />
          </div>

          <div class="form-group">
            <label class="form-label">Mot de passe</label>
            <div class="password-wrapper">
              <ion-input
                v-model="password"
                :type="passwordType"
                placeholder="Votre mot de passe"
                class="custom-input password-input"
                fill="outline"
                enterkeyhint="go"
                @keyup.enter="handleLogin"
              />
              <ion-button
                fill="clear"
                class="password-toggle"
                @click="showPassword = !showPassword"
                aria-label="Afficher le mot de passe"
              >
                <ion-icon :icon="showPassword ? eyeOffOutline : eyeOutline" />
              </ion-button>
            </div>
          </div>

          <ion-button
            expand="block"
            class="login-button"
            @click="handleLogin"
            :disabled="loading"
          >
            <ion-icon v-if="!loading" :icon="logInOutline" slot="start" />
            {{ loading ? 'Connexion...' : 'Se connecter' }}
          </ion-button>

          <p class="login-info">
            <ion-icon :icon="shieldCheckmarkOutline" class="info-icon" />
            Connexion requise pour accéder à la carte.
          </p>
        </div>
      </div>

      <ion-loading 
        :is-open="loading" 
        message="Connexion en cours..." 
        spinner="crescent"
        css-class="login-loading"
      />
      <ion-toast
        :is-open="toastOpen"
        :message="toastMessage"
        :icon="toastColor === 'success' ? checkmarkCircleOutline : alertCircleOutline"
        duration="2500"
        position="bottom"
        :class="['app-toast', toastColor === 'success' ? 'success-toast' : 'error-toast']"
        @didDismiss="toastOpen = false"
      />
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import {
  IonButton,
  IonContent,
  IonIcon,
  IonInput,
  IonLoading,
  IonPage,
  IonToast
} from '@ionic/vue';
import { computed, ref } from 'vue';
import { eyeOffOutline, eyeOutline, logInOutline, navigateOutline, shieldCheckmarkOutline, checkmarkCircleOutline, alertCircleOutline } from 'ionicons/icons';
import { useRouter } from 'vue-router';
import { loginWithEmail } from '@/services/LoginService';

const router = useRouter();
const email = ref('test@gmail.com');
const password = ref('test123');
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
    const result = await loginWithEmail(email.value, password.value);
    showToast(result.message, result.status === 'success' ? 'success' : 'danger');

    if (result.status === 'success') {
      await router.replace('/map');
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-content {
  --background: linear-gradient(160deg, #f5f1e8 0%, #e8f0e4 100%);
}

.login-container {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 20px;
  padding-top: env(safe-area-inset-top, 24px);
  padding-bottom: env(safe-area-inset-bottom, 24px);
}

/* Logo animé */
.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6aa84f, #4a8630);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 8px 24px rgba(106, 168, 79, 0.35);
}

.logo-icon {
  font-size: 36px;
  color: white;
}

.login-header h1 {
  font-size: 28px;
  font-weight: 800;
  color: #111111;
  margin: 0;
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 15px;
  color: #333333;
  margin-top: 6px;
}

/* Formulaire */
.login-form {
  background: white;
  border-radius: 20px;
  padding: 28px 24px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-form h2 {
  font-size: 22px;
  font-weight: 700;
  color: #111111;
  margin: 0 0 24px 0;
  text-align: center;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #111111;
  margin-bottom: 8px;
}

/* Inputs adaptés mobile : grande zone de toucher */
.custom-input {
  --background: #f8f9fa;
  --border-radius: 14px;
  --padding-start: 18px;
  --padding-end: 18px;
  --padding-top: 14px;
  --padding-bottom: 14px;
  font-size: 16px; /* empêche le zoom sur iOS */
  min-height: 56px;
  color: #111111;
  --color: #111111;
  --placeholder-color: #888888;
}

.password-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.password-input {
  --padding-end: 56px;
}

.password-toggle {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  --color: #7f8c8d;
  min-width: 48px;
  min-height: 48px;
  z-index: 2;
}

.password-toggle ion-icon {
  font-size: 22px;
}

/* Bouton de connexion : grand et tactile */
.login-button {
  --background: linear-gradient(135deg, #6aa84f, #5a9640);
  --background-hover: #5a9640;
  --background-activated: #4a8630;
  --border-radius: 14px;
  --box-shadow: 0 4px 16px rgba(106, 168, 79, 0.35);
  font-weight: 700;
  font-size: 17px;
  height: 56px;
  margin-top: 8px;
  text-transform: none;
  letter-spacing: 0.3px;
}

.login-button ion-icon {
  font-size: 22px;
}

/* Info bas de page */
.login-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  text-align: center;
  color: #444444;
  font-size: 13px;
  margin-top: 20px;
  margin-bottom: 0;
}

.info-icon {
  font-size: 16px;
  color: #6aa84f;
}
</style>