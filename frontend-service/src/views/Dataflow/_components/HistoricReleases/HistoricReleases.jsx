import { useContext, useEffect, useReducer, useState } from 'react';

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

import { ColumnTemplateUtils } from 'views/_functions/Utils/ColumnTemplateUtils';
import { PaginatorRecordsCount } from 'views/_components/DataTable/_functions/Utils/PaginatorRecordsCount';
import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';
import { Button } from '../../../_components/Button';
import { ValidationService } from '../../../../services/ValidationService';
import { useCheckNotifications } from '../../../_functions/Hooks/useCheckNotifications';
import { Calendar } from 'views/_components/Calendar';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { SnapshotService } from 'services/SnapshotService';
import { config } from 'conf';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import dayjs from 'dayjs';
import utc from 'dayjs/plugin/utc';

dayjs.extend(utc);

export const HistoricReleases = ({ dataflowId, dataflowType, dataProviderId, datasetId, historicReleasesView }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const { getDateTimeFormatByUserPreferences } = useDateTimeFormatByUserPreferences();

  const [historicReleasesState, historicReleasesDispatch] = useReducer(historicReleasesReducer, {
    data: [],
    dataProviderCodes: [],
    isLoading: true
  });

  const { filteredData, isFiltered } = useFilters('historicReleases');
  const [isDownloadingHistoricData, setIsDownloadingHistoricData] = useState(false);
  const [isCalendarDialogVisible, setIsCalendarDialogVisible] = useState(false);
  const [selectedReleaseForCalendar, setSelectedReleaseForCalendar] = useState(null);
  const [selectedReleaseDate, setSelectedReleaseDate] = useState(null);
  const [isUpdatingReleaseDate, setIsUpdatingReleaseDate] = useState(false);
  const [hasSelectedDay, setHasSelectedDay] = useState(false);

  const userContext = useContext(UserContext);
  const isAdmin = userContext.hasPermission([config.permissions.roles.ADMIN.key]);
  const isCustodian = userContext.hasPermission([config.permissions.roles.CUSTODIAN.key]);

  useEffect(() => {
    onLoadHistoricReleases();
  }, []);

  useCheckNotifications(
    [
      'AUTOMATICALLY_DOWNLOAD_HISTORIC_RELEASES_FILE',
      'DOWNLOAD_HISTORIC_RELEASES_FILE_ERROR',
      'DOWNLOAD_FILE_BAD_REQUEST_ERROR'
    ],
    setIsDownloadingHistoricData,
    false
  );

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
          },
          ...(isNil(datasetId) && (isAdmin || isCustodian)
            ? [
                {
                  key: 'actions',
                  header: resourcesContext.messages['actions'],
                  template: renderActionsTemplate
                }
              ]
            : [])
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

  const renderActionsTemplate = rowData => {
    return (
      <div className={styles.actionsWrapper}>
        <Button
          className="p-button-rounded p-button-secondary-transparent"
          disabled={!(isAdmin || isCustodian)}
          icon="calendar"
          onClick={() => {
            setSelectedReleaseForCalendar(rowData);
            setSelectedReleaseDate(null);
            setHasSelectedDay(false);
            setIsCalendarDialogVisible(true);
          }}
          tooltip={
            isAdmin || isCustodian
              ? resourcesContext.messages['changeReleaseDate']
              : resourcesContext.messages['changeReleaseDateAdmin']
          }
          tooltipOptions={{ position: 'top' }}
        />
      </div>
    );
  };

  const onConfirmUpdateReleaseDate = async () => {
    setIsUpdatingReleaseDate(true);
    try {
      const formattedDate = dayjs(selectedReleaseDate).utc().format('YYYY-MM-DDTHH:mm:ss[Z]');

      await SnapshotService.updateReleaseDate(selectedReleaseForCalendar.id, dataflowId, dataProviderId, formattedDate);
      notificationContext.add(
        {
          type: 'UPDATE_RELEASE_DATE_SUCCESS'
        },
        true
      );
      onLoadHistoricReleases();
    } catch (error) {
      console.error('HistoricReleases - onConfirmUpdateReleaseDate.', error);
      notificationContext.add({ type: 'UPDATE_RELEASE_DATE_ERROR' }, true);
    } finally {
      setIsUpdatingReleaseDate(false);
      setIsCalendarDialogVisible(false);
      setSelectedReleaseForCalendar(null);
      setSelectedReleaseDate(null);
    }
  };

  const onHideCalendarDialog = () => {
    setIsCalendarDialogVisible(false);
    setSelectedReleaseForCalendar(null);
    setHasSelectedDay(false);
    setSelectedReleaseDate(null);
  };

  const handleCalendarChange = event => {
    const newDate = event.target.value;
    
    if (newDate && dayjs(newDate).isValid()) {
      setSelectedReleaseDate(newDate);
      setHasSelectedDay(true);
    }
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
            id: historic.id,
            datasetId: historic.datasetId,
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

  const onDownloadHistoricData = async () => {
    let firstDatasetId = datasetId;
    if (isNil(datasetId)) {
      const data = await HistoricReleaseService.getAllRepresentative(dataflowId, dataProviderId);
      firstDatasetId = data[0]?.datasetId; // assign here
    }
    setIsDownloadingHistoricData(true);
    try {
      await ValidationService.generateHistoricDataFile(firstDatasetId, dataflowId);
      notificationContext.add({ type: 'DOWNLOAD_HISTORIC_RELEASES_START' });
    } catch (error) {
      if (error.response?.status === 400) {
        notificationContext.add({ type: 'DOWNLOAD_FILE_BAD_REQUEST_ERROR' }, true);
      } else {
        notificationContext.add({ type: 'GENERATE_HISTORIC_RELEASES_FILE_ERROR' }, true);
      }
      setIsDownloadingHistoricData(false);
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
        <Button
          className="p-button-secondary p-button-animated-blink historic-release-button"
          disabled={isDownloadingHistoricData}
          icon={isDownloadingHistoricData ? 'spinnerAnimate' : 'export'}
          label={resourcesContext.messages['downloadHistoricDataButtonLabel']}
          onClick={() => onDownloadHistoricData(datasetId, dataflowId)}
        />
      </div>
    );
  };

  return (
    <>
      {renderHistoricReleasesContent()}

      {isCalendarDialogVisible && (isAdmin || isCustodian) && (
        <ConfirmDialog
          className={styles.calendarConfirm}
          dialogStyle={{ minWidth: 'auto' }}
          disabledConfirm={!hasSelectedDay || isNil(selectedReleaseDate) || isUpdatingReleaseDate}
          header={resourcesContext.messages['changeReleaseDateHeader'] + selectedReleaseForCalendar.dataProviderCode}
          iconConfirm={isUpdatingReleaseDate ? 'spinnerAnimate' : 'check'}
          labelCancel={resourcesContext.messages['cancel']}
          labelConfirm={resourcesContext.messages['save']}
          onConfirm={onConfirmUpdateReleaseDate}
          onHide={onHideCalendarDialog}
          visible={isCalendarDialogVisible}>
          <div className={styles.calendarContent}>
            {selectedReleaseForCalendar?.releaseDate && (
              <div className={styles.currentDateInfo}>
                <strong>{resourcesContext.messages['currentReleaseDate']}</strong>{' '}
                {getDateTimeFormatByUserPreferences(selectedReleaseForCalendar.releaseDate)}
              </div>
            )}
            <Calendar
              className={styles.calendar}
              dateFormat="yy-mm-dd"
              inline={true}
              monthNavigator={true}
              onChange={handleCalendarChange}
              placeholder={resourcesContext.messages['selectNewReleaseDate']}
              showTime={hasSelectedDay}
              value={selectedReleaseDate}
              yearNavigator={true}
              yearRange="2000:2050"
            />
          </div>
        </ConfirmDialog>
      )}
    </>
  );
};
