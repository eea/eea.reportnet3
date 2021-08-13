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

export const HistoricReleases = ({
  dataflowId,
  dataProviderId,
  datasetId,
  historicReleasesView,
  isBusinessDataflow
}) => {
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
      let historicReleases = null;
      if (isNil(datasetId)) {
        const data = await HistoricReleaseService.getAllRepresentative(dataflowId, dataProviderId);
        historicReleases = uniqBy(
          data.map(historic => {
            return {
              releaseDate: historic.releaseDate,
              countryCode: historic.countryCode
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
      getCountryCode(historicReleases);
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
          key.includes('countryCode') ||
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
              isBusinessDataflow && TextUtils.areEquals(field, 'countryCode')
                ? resources.messages['companyCode']
                : resources.messages[field]
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
      .filter(key => key.includes('countryCode') || key.includes('releaseDate'))
      .map(field => {
        let template = null;
        if (field === 'releaseDate') template = releaseDateTemplate;
        return (
          <Column
            body={template}
            columnResizeMode="expand"
            field={field}
            header={
              isBusinessDataflow && TextUtils.areEquals(field, 'countryCode')
                ? resources.messages['companyCode']
                : resources.messages[field]
            }
            key={field}
            sortable={true}
          />
        );
      });
    return fieldColumns;
  };

  const filterOptionsDataCollection = [
    { type: 'multiselect', properties: [{ name: isBusinessDataflow ? 'company' : 'countryCode' }] },
    {
      type: 'checkbox',
      properties: [
        { name: 'isDataCollectionReleased', label: resources.messages['onlyReleasedDataCollectionCheckboxLabel'] },
        { name: 'isEUReleased', label: resources.messages['onlyReleasedEUDatasetCheckboxLabel'] }
      ]
    }
  ];

  const filterOptionsEUDataset = [
    { type: 'multiselect', properties: [{ name: isBusinessDataflow ? 'company' : 'countryCode' }] }
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
          summary={resources.messages['historicReleases']}
          totalRecords={historicReleasesState.filteredData.length}
          value={historicReleasesState.filteredData}>
          {historicReleasesView === 'dataCollection' && renderDataCollectionColumns(historicReleasesState.filteredData)}
          {historicReleasesView === 'EUDataset' && renderEUDatasetColumns(historicReleasesState.filteredData)}
          {historicReleasesView === 'reportingDataset' &&
            renderReportingDatasetColumns(historicReleasesState.filteredData)}
        </DataTable>
      ) : (
        <div className={styles.emptyFilteredData}>{resources.messages['noHistoricReleasesWithSelectedParameters']}</div>
      )}
    </div>
  );
};
