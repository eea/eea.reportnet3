import React, { useState } from 'react';

import styles from '../../FilterList.module.scss';

import { ReportersListItem } from './ReportersListItem';
import { SelectAllFilters } from 'ui/views/DataflowDashboards/_components/DatasetValidationDashboard/_components/FilterList/_components/SelectAllFilters';

const ReportersList = ({ datasetSchemaId, filterDispatch, reporterFilters, labels }) => {
  const [selectedAllReporters, setSelectAllReporters] = useState('checked');

  const onSelectAllFilter = () => {
    let checkboxListStyles = '';
    if (selectedAllReporters === 'unchecked') {
      checkboxListStyles = styles.unchecked;
    } else {
      checkboxListStyles = styles.neutral;
    }
    return checkboxListStyles;
  };

  return (
    <ul className={styles.list}>
      {labels.map(label => (
        <li className={styles.listItem}>
          <ReportersListItem
            key={label}
            datasetSchemaId={datasetSchemaId}
            filterDispatch={filterDispatch}
            filter={label}
            reporterFilters={reporterFilters}
            selectedAllFiltersState={selectedAllReporters}
          />
        </li>
      ))}
      <SelectAllFilters
        datasetSchemaId={datasetSchemaId}
        filterDispatch={filterDispatch}
        reporterFilters={reporterFilters}
        labels={labels}
        onSelectAllReporters={setSelectAllReporters}
      />
    </ul>
  );
};

export { ReportersList };
