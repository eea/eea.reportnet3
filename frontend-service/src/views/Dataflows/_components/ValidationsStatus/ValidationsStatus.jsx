import { Fragment, useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import styles from './ValidationsStatus.module.scss';

import { Column } from 'primereact/column';

import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { Filters } from 'views/_components/Filters';
import { PaginatorRecordsCount } from 'views/_components/DataTable/_functions/Utils/PaginatorRecordsCount';
import { Spinner } from 'views/_components/Spinner';

import { BackgroundProcessService } from 'services/BackgroundProcessService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { useApplyFilters } from 'views/_functions/Hooks/useApplyFilters';
import { useDateTimeFormatByUserPreferences } from 'views/_functions/Hooks/useDateTimeFormatByUserPreferences';

export const ValidationsStatus = ({ onCloseDialog, isDialogVisible }) => {
  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [validationStatusId, setValidationStatusId] = useState(null);
  const [validationsStatuses, setValidationsStatusesList] = useState([]);
  const [sort, setSort] = useState({ field: '', order: 0 });
  const [pagination, setPagination] = useState({ firstRow: 0, numberRows: 10, pageNum: 0 }); // TODO can be in same object with goToPage and totalRecords?
  const [totalRecords, setTotalRecords] = useState(0);
  const [goToPage, setGoToPage] = useState(1);
  const [filteredRecords, setFilteredRecords] = useState(0);
  const [isFiltered, setIsFiltered] = useState(false);

  const { getDateTimeFormatByUserPreferences } = useDateTimeFormatByUserPreferences();

  const { getFilterBy, setData } = useApplyFilters('validationsStatuses'); // TODO find how to use properly

  // TODO Ordering
  // TODO Pagination

  const { firstRow, numberRows, pageNum } = pagination;

  useEffect(() => {
    // if (true) { // TODO check double call on reset filter
    getValidationsStatuses();
    // }
  }, [pagination, sort]);

  const getValidationsStatuses = async () => {
    setLoadingStatus('pending');

    const filterBy = await getFilterBy();

    try {
      const { data } = await BackgroundProcessService.getValidationsStatuses({
        pageNum,
        numberRows,
        sortOrder: sort.order,
        sortField: sort.field,
        user: filterBy.user,
        dataflowId: filterBy.dataflowId,
        status: filterBy.status?.join()
      });

      setTotalRecords(data.totalRecords);
      setValidationsStatusesList(data.processList);
      setFilteredRecords(data.filteredRecords);
      setIsFiltered(Object.keys(filterBy).length !== 0 && data.filteredRecords !== data.totalRecords);
      setData(data.processList);
      setLoadingStatus('success');
    } catch (error) {
      console.error('ValidationsStatus - getValidationsStatuses.', error);
      setLoadingStatus('error');
      notificationContext.add({ status: 'GET_VALIDATIONS_STATUSES_ERROR' }, true);
    } finally {
      setIsLoading(false);
    }
  };

  const onConfirmDeleteDialog = async () => {
    setLoadingStatus('pending');
    setIsDeleteDialogVisible(false);

    try {
      // await DataflowService.removeFromQueue(validationStatusId); // TODO CORRECT SERVICE CALL
      getValidationsStatuses();
    } catch (error) {
      console.error('ValidationsStatus - onConfirmDeleteDialog.', error);
      setLoadingStatus('failed');

      //notificationContext.add({ status: 'DELETE_VALIDATION_FROM_QUEUE_ERROR' }, true); //TODO ADD NOTIFICATIONS
    } finally {
      setValidationStatusId(null);
    }
  };

  const onShowDeleteDialog = validation => {
    setValidationStatusId(validation);
    setIsDeleteDialogVisible(true);
  };

  const onHideDeleteDialog = () => {
    setIsDeleteDialogVisible(false);
    setValidationStatusId(null);
  };

  const onSort = event => {
    let sortField;

    if (event.sortField === 'dataflow') {
      sortField = 'dataflowName';
    } else if (sortField === 'dataset') {
      sortField = 'datasetName';
    } else {
      sortField = event.sortField;
    }

    setSort({ field: sortField, order: event.sortOrder });
  };

  const onChangePagination = event => {
    setPagination({ firstRow: event.first, numberRows: event.rows, pageNum: event.page });
  };

  const onChangePage = event => {
    setGoToPage(event.page + 1);
    onChangePagination(event);
  };

  const filterOptions = [
    {
      nestedOptions: [
        { key: 'dataflowId', label: resourcesContext.messages['dataflowId'] },
        { key: 'user', label: resourcesContext.messages['user'] }
      ],
      type: 'INPUT'
    },
    {
      key: 'status',
      label: resourcesContext.messages['status'],
      multiSelectOptions: [
        {
          type: resourcesContext.messages[config.datasetRunningStatus.IMPORTING.label].toUpperCase(),
          value: config.datasetRunningStatus.IMPORTING.key
        },
        {
          type: resourcesContext.messages[config.datasetRunningStatus.IMPORTED.label].toUpperCase(),
          value: config.datasetRunningStatus.IMPORTED.key
        },
        {
          type: resourcesContext.messages[config.datasetRunningStatus.VALIDATING.label].toUpperCase(),
          value: config.datasetRunningStatus.VALIDATING.key
        },
        {
          type: resourcesContext.messages[config.datasetRunningStatus.VALIDATED.label].toUpperCase(),
          value: config.datasetRunningStatus.VALIDATED.key
        },
        {
          type: resourcesContext.messages[config.datasetRunningStatus.IN_QUEUE.label].toUpperCase(),
          value: config.datasetRunningStatus.IN_QUEUE.key
        },
        {
          type: resourcesContext.messages[config.datasetRunningStatus.FINISHED.label].toUpperCase(),
          value: config.datasetRunningStatus.FINISHED.key
        }
      ],
      type: 'MULTI_SELECT'
    }
  ];

  const getStatusTemplate = rowData => {
    console.log('rowData.status', rowData.status);
    return <div>{resourcesContext.messages[config.datasetRunningStatus[rowData.status].label].toUpperCase()}</div>;
  };

  const getTableColumns = () => {
    const columns = [
      { key: 'dataflow', header: resourcesContext.messages['dataflow'], template: getDataflowTemplate },
      { key: 'dataset', header: resourcesContext.messages['dataset'], template: getDatasetTemplate },
      { key: 'user', header: resourcesContext.messages['user'] },
      { key: 'status', header: resourcesContext.messages['status'], template: getStatusTemplate },
      {
        key: 'queuedDate',
        header: resourcesContext.messages['queuedDate'],
        template: validation => getDateTemplate(validation, 'queuedDate')
      },
      {
        key: 'processStartingDate',
        header: resourcesContext.messages['processStartingDate'],
        template: validation => getDateTemplate(validation, 'processStartingDate')
      },
      {
        key: 'processFinishingDate',
        header: resourcesContext.messages['processFinishingDate'],
        template: validation => getDateTemplate(validation, 'processFinishingDate')
      }
      // {
      //   key: 'actions',
      //   header: resourcesContext.messages['actions'],
      //   template: getActionsTemplate,
      //   className: styles.actionsColumn
      // }
    ];

    return columns.map(column => (
      <Column
        body={column.template}
        className={column.className ? column.className : ''}
        columnResizeMode="expand"
        field={column.key}
        header={column.header}
        key={column.key}
        sortable
      />
    ));
  };

  const getActionsTemplate = validation => {
    return (
      <Button
        className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.deleteRowButton}`}
        disabled={loadingStatus === 'pending'}
        icon={getBtnIcon(validation.id)}
        onClick={() => onShowDeleteDialog(validation)}
        status="button"
      />
    );
  };

  const getBtnIcon = id => {
    if (id === validationStatusId && loadingStatus === 'pending') {
      return 'spinnerAnimate';
    }

    return 'trash';
  };

  const getDataflowTemplate = validation => (
    <p>
      {validation.dataflowName} - {validation.dataflowId}
    </p>
  );

  const getDatasetTemplate = validation => (
    <p>
      {validation.datasetName} - {validation.datasetId}
    </p>
  );

  const getDateTemplate = (validation, field) => getDateTimeFormatByUserPreferences(validation[field]);

  const dialogFooter = (
    <div className={styles.footer}>
      <Button
        className={`p-button-secondary ${styles.buttonPushRight}`}
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={onCloseDialog}
      />
    </div>
  );

  const onFilter = () => {
    if (isFiltered) {
      onChangePagination({
        firstRow: 0,
        numberRows: pagination.numberRows,
        pageNum: 0
      });
    } else {
      getValidationsStatuses();
    }
  };

  const onReset = () => {
    if (pagination.pageNum !== 0) {
      onChangePagination({
        firstRow: 0,
        numberRows: pagination.numberRows,
        pageNum: 0
      });
    }
  };

  // const renderPaginatorRecordsCount = () => (
  //   <Fragment>
  //     {isFiltered ? `${resourcesContext.messages['filtered']}: ${filteredRecords} | ` : ''}
  //     {`${resourcesContext.messages['totalRecords']} ${totalRecords} ${' '} ${resourcesContext.messages[
  //       'records'
  //     ].toLowerCase()}`}
  //   </Fragment>
  // );

  const renderDialogContent = () => {
    if (isLoading) {
      return (
        <div className={styles.noDataContent}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      );
    }

    if (isEmpty(validationsStatuses)) {
      return (
        <div className={styles.noDataContent}>
          <span>{resourcesContext.messages['noData']}</span>
        </div>
      );
    }

    return (
      <div className={styles.dialogContent}>
        <Filters
          className="lineItems"
          isLoading={isLoading}
          onFilter={onFilter}
          onReset={onReset}
          options={filterOptions}
          recoilId="validationsStatuses"
        />
        <DataTable
          autoLayout
          first={firstRow}
          hasDefaultCurrentPage
          loading={loadingStatus === 'pending' && isNil(validationStatusId)} // TODO CONTROL LOADING STATUS
          onPage={onChangePage}
          onSort={onSort}
          paginator
          paginatorRight={
            <PaginatorRecordsCount
              dataLength={totalRecords}
              filteredDataLength={filteredRecords}
              isFiltered={isFiltered}
            />
          }
          reorderableColumns
          resizableColumns
          rows={numberRows}
          rowsPerPageOptions={[5, 10, 15]}
          sortField={sort.field}
          sortOrder={sort.order}
          totalRecords={validationsStatuses.length}
          value={validationsStatuses}>
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
        header={resourcesContext.messages['validationsStatusesDialogHeader']}
        modal
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
    </Fragment>
  );
};
