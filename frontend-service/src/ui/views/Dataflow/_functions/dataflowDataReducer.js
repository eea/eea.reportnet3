export const dataflowDataReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'SET_HAS_REPRESENTATIVES_WITHOUT_DATASETS':
      return { ...state, hasRepresentativesWithoutDatasets: payload.hasRepresentativesWithoutDatasets };

    case 'SET_FORM_HAS_REPRESENTATIVES':
      return { ...state, formHasRepresentatives: payload.formHasRepresentatives };

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
    // Refactor START

    case 'SET_DATA_PROVIDER_ID':
      return { ...state, dataProviderId: payload.id };

    case 'SET_DATASET_ID_TO_SNAPSHOT_PROPS':
      return { ...state, datasetIdToSnapshotProps: payload.id };

    default:
      return state;
  }
};
