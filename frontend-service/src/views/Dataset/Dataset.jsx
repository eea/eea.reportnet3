/* eslint-disable react-hooks/exhaustive-deps */
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
import { Dashboard } from 'views/_components/Dashboard';
import { Dialog } from 'views/_components/Dialog';
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
import { Webforms } from 'views/Webforms';

import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';
import { IntegrationService } from 'services/IntegrationService';
import { ValidationService } from 'services/ValidationService';
import { WebformService } from 'services/WebformService';

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

export const Dataset = ({ isReferenceDataset }) => {
  const navigate = useNavigate();
  const { dataflowId, datasetId } = useParams();

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [dashDialogVisible, setDashDialogVisible] = useState(false);
  const [dataset, setDataset] = useState({});
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
    selectedTableSchemaId: null,
    tableSchemaId: QuerystringUtils.getUrlParamValue('tab') !== '' ? QuerystringUtils.getUrlParamValue('tab') : ''
  });
  const [datasetHasData, setDatasetHasData] = useState(false);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
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
  const [selectedCustomImportIntegration, setSelectedCustomImportIntegration] = useState({
    id: null,
    name: ''
  });
  const [importSelectedIntegrationExtension, setImportSelectedIntegrationExtension] = useState(null);
  const [hasCustodianPermissions, setHasCustodianPermissions] = useState(false);
  const [isDataLoaded, setIsDataLoaded] = useState(false);
  const [isDatasetReleased, setIsDatasetReleased] = useState(false);
  const [isDatasetUpdatable, setIsDatasetUpdatable] = useState(false);
  const [isDownloadingQCRules, setIsDownloadingQCRules] = useState(false);
  const [isDownloadingValidations, setIsDownloadingValidations] = useState(false);
  const [isImportDatasetDialogVisible, setIsImportDatasetDialogVisible] = useState(false);
  const [isImportOtherSystemsDialogVisible, setIsImportOtherSystemsDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingFile, setIsLoadingFile] = useState(false);
  const [isReferenceDatasetRegularDataflow, setIsReferenceDatasetRegularDataflow] = useState(false);
  const [isRefreshHighlighted, setIsRefreshHighlighted] = useState(false);
  const [isReportingWebform, setIsReportingWebform] = useState(false);
  const [isTableView, setIsTableView] = useState(true);
  const [isTestDataset, setIsTestDataset] = useState(undefined);
  const [isUpdatableDialogVisible, setIsUpdatableDialogVisible] = useState(false);
  const [isValidationsTabularView, setIsValidationsTabularView] = useState(false);
  const [levelErrorTypes, setLevelErrorTypes] = useState([]);
  const [metadata, setMetadata] = useState(undefined);
  const [replaceData, setReplaceData] = useState(false);
  const [schemaTables, setSchemaTables] = useState([]);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();
  const [validateDialogVisible, setValidateDialogVisible] = useState(false);
  const [validationListDialogVisible, setValidationListDialogVisible] = useState(false);
  const [validationsVisible, setValidationsVisible] = useState(false);
  const [webformData, setWebformData] = useState(null);
  const [webformOptions, setWebformOptions] = useState([]);

  let exportMenuRef = useRef();
  let importMenuRef = useRef();

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
    if (isEmpty(webformOptions)) getWebformList();
  }, []);

  useEffect(() => {
    if (!isUndefined(metadata)) {
      onLoadDatasetSchema();
    }
  }, [metadata]);

  useEffect(() => {
    if (hasCustodianPermissions) {
      leftSideBarContext.addModels([
        {
          className: 'dataflow-showPublicInfo-help-step',
          icon: 'lock',
          isVisible: isReferenceDatasetRegularDataflow || isReferenceDataset,
          label: 'referenceUpdateStatusLeftSideBarButton',
          onClick: () => setIsUpdatableDialogVisible(true),
          title: 'referenceUpdateStatusLeftSideBarButton'
        }
      ]);
    }
  }, [isReferenceDataset, isReferenceDatasetRegularDataflow, hasCustodianPermissions]);

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
    if (!isNil(dataset) && dataset.isReleasing) {
      setHasWritePermissions(!dataset.isReleasing);
    } else {
      if (!isUndefined(userContext.contextRoles)) {
        setHasWritePermissions(
          userContext.hasPermission(
            [config.permissions.roles.LEAD_REPORTER.key, config.permissions.roles.REPORTER_WRITE.key],
            `${config.permissions.prefixes.DATASET}${datasetId}`
          ) ||
            userContext.hasPermission(
              [config.permissions.roles.CUSTODIAN.key, config.permissions.roles.STEWARD.key],
              `${config.permissions.prefixes.TESTDATASET}${datasetId}`
            ) ||
            (hasCustodianPermissions && isDatasetUpdatable)
        );
        setIsTestDataset(
          userContext.hasPermission(
            [config.permissions.roles.CUSTODIAN.key, config.permissions.roles.STEWARD.key],
            `${config.permissions.prefixes.TESTDATASET}${datasetId}`
          )
        );
        setHasCustodianPermissions(
          userContext.hasContextAccessPermission(config.permissions.prefixes.REFERENCEDATASET, datasetId, [
            config.permissions.roles.CUSTODIAN.key,
            config.permissions.roles.STEWARD.key,
            config.permissions.roles.STEWARD_SUPPORT.key
          ])
        );
      }
    }
  }, [userContext, dataset]);

  useEffect(() => {
    if (!isNil(webformData)) {
      setIsReportingWebform(webformData === 'MMR-ART13');
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
      setIsLoadingFile(false);
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
    if (!isUndefined(isTestDataset)) {
      onLoadDataflow();
    }
  }, [isTestDataset]);

  useEffect(() => {
    if (metadata?.dataset.datasetSchemaId) getFileExtensions();
  }, [metadata?.dataset.datasetSchemaId, isImportDatasetDialogVisible]);

  useEffect(() => {
    getExportIntegrationsNames(externalOperationsList.export);
  }, [externalOperationsList]);

  useEffect(() => {
    if (window.location.search !== '' && !isNil(dataViewerOptions.tableSchemaId)) changeUrl();
  }, [dataViewerOptions.tableSchemaId, isTableView]);

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
      }${!isNil(webformData) ? `&view=${isTableView ? 'tabularData' : 'webform'}` : ''}`
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

  const internalExtensions = config.exportTypes.exportDatasetTypes.map(type => {
    const extensionsTypes = !isNil(type.code) && type.code.split('+');
    return {
      label: resourcesContext.messages[type.key],
      icon: extensionsTypes[0],
      command: () => onExportDataInternalExtension(type.code)
    };
  });

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
    if (isReferenceDataset) {
      return CurrentPage.REFERENCE_DATASET;
    } else if (isReferenceDatasetRegularDataflow) {
      return CurrentPage.DATAFLOW_REFERENCE_DATASET;
    } else if (metadata?.dataset.dataProviderId === 0) {
      return CurrentPage.TEST_DATASETS;
    } else {
      return CurrentPage.DATASET;
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
    } catch (error) {
      console.error('DataCollection - getMetadata.', error);
      notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId, datasetId } }, true);
    }
  };

  const onConfirmDelete = async () => {
    try {
      notificationContext.add({ type: 'DELETE_DATASET_DATA_INIT' });
      setDeleteDialogVisible(false);
      await DatasetService.deleteData(datasetId);
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
    try {
      setValidateDialogVisible(false);
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
    }
  }, [notificationContext]);

  const onHighlightRefresh = value => setIsRefreshHighlighted(value);

  useCheckNotifications(
    [
      'DOWNLOAD_EXPORT_DATASET_FILE_ERROR',
      'DOWNLOAD_FME_FILE_ERROR',
      'EXPORT_DATA_BY_ID_ERROR',
      'EXPORT_DATASET_FILE_AUTOMATICALLY_DOWNLOAD',
      'EXPORT_DATASET_FILE_DOWNLOAD',
      'EXTERNAL_EXPORT_REPORTING_FAILED_EVENT'
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
    setIsLoadingFile(true);
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
    setIsLoadingFile(true);
    notificationContext.add({ type: 'EXPORT_DATASET_DATA' });
    try {
      await DatasetService.exportDatasetData(datasetId, fileType);
    } catch (error) {
      console.error('Dataset - onExportDataInternalExtension.', error);
      onExportError('EXPORT_DATA_BY_ID_ERROR');
    }
  };

  const onLoadDataflow = async () => {
    try {
      const data = await DataflowService.get(dataflowId);
      setDataflowType(data.type);
      let dataset = [];
      if (isTestDataset) {
        dataset = data.testDatasets.find(dataset => dataset.datasetId.toString() === datasetId);
      } else if (isReferenceDataset) {
        dataset = data.referenceDatasets.find(dataset => dataset.datasetId.toString() === datasetId);
        setIsDatasetUpdatable(dataset.updatable);
      } else {
        dataset = data.datasets.find(dataset => dataset.datasetId.toString() === datasetId);
        if (!isEmpty(dataset)) {
          setIsDatasetReleased(dataset.isReleased);
        } else {
          dataset = data.referenceDatasets.find(dataset => dataset.datasetId.toString() === datasetId);
          setIsReferenceDatasetRegularDataflow(true);
          setIsDatasetReleased(dataset.isReleased);
          setIsDatasetUpdatable(dataset.updatable);
        }
      }

      setDataset(dataset);
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
      setDatasetSchemaAllTables(datasetSchema.tables);
      setDatasetSchemaName(datasetSchema.datasetSchemaName);
      setLevelErrorTypes(datasetSchema.levelErrorTypes);
      setWebformData(datasetSchema.webform);
      setIsTableView(QuerystringUtils.getUrlParamValue('view') === 'tabularData' || isNil(datasetSchema.webform));
      return datasetSchema;
    } catch (error) {
      throw new Error('SCHEMA_BY_ID_ERROR');
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
            id: tableSchema['tableSchemaId'],
            name: tableSchema['tableSchemaName'],
            hasErrors: {
              ...datasetStatistics.tables.filter(table => table['tableSchemaId'] === tableSchema['tableSchemaId'])[0]
            }.hasErrors,
            fixedNumber: tableSchema['tableSchemaFixedNumber'],
            readOnly: tableSchema['tableSchemaReadOnly']
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
      selectedRuleId: ''
    });

  const onSelectValidation = (
    tableSchemaId,
    selectedRuleId = '',
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
      <MainLayout>
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  const onUpload = async () => {
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
  };

  const renderDashboardFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink p-button-right-aligned"
      icon="cancel"
      label={resourcesContext.messages['close']}
      onClick={() => onSetVisible(setDashDialogVisible, false)}
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

  const renderSwitchView = () =>
    !isNil(webformData) && (
      <div className={styles.switchDivInput}>
        <div className={`${styles.switchDiv} datasetSchema-switchDesignToData-help-step`}>
          <TabularSwitch
            className={styles.tabularSwitch}
            elements={[resourcesContext.messages['tabularDataView'], resourcesContext.messages['webform']]}
            isValidationsTabularView={isValidationsTabularView}
            onChange={switchView => setIsTableView(switchView === resourcesContext.messages['webform'] ? false : true)}
            setIsValidationsTabularView={setIsValidationsTabularView}
            value={resourcesContext.messages['webform']}
          />
        </div>
      </div>
    );

  const switchToTabularData = () => {
    setIsTableView(true);
    setIsValidationsTabularView(true);
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

  if (isLoading) return layout(<Spinner />);

  return layout(
    <SnapshotContext.Provider
      value={{
        isSnapshotsBarVisible: isSnapshotsBarVisible,
        setIsSnapshotsBarVisible: setIsSnapshotsBarVisible,
        snapshotDispatch: snapshotDispatch,
        snapshotState: snapshotState
      }}>
      <Title
        icon={isReferenceDataset ? 'howTo' : 'dataset'}
        iconSize={isReferenceDataset ? '4rem' : '3.5rem'}
        insideTitle={`${datasetInsideTitle()}`}
        subtitle={`${metadata?.dataflow.name} - ${
          isTestDataset ? resourcesContext.messages['testDataset'] : datasetName
        }`}
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
                  disabled={!hasWritePermissions}
                  icon="import"
                  label={resourcesContext.messages['importDataset']}
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
              icon={isLoadingFile ? 'spinnerAnimate' : 'export'}
              id="buttonExportDataset"
              label={resourcesContext.messages['exportDataset']}
              onClick={event => exportMenuRef.current.show(event)}
            />
            <Menu
              className={styles.menuWrapper}
              id="exportDataSetMenu"
              model={exportButtonsList}
              popup={true}
              ref={exportMenuRef}
            />
            <Button
              className={`p-button-rounded p-button-secondary-transparent ${
                !hasWritePermissions ? null : 'p-button-animated-blink dataset-deleteDataset-help-step'
              }`}
              disabled={!hasWritePermissions}
              icon="trash"
              label={resourcesContext.messages['deleteDatasetData']}
              onClick={() => onSetVisible(setDeleteDialogVisible, true)}
            />
          </div>
          <div className="p-toolbar-group-right">
            <Button
              className={`p-button-rounded p-button-secondary-transparent dataset-validate-help-step ${
                hasWritePermissions && 'p-button-animated-blink'
              }`}
              disabled={!hasWritePermissions}
              icon="validate"
              label={resourcesContext.messages['validate']}
              onClick={() => onSetVisible(setValidateDialogVisible, true)}
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
            <Button
              className={`p-button-rounded p-button-secondary-transparent dataset-dashboards-help-step ${
                !datasetHasData ? null : 'p-button-animated-blink'
              }`}
              disabled={!datasetHasData}
              icon="dashboard"
              label={resourcesContext.messages['dashboards']}
              onClick={() => onSetVisible(setDashDialogVisible, true)}
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
              onClick={() => onLoadDatasetSchema()}
            />
          </div>
        </Toolbar>
      </div>
      {renderSwitchView()}
      {dashDialogVisible && (
        <Dialog
          footer={renderDashboardFooter}
          header={resourcesContext.messages['titleDashboard']}
          onHide={() => onSetVisible(setDashDialogVisible, false)}
          style={{ width: '70vw' }}
          visible={dashDialogVisible}>
          <Dashboard
            levelErrorTypes={levelErrorTypes}
            refresh={dashDialogVisible}
            tableSchemaNames={schemaTables.map(table => table.name)}
          />
        </Dialog>
      )}
      {isTableView ? (
        <TabsSchema
          dataProviderId={metadata?.dataset.dataProviderId}
          datasetSchemaId={metadata?.dataset.datasetSchemaId}
          hasWritePermissions={hasWritePermissions}
          isGroupedValidationDeleted={dataViewerOptions.isGroupedValidationDeleted}
          isGroupedValidationSelected={dataViewerOptions.isGroupedValidationSelected}
          isReferenceDataset={isReferenceDataset || isReferenceDatasetRegularDataflow}
          isReportingWebform={isReportingWebform}
          levelErrorTypes={levelErrorTypes}
          onHideSelectGroupedValidation={onHideSelectGroupedValidation}
          onLoadTableData={onLoadTableData}
          onTabChange={tableSchemaId => onTabChange(tableSchemaId)}
          reporting={true}
          selectedRuleId={dataViewerOptions.selectedRuleId}
          selectedRuleLevelError={dataViewerOptions.selectedRuleLevelError}
          selectedRuleMessage={dataViewerOptions.selectedRuleMessage}
          selectedTableSchemaId={dataViewerOptions.selectedTableSchemaId}
          tables={tableSchema}
          tableSchemaColumns={tableSchemaColumns}
          tableSchemaId={dataViewerOptions.tableSchemaId}
        />
      ) : (
        <Webforms
          dataflowId={dataflowId}
          dataProviderId={metadata?.dataset.dataProviderId}
          datasetId={datasetId}
          isReleasing={dataset.isReleasing}
          isReporting
          options={webformOptions}
          state={{
            datasetSchema: { tables: datasetSchemaAllTables },
            schemaTables,
            datasetStatistics: datasetStatisticsInState
          }}
          webformType={webformData}
        />
      )}

      {validationsVisible && (
        <Dialog
          className={styles.paginatorValidationViewer}
          footer={renderValidationsFooter}
          header={resourcesContext.messages['titleValidations']}
          onHide={() => onSetVisible(setValidationsVisible, false)}
          style={{ width: '90%' }}
          visible={validationsVisible}>
          <ShowValidationsList
            dataflowId={dataflowId}
            datasetId={datasetId}
            datasetName={datasetName}
            datasetSchemaId={metadata?.dataset.datasetSchemaId}
            hasWritePermissions={hasWritePermissions}
            isWebformView={!isTableView}
            levelErrorTypes={levelErrorTypes}
            onSelectValidation={onSelectValidation}
            reporting={true}
            schemaTables={schemaTables}
            switchToTabularData={switchToTabularData}
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
            reporting={true}
          />
        </Dialog>
      )}

      {isImportDatasetDialogVisible && (
        <CustomFileUpload
          accept={DatasetUtils.getValidExtensions({ validExtensions: importSelectedIntegrationExtension })}
          chooseLabel={resourcesContext.messages['selectFile']}
          className={styles.FileUpload}
          dialogClassName={styles.Dialog}
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

      {deleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resourcesContext.messages['deleteDatasetHeader']}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onConfirmDelete}
          onHide={() => onSetVisible(setDeleteDialogVisible, false)}
          visible={deleteDialogVisible}>
          {resourcesContext.messages['deleteDatasetConfirm']}
        </ConfirmDialog>
      )}

      {validateDialogVisible && (
        <ConfirmDialog
          header={resourcesContext.messages['validateDataset']}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onConfirmValidate}
          onHide={() => onSetVisible(setValidateDialogVisible, false)}
          visible={validateDialogVisible}>
          {resourcesContext.messages['validateDatasetConfirm']}
        </ConfirmDialog>
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
