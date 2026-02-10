const BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8081';

export async function saveSettings(dureeSession, nombreTentative) {
  let res;
  try {
    res = await fetch(`${BASE}/settings`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ dureeSession: Number(dureeSession), nombreTentative: Number(nombreTentative) })
    });
  } catch (networkErr) {
    throw new Error('Network error: ' + (networkErr.message || networkErr));
  }

  const text = await res.text();
  if (!res.ok) {
    // include status code for easier debugging
    throw new Error(`HTTP ${res.status}: ${text || res.statusText}`);
  }
  try { return JSON.parse(text); } catch { return text; }
}

export async function getSettings() {
  const res = await fetch(`${BASE}/settings`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}
