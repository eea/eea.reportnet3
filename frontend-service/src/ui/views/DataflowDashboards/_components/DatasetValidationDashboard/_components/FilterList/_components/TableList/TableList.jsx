import React, { useState } from 'react';

import styles from './TableList.module.css';

import { TableListItem } from './TableListItem';
import { SelectAllFilters } from 'ui/views/DataflowDashboards/_components/DatasetValidationDashboard/_components/FilterList/_components/SelectAllFilters';

const TableList = ({ datasetSchemaId, filterDispatch, tableFilters, tables }) => {
  const [selectedAllFilterState, setSelectedAllFilterState] = useState('');

  return (
    <ul className={styles.list}>
      {tables.map(table => (
        <li className={styles.listItem} key={datasetSchemaId}>
          <TableListItem
            datasetSchemaId={datasetSchemaId}
            filterDispatch={filterDispatch}
            selectedAllFilterState={selectedAllFilterState}
            table={table}
            tableFilters={tableFilters}
          />
        </li>
      ))}
      <SelectAllFilters
        datasetSchemaId={datasetSchemaId}
        filterDispatch={filterDispatch}
        filters={tableFilters}
        id={'table'}
        labels={tables}
        selectedAllFilter={setSelectedAllFilterState}
      />
    </ul>
  );
};

export { TableList };
