import { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './CloneSchemas.module.scss';

import { config } from 'conf';
import { routes } from 'conf/routes';

import { CardsView } from 'views/_components/CardsView';
import { Filters } from 'views/_components/Filters';
import { InputSwitch } from 'views/_components/InputSwitch';
import { Spinner } from 'views/_components/Spinner';
import { TableViewSchemas } from './_components/TableViewSchemas';

import { DataflowService } from 'services/DataflowService';
import { ReferenceDataflowService } from 'services/ReferenceDataflowService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { cloneSchemasReducer } from './_functions/Reducers/cloneSchemasReducer';

import { getUrl } from 'repositories/_utils/UrlUtils';

export const CloneSchemas = ({ dataflowId, getCloneDataflow, isReferenceDataflow = false }) => {
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
      if (isReferenceDataflow) {
        const { data } = await ReferenceDataflowService.all(userContext.contextRoles);
        cloneSchemasDispatch({ type: 'INITIAL_LOAD', payload: { allDataflows: cloneableDataflowList(data) } });
      } else {
        const { data } = await DataflowService.all(userContext.contextRoles);
        cloneSchemasDispatch({ type: 'INITIAL_LOAD', payload: { allDataflows: cloneableDataflowList(data) } });
      }
    } catch (error) {
      console.error('CloneSchemas - onLoadDataflows.', error);
      notificationContext.add({ type: 'LOAD_DATAFLOWS_ERROR' });
    } finally {
      isLoading(false);
    }
  };

  const onLoadFilteredData = data => cloneSchemasDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const onOpenDataflow = dataflowId => window.open(getUrl(routes.DATAFLOW, { dataflowId }, true));

  const onOpenReferenceDataflow = referenceDataflowId =>
    window.open(getUrl(routes.REFERENCE_DATAFLOW, { referenceDataflowId }, true));

  const onSelectDataflow = dataflowData => {
    cloneSchemasDispatch({ type: 'ON_SELECT_DATAFLOW', payload: { id: dataflowData.id, name: dataflowData.name } });
  };

  const cloneableDataflowList = dataflows => {
    let cloneableDataflows = dataflows.filter(
      dataflow => dataflow.id !== parseInt(dataflowId) && dataflow.userRole === config.permissions.roles.CUSTODIAN.label
    );

    let dataflowsToFilter = [];

    cloneableDataflows.forEach(dataflow => {
      let dataflowToFilter = {};
      dataflowToFilter.id = dataflow.id;
      dataflowToFilter.name = dataflow.name;
      dataflowToFilter.description = dataflow.description;
      dataflowToFilter.obligationTitle = dataflow.obligation?.title;
      dataflowToFilter.legalInstruments = dataflow.obligation?.legalInstruments?.alias;
      dataflowToFilter.status = dataflow.status;
      dataflowToFilter.expirationDate = dataflow.expirationDate;
      dataflowsToFilter.push(dataflowToFilter);
    });

    return dataflowsToFilter;
  };

  const filterOptions = isReferenceDataflow
    ? [
        {
          type: 'input',
          properties: [{ name: 'name' }, { name: 'description' }]
        },
        { type: 'multiselect', properties: [{ name: 'status' }] }
      ]
    : [
        {
          type: 'input',
          properties: [
            { name: 'name' },
            { name: 'description' },
            { name: 'obligationTitle' },
            { name: 'legalInstruments' }
          ]
        },
        { type: 'multiselect', properties: [{ name: 'status' }] },
        { type: 'date', properties: [{ name: 'expirationDate' }] }
      ];

  const renderData = () =>
    userContext.userProps.listView ? (
      <TableViewSchemas
        checkedDataflow={cloneSchemasState.chosenDataflow}
        data={cloneSchemasState.filteredData}
        handleRedirect={isReferenceDataflow ? onOpenReferenceDataflow : onOpenDataflow}
        isReferenceDataflow={isReferenceDataflow}
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
        handleRedirect={isReferenceDataflow ? onOpenReferenceDataflow : onOpenDataflow}
        isReferenceDataflow={isReferenceDataflow}
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
          data={cloneSchemasState.allDataflows}
          getFilteredData={onLoadFilteredData}
          getFilteredSearched={getFilteredState}
          options={filterOptions}
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
