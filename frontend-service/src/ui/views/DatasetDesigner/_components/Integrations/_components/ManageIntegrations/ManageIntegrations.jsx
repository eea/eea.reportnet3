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

export const ManageIntegrations = ({ designerState, manageDialogs }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const parameterRef = useRef(null);

  const { datasetSchemaId, isIntegrationManageDialogVisible } = designerState;

  const [manageIntegrationsState, manageIntegrationsDispatch] = useReducer(manageIntegrationsReducer, {
    datasetSchemaId,
    description: '',
    externalParameters: [],
    fileExtension: '',
    editorView: { isEditing: false, id: null },
    name: '',
    operation: '',
    parameterKey: '',
    parameterValue: '',
    processName: '',
    tool: 'FME'
  });

  const componentName = 'integration';

  const { editorView, externalParameters } = manageIntegrationsState;

  const getOptions = {
    operationOptions: [
      { label: 'IMPORT', value: 'IMPORT' },
      { label: 'EXPORT', value: 'EXPORT' }
    ],
    toolOptions: [{ label: 'FME', value: 'FME' }]
  };

  useEffect(() => {
    if (parameterRef.current) {
      parameterRef.current.element.focus();
    }
  }, [parameterRef.current]);

  const onCreateIntegration = async () => {
    try {
      await IntegrationService.create(manageIntegrationsState);
    } catch (error) {
      notificationContext.add({ type: 'CREATE_INTEGRATION_ERROR' });
    }
  };

  const onDeleteParameter = id => {
    const data = externalParameters.filter(parameter => parameter.id !== id);

    manageIntegrationsDispatch({ type: 'ON_DELETE_PARAMETER', payload: { data } });
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

  const onAddParameter = () => {
    const data = ManageIntegrationsUtils.onAddParameter(manageIntegrationsState);

    manageIntegrationsDispatch({ type: 'ON_ADD_PARAMETER', payload: { data } });
  };

  const onUpdate = () => {
    const data = ManageIntegrationsUtils.onUpdateCompleteParameter(editorView.id, manageIntegrationsState);
    onResetParameterInput();

    manageIntegrationsDispatch({ type: 'ON_SAVE_PARAMETER', payload: { data } });
  };

  const onSaveParameter = () => (editorView.isEditing ? onUpdate() : onAddParameter());

  const onToggleEditorView = (id, option) => {
    if (!editorView.isEditing) {
      const data = externalParameters.map(parameter => {
        if (parameter.id === id) {
          Object.assign({}, parameter, (parameter.isEditorView[option] = !parameter.isEditorView[option]));
          return parameter;
        } else return parameter;
      });

      manageIntegrationsDispatch({ type: 'ON_TOGGLE_EDITOR_VIEW', payload: { data } });
    }
  };

  const onUpdateParameter = (value, option, id) => {
    const data = ManageIntegrationsUtils.onUpdateData(id, option, externalParameters, value);

    manageIntegrationsDispatch({ type: 'ON_UPDATE_PARAMETER', payload: { data } });
  };

  const renderDialogFooter = (
    <Fragment>
      <span data-tip data-for="integrationTooltip">
        <Button
          className="p-button-rounded p-button-animated-blink"
          icon="add"
          // disabled={ManageIntegrationsUtils.checkEmptyForm(manageIntegrationsState).includes(true)}
          label={resources.messages['save']}
          onClick={() => onCreateIntegration()}
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

  const renderDialogLayout = children =>
    isIntegrationManageDialogVisible && (
      <Dialog
        footer={renderDialogFooter}
        header={'Create'}
        onHide={() => manageDialogs('isIntegrationManageDialogVisible', false, 'isIntegrationListDialogVisible', true)}
        style={{ width: '975px' }}
        visible={isIntegrationManageDialogVisible}>
        {children}
      </Dialog>
    );

  const renderDropdownLayout = (options = []) => {
    return options.map((option, index) => (
      <div className={`${styles.field} ${styles[option]} formField`} key={index}>
        <Dropdown
          appendTo={document.body}
          disabled={option === 'tool'}
          inputClassName={`p-float-label`}
          inputId={`${componentName}__${option}`}
          label={'hola'}
          onChange={event => onFillField(event.value.value, option)}
          optionLabel="label"
          options={getOptions[`${option}Options`]}
          value={manageIntegrationsState[option]}
        />
      </div>
    ));
  };

  const renderEditorInput = (option, parameter, id) => {
    return (
      <InputText
        className={styles.editorInput}
        onBlur={event => {
          ManageIntegrationsUtils.onUpdateData(id, option, externalParameters, event.target.value);
          onToggleEditorView(id, [option]);
        }}
        onChange={event => onUpdateParameter(event.target.value, option, id)}
        onFocus={() => {}}
        onKeyDown={() => {}}
        ref={parameterRef}
        value={parameter[option]}
      />
    );
  };

  const renderInputLayout = (options = []) => {
    return options.map((option, index) => (
      <div className={`${styles.field} ${styles[option]} p-float-label formField`} key={index}>
        <InputText
          id={`${componentName}__${option}`}
          onChange={event => onFillField(event.target.value, option)}
          type="search"
          value={manageIntegrationsState[option]}
        />
        <label htmlFor={`${componentName}__${option}`}>{resources.messages[option]}</label>
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
        {renderDropdownLayout(['tool', 'operation'])}
        {renderInputLayout(['fileExtension'])}
      </div>
      <div className={styles.group}>
        {renderInputLayout(['parameterKey', 'parameterValue'])}
        <span className={styles.buttonWrapper}>
          <Button
            className="p-button-rounded  p-button-animated-blink"
            disabled={isEmpty(manageIntegrationsState.parameterKey) || isEmpty(manageIntegrationsState.parameterValue)}
            icon="add"
            label={editorView.isEditing ? resources.messages['update'] : resources.messages['add']}
            onClick={() => onSaveParameter()}
          />
          <Button
            className="p-button-secondary p-button-rounded  p-button-animated-blink"
            icon={editorView.isEditing ? 'cancel' : 'undo'}
            label={editorView.isEditing ? resources.messages['cancel'] : resources.messages['reset']}
            onClick={() => onResetParameterInput()}
          />
        </span>
      </div>
      <div className={styles.group}>
        <span className={styles.parameters}>{renderParametersLayout()}</span>
      </div>
    </div>
  );
};
