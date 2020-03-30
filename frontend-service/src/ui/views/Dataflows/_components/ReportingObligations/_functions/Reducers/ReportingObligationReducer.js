export const ReportingObligationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'ON_SELECT_OBL':
      return { ...state, oblChoosed: payload.oblChoosed };

    default:
      return state;
  }
};
