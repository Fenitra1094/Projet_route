const BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8081';

export async function getAllUsers() {
  const res = await fetch(`${BASE}/api/users`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

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
}