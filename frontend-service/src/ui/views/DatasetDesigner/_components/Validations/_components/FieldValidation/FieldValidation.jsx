import { Fragment, useContext, useEffect, useReducer, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import pull from 'lodash/pull';

import { config } from 'conf';

import styles from './FieldValidation.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { InfoTab } from 'ui/views/DatasetDesigner/_components/Validations/_components/InfoTab';

import { ExpressionSelector } from 'ui/views/DatasetDesigner/_components/Validations/_components/ExpressionSelector';
import { TabView, TabPanel } from 'primereact/tabview';
import ReactTooltip from 'react-tooltip';

import { ValidationService } from 'core/services/Validation';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

import {
  createValidationReducerInitState,
  createValidationReducer
} from 'ui/views/DatasetDesigner/_components/Validations/_functions/reducers/CreateValidationReducer';

import { checkExpressions } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/checkExpressions';
import { checkFieldValidation } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/checkFieldValidation';
import { deleteExpression } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/deleteExpression';
import { deleteExpressionRecursively } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/deleteExpressionRecursively';
import { getDatasetSchemaTableFields } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getDatasetSchemaTableFields';
import { getEmptyExpression } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getEmptyExpression';
import { getFieldExpressionString } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getFieldExpressionString';
import { getFieldType } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getFieldType';
import { getSelectedFieldById } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getSelectedFieldById';
import { getSelectedTableByFieldId } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getSelectedTablebyFieldId';
import { getSelectedTableByTableSchemaId } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getSelectedTableByTableSchemaId';
import { groupExpressions } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/groupExpressions';
import { initValidationRuleCreation } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/initValidationRuleCreation';
import { resetValidationRuleCreation } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/resetValidationRuleCreation';
import { setValidationExpression } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/setValidationExpression';

const FieldValidation = ({ datasetId, tabs }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const validationContext = useContext(ValidationContext);

  const [creationFormState, creationFormDispatch] = useReducer(
    createValidationReducer,
    createValidationReducerInitState
  );

  const [clickedFields, setClickedFields] = useState([]);
  const [expressionsErrors, setExpressionsErrors] = useState({});
  const [isSubmitDisabled, setIsSubmitDisabled] = useState(false);
  const [showErrorOnExpressionTab, setShowErrorOnExpressionTab] = useState(false);
  const [showErrorOnInfoTab, setShowErrorOnInfoTab] = useState(true);
  const [tabContents, setTabContents] = useState();
  const [tabMenuActiveItem, setTabMenuActiveItem] = useState(0);
  const [tabsChanges, setTabsChanges] = useState({});

  const ruleDisablingCheckListener = [creationFormState.candidateRule.table, creationFormState.candidateRule.field];
  const ruleAdditionCheckListener = [creationFormState.areRulesDisabled, creationFormState.candidateRule];

  const componentName = 'createValidation';

  useEffect(() => {
    if (!isEmpty(tabs) && isEmpty(creationFormState.candidateRule.expressionType)) {
      creationFormDispatch({ type: 'INIT_FORM', payload: initValidationRuleCreation(tabs) });
    }
  }, [tabs]);

  useEffect(() => {
    if (!creationFormState.candidateRule.automatic) {
      setTabContents([
        <TabPanel
          key="tab_1"
          header={resourcesContext.messages.tabMenuConstraintData}
          leftIcon={showErrorOnInfoTab ? 'pi pi-exclamation-circle' : ''}
          headerClassName={showErrorOnInfoTab ? styles.error : ''}>
          <InfoTab
            componentName={componentName}
            creationFormState={creationFormState}
            onAddToClickedFields={onAddToClickedFields}
            onDeleteFromClickedFields={onDeleteFromClickedFields}
            onInfoFieldChange={onInfoFieldChange}
            printError={printError}
          />
        </TabPanel>,
        <TabPanel
          key="tab_2"
          header={resourcesContext.messages.tabMenuExpression}
          leftIcon={showErrorOnExpressionTab ? 'pi pi-exclamation-circle' : ''}
          headerClassName={showErrorOnExpressionTab ? styles.error : ''}>
          <ExpressionSelector
            componentName={componentName}
            creationFormState={creationFormState}
            onAddNewExpression={onAddNewExpression}
            onExpressionDelete={onExpressionDelete}
            onExpressionFieldUpdate={onExpressionFieldUpdate}
            onExpressionGroup={onExpressionGroup}
            onExpressionMarkToGroup={onExpressionMarkToGroup}
            onExpressionsErrors={onExpressionsErrors}
            onExpressionTypeToggle={onExpressionTypeToggle}
            onGetFieldType={onGetFieldType}
            onSetSQLsentence={onSetSQLsentence}
            tabsChanges={tabsChanges}
          />
        </TabPanel>
      ]);
    } else {
      setTabContents([
        <TabPanel
          key="tab_1"
          header={resourcesContext.messages.tabMenuConstraintData}
          leftIcon={showErrorOnInfoTab ? 'pi pi-exclamation-circle' : ''}>
          <InfoTab
            componentName={componentName}
            creationFormState={creationFormState}
            onAddToClickedFields={onAddToClickedFields}
            onDeleteFromClickedFields={onDeleteFromClickedFields}
            onInfoFieldChange={onInfoFieldChange}
            printError={printError}
          />
        </TabPanel>
      ]);
    }
  }, [clickedFields, creationFormState, showErrorOnExpressionTab, showErrorOnInfoTab]);

  useEffect(() => {
    const { table } = creationFormState.candidateRule;
    if (!isEmpty(table)) {
      creationFormDispatch({
        type: 'SET_FIELDS',
        payload: getDatasetSchemaTableFields(table, tabs)
      });
    }

    if (validationContext.referenceId) {
      creationFormDispatch({
        type: 'SET_FORM_FIELD',
        payload: {
          key: 'field',
          value: getSelectedFieldById(validationContext.referenceId, tabs)
        }
      });
    }
  }, [creationFormState.candidateRule.table]);

  useEffect(() => {
    const expressionsKeys = Object.keys(expressionsErrors);
    let hasErrors = false;
    expressionsKeys.forEach(expressionKey => {
      if (expressionsErrors[expressionKey]) {
        hasErrors = true;
      }
    });
    if (hasErrors) {
      setShowErrorOnExpressionTab(true);
    } else {
      setShowErrorOnExpressionTab(false);
    }
  }, [expressionsErrors]);

  useEffect(() => {
    let table = null;

    if (validationContext.referenceId) {
      if (!isNil(validationContext.tableSchemaId)) {
        table = getSelectedTableByTableSchemaId(validationContext.tableSchemaId, tabs);
      } else {
        table = getSelectedTableByFieldId(validationContext.referenceId, tabs);
      }

      const fieldType = getFieldType(table, { code: validationContext.referenceId }, tabs);
      creationFormDispatch({
        type: 'SET_TABLE_ID_FIELD_ID_AND_FIELD_TYPE',
        payload: {
          field: validationContext.referenceId,
          fieldType,
          table
        }
      });
    }
  }, [validationContext.referenceId]);

  useEffect(() => {
    const {
      candidateRule: { field, expressions }
    } = creationFormState;

    creationFormDispatch({
      type: 'SET_EXPRESSIONS_STRING',
      payload: getFieldExpressionString(expressions, field)
    });
  }, [creationFormState.candidateRule]);

  useEffect(() => {
    if (checkActivateRules()) {
      creationFormDispatch({
        type: 'SET_ARE_RULES_DISABLED',
        payload: false
      });
    } else if (checkDeactivateRules()) {
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
      payload: !checkFieldValidation(creationFormState.candidateRule)
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
    let errors = false;

    clickedFields.forEach(clickedField => {
      if (printError(clickedField) == 'error') errors = true;
    });

    if (errors) {
      if (showErrorOnInfoTab !== errors) {
        setShowErrorOnInfoTab(true);
      }
    } else {
      if (showErrorOnInfoTab !== errors) {
        setShowErrorOnInfoTab(false);
      }
    }
  }, [clickedFields]);

  const onExpressionTypeToggle = expressionType => {
    creationFormDispatch({
      type: 'ON_EXPRESSION_TYPE_TOGGLE',
      payload: expressionType
    });
  };

  const checkActivateRules = () => {
    return creationFormState.candidateRule.table && creationFormState.candidateRule.field;
  };

  const checkDeactivateRules = () => {
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
      if (!isNil(candidateRule) && candidateRule.automatic) {
        validationContext.onAutomaticRuleIsUpdated(true);
      }
      onHide();
    } catch (error) {
      notificationContext.add({
        type: 'QC_RULE_UPDATING_ERROR'
      });
    } finally {
      setIsSubmitDisabled(false);
    }
  };

  const onExpressionDelete = expressionId => {
    const {
      candidateRule: { expressions, allExpressions }
    } = creationFormState;
    const parsedExpressions = deleteExpressionRecursively(expressionId, expressions);
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

  const onExpressionMarkToGroup = (expressionId, field) => {
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
        allExpressions,
        groupCandidate,
        groupExpressionsActive: field.value ? 1 : -1
      }
    });
  };

  const onHide = () => {
    creationFormDispatch({ type: 'RESET_CREATION_FORM', payload: resetValidationRuleCreation() });
    validationContext.onCloseModal();
  };

  const onTabChange = tabIndex => {
    if (tabIndex != tabMenuActiveItem) {
      if (tabIndex == 1) {
        setClickedFields([...config.validations.requiredFields[validationContext.level]]);
      } else {
        setTabsChanges({
          expression: true
        });
      }
      setTabMenuActiveItem(tabIndex);
    }
  };

  const onInfoFieldChange = (fieldKey, fieldValue) => {
    onDeleteFromClickedFields(fieldKey);

    if (fieldKey === 'field') {
      const fieldType = getFieldType(creationFormState.candidateRule.table, fieldValue, tabs);
      creationFormDispatch({
        type: 'SET_FIELD_AND_FIELD_TYPE',
        payload: {
          key: fieldKey,
          value: fieldValue,
          fieldType
        }
      });
    } else {
      creationFormDispatch({
        type: 'SET_FORM_FIELD',
        payload: {
          key: fieldKey,
          value: fieldValue
        }
      });
    }
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
    if (field === 'table' || field === 'field' || field === 'errorLevel') {
      setClickedFields(cClickedFields);
    } else {
      if (cClickedFields.includes(field)) {
        cClickedFields.splice(cClickedFields.indexOf(field), 1);
        setClickedFields(cClickedFields);
      }
    }
  };

  const printError = field => {
    return clickedFields.includes(field) && isEmpty(creationFormState.candidateRule[field]) ? 'error' : '';
  };

  const onAddNewExpression = () => {
    creationFormDispatch({
      type: 'ADD_EMPTY_RULE',
      payload: getEmptyExpression()
    });
  };

  const onExpressionGroup = () => {
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
  };

  const onExpressionsErrors = (expression, value) => {
    setExpressionsErrors({
      ...expressionsErrors,
      [expression]: value
    });
  };

  const onSetSQLsentence = (key, value) => {
    creationFormDispatch({
      type: 'SET_FORM_FIELD',
      payload: {
        key,
        value
      }
    });
  };

  const onGetFieldType = field => {
    return getFieldType(creationFormState.candidateRule.table, { code: field }, tabs);
  };

  const renderFieldQCsFooter = (
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
                onClick={() => onUpdateValidationRule()}
              />
            </span>
          ) : (
            <span data-tip data-for="createTooltip">
              <Button
                id={`${componentName}__create`}
                disabled={creationFormState.isValidationCreationDisabled || isSubmitDisabled}
                className={`p-button-primary p-button-text-icon-left ${
                  !creationFormState.isValidationCreationDisabled && !isSubmitDisabled ? 'p-button-animated-blink' : ''
                }`}
                type="button"
                label={resourcesContext.messages.create}
                icon={isSubmitDisabled ? 'spinnerAnimate' : 'check'}
                onClick={() => onCreateValidationRule()}
              />
            </span>
          )}
          {(creationFormState.isValidationCreationDisabled || isSubmitDisabled) && (
            <ReactTooltip className={styles.tooltipClass} effect="solid" id="createTooltip" place="top">
              <span>{resourcesContext.messages.fcSubmitButtonDisabled}</span>
            </ReactTooltip>
          )}

          <Button
            className="p-button-secondary p-button-text-icon-left p-button-animated-blink button-right-aligned"
            icon="cancel"
            id={`${componentName}__cancel`}
            label={resourcesContext.messages.cancel}
            onClick={() => onHide()}
            type="button"
          />
        </div>
      </div>
    </div>
  );

  const dialogLayout = children => (
    <Fragment>
      {validationContext.isVisible && (
        <Dialog
          className={styles.dialog}
          footer={renderFieldQCsFooter}
          header={
            validationContext.ruleEdit
              ? resourcesContext.messages.editFieldConstraint
              : resourcesContext.messages.createFieldConstraintTitle
          }
          onHide={() => onHide()}
          style={{ width: '975px' }}
          visible={validationContext.isVisible}>
          {children}
        </Dialog>
      )}
    </Fragment>
  );

  return dialogLayout(
    <form>
      <div id={styles.QCFormWrapper}>
        <div className={styles.body}>
          <TabView
            activeIndex={tabMenuActiveItem}
            className={styles.tabView}
            onTabChange={e => onTabChange(e.index)}
            renderActiveOnly={false}>
            {tabContents}
          </TabView>
        </div>
      </div>
    </form>
  );
};

export { FieldValidation };
