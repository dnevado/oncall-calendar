import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
// Open Sans, la fuente del legacy, servida desde la propia aplicación (no desde Google Fonts).
import '@fontsource/open-sans/latin-400.css';
import '@fontsource/open-sans/latin-700.css';
import { App } from './App';

const root = document.getElementById('root');
if (!root) {
  throw new Error('No se encuentra el elemento #root');
}

createRoot(root).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
