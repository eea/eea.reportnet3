import { Fragment, useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ValidationsStatus.module.scss';

import { Column } from 'primereact/column';

import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { Spinner } from 'views/_components/Spinner';

import { ValidationService } from 'services/ValidationService'; // TODO IMPORT CORRECT SERVICE

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { useDateTimeFormatByUserPreferences } from 'views/_functions/Hooks/useDateTimeFormatByUserPreferences';
import { useFilters } from 'views/_functions/Hooks/useFilters'; // TODO CHECK HISTORIC RELEASES

export const ValidationsStatus = ({ onCloseDialog, isDialogVisible }) => {
  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [validationStatusId, setValidationStatusId] = useState(null);
  const [validationsStatuses, setValidationsStatusesList] = useState([]);

  const { getDateTimeFormatByUserPreferences } = useDateTimeFormatByUserPreferences();

  // id - not showing, dataflow name + id, dataset  name + id, user, status, queued date, process starting date and process finishing date.
  // dataflow/dataset estructura: dataflowId, dataflowName, datasetId, datasetName

  // filtrar por dataflow id y por usuario,
  // paginado

  useEffect(() => {
    getValidationsStatuses();
  }, []);

  // const getValidationsStatuses = async () => {
  const getValidationsStatuses = () => {
    setLoadingStatus('pending');

    try {
      // const data = await ValidationService.getAllStatuses(); // TODO SERVICE
      const data = [
        {
          id: 10,
          dataflowId: 111,
          dataflowName: 'Dataflow name',
          datasetId: 1,
          datasetName: 'Dataset name',
          status: 'importing',
          queuedDate: 1644572710000,
          processStartingDate: 1644572711000,
          processFinishingDate: 1644572712000,
          user: 'igor.provider@reportnet.net'
        },
        {
          id: 20,
          dataflowId: 222,
          dataflowName: 'Dataflow name',
          datasetId: 2,
          datasetName: 'Dataset name',
          status: 'imported',
          queuedDate: 1644572720000,
          processStartingDate: 1644572721000,
          processFinishingDate: 1644572722000,
          user: 'pablo.provider@reportnet.net'
        },
        {
          id: 30,
          dataflowId: 333,
          dataflowName: 'Dataflow name',
          datasetId: 3,
          datasetName: 'Dataset name',
          status: 'validating',
          queuedDate: 1644572730000,
          processStartingDate: 1644572731000,
          processFinishingDate: 1644572732000,
          user: 'miguel.provider@reportnet.net'
        },
        {
          id: 40,
          dataflowId: 444,
          dataflowName: 'Dataflow name',
          datasetId: 4,
          datasetName: 'Dataset name',
          status: 'validated',
          queuedDate: 1644572740000,
          processStartingDate: 1644572741000,
          processFinishingDate: 1644572742000,
          user: 'miriam.provider@reportnet.net'
        },
        {
          id: 50,
          dataflowId: 555,
          dataflowName: 'Dataflow name',
          datasetId: 5,
          datasetName: 'Dataset name',
          status: 'in queue',
          queuedDate: 1644572750000,
          processStartingDate: 1644572751000,
          processFinishingDate: 1644572752000,
          user: 'mikel.provider@reportnet.net'
        }
      ];
      setValidationsStatusesList(data);
      setLoadingStatus('success');
    } catch (error) {
      console.error('ValidationsStatus - getValidationsStatuses.', error);
      setLoadingStatus('error');
      notificationContext.add({ status: 'LOADING_WEBFORM_OPTIONS_ERROR' }, true);
    } finally {
      setIsLoading(false);
    }
  };

  const onConfirmDeleteDialog = async () => {
    setLoadingStatus('pending');
    setIsDeleteDialogVisible(false);

    try {
      // await ValidationService.removeFromQueue(validationStatusId); // TODO SERVICE
      getValidationsStatuses();
    } catch (error) {
      console.error('ValidationsStatus - onConfirmDeleteDialog.', error);
      setLoadingStatus('failed');

      // notificationContext.add({ status: 'DELETE_VALIDATION_FROM_QUEUE_ERROR' }, true); //TODO NOTIFICATIONS
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

  const getTableColumns = () => {
    const columns = [
      { key: 'dataflow', header: resourcesContext.messages['dataflow'], template: getDataflowTemplate },
      { key: 'dataset', header: resourcesContext.messages['dataset'], template: getDatasetTemplate },
      { key: 'user', header: resourcesContext.messages['user'] },
      { key: 'status', header: resourcesContext.messages['status'] },
      {
        key: 'queuedDate',
        header: resourcesContext.messages['queuedDate'], // TODO ADD MESSAGE
        template: validation => getDateTemplate(validation, 'queuedDate')
      },
      {
        key: 'processStartingDate',
        header: resourcesContext.messages['processStartingDate'], // TODO ADD MESSAGE
        template: validation => getDateTemplate(validation, 'processStartingDate')
      },
      {
        key: 'processFinishingDate',
        header: resourcesContext.messages['processFinishingDate'], // TODO ADD MESSAGE
        template: validation => getDateTemplate(validation, 'processFinishingDate')
      },
      {
        key: 'actions',
        header: resourcesContext.messages['actions'],
        template: getActionsTemplate,
        className: styles.actionsColumn
      }
    ];

    return columns.map(column => (
      <Column
        body={column.template}
        className={column.className ? column.className : ''}
        columnResizeMode="expand"
        field={column.key}
        header={column.header}
        key={column.key}
      />
    ));
  };

  const getBtnIcon = (id, iconName) => {
    if (id === validationStatusId && loadingStatus === 'pending') {
      return 'spinnerAnimate';
    }

    return iconName;
  };

  const getActionsTemplate = validation => {
    return (
      <Button
        className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.deleteRowButton}`}
        disabled={loadingStatus === 'pending'}
        icon={getBtnIcon(validation.id, 'trash')}
        onClick={() => onShowDeleteDialog(validation)}
        status="button"
      />
    );
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
        <DataTable
          autoLayout
          hasDefaultCurrentPage
          // loading={loadingStatus === 'pending' && isNil(validationStatusId)} // TODO CONTROL LOADING STATUS
          paginator
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
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
        // className="responsiveDialog"
        footer={dialogFooter}
        // header={resourcesContext.messages['manageWebformsConfiguration']}
        header="Validations Statuses" //TODO ADD MESSAGE
        modal
        onHide={onCloseDialog}
        visible={isDialogVisible}>
        {renderDialogContent()}
      </Dialog>

      {isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          // header={resourcesContext.messages['deleteValidationStatus']}
          header="Remove from queue" //TODO ADD MESSAGE
          labelCancel={resourcesContext.messages['cancel']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onConfirmDeleteDialog}
          onHide={onHideDeleteDialog}
          visible={isDeleteDialogVisible}>
          {/* {resourcesContext.messages['confirmDeleteValidationStatus']} // TODO ADD MESSAGE*/}
          Remove this validation fom queue?
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
