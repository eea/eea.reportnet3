import React from 'react';

import styles from './ReportersListItem.module.scss';

export const ReportersListItem = ({ datasetSchemaId, filterDispatch, item, reporterFilters }) => {
  return (
    <li className={styles.listItem}>
      <input
        id={`${item}_${datasetSchemaId}`}
        className={styles.checkbox}
        type="checkbox"
        defaultChecked={reporterFilters.includes(item) ? false : true}
        onChange={e => {
          filterDispatch({
            type: e.target.checked ? 'REPORTER_CHECKBOX_ON' : 'REPORTER_CHECKBOX_OFF',
            payload: { label: item }
          });
        }}
      />
      <label htmlFor={`${item}_${datasetSchemaId}`} className={styles.labelItem}>
        {item}
      </label>
    </li>
  );
};
