import React from "react";
import Login from "./components/Login";
import Dashboard from "./components/Dashboard";
import BlockedUsers from "./pages/BlockedUsers";
import Settings from "./pages/Settings";
import SignalementList from "./pages/SignalementList";
import NewUser from "./pages/NewUser";
import Layout from "./components/Layout";
import UserList from "./pages/UserList";
import Success from "./pages/Success";
import Failed from "./pages/Failed";

function App() {
  const path = window.location.pathname;

  // Pages without sidebar (login)
  if (path === '/' || path === '/success' || path === '/failed') return <Login />;

  // Pages with sidebar
  return (
    <Layout>
      {path === '/dashboard' && <Dashboard />}
      {path === '/settings' && <Settings />}
      {path === '/users' && <UserList />}
      {path === '/users/new' && <NewUser />}
      {path === '/users/blocked' && <BlockedUsers />}
      {path === '/signalements' && <SignalementList />}
      {/* Default page */}
      {!path.startsWith('/dashboard') && 
       !path.startsWith('/settings') && 
       !path.startsWith('/users') && 
       !path.startsWith('/signalements') && 
       <Dashboard />}
    </Layout>
  );
}

export default App;
