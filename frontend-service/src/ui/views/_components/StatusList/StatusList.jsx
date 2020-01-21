import React, { useContext } from 'react';

import styles from './StatusList.module.scss';
import colors from 'conf/colors.json';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const StatusList = ({ filterDispatch, filteredStatusTypes, statusTypes }) => {
  const resources = useContext(ResourcesContext);
  let statusTypesFilters = statusTypes.map((label, i) => {
    return (
      <li key={i} className={styles.listItem}>
        <input
          id={`${label.toString().toLowerCase()}_${i}`}
          className={styles.checkbox}
          style={{ backgroundColor: colors[label.toString().toLowerCase()] }}
          type="checkbox"
          defaultChecked={filteredStatusTypes.includes(label.toString()) ? false : true}
          onChange={e => {
            filterDispatch({
              type: e.target.checked ? 'CHECKBOX_ON' : 'CHECKBOX_OFF',
              payload: { label: label.toString() }
            });
          }}
        />
        <label htmlFor={`${label.toString().toLowerCase()}_${i}`} className={styles.labelItem}>
          {resources.messages[label.toString().toLowerCase()]}
        </label>
      </li>
    );
  });
  return <ul className={styles.list}>{statusTypesFilters}</ul>;
};
