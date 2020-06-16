export const designerReducer = (state, { type, payload }) => {
  switch (type) {
    case 'GET_DATASET_DATA':
      return {
        ...state,
        datasetDescription: payload.description,
        datasetSchema: payload.datasetSchema,
        datasetSchemaAllTables: payload.tables,
        datasetSchemaId: payload.schemaId,
        datasetStatistics: payload.datasetStatistics,
        levelErrorTypes: payload.levelErrorTypes,
        tableSchemaNames: payload.tableSchemaNames
      };

    case 'GET_METADATA':
      return {
        ...state,
        dataflowName: payload.dataflowName,
        datasetSchemaName: payload.schemaName,
        metaData: payload.metaData
      };

    case 'GET_UNIQUES':
      return { ...state, uniqueConstraintsList: payload.data };

    case 'HIGHLIGHT_REFRESH':
      return { ...state, isRefreshHighlighted: payload.value };

    case 'INITIAL_DATASET_DESCRIPTION':
      return { ...state, initialDatasetDescription: payload.value };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'IS_PREVIEW_MODE_ON':
      return { ...state, isPreviewModeOn: payload.value };

    case 'LOAD_DATASET_SCHEMAS':
      return { ...state, datasetSchemas: payload.schemas };

    case 'MANAGE_DIALOGS':
      return { ...state, [payload.dialog]: payload.value, [payload.secondDialog]: payload.secondValue };

    case 'MANAGE_UNIQUE_CONSTRAINT_DATA':
      return { ...state, manageUniqueConstraintData: { ...state.manageUniqueConstraintData, ...payload.data } };

    case 'ON_UPDATE_DESCRIPTION':
      return { ...state, datasetDescription: payload.value };

    case 'ON_UPDATE_TABLES':
      return { ...state, datasetSchemaAllTables: payload.tables };

    case 'SET_DATASET_HAS_DATA':
      return { ...state, datasetHasData: payload.hasData };

    case 'SET_DATAVIEWER_OPTIONS':
      return {
        ...state,
        dataViewerOptions: {
          ...state.dataViewerOptions,
          activeIndex: payload.activeIndex,
          isValidationSelected: payload.isValidationSelected,
          recordPositionId: payload.recordPositionId,
          selectedRecordErrorId: payload.selectedRecordErrorId
        },
        isValidationViewerVisible: false
      };

    case 'SET_IS_VALIDATION_SELECTED':
      return {
        ...state,
        dataViewerOptions: {
          ...state.dataViewerOptions,
          isValidationSelected: payload
        }
      };

    case 'TOGGLE_DASHBOARD_VISIBILITY':
      return { ...state, dashDialogVisible: payload };

    case 'TOGGLE_VALIDATION_VIEWER_VISIBILITY':
      return { ...state, isValidationViewerVisible: payload };

    default:
      return state;
  }
};
