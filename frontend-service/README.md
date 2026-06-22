# EEA Reportnet Frontend

This project uses **Vite 6** and requires **Node.js 22**.

## Prerequisites

- Node.js 22
- npm

## Install

```bash
npm install
```

## Run locally

```bash
npm start
```

This starts the Vite development server on:

- http://localhost:3000

The page reloads automatically when you change files.

## Build for production

```bash
npm run build
```

This creates the production bundle in the `build/` directory.

## Preview the production build

```bash
npm run preview
```

This serves the built app locally for verification.

## Docker build

```bash
docker build -t eea-reportnet-frontend .
```

## Docker run

```bash
docker run -p 8080:80 eea-reportnet-frontend
```

## Notes

- The project is no longer using Create React App.
- `npm start` runs Vite directly.
- `npm run build` uses `vite build`.
- `npm run preview` uses `vite preview`.