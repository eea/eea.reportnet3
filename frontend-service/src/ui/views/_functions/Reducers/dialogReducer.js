export const dialogReducer = (state, { type, payload }) => {
  console.log('type: ', type, ' payload:', payload);
  switch (type) {
    case 'UPDATE_OPEN':
      return {
        ...state,
        open: payload
      };

    default:
      return state;
  }
};
