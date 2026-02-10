const API_URL = 'http://localhost:8081';

export const getAllSignalements = async () => {
    const response = await fetch(`${API_URL}/api/signalements/List`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    });
    if (!response.ok) throw new Error('Erreur lors du chargement des signalements');
    return response.json();
};

export const updateSignalement = async (id, data) => {
    const response = await fetch(`${API_URL}/api/signalements/${id}/edit`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
    });
    if (!response.ok) throw new Error('Erreur lors de la mise à jour');
    return response.json();
};

export const getAllEntreprises = async () => {
    const response = await fetch(`${API_URL}/entreprises`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    });
    if (!response.ok) throw new Error('Erreur lors du chargement des entreprises');
    return response.json();
};

export const getAllStatus = async () => {
    const response = await fetch(`${API_URL}/status`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    });
    if (!response.ok) throw new Error('Erreur lors du chargement des status');
    return response.json();
};

export const updateSignalementStatus = async (id, statusId) => {
    const response = await fetch(`${API_URL}/api/signalements/${id}/status?statusId=${statusId}`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
        },
    });
    if (!response.ok) throw new Error('Erreur lors de la mise a jour du status');
    return response.json();
};

export const getSignalementsByDateRange = async (dateDebut, dateFin) => {
    const response = await fetch(`${API_URL}/api/signalements/by-date?dateDebut=${dateDebut}&dateFin=${dateFin}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    });
    if (!response.ok) throw new Error('Erreur lors du chargement des signalements par date');
    return response.json();
};


export async function getSignalementRecapitulatif() {
  const res = await fetch(`${BASE}/api/signalements/recapitulatif`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
};

export async function getAllSignalements() {
  const res = await fetch(`${BASE}/api/signalements`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
};

export async function getSignalementSummary() {
  const res = await fetch(`${BASE}/api/signalements/summary`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
};

export async function syncFromFirebase() {
  const res = await fetch(`${BASE}/api/sync/from-firebase`, {
    method: 'POST',
  });
  if (!res.ok) throw new Error(await res.text());
  return res.text();
};

export async function syncToFirebase() {
  const res = await fetch(`${BASE}/api/sync/to-firebase`, {
    method: 'POST',
  });
  if (!res.ok) throw new Error(await res.text());
  return res.text();
};

export async function fullSync() {
  const res = await fetch(`${BASE}/api/sync/full`, {
    method: 'POST',
  });
  if (!res.ok) throw new Error(await res.text());
  return res.text();
};

export async function getDelaiTraitementMoyen(statusFinal = 'termine') {
  const res = await fetch(`${BASE}/api/signalements/delai-traitement-moyen?statusFinal=${encodeURIComponent(statusFinal)}`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
};

export async function getAllUsers() {
  const res = await fetch(`${BASE}/api/users`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
};

export async function updateUser(id, userData) {
  const res = await fetch(`${BASE}/api/users/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(userData),
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
};
