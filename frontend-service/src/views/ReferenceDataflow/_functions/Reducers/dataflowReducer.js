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

    case 'LOAD_PERMISSIONS': {
      return {
        ...state,
        isAdmin: payload.isAdmin,
        isCustodian: payload.isCustodian,
        isCustodianUser: payload.isCustodianUser
      };
    }

    case 'ON_EDIT_DATAFLOW':
      return { ...state, name: payload.name, description: payload.description };

    case 'SET_UPDATED_DATASET_SCHEMA':
      return { ...state, updatedDatasetSchema: payload.updatedData };

    case 'SET_DESIGN_DATASET_SCHEMAS':
      return { ...state, designDatasetSchemas: payload.designDatasets };

    case 'REFRESH_PAGE':
      return { ...state, refresh: !state.refresh };

    case 'SET_IS_RIGHT_PERMISSIONS_CHANGED':
      return { ...state, isRightPermissionsChanged: payload.isRightPermissionsChanged };

    case 'SET_IS_CREATING_REFERENCE_DATASETS':
      return { ...state, isCreatingReferenceDatasets: payload.isCreatingReferenceDatasets };

    case 'SET_IS_LOADING':
      return { ...state, isLoading: payload.isLoading };

    default: {
      throw new Error(`Unhandled action type: ${type}`);
    }
  }
};
