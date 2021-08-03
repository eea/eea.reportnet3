import { useContext, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

import styles from './ReportnetLogin.module.scss';

import logo from 'assets/images/logos/logo.png';

import { Button } from 'ui/views/_components/Button';
import { ErrorMessage } from 'ui/views/_components/ErrorMessage';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserService } from 'core/services/User';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

const ReportnetLogin = ({ history }) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);
  const notificationContext = useContext(NotificationContext);
  const [loginError, setLoginError] = useState();
  const [userName, setUserName] = useState('');
  const [password, setPassword] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errors, setErrors] = useState({ userName: '', password: '' });

  const checkIsEmptyUserName = () =>
    userName.length === 0
      ? setErrors(previousErrors => {
          return { ...previousErrors, userName: resources.messages['loginFormUserError'] };
        })
      : setErrors(previousErrors => {
          return { ...previousErrors, userName: '' };
        });

  const checkIsEmptyPassword = () =>
    password.length === 0
      ? setErrors(previousErrors => {
          return { ...previousErrors, password: resources.messages['loginFormPasswordError'] };
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
        history.push(getUrl(routes.DATAFLOWS));
      } catch (error) {
        console.error('ReportnetLogin - onLogin', error);
        notificationContext.add({
          type: 'USER_SERVICE_OLD_LOGIN_ERROR',
          content: {}
        });
        userContext.onLogout();
        const errorResponse = error.response;
        if (!isUndefined(errorResponse) && errorResponse.status === 500) {
          setLoginError(resources.messages['loginFormError']);
        }
      }
      setIsSubmitting(false);
    }
  };

  return (
    <div className="rp-container login">
      <div className={`${styles.loginBoxContainer}`}>
        <div className={`${styles.loginBox}`}>
          <div className={styles.logo}>
            <img alt="Reportnet" src={logo} />
            <h1>{resources.messages['appName']}</h1>
            {!isEmpty(loginError) && <div className={styles.error}>{loginError}</div>}
          </div>

          <form>
            <fieldset>
              <label htmlFor="userName">{resources.messages['loginUserName']}</label>
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
                placeholder={resources.messages['loginUserName']}
                type="text"
                value={userName}
              />
              {errors['userName'] !== '' && <ErrorMessage message={errors['userName']} />}
            </fieldset>

            <fieldset>
              <label htmlFor="password">{resources.messages['loginPassword']}</label>
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
                placeholder={resources.messages['loginPassword']}
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
                label={resources.messages['login']}
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
