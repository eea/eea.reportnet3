/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef } from 'react';

import { withRouter } from 'react-router-dom';
import { capitalize, isUndefined } from 'lodash';

import styles from './Dataset.module.css';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dashboard } from './_components/Dashboard';
import { Dialog } from 'ui/views/_components/Dialog';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { Growl } from 'primereact/growl';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { MainLayout } from 'ui/views/_components/Layout';
import { Menu } from 'primereact/menu';
import { DatasetContext } from 'ui/views/_functions/Contexts//DatasetContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Snapshots } from 'ui/views/_components/Snapshots';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsSchema } from './_components/TabsSchema';
import { Title } from 'ui/views/_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { ValidationViewer } from './_components/ValidationViewer';
import { WebFormData } from './_components/WebFormData/WebFormData';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserService } from 'core/services/User';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

import { useReporterDataset } from 'ui/views/_components/Snapshots/_hooks/useReporterDataset';

export const Dataset = withRouter(({ match, history }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
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

  let exportMenuRef = useRef();

  let growlRef = useRef();

  useEffect(() => {
    setBreadCrumbItems([
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
            dataflowId: match.params.dataflowId
          },
          true
        ),
        command: () =>
          history.push(
            getUrl(
              routes.DATAFLOW,
              {
                dataflowId: match.params.dataflowId
              },
              true
            )
          )
      },
      { label: resources.messages['dataset'], icon: 'dataset' }
    ]);
  }, []);

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
  } = useReporterDataset(datasetId, dataflowId, growlRef);

  useEffect(() => {
    try {
      getDataflowName();
      onLoadDataflow();
    } catch (error) {
      console.error(error.response);
    }
  }, []);

  const getDataflowName = async () => {
    const dataflowData = await DataflowService.dataflowDetails(match.params.dataflowId);
    setDataflowName(dataflowData.name);
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
    setDeleteDialogVisible(false);
    const dataDeleted = await DatasetService.deleteDataById(datasetId);
    if (dataDeleted) {
      setIsDataDeleted(true);
    }
  };

  const onConfirmValidate = async () => {
    setValidateDialogVisible(false);
    await DatasetService.validateDataById(datasetId);
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
      console.error(error);
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
      if (error.response.status === 401 || error.response.status === 403) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setLoading(false);
    }
  };

  const onLoadDatasetSchema = async () => {
    try {
      const datasetSchema = await DatasetService.schemaById(datasetId);
      setDatasetSchemaName(datasetSchema.datasetSchemaName);
      setLevelErrorTypes(datasetSchema.levelErrorTypes);
      const datasetStatistics = await DatasetService.errorStatisticsById(
        datasetId,
        datasetSchema.tables.map(tableSchema => tableSchema.tableSchemaName)
      );
      setTableSchemaId(datasetSchema.tables[0].tableSchemaId);
      setDatasetName(datasetStatistics.datasetSchemaName);
      checkIsWebFormMMR(datasetStatistics.datasetSchemaName);
      const tableSchemaNamesList = [];
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
            return {
              table: table['tableSchemaName'],
              field: field['fieldId'],
              header: `${capitalize(field['name'])}`,
              type: field['type'],
              recordId: field['recordId']
            };
          });
        })
      );

      setDatasetHasErrors(datasetStatistics.datasetErrors);
    } catch (error) {
      const errorResponse = error.response;
      if (!isUndefined(errorResponse) && (errorResponse.status === 401 || errorResponse.status === 403)) {
        history.push(getUrl(routes.DATAFLOW, { dataflowId }));
      }
    }
    setLoading(false);
  };

  const onSetVisible = (fnUseState, visible) => {
    fnUseState(visible);
  };

  const onTabChange = tableSchemaId => {
    setDataViewerOptions({ ...dataViewerOptions, activeIndex: tableSchemaId.index });
    // setActiveIndex(tableSchemaId.index);
  };

  const datasetTitle = () => {
    let datasetReleasedTitle = `${datasetName} (${resources.messages['released'].toString().toLowerCase()})`;
    return isDatasetReleased ? datasetReleasedTitle : datasetName;
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
          onTabChange={tableSchemaId => onTabChange(tableSchemaId)}
          recordPositionId={dataViewerOptions.recordPositionId}
          selectedRecordErrorId={dataViewerOptions.selectedRecordErrorId}
          tables={tableSchema}
          tableSchemaColumns={tableSchemaColumns}
          isWebFormMMR={isWebFormMMR}
          hasWritePermissions={hasWritePermissions}
          levelErrorTypes={levelErrorTypes}
          onLoadTableData={onLoadTableData}
        />
      );
    }
  };

  const layout = children => {
    return (
      <MainLayout>
        <Growl ref={growlRef} />
        <BreadCrumb model={breadCrumbItems} />
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
        subtitle={`${dataflowName} - ${datasetSchemaName}`}
        icon="dataset"
        iconSize="3.5rem"
      />
      <div className={styles.ButtonsBar}>
        <Toolbar>
          <div className="p-toolbar-group-left">
            <Button
              className={`p-button-rounded p-button-secondary`}
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
              className={`p-button-rounded p-button-secondary`}
              icon={'trash'}
              label={resources.messages['deleteDatasetData']}
              disabled={!hasWritePermissions || isWebFormMMR}
              onClick={() => onSetVisible(setDeleteDialogVisible, true)}
            />
          </div>
          <div className="p-toolbar-group-right">
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'clock'}
              label={resources.messages['events']}
              onClick={null}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={!hasWritePermissions || isWebFormMMR || !datasetHasData}
              icon={'validate'}
              label={resources.messages['validate']}
              onClick={() => onSetVisible(setValidateDialogVisible, true)}
              ownButtonClasses={null}
              iconClasses={null}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={!datasetHasErrors || isWebFormMMR}
              icon={'warning'}
              label={resources.messages['showValidations']}
              onClick={() => onSetVisible(setValidationsVisible, true)}
              ownButtonClasses={null}
              iconClasses={datasetHasErrors ? 'warning' : ''}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={isWebFormMMR || !datasetHasData}
              icon={'dashboard'}
              label={resources.messages['dashboards']}
              onClick={() => onSetVisible(setDashDialogVisible, true)}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
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
      <DatasetContext.Provider
        value={{
          isValidationSelected: isValidationSelected,
          setIsValidationSelected: setIsValidationSelected,
          onValidationsVisible: () => {
            onSetVisible(setValidationsVisible, false);
          },
          onSelectValidation: (tableSchemaId, posIdRecord, selectedRecordErrorId) => {
            setDataViewerOptions({
              recordPositionId: posIdRecord,
              selectedRecordErrorId: selectedRecordErrorId,
              activeIndex: tableSchemaId
            });
          }
        }}>
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
            visible={validationsVisible}
            hasWritePermissions={hasWritePermissions}
            tableSchemaNames={tableSchemaNames}
            levelErrorTypes={levelErrorTypes}
          />
        </Dialog>
      </DatasetContext.Provider>
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
