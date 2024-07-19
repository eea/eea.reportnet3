import { Fragment, useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { Column } from 'primereact/column';

import { useRecoilValue } from 'recoil';

import styles from './JobsStatuses.module.scss';

import {
  getProviderColumns,
  getAdminCustodianColumns,
  getHistoryProviderColumns,
  getHistoryAdminCustodianColumns
} from './columns';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';
import { routes } from 'conf/routes';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { Filters } from 'views/_components/Filters';
import { LevelError } from 'views/_components/LevelError';
import { Spinner } from 'views/_components/Spinner';

import { JobsStatusesService } from 'services/JobsStatusesService';

import { getUrl } from 'repositories/_utils/UrlUtils';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from '../../../_functions/Contexts/UserContext';

import { filterByCustomFilterStore } from 'views/_components/Filters/_functions/Stores/filterStore';
import { FiltersUtils } from 'views/_components/Filters/_functions/Utils/FiltersUtils';
import { PaginatorRecordsCount } from 'views/_components/DataTable/_functions/Utils/PaginatorRecordsCount';
import { useApplyFilters } from 'views/_functions/Hooks/useApplyFilters';

import { useDateTimeFormatByUserPreferences } from 'views/_functions/Hooks/useDateTimeFormatByUserPreferences';

import { TextUtils } from 'repositories/_utils/TextUtils';

const { permissions } = config;

export const JobsStatuses = ({ onCloseDialog, isDialogVisible }) => {
  const filterBy = useRecoilValue(filterByCustomFilterStore('jobsStatuses'));

  const navigate = useNavigate();

  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);
  const userContext = useContext(UserContext);
  const isAdmin = userContext.hasPermission([permissions.roles.ADMIN.key]);
  const isCustodian = userContext.hasPermission([permissions.roles.CUSTODIAN.key, permissions.roles.STEWARD.key]);
  const isProvider = userContext.hasPermission([permissions.roles.LEAD_REPORTER.key]);

  const [activeIndex, setActiveIndex] = useState(0);
  const [expandedRows, setExpandedRows] = useState(null);
  const [filteredJobs, setFilteredJobs] = useState(0);
  const [filteredRecords, setFilteredRecords] = useState(0);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isFiltered, setIsFiltered] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isStatusInfoDialogVisible, setIsStatusInfoDialogVisible] = useState(false);
  const [jobStatus, setJobStatus] = useState(null);
  const [jobStatusHistory, setJobStatusHistory] = useState({});
  const [jobsStatuses, setJobsStatusesList] = useState([]);
  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [pagination, setPagination] = useState({ firstRow: 0, numberRows: 10, pageNum: 0 });
  const [providersTotalRecords, setProvidersTotalRecords] = useState(0);
  const [remainingJobs, setRemainingJobs] = useState(0);
  const [sort, setSort] = useState({ field: 'dateStatusChanged', order: -1 });
  const [totalRecords, setTotalRecords] = useState(0);

  const { getDateTimeFormatByUserPreferences, getDateDifferenceInMinutes } = useDateTimeFormatByUserPreferences();
  const { setData } = useApplyFilters('jobsStatuses');

  const { firstRow, numberRows, pageNum } = pagination;

  useEffect(() => {
    getJobsStatuses(undefined, 0, 10);
  }, [filterBy]);

  const getJobsStatuses = async (index, page, rows, sortOption) => {
    setLoadingStatus('pending');
    let data;
    try {
      if (index !== undefined ? index === 0 : activeIndex === 0) {
        data = await JobsStatusesService.getJobsStatuses({
          pageNum: page !== undefined ? page : pageNum,
          numberRows: rows !== undefined ? rows : numberRows,
          sortOrder: sortOption !== undefined ? sortOption.sortOrder : sort.order,
          sortField: sortOption !== undefined ? sortOption.sortField : sort.field,
          jobId: filterBy.jobId,
          jobType: filterBy.jobType?.join(),
          dataflowId: filterBy.dataflowId,
          dataflowName: filterBy.dataflowName,
          providerId: filterBy.providerId,
          datasetId: filterBy.datasetId,
          datasetName: filterBy.datasetName,
          creatorUsername: !isAdmin
            ? isProvider
              ? userContext.preferredUsername
              : filterBy.creatorUsername
            : undefined,
          jobStatus: filterBy.jobStatus?.join()
        });
        setData(data.jobsList);
        setJobsStatusesList(data.jobsList);
        setRemainingJobs(data.remainingJobs);
      } else {
        data = await JobsStatusesService.getJobsHistory({
          pageNum: page !== undefined ? page : pageNum,
          numberRows: rows !== undefined ? rows : numberRows,
          sortOrder: sortOption !== undefined ? sortOption.sortOrder : sort.order,
          sortField: sortOption !== undefined ? sortOption.sortField : sort.field,
          jobId: filterBy.jobId,
          jobType: filterBy.jobType?.join(),
          dataflowId: filterBy.dataflowId,
          dataflowName: filterBy.dataflowName,
          providerId: filterBy.providerId,
          datasetId: filterBy.datasetId,
          datasetName: filterBy.datasetName,
          creatorUsername: !isAdmin
            ? isProvider
              ? userContext.preferredUsername
              : filterBy.creatorUsername
            : undefined,
          jobStatus: filterBy.jobStatus?.join()
        });
        if (!isEmpty(filterBy)) {
          setData(data.jobHistoryVOList);
          setJobsStatusesList(data.jobHistoryVOList);
          setFilteredJobs(data.filteredJobs);
        } else {
          setIsFiltered(false);
          setJobsStatusesList([]);
          if (isProvider && index && !page) {
            setProvidersTotalRecords(data.filteredRecords);
          }
        }
      }

      if ((index !== undefined ? index === 0 : activeIndex === 0) || !isEmpty(filterBy)) {
        if (isProvider && !providersTotalRecords) {
          setProvidersTotalRecords(data.filteredRecords);
        } else if (index !== undefined && !page) {
          setProvidersTotalRecords(data.filteredRecords);
        }

        if (isProvider && Object.keys(filterBy).length === 1) {
          setIsFiltered(FiltersUtils.getIsFiltered({}));
        } else {
          setIsFiltered(FiltersUtils.getIsFiltered(filterBy));
        }

        setFilteredRecords(data.filteredRecords);
        setTotalRecords(data.totalRecords);
      }

      setLoadingStatus('success');
    } catch (error) {
      console.error('JobsStatus - getJobsStatuses.', error);
      setLoadingStatus('error');
      notificationContext.add({ type: 'GET_JOBS_STATUSES_ERROR' }, true);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  const getJobHistory = async jobId => {
    setLoadingStatus('pending');

    try {
      const data = await JobsStatusesService.getJobHistory(jobId);

      setJobStatusHistory(state => ({
        ...state,
        [jobId]: data
      }));

      setLoadingStatus('success');
    } catch (error) {
      console.error('JobsStatus - getJobHistory.', error);
      setLoadingStatus('error');
      notificationContext.add({ type: 'GET_JOBS_STATUSES_ERROR' }, true);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  const tabMenuItems = [
    {
      className: styles.flow_tab,
      id: 'monitoring',
      label: resourcesContext.messages['monitoring']
    },
    { className: styles.flow_tab, id: 'history', label: resourcesContext.messages['history'] }
  ];

  const onChangeTab = index => {
    setActiveIndex(index);
    setRemainingJobs(0);
    setFilteredJobs(0);
    setProvidersTotalRecords(0);
    if (index === 1) {
      setIsFiltered(false);
      setJobsStatusesList([]);
      getJobsStatuses(index);
    } else {
      if (isEmpty(jobsStatuses) && isEmpty(filterBy)) {
        setIsLoading(true);
        getJobsStatuses(index);
      }
    }
  };

  const onSort = event => {
    setSort({ field: event.sortField, order: event.sortOrder });
    getJobsStatuses(undefined, undefined, undefined, event);
  };

  const filterOptions = [
    {
      nestedOptions:
        isAdmin || isCustodian
          ? [
              { key: 'jobId', label: resourcesContext.messages['jobId'], keyfilter: 'pint' },
              { key: 'dataflowId', label: resourcesContext.messages['dataflowId'] },
              { key: 'dataflowName', label: resourcesContext.messages['dataflowNameTwoWords'] },
              { key: 'datasetId', label: resourcesContext.messages['datasetId'] },
              { key: 'datasetName', label: resourcesContext.messages['datasetName'] },
              { key: 'providerId', label: resourcesContext.messages['providerId'] },
              { key: 'creatorUsername', label: resourcesContext.messages['creatorUsername'] }
            ]
          : [
              { key: 'jobId', label: resourcesContext.messages['jobId'], keyfilter: 'pint' },
              { key: 'dataflowId', label: resourcesContext.messages['dataflowId'] },
              { key: 'dataflowName', label: resourcesContext.messages['dataflowNameTwoWords'] },
              { key: 'datasetId', label: resourcesContext.messages['datasetId'] },
              { key: 'datasetName', label: resourcesContext.messages['datasetName'] }
            ],
      type: 'INPUT'
    },
    {
      key: 'jobType',
      label: resourcesContext.messages['jobType'],
      multiSelectOptions: [
        {
          type: resourcesContext.messages[config.jobType.COPY_TO_EU_DATASET.label].toUpperCase(),
          value: config.jobType.COPY_TO_EU_DATASET.key
        },
        {
          type: resourcesContext.messages[config.jobType.DELETE.label].toUpperCase(),
          value: config.jobType.DELETE.key
        },
        {
          type: resourcesContext.messages[config.jobType.EXPORT.label].toUpperCase(),
          value: config.jobType.EXPORT.key
        },
        {
          type: resourcesContext.messages[config.jobType.FILE_EXPORT.label].toUpperCase(),
          value: config.jobType.FILE_EXPORT.key
        },
        {
          type: resourcesContext.messages[config.jobType.IMPORT.label].toUpperCase(),
          value: config.jobType.IMPORT.key
        },
        {
          type: resourcesContext.messages[config.jobType.RELEASE.label].toUpperCase(),
          value: config.jobType.RELEASE.key
        },
        {
          type: resourcesContext.messages[config.jobType.VALIDATION.label].toUpperCase(),
          value: config.jobType.VALIDATION.key
        }
      ],
      template: 'jobType',
      type: 'MULTI_SELECT'
    },
    {
      key: 'jobStatus',
      label: resourcesContext.messages['jobStatus'],
      multiSelectOptions: [
        {
          type: resourcesContext.messages[config.jobRunningStatus.QUEUED.label].toUpperCase(),
          value: config.jobRunningStatus.QUEUED.key
        },
        {
          type: resourcesContext.messages[config.jobRunningStatus.IN_PROGRESS.label].toUpperCase(),
          value: config.jobRunningStatus.IN_PROGRESS.key
        },
        {
          type: resourcesContext.messages[config.jobRunningStatus.FINISHED.label].toUpperCase(),
          value: config.jobRunningStatus.FINISHED.key
        },
        {
          type: resourcesContext.messages[config.jobRunningStatus.FAILED.label].toUpperCase(),
          value: config.jobRunningStatus.FAILED.key
        },
        {
          type: resourcesContext.messages[config.jobRunningStatus.REFUSED.label].toUpperCase(),
          value: config.jobRunningStatus.REFUSED.key
        },
        {
          type: resourcesContext.messages[config.jobRunningStatus.CANCELED.label].toUpperCase(),
          value: config.jobRunningStatus.CANCELED.key
        },
        {
          type: resourcesContext.messages[config.jobRunningStatus.CANCELED_BY_ADMIN.label].toUpperCase(),
          value: config.jobRunningStatus.CANCELED_BY_ADMIN.key
        }
      ],
      template: 'JobsStatus',
      type: 'MULTI_SELECT'
    }
  ];

  const getJobsStatusesColumns = () => {
    const adminCustodianColumns = getAdminCustodianColumns(styles, resourcesContext, templates);
    const historyAdminCustodianColumns = getHistoryAdminCustodianColumns(styles, resourcesContext, templates);
    const historyProviderColumns = getHistoryProviderColumns(styles, resourcesContext, templates);
    const providerColumns = getProviderColumns(styles, resourcesContext, templates);

    if (isAdmin || isCustodian) {
      if (activeIndex === 0) {
        return adminCustodianColumns;
      } else {
        return historyAdminCustodianColumns;
      }
    } else {
      if (activeIndex === 0) {
        return providerColumns;
      } else {
        return historyProviderColumns;
      }
    }
  };

  const getTableColumns = () => {
    const columns = getJobsStatusesColumns();

    if (isAdmin && activeIndex !== 1) {
      columns.push({
        key: 'buttonsUniqueId',
        header: resourcesContext.messages['actions'],
        template: getCancelButton,
        className: styles.smallColumn
      });
    }

    return columns.map(column => (
      <Column
        body={column.template}
        className={column.className ? column.className : ''}
        columnResizeMode="expand"
        expander={activeIndex === 0 && column.key === 'expanderColumn'}
        field={column.key}
        header={column.header}
        key={column.key}
        sortable={column.key !== 'buttonsUniqueId' && column.key !== 'expanderColumn'}
        style={column.style}
      />
    ));
  };

  const getCancelButton = job => (
    <ActionsColumn
      disabledButtons={
        !(
          job.jobStatus === 'IN_PROGRESS' &&
          (job.jobType === 'IMPORT' ||
            job.jobType === 'VALIDATION' ||
            job.jobType === 'RELEASE' ||
            job.jobType === 'FILE_EXPORT') &&
          getDateDifferenceInMinutes(job.dateStatusChanged) > 9
        )
      }
      onDeleteClick={() => {
        setIsDeleteDialogVisible(true);
        setJobStatus(job);
      }}
      rowDataId={job.id}
      tooltip={resourcesContext.messages['cancel']}
    />
  );

  const getJobStatusTemplate = job => (
    <div
      className={styles.statusBox}
      onClick={() => {
        setIsStatusInfoDialogVisible(true);
        setJobStatus(job);
      }}>
      <LevelError
        className={config.jobRunningStatus[job.jobStatus].label}
        type={resourcesContext.messages[config.jobRunningStatus[job.jobStatus].label]}
      />
    </div>
  );

  const getJobIdTemplate = job => <p>{activeIndex === 0 ? job.id : job.jobId}</p>;

  const getFmeJobIdTemplate = job => (
    <a
      href={getUrl(routes.FME, { fmeJobId: job.fmeJobId }, true)}
      rel="noopener noreferrer"
      style={{ cursor: 'pointer' }}
      target="_blank"
      type="button">
      {job.fmeJobId}
    </a>
  );

  const getJobTypeTemplate = job => {
    const isRelease = job.release && job.jobType === 'VALIDATION' ? '*' : '';
    return (
      <div className={styles.tooltip}>
        {job.jobType}
        {isRelease}
        {isRelease && <span className={styles.tooltiptext}>Validation For Release</span>}
      </div>
    );
  };

  const getDateAddedTemplate = (job, field) =>
    isNil(job[field]) ? '-' : getDateTimeFormatByUserPreferences(job[field]);

  const getDateStatusChangedTemplate = (job, field) =>
    isNil(job[field]) ? '-' : getDateTimeFormatByUserPreferences(job[field]);

  const getJobCreatorUsernameTemplate = job => <p>{job.creatorUsername}</p>;

  const getDataflowIdTemplate = job => {
    const dataflowId = job.dataflowId;
    return (
      <div className={styles.tooltip}>
        <p>
          <a
            href={getUrl(routes.DATAFLOW, { dataflowId }, true)}
            onClick={() => {
              navigate(getUrl(routes.DATAFLOW, { dataflowId }, true));
            }}>
            {dataflowId}
            <span className={styles.tooltiptext}> {job.dataflowName} </span>
          </a>
        </p>
      </div>
    );
  };

  const getDatasetIdTemplate = job => {
    const dataflowId = job.dataflowId;
    const datasetId = job.datasetId;
    return (
      <div className={styles.tooltip}>
        <p>
          <a
            href={getUrl(routes.DATASET, { dataflowId, datasetId }, true)}
            onClick={() => {
              navigate(getUrl(routes.DATASET, { dataflowId, datasetId }, true));
            }}>
            {job.datasetId}
            <span className={styles.tooltiptext}> {job.datasetName} </span>
          </a>
        </p>
      </div>
    );
  };

  const getProviderIdTemplate = job => <p>{job.providerId}</p>;

  const rowExpansionTemplate = data => {
    const historyData = jobStatusHistory[data.id] ?? [];

    return (
      <div className={styles.expandedTable}>
        <h6>Job history for job {data.id} </h6>
        <DataTable responsiveLayout="scroll" value={historyData}>
          <Column
            body={getJobStatusTemplate}
            field="jobStatus"
            header={resourcesContext.messages['jobStatus']}></Column>
          <Column
            body={job => getDateStatusChangedTemplate(job, 'dateStatusChanged')}
            field="dateStatusChanged"
            header={resourcesContext.messages['dateStatusChanged']}></Column>
        </DataTable>
      </div>
    );
  };

  const templates = {
    getJobIdTemplate,
    getFmeJobIdTemplate,
    getDataflowIdTemplate,
    getDatasetIdTemplate,
    getJobTypeTemplate,
    getJobStatusTemplate,
    getDateAddedTemplate,
    getDateStatusChangedTemplate,
    getJobCreatorUsernameTemplate,
    getProviderIdTemplate
  };

  const onRefresh = () => {
    setIsRefreshing(true);
    getJobsStatuses();
  };

  const dialogFooter = (
    <div className={styles.footer}>
      <Button
        className="p-button-secondary"
        disabled={loadingStatus === 'pending' || isEmpty(jobsStatuses)}
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

  const onConfirmDeleteDialog = async () => {
    setLoadingStatus('pending');
    setIsDeleteDialogVisible(false);
    try {
      await JobsStatusesService.cancelJob(jobStatus.id);
      setLoadingStatus('success');
    } catch (error) {
      console.error('JobsStatus - onConfirmDeleteDialog.', error);
      setLoadingStatus('failed');
    } finally {
      setJobStatus(null);
      getJobsStatuses();
    }
  };

  const onHideDeleteDialog = () => {
    setIsDeleteDialogVisible(false);
    setJobStatus(null);
  };

  const onHideStatusInfoDialog = () => {
    setIsStatusInfoDialogVisible(false);
    setJobStatus(null);
  };

  const renderFilters = () => (
    <Filters
      activeIndex={activeIndex}
      className="lineItems"
      isJobsStatuses={true}
      isLoading={loadingStatus === 'pending'}
      isProvider={isProvider}
      onFilter={() => setPagination({ firstRow: 0, numberRows: pagination.numberRows, pageNum: 0 })}
      onReset={() => {
        setPagination({ firstRow: 0, numberRows: pagination.numberRows, pageNum: 0 });
        if (activeIndex === 1) {
          setIsFiltered(false);
          setJobsStatusesList([]);
        }
      }}
      options={filterOptions}
      providerUsername={userContext.preferredUsername}
      recoilId="jobsStatuses"
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

    if ((isFiltered || activeIndex) && isEmpty(jobsStatuses)) {
      return (
        <div className={styles.dialogContent}>
          {renderFilters()}
          <div className={styles.noDataContent}>
            <p>{resourcesContext.messages['jobsStatusesNotMatchingFilter']}</p>
          </div>
        </div>
      );
    }

    if (isEmpty(jobsStatuses)) {
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
          expandedRows={expandedRows}
          first={firstRow}
          hasDefaultCurrentPage={true}
          lazy={true}
          loading={loadingStatus === 'pending' && isNil(jobStatus)}
          onPage={event => {
            setPagination({
              firstRow: event.first,
              numberRows: event.rows,
              pageNum: event.page
            });
            getJobsStatuses(activeIndex, event.page, event.rows);
          }}
          onRowExpand={e => {
            const historyData = jobStatusHistory[e.data.id] ?? [];

            if (!(historyData.length === 0)) {
              const jobHistoryFinalized = historyData.find(
                jobHistory =>
                  jobHistory.jobStatus === 'REFUSED' ||
                  jobHistory.jobStatus === 'CANCELED' ||
                  jobHistory.jobStatus === 'FAILED' ||
                  jobHistory.jobStatus === 'FINISHED' ||
                  jobHistory.jobStatus === 'CANCELED_BY_ADMIN'
              );

              if (!jobHistoryFinalized) {
                getJobHistory(e.data.id);
              }
            } else {
              getJobHistory(e.data.id);
            }
          }}
          onRowToggle={e => {
            setExpandedRows(e.data);
          }}
          onSort={onSort}
          paginator={true}
          paginatorRight={
            <PaginatorRecordsCount
              dataLength={isProvider && !isAdmin ? providersTotalRecords : totalRecords}
              filteredDataLength={filteredRecords}
              filteredJobsLength={filteredJobs}
              isFiltered={isFiltered}
              remainingJobsLength={remainingJobs}
            />
          }
          reorderableColumns={true}
          resizableColumns={true}
          rowExpansionTemplate={rowExpansionTemplate}
          rows={numberRows}
          rowsPerPageOptions={[5, 10, 15]}
          sortField={sort.field}
          sortOrder={sort.order}
          totalRecords={isFiltered ? filteredRecords : isProvider ? providersTotalRecords : totalRecords}
          value={jobsStatuses}>
          {getTableColumns()}
        </DataTable>
      </div>
    );
  };

  return (
    <Fragment>
      <Dialog
        activeIndex={activeIndex}
        blockScroll={false}
        className="responsiveBigDialog"
        footer={dialogFooter}
        header={resourcesContext.messages['jobsMonitoring']}
        isJobsStatusesDialog={true}
        modal={true}
        onHide={onCloseDialog}
        tabChange={onChangeTab}
        tabMenuItems={tabMenuItems}
        visible={isDialogVisible}>
        {renderDialogContent()}
      </Dialog>

      {isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm="p-button-danger"
          header={resourcesContext.messages['cancel']}
          labelCancel={resourcesContext.messages['cancel']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onConfirmDeleteDialog}
          onHide={onHideDeleteDialog}
          visible={isDeleteDialogVisible}>
          {
            <p
              dangerouslySetInnerHTML={{
                __html: TextUtils.parseText(resourcesContext.messages['cancelProcess'], {
                  selectedJobType: jobStatus.jobType,
                  jobId: jobStatus.id
                })
              }}></p>
          }
        </ConfirmDialog>
      )}

      {isStatusInfoDialogVisible && (
        <Dialog
          blockScroll={false}
          className="responsiveDialog"
          header={resourcesContext.messages['info']}
          modal={true}
          onHide={onHideStatusInfoDialog}
          visible={isStatusInfoDialogVisible}>
          {jobStatus.jobInfo ? jobStatus.jobInfo : resourcesContext.messages['noJobStatusInfo']}
        </Dialog>
      )}
    </Fragment>
  );
};
