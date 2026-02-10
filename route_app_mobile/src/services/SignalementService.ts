import { collection, getDocs } from 'firebase/firestore';
import { addPhotoToSignalement, addSignalement, onAllSignalementsChange } from './firebaseService';
import { db } from './firebaseService';

export type SignalementDraft = {
  date_: string;
  surface: string;
  budget: string;
  idUser: number;
  userEmail: string;
  idEntreprise: number | null;
  quartierId: string;
  quartierLabel: string;
  statusId: number;
  latitude: number | null;
  longitude: number | null;
  province: string;
  photos: File[];
};

export type QuartierRef = {
  id: string;
  label: string;
  latitude: number;
  longitude: number;
  provinceId: string;
};

export type SelectOption = {
  id: string;
  label: string;
};

type ReverseResult = {
  quartierLabel: string;
  quartierId: string;
  provinceId: string;
};

const normalizeLabel = (value: string) => value.trim().toLowerCase();

const findQuartierByLabel = (quartiers: QuartierRef[], label: string) => {
  const normalized = normalizeLabel(label);
  return quartiers.find((item) => normalizeLabel(item.label) === normalized) || null;
};

const getNearestQuartier = (quartiers: QuartierRef[], latitude: number, longitude: number) => {
  let best = quartiers[0];
  let bestScore = Number.POSITIVE_INFINITY;

  for (const quartier of quartiers) {
    const dx = quartier.latitude - latitude;
    const dy = quartier.longitude - longitude;
    const score = dx * dx + dy * dy;
    if (score < bestScore) {
      bestScore = score;
      best = quartier;
    }
  }

  return best;
};

export const createSignalementDraft = (idUser: number, userEmail: string): SignalementDraft => ({
  date_: new Date().toISOString(),
  surface: '',
  budget: '',
  idUser,
  userEmail,
  idEntreprise: null,
  quartierId: '',
  quartierLabel: '',
  statusId: 1,
  latitude: null,
  longitude: null,
  province: 'Antananarivo',
  photos: []
});

export const fetchQuartiers = async (): Promise<QuartierRef[]> => {
  const snapshot = await getDocs(collection(db, 'quartiers'));
  return snapshot.docs.map((docSnap) => {
    const data = docSnap.data() as any;
    return {
      id: docSnap.id,
      label: data.libelle || docSnap.id,
      latitude: Number(data.positionX ?? 0),
      longitude: Number(data.positionY ?? 0),
      provinceId: data.provinceId || 'antananarivo'
    };
  });
};

const fetchOptions = async (collectionName: string): Promise<SelectOption[]> => {
  const snapshot = await getDocs(collection(db, collectionName));
  return snapshot.docs.map((docSnap) => {
    const data = docSnap.data() as any;
    const numericId = Number(
      data.id_status ??
      data.id_entreprise ??
      data.id_province ??
      data.id_role ??
      data.id_user ??
      data.id
    );
    const idValue = Number.isFinite(numericId) && numericId > 0 ? String(numericId) : docSnap.id;
    return { id: idValue, label: data.libelle || data.entreprise || data.province || docSnap.id };
  });
};

export const fetchEntreprises = async (): Promise<SelectOption[]> => {
  return fetchOptions('entreprises');
};

export const fetchStatuses = async (): Promise<SelectOption[]> => {
  try {
    const options = await fetchOptions('statuses');
    if (options.length) return options;
  } catch (error) {
    console.error('Erreur chargement statuses:', error);
  }

  try {
    return await fetchOptions('status');
  } catch (error) {
    console.error('Erreur chargement status:', error);
    return [];
  }
};

export const applyMapSelection = async (
  draft: SignalementDraft,
  latitude: number,
  longitude: number,
  quartiers: QuartierRef[]
): Promise<SignalementDraft> => {
  const reverse = await reverseGeocode(latitude, longitude, quartiers);
  return {
    ...draft,
    latitude,
    longitude,
    quartierId: reverse.quartierId,
    quartierLabel: reverse.quartierLabel,
    province: 'Antananarivo'
  };
};

