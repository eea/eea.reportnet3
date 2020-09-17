import React, { useEffect, useState, useContext } from 'react';

import styles from './EULogin.module.css';
import logo from 'assets/images/logo-spinner.gif';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserService } from 'core/services/User';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';
import { LocalStorageUtils } from 'ui/views/_functions/Utils';
import { isNil } from 'lodash';

const EULogin = ({ location, history }) => {
  const [isLoading] = useState(true);
  const userContext = useContext(UserContext);
  const onLogin = async () => {
    try {
      const params = new URLSearchParams(location.hash);
      const code = params.get('code');

      if (code) {
        const userObject = await UserService.login(code);
        userContext.onLogin(userObject);
        const rnLocalStorage = LocalStorageUtils.get();
        let redirectUrl = getUrl(routes.DATAFLOWS);
        if (!isNil(rnLocalStorage)) {
          redirectUrl = rnLocalStorage.redirectUrl;
          LocalStorageUtils.remove();
        }
        history.push(redirectUrl);
      } else {
        history.push(getUrl(routes.ACCESS_POINT));
      }
    } catch (error) {
      console.error('error: ', error);
      history.push(getUrl(routes.ACCESS_POINT));
    }
  };
  useEffect(() => {
    onLogin();
  }, []);
  return (
    <div className="rp-container">
      <div className={`${styles.loginBoxContainer}`}>
        {isLoading && <img className={styles.logo} alt="EEA logo" src={logo} />}
      </div>
    </div>
  );
};

export { EULogin };
