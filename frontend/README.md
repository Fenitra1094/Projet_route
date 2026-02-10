# React + TypeScript + Vite

This is a React + TypeScript + Vite-based frontend for the Signalements Routiers application (formerly Vue 3).

## Project Overview

Frontend application for displaying and managing road signalement (problem reports) with:
- **Leaflet Maps** - Interactive map visualization of signalements
- **React Components** - App.tsx (main container), MapView.tsx (map management), PhotoModal.tsx (photo viewer)
- **Axios** - API communication with backend
- **Responsive Design** - Sidebar navigation, topbar with statistics, modal dialogs

## Setup & Running

```bash
# Install dependencies
npm install

# Development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

## Key Components

- **App.tsx** - Main application container with state management
- **MapView.tsx** - Leaflet map component with marker management
- **PhotoModal.tsx** - Modal dialog for viewing signalement photos
- **style.css** - Unified styling for all components

## Architecture Notes

Converted from Vue 3 to React while maintaining the same functionality:
- Vue's `ref` and `watch` → React's `useState` and `useEffect`
- Vue's `emit` → React's callback props
- Vue's `defineExpose` → React's `forwardRef` and `useImperativeHandle`
- Template syntax → TSX/JSX

