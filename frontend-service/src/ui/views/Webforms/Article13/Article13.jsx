import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './Article13.module.scss';

import { tables } from './article13.webform.json';

import { Button } from 'ui/views/_components/Button';
import { TableManagement } from './_components/TableManagement';
import { TabularSwitch } from 'ui/views/_components/TabularSwitch';
import { WebformView } from './_components/WebformView';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { article13Reducer } from './_functions/Reducers/article13Reducer';

import { Article13Utils } from './_functions/Utils/Article13Utils';
import { MetadataUtils, TextUtils } from 'ui/views/_functions/Utils';
import { WebformsUtils } from 'ui/views/Webforms/_functions/Utils/WebformsUtils';

import { TableManagementUtils } from './_components/TableManagement/_functions/Utils/TableManagementUtils';

export const Article13 = ({ dataflowId, datasetId, isReporting, state }) => {
  const { datasetSchema } = state;
  const { getFieldSchemaId, getTypeList } = Article13Utils;
  const { onParseWebformData, onParseWebformRecords, parseNewRecord, parseNewTableRecord } = WebformsUtils;
  const { parsePamsRecords } = TableManagementUtils;

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [article13State, article13Dispatch] = useReducer(article13Reducer, {
    data: [],
    isAddingRecord: false,
    isDataUpdated: false,
    isLoading: true,
    isRefresh: false,
    pamsRecords: [],
    selectedTable: { fieldSchemaId: null, pamsId: null, recordId: null, tableName: null },
    selectedTableName: null,
    selectedTableSchemaId: null,
    tableList: { group: [], single: [] },
    view: resources.messages['overview']
  });

  const { isDataUpdated, isLoading, pamsRecords, selectedTable, selectedTableName, tableList, view } = article13State;

  useEffect(() => initialLoad(), []);

  useEffect(() => {
    if (!isEmpty(article13State.data)) {
      onLoadPamsData();
    }
  }, [article13State.data, isDataUpdated]);

  useEffect(() => {
    const { fieldSchema, fieldId } = getFieldSchemaId(article13State.data, article13State.selectedTableSchemaId);

    onSelectFieldSchemaId(fieldSchema || fieldId);
  }, [article13State.data, article13State.selectedTableSchemaId]);

  const initialLoad = () => article13Dispatch({ type: 'INITIAL_LOAD', payload: { data: onLoadData() } });

  const setIsLoading = value => article13Dispatch({ type: 'IS_LOADING', payload: { value } });

  const generatePamId = () => {
    if (isEmpty(pamsRecords)) return 1;

    const recordIds = parsePamsRecords(pamsRecords)
      .map(record => parseInt(record.Id) || parseInt(record.id))
      .filter(id => !Number.isNaN(id));

    return Math.max(...recordIds) + 1;
  };

  const getParamFieldSchemaId = (param, table) => {
    return table.elements
      .filter(element => TextUtils.areEquals(element.name, param))
      .map(table => table.fieldSchema)[0];
  };

  const setTableSchemaId = tableSchemaId => {
    article13Dispatch({ type: 'GET_TABLE_SCHEMA_ID', payload: { tableSchemaId } });
  };

  const onAddPamsRecord = async type => {
    setIsAddingRecord(true);

    const table = article13State.data.filter(table => TextUtils.areEquals(table.name, 'pams'))[0];
    const newEmptyPamRecord = parseNewRecord(table.elements);

    const data = [];
    const pamId = generatePamId();
    for (let index = 0; index < newEmptyPamRecord.dataRow.length; index++) {
      const row = newEmptyPamRecord.dataRow[index];
      row.fieldData[getParamFieldSchemaId('IsGroup', table)] = type;
      row.fieldData[getParamFieldSchemaId('Id', table)] = pamId;

      data.push({ ...row });
    }

    newEmptyPamRecord.dataRow = data;

    try {
      const response = await DatasetService.addRecordsById(datasetId, table.tableSchemaId, [newEmptyPamRecord]);

      const filteredTables = datasetSchema.tables.filter(table => table.notEmpty && table.tableSchemaName !== 'PAMs');

      for (let i = 0; i < filteredTables.length; i++) {
        const newEmptyRecord = parseNewTableRecord(filteredTables[i], pamId);
        await DatasetService.addRecordsById(datasetId, filteredTables[i].tableSchemaId, [newEmptyRecord]);
      }
      if (response) {
        onUpdateData();
      }
    } catch (error) {
      console.error('error', error);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'ADD_RECORDS_BY_ID_ERROR',
        content: { dataflowId, dataflowName, datasetId, datasetName, tableName: '' }
      });
    } finally {
      setIsAddingRecord(false);
    }
  };

  const onAddTableRecord = async (table, pamNumber) => {
    const newEmptyRecord = parseNewTableRecord(table, pamNumber);
    try {
      const response = await DatasetService.addRecordsById(datasetId, table.tableSchemaId, [newEmptyRecord]);
      if (response) {
        onUpdateData();
      }
    } catch (error) {
      console.error('error', error);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'ADD_RECORDS_BY_ID_ERROR',
        content: { dataflowId, datasetId, dataflowName, datasetName, tableName: '' }
      });
    }
  };

  const onLoadData = () => {
    if (!isEmpty(datasetSchema)) return onParseWebformData(datasetSchema, tables, datasetSchema.tables);
  };

  const onLoadPamsData = async () => {
    const tableSchemaId = article13State.data.map(table => table.tableSchemaId).filter(table => !isNil(table));
    try {
      if (!isNil(tableSchemaId[0])) {
        const parentTableData = await DatasetService.tableDataById(datasetId, tableSchemaId[0], '', 100, undefined, [
          'CORRECT',
          'INFO',
          'WARNING',
          'ERROR',
          'BLOCKER'
        ]);
        if (!isNil(parentTableData.records)) {
          const tableData = {};

          const records = onParseWebformRecords(
            parentTableData.records,
            article13State.data[0],
            tableData,
            parentTableData.totalRecords
          );
          const list = getTypeList(records);

          article13Dispatch({
            type: 'ON_LOAD_PAMS_DATA',
            payload: { records, group: list['group'], single: list['single'] }
          });
        }
      }
    } catch (error) {
      console.error('error', error);
    } finally {
      setIsLoading(false);
    }
  };

  const onSelectEditTable = (pamNumberId, tableName) => {
    const filteredTable = article13State.data.filter(table => TextUtils.areEquals(table.name, tableName))[0];
    const pamSchemaId = article13State.data
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
    onToggleView(resources.messages['details']);
  };

  const onSelectFieldSchemaId = fieldSchemaId => {
    article13Dispatch({ type: 'ON_SELECT_SCHEMA_ID', payload: { fieldSchemaId } });
  };

  const onSelectRecord = (recordId, pamsId) => {
    article13Dispatch({ type: 'ON_SELECT_RECORD', payload: { recordId, pamsId } });
  };

  const onSelectTableName = name => article13Dispatch({ type: 'ON_SELECT_TABLE', payload: { name } });

  const onToggleView = view => article13Dispatch({ type: 'ON_TOGGLE_VIEW', payload: { view } });

  const onUpdateData = () => article13Dispatch({ type: 'ON_UPDATE_DATA', payload: { value: !isDataUpdated } });

  const setIsAddingRecord = value => article13Dispatch({ type: 'SET_IS_ADDING_RECIRD', payload: { value } });

  return (
    <Fragment>
      <h3 className={styles.title}>
        Questionnaire for reporting on Policies and Measures under the Monitoring Mechanism Regulation
      </h3>

      <ul className={styles.tableList}>
        {Object.keys(tableList).map((list, i) => (
          <li className={styles.tableListItem} key={i}>
            <span className={styles.tableListTitle}>{resources.messages[list]}:</span>
            {tableList[list].map((items, i) => (
              <span
                className={`${styles.tableListId} ${
                  items.recordId === selectedTable.recordId ? styles.selected : null
                }`}
                key={i}
                onClick={() => {
                  article13Dispatch({ type: 'ON_REFRESH', payload: { value: !article13State.isRefresh } });
                  onSelectRecord(items.recordId, items.id);
                  onToggleView(resources.messages['details']);
                }}>
                {items.id || '-'}
              </span>
            ))}
            <Button
              className={styles.addButton}
              disabled={article13State.isAddingRecord}
              icon={article13State.isAddingRecord ? 'spinnerAnimate' : 'add'}
              label={resources.messages['add']}
              onClick={() => onAddPamsRecord(capitalize(list))}
            />
          </li>
        ))}
      </ul>

      <TabularSwitch
        className={`${styles.tabBar} ${view === resources.messages['details'] ? undefined : styles.hide}`}
        elements={[resources.messages['overview']]}
        onChange={switchView => {
          onToggleView(switchView);
          onSelectRecord(null, null);
        }}
        value={view}
      />
      {view === resources.messages['details'] ? (
        <WebformView
          data={article13State.data}
          dataflowId={dataflowId}
          datasetId={datasetId}
          datasetSchemaId={datasetSchema.datasetSchemaId}
          isRefresh={article13State.isRefresh}
          isReporting={isReporting}
          selectedTable={selectedTable}
          selectedTableName={selectedTableName}
          setTableSchemaId={setTableSchemaId}
          state={state}
          tables={tables}
        />
      ) : (
        <TableManagement
          dataflowId={dataflowId}
          datasetId={datasetId}
          loading={isLoading}
          isReporting={isReporting}
          onAddTableRecord={onAddTableRecord}
          onRefresh={onUpdateData}
          onSelectEditTable={onSelectEditTable}
          records={pamsRecords}
          schemaTables={datasetSchema.tables}
          tables={tables}
        />
      )}
    </Fragment>
  );
};
