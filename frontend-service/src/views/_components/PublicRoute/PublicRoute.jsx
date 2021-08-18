import React from 'react';
import { Redirect, Route } from 'react-router-dom';

import isNil from 'lodash/isNil';

import { RouteUtils } from 'views/_functions/Utils';

export const PublicRoute = ({ component: Component, location, path, parentPath }) => {
  const { parseParentPath } = RouteUtils;

  const checkRedirect = props => {
    if (!isNil(parentPath) && path !== parentPath) {
      return (
        <Redirect
          to={{
            ...location,
            pathname: parseParentPath(parentPath, location),
            state: { from: props.location }
          }}
        />
      );
    } else {
      return <Component />;
    }
  };

  return <Route path={path} render={props => checkRedirect(props)} />;
};
