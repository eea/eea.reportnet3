import React, { useState } from 'react';

import styles from '../../FilterList.module.scss';

import { ReportersListItem } from './ReportersListItem';
import { SelectAllFilters } from 'ui/views/DataflowDashboards/_components/DatasetValidationDashboard/_components/FilterList/_components/SelectAllFilters';

const ReportersList = ({ datasetSchemaId, filterDispatch, reporterFilters, labels }) => {
  const [selectedAllFilterState, setSelectedAllFilterState] = useState('');

  return (
    <ul className={styles.list}>
      {labels.map(label => (
        <li className={styles.listItem}>
          <ReportersListItem
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
        datasetSchemaId={datasetSchemaId}
        filterDispatch={filterDispatch}
        filters={reporterFilters}
        labels={labels}
        selectedAllFilter={setSelectedAllFilterState}
      />
    </ul>
  );
};

export { ReportersList };
