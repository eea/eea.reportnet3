import React, { useEffect, useState } from 'react';

import { isUndefined } from 'lodash';

import styles from './ReportersListItem.module.scss';

const ReportersListItem = ({ datasetSchemaId, filterDispatch, reporter, reporterFilters, selectedAllFilterState }) => {
  const [selectedAll, setSelectedAll] = useState(true);
  const [isChecked, setIsChecked] = useState(true);

  useEffect(() => {
    setIsChecked(getStateBySelectionAndByReporter(areAllSelectedOrDeselected()));
    setSelectedAll(areAllSelectedOrDeselected);
  }, [selectedAllFilterState, selectedAll]);

  const getStateBySelectionAndByReporter = () => {
    let state = areAllSelectedOrDeselected();
    if (state === '') {
      return reporterFilters.includes(reporter) ? false : true;
    } else {
      return state;
    }
  };

  const areAllSelectedOrDeselected = () => {
    let checked;
    if (!isUndefined(selectedAllFilterState)) {
      if (selectedAllFilterState === 'checked') {
        checked = true;
      } else if (selectedAllFilterState === 'unchecked') {
        checked = false;
      } else if (selectedAllFilterState === 'indeterminate') {
        checked = '';
      }
    }
    return checked;
  };

  const ReporterItem = () => {
    return (
      <>
        <input
          id={`${reporter}_${datasetSchemaId}`}
          className={styles.checkbox}
          type="checkbox"
          defaultChecked={true}
          checked={isChecked}
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

  return <ReporterItem />;
};

export { ReportersListItem };
