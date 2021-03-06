import { useContext } from 'react';

import colors from 'conf/colors.json';

import styles from './StatusList.module.scss';

import { LevelError } from 'ui/views/_components/LevelError';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const StatusList = ({ datasetSchemaId, filterDispatch, levelErrors, statusFilters }) => {
  const resources = useContext(ResourcesContext);
  const errorListFilters = levelErrors.map(errorLevel => {
    const errorLevelStr = errorLevel.toString();
    const errorLevelLower = errorLevelStr.toLowerCase();

    return (
      <li className={styles.listItem} key={errorLevelLower}>
        <input
          className={styles.checkbox}
          defaultChecked={statusFilters.includes(errorLevelStr) ? false : true}
          id={`${errorLevelLower}_${datasetSchemaId}`}
          onChange={e => {
            filterDispatch({
              type: e.target.checked ? 'STATUS_FILTER_ON' : 'STATUS_FILTER_OFF',
              payload: { msg: errorLevelStr }
            });
          }}
          style={{ backgroundColor: colors[errorLevelLower] }}
          type="checkbox"
        />
        <label className={styles.labelItem} htmlFor={`${errorLevelLower}_${datasetSchemaId}`}>
          <LevelError type={errorLevelLower} />
        </label>
      </li>
    );
  });
  return <ul className={styles.list}>{errorListFilters}</ul>;
};

export { StatusList };
