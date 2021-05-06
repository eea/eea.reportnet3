import styles from '../../FilterList.module.scss';

import { ReporterListItem } from './ReporterListItem';
import { SelectAllFilters } from 'ui/views/DataflowDashboards/_components/DatasetValidationDashboard/_components/FilterList/_components/SelectAllFilters';

const ReporterList = ({ datasetSchemaId, filterDispatch, labels, reporterFilters }) => {
  return (
    <ul className={styles.list}>
      {labels.map(label => (
        <li className={styles.listItem} key={label}>
          <ReporterListItem
            datasetSchemaId={datasetSchemaId}
            filterDispatch={filterDispatch}
            reporter={label}
            reporterFilters={reporterFilters}
          />
        </li>
      ))}
      <SelectAllFilters
        datasetSchemaId={datasetSchemaId}
        filterDispatch={filterDispatch}
        filters={reporterFilters}
        id={'reporter'}
        labels={labels}
      />
    </ul>
  );
};

export { ReporterList };
