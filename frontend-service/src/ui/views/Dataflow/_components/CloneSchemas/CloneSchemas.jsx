import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './CloneSchemas.module.scss';

import { config } from 'conf';
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

import { getUrl } from 'core/infrastructure/CoreUtils';

export const CloneSchemas = ({ dataflowId, getCloneDataflow }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [cloneSchemasState, cloneSchemasDispatch] = useReducer(cloneSchemasReducer, {
    allDataflows: [],
    chosenDataflow: { id: null, name: '' },
    filtered: false,
    filteredData: [],
    isLoading: true,
    pagination: { first: 0, rows: 10, page: 0 }
  });

  useEffect(() => {
    onLoadDataflows();
  }, []);

  useEffect(() => {
    getCloneDataflow(cloneSchemasState.chosenDataflow);
  }, [cloneSchemasState.chosenDataflow]);

  const getFilteredState = value => cloneSchemasDispatch({ type: 'IS_FILTERED', payload: { value } });

  const getPaginatorRecordsCount = () => {
    return (
      <Fragment>
        {cloneSchemasState.filtered && cloneSchemasState.allDataflows.length !== cloneSchemasState.filteredData.length
          ? `${resources.messages['filtered']} : ${cloneSchemasState.filteredData.length} | `
          : ''}
        {resources.messages['totalRecords']} {cloneSchemasState.allDataflows.length}{' '}
        {resources.messages['records'].toLowerCase()}
        {cloneSchemasState.filtered && cloneSchemasState.allDataflows.length === cloneSchemasState.filteredData.length
          ? ` (${resources.messages['filtered'].toLowerCase()})`
          : ''}
      </Fragment>
    );
  };

  const isLoading = value => cloneSchemasDispatch({ type: 'IS_LOADING', payload: { value } });

  const onChangePagination = pagination => cloneSchemasDispatch({ type: 'ON_PAGINATE', payload: { pagination } });

  const onLoadDataflows = async () => {
    try {
      const { data } = await DataflowService.all(userContext.contextRoles);
      cloneSchemasDispatch({ type: 'INITIAL_LOAD', payload: { allDataflows: parseDataflowList(data) } });
    } catch (error) {
      console.error('onLoadDataflows error: ', error);
      notificationContext.add({ type: 'LOAD_DATAFLOWS_ERROR' });
    } finally {
      isLoading(false);
    }
  };

  const onLoadFilteredData = data => cloneSchemasDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const onOpenDataflow = dataflowId => window.open(getUrl(routes.DATAFLOW, { dataflowId }, true));

  const onSelectDataflow = dataflowData => {
    cloneSchemasDispatch({ type: 'ON_SELECT_DATAFLOW', payload: { id: dataflowData.id, name: dataflowData.name } });
  };

  const parseDataflowList = dataflows => {
    let parsedDataflows = dataflows.filter(
      dataflow => dataflow.id !== parseInt(dataflowId) && dataflow.userRole === config.permissions['DATA_CUSTODIAN']
    );

    let dataflowsToFilter = [];

    parsedDataflows.forEach(dataflow => {
      let dataflowToFilter = {};
      dataflowToFilter.id = dataflow.id;
      dataflowToFilter.name = dataflow.name;
      dataflowToFilter.description = dataflow.description;
      dataflowToFilter.obligationTitle = dataflow.obligation ? dataflow.obligation.title : null;
      dataflowToFilter.legalInstruments = dataflow.obligation ? dataflow.obligation.legalInstruments.alias : null;
      dataflowToFilter.status = dataflow.status;
      dataflowToFilter.expirationDate = dataflow.expirationDate;
      dataflowsToFilter.push(dataflowToFilter);
    });

    return dataflowsToFilter;
  };

  // const filterOptions = {
  //   input: { properties: ['name', 'description', 'obligationTitle', 'legalInstruments'] },
  //   multiselect: { properties: ['status'] },
  //   date: { properties: ['expirationDate'] }
  // };

  const filterOptions = [
    {
      type: 'input',
      properties: [{ name: 'name' }, { name: 'description' }, { name: 'obligationTitle' }, { name: 'legalInstruments' }]
    },
    { type: 'multiselect', properties: [{ name: 'status' }] },
    { type: 'date', properties: [{ name: 'expirationDate' }] }
  ];

  const renderData = () =>
    userContext.userProps.listView ? (
      <TableViewSchemas
        checkedDataflow={cloneSchemasState.chosenDataflow}
        data={cloneSchemasState.filteredData}
        handleRedirect={onOpenDataflow}
        onChangePagination={onChangePagination}
        onSelectDataflow={onSelectDataflow}
        pagination={cloneSchemasState.pagination}
        paginatorRightText={getPaginatorRecordsCount()}
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
        paginatorRightText={getPaginatorRecordsCount()}
        type={'cloneSchemas'}
      />
    );

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
        <InputSwitch checked={userContext.userProps.listView} onChange={e => userContext.onToggleTypeView(e.value)} />
        <label className={styles.switchTextInput}>{resources.messages['listView']}</label>
      </div>
      <div className={styles.filters}>
        <Filters
          options={filterOptions}
          data={cloneSchemasState.allDataflows}
          // dateOptions={['expirationDate']}
          getFilteredData={onLoadFilteredData}
          getFilteredSearched={getFilteredState}
          // inputOptions={['name', 'description', 'obligationTitle', 'legalInstruments']}
          // selectOptions={['status']}
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
