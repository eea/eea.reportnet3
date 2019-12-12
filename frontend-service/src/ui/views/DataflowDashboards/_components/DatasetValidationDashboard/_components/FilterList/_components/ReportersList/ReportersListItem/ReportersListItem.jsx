import React, { useEffect, useState } from 'react';

import styles from './ReportersListItem.module.scss';

const ReportersListItem = ({ datasetSchemaId, filterDispatch, filter, reporterFilters, selectedAllFiltersState }) => {
  const [selectedAllFilterState, setSelectedAllFilterState] = useState(selectedAllFiltersState);

  useEffect(() => {
    setSelectedAllFilterState(selectedAllFiltersState);
  }, [selectedAllFiltersState]);

  const areAllSelected = () => {
    console.log('1', selectedAllFiltersState);
    console.log('2', selectedAllFilterState);
    if (selectedAllFilterState === 'checked') {
      return true;
    } else if (selectedAllFilterState === 'unchecked') {
      return false;
    } else {
      return reporterFilters.includes(filter) ? false : true;
    }
  };

  console.log({ reporterFilters });
  console.log({ filter });
  console.log({ selectedAllFiltersState });

  const ReporterItem = () => {
    return (
      <>
        <input
          id={`${filter}_${datasetSchemaId}`}
          className={styles.checkbox}
          type="checkbox"
          defaultChecked={areAllSelected()}
          onChange={e => {
            filterDispatch({
              type: e.target.checked ? 'REPORTER_CHECKBOX_ON' : 'REPORTER_CHECKBOX_OFF',
              payload: { label: filter }
            });
          }}
        />
        <label htmlFor={`${filter}_${datasetSchemaId}`} className={styles.labelItem}>
          {filter}
        </label>
      </>
    );
  };

  return <ReporterItem />;
};

export { ReportersListItem };
