import React, { useContext } from 'react';

import styles from './AccessPoint.module.css';

import { AccessPointWebConfig } from 'conf/domain/model/AccessPoint/AccessPoint.web.config';

import logo from 'assets/images/logo.png';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const AccessPoint = () => {
  const resources = useContext(ResourcesContext);
  return (
    <div className="rp-container">
      <div className={`${styles.loginBoxContainer}`}>
        <div className={`${styles.loginBox}`}>
          <div className={styles.logo}>
            <img src={logo} alt="Reportnet" />
            <h1>{resources.messages.appName}</h1>
            <h2>
              <a href={AccessPointWebConfig.euloginUrl}>{resources.messages.linkInEulogin}</a>
            </h2>
          </div>
        </div>
      </div>
    </div>
  );
};
