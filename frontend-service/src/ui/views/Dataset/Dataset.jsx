/* eslint-disable react-hooks/exhaustive-deps */
import { Fragment, useContext, useEffect, useRef, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './Dataset.module.scss';

import { config } from 'conf';
import { DatasetConfig } from 'conf/domain/model/Dataset';
import { DatasetSchemaReporterHelpConfig } from 'conf/help/datasetSchema/reporter';
import { routes } from 'ui/routes';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { Dashboard } from 'ui/views/_components/Dashboard';
import { Dialog } from 'ui/views/_components/Dialog';
import { TabularSwitch } from 'ui/views/_components/TabularSwitch';
import { MainLayout } from 'ui/views/_components/Layout';
import { Menu } from 'ui/views/_components/Menu';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';
import { Snapshots } from 'ui/views/_components/Snapshots';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsSchema } from 'ui/views/_components/TabsSchema';
import { ValidationsList } from 'ui/views/_components/ValidationsList';
import { Title } from 'ui/views/_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { ValidationViewer } from 'ui/views/_components/ValidationViewer';
import { Webforms } from 'ui/views/Webforms';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { IntegrationService } from 'core/services/Integration';
import { ValidationService } from 'core/services/Validation';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';
import { useReporterDataset } from 'ui/views/_components/Snapshots/_hooks/useReporterDataset';

import { getUrl, TextUtils } from 'core/infrastructure/CoreUtils';
import { CurrentPage, ExtensionUtils, MetadataUtils, QuerystringUtils } from 'ui/views/_functions/Utils';

