import { Fragment, useContext, useEffect, useState } from 'react';

import { Column } from 'primereact/column';

import { useRecoilValue } from 'recoil';

import styles from './ControlStatuses.module.scss';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { Filters } from 'views/_components/Filters';
import { LevelError } from 'views/_components/LevelError';
import { Spinner } from 'views/_components/Spinner';

import { ControlStatusesService } from 'services/ControlStatusesService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from '../../../_functions/Contexts/UserContext';

import { filterByCustomFilterStore } from 'views/_components/Filters/_functions/Stores/filterStore';
import { FiltersUtils } from 'views/_components/Filters/_functions/Utils/FiltersUtils';
import { PaginatorRecordsCount } from 'views/_components/DataTable/_functions/Utils/PaginatorRecordsCount';
import { useApplyFilters } from 'views/_functions/Hooks/useApplyFilters';

import { useDateTimeFormatByUserPreferences } from 'views/_functions/Hooks/useDateTimeFormatByUserPreferences';

const { permissions } = config;

export const ControlStatuses = ({ onCloseDialog, isDialogVisible }) => {
  const filterBy = useRecoilValue(filterByCustomFilterStore('controlStatuses'));

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);
  const isAdmin = userContext.hasPermission([permissions.roles.ADMIN.key]);

  const [controlStatus, setControlStatus] = useState(null);
  const [controlStatuses, setControlStatusesList] = useState([]);
  const [filteredRecords, setFilteredRecords] = useState(0);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isFiltered, setIsFiltered] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isDeletingDatasetData, setIsDeletingDatasetData] = useState(false);
  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [pagination, setPagination] = useState({ firstRow: 0, numberRows: 10, pageNum: 0 });
  const [sort, setSort] = useState({ field: 'dateAdded', order: -1 });
  const [totalRecords, setTotalRecords] = useState(0);

  const { getDateTimeFormatByUserPreferences } = useDateTimeFormatByUserPreferences();
  const { setData } = useApplyFilters('controlStatuses');

  const { firstRow, numberRows, pageNum } = pagination;

  useEffect(() => {
    getControlStatuses();
  }, [pagination, sort]);

  const getControlStatuses = async () => {
    setLoadingStatus('pending');

    try {
      const data = await ControlStatusesService.getControlStatuses({
        pageNum,
        numberRows,
        sortOrder: sort.order,
        sortField: sort.field,
        jobId: filterBy.jobId,
        jobType: filterBy.jobType?.join(),
        dataflowId: filterBy.dataflowId,
        providerId: filterBy.providerId,
        datasetId: filterBy.datasetId,
        creatorUsername: filterBy.creatorUsername,
        jobStatus: filterBy.jobStatus?.join()
      });

      setTotalRecords(data.totalRecords);
      setControlStatusesList(data.jobsList);
      setFilteredRecords(data.filteredRecords);
      setIsFiltered(FiltersUtils.getIsFiltered(filterBy));
      setData(data.jobsList);
      setLoadingStatus('success');
    } catch (error) {
      console.error('ControlStatus - getControlStatuses.', error);
      setLoadingStatus('error');
      notificationContext.add({ type: 'GET_CONTROL_STATUSES_ERROR' }, true);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  const onSort = event => setSort({ field: event.sortField, order: event.sortOrder });

  const filterOptions = [
    {
      nestedOptions: [
        // { key: 'jobId', label: resourcesContext.messages['jobId'], keyfilter: 'pint' },
        { key: 'creatorUsername', label: resourcesContext.messages['creatorUsername'] },
        { key: 'providerId', label: resourcesContext.messages['providerId'] },
        { key: 'dataflowId', label: resourcesContext.messages['dataflowId'] },
        { key: 'datasetId', label: resourcesContext.messages['datasetId'] }
      ],
      type: 'INPUT'
    }
    // {
    //   key: 'jobType',
    //   label: resourcesContext.messages['jobType'],
    //   multiSelectOptions: [
    //     {
    //       type: resourcesContext.messages[config.jobType.IMPORT.label].toUpperCase(),
    //       value: config.jobType.IMPORT.key
    //     },
    //     {
    //       type: resourcesContext.messages[config.jobType.VALIDATION.label].toUpperCase(),
    //       value: config.jobType.VALIDATION.key
    //     },
    //     {
    //       type: resourcesContext.messages[config.jobType.RELEASE.label].toUpperCase(),
    //       value: config.jobType.RELEASE.key
    //     },
    //     {
    //       type: resourcesContext.messages[config.jobType.EXPORT.label].toUpperCase(),
    //       value: config.jobType.EXPORT.key
    //     },
    //     {
    //       type: resourcesContext.messages[config.jobType.COPY_TO_EU_DATASET.label].toUpperCase(),
    //       value: config.jobType.COPY_TO_EU_DATASET.key
    //     }
    //   ],
    //   template: 'jobType',
    //   type: 'MULTI_SELECT'
    // },
    // {
    //   key: 'jobStatus',
    //   label: resourcesContext.messages['jobStatus'],
    //   multiSelectOptions: [
    //     {
    //       type: resourcesContext.messages[config.jobRunningStatus.FAILED.label].toUpperCase(),
    //       value: config.jobRunningStatus.FAILED.key
    //     },
    //     {
    //       type: resourcesContext.messages[config.jobRunningStatus.QUEUED.label].toUpperCase(),
    //       value: config.jobRunningStatus.QUEUED.key
    //     },
    //     {
    //       type: resourcesContext.messages[config.jobRunningStatus.REFUSED.label].toUpperCase(),
    //       value: config.jobRunningStatus.REFUSED.key
    //     },
    //     {
    //       type: resourcesContext.messages[config.jobRunningStatus.FINISHED.label].toUpperCase(),
    //       value: config.jobRunningStatus.FINISHED.key
    //     },
    //     {
    //       type: resourcesContext.messages[config.jobRunningStatus.CANCELLED.label].toUpperCase(),
    //       value: config.jobRunningStatus.CANCELLED.key
    //     },
    //     {
    //       type: resourcesContext.messages[config.jobRunningStatus.IN_PROGRESS.label].toUpperCase(),
    //       value: config.jobRunningStatus.IN_PROGRESS.key
    //     }
    //   ],
    //   template: 'JobsStatus',
    //   type: 'MULTI_SELECT'
    // }
  ];

  const getTableColumns = () => {
    const columns = [
      // {
      //   key: 'jobId',
      //   header: resourcesContext.messages['jobId'],
      //   template: getJobIdTemplate,
      //   className: styles.middleColumn
      // },
      {
        key: 'creatorUsername',
        header: resourcesContext.messages['creatorUsername'],
        template: getJobCreatorUsernameTemplate,
        className: styles.middleColumn
      },
      {
        key: 'providerId',
        header: resourcesContext.messages['providerId'],
        template: getProviderIdTemplate,
        className: styles.middleColumn
      },
      // {
      //   key: 'jobType',
      //   header: resourcesContext.messages['jobType'],
      //   template: getJobTypeTemplate,
      //   className: styles.middleColumn
      // },
      {
        key: 'dataflowId',
        header: resourcesContext.messages['dataflowId'],
        template: getDataflowIdTemplate,
        className: styles.middleColumn
      },
      {
        key: 'datasetId',
        header: resourcesContext.messages['datasetId'],
        template: getDatasetIdTemplate,
        className: styles.middleColumn
      }
      // {
      //   key: 'jobStatus',
      //   header: resourcesContext.messages['jobStatus'],
      //   template: getJobStatusTemplate,
      //   className: styles.middleColumn
      // },
      // {
      //   key: 'dateAdded',
      //   header: resourcesContext.messages['dateAdded'],
      //   template: job => getDateAddedTemplate(job, 'dateAdded'),
      //   className: styles.smallColumn
      // },
      // {
      //   key: 'dateStatusChanged',
      //   header: resourcesContext.messages['dateStatusChanged'],
      //   template: job => getDateStatusChangedTemplate(job, 'dateStatusChanged'),
      //   className: styles.smallColumn
      // }
    ];

    if (isAdmin) {
      columns.push({
        key: 'buttonsUniqueId',
        header: resourcesContext.messages['actions'],
        template: getDeleteButton,
        className: styles.smallColumn
      });
    }

    return columns.map(column => (
      <Column
        body={column.template}
        className={column.className ? column.className : ''}
        columnResizeMode="expand"
        field={column.key}
        header={column.header}
        key={column.key}
        sortable={column.key !== 'buttonsUniqueId'}
      />
    ));
  };

  const getDeleteButton = rowData => (
    <ActionsColumn
      isDeletingDatasetData={isDeletingDatasetData}
      onDeleteClick={() => {
        setIsDeleteDialogVisible(true);
        setControlStatus(rowData);
      }}
      rowDataId={rowData.id}
    />
  );

  const getJobStatusTemplate = job => (
    <div>
      <LevelError
        className={config.jobRunningStatus[job.jobStatus].label}
        type={resourcesContext.messages[config.jobRunningStatus[job.jobStatus].label]}
      />
    </div>
  );

  const getJobIdTemplate = job => <p>{job.id}</p>;

  const getJobTypeTemplate = job => <p>{job.jobType}</p>;

  const getDateAddedTemplate = (job, field) =>
    isNil(job[field]) ? '-' : getDateTimeFormatByUserPreferences(job[field]);

  const getDateStatusChangedTemplate = (job, field) =>
    isNil(job[field]) ? '-' : getDateTimeFormatByUserPreferences(job[field]);

  const getJobCreatorUsernameTemplate = job => <p>{job.creatorUsername}</p>;

  const getDataflowIdTemplate = job => <p>{job.dataflowId}</p>;

  const getProviderIdTemplate = job => <p>{job.providerId}</p>;

  const getDatasetIdTemplate = job => <p>{job.datasetId}</p>;

  const onConfirmDeleteDialog = async () => {
    setLoadingStatus('pending');
    setIsDeleteDialogVisible(false);

    try {
      setIsDeletingDatasetData(true);
      getControlStatuses();
    } catch (error) {
      console.error('ControlStatus - onConfirmDeleteDialog.', error);
      setLoadingStatus('failed');

      notificationContext.add({ status: 'DELETE_PROVIDER_DATASET_DATA_ERROR' }, true);
    } finally {
      setIsDeletingDatasetData(false);
      setControlStatus(null);
    }
  };

  const onHideDeleteDialog = () => {
    setIsDeleteDialogVisible(false);
    setControlStatus(null);
  };

  const onRefresh = () => {
    setIsRefreshing(true);
    getControlStatuses();
  };

  const dialogFooter = (
    <div className={styles.footer}>
      <Button
        className="p-button-secondary"
        disabled={loadingStatus === 'pending'}
        icon={isRefreshing ? 'spinnerAnimate' : 'refresh'}
        label={resourcesContext.messages['refresh']}
        onClick={onRefresh}
      />
      <Button
        className={`p-button-secondary ${styles.buttonPushRight}`}
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={onCloseDialog}
      />
    </div>
  );

  const renderFilters = () => (
    <Filters
      className="lineItems"
      isLoading={loadingStatus === 'pending'}
      onFilter={() => setPagination({ firstRow: 0, numberRows: pagination.numberRows, pageNum: 0 })}
      onReset={() => setPagination({ firstRow: 0, numberRows: pagination.numberRows, pageNum: 0 })}
      options={filterOptions}
      recoilId="controlStatuses"
    />
  );

  const renderDialogContent = () => {
    if (isLoading) {
      return (
        <div className={styles.noDataContent}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      );
    }

    if (isFiltered && isEmpty(controlStatuses)) {
      return (
        <div className={styles.dialogContent}>
          {renderFilters()}
          <div className={styles.noDataContent}>
            <p>{resourcesContext.messages['noDatasetsWithSelectedParameters']}</p>
          </div>
        </div>
      );
    }

    if (isEmpty(controlStatuses)) {
      return (
        <div className={styles.noDataContent}>
          <span>{resourcesContext.messages['noData']}</span>
        </div>
      );
    }

    return (
      <div className={styles.dialogContent}>
        {renderFilters()}
        <DataTable
          autoLayout={true}
          className={styles.jobStatusesTable}
          first={firstRow}
          hasDefaultCurrentPage={true}
          lazy={true}
          loading={loadingStatus === 'pending' && isNil(controlStatus)}
          onPage={event =>
            setPagination({
              firstRow: event.first,
              numberRows: event.rows,
              pageNum: event.page
            })
          }
          onSort={onSort}
          paginator={true}
          paginatorRight={
            <PaginatorRecordsCount
              dataLength={totalRecords}
              filteredDataLength={filteredRecords}
              isFiltered={isFiltered}
            />
          }
          reorderableColumns={true}
          resizableColumns={true}
          rows={numberRows}
          rowsPerPageOptions={[5, 10, 15]}
          sortField={sort.field}
          sortOrder={sort.order}
          totalRecords={isFiltered ? filteredRecords : totalRecords}
          value={controlStatuses}>
          {getTableColumns()}
        </DataTable>
      </div>
    );
  };

  return (
    <Fragment>
      <Dialog
        blockScroll={false}
        className="responsiveBigDialog"
        footer={dialogFooter}
        header={resourcesContext.messages['controlStatus']}
        modal={true}
        onHide={onCloseDialog}
        visible={isDialogVisible}>
        {renderDialogContent()}
      </Dialog>

      {isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm="p-button-danger"
          header={resourcesContext.messages['providerDatasetDataRemoveDialogHeader']}
          labelCancel={resourcesContext.messages['cancel']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onConfirmDeleteDialog}
          onHide={onHideDeleteDialog}
          visible={isDeleteDialogVisible}>
          {resourcesContext.messages['providerDatasetDataRemoveDialogContent']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
