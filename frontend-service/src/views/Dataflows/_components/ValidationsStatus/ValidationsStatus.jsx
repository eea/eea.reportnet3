import { Fragment, useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ValidationsStatus.module.scss';

import { Column } from 'primereact/column';

import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { Spinner } from 'views/_components/Spinner';

// import { WebformService } from 'services/WebformService';// TODO SERVICE

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const ValidationsStatus = ({ onCloseDialog, isDialogVisible }) => {
  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [validationStatus, setValidationStatus] = useState({ id: null, name: '', status: '' });
  const [validationsStatuses, setValidationsStatusesList] = useState([]);

  //   BE:
  // -Almacenar los procesos que se realizan (por ahora validacion, importacion, restauracion, release) junto a su usuario
  // -Consultar los procesos correspondientes a un dataset y/o dataflow por usuario
  // -Consultar todos los procesos paginados por un admin

  // - Añadir a los datasets el estado de la importación y validación. Algo en plan datasetRunningStatus:
  //"importing/imported/validating/validated" o alguna estructura más dinámica

  useEffect(() => {
    getValidationsStatuses();
  }, []);

  const getValidationsStatuses = async () => {
    setLoadingStatus('pending');

    try {
      // const data = await WebformService.getAll(); // TODO SERVICE
      const data = [];
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
      // await WebformService.delete(validationStatus.id); // TODO SERVICE
      getValidationsStatuses();
    } catch (error) {
      console.error('ValidationsStatus - onConfirmDeleteDialog.', error);
      setLoadingStatus('failed');

      // if (error.response.status === 400) {
      //   // notificationContext.add({ status: 'DELETE_WEBFORM_IN_USE_ERROR' }, true);
      // } else {
      //   // notificationContext.add({ status: 'DELETE_WEBFORM_CONFIGURATION_ERROR' }, true);
      // }

      //TODO NOTIFICATIONS
    } finally {
      resetValidationStatus();
    }
  };

  const resetValidationStatus = () => setValidationStatus({ id: null, name: '', status: '' });

  const onShowDeleteDialog = validation => {
    setValidationStatus(validation);
    setIsDeleteDialogVisible(true);
  };

  const onHideDeleteDialog = () => {
    setIsDeleteDialogVisible(false);
    resetValidationStatus();
  };

  const getTableColumns = () => {
    const columns = [
      { key: 'name', header: resourcesContext.messages['name'] },
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
    if (id === validationStatus.id && loadingStatus === 'pending') {
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
          // loading={loadingStatus === 'pending' && isNil(validationStatus.id)}
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
        className="responsiveDialog"
        footer={dialogFooter}
        // header={resourcesContext.messages['manageWebformsConfiguration']}
        header="Validations Status"
        modal
        onHide={onCloseDialog}
        visible={isDialogVisible}>
        {renderDialogContent()}
      </Dialog>

      {isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          // header={resourcesContext.messages['deleteValidationStatus']}
          header="Stop the shit"
          labelCancel={resourcesContext.messages['cancel']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onConfirmDeleteDialog}
          onHide={onHideDeleteDialog}
          visible={isDeleteDialogVisible}>
          {/* {resourcesContext.messages['confirmDeleteValidationStatus']} */}
          Remove validation fom queue this validation
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
