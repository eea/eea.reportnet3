import React, { useEffect, useState } from 'react';

import isUndefined from 'lodash/isUndefined';

import styles from './ReporterListItem.module.scss';

const ReporterListItem = ({ datasetSchemaId, filterDispatch, reporter, reporterFilters, selectedAllFilterState }) => {
  // const [selectedAll, setSelectedAll] = useState(true);
  const [isChecked, setIsChecked] = useState(true);

  useEffect(() => {
    setIsChecked(getStateBySelectionAndByReporter());
    // setSelectedAll(selectedAllFilterState);
  }, [selectedAllFilterState /* , selectedAll */]);

  const getStateBySelectionAndByReporter = () => {
    if (selectedAllFilterState === 'indeterminate') {
      console.log('inside');

      return !reporterFilters.includes(reporter);
    } else {
      console.log('not in');

      return selectedAllFilterState;
    }
  };

  console.log('selectedAllFilterState', selectedAllFilterState);
  return (
    <>
      <input
        id={`${reporter}_${datasetSchemaId}`}
        className={styles.checkbox}
        type="checkbox"
        defaultChecked={isChecked}
        onChange={e => {
          setIsChecked(e.target.checked);
          filterDispatch({
            type: e.target.checked ? 'REPORTER_CHECKBOX_ON' : 'REPORTER_CHECKBOX_OFF',
            payload: { label: reporter }
          });
        }}
      />
      <label htmlFor={`${reporter}_${datasetSchemaId}`} className={styles.labelItem}>
        {reporter}
      </label>
    </>
  );
};

export { ReporterListItem };
