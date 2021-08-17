import { Fragment, useContext, useEffect, useReducer } from 'react';

import styles from './ReferencingDataflows.module.scss';

import { ReferenceDataflowService } from 'services/ReferenceDataflowService';

import { referencingDataflowsReducer } from './_functions/referencingDataflowsReducer';

import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Filters } from 'views/_components/Filters';
import { Spinner } from 'views/_components/Spinner';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';

const ReferencingDataflows = ({ referenceDataflowId }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [state, dispatch] = useReducer(referencingDataflowsReducer, {
    dataflows: [],
    error: '',
    filteredData: [],
    pagination: { first: 0, page: 0, rows: 10 },
    requestStatus: 'idle'
  });

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
      notificationContext.add({ type: 'LOADING_REFERENCING_DATAFLOWS_ERROR', error });
    }
  };

  const onPaginate = event => {
    const pagination = { first: event.first, page: event.page, rows: event.rows };
    dispatch({ type: 'ON_PAGINATE', payload: { pagination } });
  };

  const onLoadFilteredData = dataflows => {
    dispatch({ type: 'ON_LOAD_FILTERED_DATA', payload: { dataflows } });
  };

  const filterOptions = [{ type: 'input', properties: [{ name: 'name' }] }];

  const renderNameColumnTemplate = dataflow => <div>{dataflow.name}</div>;
  const renderIdColumnTemplate = dataflow => <div>{dataflow.id}</div>;

  const renderDialogLayout = children => <div className={styles.modalSize}>{children}</div>;

  if (state.requestStatus === 'pending') {
    return renderDialogLayout(<Spinner className={styles.spinner} />);
  }

  if (state.requestStatus === 'resolved' && state.dataflows.length === 0) {
    return renderDialogLayout(
      <div className={styles.noReferencingWrap}>
        <h3>{resources.messages['noReferencingDataflows']}</h3>
      </div>
    );
  }

  return renderDialogLayout(
    <Fragment>
      <Filters data={state.dataflows} getFilteredData={onLoadFilteredData} options={filterOptions} />

      {state.filteredData.length === 0 ? (
        <div className={styles.notMatchingWrap}>
          <h3>{resources.messages['dataflowsNotMatchingFilter']}</h3>
        </div>
      ) : (
        <DataTable
          first={state.pagination.first}
          getPageChange={onPaginate}
          paginator={true}
          rows={state.pagination.rows}
          rowsPerPageOptions={[5, 10, 15]}
          value={state.filteredData}>
          <Column body={renderNameColumnTemplate} header={resources.messages['referencingDataflowNameColumnLabel']} />
          <Column
            body={renderIdColumnTemplate}
            header={resources.messages['referencingDataflowIdColumnLabel']}
            style={{ width: '120px' }}
          />
        </DataTable>
      )}
    </Fragment>
  );
};

export { ReferencingDataflows };
