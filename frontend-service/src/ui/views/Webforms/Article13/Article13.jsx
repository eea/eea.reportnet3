import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

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

export const Article13 = ({ dataflowId, datasetId, isReporting = false, state }) => {
  const { datasetSchema } = state;
  const { getTypeList } = Article13Utils;
  const { onParseWebformData, onParseWebformRecords, parseNewRecord } = WebformsUtils;

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [article13State, article13Dispatch] = useReducer(article13Reducer, {
    data: [],
    isDataUpdated: false,
    isWebformView: false,
    pamsRecords: [],
    tableList: { group: [], single: [] }
  });

  const { isDataUpdated, isWebformView, pamsRecords, tableList } = article13State;

  useEffect(() => initialLoad(), []);

  useEffect(() => {
    onLoadPamsData();
  }, [article13State.data, isDataUpdated]);

  const initialLoad = () => article13Dispatch({ type: 'INITIAL_LOAD', payload: { data: onLoadData() } });

  const onAddRecord = async type => {
    const table = article13State.data.filter(table => table.recordSchemaId === pamsRecords[0].recordSchemaId)[0];

    const newEmptyRecord = parseNewRecord(table.elements);

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
    const tableSchemaId = article13State.data.map(table => table.tableSchemaId);

    try {
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
    } catch (error) {
      console.log('error', error);
    }
  };

  const onToggleView = view => article13Dispatch({ type: 'ON_TOGGLE_VIEW', payload: { view } });

  const onUpdateData = () => article13Dispatch({ type: 'ON_UPDATE_DATA', payload: { value: !isDataUpdated } });

  return (
    <Fragment>
      <h3 className={styles.title}>
        Questionnaire for reporting on Policies and Measures under the Monitoring Mechanism Regulation
      </h3>

      <div className={styles.tabBar}>
        <div className={styles.indicator} style={{ left: isWebformView ? 'calc(150px + 1.5rem)' : '1.5rem' }} />
        <div
          className={`${styles.tabItem} ${!isWebformView ? styles.selected : null}`}
          onClick={() => onToggleView(false)}>
          <p className={styles.tabLabel}>Overview</p>
        </div>
        <div
          className={`${styles.tabItem} ${isWebformView ? styles.selected : null}`}
          onClick={() => onToggleView(true)}>
          <p className={styles.tabLabel}>Webform</p>
        </div>
      </div>

      <ul className={styles.tableList}>
        {Object.keys(tableList).map(list => (
          <li className={styles.tableListItem}>
            <div className={styles.tableListTitleWrapper}>
              <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('add')} />
              <span className={styles.tableListTitle}>{list}</span>:
            </div>
            <div className={styles.tableListIds}>
              {tableList[list].map(items => (
                <span className={styles.tableListId}>{items.title}</span>
              ))}
            </div>
            <Button label={'add'} icon={'add'} onClick={() => onAddRecord()} />
          </li>
        ))}
      </ul>

      {isWebformView ? (
        <WebformView
          data={article13State.data}
          dataflowId={dataflowId}
          datasetId={datasetId}
          state={state}
          tables={tables}
        />
      ) : (
        <TableManagement
          dataflowId={dataflowId}
          datasetId={datasetId}
          onAddRecord={onAddRecord}
          onRefresh={onUpdateData}
          records={pamsRecords}
          schemaTables={datasetSchema.tables}
        />
      )}
    </Fragment>
  );
};
