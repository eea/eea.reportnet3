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

    case 'ON_CONFIRM_DELETE_DATAFLOW':
      return { ...state, deleteInput: payload.deleteInput };

    case 'ON_EDIT_DATA':
      return {
        ...state,
        description: payload.description,
        isEditDialogVisible: payload.isEditDialogVisible,
        name: payload.name
      };
    // Refactor START

    case 'SET_DATA_PROVIDER_ID':
      return { ...state, dataProviderId: payload.id };

    case 'SET_DATASET_ID_TO_SNAPSHOT_PROPS':
      return { ...state, datasetIdToSnapshotProps: payload.id };

    case 'SET_DESIGN_DATASET_SCHEMAS':
      return { ...state, designDatasetSchemas: payload.designDatasets };

    case 'SET_IS_DATA_SCHEMA_CORRECT':
      return { ...state, isDataSchemaCorrect: payload.validationResult };

    case 'SET_IS_DATA_UPDATED':
      return { ...state, isDataUpdated: !state.isDataUpdated };

    case 'SET_IS_PAGE_LOADING':
      return { ...state, isPageLoading: payload.isPageLoading };

    case 'SET_UPDATED_DATASET_SCHEMA':
      return { ...state, updatedDatasetSchema: payload.updatedData };

    case 'SET_IS_RECEIPT_LOADING':
      return { ...state, isReceiptLoading: payload.isReceiptLoading };

    case 'SET_IS_RECEIPT_OUTDATED':
      return { ...state, isReceiptOutdated: payload.isReceiptOutdated };

    case 'ON_CLEAN_UP_RECEIPT':
      return { ...state, ...payload };

    default:
      return state;
  }
};
