import React, { useState } from 'react';

import styles from '../../FilterList.module.scss';

import { ReporterListItem } from './ReporterListItem';
import { SelectAllFilters } from 'ui/views/DataflowDashboards/_components/DatasetValidationDashboard/_components/FilterList/_components/SelectAllFilters';

const ReporterList = ({ datasetSchemaId, filterDispatch, reporterFilters, labels }) => {
  const [selectedAllFilterState, setSelectedAllFilterState] = useState('');

  return (
    <ul className={styles.list}>
      {labels.map(label => (
        <li key={label} className={styles.listItem}>
          <ReporterListItem
            key={label}
            datasetSchemaId={datasetSchemaId}
            filterDispatch={filterDispatch}
            reporter={label}
            reporterFilters={reporterFilters}
            selectedAllFilterState={selectedAllFilterState}
          />
        </li>
      ))}
      <SelectAllFilters
        id={'reporter'}
        datasetSchemaId={datasetSchemaId}
        filterDispatch={filterDispatch}
        filters={reporterFilters}
        labels={labels}
        selectedAllFilter={setSelectedAllFilterState}
      />
    </ul>
  );
};

export { ReporterList };
