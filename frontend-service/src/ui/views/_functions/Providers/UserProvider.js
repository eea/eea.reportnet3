import React, { useContext, useReducer } from 'react';

import isUndefined from 'lodash/isUndefined';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { userReducer } from 'ui/views/_functions/Reducers/userReducer';

const userSettingsDefaultState = {
  userProps: {
    amPm24h: true,
    dateFormat: 'YYYY-MM-DD',
    listView: true,
    rowsPerPage: 10,
    showLogoutConfirmation: true,
    userImage: [],
    visualTheme: 'light'
  }
};

export const UserProvider = ({ children }) => {
  const notificationContext = useContext(NotificationContext);
  const [userState, userDispatcher] = useReducer(userReducer, userSettingsDefaultState);

  return (
    <UserContext.Provider
      value={{
        ...userState,
        onLogin: user => {
          userDispatcher({
            type: 'LOGIN',
            payload: { user }
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
            payload: userSettingsDefaultState
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
        onToggleAmPm24hFormat: hoursFormat => {
          userDispatcher({
            type: 'TOGGLE_DATE_FORMAT_AM_PM_24H',
            payload: hoursFormat
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
        onToggleTypeView: currentView => {
          userDispatcher({
            type: 'DEFAULT_VISUAL_TYPE',
            payload: currentView
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
        },
        hasPermission: (permissions, entity) => {
          let allow = false;
          if (isUndefined(entity)) {
            if (permissions.filter(permission => userState.accessRole.includes(permission)).length > 0) allow = true;
          } else {
            permissions.forEach(permission => {
              const role = `${entity}-${permission}`;
              if (userState.contextRoles.includes(role)) allow = true;
            });
          }
          return allow;
        }
      }}>
      {children}
    </UserContext.Provider>
  );
};
