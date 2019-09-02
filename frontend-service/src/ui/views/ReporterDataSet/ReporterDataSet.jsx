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
import { MainLayout } from 'ui/views/_components/Layout';
import { Menu } from 'primereact/menu';
import { ReporterDataSetContext } from './_components/_context/ReporterDataSetContext';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { SnapshotSlideBar } from './_components/SnapshotSlideBar';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsSchema } from './_components/TabsSchema';
import { Title } from './_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { ValidationViewer } from './_components/ValidationViewer';

import { DataSetService } from 'core/services/DataSet';
import { SnapshotService } from 'core/services/Snapshot';
import { getUrl } from 'core/infrastructure/api/getUrl';

export const SnapshotContext = React.createContext();

export const ReporterDataSet = withRouter(({ match, history }) => {
  const {
    params: { dataFlowId, dataSetId }
  } = match;
  const resources = useContext(ResourcesContext);
  const [activeIndex, setActiveIndex] = useState();
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dashDialogVisible, setDashDialogVisible] = useState(false);
  const [datasetTitle, setDatasetTitle] = useState('');
  const [datasetHasErrors, setDatasetHasErrors] = useState(false);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [exportButtonsList, setExportButtonsList] = useState([]);
  const [exportDataSetData, setExportDataSetData] = useState(undefined);
  const [exportDataSetDataName, setExportDataSetDataName] = useState('');
  const [isDataDeleted, setIsDataDeleted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadingFile, setLoadingFile] = useState(false);
  const [recordPositionId, setRecordPositionId] = useState(-1);
  const [selectedRecordErrorId, setSelectedRecordErrorId] = useState(-1);
  const [snapshotDialogVisible, setSnapshotDialogVisible] = useState(false);
  const [snapshotIsVisible, setSnapshotIsVisible] = useState(false);
  const [snapshotListData, setSnapshotListData] = useState([]);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();
  const [validateDialogVisible, setValidateDialogVisible] = useState(false);
  const [validationsVisible, setValidationsVisible] = useState(false);

  let exportMenuRef = useRef();

  const home = {
    icon: config.icons['home'],
    command: () => history.push('/')
  };

  useEffect(() => {
    onLoadDataSetSchema();
  }, [isDataDeleted]);

  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataFlowTask'],
        command: () => history.push('/data-flow-task')
      },
      {
        label: resources.messages['reportingDataFlow'],
        command: () => history.push(`/reporting-data-flow/${match.params.dataFlowId}`)
      },
      { label: resources.messages['viewData'] }
    ]);
    onLoadSnapshotList();
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
    if (!isUndefined(exportDataSetData)) {
      DownloadFile(exportDataSetData, exportDataSetDataName);
    }
  }, [exportDataSetData]);

  const onConfirmDelete = async () => {
    setDeleteDialogVisible(false);
    const dataDeleted = await DataSetService.deleteDataById(dataSetId);
    if (dataDeleted) {
      setIsDataDeleted(true);
    }
  };

  const onConfirmValidate = async () => {
    setValidateDialogVisible(false);
    await DataSetService.validateDataById(dataSetId);
  };

  const onCreateSnapshot = async () => {
    const snapshotCreated = await SnapshotService.createById(dataSetId, snapshotState.description);
    if (snapshotCreated) {
      onLoadSnapshotList();
    }
    onSetVisible(setSnapshotDialogVisible, false);
  };

  const onDeleteSnapshot = async () => {
    const snapshotDeleted = await SnapshotService.deleteById(dataSetId, snapshotState.snapShotId);
    if (snapshotDeleted) {
      onLoadSnapshotList();
    }
    onSetVisible(setSnapshotDialogVisible, false);
  };

  const onExportData = async fileType => {
    setLoadingFile(true);
    try {
      setExportDataSetDataName(createFileName(datasetTitle, fileType));
      setExportDataSetData(await DataSetService.exportDataById(dataSetId, fileType));
    } catch (error) {
      console.error(error);
    } finally {
      setLoadingFile(false);
    }
  };

  const onReleaseSnapshot = async () => {
    const snapshotReleased = await SnapshotService.releaseById(dataFlowId, dataSetId, snapshotState.snapShotId);
    if (snapshotReleased) {
      onLoadSnapshotList();
    }
    onSetVisible(setSnapshotDialogVisible, false);
  };

  const onRestoreSnapshot = async () => {
    const snapshotRestored = await SnapshotService.restoreById(dataFlowId, dataSetId, snapshotState.snapShotId);
    if (snapshotRestored) {
      onLoadSnapshotList();
    }
    onSetVisible(setSnapshotDialogVisible, false);
  };

  const onLoadSnapshotList = async () => {
    setSnapshotListData(await SnapshotService.all(dataSetId));
  };

  const onLoadDataSetSchema = async () => {
    try {
      const dataSetSchema = await DataSetService.schemaById(dataFlowId);
      const dataSetStatistics = await DataSetService.errorStatisticsById(dataSetId);
      setDatasetTitle(dataSetStatistics.dataSetSchemaName);
      setTableSchema(
        dataSetSchema.tables.map(tableSchema => {
          return {
            id: tableSchema['tableSchemaId'],
            name: tableSchema['tableSchemaName'],
            hasErrors: {
              ...dataSetStatistics.tables.filter(table => table['tableSchemaId'] === tableSchema['tableSchemaId'])[0]
            }.hasErrors
          };
        })
      );

      setTableSchemaColumns(
        dataSetSchema.tables.map(table => {
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

      setDatasetHasErrors(dataSetStatistics.datasetErrors);
    } catch (error) {
      const errorResponse = error.response;
      if (
        !isUndefined(errorResponse) &&
        (errorResponse.data.message.includes('401') || errorResponse.data.message.includes('403'))
      ) {
        history.push(getUrl(config.REPORTING_DATAFLOW.url, { dataFlowId }));
      }
    }

    setLoading(false);
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

  const snapshotInitialState = {
    apiCall: '',
    createdAt: '',
    description: '',
    dialogMessage: '',
    dataFlowId,
    dataSetId,
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
      default:
        return state;
    }
  };

  const [snapshotState, snapshotDispatch] = useReducer(snapshotReducer, snapshotInitialState);

  const getPosition = button => {
    const buttonTopPosition = button.top;
    const buttonLeftPosition = button.left;

    const exportDataSetMenu = document.getElementById('exportDataSetMenu');
    exportDataSetMenu.style.top = buttonTopPosition;
    exportDataSetMenu.style.left = buttonLeftPosition;
  };

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
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
              disabled={false}
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
              disabled={false}
              icon={'validate'}
              label={resources.messages['validate']}
              onClick={() => onSetVisible(setValidateDialogVisible, true)}
              ownButtonClasses={null}
              iconClasses={null}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={!datasetHasErrors}
              icon={'warning'}
              label={resources.messages['showValidations']}
              onClick={() => onSetVisible(setValidationsVisible, true)}
              ownButtonClasses={null}
              iconClasses={datasetHasErrors ? 'warning' : ''}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={false}
              icon={'dashboard'}
              label={resources.messages['dashboards']}
              onClick={() => onSetVisible(setDashDialogVisible, true)}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={false}
              icon={'camera'}
              label={resources.messages['snapshots']}
              onClick={() => onSetVisible(setSnapshotIsVisible, true)}
            />
          </div>
        </Toolbar>
      </div>
      <ReporterDataSetContext.Provider
        value={{
          validationsVisibleHandler: null,
          onSelectValidation: (tableSchemaId, posIdRecord, selectedRecordErrorId) => {
            setActiveIndex(tableSchemaId);
            setRecordPositionId(posIdRecord);
            setSelectedRecordErrorId(selectedRecordErrorId);
          }
        }}>
        <TabsSchema
          activeIndex={activeIndex}
          onTabChange={tableSchemaId => onTabChange(tableSchemaId)}
          recordPositionId={recordPositionId}
          selectedRecordErrorId={selectedRecordErrorId}
          tables={tableSchema}
          tableSchemaColumns={tableSchemaColumns}
        />
      </ReporterDataSetContext.Provider>
      <Dialog
        dismissableMask={true}
        header={resources.messages['titleDashboard']}
        onHide={() => onSetVisible(setDashDialogVisible, false)}
        style={{ width: '70vw' }}
        visible={dashDialogVisible}>
        <Dashboard refresh={dashDialogVisible} />
      </Dialog>
      <ReporterDataSetContext.Provider
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
          visible={validationsVisible}>
          <ValidationViewer dataSetId={dataSetId} visible={validationsVisible} />
        </Dialog>
      </ReporterDataSetContext.Provider>
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
        header={resources.messages['validateDataSet']}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        maximizable={false}
        onConfirm={onConfirmValidate}
        onHide={() => onSetVisible(setValidateDialogVisible, false)}
        visible={validateDialogVisible}>
        {resources.messages['validateDataSetConfirm']}
      </ConfirmDialog>

      <SnapshotContext.Provider
        value={{
          snapshotState: snapshotState,
          snapshotDispatch: snapshotDispatch
        }}>
        <SnapshotSlideBar
          isVisible={snapshotIsVisible}
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
