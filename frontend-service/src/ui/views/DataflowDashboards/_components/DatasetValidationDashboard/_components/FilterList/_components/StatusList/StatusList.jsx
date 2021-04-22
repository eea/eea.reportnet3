import { useContext } from 'react';

import styles from './StatusList.module.scss';
import colors from 'conf/colors.json';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const StatusList = ({ datasetSchemaId, filterDispatch, levelErrors, statusFilters }) => {
  const resources = useContext(ResourcesContext);
  let errorListFilters = levelErrors.map((errorLevel, i) => {
    const errorLevelStr = errorLevel.toString();
    const errorLevelLower = errorLevelStr.toLowerCase();

    return (
      <li key={i} className={styles.listItem}>
        <input
          className={styles.checkbox}
          defaultChecked={statusFilters.includes(errorLevelStr) ? false : true}
          id={`${errorLevelLower}_${datasetSchemaId}`}
          style={{ backgroundColor: colors[errorLevelLower] }}
          type="checkbox"
          onChange={e => {
            filterDispatch({
              type: e.target.checked ? 'STATUS_FILTER_ON' : 'STATUS_FILTER_OFF',
              payload: { msg: errorLevelStr }
            });
          }}
        />
        <label htmlFor={`${errorLevelLower}_${datasetSchemaId}`} className={styles.labelItem}>
          {resources.messages[errorLevelLower]}
        </label>
      </li>
    );
  });
  return <ul className={styles.list}>{errorListFilters}</ul>;
};

export { StatusList };
