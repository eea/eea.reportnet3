import React, { useReducer, useContext } from 'react';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { camelCase } from 'lodash';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext.js';

const leftSideBarReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ADD_HELP_STEPS':
      return {
        ...state,
        helpTitle: payload.helpTitle,
        steps: payload.steps
      };
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
    case 'SET_MENU_STATE':
      return {
        ...state,
        isLeftSideBarOpened: payload.isLeftSideBarOpened
      };
    default:
      return state;
  }
};

const LeftSideBarProvider = ({ children }) => {
  const [state, dispatch] = useReducer(leftSideBarReducer, { models: [], steps: [], helpTitle: '' });

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
        addHelpSteps: (helpTitle, steps) => {
          dispatch({
            type: 'ADD_HELP_STEPS',
            payload: { helpTitle, steps }
          });
        },
        removeModels: () => {
          dispatch({
            type: 'REMOVE_MODEL',
            payload: []
          });
        },
        setMenuState: () => {
          dispatch({
            type: 'SET_MENU_STATE',
            payload: { isLeftSideBarOpened: !state.isLeftSideBarOpened }
          });
        }
      }}>
      {children}
    </LeftSideBarContext.Provider>
  );
};
export { LeftSideBarProvider };
