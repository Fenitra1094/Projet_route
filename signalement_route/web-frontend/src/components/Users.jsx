import React, { useState, useEffect } from 'react';
import { getAllUsers, updateUser } from '../api/userApi';
import './Users.css';

function Users() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editingUser, setEditingUser] = useState(null);
  const [editForm, setEditForm] = useState({
    email: '',
    nom: '',
    prenom: '',
    id_role: '',
    synced: false
  });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const data = await getAllUsers();
      setUsers(data);
      setError(null);
    } catch (err) {
      setError(err.message);
      console.error('Erreur lors du chargement des utilisateurs:', err);
    } finally {
      setLoading(false);
    }
  };

  const startEditing = (user) => {
    setEditingUser(user.id_user);
    setEditForm({
      email: user.email,
      nom: user.nom,
      prenom: user.prenom,
      id_role: user.id_role,
      synced: user.synced
    });
  };

  const cancelEditing = () => {
    setEditingUser(null);
    setEditForm({
      email: '',
      nom: '',
      prenom: '',
      id_role: '',
      synced: false
    });
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setEditForm(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const saveUser = async () => {
    try {
      setSaving(true);
      const updatedUser = await updateUser(editingUser, editForm);
      setUsers(prev => prev.map(user => 
        user.id_user === editingUser ? updatedUser : user
      ));
      setEditingUser(null);
      setError(null);
    } catch (err) {
      setError('Erreur lors de la sauvegarde: ' + err.message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="users-container">
        <div className="loading">Chargement des utilisateurs...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="users-container">
        <div className="error">
          <h2>Erreur de chargement</h2>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="users-container">
      <h1>👥 Gestion des Utilisateurs</h1>

      <div className="users-section">
        <h2>Liste des Utilisateurs ({users.length})</h2>

        {users.length > 0 ? (
          <div className="table-container">
            <table className="users-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Email</th>
                  <th>Nom</th>
                  <th>Prénom</th>
                  <th>Rôle</th>
                  <th>Statut Sync</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id_user}>
                    <td className="user-id">#{user.id_user}</td>
                    {editingUser === user.id_user ? (
                      <>
                        <td>
                          <input
                            type="email"
                            name="email"
                            value={editForm.email}
                            onChange={handleInputChange}
                            className="edit-input"
                          />
                        </td>
                        <td>
                          <input
                            type="text"
                            name="nom"
                            value={editForm.nom}
                            onChange={handleInputChange}
                            className="edit-input"
                          />
                        </td>
                        <td>
                          <input
                            type="text"
                            name="prenom"
                            value={editForm.prenom}
                            onChange={handleInputChange}
                            className="edit-input"
                          />
                        </td>
                        <td>
                          <select
                            name="id_role"
                            value={editForm.id_role}
                            onChange={handleInputChange}
                            className="edit-select"
                          >
                            <option value={1}>Admin</option>
                            <option value={2}>Utilisateur</option>
                          </select>
                        </td>
                        <td>
                          <label className="checkbox-label">
                            <input
                              type="checkbox"
                              name="synced"
                              checked={editForm.synced}
                              onChange={handleInputChange}
                            />
                            Synchronisé
                          </label>
                        </td>
                        <td>
                          <button onClick={saveUser} className="save-btn" disabled={saving}>
                            {saving ? '💾' : '✓'}
                          </button>
                          <button onClick={cancelEditing} className="cancel-btn">
                            ✕
                          </button>
                        </td>
                      </>
                    ) : (
                      <>
                        <td className="user-email">{user.email}</td>
                        <td className="user-name">{user.nom}</td>
                        <td className="user-name">{user.prenom}</td>
                        <td className="user-role">
                          {user.id_role === 1 ? 'Admin' : 
                           user.id_role === 2 ? 'Utilisateur' : 
                           `Rôle ${user.id_role}`}
                        </td>
                        <td>
                          <span className={`user-synced ${user.synced ? 'yes' : 'no'}`}>
                            {user.synced ? '✓ Synchronisé' : '✗ Non synchronisé'}
                          </span>
                        </td>
                        <td>
                          <button onClick={() => startEditing(user)} className="edit-btn">
                            ✏️ Modifier
                          </button>
                        </td>
                      </>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="no-data">Aucun utilisateur trouvé dans la base de données</p>
        )}
      </div>

      <div className="actions">
        <button onClick={loadUsers} className="refresh-btn">
          🔄 Actualiser la liste
        </button>
        <a href="/dashboard" className="back-link">📊 Retour au Dashboard</a>
        <a href="/" className="back-link">🏠 Retour à l'accueil</a>
      </div>
    </div>
  );
}

export default Users;