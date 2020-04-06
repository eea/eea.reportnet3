import React, { useContext, useReducer } from 'react';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

const userSettingsDefaultState = {
  userProps: {
    defaultRowSelected: 10,
    defaultVisualTheme: 'light',
    showLogoutConfirmation: false,
    userIconPatch: {},
    dateFormat: 'YYYY-MM-DD'
  }
};

const userReducer = (state, { type, payload }) => {
  switch (type) {
    case 'LOGIN':
      return {
        ...state,
        ...payload.user
      };
    case 'LOGOUT':
      return userSettingsDefaultState;
    case 'ADD_SOCKET':
      return {
        ...state,
        socket: payload
      };
    case 'REFRESH_TOKEN':
      return {
        ...state,
        ...payload.user
      };
    case 'TOGGLE_LOGOUT_CONFIRM':
      return {
        ...state,
        userProps: {
          ...state.userProps,
          showLogoutConfirmation: !state.userProps.showLogoutConfirmation
        }
      };
    case 'DEFAULT_ROW_SELECTED':
      return {
        ...state,
        userProps: {
          ...state.userProps,
          defaultRowSelected: payload
        }
      };
    case 'DATE_FORMAT':
      return {
        ...state,
        userProps: {
          ...state.userProps,
          dateFormat: payload
        }
      };
    case 'DEFAULT_VISUAL_THEME':
      return {
        ...state,
        userProps: {
          ...state.userProps,
          defaultVisualTheme: payload
        }
      };
    case 'USER_ICON_PATH':
      return {
        ...state,
        userProps: {
          ...state.userProps,
          userIconPatch: payload
        }
      };

    default:
      return state;
  }
};

export const UserProvider = ({ children }) => {
  const notificationContext = useContext(NotificationContext);

  //const [state, userDispatcher] = useReducer(userReducer, {});
  const [state, userDispatcher] = useReducer(userReducer, userSettingsDefaultState);
  // const notificationContext = useContext(NotificationContext);

  console.log('state', state);

  return (
    <UserContext.Provider
      value={{
        ...state,
        onLogin: user => {
          userDispatcher({
            type: 'LOGIN',
            payload: {
              user
            }
          });
        },
        onAddSocket: socket => {
          userDispatcher({
            type: 'ADD_SOCKET',
            payload: socket
          });
        },
        onLogout: () => {
          notificationContext.deleteAll();
          userDispatcher({
            type: 'LOGOUT',
            payload: {
              user: {}
            }
          });
        },
        defaultRowSelected: rowNumber => {
          userDispatcher({
            type: 'DEFAULT_ROW_SELECTED',
            payload: rowNumber
          });
        },
        dateFormat: dateFormat => {
          userDispatcher({ type: 'DATE_FORMAT', payload: dateFormat });
        },
        onTokenRefresh: user => {
          userDispatcher({
            type: 'REFRESH_TOKEN',
            payload: {
              user
            }
          });
        },
        onToggleLogoutConfirm: () => {
          userDispatcher({
            type: 'TOGGLE_LOGOUT_CONFIRM',
            payload: {}
          });
        },
        defaultVisualTheme: currentTheme => {
          userDispatcher({
            type: 'DEFAULT_VISUAL_THEME',
            payload: currentTheme
          });
        },
        onClickUserIcon: path => {
          userDispatcher({
            type: 'USER_ICON_PATH',
            payload: path
          });
        }
      }}>
      {children}
    </UserContext.Provider>
  );
};
