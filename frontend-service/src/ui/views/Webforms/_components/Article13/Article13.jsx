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
import { TableManagement } from './_components/TableManagement';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const Article13 = ({ dataflowId, datasetId, isReporting = false, state }) => {
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
    isWebformView: false,
    tableList: { group: [], single: [] }
  });

  const { isWebformView, tableList } = article13State;

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

      {isWebformView ? null : <TableManagement />}
    </Fragment>
  );
};
