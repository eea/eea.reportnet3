import React from 'react';
export const AccessPointWeConfig = {
  euloginUrl:
    window.env.EULOGIN_URL ||
    'http://reportnet3-backend.altia.es/auth/realms/Reportnet/protocol/openid-connect/auth?client_id=reportnet&redirect_uri=http%3A%2F%2Freportnet3.altia.es%2Feulogin%2F&response_mode=fragment&response_type=code&scope=openid'
};
