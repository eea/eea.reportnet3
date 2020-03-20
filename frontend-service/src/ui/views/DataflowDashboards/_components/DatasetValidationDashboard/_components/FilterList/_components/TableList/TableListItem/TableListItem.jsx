import React, { useEffect, useState } from 'react';

import styles from './TableListItem.module.css';

export const TableListItem = ({ datasetSchemaId, filterDispatch, table, tableFilters, selectedAllFilterState }) => {
  const [selectedAll, setSelectedAll] = useState(true);
  const [isChecked, setIsChecked] = useState(true);

  useEffect(() => {
    setIsChecked(getStateBySelectionAndByReporter());
    setSelectedAll(selectedAllFilterState);
  }, [selectedAllFilterState, selectedAll]);

  const getStateBySelectionAndByReporter = () => {
    if (selectedAllFilterState === 'indeterminate') {
      return !tableFilters.includes(table.tableId);
    } else {
      return selectedAllFilterState;
    }
  };

  return (
    <div className={styles.listItem}>
      <input
        id={`${table.tableId}_${datasetSchemaId}`}
        className={styles.checkbox}
        type="checkbox"
        defaultChecked={isChecked}
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
