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

import { ManageIntegrationsUtils } from './_functions/Utils/ManageIntegrationsUtils';

export const ManageIntegrations = ({ designerState, manageDialogs, updatedData }) => {
  const { datasetSchemaId, isIntegrationManageDialogVisible } = designerState;
  const componentName = 'integration';

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const parameterRef = useRef(null);

  const [manageIntegrationsState, manageIntegrationsDispatch] = useReducer(manageIntegrationsReducer, {
    datasetSchemaId,
    description: '',
    editorView: { isEditing: false, id: null },
    externalParameters: [],
    fileExtension: '',
    id: null,
    isUpdatedVisible: false,
    name: '',
    operation: '',
    parameterKey: '',
    parameterValue: '',
    processName: '',
    tool: 'FME'
  });

  const { editorView, externalParameters } = manageIntegrationsState;

  useEffect(() => {
    if (!isEmpty(updatedData)) getUpdatedData();
  }, [updatedData]);

  useEffect(() => {
    if (parameterRef.current) parameterRef.current.element.focus();
  }, [parameterRef.current]);

  const getUpdatedData = () => manageIntegrationsDispatch({ type: 'GET_UPDATED_DATA', payload: updatedData });

  const onAddParameter = () => {
    manageIntegrationsDispatch({
      type: 'ON_ADD_PARAMETER',
      payload: { data: ManageIntegrationsUtils.onAddParameter(manageIntegrationsState) }
    });
  };

  const onChangeParameter = (value, option, id) => {
    manageIntegrationsDispatch({
      type: 'MANAGE_PARAMETERS',
      payload: { data: ManageIntegrationsUtils.onUpdateData(id, option, externalParameters, value) }
    });
  };

  const onCreateIntegration = async () => {
    try {
      await IntegrationService.create(manageIntegrationsState);
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

  const onEditParameter = id => {
    const keyData = ManageIntegrationsUtils.getParameterData(id, 'key', externalParameters);
    const valueData = ManageIntegrationsUtils.getParameterData(id, 'value', externalParameters);

    manageIntegrationsDispatch({ type: 'ON_EDIT_PARAMETER', payload: { id, keyData, valueData } });
  };

  const onFillField = (data, name) => manageIntegrationsDispatch({ type: 'ON_FILL', payload: { data, name } });

  const onResetParameterInput = () => {
    manageIntegrationsDispatch({ type: 'ON_RESET_PARAMETER', payload: { key: '', value: '' } });
  };

  const onSaveParameter = () => (editorView.isEditing ? onUpdateParameter() : onAddParameter());

  const onToggleEditorView = (id, option) => {
    if (!editorView.isEditing) {
      manageIntegrationsDispatch({
        type: 'MANAGE_PARAMETERS',
        payload: { data: ManageIntegrationsUtils.toggleParameterEditorView(id, option, externalParameters) }
      });
    }
  };

  const onUpdateParameter = () => {
    onResetParameterInput();

    manageIntegrationsDispatch({
      type: 'MANAGE_PARAMETERS',
      payload: { data: ManageIntegrationsUtils.onUpdateCompleteParameter(editorView.id, manageIntegrationsState) }
    });
  };

  const onUpdateIntegration = async () => {
    try {
      await IntegrationService.update(manageIntegrationsState);
    } catch (error) {
      console.log('error', error);
    }
  };

  const renderDialogFooter = (
    <Fragment>
      <span data-tip data-for="integrationTooltip">
        <Button
          className="p-button-rounded p-button-animated-blink"
          icon="check"
          // disabled={ManageIntegrationsUtils.checkEmptyForm(manageIntegrationsState).includes(true)}
          label={resources.messages['create']}
          onClick={() => (!isEmpty(updatedData) ? onUpdateIntegration() : onCreateIntegration())}
        />
      </span>
      <Button
        className="p-button-secondary p-button-rounded  p-button-animated-blink"
        icon="cancel"
        label={resources.messages['cancel']}
        onClick={() => manageDialogs('isIntegrationManageDialogVisible', false, 'isIntegrationListDialogVisible', true)}
      />

      <ReactTooltip className={styles.tooltipClass} effect="solid" id="integrationTooltip" place="top">
        {resources.messages['fcSubmitButtonDisabled']}
      </ReactTooltip>
    </Fragment>
  );

  const renderDialogLayout = children => (
    <Dialog
      footer={renderDialogFooter}
      header={'Create'}
      onHide={() => manageDialogs('isIntegrationManageDialogVisible', false, 'isIntegrationListDialogVisible', true)}
      style={{ width: '975px' }}
      visible={isIntegrationManageDialogVisible}>
      {children}
    </Dialog>
  );

  const renderDropdownLayout = option => (
    <div className={`${styles.field} ${styles[option]} formField`}>
      <label htmlFor={`${componentName}__${option}`}>{resources.messages[option]}</label>
      <Dropdown
        appendTo={document.body}
        inputId={`${componentName}__${option}`}
        placeholder={resources.messages[option]}
        onChange={event => onFillField(event.value.value, option)}
        optionLabel="label"
        options={[
          { label: 'IMPORT', value: 'IMPORT' },
          { label: 'EXPORT', value: 'EXPORT' }
        ]}
        value={manageIntegrationsState[option]}
      />
    </div>
  );

  const renderEditorInput = (option, parameter, id) => {
    return (
      <InputText
        className={styles.editorInput}
        onBlur={event => {
          ManageIntegrationsUtils.onUpdateData(id, option, externalParameters, event.target.value);
          onToggleEditorView(id, [option]);
        }}
        onChange={event => onChangeParameter(event.target.value, option, id)}
        onFocus={() => {}}
        onKeyDown={() => {}}
        ref={parameterRef}
        value={parameter[option]}
      />
    );
  };

  const renderInputLayout = (options = []) => {
    return options.map((option, index) => (
      <div className={`${styles.field} ${styles[option]} formField`} key={index}>
        <label htmlFor={`${componentName}__${option}`}>{resources.messages[option]}</label>
        <InputText
          id={`${componentName}__${option}`}
          onChange={event => onFillField(event.target.value, option)}
          placeholder={resources.messages[option]}
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
          <Button
            className="p-button-rounded p-button-animated-blink"
            disabled={isEmpty(manageIntegrationsState.parameterKey) || isEmpty(manageIntegrationsState.parameterValue)}
            icon="add"
            label={editorView.isEditing ? resources.messages['update'] : resources.messages['add']}
            onClick={() => onSaveParameter()}
          />
          {editorView.isEditing && (
            <Button
              className="p-button-secondary p-button-rounded p-button-animated-blink"
              icon={'cancel'}
              label={resources.messages['cancel']}
              onClick={() => onResetParameterInput()}
            />
          )}
        </span>
      </div>
      <div className={styles.group}>
        <span className={styles.parameters}>{renderParametersLayout()}</span>
      </div>
    </div>
  );
};
