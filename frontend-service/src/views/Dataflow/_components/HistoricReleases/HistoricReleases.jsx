import { Fragment, useContext, useEffect, useReducer } from 'react';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import dayjs from 'dayjs';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';
import uniqBy from 'lodash/uniqBy';

import styles from './HistoricReleases.module.scss';

import { routes } from 'conf/routes';

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
import { getUrl } from 'repositories/_utils/UrlUtils';

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

  const renderReleaseDateTemplate = rowData => {
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

  const renderDataProviderLinkBodyColumn = rowData => (
    <div onClick={e => e.stopPropagation()}>
      <span>
        {rowData.dataProviderCode}{' '}
        <a
          href={getUrl(routes.DATASET, { dataflowId, datasetId: rowData.datasetId }, true)}
          title={rowData.dataProviderCode}>
          <FontAwesomeIcon aria-hidden={false} className="p-breadcrumb-home" icon={AwesomeIcons('externalUrl')} />
        </a>
      </span>
    </div>
  );

  const renderIsDataCollectionReleasedTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.isDataCollectionReleased ? (
        <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} role="presentation" />
      ) : null}
    </div>
  );

  const renderIsEUReleasedTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.isEUReleased ? (
        <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} role="presentation" />
      ) : null}
    </div>
  );

  const renderRestrictFromPublicTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {!rowData.restrictFromPublic ? (
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
          key.includes('isEUReleased') ||
          key.includes('restrictFromPublic')
      )
      .map(field => {
        let template = null;
        if (field === 'dataProviderCode') template = renderDataProviderLinkBodyColumn;
        if (field === 'releaseDate') template = renderReleaseDateTemplate;
        if (field === 'isEUReleased') template = renderIsEUReleasedTemplate;
        if (field === 'isDataCollectionReleased') template = renderIsDataCollectionReleasedTemplate;
        if (field === 'restrictFromPublic') template = renderRestrictFromPublicTemplate;

        return (
          <Column
            body={template}
            columnResizeMode="expand"
            field={field}
            header={
              TextUtils.areEquals(field, 'dataProviderCode')
                ? TextByDataflowTypeUtils.getLabelByDataflowType(
                    resourcesContext.messages,
                    dataflowType,
                    'historicReleaseDataProviderColumnHeader'
                  )
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
      .filter(
        key => key.includes('dataProviderCode') || key.includes('releaseDate') || key.includes('restrictFromPublic')
      )
      .map(field => {
        let template = null;
        if (field === 'dataProviderCode') template = renderDataProviderLinkBodyColumn;
        if (field === 'releaseDate') template = renderReleaseDateTemplate;
        if (field === 'restrictFromPublic') template = renderRestrictFromPublicTemplate;
        return (
          <Column
            body={template}
            columnResizeMode="expand"
            field={field}
            header={
              TextUtils.areEquals(field, 'dataProviderCode')
                ? TextByDataflowTypeUtils.getLabelByDataflowType(
                    resourcesContext.messages,
                    dataflowType,
                    'historicReleaseDataProviderColumnHeader'
                  )
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
          label: TextByDataflowTypeUtils.getLabelByDataflowType(
            resourcesContext.messages,
            dataflowType,
            'historicReleaseDataProviderFilterLabel'
          )
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
    },
    {
      type: 'multiselect',
      properties: [{ name: 'restrictFromPublic', label: resourcesContext.messages['restrictFromPublic'] }]
    }
  ];

  const filterOptionsEUDataset = [
    {
      type: 'multiselect',
      properties: [
        {
          name: 'dataProviderCode',
          label: TextByDataflowTypeUtils.getLabelByDataflowType(
            resourcesContext.messages,
            dataflowType,
            'historicReleaseDataProviderFilterLabel'
          )
        },
        { name: 'restrictFromPublic', label: resourcesContext.messages['restrictFromPublic'] }
      ]
    }
  ];

  const renderReportingDatasetColumns = historicReleases => {
    const fieldColumns = Object.keys(historicReleases[0])
      .filter(key => key.includes('releaseDate'))
      .map(field => {
        let template = null;
        if (field === 'releaseDate') template = renderReleaseDateTemplate;
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

  const getFilters = filterOptions => {
    return (
      <Filters
        data={historicReleasesState.data}
        getFilteredData={onLoadFilteredData}
        getFilteredSearched={getFiltered}
        options={filterOptions}
      />
    );
  };

  const renderFilters = () => {
    if (historicReleasesView === 'dataCollection') {
      return getFilters(filterOptionsDataCollection);
    }

    if (historicReleasesView === 'EUDataset') {
      return getFilters(filterOptionsEUDataset);
    }
  };

  const renderHistoricReleasesTable = () => {
    if (isEmpty(historicReleasesState.filteredData)) {
      return (
        <div className={styles.emptyFilteredData}>
          {resourcesContext.messages['noHistoricReleasesWithSelectedParameters']}
        </div>
      );
    } else {
      return (
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
      );
    }
  };

  const renderHistoricReleasesContent = () => {
    if (historicReleasesState.isLoading) {
      return (
        <div className={styles.historicReleasesWithoutTable}>
          <div className={styles.spinner}>
            <Spinner className={styles.spinnerPosition} />
          </div>
        </div>
      );
    }

    if (isEmpty(historicReleasesState.data)) {
      return (
        <div className={styles.historicReleasesWithoutTable}>
          <div className={styles.noHistoricReleases}>{resourcesContext.messages['noHistoricReleases']}</div>
        </div>
      );
    }

    return (
      <div className={styles.historicReleases}>
        {renderFilters()}
        {renderHistoricReleasesTable()}
      </div>
    );
  };

  return renderHistoricReleasesContent();
};
