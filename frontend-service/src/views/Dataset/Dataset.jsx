import { Fragment, useContext, useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './Dataset.module.scss';

import { config } from 'conf';
import { DatasetConfig } from 'repositories/config/DatasetConfig';
import { DatasetSchemaReporterHelpConfig } from 'conf/help/datasetSchema/reporter';
import { routes } from 'conf/routes';

import { Button } from 'views/_components/Button';
import { Checkbox } from 'views/_components/Checkbox';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { CustomFileUpload } from 'views/_components/CustomFileUpload';
import { DatasetDashboardDialog } from 'views/_components/DatasetDashboardDialog';
import { Dialog } from 'views/_components/Dialog';
import { DatasetDeleteDataDialog } from 'views/_components/DatasetDeleteDataDialog';
import { MainLayout } from 'views/_components/Layout';
import { Menu } from 'views/_components/Menu';
import { QCList } from 'views/_components/QCList';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ShowValidationsList } from 'views/_components/ShowValidationsList';
import { SnapshotContext } from 'views/_functions/Contexts/SnapshotContext';
import { Snapshots } from 'views/_components/Snapshots';
import { Spinner } from 'views/_components/Spinner';
import { TabsSchema } from 'views/_components/TabsSchema';
import { TabularSwitch } from 'views/_components/TabularSwitch';
import { Title } from 'views/_components/Title';
import { Toolbar } from 'views/_components/Toolbar';
import { DatasetValidateDialog } from 'views/_components/DatasetValidateDialog';

import { Webforms } from 'views/Webforms';

import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';
import { IntegrationService } from 'services/IntegrationService';
import { ValidationService } from 'services/ValidationService';
import { WebformService } from 'services/WebformService';

import { ActionsContext } from 'views/_functions/Contexts/ActionsContext';
import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';
import { useReporterDataset } from 'views/_components/Snapshots/_hooks/useReporterDataset';

import { CurrentPage, ExtensionUtils, MetadataUtils, QuerystringUtils } from 'views/_functions/Utils';
import { DatasetUtils } from 'services/_utils/DatasetUtils';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

