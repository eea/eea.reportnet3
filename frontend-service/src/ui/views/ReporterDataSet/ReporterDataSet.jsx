/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useReducer } from 'react';
import moment from 'moment';
import { withRouter } from 'react-router-dom';

import styles from './ReporterDataSet.module.css';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { ButtonsBar } from 'ui/views/_components/ButtonsBar';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dashboard } from './_components/Dashboard';
import { Dialog } from 'ui/views/_components/Dialog';
import { MainLayout } from 'ui/views/_components/Layout';
import { ReporterDataSetContext } from './_components/_context/ReporterDataSetContext';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { SnapshotSlideBar } from './_components/SnapshotSlideBar';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsSchema } from './_components/TabsSchema';
import { Title } from './_components/Title';
import { ValidationViewer } from './_components/ValidationViewer';

import { DataSetService } from 'core/services/DataSet';
import { SnapshotService } from 'core/services/Snapshot';

export const SnapshotContext = React.createContext();

export const ReporterDataSet = withRouter(({ match, history }) => {
  const {
    params: { dataFlowId, dataSetId }
  } = match;
  const resources = useContext(ResourcesContext);
  const [activeIndex, setActiveIndex] = useState();
  const [buttonsList, setButtonsList] = useState([]);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dashDialogVisible, setDashDialogVisible] = useState(false);
  const [datasetTitle, setDatasetTitle] = useState('');
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [isDataDeleted, setIsDataDeleted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [recordPositionId, setRecordPositionId] = useState(-1);
  const [selectedRowId, setSelectedRowId] = useState(-1);
  const [snapshotDialogVisible, setSnapshotDialogVisible] = useState(false);
  const [snapshotIsVisible, setSnapshotIsVisible] = useState(false);
  const [snapshotListData, setSnapshotListData] = useState([]);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();
  const [validateDialogVisible, setValidateDialogVisible] = useState(false);
  const [validationsVisible, setValidationsVisible] = useState(false);

  const home = {
    icon: config.icons['home'],
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
    onLoadSnapshotList();
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
    const dataSetSchema = await DataSetService.schemaById(dataFlowId);
    const dataSetStatistics = await DataSetService.errorStatisticsById(dataSetId);

    setDatasetTitle(dataSetSchema.dataSetSchemaName);
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
            header: `${field['name'].charAt(0).toUpperCase()}${field['name'].slice(1)}`
          };
        });
      })
    );

    setButtonsList([
      {
        label: resources.messages['export'],
        icon: 'import',
        group: 'left',
        disabled: true,
        onClick: null
      },
      {
        label: resources.messages['deleteDatasetData'],
        icon: 'trash',
        group: 'left',
        disabled: false,
        onClick: () => onSetVisible(setDeleteDialogVisible, true)
      },
      {
        label: resources.messages['events'],
        icon: 'clock',
        group: 'right',
        disabled: true,
        onClick: null
      },
      {
        label: resources.messages['validate'],
        icon: 'validate',
        group: 'right',
        disabled: false,
        onClick: () => onSetVisible(setValidateDialogVisible, true),
        ownButtonClasses: null,
        iconClasses: null
      },
      {
        label: resources.messages['showValidations'],
        icon: 'warning',
        group: 'right',
        disabled: !dataSetStatistics.datasetErrors,
        onClick: () => onSetVisible(setValidationsVisible, true),
        ownButtonClasses: null,
        iconClasses: dataSetStatistics.datasetErrors ? 'warning' : ''
      },
      {
        label: resources.messages['dashboards'],
        icon: 'dashboard',
        group: 'right',
        disabled: false,
        onClick: () => onSetVisible(setDashDialogVisible, true)
      },
      {
        label: resources.messages['snapshots'],
        icon: 'camera',
        group: 'right',
        disabled: false,
        onClick: () => onSetVisible(setSnapshotIsVisible, true)
      }
    ]);
    setLoading(false);
  };

  const onSetVisible = (fnUseState, visible) => {
    fnUseState(visible);
  };

  const onTabChange = tableSchemaId => {
    setActiveIndex(tableSchemaId.index);
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
    }
  };

  const [snapshotState, snapshotDispatch] = useReducer(snapshotReducer, snapshotInitialState);

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
        <ButtonsBar buttonsList={buttonsList} />
      </div>
      <ReporterDataSetContext.Provider
        value={{
          validationsVisibleHandler: null,
          onSelectValidation: (tableSchemaId, posIdRecord, selectedRowId) => {
            setActiveIndex(tableSchemaId);
            setRecordPositionId(posIdRecord);
            setSelectedRowId(selectedRowId);
          }
        }}>
        <TabsSchema
          activeIndex={activeIndex}
          onTabChange={tableSchemaId => onTabChange(tableSchemaId)}
          recordPositionId={recordPositionId}
          selectedRowId={selectedRowId}
          tables={tableSchema}
          tableSchemaColumns={tableSchemaColumns}
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
          onSelectValidation: (tableSchemaId, posIdRecord, selectedRowId) => {
            setActiveIndex(tableSchemaId);
            setRecordPositionId(posIdRecord);
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
