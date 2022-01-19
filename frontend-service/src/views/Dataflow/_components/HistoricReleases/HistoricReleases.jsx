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

import { getUrl } from 'repositories/_utils/UrlUtils';

import { useDateTimeFormatByUserPreferences } from 'views/_functions/Hooks/useDateTimeFormatByUserPreferences';
import { useFilters } from 'views/_functions/Hooks/useFilters';

import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';
import { ColumnTemplateUtils } from 'views/_functions/Utils/ColumnTemplateUtils';

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

  const getHistoricReleasesColumns = () => {
    const getColumns = () => {
      if (historicReleasesView === 'releaseDate') {
        return [
          {
            key: 'releaseDate',
            header: resourcesContext.messages['releaseDate'],
            template: renderReleaseDateTemplate
          }
        ];
      } else {
        //EUDataset
        const columns = [
          {
            key: 'dataProviderCode',
            header: TextByDataflowTypeUtils.getLabelByDataflowType(
              resourcesContext.messages,
              dataflowType,
              'historicReleaseDataProviderColumnHeader'
            ),
            template: renderDataProviderLinkBodyColumn
          },
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
        ];

        if (historicReleasesView === 'dataCollection') {
          columns.splice(
            1,
            0,
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

        return columns;
      }
    };

    const columns = getColumns();

    return columns.map(column => (
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
      key: 'isPublic',
      label: resourcesContext.messages['public']
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
        { key: 'isPublic', label: resourcesContext.messages['public'] }
      ]
    }
  ];

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
    }
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
