export const dataflowReducer = (state, { type, payload }) => {
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
      return { ...state, requestStatus: 'resolved', ...payload };
    }

    case 'LOADING_ERROR': {
      return { ...state, requestStatus: 'rejected', error: payload.error };
    }

    case 'SET_UPDATED_DATASET_SCHEMA':
      return { ...state, updatedDatasetSchema: payload.updatedData };

    case 'SET_DESIGN_DATASET_SCHEMAS':
      return { ...state, designDatasetSchemas: payload.designDatasets };

    case 'REFRESH_PAGE':
      return { ...state, refresh: !state.refresh };

    case 'SET_IS_CREATING_REFERENCE_DATASETS':
      return { ...state, isCreatingReferenceDatasets: payload.isCreatingReferenceDatasets };

    default: {
      throw new Error(`Unhandled action type: ${type}`);
    }
  }
};
