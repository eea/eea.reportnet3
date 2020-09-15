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

import { IntegrationService } from 'core/services/Integration';

import { historicReleasesReducer } from './_functions/Reducers/historicReleasesReducer';
import { array } from 'prop-types';

export const HistoricReleases = ({ datasetId, historicReleasesView, datasetName }) => {
  const data = [
    {
      countryCode: 'EN',
      releaseDate: 1599565444237,
      isReleased: true,
      isEUDatasetCurrentRelease: true
    },
    {
      countryCode: 'ES',
      releaseDate: 1599565434866,
      isReleased: false,
      isEUDatasetCurrentRelease: true
    },
    {
      countryCode: 'FR',
      releaseDate: 1599565484237,
      isReleased: false,
      isEUDatasetCurrentRelease: false
    },
    {
      countryCode: 'GR',
      releaseDate: 1599563434866,
      isReleased: true,
      isEUDatasetCurrentRelease: true
    }
  ];

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
      ? datasetId.forEach((datasetId, index) => {
          onLoadHistoricReleasesArray(datasetId, datasetName[index]);
        })
      : onLoadHistoricReleases();
  }, []);

  const isLoading = value => historicReleasesDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadFilteredData = data => historicReleasesDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const response = [];
  const onLoadHistoricReleasesArray = async (datasetId, datasetName) => {
    console.log('response', response);
    console.log('response.length', response.length);
    const historicReleases = [];
    try {
      data.forEach(historicRelease => {
        historicRelease.datasetId = datasetId;
        historicRelease.datasetName = datasetName;
        response.push(historicRelease);
      });
      console.log('datasetId', datasetId);
      console.log('datasetName', datasetName);
      historicReleasesDispatch({
        type: 'INITIAL_LOAD',
        payload: { data: response, filteredData: response }
      });
      // isLoading(true);
      // const response = await IntegrationService.all(datasetId);
      // response.forEach(historicRelease => {
      //   historicRelease.datasetId = datasetId;
      //   historicRelease.datasetName = datasetName;
      //   historicReleases.push(historicRelease);
      // });
      // historicReleasesDispatch({ type: 'INITIAL_LOAD', payload: { data: response, filteredData: response } });
    } catch (error) {
      // notificationContext.add({ type: 'LOAD_HISTORIC_RELEASES_ERROR' });
      console.log('error', error);
    } finally {
      // isLoading(false);
    }
  };

  const onLoadHistoricReleases = async () => {
    try {
      const response = data;
      historicReleasesDispatch({ type: 'INITIAL_LOAD', payload: { data: response, filteredData: response } });
      // isLoading(true);
      // const response = await IntegrationService.all(datasetId);
      // historicReleasesDispatch({ type: 'INITIAL_LOAD', payload: { data: response, filteredData: response } });
    } catch (error) {
      // notificationContext.add({ type: 'LOAD_HISTORIC_RELEASES_ERROR' });
      console.log('error', error);
    } finally {
      // isLoading(false);
    }
  };

  const releaseDateTemplate = rowData => {
    return (
      <div className={styles.checkedValueColumn}>
        {moment(rowData.releaseDate).format(
          `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
            userContext.userProps.amPm24h ? '' : ' A'
          }`
        )}
      </div>
    );
  };

  const isEUDatasetCurrentReleaseTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.isEUDatasetCurrentRelease ? (
        <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} />
      ) : null}
    </div>
  );

  const isReleasedTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.isReleased ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const renderDataCollectionColumns = historicReleases => {
    const fieldColumns = Object.keys(historicReleases[0])
      // .filter(
      //   key =>
      //     key.includes('countryCode') ||
      //     key.includes('releaseDate') ||
      //     key.includes('isReleased') ||
      //     key.includes('isEUDatasetCurrentRelease')
      // )
      .map(field => {
        let template = null;
        if (field === 'releaseDate') template = releaseDateTemplate;
        if (field === 'isEUDatasetCurrentRelease') template = isEUDatasetCurrentReleaseTemplate;
        if (field === 'isReleased') template = isReleasedTemplate;

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
      .filter(key => key.includes('countryCode') || key.includes('releaseDate'))
      .map(field => {
        let template = null;
        if (field === 'releaseDate') template = releaseDateTemplate;
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
      .filter(key => key.includes('releaseDate'))
      .map(field => {
        let template = null;
        if (field === 'releaseDate') template = releaseDateTemplate;
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

  const renderReportingDatasetsColumns = historicReleases => {
    const fieldColumns = Object.keys(historicReleases[0])
      .filter(key => key.includes('datasetName') || key.includes('releaseDate'))
      .map(field => {
        let template = null;
        if (field === 'releaseDate') template = releaseDateTemplate;
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
          checkboxOptions={['isReleased', 'isEUDatasetCurrentRelease']}
        />
      )}

      {Array.isArray(datasetName) && (
        <Filters
          data={historicReleasesState.data}
          getFilteredData={onLoadFilteredData}
          selectOptions={['datasetName']}
        />
      )}

      {console.log('historicReleasesState.filteredData', historicReleasesState.filteredData)}

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
