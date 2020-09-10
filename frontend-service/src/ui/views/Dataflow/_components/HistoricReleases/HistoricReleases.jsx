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

import { historicReleasesReducer } from './_functions/Reducers/historicReleasesReducer';

export const HistoricReleases = ({ datasetId, historicReleasesView }) => {
  console.log('datasetId', datasetId);

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
    onLoadHistoricReleases();
  }, []);

  const isLoading = value => historicReleasesDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadHistoricReleases = async () => {
    try {
      // isLoading(true);
      const response = data;
      historicReleasesDispatch({ type: 'INITIAL_LOAD', payload: { data: response, filteredData: response } });
    } catch (error) {
      // notificationContext.add({ type: 'LOAD_HISTORIC_RELEASES_ERROR' });
      console.log('error', error);
    } finally {
      // isLoading(false);
    }
  };

  const releaseDateTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {moment(rowData.releaseDate).format(
        `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
          userContext.userProps.amPm24h ? '' : ' A'
        }`
      )}
    </div>
  );

  const isEUDatasetCurrentRelease = rowData => (
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

  const renderColumns = historicReleases => {
    if (historicReleasesView === 'dataCollection') {
      const fieldColumns = Object.keys(historicReleases[0]).map(field => {
        let template = null;
        if (field === 'releaseDate') template = releaseDateTemplate;
        if (field === 'isEUDatasetCurrentRelease') template = isEUDatasetCurrentRelease;
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
    } else if (historicReleasesView === 'reportingDataset') {
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
    } else {
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
    }
  };

  return (
    <div>
      {!isEmpty(historicReleasesState.filteredData) ? (
        <DataTable
          autoLayout={true}
          paginator={true}
          paginatorRight={`${resources.messages['totalRecords']} ${historicReleasesState.filteredData.length}`}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={historicReleasesState.filteredData.length}
          value={historicReleasesState.filteredData}>
          {renderColumns(historicReleasesState.filteredData)}
        </DataTable>
      ) : (
        <div className={styles.emptyFilteredData}>{resources.messages['noHistoricReleasesWithSelectedParameters']}</div>
      )}
    </div>
  );
};
