import { useContext } from 'react';
import { Redirect, useLocation } from 'react-router-dom';

import isNil from 'lodash/isNil';

import { AccessPointConfig } from 'repositories/config/AccessPointConfig';
import { routes } from 'conf/routes';

import { UserContext } from 'views/_functions/Contexts/UserContext';
import { LocalUserStorageUtils } from 'services/_utils/LocalUserStorageUtils';

export const PrivateRoute = ({ component: Component, componentProps = {} }) => {
  const location = useLocation();

  const userContext = useContext(UserContext);

  const isEuLogin = window.env.REACT_APP_EULOGIN.toString() === 'true';

  if (!LocalUserStorageUtils.hasToken() && isNil(userContext.id)) {
    if (isEuLogin && isNil(userContext.isLoggedOut)) {
      LocalUserStorageUtils.setPropertyToSessionStorage({ redirectUrl: window.location.href });
      window.location.href = AccessPointConfig.euloginUrl;
    } else {
      return <Redirect to={{ pathname: routes.ACCESS_POINT, state: { from: location } }} />;
    }
  } else {
    if (isEuLogin) {
      LocalUserStorageUtils.removeSessionStorageProperty('redirectUrl');
    }

    return <Component {...componentProps} />;
  }
};
