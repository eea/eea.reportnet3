import React, { useContext, useEffect, useReducer } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import moment from 'moment';

import isEmpty from 'lodash/isEmpty';

import styles from './HistoricReleases.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { Filters } from 'ui/views/_components/Filters';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { ReleaseService } from 'core/services/Release';

import { historicReleasesReducer } from './_functions/Reducers/historicReleasesReducer';

export const HistoricReleases = ({ datasetId, historicReleasesView, datasetName }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [historicReleasesState, historicReleasesDispatch] = useReducer(historicReleasesReducer, {
    data: [],
    filteredData: [],
    isLoading: true
  });

  useEffect(() => {
    Array.isArray(datasetId)
      ? datasetId.forEach(datasetId => onLoadHistoricReleases(datasetId))
      : onLoadHistoricReleases(datasetId);
  }, []);

  const isLoading = value => historicReleasesDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadFilteredData = data => historicReleasesDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const historicReleases = [];
  const onLoadHistoricReleases = async datasetId => {
    try {
      isLoading(true);
      const response = await ReleaseService.allDataCollectionHistoricReleases(141);
      response.forEach(historicRelease => historicReleases.push(historicRelease));
      historicReleasesDispatch({
        type: 'INITIAL_LOAD',
        payload: { data: historicReleases, filteredData: historicReleases }
      });
    } catch (error) {
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

  return (
    <div>
      {historicReleasesView === 'dataCollection' && (
        <Filters
          data={historicReleasesState.data}
          getFilteredData={onLoadFilteredData}
          selectOptions={['countryCode']}
          checkboxOptions={['isDataCollectionReleased', 'isEUReleased']}
        />
      )}

      {Array.isArray(datasetName) && (
        <Filters
          data={historicReleasesState.data}
          getFilteredData={onLoadFilteredData}
          selectOptions={['datasetName']}
        />
      )}

      {!isEmpty(historicReleasesState.filteredData) ? (
        <DataTable
          autoLayout={true}
          paginator={true}
          paginatorRight={`${resources.messages['totalRecords']} ${historicReleasesState.filteredData.length}`}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={historicReleasesState.filteredData.length}
          value={historicReleasesState.filteredData}>
          {historicReleasesView === 'dataCollection' && renderDataCollectionColumns(historicReleasesState.filteredData)}
          {historicReleasesView === 'EUDataset' && renderEUDatasetColumns(historicReleasesState.filteredData)}
          {historicReleasesView === 'reportingDataset' &&
            Array.isArray(datasetId) &&
            renderReportingDatasetsColumns(historicReleasesState.filteredData)}
          {historicReleasesView === 'reportingDataset' &&
            !Array.isArray(datasetId) &&
            renderReportingDatasetColumns(historicReleasesState.filteredData)}
        </DataTable>
      ) : (
        <div className={styles.emptyFilteredData}>{resources.messages['noHistoricReleasesWithSelectedParameters']}</div>
      )}
    </div>
  );
};
