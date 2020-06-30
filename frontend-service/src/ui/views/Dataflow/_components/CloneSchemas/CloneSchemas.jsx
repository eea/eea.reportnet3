import React, { useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './CloneSchemas.module.scss';

import { dataflowRoles } from 'conf/dataflow.config.json';
import { routes } from 'ui/routes';

import { CardsView } from 'ui/views/_components/CardsView';
import { Filters } from 'ui/views/_components/Filters';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { Spinner } from 'ui/views/_components/Spinner';
import { TableViewSchemas } from './_components/TableViewSchemas';

import { DataflowService } from 'core/services/Dataflow';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { cloneSchemasReducer } from './_functions/Reducers/cloneSchemasReducer';

import { CloneSchemasUtils } from './_functions/Utils/CloneSchemasUtils';
import { getUrl } from 'core/infrastructure/CoreUtils';

export const CloneSchemas = ({ dataflowId, getCloneDataflow }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [cloneSchemasState, cloneSchemasDispatch] = useReducer(cloneSchemasReducer, {
    accepted: [],
    allDataflows: {},
    chosenDataflow: { id: null, name: '' },
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
    getCloneDataflow(cloneSchemasState.chosenDataflow);
  }, [cloneSchemasState.chosenDataflow]);

  const isLoading = value => cloneSchemasDispatch({ type: 'IS_LOADING', payload: { value } });

  const onChangePagination = pagination => cloneSchemasDispatch({ type: 'ON_PAGINATE', payload: { pagination } });

  const onLoadDataflows = async () => {
    try {
      const allDataflows = await DataflowService.all(user.contextRoles);
      cloneSchemasDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          accepted: parseDataflowList(allDataflows.accepted),
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

  const onOpenDataflow = dataflowId => window.open(getUrl(routes.DATAFLOW, { dataflowId }, true));

  const onSelectDataflow = dataflowData => {
    cloneSchemasDispatch({ type: 'ON_SELECT_DATAFLOW', payload: { id: dataflowData.id, name: dataflowData.name } });
  };

  const onToggleView = () => {
    cloneSchemasDispatch({ type: 'ON_TOGGLE_VIEW', payload: { view: !cloneSchemasState.isTableView } });
  };

  const renderData = () =>
    cloneSchemasState.isTableView ? (
      <TableViewSchemas
        checkedDataflow={cloneSchemasState.chosenDataflow}
        data={cloneSchemasState.filteredData}
        handleRedirect={onOpenDataflow}
        onChangePagination={onChangePagination}
        onSelectDataflow={onSelectDataflow}
        pagination={cloneSchemasState.pagination}
      />
    ) : (
      <CardsView
        checkedCard={cloneSchemasState.chosenDataflow}
        contentType={'Dataflows'}
        data={cloneSchemasState.filteredData}
        handleRedirect={onOpenDataflow}
        onChangePagination={onChangePagination}
        onSelectCard={onSelectDataflow}
        pagination={cloneSchemasState.pagination}
      />
    );

  const parseDataflowList = dataflows => {
    return dataflows.filter(
      dataflow => dataflow.id !== parseInt(dataflowId) && dataflow.userRole === dataflowRoles.DATA_CUSTODIAN
    );
  };

  const cloneSchemaStyles = {
    justifyContent:
      cloneSchemasState.isLoading || isEmpty(cloneSchemasState.data || cloneSchemasState.filteredData)
        ? 'flex-start'
        : 'space-between'
  };

  if (cloneSchemasState.isLoading) return <Spinner style={{ top: 0 }} />;

  return (
    <div className={styles.cloneSchemas} style={cloneSchemaStyles}>
      <div className={styles.switchDiv}>
        <label className={styles.switchTextInput}>{resources.messages['magazineView']}</label>
        <InputSwitch checked={cloneSchemasState.isTableView} onChange={() => onToggleView()} />
        <label className={styles.switchTextInput}>{resources.messages['listView']}</label>
      </div>

      <div className={styles.filters}>
        <Filters
          data={cloneSchemasState.accepted}
          dateOptions={['expirationDate']}
          getFilteredData={onLoadFilteredData}
          inputOptions={['name', 'description', 'legalInstrument', 'obligationTitle']}
          selectOptions={['status']}
        />
      </div>

      {renderData()}

      <span
        className={`${styles.selectedDataflow} ${
          isEmpty(cloneSchemasState.data || cloneSchemasState.filteredData) ? styles.filteredSelected : ''
        }`}>
        <span>{`${resources.messages['selectedDataflow']}: `}</span>
        {!isEmpty(cloneSchemasState.chosenDataflow) ? cloneSchemasState.chosenDataflow.name : '-'}
      </span>
    </div>
  );
};
