import { Fragment, useContext, useEffect, useReducer } from 'react';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import dayjs from 'dayjs';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';
import uniqBy from 'lodash/uniqBy';

import styles from './HistoricReleases.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Filters } from 'views/_components/Filters';
import { Spinner } from 'views/_components/Spinner';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { HistoricReleaseService } from 'services/HistoricReleaseService';

import { historicReleasesReducer } from './_functions/Reducers/historicReleasesReducer';

import { TextUtils } from 'repositories/_utils/TextUtils';

import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';

export const HistoricReleases = ({ dataflowId, dataflowType, dataProviderId, datasetId, historicReleasesView }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [historicReleasesState, historicReleasesDispatch] = useReducer(historicReleasesReducer, {
    data: [],
    dataProviderCodes: [],
    filtered: false,
    filteredData: [],
    isLoading: true
  });

  useEffect(() => {
    onLoadHistoricReleases();
  }, []);

  const getDataProviderCode = historicReleases => {
    const dataProviderCodes = uniq(historicReleases.map(historicRelease => historicRelease.dataProviderCode));
    historicReleasesDispatch({ type: 'GET_DATA_PROVIDER_CODES', payload: { dataProviderCodes } });
  };

  const getFiltered = value => historicReleasesDispatch({ type: 'IS_FILTERED', payload: { value } });

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {historicReleasesState.filtered && historicReleasesState.data.length !== historicReleasesState.filteredData.length
        ? `${resourcesContext.messages['filtered']} : ${historicReleasesState.filteredData.length} | `
        : ''}
      {resourcesContext.messages['totalRecords']} {historicReleasesState.data.length}{' '}
      {resourcesContext.messages['records'].toLowerCase()}
      {historicReleasesState.filtered && historicReleasesState.data.length === historicReleasesState.filteredData.length
        ? ` (${resourcesContext.messages['filtered'].toLowerCase()})`
        : ''}
    </Fragment>
  );

  const isLoading = value => historicReleasesDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadFilteredData = data => historicReleasesDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const onLoadHistoricReleases = async () => {
    try {
      isLoading(true);
      let historicReleases = null;
      if (isNil(datasetId)) {
        const data = await HistoricReleaseService.getAllRepresentative(dataflowId, dataProviderId);
        historicReleases = uniqBy(
          data.map(historic => {
            return {
              releaseDate: historic.releaseDate,
              dataProviderCode: historic.dataProviderCode
            };
          }),
          'releaseDate'
        );
      } else {
        const data = await HistoricReleaseService.getAll(datasetId);
        historicReleases = data;
      }

      historicReleases.sort((a, b) => b.releaseDate - a.releaseDate);
      historicReleasesDispatch({
        type: 'INITIAL_LOAD',
        payload: { data: historicReleases, filteredData: historicReleases, filtered: false }
      });
      getDataProviderCode(historicReleases);
    } catch (error) {
      console.error('HistoricReleases - onLoadHistoricReleases.', error);
      notificationContext.add({ type: 'LOAD_HISTORIC_RELEASES_ERROR' });
    } finally {
      isLoading(false);
    }
  };

  const releaseDateTemplate = rowData => {
    return (
      <div className={styles.checkedValueColumn}>
        {dayjs(rowData.releaseDate).format(
          `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
            userContext.userProps.amPm24h ? '' : ' A'
          }`
        )}
      </div>
    );
  };

  const isEUReleasedTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.isEUReleased ? (
        <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} role="presentation" />
      ) : null}
    </div>
  );

  const isDataCollectionReleasedTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.isDataCollectionReleased ? (
        <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} role="presentation" />
      ) : null}
    </div>
  );

  const renderDataCollectionColumns = historicReleases => {
    const fieldColumns = Object.keys(historicReleases[0])
      .filter(
        key =>
          key.includes('dataProviderCode') ||
          key.includes('releaseDate') ||
          key.includes('isDataCollectionReleased') ||
          key.includes('isEUReleased')
      )
      .map(field => {
        let template = null;
        if (field === 'releaseDate') template = releaseDateTemplate;
        if (field === 'isEUReleased') template = isEUReleasedTemplate;
        if (field === 'isDataCollectionReleased') template = isDataCollectionReleasedTemplate;

        return (
          <Column
            body={template}
            columnResizeMode="expand"
            field={field}
            header={
              TextUtils.areEquals(field, 'dataProviderCode')
                ? resourcesContext.messages[TextByDataflowTypeUtils.getFieldLabel(dataflowType)]
                : resourcesContext.messages[field]
            }
            key={field}
            sortable={true}
          />
        );
      });
    return fieldColumns;
  };

  const renderEUDatasetColumns = historicReleases => {
    const fieldColumns = Object.keys(historicReleases[0])
      .filter(key => key.includes('dataProviderCode') || key.includes('releaseDate'))
      .map(field => {
        let template = null;
        if (field === 'releaseDate') template = releaseDateTemplate;
        return (
          <Column
            body={template}
            columnResizeMode="expand"
            field={field}
            header={
              TextUtils.areEquals(field, 'dataProviderCode')
                ? resourcesContext.messages[TextByDataflowTypeUtils.getFieldLabel(dataflowType)]
                : resourcesContext.messages[field]
            }
            key={field}
            sortable={true}
          />
        );
      });
    return fieldColumns;
  };

  const filterOptionsDataCollection = [
    {
      type: 'multiselect',
      properties: [
        {
          name: 'dataProviderCode',
          label: resourcesContext.messages[TextByDataflowTypeUtils.getFieldLabel(dataflowType)]
        }
      ]
    },
    {
      type: 'checkbox',
      properties: [
        {
          name: 'isDataCollectionReleased',
          label: resourcesContext.messages['onlyReleasedDataCollectionCheckboxLabel']
        },
        { name: 'isEUReleased', label: resourcesContext.messages['onlyReleasedEUDatasetCheckboxLabel'] }
      ]
    }
  ];

  const filterOptionsEUDataset = [
    {
      type: 'multiselect',
      properties: [
        {
          name: 'dataProviderCode',
          label: resourcesContext.messages[TextByDataflowTypeUtils.getFieldLabel(dataflowType)]
        }
      ]
    }
  ];

  const renderReportingDatasetColumns = historicReleases => {
    const fieldColumns = Object.keys(historicReleases[0])
      .filter(key => key.includes('releaseDate'))
      .map(field => {
        let template = null;
        if (field === 'releaseDate') template = releaseDateTemplate;
        return (
          <Column
            body={template}
            columnResizeMode="expand"
            field={field}
            header={resourcesContext.messages[field]}
            key={field}
            sortable={true}
          />
        );
      });
    return fieldColumns;
  };

  if (historicReleasesState.isLoading) {
    return (
      <div className={styles.historicReleasesWithoutTable}>
        <div className={styles.spinner}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      </div>
    );
  }

  return isEmpty(historicReleasesState.data) ? (
    <div className={styles.historicReleasesWithoutTable}>
      <div className={styles.noHistoricReleases}>{resourcesContext.messages['noHistoricReleases']}</div>
    </div>
  ) : (
    <div className={styles.historicReleases}>
      {historicReleasesView === 'dataCollection' && (
        <Filters
          data={historicReleasesState.data}
          getFilteredData={onLoadFilteredData}
          getFilteredSearched={getFiltered}
          options={filterOptionsDataCollection}
        />
      )}

      {historicReleasesView === 'EUDataset' && (
        <Filters
          data={historicReleasesState.data}
          getFilteredData={onLoadFilteredData}
          getFilteredSearched={getFiltered}
          options={filterOptionsEUDataset}
        />
      )}

      {!isEmpty(historicReleasesState.filteredData) ? (
        <DataTable
          autoLayout={true}
          className={
            historicReleasesView === 'dataCollection' || historicReleasesView === 'EUDataset' ? '' : styles.noFilters
          }
          paginator={true}
          paginatorRight={getPaginatorRecordsCount()}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          summary={resourcesContext.messages['historicReleases']}
          totalRecords={historicReleasesState.filteredData.length}
          value={historicReleasesState.filteredData}>
          {historicReleasesView === 'dataCollection' && renderDataCollectionColumns(historicReleasesState.filteredData)}
          {historicReleasesView === 'EUDataset' && renderEUDatasetColumns(historicReleasesState.filteredData)}
          {historicReleasesView === 'reportingDataset' &&
            renderReportingDatasetColumns(historicReleasesState.filteredData)}
        </DataTable>
      ) : (
        <div className={styles.emptyFilteredData}>
          {resourcesContext.messages['noHistoricReleasesWithSelectedParameters']}
        </div>
      )}
    </div>
  );
};
