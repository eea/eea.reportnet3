// This file will not end up inside the main application JavaScript bundle.
// Instead, it will simply be copied inside the build folder.
// The generated "index.html" will require it just before this main bundle.
// You can thus use it to define some environment variables that will
// be made available synchronously in all your JS modules under "src".
//
// Warning: this file will not be transpiled by Babel and cannot contain
// any syntax that is not yet supported by your targeted browsers.

window.env = {
  // This option can be retrieved in "src/index.js" with "window.env.REACT_APP_BACKEND".
  REACT_APP_BACKEND: 'http://reportnet3-backend.altia.es',
  //REACT_APP_BACKEND: 'http://localhost:3000',
  REACT_APP_JSON: false,
  REACT_APP_EULOGIN: false
};
