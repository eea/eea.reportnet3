import React, { useContext } from 'react';
import { Redirect, Route } from 'react-router-dom';

import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { AccessPointWebConfig } from 'conf/domain/model/AccessPoint/AccessPoint.web.config';
import { routes } from 'ui/routes';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const PrivateRoute = ({ component: Component, path }) => {
  const userContext = useContext(UserContext);

  if (window.env.REACT_APP_EULOGIN.toString() == 'true') {
    if (userStorage.hasToken() || !isUndefined(userContext.id)) {
      userStorage.removeLocalProperty('redirectUrl');
      return <Route path={path} render={() => <Component />} />;
    } else {
      if (isNull(userContext.isLoggedOut) || isUndefined(userContext.isLoggedOut)) {
        userStorage.setPropertyToLocalStorage({ redirectUrl: window.location.href });
      }
      if (userContext.isLoggedOut) {
        return (
          <Route
            path={path}
            render={props => (
              <Redirect
                to={{
                  pathname: routes.ACCESS_POINT,
                  state: { from: props.location }
                }}
              />
            )}
          />
        );
      }
      window.location.href = AccessPointWebConfig.euloginUrl;
    }
  } else {
    return (
      <Route
        path={path}
        render={props =>
          userStorage.hasToken() || !isUndefined(userContext.id) ? (
            <Component />
          ) : (
            <Redirect
              to={{
                pathname: routes.ACCESS_POINT,
                state: { from: props.location }
              }}
            />
          )
        }
      />
    );
  }
};
