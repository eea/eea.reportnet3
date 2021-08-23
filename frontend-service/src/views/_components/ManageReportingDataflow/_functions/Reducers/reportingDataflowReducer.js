export const reportingDataflowReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'ON_LOAD_DATA':
      return { ...state, name: payload.name, description: payload.description };

    case 'ON_LOAD_OBLIGATION':
      return { ...state, obligation: { id: payload.id, title: payload.title } };

    case 'ON_SUBMIT':
      return { ...state, isSubmitting: payload.submit };

    case 'ON_DELETE_INPUT_CHANGE':
      return { ...state, deleteInput: payload.deleteInput };

    case 'PREV_STATE':
      return { ...state, obligationPrevState: { id: payload.id, title: payload.title } };

    case 'RESET_STATE':
      return (state = payload.resetData);

    case 'TOGGLE_PIN':
      return { ...state, pinDataflow: payload };

    case 'SET_IS_DELETING':
      return { ...state, isDeleting: payload.isDeleting };

    default:
      return state;
  }
};
