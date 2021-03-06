import uuid from 'uuid';

import styles from './TableList.module.css';

import { SelectAllFilters } from 'ui/views/DataflowDashboards/_components/DatasetValidationDashboard/_components/FilterList/_components/SelectAllFilters';
import { TableListItem } from './TableListItem';

const TableList = ({ datasetSchemaId, filterDispatch, tableFilters, tables }) => {
  return (
    <ul className={styles.list}>
      {tables.map(table => (
        <li className={styles.listItem} key={uuid.v4()}>
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
