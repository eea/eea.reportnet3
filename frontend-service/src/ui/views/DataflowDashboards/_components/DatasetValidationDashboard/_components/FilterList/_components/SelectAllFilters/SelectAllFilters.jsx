import React, { useContext, useEffect, useState } from 'react';

import { isEmpty, isUndefined, isNull } from 'lodash';

import styles from './SelectAllFilters.module.css';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const SelectAllFilters = ({ datasetSchemaId, filterDispatch, reporterFilters, labels, selectedAllFilter }) => {
  const [checkboxState, setCheckboxState] = useState('checked');
  const [isClickedFilter, setIsClickedFilter] = useState('');
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    setCheckboxState(getCheckboxStateByReporters(reporterFilters));
    selectedAllFilter(getCheckboxStateByReporters(reporterFilters));
    setIsClickedFilter('');
  }, [reporterFilters]);

  const getCheckboxStateByReporters = reporterFilters => {
    let state = '';
    if (!isEmpty(isClickedFilter)) {
      if (isClickedFilter === 'checked') {
        state = 'checked';
      } else if (isClickedFilter === 'unchecked') {
        state = 'unchecked';
      }
    } else {
      let reporters = [];
      reporterFilters.forEach(filter => {
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
    }
    return state;
  };

  const LabelSelectAll = () => {
    return <label className={styles.labelItem}>{resources.messages['selectAll']}</label>;
  };

  return (
    <>
      <li className={styles.listItem}>
        <input
          id={`${'selectAll'}_${datasetSchemaId}`}
          className={styles.checkbox}
          type="checkbox"
          defaultChecked={true}
          checked={checkboxState === 'checked' ? true : false}
          onChange={e => {
            setIsClickedFilter(e.target.checked ? 'checked' : 'unchecked');
            filterDispatch({
              type: e.target.checked ? 'REPORTER_CHECKBOX_SELECT_ALL_ON' : 'REPORTER_CHECKBOX_SELECT_ALL_OFF',
              payload: { allFilters: labels, reportersNotSelected: reporterFilters }
            });
          }}
        />
        <LabelSelectAll />
      </li>
    </>
  );
};

export { SelectAllFilters };
