export const dataflowDataReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'HAS_REPRESENTATIVES':
      return { ...state, hasRepresentatives: payload.hasRepresentatives };

    case 'LOAD_PERMISSIONS':
      return { ...state, hasWritePermissions: payload.hasWritePermissions, isCustodian: payload.isCustodian };

    case 'MANAGE_DIALOGS':
      return {
        ...state,
        [payload.dialog]: payload.value,
        [payload.secondDialog]: payload.secondValue,
        deleteInput: payload.deleteInput
      };

    case 'ON_DELETE_DATAFLOW':
      return { ...state, deleteInput: payload.deleteInput };

    case 'ON_EDIT_DATA':
      return {
        ...state,
        description: payload.description,
        isEditDialogVisible: payload.isVisible,
        name: payload.name
      };

    default:
      return state;
  }
};
