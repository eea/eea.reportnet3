import { useContext, useEffect, useReducer } from 'react';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';
import uniqBy from 'lodash/uniqBy';

import utc from 'dayjs/plugin/utc';

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

import { getUrl } from 'repositories/_utils/UrlUtils';

import { useFilters } from 'views/_functions/Hooks/useFilters';

import { ColumnTemplateUtils } from 'views/_functions/Utils/ColumnTemplateUtils';
import { PaginatorRecordsCount } from 'views/_components/DataTable/_functions/Utils/PaginatorRecordsCount';
import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';

export const HistoricReleases = ({ dataflowId, dataflowType, dataProviderId, datasetId, historicReleasesView }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [historicReleasesState, historicReleasesDispatch] = useReducer(historicReleasesReducer, {
    data: [],
    dataProviderCodes: [],
    isLoading: true
  });

  const { filteredData, isFiltered } = useFilters('historicReleases');

  useEffect(() => {
    onLoadHistoricReleases();
  }, []);

  const getDataProviderCode = historicReleases => {
    const dataProviderCodes = uniq(historicReleases.map(historicRelease => historicRelease.dataProviderCode));
    historicReleasesDispatch({ type: 'GET_DATA_PROVIDER_CODES', payload: { dataProviderCodes } });
  };

  const getHistoricReleasesColumns = () => {
    const getColumns = () => {
      if (historicReleasesView === 'releaseDate' || historicReleasesView === 'reportingDataset') {
        return [
          {
            key: 'releaseDate',
            header: resourcesContext.messages['releaseDate'],
            template: renderReleaseDateTemplate
          }
        ];
      } else {
        const columns = [
          {
            key: 'dataProviderCode',
            header: TextByDataflowTypeUtils.getLabelByDataflowType(
              resourcesContext.messages,
              dataflowType,
              'historicReleaseDataProviderColumnHeader'
            ),
            template: renderDataProviderLinkBodyColumn
          }
        ];

        if (historicReleasesView === 'dataCollection') {
          columns.push(
            {
              key: 'isDataCollectionReleased',
              header: resourcesContext.messages['isDataCollectionReleased'],
              template: (rowData, column) =>
                ColumnTemplateUtils.getCheckTemplate(rowData, column, styles.checkedValueColumn, styles.icon)
            },
            {
              key: 'isEUReleased',
              header: resourcesContext.messages['isEUReleased'],
              template: (rowData, column) =>
                ColumnTemplateUtils.getCheckTemplate(rowData, column, styles.checkedValueColumn, styles.icon)
            }
          );
        }

        columns.push(
          {
            key: 'releaseDate',
            header: resourcesContext.messages['releaseDate'],
            template: renderReleaseDateTemplate
          },
          {
            key: 'isPublic',
            header: resourcesContext.messages['isPublic'],
            template: (rowData, column) =>
              ColumnTemplateUtils.getCheckTemplate(rowData, column, styles.checkedValueColumn, styles.icon)
          }
        );

        return columns;
      }
    };

    return getColumns().map(column => (
      <Column
        body={column.template}
        columnResizeMode="expand"
        field={column.key}
        header={column.header}
        key={column.key}
        sortable={true}
      />
    ));
  };

  const isLoading = value => historicReleasesDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadHistoricReleases = async () => {
    try {
      isLoading(true);
      let historicReleases = null;
      if (isNil(datasetId)) {
        const data = await HistoricReleaseService.getAllRepresentative(dataflowId, dataProviderId);
        historicReleases = uniqBy(
          data.map(historic => ({
            releaseDate: historic.releaseDate,
            dataProviderCode: historic.dataProviderCode
          })),
          'releaseDate'
        );
      } else {
        const data = await HistoricReleaseService.getAll(datasetId);
        historicReleases = data;
      }

      historicReleases = historicReleases.map(historic => ({ ...historic, isPublic: !historic.restrictFromPublic }));
      historicReleases.sort((a, b) => b.releaseDate - a.releaseDate);

      historicReleasesDispatch({
        type: 'INITIAL_LOAD',
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
    dayjs.extend(utc);
    return (
      <div className={styles.checkedValueColumn}>{dayjs(rowData.releaseDate).utc().format('YYYY-MM-DD HH:mm:ss')}</div>
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

  const filterOptionsDataCollection = [
    {
      type: 'MULTI_SELECT',
      key: 'dataProviderCode',
      label: TextByDataflowTypeUtils.getLabelByDataflowType(
        resourcesContext.messages,
        dataflowType,
        'historicReleaseDataProviderFilterLabel'
      ),
      multiSelectOptions: uniqBy(
        historicReleasesState.data
          .map(dataProvider => ({ type: dataProvider.dataProviderCode, value: dataProvider.dataProviderCode }))
          .sort((a, b) => a.value.localeCompare(b.value)),
        'type'
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
      key: 'isPublic',
      label: resourcesContext.messages['public'],
      multiSelectOptions: [
        { type: resourcesContext.messages['true'].toUpperCase(), value: true },
        { type: resourcesContext.messages['false'].toUpperCase(), value: false }
      ]
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
          ),
          multiSelectOptions: uniqBy(
            historicReleasesState.data
              .map(dataProvider => ({ type: dataProvider.dataProviderCode, value: dataProvider.dataProviderCode }))
              .sort((a, b) => a.value.localeCompare(b.value)),
            'type'
          )
        },
        {
          key: 'isPublic',
          label: resourcesContext.messages['public'],
          multiSelectOptions: [
            { type: resourcesContext.messages['true'].toUpperCase(), value: true },
            { type: resourcesContext.messages['false'].toUpperCase(), value: false }
          ]
        }
      ]
    }
  ];

  const getFilters = filterOptions => (
    <MyFilters
      className="historicReleases"
      data={historicReleasesState.data}
      options={filterOptions}
      viewType="historicReleases"
    />
  );
  const renderFilters = () =>
    historicReleasesView === 'dataCollection'
      ? getFilters(filterOptionsDataCollection)
      : getFilters(filterOptionsEUDataset);

  const renderHistoricReleasesTable = () => {
    const getValueTable = () =>
      historicReleasesView === 'dataCollection' || historicReleasesView === 'EUDataset'
        ? filteredData
        : historicReleasesState.data;

    if (isEmpty(filteredData) && (historicReleasesView === 'dataCollection' || historicReleasesView === 'EUDataset')) {
      return (
        <div className={styles.emptyFilteredData}>
          {resourcesContext.messages['noHistoricReleasesWithSelectedParameters']}
        </div>
      );
    }

    return (
      <DataTable
        autoLayout={true}
        className={
          historicReleasesView === 'dataCollection' || historicReleasesView === 'EUDataset' ? '' : styles.noFilters
        }
        paginator={true}
        paginatorRight={
          <PaginatorRecordsCount
            dataLength={historicReleasesState.data.length}
            filteredDataLength={filteredData.length}
            isFiltered={isFiltered}
          />
        }
        rows={10}
        rowsPerPageOptions={[5, 10, 15]}
        summary={resourcesContext.messages['historicReleases']}
        totalRecords={filteredData.length}
        value={getValueTable()}>
        {getHistoricReleasesColumns()}
      </DataTable>
    );
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
