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

    case 'IS_CLONING_STATUS':
      return { ...state, isCloningStatus: payload.status };

    default:
      return state;
  }
};
