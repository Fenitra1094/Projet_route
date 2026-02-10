import React from 'react';

export default function Failed() {
  return (
    <div style={{ padding: 20 }}>
      <h1>Échec</h1>
      <p>Opération échouée. Veuillez réessayer.</p>
      <a href="/">Retour</a>
    </div>
  );
}
