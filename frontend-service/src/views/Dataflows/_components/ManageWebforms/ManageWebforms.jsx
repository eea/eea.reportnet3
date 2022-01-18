import { Fragment, useContext, useEffect, useState } from 'react';

import styles from './ManageWebforms.module.scss';

import { Column } from 'primereact/column';

import { Button } from 'views/_components/Button';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { Spinner } from 'views/_components/Spinner';

import { WebformService } from 'services/WebformService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const ManageWebforms = ({ onCloseDialog, isDialogVisible }) => {
  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [isPending, setIsPending] = useState(false);
  const [selectedWebformId, setSelectedWebformId] = useState(null);
  const [webforms, setWebforms] = useState([]);

  useEffect(() => {
    getWebformList();
  }, []);

  useEffect(() => {
    setIsPending(loadingStatus === 'pending');
  }, [loadingStatus]);

  // TODO
  // delete: /webform/webformConfig/{id} // Check that endpoint is correct
  // download: /webform/webformConfig/{id} // Check that endpoint is correct

  // Confirm dialog: Delete webform
  // ADD / EDIT dialog

  const getWebformList = async () => {
    setLoadingStatus('pending');

    try {
      const data = await WebformService.getAll();
      setWebforms(data);
      setLoadingStatus('success');
    } catch (error) {
      console.error('ManageWebforms - getWebformList.', error);
      setLoadingStatus('failed');
      notificationContext.add({ type: 'LOADING_WEBFORM_OPTIONS_ERROR' }, true); // Todo add correct notification
    } finally {
      setLoadingStatus('idle');
    }
  };

  const getTableColumns = () => {
    const columns = [
      { key: 'label', header: resourcesContext.messages['name'] },
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
        sortable={column.key !== 'actions'}
      />
    ));
  };

  const getBtnIcon = (id, iconName) => {
    if (id === selectedWebformId && isPending) {
      return 'spinnerAnimate';
    }
    return iconName;
  };

  const onAddClick = () => {
    //todo add Create dialog
  };
  const onShowDeleteDialog = id => {
    setSelectedWebformId(id);
    //todo add confirmation dialog
  };

  const onEdit = id => {
    setSelectedWebformId(id);
    //todo add edit dialog
  };

  const onClickDownload = id => {
    setSelectedWebformId(id);
    //todo add download dialog
  };

  const getActionsTemplate = row => {
    return (
      <Fragment>
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.actionButton}`}
          disabled={isPending}
          icon={getBtnIcon(row.id, 'edit')}
          onClick={() => onEdit(row.id)}
          type="button"
        />
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.actionButton}`}
          disabled={isPending}
          icon={getBtnIcon(row.id, 'export')}
          onClick={() => onClickDownload(row.id)}
          type="button"
        />
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.deleteRowButton}`}
          disabled={isPending}
          icon={getBtnIcon(row.id, 'trash')}
          onClick={() => onShowDeleteDialog(row.id)}
          type="button"
        />
      </Fragment>
    );
  };

  const footer = (
    <Fragment>
      <Button
        className="p-button-primary"
        disabled={isPending}
        icon={isPending ? 'spinnerAnimate' : 'plus'}
        label={resourcesContext.messages['add']}
        onClick={onAddClick}
      />
      <Button
        className="p-button-secondary p-button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={onCloseDialog}
      />
    </Fragment>
  );

  const renderDialogContent = () => {
    if (isPending) {
      return (
        <div className={styles.loadingSpinner}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      );
    }

    if (loadingStatus === 'failed') {
      return (
        <div className={styles.noDataContent}>
          <p>{resourcesContext.messages['loadWebformsError']}</p>
          <Button label={resourcesContext.messages['refresh']} onClick={getWebformList} />
        </div>
      );
    }

    return (
      <DataTable
        autoLayout
        className={styles.dialogContent}
        hasDefaultCurrentPage
        paginator
        rows={10}
        rowsPerPageOptions={[5, 10, 15]}
        totalRecords={webforms.length}
        value={webforms}>
        {getTableColumns()}
      </DataTable>
    );
  };
  return (
    <Dialog
      blockScroll={false}
      className="responsiveDialog"
      footer={footer}
      header={resourcesContext.messages['manageWebformsDialogHeader']}
      modal
      onHide={onCloseDialog}
      visible={isDialogVisible}>
      {renderDialogContent()}
    </Dialog>
  );
};
