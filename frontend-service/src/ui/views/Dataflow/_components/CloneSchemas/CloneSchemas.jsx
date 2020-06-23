import React, { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './CloneSchemas.module.scss';

import { Filters } from 'ui/views/_components/Filters';
import { InputSwitch } from 'ui/views/_components/InputSwitch';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { cloneSchemasReducer } from './_functions/Reducers/cloneSchemasReducer';

export const CloneSchemas = dataflowId => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [cloneSchemasState, cloneSchemasDispatch] = useReducer(cloneSchemasReducer, {
    data: [],
    dataflowChosen: {},
    filteredData: [],
    isLoading: false,
    isTableView: true
  });

  const isLoading = value => cloneSchemasDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadFilteredData = data => cloneSchemasDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const onToggleView = () =>
    cloneSchemasDispatch({ type: 'ON_TOGGLE_VIEW', payload: { view: !cloneSchemasState.isTableView } });

  return (
    <Fragment>
      <div className={styles.switchDiv}>
        <label className={styles.switchTextInput}>{resources.messages['magazineView']}</label>
        <InputSwitch checked={cloneSchemasState.isTableView} onChange={() => onToggleView()} />
        <label className={styles.switchTextInput}>{resources.messages['listView']}</label>
      </div>

      <div className={styles.filters}>
        <Filters
          data={cloneSchemasState.data}
          getFilteredData={onLoadFilteredData}
          inputOptions={['name', 'description']}
        />
      </div>

      {!cloneSchemasState.isLoading && (
        <span
          className={`${styles.selectedDataflow} ${
            isEmpty(cloneSchemasState.data) || isEmpty(cloneSchemasState.searchedData) ? styles.filteredSelected : ''
          }`}>
          <span>{`${resources.messages['selectedDataflow']}: `}</span>
          {`${
            !isEmpty(cloneSchemasState.dataflowChosen) && !isEmpty(cloneSchemasState.dataflowChosen)
              ? cloneSchemasState.dataflowChosen.name
              : '-'
          }`}
        </span>
      )}
    </Fragment>
  );
};
