import { Fragment, useContext, useEffect, useLayoutEffect, useState } from 'react';
import isNil from 'lodash/isNil';

import classNames from 'classnames';

import styles from './ManageWebforms.module.scss';

import { Column } from 'primereact/column';

import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { CustomFileUpload } from 'views/_components/CustomFileUpload';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { DownloadFile } from 'views/_components/DownloadFile';
import { Spinner } from 'views/_components/Spinner';
import { InputText } from 'views/_components/InputText';

import { WebformService } from 'services/WebformService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const ManageWebforms = ({ onCloseDialog, isDialogVisible }) => {
  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [isLoading, setIsLoading] = useState(true);
  const [isPending, setIsPending] = useState(false);
  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [selectedWebformId, setSelectedWebformId] = useState(null);
  const [webforms, setWebforms] = useState([]);
  const [webform, setWebform] = useState({ name: '', json: null });
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isAddOrEditDialogVisible, setIsAddOrEditDialogVisible] = useState(false);

  useEffect(() => {
    getWebformList();
  }, []);

  useLayoutEffect(() => {
    setIsPending(loadingStatus === 'pending');
  }, [loadingStatus]);

  // TODO
  // delete: /webform/webformConfig/{id} // Check that endpoint is correct

  // ADD / EDIT dialog

  const getWebformList = async () => {
    try {
      const data = await WebformService.getAll();
      setWebforms(data);
    } catch (error) {
      console.error('ManageWebforms - getWebformList.', error);
      notificationContext.add({ type: 'LOADING_WEBFORM_OPTIONS_ERROR' }, true); // Todo add correct notification
    } finally {
      setIsLoading(false);
    }
  };

  const onDownload = async (id, name) => {
    setLoadingStatus('pending');

    try {
      const { data } = await WebformService.download(id);

      if (!isNil(data)) {
        DownloadFile(data, `${name}.json`);
      }

      setLoadingStatus('success');
    } catch (error) {
      console.error('ManageWebforms - onDownload.', error);
      setLoadingStatus('failed');
      notificationContext.add({ type: 'GET_WEBFORM_LIST_ERROR' }, true); // Todo add correct notification
    } finally {
      setLoadingStatus('idle');
      setSelectedWebformId(null);
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

  const addOrEditDialogFooter = (
    <div>
      <Button
        className="p-button-primary p-button-animated-blink "
        // disabled={}
        icon={false ? 'spinnerAnimate' : 'check'}
        label={resourcesContext.messages['create']}
        onClick={() => onAddClick()}
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon="cancel"
        label={resourcesContext.messages['cancel']}
        onClick={() => setIsAddOrEditDialogVisible(false)}
      />
    </div>
  );

  const onAddClick = () => {
    setIsAddOrEditDialogVisible(true);
  };

  const onShowDeleteDialog = id => {
    setSelectedWebformId(id);
    setIsDeleteDialogVisible(true);
  };

  const onHideDeleteDialog = () => {
    setIsDeleteDialogVisible(false);
    setSelectedWebformId(null);
  };

  const onConfirmDeleteDialog = async () => {
    setLoadingStatus('pending');
    setIsDeleteDialogVisible(false);
    try {
      await WebformService.delete(selectedWebformId);
      setLoadingStatus('success');
    } catch (error) {
      console.error('ManageWebforms - onConfirmDeleteDialog.', error);
      setLoadingStatus('failed');
      notificationContext.add({ type: 'DELETE_WEBFORM_ERROR' }, true); // Todo add correct notification
    } finally {
      setLoadingStatus('idle');
      setSelectedWebformId(null);
    }
  };

  const onEdit = id => {
    setSelectedWebformId(id);
    //todo add edit dialog
  };

  const onClickDownload = (id, name) => {
    setSelectedWebformId(id);
    onDownload(id, name);
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
          onClick={() => onClickDownload(row.id, row.value)}
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
    <div className={styles.footer}>
      <Button
        className="p-button-primary"
        icon={'plus'}
        label={resourcesContext.messages['add']}
        onClick={onAddClick}
      />
      <Button
        className={`p-button-secondary ${styles.buttonPushRight}`}
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={onCloseDialog}
      />
    </div>
  );

  // const hasFiles = () => webform.files && state.files.length > 0;

  // const renderChooseButton = () => {

  //   const buttonClassName = classNames('p-button p-fileupload-choose p-component p-button-text-icon-left', {
  //     'p-fileupload-choose-selected': hasFiles()
  //   });

  //   const iconClassName = classNames('p-button-icon-left pi', {
  //     'pi-plus': !hasFiles() || auto,
  //     'pi-upload': hasFiles() && !auto
  //   });

  //   return (
  //     <span className={buttonClassName} onMouseUp={()=>{}}>
  //       <span className={iconClassName} />
  //       <span className="p-button-text p-clickable">
  //         {auto ? chooseLabel : hasFiles() ? state.files[0].name : chooseLabel}
  //       </span>
  //       <input
  //         // accept={accept}
  //         // disabled={disabled}
  //         // multiple={multiple}
  //         // onBlur={onBlur}
  //         // onChange={onFileSelect}
  //         // onFocus={onFocus}
  //         // ref={fileInput}
  //         type="file"
  //       />
  //     </span>
  //   );
  // };

  const renderDialogContent = () => {
    if (isLoading) {
      return (
        <div className={styles.noDataContent}>
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
      <div className={styles.dialogContent}>
        <DataTable
          autoLayout
          hasDefaultCurrentPage
          paginator
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={webforms.length}
          value={webforms}>
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
        footer={footer}
        header={resourcesContext.messages['manageWebforms']}
        modal
        onHide={onCloseDialog}
        visible={isDialogVisible}>
        {renderDialogContent()}
      </Dialog>

      {isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resourcesContext.messages['deleteWebform']}
          labelCancel={resourcesContext.messages['cancel']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onConfirmDeleteDialog}
          onHide={onHideDeleteDialog}
          visible={isDeleteDialogVisible}>
          {resourcesContext.messages['confirmDeleteWebform']}
        </ConfirmDialog>
      )}

      {isAddOrEditDialogVisible && (
        <Dialog
          blockScroll={false}
          className="responsiveDialog"
          footer={addOrEditDialogFooter}
          header={resourcesContext.messages['addWebformDialogHeader']}
          modal
          onHide={() => {
            setIsAddOrEditDialogVisible(false);
          }}
          visible={isAddOrEditDialogVisible}>
          <label htmlFor="name">
            {resourcesContext.messages['name']}
            <InputText className={styles.inputText} id="name" readOnly value={webform.name} />
          </label>

          <div>
            {/* <CustomFileUpload
              accept=".json"
              chooseLabel={resourcesContext.messages['selectFile']}
              isDialog={false}
              mode="basic"
              name="file"
              // onUpload={onUpload}
            /> */}
            {/* {renderChooseButton()} */}
          </div>
        </Dialog>
      )}
    </Fragment>
  );
};
