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
        },

        getUserRole: entity => {
          const userRole = userState.contextRoles.filter(role => role.includes(entity));
          return userRole.map(role => role.replace(`${entity}-`, ''));
        },

        onAddSocket: socket => userDispatcher({ type: 'ADD_SOCKET', payload: socket }),

        onChangeDateFormat: dateFormat => userDispatcher({ type: 'DATE_FORMAT', payload: dateFormat }),

        onChangeRowsPerPage: rowNumber => userDispatcher({ type: 'DEFAULT_ROW_SELECTED', payload: rowNumber }),

        onLogin: user => userDispatcher({ type: 'LOGIN', payload: { user } }),

        onLogout: () => {
          notificationContext.deleteAll();
          userDispatcher({ type: 'LOGOUT', payload: userSettingsDefaultState });
        },

        onToggleAmPm24hFormat: hoursFormat => {
          userDispatcher({ type: 'TOGGLE_DATE_FORMAT_AM_PM_24H', payload: hoursFormat });
        },

        onToggleLogoutConfirm: logoutConfirmation => {
          userDispatcher({ type: 'TOGGLE_LOGOUT_CONFIRM', payload: logoutConfirmation });
        },

        onToggleSettingsLoaded: settingsLoaded => userDispatcher({ type: 'SETTINGS_LOADED', payload: settingsLoaded }),

        onToggleTypeView: currentView => userDispatcher({ type: 'DEFAULT_VISUAL_TYPE', payload: currentView }),

        onToggleVisualTheme: currentTheme => userDispatcher({ type: 'DEFAULT_VISUAL_THEME', payload: currentTheme }),

        onTokenRefresh: user => userDispatcher({ type: 'REFRESH_TOKEN', payload: { user } }),

        onUserFileUpload: base64Image => userDispatcher({ type: 'USER_AVATAR_IMAGE', payload: base64Image })
      }}>
      {children}
    </UserContext.Provider>
  );
};
