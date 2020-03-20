import React, { useEffect, useState } from 'react';

import isUndefined from 'lodash/isUndefined';

import styles from './ReporterListItem.module.scss';

const ReporterListItem = ({ datasetSchemaId, filterDispatch, reporter, reporterFilters, selectedAllFilterState }) => {
  const [selectedAll, setSelectedAll] = useState(true);
  const [isChecked, setIsChecked] = useState(true);

  useEffect(() => {
    setIsChecked(getStateBySelectionAndByReporter(areAllSelectedOrDeselected()));
    setSelectedAll(areAllSelectedOrDeselected);
  }, [selectedAllFilterState, selectedAll]);

  const getStateBySelectionAndByReporter = () => {
    let state = areAllSelectedOrDeselected();
    if (state === 'indeterminate') {
      return !reporterFilters.includes(reporter);
    } else {
      return state;
    }
  };

  const areAllSelectedOrDeselected = () => {
    if (!isUndefined(selectedAllFilterState)) {
      if (selectedAllFilterState === 'checked') {
        setIsChecked(true);
      } else if (selectedAllFilterState === 'unchecked') {
        setIsChecked(false);
      } else if (selectedAllFilterState === 'indeterminate') {
        setIsChecked('indeterminate');
      }
    }
    return isChecked;
  };

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
