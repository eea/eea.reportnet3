import { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './CloneSchemas.module.scss';

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
  const resourcesContext = useContext(ResourcesContext);
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
          ? `${resourcesContext.messages['filtered']} : ${cloneSchemasState.filteredData.length} | `
          : ''}
        {resourcesContext.messages['totalRecords']} {cloneSchemasState.allDataflows.length}{' '}
        {resourcesContext.messages['records'].toLowerCase()}
        {cloneSchemasState.filtered && cloneSchemasState.allDataflows.length === cloneSchemasState.filteredData.length
          ? ` (${resourcesContext.messages['filtered'].toLowerCase()})`
          : ''}
      </Fragment>
    );
  };

  const isLoading = value => cloneSchemasDispatch({ type: 'IS_LOADING', payload: { value } });

  const onChangePagination = pagination => cloneSchemasDispatch({ type: 'ON_PAGINATE', payload: { pagination } });

  const onLoadDataflows = async () => {
    try {
      let data;
      if (isReferenceDataflow) {
        data = await ReferenceDataflowService.getAll(userContext.accessRole, userContext.contextRoles);
      } else {
        data = await DataflowService.getCloneableDataflows();
      }

      cloneSchemasDispatch({
        type: 'INITIAL_LOAD',
        payload: { allDataflows: data.filter(dataflow => dataflow.id !== parseInt(dataflowId)) }
      });
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
            { name: 'legalInstrument' }
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
        <label className={styles.switchTextInput}>{resourcesContext.messages['magazineView']}</label>
        <InputSwitch checked={userContext.userProps.listView} onChange={e => userContext.onToggleTypeView(e.value)} />
        <label className={styles.switchTextInput}>{resourcesContext.messages['listView']}</label>
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
        <span>{`${resourcesContext.messages['selectedDataflow']}: `}</span>
        {!isEmpty(cloneSchemasState.chosenDataflow) ? cloneSchemasState.chosenDataflow.name : '-'}
      </span>
    </div>
  );
};
