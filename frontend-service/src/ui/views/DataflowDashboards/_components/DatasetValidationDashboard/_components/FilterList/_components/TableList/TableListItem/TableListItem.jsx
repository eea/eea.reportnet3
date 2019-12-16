import React from 'react';

import styles from './TableListItem.module.css';

export const TableListItem = ({ datasetSchemaId, filterDispatch, table, tableFilters }) => {
  return (
    <li className={styles.listItem}>
      <input
        id={`${table.tableId}_${datasetSchemaId}`}
        className={styles.checkbox}
        type="checkbox"
        defaultChecked={tableFilters.includes(table.tableId) ? false : true}
        onChange={e =>
          filterDispatch({
            type: e.target.checked ? 'TABLE_CHECKBOX_ON' : 'TABLE_CHECKBOX_OFF',
            payload: { tableId: table.tableId }
          })
        }
      />
      <label htmlFor={`${table.tableId}_${datasetSchemaId}`} className={styles.labelItem}>
        {table.tableName}
      </label>
    </li>
  );
};
