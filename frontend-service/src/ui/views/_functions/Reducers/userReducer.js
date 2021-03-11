export const userReducer = (state, { type, payload }) => {
  switch (type) {
    case 'LOGIN':
      return {
        ...state,
        ...payload.user,
        isLoggedOut: false
      };
    case 'LOGOUT':
      return {
        ...payload,
        isLoggedOut: true
      };
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
    case 'TOGGLE_NOTIFICATION_SOUND':
      return {
        ...state,
        userProps: {
          ...state.userProps,
          notificationSound: payload
        }
      };
    case 'TOGGLE_PUSH_NOTIFICATIONS':
      return {
        ...state,
        userProps: {
          ...state.userProps,
          pushNotifications: payload
        }
      };
    case 'TOGGLE_LOGOUT_CONFIRM':
      return {
        ...state,
        userProps: {
          ...state.userProps,
          showLogoutConfirmation: payload
        }
      };
    case 'BASEMAP_LAYER':
      return {
        ...state,
        userProps: {
          ...state.userProps,
          basemapLayer: payload
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
    case 'TOGGLE_DATE_FORMAT_AM_PM_24H':
      return {
        ...state,
        userProps: {
          ...state.userProps,
          amPm24h: payload
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
    case 'DEFAULT_VISUAL_TYPE':
      return {
        ...state,
        userProps: {
          ...state.userProps,
          listView: payload
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
    case 'USER_PINNED_DATAFLOWS':
      return {
        ...state,
        userProps: {
          ...state.userProps,
          pinnedDataflows: payload
        }
      };

    default:
      return state;
  }
};
