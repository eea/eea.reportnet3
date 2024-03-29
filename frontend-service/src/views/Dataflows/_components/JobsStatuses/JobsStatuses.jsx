import {Fragment, useContext, useEffect, useState} from 'react';
import {useRecoilValue} from 'recoil';
import {useNavigate} from 'react-router-dom';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import {config} from 'conf';
import {AwesomeIcons} from 'conf/AwesomeIcons';
import styles from './JobsStatuses.module.scss';
import {Column} from 'primereact/column';
import {ActionsColumn} from 'views/_components/ActionsColumn';
import {Button} from 'views/_components/Button';
import {ConfirmDialog} from 'views/_components/ConfirmDialog';
import {DataTable} from 'views/_components/DataTable';
import {Dialog} from 'views/_components/Dialog';
import {Filters} from 'views/_components/Filters';
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome';
import {InputText} from 'views/_components/InputText';
import {LevelError} from 'views/_components/LevelError';
import {PaginatorRecordsCount} from 'views/_components/DataTable/_functions/Utils/PaginatorRecordsCount';
import {Spinner} from 'views/_components/Spinner';
import {TooltipButton} from 'views/_components/TooltipButton';
import {BackgroundProcessService} from 'services/BackgroundProcessService';
import { JobsStatusesService } from 'services/JobsStatusesService';
import {NotificationContext} from 'views/_functions/Contexts/NotificationContext';
import {ResourcesContext} from 'views/_functions/Contexts/ResourcesContext';
import {filterByCustomFilterStore} from 'views/_components/Filters/_functions/Stores/filterStore';
import {useApplyFilters} from 'views/_functions/Hooks/useApplyFilters';
import {useDateTimeFormatByUserPreferences} from 'views/_functions/Hooks/useDateTimeFormatByUserPreferences';
import {FiltersUtils} from 'views/_components/Filters/_functions/Utils/FiltersUtils';
import {getUrl} from "repositories/_utils/UrlUtils";
import {routes} from "conf/routes";
import {UserContext} from "../../../_functions/Contexts/UserContext";

const {permissions} = config;

