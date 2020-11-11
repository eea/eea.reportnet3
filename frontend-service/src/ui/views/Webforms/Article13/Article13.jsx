import React, { Fragment, useContext, useEffect, useReducer } from 'react';
import ReactTooltip from 'react-tooltip';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import capitalize from 'lodash/capitalize';

import styles from './Article13.module.scss';

import { tables } from './article13.webform.json';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { Button } from 'ui/views/_components/Button';
import { TableManagement } from './_components/TableManagement';
import { WebformView } from './_components/WebformView';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { article13Reducer } from './_functions/Reducers/article13Reducer';

import { Article13Utils } from './_functions/Utils/Article13Utils';
import { MetadataUtils } from 'ui/views/_functions/Utils';
import { WebformsUtils } from 'ui/views/Webforms/_functions/Utils/WebformsUtils';

import { TableManagementUtils } from './_components/TableManagement/_functions/Utils/TableManagementUtils';

export const Article13 = ({ dataflowId, datasetId, isReporting = false, state }) => {
  const { datasetSchema } = state;
  const { getTypeList } = Article13Utils;
  const { onParseWebformData, onParseWebformRecords, parseNewRecord, parseNewTableRecord } = WebformsUtils;
  const { parsePamsRecords } = TableManagementUtils;

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [article13State, article13Dispatch] = useReducer(article13Reducer, {
    data: [],
    isDataUpdated: false,
    isLoading: true,
    isWebformView: false,
    pamsRecords: [],
    selectedId: null,
    selectedTable: { tableName: null, recordId: null, pamsId: null },
    selectedTableName: null,
    tableList: { group: [], single: [] }
  });

  const {
    isDataUpdated,
    isLoading,
    isWebformView,
    pamsRecords,
    selectedId,
    selectedTableName,
    tableList
  } = article13State;

  useEffect(() => initialLoad(), []);

  useEffect(() => {
    onLoadPamsData();
  }, [article13State.data, isDataUpdated]);

  const initialLoad = () => article13Dispatch({ type: 'INITIAL_LOAD', payload: { data: onLoadData() } });

  const setIsLoading = value => article13Dispatch({ type: 'IS_LOADING', payload: { value } });

  const generatePamId = () => {
    if (isEmpty(pamsRecords)) return 1;
    const recordIds = parsePamsRecords(pamsRecords)
      .map(record => parseInt(record.Id))
      .filter(id => !Number.isNaN(id));

    return Math.max(...recordIds) + 1;
  };

  const getParamFieldSchemaId = (param, table) => {
    return table.elements
      .filter(element => element.name.toUpperCase() === param.toUpperCase())
      .map(table => table.fieldSchema)[0];
  };

  const onAddPamsRecord = async type => {
    // const table = article13State.data.filter(table => table.recordSchemaId === pamsRecords[0].recordSchemaId)[0];
    const table = article13State.data.filter(table => table.name.toLowerCase() === 'pams')[0];
    const newEmptyRecord = parseNewRecord(table.elements);

    const data = [];
    for (let index = 0; index < newEmptyRecord.dataRow.length; index++) {
      const row = newEmptyRecord.dataRow[index];

      row.fieldData[getParamFieldSchemaId('IsGroup', table)] = type;
      row.fieldData[getParamFieldSchemaId('Id', table)] = generatePamId();

      data.push({ ...row });
    }

    newEmptyRecord.dataRow = data;

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
    const pamSchemaId = article13State.data
      .filter(table => table.name.toUpperCase() === 'PAMS')[0]
      .records[0].fields.filter(field => field.name.toUpperCase() === 'ID')[0].fieldId;
    let recordId = '';
    pamsRecords.forEach(pamsRecord => {
      pamsRecord.fields.forEach(field => {
        if (field.fieldSchemaId === pamSchemaId && parseInt(field.value) === parseInt(pamNumberId)) {
          recordId = pamsRecord.recordId;
        }
      });
    });
    onSelectRecord(recordId);
    onSelectTableName(tableName);
    onToggleView(true);
  };

  const onSelectRecord = (recordId, pamsId) => {
    article13Dispatch({ type: 'ON_SELECT_RECORD', payload: { recordId, pamsId } });
  };

  const onSelectTableName = name => article13Dispatch({ type: 'ON_SELECT_TABLE', payload: { name } });

  const onToggleView = view => article13Dispatch({ type: 'ON_TOGGLE_VIEW', payload: { view } });

  const onUpdateData = () => article13Dispatch({ type: 'ON_UPDATE_DATA', payload: { value: !isDataUpdated } });

  return (
    <Fragment>
      <h3 className={styles.title}>
        Questionnaire for reporting on Policies and Measures under the Monitoring Mechanism Regulation
      </h3>

      <ul className={styles.tableList}>
        {Object.keys(tableList).map(list => (
          <li className={styles.tableListItem}>
            <span className={styles.tableListTitle}>{resources.messages[list]}:</span>
            {tableList[list].map(items => (
              <span
                className={`${styles.tableListId} ${items.recordId === selectedId ? styles.selected : null}`}
                onClick={() => {
                  onSelectRecord(items.recordId, items.id);
                  onToggleView(true);
                }}>
                {items.id}
              </span>
            ))}
            <Button
              className={styles.addButton}
              label={resources.messages['add']}
              icon={'add'}
              onClick={() => onAddPamsRecord(capitalize(list))}
            />
          </li>
        ))}
      </ul>

      <div className={styles.tabBar}>
        <div className={styles.indicator} style={{ left: isWebformView ? 'calc(150px + 1.5rem)' : '1.5rem' }} />
        <div
          className={`${styles.tabItem} ${!isWebformView ? styles.selected : null}`}
          onClick={() => {
            onToggleView(false);
            onSelectRecord(null, null);
          }}>
          <p className={styles.tabLabel}>{resources.messages['overview']}</p>
        </div>
        <div
          className={`${styles.tabItem} ${isWebformView ? styles.selected : null} ${
            isEmpty(pamsRecords) ? styles.disabled : null
          }`}
          data-for="emptyTableTooltip"
          data-tip
          onClick={() => (isEmpty(pamsRecords) ? {} : onToggleView(true))}>
          <p className={styles.tabLabel}>{resources.messages['details']}</p>
        </div>
      </div>

      {isEmpty(pamsRecords) && (
        <ReactTooltip effect="solid" id="emptyTableTooltip" place="right">
          {resources.messages['overviewEmptyTableTooltip']}
        </ReactTooltip>
      )}

      {isWebformView ? (
        <WebformView
          data={article13State.data}
          dataflowId={dataflowId}
          datasetId={datasetId}
          selectedId={selectedId}
          selectedTableName={selectedTableName}
          state={state}
          tables={tables}
        />
      ) : (
        <TableManagement
          dataflowId={dataflowId}
          datasetId={datasetId}
          loading={isLoading}
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
