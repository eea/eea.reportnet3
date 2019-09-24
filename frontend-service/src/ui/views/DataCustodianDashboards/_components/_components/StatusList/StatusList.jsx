import React from 'react';

import styles from './StatusList.module.scss';

function StatusList({ filterDispatch }) {
  return (
    <ul className={styles.list}>
      <li className={styles.listItem}>
        <input
          id="correct"
          className={styles.checkbox}
          data-status="correct"
          type="checkbox"
          defaultChecked={true}
          onChange={e => {
            if (e.target.checked) {
              filterDispatch({
                type: 'STATUS_FILTER_ON',
                payload: { msg: 'CORRECT' }
              });
            } else {
              filterDispatch({
                type: 'STATUS_FILTER_OFF',
                payload: { msg: 'CORRECT' }
              });
            }
          }}
        />
        <label htmlFor="correct" className={styles.labelItem}>
          Correct
        </label>
      </li>
      <li className={styles.listItem}>
        <input
          id="warning"
          className={styles.checkbox}
          data-status="correct"
          type="checkbox"
          defaultChecked={true}
          onChange={e => {
            if (e.target.checked) {
              filterDispatch({
                type: 'STATUS_FILTER_ON',
                payload: { msg: 'WARNINGS' }
              });
            } else {
              filterDispatch({
                type: 'STATUS_FILTER_OFF',
                payload: { msg: 'WARNINGS' }
              });
            }
          }}
        />
        <label htmlFor="warning" className={styles.labelItem}>
          Warnings
        </label>
      </li>
      <li className={styles.listItem}>
        <input
          id="error"
          className={styles.checkbox}
          data-status="correct"
          type="checkbox"
          defaultChecked={true}
          onChange={e => {
            if (e.target.checked) {
              filterDispatch({
                type: 'STATUS_FILTER_ON',
                payload: { msg: 'ERRORS' }
              });
            } else {
              filterDispatch({
                type: 'STATUS_FILTER_OFF',
                payload: { msg: 'ERRORS' }
              });
            }
          }}
        />
        <label htmlFor="error" className={styles.labelItem}>
          Errors
        </label>
      </li>
    </ul>
  );
}

export { StatusList };
