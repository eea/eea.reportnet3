import React from 'react';

import styles from './ReportersListItem.module.scss';

export const ReportersListItem = ({ filterDispatch, item, reporterFilters }) => {
  return (
    <li className={styles.listItem}>
      <input
        id={item}
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
      <label htmlFor={item} className={styles.labelItem}>
        {item}
      </label>
    </li>
  );
};
