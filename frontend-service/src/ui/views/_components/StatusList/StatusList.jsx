import React, { useContext } from 'react';

import styles from './StatusList.module.scss';
import colors from 'conf/colors.json';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const StatusList = ({ filterDispatch, filteredStatusTypes, statusTypes }) => {
  const resources = useContext(ResourcesContext);

  const statusTypesFilters = statusTypes.map((label, i) => {
    const labelLowerCase = label.toString().toLowerCase();

    return (
      <li key={i} className={styles.listItem}>
        <input
          id={`${labelLowerCase}_${i}`}
          className={styles.checkbox}
          style={{ backgroundColor: colors[labelLowerCase] }}
          type="checkbox"
          defaultChecked={!filteredStatusTypes.includes(label.toString())}
          onChange={e => {
            filterDispatch({
              type: e.target.checked ? 'CHECKBOX_ON' : 'CHECKBOX_OFF',
              payload: { label: label.toString() }
            });
          }}
        />

        <label htmlFor={`${labelLowerCase}_${i}`} className={styles.labelItem}>
          {resources.messages[labelLowerCase]}
        </label>
      </li>
    );
  });
  return <ul className={styles.list}>{statusTypesFilters}</ul>;
};
