import { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import { withRouter } from 'react-router-dom';

import camelCase from 'lodash/camelCase';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './DatasetDesigner.module.scss';

import { config } from 'conf';
import { DatasetConfig } from 'conf/domain/model/Dataset';
import { DatasetSchemaRequesterEmptyHelpConfig } from 'conf/help/datasetSchema/requester/empty';
import { DatasetSchemaRequesterWithTabsHelpConfig } from 'conf/help/datasetSchema/requester/withTabs';
import WebformsConfig from 'conf/webforms.config.json';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { Dashboard } from 'ui/views/_components/Dashboard';
import { Dialog } from 'ui/views/_components/Dialog';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { Integrations } from './_components/Integrations';
import { MainLayout } from 'ui/views/_components/Layout';
import { ManageUniqueConstraint } from './_components/ManageUniqueConstraint';
import { Menu } from 'ui/views/_components/Menu';
import { Snapshots } from 'ui/views/_components/Snapshots';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsDesigner } from './_components/TabsDesigner';
import { TabularSwitch } from 'ui/views/_components/TabularSwitch';
import { Title } from 'ui/views/_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { UniqueConstraints } from './_components/UniqueConstraints';
import { Validations } from 'ui/views/DatasetDesigner/_components/Validations';
import { ValidationsList } from 'ui/views/_components/ValidationsList';
import { ValidationViewer } from 'ui/views/_components/ValidationViewer';
import { Webforms } from 'ui/views/Webforms';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { IntegrationService } from 'core/services/Integration';
import { ValidationService } from 'core/services/Validation';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

import { designerReducer } from './_functions/Reducers/designerReducer';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';
import { useDatasetDesigner } from 'ui/views/_components/Snapshots/_hooks/useDatasetDesigner';

import { CurrentPage, ExtensionUtils, MetadataUtils, QuerystringUtils, TextUtils } from 'ui/views/_functions/Utils';
import { DatasetDesignerUtils } from './_functions/Utils/DatasetDesignerUtils';
import { getUrl } from 'core/infrastructure/CoreUtils';

