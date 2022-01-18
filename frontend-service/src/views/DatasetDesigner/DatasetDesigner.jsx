import { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import ReactTooltip from 'react-tooltip';

import camelCase from 'lodash/camelCase';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './DatasetDesigner.module.scss';

import { config } from 'conf';
import { DatasetConfig } from 'repositories/config/DatasetConfig';
import { DatasetSchemaRequesterEmptyHelpConfig } from 'conf/help/datasetSchema/requester/empty';
import { DatasetSchemaRequesterWithTabsHelpConfig } from 'conf/help/datasetSchema/requester/withTabs';

import { Button } from 'views/_components/Button';
import { CharacterCounter } from 'views/_components/CharacterCounter';
import { Checkbox } from 'views/_components/Checkbox';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { CustomFileUpload } from 'views/_components/CustomFileUpload';
import { Dashboard } from 'views/_components/Dashboard';
import { Dialog } from 'views/_components/Dialog';
import { Dropdown } from 'views/_components/Dropdown';
import { InputTextarea } from 'views/_components/InputTextarea';
import { Integrations } from './_components/Integrations';
import { MainLayout } from 'views/_components/Layout';
import { ManageUniqueConstraint } from './_components/ManageUniqueConstraint';
import { Menu } from 'views/_components/Menu';
import { QCList } from 'views/_components/QCList';
import { QCGenericHistory } from './_components/QCGenericHistory';
import { ShowValidationsList } from 'views/_components/ShowValidationsList';
import { Snapshots } from 'views/_components/Snapshots';
import { Spinner } from 'views/_components/Spinner';
import { TabsDesigner } from './_components/TabsDesigner';
import { TabularSwitch } from 'views/_components/TabularSwitch';
import { Title } from 'views/_components/Title';
import { Toolbar } from 'views/_components/Toolbar';
import { UniqueConstraints } from './_components/UniqueConstraints';
import { Validations } from 'views/DatasetDesigner/_components/Validations';
import { Webforms } from 'views/Webforms';

import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';
import { IntegrationService } from 'services/IntegrationService';
import { ValidationService } from 'services/ValidationService';
import { WebformService } from 'services/WebformService';

import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'views/_functions/Contexts/SnapshotContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';
import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

import { designerReducer } from './_functions/Reducers/designerReducer';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';
import { useDatasetDesigner } from 'views/_components/Snapshots/_hooks/useDatasetDesigner';

import { CurrentPage, ExtensionUtils, MetadataUtils, QuerystringUtils } from 'views/_functions/Utils';
import { DatasetDesignerUtils } from './_functions/Utils/DatasetDesignerUtils';
import { DatasetUtils } from 'services/_utils/DatasetUtils';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

export const DatasetDesigner = ({ isReferenceDataset = false }) => {
  const { dataflowId, datasetId } = useParams();

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);
  const validationContext = useContext(ValidationContext);
  const [selectedCustomImportIntegration, setSelectedCustomImportIntegration] = useState({
    id: null,
    name: ''
  });
  const [needsRefreshUnique, setNeedsRefreshUnique] = useState(true);
  const [sqlValidationRunning, setSqlValidationRunning] = useState(false);

  const [designerState, designerDispatch] = useReducer(designerReducer, {
    areLoadedSchemas: false,
    arePrefilledTablesDeleted: false,
    areUpdatingTables: false,
    availableInPublic: false,
    constraintManagingId: '',
    dashDialogVisible: false,
    dataflowName: '',
    dataflowType: '',
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
      selectedRuleId: '',
      selectedRuleLevelError: '',
      selectedRuleMessage: '',
      selectedTableSchemaId: null,
      tableSchemaId: QuerystringUtils.getUrlParamValue('tab')
    },
    exportButtonsList: [],
    exportDatasetFileType: '',
    externalOperationsList: { export: [], import: [], importOtherSystems: [] },
    hasQCsHistory: false,
    hasWritePermissions: false,
    importButtonsList: [],
    initialDatasetDescription: '',
    isCitizenScienceDataflow: false,
    isConfigureWebformDialogVisible: false,
    isDataflowOpen: false,
    isDataUpdated: false,
    isDeleteDialogVisible: false,
    isDownloadingQCRules: false,
    isDownloadingValidations: false,
    isDuplicatedToManageUnique: false,
    isExportTableSchemaDialogVisible: false,
    isHistoryDialogVisible: false,
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
    isUniqueConstraintsListDialogVisible: false,
    isUniqueConstraintUpdating: false,
    isValidateDialogVisible: false,
    isValidationsTabularView: false,
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
    schemaTables: [],
    selectedImportExtension: null,
    selectedWebform: undefined,
    tabs: [],
    uniqueConstraintsList: [],
    validationListDialogVisible: false,
    viewType: {
      design: TextUtils.areEquals(QuerystringUtils.getUrlParamValue('view'), 'design'),
      tabularData: TextUtils.areEquals(QuerystringUtils.getUrlParamValue('view'), 'tabularData'),
      webform: TextUtils.areEquals(QuerystringUtils.getUrlParamValue('view'), 'webform')
    },
    webform: null,
    webformOptions: [],
    webformOptionsLoadingStatus: 'pending'
  });

  const {
    arePrefilledTablesDeleted,
    datasetDescription,
    datasetSchemaAllTables,
    dataViewerOptions,
    isDataflowOpen,
    isDeleteDialogVisible,
    isDesignDatasetEditorRead,
    isImportOtherSystemsDialogVisible,
    isValidateDialogVisible,
    webformOptions,
    webformOptionsLoadingStatus
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
    dataflowType: designerState.dataflowType,
    isLoading: designerState.isLoading,
    referenceDataflowId: dataflowId
  });

  useEffect(() => {
    leftSideBarContext.removeModels();
    onLoadSchema();
    callSetMetaData();
    if (isEmpty(webformOptions)) {
      getWebformList();
    }
  }, []);

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
  }, [userContext, designerState.areLoadingSchemas, designerState.areUpdatingTables]);

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

    if (notificationContext.hidden.some(notification => notification.key === 'EXPORT_QC_FAILED_EVENT')) {
      setIsDownloadingQCRules(false);
    }
  }, [notificationContext.hidden]);

  const refreshUniqueList = value => setNeedsRefreshUnique(value);

  const callSetMetaData = async () => {
    const metaData = await getMetadata({ datasetId, dataflowId });
    designerDispatch({
      type: 'GET_METADATA',
      payload: {
        dataflowName: metaData.dataflow.name,
        dataflowType: metaData.dataflow.type,
        metaData,
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
        label: resourcesContext.messages[type.key]
      };
    });

    const externalIntegrationsNames = !isEmpty(externalOperationsList.export)
      ? [
          {
            label: resourcesContext.messages['customExports'],
            items: externalOperationsList.export.map(type => {
              return {
                command: () => onExportDataExternalIntegration(type.id),
                icon: type.fileExtension,
                label: `${type.name} (.${type.fileExtension})`
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
          setSelectedCustomImportIntegration({ id: null, name: resourcesContext.messages[type.key] });
        },
        icon: extensionsTypes[0],
        label: resourcesContext.messages[type.key]
      };
    });

    const { externalOperationsList } = designerState;

    const importFromFile = !isEmpty(externalOperationsList.import)
      ? [
          {
            label: resourcesContext.messages['customImports'],
            items: externalOperationsList.import.map(type => {
              return {
                command: () => {
                  manageDialogs('isImportDatasetDialogVisible', true);
                  designerDispatch({
                    type: 'GET_SELECTED_IMPORT_EXTENSION',
                    payload: { selectedImportExtension: type.fileExtension }
                  });
                  setSelectedCustomImportIntegration({ id: type.id, name: `${type.name} (.${type.fileExtension})` });
                },
                icon: type.fileExtension,
                label: `${type.name} (.${type.fileExtension})`
              };
            })
          }
        ]
      : [];

    const importOtherSystems = !isEmpty(externalOperationsList.importOtherSystems)
      ? [
          {
            label: resourcesContext.messages['importPreviousData'],
            items: externalOperationsList.importOtherSystems.map(importOtherSystem => ({
              id: importOtherSystem.id,
              label: importOtherSystem.name,
              icon: 'upload',
              command: () => {
                setSelectedCustomImportIntegration({ id: importOtherSystem.id, name: importOtherSystem.name });
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

  const getFileExtensions = async () => {
    try {
      const allExtensions = await IntegrationService.getAllExtensionsOperations(
        dataflowId,
        designerState.datasetSchemaId
      );
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
      console.error('DatasetDesigner - getFileExtensions.', error);
      notificationContext.add({ type: 'LOADING_FILE_EXTENSIONS_ERROR' }, true);
    }
  };

  const getMetadata = async ids => {
    try {
      return await MetadataUtils.getMetadata(ids);
    } catch (error) {
      console.error('DatasetDesigner - getMetadata.', error);
      notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId, datasetId } }, true);
    } finally {
      setIsLoading(false);
    }
  };

  const getStatisticsById = async (datasetId, tableSchemaNames) => {
    try {
      return await DatasetService.getStatistics(datasetId, tableSchemaNames);
    } catch (error) {
      console.error('DatasetDesigner - getStatisticsById.', error);
      throw new Error('ERROR_STATISTICS_BY_ID_ERROR');
    }
  };

  const getUniqueConstraintsList = data => designerDispatch({ type: 'GET_UNIQUES', payload: { data } });

  const getWebformList = async () => {
    setWebformOptionsLoadingStatus('pending');

    try {
      const data = await WebformService.getAll();
      data.unshift({ id: null, label: 'No webform', value: null });

      designerDispatch({ type: 'GET_WEBFORMS', payload: { data } });
      setWebformOptionsLoadingStatus('success');
    } catch (error) {
      console.error('DatasetsDesigner - getWebformList.', error);
      setWebformOptionsLoadingStatus('failed');
      notificationContext.add({ type: 'LOADING_WEBFORM_OPTIONS_ERROR' }, true);
    }
  };

  const setIsTableCreated = isTableCreated => {
    designerDispatch({ type: 'SET_IS_TABLE_CREATED', payload: { isTableCreated } });
  };

  const setIsLoading = value => designerDispatch({ type: 'SET_IS_LOADING', payload: { value } });

  const setWebformOptionsLoadingStatus = loadingStatus =>
    designerDispatch({ type: 'SET_WEBFORM_OPTIONS_LOADING_STATUS', payload: { loadingStatus } });

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
      await DatasetService.updateDatasetDesign(datasetId, { availableInPublic: checked });
    } catch (error) {
      console.error('DatasetDesigner - onChangeAvailableInPublicView.', error);
    }
  };

  const onChangeReferenceDataset = async checked => {
    try {
      designerDispatch({ type: 'SET_REFERENCE_DATASET', payload: checked });
      await DatasetService.updateDatasetDesign(datasetId, { referenceDataset: checked });
    } catch (error) {
      console.error('DatasetDesigner - onChangeReferenceDataset.', error);
    }
  };

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
    manageDialogs('isValidateDialogVisible', false);
    try {
      await DatasetService.validate(datasetId);
      notificationContext.add(
        {
          type: 'VALIDATE_DATA_INIT',
          content: {
            customContent: { origin: 'DESIGN' },
            dataflowId,
            dataflowName: designerState.dataflowName,
            datasetId,
            datasetName: designerState.datasetSchemaName,
            type: 'DESIGN'
          }
        },
        true
      );
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        console.error('DatasetDesigner - onConfirmValidate.', error);
        notificationContext.add(
          {
            type: 'VALIDATE_DESIGN_DATA_ERROR',
            content: {
              customContent: { origin: 'DESIGN' },
              dataflowId,
              dataflowName: designerState.dataflowName,
              datasetId,
              datasetName: designerState.datasetSchemaName
            }
          },
          true
        );
      }
    }
  };

  const onConfirmDelete = async () => {
    try {
      notificationContext.add({ type: 'DELETE_DATASET_DATA_INIT' });
      manageDialogs('isDeleteDialogVisible', false);
      await DatasetService.deleteData(datasetId, arePrefilledTablesDeleted);
      onResetDelete();
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        console.error('Dataset - onConfirmDelete.', error);
        notificationContext.add(
          {
            type: 'DATASET_SERVICE_DELETE_DATA_BY_ID_ERROR',
            content: {
              dataflowId,
              datasetId,
              dataflowName: designerState.dataflowName,
              datasetName: designerState.datasetSchemaName
            }
          },
          true
        );
      }
    }
  };

  const onHideDelete = () => {
    manageDialogs('isDeleteDialogVisible', false);
    onResetDelete();
  };

  const onExportError = async exportNotification => {
    const {
      dataflow: { name: dataflowName },
      dataset: { name: datasetName }
    } = await getMetadata({ dataflowId, datasetId });

    notificationContext.add(
      {
        type: exportNotification,
        content: { dataflowName: dataflowName, datasetName: datasetName }
      },
      true
    );
  };

  const onExportDataExternalIntegration = async integrationId => {
    setIsLoadingFile(true);
    notificationContext.add({ type: 'EXPORT_DATASET_DATA' });

    try {
      await DatasetService.exportDatasetDataExternal(datasetId, integrationId);
    } catch (error) {
      console.error('DatasetDesigner - onExportDataExternalIntegration.', error);
      notificationContext.add(
        {
          type: 'EXTERNAL_EXPORT_DESIGN_FAILED_EVENT',
          content: { dataflowId, datasetId, datasetName: designerState.datasetSchemaName }
        },
        true
      );
    }
  };

  const onExportDataInternalExtension = async fileType => {
    setIsLoadingFile(true);
    notificationContext.add({ type: 'EXPORT_DATASET_DATA' });

    try {
      await DatasetService.exportDatasetData(datasetId, fileType);
    } catch (error) {
      console.error('DatasetDesigner - onExportDataInternalExtension.', error);
      onExportError('EXPORT_DATA_BY_ID_ERROR');
    }
  };

  useEffect(() => {
    const validationFinished = notificationContext.toShow.find(
      notification => notification.key === 'VALIDATION_FINISHED_EVENT'
    );
    if (validationFinished && validationFinished.content.datasetId?.toString() === datasetId.toString()) {
      onHighlightRefresh(true);
    }
    const isImportFieldSchemaCompleted = notificationContext.toShow.some(
      notification => notification.key === 'IMPORT_FIELD_SCHEMA_COMPLETED_EVENT'
    );

    if (isImportFieldSchemaCompleted) {
      window.location.reload();
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
    ['AUTOMATICALLY_DOWNLOAD_QC_RULES_FILE', 'DOWNLOAD_QC_RULES_FILE_ERROR', 'DOWNLOAD_FILE_BAD_REQUEST_ERROR'],
    setIsDownloadingQCRules,
    false
  );

  useCheckNotifications(
    ['AUTOMATICALLY_DOWNLOAD_VALIDATIONS_FILE', 'DOWNLOAD_VALIDATIONS_FILE_ERROR', 'DOWNLOAD_FILE_BAD_REQUEST_ERROR'],
    setIsDownloadingValidations,
    false
  );

  useCheckNotifications(
    ['VALIDATE_RULES_COMPLETED_EVENT', 'VALIDATE_RULES_ERROR_EVENT'],
    setSqlValidationRunning,
    false
  );

  const onHideValidationsDialog = () => {
    if (validationContext.opener === 'validationsListDialog' && validationContext.reOpenOpener) {
      validationContext.onResetOpener();
    }
    manageDialogs('validationListDialogVisible', false);
  };

  const onResetDelete = () => {
    designerDispatch({
      type: 'SET_ARE_PREFILLED_TABLES_DELETED',
      payload: { arePrefilledTablesDeleted: false }
    });
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
        const dataset = await DatasetService.getSchema(dataflowId, datasetId);
        const tableSchemaList = [];
        dataset.tables.forEach(table => tableSchemaList.push({ name: table.tableSchemaName, id: table.tableSchemaId }));

        const datasetStatisticsDTO = await getStatisticsById(
          datasetId,
          dataset.tables.map(tableSchema => tableSchema.tableSchemaName)
        );

        setIsLoading(false);
        designerDispatch({
          type: 'GET_DATASET_DATA',
          payload: {
            availableInPublic: dataset.availableInPublic,
            datasetSchema: dataset,
            datasetStatistics: datasetStatisticsDTO,
            description: dataset.datasetSchemaDescription || '',
            levelErrorTypes: dataset.levelErrorTypes,
            previousWebform: dataset.webform,
            referenceDataset: dataset.referenceDataset,
            schemaId: dataset.datasetSchemaId,
            tables: dataset.tables,
            schemaTables: tableSchemaList,
            webform: dataset.webform
          }
        });
      };
      const getDatasetSchemas = async () => {
        const data = await DataflowService.getSchemas(dataflowId);
        designerDispatch({ type: 'LOAD_DATASET_SCHEMAS', payload: { schemas: data } });
      };

      getDatasetSchemaId();
      getDatasetSchemas();
    } catch (error) {
      console.error('DatasetDesigner - onLoadSchema.', error);
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
      await DatasetService.updateDatasetDesign(datasetId, descriptionObject);
    } catch (error) {
      console.error('DatasetDesigner - onUpdateDescription.', error);
    }
  };

  const onUpdateWebform = async () => {
    try {
      const webformObject = { webform: { name: designerState.selectedWebform.value } };
      await DatasetService.updateDatasetDesign(datasetId, webformObject);
      onLoadSchema();
    } catch (error) {
      console.error('DatasetDesigner - onUpdateWebform.', error);
    } finally {
      onCloseConfigureWebformModal();
    }
  };

  const onUpdateTable = tables => designerDispatch({ type: 'ON_UPDATE_TABLES', payload: { tables } });

  const onUpdateSchema = schema => designerDispatch({ type: 'ON_UPDATE_SCHEMA', payload: { schema } });

  const onUpload = async () => {
    manageDialogs('isImportDatasetDialogVisible', false);
    setSelectedCustomImportIntegration({ id: null, name: null });
    try {
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });

      notificationContext.add(
        {
          type: 'DATASET_DATA_LOADING_INIT',
          content: {
            customContent: {
              datasetLoading: resourcesContext.messages['datasetLoading'],
              datasetLoadingMessage: resourcesContext.messages['datasetLoadingMessage'],
              title: TextUtils.ellipsis(datasetName, config.notifications.STRING_LENGTH_MAX)
            },
            dataflowName,
            datasetName
          }
        },
        true
      );
    } catch (error) {
      console.error('DatasetDesigner - onUpload.', error);
      notificationContext.add(
        {
          type: 'EXTERNAL_IMPORT_DESIGN_FAILED_EVENT',
          content: { dataflowName: designerState.dataflowName, datasetName: designerState.datasetSchemaName }
        },
        true
      );
    }
  };

  const cleanImportOtherSystemsDialog = () => {
    designerDispatch({ type: 'SET_REPLACE_DATA', payload: { value: false } });
    manageDialogs('isImportOtherSystemsDialogVisible', false);
  };

  const onImportDatasetError = async ({ xhr }) => {
    if (xhr.status === 400) {
      notificationContext.add(
        {
          type: 'IMPORT_DESIGN_BAD_REQUEST_ERROR',
          content: {
            dataflowId,
            datasetId,
            datasetName: designerState.datasetSchemaName
          }
        },
        true
      );
    }
    if (xhr.status === 423) {
      notificationContext.add(
        {
          type: 'GENERIC_BLOCKED_ERROR',
          content: {
            dataflowId,
            datasetId,
            datasetName: designerState.datasetSchemaName
          }
        },
        true
      );
    }
  };

  const onImportOtherSystems = async () => {
    try {
      cleanImportOtherSystemsDialog();
      await IntegrationService.runIntegration(selectedCustomImportIntegration.id, datasetId, designerState.replaceData);
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
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        console.error('DatasetDesigner - onImportOtherSystems.', error);
        notificationContext.add(
          {
            type: 'EXTERNAL_IMPORT_DESIGN_FROM_OTHER_SYSTEM_FAILED_EVENT',
            content: { dataflowName: designerState.dataflowName, datasetName: designerState.datasetSchemaName }
          },
          true
        );
      }
    }
  };

  const validateQcRules = async () => {
    setSqlValidationRunning(true);
    try {
      await DatasetService.validateSqlRules(datasetId, designerState.datasetSchemaId);
    } catch (error) {
      console.error('DatasetDesigner - validateQcRules.', error);
    }
  };

  const setIsHistoryDialogVisible = isHistoryDialogVisible => {
    designerDispatch({
      type: 'SET_IS_HISTORY_DIALOG_VISIBLE',
      payload: isHistoryDialogVisible
    });
  };

  const onCloseHistoryDialog = () => {
    setIsHistoryDialogVisible(false);
  };

  const renderActionButtonsValidationDialog = (
    <div className={styles.qcDialogFooterWrapper}>
      {isDataflowOpen && (
        <span data-for="qcHistoryTooltip" data-tip>
          <Button
            className={`p-button-secondary ${designerState.hasQCsHistory ? 'p-button-animated-blink' : ''}`}
            disabled={!designerState.hasQCsHistory}
            icon="info"
            label={resourcesContext.messages['allQCsHistoryBtn']}
            onClick={() => setIsHistoryDialogVisible(true)}
          />
        </span>
      )}

      {!designerState.hasQCsHistory && (
        <ReactTooltip border effect="solid" id="qcHistoryTooltip" place="top">
          {resourcesContext.messages['genericQCsHistoryButtonTooltip']}
        </ReactTooltip>
      )}

      <Button
        className="p-button-secondary p-button-animated-blink"
        disabled={designerState.isDownloadingQCRules}
        icon={designerState.isDownloadingQCRules ? 'spinnerAnimate' : 'export'}
        label={resourcesContext.messages['downloadQCsButtonLabel']}
        onClick={() => onDownloadQCRules()}
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon="plus"
        label={resourcesContext.messages['createFieldValidationBtn']}
        onClick={() => validationContext.onOpenModalFromOpener('field', 'validationsListDialog')}
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon="plus"
        label={resourcesContext.messages['createRowValidationBtn']}
        onClick={() => validationContext.onOpenModalFromOpener('row', 'validationsListDialog')}
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon="plus"
        label={resourcesContext.messages['createTableValidationBtn']}
        onClick={() => validationContext.onOpenModalFromOpener('dataset', 'validationsListDialog')}
      />
      <Button
        className={`p-button-secondary p-button-animated-blink ${styles.buttonAlignRight}`}
        icon={sqlValidationRunning ? 'spinnerAnimate' : 'check'}
        label={resourcesContext.messages['validateSqlRulesBtn']}
        onClick={validateQcRules}
        tooltip={resourcesContext.messages['validateRulesBtnTootip']}
        tooltipOptions={{ position: 'top' }}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={onHideValidationsDialog}
      />
    </div>
  );

  const renderDashboardFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink p-button-right-aligned"
      icon="cancel"
      label={resourcesContext.messages['close']}
      onClick={() => designerDispatch({ type: 'TOGGLE_DASHBOARD_VISIBILITY', payload: false })}
    />
  );

  const renderImportOtherSystemsFooter = (
    <Fragment>
      <Button
        className="p-button-animated-blink"
        icon="check"
        label={resourcesContext.messages['import']}
        onClick={() => onImportOtherSystems()}
      />
      <Button
        className="p-button-secondary button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['cancel']}
        onClick={() => cleanImportOtherSystemsDialog()}
      />
    </Fragment>
  );

  function setIsDownloadingQCRules(isDownloadingQCRules) {
    designerDispatch({ type: 'SET_IS_DOWNLOADING_QC_RULES', payload: { isDownloadingQCRules } });
  }

  function setIsDownloadingValidations(isDownloadingValidations) {
    designerDispatch({ type: 'SET_IS_DOWNLOADING_VALIDATIONS', payload: { isDownloadingValidations } });
  }

  function setIsValidationsTabularView(isValidationsTabularView) {
    designerDispatch({ type: 'SET_IS_VALIDATIONS_TABULAR_VIEW', payload: { isValidationsTabularView } });
  }

  const onDownloadQCRules = async () => {
    setIsDownloadingQCRules(true);

    try {
      await ValidationService.generateQCRulesFile(datasetId);
      notificationContext.add({ type: 'DOWNLOAD_QC_RULES_START' });
    } catch (error) {
      if (error.response?.status === 400) {
        notificationContext.add({ type: 'DOWNLOAD_FILE_BAD_REQUEST_ERROR' }, true);
      } else {
        notificationContext.add({ type: 'GENERATE_QC_RULES_FILE_ERROR' }, true);
      }
      setIsDownloadingQCRules(false);
    }
  };

  const onDownloadValidations = async () => {
    setIsDownloadingValidations(true);
    try {
      await ValidationService.generateShowValidationsFile(datasetId);
      notificationContext.add({ type: 'DOWNLOAD_VALIDATIONS_START' });
    } catch (error) {
      console.error('DatasetDesigner - onDownloadValidations.', error);
      if (error.response?.status === 400) {
        notificationContext.add({ type: 'DOWNLOAD_FILE_BAD_REQUEST_ERROR' }, true);
      } else {
        notificationContext.add({ type: 'DOWNLOAD_VALIDATIONS_ERROR' }, true);
      }
      setIsDownloadingValidations(false);
    }
  };

  const renderValidationsFooter = (
    <div className={styles.validationsFooter}>
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        disabled={designerState.isDownloadingValidations}
        icon={designerState.isDownloadingValidations ? 'spinnerAnimate' : 'export'}
        label={resourcesContext.messages['downloadValidationsButtonLabel']}
        onClick={onDownloadValidations}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['close']}
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
        elements={Object.keys(designerState.viewType).map(view => resourcesContext.messages[`${view}View`])}
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
            ? resourcesContext.messages[`${QuerystringUtils.getUrlParamValue('view')}View`]
            : resourcesContext.messages['designView']
        }
      />
    );
  };

  const renderSwitchView = () => {
    const switchView = (
      <TabularSwitch
        elements={[resourcesContext.messages['designView'], resourcesContext.messages['tabularDataView']]}
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
            ? resourcesContext.messages[`${QuerystringUtils.getUrlParamValue('view')}View`]
            : resourcesContext.messages['designView']
        }
      />
    );

    return (
      <div className={styles.switchDivInput}>
        <div className={`${styles.switchDiv} datasetSchema-switchDesignToData-help-step`}>
          {!isNil(designerState.webform) && !isDataflowOpen && !isDesignDatasetEditorRead
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
        header={resourcesContext.messages['uniqueConstraints']}
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
        icon="check"
        label={resourcesContext.messages['save']}
        onClick={() => {
          onUpdateWebform();
          if (isNil(designerState?.selectedWebform?.value)) {
            changeMode('design');
          }
        }}
      />
      <Button
        className="p-button-secondary p-button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['cancel']}
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
          icon="plus"
          label={resourcesContext.messages['addUniqueConstraint']}
          onClick={() => manageDialogs('isManageUniqueConstraintDialogVisible', true)}
        />
      </div>
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['close']}
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

  const setHasQCsHistory = hasQCsHistory =>
    designerDispatch({ type: 'SET_HAS_QCS_HISTORY', payload: { hasQCsHistory } });

  const validationsListDialog = () => {
    if (designerState.validationListDialogVisible) {
      return (
        <Dialog
          footer={renderActionButtonsValidationDialog}
          header={resourcesContext.messages['qcRules']}
          onHide={() => onHideValidationsDialog()}
          style={{ width: '90%' }}
          visible={designerState.validationListDialogVisible}>
          <QCList
            dataflowId={dataflowId}
            dataset={designerState.metaData.dataset}
            datasetSchemaAllTables={datasetSchemaAllTables}
            datasetSchemaId={designerState.datasetSchemaId}
            isDataflowOpen={isDataflowOpen}
            isDatasetDesigner
            setHasQCsHistory={setHasQCsHistory}
          />
        </Dialog>
      );
    }
  };

  const deletePrefilledDataCheckbox = (
    <div className={styles.checkboxWrapper}>
      <Checkbox
        ariaLabelledBy="arePrefilledTablesDeleted"
        checked={arePrefilledTablesDeleted}
        id="arePrefilledTablesDeleted"
        inputId="arePrefilledTablesDeleted"
        onChange={() =>
          designerDispatch({
            type: 'SET_ARE_PREFILLED_TABLES_DELETED',
            payload: { arePrefilledTablesDeleted: !arePrefilledTablesDeleted }
          })
        }
        role="checkbox"
      />
      <label htmlFor="arePrefilledTablesDeletedCheckbox">
        <span
          onClick={() =>
            designerDispatch({
              type: 'SET_ARE_PREFILLED_TABLES_DELETED',
              payload: { arePrefilledTablesDeleted: !arePrefilledTablesDeleted }
            })
          }>
          {resourcesContext.messages['arePrefilledTablesDeletedCheckboxLabel']}
        </span>
      </label>
    </div>
  );

  const renderWebformOptionsContent = () => {
    const webform = webformOptions.find(option => option.value === designerState.webform);

    if (webformOptionsLoadingStatus === 'pending') return <Spinner style={{ top: 0 }} />;

    return (
      <Fragment>
        <div className={styles.titleWrapper}>{resourcesContext.messages['configureWebformMessage']}</div>
        <Dropdown
          appendTo={document.body}
          ariaLabel={'configureWebform'}
          inputId="configureWebformDropDown"
          onChange={e =>
            designerDispatch({ type: 'SET_SELECTED_WEBFORM', payload: { selectedWebform: e.target.value } })
          }
          optionLabel="label"
          options={webformOptions}
          placeholder={resourcesContext.messages['configureWebformPlaceholder']}
          value={designerState.selectedWebform || webform}
        />
      </Fragment>
    );
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
          ariaLabelledBy={designerState.datasetSchemaName}
          icon="pencilRuler"
          iconSize="3.4rem"
          subtitle={designerState.dataflowName}
          title={`${resourcesContext.messages['datasetSchema']}: ${designerState.datasetSchemaName}`}
        />
        <h4 className={styles.descriptionLabel}>
          {resourcesContext.messages['newDatasetSchemaDescriptionPlaceHolder']}
        </h4>
        <div className={styles.ButtonsBar}>
          <div className={styles.datasetDescriptionRow}>
            <InputTextarea
              className={`${styles.datasetDescription} datasetSchema-metadata-help-step`}
              collapsedHeight={75}
              disabled={isDataflowOpen || isDesignDatasetEditorRead}
              id="datasetDescription"
              key="datasetDescription"
              onBlur={e => onBlurDescription(e.target.value)}
              onChange={e => designerDispatch({ type: 'ON_UPDATE_DESCRIPTION', payload: { value: e.target.value } })}
              onFocus={e =>
                designerDispatch({ type: 'INITIAL_DATASET_DESCRIPTION', payload: { value: e.target.value } })
              }
              onKeyDown={e => onKeyChange(e)}
              placeholder={resourcesContext.messages['newDatasetSchemaDescriptionPlaceHolder']}
              value={datasetDescription}
            />
            <CharacterCounter
              currentLength={datasetDescription.length}
              maxLength={config.DESCRIPTION_MAX_LENGTH}
              style={{ position: 'relative', right: '75px', top: '52px' }}
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
                  {resourcesContext.messages['referenceDataset']}
                </label>
              </div>
              {!TextUtils.areEquals(designerState.dataflowType, config.dataflowType.BUSINESS.value) && (
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
                    {resourcesContext.messages['availableInPublicView']}
                  </label>
                </div>
              )}
              <Button
                className={`p-button-secondary ${
                  !isDataflowOpen && !isDesignDatasetEditorRead && !designerState.referenceDataset
                    ? 'p-button-animated-blink'
                    : null
                } datasetSchema-uniques-help-step`}
                disabled={isDataflowOpen || isDesignDatasetEditorRead || designerState.referenceDataset}
                icon="table"
                label={resourcesContext.messages['configureWebform']}
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
                icon="import"
                label={resourcesContext.messages['importDataset']}
                onClick={event => importMenuRef.current.show(event)}
              />
              <Menu
                className={styles.menuWrapper}
                id="importDatasetMenu"
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
                label={resourcesContext.messages['exportDataset']}
                onClick={event => exportMenuRef.current.show(event)}
              />
              <Menu
                className={styles.menuWrapper}
                id="exportDatasetMenu"
                model={designerState.exportButtonsList}
                popup={true}
                ref={exportMenuRef}
              />
              <Button
                className="p-button-rounded p-button-secondary-transparent p-button-animated-blink dataset-deleteDataset-help-step"
                icon="trash"
                label={resourcesContext.messages['deleteDatasetData']}
                onClick={() => manageDialogs('isDeleteDialogVisible', true)}
              />
            </div>
            <div className="p-toolbar-group-right">
              <Button
                className={`p-button-rounded p-button-secondary-transparent ${
                  !isDataflowOpen && !isDesignDatasetEditorRead ? ' p-button-animated-blink' : null
                }`}
                disabled={isDataflowOpen || isDesignDatasetEditorRead}
                icon="validate"
                label={resourcesContext.messages['validate']}
                onClick={() => manageDialogs('isValidateDialogVisible', true)}
              />

              <Button
                className="p-button-rounded p-button-secondary-transparent p-button-animated-blink"
                icon="warning"
                iconClasses={designerState.datasetStatistics.datasetErrors ? 'warning' : ''}
                label={resourcesContext.messages['showValidations']}
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
                label={resourcesContext.messages['qcRules']}
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
                label={resourcesContext.messages['uniqueConstraints']}
                onClick={() => manageDialogs('isUniqueConstraintsListDialogVisible', true)}
              />

              <Button
                className={`p-button-rounded p-button-secondary-transparent ${
                  !isDesignDatasetEditorRead && !designerState.referenceDataset ? styles.integrationsButton : null
                }`}
                disabled={isDesignDatasetEditorRead || designerState.referenceDataset}
                icon="export"
                iconClasses={styles.integrationsButtonIcon}
                label={resourcesContext.messages['externalIntegrations']}
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
                label={resourcesContext.messages['dashboards']}
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
                label={resourcesContext.messages['snapshots']}
                onClick={() => setIsSnapshotsBarVisible(!isSnapshotsBarVisible)}
              />

              <Button
                className={`p-button-rounded p-button-${
                  designerState.isRefreshHighlighted ? 'primary' : 'secondary-transparent'
                }  ${!isDataflowOpen && !isDesignDatasetEditorRead ? 'p-button-animated-blink' : null}`}
                disabled={isDataflowOpen || isDesignDatasetEditorRead}
                icon="refresh"
                label={resourcesContext.messages['refresh']}
                onClick={onLoadSchema}
              />
            </div>
          </Toolbar>
        </div>
        {renderSwitchView()}
        {!isNil(designerState.webform) && designerState.viewType['webform'] ? (
          <Webforms
            dataflowId={dataflowId}
            datasetId={datasetId}
            options={webformOptions}
            state={designerState}
            webformType={designerState.webform}
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
            isDataflowOpen={isDataflowOpen}
            isDesignDatasetEditorRead={isDesignDatasetEditorRead}
            isGroupedValidationDeleted={dataViewerOptions.isGroupedValidationDeleted}
            isGroupedValidationSelected={dataViewerOptions.isGroupedValidationSelected}
            isReferenceDataset={designerState.referenceDataset}
            manageDialogs={manageDialogs}
            manageUniqueConstraint={manageUniqueConstraint}
            onChangeReference={onChangeReference}
            onHideSelectGroupedValidation={onHideSelectGroupedValidation}
            onLoadTableData={onLoadTableData}
            onTabChange={onTabChange}
            onUpdateSchema={onUpdateSchema}
            onUpdateTable={onUpdateTable}
            selectedRuleId={dataViewerOptions.selectedRuleId}
            selectedRuleLevelError={dataViewerOptions.selectedRuleLevelError}
            selectedRuleMessage={dataViewerOptions.selectedRuleMessage}
            selectedTableSchemaId={dataViewerOptions.selectedTableSchemaId}
            setActiveTableSchemaId={tabSchemaId =>
              designerDispatch({
                type: 'SET_DATAVIEWER_OPTIONS',
                payload: { ...dataViewerOptions, tableSchemaId: tabSchemaId }
              })
            }
            tableSchemaId={dataViewerOptions.tableSchemaId}
            viewType={designerState.viewType}
          />
        )}
        {designerState.datasetSchema && designerState.tabs && validationContext.isVisible && (
          <Validations
            dataflowType={designerState.dataflowType}
            datasetId={datasetId}
            datasetSchema={designerState.datasetSchema}
            datasetSchemas={designerState.datasetSchemas}
            isCitizenScienceDataflow={designerState.isCitizenScienceDataflow}
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

        {isValidateDialogVisible && (
          <ConfirmDialog
            header={resourcesContext.messages['validateDataset']}
            labelCancel={resourcesContext.messages['no']}
            labelConfirm={resourcesContext.messages['yes']}
            onConfirm={onConfirmValidate}
            onHide={() => manageDialogs('isValidateDialogVisible', false)}
            visible={isValidateDialogVisible}>
            {resourcesContext.messages['validateDatasetConfirm']}
          </ConfirmDialog>
        )}

        {isDeleteDialogVisible && (
          <ConfirmDialog
            classNameConfirm={'p-button-danger'}
            header={resourcesContext.messages['deleteDatasetHeader']}
            labelCancel={resourcesContext.messages['no']}
            labelConfirm={resourcesContext.messages['yes']}
            onConfirm={onConfirmDelete}
            onHide={onHideDelete}
            visible={isDeleteDialogVisible}>
            <div>{resourcesContext.messages['deleteDatasetConfirm']}</div>
            {deletePrefilledDataCheckbox}
          </ConfirmDialog>
        )}

        {designerState.dashDialogVisible && (
          <Dialog
            footer={renderDashboardFooter}
            header={resourcesContext.messages['titleDashboard']}
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
            header={resourcesContext.messages['configureWebform']}
            onHide={() => {
              designerDispatch({ type: 'RESET_SELECTED_WEBFORM' });
              onCloseConfigureWebformModal();
            }}
            style={{ width: '30%' }}
            visible={designerState.isConfigureWebformDialogVisible}>
            {renderWebformOptionsContent()}
          </Dialog>
        )}

        {designerState.isValidationViewerVisible && (
          <Dialog
            className={styles.paginatorValidationViewer}
            footer={renderValidationsFooter}
            header={resourcesContext.messages['titleValidations']}
            onHide={() => designerDispatch({ type: 'TOGGLE_VALIDATION_VIEWER_VISIBILITY', payload: false })}
            style={{ width: '90%' }}
            visible={designerState.isValidationViewerVisible}>
            <ShowValidationsList
              dataflowId={dataflowId}
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

        {designerState.isHistoryDialogVisible && (
          <QCGenericHistory
            datasetId={datasetId}
            isDialogVisible={designerState.isHistoryDialogVisible}
            onCloseDialog={onCloseHistoryDialog}
          />
        )}

        {designerState.isImportDatasetDialogVisible && (
          <CustomFileUpload
            accept={DatasetUtils.getValidExtensions({ validExtensions: designerState.selectedImportExtension })}
            chooseLabel={resourcesContext.messages['selectFile']}
            className={styles.FileUpload}
            dialogClassName={styles.Dialog}
            dialogHeader={selectedCustomImportIntegration.name}
            dialogOnHide={() => {
              manageDialogs('isImportDatasetDialogVisible', false);
              setSelectedCustomImportIntegration({ id: null, name: null });
            }}
            dialogVisible={designerState.isImportDatasetDialogVisible}
            infoTooltip={`${
              resourcesContext.messages['supportedFileExtensionsTooltip']
            } ${DatasetUtils.getValidExtensions({
              isTooltip: true,
              validExtensions: designerState.selectedImportExtension
            })}`}
            invalidExtensionMessage={resourcesContext.messages['invalidExtensionFile']}
            isDialog={true}
            name="file"
            onError={onImportDatasetError}
            onUpload={onUpload}
            replaceCheck={true}
            url={`${window.env.REACT_APP_BACKEND}${
              isNil(selectedCustomImportIntegration.id)
                ? getUrl(DatasetConfig.importFileDataset, {
                    datasetId: datasetId,
                    delimiter: encodeURIComponent(config.IMPORT_FILE_DELIMITER)
                  })
                : getUrl(DatasetConfig.importFileDatasetExternal, {
                    datasetId: datasetId,
                    integrationId: selectedCustomImportIntegration.id
                  })
            }`}
          />
        )}

        {isImportOtherSystemsDialogVisible && (
          <Dialog
            className={styles.Dialog}
            footer={renderImportOtherSystemsFooter}
            header={selectedCustomImportIntegration.name}
            onHide={() => {
              cleanImportOtherSystemsDialog();
              setSelectedCustomImportIntegration({ id: null, name: null });
            }}
            visible={isImportOtherSystemsDialogVisible}>
            <div
              className={styles.text}
              dangerouslySetInnerHTML={{
                __html: TextUtils.parseText(resourcesContext.messages['importPreviousDataConfirm'], {
                  importName: selectedCustomImportIntegration.name
                })
              }}></div>
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
                  {resourcesContext.messages['replaceData']}
                </span>
              </label>
            </div>
          </Dialog>
        )}
      </div>
    </SnapshotContext.Provider>
  );
};
