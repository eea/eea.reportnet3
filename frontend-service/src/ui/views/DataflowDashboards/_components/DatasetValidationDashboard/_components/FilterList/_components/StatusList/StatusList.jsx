import React, { useContext } from 'react';

import styles from './StatusList.module.scss';
import colors from 'conf/colors.json';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const StatusList = ({ levelErrors, filterDispatch, statusFilters, datasetSchemaId }) => {
  const resources = useContext(ResourcesContext);
  let errorListFilters = levelErrors.map((errorLevel, i) => {
    return (
      <li key={i} className={styles.listItem}>
        <input
          id={`${errorLevel.toString().toLowerCase()}_${datasetSchemaId}`}
          className={styles.checkbox}
          style={{ backgroundColor: colors[errorLevel.toString().toLowerCase()] }}
          type="checkbox"
          defaultChecked={statusFilters.includes(errorLevel.toString()) ? false : true}
          onChange={e => {
            filterDispatch({
              type: e.target.checked ? 'STATUS_FILTER_ON' : 'STATUS_FILTER_OFF',
              payload: { msg: errorLevel.toString() }
            });
          }}
        />
        <label htmlFor={`${errorLevel.toString().toLowerCase()}_${datasetSchemaId}`} className={styles.labelItem}>
          {resources.messages[errorLevel.toString().toLowerCase()]}
        </label>
      </li>
    );
  });
  return <ul className={styles.list}>{errorListFilters}</ul>;
};
