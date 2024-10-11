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
  // REACT_APP_BACKEND: 'http://localhost:3000',

  /**
   * DEV
   */
  // REACT_APP_BACKEND: 'https://dev-api.reportnet.europa.eu',
  // EULOGIN_URL:
  //     'https://dev-auth.reportnet.europa.eu/auth/realms/Reportnet/protocol/openid-connect/auth?client_id=reportnet&redirect_uri=https%3A%2F%2Fdev.reportnet.europa.eu%2Feulogin%2F&response_mode=fragment&response_type=code&scope=openid',
  // WEBSOCKET_URL: 'wss://dev.reportnet.europa.eu/communication/reportnet-websocket',

  /**
   * SANDBOX
   */
  REACT_APP_BACKEND: 'https://sandbox-api.reportnet.europa.eu',
  EULOGIN_URL:
    'https://sandbox-auth.reportnet.europa.eu/auth/realms/Reportnet/protocol/openid-connect/auth?client_id=reportnet&redirect_uri=https%3A%2F%2Fsandbox.reportnet.europa.eu%2Feulogin%2F&response_mode=fragment&response_type=code&scope=openid',
  WEBSOCKET_URL: 'wss://sandbox.reportnet.europa.eu/communication/reportnet-websocket',

  /**
   * TEST
   */
  // REACT_APP_BACKEND: 'https://test-api.reportnet.europa.eu',
  // EULOGIN_URL:
  //     'https://test-auth.reportnet.europa.eu/auth/realms/Reportnet/protocol/openid-connect/auth?client_id=reportnet&redirect_uri=https%3A%2F%2Ftest.reportnet.europa.eu%2Feulogin%2F&response_mode=fragment&response_type=code&scope=openid',
  // WEBSOCKET_URL: 'wss://test.reportnet.europa.eu/communication/reportnet-websocket',

  REACT_APP_EULOGIN: false,
  DOCUMENTATION_FOLDER: 'test'
};
