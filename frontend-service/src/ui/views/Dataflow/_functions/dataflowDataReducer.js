export const dataflowDataReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'DELETE_DIALOG':
      return {
        ...state,
        isDeleteDialogVisible: payload.isVisible,
        isPropertiesDialogVisible: payload.propertiesDialog
      };

    case 'EDIT_DIALOG':
      return { ...state, isEditDialogVisible: payload.isVisible };

    case 'MANAGE_ROLES_DIALOG':
      return { ...state, isManageRolesDialogVisible: payload.isVisible };

    case 'PROPERTIES_DIALOG':
      return { ...state, isPropertiesDialogVisible: payload.isVisible };

    case 'ON_EDIT_DATA':
      return {
        ...state,
        description: payload.description,
        isEditDialogVisible: payload.isVisible,
        name: payload.name
      };

    case 'ON_DELETE_DATAFLOW':
      return { ...state, deleteInput: payload.deleteInput };

    default:
      return state;
  }
};
