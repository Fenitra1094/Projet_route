<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Carte</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="openSignalementForm">Signaler</ion-button>
          <ion-button @click="handleLogout">Deconnexion</ion-button>
        </ion-buttons>
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
              <ion-label position="stacked">Surface (m2)</ion-label>
              <ion-input v-model="draft.surface" type="number" />
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Budget</ion-label>
              <ion-input v-model="draft.budget" type="number" />
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Entreprise</ion-label>
              <ion-select v-model="draft.entrepriseId" placeholder="Choisir">
                <ion-select-option
                  v-for="item in entreprises"
                  :key="item.id"
                  :value="item.id"
                >
                  {{ item.label }}
                </ion-select-option>
              </ion-select>
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Statut</ion-label>
              <ion-select v-model="draft.statusId" placeholder="Choisir">
                <ion-select-option
                  v-for="item in statuses"
                  :key="item.id"
                  :value="item.id"
                >
                  {{ item.label }}
                </ion-select-option>
              </ion-select>
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Description</ion-label>
              <ion-textarea v-model="draft.description" auto-grow />
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
  IonSelect,
  IonSelectOption,
  IonTextarea,
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
  fetchEntreprises,
  fetchQuartiers,
  fetchStatuses,
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
const entreprises = ref<SelectOption[]>([]);
const statuses = ref<SelectOption[]>([]);
const quartiers = ref<QuartierRef[]>([]);
const draft = ref<SignalementDraft>(createSignalementDraft(getUserId(), getUserEmail()));
const saving = ref(false);
const toastOpen = ref(false);
const toastMessage = ref('');
const toastColor = ref<'success' | 'danger'>('success');
let signalementLayer: L.LayerGroup | null = null;
let signalementUnsub: (() => void) | null = null;

const openSignalementForm = () => {
  showSignalModal.value = true;
};

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

  for (const item of items) {
    if (item.latitude == null || item.longitude == null) continue;
    const marker = L.marker([item.latitude, item.longitude]);
    const status = item.status || item.statusId || 'nouveau';
    const content = `
      <strong>${item.quartierLabel || item.quartierId || 'Quartier'}</strong><br />
      Date: ${item.date_ || ''}<br />
      Statut: ${status}<br />
      Surface: ${item.surface || ''} m2<br />
      Budget: ${item.budget || ''}<br />
      Entreprise: ${item.entrepriseId || ''}
    `;
    marker.bindPopup(content);
    marker.addTo(signalementLayer);
  }
};

onMounted(() => {
  carteInstance = initCarte('map');
  carteInstance.map.on('click', handleMapClick);
  signalementLayer = L.layerGroup().addTo(carteInstance.map);

  Promise.all([fetchEntreprises(), fetchStatuses(), fetchQuartiers()])
    .then(([entreprisesData, statusesData, quartiersData]) => {
      entreprises.value = entreprisesData;
      statuses.value = statusesData;
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
</style>
