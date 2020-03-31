import React, { useReducer } from 'react';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext.js';

const breadCrumbReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ADD_MODEL':
      return {
        ...state,
        model: payload
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
        }
      }}>
      {children}
    </BreadCrumbContext.Provider>
  );
};
export { BreadCrumbProvider };
