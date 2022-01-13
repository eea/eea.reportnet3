import { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ReferencingDataflows.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { MyFilters } from 'views/_components/MyFilters';
import { Spinner } from 'views/_components/Spinner';

import { ReferenceDataflowService } from 'services/ReferenceDataflowService';

import { referencingDataflowsReducer } from './_functions/referencingDataflowsReducer';

import { useFilters } from 'views/_functions/Hooks/useFilters';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';

export const ReferencingDataflows = ({ referenceDataflowId }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [state, dispatch] = useReducer(referencingDataflowsReducer, {
    dataflows: [],
    pagination: { first: 0, page: 0, rows: 10 },
    requestStatus: 'idle'
  });

  const { filteredData } = useFilters('referencingDataflows');

  const filterOptions = [{ type: 'INPUT', nestedOptions: [{ key: 'name', label: resourcesContext.messages['name'] }] }];

  useEffect(() => {
    onLoadDataflows();
  }, []);

  const onLoadDataflows = async () => {
    dispatch({ type: 'LOADING_STARTED' });

    try {
      const referencingDataflowsResponse = await ReferenceDataflowService.getReferencingDataflows(referenceDataflowId);

      dispatch({
        type: 'LOADING_SUCCESS',
        payload: { dataflows: referencingDataflowsResponse.data }
      });
    } catch (error) {
      console.error('ReferencingDataflows - onLoadDataflows.', error);
      notificationContext.add({ type: 'LOADING_REFERENCING_DATAFLOWS_ERROR', error }, true);
    }
  };

  const onPaginate = event => {
    const pagination = { first: event.first, page: event.page, rows: event.rows };
    dispatch({ type: 'ON_PAGINATE', payload: { pagination } });
  };

  const renderNameColumnTemplate = dataflow => <div>{dataflow.name}</div>;

  const renderIdColumnTemplate = dataflow => <div>{dataflow.id}</div>;

  const renderDialogLayout = children => (
    <div className={isEmpty(state.dataflows) ? styles.modalEmpty : styles.modalData}>{children}</div>
  );

  if (state.requestStatus === 'pending') {
    return renderDialogLayout(<Spinner className={styles.spinner} />);
  }

  if (state.requestStatus === 'resolved' && state.dataflows.length === 0) {
    return renderDialogLayout(
      <div className={styles.noReferencingWrap}>
        <h3>{resourcesContext.messages['noReferencingDataflows']}</h3>
        <h3>{resourcesContext.messages['noReferencingDataflowsMoreInfo']}</h3>
      </div>
    );
  }

  const renderDialogContent = () => {
    if (isEmpty(filteredData)) {
      return (
        <div className={styles.notMatchingWrap}>
          <h3>{resourcesContext.messages['dataflowsNotMatchingFilter']}</h3>
        </div>
      );
    }

    return (
      <DataTable
        first={state.pagination.first}
        getPageChange={onPaginate}
        paginator={true}
        rows={state.pagination.rows}
        rowsPerPageOptions={[5, 10, 15]}
        value={filteredData}>
        <Column
          body={renderNameColumnTemplate}
          header={resourcesContext.messages['referencingDataflowNameColumnLabel']}
        />
        <Column
          body={renderIdColumnTemplate}
          header={resourcesContext.messages['referencingDataflowIdColumnLabel']}
          style={{ width: '120px' }}
        />
      </DataTable>
    );
  };

  return renderDialogLayout(
    <Fragment>
      <MyFilters className="referencingDataflows" data={state.dataflows} options={filterOptions} />
      {renderDialogContent()}
    </Fragment>
  );
};
