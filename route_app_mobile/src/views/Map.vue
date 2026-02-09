<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Carte</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="handleLogout">Deconnexion</ion-button>
        </ion-buttons>
      </ion-toolbar>
      <ion-toolbar>
        <ion-item lines="none">
          <ion-label>Mes signalements</ion-label>
          <ion-toggle v-model="showMineOnly" />
        </ion-item>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <div id="map" class="map-container"></div>

      <ion-modal :is-open="showSignalModal" @didDismiss="showSignalModal = false">
        <ion-header>
          <ion-toolbar>
            <ion-title>Nouveau signalement</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="showSignalModal = false">Fermer</ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding">
          <ion-list>
            <ion-item>
              <ion-label position="stacked">Date</ion-label>
              <ion-input :value="draft.date_" readonly />
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Province</ion-label>
              <ion-input :value="draft.provinceId" readonly />
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Quartier</ion-label>
              <ion-input :value="draft.quartierLabel" readonly />
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Latitude</ion-label>
              <ion-input :value="draft.latitude" readonly />
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Longitude</ion-label>
              <ion-input :value="draft.longitude" readonly />
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Photos</ion-label>
              <input
                class="photo-input"
                type="file"
                accept="image/*"
                multiple
                @change="handlePhotoChange"
              />
            </ion-item>
          </ion-list>
          <ion-button expand="block" class="ion-margin-top" @click="handleSave">
            Enregistrer
          </ion-button>
        </ion-content>
      </ion-modal>
      <ion-loading :is-open="saving" message="Enregistrement..." />
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
  IonButtons,
  IonContent,
  IonHeader,
  IonInput,
  IonItem,
  IonLabel,
  IonLoading,
  IonList,
  IonModal,
  IonPage,
  IonToggle,
  IonToast,
  IonTitle,
  IonToolbar
} from '@ionic/vue';
import L, { type LeafletMouseEvent } from 'leaflet';
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { clearSession, logoutUser } from '@/services/LoginService';
import { initCarte, type CarteInstance } from '@/services/CarteService';
import {
  applyMapSelection,
  createSignalementDraft,
  fetchQuartiers,
  listenSignalements,
  saveSignalement,
  updateDraftPhotos,
  type QuartierRef,
  type SignalementDraft,
  type SelectOption
} from '@/services/SignalementService';
import 'leaflet/dist/leaflet.css';

const router = useRouter();
let carteInstance: CarteInstance | null = null;
const showSignalModal = ref(false);
const quartiers = ref<QuartierRef[]>([]);
const draft = ref<SignalementDraft>(createSignalementDraft(getUserId(), getUserEmail()));
const showMineOnly = ref(false);
const saving = ref(false);
const toastOpen = ref(false);
const toastMessage = ref('');
const toastColor = ref<'success' | 'danger'>('success');
let signalementLayer: L.LayerGroup | null = null;
let signalementUnsub: (() => void) | null = null;
const currentUserId = getUserId();

const handleMapClick = async (event: LeafletMouseEvent) => {
  draft.value = await applyMapSelection(
    draft.value,
    event.latlng.lat,
    event.latlng.lng,
    quartiers.value
  );
  showSignalModal.value = true;
  carteInstance?.map.openPopup('Signaler', event.latlng);
};

const handlePhotoChange = (event: Event) => {
  const input = event.target as HTMLInputElement;
  draft.value = updateDraftPhotos(draft.value, input.files);
};

const handleSave = async () => {
  try {
    saving.value = true;
    await saveSignalement(draft.value);
    toastMessage.value = 'Signalement enregistre.';
    toastColor.value = 'success';
    toastOpen.value = true;
    showSignalModal.value = false;
    draft.value = createSignalementDraft(getUserId(), getUserEmail());
  } catch (error) {
    toastMessage.value = 'Erreur lors de l\'enregistrement.';
    toastColor.value = 'danger';
    toastOpen.value = true;
  } finally {
    saving.value = false;
  }
};

function getUserId(): string {
  const raw = localStorage.getItem('user');
  if (!raw) return '';

  try {
    const parsed = JSON.parse(raw);
    return parsed?.uid || '';
  } catch {
    return '';
  }
}

function getUserEmail(): string {
  const raw = localStorage.getItem('user');
  if (!raw) return '';

  try {
    const parsed = JSON.parse(raw);
    return parsed?.email || '';
  } catch {
    return '';
  }
}

const renderSignalements = (items: any[]) => {
  if (!carteInstance) return;

  if (!signalementLayer) {
    signalementLayer = L.layerGroup().addTo(carteInstance.map);
  }

  signalementLayer.clearLayers();

  const statusIcons: Record<string, string> = {
    nouveau: '🆕',
    en_cours: '⚠️',
    termine: '✅'
  };

  const visibleItems = showMineOnly.value
    ? items.filter((item) => item.userId === currentUserId)
    : items;

  for (const item of visibleItems) {
    if (item.latitude == null || item.longitude == null) continue;
    const status = String(item.status || item.statusId || 'nouveau');
    const normalizedStatus = status.toLowerCase();
    const iconLabel = statusIcons[normalizedStatus] || '📍';
    const icon = L.divIcon({
      className: 'signalement-icon',
      html: `<span>${iconLabel}</span>`
    });
    const marker = L.marker([item.latitude, item.longitude], { icon });
    const photoLinks = Array.isArray(item.photos)
      ? item.photos
          .map((photo: any) => photo?.url)
          .filter(Boolean)
          .map((url: string) => `<a href="${url}" target="_blank">Photo</a>`)
          .join(' | ')
      : '';
    const content = `
      <strong>${item.provinceId || 'Antananarivo'}</strong><br />
      Quartier: ${item.quartierLabel || item.quartierId || 'Quartier'}<br />
      Date: ${item.date_ || ''}<br />
      Statut: ${status}<br />
      Surface: ${item.surface || ''} m2<br />
      Budget: ${item.budget || ''}<br />
      Entreprise: ${item.entrepriseId || ''}<br />
      ${photoLinks}
    `;
    marker.bindPopup(content);
    marker.addTo(signalementLayer);
  }
};

onMounted(() => {
  carteInstance = initCarte('map');
  carteInstance.map.on('click', handleMapClick);
  signalementLayer = L.layerGroup().addTo(carteInstance.map);

  fetchQuartiers()
    .then((quartiersData) => {
      quartiers.value = quartiersData;
    })
    .catch(() => {
      // Ignore load errors for now.
    });

  signalementUnsub = listenSignalements(renderSignalements);
});

onBeforeUnmount(() => {
  if (carteInstance) {
    carteInstance.map.off('click', handleMapClick);
    carteInstance.destroy();
    carteInstance = null;
  }

  if (signalementLayer) {
    signalementLayer.clearLayers();
    signalementLayer = null;
  }

  if (signalementUnsub) {
    signalementUnsub();
    signalementUnsub = null;
  }
});

const handleLogout = async () => {
  await logoutUser();
  clearSession();
  await router.replace('/login');
};
</script>

<style scoped>
.map-container {
  height: 100%;
  width: 100%;
}

.signalement-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  font-size: 22px;
}
</style>
