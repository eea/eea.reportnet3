import React, { useContext, useState } from 'react';

import styles from './SelectAllFilters.module.css';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const SelectAllFilters = ({ datasetSchemaId, filterDispatch, reporterFilters, labels, onSelectAllReporters }) => {
  const [checkboxState, setCheckboxState] = useState('CHECKED');
  const resources = useContext(ResourcesContext);

  const LabelSelectAll = () => {
    console.log(checkboxState);
    let selectAllText =
      checkboxState === 'CHECKED' ? resources.messages['deselectAll'] : resources.messages['selectAll'];
    return <label className={styles.labelItem}>{selectAllText}</label>;
  };

  const onSelectAll = () => {
    // onSelectAllReporters(true);
  };

  return (
    <>
      <li className={styles.listItem}>
        <input
          id={`${'selectAll'}_${datasetSchemaId}`}
          className={styles.checkbox}
          type="checkbox"
          defaultChecked={true}
          onChange={e => {
            setCheckboxState(e.target.checked ? 'CHECKED' : 'UNCHECKED');
            filterDispatch({
              type: e.target.checked ? 'REPORTER_CHECKBOX_SELECT_ALL_ON' : 'REPORTER_CHECKBOX_SELECT_ALL_OFF',
              payload: { allFilters: labels, reportersNotSelected: reporterFilters }
            });
          }}
        />
        <LabelSelectAll></LabelSelectAll>
      </li>
    </>
  );
};

export { SelectAllFilters };
