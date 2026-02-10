import React from "react";
import Login from "./components/Login";
import Dashboard from "./components/Dashboard";
import Success from "./pages/Success";
import Failed from "./pages/Failed";
import UserList from "./pages/UserList";

function App() {
  const path = window.location.pathname;

  if (path === '/success') return <Success />;
  if (path === '/failed') return <Failed />;
  if (path === '/dashboard') return <Dashboard />;
  if (path === '/users') return <UserList />;

  return (
    <div>
      <Login />
    </div>
  );
}

export default App;
