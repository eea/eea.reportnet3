import React from 'react';

import styles from './StatusList.module.scss';

function StatusList({ filterDispatch }) {
  return (
    <ul className={styles.list}>
      <li className={styles.correct}>
        <input
          id="correct"
          className={styles.checkbox}
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
        <label htmlFor="warning">Correct</label>
      </li>
      <li className={styles.warning}>
        <input
          id="warning"
          className={styles.checkbox}
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
        <label htmlFor="warning">Warnings</label>
      </li>
      <li className={styles.error}>
        <input
          id="error"
          className={styles.checkbox}
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
        <label htmlFor="error">Errors</label>
      </li>
    </ul>
  );
}

export { StatusList };
