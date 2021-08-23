import { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './SelectAllFilters.module.scss';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

const SelectAllFilters = ({ datasetSchemaId, filterDispatch, filters, id, labels }) => {
  const resourcesContext = useContext(ResourcesContext);

  const [isChecked, setIsChecked] = useState(true);

  useEffect(() => {
    setIsChecked(isEmpty(filters));
  }, [filters]);

  return (
    <li className={styles.listItem}>
      <input
        checked={isChecked}
        className={styles.checkbox}
        id={`${id}_${'selectAll'}_${datasetSchemaId}`}
        onChange={e => {
          setIsChecked(e.target.checked);
          filterDispatch({
            type: e.target.checked
              ? id.toString().toUpperCase() + '_CHECKBOX_SELECT_ALL_ON'
              : id.toString().toUpperCase() + '_CHECKBOX_SELECT_ALL_OFF',
            payload: { allFilters: labels, reportersNotSelected: filters }
          });
        }}
        type="checkbox"
      />
      <label className={styles.labelItem} htmlFor={`${id}_${'selectAll'}_${datasetSchemaId}`}>
        {resourcesContext.messages['selectAll']}
      </label>
    </li>
  );
};

export { SelectAllFilters };
