import { Fragment, useContext, useEffect, useState } from 'react';
import { useRecoilValue } from 'recoil';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import styles from './DatasetsInfo.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { MyFilters } from 'views/_components/MyFilters';
import { Spinner } from 'views/_components/Spinner';

import { DataflowService } from 'services/DataflowService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { filterByState } from 'views/_components/MyFilters/_functions/Stores/filtersStores';

import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';

export const DatasetsInfo = ({ dataflowId, dataflowType }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const filterBy = useRecoilValue(filterByState('datasetInfo'));
  const isDataFiltered = !isEmpty(filterBy);

  const [datasetsInfo, setDatasetsInfo] = useState([]);
  const [filteredData, setFilteredData] = useState(datasetsInfo);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    onLoadDatasetsSummary();
  }, []);

  const onLoadDatasetsSummary = async () => {
    try {
      setIsLoading(true);
      const datasets = await DataflowService.getDatasetsInfo(dataflowId);

      datasets.forEach(dataset => {
        dataset.providerData = !isNil(dataset.dataProviderName)
          ? `${dataset.dataProviderName} (${dataset.dataProviderCode})`
          : '';
        dataset.type = resourcesContext.messages[dataset.type];
      });

      setDatasetsInfo(datasets);
    } catch (error) {
      console.error('DatasetsInfo - onLoadDatasetsSummary.', error);
      notificationContext.add({ type: 'LOAD_DATASETS_SUMMARY_ERROR' }, true);
    } finally {
      setIsLoading(false);
    }
  };

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {isDataFiltered && datasetsInfo.length !== filteredData.length
        ? `${resourcesContext.messages['filtered']} : ${filteredData.length} | `
        : ''}
      {resourcesContext.messages['totalRecords']} {datasetsInfo.length}{' '}
      {resourcesContext.messages['records'].toLowerCase()}
      {isDataFiltered && datasetsInfo.length === filteredData.length
        ? ` (${resourcesContext.messages['filtered'].toLowerCase()})`
        : ''}
    </Fragment>
  );

  const filterOptions = [
    { key: 'name', label: resourcesContext.messages['name'], type: 'INPUT' },
    {
      nestedOptions: [
        { key: 'type', label: resourcesContext.messages['type'] },
        {
          key: 'providerData',
          label: TextByDataflowTypeUtils.getLabelByDataflowType(
            resourcesContext.messages,
            dataflowType,
            'datasetsInfoDataProviderNameFilterLabel'
          )
        }
      ],
      type: 'MULTI_SELECT'
    }
  ];

  const filterReferenceDataflowOptions = [
    { key: 'name', label: resourcesContext.messages['name'], type: 'INPUT' },
    { key: 'type', label: resourcesContext.messages['type'], type: 'MULTI_SELECT' }
  ];

  const onLoadFilteredData = value => setFilteredData(value);

  const renderDatasetsInfoContent = () => {
    if (isLoading) {
      return <Spinner />;
    }

    if (isEmpty(datasetsInfo)) {
      return <div className={styles.noDatasets}>{resourcesContext.messages['noDatasets']}</div>;
    }

    return (
      <div className={styles.datasets}>
        {renderFilters()}
        {renderDatasetsInfoTable()}
      </div>
    );
  };

  const renderDatasetsInfoTable = () => {
    if (isEmpty(filteredData)) {
      return (
        <div className={styles.emptyFilteredData}>{resourcesContext.messages['noDatasetsWithSelectedParameters']}</div>
      );
    }

    return (
      <DataTable
        paginator={true}
        paginatorRight={!isNil(filteredData) && getPaginatorRecordsCount()}
        rows={10}
        rowsPerPageOptions={[5, 10, 15]}
        summary={resourcesContext.messages['datasetsInfo']}
        totalRecords={datasetsInfo.length}
        value={filteredData}>
        <Column field="name" header={resourcesContext.messages['name']} sortable={true} />
        <Column field="type" header={resourcesContext.messages['type']} sortable={true} />
        {!(dataflowType === config.dataflowType.REFERENCE.value) && (
          <Column
            field="providerData"
            header={TextByDataflowTypeUtils.getLabelByDataflowType(
              resourcesContext.messages,
              dataflowType,
              'datasetsInfoDataProviderColumnHeader'
            )}
            sortable={true}
          />
        )}
        <Column field="id" header={resourcesContext.messages['datasetId']} sortable={true} />
      </DataTable>
    );
  };

  const renderFilters = () => (
    <MyFilters
      className="datasetsInfo"
      data={datasetsInfo}
      getFilteredData={onLoadFilteredData}
      options={dataflowType === config.dataflowType.REFERENCE.value ? filterReferenceDataflowOptions : filterOptions}
      viewType="datasetInfo"
    />
  );

  return <div className={styles.container}>{renderDatasetsInfoContent()}</div>;
};
