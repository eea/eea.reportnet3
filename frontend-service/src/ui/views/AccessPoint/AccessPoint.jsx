import React, { useEffect, useContext } from 'react';

import styles from './AccessPoint.module.css';

import logo from 'assets/images/logo.png';

import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const AccessPoint = ({ history }) => {
  const resources = useContext(ResourcesContext);
  return (
    <div className="rp-container">
      <div className={`${styles.loginBoxContainer}`}>
        <div className={`${styles.loginBox}`}>
          <div className={styles.logo}>
            <img src={logo} alt="Reportnet" />
            <h1>{resources.messages.appName}</h1>
            <h2>
              <a
                onClick={e => {
                  history.push(
                    '/eulogin?state=1489230d-c1b1-4era25-8b85-1f9a5b109171&session_state=86974635-7d00-473f-80db-acb72bce1750&code=1c012476-b252-46c4-b853-251c97f3672f.86974635-7d00-473f-80db-acb72bce1750.a598977f-b726-4a35-9211-6aefd4ac6bdc'
                  );
                }}>
                {resources.messages.linkInEulogin}
              </a>
            </h2>
          </div>
        </div>
      </div>
    </div>
  );
};
