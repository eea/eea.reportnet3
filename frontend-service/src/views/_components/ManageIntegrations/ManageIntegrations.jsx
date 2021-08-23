import { Fragment, useContext, useEffect, useReducer, useRef } from 'react';
import ReactTooltip from 'react-tooltip';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniqueId from 'lodash/uniqueId';

import styles from './ManageIntegrations.module.scss';

import { config } from 'conf';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { Button } from 'views/_components/Button';
import { Checkbox } from 'views/_components/Checkbox';
import { Dialog } from 'views/_components/Dialog';
import { Dropdown } from 'views/_components/Dropdown';
import { InputText } from 'views/_components/InputText';
import { Spinner } from 'views/_components/Spinner';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { IntegrationService } from 'services/IntegrationService';

import { manageIntegrationsReducer } from './_functions/Reducers/manageIntegrationsReducer';

import { useInputTextFocus } from 'views/_functions/Hooks/useInputTextFocus';
import { useLockBodyScroll } from 'views/_functions/Hooks/useLockBodyScroll';

import { ManageIntegrationsUtils } from './_functions/Utils/ManageIntegrationsUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

export const ManageIntegrations = ({
  dataflowId,
  datasetId,
  datasetType,
  integrationsList,
  manageDialogs,
  onUpdateData,
  refreshList,
  setIsCreating,
  setIsUpdating = () => {},
  state,
  updatedData
}) => {
  const { datasetSchemaId, isIntegrationManageDialogVisible } = state;
  const componentName = 'integration';

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const editParameterRef = useRef(null);
  const integrationNameRef = useRef(null);
  const parameterRef = useRef(null);

  const inputRefs = { name: integrationNameRef, parameterKey: editParameterRef };

  const [manageIntegrationsState, manageIntegrationsDispatch] = useReducer(manageIntegrationsReducer, {
    dataflowId,
    datasetSchemaId,
    description: '',
    displayErrors: false,
    editorView: { isEditing: false, id: null },
    externalParameters: [],
    fileExtension: '',
    id: null,
    isIntegrationCreating: false,
    isIntegrationEditing: false,
    isLoading: true,
    isUpdatedVisible: false,
    name: '',
    notificationRequired: false,
    operation: {},
    parameterKey: '',
    parametersErrors: { content: '', header: '', isDialogVisible: false, option: '' },
    parameterValue: '',
    processes: [],
    processName: {},
    repositories: [],
    repository: {},
    tool: 'FME'
  });

  const { editorView, externalParameters, parameterKey, parametersErrors } = manageIntegrationsState;
  const { isDuplicatedIntegrationName, isDuplicatedParameter, isFormEmpty, isParameterEditing, printError } =
    ManageIntegrationsUtils;

  const isEditingParameter = isParameterEditing(externalParameters);
  const isEmptyForm = isFormEmpty(manageIntegrationsState);
  const isIntegrationNameDuplicated = isDuplicatedIntegrationName(
    manageIntegrationsState.name,
    integrationsList,
    manageIntegrationsState.id
  );
  const isKeyDuplicated = isDuplicatedParameter(editorView.id, externalParameters, parameterKey);
  const operationsWithFileExtension = ['IMPORT', 'EXPORT'];

  useEffect(() => {
    if (!isEmpty(updatedData)) getUpdatedData();
  }, [updatedData]);

  useEffect(() => {
    getRepositories();
  }, []);

  useEffect(() => {
    getProcesses();
  }, [manageIntegrationsState.repository]);

  useInputTextFocus(editorView.isEditing, editParameterRef);
  useInputTextFocus(isEditingParameter, parameterRef);
  useInputTextFocus(isIntegrationManageDialogVisible, integrationNameRef);

  useLockBodyScroll(parametersErrors.isDialogVisible);

  const getRepositories = async () => {
    try {
      manageIntegrationsDispatch({
        type: 'GET_REPOSITORIES',
        payload: { data: await IntegrationService.getFMERepositories(datasetId) }
      });
    } catch (error) {
      console.error('ManageIntegrations - getRepositories.', error);
      notificationContext.add({ type: 'ERROR_LOADING_REPOSITORIES' });
    } finally {
      isLoading(false);
    }
  };

  const getProcesses = async () => {
    if (!isEmpty(manageIntegrationsState.repository)) {
      try {
        manageIntegrationsDispatch({
          type: 'GET_PROCESSES',
          payload: {
            data: await IntegrationService.getFMEProcesses(manageIntegrationsState.repository.value, datasetId)
          }
        });
      } catch (error) {
        console.error('ManageIntegrations - getProcesses.', error);
        notificationContext.add({ type: 'ERROR_LOADING_PROCESSES' });
      }
    } else {
      manageIntegrationsDispatch({ type: 'GET_PROCESSES', payload: { data: [] } });
    }
  };

  const getUpdatedData = () => manageIntegrationsDispatch({ type: 'GET_UPDATED_DATA', payload: updatedData });

  const isLoading = value => manageIntegrationsDispatch({ type: 'IS_LOADING', payload: { value } });

  const onAddParameter = () => {
    manageIntegrationsDispatch({
      type: 'ON_ADD_PARAMETER',
      payload: { data: ManageIntegrationsUtils.onAddParameter(manageIntegrationsState) }
    });
  };

  const onBlurParameter = (id, option, event) => {
    if (isEmpty(event.target.value.trim())) onToggleDialogError('empty', option, true);

    !isDuplicatedParameter(id, externalParameters, event.target.value)
      ? onUpdateSingleParameter(id, option, event)
      : onToggleDialogError('duplicated', option, true);
  };

  const onChangeNotificationRequiredCheckboxEvent = (data, name) => {
    manageIntegrationsDispatch({ type: 'IS_NOTIFICATION_REQUIRED', payload: { data, name } });
  };

  const onChangeParameter = (value, option, id) => {
    manageIntegrationsDispatch({
      type: 'MANAGE_PARAMETERS',
      payload: { data: ManageIntegrationsUtils.onUpdateData(id, option, externalParameters, value) }
    });
  };

  const onCloseModal = () => {
    if (datasetType === 'designDataset') {
      manageDialogs('isIntegrationManageDialogVisible', false);
    } else {
      manageDialogs(false);
    }
  };

  const setIsIntegrationManaging = (state, value) => {
    manageIntegrationsDispatch({
      type: 'SET_IS_INTEGRATION_MANAGING',
      payload: { state, value }
    });
  };

  const onCreateIntegration = async () => {
    setIsIntegrationManaging('isIntegrationCreating', true);
    setIsCreating(true);
    try {
      manageIntegrationsState.name = manageIntegrationsState.name.trim();
      await IntegrationService.create(manageIntegrationsState);
      onCloseModal();
      onUpdateData();
      refreshList(true);
    } catch (error) {
      console.error('ManageIntegrations - onCreateIntegration.', error);
      notificationContext.add({ type: 'CREATE_INTEGRATION_ERROR' });
      setIsCreating(false);
    } finally {
      setIsIntegrationManaging('isIntegrationCreating', false);
    }
  };

  const onDeleteParameter = id => {
    manageIntegrationsDispatch({
      type: 'MANAGE_PARAMETERS',
      payload: { data: externalParameters.filter(parameter => parameter.id !== id) }
    });
  };

  const onEditKeyDown = (event, id, option) => {
    const duplicated = isDuplicatedParameter(id, externalParameters, event.target.value);

    if (event.key === 'Enter' && isEmpty(event.target.value.trim())) onToggleDialogError('empty', option, true);

    if (event.key === 'Enter' && duplicated) onToggleDialogError('duplicated', option, true);

    if (event.key === 'Enter' && !duplicated) onUpdateSingleParameter(id, option, event);

    if (event.key === 'Escape') {
      event.preventDefault();
      const value = externalParameters.filter(parameter => parameter.id === id).map(value => value.prevValue[option]);

      ManageIntegrationsUtils.onUpdateData(id, option, externalParameters, value);
      onToggleEditorView(id, [option]);
    }
  };

  const onEditParameter = id => {
    const keyData = ManageIntegrationsUtils.getParameterData(id, 'key', externalParameters);
    const valueData = ManageIntegrationsUtils.getParameterData(id, 'value', externalParameters);

    manageIntegrationsDispatch({ type: 'TOGGLE_EDIT_VIEW', payload: { id, isEdit: true, keyData, valueData } });
  };

  const onFillField = (data, name) => {
    manageIntegrationsDispatch({ type: 'ON_FILL', payload: { data, name } });
  };

  const onFillFieldRepository = (data, name) => {
    manageIntegrationsDispatch({ type: 'ON_FILL_REPOSITORY', payload: { data, name, processName: [] } });
  };

  const onFillOperation = (data, name) => {
    manageIntegrationsDispatch({ type: 'ON_FILL_OPERATION', payload: { data, name } });
    manageIntegrationsDispatch({
      type: 'CLEAR_FILE_EXTENSION_NOTIFICATION_REQUIRED',
      payload: { fileExtension: '', notificationRequired: false }
    });
  };

  const onResetParameterInput = () => {
    manageIntegrationsDispatch({
      type: 'TOGGLE_EDIT_VIEW',
      payload: { id: null, isEdit: false, keyData: '', valueData: '' }
    });
  };

  const onSaveKeyDown = event => {
    if (event.key === 'Enter' && !isEmpty(parameterKey.trim()) && !isKeyDuplicated) {
      onSaveParameter();
    }
  };

  const onSaveParameter = () => (editorView.isEditing ? onUpdateParameter() : onAddParameter());

  const onShowErrors = () => manageIntegrationsDispatch({ type: 'SHOW_ERRORS', payload: { value: true } });

  const onToggleDialogError = (errorType, option, value) => {
    const dialogContent = {
      duplicated: resourcesContext.messages['duplicatedParameterKeyErrorContent'],
      empty: resourcesContext.messages['emptyParameterErrorContent']
    };

    const dialogHeader = {
      duplicated: resourcesContext.messages['duplicatedParameterKeyErrorHeader'],
      empty: resourcesContext.messages['emptyParameterErrorHeader']
    };

    if (parameterRef.current) parameterRef.current.element.focus();

    manageIntegrationsDispatch({
      type: 'TOGGLE_ERROR_DIALOG',
      payload: { content: dialogContent[errorType], header: dialogHeader[errorType], option, value }
    });
  };

  const onToggleEditorView = (id, option) => {
    if (!editorView.isEditing) {
      manageIntegrationsDispatch({
        type: 'MANAGE_PARAMETERS',
        payload: { data: ManageIntegrationsUtils.toggleParameterEditorView(id, option, externalParameters) }
      });
    }
  };

  const onUpdateIntegration = async () => {
    try {
      setIsIntegrationManaging('isIntegrationEditing', true);
      setIsUpdating(true);
      manageIntegrationsState.name = manageIntegrationsState.name.trim();
      await IntegrationService.update(manageIntegrationsState);
      onCloseModal();
      onUpdateData();
      refreshList(true);
    } catch (error) {
      console.error('ManageIntegrations - onUpdateIntegration.', error);
      notificationContext.add({ type: 'UPDATE_INTEGRATION_ERROR' });
      setIsUpdating(false);
    } finally {
      setIsIntegrationManaging('isIntegrationEditing', false);
    }
  };

  const onUpdateParameter = () => {
    onResetParameterInput();

    manageIntegrationsDispatch({
      type: 'MANAGE_PARAMETERS',
      payload: { data: ManageIntegrationsUtils.onUpdateCompleteParameter(editorView.id, manageIntegrationsState) }
    });
  };

  const onUpdateSingleParameter = (id, option, event) => {
    if (!isEmpty(event.target.value.trim())) {
      ManageIntegrationsUtils.onUpdateData(id, option, externalParameters, event.target.value);
      onToggleEditorView(id, [option]);
    }
  };

  const renderDialogFooterTooltipContent = () => {
    if (isIntegrationNameDuplicated) return 'duplicatedIntegrationName';
    return 'fcSubmitButtonDisabled';
  };

  const renderCheckboxLayout = options => {
    return options.map(option => (
      <div className={`${styles.field} ${styles[option]} formField `} key={`${componentName}__${option}`}>
        <label htmlFor={`${componentName}__${option}`}>
          {resourcesContext.messages[option]}
          <Button
            className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
            icon="infoCircle"
            tooltip={resourcesContext.messages['notificationRequiredTooltip']}
            tooltipOptions={{ position: 'top' }}
          />
        </label>
        <div className={styles.checkboxWrapper}>
          <Checkbox
            ariaLabel={`${componentName}__${option}`}
            checked={manageIntegrationsState.notificationRequired}
            id={`${componentName}__${option}`}
            inputId={`${componentName}__${option}`}
            label="notificationRequired"
            onChange={event => {
              onChangeNotificationRequiredCheckboxEvent(event.checked, option);
            }}
            value={manageIntegrationsState[option]}
          />
        </div>
      </div>
    ));
  };

  const renderDialogFooter = (
    <Fragment>
      <span data-for="integrationTooltip" data-tip>
        <Button
          className="p-button-rounded p-button-animated-blink"
          disabled={
            isIntegrationNameDuplicated ||
            manageIntegrationsState.isIntegrationCreating ||
            manageIntegrationsState.isIntegrationEditing
          }
          icon={
            manageIntegrationsState.isIntegrationCreating || manageIntegrationsState.isIntegrationEditing
              ? 'spinnerAnimate'
              : 'check'
          }
          label={!isEmpty(updatedData) ? resourcesContext.messages['update'] : resourcesContext.messages['create']}
          onClick={() => {
            if (isEmptyForm) onShowErrors();
            else !isEmpty(updatedData) ? onUpdateIntegration() : onCreateIntegration();
          }}
        />
      </span>
      <Button
        className="p-button-secondary p-button-rounded p-button-animated-blink button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['cancel']}
        onClick={() => onCloseModal()}
      />

      {(isEmptyForm || isIntegrationNameDuplicated) && (
        <ReactTooltip border={true} effect="solid" id="integrationTooltip" place="top">
          {resourcesContext.messages[renderDialogFooterTooltipContent()]}
        </ReactTooltip>
      )}
    </Fragment>
  );

  const renderDialogLayout = children =>
    isIntegrationManageDialogVisible && (
      <Dialog
        footer={renderDialogFooter}
        header={
          !isEmpty(updatedData)
            ? resourcesContext.messages['editExternalIntegration']
            : resourcesContext.messages['createExternalIntegration']
        }
        onHide={() => onCloseModal()}
        style={{ width: '975px' }}
        visible={isIntegrationManageDialogVisible}>
        {children}
      </Dialog>
    );

  const renderDropdownLayout = (options = []) => {
    const optionList = {
      operation: [
        { label: resourcesContext.messages['importOperationManageIntegration'].toUpperCase(), value: 'IMPORT' },
        {
          label: resourcesContext.messages['importFromOtherSystemOperationManageIntegration'].toUpperCase(),
          value: 'IMPORT_FROM_OTHER_SYSTEM'
        },
        { label: resourcesContext.messages['exportOperationManageIntegration'].toUpperCase(), value: 'EXPORT' }
      ],
      repository: manageIntegrationsState.repositories,
      processName: manageIntegrationsState.processes
    };

    return options.map(option => (
      <div
        className={`${styles.field} ${styles[option]} formField ${printError(option, manageIntegrationsState)}`}
        key={`${componentName}__${option}`}>
        <label htmlFor={`${componentName}__${option}`}>{resourcesContext.messages[option]}</label>
        <Dropdown
          appendTo={document.body}
          ariaLabel={resourcesContext.messages[option]}
          disabled={isEmpty(optionList[option])}
          filter={optionList[option].length > 7}
          inputId={`${componentName}__${option}`}
          name={uniqueId(resourcesContext.messages[option])}
          onChange={event => {
            if (option === 'repository') {
              onFillFieldRepository(event.value, option);
            } else if (option === 'operation') {
              onFillOperation(event.value, option);
            } else {
              onFillField(event.value, option);
            }
          }}
          optionLabel="label"
          options={optionList[option]}
          placeholder={resourcesContext.messages[`${option}PlaceHolder`]}
          value={manageIntegrationsState[option]}
        />
      </div>
    ));
  };

  const renderEditorInput = (option, parameter, id) => {
    return (
      <InputText
        id={`editor_${parameter}`}
        onBlur={event => onBlurParameter(id, option, event)}
        onChange={event => onChangeParameter(event.target.value, option, id)}
        onKeyPress={event => onEditKeyDown(event, id, option)}
        ref={parameterRef}
        value={parameter[option]}
      />
    );
  };

  const renderErrorDialogFooter = (
    <Button icon="check" label={resourcesContext.messages['ok']} onClick={() => onToggleDialogError('', '', false)} />
  );

  const renderInputLayout = (options = []) => {
    return options.map(option => {
      return (
        <div
          className={`${styles.field} formField ${printError(option, manageIntegrationsState)} ${
            manageIntegrationsState.operation.value === 'IMPORT' && option === 'fileExtension'
              ? styles.fileExtensionNotification
              : styles[option]
          }`}
          key={`${componentName}__${option}`}>
          <label htmlFor={`${componentName}__${option}`}>{resourcesContext.messages[option]}</label>
          <InputText
            id={`${componentName}__${option}`}
            maxLength={
              option === 'fileExtension'
                ? config.MAX_FILE_EXTENSION_LENGTH
                : option === 'name'
                ? config.MAX_INTEGRATION_NAME_LENGTH
                : config.INPUT_MAX_LENGTH
            }
            onChange={event => onFillField(event.target.value, option)}
            onKeyDown={event => onSaveKeyDown(event)}
            placeholder={resourcesContext.messages[option]}
            ref={inputRefs[option]}
            value={manageIntegrationsState[option]}
          />
        </div>
      );
    });
  };

  const renderParametersLayout = () => {
    const data = [];

    for (let index = 0; index < externalParameters.length; index++) {
      const parameter = externalParameters[index];
      data.push(
        <li
          className={`${styles.item} ${parameter.id === editorView.id ? styles.selected : undefined}`}
          key={parameter.id}>
          <span className={styles.key} onDoubleClick={() => onToggleEditorView(parameter.id, ['key'])}>
            {resourcesContext.messages['parameterKey']}:
            <span
              className={styles.parameterText}
              style={{ overflow: parameter.isEditorView.key ? 'visible' : 'hidden' }}>
              {parameter.isEditorView.key ? renderEditorInput('key', parameter, parameter.id) : parameter.key}
            </span>
          </span>
          <span className={styles.value} onDoubleClick={() => onToggleEditorView(parameter.id, ['value'])}>
            {resourcesContext.messages['parameterValue']}:
            <span
              className={styles.parameterText}
              style={{ overflow: parameter.isEditorView.value ? 'visible' : 'hidden' }}>
              {parameter.isEditorView.value ? renderEditorInput('value', parameter, parameter.id) : parameter.value}
            </span>
          </span>
          <ActionsColumn
            disabledButtons={editorView.isEditing}
            onDeleteClick={() => onDeleteParameter(parameter.id)}
            onEditClick={() => onEditParameter(parameter.id)}
          />
        </li>
      );
    }

    return <ul className={styles.list}>{data}</ul>;
  };

  if (manageIntegrationsState.isLoading) return renderDialogLayout(<Spinner style={{ top: 0 }} />);

  return renderDialogLayout(
    <Fragment>
      <div className={styles.content}>
        <div className={styles.group}>{renderInputLayout(['name', 'description'])}</div>
        <div className={styles.group}>{renderDropdownLayout(['repository', 'processName'])}</div>
        {(isEmpty(updatedData) || manageIntegrationsState.operation.value !== 'EXPORT_EU_DATASET') && (
          <div className={styles.group}>
            {renderDropdownLayout(['operation'])}
            {!isNil(manageIntegrationsState.operation) &&
            operationsWithFileExtension.includes(manageIntegrationsState.operation.value)
              ? renderInputLayout(['fileExtension'])
              : null}
            {!isNil(manageIntegrationsState.operation) && manageIntegrationsState.operation.value === 'IMPORT'
              ? renderCheckboxLayout(['notificationRequired'])
              : null}
          </div>
        )}
        <div className={styles.group}>
          {renderInputLayout(['parameterKey', 'parameterValue'])}
          <span className={styles.buttonWrapper}>
            <span data-for="addParameterTooltip" data-tip>
              <Button
                className="p-button-rounded p-button-animated-blink"
                disabled={isEmpty(parameterKey.trim()) || isKeyDuplicated}
                icon="add"
                label={editorView.isEditing ? resourcesContext.messages['update'] : resourcesContext.messages['add']}
                onClick={() => onSaveParameter()}
              />
            </span>
            {editorView.isEditing && (
              <Button
                className="p-button-secondary p-button-rounded p-button-animated-blink p-button-right-aligned"
                icon={'cancel'}
                label={resourcesContext.messages['cancel']}
                onClick={() => onResetParameterInput()}
              />
            )}

            {isKeyDuplicated && (
              <ReactTooltip border={true} effect="solid" id="addParameterTooltip" place="top">
                {resourcesContext.messages['parameterAlreadyExists']}
              </ReactTooltip>
            )}
          </span>
        </div>
        <div className={styles.group}>
          <span className={styles.parameters}>{renderParametersLayout()}</span>
        </div>
      </div>

      {parametersErrors.isDialogVisible && (
        <Dialog
          footer={renderErrorDialogFooter}
          header={parametersErrors.header}
          onHide={() => onToggleDialogError('', '', false)}
          visible={parametersErrors.isDialogVisible}>
          <span
            dangerouslySetInnerHTML={{
              __html: TextUtils.parseText(parametersErrors.content, { option: parametersErrors.option })
            }}
          />
        </Dialog>
      )}
    </Fragment>
  );
};

ManageIntegrations.defaultProps = {
  dataflowId: null,
  datasetId: null,
  datasetType: 'designDataset',
  onUpdateData: () => {},
  refreshList: () => {}
};
