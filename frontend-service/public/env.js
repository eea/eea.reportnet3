// This file will not end up inside the main application JavaScript bundle.
// Instead, it will simply be copied inside the build folder.
// The generated "index.html" will require it just before this main bundle.
// You can thus use it to define some environment variables that will
// be made available synchronously in all your JS modules under "src".
//
// Warning: this file will not be transpiled by Babel and cannot contain
// any syntax that is not yet supported by your targeted browsers.

window.env = {
  //SANDBOX
  REACT_APP_BACKEND: 'http://rn3sandbox-api.altia.es',
  EULOGIN_URL:
    'http://rn3dev-api.altia.es/auth/realms/Reportnet/protocol/openid-connect/auth?client_id=reportnet&redirect_uri=http%3A%2F%2Frn3dev.altia.es%2Feulogin%2F&response_mode=fragment&response_type=code&scope=openid',
  WEBSOCKET_URL: 'ws://rn3sandbox.altia.es/communication/reportnet-websocket',
  DOCUMENTATION_FOLDER: 'test'

  // BETA
  // REACT_APP_BACKEND: 'http://rn3beta-backend.altia.es',
  // EULOGIN_URL:
  //   'http://reportnet3-backend.altia.es/auth/realms/Reportnet/protocol/openid-connect/auth?client_id=reportnet&redirect_uri=http%3A%2F%2Frn3beta.altia.es%2Feulogin%2F&state=1489230d-c1b1-4e25-8b85-1f9a5b109171&response_mode=fragment&response_type=code&scope=openid',
  // WEBSOCKET_URL: 'ws://rn3sandbox.altia.es/communication/reportnet-websocket',
  // DOCUMENTATION_FOLDER: 'test'
};