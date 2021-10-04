import { Fragment, useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import styles from './DatasetsInfo.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Filters } from 'views/_components/Filters';
import { Spinner } from 'views/_components/Spinner';

import { DataflowService } from 'services/DataflowService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';

export const DatasetsInfo = ({ dataflowId, dataflowType }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [datasetsInfo, setDatasetsInfo] = useState([]);
  const [filteredData, setFilteredData] = useState(datasetsInfo);
  const [isDataFiltered, setIsDataFiltered] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    onLoadDatasetsSummary();
  }, []);

  useEffect(() => {
    if (!isDataFiltered) {
      setFilteredData(datasetsInfo);
    }
  }, [isDataFiltered]);

  const onLoadDatasetsSummary = async () => {
    try {
      setIsLoading(true);
      const datasets = await DataflowService.getDatasetsInfo(dataflowId);

      setDatasetsInfo(datasets);
      setFilteredData(datasets);
    } catch (error) {
      console.error('DatasetsInfo - onLoadDatasetsSummary.', error);
      notificationContext.add({ type: 'LOAD_DATASETS_SUMMARY_ERROR' });
    } finally {
      setIsLoading(false);
    }
  };

  const getFilteredState = value => setIsDataFiltered(value);

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
    { type: 'input', properties: [{ name: 'name' }] },
    {
      type: 'multiselect',
      properties: [
        { name: 'type' },
        {
          name: 'dataProviderName',
          label: TextByDataflowTypeUtils.getLabelByDataflowType(
            resourcesContext.messages,
            dataflowType,
            'datasetsInfoDataProviderNameFilterLabel'
          )
        }
      ]
    }
  ];

  const onLoadFilteredData = value => setFilteredData(value);

  const renderDataProviderNameTemplate = rowData =>
    !isEmpty(rowData.dataProviderName) && (
      <div className={styles.checkedValueColumn}>{`${rowData.dataProviderName} (${rowData.dataProviderCode})`}</div>
    );

  const renderTypeTemplate = rowData => {
    switch (rowData.type) {
      case 'DESIGN':
        return resourcesContext.messages['design'];

      case 'TEST':
        return resourcesContext.messages['testDataset'];

      case 'REPORTING':
        return resourcesContext.messages['reportingDataset'];

      case 'COLLECTION':
        return resourcesContext.messages['dataCollection'];

      case 'EUDATASET':
        return resourcesContext.messages['euDataset'];

      case 'REFERENCE':
        return resourcesContext.messages['referenceDataset'];

      default:
        break;
    }
  };

  const renderFilters = () => (
    <Filters
      data={datasetsInfo}
      getFilteredData={onLoadFilteredData}
      getFilteredSearched={getFilteredState}
      options={filterOptions}
    />
  );

  return (
    <div className={styles.container}>
      {isLoading ? (
        <Spinner />
      ) : isEmpty(datasetsInfo) ? (
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
              summary="datasetsInfo"
              totalRecords={datasetsInfo.length}
              value={filteredData}>
              <Column field="name" header={resourcesContext.messages['name']} sortable={true} />
              <Column
                body={renderTypeTemplate}
                field="type"
                header={resourcesContext.messages['type']}
                sortable={true}
              />
              <Column
                body={renderDataProviderNameTemplate}
                field="dataProviderName"
                header={TextByDataflowTypeUtils.getLabelByDataflowType(
                  resourcesContext.messages,
                  dataflowType,
                  'datasetsInfoDataProviderColumnHeader'
                )}
                sortable={true}
              />
              <Column field="id" header={resourcesContext.messages['datasetId']} sortable={true} />
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
