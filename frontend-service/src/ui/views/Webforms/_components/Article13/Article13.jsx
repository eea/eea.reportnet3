import React, { Fragment, useContext, useEffect, useReducer } from 'react';
import ReactTooltip from 'react-tooltip';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import keys from 'lodash/keys';
import pickBy from 'lodash/pickBy';

import styles from './Article13.module.scss';

import { tables } from './article13.webform.json';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { Spinner } from 'ui/views/_components/Spinner';
import { TableManagement } from './_components/TableManagement';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { WebformView } from './_components/WebformView';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { Article15Utils } from '../Article15/_functions/Utils/Article15Utils';
import { MetadataUtils } from 'ui/views/_functions/Utils';
import { TextUtils } from 'ui/views/_functions/Utils';

export const Article13 = ({ dataflowId, datasetId, isReporting = false, state }) => {
  const { datasetSchema } = state;

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const article13Reducer = (state, { type, payload }) => {
    switch (type) {
      case 'INITIAL_LOAD':
        return { ...state, ...payload };

      case 'ON_TOGGLE_VIEW':
        return { ...state, isWebformView: payload.view };

      case 'ON_LOAD_PAMS_DATA':
        return { ...state, pamsRecords: payload.records };

      default:
        return state;
    }
  };

  const [article13State, article13Dispatch] = useReducer(article13Reducer, {
    data: [],
    isWebformView: false,
    pamsRecords: [],
    tableList: { group: [], single: [] }
  });

  const { isWebformView, pamsRecords, tableList } = article13State;

  useEffect(() => {
    initialLoad();
  }, []);

  useEffect(() => {
    onLoadPamsData();
  }, [article13State.data]);

  const initialLoad = () => {
    const allTables = tables.map(table => table.name);
    const parsedData = onLoadData();

    article13Dispatch({ type: 'INITIAL_LOAD', payload: { data: parsedData } });
  };

  const onLoadData = () => {
    if (!isEmpty(datasetSchema)) return onParseWebformData(tables, datasetSchema.tables);
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

        article13Dispatch({ type: 'ON_LOAD_PAMS_DATA', payload: { records } });
      }
    } catch (error) {
      console.log('error', error);
    }
  };

  const onParseWebformRecords = (records, webform, tableData, totalRecords) => {
    return records.map(record => {
      const { fields } = record;
      const { elements } = webform;

      const result = [];

      for (let index = 0; index < elements.length; index++) {
        const element = elements[index];

        if (element.type === 'FIELD') {
          result.push({
            fieldType: 'EMPTY',
            ...element,
            ...fields.find(field => field['fieldSchemaId'] === element['fieldSchema']),
            codelistItems: element.codelistItems || [],
            description: element.description || '',
            isDisabled: isNil(element.fieldSchema),
            maxSize: element.maxSize,
            name: element.name,
            recordId: record.recordId,
            type: element.type,
            validExtensions: element.validExtensions
          });
        } else {
          if (tableData[element.tableSchemaId]) {
            const tableElementsRecords = onParseWebformRecords(
              tableData[element.tableSchemaId].records,
              element,
              tableData,
              totalRecords
            );
            result.push({ ...element, elementsRecords: tableElementsRecords });
          } else {
            result.push({ ...element, tableNotCreated: true, elementsRecords: [] });
          }
        }
      }

      return { ...record, elements: result, totalRecords };
    });
  };

  const onParseWebformData = (allTables, schemaTables) => {
    const data = Article15Utils.mergeArrays(allTables, schemaTables, 'name', 'tableSchemaName');
    data.map(table => {
      if (table.records) {
        table.records[0].fields = table.records[0].fields.map(field => {
          const { fieldId, recordId, type } = field;

          return { fieldSchema: fieldId, fieldType: type, recordSchemaId: recordId, ...field };
        });
      }
    });

    for (let index = 0; index < data.length; index++) {
      const table = data[index];

      if (table.records) {
        const { elements, records } = table;

        const result = [];
        for (let index = 0; index < elements.length; index++) {
          if (elements[index].type === 'FIELD') {
            result.push({
              ...elements[index],
              ...records[0].fields.find(element => element['name'] === elements[index]['name']),
              type: elements[index].type
            });
          } else if (elements[index].type === 'TABLE') {
            const filteredTable = datasetSchema.tables.filter(table => table.tableSchemaName === elements[index].name);
            const parsedTable = onParseWebformData([elements[index]], filteredTable);

            result.push({ ...elements[index], ...parsedTable[0], type: elements[index].type });
          } else if (elements[index].type === 'LABEL') {
            result.push({ ...elements[index] });
          }
        }

        table.elements = result;
      }
    }

    return data;
  };

  const onToggleView = view => article13Dispatch({ type: 'ON_TOGGLE_VIEW', payload: { view } });

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
              {/* <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('add')} /> */}
              <span className={styles.tableListTitle}>{list}</span>:
            </div>
            <div className={styles.tableListIds}>
              {tableList[list].map(items => (
                <span className={styles.tableListId}>{items.id}</span>
              ))}
            </div>
            <Button label={'add'} icon={'add'} />
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
        <TableManagement dataflowId={dataflowId} datasetId={datasetId} records={pamsRecords} />
      )}
    </Fragment>
  );
};
