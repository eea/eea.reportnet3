/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useReducer, useRef } from 'react';
import moment from 'moment';
import { withRouter } from 'react-router-dom';
import { isUndefined } from 'lodash';

import styles from './ReporterDataSet.module.css';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dashboard } from './_components/Dashboard';
import { Dialog } from 'ui/views/_components/Dialog';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { Growl } from 'primereact/growl';
import { InputSwitch } from 'primereact/inputswitch';
import { MainLayout } from 'ui/views/_components/Layout';
import { Menu } from 'primereact/menu';
import { ReporterDatasetContext } from './_components/_context/ReporterDataSetContext';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { SnapshotSlideBar } from './_components/SnapshotSlideBar';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsSchema } from './_components/TabsSchema';
import { Title } from './_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { ValidationViewer } from './_components/ValidationViewer';
import { WebFormData } from './_components/WebFormData/WebFormData';

import { DatasetService } from 'core/services/DataSet';
import { SnapshotService } from 'core/services/Snapshot';
import { UserContext } from 'ui/views/_components/_context/UserContext';
import { SnapshotContext } from 'ui/views/_components/_context/SnapshotContext';
import { UserService } from 'core/services/User';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { routes } from 'ui/routes';

export const ReporterDataset = withRouter(({ match, history }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);
  const [activeIndex, setActiveIndex] = useState();
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dashDialogVisible, setDashDialogVisible] = useState(false);
  const [datasetTitle, setDatasetTitle] = useState('');
  const [datasetHasErrors, setDatasetHasErrors] = useState(false);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [exportButtonsList, setExportButtonsList] = useState([]);
  const [exportDatasetData, setExportDatasetData] = useState(undefined);
  const [exportDatasetDataName, setExportDatasetDataName] = useState('');
  const [isDataDeleted, setIsDataDeleted] = useState(false);
  const [isLoadingSnapshotListData, setIsLoadingSnapshotListData] = useState(true);
  const [isInputSwitchChecked, setIsInputSwitchChecked] = useState(false);
  const [isWebFormMMR, setIsWebFormMMR] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadingFile, setLoadingFile] = useState(false);
  const [recordPositionId, setRecordPositionId] = useState(-1);
  const [selectedRecordErrorId, setSelectedRecordErrorId] = useState(-1);
  const [snapshotDialogVisible, setSnapshotDialogVisible] = useState(false);
  const [snapshotIsVisible, setSnapshotIsVisible] = useState(false);
  const [snapshotListData, setSnapshotListData] = useState([]);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();
  const [tableSchemaNames, setTableSchemaNames] = useState([]);
  const [validateDialogVisible, setValidateDialogVisible] = useState(false);
  const [validationsVisible, setValidationsVisible] = useState(false);
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [tableSchemaId, setTableSchemaId] = useState();

  let exportMenuRef = useRef();

  const home = {
    icon: config.icons['home'],
    command: () => history.push(getUrl(routes.DATAFLOWS))
  };

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
    setBreadCrumbItems([
      {
        label: resources.messages['dataflowList'],
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['dataflow'],
        command: () => history.push(`/dataflow/${match.params.dataflowId}`)
      },
      { label: resources.messages['dataset'] }
    ]);
  }, []);

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
  }, [datasetTitle]);

  useEffect(() => {
    if (!isUndefined(exportDatasetData)) {
      DownloadFile(exportDatasetData, exportDatasetDataName);
    }
  }, [exportDatasetData]);

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

  const onCreateSnapshot = async () => {
    const snapshotCreated = await SnapshotService.createById(datasetId, snapshotState.description);
    if (snapshotCreated) {
      onLoadSnapshotList();
    }
    onSetVisible(setSnapshotDialogVisible, false);
  };

  const onDeleteSnapshot = async () => {
    const snapshotDeleted = await SnapshotService.deleteById(datasetId, snapshotState.snapShotId);
    if (snapshotDeleted) {
      onLoadSnapshotList();
    }
    onSetVisible(setSnapshotDialogVisible, false);
  };

  const onExportData = async fileType => {
    setLoadingFile(true);
    try {
      setExportDatasetDataName(createFileName(datasetTitle, fileType));
      setExportDatasetData(await DatasetService.exportDataById(datasetId, fileType));
    } catch (error) {
      console.error(error);
    } finally {
      setLoadingFile(false);
    }
  };

  const onReleaseSnapshot = async () => {
    const snapshotReleased = await SnapshotService.releaseById(dataflowId, datasetId, snapshotState.snapShotId);
    if (snapshotReleased) {
      onLoadSnapshotList();
    }
    onSetVisible(setSnapshotDialogVisible, false);
  };

  const onRestoreSnapshot = async () => {
    const response = await SnapshotService.restoreById(dataflowId, datasetId, snapshotState.snapShotId);
    if (response) {
      snapshotDispatch({ type: 'mark_as_restored', payload: {} });
      onGrowlAlert({
        severity: 'info',
        summary: resources.messages.snapshotItemRestoreProcessSummary,
        detail: resources.messages.snapshotItemRestoreProcessDetail,
        life: '5000'
      });
    }

    onSetVisible(setSnapshotDialogVisible, false);
  };

  const onLoadDatasetSchema = async () => {
    try {
      const datasetSchema = await DatasetService.schemaById(dataflowId);
      const datasetStatistics = await DatasetService.errorStatisticsById(datasetId);
      setTableSchemaId(datasetSchema.tables[0].tableSchemaId);
      setDatasetTitle(datasetStatistics.datasetSchemaName);
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
              header: `${field['name'].charAt(0).toUpperCase()}${field['name'].slice(1)}`,
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

  const onLoadSnapshotList = async () => {
    try {
      setIsLoadingSnapshotListData(true);
      //Settimeout for avoiding the overlaping between the slidebar transition and the api call
      setTimeout(async () => {
        const snapshotsData = await SnapshotService.all(datasetId);
        setSnapshotListData(snapshotsData);
        setIsLoadingSnapshotListData(false);
      }, 500);
    } catch (error) {
      setIsLoadingSnapshotListData(false);
    }
  };

  const onSetVisible = (fnUseState, visible) => {
    fnUseState(visible);
  };

  const onTabChange = tableSchemaId => {
    setActiveIndex(tableSchemaId.index);
  };

  const createFileName = (fileName, fileType) => {
    return `${fileName}.${fileType}`;
  };

  const onGrowlAlert = message => {
    growlRef.current.show(message);
  };

  let growlRef = useRef();

  const snapshotInitialState = {
    apiCall: '',
    createdAt: '',
    description: '',
    dialogMessage: '',
    dataflowId,
    datasetId,
    snapShotId: '',
    action: () => {}
  };

  const snapshotReducer = (state, { type, payload }) => {
    switch (type) {
      case 'create_snapshot':
        onSetVisible(setSnapshotDialogVisible, true);
        return {
          ...state,
          snapShotId: '',
          creationDate: Date.now(),
          description: payload.description,
          dialogMessage: resources.messages.createSnapshotMessage,
          action: onCreateSnapshot
        };

      case 'delete_snapshot':
        onSetVisible(setSnapshotDialogVisible, true);
        return {
          ...state,
          snapShotId: payload.id,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogMessage: resources.messages.deleteSnapshotMessage,
          action: onDeleteSnapshot
        };

      case 'release_snapshot':
        onSetVisible(setSnapshotDialogVisible, true);
        return {
          ...state,
          snapShotId: payload.id,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogMessage: resources.messages.releaseSnapshotMessage,
          action: onReleaseSnapshot
        };
      case 'restore_snapshot':
        onSetVisible(setSnapshotDialogVisible, true);
        return {
          ...state,
          snapShotId: payload.id,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogMessage: resources.messages.restoreSnapshotMessage,
          action: onRestoreSnapshot
        };
      case 'mark_as_restored':
        return {
          ...state,
          restored: state.snapShotId
        };
      case 'clear_restored':
        return {
          ...state,
          restored: undefined
        };
      default:
        return state;
    }
  };

  const [snapshotState, snapshotDispatch] = useReducer(snapshotReducer, snapshotInitialState);

  const getPosition = button => {
    const buttonTopPosition = button.top;
    const buttonLeftPosition = button.left;

    const exportDatasetMenu = document.getElementById('exportDataSetMenu');
    exportDatasetMenu.style.top = buttonTopPosition;
    exportDatasetMenu.style.left = buttonLeftPosition;
  };

  const layout = children => {
    return (
      <MainLayout>
        <Growl ref={growlRef} />
        <BreadCrumb model={breadCrumbItems} home={home} />
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

  const checkIsWebFormMMR = datasetName => {
    const mmrDatasetName = 'MMR_TEST';
    if (datasetName.toString().toLowerCase() === mmrDatasetName.toString().toLowerCase()) {
      setIsInputSwitchChecked(true);
      setIsWebFormMMR(true);
    } else {
      setIsWebFormMMR(false);
    }
  };

  const isWebForm = () => {
    if (isInputSwitchChecked) {
      return <WebFormData datasetId={datasetId} tableSchemaId={tableSchemaId} />;
    } else {
      return (
        <SnapshotContext.Provider
          value={{
            snapshotState: snapshotState,
            snapshotDispatch: snapshotDispatch
          }}>
          <TabsSchema
            activeIndex={activeIndex}
            onTabChange={tableSchemaId => onTabChange(tableSchemaId)}
            recordPositionId={recordPositionId}
            selectedRecordErrorId={selectedRecordErrorId}
            tables={tableSchema}
            tableSchemaColumns={tableSchemaColumns}
            isWebFormMMR={isWebFormMMR}
            hasWritePermissions={hasWritePermissions}
          />
        </SnapshotContext.Provider>
      );
    }
  };

  if (loading) {
    return layout(<Spinner />);
  }

  return layout(
    <div>
      <Title title={`${resources.messages['titleDataset']}${datasetTitle}`} />
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
              disabled={false}
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
              disabled={!hasWritePermissions || isWebFormMMR}
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
              disabled={isWebFormMMR}
              icon={'dashboard'}
              label={resources.messages['dashboards']}
              onClick={() => onSetVisible(setDashDialogVisible, true)}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={!hasWritePermissions}
              icon={'camera'}
              label={resources.messages['snapshots']}
              onClick={() => {
                onSetVisible(setSnapshotIsVisible, true);
                onLoadSnapshotList();
              }}
            />
          </div>
        </Toolbar>
      </div>

      <ReporterDatasetContext.Provider
        value={{
          validationsVisibleHandler: null,
          onSelectValidation: (tableSchemaId, posIdRecord, selectedRecordErrorId) => {
            setActiveIndex(tableSchemaId);
            setRecordPositionId(posIdRecord);
            setSelectedRecordErrorId(selectedRecordErrorId);
          }
        }}>
        {showWebFormInputSwitch()}
        {isWebForm()}
      </ReporterDatasetContext.Provider>

      <Dialog
        dismissableMask={true}
        header={resources.messages['titleDashboard']}
        onHide={() => onSetVisible(setDashDialogVisible, false)}
        style={{ width: '70vw' }}
        visible={dashDialogVisible}>
        <Dashboard refresh={dashDialogVisible} />
      </Dialog>

      <ReporterDatasetContext.Provider
        value={{
          onValidationsVisible: () => {
            onSetVisible(setValidationsVisible, false);
          },
          onSelectValidation: (tableSchemaId, posIdRecord, selectedRecordErrorId) => {
            setActiveIndex(tableSchemaId);
            setRecordPositionId(posIdRecord);
            setSelectedRecordErrorId(selectedRecordErrorId);
          }
        }}>
        <Dialog
          dismissableMask={true}
          header={resources.messages['titleValidations']}
          maximizable
          onHide={() => onSetVisible(setValidationsVisible, false)}
          style={{ width: '80%' }}
          visible={validationsVisible}
          className={styles.paginatorValidationViewer}>
          <ValidationViewer
            datasetId={datasetId}
            datasetName={datasetTitle}
            visible={validationsVisible}
            hasWritePermissions={hasWritePermissions}
            tableSchemaNames={tableSchemaNames}
          />
        </Dialog>
      </ReporterDatasetContext.Provider>

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

      <SnapshotContext.Provider
        value={{
          snapshotState: snapshotState,
          snapshotDispatch: snapshotDispatch
        }}>
        <SnapshotSlideBar
          isVisible={snapshotIsVisible}
          isLoadingSnapshotListData={isLoadingSnapshotListData}
          setIsVisible={setSnapshotIsVisible}
          setSnapshotDialogVisible={setSnapshotDialogVisible}
          snapshotListData={snapshotListData}
        />

        <ConfirmDialog
          className={styles.snapshotDialog}
          header={snapshotState.dialogMessage}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          maximizable={false}
          onConfirm={snapshotState.action}
          onHide={() => onSetVisible(setSnapshotDialogVisible, false)}
          showHeader={false}
          visible={snapshotDialogVisible}>
          <ul>
            <li>
              <strong>{resources.messages.creationDate}: </strong>
              {moment(snapshotState.creationDate).format('DD/MM/YYYY HH:mm:ss')}
            </li>
            <li>
              <strong>{resources.messages.description}: </strong>
              {snapshotState.description}
            </li>
          </ul>
        </ConfirmDialog>
      </SnapshotContext.Provider>
    </div>
  );
});
