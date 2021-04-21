import { useContext, useEffect, useState } from 'react';

import { isEmpty } from 'lodash';

import styles from './SelectAllFilters.module.scss';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const SelectAllFilters = ({ datasetSchemaId, filterDispatch, filters, id, labels }) => {
  const [isChecked, setIsChecked] = useState(true);
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    setIsChecked(isEmpty(filters));
  }, [filters]);

  return (
    <>
      <li className={styles.listItem}>
        <input
          id={`${id}_${'selectAll'}_${datasetSchemaId}`}
          className={styles.checkbox}
          type="checkbox"
          checked={isChecked}
          onChange={e => {
            setIsChecked(e.target.checked);
            filterDispatch({
              type: e.target.checked
                ? id.toString().toUpperCase() + '_CHECKBOX_SELECT_ALL_ON'
                : id.toString().toUpperCase() + '_CHECKBOX_SELECT_ALL_OFF',
              payload: { allFilters: labels, reportersNotSelected: filters }
            });
          }}
        />
        <label htmlFor={`${id}_${'selectAll'}_${datasetSchemaId}`} className={styles.labelItem}>
          {resources.messages['selectAll']}
        </label>
      </li>
    </>
  );
};

export { SelectAllFilters };
