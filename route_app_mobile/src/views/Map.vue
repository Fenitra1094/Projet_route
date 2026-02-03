<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Carte des Signalements</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="refreshMap">
            <ion-icon :icon="refreshOutline"></ion-icon>
          </ion-button>
          <ion-button @click="openAddModal">
            <ion-icon :icon="addOutline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <!-- Carte Container -->
      <div id="map-container" class="map-container"></div>

      <!-- Loading Spinner -->
      <div v-if="loading" class="loading-overlay">
        <ion-spinner name="crescent"></ion-spinner>
        <p>Chargement des signalements...</p>
      </div>

      <!-- Error Message -->
      <ion-card v-if="errorMessage" color="danger">
        <ion-card-content>
          <ion-text color="light">{{ errorMessage }}</ion-text>
          <ion-button expand="block" @click="refreshMap" class="ion-margin-top">
            Réessayer
          </ion-button>
        </ion-card-content>
      </ion-card>

      <!-- Modal d'ajout de signalement -->
      <ion-modal 
        :is-open="showAddModal" 
        @didDismiss="showAddModal = false"
        :initial-breakpoint="0.75"
        :breakpoints="[0, 0.25, 0.5, 0.75]"
      >
        <ion-header>
          <ion-toolbar>
            <ion-title>Nouveau Signalement</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="showAddModal = false">
                <ion-icon :icon="closeOutline"></ion-icon>
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding">
          <AddSignalementForm 
            @signalement-added="handleSignalementAdded"
            @cancel="showAddModal = false"
          />
        </ion-content>
      </ion-modal>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { 
  IonPage, 
  IonHeader, 
  IonToolbar, 
  IonTitle, 
  IonContent,
  IonButton, 
  IonButtons,
  IonIcon,
  IonSpinner,
  IonText,
  IonCard,
  IonCardContent,
  IonModal,
  onIonViewDidEnter,
  onIonViewDidLeave
} from '@ionic/vue';
import { 
  ref, 
  onMounted, 
  onUnmounted 
} from 'vue';
import { 
  addOutline, 
  refreshOutline, 
  closeOutline,
  locationOutline 
} from 'ionicons/icons';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import axios from 'axios';
import AddSignalementForm from '@/components/AddSignalementForm.vue';

// Configuration
const API_BASE_URL = import.meta.env.VUE_APP_API_URL || 'http://localhost:8081/api';

// États
const loading = ref(false);
const errorMessage = ref('');
const showAddModal = ref(false);
const signalements = ref<any[]>([]);

// Références Leaflet
let map: L.Map | null = null;
let markers: L.Marker[] = [];

// Coordonnées d'Antananarivo (centrale)
const TANA_CENTER: [number, number] = [-18.8792, 47.5079];
const MAP_ZOOM = 13;

// Initialisation de la carte
const initMap = () => {
  console.log('🗺️ Initialisation de la carte...');
  
  try {
    // Créer la carte
    map = L.map('map-container').setView(TANA_CENTER, MAP_ZOOM);
    
    // Ajouter la couche OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      maxZoom: 19
    }).addTo(map);
    
    console.log('✅ Carte initialisée');
    
    // Charger les signalements
    loadSignalements();
    
  } catch (error) {
    console.error('❌ Erreur d\'initialisation de la carte:', error);
    errorMessage.value = 'Impossible de charger la carte';
  }
};

// Configuration des icônes Leaflet
const createCustomIcon = (status: string) => {
  // Couleurs selon le statut
  const colorMap: Record<string, string> = {
    'En attente': '#ff9800', // Orange
    'En cours': '#2196f3',   // Bleu
    'Terminé': '#4caf50',    // Vert
    'Annulé': '#f44336'      // Rouge
  };
  
  const color = colorMap[status] || '#9e9e9e'; // Gris par défaut
  
  return L.divIcon({
    className: 'custom-marker',
    html: `
      <div style="
        background-color: ${color};
        width: 24px;
        height: 24px;
        border-radius: 50%;
        border: 3px solid white;
        box-shadow: 0 2px 5px rgba(0,0,0,0.3);
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-weight: bold;
        font-size: 12px;
      ">
        <ion-icon name="location" style="font-size: 12px;"></ion-icon>
      </div>
    `,
    iconSize: [24, 24],
    iconAnchor: [12, 24],
    popupAnchor: [0, -24]
  });
};

