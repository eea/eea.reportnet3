import uniqueId from 'lodash/uniqueId';

import styles from './TableList.module.css';

import { SelectAllFilters } from 'views/DataflowDashboards/_components/DatasetValidationDashboard/_components/FilterList/_components/SelectAllFilters';
import { TableListItem } from './TableListItem';

export const TableList = ({ datasetSchemaId, filterDispatch, tableFilters, tables }) => (
  <ul className={styles.list}>
    {tables.map(table => (
      <li className={styles.listItem} key={uniqueId()}>
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
