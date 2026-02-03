import React, { useState, useEffect } from "react";
import { login, register, getRoles } from "../api/authApi";
import "./Login.css";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [nom, setNom] = useState("");
  const [roles, setRoles] = useState([]);
  const [selectedRole, setSelectedRole] = useState(1);
  const [isRegister, setIsRegister] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    try {
      if (isRegister) {
        await register({
          email,
          password,
          nom,
          id_role: selectedRole
        });
        window.location.pathname = '/success';
      } else {
        await login(email, password);
        window.location.pathname = '/success';
      }
    } catch (err) {
      console.error(err);
      setError("Erreur lors de l'opération");
      window.location.pathname = '/failed';
    }
  };

  useEffect(() => {
    (async () => {
      try {
        const r = await getRoles();
        setRoles(r);
        if (r && r.length > 0) setSelectedRole(r[0].id_role || 1);
      } catch (e) {
        console.warn('Could not fetch roles:', e);
      }
    })();
  }, []);

  return (
    <div className="login-container">
      <div className="login-card">
        <h2 className="login-title">{isRegister ? "Inscription" : "Connexion"}</h2>

        <form className="login-form" onSubmit={handleSubmit}>
          {isRegister && (
            <>
              <input
                className="login-input"
                placeholder="Nom"
                value={nom}
                onChange={e => setNom(e.target.value)}
                required
              />

              <select className="login-select" value={selectedRole} onChange={e => setSelectedRole(Number(e.target.value))}>
                {roles.map(r => (
                  <option key={r.id_role} value={r.id_role}>{r.libelle}</option>
                ))}
              </select>
            </>
          )}

          <input
            className="login-input"
            type="email"
            placeholder="Email"
            value={email}
            onChange={e => setEmail(e.target.value)}
            required
          />

          <input
            className="login-input"
            type="password"
            placeholder="Mot de passe"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
          />

          <div className="login-actions">
            <button className="login-button" type="submit">
              {isRegister ? "S'inscrire" : "Se connecter"}
            </button>

            <button type="button" className="toggle-button" onClick={() => setIsRegister(!isRegister)}>
              {isRegister ? "Déjà un compte ? Se connecter" : "Pas de compte ? S'inscrire"}
            </button>
          </div>

          {error && <p className="login-error">{error}</p>}
        </form>
      </div>
    </div>
  );
}

export default Login;
