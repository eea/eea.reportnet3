import { Fragment, useContext, useEffect, useRef, useState } from 'react';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import orderBy from 'lodash/orderBy';

import styles from './ManageWebforms.module.scss';

import { Column } from 'primereact/column';

import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { InputFile } from './_components/InputFile';
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

  const [isAddEditDialogVisible, setIsAddEditDialogVisible] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [jsonContent, setJsonContent] = useState(null);
  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [selectedWebformId, setSelectedWebformId] = useState(null);
  const [webformName, setWebformName] = useState('');
  const [webforms, setWebforms] = useState([]);

  const fileRef = useRef(null);

  useEffect(() => {
    getWebformList();
  }, []);

  useEffect(() => {
    setWebformName(() => getInitialName());
  }, [selectedWebformId]);

  const getInitialName = () => {
    if (!selectedWebformId) {
      return '';
    }

    return webforms.find(webform => webform.id === selectedWebformId).label;
  };

  const getWebformList = async () => {
    setLoadingStatus('pending');

    try {
      const data = await WebformService.getAll();
      setWebforms(orderBy(data, 'id', 'asc'));
    } catch (error) {
      console.error('ManageWebforms - getWebformList.', error);
      notificationContext.add({ type: 'LOADING_WEBFORM_OPTIONS_ERROR' }, true);
    } finally {
      setLoadingStatus('idle');
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
      setLoadingStatus('error');
      notificationContext.add({ type: 'DOWNLOAD_WEBFORM_CONFIGURATION_ERROR' }, true);
    } finally {
      setSelectedWebformId(null);
    }
  };

  const onConfirm = async () => {
    setLoadingStatus('pending');

    try {
      isNil(selectedWebformId)
        ? await WebformService.create(webformName, jsonContent)
        : await WebformService.update(webformName, jsonContent, selectedWebformId);

      setLoadingStatus('success');
      getWebformList();
      setIsAddEditDialogVisible(false);
      setJsonContent(null);
      setSelectedWebformId(null);
      setWebformName('');
    } catch (error) {
      console.error('ManageWebforms - onConfirm.', error);
      setLoadingStatus('error');
      isNil(selectedWebformId)
        ? notificationContext.add({ type: 'CREATE_WEBFORM_CONFIGURATION_ERROR' }, true)
        : notificationContext.add({ type: 'EDIT_WEBFORM_CONFIGURATION_ERROR' }, true);
    }
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
      getWebformList();
    } catch (error) {
      console.error('ManageWebforms - onConfirmDeleteDialog.', error);
      setLoadingStatus('failed');
      notificationContext.add({ type: 'DELETE_WEBFORM_CONFIGURATION_ERROR' }, true);
    } finally {
      setLoadingStatus('idle');
      setSelectedWebformId(null);
    }
  };

  const onEditClick = id => {
    setSelectedWebformId(id);
    setIsAddEditDialogVisible(true);
  };

  const onAddEditDialogClose = () => {
    setIsAddEditDialogVisible(false);
    setSelectedWebformId(null);
    setJsonContent(null);
    setWebformName('');
  };

  const onClickDownload = (id, name) => {
    setSelectedWebformId(id);
    onDownload(id, name);
  };

  const onFileUpload = async e => {
    if (!isNil(e.target.files[0])) {
      const reader = new FileReader();
      reader.onload = async e => {
        const text = e.target.result;
        setJsonContent(text);
      };
      reader.readAsText(e.target.files[0]);
    }
  };

  const onClearFile = () => {
    setJsonContent(null);
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
    if (id === selectedWebformId && loadingStatus === 'pending') {
      return 'spinnerAnimate';
    }

    return iconName;
  };

  const getActionsTemplate = row => {
    return (
      <Fragment>
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.actionButton}`}
          disabled={loadingStatus === 'pending'}
          icon={getBtnIcon(row.id, 'edit')}
          onClick={() => onEditClick(row.id)}
          type="button"
        />
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.actionButton}`}
          disabled={loadingStatus === 'pending'}
          icon={getBtnIcon(row.id, 'export')}
          onClick={() => onClickDownload(row.id, row.label)}
          type="button"
        />
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.deleteRowButton}`}
          disabled={loadingStatus === 'pending'}
          icon={getBtnIcon(row.id, 'trash')}
          onClick={() => onShowDeleteDialog(row.id)}
          type="button"
        />
      </Fragment>
    );
  };

  const dialogFooter = (
    <div className={styles.footer}>
      <Button
        className="p-button-primary"
        icon={'plus'}
        label={resourcesContext.messages['add']}
        onClick={() => setIsAddEditDialogVisible(true)}
      />
      <Button
        className={`p-button-secondary ${styles.buttonPushRight}`}
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={onCloseDialog}
      />
    </div>
  );

  const checkNameExists = () => webforms.some(webform => webform.label === webformName);

  const getIsDisabledConfirmBtn = () => {
    if (isNil(selectedWebformId)) {
      return isEmpty(webformName) || isEmpty(jsonContent) || checkNameExists() || loadingStatus === 'pending';
    }

    if (!isEmpty(webformName) && isEmpty(jsonContent)) {
      return getInitialName() === webformName || checkNameExists();
    }

    return (isEmpty(jsonContent) && isEmpty(webformName)) || isEmpty(webformName) || loadingStatus === 'pending';
  };

  const addEditDialogFooter = (
    <Fragment>
      <Button
        className={`p-button-primary ${getIsDisabledConfirmBtn() ? '' : 'p-button-animated-blink'}`}
        disabled={getIsDisabledConfirmBtn()}
        icon={loadingStatus === 'pending' ? 'spinnerAnimate' : 'check'}
        label={isNil(selectedWebformId) ? resourcesContext.messages['create'] : resourcesContext.messages['edit']}
        onClick={onConfirm}
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon="cancel"
        label={resourcesContext.messages['cancel']}
        onClick={onAddEditDialogClose}
      />
    </Fragment>
  );

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
          <p>{resourcesContext.messages['loadWebformsConfigurationError']}</p>
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
        footer={dialogFooter}
        header={resourcesContext.messages['manageWebformsConfiguration']}
        modal
        onHide={onCloseDialog}
        visible={isDialogVisible}>
        {renderDialogContent()}
      </Dialog>

      {isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resourcesContext.messages['deleteWebformConfiguration']}
          labelCancel={resourcesContext.messages['cancel']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onConfirmDeleteDialog}
          onHide={onHideDeleteDialog}
          visible={isDeleteDialogVisible}>
          {resourcesContext.messages['confirmDeleteWebformConfiguration']}
        </ConfirmDialog>
      )}

      {isAddEditDialogVisible && (
        <Dialog
          blockScroll={false}
          className={`responsiveDialog ${styles.addEditDialog}`}
          footer={addEditDialogFooter}
          header={
            isNil(selectedWebformId)
              ? resourcesContext.messages['addWebformConfigurationDialogHeader']
              : resourcesContext.messages['editWebformConfigurationDialogHeader']
          }
          modal
          onHide={onAddEditDialogClose}
          visible={isAddEditDialogVisible}>
          <label htmlFor="name">
            {resourcesContext.messages['name']}
            <InputText
              className={styles.nameInput}
              id="name"
              maxLength={50}
              onChange={event => setWebformName(event.target.value)}
              value={webformName}
            />
          </label>

          <InputFile
            accept="application/JSON"
            buttonTextNoFile={resourcesContext.messages['inputFileButtonNotSelected']}
            buttonTextWithFile={resourcesContext.messages['inputFileButtonSelected']}
            fileRef={fileRef}
            onChange={onFileUpload}
            onClearFile={onClearFile}
          />
        </Dialog>
      )}
    </Fragment>
  );
};
