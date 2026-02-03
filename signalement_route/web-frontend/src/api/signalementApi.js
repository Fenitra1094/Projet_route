const BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8081';

export async function getSignalementRecapitulatif() {
  const res = await fetch(`${BASE}/api/signalements/recapitulatif`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function getAllSignalements() {
  const res = await fetch(`${BASE}/api/signalements`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function getSignalementSummary() {
  const res = await fetch(`${BASE}/api/signalements/summary`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function syncFromFirebase() {
  const res = await fetch(`${BASE}/api/sync/from-firebase`, {
    method: 'POST',
  });
  if (!res.ok) throw new Error(await res.text());
  return res.text();
}

export async function syncToFirebase() {
  const res = await fetch(`${BASE}/api/sync/to-firebase`, {
    method: 'POST',
  });
  if (!res.ok) throw new Error(await res.text());
  return res.text();
}

export async function fullSync() {
  const res = await fetch(`${BASE}/api/sync/full`, {
    method: 'POST',
  });
  if (!res.ok) throw new Error(await res.text());
  return res.text();
}