import React, { useEffect, useContext } from 'react';

import styles from './AccessPoint.module.css';

import logo from 'assets/images/logo.png';

import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const AccessPoint = ({ history }) => {
  //comentario tonto
  const resources = useContext(ResourcesContext);
  return (
    <div className="rp-container">
      <div className={`${styles.loginBoxContainer}`}>
        <div className={`${styles.loginBox}`}>
          <div className={styles.logo}>
            <img src={logo} alt="Reportnet" />
            <h1>{resources.messages.appName}</h1>
            <h2>
              <a href="http://reportnet3-backend.altia.es/auth/realms/Reportnet/protocol/openid-connect/auth?client_id=reportnet&redirect_uri=http%3A%2F%2Freportnet3.altia.es%2Feulogin%2F&response_mode=fragment&response_type=code&scope=openid">
                {resources.messages.linkInEulogin}
              </a>
            </h2>
          </div>
        </div>
      </div>
    </div>
  );
};
