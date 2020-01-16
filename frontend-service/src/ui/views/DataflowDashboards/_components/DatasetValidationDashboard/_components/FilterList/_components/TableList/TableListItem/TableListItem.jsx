import React, { useEffect, useState } from 'react';

import { isUndefined } from 'lodash';

import styles from './TableListItem.module.css';

export const TableListItem = ({ datasetSchemaId, filterDispatch, table, tableFilters, selectedAllFilterState }) => {
  const [selectedAll, setSelectedAll] = useState(true);
  const [isChecked, setIsChecked] = useState(true);

  useEffect(() => {
    setIsChecked(getStateBySelectionAndByReporter(areAllSelectedOrDeselected()));
    setSelectedAll(areAllSelectedOrDeselected);
  }, [selectedAllFilterState, selectedAll]);

  const getStateBySelectionAndByReporter = () => {
    let state = areAllSelectedOrDeselected();
    if (state === 'indeterminate') {
      return tableFilters.includes(table.tableId) ? false : true;
    } else {
      return state;
    }
  };

  const areAllSelectedOrDeselected = () => {
    let isChecked;
    if (!isUndefined(selectedAllFilterState)) {
      if (selectedAllFilterState === 'checked') {
        isChecked = true;
      } else if (selectedAllFilterState === 'unchecked') {
        isChecked = false;
      } else if (selectedAllFilterState === 'indeterminate') {
        isChecked = 'indeterminate';
      }
    }
    return isChecked;
  };

  return (
    <li className={styles.listItem}>
      <input
        id={`${table.tableId}_${datasetSchemaId}`}
        className={styles.checkbox}
        type="checkbox"
        checked={isChecked}
        defaultChecked={true}
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
    </li>
  );
};
