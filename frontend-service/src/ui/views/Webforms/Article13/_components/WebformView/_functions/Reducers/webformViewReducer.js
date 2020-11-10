export const webformViewReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'ON_CHANGE_TAB':
      return { ...state, isVisible: payload.isVisible };

    default:
      return state;
  }
};
