export const userReducer = (state, { type, payload }) => {
  switch (type) {
    case 'LOGIN':
      return {
        ...state,
        ...payload.user
      };
    case 'LOGOUT':
      return (state = payload);
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

    default:
      return state;
  }
};
