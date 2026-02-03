// src/router/index.js
import { createRouter, createWebHistory } from '@ionic/vue-router';
import Login from '@/views/Login.vue';
import Carte from '@/views/Carte.vue';
import Map from '@/views/Map.vue';

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
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const user = localStorage.getItem('user');
  
  if (requiresAuth && !user) {
    next('/login');
  } else {
    next();
  }
});

export default router;