export const DatasetDesigner = withRouter(({ history, isReferenceDataset = false, match }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);
  const validationContext = useContext(ValidationContext);

  const [importFromOtherSystemSelectedIntegrationId, setImportFromOtherSystemSelectedIntegrationId] = useState();
  const [importSelectedIntegrationId, setImportSelectedIntegrationId] = useState(null);
  const [needsRefreshUnique, setNeedsRefreshUnique] = useState(true);
  const [schemaImported, setSchemaImported] = useState(false);
  const [sqlValidationRunning, setSqlValidationRunning] = useState(false);

  const [designerState, designerDispatch] = useReducer(designerReducer, {
    availableInPublic: false,
    areLoadedSchemas: false,
    areUpdatingTables: false,
    dashDialogVisible: false,
    constraintManagingId: '',
    dataflowName: '',
    datasetDescription: '',
    datasetHasData: false,
    datasetSchema: {},
    datasetSchemaAllTables: [],
    datasetSchemaId: '',
    datasetSchemaName: '',
    datasetSchemas: [],
    datasetStatistics: [],
    dataViewerOptions: {
      isGroupedValidationDeleted: false,
      isGroupedValidationSelected: false,
      isValidationSelected: false,
      recordPositionId: -1,
      selectedRecordErrorId: -1,
      selectedRuleId: '',
      selectedRuleLevelError: '',
      selectedRuleMessage: '',
      selectedTableSchemaId: null,
      tableSchemaId: QuerystringUtils.getUrlParamValue('tab')
    },
    exportButtonsList: [],
    exportDatasetFileType: '',
    externalOperationsList: { export: [], import: [], importOtherSystems: [] },
    hasWritePermissions: false,
    importButtonsList: [],
    initialDatasetDescription: '',
    isConfigureWebformDialogVisible: false,
    isDataflowOpen: false,
    isDataUpdated: false,
    isDuplicatedToManageUnique: false,
    isDownloadingValidations: false,
    isExportTableSchemaDialogVisible: false,
    isImportDatasetDialogVisible: false,
    isImportOtherSystemsDialogVisible: false,
    isImportTableSchemaDialogVisible: false,
    isIntegrationListDialogVisible: false,
    isIntegrationManageDialogVisible: false,
    isLoading: true,
    isLoadingFile: false,
    isManageUniqueConstraintDialogVisible: false,
    isRefreshHighlighted: false,
    isTableCreated: false,
    isUniqueConstraintCreating: false,
    isUniqueConstraintUpdating: false,
    isUniqueConstraintsListDialogVisible: false,
    isValidationViewerVisible: false,
    levelErrorTypes: [],
    manageUniqueConstraintData: {
      fieldData: [],
      isTableCreationMode: false,
      tableSchemaId: null,
      tableSchemaName: '',
      uniqueId: null
    },
    metaData: {},
    previousWebform: null,
    referenceDataset: false,
    refresh: false,
    replaceData: false,
    selectedImportExtension: null,
    schemaTables: [],
    tabs: [],
    uniqueConstraintsList: [],
    validateDialogVisible: false,
    validationListDialogVisible: false,
    viewType: {
      design: TextUtils.areEquals(QuerystringUtils.getUrlParamValue('view'), 'design'),
      tabularData: TextUtils.areEquals(QuerystringUtils.getUrlParamValue('view'), 'tabularData'),
      webform: TextUtils.areEquals(QuerystringUtils.getUrlParamValue('view'), 'webform')
    },
    webform: null,
    selectedWebform: undefined,
    isValidationsTabularView: false
  });

  const {
    datasetSchemaAllTables,
    datasetDescription,
    dataViewerOptions,
    isDataflowOpen,
    isDesignDatasetEditorRead,
    isImportOtherSystemsDialogVisible,
    validateDialogVisible
  } = designerState;

  const exportMenuRef = useRef();
  const importMenuRef = useRef();

  const {
    isLoadingSnapshotListData,
    isSnapshotDialogVisible,
    isSnapshotsBarVisible,
    setIsSnapshotDialogVisible,
    setIsSnapshotsBarVisible,
    snapshotDispatch,
    snapshotListData,
    snapshotState
  } = useDatasetDesigner(dataflowId, datasetId, designerState.datasetSchemaId);

  useBreadCrumbs({
    currentPage: isReferenceDataset ? CurrentPage.REFERENCE_DATASET_DESIGNER : CurrentPage.DATASET_DESIGNER,
    dataflowId,
    history,
    referenceDataflowId: dataflowId
  });

  useEffect(() => {
    leftSideBarContext.removeModels();
    onLoadSchema();
    callSetMetaData();
  }, []);

  useEffect(() => {    
    if (schemaImported) {
      onLoadSchema();
      setSchemaImported(false);
    }
  }, [schemaImported]);

  useEffect(() => {
    if (!isUndefined(userContext.contextRoles)) {
      if (userContext.accessRole[0] !== config.permissions.roles.EDITOR_READ.key && !isDataflowOpen) {
        if (datasetSchemaAllTables.length > 1) {
          leftSideBarContext.addHelpSteps(
            DatasetSchemaRequesterWithTabsHelpConfig,
            'datasetSchemaRequesterWithTabsHelpConfig'
          );
        } else {
          leftSideBarContext.addHelpSteps(
            DatasetSchemaRequesterEmptyHelpConfig,
            'datasetSchemaRequesterEmptyHelpConfig'
          );
        }
      } else {
        leftSideBarContext.removeHelpSteps();
      }
    }
  }, [userContext, designerState, designerState.areLoadingSchemas, designerState.areUpdatingTables]);

  useEffect(() => {
    if (designerState.validationListDialogVisible) {
      validationContext.resetReOpenOpener();
    }
  }, [designerState.validationListDialogVisible]);

  useEffect(() => {
    if (window.location.search !== '' && !isNil(dataViewerOptions.tableSchemaId)) {
      changeUrl();
    } else {
      changeUrl(true);
    }
  }, [designerState.viewType, dataViewerOptions.tableSchemaId]);

  useEffect(() => {
    if (designerState.datasetSchemaId) getFileExtensions();
  }, [designerState.datasetSchemaId, designerState.isImportDatasetDialogVisible, designerState.isDataUpdated]);

  useEffect(() => {
    getExportList();
  }, [designerState.datasetSchemaName, designerState.externalOperationsList]);

  useEffect(() => {
    getImportList();
  }, [designerState.externalOperationsList]);

  useEffect(() => {
    if (!isUndefined(userContext.contextRoles)) {
      const isDataflowOpen =
        userContext.hasPermission(
          [config.permissions.roles.CUSTODIAN.key, config.permissions.roles.STEWARD.key],
          `${config.permissions.prefixes.DATAFLOW}${dataflowId}`
        ) && designerState?.metaData?.dataflow?.status === config.dataflowStatus.OPEN;
      const isDesignDatasetEditorRead =
        userContext.hasPermission(
          [config.permissions.roles.EDITOR_READ.key],
          `${config.permissions.prefixes.DATAFLOW}${dataflowId}`
        ) && designerState?.metaData?.dataflow?.status === config.dataflowStatus.DESIGN;

      designerDispatch({
        type: 'IS_DATAFLOW_EDITABLE',
        payload: { isDataflowOpen, isDesignDatasetEditorRead }
      });
    }
  }, [userContext, designerState?.metaData?.dataflow?.status]);

  useEffect(() => {
    if (notificationContext.hidden.some(notification => notification.key === 'EXPORT_DATASET_FAILED_EVENT')) {
      setIsLoadingFile(false);
    }

    if (notificationContext.hidden.some(notification => notification.key === 'DOWNLOAD_VALIDATIONS_FAILED_EVENT')) {
      setIsDownloadingValidations(false);
    }
  }, [notificationContext.hidden]);

  const refreshUniqueList = value => setNeedsRefreshUnique(value);

  const callSetMetaData = async () => {
    const metaData = await getMetadata({ datasetId, dataflowId });
    designerDispatch({
      type: 'GET_METADATA',
      payload: {
        metaData,
        dataflowName: metaData.dataflow.name,
        schemaName: metaData.dataset.name
      }
    });
  };

  const changeMode = viewMode => designerDispatch({ type: 'SET_VIEW_MODE', payload: { value: viewMode } });

  const changeUrl = (changeOnlyView = false) => {
    window.history.replaceState(
      null,
      null,
      !changeOnlyView
        ? `?tab=${
            QuerystringUtils.getUrlParamValue('tab') !== ''
              ? QuerystringUtils.getUrlParamValue('tab')
              : dataViewerOptions.tableSchemaId
          }${`&view=${Object.keys(designerState.viewType).filter(view => designerState.viewType[view] === true)}`}`
        : `?tab=${QuerystringUtils.getUrlParamValue('tab')}${`&view=${Object.keys(designerState.viewType).filter(
            view => designerState.viewType[view] === true
          )}`}`
    );
  };

  const getExportList = () => {
    const { externalOperationsList } = designerState;

    const internalExtensionsList = config.exportTypes.exportDatasetTypes.map(type => {
      const extensionsTypes = !isNil(type.code) && type.code.split('+');
      return {
        command: () => onExportDataInternalExtension(type.code),
        icon: extensionsTypes[0],
        label: type.text
      };
    });

    const externalIntegrationsNames = !isEmpty(externalOperationsList.export)
      ? [
          {
            label: resources.messages['customExports'],
            items: externalOperationsList.export.map(type => {
              return {
                command: () => onExportDataExternalIntegration(type.id),
                icon: type.fileExtension,
                label: `${type.name.toUpperCase()} (.${type.fileExtension.toLowerCase()})`
              };
            })
          }
        ]
      : [];

    designerDispatch({
      type: 'GET_EXPORT_LIST',
      payload: { exportList: internalExtensionsList.concat(externalIntegrationsNames) }
    });
  };

  const getImportList = () => {
    const internalExtensionsList = config.importTypes.importDatasetTypes.map(type => {
      const extensionsTypes = !isNil(type.code) && type.code.split('+');
      return {
        command: () => {
          manageDialogs('isImportDatasetDialogVisible', true);
          designerDispatch({
            type: 'GET_SELECTED_IMPORT_EXTENSION',
            payload: { selectedImportExtension: type.code }
          });
        },
        icon: extensionsTypes[0],
        label: type.text
      };
    });

    const { externalOperationsList } = designerState;

    const importFromFile = !isEmpty(externalOperationsList.import)
      ? [
          {
            label: resources.messages['customImports'],
            items: externalOperationsList.import.map(type => {
              return {
                command: () => {
                  manageDialogs('isImportDatasetDialogVisible', true);
                  designerDispatch({
                    type: 'GET_SELECTED_IMPORT_EXTENSION',
                    payload: { selectedImportExtension: type.fileExtension }
                  });
                  setImportSelectedIntegrationId(type.id);
                },
                icon: type.fileExtension,
                label: `${type.name.toUpperCase()} (.${type.fileExtension.toLowerCase()})`
              };
            })
          }
        ]
      : [];

    const importOtherSystems = !isEmpty(externalOperationsList.importOtherSystems)
      ? [
          {
            label: resources.messages['importPreviousData'],
            items: externalOperationsList.importOtherSystems.map(importOtherSystem => ({
              id: importOtherSystem.id,
              label: importOtherSystem.name,
              icon: 'upload',
              command: () => {
                setImportFromOtherSystemSelectedIntegrationId(importOtherSystem.id);
                manageDialogs('isImportOtherSystemsDialogVisible', true);
              }
            }))
          }
        ]
      : [];

    designerDispatch({
      type: 'GET_IMPORT_LIST',
      payload: { importList: internalExtensionsList.concat(importFromFile).concat(importOtherSystems) }
    });
  };

  const validImportExtensions = `.${designerState.selectedImportExtension}`;

  const infoExtensionsTooltip = `${resources.messages['supportedFileExtensionsTooltip']} ${validImportExtensions}`;

  const getFileExtensions = async () => {
    try {
      const allExtensions = await IntegrationService.allExtensionsOperations(dataflowId, designerState.datasetSchemaId);
      const externalOperations = ExtensionUtils.groupOperations('operation', allExtensions);
      designerDispatch({
        type: 'LOAD_EXTERNAL_OPERATIONS',
        payload: {
          export: externalOperations.export,
          import: externalOperations.import,
          importOtherSystems: externalOperations.importOtherSystems
        }
      });
    } catch (error) {
      notificationContext.add({ type: 'LOADING_FILE_EXTENSIONS_ERROR' });
    }
  };

  const getMetadata = async ids => {
    try {
      return await MetadataUtils.getMetadata(ids);
    } catch (error) {
      notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId, datasetId } });
    } finally {
      setIsLoading(false);
    }
  };

  const getStatisticsById = async (datasetId, tableSchemaNames) => {
    try {
      const statistics = await DatasetService.errorStatisticsById(datasetId, tableSchemaNames);
      return statistics.data;
    } catch (error) {
      console.error(error);
      throw new Error('ERROR_STATISTICS_BY_ID_ERROR');
    }
  };

  const getUniqueConstraintsList = data => designerDispatch({ type: 'GET_UNIQUES', payload: { data } });

  const setIsTableCreated = isTableCreated => {
    designerDispatch({ type: 'SET_IS_TABLE_CREATED', payload: { isTableCreated } });
  };

  const setIsLoading = value => designerDispatch({ type: 'SET_IS_LOADING', payload: { value } });

  const setIsLoadingFile = value => designerDispatch({ type: 'SET_IS_LOADING_FILE', payload: { value } });

  const manageDialogs = (dialog, value, secondDialog, secondValue) => {
    designerDispatch({ type: 'MANAGE_DIALOGS', payload: { dialog, value, secondDialog, secondValue } });
  };

  const manageUniqueConstraint = data => designerDispatch({ type: 'MANAGE_UNIQUE_CONSTRAINT_DATA', payload: { data } });

  const onBlurDescription = description => {
    if (description !== designerState.initialDatasetDescription) {
      onUpdateDescription(description);
    }
  };

  const onChangeAvailableInPublicView = async checked => {
    try {
      designerDispatch({ type: 'SET_AVAILABLE_PUBLIC_VIEW', payload: checked });
      await DatasetService.updateDatasetSchemaDesign(datasetId, { availableInPublic: checked });
    } catch (error) {
      console.error('Error during datasetSchema Available in public view update: ', error);
    }
  };

  const onChangeReferenceDataset = async checked => {
    try {
      designerDispatch({ type: 'SET_REFERENCE_DATASET', payload: checked });
      await DatasetService.updateDatasetSchemaDesign(datasetId, { referenceDataset: checked });
    } catch (error) {
      console.error('Error during datasetSchema reference dataset update: ', error);
    }
  };

  const onChangeIsValidationSelected = options =>
    designerDispatch({ type: 'SET_IS_VALIDATION_SELECTED', payload: options });

  const onChangeReference = (tabs, datasetSchemaId) => {
    const inmDatasetSchemas = [...designerState.datasetSchemas];
    const datasetSchemaIndex = DatasetDesignerUtils.getIndexById(datasetSchemaId, inmDatasetSchemas);
    inmDatasetSchemas[datasetSchemaIndex].tables = tabs;
    if (!isNil(inmDatasetSchemas)) {
      inmDatasetSchemas.forEach(datasetSchema =>
        datasetSchema.tables.forEach(table => {
          if (!table.addTab && !isUndefined(table.records)) {
            table.records.forEach(record =>
              record.fields.forEach(field => {
                if (!isNil(field) && field.pk) {
                  if (DatasetDesignerUtils.getCountPKUseInAllSchemas(field.fieldId, inmDatasetSchemas) > 0) {
                    table.hasPKReferenced = true;
                    field.pkReferenced = true;
                  } else {
                    table.hasPKReferenced = false;
                    field.pkReferenced = false;
                  }
                }
              })
            );
          }
        })
      );
    }
    designerDispatch({ type: 'LOAD_DATASET_SCHEMAS', payload: { schemas: inmDatasetSchemas } });
  };

  const onChangeView = value => {
    const viewType = { ...designerState.viewType };
    Object.keys(viewType).forEach(view => {
      viewType[view] = false;
      viewType[value] = true;
    });

    designerDispatch({ type: 'ON_CHANGE_VIEW', payload: { viewType } });
  };

  const onCloseUniqueListModal = () => {
    manageDialogs('isUniqueConstraintsListDialogVisible', false);
    refreshUniqueList(true);
  };

  const onCloseConfigureWebformModal = () => manageDialogs('isConfigureWebformDialogVisible', false);

  const onConfirmValidate = async () => {
    manageDialogs('validateDialogVisible', false);
    try {
      await DatasetService.validateDataById(datasetId);
      notificationContext.add({
        type: 'VALIDATE_DATA_INIT',
        content: {
          countryName: 'DESIGN',
          dataflowId,
          dataflowName: designerState.dataflowName,
          datasetId,
          datasetName: designerState.datasetSchemaName
        }
      });
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({
          type: 'GENERIC_BLOCKED_ERROR'
        });
      } else {
        notificationContext.add({
          type: 'VALIDATE_DATA_BY_ID_ERROR',
          content: {
            countryName: 'DESIGN',
            dataflowId,
            dataflowName: designerState.dataflowName,
            datasetId,
            datasetName: designerState.datasetSchemaName
          }
        });
      }
    }
  };

  const onExportError = async exportNotification => {
    const {
      dataflow: { name: dataflowName },
      dataset: { name: datasetName }
    } = await getMetadata({ dataflowId, datasetId });

    notificationContext.add({
      type: exportNotification,
      content: { dataflowName: dataflowName, datasetName: datasetName }
    });
  };

  const onExportDataExternalIntegration = async integrationId => {
    setIsLoadingFile(true);
    notificationContext.add({ type: 'EXPORT_DATASET_DATA' });

    try {
      await DatasetService.exportDatasetDataExternal(datasetId, integrationId);
    } catch (error) {
      onExportError('EXTERNAL_EXPORT_DESIGN_FAILED_EVENT');
    }
  };

  const onExportDataInternalExtension = async fileType => {
    setIsLoadingFile(true);
    notificationContext.add({ type: 'EXPORT_DATASET_DATA' });

    try {
      await DatasetService.exportDataById(datasetId, fileType);
    } catch (error) {
      onExportError('EXPORT_DATA_BY_ID_ERROR');
    }
  };

  useEffect(() => {
    const isNotification = notificationContext.toShow.find(
      notification => notification.key === 'VALIDATION_FINISHED_EVENT'
    );
    if (isNotification && isNotification.content.datasetId?.toString() === datasetId.toString()) {
      onHighlightRefresh(true);
    }
  }, [notificationContext]);

  const onHighlightRefresh = value => designerDispatch({ type: 'HIGHLIGHT_REFRESH', payload: { value } });

  useCheckNotifications(
    [
      'DOWNLOAD_EXPORT_DATASET_FILE_ERROR',
      'DOWNLOAD_FME_FILE_ERROR',
      'EXPORT_DATA_BY_ID_ERROR',
      'EXPORT_DATASET_FILE_AUTOMATICALLY_DOWNLOAD',
      'EXPORT_DATASET_FILE_DOWNLOAD',
      'EXTERNAL_EXPORT_DESIGN_FAILED_EVENT'
    ],
    setIsLoadingFile,
    false
  );

  useCheckNotifications(
    ['AUTOMATICALLY_DOWNLOAD_VALIDATIONS_FILE', 'DOWNLOAD_VALIDATIONS_FILE_ERROR'],
    setIsDownloadingValidations,
    false
  );

  useCheckNotifications(
    ['VALIDATE_RULES_COMPLETED_EVENT', 'VALIDATE_RULES_ERROR_EVENT'],
    setSqlValidationRunning,
    false
  );

  useCheckNotifications(['IMPORT_FIELD_SCHEMA_COMPLETED_EVENT'], setSchemaImported, true);

  const onHideValidationsDialog = () => {
    if (validationContext.opener === 'validationsListDialog' && validationContext.reOpenOpener) {
      validationContext.onResetOpener();
    }
    manageDialogs('validationListDialogVisible', false);
  };

  const onKeyChange = event => {
    if (event.key === 'Escape') {
      designerDispatch({ type: 'ON_UPDATE_DESCRIPTION', payload: { value: designerState.initialDatasetDescription } });
    } else if (event.key === 'Enter') {
      event.preventDefault();
      onBlurDescription(event.target.value);
    }
  };

  const onUpdateTabs = data => {
    const parsedData = [];
    data.forEach(table => parsedData.push({ name: table.tableSchemaName, id: table.tableSchemaId }));
    designerDispatch({ type: 'ON_UPDATE_TABS', payload: { data: parsedData, tabs: data } });
  };

  const onLoadSchema = () => {
    onHighlightRefresh(false);

    try {
      setIsLoading(true);
      const getDatasetSchemaId = async () => {
        const dataset = await DatasetService.schemaById(datasetId);
        const tableSchemaList = [];
        dataset.data.tables.forEach(table =>
          tableSchemaList.push({ name: table.tableSchemaName, id: table.tableSchemaId })
        );

        const datasetStatisticsDTO = await getStatisticsById(
          datasetId,
          dataset.data.tables.map(tableSchema => tableSchema.tableSchemaName)
        );

        setIsLoading(false);
        designerDispatch({
          type: 'GET_DATASET_DATA',
          payload: {
            availableInPublic: dataset.data.availableInPublic,
            datasetSchema: dataset.data,
            datasetStatistics: datasetStatisticsDTO,
            description: dataset.data.datasetSchemaDescription,
            levelErrorTypes: dataset.data.levelErrorTypes,
            previousWebform: WebformsConfig.filter(item => item.value === dataset.data.webform)[0],
            referenceDataset: dataset.data.referenceDataset,
            schemaId: dataset.data.datasetSchemaId,
            tables: dataset.data.tables,
            schemaTables: tableSchemaList,
            webform: WebformsConfig.filter(item => item.value === dataset.data.webform)[0]
          }
        });
      };
      const getDatasetSchemas = async () => {
        const { data } = await DataflowService.getAllSchemas(dataflowId);
        designerDispatch({ type: 'LOAD_DATASET_SCHEMAS', payload: { schemas: data } });
      };

      getDatasetSchemaId();
      getDatasetSchemas();
    } catch (error) {
      console.error(`Error while loading schema: ${error}`);
    }
  };

  const onLoadTableData = hasData => designerDispatch({ type: 'SET_DATASET_HAS_DATA', payload: { hasData } });

  const onHideSelectGroupedValidation = () =>
    designerDispatch({
      type: 'SET_DATAVIEWER_GROUPED_OPTIONS',
      payload: {
        ...dataViewerOptions,
        isGroupedValidationDeleted: true,
        isGroupedValidationSelected: false,
        selectedRuleId: '',
        selectedRuleLevelError: '',
        selectedRuleMessage: ''
      }
    });

  const onSelectValidation = (
    tableSchemaId,
    selectedRuleId = '',
    selectedRuleMessage = '',
    selectedRuleLevelError = ''
  ) => {
    designerDispatch({
      type: 'SET_DATAVIEWER_GROUPED_OPTIONS',
      payload: {
        ...dataViewerOptions,
        isGroupedValidationDeleted: false,
        isGroupedValidationSelected: true,
        recordPositionId: -1,
        selectedRuleId,
        selectedRuleLevelError,
        selectedRuleMessage,
        selectedTableSchemaId: tableSchemaId,
        tableSchemaId
      }
    });
  };

  const onTabChange = table =>
    designerDispatch({
      type: 'SET_DATAVIEWER_OPTIONS',
      payload: {
        ...dataViewerOptions,
        tableSchemaId: table.tableSchemaId
      }
    });

  const onUpdateData = () => {
    designerDispatch({ type: 'ON_UPDATE_DATA', payload: { isUpdated: !designerState.isDataUpdated } });
  };

  const onUpdateDescription = async description => {
    try {
      const descriptionObject = { description: description };
      await DatasetService.updateDatasetSchemaDesign(datasetId, descriptionObject);
    } catch (error) {
      console.error('Error during datasetSchema Description update: ', error);
    }
  };

  const onUpdateWebform = async () => {
    try {
      const webformObject = { webform: { name: designerState.selectedWebform.value } };
      const response = await DatasetService.updateDatasetSchemaDesign(datasetId, webformObject);
      if (response.status >= 200 && response.status <= 299) {
        onLoadSchema();
      }
    } catch (error) {
      console.error('Error during datasetSchema Webform update: ', error);
    } finally {
      onCloseConfigureWebformModal();
    }
  };

  const onUpdateTable = tables => designerDispatch({ type: 'ON_UPDATE_TABLES', payload: { tables } });

  const onUpdateSchema = schema => designerDispatch({ type: 'ON_UPDATE_SCHEMA', payload: { schema } });

  const onUpload = async () => {
    manageDialogs('isImportDatasetDialogVisible', false);
    setImportSelectedIntegrationId(null);
    try {
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });

      notificationContext.add({
        type: 'DATASET_DATA_LOADING_INIT',
        content: {
          dataflowName,
          datasetLoading: resources.messages['datasetLoading'],
          datasetLoadingMessage: resources.messages['datasetLoadingMessage'],
          datasetName,
          title: TextUtils.ellipsis(datasetName, config.notifications.STRING_LENGTH_MAX)
        }
      });
    } catch (error) {
      console.error('error', error);
      notificationContext.add({
        type: 'EXTERNAL_IMPORT_DESIGN_FAILED_EVENT',
        content: { dataflowName: designerState.dataflowName, datasetName: designerState.datasetSchemaName }
      });
    }
  };

  const cleanImportOtherSystemsDialog = () => {
    designerDispatch({ type: 'SET_REPLACE_DATA', payload: { value: false } });
    manageDialogs('isImportOtherSystemsDialogVisible', false);
  };

  const onImportDatasetError = async ({ xhr }) => {
    if (xhr.status === 400) {
      notificationContext.add({
        type: 'IMPORT_DESIGN_BAD_REQUEST_ERROR',
        content: {
          dataflowId,
          datasetId,
          datasetName: designerState.datasetSchemaName
        }
      });
    }
    if (xhr.status === 423) {
      notificationContext.add({
        type: 'GENERIC_BLOCKED_ERROR',
        content: {
          dataflowId,
          datasetId,
          datasetName: designerState.datasetSchemaName
        }
      });
    }
  };

  const onImportOtherSystems = async () => {
    try {
      cleanImportOtherSystemsDialog();
      await IntegrationService.runIntegration(
        importFromOtherSystemSelectedIntegrationId,
        datasetId,
        designerState.replaceData
      );
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'DATASET_IMPORT_INIT',
        content: { dataflowId, datasetId, dataflowName, datasetName }
      });
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({
          type: 'GENERIC_BLOCKED_ERROR'
        });
      } else {
        notificationContext.add({
          type: 'EXTERNAL_IMPORT_DESIGN_FROM_OTHER_SYSTEM_FAILED_EVENT',
          content: { dataflowName: designerState.dataflowName, datasetName: designerState.datasetSchemaName }
        });
      }
    }
  };

  const validateQcRules = async () => {
    setSqlValidationRunning(true);
    try {
      await DatasetService.validateSqlRules(datasetId, designerState.datasetSchemaId);
    } catch (error) {
      console.error('error', error);
    }
  };

  const renderActionButtonsValidationDialog = (
    <Fragment>
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'plus'}
        label={resources.messages['createFieldValidationBtn']}
        onClick={() => validationContext.onOpenModalFromOpener('field', 'validationsListDialog')}
        style={{ float: 'left' }}
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'plus'}
        label={resources.messages['createRowValidationBtn']}
        onClick={() => validationContext.onOpenModalFromOpener('row', 'validationsListDialog')}
        style={{ float: 'left' }}
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'plus'}
        label={resources.messages['createTableValidationBtn']}
        onClick={() => validationContext.onOpenModalFromOpener('dataset', 'validationsListDialog')}
        style={{ float: 'left' }}
      />

      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={sqlValidationRunning ? 'spinnerAnimate' : 'check'}
        label={resources.messages['validateSqlRulesBtn']}
        onClick={() => validateQcRules()}
        tooltip={resources.messages['validateRulesBtnTootip']}
        tooltipOptions={{ position: 'top' }}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => onHideValidationsDialog()}
      />
    </Fragment>
  );

  const renderDashboardFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink p-button-right-aligned"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => designerDispatch({ type: 'TOGGLE_DASHBOARD_VISIBILITY', payload: false })}
    />
  );

  const renderImportOtherSystemsFooter = (
    <Fragment>
      <Button
        className="p-button-animated-blink"
        icon={'check'}
        label={resources.messages['import']}
        onClick={() => onImportOtherSystems()}
      />
      <Button
        className="p-button-secondary button-right-aligned"
        icon="cancel"
        label={resources.messages['cancel']}
        onClick={() => cleanImportOtherSystemsDialog()}
      />
    </Fragment>
  );

  function setIsDownloadingValidations(isDownloadingValidations) {
    designerDispatch({ type: 'SET_IS_DOWNLOADING_VALIDATIONS', payload: { isDownloadingValidations } });
  }

  function setIsValidationsTabularView(isValidationsTabularView) {
    designerDispatch({ type: 'SET_IS_VALIDATIONS_TABULAR_VIEW', payload: { isValidationsTabularView } });
  }

  const onDownloadValidations = async () => {
    setIsDownloadingValidations(true);
    notificationContext.add({ type: 'DOWNLOAD_VALIDATIONS_START' });

    try {
      await ValidationService.generateFile(datasetId);
    } catch (error) {
      notificationContext.add({ type: 'DOWNLOAD_VALIDATIONS_ERROR' });

      setIsDownloadingValidations(false);
    }
  };

  const renderValidationsFooter = (
    <div className={styles.validationsFooter}>
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon={designerState.isDownloadingValidations ? 'spinnerAnimate' : 'export'}
        label={resources.messages['downloadValidationsButtonLabel']}
        onClick={onDownloadValidations}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => manageDialogs('isValidationViewerVisible', false)}
      />
    </div>
  );

  const switchToTabularData = () => {
    designerDispatch({
      type: 'SET_VIEW_MODE',
      payload: { value: 'tabularData' }
    });
    setIsValidationsTabularView(true);
  };

  const renderRadioButtons = () => {
    return (
      <TabularSwitch
        elements={Object.keys(designerState.viewType).map(view => resources.messages[`${view}View`])}
        getIsTableCreated={setIsTableCreated}
        isTableCreated={designerState.isTableCreated}
        isValidationsTabularView={designerState.isValidationsTabularView}
        onChange={switchView => {
          const views = { design: 'design', tabularData: 'tabularData', webform: 'webform' };
          onChangeView(views[camelCase(switchView)]);
          changeMode(views[camelCase(switchView)]);
        }}
        setIsValidationsTabularView={setIsValidationsTabularView}
        value={
          QuerystringUtils.getUrlParamValue('view') !== ''
            ? resources.messages[`${QuerystringUtils.getUrlParamValue('view')}View`]
            : resources.messages['designView']
        }
      />
    );
  };

  const renderSwitchView = () => {
    const switchView = (
      <TabularSwitch
        elements={[resources.messages['designView'], resources.messages['tabularDataView']]}
        getIsTableCreated={setIsTableCreated}
        isTableCreated={designerState.isTableCreated}
        isValidationsTabularView={designerState.isValidationsTabularView}
        onChange={switchView =>
          designerDispatch({
            type: 'SET_VIEW_MODE',
            payload: { value: switchView === 'Design' ? 'design' : 'tabularData' }
          })
        }
        setIsValidationsTabularView={setIsValidationsTabularView}
        value={
          QuerystringUtils.getUrlParamValue('view') !== ''
            ? resources.messages[`${QuerystringUtils.getUrlParamValue('view')}View`]
            : resources.messages['designView']
        }
      />
    );

    return (
      <div className={styles.switchDivInput}>
        <div className={`${styles.switchDiv} datasetSchema-switchDesignToData-help-step`}>
          {!isNil(designerState.webform) &&
          !isNil(designerState.webform.value) &&
          !isDataflowOpen &&
          !isDesignDatasetEditorRead
            ? renderRadioButtons()
            : switchView}
        </div>
      </div>
    );
  };

  const renderUniqueConstraintsDialog = () =>
    designerState.isUniqueConstraintsListDialogVisible && (
      <Dialog
        footer={renderUniqueConstraintsFooter}
        header={resources.messages['uniqueConstraints']}
        onHide={() => onCloseUniqueListModal()}
        style={{ width: '70%' }}
        visible={designerState.isUniqueConstraintsListDialogVisible}>
        <UniqueConstraints
          dataflowId={dataflowId}
          designerState={designerState}
          getManageUniqueConstraint={manageUniqueConstraint}
          getUniques={getUniqueConstraintsList}
          manageDialogs={manageDialogs}
          needsRefresh={needsRefreshUnique}
          refreshList={refreshUniqueList}
          setConstraintManagingId={setConstraintManagingId}
          setIsDuplicatedToManageUnique={setIsDuplicatedToManageUnique}
          setIsUniqueConstraintCreating={setIsUniqueConstraintCreating}
          setIsUniqueConstraintUpdating={setIsUniqueConstraintUpdating}
        />
      </Dialog>
    );

  const renderConfigureWebformFooter = (
    <Fragment>
      <Button
        className={`${
          !isUndefined(designerState.selectedWebform) &&
          designerState?.selectedWebform?.value !== designerState?.webform?.value &&
          'p-button-animated-blink'
        } ${styles.saveButton}`}
        disabled={
          isUndefined(designerState.selectedWebform) ||
          designerState?.selectedWebform?.value === designerState?.webform?.value
        }
        icon={'check'}
        label={resources.messages['save']}
        onClick={() => {
          onUpdateWebform();
          if (isNil(designerState?.selectedWebform?.value)) {
            changeMode('design');
          }
        }}
      />
      <Button
        className="p-button-secondary p-button-right-aligned"
        icon={'cancel'}
        label={resources.messages['cancel']}
        onClick={() => {
          designerDispatch({ type: 'RESET_SELECTED_WEBFORM' });
          onCloseConfigureWebformModal();
        }}
      />
    </Fragment>
  );

  const renderUniqueConstraintsFooter = (
    <Fragment>
      <div className="p-toolbar-group-left">
        <Button
          className="p-button-secondary p-button-animated-blink"
          icon={'plus'}
          label={resources.messages['addUniqueConstraint']}
          onClick={() => manageDialogs('isManageUniqueConstraintDialogVisible', true)}
        />
      </div>
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => onCloseUniqueListModal()}
      />
    </Fragment>
  );

  const setIsDuplicatedToManageUnique = value =>
    designerDispatch({ type: 'UPDATED_IS_DUPLICATED', payload: { value } });

  const setConstraintManagingId = constraintManagingId =>
    designerDispatch({ type: 'SET_CONSTRAINT_MANAGING_ID', payload: { constraintManagingId } });

  const setIsUniqueConstraintCreating = isUniqueConstraintCreatingValue =>
    designerDispatch({ type: 'SET_IS_CONSTRAINT_CREATING', payload: { isUniqueConstraintCreatingValue } });

  const setIsUniqueConstraintUpdating = isUniqueConstraintUpdatingValue =>
    designerDispatch({ type: 'SET_IS_CONSTRAINT_UPDATING', payload: { isUniqueConstraintUpdatingValue } });

  const validationsListDialog = () => {
    if (designerState.validationListDialogVisible) {
      return (
        <Dialog
          footer={renderActionButtonsValidationDialog}
          header={resources.messages['qcRules']}
          onHide={() => onHideValidationsDialog()}
          style={{ width: '90%' }}
          visible={designerState.validationListDialogVisible}>
          <ValidationsList
            dataset={designerState.metaData.dataset}
            datasetSchemaAllTables={datasetSchemaAllTables}
            datasetSchemaId={designerState.datasetSchemaId}
          />
        </Dialog>
      );
    }
  };

  const layout = children => (
    <MainLayout>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  if (designerState.isLoading) return layout(<Spinner />);

  return layout(
    <SnapshotContext.Provider
      value={{
        isSnapshotsBarVisible: isSnapshotsBarVisible,
        setIsSnapshotsBarVisible: setIsSnapshotsBarVisible,
        snapshotDispatch: snapshotDispatch,
        snapshotState: snapshotState
      }}>
      <div className={styles.noScrollDatasetDesigner}>
        <Title
          icon="pencilRuler"
          iconSize="3.4rem"
          subtitle={designerState.dataflowName}
          title={`${resources.messages['datasetSchema']}: ${designerState.datasetSchemaName}`}
        />
        <h4 className={styles.descriptionLabel}>{resources.messages['newDatasetSchemaDescriptionPlaceHolder']}</h4>
        <div className={styles.ButtonsBar}>
          <div className={styles.datasetDescriptionRow}>
            <InputTextarea
              className={`${styles.datasetDescription} datasetSchema-metadata-help-step`}
              collapsedHeight={55}
              disabled={isDataflowOpen || isDesignDatasetEditorRead}
              expandableOnClick={true}
              id="datasetDescription"
              key="datasetDescription"
              onBlur={e => onBlurDescription(e.target.value)}
              onChange={e => designerDispatch({ type: 'ON_UPDATE_DESCRIPTION', payload: { value: e.target.value } })}
              onFocus={e =>
                designerDispatch({ type: 'INITIAL_DATASET_DESCRIPTION', payload: { value: e.target.value } })
              }
              onKeyDown={e => onKeyChange(e)}
              placeholder={resources.messages['newDatasetSchemaDescriptionPlaceHolder']}
              value={datasetDescription || ''}
            />

            <div className={styles.datasetConfigurationButtons}>
              <div>
                <Checkbox
                  ariaLabelledBy="reference_dataset_label"
                  checked={designerState.referenceDataset}
                  disabled={isDesignDatasetEditorRead || isDataflowOpen || isReferenceDataset}
                  id="reference_dataset"
                  inputId="reference_dataset_checkbox"
                  onChange={e => onChangeReferenceDataset(e.checked)}
                  role="checkbox"
                />
                <label
                  id="reference_dataset_label"
                  onClick={() => {
                    if (!isDesignDatasetEditorRead && !isDataflowOpen && !isReferenceDataset) {
                      designerDispatch({
                        type: 'SET_REFERENCE_DATASET',
                        payload: !designerState.referenceDataset
                      });
                      onChangeReferenceDataset(!designerState.referenceDataset);
                    }
                  }}
                  style={{
                    color: 'var(--main-font-color)',
                    cursor: isDesignDatasetEditorRead || isDataflowOpen ? 'default' : 'pointer',
                    fontSize: '11pt',
                    fontWeight: 'bold',
                    marginLeft: '6px',
                    marginRight: '6px',
                    opacity: isDesignDatasetEditorRead || isDataflowOpen ? 0.5 : 1
                  }}>
                  {resources.messages['referenceDataset']}
                </label>
              </div>
              <div>
                <Checkbox
                  ariaLabelledBy="available_in_public_view_label"
                  checked={designerState.availableInPublic}
                  disabled={isDesignDatasetEditorRead}
                  id="available_in_public_view"
                  inputId="available_in_public_view_checkbox"
                  onChange={e => onChangeAvailableInPublicView(e.checked)}
                  role="checkbox"
                />
                <label
                  id="available_in_public_view_label"
                  onClick={() => {
                    if (!isDesignDatasetEditorRead) {
                      designerDispatch({
                        type: 'SET_AVAILABLE_PUBLIC_VIEW',
                        payload: !designerState.availableInPublic
                      });
                      onChangeAvailableInPublicView(!designerState.availableInPublic);
                    }
                  }}
                  style={{
                    color: 'var(--main-font-color)',
                    cursor: isDesignDatasetEditorRead ? 'default' : 'pointer',
                    fontSize: '11pt',
                    fontWeight: 'bold',
                    marginLeft: '6px',
                    marginRight: '6px',
                    opacity: isDesignDatasetEditorRead ? 0.5 : 1
                  }}>
                  {resources.messages['availableInPublicView']}
                </label>
              </div>
              <Button
                className={`p-button-secondary ${
                  !isDataflowOpen && !isDesignDatasetEditorRead && !designerState.referenceDataset
                    ? 'p-button-animated-blink'
                    : null
                } datasetSchema-uniques-help-step`}
                disabled={isDataflowOpen || isDesignDatasetEditorRead || designerState.referenceDataset}
                icon={'table'}
                label={resources.messages['configureWebform']}
                onClick={() => manageDialogs('isConfigureWebformDialogVisible', true)}
              />
            </div>
          </div>
          <Toolbar>
            <div className="p-toolbar-group-left">
              <Button
                className={`p-button-rounded p-button-secondary ${
                  !isDataflowOpen && !isDesignDatasetEditorRead ? 'p-button-animated-blink' : null
                }`}
                disabled={isDataflowOpen || isDesignDatasetEditorRead}
                icon={'import'}
                label={resources.messages['importDataset']}
                onClick={event => importMenuRef.current.show(event)}
              />
              <Menu
                className={styles.menuWrapper}
                id="importDataSetMenu"
                model={designerState.importButtonsList}
                popup={true}
                ref={importMenuRef}
              />
              <Button
                className={`p-button-rounded p-button-secondary-transparent ${
                  !isDataflowOpen && !isDesignDatasetEditorRead ? 'p-button-animated-blink' : null
                }`}
                disabled={isDataflowOpen || isDesignDatasetEditorRead}
                icon={designerState.isLoadingFile ? 'spinnerAnimate' : 'export'}
                id="buttonExportDataset"
                label={resources.messages['exportDataset']}
                onClick={event => exportMenuRef.current.show(event)}
              />
              <Menu
                className={styles.menuWrapper}
                id="exportDataSetMenu"
                model={designerState.exportButtonsList}
                popup={true}
                ref={exportMenuRef}
              />
            </div>
            <div className="p-toolbar-group-right">
              <Button
                className={`p-button-rounded p-button-secondary-transparent ${
                  !isDataflowOpen && !isDesignDatasetEditorRead ? ' p-button-animated-blink' : null
                }`}
                disabled={isDataflowOpen || isDesignDatasetEditorRead}
                icon="validate"
                label={resources.messages['validate']}
                onClick={() => manageDialogs('validateDialogVisible', true)}
              />

              <Button
                className="p-button-rounded p-button-secondary-transparent p-button-animated-blink"
                icon="warning"
                iconClasses={designerState.datasetStatistics.datasetErrors ? 'warning' : ''}
                label={resources.messages['showValidations']}
                onClick={() => designerDispatch({ type: 'TOGGLE_VALIDATION_VIEWER_VISIBILITY', payload: true })}
              />

              <Button
                className={`p-button-rounded p-button-secondary-transparent ${
                  !isDesignDatasetEditorRead && (!isDataflowOpen || !designerState.referenceDataset)
                    ? 'p-button-animated-blink'
                    : null
                } datasetSchema-qcRules-help-step`}
                disabled={isDesignDatasetEditorRead || (isDataflowOpen && designerState.referenceDataset)}
                icon="horizontalSliders"
                label={resources.messages['qcRules']}
                onClick={() => manageDialogs('validationListDialogVisible', true)}
              />

              <Button
                className={`p-button-rounded p-button-secondary-transparent ${
                  !isDesignDatasetEditorRead && (!isDataflowOpen || !designerState.referenceDataset)
                    ? 'p-button-animated-blink'
                    : null
                }`}
                disabled={isDesignDatasetEditorRead || (isDataflowOpen && designerState.referenceDataset)}
                icon="key"
                label={resources.messages['uniqueConstraints']}
                onClick={() => manageDialogs('isUniqueConstraintsListDialogVisible', true)}
              />

              <Button
                className={`p-button-rounded p-button-secondary-transparent ${
                  !isDesignDatasetEditorRead && !designerState.referenceDataset ? styles.integrationsButton : null
                }`}
                disabled={isDesignDatasetEditorRead || designerState.referenceDataset}
                icon="export"
                iconClasses={styles.integrationsButtonIcon}
                label={resources.messages['externalIntegrations']}
                onClick={() => manageDialogs('isIntegrationListDialogVisible', true)}
              />

              <Button
                className={`p-button-rounded p-button-secondary-transparent ${
                  designerState.datasetHasData &&
                  !isDataflowOpen &&
                  !isDesignDatasetEditorRead &&
                  'p-button-animated-blink'
                }`}
                disabled={!designerState.datasetHasData || isDataflowOpen || isDesignDatasetEditorRead}
                icon="dashboard"
                label={resources.messages['dashboards']}
                onClick={() => designerDispatch({ type: 'TOGGLE_DASHBOARD_VISIBILITY', payload: true })}
              />
              <Button
                className={`p-button-rounded p-button-secondary-transparent datasetSchema-manageCopies-help-step ${
                  !designerState.hasWritePermissions && !isDataflowOpen && !isDesignDatasetEditorRead
                    ? 'p-button-animated-blink'
                    : null
                }`}
                disabled={designerState.hasWritePermissions || isDataflowOpen || isDesignDatasetEditorRead}
                icon="camera"
                label={resources.messages['snapshots']}
                onClick={() => setIsSnapshotsBarVisible(!isSnapshotsBarVisible)}
              />

              <Button
                className={`p-button-rounded p-button-${
                  designerState.isRefreshHighlighted ? 'primary' : 'secondary-transparent'
                }  ${!isDataflowOpen && !isDesignDatasetEditorRead ? 'p-button-animated-blink' : null}`}
                disabled={isDataflowOpen || isDesignDatasetEditorRead}
                icon="refresh"
                label={resources.messages['refresh']}
                onClick={() => setSchemaImported(true)}
              />
            </div>
          </Toolbar>
        </div>
        {renderSwitchView()}
        {!isNil(designerState.webform) && !isNil(designerState.webform.value) && designerState.viewType['webform'] ? (
          <Webforms
            dataflowId={dataflowId}
            datasetId={datasetId}
            state={designerState}
            webformType={designerState.webform.value}
          />
        ) : (
          <TabsDesigner
            changeMode={changeMode}
            datasetSchema={designerState.datasetSchema}
            datasetSchemas={designerState.datasetSchemas}
            datasetStatistics={designerState.datasetStatistics}
            designerState={designerState}
            editable={!isDataflowOpen && !isDesignDatasetEditorRead}
            getIsTableCreated={setIsTableCreated}
            getUpdatedTabs={onUpdateTabs}
            history={history}
            isDataflowOpen={isDataflowOpen}
            isDesignDatasetEditorRead={isDesignDatasetEditorRead}
            isGroupedValidationDeleted={dataViewerOptions.isGroupedValidationDeleted}
            isGroupedValidationSelected={dataViewerOptions.isGroupedValidationSelected}
            isReferenceDataset={designerState.referenceDataset}
            isValidationSelected={dataViewerOptions.isValidationSelected}
            manageDialogs={manageDialogs}
            manageUniqueConstraint={manageUniqueConstraint}
            onChangeIsValidationSelected={onChangeIsValidationSelected}
            onChangeReference={onChangeReference}
            onHideSelectGroupedValidation={onHideSelectGroupedValidation}
            onLoadTableData={onLoadTableData}
            onTabChange={onTabChange}
            onUpdateSchema={onUpdateSchema}
            onUpdateTable={onUpdateTable}
            recordPositionId={dataViewerOptions.recordPositionId}
            schemaImported={schemaImported}
            selectedRecordErrorId={dataViewerOptions.selectedRecordErrorId}
            selectedRuleId={dataViewerOptions.selectedRuleId}
            selectedRuleLevelError={dataViewerOptions.selectedRuleLevelError}
            selectedRuleMessage={dataViewerOptions.selectedRuleMessage}
            selectedTableSchemaId={dataViewerOptions.selectedTableSchemaId}
            setActiveTableSchemaId={tabSchemaId =>
              designerDispatch({
                type: 'SET_DATAVIEWER_OPTIONS',
                payload: { ...dataViewerOptions, tableSchemaId: tabSchemaId, selectedRecordErrorId: -1 }
              })
            }
            tableSchemaId={dataViewerOptions.tableSchemaId}
            viewType={designerState.viewType}
          />
        )}
        {designerState.datasetSchema && designerState.tabs && validationContext.isVisible && (
          <Validations
            datasetId={datasetId}
            datasetSchema={designerState.datasetSchema}
            datasetSchemas={designerState.datasetSchemas}
            tabs={DatasetDesignerUtils.getTabs({
              datasetSchema: designerState.datasetSchema,
              datasetSchemas: designerState.datasetSchemas,
              editable: true
            })}
          />
        )}
        <Snapshots
          isLoadingSnapshotListData={isLoadingSnapshotListData}
          isSnapshotDialogVisible={isSnapshotDialogVisible}
          setIsSnapshotDialogVisible={setIsSnapshotDialogVisible}
          snapshotListData={snapshotListData}
        />
        {validationsListDialog()}
        {renderUniqueConstraintsDialog()}

        <Integrations
          dataflowId={dataflowId}
          datasetId={datasetId}
          designerState={designerState}
          manageDialogs={manageDialogs}
          onUpdateData={onUpdateData}
        />

        <ManageUniqueConstraint
          dataflowId={dataflowId}
          designerState={designerState}
          manageDialogs={manageDialogs}
          refreshList={refreshUniqueList}
          resetUniques={manageUniqueConstraint}
          setConstraintManagingId={setConstraintManagingId}
          setIsUniqueConstraintCreating={setIsUniqueConstraintCreating}
          setIsUniqueConstraintUpdating={setIsUniqueConstraintUpdating}
        />

        {validateDialogVisible && (
          <ConfirmDialog
            header={resources.messages['validateDataset']}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            onConfirm={onConfirmValidate}
            onHide={() => manageDialogs('validateDialogVisible', false)}
            visible={validateDialogVisible}>
            {resources.messages['validateDatasetConfirm']}
          </ConfirmDialog>
        )}

        {designerState.dashDialogVisible && (
          <Dialog
            footer={renderDashboardFooter}
            header={resources.messages['titleDashboard']}
            onHide={() => designerDispatch({ type: 'TOGGLE_DASHBOARD_VISIBILITY', payload: false })}
            style={{ width: '70vw' }}
            visible={designerState.dashDialogVisible}>
            <Dashboard
              levelErrorTypes={designerState.levelErrorTypes}
              refresh={designerState.dashDialogVisible}
              tableSchemaNames={designerState.schemaTables.map(table => table.name)}
            />
          </Dialog>
        )}
        {designerState.isConfigureWebformDialogVisible && (
          <Dialog
            footer={renderConfigureWebformFooter}
            header={resources.messages['configureWebform']}
            onHide={() => {
              designerDispatch({ type: 'RESET_SELECTED_WEBFORM' });
              onCloseConfigureWebformModal();
            }}
            style={{ width: '30%' }}
            visible={designerState.isConfigureWebformDialogVisible}>
            <div className={styles.titleWrapper}>{resources.messages['configureWebformMessage']}</div>
            <Dropdown
              appendTo={document.body}
              ariaLabel={'configureWebform'}
              inputId="configureWebformDropDown"
              onChange={e =>
                designerDispatch({ type: 'SET_SELECTED_WEBFORM', payload: { selectedWebform: e.target.value } })
              }
              optionLabel="label"
              options={WebformsConfig}
              placeholder={resources.messages['configureWebformPlaceholder']}
              value={isUndefined(designerState.selectedWebform) ? designerState.webform : designerState.selectedWebform}
            />
          </Dialog>
        )}

        {designerState.isValidationViewerVisible && (
          <Dialog
            className={styles.paginatorValidationViewer}
            footer={renderValidationsFooter}
            header={resources.messages['titleValidations']}
            onHide={() => designerDispatch({ type: 'TOGGLE_VALIDATION_VIEWER_VISIBILITY', payload: false })}
            style={{ width: '90%' }}
            visible={designerState.isValidationViewerVisible}>
            <ValidationViewer
              datasetId={datasetId}
              datasetName={designerState.datasetSchemaName}
              datasetSchemaId={designerState.datasetSchemaId}
              hasWritePermissions={designerState.hasWritePermissions}
              isWebformView={designerState.viewType.webform}
              levelErrorTypes={designerState.datasetSchema.levelErrorTypes}
              onSelectValidation={onSelectValidation}
              schemaTables={designerState.schemaTables}
              switchToTabularData={switchToTabularData}
              tables={designerState.datasetSchema.tables}
              visible={designerState.isValidationViewerVisible}
            />
          </Dialog>
        )}

        {designerState.isImportDatasetDialogVisible && (
          <CustomFileUpload
            accept={validImportExtensions}
            chooseLabel={resources.messages['selectFile']}
            className={styles.FileUpload}
            dialogClassName={styles.Dialog}
            dialogHeader={`${resources.messages['uploadDataset']}${designerState.datasetSchemaName}`}
            dialogOnHide={() => {
              manageDialogs('isImportDatasetDialogVisible', false);
              setImportSelectedIntegrationId(null);
            }}
            dialogVisible={designerState.isImportDatasetDialogVisible}
            fileLimit={1}
            infoTooltip={infoExtensionsTooltip}
            invalidExtensionMessage={resources.messages['invalidExtensionFile']}
            isDialog={true}
            mode="advanced"
            multiple={false}
            name="file"
            onError={onImportDatasetError}
            onUpload={onUpload}
            replaceCheck={true}
            url={`${window.env.REACT_APP_BACKEND}${
              isNil(importSelectedIntegrationId)
                ? getUrl(DatasetConfig.importFileDataset, {
                    datasetId: datasetId,
                    delimiter: `${config.IMPORT_FILE_DELIMITER}`
                  })
                : getUrl(DatasetConfig.importFileDatasetExternal, {
                    datasetId: datasetId,
                    integrationId: importSelectedIntegrationId
                  })
            }`}
          />
        )}

        {isImportOtherSystemsDialogVisible && (
          <Dialog
            className={styles.Dialog}
            footer={renderImportOtherSystemsFooter}
            header={resources.messages['importPreviousDataHeader']}
            onHide={cleanImportOtherSystemsDialog}
            visible={isImportOtherSystemsDialogVisible}>
            <div className={styles.text}>{resources.messages['importPreviousDataConfirm']}</div>
            <div className={styles.checkboxWrapper}>
              <Checkbox
                checked={designerState.replaceData}
                id="replaceCheckbox"
                inputId="replaceCheckbox"
                onChange={() =>
                  designerDispatch({ type: 'SET_REPLACE_DATA', payload: { value: !designerState.replaceData } })
                }
                role="checkbox"
              />
              <label htmlFor="replaceCheckbox">
                <span
                  className={styles.replaceDataLabel}
                  onClick={() =>
                    designerDispatch({ type: 'SET_REPLACE_DATA', payload: { value: !designerState.replaceData } })
                  }>
                  {resources.messages['replaceData']}
                </span>
              </label>
            </div>
          </Dialog>
        )}
      </div>
    </SnapshotContext.Provider>
  );
});
