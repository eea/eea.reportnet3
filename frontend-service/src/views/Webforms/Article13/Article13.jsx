import { Fragment, useContext, useEffect, useReducer } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniqueId from 'lodash/uniqueId';

import styles from './Article13.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { tables } from './article13.webform.json';

import { Button } from 'views/_components/Button';
import { TableManagement } from './_components/TableManagement';
import { TabularSwitch } from 'views/_components/TabularSwitch';
import { WebformView } from './_components/WebformView';

import { DatasetService } from 'services/DatasetService';
import { WebformService } from 'services/WebformService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { article13Reducer } from './_functions/Reducers/article13Reducer';

import { Article13Utils } from './_functions/Utils/Article13Utils';
import { MetadataUtils } from 'views/_functions/Utils';
import { WebformsUtils } from 'views/Webforms/_functions/Utils/WebformsUtils';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const Article13 = ({ dataProviderId, dataflowId, datasetId, isReleasing, isReporting, state }) => {
  const { checkErrors, getFieldSchemaId, getTypeList, hasErrors, parseListOfSinglePams } = Article13Utils;
  const { datasetSchema, datasetStatistics } = state;
  const { onParseWebformData, onParseWebformRecords, parseNewTableRecord, parsePamsRecords } = WebformsUtils;

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [article13State, article13Dispatch] = useReducer(article13Reducer, {
    data: [],
    hasErrors: true,
    isAddingSingleRecord: false,
    isAddingGroupRecord: false,
    isDataUpdated: false,
    isLoading: true,
    isRefresh: false,
    pamsRecords: [],
    selectedTable: { fieldSchemaId: null, pamsId: null, recordId: null, tableName: null },
    selectedTableName: null,
    selectedTableSchemaId: null,
    tableList: { group: [], single: [] },
    view: resourcesContext.messages['overview']
  });

  const { isDataUpdated, isLoading, pamsRecords, selectedTable, selectedTableName, tableList, view } = article13State;

  useEffect(() => initialLoad(), []);

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
    article13Dispatch({
      type: 'INITIAL_LOAD',
      payload: { data: onLoadData() }
    });
  };

  const setIsLoading = value => article13Dispatch({ type: 'IS_LOADING', payload: { value } });

  const generatePamId = pamsTableRecords => {
    if (isEmpty(pamsTableRecords)) return 1;

    const recordIds = parsePamsRecords(pamsTableRecords)
      .map(record => parseInt(record.Id) || parseInt(record.id))
      .filter(id => !Number.isNaN(id));

    return Math.max(...recordIds) + 1;
  };

  const setTableSchemaId = tableSchemaId => {
    article13Dispatch({ type: 'GET_TABLE_SCHEMA_ID', payload: { tableSchemaId } });
  };

  const getPamsTableRecords = async tableSchemaId => {
    if (!isNil(tableSchemaId[0])) {
      const data = await DatasetService.getTableData({
        datasetId,
        tableSchemaId: tableSchemaId[0],
        pageSize: 300,
        levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER']
      });
      return onParseWebformRecords(data.records, article13State.data[0], {}, data.totalRecords) || [];
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
    const tableSchemaId = article13State.data.map(table => table.tableSchemaId).filter(table => !isNil(table));

    try {
      const pamsTableRecords = await getPamsTableRecords(tableSchemaId);
      await WebformService.addPamsRecords(datasetId, filteredTables, generatePamId(pamsTableRecords), capitalize(type));
      onUpdateData();
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({
          type: 'GENERIC_BLOCKED_ERROR'
        });
      } else {
        console.error('Article13 - onAddPamsRecord.', error);
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
        notificationContext.add({
          type: 'ADD_RECORDS_ERROR',
          content: { dataflowId, dataflowName, datasetId, datasetName, tableName: '' }
        });
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
        notificationContext.add({
          type: 'GENERIC_BLOCKED_ERROR'
        });
      } else {
        console.error('Article13 - onAddTableRecord.', error);
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
        notificationContext.add({
          type: 'ADD_RECORDS_ERROR',
          content: { dataflowId, datasetId, dataflowName, datasetName, tableName: '' }
        });
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
    const tableSchemaId = article13State.data.map(table => table.tableSchemaId).filter(table => !isNil(table));
    try {
      if (!isNil(tableSchemaId[0])) {
        const data = await DatasetService.getTableData({
          datasetId,
          tableSchemaId: tableSchemaId[0],
          pageSize: 300,
          levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER']
        });

        if (!isNil(data.records)) {
          const tableData = {};

          const records = onParseWebformRecords(data.records, article13State.data[0], tableData, data.totalRecords);
          const list = getTypeList(records);

          article13Dispatch({
            type: 'ON_LOAD_PAMS_DATA',
            payload: { records, group: list['group'], single: list['single'] }
          });
        }
      }
    } catch (error) {
      console.error('Article13 - onLoadPamsData.', error);
    } finally {
      setIsLoading(false);
    }
  };

  const onUpdatePamsValue = (recordId, pamsValue, fieldId, isPamTitle = false) => {
    article13Dispatch({
      type: 'UPDATE_PAMS_RECORDS',
      payload: { fieldId, pamsValue, recordId, dataUpdated: !isDataUpdated, isPamTitle }
    });
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
    onToggleView(resourcesContext.messages['details']);
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
      <h2 className={styles.title}>{resourcesContext.messages['webformArticle13Title']}</h2>
      {children}
    </Fragment>
  );

  if (article13State.hasErrors) return renderLayout(renderErrorMessages());

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
                    article13Dispatch({ type: 'ON_REFRESH', payload: { value: !article13State.isRefresh } });
                    onSelectRecord(items.recordId, items.id);
                    onToggleView(resourcesContext.messages['details']);
                  }}>
                  {items.id || '-'}
                </span>
              ))}
            </div>
            <div className={styles.addButtonWrapper}>
              <Button
                className={styles.addButton}
                disabled={article13State.isAddingSingleRecord || article13State.isAddingGroupRecord || isReleasing}
                icon={
                  list === 'single'
                    ? article13State.isAddingSingleRecord
                      ? 'spinnerAnimate'
                      : 'add'
                    : article13State.isAddingGroupRecord
                    ? 'spinnerAnimate'
                    : 'add'
                }
                label={resourcesContext.messages[list === 'single' ? 'addSingle' : 'addGroup']}
                onClick={() => onAddPamsRecord(list)}
              />
            </div>
          </li>
        ))}
      </ul>

      <TabularSwitch
        className={`${styles.tabBar} ${view === resourcesContext.messages['details'] ? undefined : styles.hide}`}
        elements={[resourcesContext.messages['overview']]}
        onChange={switchView => {
          onToggleView(switchView);
          onSelectRecord(null, null);
        }}
        value={view}
      />
      {view === resourcesContext.messages['details'] ? (
        <WebformView
          data={article13State.data}
          dataProviderId={dataProviderId}
          dataflowId={dataflowId}
          datasetId={datasetId}
          datasetSchemaId={datasetSchema.datasetSchemaId}
          getFieldSchemaId={getFieldSchemaId}
          isAddingPamsId={article13State.isAddingSingleRecord || article13State.isAddingGroupRecord}
          isRefresh={article13State.isRefresh}
          isReporting={isReporting}
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
