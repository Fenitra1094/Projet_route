import React, { useEffect, useState } from 'react';
import { getBlocked, unblockUsers } from '../api/authApi';
import './BlockedUsers.css';

export default function BlockedUsers() {
  const [users, setUsers] = useState([]);
  const [selected, setSelected] = useState(new Set());
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const load = async () => {
    setLoading(true);
    setMessage('');
    try {
      const data = await getBlocked();
      setUsers(data || []);
      setSelected(new Set());
    } catch (e) {
      setMessage(e.message || 'Erreur chargement');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const toggle = (id) => {
    const s = new Set(selected);
    if (s.has(id)) s.delete(id);
    else s.add(id);
    setSelected(s);
  };

  const handleUnblock = async () => {
    if (selected.size === 0) {
      setMessage('Sélectionnez au moins un utilisateur');
      return;
    }
    setLoading(true);
    setMessage('');
    try {
      const ids = Array.from(selected);
      const res = await unblockUsers(ids);
      setMessage(res || 'Opération terminée');
      await load();
    } catch (e) {
      setMessage(e.message || 'Erreur lors du déblocage');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="blocked-container">
      <h1>Utilisateurs bloqués</h1>
      {message && <div className="message">{message}</div>}
      {loading && <div className="loading">Chargement...</div>}
      {!loading && users.length === 0 && <div className="empty">Aucun utilisateur bloqué.</div>}
      {!loading && users.length > 0 && (
        <div>
          <table className="blocked-table">
            <thead>
              <tr>
                <th className="col-check"></th>
                <th>Email</th>
                <th>Nom</th>
                <th>Rôle</th>
              </tr>
            </thead>
            <tbody>
              {users.map(u => (
                <tr key={u.id_user}>
                  <td className="col-check">
                    <input type="checkbox" checked={selected.has(u.id_user)} onChange={() => toggle(u.id_user)} />
                  </td>
                  <td>{u.email}</td>
                  <td>{u.nom || ''}</td>
                  <td>{u.id_role || ''}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="actions">
            <button className="btn" onClick={handleUnblock} disabled={loading}>Débloquer la sélection</button>
            <button className="btn btn-secondary" onClick={load} disabled={loading}>Rafraîchir</button>
          </div>
        </div>
      )}
    </div>
  );
}
