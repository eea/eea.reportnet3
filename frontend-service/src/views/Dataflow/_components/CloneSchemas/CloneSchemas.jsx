import { useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './CloneSchemas.module.scss';

import { routes } from 'conf/routes';

import { CardsView } from 'views/_components/CardsView';
import { InputSwitch } from 'views/_components/InputSwitch';
import { MyFilters } from 'views/_components/MyFilters';
import { Spinner } from 'views/_components/Spinner';
import { TableViewSchemas } from './_components/TableViewSchemas';

import { DataflowService } from 'services/DataflowService';
import { ReferenceDataflowService } from 'services/ReferenceDataflowService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { cloneSchemasReducer } from './_functions/Reducers/cloneSchemasReducer';

import { useFilters } from 'views/_functions/Hooks/useFilters';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { PaginatorRecordsCount } from 'views/_components/DataTable/_functions/Utils/PaginatorRecordsCount';

export const CloneSchemas = ({ dataflowId, getCloneDataflow, isReferenceDataflow = false }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [cloneSchemasState, cloneSchemasDispatch] = useReducer(cloneSchemasReducer, {
    allDataflows: [],
    chosenDataflow: { id: null, name: '-' },
    isLoading: true,
    pagination: { first: 0, rows: 10, page: 0 }
  });

  const { filteredData, isFiltered } = useFilters('cloneSchemas');

  useEffect(() => {
    onLoadDataflows();
  }, []);

  useEffect(() => {
    getCloneDataflow(cloneSchemasState.chosenDataflow);
  }, [cloneSchemasState.chosenDataflow]);

  const getPaginatorRight = () => (
    <PaginatorRecordsCount
      dataLength={cloneSchemasState.allDataflows.length}
      filteredDataLength={filteredData.length}
      isFiltered={isFiltered}
    />
  );

  const isLoading = value => cloneSchemasDispatch({ type: 'IS_LOADING', payload: { value } });

  const onChangePagination = pagination => cloneSchemasDispatch({ type: 'ON_PAGINATE', payload: { pagination } });

  const onLoadDataflows = async () => {
    try {
      let data;
      if (isReferenceDataflow) {
        const response = await ReferenceDataflowService.getAll(userContext.accessRole, userContext.contextRoles);
        data = response.dataflows;
      } else {
        data = await DataflowService.getCloneableDataflows();
      }

      cloneSchemasDispatch({
        type: 'INITIAL_LOAD',
        payload: { allDataflows: data.filter(dataflow => dataflow.id !== parseInt(dataflowId)) }
      });
    } catch (error) {
      console.error('CloneSchemas - onLoadDataflows.', error);
      notificationContext.add({ type: 'LOAD_DATAFLOWS_ERROR' }, true);
    } finally {
      isLoading(false);
    }
  };

  const onOpenDataflow = dataflowId => window.open(getUrl(routes.DATAFLOW, { dataflowId }, true));

  const onOpenReferenceDataflow = referenceDataflowId =>
    window.open(getUrl(routes.REFERENCE_DATAFLOW, { referenceDataflowId }, true));

  const onSelectDataflow = dataflowData => {
    cloneSchemasDispatch({ type: 'ON_SELECT_DATAFLOW', payload: { id: dataflowData.id, name: dataflowData.name } });
  };

  const filterOptions = isReferenceDataflow
    ? [
        {
          type: 'INPUT',
          nestedOptions: [
            { key: 'name', label: resourcesContext.messages['name'] },
            { key: 'description', label: resourcesContext.messages['description'] }
          ]
        },
        {
          type: 'MULTI_SELECT',
          nestedOptions: [{ key: 'status', label: resourcesContext.messages['status'], template: 'LevelError' }]
        }
      ]
    : [
        {
          type: 'INPUT',
          nestedOptions: [
            { key: 'name', label: resourcesContext.messages['name'] },
            { key: 'description', label: resourcesContext.messages['description'] },
            { key: 'obligationTitle', label: resourcesContext.messages['obligation'] },
            { key: 'legalInstrument', label: resourcesContext.messages['legalInstrument'] }
          ]
        },
        {
          type: 'MULTI_SELECT',
          nestedOptions: [{ key: 'status', label: resourcesContext.messages['status'], template: 'LevelError' }]
        },
        { type: 'DATE', key: 'expirationDate', label: resourcesContext.messages['expirationDate'] }
      ];

  const renderData = () => {
    if (userContext.userProps.listView) {
      return (
        <TableViewSchemas
          checkedDataflow={cloneSchemasState.chosenDataflow}
          data={filteredData}
          handleRedirect={isReferenceDataflow ? onOpenReferenceDataflow : onOpenDataflow}
          isReferenceDataflow={isReferenceDataflow}
          onChangePagination={onChangePagination}
          onSelectDataflow={onSelectDataflow}
          pagination={cloneSchemasState.pagination}
          paginatorRightText={getPaginatorRight()}
        />
      );
    } else {
      return (
        <CardsView
          checkedCard={cloneSchemasState.chosenDataflow}
          contentType={'Dataflows'}
          data={filteredData}
          handleRedirect={isReferenceDataflow ? onOpenReferenceDataflow : onOpenDataflow}
          isReferenceDataflow={isReferenceDataflow}
          onChangePagination={onChangePagination}
          onSelectCard={onSelectDataflow}
          pagination={cloneSchemasState.pagination}
          paginatorRightText={getPaginatorRight()}
          type={'cloneSchemas'}
        />
      );
    }
  };

  const cloneSchemaStyles = {
    justifyContent:
      cloneSchemasState.isLoading || isEmpty(cloneSchemasState.data || filteredData) ? 'flex-start' : 'space-between'
  };

  if (cloneSchemasState.isLoading) {
    return <Spinner style={{ top: 0 }} />;
  }

  return (
    <div className={styles.cloneSchemas} style={cloneSchemaStyles}>
      <div className={styles.switchDiv}>
        <label className={styles.switchTextInput}>{resourcesContext.messages['magazineView']}</label>
        <InputSwitch checked={userContext.userProps.listView} onChange={e => userContext.onToggleTypeView(e.value)} />
        <label className={styles.switchTextInput}>{resourcesContext.messages['listView']}</label>
      </div>
      <div className={styles.filters}>
        <MyFilters
          className="cloneSchemas"
          data={cloneSchemasState.allDataflows}
          options={filterOptions}
          viewType="cloneSchemas"
        />
      </div>
      {renderData()}
      <span
        className={`${styles.selectedDataflow} ${
          isEmpty(cloneSchemasState.data || filteredData) ? styles.filteredSelected : ''
        }`}>
        <span>{`${resourcesContext.messages['selectedDataflow']}: `}</span>
        {cloneSchemasState.chosenDataflow.name}
      </span>
    </div>
  );
};
