export const webformViewReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'ON_CHANGE_TAB':
      return { ...state, isVisible: payload.isVisible };

    case 'SET_IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'SET_SINGLE_CALCULATED_DATA':
      return { ...state, singlesCalculatedData: payload };

    default:
      return state;
  }
};
