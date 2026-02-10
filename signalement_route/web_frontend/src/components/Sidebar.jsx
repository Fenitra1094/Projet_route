import React, { useState } from 'react';
import './Sidebar.css';

function Sidebar() {
    const [expandedMenu, setExpandedMenu] = useState(null);
    const path = window.location.pathname;

    const toggleMenu = (menu) => {
        setExpandedMenu(expandedMenu === menu ? null : menu);
    };

    const handleNavigation = (path) => {
        window.location.pathname = path;
    };

    const isActive = (checkPath) => path === checkPath;

    return (
        <div className="sidebar">
            <div className="sidebar-header">
                <h2>Travaux</h2>
            </div>

            <nav className="sidebar-nav">
                {/* Dashboard */}
                <div className="nav-item">
                    <button 
                        className={`nav-link ${isActive('/dashboard') ? 'active' : ''}`}
                        onClick={() => handleNavigation('/dashboard')}
                    >
                        <span className="nav-icon">📊</span>
                        <span>Dashboard</span>
                    </button>
                </div>

                {/* Settings */}
                <div className="nav-item">
                    <button 
                        className={`nav-link ${isActive('/settings') ? 'active' : ''}`}
                        onClick={() => handleNavigation('/settings')}
                    >
                        <span className="nav-icon">⚙️</span>
                        <span>Paramètres</span>
                    </button>
                </div>

                {/* Utilisateurs with dropdown */}
                <div className="nav-item">
                    <button 
                        className={`nav-link dropdown-toggle ${expandedMenu === 'utilisateurs' ? 'expanded' : ''}`}
                        onClick={() => toggleMenu('utilisateurs')}
                    >
                        <span className="nav-icon">👤</span>
                        <span>Utilisateurs</span>
                        <span className="dropdown-arrow">▼</span>
                    </button>
                    {expandedMenu === 'utilisateurs' && (
                        <div className="submenu">
                            <button 
                                className="submenu-link"
                                onClick={() => handleNavigation('/users')}
                            >
                                <span>Liste utilisateurs</span>
                            </button>
                            <button 
                                className="submenu-link"
                                onClick={() => handleNavigation('/users/new')}
                            >
                                <span>+ Nouveau</span>
                            </button>
                            <button 
                                className="submenu-link"
                                onClick={() => handleNavigation('/users/blocked')}
                            >
                                <span>Liste bloquée</span>
                            </button>
                        </div>
                    )}
                </div>

                {/* Signalements with dropdown */}
                <div className="nav-item">
                    <button 
                        className={`nav-link dropdown-toggle ${expandedMenu === 'signalements' ? 'expanded' : ''}`}
                        onClick={() => toggleMenu('signalements')}
                    >
                        <span className="nav-icon">📋</span>
                        <span>Signalements</span>
                        <span className="dropdown-arrow">▼</span>
                    </button>
                    {expandedMenu === 'signalements' && (
                        <div className="submenu">
                            <button 
                                className="submenu-link"
                                onClick={() => handleNavigation('/signalements')}
                            >
                                <span>Liste signalements</span>
                            </button>
                        </div>
                    )}
                </div>

                

                {/* Statuts */}
                <div className="nav-item">
                    <button 
                        className={`nav-link ${isActive('/status') ? 'active' : ''}`}
                        onClick={() => handleNavigation('/status')}
                    >
                        <span className="nav-icon">📌</span>
                        <span>Statuts</span>
                    </button>
                </div>
            </nav>

            <div className="sidebar-footer">
                <button 
                    className="logout-btn"
                    onClick={() => {
                        localStorage.clear();
                        window.location.pathname = '/';
                    }}
                >
                    Déconnexion
                </button>
            </div>
        </div>
    );
}

export default Sidebar;
