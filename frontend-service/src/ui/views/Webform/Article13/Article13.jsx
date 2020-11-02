import React, { Fragment, useContext, useEffect, useReducer } from 'react';
import ReactTooltip from 'react-tooltip';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import keys from 'lodash/keys';
import pickBy from 'lodash/pickBy';

import styles from './Article13.module.scss';

import { tables } from './article13.webform.json';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
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

  const [article13State, article13Dispatch] = useReducer(article13Reducer, { isWebformView: false });

  const { isWebformView } = article13State;

  const onToggleView = view => article13Dispatch({ type: 'ON_TOGGLE_VIEW', payload: { view } });

  const data = [
    {
      PaMNumber: 0,
      nameOfPolicy: 'test',
      singleOrGroup: 'single',
      table1: 'aaaa',
      table2: 'bbbb',
      table3: 'cccc'
    },
    {
      PaMNumber: 0,
      nameOfPolicy: 'test',
      singleOrGroup: 'group',
      table1: 'dddd',
      table2: 'eeee',
      table3: 'ffff'
    }
  ];

  return (
    <Fragment>
      <h3 className={styles.title}>
        Questionnaire for reporting on Policies and Measures under the Monitoring Mechanism Regulation
      </h3>

      <div className={styles.switch}>
        <span className={styles.option}>overView</span>
        <InputSwitch checked={isWebformView} onChange={event => onToggleView(event.value)} />
        <span className={styles.option}>{resources.messages['webform']}</span>
      </div>

      <DataTable
        autoLayout={true}
        // totalRecords={constraintsState.filteredData.length}
        // value={constraintsState.filteredData}
        value={data}>
        {data.map(datos => {
          console.log('datos', datos);

          Object.keys(datos).map(d => {
            return (
              <Column
                // header={messages[selected]['hours']}
                // headerStyle={{ background: online ? '#ff3800' : '', color: online ? 'white' : '' }}
                field={d}
              />
            );
          });
        })}
      </DataTable>
    </Fragment>
  );
};
