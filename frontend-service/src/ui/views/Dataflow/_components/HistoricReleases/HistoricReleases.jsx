import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import dayjs from 'dayjs';

import isEmpty from 'lodash/isEmpty';
import uniq from 'lodash/uniq';

import styles from './HistoricReleases.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { Filters } from 'ui/views/_components/Filters';
import { Spinner } from 'ui/views/_components/Spinner';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { HistoricReleaseService } from 'core/services/HistoricRelease';

import { historicReleasesReducer } from './_functions/Reducers/historicReleasesReducer';

export const HistoricReleases = ({ dataflowId, dataProviderId, datasetId, historicReleasesView }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [historicReleasesState, historicReleasesDispatch] = useReducer(historicReleasesReducer, {
    countryCodes: [],
    data: [],
    filteredData: [],
    filtered: false,
    isLoading: true
  });

  useEffect(() => {
    onLoadHistoricReleases();
  }, []);

  const getCountryCode = historicReleases => {
    const countryCodes = uniq(historicReleases.map(historicRelease => historicRelease.countryCode));
    historicReleasesDispatch({ type: 'GET_COUNTRY_CODES', payload: { countryCodes } });
  };

  const getFiltered = value => historicReleasesDispatch({ type: 'IS_FILTERED', payload: { value } });

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {historicReleasesState.filtered && historicReleasesState.data.length !== historicReleasesState.filteredData.length
        ? `${resources.messages['filtered']} : ${historicReleasesState.filteredData.length} | `
        : ''}
      {resources.messages['totalRecords']} {historicReleasesState.data.length}{' '}
      {resources.messages['records'].toLowerCase()}
      {historicReleasesState.filtered && historicReleasesState.data.length === historicReleasesState.filteredData.length
        ? ` (${resources.messages['filtered'].toLowerCase()})`
        : ''}
    </Fragment>
  );

  const isLoading = value => historicReleasesDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadFilteredData = data => historicReleasesDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const onLoadHistoricReleases = async () => {
    try {
      isLoading(true);
      let response = null;
      Array.isArray(datasetId)
        ? datasetId.length === 1
          ? (response = await HistoricReleaseService.allHistoricReleases(datasetId[0]))
          : (response = await HistoricReleaseService.allRepresentativeHistoricReleases(dataflowId, dataProviderId))
        : (response = await HistoricReleaseService.allHistoricReleases(datasetId));
      response.sort((a, b) => b.releasedDate - a.releasedDate);
      historicReleasesDispatch({
        type: 'INITIAL_LOAD',
        payload: { data: response, filteredData: response, filtered: false }
      });
      getCountryCode(response);
    } catch (error) {
      notificationContext.add({ type: 'LOAD_HISTORIC_RELEASES_ERROR' });
    } finally {
      isLoading(false);
    }
  };

  const releasedDateTemplate = rowData => {
    return (
      <div className={styles.checkedValueColumn}>
        {dayjs(rowData.releasedDate).format(
          `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
            userContext.userProps.amPm24h ? '' : ' A'
          }`
        )}
      </div>
    );
  };

  const isEUReleasedTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.isEUReleased ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const isDataCollectionReleasedTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.isDataCollectionReleased ? (
        <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} />
      ) : null}
    </div>
  );

  const renderDataCollectionColumns = historicReleases => {
    const fieldColumns = Object.keys(historicReleases[0])
      .filter(
        key =>
          key.includes('countryCode') ||
          key.includes('releasedDate') ||
          key.includes('isDataCollectionReleased') ||
          key.includes('isEUReleased')
      )
      .map(field => {
        let template = null;
        if (field === 'releasedDate') template = releasedDateTemplate;
        if (field === 'isEUReleased') template = isEUReleasedTemplate;
        if (field === 'isDataCollectionReleased') template = isDataCollectionReleasedTemplate;

        return (
          <Column
            body={template}
            columnResizeMode="expand"
            field={field}
            header={resources.messages[field]}
            key={field}
            sortable={true}
          />
        );
      });
    return fieldColumns;
  };

  const renderEUDatasetColumns = historicReleases => {
    const fieldColumns = Object.keys(historicReleases[0])
      .filter(key => key.includes('countryCode') || key.includes('releasedDate'))
      .map(field => {
        let template = null;
        if (field === 'releasedDate') template = releasedDateTemplate;
        return (
          <Column
            body={template}
            columnResizeMode="expand"
            field={field}
            header={resources.messages[field]}
            key={field}
            sortable={true}
          />
        );
      });
    return fieldColumns;
  };

  const renderReportingDatasetColumns = historicReleases => {
    const fieldColumns = Object.keys(historicReleases[0])
      .filter(key => key.includes('releasedDate'))
      .map(field => {
        let template = null;
        if (field === 'releasedDate') template = releasedDateTemplate;
        return (
          <Column
            body={template}
            columnResizeMode="expand"
            field={field}
            header={resources.messages[field]}
            key={field}
            sortable={true}
          />
        );
      });
    return fieldColumns;
  };

  const getOrderedValidations = historicReleases => {
    const historicReleasesWithPriority = [
      { id: 'datasetName', index: 0 },
      { id: 'releasedDate', index: 1 }
    ];

    return historicReleases
      .map(error => historicReleasesWithPriority.filter(e => error === e.id))
      .flat()
      .sort((a, b) => a.index - b.index)
      .map(orderedError => orderedError.id);
  };

  const renderReportingDatasetsColumns = historicReleases => {
    const fieldColumns = getOrderedValidations(Object.keys(historicReleases[0]))
      .filter(key => key.includes('datasetName') || key.includes('releasedDate'))
      .map(field => {
        let template = null;
        if (field === 'releasedDate') template = releasedDateTemplate;
        return (
          <Column
            body={template}
            columnResizeMode="expand"
            field={field}
            header={resources.messages[field]}
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
      <div className={styles.noHistoricReleases}>{resources.messages['noHistoricReleases']}</div>
    </div>
  ) : (
    <div className={styles.historicReleases}>
      {historicReleasesView === 'dataCollection' && (
        <Filters
          checkboxOptions={['isDataCollectionReleased', 'isEUReleased']}
          data={historicReleasesState.data}
          getFilteredData={onLoadFilteredData}
          getFilteredSearched={getFiltered}
          selectOptions={['countryCode']}
        />
      )}

      {datasetId.length > 1 && (
        <Filters
          data={historicReleasesState.data}
          getFilteredData={onLoadFilteredData}
          getFilteredSearched={getFiltered}
          selectOptions={['datasetName']}
        />
      )}

      {historicReleasesState.countryCodes.length > 1 && historicReleasesView === 'EUDataset' && (
        <Filters
          data={historicReleasesState.data}
          getFilteredData={onLoadFilteredData}
          getFilteredSearched={getFiltered}
          selectOptions={['countryCode']}
        />
      )}

      {!isEmpty(historicReleasesState.filteredData) ? (
        <DataTable
          className={datasetId.length > 1 || historicReleasesView === 'dataCollection' ? '' : styles.noFilters}
          autoLayout={true}
          paginator={true}
          paginatorRight={getPaginatorRecordsCount()}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={historicReleasesState.filteredData.length}
          value={historicReleasesState.filteredData}>
          {historicReleasesView === 'dataCollection' && renderDataCollectionColumns(historicReleasesState.filteredData)}
          {historicReleasesView === 'EUDataset' && renderEUDatasetColumns(historicReleasesState.filteredData)}
          {datasetId.length > 1
            ? renderReportingDatasetsColumns(historicReleasesState.filteredData)
            : historicReleasesView === 'reportingDataset' &&
              renderReportingDatasetColumns(historicReleasesState.filteredData)}
        </DataTable>
      ) : (
        <div className={styles.emptyFilteredData}>{resources.messages['noHistoricReleasesWithSelectedParameters']}</div>
      )}
    </div>
  );
};
