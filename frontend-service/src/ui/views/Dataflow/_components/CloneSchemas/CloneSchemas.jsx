import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './CloneSchemas.module.scss';

import { CardsView } from 'ui/views/_components/CardsView';
import { Filters } from 'ui/views/_components/Filters';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { TableViewSchemas } from './_components/TableViewSchemas';

import { DataflowService } from 'core/services/Dataflow';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { cloneSchemasReducer } from './_functions/Reducers/cloneSchemasReducer';

import { CloneSchemasUtils } from './_functions/Utils/CloneSchemasUtils';

export const CloneSchemas = ({ dataflowId, getCloneDataflowId }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [cloneSchemasState, cloneSchemasDispatch] = useReducer(cloneSchemasReducer, {
    accepted: [],
    allDataflows: {},
    chosenDataflow: {},
    chosenDataflowId: null,
    completed: [],
    filteredData: [],
    isLoading: true,
    isTableView: true,
    pagination: { first: 0, rows: 10, page: 0 },
    pending: []
  });

  useEffect(() => {
    onLoadDataflows();
  }, []);

  useEffect(() => {
    getCloneDataflowId(cloneSchemasState.chosenDataflowId);
  }, [cloneSchemasState.chosenDataflowId]);

  const isLoading = value => cloneSchemasDispatch({ type: 'IS_LOADING', payload: { value } });

  const onChangePagination = pagination => {
    cloneSchemasDispatch({ type: 'ON_PAGINATE', payload: { pagination } });
  };

  const onLoadDataflows = async () => {
    isLoading(true);
    try {
      const allDataflows = await DataflowService.all(user.contextRoles);
      cloneSchemasDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          accepted: allDataflows.accepted.filter(dataflow => dataflow.id !== parseInt(dataflowId)),
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

  const onSelectDataflow = rowData => {
    cloneSchemasDispatch({ type: 'ON_SELECT_DATAFLOW', payload: { rowData } });
  };

  const onToggleView = () => {
    cloneSchemasDispatch({ type: 'ON_TOGGLE_VIEW', payload: { view: !cloneSchemasState.isTableView } });
  };

  const renderData = () =>
    cloneSchemasState.isTableView ? (
      <TableViewSchemas
        checkedDataflow={cloneSchemasState.chosenDataflow}
        data={cloneSchemasState.filteredData}
        onChangePagination={onChangePagination}
        onSelectDataflow={onSelectDataflow}
        pagination={cloneSchemasState.pagination}
        selectedDataflowId={cloneSchemasState.chosenDataflowId}
      />
    ) : (
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
    <div className={styles.cloneSchemas}>
      <div className={styles.viewsFilters}>
        <span className={styles.filters}>
          <Filters
            data={cloneSchemasState.accepted}
            dateOptions={['expirationDate']}
            getFilteredData={onLoadFilteredData}
            inputOptions={['name', 'description']}
            selectOptions={['status']}
          />
        </span>
        <span className={styles.switchDiv}>
          <label className={styles.switchTextInput}>{resources.messages['magazineView']}</label>
          <InputSwitch checked={cloneSchemasState.isTableView} onChange={() => onToggleView()} />
          <label className={styles.switchTextInput}>{resources.messages['listView']}</label>
        </span>
      </div>

      {renderData()}

      {!cloneSchemasState.isLoading && (
        <span
          className={`${styles.selectedDataflow} ${
            isEmpty(cloneSchemasState.data) || isEmpty(cloneSchemasState.searchedData) ? styles.filteredSelected : ''
          }`}>
          <span>{`${resources.messages['selectedDataflow']}: `}</span>
          {`${!isEmpty(cloneSchemasState.chosenDataflow) ? cloneSchemasState.chosenDataflow.name : '-'}`}
        </span>
      )}
    </div>
  );
};
