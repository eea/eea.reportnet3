import React, { useEffect, useReducer, useContext, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import pull from 'lodash/pull';

import styles from './CreateValidation.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
import { Dialog } from 'ui/views/_components/Dialog';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
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

  const ruleDisablingCheckListener = [creationFormState.candidateRule.table, creationFormState.candidateRule.field];
  const ruleAdditionCheckListener = [creationFormState.areRulesDisabled, creationFormState.candidateRule];

  const componentName = 'createValidation';

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
    const dropdownOptions = {
      disabled: true,
      placeholder: resourcesContext.messages.field,
      options: [],
      onChange: () => {},
      value: null
    };
    if (isNil(tableFields)) {
      dropdownOptions.value = null;
    }
    if (!isNil(tableFields) && tableFields.length == 0) {
      dropdownOptions.placeholder = resourcesContext.messages.designSchemaTabNoFields;
      dropdownOptions.value = null;
    }
    if (!isNil(tableFields) && tableFields.length > 0) {
      dropdownOptions.options = tableFields;
      dropdownOptions.disabled = false;
      dropdownOptions.onChange = e =>
        creationFormDispatch({
          type: 'SET_FORM_FIELD',
          payload: {
            key: 'field',
            value: e.target.value
          }
        });
      dropdownOptions.value = creationFormState.candidateRule.field;
    }
    setfieldsDropdown(
      <Dropdown
        id={`${componentName}__field`}
        disabled={dropdownOptions.disabled}
        appendTo={document.body}
        filterPlaceholder={dropdownOptions.placeholder}
        placeholder={dropdownOptions.placeholder}
        optionLabel="label"
        options={dropdownOptions.options}
        onChange={dropdownOptions.onChange}
        value={dropdownOptions.value}
      />
    );
  }, [
    creationFormState.tableFields,
    validationContext.isVisible,
    creationFormState.candidateRule.field,
    creationFormState.candidateRule.table
  ]);

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
      candidateRule: { expressions, allExpressions }
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

  const dialogLayout = children => (
    <Dialog
      header="Create Field Validation rule"
      visible={validationContext.isVisible}
      style={{ width: '90%' }}
      onHide={e => onHide()}>
      {children}
    </Dialog>
  );

  return dialogLayout(
    <form>
      <div id={styles.QCFormWrapper}>
        <div className={styles.body}>
          <div className={styles.section}>
            <div className={styles.subsection}>
              <div className={styles.field}>
                <label htmlFor="table">{resourcesContext.messages.table}</label>
                <Dropdown
                  id={`${componentName}__table`}
                  appendTo={document.body}
                  filterPlaceholder={resourcesContext.messages.table}
                  placeholder={resourcesContext.messages.table}
                  optionLabel="label"
                  options={creationFormState.schemaTables}
                  onChange={e =>
                    creationFormDispatch({
                      type: 'SET_FORM_FIELD',
                      payload: {
                        key: 'table',
                        value: e.target.value
                      }
                    })
                  }
                  value={creationFormState.candidateRule.table}
                />
              </div>
              <div className={styles.field}>
                <label htmlFor="field">{resourcesContext.messages.field}</label>
                {fieldsDropdown}
              </div>
              <div className={styles.field}>
                <label htmlFor="shortCode">{resourcesContext.messages.ruleShortCode}</label>
                <InputText
                  id={`${componentName}__shortCode`}
                  placeholder={resourcesContext.messages.ruleShortCode}
                  value={creationFormState.candidateRule.shortCode}
                  onChange={e =>
                    creationFormDispatch({
                      type: 'SET_FORM_FIELD',
                      payload: {
                        key: 'shortCode',
                        value: e.target.value
                      }
                    })
                  }
                />
              </div>
              <div className={styles.field}>
                <label htmlFor="name">{resourcesContext.messages.ruleName}</label>
                <InputText
                  id={`${componentName}__name`}
                  placeholder={resourcesContext.messages.ruleName}
                  value={creationFormState.candidateRule.name}
                  onChange={e =>
                    creationFormDispatch({
                      type: 'SET_FORM_FIELD',
                      payload: {
                        key: 'name',
                        value: e.target.value
                      }
                    })
                  }
                />
              </div>
              <div className={styles.field}>
                <label htmlFor="description">{resourcesContext.messages.description}</label>
                <InputText
                  id={`${componentName}__description`}
                  placeholder={resourcesContext.messages.description}
                  value={creationFormState.candidateRule.description}
                  onChange={e =>
                    creationFormDispatch({
                      type: 'SET_FORM_FIELD',
                      payload: {
                        key: 'description',
                        value: e.target.value
                      }
                    })
                  }
                />
              </div>
              <div className={`${styles.field} ${styles.errorMessage}`}>
                <label htmlFor="errorMessage">{resourcesContext.messages.ruleErrorMessage}</label>
                <InputText
                  id={`${componentName}__errorMessage`}
                  placeholder={resourcesContext.messages.ruleErrorMessage}
                  value={creationFormState.candidateRule.errorMessage}
                  onChange={e =>
                    creationFormDispatch({
                      type: 'SET_FORM_FIELD',
                      payload: {
                        key: 'errorMessage',
                        value: e.target.value
                      }
                    })
                  }
                />
              </div>
              <div className={styles.field}>
                <label htmlFor="description">{resourcesContext.messages.errorType}</label>
                <Dropdown
                  id={`${componentName}__errorType`}
                  filterPlaceholder={resourcesContext.messages.errorType}
                  placeholder={resourcesContext.messages.errorType}
                  appendTo={document.body}
                  optionLabel="label"
                  options={creationFormState.errorLevels}
                  onChange={e =>
                    creationFormDispatch({
                      type: 'SET_FORM_FIELD',
                      payload: {
                        key: 'errorLevel',
                        value: e.target.value
                      }
                    })
                  }
                  value={creationFormState.candidateRule.errorLevel}
                />
              </div>
            </div>
            <div className={styles.subsection}>
              <div className={`${styles.field} ${styles.qcActive}`}>
                <label htmlFor="QcActive">{resourcesContext.messages.active}</label>
                <Checkbox
                  id={`${componentName}__active`}
                  onChange={e =>
                    creationFormDispatch({
                      type: 'SET_FORM_FIELD',
                      payload: { key: 'active', value: e.checked }
                    })
                  }
                  isChecked={creationFormState.candidateRule.active}
                />
              </div>
            </div>
          </div>
          <div className={styles.section}>
            <ul>
              <li className={styles.expressionsHeader}>
                <span>{resourcesContext.messages.group}</span>
                <span>{resourcesContext.messages.andor}</span>
                <span>{resourcesContext.messages.operatorType}</span>
                <span>{resourcesContext.messages.operator}</span>
                <span>{resourcesContext.messages.value}</span>
              </li>
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
                  />
                ))}
            </ul>
          </div>

          {creationFormState.groupExpressionsActive >= 2 && (
            <div className={styles.section}>
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
                        allExpressions: [...creationFormState.candidateRule.allExpressions, groupingResult.newGroup]
                      }
                    });
                }}
              />
            </div>
          )}
          <div className={styles.section}>
            <textarea
              name=""
              id=""
              cols="30"
              readOnly
              rows="5"
              value={creationFormState.validationRuleString}></textarea>
          </div>
        </div>
        <div className={styles.footer}>
          <div className={`${styles.section} ${styles.footerToolBar}`}>
            <div className={styles.subsection}>
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
            </div>
            <div className={styles.subsection}>
              {validationContext.ruleEdit ? (
                <Button
                  id={`${componentName}__update`}
                  disabled={creationFormState.isValidationCreationDisabled || isSubmitDisabled}
                  className="p-button-primary p-button-text-icon-left"
                  type="button"
                  label={resourcesContext.messages.update}
                  icon={isSubmitDisabled ? 'spinnerAnimate' : 'check'}
                  onClick={e => onUpdateValidationRule()}
                />
              ) : (
                <Button
                  id={`${componentName}__create`}
                  disabled={creationFormState.isValidationCreationDisabled || isSubmitDisabled}
                  className="p-button-primary p-button-text-icon-left"
                  type="button"
                  label={resourcesContext.messages.create}
                  icon={isSubmitDisabled ? 'spinnerAnimate' : 'check'}
                  onClick={e => onCreateValidationRule()}
                />
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
  );
};

export { CreateValidation };
