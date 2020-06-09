import React, { useEffect, useReducer, useContext, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import pull from 'lodash/pull';

import { config } from 'conf';

import styles from './RowValidation.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { ExpressionSelector } from 'ui/views/DatasetDesigner/_components/Validations/_components/ExpressionSelector';
import { ExpressionsTab } from 'ui/views/DatasetDesigner/_components/Validations/_components/ExpressionsTab';
import { InfoTab } from 'ui/views/DatasetDesigner/_components/Validations/_components/InfoTab';
import ReactTooltip from 'react-tooltip';
import { TabView, TabPanel } from 'primereact/tabview';

import { ValidationService } from 'core/services/Validation';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

import {
  createValidationReducerInitState,
  createValidationReducer
} from 'ui/views/DatasetDesigner/_components/Validations/_functions/reducers/CreateValidationReducer';

import { checkComparisonExpressions } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/checkComparisonExpressions';
import { checkComparisonValidation } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/checkComparisonValidation';
import { checkComparisonValidationIfThen } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/checkComparisonValidationIfThen';
import { deleteExpression } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/deleteExpression';
import { deleteExpressionRecursively } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/deleteExpressionRecursively';
import { getDatasetSchemaTableFields } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getDatasetSchemaTableFields';
import { getEmptyExpression } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getEmptyExpression';
import { getComparisonExpressionString } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getComparisonExpressionString';
import { getFieldType } from '../../_functions/utils/getFieldType';
import { getSelectedTableByRecordId } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getSelectedTableByRecordId';
import { groupExpressions } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/groupExpressions';
import { initValidationRuleCreation } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/initValidationRuleCreation';
import { resetValidationRuleCreation } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/resetValidationRuleCreation';
import { setExpressionsfieldsTypes } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/setExpressionsfieldsTypes';
import { setValidationExpression } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/setValidationExpression';

export const RowValidation = ({ datasetId, tabs }) => {
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

  const componentName = 'createValidation';

  useEffect(() => {
    if (!isEmpty(tabs)) {
      creationFormDispatch({ type: 'INIT_FORM', payload: initValidationRuleCreation(tabs) });
    }
  }, [tabs]);

  useEffect(() => {
    if (!creationFormState.candidateRule.automatic) {
      setTabContents([
        <TabPanel
          header={resourcesContext.messages.tabMenuConstraintData}
          headerClassName={showErrorOnInfoTab ? styles.error : ''}
          leftIcon={showErrorOnInfoTab ? 'pi pi-exclamation-circle' : ''}>
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
          header={resourcesContext.messages.tabMenuExpression}
          headerClassName={showErrorOnExpressionTab ? styles.error : ''}
          leftIcon={showErrorOnExpressionTab ? 'pi pi-exclamation-circle' : ''}>
          <ExpressionSelector
            componentName={componentName}
            creationFormState={creationFormState}
            onAddNewExpression={onAddNewExpression}
            onAddNewExpressionIf={onAddNewExpressionIf}
            onAddNewExpressionThen={onAddNewExpressionThen}
            onExpressionDelete={onExpressionDelete}
            onExpressionFieldUpdate={onExpressionFieldUpdate}
            onExpressionGroup={onExpressionGroup}
            onExpressionIfDelete={onExpressionIfDelete}
            onExpressionIfFieldUpdate={onExpressionIfFieldUpdate}
            onExpressionIfGroup={onExpressionIfGroup}
            onExpressionIfMarkToGroup={onExpressionIfMarkToGroup}
            onExpressionsErrors={onExpressionsErrors}
            onExpressionThenDelete={onExpressionThenDelete}
            onExpressionThenFieldUpdate={onExpressionThenFieldUpdate}
            onExpressionThenGroup={onExpressionThenGroup}
            onExpressionMarkToGroup={onExpressionMarkToGroup}
            onExpressionThenMarkToGroup={onExpressionThenMarkToGroup}
            onExpressionTypeToggle={onExpressionTypeToggle}
            onGetFieldType={onGetFieldType}
            tabsChanges={tabsChanges}
          />
        </TabPanel>
      ]);
    } else {
      setTabContents([
        <TabPanel
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
  }, [creationFormState, clickedFields, showErrorOnInfoTab, showErrorOnExpressionTab]);

  useEffect(() => {
    const { table } = creationFormState.candidateRule;
    if (!isEmpty(table)) {
      creationFormDispatch({
        type: 'SET_FIELDS',
        payload: getDatasetSchemaTableFields(table, tabs)
      });
    }

    // if (validationContext.referenceId) {
    //   creationFormDispatch({
    //     type: 'SET_FORM_FIELD',
    //     payload: {
    //       key: 'field',
    //       value: getSelectedFieldById(validationContext.referenceId, tabs)
    //     }
    //   });
    // }
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
    if (validationContext.referenceId) {
      // if (!isNil(validationContext.tableSchemaId)) {
      //   table = getSelectedTableByTableSchemaId(validationContext.tableSchemaId, tabs);
      // } else {
      //   table = getSelectedTableByFieldId(validationContext.referenceId, tabs);
      // }
      const table = getSelectedTableByRecordId(validationContext.referenceId, tabs);
      creationFormDispatch({
        type: 'SET_FORM_FIELD',
        payload: {
          key: 'table',
          value: table
        }
      });
    }
  }, [validationContext.referenceId]);

  useEffect(() => {
    const {
      candidateRule: { table, expressions }
    } = creationFormState;

    creationFormDispatch({
      type: 'SET_EXPRESSIONS_STRING',
      payload: getComparisonExpressionString(expressions, tabs)
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
  }, [creationFormState.candidateRule.table]);

  useEffect(() => {
    const {
      candidateRule: { expressions, expressionsIf, expressionsThen }
    } = creationFormState;

    creationFormDispatch({
      type: 'SET_IS_VALIDATION_ADDING_DISABLED',
      payload: checkComparisonExpressions(expressions)
    });

    creationFormDispatch({
      type: 'SET_IS_VALIDATION_ADDING_DISABLED_IF',
      payload: checkComparisonExpressions(expressionsIf)
    });

    creationFormDispatch({
      type: 'SET_IS_VALIDATION_ADDING_DISABLED_THEN',
      payload: checkComparisonExpressions(expressionsThen)
    });
  }, [creationFormState.areRulesDisabled, creationFormState.candidateRule]);

  useEffect(() => {
    creationFormDispatch({
      type: 'SET_IS_VALIDATION_CREATION_DISABLED',
      payload:
        creationFormState.candidateRule.expressionType === 'ifThenClause'
          ? !checkComparisonValidationIfThen(creationFormState.candidateRule)
          : !checkComparisonValidation(creationFormState.candidateRule)
    });
  }, [creationFormState.candidateRule, creationFormState.candidateRule.expressionType]);

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
      if (printError(clickedField) === 'error') errors = true;
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

  const checkActivateRules = () => {
    return !isEmpty(creationFormState.candidateRule.table);
  };

  const checkDeactivateRules = () => {
    return (
      (!creationFormState.candidateRule.table || !creationFormState.candidateRule.field) &&
      !creationFormState.areRulesDisabled
    );
  };

  const getRecordIdByTableSchemaId = tableSchemaId => {
    const filteredTables = tabs.filter(tab => tab.tableSchemaId === tableSchemaId);
    const [filteredTable] = filteredTables;
    return filteredTable.recordSchemaId;
  };

  const onCreateValidationRule = async () => {
    try {
      setIsSubmitDisabled(true);
      const { candidateRule } = creationFormState;
      candidateRule.recordSchemaId = getRecordIdByTableSchemaId(candidateRule.table.code);
      setExpressionsfieldsTypes(candidateRule.expressions, candidateRule.table, tabs);
      await ValidationService.createRowRule(datasetId, candidateRule);
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
      candidateRule.recordSchemaId = getRecordIdByTableSchemaId(candidateRule.table.code);
      setExpressionsfieldsTypes(candidateRule.expressions, candidateRule.table, tabs);
      await ValidationService.updateRowRule(datasetId, candidateRule);
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

  const onExpressionIfDelete = expressionId => {
    const {
      candidateRule: { expressionsIf, allExpressionsIf }
    } = creationFormState;
    const parsedExpressions = deleteExpressionRecursively(expressionId, expressionsIf);
    const parsedAllExpressions = deleteExpression(expressionId, allExpressionsIf);
    creationFormDispatch({
      type: 'UPDATE_RULES_IF',
      payload: parsedAllExpressions
    });
    creationFormDispatch({
      type: 'UPDATE_EXPRESSIONS_IF_TREE',
      payload: parsedExpressions
    });
  };

  const onExpressionThenDelete = expressionId => {
    const {
      candidateRule: { expressionsThen, allExpressionsThen }
    } = creationFormState;
    const parsedExpressions = deleteExpressionRecursively(expressionId, expressionsThen);
    const parsedAllExpressions = deleteExpression(expressionId, allExpressionsThen);

    creationFormDispatch({
      type: 'UPDATE_RULES_THEN',
      payload: parsedAllExpressions
    });

    creationFormDispatch({
      type: 'UPDATE_EXPRESSIONS_THEN_TREE',
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

  const onExpressionIfFieldUpdate = (expressionId, field) => {
    const {
      candidateRule: { allExpressionsIf }
    } = creationFormState;
    creationFormDispatch({
      type: 'UPDATE_IF_RULES',
      payload: setValidationExpression(expressionId, field, allExpressionsIf)
    });
  };

  const onExpressionThenFieldUpdate = (expressionId, field) => {
    const {
      candidateRule: { allExpressionsThen }
    } = creationFormState;
    creationFormDispatch({
      type: 'UPDATE_THEN_RULES',
      payload: setValidationExpression(expressionId, field, allExpressionsThen)
    });
  };

  const onExpressionMarkToGroup = (expressionId, field) => {
    const {
      groupCandidate,
      candidateRule: { allExpressions }
    } = creationFormState;

    const [currentExpression] = allExpressions.filter(expression => expression.expressionId === expressionId);
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

  const onExpressionIfMarkToGroup = (expressionId, field) => {
    const {
      groupCandidate,
      candidateRule: { allExpressionsIf }
    } = creationFormState;

    const [currentExpression] = allExpressionsIf.filter(expression => expression.expressionId === expressionId);
    currentExpression[field.key] = field.value;

    if (field.value) {
      groupCandidate.push(expressionId);
    } else {
      pull(groupCandidate, expressionId);
    }
    creationFormDispatch({
      type: 'GROUP_IF_RULES_ACTIVATOR',
      payload: {
        allExpressionsIf,
        groupCandidate,
        groupExpressionsActive: field.value ? 1 : -1
      }
    });
  };

  const onExpressionThenMarkToGroup = (expressionId, field) => {
    const {
      groupCandidate,
      candidateRule: { allExpressionsThen }
    } = creationFormState;

    const [currentExpression] = allExpressionsThen.filter(expression => expression.expressionId === expressionId);
    currentExpression[field.key] = field.value;

    if (field.value) {
      groupCandidate.push(expressionId);
    } else {
      pull(groupCandidate, expressionId);
    }
    creationFormDispatch({
      type: 'GROUP_RULES_ACTIVATOR',
      payload: {
        allExpressionsThen,
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
    if (tabIndex !== tabMenuActiveItem) {
      if (tabIndex === 1) {
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
    return clickedFields.includes(field) && isEmpty(creationFormState.candidateRule[field]) ? 'error' : '';
  };

  const onAddNewExpression = () => {
    creationFormDispatch({
      type: 'ADD_EMPTY_RULE',
      payload: getEmptyExpression()
    });
  };

  const onAddNewExpressionIf = () => {
    creationFormDispatch({
      type: 'ADD_EMPTY_IF_RULE',
      payload: getEmptyExpression()
    });
  };

  const onAddNewExpressionThen = () => {
    creationFormDispatch({
      type: 'ADD_EMPTY_THEN_RULE',
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

  const onExpressionIfGroup = () => {
    const groupingResult = groupExpressions(
      creationFormState.candidateRule.expressionsIf,
      creationFormState.groupExpressionsActive,
      creationFormState.groupCandidate
    );
    if (!isNil(groupingResult.newGroup))
      creationFormDispatch({
        type: 'GROUP_EXPRESSIONS',
        payload: {
          expressionsIf: groupingResult.expressionsIf,
          allExpressionsIf: [...creationFormState.candidateRule.allExpressionsIf, groupingResult.newGroup]
        }
      });
  };

  const onExpressionThenGroup = () => {
    const groupingResult = groupExpressions(
      creationFormState.candidateRule.expressionsThen,
      creationFormState.groupExpressionsActive,
      creationFormState.groupCandidate
    );
    if (!isNil(groupingResult.newGroup))
      creationFormDispatch({
        type: 'GROUP_EXPRESSIONS',
        payload: {
          expressionsThen: groupingResult.expressionsThen,
          allExpressionsThen: [...creationFormState.candidateRule.allExpressionsThen, groupingResult.newGroup]
        }
      });
  };

  const onExpressionsErrors = (expression, value) => {
    setExpressionsErrors({
      ...expressionsErrors,
      [expression]: value
    });
  };

  const onExpressionTypeToggle = expressionType => {
    creationFormDispatch({
      type: 'ON_EXPRESSION_TYPE_TOGGLE',
      payload: expressionType
    });
  };

  const onGetFieldType = field => {
    return getFieldType(creationFormState.candidateRule.table, { code: field }, tabs);
  };

  const getRuleCreationBtn = () => {
    const options = {
      onClick: () => {},
      disabled: true,
      label: '',
      id: ''
    };

    if (validationContext.ruleEdit) {
      options.onClick = () => onUpdateValidationRule();
      options.label = resourcesContext.messages.update;
      options.id = `${componentName}__update`;
    } else {
      options.onClick = () => onCreateValidationRule();
      options.label = resourcesContext.messages.create;
      options.id = `${componentName}__create`;
    }

    return (
      <span data-tip data-for="createTooltip">
        <Button
          className="p-button-primary p-button-text-icon-left"
          disabled={creationFormState.isValidationCreationDisabled || isSubmitDisabled}
          icon={isSubmitDisabled ? 'spinnerAnimate' : 'check'}
          id={options.id}
          label={options.label}
          onClick={options.onClick}
          type="button"
        />
      </span>
    );
  };

  const renderRowQCsFooter = (
    <div className={styles.footer}>
      <div className={`${styles.section} ${styles.footerToolBar}`}>
        <div className={styles.subsection}>
          {getRuleCreationBtn()}
          {(creationFormState.isValidationCreationDisabled || isSubmitDisabled) && (
            <ReactTooltip className={styles.tooltipClass} effect="solid" id="createTooltip" place="top">
              <span>{resourcesContext.messages.fcSubmitButtonDisabled}</span>
            </ReactTooltip>
          )}

          <Button
            className="p-button-secondary p-button-text-icon-left"
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
    <Dialog
      className={styles.dialog}
      footer={renderRowQCsFooter}
      header={
        validationContext.ruleEdit
          ? resourcesContext.messages.editRowConstraint
          : resourcesContext.messages.createRowConstraint
      }
      visible={validationContext.isVisible}
      style={{ width: '975px' }}
      onHide={() => onHide()}>
      {children}
    </Dialog>
  );

  return dialogLayout(
    <>
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
    </>
  );
};
