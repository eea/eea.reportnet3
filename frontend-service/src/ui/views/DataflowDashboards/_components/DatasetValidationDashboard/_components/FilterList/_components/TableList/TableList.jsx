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
            table={table}
            tableFilters={tableFilters}
            selectedAllFilterState={selectedAllFilterState}
          />
        </li>
      ))}
      <SelectAllFilters
        id={'table'}
        datasetSchemaId={datasetSchemaId}
        filterDispatch={filterDispatch}
        filters={tableFilters}
        labels={tables}
        selectedAllFilter={setSelectedAllFilterState}
      />
    </ul>
  );
};

export { TableList };
