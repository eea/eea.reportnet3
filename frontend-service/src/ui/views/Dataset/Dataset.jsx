/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef } from 'react';

import { withRouter } from 'react-router-dom';
import { capitalize, isUndefined } from 'lodash';

import styles from './Dataset.module.css';

import { config } from 'conf';
import { DatasetConfig } from 'conf/domain/model/Dataset';
import { routes } from 'ui/routes';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dashboard } from './_components/Dashboard';
import { Dialog } from 'ui/views/_components/Dialog';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { MainLayout } from 'ui/views/_components/Layout';
import { Menu } from 'primereact/menu';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Snapshots } from 'ui/views/_components/Snapshots';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsSchema } from 'ui/views/_components/TabsSchema';
import { Title } from 'ui/views/_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { ValidationViewer } from './_components/ValidationViewer';
import { WebFormData } from './_components/WebFormData/WebFormData';

import { CodelistService } from 'core/services/Codelist';
import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { UserService } from 'core/services/User';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { useReporterDataset } from 'ui/views/_components/Snapshots/_hooks/useReporterDataset';

import { MetadataUtils } from 'ui/views/_functions/Utils';
import { getUrl } from 'core/infrastructure/CoreUtils';

export const Dataset = withRouter(({ match, history }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;

  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [dashDialogVisible, setDashDialogVisible] = useState(false);
  const [dataflowName, setDataflowName] = useState('');
  const [datasetSchemaName, setDatasetSchemaName] = useState();
  const [datasetName, setDatasetName] = useState('');
  const [datasetHasErrors, setDatasetHasErrors] = useState(false);
  const [dataViewerOptions, setDataViewerOptions] = useState({
    recordPositionId: -1,
    selectedRecordErrorId: -1,
    activeIndex: null
  });
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [exportButtonsList, setExportButtonsList] = useState([]);
  const [exportDatasetData, setExportDatasetData] = useState(undefined);
  const [exportDatasetDataName, setExportDatasetDataName] = useState('');
  const [datasetHasData, setDatasetHasData] = useState(false);
  const [isDataDeleted, setIsDataDeleted] = useState(false);
  const [isDatasetReleased, setIsDatasetReleased] = useState(false);
  const [isInputSwitchChecked, setIsInputSwitchChecked] = useState(false);
  const [isValidationSelected, setIsValidationSelected] = useState(false);
  const [isWebFormMMR, setIsWebFormMMR] = useState(false);
  const [levelErrorTypes, setLevelErrorTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingFile, setLoadingFile] = useState(false);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();
  const [tableSchemaNames, setTableSchemaNames] = useState([]);
  const [validateDialogVisible, setValidateDialogVisible] = useState(false);
  const [validationsVisible, setValidationsVisible] = useState(false);
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [tableSchemaId, setTableSchemaId] = useState();
  const [metaData, setMetaData] = useState({});

  let exportMenuRef = useRef();

  const callSetMetaData = async () => {
    setMetaData(await getMetadata({ datasetId, dataflowId }));
  };

  useEffect(() => {
    callSetMetaData();
  }, []);

  useEffect(() => {
    if (!isUndefined(metaData.dataset)) {
      console.info('dataset.Metadata: %o', metaData);
      const breadCrumbs = [
        {
          label: resources.messages['dataflowList'],
          icon: 'home',
          href: getUrl(routes.DATAFLOWS),
          command: () => history.push(getUrl(routes.DATAFLOWS))
        },
        {
          label: resources.messages['dataflow'],
          icon: 'archive',
          href: getUrl(
            routes.DATAFLOW,
            {
              dataflowId
            },
            true
          ),
          command: () =>
            history.push(
              getUrl(
                routes.DATAFLOW,
                {
                  dataflowId
                },
                true
              )
            )
        }
      ];
      if (breadCrumbContext.model.find(model => model.icon === 'representative')) {
        breadCrumbs.push({
          label: !isUndefined(metaData.dataset) ? metaData.dataset.name : resources.messages['representative'],
          icon: 'representative',
          href: getUrl(
            routes.REPRESENTATIVE,
            {
              dataflowId,
              representative: metaData.dataset.name
            },
            true
          ),
          command: () =>
            history.push(
              getUrl(
                routes.REPRESENTATIVE,
                {
                  dataflowId,
                  representative: metaData.dataset.name
                },
                true
              )
            )
        });
      }
      breadCrumbs.push({ label: resources.messages['dataset'], icon: 'dataset' });
      breadCrumbContext.add(breadCrumbs);
      leftSideBarContext.removeModels();
    }
  }, [metaData]);

  useEffect(() => {
    if (!isUndefined(user.contextRoles)) {
      setHasWritePermissions(
        UserService.hasPermission(user, [config.permissions.PROVIDER], `${config.permissions.DATASET}${datasetId}`)
      );
    }
  }, [user]);

  useEffect(() => {
    onLoadDatasetSchema();
  }, [isDataDeleted]);

  useEffect(() => {
    let exportOptions = config.exportTypes;
    const exportOptionsFilter = exportOptions.filter(type => type.code !== 'csv');

    setExportButtonsList(
      exportOptionsFilter.map(type => ({
        label: type.text,
        icon: config.icons['archive'],
        command: () => onExportData(type.code)
      }))
    );
  }, [datasetName]);

  useEffect(() => {
    if (!isUndefined(exportDatasetData)) {
      DownloadFile(exportDatasetData, exportDatasetDataName);
    }
  }, [exportDatasetData]);

  const {
    isLoadingSnapshotListData,
    isSnapshotsBarVisible,
    setIsSnapshotsBarVisible,
    isSnapshotDialogVisible,
    setIsSnapshotDialogVisible,
    snapshotDispatch,
    snapshotListData,
    snapshotState
  } = useReporterDataset(datasetId, dataflowId);

  useEffect(() => {
    try {
      getDataflowName();
      onLoadDataflow();
    } catch (error) {
      console.error(error.response);
    }
  }, []);

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

  const checkIsWebFormMMR = datasetName => {
    const mmrDatasetName = 'MMR_TEST';
    if (datasetName.toString().toLowerCase() === mmrDatasetName.toString().toLowerCase()) {
      setIsInputSwitchChecked(true);
      setIsWebFormMMR(true);
    } else {
      setIsWebFormMMR(false);
    }
  };

  const getPosition = button => {
    const buttonTopPosition = button.top;
    const buttonLeftPosition = button.left;

    const exportDatasetMenu = document.getElementById('exportDataSetMenu');
    exportDatasetMenu.style.top = buttonTopPosition;
    exportDatasetMenu.style.left = buttonLeftPosition;
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
        type: 'DELETE_DATA_BY_ID_ERROR',
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
    //  QUE ES ESO??
    /*     const {
      dataflow: { name: dataflowName },
      dataset: { name: datasetName }
    } = await getMetadata({ dataflowId, datasetId }); */

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

  const onLoadTableData = hasData => {
    setDatasetHasData(hasData);
  };

  const onExportData = async fileType => {
    setLoadingFile(true);
    try {
      setExportDatasetDataName(createFileName(datasetName, fileType));
      setExportDatasetData(await DatasetService.exportDataById(datasetId, fileType));
    } catch (error) {
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'EXPORT_DATA_BY_ID_ERROR',
        content: {
          dataflowId,
          datasetId,
          dataflowName,
          datasetName
        }
      });
    } finally {
      setLoadingFile(false);
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
      setLoading(false);
    }
  };

  const getDataSchema = async () => {
    try {
      const datasetSchema = await DatasetService.schemaById(datasetId);
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

  const getCodelistsList = async datasetSchemas => {
    try {
      const codelistsList = await CodelistService.getCodelistsList(datasetSchemas);
      return codelistsList;
    } catch (error) {
      throw new Error('CODELIST_SERVICE_GET_CODELISTS_LIST');
    }
  };

  const onLoadDatasetSchema = async () => {
    try {
      const datasetSchema = await getDataSchema();
      const codelistsList = await getCodelistsList([datasetSchema]);
      console.log({ codelistsList });
      const datasetStatistics = await getStatisticsById(
        datasetId,
        datasetSchema.tables.map(tableSchema => tableSchema.tableSchemaName)
      );
      setTableSchemaId(datasetSchema.tables[0].tableSchemaId);
      setDatasetName(datasetStatistics.datasetSchemaName);
      checkIsWebFormMMR(datasetStatistics.datasetSchemaName);
      const tableSchemaNamesList = [];
      console.log({ datasetSchema });
      setTableSchema(
        datasetSchema.tables.map(tableSchema => {
          tableSchemaNamesList.push(tableSchema.tableSchemaName);
          return {
            id: tableSchema['tableSchemaId'],
            name: tableSchema['tableSchemaName'],
            hasErrors: {
              ...datasetStatistics.tables.filter(table => table['tableSchemaId'] === tableSchema['tableSchemaId'])[0]
            }.hasErrors
          };
        })
      );
      setTableSchemaNames(tableSchemaNamesList);
      setTableSchemaColumns(
        datasetSchema.tables.map(table => {
          return table.records[0].fields.map(field => {
            let codelist = {};
            if (field.type === 'CODELIST') {
              codelist = codelistsList.find(codelist => codelist.id === field.codelistId);
            }
            return {
              table: table['tableSchemaName'],
              field: field['fieldId'],
              header: `${capitalize(field['name'])}`,
              type: field['type'],
              recordId: field['recordId'],
              codelistId: field.codelistId,
              codelistName: codelist.name,
              codelistVersion: codelist.version,
              codelistItems: codelist.items
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
          // dataflowId,
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
      setLoading(false);
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
    // setActiveIndex(tableSchemaId.index);
  };

  const datasetTitle = () => {
    let datasetReleasedTitle = `${datasetSchemaName} (${resources.messages['released'].toString().toLowerCase()})`;
    return isDatasetReleased ? datasetReleasedTitle : datasetSchemaName;
  };

  const showWebFormInputSwitch = () => {
    if (isWebFormMMR) {
      return (
        <div className={styles.InputSwitchContainer}>
          <div className={styles.InputSwitchDiv}>
            <span className={styles.InputSwitchText}>{resources.messages['grid']}</span>
            {WebFormInputSwitch}
            <span className={styles.InputSwitchText}>{resources.messages['webForm']}</span>
          </div>
        </div>
      );
    }
  };

  const isWebForm = () => {
    if (isInputSwitchChecked) {
      return <WebFormData datasetId={datasetId} tableSchemaId={tableSchemaId} />;
    } else {
      return (
        <TabsSchema
          activeIndex={dataViewerOptions.activeIndex}
          hasWritePermissions={hasWritePermissions}
          isValidationSelected={isValidationSelected}
          isWebFormMMR={isWebFormMMR}
          levelErrorTypes={levelErrorTypes}
          onLoadTableData={onLoadTableData}
          onTabChange={tableSchemaId => onTabChange(tableSchemaId)}
          recordPositionId={dataViewerOptions.recordPositionId}
          selectedRecordErrorId={dataViewerOptions.selectedRecordErrorId}
          setIsValidationSelected={setIsValidationSelected}
          tables={tableSchema}
          tableSchemaColumns={tableSchemaColumns}
        />
      );
    }
  };

  const layout = children => {
    return (
      <MainLayout>
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  let WebFormInputSwitch = (
    <InputSwitch
      className={styles.WebFormInputSwitch}
      checked={isInputSwitchChecked}
      onChange={e => {
        setIsInputSwitchChecked(e.value);
      }}
    />
  );

  if (loading) {
    return layout(<Spinner />);
  }

  return layout(
    <SnapshotContext.Provider
      value={{
        snapshotState: snapshotState,
        snapshotDispatch: snapshotDispatch,
        isSnapshotsBarVisible: isSnapshotsBarVisible,

        setIsSnapshotsBarVisible: setIsSnapshotsBarVisible
      }}>
      <Title
        title={`${datasetTitle()}`}
        subtitle={`${dataflowName} - ${datasetName}`}
        icon="dataset"
        iconSize="3.5rem"
      />
      <div className={styles.ButtonsBar}>
        <Toolbar>
          <div className="p-toolbar-group-left">
            <Button
              className={`p-button-rounded p-button-secondary-transparent ${
                !hasWritePermissions ? null : 'p-button-animated-upload'
              }`}
              disabled={!hasWritePermissions}
              icon={loadingFile ? 'spinnerAnimate' : 'import'}
              label={resources.messages['export']}
              onClick={event => exportMenuRef.current.show(event)}
            />
            <Menu
              model={exportButtonsList}
              popup={true}
              ref={exportMenuRef}
              id="exportDataSetMenu"
              onShow={e => {
                getPosition(e.target.style);
              }}
            />
            <Button
              className={`p-button-rounded p-button-secondary-transparent ${
                !hasWritePermissions || isWebFormMMR ? null : 'p-button-animated-blink'
              }`}
              icon={'trash'}
              label={resources.messages['deleteDatasetData']}
              disabled={!hasWritePermissions || isWebFormMMR}
              onClick={() => onSetVisible(setDeleteDialogVisible, true)}
            />
          </div>
          <div className="p-toolbar-group-right">
            {/* <Button
              className={`p-button-rounded p-button-secondary-transparent`}
              disabled={true}
              icon={'clock'}
              label={resources.messages['events']}
              onClick={null}
            /> */}
            <Button
              className={`p-button-rounded p-button-secondary-transparent ${
                !hasWritePermissions || isWebFormMMR || !datasetHasData ? null : 'p-button-animated-blink'
              }`}
              disabled={!hasWritePermissions || isWebFormMMR || !datasetHasData}
              icon={'validate'}
              label={resources.messages['validate']}
              onClick={() => onSetVisible(setValidateDialogVisible, true)}
              ownButtonClasses={null}
              iconClasses={null}
            />
            <Button
              className={`p-button-rounded p-button-secondary-transparent ${
                !datasetHasErrors || isWebFormMMR ? null : 'p-button-animated-blink'
              }`}
              disabled={!datasetHasErrors || isWebFormMMR}
              icon={'warning'}
              label={resources.messages['showValidations']}
              onClick={() => onSetVisible(setValidationsVisible, true)}
              ownButtonClasses={null}
              iconClasses={datasetHasErrors ? 'warning' : ''}
            />
            <Button
              className={`p-button-rounded p-button-secondary-transparent ${
                isWebFormMMR || !datasetHasData ? null : 'p-button-animated-blink'
              }`}
              disabled={isWebFormMMR || !datasetHasData}
              icon={'dashboard'}
              label={resources.messages['dashboards']}
              onClick={() => onSetVisible(setDashDialogVisible, true)}
            />
            <Button
              className={`p-button-rounded p-button-secondary-transparent ${
                !hasWritePermissions ? null : 'p-button-animated-blink'
              }`}
              disabled={!hasWritePermissions}
              icon={'camera'}
              label={resources.messages['snapshots']}
              onClick={() => setIsSnapshotsBarVisible(!isSnapshotsBarVisible)}
            />
          </div>
        </Toolbar>
      </div>
      <Dialog
        dismissableMask={true}
        header={resources.messages['titleDashboard']}
        onHide={() => onSetVisible(setDashDialogVisible, false)}
        style={{ width: '70vw' }}
        visible={dashDialogVisible}>
        <Dashboard refresh={dashDialogVisible} levelErrorTypes={levelErrorTypes} tableSchemaNames={tableSchemaNames} />
      </Dialog>
      {showWebFormInputSwitch()}
      {isWebForm()}
      <Dialog
        className={styles.paginatorValidationViewer}
        dismissableMask={true}
        header={resources.messages['titleValidations']}
        maximizable
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
      <ConfirmDialog
        header={resources.messages['deleteDatasetHeader']}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        maximizable={false}
        onConfirm={onConfirmDelete}
        onHide={() => onSetVisible(setDeleteDialogVisible, false)}
        visible={deleteDialogVisible}>
        {resources.messages['deleteDatasetConfirm']}
      </ConfirmDialog>
      <ConfirmDialog
        header={resources.messages['validateDataset']}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        maximizable={false}
        onConfirm={onConfirmValidate}
        onHide={() => onSetVisible(setValidateDialogVisible, false)}
        visible={validateDialogVisible}>
        {resources.messages['validateDatasetConfirm']}
      </ConfirmDialog>
      <Snapshots
        snapshotListData={snapshotListData}
        isLoadingSnapshotListData={isLoadingSnapshotListData}
        isSnapshotDialogVisible={isSnapshotDialogVisible}
        setIsSnapshotDialogVisible={setIsSnapshotDialogVisible}
      />
    </SnapshotContext.Provider>
  );
});
