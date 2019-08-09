/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useReducer } from 'react';
import moment from 'moment';

import styles from './ReporterDataSet.module.css';

import { config } from 'assets/conf';

import { BreadCrumb } from 'primereact/breadcrumb';
import { ButtonsBar } from 'ui/views/_components/ButtonsBar';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dashboard } from './_components/Dashboard';
import { Dialog } from 'primereact/dialog';
import { MainLayout } from 'ui/views/_components/Layout';
import { ReporterDataSetContext } from './_components/_context/ReporterDataSetContext';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { SnapshotSlideBar } from './_components/SnapshotSlideBar';
import { TabsSchema } from './_components/TabsSchema';
import { Title } from './_components/Title';
import { ValidationViewer } from './_components/ValidationViewer';

import { SnapshotService } from 'core/services/Snapshot';

import { getUrl } from 'core/infrastructure/getUrl';

import { DataSetService } from 'core/services/DataSet';

import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const SnapshotContext = React.createContext();

export const ReporterDataSet = ({ match, history }) => {
  const {
    params: { dataFlowId, dataSetId }
  } = match;
  const resources = useContext(ResourcesContext);
  const [activeIndex, setActiveIndex] = useState();
  const [buttons, setButtons] = useState([]);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dashDialogVisible, setDashDialogVisible] = useState(false);
  const [datasetTitle, setDatasetTitle] = useState('');
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [isDataDeleted, setIsDataDeleted] = useState(false);
  const [recordPositionId, setRecordPositionId] = useState(0);
  const [selectedRowId, setSelectedRowId] = useState(-1);
  const [snapshotDialogVisible, setSnapshotDialogVisible] = useState(false);
  const [snapshotIsVisible, setSnapshotIsVisible] = useState(false);
  const [snapshotListData, setSnapshotListData] = useState([]);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();
  const [validateDialogVisible, setValidateDialogVisible] = useState(false);
  const [validationsVisible, setValidationsVisible] = useState(false);

  const home = {
    icon: resources.icons['home'],
    command: () => history.push('/')
  };

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
  }, []);

  useEffect(() => {
    onLoadDataSetSchema();
  }, [isDataDeleted]);

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

  const onLoadSnapshotList = async () => {
    setSnapshotListData(await SnapshotService.all(dataSetId));
  };

  const onLoadDataSetSchema = async () => {
    const dataSetSchema = await DataSetService.dataSetSchemaById(dataFlowId);
    const dataSetStatistics = await DataSetService.errorStatisticsById(dataSetId);

    setDatasetTitle(dataSetSchema.dataSetSchemaName);
    setTableSchema(
      dataSetSchema.tables.map((field, i) => {
        return {
          id: field['tableSchemaId'],
          name: field['tableSchemaName'],
          hasErrors: {
            ...dataSetStatistics.tables.filter(table => table['tableSchemaId'] === field['tableSchemaId'])[0]
          }.tableErrors
        };
      })
    );

    setTableSchemaColumns(
      dataSetSchema.tables.map(table => {
        return table.records[0].fields.map(field => {
          return {
            table: table['tableSchemaName'],
            field: field['fieldId'],
            header: `${field['name'].charAt(0).toUpperCase()}${field['name'].slice(1)}`
          };
        });
      })
    );

    setButtons([
      {
        label: resources.messages['export'],
        icon: '1',
        group: 'left',
        disabled: true,
        onClick: null
      },
      {
        label: resources.messages['deleteDatasetData'],
        icon: '2',
        group: 'left',
        disabled: false,
        onClick: () => onSetVisible(setDeleteDialogVisible, true)
      },
      {
        label: resources.messages['events'],
        icon: '4',
        group: 'right',
        disabled: true,
        onClick: null
      },
      {
        label: resources.messages['validate'],
        icon: '10',
        group: 'right',
        disabled: false,
        //!validationError,
        onClick: () => onSetVisible(setValidateDialogVisible, true),
        ownButtonClasses: null,
        iconClasses: null
      },
      {
        label: resources.messages['showValidations'],
        icon: '3',
        group: 'right',
        disabled: !dataSetStatistics.datasetErrors,
        onClick: () => onSetVisible(setValidationsVisible, true),
        ownButtonClasses: null,
        iconClasses: dataSetStatistics.datasetErrors ? 'warning' : ''
      },
      {
        //title: "Dashboards",
        label: resources.messages['dashboards'],
        icon: '5',
        group: 'right',
        disabled: false,
        onClick: () => onSetVisible(setDashDialogVisible, true)
      },
      {
        label: resources.messages['snapshots'],
        icon: '12',
        group: 'right',
        disabled: false,
        onClick: () => setSnapshotIsVisible(true)
      }
    ]);
  };

  const onSetVisible = (fnUseState, visible) => {
    fnUseState(visible);
  };

  const onTabChange = idTableSchema => {
    setActiveIndex(idTableSchema.index);
    setSelectedRowId(-1);
    setRecordPositionId(0);
  };

  const onRestoreSnapshot = async () => {
    const snapshotRestored = await SnapshotService.restoreById(dataFlowId, dataSetId, snapshotState.snapShotId);
    if (snapshotRestored) {
      onLoadSnapshotList();
    }
    onSetVisible(setSnapshotDialogVisible, false);
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
      // break;
    }
  };

  const [snapshotState, snapshotDispatch] = useReducer(snapshotReducer, snapshotInitialState);

  return (
    <MainLayout>
      <BreadCrumb model={breadCrumbItems} home={home} />

      <div className="rep-container">
        <div className="titleDiv">
          <Title title={`${resources.messages['titleDataset']}${datasetTitle}`} />
        </div>
        <div className={styles.ButtonsBar}>
          <ButtonsBar buttonsList={buttons} />
        </div>
        <ReporterDataSetContext.Provider
          value={{
            onValidationsVisible: null,
            onSetTab: null,
            onSetPage: recordPositionId => {
              setRecordPositionId(recordPositionId);
            },
            onSetSelectedRowId: selectedRowId => {
              setSelectedRowId(selectedRowId);
            }
          }}>
          <TabsSchema
            activeIndex={activeIndex}
            onTabChange={idTableSchema => onTabChange(idTableSchema)}
            recordPositionId={recordPositionId}
            selectedRowId={selectedRowId}
            tables={tableSchema}
            tableSchemaColumns={tableSchemaColumns}
            urlViewer={`${config.dataviewerAPI.url}${dataSetId}`}
          />
        </ReporterDataSetContext.Provider>
        <Dialog
          dismissableMask={true}
          header={resources.messages['titleDashboard']}
          maximizable
          onHide={() => onSetVisible(setDashDialogVisible, false)}
          style={{ width: '80%' }}
          visible={dashDialogVisible}>
          <Dashboard refresh={dashDialogVisible} />
        </Dialog>
        <ReporterDataSetContext.Provider
          value={{
            onValidationsVisible: () => {
              onSetVisible(setValidationsVisible, false);
            },
            onSetTab: idTableSchema => {
              setActiveIndex(idTableSchema);
            },
            onSetPage: recordPositionId => {
              setRecordPositionId(recordPositionId);
            },
            onSetSelectedRowId: selectedRowId => {
              setSelectedRowId(selectedRowId);
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
            onLoadSnapshotList={onLoadSnapshotList}
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
    </MainLayout>
  );
};
