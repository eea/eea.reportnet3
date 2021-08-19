import { useContext, useEffect, useReducer, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import pull from 'lodash/pull';

import { config } from 'conf';

import styles from './RowValidation.module.scss';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { ExpressionSelector } from 'views/DatasetDesigner/_components/Validations/_components/ExpressionSelector';
import { InfoTab } from 'views/DatasetDesigner/_components/Validations/_components/InfoTab';
import { TabView, TabPanel } from 'primereact/tabview';
import ReactTooltip from 'react-tooltip';

import { ValidationService } from 'services/ValidationService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

import {
  createValidationReducerInitState,
  createValidationReducer
} from 'views/DatasetDesigner/_components/Validations/_functions/Reducers/CreateValidationReducer';

import { checkComparisonExpressions } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/checkComparisonExpressions';
import { checkComparisonSQLsentence } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/checkComparisonSQLsentence';
import { checkComparisonValidation } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/checkComparisonValidation';
import { checkComparisonValidationIfThen } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/checkComparisonValidationIfThen';
import { deleteExpression } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/deleteExpression';
import { deleteExpressionRecursively } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/deleteExpressionRecursively';
import { getComparisonExpressionString } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/getComparisonExpressionString';
import { getDatasetSchemaTableFields } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/getDatasetSchemaTableFields';
import { getEmptyExpression } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/getEmptyExpression';
import { getFieldType } from '../../_functions/Utils/getFieldType';
import { getSelectedTableByRecordId } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/getSelectedTableByRecordId';
import { groupExpressions } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/groupExpressions';
import { initValidationRuleCreation } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/initValidationRuleCreation';
import { resetValidationRuleCreation } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/resetValidationRuleCreation';
import { setExpressionsFieldsTypes } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/setExpressionsFieldsTypes';
import { setValidationExpression } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/setValidationExpression';

export const RowValidation = ({ datasetId, isBusinessDataflow, tabs }) => {
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
    if (!isEmpty(tabs) && isEmpty(creationFormState.candidateRule.expressionType)) {
      creationFormDispatch({ type: 'INIT_FORM', payload: initValidationRuleCreation(tabs) });
    }
  }, [tabs]);

  useEffect(() => {
    if (!creationFormState.candidateRule.automatic) {
      setTabContents([
        <TabPanel
          header={resourcesContext.messages.tabMenuConstraintData}
          headerClassName={showErrorOnInfoTab ? styles.error : ''}
          key="rowInfoTab"
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
          key="rowExpressionTab"
          leftIcon={showErrorOnExpressionTab ? 'pi pi-exclamation-circle' : ''}>
          <ExpressionSelector
            componentName={componentName}
            creationFormState={creationFormState}
            isBusinessDataflow={isBusinessDataflow}
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
            onExpressionMarkToGroup={onExpressionMarkToGroup}
            onExpressionThenDelete={onExpressionThenDelete}
            onExpressionThenFieldUpdate={onExpressionThenFieldUpdate}
            onExpressionThenGroup={onExpressionThenGroup}
            onExpressionThenMarkToGroup={onExpressionThenMarkToGroup}
            onExpressionTypeToggle={onExpressionTypeToggle}
            onExpressionsErrors={onExpressionsErrors}
            onGetFieldType={onGetFieldType}
            onSetSqlSentence={onSetSqlSentence}
            tabsChanges={tabsChanges}
          />
        </TabPanel>
      ]);
    } else {
      setTabContents([
        <TabPanel
          header={resourcesContext.messages.tabMenuConstraintData}
          key="rowInfoTab"
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
    let formula = '';

    const {
      candidateRule: { expressions, expressionType, expressionsIf, expressionsThen }
    } = creationFormState;

    if (expressionType === 'ifThenClause') {
      formula = `IF ( ${getComparisonExpressionString(expressionsIf, tabs)} ) THEN ( ${getComparisonExpressionString(
        expressionsThen,
        tabs
      )} )`;
    } else {
      formula = getComparisonExpressionString(expressions, tabs);
    }

    creationFormDispatch({
      type: 'SET_EXPRESSIONS_STRING',
      payload: formula
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
      const { candidateRule, expressionText } = creationFormState;
      candidateRule.recordSchemaId = getRecordIdByTableSchemaId(candidateRule.table.code);
      candidateRule.expressionText = expressionText;

      if (candidateRule.expressionType === 'ifThenClause') {
        setExpressionsFieldsTypes(candidateRule.expressionsIf, candidateRule.table, tabs);
        setExpressionsFieldsTypes(candidateRule.expressionsThen, candidateRule.table, tabs);
      }

      if (candidateRule.expressionType === 'fieldValidation' || candidateRule.expressionType === 'fieldComparison') {
        setExpressionsFieldsTypes(candidateRule.expressions, candidateRule.table, tabs);
      }

      await ValidationService.createRowRule(datasetId, candidateRule);
      onHide();
    } catch (error) {
      console.error('RowValidation - onCreateValidationRule.', error);
      notificationContext.add({
        type: 'QC_RULE_CREATION_ERROR'
      });
    } finally {
      setIsSubmitDisabled(false);
    }
  };

  const onUpdateValidationRule = async () => {
    try {
      setIsSubmitDisabled(true);
      const { candidateRule, expressionText } = creationFormState;
      candidateRule.recordSchemaId = getRecordIdByTableSchemaId(candidateRule.table.code);
      candidateRule.expressionText = expressionText;

      if (candidateRule.expressionType === 'ifThenClause') {
        setExpressionsFieldsTypes(candidateRule.expressionsIf, candidateRule.table, tabs);
        setExpressionsFieldsTypes(candidateRule.expressionsThen, candidateRule.table, tabs);
      }

      if (candidateRule.expressionType === 'fieldValidation' || candidateRule.expressionType === 'fieldComparison') {
        setExpressionsFieldsTypes(candidateRule.expressions, candidateRule.table, tabs);
      }

      await ValidationService.updateRowRule(datasetId, candidateRule);
      onHide();
    } catch (error) {
      console.error('RowValidation - onUpdateValidationRule.', error);
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
      groupCandidateIf,
      candidateRule: { allExpressionsIf }
    } = creationFormState;

    const [currentExpression] = allExpressionsIf.filter(expression => expression.expressionId === expressionId);
    currentExpression[field.key] = field.value;

    if (field.value) {
      groupCandidateIf.push(expressionId);
    } else {
      pull(groupCandidateIf, expressionId);
    }
    creationFormDispatch({
      type: 'GROUP_IF_RULES_ACTIVATOR',
      payload: {
        allExpressionsIf,
        groupCandidateIf,
        groupExpressionsIfActive: field.value ? 1 : -1
      }
    });
  };

  const onExpressionThenMarkToGroup = (expressionId, field) => {
    const {
      groupCandidateThen,
      candidateRule: { allExpressionsThen }
    } = creationFormState;

    const [currentExpression] = allExpressionsThen.filter(expression => expression.expressionId === expressionId);
    currentExpression[field.key] = field.value;

    if (field.value) {
      groupCandidateThen.push(expressionId);
    } else {
      pull(groupCandidateThen, expressionId);
    }
    creationFormDispatch({
      type: 'GROUP_THEN_RULES_ACTIVATOR',
      payload: {
        allExpressionsThen,
        groupCandidateThen,
        groupExpressionsThenActive: field.value ? 1 : -1
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
      creationFormState.groupExpressionsIfActive,
      creationFormState.groupCandidateIf
    );
    if (!isNil(groupingResult.newGroup))
      creationFormDispatch({
        type: 'GROUP_EXPRESSIONS_IF',
        payload: {
          expressionsIf: groupingResult.expressions,
          allExpressionsIf: [...creationFormState.candidateRule.allExpressionsIf, groupingResult.newGroup]
        }
      });
  };

  const onExpressionThenGroup = () => {
    const groupingResult = groupExpressions(
      creationFormState.candidateRule.expressionsThen,
      creationFormState.groupExpressionsThenActive,
      creationFormState.groupCandidateThen
    );

    if (!isNil(groupingResult.newGroup))
      creationFormDispatch({
        type: 'GROUP_EXPRESSIONS_THEN',
        payload: {
          expressionsThen: groupingResult.expressions,
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

  const onSetSqlSentence = (key, value) => {
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

  const getIsCreationDisabled = () => {
    if (creationFormState.candidateRule.expressionType === 'sqlSentence') {
      return (
        creationFormState.isValidationCreationDisabled ||
        isSubmitDisabled ||
        !checkComparisonSQLsentence(creationFormState?.candidateRule?.sqlSentence)
      );
    }

    return creationFormState.isValidationCreationDisabled || isSubmitDisabled;
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
      <span data-for="createTooltip" data-tip>
        <Button
          className={`p-button-primary p-button-text-icon-left ${
            !creationFormState.isValidationCreationDisabled && !isSubmitDisabled ? 'p-button-animated-blink' : ''
          }`}
          disabled={getIsCreationDisabled()}
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
            <ReactTooltip border={true} className={styles.tooltipClass} effect="solid" id="createTooltip" place="top">
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

  const dialogLayout = children =>
    validationContext.isVisible && (
      <Dialog
        className={styles.dialog}
        footer={renderRowQCsFooter}
        header={
          validationContext.ruleEdit
            ? resourcesContext.messages.editRowConstraint
            : resourcesContext.messages.createRowConstraint
        }
        onHide={() => onHide()}
        style={{ width: '975px' }}
        visible={validationContext.isVisible}>
        {children}
      </Dialog>
    );

  return dialogLayout(
    <form>
      <div id={styles.QCFormWrapper}>
        <div className={styles.body}>
          <TabView
            activeIndex={tabMenuActiveItem}
            className={styles.tabView}
            name="RowValidation"
            onTabChange={e => onTabChange(e.index)}
            renderActiveOnly={false}>
            {tabContents}
          </TabView>
        </div>
      </div>
    </form>
  );
};
