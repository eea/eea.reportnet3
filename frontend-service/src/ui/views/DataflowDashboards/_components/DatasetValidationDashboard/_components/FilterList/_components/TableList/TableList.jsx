import styles from './TableList.module.css';

import uuid from 'uuid';

import { TableListItem } from './TableListItem';
import { SelectAllFilters } from 'ui/views/DataflowDashboards/_components/DatasetValidationDashboard/_components/FilterList/_components/SelectAllFilters';

const TableList = ({ datasetSchemaId, filterDispatch, tableFilters, tables }) => {
  return (
    <ul className={styles.list}>
      {tables.map(table => (
        <li key={uuid.v4()} className={styles.listItem}>
          <TableListItem
            datasetSchemaId={datasetSchemaId}
            filterDispatch={filterDispatch}
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
      />
    </ul>
  );
};

export { TableList };
