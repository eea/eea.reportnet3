export const reportingObligationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'ON_SELECT_OBL':
      return { ...state, oblChoosed: payload.oblChoosed };

    case 'ON_TOGGLE_VIEW':
      return { ...state, isTableView: payload.view };

    default:
      return state;
  }
};
