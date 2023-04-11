import { useContext, useReducer } from 'react';

import dayjs from 'dayjs';
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';

import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { DateTimeUtils } from 'services/_utils/DateTimeUtils';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { userReducer } from 'views/_functions/Reducers/userReducer';

import { SystemNotificationService } from 'services/SystemNotificationService';

dayjs.extend(utc);
dayjs.extend(timezone);

const userSettingsDefaultState = {
  currentDataflowType: undefined,
  userProps: {
    amPm24h: true,
    basemapLayer: 'Topographic',
    dateFormat: 'YYYY-MM-DD',
    listView: true,
    localTimezone: true,
    notificationSound: false,
    pushNotifications: false,
    pinnedDataflows: [],
    rowsPerPage: 10,
    showLogoutConfirmation: true,
    timezone: DateTimeUtils.convertTimeZoneName(dayjs.tz.guess()),
    userImage: [],
    visualTheme: 'light'
  },
  isLoggedOut: null
};

export const UserProvider = ({ children }) => {
  const notificationContext = useContext(NotificationContext);
  const [userState, userDispatcher] = useReducer(userReducer, userSettingsDefaultState);

  const onLoadSystemNotifications = async () => {
    const unparsedNotifications = await SystemNotificationService.all();
    unparsedNotifications.forEach(notification => {
      notificationContext.add(notification, false, true);
    });
  };

  return (
    <UserContext.Provider
      value={{
        ...userState,
        hasPermission: (permissions, entity) => {
          if (isUndefined(entity)) {
            return permissions.some(permission => userState.accessRole?.includes(permission));
          } else {
            return permissions.some(permission => userState.contextRoles?.includes(`${entity}-${permission}`));
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

        onLogin: user => {
          onLoadSystemNotifications();
          userDispatcher({ type: 'LOGIN', payload: user });
        },

        onLogout: () => {
          notificationContext.deleteAll();
          userDispatcher({ type: 'LOGOUT', payload: userSettingsDefaultState });
        },

        onChangeTimezone: timezone => {
          userDispatcher({ type: 'TIME_ZONE', payload: timezone });
          if (timezone === DateTimeUtils.convertTimeZoneName(dayjs.tz.guess())) {
            userDispatcher({ type: 'TOGGLE_TIME_ZONE', payload: true });
          } else {
            userDispatcher({ type: 'TOGGLE_TIME_ZONE', payload: false });
          }
        },

        onToggleTimezone: async localTimezone => {
          userDispatcher({ type: 'TOGGLE_TIME_ZONE', payload: localTimezone });
          if (localTimezone) {
            userDispatcher({
              type: 'TIME_ZONE',
              payload: DateTimeUtils.convertTimeZoneName(dayjs.tz.guess())
            });
          }
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
