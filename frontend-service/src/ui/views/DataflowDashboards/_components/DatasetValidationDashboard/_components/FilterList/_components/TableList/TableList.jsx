import React from 'react';

import styles from './TableList.module.css';

import { TableListItem } from './TableListItem';
import { SelectAllFilters } from 'ui/views/DataflowDashboards/_components/DatasetValidationDashboard/_components/FilterList/_components/SelectAllFilters';

const TableList = ({ datasetSchemaId, filterDispatch, tableFilters, tables }) => {
  return (
    <ul className={styles.list}>
      {tables.map(table => (
        <li className={styles.listItem}>
          <TableListItem
            datasetSchemaId={datasetSchemaId}
            filterDispatch={filterDispatch}
            table={table}
            tableFilters={tableFilters}
          />
        </li>
      ))}
      {/* <SelectAllFilters
        datasetSchemaId={datasetSchemaId}
        filterDispatch={filterDispatch}
        filters={reporterFilters}
        labels={labels}
        selectedAllFilter={setSelectedAllFilterState}
      /> */}
    </ul>
  );
};

export { TableList };
