export const designerReducer = (state, { type, payload }) => {
  switch (type) {
    case 'GET_DATASET_DATA':
      return {
        ...state,
        datasetDescription: payload.description,
        datasetSchemaAllTables: payload.tables,
        datasetSchemaId: payload.schemaId
      };

    case 'GET_METADATA':
      return {
        ...state,
        dataflowName: payload.dataflowName,
        datasetSchemaName: payload.schemaName,
        metaData: payload.metaData
      };

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

    case 'ON_UPDATE_DESCRIPTION':
      return { ...state, datasetDescription: payload.value };

    case 'ON_UPDATE_TABLES':
      return { ...state, datasetSchemaAllTables: payload.tables };

    case 'SET_DATASET_HAS_DATA':
      return { ...state, datasetHasData: payload.hasData };

    default:
      return state;
  }
};
