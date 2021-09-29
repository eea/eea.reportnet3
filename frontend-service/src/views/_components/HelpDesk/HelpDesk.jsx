import { Fragment, useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './HelpDesk.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Filters } from 'views/_components/Filters';
import { Spinner } from 'views/_components/Spinner';

import { DataflowService } from 'services/DataflowService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';

export const HelpDesk = ({ dataflowId, dataflowType }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [datasetsSummary, setDatasetsSummary] = useState([]);
  const [filteredData, setFilteredData] = useState(datasetsSummary);
  const [isDataFiltered, setIsDataFiltered] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    onLoadDatasetsSummary();
  }, []);

  useEffect(() => {
    if (!isDataFiltered) {
      setFilteredData(datasetsSummary);
    }
  }, [isDataFiltered]);

  const onLoadDatasetsSummary = async () => {
    try {
      setIsLoading(true);
      const datasets = await DataflowService.getDatasetsSummary(dataflowId);

      setDatasetsSummary(datasets);
      setFilteredData(datasets);
    } catch (error) {
      console.error('HelpDesk - onLoadDatasetsSummary.', error);
      notificationContext.add({ type: 'LOAD_DATASETS_SUMMARY_ERROR' });
    } finally {
      setIsLoading(false);
    }
  };

  const getFilteredState = value => setIsDataFiltered(value);

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {isDataFiltered && datasetsSummary.length !== filteredData.length
        ? `${resourcesContext.messages['filtered']} : ${filteredData.length} | `
        : ''}
      {resourcesContext.messages['totalRecords']} {datasetsSummary.length}{' '}
      {resourcesContext.messages['records'].toLowerCase()}
      {isDataFiltered && datasetsSummary.length === filteredData.length
        ? ` (${resourcesContext.messages['filtered'].toLowerCase()})`
        : ''}
    </Fragment>
  );

  const onLoadFilteredData = value => setFilteredData(value);

  const filterOptions = [
    { type: 'input', properties: [{ name: 'name' }] },
    {
      type: 'multiselect',
      properties: [
        { name: 'type' },
        {
          name: 'providerName',
          label: TextByDataflowTypeUtils.getLabelByDataflowType(
            resourcesContext.messages,
            dataflowType,
            'manualAcceptanceDataProviderNameFilterLabel'
          )
        }
      ]
    }
  ];

  const renderFilters = () => (
    <Filters
      data={datasetsSummary}
      getFilteredData={onLoadFilteredData}
      getFilteredSearched={getFilteredState}
      options={filterOptions}
    />
  );

  return (
    <div className={styles.container}>
      {isLoading ? (
        <Spinner />
      ) : isEmpty(datasetsSummary) ? (
        <div className={styles.noDatasets}>{resourcesContext.messages['noDatasets']}</div>
      ) : (
        <div className={styles.datasets}>
          {renderFilters()}
          {!isEmpty(filteredData) ? (
            <DataTable
              paginator={true}
              paginatorRight={!isNil(filteredData) && getPaginatorRecordsCount()}
              rows={10}
              rowsPerPageOptions={[5, 10, 15]}
              summary="datasetsSummary"
              totalRecords={datasetsSummary.length}
              value={filteredData}>
              <Column field="name" header={resourcesContext.messages['name']} sortable={true} />
              <Column field="type" header={resourcesContext.messages['type']} sortable={true} />
              <Column
                field="providerName"
                header={TextByDataflowTypeUtils.getLabelByDataflowType(
                  resourcesContext.messages,
                  dataflowType,
                  'helpDeskDataProviderColumnHeader'
                )}
                sortable={true}
              />
              <Column field="datasetId" header={resourcesContext.messages['datasetId']} sortable={true} />
            </DataTable>
          ) : (
            <div className={styles.emptyFilteredData}>
              {resourcesContext.messages['noDatasetsWithSelectedParameters']}
            </div>
          )}
        </div>
      )}
    </div>
  );
};
