export const tabularSwitchReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ON_CHANGE_VIEW':
      return { ...state, views: payload.viewType };

    default:
      return state;
  }
};
