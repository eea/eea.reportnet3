import { useReducer } from 'react';

import { BreadCrumbContext } from 'views/_functions/Contexts/BreadCrumbContext';

const breadCrumbReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ADD_MODEL':
      return { ...state, model: payload };

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
