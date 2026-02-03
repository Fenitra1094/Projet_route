import React from "react";
import Login from "./components/Login";
import Dashboard from "./components/Dashboard";
import Success from "./pages/Success";
import Failed from "./pages/Failed";

function App() {
  const path = window.location.pathname;

  if (path === '/success') return <Success />;
  if (path === '/failed') return <Failed />;
  if (path === '/dashboard') return <Dashboard />;

  return (
    <div>
      <Login />
    </div>
  );
}

export default App;
