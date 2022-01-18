import { Fragment, useState, useEffect, useContext } from 'react';

import styles from './ManageWebforms.module.scss';

import { Column } from 'primereact/column';

import { Button } from 'views/_components/Button';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';

import { WebformService } from 'services/WebformService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const ManageWebforms = ({ onCloseDialog, isDialogVisible }) => {
  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [isLoadingStatus, setLoadingStatus] = useState('idle');
  const [isPending, setIsPending] = useState(false);
  const [selectedWebformId, setSelectedWebformId] = useState(null);
  const [webforms, setWebforms] = useState([]);

  useEffect(() => {
    getWebformList();
  }, []);

  useEffect(() => {
    setIsPending(isPending);
  }, [isLoadingStatus]);

  console.log(`webforms`, webforms);

  // Tabla de Webforms
  // => objeto de las columnas  //
  //                webformName
  //                actions
  //                        delete => botón de borrar y que devuelva un error si el webform está en uso en algún dataset
  //                        download => JSON
  //                        edit

  // Endpoints
  // delete: /webform/webformConfig/{id} // Check that endpoint is correct
  // download: /webform/webformConfig/{id} // Check that endpoint is correct

  // Confirm dialog: Delete webform
  //

  const getTableColumns = () => {
    const columns = [
      { key: 'label', header: resourcesContext.messages['name'] },
      {
        key: 'actions',
        header: resourcesContext.messages['actions'],
        template: getActionsTemplate
      }
    ];

    return columns.map(column => (
      <Column
        body={column.template}
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
          tooltip={resourcesContext.messages['edit']}
          tooltipOptions={{ position: 'top' }}
          type="button"
        />
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.actionButton}`}
          disabled={isPending}
          icon={getBtnIcon(row.id, 'export')}
          onClick={() => onClickDownload(row.id)}
          tooltip={resourcesContext.messages['download']}
          tooltipOptions={{ position: 'top' }}
          type="button"
        />
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.deleteRowButton}`}
          disabled={isPending}
          icon={getBtnIcon(row.id, 'trash')}
          onClick={() => onShowDeleteDialog(row.id)}
          tooltip={resourcesContext.messages['delete']}
          tooltipOptions={{ position: 'top' }}
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
        // onClick={}
      />
      <Button
        className="p-button-secondary p-button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={onCloseDialog}
      />
    </Fragment>
  );

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

  return (
    <Dialog
      blockScroll={false}
      className="responsiveDialog"
      footer={footer}
      header={resourcesContext.messages['manageWebformsDialogHeader']}
      modal
      onHide={onCloseDialog}
      visible={isDialogVisible}>
      <DataTable
        autoLayout
        //   className={styles.dialogContent}
        hasDefaultCurrentPage
        paginator
        rows={10}
        rowsPerPageOptions={[5, 10, 15]}
        totalRecords={webforms.length}
        value={webforms}>
        {getTableColumns()}
      </DataTable>
    </Dialog>
  );
};
