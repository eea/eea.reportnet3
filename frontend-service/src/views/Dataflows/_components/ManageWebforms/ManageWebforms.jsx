import { Fragment, useContext, useEffect, useLayoutEffect, useRef, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import orderBy from 'lodash/orderBy';

import styles from './ManageWebforms.module.scss';

import { Column } from 'primereact/column';

import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { DownloadFile } from 'views/_components/DownloadFile';
import { Dropdown } from 'views/_components/Dropdown';
import { InputFile } from './_components/InputFile';
import { InputText } from 'views/_components/InputText';
import { Spinner } from 'views/_components/Spinner';

import { WebformService } from 'services/WebformService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const ManageWebforms = ({ onCloseDialog, isDialogVisible }) => {
  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [errors, setErrors] = useState({ name: false, type: false, content: false });
  const [isAddEditDialogVisible, setIsAddEditDialogVisible] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [webformConfiguration, setWebformConfiguration] = useState({ id: null, name: '', type: '', content: '' });
  const [webformsList, setWebformsConfigurationsList] = useState([]);

  const fileRef = useRef(null);

  const dropdownOptions = [
    { name: resourcesContext.messages['pamsLabel'], value: 'PAMS' },
    { name: resourcesContext.messages['qaLabel'], value: 'QA' },
    { name: resourcesContext.messages['tables'], value: 'TABLES' }
  ];

  const typesKeyValues = {
    PAMS: resourcesContext.messages['pamsLabel'],
    QA: resourcesContext.messages['qaLabel'],
    TABLES: resourcesContext.messages['tables']
  };

  useEffect(() => {
    getWebformList();
  }, []);

  useEffect(() => {
    setWebformConfiguration(getInitialWebformConfiguration);
  }, [webformConfiguration.id]);

  useLayoutEffect(() => {
    if (isAddEditDialogVisible) {
      checkHasErrors();
    }
  }, [webformConfiguration]);

  const getWebformList = async () => {
    setLoadingStatus('pending');

    try {
      const data = await WebformService.getAll();
      setWebformsConfigurationsList(orderBy(data, 'name', 'asc'));
      setLoadingStatus('success');
    } catch (error) {
      console.error('ManageWebforms - getWebformList.', error);
      setLoadingStatus('error');
      notificationContext.add({ type: 'LOADING_WEBFORM_OPTIONS_ERROR' }, true);
    } finally {
      setIsLoading(false);
    }
  };

  const onConfirmDeleteDialog = async () => {
    setLoadingStatus('pending');
    setIsDeleteDialogVisible(false);

    try {
      await WebformService.delete(webformConfiguration.id);
      getWebformList();
    } catch (error) {
      console.error('ManageWebforms - onConfirmDeleteDialog.', error);
      setLoadingStatus('failed');
      notificationContext.add({ type: 'DELETE_WEBFORM_CONFIGURATION_ERROR' }, true);
    } finally {
      resetWebformConfiguration();
    }
  };

  const resetWebformConfiguration = () => setWebformConfiguration({ id: null, name: '', type: '', content: '' });

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
      resetWebformConfiguration();
    }
  };

  const onConfirm = async () => {
    setLoadingStatus('pending');

    try {
      if (isNil(webformConfiguration.id)) {
        await WebformService.create(webformConfiguration);
      } else {
        await WebformService.update(webformConfiguration);
      }

      setLoadingStatus('success');
      getWebformList();
      setIsAddEditDialogVisible(false);
      resetWebformConfiguration();
    } catch (error) {
      console.error('ManageWebforms - onConfirm.', error);
      setLoadingStatus('error');
      if (isNil(webformConfiguration.id)) {
        notificationContext.add({ type: 'CREATE_WEBFORM_CONFIGURATION_ERROR' }, true);
      } else {
        notificationContext.add({ type: 'EDIT_WEBFORM_CONFIGURATION_ERROR' }, true);
      }
    }
  };

  const onShowDeleteDialog = row => {
    setWebformConfiguration({ ...row });
    setIsDeleteDialogVisible(true);
  };

  const onHideDeleteDialog = () => {
    setIsDeleteDialogVisible(false);
    resetWebformConfiguration();
  };

  const onEditClick = row => {
    setWebformConfiguration({ ...row });
    setIsAddEditDialogVisible(true);
  };

  const onAddEditDialogClose = () => {
    setIsAddEditDialogVisible(false);
    resetWebformConfiguration();
    setErrors({ name: false, content: false });
  };

  const onClickDownload = row => {
    setWebformConfiguration({ ...row });
    onDownload(row.id, row.name);
  };

  const onFileUpload = async e => {
    if (!isNil(e.target.files[0])) {
      const reader = new FileReader();
      reader.onload = async e => {
        const text = e.target.result;
        setWebformConfiguration(prev => ({ ...prev, content: text }));
      };
      reader.readAsText(e.target.files[0]);
    }
  };

  const onClearFile = () => setWebformConfiguration(prev => ({ ...prev, content: '' }));

  const getTableColumns = () => {
    const columns = [
      { key: 'name', header: resourcesContext.messages['name'] },
      { key: 'type', header: resourcesContext.messages['type'], template: getTypeTemplate },
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
    if (id === webformConfiguration.id && loadingStatus === 'pending') {
      return 'spinnerAnimate';
    }

    return iconName;
  };

  const getTypeTemplate = ({ type }) => {
    return <span>{typesKeyValues[type]}</span>;
  };

  const getActionsTemplate = row => {
    return (
      <Fragment>
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.actionButton}`}
          disabled={loadingStatus === 'pending'}
          icon={getBtnIcon(row.id, 'edit')}
          onClick={() => onEditClick(row)}
          type="button"
        />
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.actionButton}`}
          disabled={loadingStatus === 'pending'}
          icon={getBtnIcon(row.id, 'export')}
          onClick={() => onClickDownload(row)}
          type="button"
        />
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.deleteRowButton}`}
          disabled={loadingStatus === 'pending'}
          icon={getBtnIcon(row.id, 'trash')}
          onClick={() => onShowDeleteDialog(row)}
          type="button"
        />
      </Fragment>
    );
  };

  const dialogFooter = (
    <div className={styles.footer}>
      <Button
        className="p-button-primary"
        disabled={loadingStatus === 'pending' && isNil(webformConfiguration.id)}
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

  const getInitialWebformConfiguration = () => {
    if (!webformConfiguration.id) {
      return { id: null, name: '', type: '', content: '' };
    }

    const currentWebform = webformsList.find(webform => webform.id === webformConfiguration.id);

    return { ...currentWebform };
  };

  const checkLabelExists = () => webformsList.some(webform => webform.name === webformConfiguration.name);

  const checkNameExistsWithoutCurrent = () =>
    webformsList
      .filter(webform => webform.id !== webformConfiguration.id)
      .some(webform => webform.name === webformConfiguration.name);

  const getIsDisabledConfirmBtn = () => {
    if (isNil(webformConfiguration.id)) {
      return (
        isEmpty(webformConfiguration.name) ||
        isEmpty(webformConfiguration.content) ||
        isEmpty(webformConfiguration.type) ||
        checkLabelExists() ||
        loadingStatus === 'pending'
      );
    }

    if (!isEmpty(webformConfiguration.name) && isEmpty(webformConfiguration.content)) {
      return getInitialWebformConfiguration().name === webformConfiguration.name || checkLabelExists();
    }

    return (
      (isEmpty(webformConfiguration.content) && isEmpty(webformConfiguration.name)) ||
      isEmpty(webformConfiguration.name) ||
      checkNameExistsWithoutCurrent() ||
      loadingStatus === 'pending'
    );
  };

  const checkHasErrors = () => {
    if (isNil(webformConfiguration.id)) {
      setErrors({
        name: isEmpty(webformConfiguration.name) || checkLabelExists(),
        type: isEmpty(webformConfiguration.type),
        content: isEmpty(webformConfiguration.content)
      });
    } else {
      setErrors({
        name: isEmpty(webformConfiguration.name) || checkNameExistsWithoutCurrent(),
        content: false
      });
    }
  };

  const addEditDialogFooter = (
    <Fragment>
      <Button
        className={`p-button-primary ${getIsDisabledConfirmBtn() ? '' : 'p-button-animated-blink'}`}
        disabled={getIsDisabledConfirmBtn()}
        icon={loadingStatus === 'pending' ? 'spinnerAnimate' : 'check'}
        label={isNil(webformConfiguration.id) ? resourcesContext.messages['create'] : resourcesContext.messages['save']}
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

    return (
      <div className={styles.dialogContent}>
        <DataTable
          autoLayout
          hasDefaultCurrentPage
          loading={loadingStatus === 'pending' && isNil(webformConfiguration.id)}
          paginator
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={webformsList.length}
          value={webformsList}>
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
            isNil(webformConfiguration.id)
              ? resourcesContext.messages['addWebformConfigurationDialogHeader']
              : resourcesContext.messages['editWebformConfigurationDialogHeader']
          }
          modal
          onHide={onAddEditDialogClose}
          visible={isAddEditDialogVisible}>
          <InputText
            className={`${styles.nameInput} ${errors.name ? styles.inputError : ''}`}
            id="name"
            maxLength={50}
            onChange={event => {
              event.persist();
              setWebformConfiguration(prev => ({ ...prev, name: event.target.value }));
            }}
            placeholder={resourcesContext.messages['name']}
            value={webformConfiguration.name}
          />

          <Dropdown
            appendTo={document.body}
            className={`${styles.typeDropdown} ${errors.type ? styles.typeError : ''}`}
            id="errorType"
            onChange={e => setWebformConfiguration(prev => ({ ...prev, type: e.value.value }))}
            optionLabel="name"
            options={dropdownOptions}
            optionValue="value"
            placeholder={resourcesContext.messages['webformsConfigurationsSelect']}
            value={{ name: typesKeyValues[webformConfiguration.type], value: webformConfiguration.type }}
          />

          <InputFile
            accept=".json"
            buttonTextNoFile={resourcesContext.messages['inputFileButtonNotSelected']}
            buttonTextWithFile={resourcesContext.messages['inputFileButtonSelected']}
            fileRef={fileRef}
            hasError={errors.content}
            onChange={onFileUpload}
            onClearFile={onClearFile}
          />
        </Dialog>
      )}
    </Fragment>
  );
};