export const Dataset = withRouter(({ match, history, isReferenceDataset }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [dashDialogVisible, setDashDialogVisible] = useState(false);
  const [dataProviderId, setDataProviderId] = useState(null);
  const [dataflowName, setDataflowName] = useState('');
  const [dataset, setDataset] = useState({});
  const [datasetFeedbackStatus, setDatasetFeedbackStatus] = useState('');
  const [datasetSchemaAllTables, setDatasetSchemaAllTables] = useState([]);
  const [datasetSchemaId, setDatasetSchemaId] = useState(null);
  const [datasetSchemaName, setDatasetSchemaName] = useState();
  const [datasetName, setDatasetName] = useState('');
  const [datasetHasErrors, setDatasetHasErrors] = useState(false);
  const [dataViewerOptions, setDataViewerOptions] = useState({
    isGroupedValidationDeleted: false,
    isGroupedValidationSelected: false,
    isValidationSelected: false,
    recordPositionId: -1,
    selectedRecordErrorId: -1,
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
  const [datasetStatisticsInState, setDatasetStatisticsInState] = useState(undefined);
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [importButtonsList, setImportButtonsList] = useState([]);
  const [importFromOtherSystemSelectedIntegrationId, setImportFromOtherSystemSelectedIntegrationId] = useState();
  const [importSelectedIntegrationExtension, setImportSelectedIntegrationExtension] = useState(null);
  const [importSelectedIntegrationId, setImportSelectedIntegrationId] = useState(null);
  const [isCustodianOrSteward, setIsCustodianOrSteward] = useState(false);
  const [isDataLoaded, setIsDataLoaded] = useState(false);
  const [isDatasetReleased, setIsDatasetReleased] = useState(false);
  const [isDatasetUpdatable, setIsDatasetUpdatable] = useState(false);
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
  const [metaData, setMetaData] = useState({});
  const [replaceData, setReplaceData] = useState(false);
  const [schemaTables, setSchemaTables] = useState([]);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();
  const [validateDialogVisible, setValidateDialogVisible] = useState(false);
  const [validationListDialogVisible, setValidationListDialogVisible] = useState(false);
  const [validationsVisible, setValidationsVisible] = useState(false);
  const [webformData, setWebformData] = useState(null);

  let exportMenuRef = useRef();
  let importMenuRef = useRef();

  const callSetMetaData = async () => {
    setMetaData(await getMetadata({ datasetId, dataflowId }));
  };

  useBreadCrumbs({
    currentPage: isReferenceDataset ? CurrentPage.REFERENCE_DATASET : CurrentPage.DATASET,
    dataflowId,
    history,
    metaData,
    referenceDataflowId: dataflowId
  });

  useEffect(() => {
    leftSideBarContext.removeModels();
  }, []);

  useEffect(() => {
    if (isCustodianOrSteward) {
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
  }, [isCustodianOrSteward, isReferenceDataset, isReferenceDatasetRegularDataflow]);

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
            (isCustodianOrSteward && isDatasetUpdatable)
        );
        setIsTestDataset(
          userContext.hasPermission(
            [config.permissions.roles.CUSTODIAN.key, config.permissions.roles.STEWARD.key],
            `${config.permissions.prefixes.TESTDATASET}${datasetId}`
          )
        );
        setIsCustodianOrSteward(
          userContext.hasContextAccessPermission(config.permissions.prefixes.REFERENCEDATASET, datasetId, [
            config.permissions.roles.CUSTODIAN.key,
            config.permissions.roles.STEWARD.key
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
    onLoadDatasetSchema();
  }, []);

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
    callSetMetaData();
    getDataflowName();
    getDatasetData();
  }, []);

  useEffect(() => {
    if (!isUndefined(isTestDataset)) {
      onLoadDataflow();
    }
  }, [isTestDataset]);

  useEffect(() => {
    if (datasetSchemaId) getFileExtensions();
  }, [datasetSchemaId, isImportDatasetDialogVisible]);

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

  const internalImportExtensionsList = config.importTypes.importDatasetTypes.map(type => {
    const extensionsTypes = !isNil(type.code) && type.code.split('+');
    return {
      command: () => {
        setIsImportDatasetDialogVisible(true);
        setImportSelectedIntegrationExtension(type.code);
      },
      icon: extensionsTypes[0],
      label: type.text
    };
  });

  const importFromFile = !isEmpty(externalOperationsList.import)
    ? [
        {
          label: resources.messages['customImports'],
          items: externalOperationsList.import.map(type => {
            return {
              command: () => {
                setIsImportDatasetDialogVisible(true);
                setImportSelectedIntegrationId(type.id);
                setImportSelectedIntegrationExtension(type.fileExtension);
              },
              icon: type.fileExtension,
              label: `${type.name.toUpperCase()} (.${type.fileExtension.toLowerCase()})`
            };
          })
        }
      ]
    : [];

  const importFromOtherSystems = !isEmpty(externalOperationsList.importOtherSystems)
    ? [
        {
          label: resources.messages['importPreviousData'],
          items: externalOperationsList.importOtherSystems.map(importOtherSystem => ({
            label: importOtherSystem.name,
            icon: 'upload',
            command: () => {
              setImportFromOtherSystemSelectedIntegrationId(importOtherSystem.id);
              setIsImportOtherSystemsDialogVisible(true);
            }
          }))
        }
      ]
    : [];

  const validImportExtensions = `.${importSelectedIntegrationExtension}`;

  const infoExtensionsTooltip = `${resources.messages['supportedFileExtensionsTooltip']} ${validImportExtensions}`;

  const internalExtensions = config.exportTypes.exportDatasetTypes.map(type => {
    const extensionsTypes = !isNil(type.code) && type.code.split('+');
    return {
      label: type.text,
      icon: extensionsTypes[0],
      command: () => onExportDataInternalExtension(type.code)
    };
  });

  const externalIntegrationsNames = [
    {
      label: resources.messages['customExports'],
      items: externalOperationsList.export.map(type => ({
        label: `${type.name.toUpperCase()} (.${type.fileExtension.toLowerCase()})`,
        icon: type.fileExtension,
        command: () => onExportDataExternalIntegration(type.id)
      }))
    }
  ];

  const getFileExtensions = async () => {
    try {
      const allExtensions = await IntegrationService.allExtensionsOperations(dataflowId, datasetSchemaId);
      setExternalOperationsList(ExtensionUtils.groupOperations('operation', allExtensions));
    } catch (error) {
      notificationContext.add({ type: 'LOADING_FILE_EXTENSIONS_ERROR' });
    }
  };

  const getDatasetData = async () => {
    try {
      const metadata = await MetadataUtils.getDatasetMetadata(datasetId);
      setDatasetSchemaId(metadata.datasetSchemaId);
      setDatasetFeedbackStatus(metadata.datasetFeedbackStatus);
      setDataProviderId(metadata.dataProviderId);
    } catch (error) {
      notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId, datasetId } });
    }
  };

  const getMetadata = async ids => {
    try {
      return await MetadataUtils.getMetadata(ids);
    } catch (error) {
      notificationContext.add({
        type: 'GET_METADATA_ERROR',
        content: {
          dataflowId,
          datasetId
        }
      });
    }
  };

  const getDataflowName = async () => {
    try {
      const { data } = await DataflowService.dataflowDetails(match.params.dataflowId);
      setDataflowName(data.name);
    } catch (error) {
      notificationContext.add({ type: 'DATAFLOW_DETAILS_ERROR', content: {} });
    }
  };

  const onChangeIsValidationSelected = options => {
    setDataViewerOptions({
      ...dataViewerOptions,
      isGroupedValidationSelected: options.isGroupedValidationSelected,
      isValidationSelected: options.isValidationSelected
    });
  };

  const onConfirmDelete = async () => {
    try {
      notificationContext.add({ type: 'DELETE_DATASET_DATA_INIT' });
      setDeleteDialogVisible(false);
      await DatasetService.deleteDataById(datasetId);
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
      } else {
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await getMetadata({ dataflowId, datasetId });
        notificationContext.add({
          type: 'DATASET_SERVICE_DELETE_DATA_BY_ID_ERROR',
          content: { dataflowId, datasetId, dataflowName, datasetName }
        });
      }
    }
  };

  const onConfirmValidate = async () => {
    try {
      setValidateDialogVisible(false);
      await DatasetService.validateDataById(datasetId);
      notificationContext.add({
        type: 'VALIDATE_DATA_INIT',
        content: { countryName: datasetName, dataflowId, dataflowName, datasetId, datasetName: datasetSchemaName }
      });
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({
          type: 'GENERIC_BLOCKED_ERROR'
        });
      } else {
        notificationContext.add({
          type: 'VALIDATE_DATA_BY_ID_ERROR',
          content: { countryName: datasetName, dataflowId, dataflowName, datasetId, datasetName: datasetSchemaName }
        });
      }
    }
  };

  const cleanImportOtherSystemsDialog = () => {
    setReplaceData(false);
    onSetVisible(setIsImportOtherSystemsDialogVisible, false);
  };

  const onImportDatasetError = async ({ xhr }) => {
    if (xhr.status === 400) {
      notificationContext.add({
        type: 'IMPORT_REPORTING_BAD_REQUEST_ERROR',
        content: { dataflowId, datasetId, datasetName: datasetSchemaName }
      });
    }
    if (xhr.status === 423) {
      notificationContext.add({
        type: 'GENERIC_BLOCKED_ERROR',
        content: {
          dataflowId,
          datasetId,
          datasetName: datasetSchemaName
        }
      });
    }
  };

  const onImportOtherSystems = async () => {
    try {
      cleanImportOtherSystemsDialog();
      const dataImported = await IntegrationService.runIntegration(
        importFromOtherSystemSelectedIntegrationId,
        datasetId,
        replaceData
      );
      if (dataImported.status >= 200 && dataImported.status <= 299) {
        setIsDataLoaded(true);
      }
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
          type: 'EXTERNAL_IMPORT_REPORTING_FROM_OTHER_SYSTEM_FAILED_EVENT',
          content: {
            dataflowName: dataflowName,
            datasetName: datasetName
          }
        });
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
    ['AUTOMATICALLY_DOWNLOAD_VALIDATIONS_FILE', 'DOWNLOAD_VALIDATIONS_FILE_ERROR'],
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
    } = await getMetadata({ dataflowId, datasetId });

    notificationContext.add({
      type: exportNotification,
      content: {
        dataflowName: dataflowName,
        datasetName: datasetName
      }
    });
  };

  const onExportDataExternalIntegration = async integrationId => {
    setIsLoadingFile(true);
    notificationContext.add({
      type: 'EXPORT_DATASET_DATA'
    });
    try {
      await DatasetService.exportDatasetDataExternal(datasetId, integrationId);
    } catch (error) {
      onExportError('EXTERNAL_EXPORT_REPORTING_FAILED_EVENT');
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

  const onLoadDataflow = async () => {
    try {
      const { data } = await DataflowService.reporting(match.params.dataflowId);
      let dataset = [];
      if (isTestDataset) {
        dataset = data.testDatasets.filter(dataset => dataset.datasetId.toString() === datasetId);
      } else if (isReferenceDataset) {
        dataset = data.referenceDatasets.filter(dataset => dataset.datasetId.toString() === datasetId);
        setIsDatasetUpdatable(dataset[0]?.updatable);
      } else {
        dataset = data.datasets.filter(dataset => dataset.datasetId.toString() === datasetId);
        if (!isEmpty(dataset)) {
          setIsDatasetReleased(dataset[0]?.isReleased);
        } else {
          dataset = data.referenceDatasets.filter(dataset => dataset.datasetId.toString() === datasetId);
          setIsReferenceDatasetRegularDataflow(true);
          setIsDatasetReleased(dataset[0]?.isReleased);
        }
      }

      setDataset(dataset[0]);
    } catch (error) {
      console.error(error);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'REPORTING_ERROR',
        content: { dataflowId, datasetId, dataflowName, datasetName }
      });
      if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
        history.push(getUrl(routes.DATAFLOWS));
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
      const datasetSchema = await DatasetService.schemaById(datasetId);
      setDatasetSchemaAllTables(datasetSchema.data.tables);
      setDatasetSchemaName(datasetSchema.data.datasetSchemaName);
      setLevelErrorTypes(datasetSchema.data.levelErrorTypes);
      setWebformData(datasetSchema.data.webform);
      setIsTableView(QuerystringUtils.getUrlParamValue('view') === 'tabularData' || isNil(datasetSchema.data.webform));
      return datasetSchema.data;
    } catch (error) {
      throw new Error('SCHEMA_BY_ID_ERROR');
    }
  };

  const getStatisticsById = async (datasetId, tableSchemaNames) => {
    try {
      const datasetStatistics = await DatasetService.errorStatisticsById(datasetId, tableSchemaNames);
      return datasetStatistics.data;
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
              table: table['tableSchemaName'],
              type: field['type'],
              validExtensions: field['validExtensions']
            };
          });
        })
      );

      setDatasetHasErrors(datasetStatistics.datasetErrors);
    } catch (error) {
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await getMetadata({ dataflowId, datasetId });
      setDatasetName(datasetName);
      const datasetError = {
        type: error.message,
        content: {
          datasetId,
          dataflowName,
          datasetName
        }
      };
      notificationContext.add(datasetError);
      if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
        history.push(getUrl(routes.DATAFLOW, { dataflowId }));
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
      isValidationSelected: false,
      recordPositionId: -1,
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
      recordPositionId: -1,
      selectedRecordErrorId: -1,
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

  const datasetInsideTitle = () => {
    if (dataset.isReleasing) {
      return `${resources.messages['isReleasing']} `;
    } else if (!isEmpty(datasetFeedbackStatus)) {
      return `${datasetFeedbackStatus} `;
    } else if (isEmpty(datasetFeedbackStatus) && isDatasetReleased) {
      return `${resources.messages['released'].toString()}`;
    } else {
      return '';
    }
  };

  const validationListFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink p-button-right-aligned"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => onSetVisible(setValidationListDialogVisible, false)}
    />
  );

  const layout = children => {
    return (
      <MainLayout history={history}>
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  const onUpload = async () => {
    setIsImportDatasetDialogVisible(false);
    setImportSelectedIntegrationId(null);
    const {
      dataflow: { name: dataflowName },
      dataset: { name: datasetName }
    } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
    notificationContext.add({
      type: 'DATASET_DATA_LOADING_INIT',
      content: {
        datasetLoadingMessage: resources.messages['datasetLoadingMessage'],
        title: TextUtils.ellipsis(datasetName, config.notifications.STRING_LENGTH_MAX),
        datasetLoading: resources.messages['datasetLoading'],
        dataflowName,
        datasetName
      }
    });
  };

  const renderDashboardFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink p-button-right-aligned"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => onSetVisible(setDashDialogVisible, false)}
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

  const renderSwitchView = () =>
    !isNil(webformData) && (
      <div className={styles.switchDivInput}>
        <div className={`${styles.switchDiv} datasetSchema-switchDesignToData-help-step`}>
          <TabularSwitch
            className={styles.tabularSwitch}
            elements={[resources.messages['tabularDataView'], resources.messages['webform']]}
            isValidationsTabularView={isValidationsTabularView}
            onChange={switchView => setIsTableView(switchView === resources.messages['webform'] ? false : true)}
            setIsValidationsTabularView={setIsValidationsTabularView}
            value={resources.messages['webform']}
          />
        </div>
      </div>
    );

  const referenceStateDialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => setIsUpdatableDialogVisible(false)}
    />
  );

  const switchToTabularData = () => {
    setIsTableView(true);
    setIsValidationsTabularView(true);
  };

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

  const onConfirmUpdateReferenceDataset = async () => {
    setIsUpdatableDialogVisible(false);
    try {
      await DatasetService.updateReferenceDatasetStatus(datasetId, !dataset.updatable);
      onLoadDataflow();
    } catch (error) {
      notificationContext.add({ type: 'UNLOCK_DATASET_ERROR' });
    }
  };

  const renderValidationsFooter = (
    <div className={styles.validationsFooter}>
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon={isDownloadingValidations ? 'spinnerAnimate' : 'export'}
        label={resources.messages['downloadValidationsButtonLabel']}
        onClick={onDownloadValidations}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon={'cancel'}
        label={resources.messages['close']}
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
        subtitle={`${dataflowName} - ${datasetName}`}
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
                  label={resources.messages['importDataset']}
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
              className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink datasetSchema-export-dataset-help-step`}
              icon={isLoadingFile ? 'spinnerAnimate' : 'export'}
              id="buttonExportDataset"
              label={resources.messages['exportDataset']}
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
              label={resources.messages['deleteDatasetData']}
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
              label={resources.messages['validate']}
              onClick={() => onSetVisible(setValidateDialogVisible, true)}
            />
            <Button
              className="p-button-rounded p-button-secondary-transparent dataset-showValidations-help-step p-button-animated-blink"
              icon="warning"
              iconClasses={datasetHasErrors ? 'warning' : ''}
              label={resources.messages['showValidations']}
              onClick={() => onSetVisible(setValidationsVisible, true)}
            />
            <Button
              className={
                'p-button-rounded p-button-secondary-transparent p-button-animated-blink datasetSchema-qcRules-help-step'
              }
              icon="horizontalSliders"
              label={resources.messages['qcRules']}
              onClick={() => onSetVisible(setValidationListDialogVisible, true)}
            />
            <Button
              className={`p-button-rounded p-button-secondary-transparent dataset-dashboards-help-step ${
                !datasetHasData ? null : 'p-button-animated-blink'
              }`}
              disabled={!datasetHasData}
              icon="dashboard"
              label={resources.messages['dashboards']}
              onClick={() => onSetVisible(setDashDialogVisible, true)}
            />
            <Button
              className={`p-button-rounded p-button-secondary-transparent datasetSchema-manageCopies-help-step ${
                !hasWritePermissions ? null : 'p-button-animated-blink'
              }`}
              disabled={!hasWritePermissions}
              icon="camera"
              label={resources.messages['snapshots']}
              onClick={() => setIsSnapshotsBarVisible(!isSnapshotsBarVisible)}
            />
            <Button
              className={`p-button-rounded p-button-${
                isRefreshHighlighted ? 'primary' : 'secondary-transparent'
              } p-button-animated-blink dataset-refresh-help-step`}
              icon="refresh"
              label={resources.messages['refresh']}
              onClick={() => onLoadDatasetSchema()}
            />
          </div>
        </Toolbar>
      </div>
      {renderSwitchView()}
      {dashDialogVisible && (
        <Dialog
          footer={renderDashboardFooter}
          header={resources.messages['titleDashboard']}
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
          dataProviderId={dataProviderId}
          hasWritePermissions={hasWritePermissions}
          isGroupedValidationDeleted={dataViewerOptions.isGroupedValidationDeleted}
          isGroupedValidationSelected={dataViewerOptions.isGroupedValidationSelected}
          isReferenceDataset={isReferenceDataset || isReferenceDatasetRegularDataflow}
          isReportingWebform={isReportingWebform}
          isValidationSelected={dataViewerOptions.isValidationSelected}
          levelErrorTypes={levelErrorTypes}
          onChangeIsValidationSelected={onChangeIsValidationSelected}
          onHideSelectGroupedValidation={onHideSelectGroupedValidation}
          onLoadTableData={onLoadTableData}
          onTabChange={tableSchemaId => onTabChange(tableSchemaId)}
          recordPositionId={dataViewerOptions.recordPositionId}
          reporting={true}
          selectedRecordErrorId={dataViewerOptions.selectedRecordErrorId}
          selectedRuleId={dataViewerOptions.selectedRuleId}
          selectedRuleLevelError={dataViewerOptions.selectedRuleLevelError}
          selectedRuleMessage={dataViewerOptions.selectedRuleMessage}
          selectedTableSchemaId={dataViewerOptions.selectedTableSchemaId}
          tableSchemaColumns={tableSchemaColumns}
          tableSchemaId={dataViewerOptions.tableSchemaId}
          tables={tableSchema}
        />
      ) : (
        <Webforms
          dataProviderId={dataProviderId}
          dataflowId={dataflowId}
          datasetId={datasetId}
          isReleasing={dataset.isReleasing}
          isReporting
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
          header={resources.messages['titleValidations']}
          onHide={() => onSetVisible(setValidationsVisible, false)}
          style={{ width: '90%' }}
          visible={validationsVisible}>
          <ValidationViewer
            datasetId={datasetId}
            datasetName={datasetName}
            datasetSchemaId={datasetSchemaId}
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
          header={resources.messages['qcRules']}
          onHide={() => onSetVisible(setValidationListDialogVisible, false)}
          style={{ width: '90%' }}
          visible={validationListDialogVisible}>
          <ValidationsList
            dataset={{ datasetId: datasetId, name: datasetSchemaName }}
            datasetSchemaAllTables={datasetSchemaAllTables}
            datasetSchemaId={datasetSchemaId}
            reporting={true}
          />
        </Dialog>
      )}

      {isImportDatasetDialogVisible && (
        <CustomFileUpload
          accept={validImportExtensions}
          chooseLabel={resources.messages['selectFile']}
          className={styles.FileUpload}
          dialogClassName={styles.Dialog}
          dialogHeader={`${resources.messages['uploadDataset']}${datasetName}`}
          dialogOnHide={() => {
            setIsImportDatasetDialogVisible(false);
            setImportSelectedIntegrationId(null);
          }}
          dialogVisible={isImportDatasetDialogVisible}
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
                  delimiter: encodeURIComponent(config.IMPORT_FILE_DELIMITER)
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
              checked={replaceData}
              id="replaceCheckbox"
              inputId="replaceCheckbox"
              onChange={() => setReplaceData(!replaceData)}
              role="checkbox"
            />
            <label htmlFor="replaceCheckbox">
              <span className={styles.replaceDataLabel} onClick={() => setReplaceData(!replaceData)}>
                {resources.messages['replaceData']}
              </span>
            </label>
          </div>
        </Dialog>
      )}

      {deleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resources.messages['deleteDatasetHeader']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={onConfirmDelete}
          onHide={() => onSetVisible(setDeleteDialogVisible, false)}
          visible={deleteDialogVisible}>
          {resources.messages['deleteDatasetConfirm']}
        </ConfirmDialog>
      )}

      {validateDialogVisible && (
        <ConfirmDialog
          header={resources.messages['validateDataset']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={onConfirmValidate}
          onHide={() => onSetVisible(setValidateDialogVisible, false)}
          visible={validateDialogVisible}>
          {resources.messages['validateDatasetConfirm']}
        </ConfirmDialog>
      )}

      {isUpdatableDialogVisible && (
        <ConfirmDialog
          disabledConfirm={isDatasetUpdatable === dataset.updatable}
          header={resources.messages['referenceStateDialogHeader']}
          labelCancel={resources.messages['cancel']}
          labelConfirm={resources.messages['save']}
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
            <a onClick={() => setIsDatasetUpdatable(!isDatasetUpdatable)}>
              {resources.messages['unlockReferenceDatasetLabel']}
            </a>
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
});
