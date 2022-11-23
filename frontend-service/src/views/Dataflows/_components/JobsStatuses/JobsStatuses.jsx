import {useContext, useEffect, useState} from 'react';
import {useRecoilValue} from 'recoil';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import {config} from 'conf';
import styles from './JobsStatuses.module.scss';
import {Column} from 'primereact/column';
import {Button} from 'views/_components/Button';
import {DataTable} from 'views/_components/DataTable';
import {Dialog} from 'views/_components/Dialog';
import {Filters} from 'views/_components/Filters';
import {LevelError} from 'views/_components/LevelError';
import {PaginatorRecordsCount} from 'views/_components/DataTable/_functions/Utils/PaginatorRecordsCount';
import {Spinner} from 'views/_components/Spinner';
import {JobsStatusesService} from 'services/JobsStatusesService';
import {NotificationContext} from 'views/_functions/Contexts/NotificationContext';
import {ResourcesContext} from 'views/_functions/Contexts/ResourcesContext';
import {filterByCustomFilterStore} from 'views/_components/Filters/_functions/Stores/filterStore';
import {useApplyFilters} from 'views/_functions/Hooks/useApplyFilters';
import {useDateTimeFormatByUserPreferences} from 'views/_functions/Hooks/useDateTimeFormatByUserPreferences';
import {FiltersUtils} from 'views/_components/Filters/_functions/Utils/FiltersUtils';

