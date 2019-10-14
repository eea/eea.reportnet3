import React from 'react';
export const AccessPointWebConfig = {
  euloginUrl:
    window.env.EULOGIN_URL ||
    `${window.env.REACT_APP_BACKEND}/auth/realms/Reportnet/protocol/openid-connect/auth?client_id=reportnet&redirect_uri=http%3A%2F%2Frn3beta.altia.es%2Feulogin%2F&state=1489230d-c1b1-4e25-8b85-1f9a5b109171&response_mode=fragment&response_type=code&scope=openid`
};
