import React, { useContext, useReducer } from 'react';

import { DialogContext } from 'ui/views/_functions/Contexts/DialogContext';

import { dialogReducer } from 'ui/views/_functions/Reducers/dialogReducer';

export const DialogProvider = ({ children }) => {
  const dialogContext = useContext(DialogContext);

  const [state, dispatch] = useReducer(dialogReducer, { open: [] });

  return (
    <DialogContext.Provider
      value={{
        ...state,
        add: dialogId => {
          console.log(state);
          console.log('dialogId: ', dialogId);
          const dialogs = [...state.open];
          console.log('dialogs ', dialogs);
          if (!dialogs.includes(dialogId)) {
            dialogs.push(dialogId);
          }
          console.log('dialogs ', dialogs);
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
