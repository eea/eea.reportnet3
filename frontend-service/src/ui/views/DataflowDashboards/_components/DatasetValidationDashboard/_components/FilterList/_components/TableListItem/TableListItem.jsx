import React from 'react';

import styles from './TableListItem.module.css';

export const TableListItem = ({ datasetSchemaId, filterDispatch, item, tableFilters }) => {
  return (
    <li className={styles.listItem}>
      <input
        id={`${item.tableId}_${datasetSchemaId}`}
        className={styles.checkbox}
        type="checkbox"
        defaultChecked={tableFilters.includes(item.tableId) ? false : true}
        onChange={e =>
          filterDispatch({
            type: e.target.checked ? 'TABLE_CHECKBOX_ON' : 'TABLE_CHECKBOX_OFF',
            payload: { tableId: item.tableId }
          })
        }
      />
      <label htmlFor={`${item.tableId}_${datasetSchemaId}`} className={styles.labelItem}>
        {item.tableName}
      </label>
    </li>
  );
};
