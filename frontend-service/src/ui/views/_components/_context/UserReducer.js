export const userReducer = (state, { type, payload }) => {
  switch (type) {
    case 'LOGIN':
      return {
        ...state,
        ...payload.user
      };
    case 'LOGOUT':
      return {};
    case 'REFRESH_TOKEN':
      return {
        ...state,
        tokenExpireTime: payload.tokenExpireTime
      };
      break;

    default:
      return state;
  }
};
