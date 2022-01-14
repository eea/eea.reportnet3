import { Fragment, useContext, useEffect, useReducer } from 'react';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';
import uniqBy from 'lodash/uniqBy';

import styles from './HistoricReleases.module.scss';

import { routes } from 'conf/routes';

import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { MyFilters } from 'views/_components/MyFilters';
import { Spinner } from 'views/_components/Spinner';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { HistoricReleaseService } from 'services/HistoricReleaseService';

import { historicReleasesReducer } from './_functions/Reducers/historicReleasesReducer';

import { TextUtils } from 'repositories/_utils/TextUtils';
import { getUrl } from 'repositories/_utils/UrlUtils';

import { useDateTimeFormatByUserPreferences } from 'views/_functions/Hooks/useDateTimeFormatByUserPreferences';

import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';
import { useFilters } from 'views/_functions/Hooks/useFilters';

export const HistoricReleases = ({ dataflowId, dataflowType, dataProviderId, datasetId, historicReleasesView }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [historicReleasesState, historicReleasesDispatch] = useReducer(historicReleasesReducer, {
    data: [],
    dataProviderCodes: [],
    isLoading: true
  });

  const { filteredData, isFiltered } = useFilters('historicReleases');

  const { getDateTimeFormatByUserPreferences } = useDateTimeFormatByUserPreferences();

  useEffect(() => {
    onLoadHistoricReleases();
  }, []);

  const getDataProviderCode = historicReleases => {
    const dataProviderCodes = uniq(historicReleases.map(historicRelease => historicRelease.dataProviderCode));
    historicReleasesDispatch({ type: 'GET_DATA_PROVIDER_CODES', payload: { dataProviderCodes } });
  };

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {isFiltered && historicReleasesState.data.length !== filteredData.length
        ? `${resourcesContext.messages['filtered']} : ${filteredData.length} | `
        : ''}
      {resourcesContext.messages['totalRecords']} {historicReleasesState.data.length}{' '}
      {resourcesContext.messages['records'].toLowerCase()}
      {isFiltered && historicReleasesState.data.length === filteredData.length
        ? ` (${resourcesContext.messages['filtered'].toLowerCase()})`
        : ''}
    </Fragment>
  );

  const isLoading = value => historicReleasesDispatch({ type: 'IS_LOADING', payload: { value } });

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
        // payload: { data: historicReleases, filteredData: historicReleases, filtered: false }
        payload: { data: historicReleases }
      });
      getDataProviderCode(historicReleases);
    } catch (error) {
      console.error('HistoricReleases - onLoadHistoricReleases.', error);
      notificationContext.add({ type: 'LOAD_HISTORIC_RELEASES_ERROR' }, true);
    } finally {
      isLoading(false);
    }
  };

  const renderReleaseDateTemplate = rowData => {
    return <div className={styles.checkedValueColumn}>{getDateTimeFormatByUserPreferences(rowData.releaseDate)}</div>;
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
      {rowData.restrictFromPublic ? (
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
      type: 'MULTI_SELECT',
      key: 'dataProviderCode',
      label: TextByDataflowTypeUtils.getLabelByDataflowType(
        resourcesContext.messages,
        dataflowType,
        'historicReleaseDataProviderFilterLabel'
      )
    },
    {
      type: 'CHECKBOX',
      nestedOptions: [
        {
          key: 'isDataCollectionReleased',
          label: resourcesContext.messages['onlyReleasedDataCollectionCheckboxLabel']
        },
        { key: 'isEUReleased', label: resourcesContext.messages['onlyReleasedEUDatasetCheckboxLabel'] }
      ]
    },
    {
      type: 'MULTI_SELECT',
      key: 'restrictFromPublic',
      label: resourcesContext.messages['restrictFromPublic']
    }
  ];

  const filterOptionsEUDataset = [
    {
      type: 'MULTI_SELECT',
      nestedOptions: [
        {
          key: 'dataProviderCode',
          label: TextByDataflowTypeUtils.getLabelByDataflowType(
            resourcesContext.messages,
            dataflowType,
            'historicReleaseDataProviderFilterLabel'
          )
        },
        { key: 'restrictFromPublic', label: resourcesContext.messages['restrictFromPublic'] }
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
      <MyFilters
        className="historicReleases"
        data={historicReleasesState.data}
        options={filterOptions}
        viewType="historicReleases"
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
    if (isEmpty(filteredData)) {
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
          totalRecords={filteredData.length}
          value={filteredData}>
          {historicReleasesView === 'dataCollection' && renderDataCollectionColumns(filteredData)}
          {historicReleasesView === 'EUDataset' && renderEUDatasetColumns(filteredData)}
          {historicReleasesView === 'reportingDataset' && renderReportingDatasetColumns(filteredData)}
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
