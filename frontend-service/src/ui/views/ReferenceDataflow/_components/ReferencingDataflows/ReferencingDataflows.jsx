import { useContext, useEffect, useReducer } from 'react';

import styles from './ReferencingDataflows.module.scss';

import { referencingDataflowsReducer } from './_functions/referencingDataflowsReducer';

import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { Filters } from 'ui/views/_components/Filters';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Fragment } from 'react';

const ReferencingDataflows = ({ referenceDataflowId }) => {
  const resources = useContext(ResourcesContext);
  const [state, dispatch] = useReducer(referencingDataflowsReducer, {
    dataflows: [],
    error: '',
    filteredData: [],
    pagination: { first: 0, page: 0, rows: 10 },
    requestStatus: 'idle'
  });

  useEffect(() => {
    //fetch data
    //setDataflows
  }, []);

  //make api call

  //add filters
  //add table
  //add pagination

  //spinner on fetch
  //message on empty array
  // filters and table when has data

  const onPaginate = event => {
    const pagination = { first: event.first, page: event.page, rows: event.rows };
    dispatch({ type: 'ON_PAGINATE', payload: { pagination } });
  };

  const onLoadFilteredData = dataflows => {
    dispatch({ type: 'ON_LOAD_FILTERED_DATA', payload: { dataflows } });
  };

  const filterOptions = [{ type: 'input', properties: [{ name: 'name' }] }];

  const renderNameColumnTemplate = dataflow => <div>{dataflow.name}</div>;

  return (
    <Fragment>
      <Filters data={state.dataflows} getFilteredData={onLoadFilteredData} options={filterOptions} />

      <DataTable
        first={state.pagination.first}
        getPageChange={onPaginate}
        paginator={true}
        rows={state.pagination.rows}
        rowsPerPageOptions={[5, 10, 15]}
        value={state.filteredData}>
        <Column body={renderNameColumnTemplate} header={resources.messages['referencingDataflowNameColumnLabel']} />
      </DataTable>
    </Fragment>
  );
};

export { ReferencingDataflows };
