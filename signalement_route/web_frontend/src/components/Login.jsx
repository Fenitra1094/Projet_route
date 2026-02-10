import React, { useState } from "react";
import { login, register } from "../api/authApi";
import "./Login.css";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [nom, setNom] = useState("");
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
          id_role:1
        });
        window.location.pathname = '/signalements';
      } else {
        await login(email, password);
        window.location.pathname = '/signalements';
      }
    } catch (err) {
      console.error(err);
      // Afficher le vrai message d'erreur du serveur
      const errorMessage = err.message || "Erreur lors de l'opération";
      setError(errorMessage);
    }
  };

  

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

            
          </div>

          {error && <p className="login-error">{error}</p>}
        </form>
      </div>
    </div>
  );
}

export default Login;