export const JobsStatuses = ({onCloseDialog, isDialogVisible}) => {
    const filterBy = useRecoilValue(filterByCustomFilterStore('jobsStatuses'));
    const resourcesContext = useContext(ResourcesContext);
    const notificationContext = useContext(NotificationContext);
    const [filteredRecords, setFilteredRecords] = useState(0);
    const [isFiltered, setIsFiltered] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [loadingStatus, setLoadingStatus] = useState('idle');
    const [pagination, setPagination] = useState({firstRow: 0, numberRows: 10, pageNum: 0});
    const [sort, setSort] = useState({field: 'dateAdded', order: -1});
    const [totalRecords, setTotalRecords] = useState(0);
    const [jobsStatuses, setJobsStatusesList] = useState([]);
    const [jobStatus, setJobStatus] = useState(null);

    const {getDateTimeFormatByUserPreferences} = useDateTimeFormatByUserPreferences();
    const {setData} = useApplyFilters('jobsStatuses');

    const {firstRow, numberRows, pageNum} = pagination;

    useEffect(() => {
        getJobsStatuses();
    }, [pagination, sort]);

    const getJobsStatuses = async () => {
        setLoadingStatus('pending');

        try {
            const data = await JobsStatusesService.getJobsStatuses({
                pageNum,
                numberRows,
                sortOrder: sort.order,
                sortField: sort.field,
                jobId: filterBy.jobId,
                jobType: filterBy.jobType,
                processId: filterBy.processId,
                creatorUsername: filterBy.creatorUsername,
                jobStatus: filterBy.jobStatus?.join()
            });
            
            setTotalRecords(data.totalRecords);
            setJobsStatusesList(data.jobsList);
            setFilteredRecords(data.filteredRecords);
            setIsFiltered(FiltersUtils.getIsFiltered(filterBy));
            setData(data.jobsList);
            setLoadingStatus('success');
        } catch (error) {
            console.error('JobsStatus - getJobsStatuses.', error);
            setLoadingStatus('error');
            notificationContext.add({type: 'GET_JOBS_STATUSES_ERROR'}, true);
        } finally {
            setIsLoading(false);
            setIsRefreshing(false);
        }
    };

    const onSort = event => setSort({field: event.sortField, order: event.sortOrder});

    const filterOptions = [
        {
            nestedOptions: [
                {key: 'jobId', label: resourcesContext.messages['jobId'], keyfilter: 'pint'},
                {key: 'processId', label: resourcesContext.messages['processId']},
                {key: 'creatorUsername', label: resourcesContext.messages['creatorUsername']}
            ],
            type: 'INPUT'
        },
        {
            key: 'jobType',
            label: resourcesContext.messages['jobType'],
            multiSelectOptions: [
                {
                    type: resourcesContext.messages[config.jobType.IMPORT.label].toUpperCase(),
                    value: config.jobType.IMPORT.key
                },
                {
                    type: resourcesContext.messages[config.jobType.VALIDATION.label].toUpperCase(),
                    value: config.jobType.VALIDATION.key
                },
                {
                    type: resourcesContext.messages[config.jobType.RELEASE.label].toUpperCase(),
                    value: config.jobType.RELEASE.key
                },
                {
                    type: resourcesContext.messages[config.jobType.EXPORT.label].toUpperCase(),
                    value: config.jobType.EXPORT.key
                },
                {
                    type: resourcesContext.messages[config.jobType.COPY_TO_EU_DATASET.label].toUpperCase(),
                    value: config.jobType.COPY_TO_EU_DATASET.key
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
                    type: resourcesContext.messages[config.jobRunningStatus.FAILED.label].toUpperCase(),
                    value: config.jobRunningStatus.FAILED.key
                },
                {
                    type: resourcesContext.messages[config.jobRunningStatus.QUEUED.label].toUpperCase(),
                    value: config.jobRunningStatus.QUEUED.key
                },
                {
                    type: resourcesContext.messages[config.jobRunningStatus.REFUSED.label].toUpperCase(),
                    value: config.jobRunningStatus.REFUSED.key
                },
                {
                    type: resourcesContext.messages[config.jobRunningStatus.FINISHED.label].toUpperCase(),
                    value: config.jobRunningStatus.FINISHED.key
                },
                {
                    type: resourcesContext.messages[config.jobRunningStatus.CANCELLED.label].toUpperCase(),
                    value: config.jobRunningStatus.CANCELLED.key
                },
                {
                    type: resourcesContext.messages[config.jobRunningStatus.IN_PROGRESS.label].toUpperCase(),
                    value: config.jobRunningStatus.IN_PROGRESS.key
                }
            ],
            template: 'JobsStatus',
            type: 'MULTI_SELECT'
        }
    ];

    const getTableColumns = () => {
        const columns = [
            {
                key: 'jobId',
                header: resourcesContext.messages['jobId'],
                template: getJobIdTemplate,
                className: styles.middleColumn
            },
            {
                key: 'processId',
                header: resourcesContext.messages['processId'],
                template: getJobProcessIdTemplate,
                className: styles.largeColumn
            },
            {
                key: 'creatorUsername',
                header: resourcesContext.messages['creatorUsername'],
                template: getJobCreatorUsernameTemplate,
                className: styles.middleColumn
            },
            {
                key: 'jobType',
                header: resourcesContext.messages['jobType'],
                template: getJobTypeTemplate,
                className: styles.middleColumn
            },
            {
                key: 'jobStatus',
                header: resourcesContext.messages['jobStatus'],
                template: getJobStatusTemplate,
                className: styles.middleColumn
            },
            {
                key: 'dateAdded',
                header: resourcesContext.messages['dateAdded'],
                template: job => getDateAddedTemplate(job, 'dateAdded'),
                className: styles.smallColumn
            },
            {
                key: 'dateStatusChanged',
                header: resourcesContext.messages['dateStatusChanged'],
                template: job => getDateStatusChangedTemplate(job, 'dateStatusChanged'),
                className: styles.smallColumn
            },
        ];

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

    const getJobStatusTemplate = job => (
        <div>
            <LevelError
                className={config.jobRunningStatus[job.jobStatus].label}
                type={resourcesContext.messages[config.jobRunningStatus[job.jobStatus].label]}
            />
        </div>
    );

    const getJobIdTemplate = job => (
        <p>
            {job.id}
        </p>
    );
    const getJobTypeTemplate = job => (
        <p>
            {job.jobType} 
        </p>
    );
    

    const getDateAddedTemplate = (job, field) =>
    isNil(job[field]) ? '-' : getDateTimeFormatByUserPreferences(job[field]);

    const getDateStatusChangedTemplate = (job, field) =>
    isNil(job[field]) ? '-' : getDateTimeFormatByUserPreferences(job[field]);

    const getJobCreatorUsernameTemplate = job => (
        <p>
            {job.creatorUsername} 
        </p>
    );

    const getJobProcessIdTemplate = job => (
        <p>
            {job.processId} 
        </p>
    );

    const onRefresh = () => {
        setIsRefreshing(true);
        getJobsStatuses();
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
            onFilter={() => setPagination({firstRow: 0, numberRows: pagination.numberRows, pageNum: 0})}
            onReset={() => setPagination({firstRow: 0, numberRows: pagination.numberRows, pageNum: 0})}
            options={filterOptions}
            recoilId="jobsStatuses"
        />
    );

    const renderDialogContent = () => {
        if (isLoading) {
            return (
                <div className={styles.noDataContent}>
                    <Spinner className={styles.spinnerPosition}/>
                </div>
            );
        }

        if (isFiltered && isEmpty(jobsStatuses)) {
            return (
                <div className={styles.dialogContent}>
                    {renderFilters()}
                    <div className={styles.noDataContent}>
                        <p>{resourcesContext.messages
                        ['jobsStatusesNotMatchingFilter']}</p>
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
                    first={firstRow}
                    hasDefaultCurrentPage={true}
                    lazy={true}
                    loading={loadingStatus === 'pending' && isNil(jobStatus)}
                    onPage={event => setPagination({
                        firstRow: event.first,
                        numberRows: event.rows,
                        pageNum: event.page
                    })}
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
                    value={jobsStatuses}>
                    {getTableColumns()}
                </DataTable>
            </div>
        );
    };

    return (
            <Dialog
                blockScroll={false}
                className="responsiveBigDialog"
                footer={dialogFooter}
                header={resourcesContext.messages['jobsMonitoring']}
                modal={true}
                onHide={onCloseDialog}
                visible={isDialogVisible}>
                {renderDialogContent()}
            </Dialog>
    );
};
