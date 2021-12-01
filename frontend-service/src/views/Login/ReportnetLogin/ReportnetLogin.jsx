import { useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

import styles from './ReportnetLogin.module.scss';

import logo from 'views/_assets/images/logos/logo.png';

import { Button } from 'views/_components/Button';
import { ErrorMessage } from 'views/_components/ErrorMessage';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';
import { UserService } from 'services/UserService';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { routes } from 'conf/routes';

const ReportnetLogin = () => {
  const navigate = useNavigate();

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [errors, setErrors] = useState({ userName: '', password: '' });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [loginError, setLoginError] = useState();
  const [password, setPassword] = useState('');
  const [userName, setUserName] = useState('');

  const checkIsEmptyUserName = () =>
    userName.length === 0
      ? setErrors(previousErrors => {
          return { ...previousErrors, userName: resourcesContext.messages['loginFormUserError'] };
        })
      : setErrors(previousErrors => {
          return { ...previousErrors, userName: '' };
        });

  const checkIsEmptyPassword = () =>
    password.length === 0
      ? setErrors(previousErrors => {
          return { ...previousErrors, password: resourcesContext.messages['loginFormPasswordError'] };
        })
      : setErrors(previousErrors => {
          return { ...previousErrors, password: '' };
        });

  const checkInputs = () => {
    checkIsEmptyUserName();
    checkIsEmptyPassword();
    return errors.password === '' && errors.userName === '';
  };

  const onLogin = async () => {
    if (checkInputs()) {
      setIsSubmitting(true);
      try {
        const userObject = await UserService.oldLogin(userName, password);
        userContext.onLogin(userObject);
        navigate(getUrl(routes.DATAFLOWS));
      } catch (error) {
        console.error('ReportnetLogin - onLogin.', error);
        notificationContext.add(
          {
            type: 'USER_SERVICE_OLD_LOGIN_ERROR',
            content: {}
          },
          true
        );
        userContext.onLogout();
        const errorResponse = error.response;
        if (!isUndefined(errorResponse) && errorResponse.status === 500) {
          setLoginError(resourcesContext.messages['loginFormError']);
        }
        setIsSubmitting(false);
      }
    }
  };

  return (
    <div className="rp-container login">
      <div className={`${styles.loginBoxContainer}`}>
        <div className={`${styles.loginBox}`}>
          <div className={styles.logo}>
            <img alt="Reportnet" src={logo} />
            <h1>{resourcesContext.messages['appName']}</h1>
            {!isEmpty(loginError) && <div className={styles.error}>{loginError}</div>}
          </div>

          <form>
            <fieldset>
              <label htmlFor="userName">{resourcesContext.messages['loginUserName']}</label>
              <input
                className={errors.userName !== '' ? styles.hasErrors : null}
                id="userName"
                name="userName"
                onBlur={() => checkIsEmptyUserName()}
                onChange={e => {
                  setUserName(e.target.value);
                  setLoginError('');
                }}
                onFocus={() =>
                  setErrors(previousErrors => {
                    return { ...previousErrors, userName: '' };
                  })
                }
                placeholder={resourcesContext.messages['loginUserName']}
                type="text"
                value={userName}
              />
              {errors['userName'] !== '' && <ErrorMessage message={errors['userName']} />}
            </fieldset>

            <fieldset>
              <label htmlFor="password">{resourcesContext.messages['loginPassword']}</label>
              <input
                autoComplete="password"
                className={errors.password !== '' ? styles.hasErrors : null}
                id="password"
                name="password"
                onBlur={() => checkIsEmptyPassword()}
                onChange={e => {
                  setPassword(e.target.value);
                  setLoginError('');
                }}
                onFocus={() =>
                  setErrors(previousErrors => {
                    return { ...previousErrors, password: '' };
                  })
                }
                placeholder={resourcesContext.messages['loginPassword']}
                type="password"
                value={password}
              />
              {errors['password'] !== '' && <ErrorMessage message={errors['password']} />}
            </fieldset>

            <fieldset className={`${styles.buttonHolder}`}>
              <Button
                className="rp-btn primary"
                disabled={isSubmitting}
                id="kc-login"
                label={resourcesContext.messages['login']}
                layout="simple"
                onClick={() => onLogin()}
                type="button"
              />
            </fieldset>
          </form>
        </div>
      </div>
    </div>
  );
};

export { ReportnetLogin };
