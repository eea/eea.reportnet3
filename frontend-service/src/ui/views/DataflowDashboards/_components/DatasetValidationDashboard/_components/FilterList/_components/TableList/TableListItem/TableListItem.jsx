import { useEffect, useState } from 'react';

import styles from './TableListItem.module.scss';

const TableListItem = ({ datasetSchemaId, filterDispatch, table, tableFilters }) => {
  const [isChecked, setIsChecked] = useState(true);

  useEffect(() => {
    setIsChecked(!tableFilters.includes(table.tableId));
  }, [tableFilters]);

  return (
    <div className={styles.listItem}>
      <input
        id={`${table.tableId}_${datasetSchemaId}`}
        className={styles.checkbox}
        type="checkbox"
        checked={isChecked}
        onChange={e => {
          setIsChecked(e.target.checked);
          filterDispatch({
            type: e.target.checked ? 'TABLE_CHECKBOX_ON' : 'TABLE_CHECKBOX_OFF',
            payload: { tableId: table.tableId }
          });
        }}
      />
      <label htmlFor={`${table.tableId}_${datasetSchemaId}`} className={styles.labelItem}>
        {table.tableName}
      </label>
    </div>
  );
};

export { TableListItem };