// Charger les signalements depuis l'API
const loadSignalements = async () => {
  try {
    loading.value = true;
    errorMessage.value = '';
    
    console.log('📡 Chargement des signalements...');
    
    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('Non authentifié');
    }
    
    const response = await axios.get(`${API_BASE_URL}/signalements`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (response.status === 200) {
      signalements.value = response.data;
      console.log(`✅ ${signalements.value.length} signalements chargés`);
      
      // Afficher les markers
      displayMarkers();
    }
    
  } catch (error: any) {
    console.error('❌ Erreur chargement signalements:', error);
    
    if (error.response?.status === 401) {
      errorMessage.value = 'Session expirée. Veuillez vous reconnecter.';
      // Rediriger vers login
      setTimeout(() => {
        window.location.href = '/login';
      }, 3000);
    } else if (error.response?.status === 403) {
      errorMessage.value = 'Accès non autorisé';
    } else if (error.message === 'Non authentifié') {
      errorMessage.value = 'Veuillez vous connecter';
    } else {
      errorMessage.value = 'Erreur lors du chargement des signalements: ' + 
        (error.response?.data?.message || error.message);
    }
  } finally {
    loading.value = false;
  }
};

// Afficher les markers sur la carte
const displayMarkers = () => {
  if (!map) return;
  
  // Supprimer les anciens markers
  clearMarkers();
  
  // Ajouter un marker pour chaque signalement
  signalements.value.forEach((signalement, index) => {
    try {
      const position: [number, number] = [
        signalement.quartier?.positionY || TANA_CENTER[0] + (Math.random() * 0.01 - 0.005),
        signalement.quartier?.positionX || TANA_CENTER[1] + (Math.random() * 0.01 - 0.005)
      ];
      
      // Créer le marker avec icône personnalisée
      const marker = L.marker(position, {
        icon: createCustomIcon(signalement.status?.libelle || 'Inconnu'),
        title: `Signalement #${signalement.idSignalement}`
      }).addTo(map!);
      
      // Préparer le contenu du popup
      const popupContent = createPopupContent(signalement);
      
      // Ajouter le popup
      marker.bindPopup(popupContent, {
        maxWidth: 300,
        minWidth: 250
      });
      
      markers.push(marker);
      
    } catch (error) {
      console.error(`❌ Erreur création marker ${index}:`, error);
    }
  });
  
  // Ajuster la vue si des markers existent
  if (markers.length > 0) {
    const markerGroup = L.featureGroup(markers);
    map.fitBounds(markerGroup.getBounds().pad(0.1));
  }
};

// Créer le contenu HTML du popup
const createPopupContent = (signalement: any) => {
  const date = new Date(signalement.date).toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
  
  const statusColor = signalement.status?.libelle === 'Terminé' ? 'success' : 
                     signalement.status?.libelle === 'En cours' ? 'primary' :
                     signalement.status?.libelle === 'En attente' ? 'warning' : 'danger';
  
  return `
    <div style="font-family: Arial, sans-serif; padding: 10px;">
      <h3 style="margin-top: 0; color: #3880ff;">
        Signalement #${signalement.idSignalement}
      </h3>
      
      <div style="margin-bottom: 8px;">
        <strong>📅 Date:</strong> ${date}
      </div>
      
      <div style="margin-bottom: 8px;">
        <strong>📍 Quartier:</strong> ${signalement.quartier?.quartier || 'Non spécifié'}
      </div>
      
      <div style="margin-bottom: 8px;">
        <strong>🏢 Entreprise:</strong> ${signalement.entreprise?.entreprise || 'Aucune'}
      </div>
      
      <div style="margin-bottom: 8px;">
        <strong>📏 Surface:</strong> ${signalement.surface ? signalement.surface + ' m²' : 'Non spécifiée'}
      </div>
      
      <div style="margin-bottom: 8px;">
        <strong>💰 Budget:</strong> ${signalement.budget ? signalement.budget + ' Ar' : 'Non spécifié'}
      </div>
      
      <div style="margin-bottom: 8px;">
        <strong>📋 Statut:</strong> 
        <span style="
          color: white; 
          background-color: ${statusColor === 'success' ? '#4caf50' : 
                           statusColor === 'primary' ? '#2196f3' : 
                           statusColor === 'warning' ? '#ff9800' : '#f44336'};
          padding: 2px 8px;
          border-radius: 12px;
          font-size: 12px;
          font-weight: bold;
        ">
          ${signalement.status?.libelle || 'Inconnu'}
        </span>
      </div>
      
      <div style="margin-top: 12px; font-size: 12px; color: #666;">
        Signalé par: ${signalement.user?.nom || 'Utilisateur'} 
        ${signalement.user?.prenom ? '(' + signalement.user.prenom + ')' : ''}
      </div>
    </div>
  `;
};

