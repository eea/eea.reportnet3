import { useContext } from 'react';

import styles from './StatusList.module.scss';
import colors from 'conf/colors.json';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const StatusList = ({ filterDispatch, filteredStatusTypes, statusTypes }) => {
  const resources = useContext(ResourcesContext);

  const statusTypesFilters = statusTypes.map((label, i) => {
    label = label.toString();
    const labelLowerCase = label.toLowerCase();

    return (
      <li className={styles.listItem} key={labelLowerCase}>
        <input
          className={styles.checkbox}
          defaultChecked={!filteredStatusTypes.includes(label)}
          id={`${labelLowerCase}_${i}`}
          onChange={e => {
            filterDispatch({
              type: e.target.checked ? 'CHECKBOX_ON' : 'CHECKBOX_OFF',
              payload: { label }
            });
          }}
          style={{ backgroundColor: colors[labelLowerCase] }}
          type="checkbox"
        />

        <label className={styles.labelItem} htmlFor={`${labelLowerCase}_${i}`}>
          {resources.messages[labelLowerCase]}
        </label>
      </li>
    );
  });
  return <ul className={styles.list}>{statusTypesFilters}</ul>;
};
