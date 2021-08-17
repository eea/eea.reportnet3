import { useEffect, useState, useContext } from 'react';

import isNil from 'lodash/isNil';

import styles from './EULogin.module.css';
import logo from 'views/_assets/images/logos/logo-spinner.gif';

import { UserContext } from 'views/_functions/Contexts/UserContext';
import { UserService } from 'services/UserService';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { routes } from 'conf/routes';
import { LocalUserStorageUtils } from 'services/_utils/LocalUserStorageUtils';

const EULogin = ({ location, history }) => {
  const [isLoading] = useState(true);
  const userContext = useContext(UserContext);
  const onLogin = async () => {
    try {
      const params = new URLSearchParams(location.hash);
      const code = params.get('code');

      if (code && !userContext.isLoggedOut) {
        const userObject = await UserService.login(code);
        userContext.onLogin(userObject);
        const rnLocalStorage = LocalUserStorageUtils.getSessionStorage();
        if (!isNil(rnLocalStorage?.redirectUrl)) {
          LocalUserStorageUtils.removeSessionStorageProperty('redirectUrl');
          window.location.href = rnLocalStorage.redirectUrl;
        } else {
          history.push(getUrl(routes.DATAFLOWS));
        }
      } else {
        history.push(getUrl(routes.ACCESS_POINT));
      }
    } catch (error) {
      console.error('EULogin - onLogin.', error);
      history.push(getUrl(routes.ACCESS_POINT));
    }
  };
  useEffect(() => {
    onLogin();
  }, []);
  return (
    <div className="rp-container">
      <div className={`${styles.loginBoxContainer}`}>
        {isLoading && <img alt="EEA logo" className={styles.logo} src={logo} />}
      </div>
    </div>
  );
};

export { EULogin };
