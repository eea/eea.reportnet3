import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import * as serviceWorker from './serviceWorker';
import './interceptors.js';

import 'primereact/resources/primereact.min.css';
import './styles.css';
import 'primeicons/primeicons.css';
import './index.css';
import './reportnet.css';

var { registerObserver } = require('react-perf-devtool');
registerObserver();

ReactDOM.render(<App />, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
