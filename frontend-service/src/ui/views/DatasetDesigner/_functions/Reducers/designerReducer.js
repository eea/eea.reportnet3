import { QuerystringUtils } from 'ui/views/_functions/Utils/QuerystringUtils';

export const designerReducer = (state, { type, payload }) => {
  switch (type) {
    case 'GET_EXPORT_LIST':
      return { ...state, exportButtonsList: payload.exportList };

    case 'GET_IMPORT_LIST':
      return { ...state, importButtonsList: payload.importList };

    case 'GET_DATASET_DATA':
      return {
        ...state,
        dataViewerOptions: {
          ...state.dataViewerOptions,
          selectedRecordErrorId: -1
        },
        datasetDescription: payload.description,
        datasetSchema: payload.datasetSchema,
        datasetSchemaAllTables: payload.tables,
        datasetSchemaId: payload.schemaId,
        datasetStatistics: payload.datasetStatistics,
        dataViewerOptions: {
          ...state.dataViewerOptions,
          tableSchemaId:
            QuerystringUtils.getUrlParamValue('tab') !== ''
              ? QuerystringUtils.getUrlParamValue('tab')
              : payload.tables.length === 0
              ? ''
              : payload.tables[0].tableSchemaId
        },
        levelErrorTypes: payload.levelErrorTypes,
        previousWebform: payload.previousWebform,
        schemaTables: payload.schemaTables,
        webform: payload.webform
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

    case 'UPDATED_IS_DUPLICATED':
      return { ...state, isDuplicatedToManageUnique: payload.value };

    case 'INITIAL_DATASET_DESCRIPTION':
      return { ...state, initialDatasetDescription: payload.value };

    case 'SET_IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'SET_IS_LOADING_FILE':
      return { ...state, isLoadingFile: payload.value };

    case 'SET_REPLACE_DATA':
      return { ...state, replaceData: payload.value };

    case 'SET_VIEW_MODE':
      const inmViewType = { ...state.viewType };
      Object.keys(inmViewType).forEach(view => {
        if (view === payload.value) {
          inmViewType[view] = true;
        } else {
          inmViewType[view] = false;
        }
      });
      return { ...state, viewType: inmViewType };

    case 'LOAD_EXTERNAL_OPERATIONS':
      return {
        ...state,
        externalOperationsList: {
          ...state.externalOperationsList,
          export: payload.export,
          import: payload.import,
          importOtherSystems: payload.importOtherSystems
        }
      };

    case 'LOAD_DATASET_SCHEMAS':
      return { ...state, datasetSchemas: payload.schemas, areLoadedSchemas: true };

    case 'MANAGE_DIALOGS':
      return { ...state, [payload.dialog]: payload.value, [payload.secondDialog]: payload.secondValue };

    case 'MANAGE_UNIQUE_CONSTRAINT_DATA':
      return { ...state, manageUniqueConstraintData: { ...state.manageUniqueConstraintData, ...payload.data } };

    case 'ON_EXPORT_DATA':
      return { ...state, exportDatasetData: payload.data, exportDatasetDataName: payload.name };

    case 'ON_UPDATE_DATA':
      return { ...state, isDataUpdated: payload.isUpdated };

    case 'ON_UPDATE_DESCRIPTION':
      return { ...state, datasetDescription: payload.value };

    case 'ON_UPDATE_TABLES':
      return { ...state, datasetSchemaAllTables: payload.tables, areUpdatingTables: true };

    case 'SET_DATASET_HAS_DATA':
      return { ...state, datasetHasData: payload.hasData };

    case 'SET_DATAVIEWER_GROUPED_OPTIONS':
      return {
        ...state,
        dataViewerOptions: {
          ...state.dataViewerOptions,
          activeIndex: payload.activeIndex,
          isGroupedValidationDeleted: payload.isGroupedValidationDeleted,
          isGroupedValidationSelected: payload.isGroupedValidationSelected,
          isValidationSelected: false,
          recordPositionId: -1,
          selectedRecordErrorId: -1,
          selectedRuleId: payload.selectedRuleId,
          selectedRuleLevelError: payload.selectedRuleLevelError,
          selectedRuleMessage: payload.selectedRuleMessage,
          tableSchemaId: payload.tableSchemaId
        },
        isValidationViewerVisible: false
      };

    case 'SET_DATAVIEWER_OPTIONS':
      return {
        ...state,
        dataViewerOptions: {
          ...state.dataViewerOptions,
          activeIndex: payload.activeIndex,
          isGroupedValidationDeleted: false,
          isGroupedValidationSelected: false,
          isValidationSelected: payload.isValidationSelected,
          recordPositionId: payload.recordPositionId,
          selectedRecordErrorId: payload.selectedRecordErrorId,
          selectedRuleId: payload.selectedRuleId,
          selectedRuleLevelError: payload.selectedRuleLevelError,
          selectedRuleMessage: payload.selectedRuleMessage,
          tableSchemaId: payload.tableSchemaId
        },
        isValidationViewerVisible: false
      };

    case 'SET_EXPORT_DATASET_FILE_TYPE':
      return { ...state, exportDatasetFileType: payload.fileType };

    case 'SET_IS_VALIDATION_SELECTED':
      return {
        ...state,
        dataViewerOptions: {
          ...state.dataViewerOptions,
          isValidationSelected: payload,
          isGroupedValidationSelected: payload
        }
      };

    case 'TOGGLE_DASHBOARD_VISIBILITY':
      return { ...state, dashDialogVisible: payload };

    case 'TOGGLE_VALIDATION_VIEWER_VISIBILITY':
      return { ...state, isValidationViewerVisible: payload };

    case 'ON_CHANGE_VIEW':
      return { ...state, viewType: payload.viewType };

    case 'UPDATE_PREVIOUS_WEBFORM':
      return { ...state, webform: state.previousWebform };

    case 'UPDATE_WEBFORM':
      return { ...state, previousWebform: state.webform, webform: payload };

    default:
      return state;
  }
};
