export const referenceBigButtonsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'HANDLE_DIALOGS':
      return { ...state, dialogVisibility: { ...state.dialogVisibility, [payload.dialog]: payload.isVisible } };

    case 'GET_DELETE_INDEX':
      return { ...state, deleteIndex: payload.index };

    case 'SET_IS_DATA_SCHEMA_CORRECT':
      return { ...state, isCreateReferenceEnabled: payload.data };

    case 'GET_DATAFLOW_TO_CLONE':
      return { ...state, cloneDataflow: payload.dataflow };

    case 'IS_CLONING_DATAFLOW':
      return { ...state, isCloningDataflow: payload.status };

    case 'IS_IMPORTING_DATAFLOW':
      return { ...state, isImportingDataflow: payload.status };

    case 'SET_IS_DESIGN_STATUS':
      return { ...state, isDesignStatus: payload.isDesignStatus };

    case 'SET_HAS_DATASETS':
      return { ...state, hasDatasets: payload.hasDatasets };

    default:
      return state;
  }
};
