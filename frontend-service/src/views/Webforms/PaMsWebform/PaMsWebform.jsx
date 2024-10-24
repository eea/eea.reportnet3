import { Fragment, useContext, useEffect, useReducer } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniqueId from 'lodash/uniqueId';

import styles from './PaMsWebform.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Button } from 'views/_components/Button';
import { TableManagement } from './_components/TableManagement';
import { WebformView } from './_components/WebformView';

import { DatasetService } from 'services/DatasetService';
import { WebformService } from 'services/WebformService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { paMsWebformReducer } from './_functions/Reducers/paMsWebformReducer';

import { MetadataUtils } from 'views/_functions/Utils';
import { PaMsWebformUtils } from './_functions/Utils/PaMsWebformUtils';
import { WebformsUtils } from 'views/Webforms/_functions/Utils/WebformsUtils';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const PaMsWebform = ({
  bigData,
  dataflowId,
  dataProviderId,
  datasetId,
  isIcebergCreated,
  isReleasing,
  isReporting,
  overview,
  state,
  tables = []
}) => {
  const { checkErrors, getFieldSchemaId, getTypeList, hasErrors, parseListOfSinglePams } = PaMsWebformUtils;
  const { datasetSchema, datasetStatistics } = state;
  const { onParseWebformData, onParseWebformRecords, parseNewTableRecord, parsePamsRecords } = WebformsUtils;

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [paMsWebformState, paMsWebformDispatch] = useReducer(paMsWebformReducer, {
    data: [],
    hasErrors: true,
    isAddingGroupRecord: false,
    isAddingSingleRecord: false,
    isDataUpdated: false,
    isLoading: true,
    isRefresh: false,
    pamsRecords: [],
    selectedTable: { fieldSchemaId: null, pamsId: null, recordId: null, tableName: null },
    selectedTableName: null,
    selectedTableSchemaId: null,
    tableList: { group: [], single: [] },
    view: 'overview'
  });
  const { isDataUpdated, isLoading, pamsRecords, selectedTable, selectedTableName, tableList, view } = paMsWebformState;

  useEffect(() => initialLoad(), [tables]);

  useEffect(() => {
    if (!isEmpty(paMsWebformState.data)) {
      onLoadPamsData();
      paMsWebformDispatch({ type: 'HAS_ERRORS', payload: { value: hasErrors(paMsWebformState.data) } });
    }
  }, [paMsWebformState.data, isDataUpdated]);

  useEffect(() => {
    setIsAddingSingleRecord(false);
    setIsAddingGroupRecord(false);
  }, [tableList]);

  useEffect(() => {
    const { fieldId, fieldSchema } = getFieldSchemaId(paMsWebformState.data, paMsWebformState.selectedTableSchemaId);

    onSelectFieldSchemaId(fieldSchema || fieldId);
  }, [paMsWebformState.data, paMsWebformState.selectedTableSchemaId]);

  useEffect(() => {
    if (isDataUpdated)
      paMsWebformDispatch({
        type: 'UPDATE_DATA',
        payload: { data: onLoadData() }
      });
  }, [isDataUpdated]);

  const initialLoad = () => {
    paMsWebformDispatch({
      type: 'INITIAL_LOAD',
      payload: { data: onLoadData() }
    });
  };

  const setIsLoading = value => paMsWebformDispatch({ type: 'IS_LOADING', payload: { value } });

  const generatePamId = pamsTableRecords => {
    if (isEmpty(pamsTableRecords)) return 1;

    const recordIds = parsePamsRecords(pamsTableRecords)
      .map(record => parseInt(record.Id) || parseInt(record.id))
      .filter(id => !Number.isNaN(id));

    return Math.max(...recordIds) + 1;
  };

  const setTableSchemaId = tableSchemaId => {
    paMsWebformDispatch({ type: 'GET_TABLE_SCHEMA_ID', payload: { tableSchemaId } });
  };

  const getPamsTableRecords = async tableSchemaId => {
    let data;
    if (!isNil(tableSchemaId[0])) {
      if (bigData) {
        data = await DatasetService.getTableDataDL({
          datasetId,
          tableSchemaId: tableSchemaId[0],
          pageSize: 300,
          levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER']
        });
      } else {
        data = await DatasetService.getTableData({
          datasetId,
          tableSchemaId: tableSchemaId[0],
          pageSize: 300,
          levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER']
        });
      }

      return onParseWebformRecords(data.records, paMsWebformState.data[0], {}, data.totalRecords) || [];
    }

    return [];
  };

  const onAddPamsRecord = async type => {
    if (type === 'single') {
      setIsAddingSingleRecord(true);
    } else if (type === 'group') {
      setIsAddingGroupRecord(true);
    }
    const filteredTables = datasetSchema.tables.filter(table => table.tableSchemaNotEmpty);
    const tableSchemaId = paMsWebformState.data.map(table => table.tableSchemaId).filter(table => !isNil(table));

    try {
      const pamsTableRecords = await getPamsTableRecords(tableSchemaId);
      await WebformService.addPamsRecords(datasetId, filteredTables, generatePamId(pamsTableRecords), capitalize(type));
      onUpdateData();
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        console.error('PaMsWebform - onAddPamsRecord.', error);
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
        notificationContext.add(
          {
            type: 'ADD_RECORDS_ERROR',
            content: { dataflowId, dataflowName, datasetId, datasetName, customContent: { tableName: '' } }
          },
          true
        );
      }
      if (type === 'single') {
        setIsAddingSingleRecord(false);
      } else if (type === 'group') {
        setIsAddingGroupRecord(false);
      }
    }
  };

  const onAddTableRecord = async (table, pamNumber) => {
    const newEmptyRecord = parseNewTableRecord(table, pamNumber);

    try {
      await DatasetService.createRecord(datasetId, table.tableSchemaId, [newEmptyRecord]);
      onUpdateData();
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        console.error('PaMsWebform - onAddTableRecord.', error);
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
        notificationContext.add(
          {
            type: 'ADD_RECORDS_ERROR',
            content: { dataflowId, datasetId, dataflowName, datasetName, customContent: { tableName: '' } }
          },
          true
        );
      }
    }
  };

  const onLoadData = () => {
    if (!isEmpty(datasetSchema)) {
      const data = onParseWebformData(datasetSchema, tables, datasetSchema.tables, datasetStatistics);

      data.forEach(table =>
        table.elements.forEach(element => {
          if (TextUtils.areEquals(element.name, 'pams')) {
            const listOfSingles = element.elements.find(el => TextUtils.areEquals(el.name, 'ListOfSinglePams'));
            if (!isNil(listOfSingles)) {
              listOfSingles.fieldType = 'MULTISELECT_CODELIST';
              listOfSingles.codelistItems = parseListOfSinglePams(pamsRecords);
            }
          }
        })
      );
      return data;
    }
  };

  const onLoadPamsData = async () => {
    const tableSchemaId = paMsWebformState.data.map(table => table.tableSchemaId).filter(table => !isNil(table));
    try {
      let data;
      if (!isNil(tableSchemaId[0])) {
        if (bigData) {
          data = await DatasetService.getTableDataDL({
            datasetId,
            tableSchemaId: tableSchemaId[0],
            pageSize: 300,
            levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER']
          });
        } else {
          data = await DatasetService.getTableData({
            datasetId,
            tableSchemaId: tableSchemaId[0],
            pageSize: 300,
            levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER']
          });
        }

        if (!isNil(data.records)) {
          const tableData = {};

          const records = onParseWebformRecords(data.records, paMsWebformState.data[0], tableData, data.totalRecords);
          const list = getTypeList(records);

          paMsWebformDispatch({
            type: 'ON_LOAD_PAMS_DATA',
            payload: { records, group: list['group'], single: list['single'] }
          });
        }
      }
    } catch (error) {
      console.error('PaMsWebform - onLoadPamsData.', error);
    } finally {
      setIsLoading(false);
    }
  };

  const onUpdatePamsValue = (recordId, pamsValue, fieldId, isPamTitle = false) => {
    paMsWebformDispatch({
      type: 'UPDATE_PAMS_RECORDS',
      payload: { fieldId, pamsValue, recordId, dataUpdated: !isDataUpdated, isPamTitle }
    });
  };

  const onSelectEditTable = (pamNumberId, tableName) => {
    const filteredTable = paMsWebformState.data.filter(table => TextUtils.areEquals(table.name, tableName))[0];
    const pamSchemaId = paMsWebformState.data
      .filter(table => TextUtils.areEquals(table.name, 'PAMS'))[0]
      .records[0].fields.filter(field => TextUtils.areEquals(field.name, 'ID'))[0].fieldId;
    let recordId = '';
    pamsRecords.forEach(pamsRecord => {
      pamsRecord.fields.forEach(field => {
        if (field.fieldSchemaId === pamSchemaId && parseInt(field.value) === parseInt(pamNumberId)) {
          recordId = pamsRecord.recordId;
        }
      });
    });

    setTableSchemaId(filteredTable.tableSchemaId);
    onSelectRecord(recordId, pamNumberId);
    onSelectTableName(tableName);
    onToggleView('details');
  };

  const onSelectFieldSchemaId = fieldSchemaId => {
    paMsWebformDispatch({ type: 'ON_SELECT_SCHEMA_ID', payload: { fieldSchemaId } });
  };

  const onSelectRecord = (recordId, pamsId) => {
    paMsWebformDispatch({ type: 'ON_SELECT_RECORD', payload: { recordId, pamsId } });
  };

  const onSelectTableName = name => paMsWebformDispatch({ type: 'ON_SELECT_TABLE', payload: { name } });

  const onToggleView = view => paMsWebformDispatch({ type: 'ON_TOGGLE_VIEW', payload: { view } });

  const onUpdateData = () => paMsWebformDispatch({ type: 'ON_UPDATE_DATA', payload: { value: !isDataUpdated } });

  const setIsAddingSingleRecord = value =>
    paMsWebformDispatch({ type: 'SET_IS_ADDING_SINGLE_RECORD', payload: { value } });

  const setIsAddingGroupRecord = value =>
    paMsWebformDispatch({ type: 'SET_IS_ADDING_GROUP_RECORD', payload: { value } });

  const renderOverviewButton = () => {
    if (view !== 'details') {
      return <div />;
    }

    return (
      <div className={styles.overviewButton}>
        <Button
          label={resourcesContext.messages['overview']}
          onClick={() => {
            onToggleView('overview');
            onSelectRecord(null, null);
          }}
        />
      </div>
    );
  };

  const renderView = () => {
    if (view === 'details') {
      return (
        <WebformView
          bigData={bigData}
          data={paMsWebformState.data}
          dataflowId={dataflowId}
          dataProviderId={dataProviderId}
          datasetId={datasetId}
          datasetSchemaId={datasetSchema.datasetSchemaId}
          getFieldSchemaId={getFieldSchemaId}
          isAddingPamsId={paMsWebformState.isAddingSingleRecord || paMsWebformState.isAddingGroupRecord}
          isIcebergCreated={isIcebergCreated}
          isRefresh={paMsWebformState.isRefresh}
          isReporting={isReporting}
          onUpdatePamsValue={onUpdatePamsValue}
          pamsRecords={pamsRecords}
          selectedTable={selectedTable}
          selectedTableName={selectedTableName}
          setTableSchemaId={setTableSchemaId}
          state={state}
          tables={tables.filter(table => table.isVisible)}
        />
      );
    }

    return (
      <TableManagement
        bigData={bigData}
        dataflowId={dataflowId}
        datasetId={datasetId}
        isAddingPamsId={paMsWebformState.isAddingSingleRecord || paMsWebformState.isAddingGroupRecord}
        loading={isLoading}
        onAddTableRecord={onAddTableRecord}
        onRefresh={onUpdateData}
        onSelectEditTable={onSelectEditTable}
        overview={overview}
        records={pamsRecords}
        schemaTables={datasetSchema.tables}
        tables={tables}
      />
    );
  };

  const renderErrorMessages = () => {
    const missingElements = checkErrors(paMsWebformState.data);

    return (
      <Fragment>
        <h4 className={styles.title}>{resourcesContext.messages['missingWebformTablesOrFieldsMissing']}</h4>
        <div className={styles.missingElements}>
          {Object.keys(missingElements).map(key => {
            const { fields, table } = missingElements[key];

            return (
              fields.some(field => field.isMissing) && (
                <div key={uniqueId()}>
                  <span className={styles.tableTitle}>
                    <FontAwesomeIcon icon={AwesomeIcons('table')} /> {table.name}
                  </span>
                  <ul>{fields.map(field => field.isMissing && <li key={uniqueId()}> {field.name}</li>)}</ul>
                </div>
              )
            );
          })}
        </div>
      </Fragment>
    );
  };

  const renderLayout = children => (
    <Fragment>
      <h2 className={styles.title}>
        <FontAwesomeIcon icon={AwesomeIcons('exclamationTriangle')} />
        <strong> {resourcesContext.messages['webformPaMsTitle']}</strong>
      </h2>
      {children}
    </Fragment>
  );

  if (paMsWebformState.hasErrors) {
    return renderLayout(renderErrorMessages());
  }

  return renderLayout(
    <Fragment>
      <ul className={styles.tableList}>
        {Object.keys(tableList).map(list => (
          <li className={styles.tableListItem} key={uniqueId()}>
            <div className={styles.tableListTitleWrapper}>
              <span className={styles.tableListTitle}>{resourcesContext.messages[list]}:</span>
            </div>
            <div className={styles.tableListContentWrapper}>
              {tableList[list].map(items => (
                <span
                  className={`${styles.tableListId} ${
                    items.recordId === selectedTable.recordId ? styles.selected : null
                  }`}
                  key={uniqueId()}
                  onClick={() => {
                    paMsWebformDispatch({ type: 'ON_REFRESH', payload: { value: !paMsWebformState.isRefresh } });
                    onSelectRecord(items.recordId, items.id);
                    onToggleView('details');
                  }}>
                  {items.id || '-'}
                </span>
              ))}
            </div>
            <div className={styles.addButtonWrapper}>
              <Button
                className={styles.addButton}
                disabled={paMsWebformState.isAddingSingleRecord || paMsWebformState.isAddingGroupRecord || isReleasing}
                icon={
                  list === 'single'
                    ? paMsWebformState.isAddingSingleRecord
                      ? 'spinnerAnimate'
                      : 'add'
                    : paMsWebformState.isAddingGroupRecord
                    ? 'spinnerAnimate'
                    : 'add'
                }
                label={
                  list === 'single' ? resourcesContext.messages['addSingle'] : resourcesContext.messages['addGroup']
                }
                onClick={() => onAddPamsRecord(list)}
              />
            </div>
          </li>
        ))}
      </ul>

      {renderOverviewButton()}

      {renderView()}
    </Fragment>
  );
};