export const reverseGeocode = async (
  latitude: number,
  longitude: number,
  quartiers: QuartierRef[]
): Promise<ReverseResult> => {
  const url = new URL('https://nominatim.openstreetmap.org/reverse');
  url.searchParams.set('format', 'jsonv2');
  url.searchParams.set('lat', String(latitude));
  url.searchParams.set('lon', String(longitude));
  url.searchParams.set('zoom', '18');
  url.searchParams.set('addressdetails', '1');

  const response = await fetch(url.toString(), {
    headers: { 'Accept-Language': 'fr' }
  });

  if (!response.ok) {
    const fallback = quartiers.length ? getNearestQuartier(quartiers, latitude, longitude) : null;
    return {
      quartierId: fallback?.id || '',
      quartierLabel: fallback?.label || 'Inconnu',
      provinceId: fallback?.provinceId || 'antananarivo'
    };
  }

  const data = await response.json();
  const address = data?.address || {};
  const quartierLabel =
    address.suburb ||
    address.neighbourhood ||
    address.quarter ||
    address.village ||
    address.city_district ||
    'Inconnu';

  const match = findQuartierByLabel(quartiers, quartierLabel);
  if (match) {
    return { quartierId: match.id, quartierLabel: match.label, provinceId: match.provinceId };
  }

  if (quartiers.length) {
    const nearest = getNearestQuartier(quartiers, latitude, longitude);
    return {
      quartierId: nearest.id,
      quartierLabel: quartierLabel,
      provinceId: nearest.provinceId
    };
  }

  return {
    quartierId: '',
    quartierLabel: quartierLabel,
    provinceId: 'antananarivo'
  };
};

export const updateDraftPhotos = (draft: SignalementDraft, files: FileList | null) => {
  if (!files) return draft;
  return {
    ...draft,
    photos: Array.from(files)
  };
};

const compressImage = async (file: File, maxWidth = 1280, quality = 0.7): Promise<File> => {
  const bitmap = await createImageBitmap(file);
  const scale = Math.min(1, maxWidth / bitmap.width);
  const width = Math.round(bitmap.width * scale);
  const height = Math.round(bitmap.height * scale);

  const canvas = document.createElement('canvas');
  canvas.width = width;
  canvas.height = height;

  const ctx = canvas.getContext('2d');
  if (!ctx) {
    return file;
  }

  ctx.drawImage(bitmap, 0, 0, width, height);

  const blob: Blob = await new Promise((resolve, reject) => {
    canvas.toBlob((result) => {
      if (result) resolve(result);
      else reject(new Error('Compression echouee'));
    }, 'image/jpeg', quality);
  });

  const ext = file.name.split('.').pop() || 'jpg';
  const baseName = file.name.replace(new RegExp(`\\.${ext}$`), '');
  return new File([blob], `${baseName}_compressed.jpg`, { type: 'image/jpeg' });
};

const uploadToCloudinary = async (file: File): Promise<string> => {
  const cloudName = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME as string | undefined;
  const uploadPreset = import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET as string | undefined;

  if (!cloudName || !uploadPreset) {
    throw new Error('Cloudinary non configure');
  }

  const url = `https://api.cloudinary.com/v1_1/${cloudName}/image/upload`;
  const formData = new FormData();
  formData.append('file', file);
  formData.append('upload_preset', uploadPreset);

  const response = await fetch(url, {
    method: 'POST',
    body: formData
  });

  if (!response.ok) {
    throw new Error('Echec upload Cloudinary');
  }

  const data = await response.json();
  return data.secure_url as string;
};

export const saveSignalement = async (draft: SignalementDraft) => {
  if (!draft.latitude || !draft.longitude) {
    throw new Error('Coordonnees manquantes');
  }

  const idStatus = Number(draft.statusId) || 1;
  const idEntreprise = draft.idEntreprise ? Number(draft.idEntreprise) : null;

  const signalement = await addSignalement({
    latitude: draft.latitude,
    longitude: draft.longitude,
    quartier: draft.quartierLabel,
    province: draft.province,
    id_entreprise: idEntreprise,
    id_status: idStatus,
    surface: draft.surface,
    budget: draft.budget,
    date_: draft.date_,
    id_user: draft.idUser,
    user_email: draft.userEmail
  });

  if (draft.photos.length) {
    const compressed = await Promise.all(
      draft.photos.map((file) => compressImage(file))
    );
    const uploads = compressed.map((file) => uploadToCloudinary(file));
    const urls = await Promise.all(uploads);
    for (const url of urls) {
      await addPhotoToSignalement(signalement.docId, signalement.idSignalement, url);
    }
  }

  return signalement.docId;
};

export const listenSignalements = (
  callback: (signalements: any[]) => void,
  onError?: (error: any) => void
) => onAllSignalementsChange(callback, onError);
