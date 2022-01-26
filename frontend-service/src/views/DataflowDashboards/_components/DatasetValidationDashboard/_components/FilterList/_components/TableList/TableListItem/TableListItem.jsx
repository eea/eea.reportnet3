import { useEffect, useState } from 'react';

import styles from './TableListItem.module.scss';

export const TableListItem = ({ datasetSchemaId, filterDispatch, table, tableFilters }) => {
  const [isChecked, setIsChecked] = useState(true);

  useEffect(() => {
    setIsChecked(!tableFilters.includes(table.tableId));
  }, [tableFilters]);

  return (
    <div className={styles.listItem}>
      <input
        checked={isChecked}
        className={styles.checkbox}
        id={`${table.tableId}_${datasetSchemaId}`}
        onChange={e => {
          setIsChecked(e.target.checked);
          filterDispatch({
            type: e.target.checked ? 'TABLE_CHECKBOX_ON' : 'TABLE_CHECKBOX_OFF',
            payload: { tableId: table.tableId }
          });
        }}
        type="checkbox"
      />
      <label className={styles.labelItem} htmlFor={`${table.tableId}_${datasetSchemaId}`}>
        {table.tableName}
      </label>
    </div>
  );
};
