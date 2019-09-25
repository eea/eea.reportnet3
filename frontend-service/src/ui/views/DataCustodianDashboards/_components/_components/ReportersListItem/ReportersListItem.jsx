import React from 'react';

import styles from './ReportersListItem.module.scss';

function ReportersListItem({ filterDispatch, item }) {
  return (
    <li className={styles.listItem}>
      <input
        id={item}
        className={styles.checkbox}
        type="checkbox"
        defaultChecked={true}
        onChange={e => {
          if (e.target.checked) {
            filterDispatch({
              type: 'REPORTER_CHECKBOX_ON',
              payload: { label: item }
            });
          } else {
            filterDispatch({
              type: 'REPORTER_CHECKBOX_OFF',
              payload: { label: item }
            });
          }
        }}
      />
      <label htmlFor={item} className={styles.labelItem}>
        {item}
      </label>
    </li>
  );
}

export { ReportersListItem };
