import { useContext, useReducer } from 'react';

import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { config } from 'conf';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { userReducer } from 'views/_functions/Reducers/userReducer';

const userSettingsDefaultState = {
  currentDataflowType: config.dataflowType.REPORTING,
  userProps: {
    amPm24h: true,
    basemapLayer: 'Topographic',
    dateFormat: 'YYYY-MM-DD',
    listView: true,
    notificationSound: false,
    pushNotifications: false,
    pinnedDataflows: [],
    rowsPerPage: 10,
    showLogoutConfirmation: true,
    userImage: [],
    visualTheme: 'light'
  },
  isLoggedOut: null
};

export const UserProvider = ({ children }) => {
  const notificationContext = useContext(NotificationContext);
  const [userState, userDispatcher] = useReducer(userReducer, userSettingsDefaultState);

  return (
    <UserContext.Provider
      value={{
        ...userState,
        hasPermission: (permissions, entity) => {
          if (isUndefined(entity)) {
            return permissions.some(permission => userState.accessRole.includes(permission));
          } else {
            return permissions.some(permission => userState.contextRoles.includes(`${entity}-${permission}`));
          }
        },

        hasContextAccessPermission: (entity, entityID, allowedPermissions) => {
          if (isNil(userState.contextRoles)) {
            return false;
          }
          return allowedPermissions.some(allowedPermission => {
            if (isNil(entityID)) {
              return userState.contextRoles.some(role => role.startsWith(entity) && role.endsWith(allowedPermission));
            } else {
              return userState.contextRoles.includes(`${entity}${entityID}-${allowedPermission}`);
            }
          });
        },

        setCurrentDataflowType: currentDataflowType =>
          userDispatcher({ type: 'SET_CURRENT_DATAFLOW_TYPE', payload: { currentDataflowType } }),

        getUserRole: entity => {
          const userRole = userState.contextRoles.filter(role => role.includes(entity));
          return userRole.map(role => role.replace(`${entity}-`, ''));
        },

        onAddSocket: socket => userDispatcher({ type: 'ADD_SOCKET', payload: socket }),

        onChangeBasemapLayer: basemapLayer => userDispatcher({ type: 'BASEMAP_LAYER', payload: basemapLayer }),
        onChangeDateFormat: dateFormat => userDispatcher({ type: 'DATE_FORMAT', payload: dateFormat }),
        onChangePinnedDataflows: pinnedDataflows =>
          userDispatcher({ type: 'USER_PINNED_DATAFLOWS', payload: pinnedDataflows }),
        onChangeRowsPerPage: rowNumber => userDispatcher({ type: 'DEFAULT_ROW_SELECTED', payload: rowNumber }),

        onLogin: user => userDispatcher({ type: 'LOGIN', payload: user }),

        onLogout: () => {
          notificationContext.deleteAll();
          userDispatcher({ type: 'LOGOUT', payload: userSettingsDefaultState });
        },

        onToggleAmPm24hFormat: hoursFormat => {
          userDispatcher({ type: 'TOGGLE_DATE_FORMAT_AM_PM_24H', payload: hoursFormat });
        },

        onToggleNotificationSound: notificationSound => {
          userDispatcher({ type: 'TOGGLE_NOTIFICATION_SOUND', payload: notificationSound });
        },

        onTogglePushNotifications: pushNotifications => {
          userDispatcher({ type: 'TOGGLE_PUSH_NOTIFICATIONS', payload: pushNotifications });
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
