const BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8081';

// export async function login(email, password) {
//   const res = await fetch(`${BASE}/auth/login`, {
//     method: 'POST',
//     headers: { 'Content-Type': 'application/json' },
//     body: JSON.stringify({ email, password })
//   });

//   if (!res.ok) throw new Error(await res.text());
//   return res.json();
// }

// export async function register(user) {
//   const res = await fetch(`${BASE}/auth/register`, {
//     method: 'POST',
//     headers: { 'Content-Type': 'application/json' },
//     body: JSON.stringify(user)
//   });

//   if (!res.ok) throw new Error(await res.text());
//   return res.json();
// }

export async function syncNow() {
  const res = await fetch(`${BASE}/auth/sync`, { method: 'POST' });
  const text = await res.text();
  if (!res.ok) throw new Error(text);
  return text;
}

export async function getRoles() {
  const res = await fetch(`${BASE}/api/roles`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export function installOnlineHandler() {
  window.addEventListener('online', async () => {
    console.log('Network regained, attempting backend sync...');
    try {
      const result = await syncNow();
      console.log('Sync result:', result);
    } catch (e) {
      console.warn('Sync on online failed:', e);
    }
  });

  // try immediate sync at startup if online
  if (navigator.onLine) {
    syncNow().catch(() => {});
  }
}


// ===== LOGIN =====
export function login(email, password) {
  return fetch(`${BASE}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password })
  }).then(async res => {
    if (!res.ok) {
      throw new Error(await res.text());
    }
    return res.json();
  });
}

// ===== REGISTER =====
export function register(user) {
  return fetch(`${BASE}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(user)
  }).then(async res => {
    if (!res.ok) {
      throw new Error(await res.text());
    }
    return res.json();
  });
}

// ===== BLOCKED USERS =====
export function getBlocked() {
  return fetch(`${BASE}/auth/blocked`).then(async res => {
    if (!res.ok) {
      throw new Error(await res.text());
    }
    return res.json();
  });
}

export function unblockUsers(ids) {
  return fetch(`${BASE}/auth/unblock`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(ids)
  }).then(async res => {
    const text = await res.text();
    if (!res.ok) throw new Error(text);
    return text;
  });
}
