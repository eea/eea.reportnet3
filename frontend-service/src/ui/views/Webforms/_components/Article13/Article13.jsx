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

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { Article15Utils } from '../Article15/_functions/Utils/Article15Utils';

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

      default:
        return state;
    }
  };

  const [article13State, article13Dispatch] = useReducer(article13Reducer, {
    data: [],
    isWebformView: false,
    tableList: { group: [], single: [] }
  });

  useEffect(() => {
    initialLoad();
  }, []);

  const { isWebformView, tableList } = article13State;

  const initialLoad = () => {
    const allTables = tables.map(table => table.name);
    const parsedData = onLoadData();

    article13Dispatch({ type: 'INITIAL_LOAD', payload: { data: parsedData } });
  };

  const onLoadData = () => {
    if (!isEmpty(datasetSchema)) return onParseWebformData(tables, datasetSchema.tables);
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

      {/* <div className={styles.switch}>
        <span className={styles.option}>overView</span>
        <InputSwitch checked={isWebformView} onChange={event => onToggleView(event.value)} />
        <span className={styles.option}>{resources.messages['webform']}</span>
      </div> */}

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
        <TableManagement />
      )}
    </Fragment>
  );
};
