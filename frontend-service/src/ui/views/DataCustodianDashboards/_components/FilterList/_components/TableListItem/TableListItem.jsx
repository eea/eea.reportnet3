import React from 'react';

import styles from './TableListItem.module.css';

export const TableListItem = ({ arrayTableFilter, filterDispatch, item }) => {
  return (
    <li className={styles.listItem}>
      <input
        id={item.tableId}
        className={styles.checkbox}
        type="checkbox"
        defaultChecked={arrayTableFilter.includes(item.tableId) ? false : true}
        onChange={e =>
          filterDispatch({
            type: e.target.checked ? 'TABLE_CHECKBOX_ON' : 'TABLE_CHECKBOX_OFF',
            payload: { tableId: item.tableId }
          })
        }
      />
      <label htmlFor={item.tableId} className={styles.labelItem}>
        {item.tableName}
      </label>
    </li>
  );
};
