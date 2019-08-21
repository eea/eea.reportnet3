import React, { useContext } from 'react';
import { Redirect, Route } from 'react-router-dom';
import { routes } from 'ui/routes';
import { UserContext } from 'ui/views/_components/_context/UserContext';

export const PrivateRoute = ({ component: Component, ...rest }) => {
  const user = useContext(UserContext);
  return (
    <Route
      {...rest}
      render={props =>
        user.logged ? (
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
