export const dataflowDataReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'HAS_REPRESENTATIVES':
      return { ...state, hasRepresentativesWithoutDatasets: payload.hasRepresentativesWithoutDatasets };

    case 'MANAGE_UPDATE_DATASETS_NEW_REPRESENTATIVES':
      return { ...state, isUpdateDatasetsNewRepresentativesActive: payload.isUpdateDatasetsNewRepresentativesActive };

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
