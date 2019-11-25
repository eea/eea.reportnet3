import React, { useEffect, useState, useContext } from 'react';

import styles from './Eulogin.module.css';
import logo from 'assets/images/logo-spinner.gif';

import { UserContext } from 'ui/views/_components/_context/UserContext';
import { UserService } from 'core/services/User';

import { getUrl } from 'core/infrastructure/api/getUrl';
import { routes } from 'ui/routes';

export const Eulogin = ({ location, history }) => {
  const [isLoading, setIsLoading] = useState(true);
  const user = useContext(UserContext);
  const onLogin = async () => {
    try {
      const params = new URLSearchParams(location.hash);
      const code = params.get('code');
      if (code) {
        const userObject = await UserService.login(code);
        user.onLogin(userObject);
        history.push(getUrl(routes.DATAFLOWS));
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
      <div className={`${styles.loginBoxContainer}`}>{isLoading && <img className={styles.logo} src={logo} />}</div>
    </div>
  );
};
