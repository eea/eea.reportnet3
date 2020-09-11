/* eslint-disable react-hooks/exhaustive-deps */
import React, { Fragment, useContext, useEffect, useRef, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';
import uniq from 'lodash/uniq';

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
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { MainLayout } from 'ui/views/_components/Layout';
import { Menu } from 'primereact/menu';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';
import { Snapshots } from 'ui/views/_components/Snapshots';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsSchema } from 'ui/views/_components/TabsSchema';
import { TabsValidations } from 'ui/views/_components/TabsValidations';
import { Title } from 'ui/views/_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { ValidationViewer } from 'ui/views/_components/ValidationViewer';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { IntegrationService } from 'core/services/Integration';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';
import { useReporterDataset } from 'ui/views/_components/Snapshots/_hooks/useReporterDataset';

import { getUrl, TextUtils } from 'core/infrastructure/CoreUtils';
import { CurrentPage, ExtensionUtils, MetadataUtils } from 'ui/views/_functions/Utils';

export const Dataset = withRouter(({ match, history }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [dashDialogVisible, setDashDialogVisible] = useState(false);
  const [dataflowName, setDataflowName] = useState('');
  const [datasetSchemaAllTables, setDatasetSchemaAllTables] = useState([]);
  const [datasetSchemaId, setDatasetSchemaId] = useState(null);
  const [datasetSchemaName, setDatasetSchemaName] = useState();
  // const [datasetSchemas, setDatasetSchemas] = useState([]);
  const [datasetName, setDatasetName] = useState('');
  const [datasetHasErrors, setDatasetHasErrors] = useState(false);
  const [dataViewerOptions, setDataViewerOptions] = useState({
    recordPositionId: -1,
    selectedRecordErrorId: -1,
    activeIndex: null
  });
  const [datasetHasData, setDatasetHasData] = useState(false);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [exportButtonsList, setExportButtonsList] = useState([]);
  const [exportDatasetData, setExportDatasetData] = useState(undefined);
  const [exportDatasetDataName, setExportDatasetDataName] = useState('');
  const [externalOperationsList, setExternalOperationsList] = useState({
    export: [],
    import: [],
    importOtherSystems: []
  });
  const [externalExportExtensions, setExternalExportExtensions] = useState([]);
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [importButtonsList, setImportButtonsList] = useState([]);
  const [importFromOtherSystemSelectedIntegrationId, setImportFromOtherSystemSelectedIntegrationId] = useState();
  const [isDataDeleted, setIsDataDeleted] = useState(false);
  const [isDataLoaded, setIsDataLoaded] = useState(false);
  const [isDatasetReleased, setIsDatasetReleased] = useState(false);
  const [isImportDatasetDialogVisible, setIsImportDatasetDialogVisible] = useState(false);
  const [isImportOtherSystemsDialogVisible, setIsImportOtherSystemsDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingFile, setIsLoadingFile] = useState(false);
  const [isRefreshHighlighted, setIsRefreshHighlighted] = useState(false);
  const [isValidationSelected, setIsValidationSelected] = useState(false);
  const [levelErrorTypes, setLevelErrorTypes] = useState([]);
  const [metaData, setMetaData] = useState({});
  const [replaceData, setReplaceData] = useState(false);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();
  const [tableSchemaId, setTableSchemaId] = useState();
  const [tableSchemaNames, setTableSchemaNames] = useState([]);
  const [validateDialogVisible, setValidateDialogVisible] = useState(false);
  const [validationListDialogVisible, setValidationListDialogVisible] = useState(false);
  const [validationsVisible, setValidationsVisible] = useState(false);

  let exportMenuRef = useRef();
  let importMenuRef = useRef();

  const callSetMetaData = async () => {
    setMetaData(await getMetadata({ datasetId, dataflowId }));
  };

  useBreadCrumbs({ currentPage: CurrentPage.DATASET, dataflowId, history, metaData });

  useEffect(() => {
    leftSideBarContext.removeModels();
  }, []);

  useEffect(() => {
    if (!isUndefined(userContext.contextRoles)) {
      setHasWritePermissions(
        userContext.hasPermission([config.permissions.LEAD_REPORTER], `${config.permissions.DATASET}${datasetId}`) ||
          userContext.hasPermission([config.permissions.REPORTER_WRITE], `${config.permissions.DATASET}${datasetId}`)
      );
    }
  }, [userContext]);

  useEffect(() => {
    onLoadDatasetSchema();
  }, [isDataDeleted]);

  useEffect(() => {
    if (!isUndefined(userContext.contextRoles)) {
      leftSideBarContext.addHelpSteps(DatasetSchemaReporterHelpConfig, 'datasetSchemaReporterHelpConfig');
    }
  }, [userContext, isDataLoaded, tableSchemaColumns]);

  useEffect(() => {
    if (isEmpty(externalExportExtensions)) {
      setExportButtonsList(internalExtensions);
    } else {
      setExportButtonsList(internalExtensions.concat(externalExtensions));
    }
  }, [datasetName, externalExportExtensions]);

  useEffect(() => {
    if (isEmpty(externalOperationsList.import)) {
      setImportButtonsList(importFromOtherSystems);
    } else {
      setImportButtonsList(importFromFile.concat(importFromOtherSystems));
    }
  }, [externalOperationsList.import]);

  useEffect(() => {
    if (!isUndefined(exportDatasetData)) {
      DownloadFile(exportDatasetData, exportDatasetDataName);
    }
  }, [exportDatasetData]);

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
    getDatasetSchemaId();
    onLoadDataflow();
  }, []);

  useEffect(() => {
    if (datasetSchemaId) getFileExtensions();
  }, [datasetSchemaId, isImportDatasetDialogVisible]);

  useEffect(() => {
    getExportExtensions(externalOperationsList.export);
  }, [externalOperationsList]);

  const parseUniqExportExtensions = exportExtensionsOperationsList => {
    return exportExtensionsOperationsList.map(uniqExportExtension => ({
      text: `${uniqExportExtension.toUpperCase()} (.${uniqExportExtension.toLowerCase()})`,
      code: uniqExportExtension.toLowerCase()
    }));
  };

  const getExportExtensions = exportExtensionsOperationsList => {
    const uniqExportExtensions = uniq(exportExtensionsOperationsList.map(element => element.fileExtension));
    setExternalExportExtensions(parseUniqExportExtensions(uniqExportExtensions));
  };

  const importFromFile = [
    {
      label: resources.messages['importFromFile'],
      icon: config.icons['import'],
      command: () => setIsImportDatasetDialogVisible(true)
    }
  ];

  const importFromOtherSystems = [
    {
      label: resources.messages['importPreviousData'],
      items: externalOperationsList.importOtherSystems.map(importOtherSystem => ({
        label: importOtherSystem.name,
        icon: config.icons['import'],
        command: () => {
          setImportFromOtherSystemSelectedIntegrationId(importOtherSystem.id);
          setIsImportOtherSystemsDialogVisible(true);
        }
      }))
    }
  ];

  const internalExtensions = config.exportTypes.exportDatasetTypes.map(type => ({
    label: type.text,
    icon: config.icons['archive'],
    command: () => onExportDataInternalExtension(type.code)
  }));

  const externalExtensions = [
    {
      label: resources.messages['externalExtensions'],
      items: externalExportExtensions.map(type => ({
        label: type.text,
        icon: config.icons['archive'],
        command: () => onExportDataExternalExtension(type.code)
      }))
    }
  ];

  const getFileExtensions = async () => {
    try {
      const response = await IntegrationService.allExtensionsOperations(datasetSchemaId);
      setExternalOperationsList(ExtensionUtils.groupOperations('operation', response));
    } catch (error) {
      notificationContext.add({ type: 'LOADING_FILE_EXTENSIONS_ERROR' });
    }
  };

  const getDatasetSchemaId = async () => {
    try {
      const metadata = await MetadataUtils.getDatasetMetadata(datasetId);
      setDatasetSchemaId(metadata.datasetSchemaId);
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
      const dataflowData = await DataflowService.dataflowDetails(match.params.dataflowId);
      setDataflowName(dataflowData.name);
    } catch (error) {
      notificationContext.add({
        type: 'DATAFLOW_DETAILS_ERROR',
        content: {}
      });
    }
  };

  const createFileName = (fileName, fileType) => {
    return `${fileName}.${fileType}`;
  };

  const getPosition = e => {
    const button = e.currentTarget;
    const left = `${button.offsetLeft}px`;
    const topValue = button.offsetHeight + button.offsetTop + 3;
    const top = `${topValue}px `;
    const menu = button.nextElementSibling;
    menu.style.top = top;
    menu.style.left = left;
  };

  const onConfirmDelete = async () => {
    try {
      setDeleteDialogVisible(false);
      const dataDeleted = await DatasetService.deleteDataById(datasetId);
      if (dataDeleted) {
        setIsDataDeleted(true);
      }
    } catch (error) {
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'DATASET_SERVICE_DELETE_DATA_BY_ID_ERROR',
        content: {
          dataflowId,
          datasetId,
          dataflowName,
          datasetName
        }
      });
    }
  };

  const onConfirmValidate = async () => {
    try {
      setValidateDialogVisible(false);
      await DatasetService.validateDataById(datasetId);
      notificationContext.add({
        type: 'VALIDATE_DATA_INIT',
        content: {
          dataflowId,
          datasetId,
          dataflowName,
          datasetName
        }
      });
    } catch (error) {
      notificationContext.add({
        type: 'VALIDATE_DATA_BY_ID_ERROR',
        content: {
          dataflowId,
          datasetId,
          dataflowName,
          datasetName
        }
      });
    }
  };

  const cleanImportOtherSystemsDialog = () => {
    setReplaceData(false);
    onSetVisible(setIsImportOtherSystemsDialogVisible, false);
  };

  const onImportOtherSystems = async () => {
    try {
      cleanImportOtherSystemsDialog();
      const dataImported = await IntegrationService.runIntegration(
        importFromOtherSystemSelectedIntegrationId,
        datasetId,
        replaceData
      );
      if (dataImported) {
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
    } catch {
      notificationContext.add({
        type: 'EXTERNAL_IMPORT_REPORING_FROM_OTHER_SYSTEM_ERROR',
        content: {
          dataflowName: dataflowName,
          datasetName: datasetName
        }
      });
    }
  };

  const onHighlightRefresh = value => setIsRefreshHighlighted(value);

  useCheckNotifications(['VALIDATION_FINISHED_EVENT'], onHighlightRefresh, true);
  useCheckNotifications(
    ['DOWNLOAD_FME_FILE_ERROR', 'EXTERNAL_INTEGRATION_DOWNLOAD', 'EXTERNAL_EXPORT_REPORTING_FAILED_EVENT'],
    setIsLoadingFile,
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

  const onExportDataExternalExtension = async fileExtension => {
    setIsLoadingFile(true);
    notificationContext.add({
      type: 'EXPORT_EXTERNAL_INTEGRATION_DATASET'
    });
    try {
      await DatasetService.exportDatasetDataExternal(datasetId, fileExtension);
    } catch (error) {
      onExportError('EXTERNAL_EXPORT_REPORTING_FAILED_EVENT');
    }
  };

  const onExportDataInternalExtension = async fileType => {
    setIsLoadingFile(true);
    try {
      setExportDatasetDataName(createFileName(datasetName, fileType));
      setExportDatasetData(await DatasetService.exportDataById(datasetId, fileType));
    } catch (error) {
      onExportError('EXPORT_DATA_BY_ID_ERROR');
    } finally {
      setIsLoadingFile(false);
    }
  };

  const onLoadDataflow = async () => {
    try {
      const dataflow = await DataflowService.reporting(match.params.dataflowId);
      const dataset = dataflow.datasets.filter(datasets => datasets.datasetId == datasetId);
      setIsDatasetReleased(dataset[0].isReleased);
    } catch (error) {
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'REPORTING_ERROR',
        content: {
          dataflowId,
          datasetId,
          dataflowName,
          datasetName
        }
      });
      if (error.response && (error.response.status === 401 || error.response.status === 403)) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setIsLoading(false);
    }
  };

  const getDataSchema = async () => {
    try {
      const datasetSchema = await DatasetService.schemaById(datasetId);
      setDatasetSchemaAllTables(datasetSchema.tables);
      setDatasetSchemaName(datasetSchema.datasetSchemaName);
      setLevelErrorTypes(datasetSchema.levelErrorTypes);
      return datasetSchema;
    } catch (error) {
      throw new Error('SCHEMA_BY_ID_ERROR');
    }
  };

  const getStatisticsById = async (datasetId, tableSchemaNames) => {
    try {
      const datasetStatistics = await DatasetService.errorStatisticsById(datasetId, tableSchemaNames);
      return datasetStatistics;
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
      setTableSchemaId(datasetSchema.tables[0].tableSchemaId);
      setDatasetName(datasetStatistics.datasetSchemaName);
      const tableSchemaNamesList = [];
      setTableSchema(
        datasetSchema.tables.map(tableSchema => {
          tableSchemaNamesList.push(tableSchema.tableSchemaName);
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
      setTableSchemaNames(tableSchemaNamesList);
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

  const onSelectValidation = (tableSchemaId, posIdRecord, selectedRecordErrorId) => {
    setDataViewerOptions({
      recordPositionId: posIdRecord,
      selectedRecordErrorId: selectedRecordErrorId,
      activeIndex: tableSchemaId
    });
    setIsValidationSelected(true);
    onSetVisible(setValidationsVisible, false);
  };

  const onSetVisible = (fnUseState, visible) => {
    fnUseState(visible);
  };

  const onTabChange = tableSchemaId => {
    setDataViewerOptions({ ...dataViewerOptions, activeIndex: tableSchemaId.index });
  };

  const datasetTitle = () => {
    let datasetReleasedTitle = `${datasetSchemaName} (${resources.messages['released'].toString().toLowerCase()})`;
    return isDatasetReleased ? datasetReleasedTitle : datasetSchemaName;
  };

  const validationListFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => onSetVisible(setValidationListDialogVisible, false)}
    />
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
    //setIsTableDeleted(false);
  };

  const getImportExtensions = externalOperationsList.import
    .map(file => `.${file.fileExtension}`)
    .join(', ')
    .toLowerCase();

  const infoExtensionsTooltip = `${resources.messages['supportedFileExtensionsTooltip']} ${uniq(
    getImportExtensions.split(', ')
  ).join(', ')}`;

  const renderCustomFileUploadFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => setIsImportDatasetDialogVisible(false)}
    />
  );

  const renderDashboardFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => onSetVisible(setDashDialogVisible, false)}
    />
  );

  const renderImportOtherSystemsFooter = (
    <Fragment>
      <Button
        className="p-button-animated-blink"
        label={resources.messages['import']}
        icon={'check'}
        onClick={() => onImportOtherSystems()}
      />
      <Button
        className="p-button-secondary"
        icon="cancel"
        label={resources.messages['cancel']}
        onClick={() => cleanImportOtherSystemsDialog()}
      />
    </Fragment>
  );

  const renderValidationsFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => manageDialogs('isValidationViewerVisible', false)}
    />
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
        title={`${datasetTitle()}`}
        subtitle={`${dataflowName} - ${datasetName}`}
        icon="dataset"
        iconSize="3.5rem"
      />
      <div className={styles.ButtonsBar}>
        <Toolbar>
          <div className="p-toolbar-group-left datasetSchema-buttonsbar-dataset-data-help-step">
            {hasWritePermissions &&
              (!isEmpty(externalOperationsList.import) || !isEmpty(externalOperationsList.importOtherSystems)) && (
                <Fragment>
                  <Button
                    className={`p-button-rounded p-button-secondary datasetSchema-buttonsbar-dataset-data-help-step ${
                      !hasWritePermissions ? null : 'p-button-animated-blink'
                    }`}
                    disabled={!hasWritePermissions}
                    icon={'import'}
                    label={resources.messages['importDataset']}
                    onClick={
                      !isEmpty(externalOperationsList.importOtherSystems)
                        ? event => importMenuRef.current.show(event)
                        : () => setIsImportDatasetDialogVisible(true)
                    }
                  />
                  {!isEmpty(externalOperationsList.importOtherSystems) && (
                    <Menu
                      model={importButtonsList}
                      popup={true}
                      ref={importMenuRef}
                      id="importDataSetMenu"
                      onShow={e => {
                        getPosition(e);
                      }}
                    />
                  )}
                </Fragment>
              )}
            <Button
              id="buttonExportDataset"
              className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink datasetSchema-export-dataset-help-step`}
              icon={isLoadingFile ? 'spinnerAnimate' : 'export'}
              label={resources.messages['exportDataset']}
              onClick={event => exportMenuRef.current.show(event)}
            />
            <Menu
              model={exportButtonsList}
              popup={true}
              ref={exportMenuRef}
              id="exportDataSetMenu"
              onShow={e => {
                getPosition(e);
              }}
            />
            <Button
              className={`p-button-rounded p-button-secondary-transparent ${
                !hasWritePermissions ? null : 'p-button-animated-blink dataset-deleteDataset-help-step'
              }`}
              icon={'trash'}
              label={resources.messages['deleteDatasetData']}
              disabled={!hasWritePermissions}
              onClick={() => onSetVisible(setDeleteDialogVisible, true)}
            />
          </div>
          <div className="p-toolbar-group-right">
            <Button
              className={`p-button-rounded p-button-secondary-transparent dataset-validate-help-step ${
                !hasWritePermissions || !datasetHasData ? null : 'p-button-animated-blink'
              }`}
              disabled={!hasWritePermissions || !datasetHasData}
              icon={'validate'}
              label={resources.messages['validate']}
              onClick={() => onSetVisible(setValidateDialogVisible, true)}
              ownButtonClasses={null}
              iconClasses={null}
            />
            <Button
              className={`p-button-rounded p-button-secondary-transparent dataset-showValidations-help-step ${
                !datasetHasErrors ? null : 'p-button-animated-blink'
              }`}
              disabled={!datasetHasErrors}
              icon={'warning'}
              label={resources.messages['showValidations']}
              onClick={() => onSetVisible(setValidationsVisible, true)}
              ownButtonClasses={null}
              iconClasses={datasetHasErrors ? 'warning' : ''}
            />
            <Button
              className={
                'p-button-rounded p-button-secondary-transparent p-button-animated-blink datasetSchema-qcRules-help-step'
              }
              icon={'horizontalSliders'}
              label={resources.messages['qcRules']}
              onClick={() => onSetVisible(setValidationListDialogVisible, true)}
              ownButtonClasses={null}
            />
            <Button
              className={`p-button-rounded p-button-secondary-transparent dataset-dashboards-help-step ${
                !datasetHasData ? null : 'p-button-animated-blink'
              }`}
              disabled={!datasetHasData}
              icon={'dashboard'}
              label={resources.messages['dashboards']}
              onClick={() => onSetVisible(setDashDialogVisible, true)}
            />
            <Button
              className={`p-button-rounded p-button-secondary-transparent datasetSchema-manageCopies-help-step ${
                !hasWritePermissions ? null : 'p-button-animated-blink'
              }`}
              disabled={!hasWritePermissions}
              icon={'camera'}
              label={resources.messages['snapshots']}
              onClick={() => setIsSnapshotsBarVisible(!isSnapshotsBarVisible)}
            />
            <Button
              className={`p-button-rounded p-button-${
                isRefreshHighlighted ? 'primary' : 'secondary-transparent'
              } p-button-animated-blink dataset-refresh-help-step`}
              icon={'refresh'}
              label={resources.messages['refresh']}
              onClick={() => onLoadDatasetSchema()}
            />
          </div>
        </Toolbar>
      </div>
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
            tableSchemaNames={tableSchemaNames}
          />
        </Dialog>
      )}
      <TabsSchema
        activeIndex={dataViewerOptions.activeIndex}
        hasWritePermissions={hasWritePermissions}
        isDatasetDeleted={isDataDeleted}
        isValidationSelected={isValidationSelected}
        levelErrorTypes={levelErrorTypes}
        onLoadTableData={onLoadTableData}
        onTabChange={tableSchemaId => onTabChange(tableSchemaId)}
        recordPositionId={dataViewerOptions.recordPositionId}
        reporting={true}
        selectedRecordErrorId={dataViewerOptions.selectedRecordErrorId}
        setIsValidationSelected={setIsValidationSelected}
        tables={tableSchema}
        tableSchemaColumns={tableSchemaColumns}
      />
      {validationsVisible && (
        <Dialog
          className={styles.paginatorValidationViewer}
          footer={renderValidationsFooter}
          header={resources.messages['titleValidations']}
          onHide={() => onSetVisible(setValidationsVisible, false)}
          style={{ width: '80%' }}
          visible={validationsVisible}>
          <ValidationViewer
            datasetId={datasetId}
            datasetName={datasetName}
            hasWritePermissions={hasWritePermissions}
            levelErrorTypes={levelErrorTypes}
            onSelectValidation={onSelectValidation}
            tableSchemaNames={tableSchemaNames}
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
          <TabsValidations
            dataset={{ datasetId: datasetId, name: datasetSchemaName }}
            datasetSchemaAllTables={datasetSchemaAllTables}
            datasetSchemaId={datasetSchemaId}
            onHideValidationsDialog={() => onSetVisible(setValidationListDialogVisible, false)}
            reporting={true}
          />
        </Dialog>
      )}
      {isImportDatasetDialogVisible && (
        <Dialog
          className={styles.Dialog}
          footer={renderCustomFileUploadFooter}
          header={`${resources.messages['uploadDataset']}${datasetName}`}
          onHide={() => setIsImportDatasetDialogVisible(false)}
          visible={isImportDatasetDialogVisible}>
          <CustomFileUpload
            accept={getImportExtensions}
            chooseLabel={resources.messages['selectFile']} //allowTypes="/(\.|\/)(csv)$/"
            className={styles.FileUpload}
            fileLimit={1}
            infoTooltip={infoExtensionsTooltip}
            invalidExtensionMessage={resources.messages['invalidExtensionFile']}
            mode="advanced"
            multiple={false}
            name="file"
            onUpload={onUpload}
            replaceCheck={true}
            url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.importDatasetData, {
              datasetId: datasetId
            })}`}
          />
        </Dialog>
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
              id="replaceCheckbox"
              inputId="replaceCheckbox"
              isChecked={replaceData}
              onChange={() => setReplaceData(!replaceData)}
              role="checkbox"
            />
            <label htmlFor="replaceCheckbox">
              <a onClick={() => setReplaceData(!replaceData)}>{resources.messages['replaceData']}</a>
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
      <Snapshots
        isLoadingSnapshotListData={isLoadingSnapshotListData}
        isSnapshotDialogVisible={isSnapshotDialogVisible}
        setIsSnapshotDialogVisible={setIsSnapshotDialogVisible}
        snapshotListData={snapshotListData}
      />
    </SnapshotContext.Provider>
  );
});
