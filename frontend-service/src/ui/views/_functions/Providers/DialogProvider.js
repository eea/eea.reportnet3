import React, { useContext, useReducer } from 'react';

import { DialogContext } from 'ui/views/_functions/Contexts/DialogContext';

import { dialogReducer } from 'ui/views/_functions/Reducers/dialogReducer';

export const DialogProvider = ({ children }) => {
  const dialogContext = useContext(DialogContext);

  const [state, dispatch] = useReducer(dialogReducer, { open: 0 });

  return (
    <DialogContext.Provider
      value={{
        ...state,
        add: () => {
          dispatch({ type: 'UPDATE_OPEN', payload: state.open + 1 });
        },
        remove: () => {
          dispatch({ type: 'UPDATE_OPEN', payload: state.open - 1 });
        }
      }}>
      {children}
    </DialogContext.Provider>
  );
};
