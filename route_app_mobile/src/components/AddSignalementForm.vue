<template>
  <form @submit.prevent="submitForm" class="signalement-form">
    <ion-list>
      <!-- Quartier -->
      <ion-item>
        <ion-label position="stacked">Quartier <span class="required">*</span></ion-label>
        <ion-select 
          v-model="formData.idQuartier"
          placeholder="Sélectionner un quartier"
          :disabled="loading"
          required
        >
          <ion-select-option 
            v-for="quartier in quartiers" 
            :key="quartier.id_quartier"
            :value="quartier.id_quartier"
          >
            {{ quartier.quartier }} ({{ quartier.province?.province }})
          </ion-select-option>
        </ion-select>
      </ion-item>

      <!-- Entreprise (optionnel) -->
      <ion-item>
        <ion-label position="stacked">Entreprise</ion-label>
        <ion-select 
          v-model="formData.idEntreprise"
          placeholder="Sélectionner une entreprise"
          :disabled="loading"
        >
          <ion-select-option :value="null">Aucune entreprise</ion-select-option>
          <ion-select-option 
            v-for="entreprise in entreprises" 
            :key="entreprise.idEntreprise"
            :value="entreprise.idEntreprise"
          >
            {{ entreprise.entreprise }}
          </ion-select-option>
        </ion-select>
      </ion-item>

      <!-- Surface -->
      <ion-item>
        <ion-label position="stacked">Surface (m²) <span class="required">*</span></ion-label>
        <ion-input 
          v-model="formData.surface"
          type="number"
          placeholder="Ex: 150.50"
          step="0.01"
          min="0"
          :disabled="loading"
          required
        ></ion-input>
      </ion-item>

      <!-- Budget -->
      <ion-item>
        <ion-label position="stacked">Budget (Ar)</ion-label>
        <ion-input 
          v-model="formData.budget"
          type="number"
          placeholder="Ex: 5000000"
          step="1000"
          min="0"
          :disabled="loading"
        ></ion-input>
      </ion-item>

      <!-- Description (optionnel) -->
      <ion-item>
        <ion-label position="stacked">Description</ion-label>
        <ion-textarea 
          v-model="formData.description"
          placeholder="Description du problème..."
          rows="3"
          :disabled="loading"
        ></ion-textarea>
      </ion-item>
    </ion-list>

    <!-- Messages d'erreur/succès -->
    <div v-if="successMessage" class="success-message">
      <ion-text color="success">{{ successMessage }}</ion-text>
    </div>

    <div v-if="errorMessage" class="error-message">
      <ion-text color="danger">{{ errorMessage }}</ion-text>
    </div>

    <!-- Boutons d'action -->
    <div class="form-actions">
      <ion-button 
        expand="block" 
        color="medium" 
        fill="clear" 
        @click="$emit('cancel')"
        :disabled="loading"
      >
        Annuler
      </ion-button>
      
      <ion-button 
        expand="block" 
        type="submit"
        :disabled="loading || !isFormValid"
      >
        <ion-spinner v-if="loading" name="crescent"></ion-spinner>
        <span v-else>Ajouter le signalement</span>
      </ion-button>
    </div>
  </form>
</template>

<script setup lang="ts">
import {
  IonList,
  IonItem,
  IonLabel,
  IonSelect,
  IonSelectOption,
  IonInput,
  IonTextarea,
  IonButton,
  IonSpinner,
  IonText
} from '@ionic/vue';
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';

const API_BASE_URL = import.meta.env.VUE_APP_API_URL || 'http://localhost:8081/api';

// Props et events
const emit = defineEmits(['signalement-added', 'cancel']);

// États
const loading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

// Données pour les selects
const quartiers = ref<any[]>([]);
const entreprises = ref<any[]>([]);

// Formulaire
const formData = ref({
  idQuartier: null as number | null,
  idEntreprise: null as number | null,
  surface: '',
  budget: '',
  description: ''
});

// Validation du formulaire
const isFormValid = computed(() => {
  return formData.value.idQuartier !== null && 
         formData.value.surface !== '' && 
         parseFloat(formData.value.surface) > 0;
});

// Charger les données pour les selects
const loadSelectData = async () => {
  try {
    const token = localStorage.getItem('token');
    
    // Charger les quartiers
    const quartiersResponse = await axios.get(`${API_BASE_URL}/quartiers`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    quartiers.value = quartiersResponse.data;
    
    // Charger les entreprises
    const entreprisesResponse = await axios.get(`${API_BASE_URL}/entreprises`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    entreprises.value = entreprisesResponse.data;
    
  } catch (error) {
    console.error('Erreur chargement données:', error);
    errorMessage.value = 'Erreur lors du chargement des données';
  }
};

// Soumettre le formulaire
const submitForm = async () => {
  try {
    loading.value = true;
    errorMessage.value = '';
    successMessage.value = '';
    
    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('Non authentifié');
    }
    
    // Préparer les données
    const signalementData = {
      idQuartier: formData.value.idQuartier,
      idEntreprise: formData.value.idEntreprise,
      surface: parseFloat(formData.value.surface),
      budget: formData.value.budget ? parseFloat(formData.value.budget) : null,
      description: formData.value.description || null,
      date: new Date().toISOString()
    };
    
    console.log('Envoi du signalement:', signalementData);
    
    // Envoyer à l'API
    const response = await axios.post(
      `${API_BASE_URL}/signalements`,
      signalementData,
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    if (response.status === 201) {
      successMessage.value = 'Signalement ajouté avec succès!';
      
      // Émettre l'événement
      emit('signalement-added', response.data);
      
      // Réinitialiser le formulaire
      resetForm();
      
      // Fermer automatiquement après 2 secondes
      setTimeout(() => {
        emit('cancel');
      }, 2000);
    }
    
  } catch (error: any) {
    console.error('Erreur ajout signalement:', error);
    
    if (error.response?.status === 401) {
      errorMessage.value = 'Session expirée. Veuillez vous reconnecter.';
    } else if (error.response?.status === 400) {
      errorMessage.value = 'Données invalides: ' + 
        (error.response.data?.message || 'Vérifiez les champs');
    } else {
      errorMessage.value = 'Erreur lors de l\'ajout: ' + 
        (error.response?.data?.message || error.message);
    }
  } finally {
    loading.value = false;
  }
};

// Réinitialiser le formulaire
const resetForm = () => {
  formData.value = {
    idQuartier: null,
    idEntreprise: null,
    surface: '',
    budget: '',
    description: ''
  };
};

// Au montage
onMounted(() => {
  loadSelectData();
});
</script>

<style scoped>
.signalement-form {
  padding: 16px 0;
}

.required {
  color: #f44336;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.form-actions ion-button {
  flex: 1;
}

.success-message, .error-message {
  padding: 12px;
  border-radius: 8px;
  margin-top: 16px;
  text-align: center;
}

.success-message {
  background-color: rgba(76, 175, 80, 0.1);
}

.error-message {
  background-color: rgba(244, 67, 54, 0.1);
}
</style>