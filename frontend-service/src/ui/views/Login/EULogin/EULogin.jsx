import { useEffect, useState, useContext } from 'react';

import isNil from 'lodash/isNil';

import styles from './EULogin.module.css';
import logo from 'assets/images/logos/logo-spinner.gif';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserService } from 'core/services/User';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';
import { userStorage } from 'core/domain/model/User/UserStorage';

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
        const rnLocalStorage = userStorage.getSessionStorage();
        if (!isNil(rnLocalStorage?.redirectUrl)) {
          userStorage.removeSessionStorageProperty('redirectUrl');
          window.location.href = rnLocalStorage.redirectUrl;
        } else {
          history.push(getUrl(routes.DATAFLOWS));
        }
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
        {isLoading && <img alt="EEA logo" className={styles.logo} src={logo} />}
      </div>
    </div>
  );
};

export { EULogin };
