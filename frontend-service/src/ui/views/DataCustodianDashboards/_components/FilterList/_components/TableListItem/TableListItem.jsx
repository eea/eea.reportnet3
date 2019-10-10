import React from 'react';

import styles from './TableListItem.module.css';

function TableListItem({ filterDispatch, item }) {
  return (
    <li className={styles.listItem}>
      <input
        id={item.tableId}
        className={styles.checkbox}
        type="checkbox"
        defaultChecked={true}
        onChange={e => {
          if (e.target.checked) {
            filterDispatch({
              type: 'TABLE_CHECKBOX_ON',
              payload: { tableId: item.tableId }
            });
          } else {
            filterDispatch({
              type: 'TABLE_CHECKBOX_OFF',
              payload: { tableId: item.tableId }
            });
          }
        }}
      />
      <label htmlFor={item.tableId} className={styles.labelItem}>
        {item.tableName}
      </label>
    </li>
  );
}

export { TableListItem };
