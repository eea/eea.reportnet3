import { useContext } from 'react';
import { Redirect, Route } from 'react-router-dom';

import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { AccessPointConfig } from 'repositories/config/AccessPointConfig';
import { routes } from 'conf/routes';

import { RouteUtils } from 'views/_functions/Utils';

import { UserContext } from 'views/_functions/Contexts/UserContext';
import { LocalUserStorageUtils } from 'services/_utils/LocalUserStorageUtils';

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

  if (window.env.REACT_APP_EULOGIN.toString() === 'true') {
    if (LocalUserStorageUtils.hasToken() || !isUndefined(userContext.id)) {
      LocalUserStorageUtils.removeSessionStorageProperty('redirectUrl');
      return <Route children={props => checkRedirect(props)} path={path} />;
    } else {
      if (isNull(userContext.isLoggedOut) || isUndefined(userContext.isLoggedOut)) {
        LocalUserStorageUtils.setPropertyToSessionStorage({ redirectUrl: window.location.href });
      }
      if (userContext.isLoggedOut) {
        return (
          <Route
            children={props => <Redirect to={{ pathname: routes.ACCESS_POINT, state: { from: props.location } }} />}
            path={path}
          />
        );
      }
      window.location.href = AccessPointConfig.euloginUrl;
    }
  } else {
    return (
      <Route
        children={props => {
          if (LocalUserStorageUtils.hasToken() || !isUndefined(userContext.id)) {
            return checkRedirect(props);
          } else {
            return <Redirect to={{ pathname: routes.ACCESS_POINT, state: { from: props.location } }} />;
          }
        }}
        path={path}
      />
    );
  }
};
