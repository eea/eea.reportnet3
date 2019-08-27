export const userReducer = (state, { type, payload }) => {
  switch (type) {
    case 'login':
      return {
        ...state,
        ...payload.user
      };
    case 'logout':
      return {};
    case 'refreshToken':
      return {
        ...state,
        tokenExpireTime: payload.tokenExpireTime
      };
      break;

    default:
      return state;
  }
};
