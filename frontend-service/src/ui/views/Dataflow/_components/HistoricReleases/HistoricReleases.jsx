import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import moment from 'moment';

import isEmpty from 'lodash/isEmpty';

import styles from './HistoricReleases.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { Filters } from 'ui/views/_components/Filters';
import { Spinner } from 'ui/views/_components/Spinner';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { ReleaseService } from 'core/services/Release';

import { historicReleasesReducer } from './_functions/Reducers/historicReleasesReducer';

export const HistoricReleases = ({ dataflowId, dataProviderId, datasetId, historicReleasesView }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [historicReleasesState, historicReleasesDispatch] = useReducer(historicReleasesReducer, {
    data: [],
    filteredData: [],
    filtered: false,
    isLoading: true
  });

  useEffect(() => {
    onLoadHistoricReleases();
  }, []);

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
          ? (response = await ReleaseService.allHistoricReleases(datasetId[0]))
          : (response = await ReleaseService.allRepresentativeHistoricReleases(dataflowId, dataProviderId))
        : (response = await ReleaseService.allHistoricReleases(datasetId));
      historicReleasesDispatch({
        type: 'INITIAL_LOAD',
        payload: { data: response, filteredData: response, filtered: false }
      });
    } catch (error) {
      console.error('error', error);
      notificationContext.add({ type: 'LOAD_HISTORIC_RELEASES_ERROR' });
    } finally {
      isLoading(false);
    }
  };

  const releasedDateTemplate = rowData => {
    return (
      <div className={styles.checkedValueColumn}>
        {moment(rowData.releasedDate).format(
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
          <Spinner style={{ top: 0, left: 0 }} />
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
          // selectOptions={['countryCode', 'isDataCollectionReleased', 'isEUReleased']}
          selectOptions={['countryCode']}
        />
      )}

      {Array.isArray(datasetId) && (
        <Filters
          data={historicReleasesState.data}
          getFilteredData={onLoadFilteredData}
          getFilteredSearched={getFiltered}
          selectOptions={['datasetName']}
        />
      )}

      {!isEmpty(historicReleasesState.filteredData) ? (
        <DataTable
          className={Array.isArray(datasetId) || historicReleasesView === 'dataCollection' ? '' : styles.noFilters}
          autoLayout={true}
          paginator={true}
          paginatorRight={getPaginatorRecordsCount()}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={historicReleasesState.filteredData.length}
          value={historicReleasesState.filteredData}>
          {historicReleasesView === 'dataCollection' && renderDataCollectionColumns(historicReleasesState.filteredData)}
          {historicReleasesView === 'EUDataset' && renderEUDatasetColumns(historicReleasesState.filteredData)}
          {Array.isArray(datasetId)
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
