import React, { useContext } from 'react';
import { Redirect, Route } from 'react-router-dom';

import { isUndefined } from 'lodash';

import { AccessPointWebConfig } from 'conf/domain/model/AccessPoint/AccessPoint.web.config';
import { routes } from 'ui/routes';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { userStorage } from 'core/domain/model/User/UserStorage';

import { LocalStorageUtils } from 'ui/views/_functions/Utils';

export const PrivateRoute = ({ component: Component, path }) => {
  const userContext = useContext(UserContext);

  if (window.env.REACT_APP_EULOGIN.toString() == 'true') {
    if (userStorage.hasToken() || !isUndefined(userContext.id)) {
      return <Component />;
    } else {
      LocalStorageUtils.set({ redirectUrl: path });
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
