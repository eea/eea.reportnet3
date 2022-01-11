import { useEffect, useState, useContext } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

import isNil from 'lodash/isNil';

import styles from './EULogin.module.css';
import logo from 'views/_assets/images/logos/logo-spinner.gif';

import { UserContext } from 'views/_functions/Contexts/UserContext';
import { UserService } from 'services/UserService';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { routes } from 'conf/routes';
import { LocalUserStorageUtils } from 'services/_utils/LocalUserStorageUtils';

const EULogin = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const userContext = useContext(UserContext);

  const [isLoading] = useState(true);

  useEffect(() => {
    onLogin();
  }, []);

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
          navigate(getUrl(routes.DATAFLOWS));
        }
      } else {
        navigate(getUrl(routes.ACCESS_POINT));
      }
    } catch (error) {
      console.error('EULogin - onLogin.', error);
      navigate(getUrl(routes.ACCESS_POINT));
    }
  };

  return (
    <div className="rp-container">
      <div className={`${styles.loginBoxContainer}`}>
        {isLoading && <img alt="EEA logo" className={styles.logo} src={logo} />}
      </div>
    </div>
  );
};

export { EULogin };
