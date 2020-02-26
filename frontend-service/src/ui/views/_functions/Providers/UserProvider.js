import React, { useReducer, useContext } from 'react';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

const userSettingsDefaultState = {
  userProps: {
    defaultRowSelected: 10,
    defaultVisualTheme: 'light',
    showLogoutConfirmation: false
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
    case 'DEFAULT_VISUAL_THEME':
      return {
        ...state,
        userProps: {
          ...state.userProps,
          defaultVisualTheme: payload
        }
      };

    default:
      return state;
  }
};

export const UserProvider = ({ children }) => {
  const [state, dispatch] = useReducer(userReducer, userSettingsDefaultState);
  const notificationContext = useContext(NotificationContext);

  console.log('state', state);

  return (
    <UserContext.Provider
      value={{
        ...state,
        onLogin: user => {
          dispatch({
            type: 'LOGIN',
            payload: {
              user
            }
          });
        },
        onAddSocket: socket => {
          dispatch({
            type: 'ADD_SOCKET',
            payload: socket
          });
        },
        onLogout: () => {
          notificationContext.deleteAll();
          dispatch({
            type: 'LOGOUT',
            payload: {
              user: {}
            }
          });
        },
        defaultRowSelected: rowNumber => {
          dispatch({
            type: 'DEFAULT_ROW_SELECTED',
            payload: rowNumber
          });
        },
        onTokenRefresh: user => {
          dispatch({
            type: 'REFRESH_TOKEN',
            payload: {
              user
            }
          });
        },
        onToggleLogoutConfirm: () => {
          dispatch({
            type: 'TOGGLE_LOGOUT_CONFIRM',
            payload: {}
          });
        },
        defaultVisualTheme: currentTheme => {
          dispatch({
            type: 'DEFAULT_VISUAL_THEME',
            payload: currentTheme
          });
        }
      }}>
      {children}
    </UserContext.Provider>
  );
};
