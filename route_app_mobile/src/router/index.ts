// src/router/index.js
import { createRouter, createWebHistory } from '@ionic/vue-router';
import Login from '@/views/Login.vue';
import Carte from '@/views/Carte.vue';
import Map from '@/views/Map.vue';
import { clearSession, isSessionValid, logoutUser, startUserStatusListener } from '@/services/firebaseService';

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/map',
    name: 'Map',
    component: Map,
    meta: { requiresAuth: true }
  },
  {
    path: '/carte',
    name: 'Carte',
    component: Carte,
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// Garde de navigation
router.beforeEach(async (to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const user = localStorage.getItem('user');
  
  if (requiresAuth && !user) {
    next('/login');
    return;
    
  }

  if (requiresAuth && !isSessionValid()) {
    try {
      await logoutUser();
    } catch (error) {
      console.error('Erreur deconnexion:', error);
    }
    clearSession();
    next('/login');
    return;
  }

  if (requiresAuth && user) {
    try {
      const parsed = JSON.parse(user);
      if (parsed?.uid) startUserStatusListener(parsed.uid);
    } catch {
      clearSession();
      next('/login');
      return;
    }
  }

  next();
});

export default router;