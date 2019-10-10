import React, { useContext } from 'react';

import styles from './StatusList.module.scss';

import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

function StatusList({ color, filterDispatch }) {
  const resources = useContext(ResourcesContext);

  return (
    <ul className={styles.list}>
      <li className={styles.listItem}>
        <input
          id="correct"
          className={styles.checkbox}
          style={{ backgroundColor: color.CORRECT }}
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
          {resources.messages.correct}
        </label>
      </li>
      <li className={styles.listItem}>
        <input
          id="warning"
          className={styles.checkbox}
          style={{ backgroundColor: color.WARNING }}
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
          {resources.messages.warning}
        </label>
      </li>
      <li className={styles.listItem}>
        <input
          id="error"
          className={styles.checkbox}
          style={{ backgroundColor: color.ERROR }}
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
          {resources.messages.error}
        </label>
      </li>
    </ul>
  );
}

export { StatusList };
