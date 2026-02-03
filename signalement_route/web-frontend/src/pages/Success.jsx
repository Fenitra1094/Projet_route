import React from 'react';

export default function Success() {
  return (
    <div style={{ padding: 20, textAlign: 'center' }}>
      <h1>Succès</h1>
      <p>Opération réussie.</p>

      <div style={{ margin: '30px 0' }}>
        <a
          href="/dashboard"
          style={{
            display: 'inline-block',
            padding: '12px 24px',
            backgroundColor: '#3498db',
            color: 'white',
            textDecoration: 'none',
            borderRadius: '6px',
            marginRight: '15px',
            fontSize: '16px',
            transition: 'background-color 0.3s'
          }}
          onMouseOver={(e) => e.target.style.backgroundColor = '#2980b9'}
          onMouseOut={(e) => e.target.style.backgroundColor = '#3498db'}
        >
          📊 Voir le récapitulatif des signalements
        </a>
      </div>

      <a href="/" style={{ color: '#7f8c8d', textDecoration: 'none' }}>Retour à l'accueil</a>
    </div>
  );
}
