import React, { useReducer } from 'react';

import { DialogContext } from 'ui/views/_functions/Contexts/DialogContext';

import { dialogReducer } from 'ui/views/_functions/Reducers/dialogReducer';

export const DialogProvider = ({ children }) => {
  const [state, dispatch] = useReducer(dialogReducer, { open: [] });

  return (
    <DialogContext.Provider
      value={{
        ...state,
        add: dialogId => {
          const dialogs = [...state.open];
          if (!dialogs.includes(dialogId)) {
            dialogs.push(dialogId);
          }
          dispatch({ type: 'UPDATE_OPEN', payload: dialogs });
        },
        remove: dialogId => {
          const dialogs = state.open;
          if (dialogs.includes(dialogId)) {
            dialogs.splice(dialogs.indexOf(dialogId), 1);
          }
          dispatch({ type: 'UPDATE_OPEN', payload: dialogs });
        }
      }}>
      {children}
    </DialogContext.Provider>
  );
};
