import React from 'react';
import { createRoot } from 'react-dom/client';
import './interceptors';
import App from './App.jsx';

import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';
import './index.scss';
import './toastoverrides.scss';

const container = document.getElementById('root');
const root = createRoot(container);
root.render(<React.StrictMode><App /></React.StrictMode>);