export const userReducer = (state, { type, payload }) => {
  switch (type) {
    case 'login':
      return {
        ...state,
        ...payload
      };
    case 'logout':
      return {};
    case 'refreshToken':
      break;

    default:
      return state;
  }
};
