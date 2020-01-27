import React, { useReducer, useContext } from 'react';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { camelCase } from 'lodash';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext.js';

const breadCrumbReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ADD_MODEL':
      return {
        ...state,
        model: payload
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

const BreadCrumbProvider = ({ children }) => {
  const [state, dispatch] = useReducer(breadCrumbReducer, { model: [], isLeftSideBarOpened: false });

  return (
    <BreadCrumbContext.Provider
      value={{
        ...state,
        add: model => {
          dispatch({
            type: 'ADD_MODEL',
            payload: model
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
    </BreadCrumbContext.Provider>
  );
};
export { BreadCrumbProvider };
