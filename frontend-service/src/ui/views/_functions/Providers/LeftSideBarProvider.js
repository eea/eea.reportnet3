import React, { useReducer, useContext } from 'react';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { camelCase } from 'lodash';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext.js';

const leftSideBarReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ADD_MODEL':
      return {
        ...state,
        models: payload
      };
    case 'REMOVE_MODEL':
      return {
        ...state,
        models: []
      };
    default:
      return state;
  }
};

const LeftSideBarProvider = ({ children }) => {
  const [state, dispatch] = useReducer(leftSideBarReducer, { models: [] });

  return (
    <LeftSideBarContext.Provider
      value={{
        ...state,
        addModels: models => {
          dispatch({
            type: 'ADD_MODEL',
            payload: models
          });
        },
        removeModels: () => {
          dispatch({
            type: 'REMOVE_MODEL',
            payload: []
          });
        }
      }}>
      {children}
    </LeftSideBarContext.Provider>
  );
};
export { LeftSideBarProvider };
