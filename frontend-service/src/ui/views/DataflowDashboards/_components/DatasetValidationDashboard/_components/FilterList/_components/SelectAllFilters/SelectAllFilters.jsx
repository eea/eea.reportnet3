import React, { useContext, useEffect, useState } from 'react';

import { isEmpty, isUndefined, isNull } from 'lodash';

import styles from './SelectAllFilters.module.css';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const SelectAllFilters = ({ datasetSchemaId, filterDispatch, reporterFilters, labels, onSelectAllReporters }) => {
  const [selectAllState, setSelectAllState] = useState('checked');
  const [checkboxState, setCheckboxState] = useState('checked');
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    let state = '';
    let checkbox = '';
    console.log('test 2');
    setCheckboxState(checkbox);
    setSelectAllState(state);
    setCheckboxState(getCheckboxStateByReporters(reporterFilters));
    onSelectAllReporters(getCheckboxStateByReporters(reporterFilters));
  }, [reporterFilters]);

  const getCheckboxStateByReporters = reporterFilters => {
    let reporters = [];
    reporterFilters.forEach(filter => {
      if (!isUndefined(filter) && !isEmpty(filter) && !isNull(filter)) {
        reporters.push(filter);
      }
    });
    if (reporters.length === 0) {
      return 'checked';
    } else {
      if (reporters.length === labels.length) {
        return 'unchecked';
      } else {
        return 'indeterminate';
      }
    }
  };

  const LabelSelectAll = () => {
    // console.log(selectAllState);
    let selectAllText =
      checkboxState === 'checked' ? resources.messages['deselectAll'] : resources.messages['selectAll'];
    return <label className={styles.labelItem}>{selectAllText}</label>;
  };

  const onSelectedAllFilter = checked => {
    console.log('test', checked, checkboxState, !checked ? 'unchecked' : 'checked');
    onSelectAllReporters(checked ? 'unchecked' : 'checked');
  };

  return (
    <>
      <li className={styles.listItem}>
        <input
          id={`${'selectAll'}_${datasetSchemaId}`}
          className={`${styles.checkbox} ${styles.checkboxState}`}
          type="checkbox"
          defaultChecked={true}
          onChange={e => {
            onSelectedAllFilter(e.target.checked);
            filterDispatch({
              type: e.target.checked ? 'REPORTER_CHECKBOX_SELECT_ALL_ON' : 'REPORTER_CHECKBOX_SELECT_ALL_OFF',
              payload: { allFilters: labels, reportersNotSelected: reporterFilters }
            });
          }}
        />
        <LabelSelectAll state={selectAllState}></LabelSelectAll>
      </li>
    </>
  );
};

export { SelectAllFilters };
