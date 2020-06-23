import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './CloneSchemas.module.scss';

import { CardsView } from 'ui/views/_components/CardsView';
import { Filters } from 'ui/views/_components/Filters';
import { InputSwitch } from 'ui/views/_components/InputSwitch';

import { DataflowService } from 'core/services/Dataflow';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { cloneSchemasReducer } from './_functions/Reducers/cloneSchemasReducer';

import { CloneSchemasUtils } from './_functions/Utils/CloneSchemasUtils';

export const CloneSchemas = dataflowId => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [cloneSchemasState, cloneSchemasDispatch] = useReducer(cloneSchemasReducer, {
    accepted: [],
    allDataflows: {},
    completed: [],
    filteredData: [],
    isLoading: true,
    pending: []
  });

  useEffect(() => {
    onLoadDataflows();
  }, []);

  const isLoading = value => cloneSchemasDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadDataflows = async () => {
    isLoading(true);
    try {
      const allDataflows = await DataflowService.all(user.contextRoles);
      cloneSchemasDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          accepted: CloneSchemasUtils.parseDataflowsList(allDataflows.accepted),
          allDataflows,
          completed: allDataflows.completed,
          pending: allDataflows.pending
        }
      });
    } catch (error) {
      console.error('onLoadDataflows error: ', error);
    } finally {
      isLoading(false);
    }
  };

  const onLoadFilteredData = data => cloneSchemasDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const onToggleView = () => {
    cloneSchemasDispatch({ type: 'ON_TOGGLE_VIEW', payload: { view: !cloneSchemasState.isTableView } });
  };

  const renderCardView = () => (
    <CardsView
      // checkedCard={reportingObligationState.oblChoosed}
      data={cloneSchemasState.filteredData}
      // handleRedirect={onOpenObligation}
      // onChangePagination={onChangePagination}
      // onSelectCard={onSelectObl}
      // pagination={reportingObligationState.pagination}
    />
  );

  return (
    <Fragment>
      <div className={styles.switchDiv}>
        <label className={styles.switchTextInput}>{resources.messages['magazineView']}</label>
        <InputSwitch checked={cloneSchemasState.isTableView} onChange={() => onToggleView()} />
        <label className={styles.switchTextInput}>{resources.messages['listView']}</label>
      </div>

      <div className={styles.filters}>
        <Filters
          data={cloneSchemasState.accepted}
          getFilteredData={onLoadFilteredData}
          inputOptions={['name', 'description']}
        />
      </div>

      {renderCardView()}

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
