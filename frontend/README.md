# URL Shortener — Frontend

Vue 3 + Tailwind CSS dashboard for the URL shortener API.

## Stack

- **Vue 3** (Composition API + `<script setup>`)
- **Pinia** — state management
- **Vue Router 4** — client-side routing with navigation guards
- **Axios** — HTTP client with JWT auto-refresh interceptor
- **Tailwind CSS 3** — utility-first styling with custom design tokens
- **Vite 5** — build tool + dev server proxy

## Setup

```bash
cd frontend
npm install
npm run dev       # http://localhost:5173
```

The Vite dev server proxies `/api` to `http://localhost:8080` — make sure the Spring Boot backend is running first.

## Project Structure

```
src/
├── api/
│   ├── client.js       # Axios instance — JWT attach + auto-refresh on 401
│   ├── auth.js         # Auth API calls
│   └── urls.js         # URL CRUD + analytics API calls
├── stores/
│   ├── auth.js         # Pinia — tokens, login/logout, decoded email
│   └── urls.js         # Pinia — URL list, pagination, CRUD actions
├── router/
│   └── index.js        # Routes + auth/guest navigation guards
├── views/
│   ├── LoginView.vue
│   ├── RegisterView.vue
│   ├── DashboardView.vue   # Main link management page
│   └── AnalyticsView.vue   # Per-link click analytics + bar chart
├── components/
│   ├── Navbar.vue
│   ├── UrlCard.vue          # Single link row with copy/toggle/delete/analytics
│   └── CreateLinkModal.vue  # New link form (URL + optional alias + expiry)
└── assets/
    └── main.css            # Tailwind directives + component classes
```

## Features

- JWT auth with auto token refresh (transparent to the user)
- Dashboard — paginated link list, copy short URL, toggle active, delete
- Create modal — URL, custom alias, optional expiry date
- Analytics page — total clicks, avg/day, peak day, 30-day bar chart
- Animated transitions throughout (fade-up, slide-in, page transitions)
- Fully dark themed with teal accent color system

## Build for Production

```bash
npm run build     # outputs to dist/
```

Point Spring Boot's static resource handler (or a CDN/nginx) at the `dist/` folder.
