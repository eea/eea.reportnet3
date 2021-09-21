export const dialogReducer = (state, { type, payload }) => {
  switch (type) {
    case 'UPDATE_OPEN':
      return { ...state, open: payload };

    default:
      return state;
  }
};
