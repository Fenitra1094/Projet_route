export type SignalementDraft = {
  date_: string;
  surface: string;
  budget: string;
  userId: string;
  entrepriseId: string;
  quartierId: string;
  quartierLabel: string;
  statusId: string;
  latitude: number | null;
  longitude: number | null;
  provinceId: string;
};

type QuartierRef = {
  id: string;
  label: string;
  latitude: number;
  longitude: number;
  provinceId: string;
};

type SelectOption = {
  id: string;
  label: string;
};

const QUARTIERS: QuartierRef[] = [
  {
    id: 'analakely',
    label: 'Analakely',
    latitude: -18.9087,
    longitude: 47.5266,
    provinceId: 'antananarivo'
  },
  {
    id: 'anosy',
    label: 'Anosy',
    latitude: -18.9212,
    longitude: 47.5158,
    provinceId: 'antananarivo'
  },
  {
    id: 'ivandry',
    label: 'Ivandry',
    latitude: -18.8695,
    longitude: 47.5453,
    provinceId: 'antananarivo'
  }
];

const ENTREPRISES: SelectOption[] = [
  { id: 'socobis', label: 'Socobis' },
  { id: 'jirama', label: 'Jirama' },
  { id: 'telma', label: 'Telma' }
];

const STATUSES: SelectOption[] = [
  { id: 'nouveau', label: 'Nouveau' },
  { id: 'en_cours', label: 'En cours' },
  { id: 'termine', label: 'Termine' }
];

const getNearestQuartier = (latitude: number, longitude: number): QuartierRef => {
  let best = QUARTIERS[0];
  let bestScore = Number.POSITIVE_INFINITY;

  for (const quartier of QUARTIERS) {
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

export const createSignalementDraft = (userId: string): SignalementDraft => ({
  date_: new Date().toISOString(),
  surface: '',
  budget: '',
  userId,
  entrepriseId: '',
  quartierId: '',
  quartierLabel: '',
  statusId: 'nouveau',
  latitude: null,
  longitude: null,
  provinceId: 'antananarivo'
});

export const applyMapSelection = (
  draft: SignalementDraft,
  latitude: number,
  longitude: number
): SignalementDraft => {
  const quartier = getNearestQuartier(latitude, longitude);
  return {
    ...draft,
    latitude,
    longitude,
    quartierId: quartier.id,
    quartierLabel: quartier.label,
    provinceId: quartier.provinceId
  };
};

export const getEntreprises = () => [...ENTREPRISES];
export const getStatuses = () => [...STATUSES];
export const getQuartiers = () => [...QUARTIERS];
