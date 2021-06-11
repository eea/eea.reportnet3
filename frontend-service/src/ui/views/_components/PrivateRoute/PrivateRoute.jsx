import { useContext } from 'react';
import { Redirect, Route } from 'react-router-dom';

import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { AccessPointWebConfig } from 'conf/domain/model/AccessPoint/AccessPoint.web.config';
import { routes } from 'ui/routes';

import { RouteUtils } from 'ui/views/_functions/Utils';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const PrivateRoute = ({ component: Component, componentProps = {}, location, parentPath, path }) => {
  const userContext = useContext(UserContext);

  const { parseParentPath } = RouteUtils;

  const checkRedirect = props => {
    if (!isNil(parentPath) && path !== parentPath) {
      return (
        <Redirect
          to={{ ...location, pathname: parseParentPath(parentPath, location), state: { from: props.location } }}
        />
      );
    } else {
      return <Component {...props} {...componentProps} />;
    }
  };

  if (window.env.REACT_APP_EULOGIN.toString() == 'true') {
    if (userStorage.hasToken() || !isUndefined(userContext.id)) {
      userStorage.removeSessionStorageProperty('redirectUrl');
      return <Route path={path} render={props => checkRedirect(props)} />;
    } else {
      if (isNull(userContext.isLoggedOut) || isUndefined(userContext.isLoggedOut)) {
        userStorage.setPropertyToSessionStorage({ redirectUrl: window.location.href });
      }
      if (userContext.isLoggedOut) {
        return (
          <Route
            path={path}
            render={props => <Redirect to={{ pathname: routes.ACCESS_POINT, state: { from: props.location } }} />}
          />
        );
      }
      window.location.href = AccessPointWebConfig.euloginUrl;
    }
  } else {
    return (
      <Route
        path={path}
        render={props => {
          if (userStorage.hasToken() || !isUndefined(userContext.id)) {
            return checkRedirect(props);
          } else {
            return <Redirect to={{ pathname: routes.ACCESS_POINT, state: { from: props.location } }} />;
          }
        }}
      />
    );
  }
};
