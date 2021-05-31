export const dataflowDataReducer = (state, { type, payload }) => {
  switch (type) {
    case 'MANAGE_DIALOGS':
      return {
        ...state,
        [payload.dialog]: payload.value,
        [payload.secondDialog]: payload.secondValue,
        deleteInput: payload.deleteInput
      };

    case 'LOADING_STARTED': {
      return { ...state, requestStatus: 'pending' };
    }

    case 'LOADING_SUCCESS': {
      console.log(`payload`, payload);
      return { ...state, requestStatus: 'resolved', ...payload };
    }

    case 'LOADING_ERROR': {
      return { ...state, requestStatus: 'rejected', error: payload.error };
    }

    default: {
      throw new Error(`Unhandled action type: ${type}`);
    }
  }
};
