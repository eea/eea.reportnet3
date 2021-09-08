export const euDatasetReducer = (state, { type, payload }) => {
  switch (type) {
    case 'GET_DATA_SCHEMA':
      return {
        ...state,
        datasetSchemaAllTables: payload.allTables,
        datasetSchemaId: payload.schemaId,
        datasetSchemaName: payload.schemaName,
        levelErrorTypes: payload.errorTypes
      };

    case 'GET_DATAFLOW_DETAILS':
      return {
        ...state,
        dataflowName: payload.name,
        isBusinessDataflow: payload.isBusinessDataflow,
        isCitizenScienceDataflow: payload.isCitizenScienceDataflow
      };

    case 'GET_EXPORT_EXTENSIONS_LIST':
      return { ...state, exportExtensionsList: payload.internalExtensionList };

    case 'GET_METADATA':
      return { ...state, metaData: payload.metadata };

    case 'HANDLE_DIALOGS':
      return { ...state, isDialogVisible: { ...state.isDialogVisible, [payload.dialog]: payload.value } };

    case 'HAS_WRITE_PERMISSIONS':
      return { ...state, hasWritePermissions: payload.hasWritePermissions };

    case 'IS_DATA_UPDATED':
      return { ...state, isDataUpdated: payload.value };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'ON_HIGHLIGHT_REFRESH':
      return { ...state, isRefreshHighlighted: payload.value };

    case 'ON_LOAD_TABLE_DATA':
      return { ...state, datasetHasData: payload.hasData };

    case 'ON_LOAD_DATASET_SCHEMA':
      return {
        ...state,
        datasetHasErrors: payload.datasetErrors,
        datasetName: payload.datasetName,
        tableSchema: payload.tableSchema,
        tableSchemaColumns: payload.tableSchemaColumns,
        tableSchemaId: payload.schemaId,
        tableSchemaNames: payload.tableSchemaNames
      };

    case 'ON_TAB_CHANGE':
      return { ...state, dataViewerOptions: { ...state.dataViewerOptions, tableSchemaId: payload.tableSchemaId } };

    case 'SET_IS_LOADING_FILE':
      return { ...state, isLoadingFile: payload.value };

    default:
      return state;
  }
};
