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
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const SnapshotContext = React.createContext();

export const ReporterDataSet = ({ match, history }) => {
  const {
    params: { dataFlowId, dataSetId }
  } = match;
  const resources = useContext(ResourcesContext);
  const [datasetTitle, setDatasetTitle] = useState('');
  const [customButtons, setCustomButtons] = useState([]);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();
  const [dashDialogVisible, setDashDialogVisible] = useState(false);
  const [validationsVisible, setValidationsVisible] = useState(false);
  const [validateDialogVisible, setValidateDialogVisible] = useState(false);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [isDataDeleted, setIsDataDeleted] = useState(false);
  const [activeIndex, setActiveIndex] = useState();
  const [positionIdRecord, setPositionIdRecord] = useState(0);
  const [idSelectedRow, setIdSelectedRow] = useState(-1);
  const [snapshotDialogVisible, setSnapshotDialogVisible] = useState(false);
  const [snapshotIsVisible, setSnapshotIsVisible] = useState(false);
  const [snapshotListData, setSnapshotListData] = useState([]);

  const home = {
    icon: resources.icons['home'],
    command: () => history.push('/')
  };

  useEffect(() => {
    console.log('ReporterDataSet useEffect');

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

    const dataPromise = HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/datosDataSchema2.json'
        : getUrl(config.dataSchemaAPI.url, { dataFlowId }),
      queryString: {}
    });
    dataPromise
      .then(response => {
        const dataPromiseError = HTTPRequester.get({
          url: window.env.REACT_APP_JSON
            ? '/jsons/error-statistics.json'
            : `${config.loadStatisticsAPI.url}${dataSetId}`,
          queryString: {}
        });

        //Parse JSON to array statistic values
        dataPromiseError.then(res => {
          setDatasetTitle(res.data.nameDataSetSchema);
          setTableSchema(
            response.data.tableSchemas.map((item, i) => {
              return {
                id: item['idTableSchema'],
                name: item['nameTableSchema'],
                hasErrors: {
                  ...res.data.tables.filter(t => t['idTableSchema'] === item['idTableSchema'])[0]
                }.tableErrors
              };
            })
          );

          setTableSchemaColumns(
            response.data.tableSchemas.map(table => {
              return table.recordSchema.fieldSchema.map(item => {
                return {
                  table: table['nameTableSchema'],
                  field: item['id'],
                  header: `${item['name'].charAt(0).toUpperCase()}${item['name'].slice(1)}`
                };
              });
            })
          );

          //#region Button inicialization

          setCustomButtons([
            {
              label: resources.messages['export'],
              icon: '1',
              group: 'left',
              disabled: true,
              clickHandler: null
            },
            {
              label: resources.messages['deleteDatasetData'],
              icon: '2',
              group: 'left',
              disabled: false,
              clickHandler: () => setVisibleHandler(setDeleteDialogVisible, true)
            },
            {
              label: resources.messages['events'],
              icon: '4',
              group: 'right',
              disabled: true,
              clickHandler: null
            },
            {
              label: resources.messages['validate'],
              icon: '10',
              group: 'right',
              disabled: false,
              //!validationError,
              clickHandler: () => setVisibleHandler(setValidateDialogVisible, true),
              ownButtonClasses: null,
              iconClasses: null
            },
            {
              label: resources.messages['showValidations'],
              icon: '3',
              group: 'right',
              disabled: !res.data.datasetErrors,
              clickHandler: () => setVisibleHandler(setValidationsVisible, true),
              ownButtonClasses: null,
              iconClasses: response.data.datasetErrors ? 'warning' : ''
            },
            {
              //title: "Dashboards",
              label: resources.messages['dashboards'],
              icon: '5',
              group: 'right',
              disabled: false,
              clickHandler: () => setVisibleHandler(setDashDialogVisible, true)
            },
            {
              label: resources.messages['snapshots'],
              icon: '12',
              group: 'right',
              disabled: false,
              clickHandler: () => setSnapshotIsVisible(true)
            }
          ]);
          //#endregion Button inicialization
        });
      })
      .catch(error => {
        console.log(error);
        return error;
      });
  }, [isDataDeleted]);

  const setVisibleHandler = (fnUseState, visible) => {
    fnUseState(visible);
  };

  const onConfirmDeleteHandler = () => {
    setDeleteDialogVisible(false);
    HTTPRequester.delete({
      url: window.env.REACT_APP_JSON
        ? `/dataset/${dataSetId}/deleteImportData`
        : `/dataset/${dataSetId}/deleteImportData`,
      queryString: {}
    }).then(res => {
      setIsDataDeleted(true);
    });
  };

  const onConfirmValidateHandler = () => {
    setValidateDialogVisible(false);
    HTTPRequester.update({
      url: window.env.REACT_APP_JSON ? '/jsons/list-of-errors.json' : `/validation/dataset/${dataSetId}`,
      queryString: {}
    });
  };

  const onTabChangeHandler = idTableSchema => {
    setActiveIndex(idTableSchema.index);
    setIdSelectedRow(-1);
    setPositionIdRecord(0);
  };

  const setSnapshotList = async () => {
    setSnapshotListData(await SnapshotService.all(getUrl(config.loadSnapshotsListAPI.url, { dataSetId })));
  };

  const createSnapshot = () => {
    HTTPRequester.post({
      url: getUrl(config.createSnapshot.url, {
        // dataFlowId,
        dataSetId,
        snapshotDescription: snapshotState.description
      }),
      data: {
        // date: snapshotState.createdAt,
        description: snapshotState.description
      }
    })
      .then(response => {
        setSnapshotList();
      })
      .catch(error => {});
    setVisibleHandler(setSnapshotDialogVisible, false);
  };

  const restoreSnapshot = () => {
    HTTPRequester.update({
      url: getUrl(config.restoreSnaphost.url, {
        dataFlowId,
        dataSetId,
        snapshotId: snapshotState.snapShotId
      })
    })
      .then(response => {
        setSnapshotList();
        console.log('restoreSnapshot response', response);
      })
      .catch(error => {
        console.log('restoreSnapshot error', error);
      });
    setVisibleHandler(setSnapshotDialogVisible, false);
  };

  const deleteSnapshot = () => {
    HTTPRequester.delete({
      url: getUrl(config.deleteSnapshotByID.url, {
        // dataFlowId,
        dataSetId,
        snapshotId: snapshotState.snapShotId
      })
    })
      .then(response => {
        setSnapshotList();
        console.log('deleteSnapshot response', response);
      })
      .catch(error => {
        console.error('deleteSnapshot error', error);
      });
    setVisibleHandler(setSnapshotDialogVisible, false);
  };

  const snapshotInitialStateObj = {
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
        setVisibleHandler(setSnapshotDialogVisible, true);
        return {
          ...state,
          snapShotId: '',
          creationDate: Date.now(),
          description: payload.description,
          dialogMessage: resources.messages.createSnapshotMessage,
          action: createSnapshot
        };

      case 'delete_snapshot':
        setVisibleHandler(setSnapshotDialogVisible, true);
        return {
          ...state,
          snapShotId: payload.id,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogMessage: resources.messages.deleteSnapshotMessage,
          action: deleteSnapshot
        };

      case 'restore_snapshot':
        setVisibleHandler(setSnapshotDialogVisible, true);
        return {
          ...state,
          snapShotId: payload.id,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogMessage: resources.messages.restoreSnapshotMessage,
          action: restoreSnapshot
        };
      default:
        return state;
      // break;
    }
  };

  const [snapshotState, snapshotDispatch] = useReducer(snapshotReducer, snapshotInitialStateObj);

  return (
    <MainLayout>
      <BreadCrumb model={breadCrumbItems} home={home} />

      <div className="rep-container">
        <div className="titleDiv">
          <Title title={`${resources.messages['titleDataset']}${datasetTitle}`} />
        </div>
        <div className={styles.ButtonsBar}>
          <ButtonsBar buttonsList={customButtons} />
        </div>
        <ReporterDataSetContext.Provider
          value={{
            validationsVisibleHandler: null,
            setTabHandler: null,
            setPageHandler: posIdRecord => {
              setPositionIdRecord(posIdRecord);
            },
            setIdSelectedRowHandler: selectedRowId => {
              setIdSelectedRow(selectedRowId);
            }
          }}>
          <TabsSchema
            tables={tableSchema}
            tableSchemaColumns={tableSchemaColumns}
            urlViewer={`${config.dataviewerAPI.url}${dataSetId}`}
            activeIndex={activeIndex}
            positionIdRecord={positionIdRecord}
            onTabChangeHandler={idTableSchema => onTabChangeHandler(idTableSchema)}
            idSelectedRow={idSelectedRow}
          />
        </ReporterDataSetContext.Provider>
        <Dialog
          visible={dashDialogVisible}
          onHide={() => setVisibleHandler(setDashDialogVisible, false)}
          header={resources.messages['titleDashboard']}
          maximizable
          dismissableMask={true}
          style={{ width: '80%' }}>
          <Dashboard refresh={dashDialogVisible} />
        </Dialog>
        {/* TODO: ¿Merece la pena utilizar ContextAPI a un único nivel? */}
        <ReporterDataSetContext.Provider
          value={{
            validationsVisibleHandler: () => {
              setVisibleHandler(setValidationsVisible, false);
            },
            setTabHandler: idTableSchema => {
              setActiveIndex(idTableSchema);
            },
            setPageHandler: posIdRecord => {
              setPositionIdRecord(posIdRecord);
            },
            setIdSelectedRowHandler: selectedRowId => {
              setIdSelectedRow(selectedRowId);
            }
          }}>
          <Dialog
            visible={validationsVisible}
            onHide={() => setVisibleHandler(setValidationsVisible, false)}
            header={resources.messages['titleValidations']}
            maximizable
            dismissableMask={true}
            style={{ width: '80%' }}>
            <ValidationViewer dataSetId={dataSetId} visible={validationsVisible} />
          </Dialog>
        </ReporterDataSetContext.Provider>
        <ConfirmDialog
          onConfirm={onConfirmDeleteHandler}
          onHide={() => setVisibleHandler(setDeleteDialogVisible, false)}
          visible={deleteDialogVisible}
          header={resources.messages['deleteDatasetHeader']}
          maximizable={false}
          labelConfirm={resources.messages['yes']}
          labelCancel={resources.messages['no']}>
          {resources.messages['deleteDatasetConfirm']}
        </ConfirmDialog>
        <ConfirmDialog
          onConfirm={onConfirmValidateHandler}
          onHide={() => setVisibleHandler(setValidateDialogVisible, false)}
          visible={validateDialogVisible}
          header={resources.messages['validateDataSet']}
          maximizable={false}
          labelConfirm={resources.messages['yes']}
          labelCancel={resources.messages['no']}>
          {resources.messages['validateDataSetConfirm']}
        </ConfirmDialog>

        <SnapshotContext.Provider
          value={{
            snapshotState: snapshotState,
            snapshotDispatch: snapshotDispatch
          }}>
          <SnapshotSlideBar
            dataSetId={dataSetId}
            isVisible={snapshotIsVisible}
            setIsVisible={setSnapshotIsVisible}
            setSnapshotDialogVisible={setSnapshotDialogVisible}
            setVisibleHandler={setVisibleHandler}
            setSnapshotList={setSnapshotList}
            snapshotListData={snapshotListData}
          />
          <ConfirmDialog
            onConfirm={snapshotState.action}
            className={styles.snapshotDialog}
            onHide={() => setVisibleHandler(setSnapshotDialogVisible, false)}
            visible={snapshotDialogVisible}
            showHeader={false}
            maximizable={false}
            labelConfirm={resources.messages['yes']}
            labelCancel={resources.messages['no']}
            header={snapshotState.dialogMessage}>
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
