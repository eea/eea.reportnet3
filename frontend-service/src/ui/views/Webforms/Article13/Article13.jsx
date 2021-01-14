import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './Article13.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { tables } from './article13.webform.json';

import { Button } from 'ui/views/_components/Button';
import { TableManagement } from './_components/TableManagement';
import { TabularSwitch } from 'ui/views/_components/TabularSwitch';
import { WebformView } from './_components/WebformView';

import { DatasetService } from 'core/services/Dataset';
import { WebformService } from 'core/services/Webform';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { article13Reducer } from './_functions/Reducers/article13Reducer';

import { Article13Utils } from './_functions/Utils/Article13Utils';
import { MetadataUtils, TextUtils } from 'ui/views/_functions/Utils';
import { WebformsUtils } from 'ui/views/Webforms/_functions/Utils/WebformsUtils';

export const Article13 = ({ dataflowId, datasetId, isReporting, state }) => {
  const { checkErrors, getFieldSchemaId, getTypeList, hasErrors, parseListOfSinglePams } = Article13Utils;
  const { datasetSchema } = state;
  const { onParseWebformData, onParseWebformRecords, parseNewTableRecord, parsePamsRecords } = WebformsUtils;

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [article13State, article13Dispatch] = useReducer(article13Reducer, {
    data: [],
    hasErrors: true,
    isAddingSingleRecord: false,
    isAddingGroupRecord: false,
    isDataUpdated: false,
    isDataUpdatedWithSingles: false,
    isLoading: true,
    isRefresh: false,
    pamsRecords: [],
    selectedTable: { fieldSchemaId: null, pamsId: null, recordId: null, tableName: null },
    selectedTableName: null,
    selectedTableSchemaId: null,
    tableList: { group: [], single: [] },
    view: resources.messages['overview']
  });

  const {
    isDataUpdated,
    isDataUpdatedWithSingles,
    isLoading,
    pamsRecords,
    selectedTable,
    selectedTableName,
    tableList,
    view
  } = article13State;

  useEffect(() => initialLoad(), [pamsRecords]);

  useEffect(() => {
    if (!isEmpty(article13State.data)) {
      onLoadPamsData();
      article13Dispatch({ type: 'HAS_ERRORS', payload: { value: hasErrors(article13State.data) } });
    }
  }, [article13State.data, isDataUpdated]);

  useEffect(() => {
    setIsAddingSingleRecord(false);
    setIsAddingGroupRecord(false);
  }, [tableList]);

  useEffect(() => {
    const { fieldId, fieldSchema } = getFieldSchemaId(article13State.data, article13State.selectedTableSchemaId);

    onSelectFieldSchemaId(fieldSchema || fieldId);
  }, [article13State.data, article13State.selectedTableSchemaId]);

  useEffect(() => {
    if (isDataUpdated)
      article13Dispatch({
        type: 'UPDATE_DATA',
        payload: { data: onLoadData() }
      });
  }, [isDataUpdated]);

  const initialLoad = () => {
    if (!isDataUpdatedWithSingles) {
      article13Dispatch({
        type: 'INITIAL_LOAD',
        payload: { data: onLoadData(), isDataUpdatedWithSingles: !isEmpty(pamsRecords) }
      });
    }
  };

  const setIsLoading = value => article13Dispatch({ type: 'IS_LOADING', payload: { value } });

  const generatePamId = () => {
    if (isEmpty(pamsRecords)) return 1;

    const recordIds = parsePamsRecords(pamsRecords)
      .map(record => parseInt(record.Id) || parseInt(record.id))
      .filter(id => !Number.isNaN(id));

    return Math.max(...recordIds) + 1;
  };

  const setTableSchemaId = tableSchemaId => {
    article13Dispatch({ type: 'GET_TABLE_SCHEMA_ID', payload: { tableSchemaId } });
  };

  const onAddPamsRecord = async type => {
    if (type === 'single') {
      setIsAddingSingleRecord(true);
    } else if (type === 'group') {
      setIsAddingGroupRecord(true);
    }
    const filteredTables = datasetSchema.tables.filter(table => table.tableSchemaNotEmpty);

    try {
      const response = await WebformService.addPamsRecords(
        datasetId,
        filteredTables,
        generatePamId(),
        capitalize(type)
      );
      if (response.status >= 200 && response.status <= 299) {
        onUpdateData();
      }
    } catch (error) {
      console.error('error', error);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'ADD_RECORDS_ERROR',
        content: { dataflowId, dataflowName, datasetId, datasetName, tableName: '' }
      });
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
      const response = await DatasetService.addRecordsById(datasetId, table.tableSchemaId, [newEmptyRecord]);
      if (response.status >= 200 && response.status <= 299) {
        onUpdateData();
      }
    } catch (error) {
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'ADD_RECORDS_ERROR',
        content: { dataflowId, datasetId, dataflowName, datasetName, tableName: '' }
      });
    }
  };

  const onLoadData = () => {
    if (!isEmpty(datasetSchema)) {
      const data = onParseWebformData(datasetSchema, tables, datasetSchema.tables);

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

  const onUpdatePamsValue = (recordId, pamsValue, fieldId, isPamTitle = false) =>
    article13Dispatch({
      type: 'UPDATE_PAMS_RECORDS',
      payload: { fieldId, pamsValue, recordId, dataUpdated: !isDataUpdated, isPamTitle }
    });

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

  const setIsAddingSingleRecord = value =>
    article13Dispatch({ type: 'SET_IS_ADDING_SINGLE_RECORD', payload: { value } });

  const setIsAddingGroupRecord = value => article13Dispatch({ type: 'SET_IS_ADDING_GROUP_RECORD', payload: { value } });

  const renderErrorMessages = () => {
    const missingElements = checkErrors(article13State.data);

    return (
      <Fragment>
        <h4 className={styles.title}>{resources.messages['missingWebformTablesOrFieldsMissing']}</h4>
        <div className={styles.missingElements}>
          {Object.keys(missingElements).map((key, i) => {
            const { fields, table } = missingElements[key];

            return (
              fields.some(field => field.isMissing) && (
                <div key={i}>
                  <span className={styles.tableTitle}>
                    <FontAwesomeIcon icon={AwesomeIcons('table')} /> {table.name}
                  </span>
                  <ul>{fields.map((field, index) => field.isMissing && <li key={index}> {field.name}</li>)}</ul>
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
      <h2 className={styles.title}>{resources.messages['webformArticle13Title']}</h2>
      {children}
    </Fragment>
  );

  if (article13State.hasErrors) return renderLayout(renderErrorMessages());

  return renderLayout(
    <Fragment>
      <ul className={styles.tableList}>
        {Object.keys(tableList).map((list, i) => (
          <li className={styles.tableListItem} key={i}>
            <div className={styles.tableListTitleWrapper}>
              <span className={styles.tableListTitle}>{resources.messages[list]}:</span>
            </div>
            <div className={styles.tableListContentWrapper}>
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
            </div>
            <div className={styles.addButtonWrapper}>
              <Button
                className={styles.addButton}
                disabled={article13State.isAddingSingleRecord || article13State.isAddingGroupRecord}
                icon={
                  list === 'single'
                    ? article13State.isAddingSingleRecord
                      ? 'spinnerAnimate'
                      : 'add'
                    : article13State.isAddingGroupRecord
                    ? 'spinnerAnimate'
                    : 'add'
                }
                label={resources.messages[list === 'single' ? 'addSingle' : 'addGroup']}
                onClick={() => onAddPamsRecord(list)}
              />
            </div>
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
          getFieldSchemaId={getFieldSchemaId}
          isRefresh={article13State.isRefresh}
          isReporting={isReporting}
          isAddingPamsId={article13State.isAddingSingleRecord || article13State.isAddingGroupRecord}
          onUpdatePamsValue={onUpdatePamsValue}
          pamsRecords={pamsRecords}
          selectedTable={selectedTable}
          selectedTableName={selectedTableName}
          setTableSchemaId={setTableSchemaId}
          state={state}
          tables={tables.filter(table => table.isVisible)}
        />
      ) : (
        <TableManagement
          dataflowId={dataflowId}
          datasetId={datasetId}
          isAddingPamsId={article13State.isAddingSingleRecord || article13State.isAddingGroupRecord}
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
