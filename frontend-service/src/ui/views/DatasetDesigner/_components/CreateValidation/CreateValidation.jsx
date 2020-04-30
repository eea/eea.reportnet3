import React, { Fragment, useEffect, useReducer, useContext, useState, useRef } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import pull from 'lodash/pull';
import pick from 'lodash/pick';

import styles from './CreateValidation.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
import { Dialog } from 'ui/views/_components/Dialog';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import ReactTooltip from 'react-tooltip';
import { TabMenu } from 'primereact/tabmenu';
import { ValidationExpressionSelector } from './_components/ValidationExpressionSelector';

import { ValidationService } from 'core/services/Validation';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

import {
  createValidationReducerInitState,
  createValidationReducer
} from './_functions/reducers/CreateValidationReducer';

import { checkExpressions } from './_functions/utils/checkExpressions';
import { checkValidation } from './_functions/utils/checkValidation';
import { deleteExpression } from './_functions/utils/deleteExpression';
import { deleteExpressionRecursivily } from './_functions/utils/deleteExpressionRecursivily';
import { getDatasetSchemaTableFields } from './_functions/utils/getDatasetSchemaTableFields';
import { getEmptyExpression } from './_functions/utils/getEmptyExpression';
import { getExpressionString } from './_functions/utils/getExpressionString';
import { getSelectedFieldById } from './_functions/utils/getSeletedFieldById';
import { getSelectedTableByFieldId } from './_functions/utils/getSelectedTablebyFieldId';
import { getSelectedTableBytableSchemaId } from './_functions/utils/getSelectedTableBytableSchemaId';
import { groupExpressions } from './_functions/utils/groupExpressions';
import { initValidationRuleCreation } from './_functions/utils/initValidationRuleCreation';
import { resetValidationRuleCreation } from './_functions/utils/resetValidationRuleCreation';
import { setValidationExpression } from './_functions/utils/setValidationExpression';

