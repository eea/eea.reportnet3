import React, { useContext, useEffect, useState } from 'react';

import { isEmpty, isUndefined, isNull } from 'lodash';

import styles from './SelectAllFilters.module.css';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const SelectAllFilters = ({ datasetSchemaId, filterDispatch, filters, id, labels, selectedAllFilter }) => {
  const [checkboxState, setCheckboxState] = useState('checked');
  const [isClickedFilter, setIsClickedFilter] = useState('');
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    setCheckboxState(getCheckboxStateByClickAndFilters(filters));
    selectedAllFilter(getCheckboxStateByClickAndFilters(filters));
    setIsClickedFilter('');
  }, [filters]);

  const getStateIfIsClicked = () => {
    let state = '';
    if (isClickedFilter === 'checked') {
      state = 'checked';
    } else if (isClickedFilter === 'unchecked') {
      state = 'unchecked';
    }
    return state;
  };

  const getStateByFiltersSelected = filters => {
    let state = '';
    let reporters = [];
    filters.forEach(filter => {
      if (!isUndefined(filter) && !isEmpty(filter) && !isNull(filter)) {
        reporters.push(filter);
      }
    });
    if (reporters.length === 0) {
      state = 'checked';
    } else {
      if (reporters.length == labels.length) {
        state = 'unchecked';
      } else {
        state = 'indeterminate';
      }
    }
    return state;
  };

  const getCheckboxStateByClickAndFilters = filters => {
    let state = '';
    if (!isEmpty(isClickedFilter)) {
      state = getStateIfIsClicked();
    } else {
      state = getStateByFiltersSelected(filters);
    }
    return state;
  };

  return (
    <>
      <li className={styles.listItem}>
        <input
          id={`${id}_${'selectAll'}_${datasetSchemaId}`}
          className={styles.checkbox}
          type="checkbox"
          defaultChecked={true}
          checked={checkboxState === 'checked' ? true : false}
          onChange={e => {
            setIsClickedFilter(e.target.checked ? 'checked' : 'unchecked');
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
