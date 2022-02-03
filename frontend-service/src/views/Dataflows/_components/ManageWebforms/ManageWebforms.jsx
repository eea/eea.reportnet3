import { Fragment, useContext, useEffect, useRef, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import orderBy from 'lodash/orderBy';
import ReactTooltip from 'react-tooltip';

import styles from './ManageWebforms.module.scss';

import { Column } from 'primereact/column';

import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { DownloadFile } from 'views/_components/DownloadFile';
import { Dropdown } from 'views/_components/Dropdown';
import { ErrorMessage } from 'views/_components/ErrorMessage';
import { InputFile } from 'views/_components/InputFile';
import { InputText } from 'views/_components/InputText';
import { Spinner } from 'views/_components/Spinner';

import { WebformService } from 'services/WebformService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const ManageWebforms = ({ onCloseDialog, isDialogVisible }) => {
  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [errors, setErrors] = useState({
    name: { hasErrors: false, message: '' },
    type: { hasErrors: false, message: '' },
    content: { hasErrors: false, message: '' }
  });
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

  useEffect(() => {
    if (isAddEditDialogVisible) {
      checkHasErrors('content');
    }
  }, [webformConfiguration.content]);

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

      if (error.response.status === 400) {
        notificationContext.add({ type: 'DELETE_WEBFORM_IN_USE_ERROR' }, true);
      } else {
        notificationContext.add({ type: 'DELETE_WEBFORM_CONFIGURATION_ERROR' }, true);
      }
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

  const onShowDeleteDialog = webformRow => {
    setWebformConfiguration(webformRow);
    setIsDeleteDialogVisible(true);
  };

  const onHideDeleteDialog = () => {
    setIsDeleteDialogVisible(false);
    resetWebformConfiguration();
  };

  const onEditClick = webformRow => {
    setWebformConfiguration(webformRow);
    setIsAddEditDialogVisible(true);
  };

  const onAddEditDialogClose = () => {
    setIsAddEditDialogVisible(false);
    resetWebformConfiguration();
    setErrors({ name: false, type: false, content: false });
  };

  const onClickDownload = webformRow => {
    setWebformConfiguration(webformRow);
    onDownload(webformRow.id, webformRow.name);
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

  const getTypeTemplate = ({ type }) => <span>{typesKeyValues[type]}</span>;

  const getActionsTemplate = webformRow => {
    return (
      <Fragment>
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.actionButton}`}
          disabled={loadingStatus === 'pending'}
          icon={getBtnIcon(webformRow.id, 'edit')}
          onClick={() => onEditClick(webformRow)}
          type="button"
        />
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.actionButton}`}
          disabled={loadingStatus === 'pending'}
          icon={getBtnIcon(webformRow.id, 'export')}
          onClick={() => onClickDownload(webformRow)}
          type="button"
        />
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.deleteRowButton}`}
          disabled={loadingStatus === 'pending'}
          icon={getBtnIcon(webformRow.id, 'trash')}
          onClick={() => onShowDeleteDialog(webformRow)}
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

  const checkNameExists = () => webformsList.some(webform => webform.name === webformConfiguration.name.trim());

  const checkNameExistsWithoutCurrent = () =>
    webformsList
      .filter(webform => webform.id !== webformConfiguration.id)
      .some(webform => webform.name === webformConfiguration.name.trim());

  const getIsDisabledConfirmBtn = () => {
    if (isNil(webformConfiguration.id)) {
      return (
        isEmpty(webformConfiguration.name.trim()) ||
        isEmpty(webformConfiguration.content) ||
        isEmpty(webformConfiguration.type) ||
        checkNameExists() ||
        loadingStatus === 'pending'
      );
    }

    const initialWebformConfiguration = getInitialWebformConfiguration();

    if (
      !isEmpty(webformConfiguration.name.trim()) &&
      isEmpty(webformConfiguration.content) &&
      initialWebformConfiguration.type === webformConfiguration.type
    ) {
      return initialWebformConfiguration.name === webformConfiguration.name || checkNameExists();
    }

    if (
      initialWebformConfiguration.type !== webformConfiguration.type &&
      isEmpty(webformConfiguration.content) &&
      initialWebformConfiguration.name === webformConfiguration.name
    ) {
      return initialWebformConfiguration.type === webformConfiguration.type;
    }

    return (
      (isEmpty(webformConfiguration.content) && isEmpty(webformConfiguration.name.trim())) ||
      isEmpty(webformConfiguration.name.trim()) ||
      checkNameExistsWithoutCurrent() ||
      loadingStatus === 'pending'
    );
  };

  const checkHasErrors = field => {
    let hasErrors = false;
    let message = '';

    if (isNil(webformConfiguration.id)) {
      switch (field) {
        case 'name':
          if (isEmpty(webformConfiguration.name.trim())) {
            hasErrors = true;
            message = resourcesContext.messages['nameIsEmptyError'];
          } else if (checkNameExists()) {
            hasErrors = true;
            message = resourcesContext.messages['nameExistsError'];
          }
          break;

        case 'type':
          webformConfiguration;
          if (isEmpty(webformConfiguration.type)) {
            hasErrors = true;
            message = resourcesContext.messages['typeNotSelectedError'];
          }
          break;

        case 'content':
          if (isEmpty(webformConfiguration.content)) {
            hasErrors = true;
            message = resourcesContext.messages['fileNotSelectedError'];
          }
          break;

        default:
          break;
      }
    } else {
      switch (field) {
        case 'name':
          if (isEmpty(webformConfiguration.name.trim())) {
            hasErrors = true;
            message = resourcesContext.messages['nameIsEmptyError'];
          } else if (checkNameExistsWithoutCurrent()) {
            hasErrors = true;
            message = resourcesContext.messages['nameExistsError'];
          }
          break;

        default:
          break;
      }
    }

    setErrors(prevErrors => ({ ...prevErrors, [field]: { hasErrors, message } }));
  };

  const renderTooltip = () => {
    if (isNil(webformConfiguration.id) && getIsDisabledConfirmBtn() && isEmpty(webformConfiguration.content)) {
      return (
        <ReactTooltip border={true} className={styles.tooltip} effect="solid" id="confirmBtn" place="top">
          {resourcesContext.messages['fileNotSelectedError']}
        </ReactTooltip>
      );
    }
  };

  const addEditDialogFooter = (
    <Fragment>
      <span data-for="confirmBtn" data-tip>
        <Button
          className={`p-button-primary ${getIsDisabledConfirmBtn() ? '' : 'p-button-animated-blink'}`}
          disabled={getIsDisabledConfirmBtn()}
          icon={loadingStatus === 'pending' ? 'spinnerAnimate' : 'check'}
          label={
            isNil(webformConfiguration.id) ? resourcesContext.messages['create'] : resourcesContext.messages['save']
          }
          onClick={onConfirm}
        />
      </span>

      {renderTooltip()}

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

    if (isEmpty(webformsList)) {
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
            className={`${styles.nameInput} ${errors.name.hasErrors ? styles.inputError : ''}`}
            id="name"
            maxLength={50}
            onBlur={() => checkHasErrors('name')}
            onChange={event => {
              event.persist();
              setWebformConfiguration(prev => ({ ...prev, name: event.target.value }));
              setErrors(prevErrors => ({ ...prevErrors, name: { hasErrors: false, message: '' } }));
            }}
            placeholder={resourcesContext.messages['name']}
            value={webformConfiguration.name}
          />
          {errors.name.hasErrors && <ErrorMessage className={styles.errorMessage} message={errors.name.message} />}

          <Dropdown
            appendTo={document.body}
            className={`${styles.typeDropdown} ${errors.type.hasErrors ? styles.typeError : ''}`}
            id="type"
            onBlur={() => checkHasErrors('type')}
            onChange={e => {
              setWebformConfiguration({ ...webformConfiguration, type: e.value.value });
              setErrors(prevErrors => ({ ...prevErrors, type: { hasErrors: false, message: '' } }));
            }}
            optionLabel="name"
            options={dropdownOptions}
            optionValue="value"
            placeholder={resourcesContext.messages['webformsConfigurationsSelect']}
            value={{ name: typesKeyValues[webformConfiguration.type], value: webformConfiguration.type }}
          />
          {errors.type.hasErrors && <ErrorMessage className={styles.errorMessage} message={errors.type.message} />}

          <InputFile
            accept=".json"
            buttonTextNoFile={resourcesContext.messages['inputFileButtonNotSelected']}
            buttonTextWithFile={resourcesContext.messages['inputFileButtonSelected']}
            className={styles.inputFile}
            fileRef={fileRef}
            hasError={errors.content.hasErrors}
            onChange={onFileUpload}
            onClearFile={onClearFile}
            onClick={() => checkHasErrors('content')}
          />
        </Dialog>
      )}
    </Fragment>
  );
};
