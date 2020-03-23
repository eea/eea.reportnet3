import React, { useContext } from 'react';

import styles from './StatusList.module.scss';
import colors from 'conf/colors.json';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const StatusList = ({ datasetSchemaId, filterDispatch, levelErrors, statusFilters }) => {
  const resources = useContext(ResourcesContext);
  let errorListFilters = levelErrors.map((errorLevel, i) => {
    return (
      <li key={i} className={styles.listItem}>
        <input
          className={styles.checkbox}
          defaultChecked={statusFilters.includes(errorLevel.toString()) ? false : true}
          id={`${errorLevel.toString().toLowerCase()}_${datasetSchemaId}`}
          style={{ backgroundColor: colors[errorLevel.toString().toLowerCase()] }}
          type="checkbox"
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

export { StatusList };