export const Dataset = ({ isReferenceDatasetReferenceDataflow }) => {
  const navigate = useNavigate();
  const { dataflowId, datasetId } = useParams();

  const actionsContext = useContext(ActionsContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [dataset, setDataset] = useState({});
  const [datasetProgressBarSteps, setDatasetProgressBarSteps] = useState({
    steps: [
      {
        stepNumber: 1,
        labelCompleted: resourcesContext.messages['importedData'],
        labelUndone: resourcesContext.messages['importData'],
        labelRunning: resourcesContext.messages['importingData'],
        labelError: resourcesContext.messages['importDataError']
      },
      {
        stepNumber: 2,
        labelCompleted: resourcesContext.messages['validatedData'],
        labelUndone: resourcesContext.messages['validateData'],
        labelRunning: resourcesContext.messages['validatingData'],
        labelError: resourcesContext.messages['validatingDataError'],
        isRunning: true
      }
    ],
    currentStep: 0
  });
  const [datasetSchemaAllTables, setDatasetSchemaAllTables] = useState([]);
  const [datasetSchemaName, setDatasetSchemaName] = useState();
  const [datasetName, setDatasetName] = useState('');
  const [datasetHasErrors, setDatasetHasErrors] = useState(false);
  const [dataViewerOptions, setDataViewerOptions] = useState({
    isGroupedValidationDeleted: false,
    isGroupedValidationSelected: false,
    selectedRuleId: '',
    selectedRuleLevelError: '',
    selectedRuleMessage: '',
    selectedShortCode: '',
    selectedTableSchemaId: null,
    tableSchemaId: QuerystringUtils.getUrlParamValue('tab') !== '' ? QuerystringUtils.getUrlParamValue('tab') : ''
  });
  const [datasetHasData, setDatasetHasData] = useState(false);
  const [exportButtonsList, setExportButtonsList] = useState([]);
  const [externalOperationsList, setExternalOperationsList] = useState({
    export: [],
    import: [],
    importOtherSystems: []
  });
  const [dataflowType, setDataflowType] = useState('');
  const [datasetStatisticsInState, setDatasetStatisticsInState] = useState(undefined);
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [importButtonsList, setImportButtonsList] = useState([]);
  const [isIcebergCreated, setIsIcebergCreated] = useState(false);
  const [isLoadingIceberg, setIsLoadingIceberg] = useState(false);
  const [selectedCustomImportIntegration, setSelectedCustomImportIntegration] = useState({
    id: null,
    name: ''
  });
  const [importSelectedIntegrationExtension, setImportSelectedIntegrationExtension] = useState(null);
  const [isDataLoaded, setIsDataLoaded] = useState(false);
  const [isDataset, setIsDataset] = useState(false);
  const [isDatasetReleased, setIsDatasetReleased] = useState(false);
  const [isDatasetUpdatable, setIsDatasetUpdatable] = useState(false);
  const [isDownloadingQCRules, setIsDownloadingQCRules] = useState(false);
  const [isDownloadingValidations, setIsDownloadingValidations] = useState(false);
  const [isImportDatasetDialogVisible, setIsImportDatasetDialogVisible] = useState(false);
  const [isImportOtherSystemsDialogVisible, setIsImportOtherSystemsDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isReferenceDataset, setIsReferenceDataset] = useState(false);
  const [isRefreshHighlighted, setIsRefreshHighlighted] = useState(false);
  const [isReportingWebform, setIsReportingWebform] = useState(false);
  const [selectedView, setSelectedView] = useState('');
  const [isTableDataRestorationInProgress, setIsTableDataRestorationInProgress] = useState(false);
  const [isTestDataset, setIsTestDataset] = useState(false);

  const [isUpdatableDialogVisible, setIsUpdatableDialogVisible] = useState(false);
  const [levelErrorTypes, setLevelErrorTypes] = useState([]);
  const [metadata, setMetadata] = useState(undefined);
  const [noEditableCheck, setNoEditableCheck] = useState(false);
  const [replaceData, setReplaceData] = useState(false);
  const [schemaTables, setSchemaTables] = useState([]);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();
  const [validationListDialogVisible, setValidationListDialogVisible] = useState(false);
  const [validationsVisible, setValidationsVisible] = useState(false);
  const [webformData, setWebformData] = useState(null);
  const [webformOptions, setWebformOptions] = useState([]);

  let exportMenuRef = useRef();
  let importMenuRef = useRef();
  let bigDataRef = useRef();

  bigDataRef.current = metadata?.dataflow.bigData;

  useBreadCrumbs({
    currentPage: getCurrentPage(),
    dataflowId,
    dataflowType,
    dataProviderId: metadata?.dataset.dataProviderId,
    dataProviderName: metadata?.dataset.name,
    isLoading,
    metaData: metadata,
    referenceDataflowId: dataflowId
  });

  useEffect(() => {
    leftSideBarContext.removeModels();
    getMetadata();
    if (isEmpty(webformOptions)) {
      getWebformList();
    }
  }, []);

  useEffect(() => {
    if (!isUndefined(metadata)) {
      onLoadDatasetSchema();
    }
  }, [metadata]);

  useEffect(() => {
    if (!isNil(tableSchema) && tableSchema.length > 0) {
      setDataViewerOptions({
        ...dataViewerOptions,
        tableSchemaId:
          QuerystringUtils.getUrlParamValue('tab') !== '' ? QuerystringUtils.getUrlParamValue('tab') : tableSchema[0].id
      });
    }
  }, [tableSchema]);

  useEffect(() => {
    if (isNil(dataset)) {
      return;
    }

    if (!isUndefined(userContext.contextRoles)) {
      if (isDataset) {
        if (dataset.isReleasing) {
          setHasWritePermissions(false);
        } else {
          const isReporterDataset = userContext.hasPermission(
            [config.permissions.roles.LEAD_REPORTER.key, config.permissions.roles.REPORTER_WRITE.key],
            `${config.permissions.prefixes.DATASET}${datasetId}`
          );
          setHasWritePermissions(isReporterDataset);
        }
      } else if (isTestDataset) {
        const hasWritePermissionsTestDataset = userContext.hasPermission(
          [
            config.permissions.roles.CUSTODIAN.key,
            config.permissions.roles.STEWARD.key,
            config.permissions.roles.STEWARD_SUPPORT.key
          ],
          `${config.permissions.prefixes.TESTDATASET}${datasetId}`
        );
        setHasWritePermissions(hasWritePermissionsTestDataset);
      } else if (isReferenceDataset) {
        const isCustodianInReferenceDataset = userContext.hasContextAccessPermission(
          config.permissions.prefixes.REFERENCEDATASET,
          datasetId,
          [config.permissions.roles.CUSTODIAN.key, config.permissions.roles.STEWARD.key]
        );
        setHasWritePermissions(isCustodianInReferenceDataset && isDatasetUpdatable);

        if (isCustodianInReferenceDataset) {
          leftSideBarContext.addModels([
            {
              className: 'dataflow-showPublicInfo-help-step',
              icon: 'lock',
              isVisible: true,
              label: 'referenceUpdateStatusLeftSideBarButton',
              onClick: () => setIsUpdatableDialogVisible(true),
              title: 'referenceUpdateStatusLeftSideBarButton'
            }
          ]);
        }
      }
    }
  }, [userContext, dataset]);

  useEffect(() => {
    if (!isNil(webformData)) {
      setIsReportingWebform(webformData?.type === 'PAMS');
    }
  }, [webformData]);

  useEffect(() => {
    if (!isUndefined(userContext.contextRoles)) {
      leftSideBarContext.addHelpSteps(DatasetSchemaReporterHelpConfig, 'datasetSchemaReporterHelpConfig');
    }
  }, [userContext, isDataLoaded, tableSchemaColumns]);

  useEffect(() => {
    setExportButtonsList(
      isEmpty(externalOperationsList.export) ? internalExtensions : internalExtensions.concat(externalIntegrationsNames)
    );
  }, [datasetName, externalOperationsList.export]);

  useEffect(() => {
    setImportButtonsList(internalImportExtensionsList.concat(importFromFile).concat(importFromOtherSystems));
  }, [externalOperationsList.import]);

  useEffect(() => {
    if (notificationContext.hidden.some(notification => notification.key === 'EXPORT_DATASET_FAILED_EVENT')) {
      actionsContext.changeExportDatasetState(false);
    }

    if (notificationContext.hidden.some(notification => notification.key === 'DOWNLOAD_VALIDATIONS_FAILED_EVENT')) {
      setIsDownloadingValidations(false);
    }

    if (notificationContext.hidden.some(notification => notification.key === 'EXPORT_QC_FAILED_EVENT')) {
      setIsDownloadingQCRules(false);
    }
  }, [notificationContext.hidden]);

  const {
    isLoadingSnapshotListData,
    isSnapshotDialogVisible,
    isSnapshotsBarVisible,
    setIsSnapshotDialogVisible,
    setIsSnapshotsBarVisible,
    snapshotDispatch,
    snapshotListData,
    snapshotState
  } = useReporterDataset(datasetId, dataflowId);

  useEffect(() => {
    actionsContext.testProcess(datasetId);
  }, [datasetId]);

  useEffect(() => {
    onGetIcebergTables();
    onLoadDataflow();
  }, []);

  useEffect(() => {
    if (snapshotState.isRestoring) {
      changeProgressStepBar({ step: 0, currentStep: 1, isRunning: true, completed: false, withError: false });
    }
  }, [snapshotState.isRestoring]);

  useEffect(() => {
    if (metadata?.dataset.datasetSchemaId) getFileExtensions();
  }, [metadata?.dataset.datasetSchemaId, isImportDatasetDialogVisible]);

  useEffect(() => {
    getExportIntegrationsNames(externalOperationsList.export);
  }, [externalOperationsList]);

  useEffect(() => {
    if (window.location.search !== '' && !isNil(dataViewerOptions.tableSchemaId)) {
      changeUrl();
    }
  }, [dataViewerOptions.tableSchemaId, selectedView]);

  const convertHelper = async () => {
    setIsLoadingIceberg(true);
    if (isIcebergCreated) {
      await DatasetService.convertIcebergsToParquets({
        dataflowId,
        datasetId,
        providerId: !isTestDataset ? metadata?.dataset.dataProviderId : undefined
      });
    } else {
      await DatasetService.convertParquetsToIcebergs({
        dataflowId,
        datasetId,
        providerId: !isTestDataset ? metadata?.dataset.dataProviderId : undefined
      });
    }
    setIsIcebergCreated(!isIcebergCreated);
    setIsLoadingIceberg(false);
  };

  const onGetIcebergTables = async () => {
    const icebergTables = await DataflowService.getIcebergTables({ dataflowId, datasetId });
    setIsIcebergCreated(!isEmpty(icebergTables?.data));
  };

  const changeProgressStepBar = stepInfo => {
    const inmDatasetProgressBarSteps = [...datasetProgressBarSteps.steps];
    inmDatasetProgressBarSteps[stepInfo.step].isRunning = stepInfo.isRunning;
    inmDatasetProgressBarSteps[stepInfo.step].completed = stepInfo.completed || false;
    setDatasetProgressBarSteps({ steps: inmDatasetProgressBarSteps, currentStep: stepInfo.currentStep });
  };

  const changeUrl = () => {
    window.history.replaceState(
      null,
      null,
      `?tab=${
        dataViewerOptions.tableSchemaId !== ''
          ? dataViewerOptions.tableSchemaId
          : !isEmpty(tableSchema)
          ? tableSchema[0].id
          : ''
      }${!isNil(webformData?.name) ? `&view=${selectedView}` : ''}`
    );
  };

  const parseExportIntegrationsNames = exportNamesOperationsList => {
    return exportNamesOperationsList.map(exportNameOperation => ({
      text: `${exportNameOperation.toUpperCase()} (.${exportNameOperation.toLowerCase()})`,
      code: exportNameOperation.toLowerCase()
    }));
  };

  const getExportIntegrationsNames = exportOperationsList => {
    parseExportIntegrationsNames(exportOperationsList.map(element => element.name));
  };

  const getWebformList = async () => {
    try {
      setWebformOptions(await WebformService.getAll());
    } catch (error) {
      console.error('Dataset - getWebformList.', error);
      notificationContext.add({ type: 'LOADING_WEBFORM_OPTIONS_ERROR' }, true);
    }
  };

  const internalImportExtensionsList = config.importTypes.importDatasetTypes.map(type => {
    const extensionsTypes = !isNil(type.code) && type.code.split('+');
    return {
      command: () => {
        setIsImportDatasetDialogVisible(true);
        setSelectedCustomImportIntegration({ id: null, name: resourcesContext.messages[type.key] });
        setImportSelectedIntegrationExtension(type.code);
      },
      icon: extensionsTypes[0],
      label: resourcesContext.messages[type.key]
    };
  });

  const importFromFile = !isEmpty(externalOperationsList.import)
    ? [
        {
          label: resourcesContext.messages['customImports'],
          items: externalOperationsList.import.map(type => {
            return {
              command: () => {
                setIsImportDatasetDialogVisible(true);
                setSelectedCustomImportIntegration({ id: type.id, name: `${type.name} (.${type.fileExtension})` });
                setImportSelectedIntegrationExtension(type.fileExtension);
              },
              icon: type.fileExtension,
              label: `${type.name} (.${type.fileExtension})`
            };
          })
        }
      ]
    : [];

  const importFromOtherSystems = !isEmpty(externalOperationsList.importOtherSystems)
    ? [
        {
          label: resourcesContext.messages['importPreviousData'],
          items: externalOperationsList.importOtherSystems.map(importOtherSystem => ({
            label: importOtherSystem.name,
            icon: 'upload',
            command: () => {
              setSelectedCustomImportIntegration({ id: importOtherSystem.id, name: importOtherSystem.name });
              setIsImportOtherSystemsDialogVisible(true);
            }
          }))
        }
      ]
    : [];

  const internalExtensions = config.exportTypes.exportDatasetTypes
    .map(type => {
      const extensionsTypes = !isNil(type.code) && type.code.split('+');

      if (bigDataRef?.current) {
        if (extensionsTypes?.includes('zip') && extensionsTypes?.includes('csv')) {
          return {
            command: () => onExportDataInternalExtension(type.code),
            icon: extensionsTypes[0],
            label: resourcesContext.messages[type.key]
          };
        } else {
          return null;
        }
      } else {
        return {
          command: () => onExportDataInternalExtension(type.code),
          icon: extensionsTypes[0],
          label: resourcesContext.messages[type.key]
        };
      }
    })
    .filter(item => item !== null);

  const externalIntegrationsNames = [
    {
      label: resourcesContext.messages['customExports'],
      items: externalOperationsList.export.map(type => ({
        label: `${type.name} (.${type.fileExtension})`,
        icon: type.fileExtension,
        command: () => onExportDataExternalIntegration(type.id)
      }))
    }
  ];

  function getCurrentPage() {
    if (isDataset) {
      return CurrentPage.DATASET;
    } else if (isTestDataset) {
      return CurrentPage.TEST_DATASETS;
    } else if (isReferenceDatasetReferenceDataflow) {
      return CurrentPage.REFERENCE_DATASET;
    } else if (isReferenceDataset) {
      return CurrentPage.DATAFLOW_REFERENCE_DATASET;
    }
  }

  const getFileExtensions = async () => {
    try {
      const allExtensions = await IntegrationService.getAllExtensionsOperations(
        dataflowId,
        metadata.dataset.datasetSchemaId
      );
      setExternalOperationsList(ExtensionUtils.groupOperations('operation', allExtensions));
    } catch (error) {
      console.error('Dataset - getFileExtensions.', error);
      notificationContext.add({ type: 'LOADING_FILE_EXTENSIONS_ERROR' }, true);
    }
  };

  const getMetadata = async () => {
    try {
      const metaData = await MetadataUtils.getMetadata({ datasetId, dataflowId });
      setMetadata(metaData);

      const stepStatus = DatasetUtils.getDatasetStepRunningStatus(metaData.dataset.datasetRunningStatus);
      changeProgressStepBar(stepStatus);
    } catch (error) {
      console.error('DataCollection - getMetadata.', error);
      notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId, datasetId } }, true);
    }
  };

  const onConfirmDelete = async () => {
    const action = 'DATASET_DELETE';
    actionsContext.testProcess(datasetId, action);
    try {
      notificationContext.add({ type: 'DELETE_DATASET_DATA_INIT' });
      await DatasetService.deleteData(datasetId, isReferenceDataset);
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        console.error('Dataset - onConfirmDelete.', error);
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = metadata;
        notificationContext.add(
          {
            type: 'DATASET_SERVICE_DELETE_DATA_BY_ID_ERROR',
            content: { dataflowId, datasetId, dataflowName, datasetName }
          },
          true
        );
      }
    }
  };

  const onConfirmValidate = async () => {
    const action = 'DATASET_VALIDATE';
    actionsContext.testProcess(datasetId, action);
    try {
      await DatasetService.validate(datasetId);
      notificationContext.add(
        {
          type: 'VALIDATE_DATA_INIT',
          content: {
            customContent: { origin: datasetName },
            dataflowId,
            dataflowName: metadata.dataflow.name,
            datasetId,
            datasetName: datasetSchemaName,
            type: 'REPORTING'
          }
        },
        true
      );
      changeProgressStepBar({ step: 1, currentStep: 2, isRunning: true });
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        console.error('Dataset - onConfirmValidate.', error);
        notificationContext.add(
          {
            type: 'VALIDATE_REPORTING_DATA_ERROR',
            content: {
              customContent: { origin: datasetName },
              dataflowId,
              dataflowName: metadata.dataflow.name,
              datasetId,
              datasetName: datasetSchemaName
            }
          },
          true
        );
      }
    }
  };

  const cleanImportOtherSystemsDialog = () => {
    setReplaceData(false);
    onSetVisible(setIsImportOtherSystemsDialogVisible, false);
  };

  const onImportDatasetError = async ({ xhr }) => {
    if (xhr.status === 400) {
      notificationContext.add(
        {
          type: 'IMPORT_REPORTING_BAD_REQUEST_ERROR',
          content: { dataflowId, datasetId, datasetName: datasetSchemaName }
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
            datasetName: datasetSchemaName
          }
        },
        true
      );
    }
  };

  const onImportOtherSystems = async () => {
    try {
      cleanImportOtherSystemsDialog();
      await IntegrationService.runIntegration(selectedCustomImportIntegration.id, datasetId, replaceData);
      setIsDataLoaded(true);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = metadata;
      notificationContext.add({
        type: 'DATASET_IMPORT_INIT',
        content: { dataflowId, datasetId, dataflowName, datasetName }
      });
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        console.error('Dataset - onImportOtherSystems.', error);
        notificationContext.add(
          {
            type: 'EXTERNAL_IMPORT_REPORTING_FROM_OTHER_SYSTEM_FAILED_EVENT',
            content: {
              dataflowName: metadata.dataflow.name,
              datasetName: datasetName
            }
          },
          true
        );
      }
    }
  };

  useEffect(() => {
    const isNotification = notificationContext.toShow.find(
      notification => notification.key === 'VALIDATION_FINISHED_EVENT'
    );
    if (isNotification && isNotification.content.datasetId?.toString() === datasetId.toString()) {
      onHighlightRefresh(true);
      changeProgressStepBar({ step: 1, currentStep: 2, isRunning: false, completed: false, withError: false });
    }

    const validationFinishedWithError = notificationContext.toShow.find(
      notification => notification.key === 'IMPORT_REPORTING_FAILED_EVENT'
    );
    if (
      validationFinishedWithError &&
      validationFinishedWithError.content.datasetId?.toString() === datasetId.toString()
    ) {
      onHighlightRefresh(true);
      changeProgressStepBar({ step: 1, currentStep: 2, isRunning: false, completed: false, withError: true });
    }

    const isImportDataCompleted = notificationContext.toShow.find(
      notification => notification.key === 'IMPORT_REPORTING_COMPLETED_EVENT'
    );

    const isRestoreSnapshotDataCompleted = notificationContext.toShow.some(
      notification => notification.key === 'RESTORE_DATASET_SNAPSHOT_COMPLETED_EVENT'
    );

    const isDeletedDataCompleted = notificationContext.toShow.find(
      notification => notification.key === 'DELETE_DATASET_DATA_COMPLETED_EVENT'
    );

    const isDeletedTableDataCompleted = notificationContext.toShow.find(
      notification => notification.key === 'DELETE_TABLE_COMPLETED_EVENT'
    );

    if (
      (isImportDataCompleted && isImportDataCompleted.content?.datasetId.toString() === datasetId.toString()) ||
      (isDeletedDataCompleted && isDeletedDataCompleted.content?.datasetId.toString() === datasetId.toString()) ||
      (isDeletedTableDataCompleted &&
        isDeletedTableDataCompleted.content?.datasetId.toString() === datasetId.toString())
    ) {
      onHighlightRefresh(true);
    }

    if (isImportDataCompleted || isRestoreSnapshotDataCompleted || isDeletedDataCompleted) {
      changeProgressStepBar({ step: 1, currentStep: 2, isRunning: true, completed: false, withError: false });
    }
  }, [notificationContext]);

  const onHighlightRefresh = value => setIsRefreshHighlighted(value);

  useCheckNotifications(
    [
      'CALL_FME_PROCESS_FAILED_EVENT',
      'DOWNLOAD_EXPORT_DATASET_FILE_ERROR',
      'DOWNLOAD_FME_FILE_ERROR',
      'EXPORT_DATA_BY_ID_ERROR',
      'EXPORT_DATASET_FILE_AUTOMATICALLY_DOWNLOAD',
      'EXPORT_DATASET_FILE_DOWNLOAD',
      'EXPORT_TABLE_DATA_FILE_AUTOMATICALLY_DOWNLOAD',
      'EXTERNAL_EXPORT_REPORTING_FAILED_EVENT',
      'DOWNLOAD_EXPORT_TABLE_DATA_FILE_ERROR'
    ],
    actionsContext.changeExportDatasetState,
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

  const onLoadTableData = hasData => {
    setDatasetHasData(hasData);
  };

  const onExportError = async exportNotification => {
    const {
      dataflow: { name: dataflowName },
      dataset: { name: datasetName }
    } = metadata;

    notificationContext.add(
      {
        type: exportNotification,
        content: {
          dataflowName: dataflowName,
          datasetName: datasetName
        }
      },
      true
    );
  };

  const onExportDataExternalIntegration = async integrationId => {
    const action = 'DATASET_EXPORT';
    actionsContext.testProcess(datasetId, action);
    notificationContext.add({ type: 'EXPORT_DATASET_DATA' });
    try {
      await DatasetService.exportDatasetDataExternal(datasetId, integrationId);
    } catch (error) {
      console.error('Dataset - onExportDataExternalIntegration.', error);
      notificationContext.add(
        {
          type: 'EXTERNAL_EXPORT_REPORTING_FAILED_EVENT',
          content: { dataflowId, datasetId, datasetName: datasetSchemaName }
        },
        true
      );
    }
  };

  const onExportDataInternalExtension = async fileType => {
    const action = 'DATASET_EXPORT';
    actionsContext.testProcess(datasetId, action);
    notificationContext.add({ type: 'EXPORT_DATASET_DATA' });
    try {
      if (bigDataRef.current) {
        await DatasetService.exportDatasetDataDL(datasetId, fileType);
      } else {
        await DatasetService.exportDatasetData(datasetId, fileType);
      }
    } catch (error) {
      console.error('Dataset - onExportDataInternalExtension.', error);
      onExportError('EXPORT_DATA_BY_ID_ERROR');
    }
  };

  const onLoadDataflow = async () => {
    try {
      const data = await DataflowService.get(dataflowId);
      setDataflowType(data.type);

      const dataset = data.datasets.find(dataset => dataset.datasetId.toString() === datasetId);

      if (!isNil(dataset)) {
        setIsDatasetReleased(dataset.isReleased);
        setIsDataset(true);
        setDataset(dataset);
        return;
      }

      const testDataset = data.testDatasets.find(dataset => dataset.datasetId.toString() === datasetId);

      if (!isNil(testDataset)) {
        setIsTestDataset(true);
        setDataset(testDataset);
        return;
      }

      const referenceDataset = data.referenceDatasets.find(dataset => dataset.datasetId.toString() === datasetId);

      if (!isNil(referenceDataset)) {
        if (!isReferenceDatasetReferenceDataflow) {
          setIsDatasetReleased(referenceDataset.isReleased);
        }
        setIsReferenceDataset(true);
        setIsDatasetUpdatable(referenceDataset.updatable);
        setDataset(referenceDataset);
        return;
      }
    } catch (error) {
      console.error('Dataset - onLoadDataflow.', error);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = metadata;
      notificationContext.add(
        {
          type: 'REPORTING_ERROR',
          content: { dataflowId, datasetId, dataflowName, datasetName }
        },
        true
      );
      if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
        navigate(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setIsLoading(false);
    }
  };

  useCheckNotifications(
    [
      'RELEASE_COMPLETED_EVENT',
      'RELEASE_PROVIDER_COMPLETED_EVENT',
      'RELEASE_FAILED_EVENT',
      'RELEASE_BLOCKED_EVENT',
      'RELEASE_BLOCKERS_FAILED_EVENT'
    ],
    onLoadDataflow
  );

  const getDataSchema = async () => {
    try {
      const datasetSchema = await DatasetService.getSchema(dataflowId, datasetId);
      filterManualEdit(datasetSchema.tables);
      setDatasetSchemaAllTables(datasetSchema.tables);
      setDatasetSchemaName(datasetSchema.datasetSchemaName);
      setLevelErrorTypes(datasetSchema.levelErrorTypes);
      setWebformData(datasetSchema.webform);
      if (isNil(datasetSchema.webform?.name)) {
        setSelectedView('tabularData');
      } else {
        setSelectedView('webform');
      }
      return datasetSchema;
    } catch (error) {
      throw new Error('SCHEMA_BY_ID_ERROR');
    }
  };

  const filterManualEdit = tablesArray => {
    let length = tablesArray.length;

    tablesArray?.forEach(table => {
      if (!table?.dataAreManuallyEditable) length -= 1;
    });

    if (length === 0) {
      setNoEditableCheck(true);
    } else {
      setNoEditableCheck(false);
    }
  };

  const getStatisticsById = async (datasetId, tableSchemaNames) => {
    try {
      return await DatasetService.getStatistics(datasetId, tableSchemaNames);
    } catch (error) {
      throw new Error('ERROR_STATISTICS_BY_ID_ERROR');
    }
  };

  const onLoadDatasetSchema = async () => {
    onHighlightRefresh(false);

    try {
      setIsLoading(true);
      const datasetSchema = await getDataSchema();
      const datasetStatistics = await getStatisticsById(
        datasetId,
        datasetSchema.tables.map(tableSchema => tableSchema.tableSchemaName)
      );
      setDatasetStatisticsInState({ ...datasetStatistics });
      setDatasetName(datasetStatistics.datasetSchemaName);
      const tableSchemaList = [];
      setTableSchema(
        datasetSchema.tables.map(tableSchema => {
          tableSchemaList.push({ name: tableSchema.tableSchemaName, id: tableSchema.tableSchemaId });
          return {
            dataAreManuallyEditable: tableSchema.dataAreManuallyEditable,
            description: tableSchema.description || tableSchema.tableSchemaDescription,
            id: tableSchema.tableSchemaId,
            name: tableSchema.tableSchemaName,
            notEmpty: tableSchema.notEmpty,
            hasInfoTooltip: true,
            hasErrors: {
              ...datasetStatistics.tables.filter(table => table.tableSchemaId === tableSchema.tableSchemaId)[0]
            }.hasErrors,
            fixedNumber: tableSchema.tableSchemaFixedNumber,
            numberOfFields: tableSchema.records ? tableSchema.records[0].fields?.length : 0,
            readOnly: tableSchema.tableSchemaReadOnly,
            toPrefill: tableSchema.tableSchemaToPrefill
          };
        })
      );
      setSchemaTables(tableSchemaList);
      setTableSchemaColumns(
        datasetSchema.tables.map(table => {
          return table.records[0].fields.map(field => {
            return {
              codelistItems: field['codelistItems'],
              description: field['description'],
              field: field['fieldId'],
              header: field['name'],
              pk: field['pk'],
              maxSize: field['maxSize'],
              pkHasMultipleValues: field['pkHasMultipleValues'],
              readOnly: field['readOnly'],
              recordId: field['recordId'],
              referencedField: field['referencedField'],
              required: field['required'],
              table: table['tableSchemaName'],
              type: field['type'],
              validExtensions: field['validExtensions']
            };
          });
        })
      );

      setDatasetHasErrors(datasetStatistics.datasetErrors);
    } catch (error) {
      console.error('Dataset - onLoadDatasetSchema.', error);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = metadata;
      setDatasetName(datasetName);
      const datasetError = {
        type: error.message,
        content: {
          datasetId,
          dataflowName,
          datasetName
        }
      };
      notificationContext.add(datasetError, true);
      if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
        navigate(getUrl(routes.DATAFLOW, { dataflowId }));
      }
    } finally {
      setIsLoading(false);
      setIsDataLoaded(true);
    }
  };

  const onHideSelectGroupedValidation = () =>
    setDataViewerOptions({
      ...dataViewerOptions,
      isGroupedValidationDeleted: true,
      isGroupedValidationSelected: false,
      selectedRuleMessage: '',
      selectedRuleLevelError: '',
      selectedRuleId: '',
      selectedShortCode: ''
    });

  const onSelectValidation = (
    tableSchemaId,
    selectedRuleId = '',
    selectedShortCode = '',
    selectedRuleMessage = '',
    selectedRuleLevelError = ''
  ) => {
    setDataViewerOptions({
      ...dataViewerOptions,
      isGroupedValidationDeleted: false,
      isGroupedValidationSelected: true,
      selectedRuleId,
      selectedRuleLevelError,
      selectedRuleMessage,
      selectedShortCode,
      selectedTableSchemaId: tableSchemaId,
      tableSchemaId
    });

    onSetVisible(setValidationsVisible, false);
  };

  const onSetVisible = (fnUseState, visible) => {
    fnUseState(visible);
  };

  const onTabChange = table =>
    setDataViewerOptions({
      ...dataViewerOptions,
      tableSchemaId: table.tableSchemaId
    });

  const onDownloadQCRules = async () => {
    setIsDownloadingQCRules(true);

    try {
      await ValidationService.generateQCRulesFile(datasetId);
      notificationContext.add({ type: 'DOWNLOAD_QC_RULES_START' });
    } catch (error) {
      console.error('DatasetDesigner - onDownloadQCRules.', error);
      if (error.response?.status === 400) {
        notificationContext.add({ type: 'DOWNLOAD_FILE_BAD_REQUEST_ERROR' }, true);
      } else {
        notificationContext.add({ type: 'GENERATE_QC_RULES_FILE_ERROR' }, true);
      }
      setIsDownloadingQCRules(false);
    }
  };

  const onRestoreData = restorationInProgress => {
    setIsTableDataRestorationInProgress(restorationInProgress);
  };

  const datasetInsideTitle = () => {
    if (dataset?.isReleasing) {
      return `${resourcesContext.messages['isReleasing']} `;
    } else if (!isEmpty(metadata?.dataset.datasetFeedbackStatus)) {
      return `${metadata?.dataset.datasetFeedbackStatus} `;
    } else if (isEmpty(metadata?.dataset.datasetFeedbackStatus) && isDatasetReleased) {
      return `${resourcesContext.messages['released'].toString()}`;
    } else {
      return '';
    }
  };

  const validationListFooter = (
    <Fragment>
      <Button
        className="p-button-secondary p-button-animated-blink"
        disabled={isDownloadingQCRules}
        icon={isDownloadingQCRules ? 'spinnerAnimate' : 'export'}
        label={resourcesContext.messages['downloadQCsButtonLabel']}
        onClick={() => onDownloadQCRules()}
        style={{ float: 'left' }}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={() => onSetVisible(setValidationListDialogVisible, false)}
      />
    </Fragment>
  );

  const layout = children => {
    return (
      <MainLayout bigData={metadata?.dataflow.bigData}>
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  const onUpload = async () => {
    const action = 'DATASET_IMPORT';
    actionsContext.testProcess(datasetId, action);
    setIsImportDatasetDialogVisible(false);
    setSelectedCustomImportIntegration({ id: null, name: null });
    const {
      dataflow: { name: dataflowName },
      dataset: { name: datasetName }
    } = metadata;

    notificationContext.add(
      {
        type: 'DATASET_DATA_LOADING_INIT',
        content: {
          customContent: {
            datasetLoadingMessage: resourcesContext.messages['datasetLoadingMessage'],
            title: TextUtils.ellipsis(datasetName, config.notifications.STRING_LENGTH_MAX),
            datasetLoading: resourcesContext.messages['datasetLoading']
          },
          dataflowName,
          datasetName
        }
      },
      true
    );
    changeProgressStepBar({ step: 0, currentStep: 1, isRunning: true });
  };

  const renderImportOtherSystemsFooter = (
    <Fragment>
      <Button
        className="p-button-animated-blink"
        icon="check"
        label={resourcesContext.messages['import']}
        onClick={onImportOtherSystems}
      />
      <Button
        className="p-button-secondary button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['cancel']}
        onClick={cleanImportOtherSystemsDialog}
      />
    </Fragment>
  );

  const renderSwitchView = () => {
    if (!isNil(webformData?.name)) {
      const viewModes = [
        { key: 'tabularData', label: resourcesContext.messages['tabularDataView'] },
        { key: 'webform', label: resourcesContext.messages['webform'] }
      ];

      return (
        <div className={styles.switchDivInput}>
          <div className={`${styles.switchDiv} datasetSchema-switchDesignToData-help-step`}>
            <TabularSwitch
              elements={viewModes}
              onChange={switchView => setSelectedView(switchView)}
              value={selectedView}
            />
          </div>
        </div>
      );
    }
  };

  const onDownloadValidations = async () => {
    setIsDownloadingValidations(true);

    try {
      await ValidationService.generateShowValidationsFile(datasetId);
      notificationContext.add({ type: 'DOWNLOAD_VALIDATIONS_START' });
    } catch (error) {
      console.error('Dataset - onDownloadValidations.', error);
      if (error.response?.status === 400) {
        notificationContext.add({ type: 'DOWNLOAD_FILE_BAD_REQUEST_ERROR' }, true);
      } else {
        notificationContext.add({ type: 'DOWNLOAD_VALIDATIONS_ERROR' }, true);
      }
      setIsDownloadingValidations(false);
    }
  };

  const onConfirmUpdateReferenceDataset = async () => {
    setIsUpdatableDialogVisible(false);
    try {
      await DatasetService.updateReferenceDatasetStatus(datasetId, !dataset.updatable);
      onLoadDataflow();
      onLoadDatasetSchema();
    } catch (error) {
      console.error('Dataset - onConfirmUpdateReferenceDataset.', error);
      notificationContext.add({ type: 'UNLOCK_DATASET_ERROR' }, true);
    }
  };

  const renderValidationsFooter = (
    <div className={styles.validationsFooter}>
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        disabled={isDownloadingValidations}
        icon={isDownloadingValidations ? 'spinnerAnimate' : 'export'}
        label={resourcesContext.messages['downloadValidationsButtonLabel']}
        onClick={onDownloadValidations}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={() => setValidationsVisible(false)}
      />
    </div>
  );

  const renderTableWebformView = () => {
    if (selectedView === 'webform') {
      return (
        <Webforms
          bigData={metadata?.dataflow.bigData}
          dataflowId={dataflowId}
          dataProviderId={metadata?.dataset.dataProviderId}
          datasetId={datasetId}
          isIcebergCreated={isIcebergCreated}
          isLoadingIceberg={isLoadingIceberg}
          isReleasing={dataset.isReleasing}
          isReporting
          options={webformOptions}
          state={{
            datasetSchema: { tables: datasetSchemaAllTables },
            schemaTables,
            datasetStatistics: datasetStatisticsInState
          }}
          webform={webformData}
        />
      );
    }

    return (
      <TabsSchema
        bigData={metadata?.dataflow.bigData}
        dataProviderId={metadata?.dataset.dataProviderId}
        datasetSchemaId={metadata?.dataset.datasetSchemaId}
        datasetType={metadata?.dataset.datasetType}
        hasWritePermissions={hasWritePermissions}
        isGroupedValidationDeleted={dataViewerOptions.isGroupedValidationDeleted}
        isGroupedValidationSelected={dataViewerOptions.isGroupedValidationSelected}
        isIcebergCreated={isIcebergCreated}
        isReferenceDataset={isReferenceDataset}
        isReportingWebform={isReportingWebform}
        isTableDataRestorationInProgress={isTableDataRestorationInProgress}
        levelErrorTypes={levelErrorTypes}
        onHideSelectGroupedValidation={onHideSelectGroupedValidation}
        onLoadTableData={onLoadTableData}
        onRestoreData={onRestoreData}
        onTabChange={tableSchemaId => onTabChange(tableSchemaId)}
        reporting={true}
        selectedRuleId={dataViewerOptions.selectedRuleId}
        selectedRuleLevelError={dataViewerOptions.selectedRuleLevelError}
        selectedRuleMessage={dataViewerOptions.selectedRuleMessage}
        selectedShortCode={dataViewerOptions.selectedShortCode}
        selectedTableSchemaId={dataViewerOptions.selectedTableSchemaId}
        tables={tableSchema}
        tableSchemaColumns={tableSchemaColumns}
        tableSchemaId={dataViewerOptions.tableSchemaId}
      />
    );
  };

  if (isLoading) {
    return layout(<Spinner />);
  }

  if (isLoadingIceberg)
    return layout(
      <div style={{ top: 0, margin: '1rem' }}>
        <Spinner style={{ top: 0, margin: '1rem' }} />
        <p style={{ position: 'absolute', left: '50%', transform: 'translateX(-50%)', margin: 0 }}>
          {resourcesContext.messages['tablesAreBeingConverted']}
        </p>
      </div>
    );

  return layout(
    <SnapshotContext.Provider
      value={{
        isSnapshotsBarVisible: isSnapshotsBarVisible,
        setIsSnapshotsBarVisible: setIsSnapshotsBarVisible,
        snapshotDispatch: snapshotDispatch,
        snapshotState: snapshotState
      }}>
      <Title
        icon={isReferenceDatasetReferenceDataflow ? 'howTo' : 'dataset'}
        iconSize={isReferenceDatasetReferenceDataflow ? '4rem' : '3.5rem'}
        insideTitle={`${datasetInsideTitle()}`}
        subtitle={
          metadata?.dataflow.bigData ? (
            <p
              dangerouslySetInnerHTML={{
                __html: TextUtils.parseText(resourcesContext.messages['bigDataDataflowNamed'], {
                  name: `${metadata?.dataflow.name} - ${
                    isTestDataset ? resourcesContext.messages['testDataset'] : datasetName
                  }`
                })
              }}></p>
          ) : (
            `${metadata?.dataflow.name} - ${isTestDataset ? resourcesContext.messages['testDataset'] : datasetName}`
          )
        }
        title={datasetSchemaName}
      />
      <div className={styles.ButtonsBar}>
        <Toolbar>
          <div className="p-toolbar-group-left datasetSchema-buttonsbar-dataset-data-help-step">
            {hasWritePermissions && (
              <Fragment>
                <Button
                  className={`p-button-rounded p-button-secondary datasetSchema-buttonsbar-dataset-data-help-step ${
                    !hasWritePermissions ? null : 'p-button-animated-blink'
                  }`}
                  disabled={
                    isIcebergCreated ||
                    !hasWritePermissions ||
                    isTableDataRestorationInProgress ||
                    actionsContext.importDatasetProcessing ||
                    actionsContext.exportDatasetProcessing ||
                    actionsContext.deleteDatasetProcessing ||
                    actionsContext.importTableProcessing ||
                    actionsContext.exportTableProcessing ||
                    actionsContext.deleteTableProcessing ||
                    actionsContext.validateDatasetProcessing
                  }
                  icon={actionsContext.importDatasetProcessing ? 'spinnerAnimate' : 'import'}
                  label={
                    actionsContext.importDatasetProcessing
                      ? resourcesContext.messages['importInProgress']
                      : resourcesContext.messages['importDataset']
                  }
                  onClick={event => importMenuRef.current.show(event)}
                />
                <Menu
                  className={styles.menuWrapper}
                  id="importDataSetMenu"
                  model={importButtonsList}
                  popup={true}
                  ref={importMenuRef}
                />
              </Fragment>
            )}
            <Button
              className="p-button-rounded p-button-secondary-transparent p-button-animated-blink datasetSchema-export-dataset-help-step"
              disabled={
                (hasWritePermissions && isIcebergCreated) ||
                isTableDataRestorationInProgress ||
                actionsContext.importDatasetProcessing ||
                actionsContext.exportDatasetProcessing ||
                actionsContext.deleteDatasetProcessing ||
                actionsContext.importTableProcessing ||
                actionsContext.exportTableProcessing ||
                actionsContext.deleteTableProcessing ||
                actionsContext.validateDatasetProcessing
              }
              icon={actionsContext.exportDatasetProcessing ? 'spinnerAnimate' : 'export'}
              id="buttonExportDataset"
              label={
                actionsContext.exportDatasetProcessing
                  ? resourcesContext.messages['exportInProgress']
                  : resourcesContext.messages['exportDataset']
              }
              onClick={event => exportMenuRef.current.show(event)}
            />
            <Menu
              className={styles.menuWrapper}
              id="exportDataSetMenu"
              model={exportButtonsList}
              popup={true}
              ref={exportMenuRef}
            />
            <DatasetDeleteDataDialog
              disabled={
                isIcebergCreated ||
                !hasWritePermissions ||
                isTableDataRestorationInProgress ||
                actionsContext.importDatasetProcessing ||
                actionsContext.exportDatasetProcessing ||
                actionsContext.deleteDatasetProcessing ||
                actionsContext.importTableProcessing ||
                actionsContext.exportTableProcessing ||
                actionsContext.deleteTableProcessing ||
                actionsContext.validateDatasetProcessing
              }
              icon={actionsContext.deleteDatasetProcessing ? 'spinnerAnimate' : 'trash'}
              label={
                actionsContext.deleteDatasetProcessing
                  ? resourcesContext.messages['deleteInProgress']
                  : resourcesContext.messages['deleteDatasetData']
              }
              onConfirmDelete={onConfirmDelete}
            />
          </div>
          <div className="p-toolbar-group-right">
            <DatasetValidateDialog
              disabled={
                isIcebergCreated ||
                !hasWritePermissions ||
                isTableDataRestorationInProgress ||
                actionsContext.importDatasetProcessing ||
                actionsContext.exportDatasetProcessing ||
                actionsContext.deleteDatasetProcessing ||
                actionsContext.importTableProcessing ||
                actionsContext.exportTableProcessing ||
                actionsContext.deleteTableProcessing ||
                actionsContext.validateDatasetProcessing
              }
              icon={actionsContext.validateDatasetProcessing ? 'spinnerAnimate' : 'validate'}
              label={
                actionsContext.validateDatasetProcessing
                  ? resourcesContext.messages['validationInProgress']
                  : resourcesContext.messages['validate']
              }
              onConfirmValidate={onConfirmValidate}
            />
            <Button
              className="p-button-rounded p-button-secondary-transparent dataset-showValidations-help-step p-button-animated-blink"
              icon="warning"
              iconClasses={datasetHasErrors ? 'warning' : ''}
              label={resourcesContext.messages['showValidations']}
              onClick={() => onSetVisible(setValidationsVisible, true)}
            />
            <Button
              className={
                'p-button-rounded p-button-secondary-transparent p-button-animated-blink datasetSchema-qcRules-help-step'
              }
              icon="horizontalSliders"
              label={resourcesContext.messages['qcRules']}
              onClick={() => onSetVisible(setValidationListDialogVisible, true)}
            />
            <DatasetDashboardDialog
              disabled={!datasetHasData}
              levelErrorTypes={levelErrorTypes}
              tableSchemas={schemaTables.map(table => table.name)}
            />
            <Button
              className={`p-button-rounded p-button-secondary-transparent datasetSchema-manageCopies-help-step ${
                !hasWritePermissions ? null : 'p-button-animated-blink'
              }`}
              disabled={!hasWritePermissions}
              icon="camera"
              label={resourcesContext.messages['snapshots']}
              onClick={() => setIsSnapshotsBarVisible(!isSnapshotsBarVisible)}
            />
            <Button
              className={`p-button-rounded p-button-${
                isRefreshHighlighted ? 'primary' : 'secondary-transparent'
              } p-button-animated-blink dataset-refresh-help-step`}
              icon="refresh"
              label={resourcesContext.messages['refresh']}
              onClick={onLoadDatasetSchema}
            />
            {metadata?.dataflow.bigData && (
              <Button
                className={styles.openWebformButton}
                disabled={!hasWritePermissions || isLoadingIceberg || noEditableCheck}
                helpClassName={!isIcebergCreated ? 'p-button-reverse' : 'p-button-copy'}
                icon={!isIcebergCreated ? 'lock' : 'unlock'}
                isLoading={isLoadingIceberg}
                key={isIcebergCreated}
                label={
                  !isIcebergCreated ? resourcesContext.messages['enableEdit'] : resourcesContext.messages['disableEdit']
                }
                onClick={() => convertHelper()}
              />
            )}
          </div>
        </Toolbar>
      </div>
      <div className={styles.progressSwitchWrapper}>{renderSwitchView()}</div>
      {renderTableWebformView()}
      {validationsVisible && (
        <Dialog
          className={styles.paginatorValidationViewer}
          footer={renderValidationsFooter}
          header={resourcesContext.messages['titleValidations']}
          onHide={() => onSetVisible(setValidationsVisible, false)}
          style={{ width: '90%' }}
          visible={validationsVisible}>
          <ShowValidationsList
            bigData={metadata?.dataflow.bigData}
            dataflowId={dataflowId}
            datasetId={datasetId}
            datasetName={datasetName}
            datasetSchemaId={metadata?.dataset.datasetSchemaId}
            hasWritePermissions={hasWritePermissions}
            isWebformView={selectedView === 'webform'}
            levelErrorTypes={levelErrorTypes}
            onSelectValidation={onSelectValidation}
            reporting={true}
            schemaTables={schemaTables}
            switchToTabularData={() => setSelectedView('tabularData')}
            tables={datasetSchemaAllTables}
            visible={validationsVisible}
          />
        </Dialog>
      )}
      {validationListDialogVisible && (
        <Dialog
          footer={validationListFooter}
          header={resourcesContext.messages['qcRules']}
          onHide={() => onSetVisible(setValidationListDialogVisible, false)}
          style={{ width: '90%' }}
          visible={validationListDialogVisible}>
          <QCList
            dataflowId={dataflowId}
            dataset={{ datasetId: datasetId, name: datasetSchemaName }}
            datasetSchemaAllTables={datasetSchemaAllTables}
            datasetSchemaId={metadata?.dataset.datasetSchemaId}
          />
        </Dialog>
      )}
      {isImportDatasetDialogVisible && (
        <CustomFileUpload
          accept={DatasetUtils.getValidExtensions({ validExtensions: importSelectedIntegrationExtension })}
          bigData={metadata?.dataflow.bigData}
          chooseLabel={resourcesContext.messages['selectFile']}
          className={styles.FileUpload}
          dataflowId={dataflowId}
          datasetId={datasetId}
          dialogHeader={selectedCustomImportIntegration.name}
          dialogOnHide={() => {
            setIsImportDatasetDialogVisible(false);
            setSelectedCustomImportIntegration({ id: null, name: null });
          }}
          dialogVisible={isImportDatasetDialogVisible}
          infoTooltip={`${
            resourcesContext.messages['supportedFileExtensionsTooltip']
          } ${DatasetUtils.getValidExtensions({
            isTooltip: true,
            validExtensions: importSelectedIntegrationExtension
          })}`}
          integrationId={selectedCustomImportIntegration.id ? selectedCustomImportIntegration.id : undefined}
          invalidExtensionMessage={resourcesContext.messages['invalidExtensionFile']}
          isDialog={true}
          name="file"
          onError={onImportDatasetError}
          onUpload={onUpload}
          providerId={metadata?.dataset.dataProviderId}
          replaceCheck={true}
          s3Check={true}
          s3TestCheck={true}
          timeoutBeforeClose={true}
          url={`${window.env.REACT_APP_BACKEND}${
            isNil(selectedCustomImportIntegration.id)
              ? getUrl(DatasetConfig.importFileDatasetUpd, {
                  datasetId: datasetId,
                  dataflowId: dataflowId,
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
              checked={replaceData}
              id="replaceCheckbox"
              inputId="replaceCheckbox"
              onChange={() => setReplaceData(!replaceData)}
              role="checkbox"
            />
            <label htmlFor="replaceCheckbox">
              <span className={styles.replaceDataLabel} onClick={() => setReplaceData(!replaceData)}>
                {resourcesContext.messages['replaceData']}
              </span>
            </label>
          </div>
        </Dialog>
      )}
      {isUpdatableDialogVisible && (
        <ConfirmDialog
          disabledConfirm={isDatasetUpdatable === dataset.updatable}
          header={resourcesContext.messages['referenceStateDialogHeader']}
          labelCancel={resourcesContext.messages['cancel']}
          labelConfirm={resourcesContext.messages['save']}
          onConfirm={onConfirmUpdateReferenceDataset}
          onHide={() => setIsUpdatableDialogVisible(false)}
          visible={isUpdatableDialogVisible}>
          <Checkbox
            checked={isDatasetUpdatable}
            id="referenceDatasetUpdatableCheckbox"
            inputId="referenceDatasetUpdatableCheckbox"
            onChange={() => setIsDatasetUpdatable(!isDatasetUpdatable)}
            role="checkbox"
          />
          <label className={styles.checkboxLabel} htmlFor="referenceDatasetUpdatableCheckbox">
            <span className={styles.pointer} onClick={() => setIsDatasetUpdatable(!isDatasetUpdatable)}>
              {resourcesContext.messages['unlockReferenceDatasetLabel']}
            </span>
          </label>
        </ConfirmDialog>
      )}
      <Snapshots
        isLoadingSnapshotListData={isLoadingSnapshotListData}
        isSnapshotDialogVisible={isSnapshotDialogVisible}
        setIsSnapshotDialogVisible={setIsSnapshotDialogVisible}
        snapshotListData={snapshotListData}
      />
    </SnapshotContext.Provider>
  );
};
