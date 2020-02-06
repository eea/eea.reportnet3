import React, { useContext } from 'react';
import { Redirect, Route } from 'react-router-dom';

import { isUndefined } from 'lodash';

import { routes } from 'ui/routes';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const PrivateRoute = ({ component: Component, path }) => {
  const user = useContext(UserContext);
  return (
    <Route
      path={path}
      render={props =>
        userStorage.hasToken() || !isUndefined(user.id) ? (
          <Component />
        ) : (
          <Redirect
            to={{
              pathname: routes.LOGIN,
              state: { from: props.location }
            }}
          />
        )
      }
    />
  );
};
