import React, { useContext, useState } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isEmpty, isUndefined } from 'lodash';

import styles from './ReportnetLogin.module.css';

import logo from 'assets/images/logos/logo.png';

import { Button } from 'ui/views/_components/Button';
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
  const initialValues = {
    userName: '',
    password: ''
  };
  const validationSchema = Yup.object().shape({
    userName: Yup.string().required('A user name is required'),
    password: Yup.string().required('A password is required')
  });
  return (
    <div className="rp-container login">
      <div className={`${styles.loginBoxContainer}`}>
        <div className={`${styles.loginBox}`}>
          <div className={styles.logo}>
            <img src={logo} alt="Reportnet" />
            <h1>{resources.messages.appName}</h1>
            {!isEmpty(loginError) && <div className={styles.error}>{loginError}</div>}
            {/* <Link to={routes.DATAFLOWS}>cast</Link> */}
          </div>
          <Formik
            initialValues={initialValues}
            validationSchema={validationSchema}
            onSubmit={async (values, { setSubmitting }) => {
              setSubmitting(true);
              try {
                const userObject = await UserService.oldLogin(values.userName, values.password);
                userContext.onLogin(userObject);
                history.push(getUrl(routes.DATAFLOWS));
              } catch (error) {
                console.error(error);
                notificationContext.add({
                  type: 'USER_SERVICE_OLD_LOGIN_ERROR',
                  content: {}
                });
                userContext.onLogout();
                const errorResponse = error.response;
                if (!isUndefined(errorResponse) && errorResponse.status === 500) {
                  setLoginError('Incorrect username or password');
                  console.error(errorResponse.data.message);
                }
              }

              setSubmitting(false);
            }}
            render={({ setFieldValue }) => (
              <Form>
                <fieldset>
                  <label htmlFor="userName">{resources.messages.loginUserName}</label>
                  <Field
                    id={'userName'}
                    name="userName"
                    type="text"
                    placeholder={resources.messages.loginUserName}
                    onChange={e => {
                      setFieldValue('userName', e.target.value);
                      setLoginError('');
                    }}
                  />
                  <ErrorMessage className="error" name="userName" component="div" />
                </fieldset>
                <fieldset>
                  <label htmlFor="password">{resources.messages.loginPassword}</label>
                  <Field
                    id={'password'}
                    name="password"
                    type="password"
                    placeholder={resources.messages.loginPassword}
                    autoComplete="password"
                    onChange={e => {
                      setFieldValue('password', e.target.value);
                      setLoginError('');
                    }}
                  />
                  <ErrorMessage className="error" name="password" component="div" />
                </fieldset>
                <fieldset className={`${styles.buttonHolder}`}>
                  <Button layout="simple" type="submit" label={resources.messages.login} className="rp-btn primary" />
                </fieldset>
              </Form>
            )}
          />
        </div>
      </div>
    </div>
  );
};

export { ReportnetLogin };