const CreateValidation = ({ toggleVisibility, datasetId, tabs }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const validationContext = useContext(ValidationContext);

  const [creationFormState, creationFormDispatch] = useReducer(
    createValidationReducer,
    createValidationReducerInitState
  );
  const [fieldsDropdown, setfieldsDropdown] = useState();
  const [isSubmitDisabled, setIsSubmitDisabled] = useState(false);
  const [tableFieldOptions, setTableFieldOptions] = useState({
    disabled: true,
    placeholder: resourcesContext.messages.fieldConstraintTableFieldNoOptions
  });
  const [tabMenuItems] = useState([
    {
      // label: resources.messages['dataflowAcceptedPendingTab'],
      label: resourcesContext.messages.tabMenuConstraintData,
      className: styles.flow_tab,
      tabKey: 'data'
    },
    {
      label: resourcesContext.messages.tabMenuExpression,
      className: styles.flow_tab,
      tabKey: 'expression'
    }
  ]);
  const [tabMenuActiveItem, setTabMenuActiveItem] = useState(tabMenuItems[0]);
  const [tabsChanges, setTabsChanges] = useState({});
  const [clickedFields, setClickedFields] = useState([]);

  const ruleDisablingCheckListener = [creationFormState.candidateRule.table, creationFormState.candidateRule.field];
  const ruleAdditionCheckListener = [creationFormState.areRulesDisabled, creationFormState.candidateRule];

  const componentName = 'createValidation';

  useEffect(() => {
    const tabsKeys = tabMenuItems.map(tabMenu => {
      return pick(tabMenu, ['tabKey']);
    });
    const tabChangesInitValues = {};
    tabsKeys.forEach(tab => {
      tabChangesInitValues[tab.tabKey] = false;
    });

    setTabsChanges(tabChangesInitValues);
  }, []);

  useEffect(() => {
    if (!isEmpty(tabs)) {
      creationFormDispatch({ type: 'INIT_FORM', payload: initValidationRuleCreation(tabs) });
    }
  }, [tabs]);

  useEffect(() => {
    const { table } = creationFormState.candidateRule;
    if (!isEmpty(table)) {
      creationFormDispatch({
        type: 'SET_FIELDS',
        payload: getDatasetSchemaTableFields(table, tabs)
      });
    }

    if (validationContext.fieldId) {
      creationFormDispatch({
        type: 'SET_FORM_FIELD',
        payload: {
          key: 'field',
          value: getSelectedFieldById(validationContext.fieldId, tabs)
        }
      });
    }
  }, [creationFormState.candidateRule.table]);

  useEffect(() => {
    let table = null;
    if (validationContext.fieldId) {
      if (!isNil(validationContext.tableSchemaId)) {
        table = getSelectedTableBytableSchemaId(validationContext.tableSchemaId, tabs);
      } else {
        table = getSelectedTableByFieldId(validationContext.fieldId, tabs);
      }
      creationFormDispatch({
        type: 'SET_FORM_FIELD',
        payload: {
          key: 'table',
          value: table
        }
      });
    }
  }, [validationContext.fieldId]);

  useEffect(() => {
    const {
      candidateRule: { field, expressions }
    } = creationFormState;

    creationFormDispatch({
      type: 'SET_EXPRESSIONS_STRING',
      payload: getExpressionString(expressions, field)
    });
  }, [creationFormState.candidateRule]);

  useEffect(() => {
    if (checkActivateRules()) {
      creationFormDispatch({
        type: 'SET_ARE_RULES_DISABLED',
        payload: false
      });
    } else if (checkDesactivateRules()) {
      creationFormDispatch({
        type: 'SET_ARE_RULES_DISABLED',
        payload: true
      });
    }
  }, [...ruleDisablingCheckListener]);

  useEffect(() => {
    const {
      candidateRule: { expressions }
    } = creationFormState;
    creationFormDispatch({
      type: 'SET_IS_VALIDATION_ADDING_DISABLED',
      payload: checkExpressions(expressions)
    });
  }, [...ruleAdditionCheckListener]);

  useEffect(() => {
    creationFormDispatch({
      type: 'SET_IS_VALIDATION_CREATION_DISABLED',
      payload: !checkValidation(creationFormState.candidateRule)
    });
  }, [creationFormState.candidateRule]);

  useEffect(() => {
    if (validationContext.ruleEdit && !isEmpty(validationContext.ruleToEdit)) {
      creationFormDispatch({
        type: 'POPULATE_CREATE_FORM',
        payload: validationContext.ruleToEdit
      });
    }
  }, [validationContext.ruleEdit]);

  useEffect(() => {
    const { tableFields } = creationFormState;
    const fieldDropdownOptions = {
      disabled: true,
      placeholder: resourcesContext.messages.field,
      options: [],
      onChange: () => {},
      value: null
    };
    if (isNil(tableFields)) {
      fieldDropdownOptions.value = null;
    }
    if (!isNil(tableFields) && tableFields.length == 0) {
      fieldDropdownOptions.placeholder = resourcesContext.messages.designSchemaTabNoFields;
      fieldDropdownOptions.value = null;
    }
    if (!isNil(tableFields) && tableFields.length > 0) {
      fieldDropdownOptions.options = tableFields;
      fieldDropdownOptions.disabled = false;
      fieldDropdownOptions.onChange = e => onInfoFieldChange('field', e.target.value);
      fieldDropdownOptions.value = creationFormState.candidateRule.field;
    }
    setfieldsDropdown(
      <Dropdown
        id={`${componentName}__field`}
        disabled={fieldDropdownOptions.disabled}
        appendTo={document.body}
        filterPlaceholder={fieldDropdownOptions.placeholder}
        placeholder={fieldDropdownOptions.placeholder}
        optionLabel="label"
        options={fieldDropdownOptions.options}
        onChange={fieldDropdownOptions.onChange}
        value={fieldDropdownOptions.value}
      />
    );
  }, [
    creationFormState.tableFields,
    validationContext.isVisible,
    creationFormState.candidateRule.field,
    creationFormState.candidateRule.table
  ]);

  useEffect(() => {
    if (creationFormState.schemaTables.length > 0) {
      setTableFieldOptions({
        disabled: false,
        placeholder: resourcesContext.messages.table
      });
    } else {
      setTableFieldOptions({
        disabled: true,
        placeholder: resourcesContext.messages.fieldConstraintTableFieldNoOptions
      });
    }
  }, [creationFormState.schemaTables]);

  const checkActivateRules = () => {
    return creationFormState.candidateRule.table && creationFormState.candidateRule.field;
  };

  const checkDesactivateRules = () => {
    return (
      (!creationFormState.candidateRule.table || !creationFormState.candidateRule.field) &&
      !creationFormState.areRulesDisabled
    );
  };

  const onCreateValidationRule = async () => {
    try {
      setIsSubmitDisabled(true);
      const { candidateRule } = creationFormState;
      await ValidationService.create(datasetId, candidateRule);
      onHide();
    } catch (error) {
      notificationContext.add({
        type: 'QC_RULE_CREATION_ERROR'
      });
      console.error('onCreateValidationRule error', error);
    } finally {
      setIsSubmitDisabled(false);
    }
  };

  const onUpdateValidationRule = async () => {
    try {
      setIsSubmitDisabled(true);
      const { candidateRule } = creationFormState;
      await ValidationService.update(datasetId, candidateRule);
      onHide();
    } catch (error) {
      notificationContext.add({
        type: 'QC_RULE_UPDATING_ERROR'
      });
      console.error('onUpdateValidationRule error', error);
    } finally {
      setIsSubmitDisabled(false);
    }
  };

  const onExpressionDelete = expressionId => {
    const {
      candidateRule: { expressions, allExpressions }
    } = creationFormState;
    const parsedExpressions = deleteExpressionRecursivily(expressionId, expressions);
    const parsedAllExpressions = deleteExpression(expressionId, allExpressions);
    creationFormDispatch({
      type: 'UPDATE_RULES',
      payload: parsedAllExpressions
    });
    creationFormDispatch({
      type: 'UPDATE_EXPRESSIONS_TREE',
      payload: parsedExpressions
    });
  };

  const onExpressionFieldUpdate = (expressionId, field) => {
    const {
      candidateRule: { allExpressions }
    } = creationFormState;
    creationFormDispatch({
      type: 'UPDATE_RULES',
      payload: setValidationExpression(expressionId, field, allExpressions)
    });
  };

  const onExpressionGroup = (expressionId, field) => {
    const {
      groupCandidate,
      candidateRule: { allExpressions }
    } = creationFormState;

    const [currentExpression] = allExpressions.filter(expression => expression.expressionId == expressionId);
    currentExpression[field.key] = field.value;

    if (field.value) {
      groupCandidate.push(expressionId);
    } else {
      pull(groupCandidate, expressionId);
    }
    creationFormDispatch({
      type: 'GROUP_RULES_ACTIVATOR',
      payload: {
        groupExpressionsActive: field.value ? 1 : -1,
        groupCandidate,
        allExpressions
      }
    });
  };

  const onHide = () => {
    creationFormDispatch({ type: 'RESET_CREATION_FORM', payload: resetValidationRuleCreation() });
    validationContext.onCloseModal();
    toggleVisibility(false);
  };

  const onTabChange = tab => {
    setTabsChanges({
      ...tabsChanges,
      [tabMenuActiveItem.tabKey]: true
    });
    setTabMenuActiveItem(tab);
  };

  const onInfoFieldChange = (fieldKey, fieldValue) => {
    onDeleteFromClickedFields(fieldKey);
    creationFormDispatch({
      type: 'SET_FORM_FIELD',
      payload: {
        key: fieldKey,
        value: fieldValue
      }
    });
  };

  const onAddToClickedFields = field => {
    const cClickedFields = [...clickedFields];
    if (!cClickedFields.includes(field)) {
      cClickedFields.push(field);
      setClickedFields(cClickedFields);
    }
  };
  const onDeleteFromClickedFields = field => {
    const cClickedFields = [...clickedFields];
    if (cClickedFields.includes(field)) {
      cClickedFields.splice(cClickedFields.indexOf(field), 1);
      setClickedFields(cClickedFields);
    }
  };

  const printError = field => {
    return (tabsChanges.data || clickedFields.includes(field)) && isEmpty(creationFormState.candidateRule[field])
      ? 'error'
      : '';
  };

  const dialogLayout = children => (
    <Dialog
      header={
        validationContext.ruleEdit
          ? resourcesContext.messages.editFieldConstraint
          : resourcesContext.messages.createFieldConstraintTitle
      }
      visible={validationContext.isVisible}
      style={{ width: '975px' }}
      onHide={e => onHide()}>
      {children}
    </Dialog>
  );

  return dialogLayout(
    <>
      <TabMenu model={tabMenuItems} activeItem={tabMenuActiveItem} onTabChange={e => onTabChange(e.value)} />
      <form>
        <div id={styles.QCFormWrapper}>
          <div className={styles.body}>
            {tabMenuActiveItem.tabKey == 'data' && (
              <div className={styles.section}>
                <fieldset>
                  <div
                    onBlur={e => onAddToClickedFields('table')}
                    className={`${styles.field} ${styles.qcTable} formField ${printError('table')}`}>
                    <label htmlFor="table">{resourcesContext.messages.table}</label>
                    <Dropdown
                      id={`${componentName}__table`}
                      disabled={tableFieldOptions.disabled}
                      appendTo={document.body}
                      filterPlaceholder={resourcesContext.messages.table}
                      placeholder={tableFieldOptions.placeholder}
                      optionLabel="label"
                      options={creationFormState.schemaTables}
                      value={creationFormState.candidateRule.table}
                      onChange={e => onInfoFieldChange('table', e.target.value)}
                    />
                  </div>
                  <div
                    onBlur={e => onAddToClickedFields('field')}
                    className={`${styles.field} ${styles.qcField} formField ${printError('field')}`}>
                    <label htmlFor="field">{resourcesContext.messages.field}</label>
                    {fieldsDropdown}
                  </div>
                  <div
                    onBlur={e => onAddToClickedFields('shortCode')}
                    className={`${styles.field} ${styles.qcShortCode} formField ${printError('shortCode')}`}>
                    <label htmlFor="shortCode">{resourcesContext.messages.ruleShortCode}</label>
                    <InputText
                      id={`${componentName}__shortCode`}
                      placeholder={resourcesContext.messages.ruleShortCode}
                      value={creationFormState.candidateRule.shortCode}
                      onChange={e => onInfoFieldChange('shortCode', e.target.value)}
                    />
                  </div>
                  <div className={`${styles.field} ${styles.qcEnabled} formField `}>
                    <label htmlFor="QcActive">{resourcesContext.messages.enabled}</label>
                    <Checkbox
                      id={`${componentName}__active`}
                      onChange={e => onInfoFieldChange('active', e.checked)}
                      isChecked={creationFormState.candidateRule.active}
                    />
                  </div>
                </fieldset>
                <fieldset>
                  <div
                    onBlur={e => onAddToClickedFields('name')}
                    className={`${styles.field} ${styles.qcName} formField ${printError('name')}`}>
                    <label htmlFor="name">{resourcesContext.messages.ruleName}</label>
                    <InputText
                      id={`${componentName}__name`}
                      placeholder={resourcesContext.messages.ruleName}
                      value={creationFormState.candidateRule.name}
                      onChange={e => onInfoFieldChange('name', e.target.value)}
                    />
                  </div>
                  <div className={`${styles.field} ${styles.qcDescription} formField`}>
                    <label htmlFor="description">{resourcesContext.messages.description}</label>
                    <InputText
                      id={`${componentName}__description`}
                      placeholder={resourcesContext.messages.description}
                      value={creationFormState.candidateRule.description}
                      onChange={e => onInfoFieldChange('description', e.target.value)}
                    />
                  </div>
                </fieldset>
                <fieldset>
                  <div
                    onBlur={e => onAddToClickedFields('errorLevel')}
                    className={`${styles.field} ${styles.qcErrorType} formField ${printError('errorLevel')}`}>
                    <label htmlFor="errorType">{resourcesContext.messages.errorType}</label>
                    <Dropdown
                      id={`${componentName}__errorType`}
                      filterPlaceholder={resourcesContext.messages.errorType}
                      placeholder={resourcesContext.messages.errorType}
                      appendTo={document.body}
                      optionLabel="label"
                      options={creationFormState.errorLevels}
                      onChange={e => onInfoFieldChange('errorLevel', e.target.value)}
                      value={creationFormState.candidateRule.errorLevel}
                    />
                  </div>
                  <div
                    onBlur={e => onAddToClickedFields('errorMessage')}
                    className={`${styles.field} ${styles.qcErrorMessage} formField ${printError('errorMessage')}`}>
                    <label htmlFor="errorMessage">{resourcesContext.messages.ruleErrorMessage}</label>
                    <InputText
                      id={`${componentName}__errorMessage`}
                      placeholder={resourcesContext.messages.ruleErrorMessage}
                      value={creationFormState.candidateRule.errorMessage}
                      onChange={e => onInfoFieldChange('errorMessage', e.target.value)}
                    />
                  </div>
                </fieldset>
              </div>
            )}
            {tabMenuActiveItem.tabKey == 'expression' && (
              <>
                <div className={styles.section}>
                  <ul>
                    {creationFormState.candidateRule.expressions &&
                      creationFormState.candidateRule.expressions.map((expression, i) => (
                        <ValidationExpressionSelector
                          expressionValues={expression}
                          isDisabled={creationFormState.areRulesDisabled}
                          key={expression.expressionId}
                          onExpressionDelete={onExpressionDelete}
                          onExpressionFieldUpdate={onExpressionFieldUpdate}
                          onExpressionGroup={onExpressionGroup}
                          position={i}
                          showRequiredFields={tabsChanges.expression}
                        />
                      ))}
                  </ul>
                  <div className={styles.expressionsActionsBtns}>
                    <Button
                      id={`${componentName}__addExpresion`}
                      disabled={creationFormState.isRuleAddingDisabled}
                      className="p-button-primary p-button-text-icon-left"
                      type="button"
                      label={resourcesContext.messages.addNewRule}
                      icon="plus"
                      onClick={e =>
                        creationFormDispatch({
                          type: 'ADD_EMPTY_RULE',
                          payload: getEmptyExpression()
                        })
                      }
                    />
                    {creationFormState.groupExpressionsActive >= 2 && (
                      <Button
                        id={`${componentName}__groupExpresions`}
                        className="p-button-primary p-button-text"
                        type="button"
                        label="Group"
                        icon="plus"
                        onClick={e => {
                          const groupingResult = groupExpressions(
                            creationFormState.candidateRule.expressions,
                            creationFormState.groupExpressionsActive,
                            creationFormState.groupCandidate
                          );
                          if (!isNil(groupingResult.newGroup))
                            creationFormDispatch({
                              type: 'GROUP_EXPRESSIONS',
                              payload: {
                                expressions: groupingResult.expressions,
                                allExpressions: [
                                  ...creationFormState.candidateRule.allExpressions,
                                  groupingResult.newGroup
                                ]
                              }
                            });
                        }}
                      />
                    )}
                  </div>
                </div>
                <div className={styles.section}>
                  <textarea
                    name=""
                    id=""
                    cols="30"
                    readOnly
                    rows="5"
                    value={creationFormState.validationRuleString}></textarea>
                </div>
              </>
            )}
          </div>
          <div className={styles.footer}>
            <div className={`${styles.section} ${styles.footerToolBar}`}>
              <div className={styles.subsection}>
                {validationContext.ruleEdit ? (
                  <span data-tip data-for="createTooltip">
                    <Button
                      id={`${componentName}__update`}
                      disabled={creationFormState.isValidationCreationDisabled || isSubmitDisabled}
                      className="p-button-primary p-button-text-icon-left"
                      type="button"
                      label={resourcesContext.messages.update}
                      icon={isSubmitDisabled ? 'spinnerAnimate' : 'check'}
                      onClick={e => onUpdateValidationRule()}
                    />
                  </span>
                ) : (
                  <span data-tip data-for="createTooltip">
                    <Button
                      id={`${componentName}__create`}
                      disabled={creationFormState.isValidationCreationDisabled || isSubmitDisabled}
                      className="p-button-primary p-button-text-icon-left"
                      type="button"
                      label={resourcesContext.messages.create}
                      icon={isSubmitDisabled ? 'spinnerAnimate' : 'check'}
                      onClick={e => onCreateValidationRule()}
                    />
                  </span>
                )}
                {(creationFormState.isValidationCreationDisabled || isSubmitDisabled) && (
                  <ReactTooltip className={styles.tooltipClass} effect="solid" id="createTooltip" place="top">
                    <span>{resourcesContext.messages.fcSubmitButtonDisabled}</span>
                  </ReactTooltip>
                )}

                <Button
                  id={`${componentName}__cancel`}
                  className="p-button-secondary p-button-text-icon-left"
                  type="button"
                  label={resourcesContext.messages.cancel}
                  icon="cancel"
                  onClick={e => onHide()}
                />
              </div>
            </div>
          </div>
        </div>
      </form>
    </>
  );
};

export { CreateValidation };