// Supprimer tous les markers
const clearMarkers = () => {
  markers.forEach(marker => {
    if (map && marker) {
      map.removeLayer(marker);
    }
  });
  markers = [];
};

// Rafraîchir la carte
const refreshMap = () => {
  console.log('🔄 Rafraîchissement de la carte...');
  loadSignalements();
};

// Ouvrir le modal d'ajout
const openAddModal = () => {
  showAddModal.value = true;
};

// Gérer l'ajout d'un signalement
const handleSignalementAdded = (newSignalement: any) => {
  console.log('✅ Nouveau signalement ajouté:', newSignalement);
  showAddModal.value = false;
  
  // Ajouter le nouveau signalement à la liste
  signalements.value.unshift(newSignalement);
  
  // Recharger les signalements (ou ajouter juste le nouveau marker)
  loadSignalements();
  
  // Afficher un toast de succès
  showSuccessToast('Signalement ajouté avec succès!');
};

// Afficher un toast
const showSuccessToast = async (message: string) => {
  const { toastController } = await import('@ionic/vue');
  const toast = await toastController.create({
    message,
    duration: 3000,
    color: 'success',
    position: 'top'
  });
  await toast.present();
};

// Nettoyer la carte
const cleanupMap = () => {
  if (map) {
    clearMarkers();
    map.remove();
    map = null;
  }
};

// Lifecycle hooks
onMounted(() => {
  console.log('📱 Composant Map monté');
  
  // Petit délai pour s'assurer que le DOM est prêt
  setTimeout(() => {
    initMap();
  }, 100);
});

onUnmounted(() => {
  console.log('🗑️ Composant Map démonté');
  cleanupMap();
});

onIonViewDidEnter(() => {
  console.log('👁️ Vue Map entrée');
  // Rafraîchir à chaque fois qu'on entre dans la vue
  if (map) {
    loadSignalements();
  }
});

onIonViewDidLeave(() => {
  console.log('👋 Vue Map quittée');
});
</script>

<style scoped>
.map-container {
  width: 100%;
  height: calc(100vh - 200px);
  min-height: 400px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 2px solid #3880ff;
}

.loading-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.9);
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.loading-overlay p {
  margin-top: 10px;
  color: #3880ff;
  font-weight: 500;
}

/* Styles pour les markers personnalisés */
:deep(.custom-marker) {
  background: transparent !important;
  border: none !important;
}

/* Style pour la carte Leaflet */
:deep(.leaflet-container) {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

:deep(.leaflet-popup-content) {
  font-size: 14px;
}

:deep(.leaflet-popup-content-wrapper) {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* Bouton d'action flottant */
.floating-button {
  position: fixed;
  bottom: 80px;
  right: 20px;
  z-index: 1000;
  --background: #3880ff;
  --background-activated: #3171e0;
  --border-radius: 50%;
  width: 56px;
  height: 56px;
  box-shadow: 0 4px 12px rgba(56, 128, 255, 0.3);
}
</style>