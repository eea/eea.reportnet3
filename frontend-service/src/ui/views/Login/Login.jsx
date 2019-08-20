import React, { useContext } from 'react';

import styles from './Login.module.css';

import logo from 'assets/images/logo.png';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { UserService } from 'core/services/User';

const Login = ({ history }) => {
  const resources = useContext(ResourcesContext);
  return (
    <div className="rp-container">
      <div className={`${styles.loginBoxContainer}`}>
        <div className={`${styles.loginBox}`}>
          <div className={styles.logo}>
            <img src={logo} alt="Reportnet" />
            <h1>{resources.messages.appName}</h1>
          </div>
          <form>
            <fieldset>
              <label htmlFor="userName">{resources.messages.loginUserName}</label>
              <input type="text" placeholder={resources.messages.loginUserName} />
            </fieldset>
            <fieldset>
              <label htmlFor="password">{resources.messages.loginPassword}</label>
              <input type="password" placeholder={resources.messages.loginPassword} autoComplete="password" />
            </fieldset>
            <fieldset className={`${styles.buttonHolder}`}>
              <Button
                layout="simple"
                label={resources.messages.loginLogin}
                className="rp-btn primary"
                onClick={() => history.push('/data-flow-task/')}
              />
            </fieldset>
          </form>
        </div>
      </div>
    </div>
  );
};

export { Login };
