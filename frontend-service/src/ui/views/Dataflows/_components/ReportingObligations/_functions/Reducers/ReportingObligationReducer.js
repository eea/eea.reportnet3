export const ReportingObligationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    default:
      return state;
  }
};
