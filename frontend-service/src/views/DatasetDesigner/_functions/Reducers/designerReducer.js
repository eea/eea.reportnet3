import { QuerystringUtils } from 'views/_functions/Utils/QuerystringUtils';

export const designerReducer = (state, { type, payload }) => {
  switch (type) {
    case 'GET_EXPORT_LIST':
      return { ...state, exportButtonsList: payload.exportList };

    case 'GET_IMPORT_LIST':
      return { ...state, importButtonsList: payload.importList };

    case 'GET_DATASET_DATA':
      return {
        ...state,
        availableInPublic: payload.availableInPublic,
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
        referenceDataset: payload.referenceDataset,
        schemaTables: payload.schemaTables,
        webform: payload.webform
      };

    case 'GET_METADATA':
      return {
        ...state,
        dataflowName: payload.dataflowName,
        dataflowType: payload.dataflowType,
        datasetSchemaName: payload.schemaName,
        metaData: payload.metaData
      };

    case 'GET_SELECTED_IMPORT_EXTENSION':
      return { ...state, selectedImportExtension: payload.selectedImportExtension };

    case 'GET_UNIQUES':
      return { ...state, uniqueConstraintsList: payload.data };

    case 'GET_WEBFORMS':
      return { ...state, webformOptions: payload.data };

    case 'HAS_WRITE_PERMISSIONS':
      return { ...state, hasWritePermissions: payload.hasWritePermissions };

    case 'HIGHLIGHT_REFRESH':
      return { ...state, isRefreshHighlighted: payload.value };

    case 'IS_DATAFLOW_EDITABLE':
      return {
        ...state,
        isDataflowOpen: payload.isDataflowOpen,
        isDesignDatasetEditorRead: payload.isDesignDatasetEditorRead
      };

    case 'UPDATED_IS_DUPLICATED':
      return { ...state, isDuplicatedToManageUnique: payload.value };

    case 'INITIAL_DATASET_DESCRIPTION':
      return { ...state, initialDatasetDescription: payload.value };

    case 'SET_REFERENCE_DATASET':
      return { ...state, referenceDataset: payload };

    case 'SET_CONSTRAINT_MANAGING_ID':
      return { ...state, constraintManagingId: payload.constraintManagingId };

    case 'SET_IS_CONSTRAINT_CREATING':
      return { ...state, isUniqueConstraintCreating: payload.isUniqueConstraintCreatingValue };

    case 'SET_IS_CONSTRAINT_UPDATING':
      return { ...state, isUniqueConstraintUpdating: payload.isUniqueConstraintUpdatingValue };

    case 'SET_IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'SET_IS_LOADING_FILE':
      return { ...state, isLoadingFile: payload.value };

    case 'SET_REPLACE_DATA':
      return { ...state, replaceData: payload.value };

    case 'SET_IS_TABLE_CREATED':
      return { ...state, isTableCreated: payload.isTableCreated };

    case 'SET_IS_HISTORY_DIALOG_VISIBLE':
      return { ...state, isHistoryDialogVisible: payload };

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

    case 'ON_UPDATE_DATA':
      return { ...state, isDataUpdated: payload.isUpdated };

    case 'ON_UPDATE_DESCRIPTION':
      return { ...state, datasetDescription: payload.value };

    case 'ON_UPDATE_TABLES':
      return { ...state, datasetSchemaAllTables: payload.tables, areUpdatingTables: true };

    case 'ON_UPDATE_SCHEMA':
      return { ...state, datasetSchema: { ...state.datasetSchema, tables: payload.schema } };

    case 'SET_AVAILABLE_PUBLIC_VIEW':
      return { ...state, availableInPublic: payload };

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
          selectedRuleId: payload.selectedRuleId,
          selectedRuleLevelError: payload.selectedRuleLevelError,
          selectedRuleMessage: payload.selectedRuleMessage,
          tableSchemaId: payload.tableSchemaId,
          selectedTableSchemaId: payload.selectedTableSchemaId
        },
        isValidationViewerVisible: false
      };

    case 'SET_DATAVIEWER_OPTIONS':
      return {
        ...state,
        dataViewerOptions: {
          ...state.dataViewerOptions,
          activeIndex: payload.activeIndex,
          isGroupedValidationDeleted: payload.isGroupedValidationDeleted,
          isGroupedValidationSelected: payload.isGroupedValidationSelected,
          selectedRuleId: payload.selectedRuleId,
          selectedRuleLevelError: payload.selectedRuleLevelError,
          selectedRuleMessage: payload.selectedRuleMessage,
          tableSchemaId: payload.tableSchemaId,
          selectedTableSchemaId: payload.selectedTableSchemaId
        },
        isValidationViewerVisible: false
      };

    case 'SET_EXPORT_DATASET_FILE_TYPE':
      return { ...state, exportDatasetFileType: payload.fileType };

    case 'SET_IS_VALIDATIONS_TABULAR_VIEW':
      return { ...state, isValidationsTabularView: payload.isValidationsTabularView };

    case 'TOGGLE_VALIDATION_VIEWER_VISIBILITY':
      return { ...state, isValidationViewerVisible: payload };

    case 'ON_CHANGE_VIEW':
      return { ...state, viewType: payload.viewType };

    case 'SET_PROGRESS_STEP_BAR':
      const inmDatasetProgressBarSteps = [...state.datasetProgressBarSteps];
      inmDatasetProgressBarSteps[payload.step].isRunning = payload.value;
      return {
        ...state,
        datasetProgressBarCurrentStep: payload.currentStep,
        datasetProgressBarSteps: inmDatasetProgressBarSteps
      };

    case 'SET_SELECTED_WEBFORM':
      return { ...state, selectedWebform: payload.selectedWebform };

    case 'RESET_SELECTED_WEBFORM':
      return { ...state, selectedWebform: undefined };

    case 'ON_UPDATE_TABS':
      return { ...state, schemaTables: payload.data, tabs: payload.tabs };

    case 'SET_IS_DOWNLOADING_QC_RULES':
      return { ...state, isDownloadingQCRules: payload.isDownloadingQCRules };

    case 'SET_IS_DOWNLOADING_VALIDATIONS':
      return { ...state, isDownloadingValidations: payload.isDownloadingValidations };

    case 'SET_ARE_PREFILLED_TABLES_DELETED':
      return { ...state, arePrefilledTablesDeleted: payload.arePrefilledTablesDeleted };

    case 'SET_WEBFORM_OPTIONS_LOADING_STATUS':
      return { ...state, webformOptionsLoadingStatus: payload.loadingStatus };

    case 'SET_HAS_QCS_HISTORY':
      return { ...state, hasQCsHistory: payload.hasQCsHistory };

    default:
      return state;
  }
};
