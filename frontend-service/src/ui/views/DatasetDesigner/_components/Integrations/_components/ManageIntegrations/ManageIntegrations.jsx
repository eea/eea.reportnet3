import React, { Fragment, useContext, useEffect, useReducer, useRef } from 'react';
import ReactTooltip from 'react-tooltip';

import isEmpty from 'lodash/isEmpty';

import styles from './ManageIntegrations.module.scss';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { IntegrationService } from 'core/services/Integration';

import { manageIntegrationsReducer } from './_functions/Reducers/manageIntegrationsReducer';

import { useInputTextFocus } from 'ui/views/_functions/Hooks/useInputTextFocus';
import { useLockBodyScroll } from 'ui/views/_functions/Hooks/useLockBodyScroll';

import { ManageIntegrationsUtils } from './_functions/Utils/ManageIntegrationsUtils';
import { TextUtils } from 'ui/views/_functions/Utils';

export const ManageIntegrations = ({ dataflowId, designerState, integrationsList, manageDialogs, updatedData }) => {
  const { datasetSchemaId, isIntegrationManageDialogVisible } = designerState;
  const componentName = 'integration';

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

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
    isUpdatedVisible: false,
    name: '',
    operation: '',
    parameterKey: '',
    parametersErrors: { content: '', header: '', isDialogVisible: false, option: '' },
    parameterValue: '',
    processName: '',
    tool: 'FME'
  });

  const { editorView, externalParameters, parameterKey, parameterValue, parametersErrors } = manageIntegrationsState;
  const {
    isDuplicatedIntegration,
    isDuplicatedIntegrationName,
    isDuplicatedParameter,
    isFormEmpty,
    isParameterEditing,
    printError
  } = ManageIntegrationsUtils;

  const isEditingParameter = isParameterEditing(externalParameters);
  const isEmptyForm = isFormEmpty(manageIntegrationsState);
  const isIntegrationDuplicated = isDuplicatedIntegration(manageIntegrationsState, updatedData);
  const isIntegrationNameDuplicated = isDuplicatedIntegrationName(
    manageIntegrationsState.name,
    integrationsList,
    manageIntegrationsState.id
  );
  const isKeyDuplicated = isDuplicatedParameter(editorView.id, externalParameters, parameterKey);

  useEffect(() => {
    if (!isEmpty(updatedData)) getUpdatedData();
  }, [updatedData]);

  useInputTextFocus(editorView.isEditing, editParameterRef);
  useInputTextFocus(isEditingParameter, parameterRef);
  useInputTextFocus(isIntegrationManageDialogVisible, integrationNameRef);

  useLockBodyScroll(parametersErrors.isDialogVisible);

  const getUpdatedData = () => manageIntegrationsDispatch({ type: 'GET_UPDATED_DATA', payload: updatedData });

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

  const onChangeParameter = (value, option, id) => {
    manageIntegrationsDispatch({
      type: 'MANAGE_PARAMETERS',
      payload: { data: ManageIntegrationsUtils.onUpdateData(id, option, externalParameters, value) }
    });
  };

  const onCreateIntegration = async () => {
    try {
      const response = await IntegrationService.create(manageIntegrationsState);
      if (response.status >= 200 && response.status <= 299) {
        manageDialogs('isIntegrationManageDialogVisible', false, 'isIntegrationListDialogVisible', true);
      }
    } catch (error) {
      notificationContext.add({ type: 'CREATE_INTEGRATION_ERROR' });
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

  const onFillField = (data, name) => manageIntegrationsDispatch({ type: 'ON_FILL', payload: { data, name } });

  const onResetParameterInput = () => {
    manageIntegrationsDispatch({
      type: 'TOGGLE_EDIT_VIEW',
      payload: { id: null, isEdit: false, keyData: '', valueData: '' }
    });
  };

  const onSaveKeyDown = event => {
    if (event.key === 'Enter' && !isEmpty(parameterKey.trim()) && !isEmpty(parameterValue.trim()) && !isKeyDuplicated) {
      onSaveParameter();
    }
  };

  const onSaveParameter = () => (editorView.isEditing ? onUpdateParameter() : onAddParameter());

  const onShowErrors = () => manageIntegrationsDispatch({ type: 'SHOW_ERRORS', payload: { value: true } });

  const onToggleDialogError = (errorType, option, value) => {
    const dialogContent = {
      duplicated: resources.messages['duplicatedParameterKeyErrorContent'],
      empty: resources.messages['emptyParameterErrorContent']
    };

    const dialogHeader = {
      duplicated: resources.messages['duplicatedParameterKeyErrorHeader'],
      empty: resources.messages['emptyParameterErrorHeader']
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
      const response = await IntegrationService.update(manageIntegrationsState);
      if (response.status >= 200 && response.status <= 299) {
        manageDialogs('isIntegrationManageDialogVisible', false, 'isIntegrationListDialogVisible', true);
      }
    } catch (error) {
      notificationContext.add({ type: 'UPDATE_INTEGRATION_ERROR' });
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

  const renderDialogFooter = (
    <Fragment>
      <span data-tip data-for="integrationTooltip">
        <Button
          className="p-button-rounded p-button-animated-blink"
          disabled={isIntegrationNameDuplicated}
          icon="check"
          label={!isEmpty(updatedData) ? resources.messages['update'] : resources.messages['create']}
          onClick={() => {
            if (isEmptyForm) onShowErrors();
            else !isEmpty(updatedData) ? onUpdateIntegration() : onCreateIntegration();
          }}
        />
      </span>
      <Button
        className="p-button-secondary p-button-rounded  p-button-animated-blink"
        icon="cancel"
        label={resources.messages['cancel']}
        onClick={() => manageDialogs('isIntegrationManageDialogVisible', false, 'isIntegrationListDialogVisible', true)}
      />

      {(isEmptyForm || isIntegrationNameDuplicated) && (
        <ReactTooltip effect="solid" id="integrationTooltip" place="top">
          {isIntegrationNameDuplicated
            ? resources.messages['duplicatedIntegrationName']
            : resources.messages['fcSubmitButtonDisabled']}
        </ReactTooltip>
      )}
    </Fragment>
  );

  const renderDialogLayout = children => (
    <Dialog
      closeOnEscape={false}
      // closeOnEscape={isEditingParameter}
      footer={renderDialogFooter}
      header={
        !isEmpty(updatedData)
          ? resources.messages['editExternalIntegration']
          : resources.messages['createExternalIntegration']
      }
      onHide={() => manageDialogs('isIntegrationManageDialogVisible', false, 'isIntegrationListDialogVisible', true)}
      style={{ width: '975px' }}
      visible={isIntegrationManageDialogVisible}>
      {children}
    </Dialog>
  );

  const renderDropdownLayout = option => (
    <div className={`${styles.field} ${styles[option]} formField ${printError(option, manageIntegrationsState)}`}>
      <label htmlFor={`${componentName}__${option}`}>{resources.messages[option]}</label>
      <Dropdown
        appendTo={document.body}
        inputId={`${componentName}__${option}`}
        onChange={event => onFillField(event.value, option)}
        optionLabel="label"
        options={[
          { label: 'IMPORT', value: 'IMPORT' },
          { label: 'EXPORT', value: 'EXPORT' }
        ]}
        placeholder={resources.messages[`${option}PlaceHolder`]}
        value={manageIntegrationsState[option]}
      />
    </div>
  );

  const renderEditorInput = (option, parameter, id) => {
    return (
      <InputText
        onBlur={event => onBlurParameter(id, option, event)}
        onChange={event => onChangeParameter(event.target.value, option, id)}
        onKeyDown={event => onEditKeyDown(event, id, option)}
        ref={parameterRef}
        value={parameter[option]}
      />
    );
  };

  const renderErrorDialogFooter = (
    <Button icon="check" label={resources.messages['ok']} onClick={() => onToggleDialogError('', '', false)} />
  );

  const renderInputLayout = (options = []) => {
    return options.map((option, index) => (
      <div
        className={`${styles.field} ${styles[option]} formField ${printError(option, manageIntegrationsState)}`}
        key={index}>
        <label htmlFor={`${componentName}__${option}`}>{resources.messages[option]}</label>
        <InputText
          id={`${componentName}__${option}`}
          onChange={event => onFillField(event.target.value, option)}
          onKeyDown={event => onSaveKeyDown(event)}
          placeholder={resources.messages[option]}
          ref={inputRefs[option]}
          type="search"
          value={manageIntegrationsState[option]}
        />
      </div>
    ));
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
            {resources.messages['parameterKey']}:
            <span
              className={styles.parameterText}
              style={{ overflow: parameter.isEditorView.key ? 'visible' : 'hidden' }}>
              {parameter.isEditorView.key ? renderEditorInput('key', parameter, parameter.id) : parameter.key}
            </span>
          </span>
          <span className={styles.value} onDoubleClick={() => onToggleEditorView(parameter.id, ['value'])}>
            {resources.messages['parameterValue']}:
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

  return renderDialogLayout(
    <Fragment>
      <div className={styles.content}>
        <div className={styles.group}>{renderInputLayout(['name', 'description'])}</div>
        <div className={styles.group}>
          {renderInputLayout(['processName'])}
          {renderDropdownLayout('operation')}
          {renderInputLayout(['fileExtension'])}
        </div>
        <div className={styles.group}>
          {renderInputLayout(['parameterKey', 'parameterValue'])}
          <span className={styles.buttonWrapper}>
            <span data-tip data-for="addParameterTooltip">
              <Button
                className="p-button-rounded p-button-animated-blink"
                disabled={isEmpty(parameterKey.trim()) || isEmpty(parameterValue.trim()) || isKeyDuplicated}
                icon="add"
                label={editorView.isEditing ? resources.messages['update'] : resources.messages['add']}
                onClick={() => onSaveParameter()}
              />
            </span>
            {editorView.isEditing && (
              <Button
                className="p-button-secondary p-button-rounded p-button-animated-blink"
                icon={'cancel'}
                label={resources.messages['cancel']}
                onClick={() => onResetParameterInput()}
              />
            )}

            {isKeyDuplicated && (
              <ReactTooltip effect="solid" id="addParameterTooltip" place="top">
                {resources.messages['parameterAlreadyExists']}
              </ReactTooltip>
            )}
          </span>
        </div>
        <div className={styles.group}>
          <span className={styles.parameters}>{renderParametersLayout()}</span>
        </div>
      </div>

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
    </Fragment>
  );
};
