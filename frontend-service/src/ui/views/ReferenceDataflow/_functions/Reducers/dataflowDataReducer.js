export const dataflowDataReducer = (state, { type, payload }) => {
  switch (type) {
    case 'MANAGE_DIALOGS':
      return {
        ...state,
        [payload.dialog]: payload.value,
        [payload.secondDialog]: payload.secondValue,
        deleteInput: payload.deleteInput
      };

    case 'LOADING_ERROR': {
      return {
        ...state,
        requestStatus: 'rejected',
        error: payload.error
      };
    }
    case 'LOADING_SUCCESS': {
      return {
        ...state,
        requestStatus: 'resolved',
        data: payload.referenceDataflow,
        description: payload.referenceDataflow.description,
        name: payload.referenceDataflow.name,
        status: payload.referenceDataflow.status
      };
    }
    case 'LOADING_STARTED': {
      return {
        ...state,
        requestStatus: 'pending'
      };
    }
    default: {
      throw new Error(`Unhandled action type: ${type}`);
    }
  }
};