export const JobsStatuses = ({onCloseDialog, isDialogVisible}) => {
    const filterBy = useRecoilValue(filterByCustomFilterStore('jobsStatuses'));
    const navigate = useNavigate();

    const resourcesContext = useContext(ResourcesContext);
    const notificationContext = useContext(NotificationContext);
    const userContext = useContext(UserContext);
    const isAdmin = userContext.hasPermission([permissions.roles.ADMIN.key]);

    const [editedValidationStatusPriority, setEditedValidationStatusPriority] = useState();
    const [filteredRecords, setFilteredRecords] = useState(0);
    const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
    const [isEditDialogVisible, setIsEditDialogVisible] = useState(false);
    const [isFiltered, setIsFiltered] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [isUpdating, setIsUpdating] = useState(false);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [loadingStatus, setLoadingStatus] = useState('idle');
    const [pagination, setPagination] = useState({firstRow: 0, numberRows: 10, pageNum: 0});
    const [sort, setSort] = useState({field: 'queuedDate', order: -1});
    const [totalRecords, setTotalRecords] = useState(0);
    const [jobsStatuses, setJobsStatusesList] = useState([]);
    const [validationStatus, setValidationStatus] = useState(null);

    const [jobStatus, setJobStatus] = useState(null);

    const {getDateTimeFormatByUserPreferences} = useDateTimeFormatByUserPreferences();
    const {setData} = useApplyFilters('jobsStatuses');

    const {firstRow, numberRows, pageNum} = pagination;

    useEffect(() => {
        getJobsStatuses();
    }, [pagination, sort]);

    useEffect(() => {
        if (isEditDialogVisible) {
            setEditedValidationStatusPriority(validationStatus.priority);
        }
    }, [isEditDialogVisible]);

    const getJobsStatuses = async () => {
        setLoadingStatus('pending');

        try {
            const data = await JobsStatusesService.getJobsStatuses();
            console.log("Data:"+data);
            setTotalRecords(data.totalRecords);
            setJobsStatusesList(data);
            setFilteredRecords(data.filteredRecords);
            setIsFiltered(FiltersUtils.getIsFiltered(filterBy));
            setData(data);
            setLoadingStatus('success');
        } catch (error) {
            console.error('ValidationsStatus - getJobsStatuses.', error);
            setLoadingStatus('error');
            notificationContext.add({type: 'GET_VALIDATIONS_STATUSES_ERROR'}, true);
        } finally {
            setIsLoading(false);
            setIsRefreshing(false);
        }
    };

    const onConfirmDeleteDialog = async () => {
        setLoadingStatus('pending');
        setIsDeleteDialogVisible(false);

        try {
            getJobsStatuses();
        } catch (error) {
            console.error('ValidationsStatus - onConfirmDeleteDialog.', error);
            setLoadingStatus('failed');

            notificationContext.add({status: 'DELETE_VALIDATION_FROM_QUEUE_ERROR'}, true);
        } finally {
            setValidationStatus(null);
        }
    };

    const onHideDeleteDialog = () => {
        setIsDeleteDialogVisible(false);
        setValidationStatus(null);
    };

    const onHideEditDialog = () => {
        setIsEditDialogVisible(false);
        setValidationStatus(null);
    };

    const onSort = event => setSort({field: event.sortField, order: event.sortOrder});

    const filterOptions = [
        {
            nestedOptions: [
                {key: 'dataflowId', label: resourcesContext.messages['dataflowId'], keyfilter: 'pint'},
                {key: 'user', label: resourcesContext.messages['user']}
            ],
            type: 'INPUT'
        },
        {
            key: 'status',
            label: resourcesContext.messages['status'],
            multiSelectOptions: [
                {
                    type: resourcesContext.messages[config.datasetRunningStatus.IN_PROGRESS.label].toUpperCase(),
                    value: config.datasetRunningStatus.IN_PROGRESS.key
                },
                {
                    type: resourcesContext.messages[config.datasetRunningStatus.IN_QUEUE.label].toUpperCase(),
                    value: config.datasetRunningStatus.IN_QUEUE.key
                },
                {
                    type: resourcesContext.messages[config.datasetRunningStatus.FINISHED.label].toUpperCase(),
                    value: config.datasetRunningStatus.FINISHED.key
                },
                {
                    type: resourcesContext.messages[config.datasetRunningStatus.CANCELED.label].toUpperCase(),
                    value: config.datasetRunningStatus.CANCELED.key
                }
            ],
            template: 'ValidationsStatus',
            type: 'MULTI_SELECT'
        }
    ];

    

    const getTableColumns = () => {
        const columns = [
            {
                key: 'id',
                header: 'Job ID',
                template: getJobIdTemplate,
                className: styles.largeColumn
            },
            {
                key: 'jobType',
                header:'Job Type ',
                template: getJobTypeTemplate,
                className: styles.largeColumn
            },
            {
                key: 'jobStatus',
                header:'Job Status ',
                template: getJobStatusTemplate,
                className: styles.largeColumn
            },
            {
                key: 'dateAdded',
                header: 'Date Added',
                template: job => getDateAddedTemplate(job, 'dateAdded'),
                className: styles.largeColumn
            },
            {
                key: 'dateStatusChanged',
                header: 'Date Status Changed',
                template: job => getDateStatusChangedTemplate(job, 'dateStatusChanged'),
                className: styles.largeColumn
            },
            {
                key: 'creatorUsername',
                header:'Creator Username ',
                template: getJobCreatorUsernameTemplate,
                className: styles.largeColumn
            },
            {
                key: 'processid',
                header:'Process ID  ',
                template: getJobProcessIdTemplate,
                className: styles.largeColumn
            },
      
       
          
        ];
        if (isAdmin) {
            columns.push({
                key: 'buttonsUniqueId',
                header: resourcesContext.messages['actions'],
                template: getEditButton
            })
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

    const getEditButton = rowData => (
        <ActionsColumn
            isUpdating={isUpdating}
            onEditClick={() => {
                setIsEditDialogVisible(true);
                setValidationStatus(rowData);
            }}
            rowDataId={rowData.id}
        />
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
    
    const getJobStatusTemplate = job => (
        <p>
            {job.jobStatus} 
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

    const onUpdatePriority = async () => {
        try {
            setIsUpdating(true);
            await BackgroundProcessService.update({
                processId: validationStatus.id,
                priority: editedValidationStatusPriority
            });

            getJobsStatuses();
        } catch (error) {
            console.error('ValidationsStatus - onUpdatePriority.', error);
            notificationContext.add({type: 'SET_VALIDATIONS_STATUS_PRIORITY_ERROR'}, true);
        } finally {
            setIsUpdating(false);
            setIsEditDialogVisible(false);
        }
    };

    const editDialogFooter = (
        <div>
            <Button
                disabled={isUpdating || editedValidationStatusPriority < 1 || editedValidationStatusPriority > 100}
                icon={isUpdating ? 'spinnerAnimate' : 'check'}
                label={resourcesContext.messages['update']}
                onClick={onUpdatePriority}
            />
            <Button
                className={`p-button-secondary ${styles.buttonPushRight}`}
                icon="cancel"
                label={resourcesContext.messages['cancel']}
                onClick={onHideEditDialog}
            />
        </div>
    );

    const renderFilters = () => (
        <Filters
            className="lineItems"
            isLoading={loadingStatus === 'pending'}
            onFilter={() => setPagination({firstRow: 0, numberRows: pagination.numberRows, pageNum: 0})}
            onReset={() => setPagination({firstRow: 0, numberRows: pagination.numberRows, pageNum: 0})}
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
                    className={styles.validationStatusesTable}
                    first={firstRow}
                    hasDefaultCurrentPage={true}
                    lazy={true}
                    loading={loadingStatus === 'pending' && isNil(validationStatus)}
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

    const renderEditDialogContent = () => (
        <div>
            <label>{resourcesContext.messages['priority']}</label>
            <TooltipButton
                message={resourcesContext.messages['priorityMessage']}
                uniqueIdentifier={'process_priority'}></TooltipButton>
            <InputText
                className={
                    editedValidationStatusPriority < 1 || editedValidationStatusPriority > 100 ? styles.error : styles.priority
                }
                keyfilter="pint"
                onChange={e => setEditedValidationStatusPriority(e.target.value)}
                value={editedValidationStatusPriority}
            />
        </div>
    );

    return (
        <Fragment>
            <Dialog
                blockScroll={false}
                className="responsiveBigDialog"
                footer={dialogFooter}
                header='Jobs Status'
                modal={true}
                onHide={onCloseDialog}
                visible={isDialogVisible}>
                {renderDialogContent()}
            </Dialog>

            {isDeleteDialogVisible && (
                <ConfirmDialog
                    classNameConfirm="p-button-danger"
                    header={resourcesContext.messages['validationRemoveQueueDialogHeader']}
                    labelCancel={resourcesContext.messages['cancel']}
                    labelConfirm={resourcesContext.messages['yes']}
                    onConfirm={onConfirmDeleteDialog}
                    onHide={onHideDeleteDialog}
                    visible={isDeleteDialogVisible}>
                    {resourcesContext.messages['validationRemoveQueueDialogContent']}
                </ConfirmDialog>
            )}

            {isEditDialogVisible && (
                <Dialog
                    footer={editDialogFooter}
                    header={resourcesContext.messages['editValidationsStatus']}
                    modal={true}
                    onHide={onHideEditDialog}
                    visible={isEditDialogVisible}>
                    {renderEditDialogContent()}
                </Dialog>
            )}
        </Fragment>
    );
};
