import React, { useContext, useReducer } from 'react';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

const userSettingsDefaultState = {
  userProps: {
    rowsPerPage: 10,
    visualTheme: 'light',
    showLogoutConfirmation: true,
    userImage: [],
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
          showLogoutConfirmation: payload
        }
      };
    case 'DEFAULT_ROW_SELECTED':
      return {
        ...state,
        userProps: {
          ...state.userProps,
          rowsPerPage: payload
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
          visualTheme: payload
        }
      };
    case 'SETTINGS_LOADED':
      return {
        ...state,
        userProps: {
          ...state.userProps,
          settingsLoaded: payload
        }
      };
    case 'USER_AVATAR_IMAGE':
      return {
        ...state,
        userProps: {
          ...state.userProps,
          userImage: payload
        }
      };

    default:
      return state;
  }
};

export const UserProvider = ({ children }) => {
  const notificationContext = useContext(NotificationContext);
  const themeContext = useContext(ThemeContext);
  //const [state, userDispatcher] = useReducer(userReducer, {});
  const [state, userDispatcher] = useReducer(userReducer, userSettingsDefaultState);
  // const notificationContext = useContext(NotificationContext);

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
        onChangeRowsPerPage: rowNumber => {
          userDispatcher({
            type: 'DEFAULT_ROW_SELECTED',
            payload: rowNumber
          });
        },
        onChangeDateFormat: dateFormat => {
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
        onToggleLogoutConfirm: logoutConf => {
          userDispatcher({
            type: 'TOGGLE_LOGOUT_CONFIRM',
            payload: logoutConf
          });
        },
        onToggleVisualTheme: currentTheme => {
          userDispatcher({
            type: 'DEFAULT_VISUAL_THEME',
            payload: currentTheme
          });
        },
        onToggleSettingsLoaded: settingsLoaded => {
          userDispatcher({
            type: 'SETTINGS_LOADED',
            payload: settingsLoaded
          });
        },
        onUserFileUpload: base64Image => {
          userDispatcher({
            type: 'USER_AVATAR_IMAGE',
            payload: base64Image
          });
        }
      }}>
      {children}
    </UserContext.Provider>
  );
};
