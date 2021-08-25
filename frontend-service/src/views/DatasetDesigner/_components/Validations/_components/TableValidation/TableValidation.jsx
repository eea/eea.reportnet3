import { useContext, useEffect, useReducer, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import styles from './TableValidation.module.scss';

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

import { checkComparisonRelation } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/checkComparisonRelation';
import { checkComparisonSqlSentence } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/checkComparisonSqlSentence';
import { checkComparisonValidation } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/checkComparisonValidation';
import { deleteLink } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/deleteLink';
import { getDatasetSchemaTableFields } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/getDatasetSchemaTableFields';
import { getDatasetSchemaTableFieldsBySchema } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/getDatasetSchemaTableFieldsBySchema';
import { getEmptyLink } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/getEmptyLink';
import { getFieldType } from '../../_functions/Utils/getFieldType';
import { getReferencedTables } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/getReferencedTables';
import { getSelectedTableByTableSchemaId } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/getSelectedTableByTableSchemaId';
import { initValidationRuleRelationCreation } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/initValidationRuleRelationCreation';
import { resetValidationRuleCreation } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/resetValidationRuleCreation';
import { setValidationRelation } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/setValidationRelation';

export const TableValidation = ({ datasetId, datasetSchema, datasetSchemas, isBusinessDataflow, tabs }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const validationContext = useContext(ValidationContext);

  const [creationFormState, creationFormDispatch] = useReducer(
    createValidationReducer,
    createValidationReducerInitState
  );

  const [clickedFields, setClickedFields] = useState([]);
  const [relationsErrors, setRelationsErrors] = useState({});
  const [isSubmitDisabled, setIsSubmitDisabled] = useState(false);
  const [showErrorOnRelationsTab, setShowErrorOnRelationsTab] = useState(false);
  const [showErrorOnInfoTab, setShowErrorOnInfoTab] = useState(true);
  const [tabContents, setTabContents] = useState();
  const [tabMenuActiveItem, setTabMenuActiveItem] = useState(0);
  const [tabsChanges, setTabsChanges] = useState({});

  const componentName = 'createValidation';

  useEffect(() => {
    if (!isEmpty(tabs) && isEmpty(creationFormState.candidateRule.expressionType)) {
      creationFormDispatch({
        type: 'INIT_FORM',
        payload: initValidationRuleRelationCreation(tabs, datasetSchema.datasetSchemaId, datasetSchemas)
      });
    }
  }, [tabs]);

  useEffect(() => {
    if (!creationFormState.candidateRule.automatic) {
      setTabContents([
        <TabPanel
          header={resourcesContext.messages.tabMenuConstraintData}
          headerClassName={showErrorOnInfoTab ? styles.error : ''}
          key="datasetValidationInfo"
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
          header={resourcesContext.messages.tabMenuTableRelations}
          headerClassName={showErrorOnRelationsTab ? styles.error : ''}
          key="datasetValidationRelations"
          leftIcon={showErrorOnRelationsTab ? 'pi pi-exclamation-circle' : ''}>
          <ExpressionSelector
            componentName={componentName}
            creationFormState={creationFormState}
            isBusinessDataflow={isBusinessDataflow}
            onAddNewRelation={onAddNewRelation}
            onDatasetSchemaChange={onDatasetSchemaChange}
            onDoubleReferencedChange={onDoubleReferencedChange}
            onExpressionTypeToggle={onExpressionTypeToggle}
            onGetFieldType={onGetFieldType}
            onInfoFieldChange={onInfoFieldChange}
            onReferencedTableChange={onReferencedTableChange}
            onRelationDelete={onRelationDelete}
            onRelationFieldUpdate={onRelationFieldUpdate}
            onRelationsErrors={onRelationsErrors}
            onSetSqlSentence={onSetSqlSentence}
            showRequiredFields={tabsChanges.expression}
            tabsChanges={tabsChanges}
          />
        </TabPanel>
      ]);
    } else {
      setTabContents([
        <TabPanel
          header={resourcesContext.messages.tabMenuConstraintData}
          key="datasetValidationInfo"
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
  }, [creationFormState, clickedFields, showErrorOnInfoTab, showErrorOnRelationsTab]);

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
    const relationsKeys = Object.keys(relationsErrors);
    let hasErrors = false;
    relationsKeys.forEach(relationKey => {
      if (relationsErrors[relationKey]) {
        hasErrors = true;
      }
    });
    if (hasErrors) {
      setShowErrorOnRelationsTab(true);
    } else {
      setShowErrorOnRelationsTab(false);
    }
  }, [relationsErrors]);

  useEffect(() => {
    if (validationContext.referenceId) {
      const table = getSelectedTableByTableSchemaId(validationContext.referenceId, tabs);

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
  }, [creationFormState.candidateRule]);

  useEffect(() => {
    if (!isNil(creationFormState.candidateRule.relations)) {
      creationFormDispatch({
        type: 'SET_IS_VALIDATION_ADDING_DISABLED',
        payload: checkComparisonRelation(creationFormState.candidateRule.relations.links)
      });
    }
  }, [creationFormState.candidateRule]);

  useEffect(() => {
    creationFormDispatch({
      type: 'SET_IS_VALIDATION_CREATION_DISABLED',
      payload: !checkComparisonValidation(creationFormState.candidateRule)
    });
  }, [creationFormState.candidateRule]);

  useEffect(() => {
    if (validationContext.ruleEdit && !isEmpty(validationContext.ruleToEdit)) {
      creationFormDispatch({
        type: 'POPULATE_CREATE_FORM',
        payload: isNil(validationContext.ruleToEdit.sqlSentence)
          ? parseRuleToEdit(validationContext.ruleToEdit)
          : validationContext.ruleToEdit
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
    return (
      !isEmpty(creationFormState.candidateRule.table) &&
      !isEmpty(creationFormState.candidateRule.relations.referencedTable) &&
      !isEmpty(creationFormState.candidateRule.relations.referencedDatasetSchema)
    );
  };

  const checkDeactivateRules = () => {
    if (!creationFormState.candidateRule.expressionType === 'sqlSentence') {
      return (
        (!creationFormState.candidateRule.table || creationFormState.candidateRule.relations.links.length > 1) &&
        !creationFormState.areRulesDisabled
      );
    } else {
      return false;
    }
  };

  const getRecordIdByTableSchemaId = tableSchemaId => {
    const filteredTables = tabs.filter(tab => tab.tableSchemaId === tableSchemaId);
    const [filteredTable] = filteredTables;
    return filteredTable.recordSchemaId;
  };

  const parseRuleToEdit = rule => {
    if (rule.automatic) return rule;
    const inmRuleToEdit = { ...rule };
    const filteredReferencedDatasetSchema = datasetSchemas.filter(
      dataset => dataset.datasetSchemaId === rule.relations.referencedDatasetSchema.code
    );

    let originTableSchemaId = '';
    let referencedTableSchemaId = '';

    const filteredOriginDatasetSchema = datasetSchemas.filter(
      dataset => dataset.datasetSchemaId === rule.relations.originDatasetSchema
    );

    if (!isNil(filteredOriginDatasetSchema[0])) {
      rule.relations.referencedDatasetSchema.code !== rule.relations.originDatasetSchema
        ? filteredOriginDatasetSchema[0].tables.forEach(table => {
            if (!isNil(table.records)) {
              table.records[0].fields.forEach(field => {
                if (field.fieldId === rule.relations.links[0].originField.code) {
                  originTableSchemaId = table.tableSchemaId;
                  inmRuleToEdit.relations.table = { label: table.tableSchemaName, code: table.tableSchemaId };
                }
              });
            }
          })
        : tabs.forEach(table => {
            if (!isNil(table.records)) {
              table.records[0].fields.forEach(field => {
                if (field.fieldId === rule.relations.links[0].originField.code) {
                  originTableSchemaId = table.tableSchemaId;
                  inmRuleToEdit.relations.table = { label: table.tableSchemaName, code: table.tableSchemaId };
                }
              });
            }
          });
    }

    if (!isNil(filteredReferencedDatasetSchema[0])) {
      inmRuleToEdit.relations.referencedTables =
        rule.relations.referencedDatasetSchema.code !== rule.relations.originDatasetSchema
          ? filteredReferencedDatasetSchema[0].tables.map(table => {
              return { code: table.tableSchemaId, label: table.tableSchemaName };
            })
          : tabs.map(table => {
              return { code: table.tableSchemaId, label: table.tableSchemaName };
            });

      rule.relations.referencedDatasetSchema.code !== rule.relations.originDatasetSchema
        ? filteredReferencedDatasetSchema[0].tables.forEach(table => {
            if (!isNil(table.records)) {
              table.records[0].fields.forEach(field => {
                if (field.fieldId === rule.relations.links[0].referencedField.code) {
                  referencedTableSchemaId = table.tableSchemaId;
                  inmRuleToEdit.relations.referencedTable = { label: table.tableSchemaName, code: table.tableSchemaId };
                }
              });
            }
          })
        : tabs.forEach(table => {
            if (!isNil(table.records)) {
              table.records[0].fields.forEach(field => {
                if (field.fieldId === rule.relations.links[0].referencedField.code) {
                  referencedTableSchemaId = table.tableSchemaId;
                  inmRuleToEdit.relations.referencedTable = { label: table.tableSchemaName, code: table.tableSchemaId };
                }
              });
            }
          });

      const filteredOriginTable = filteredOriginDatasetSchema[0].tables.filter(
        table => table.tableSchemaId === originTableSchemaId
      );

      let filteredOriginFields;
      if (rule.relations.referencedDatasetSchema.code !== rule.relations.originDatasetSchema) {
        filteredOriginFields = filteredOriginTable[0].records[0].fields.map(field => {
          return { code: field.fieldId, label: field.name };
        });
      } else {
        const { tableNonSqlFields } = getDatasetSchemaTableFields(rule.relations.table, tabs);
        filteredOriginFields = tableNonSqlFields;
      }

      inmRuleToEdit.relations.tableFields = filteredOriginFields;

      const filteredReferencedTable = filteredReferencedDatasetSchema[0].tables.filter(
        table => table.tableSchemaId === referencedTableSchemaId
      );
      let filteredReferencedFields;
      if (rule.relations.referencedDatasetSchema.code !== rule.relations.originDatasetSchema) {
        filteredReferencedFields = filteredReferencedTable[0].records[0].fields.map(field => {
          return { code: field.fieldId, label: field.name };
        });
      } else {
        const { tableNonSqlFields } = getDatasetSchemaTableFields(rule.relations.referencedTable, tabs);
        filteredReferencedFields = tableNonSqlFields;
      }

      inmRuleToEdit.relations.referencedFields = filteredReferencedFields;

      inmRuleToEdit.relations.links.forEach(link => {
        link.referencedField.label = filteredReferencedFields.filter(
          filteredField => filteredField.code === link.referencedField.code
        )[0].label;
        link.originField.label = filteredOriginFields.filter(
          originField => originField.code === link.originField.code
        )[0].label;
      });
    }

    if (!isNil(filteredReferencedDatasetSchema[0])) {
      inmRuleToEdit.relations.referencedDatasetSchema.label = filteredReferencedDatasetSchema[0].datasetSchemaName;
    }

    return inmRuleToEdit;
  };

  const onCreateValidationRule = async () => {
    try {
      setIsSubmitDisabled(true);
      const { candidateRule, expressionText } = creationFormState;
      candidateRule.recordSchemaId = getRecordIdByTableSchemaId(candidateRule.table.code);
      candidateRule.expressionText = expressionText;

      await ValidationService.createTableRule(datasetId, candidateRule);
      onHide();
    } catch (error) {
      console.error('TableValidation - onCreateValidationRule.', error);
      notificationContext.add({
        type: 'QC_RULE_CREATION_ERROR'
      });
    } finally {
      setIsSubmitDisabled(false);
    }
  };

  const onDoubleReferencedChange = async checked => {
    creationFormDispatch({
      type: 'UPDATE_IS_DOUBLE_REFERENCED',
      payload: checked
    });
  };

  const onUpdateValidationRule = async () => {
    try {
      setIsSubmitDisabled(true);
      const { candidateRule, expressionText } = creationFormState;
      candidateRule.recordSchemaId = getRecordIdByTableSchemaId(candidateRule.table.code);
      candidateRule.expressionText = expressionText;

      await ValidationService.updateTableRule(datasetId, candidateRule);
      if (!isNil(candidateRule) && candidateRule.automatic) {
        validationContext.onAutomaticRuleIsUpdated(true);
      }
      onHide();
    } catch (error) {
      console.error('TableValidation - onUpdateValidationRule.', error);
      notificationContext.add({
        type: 'QC_RULE_UPDATING_ERROR'
      });
    } finally {
      setIsSubmitDisabled(false);
    }
  };

  const onRelationDelete = linkId => {
    const {
      candidateRule: { relations }
    } = creationFormState;
    const parsedAllLinks = deleteLink(linkId, relations.links);
    creationFormDispatch({
      type: 'UPDATE_LINKS',
      payload: parsedAllLinks
    });
  };

  const onRelationFieldUpdate = (linkId, field) => {
    const {
      candidateRule: { relations }
    } = creationFormState;
    creationFormDispatch({
      type: 'UPDATE_LINKS',
      payload: setValidationRelation(linkId, field, relations.links)
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
      type: fieldKey === 'table' ? 'SET_FORM_FIELD_RELATION' : 'SET_FORM_FIELD',
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

  const onAddNewRelation = () => {
    creationFormDispatch({
      type: 'ADD_EMPTY_RELATION',
      payload: getEmptyLink()
    });
  };

  const onDatasetSchemaChange = referencedDatasetSchema => {
    creationFormDispatch({
      type: 'SET_REFERENCED_TABLES',
      payload: getReferencedTables(referencedDatasetSchema, datasetSchemas)
    });
  };

  const onReferencedTableChange = referencedTable => {
    let referencedFields;
    if (
      creationFormState.candidateRule.relations.referencedDatasetSchema.code !==
      creationFormState.candidateRule.relations.originDatasetSchema
    ) {
      referencedFields = getDatasetSchemaTableFieldsBySchema(
        referencedTable,
        datasetSchemas,
        creationFormState.candidateRule.relations.referencedDatasetSchema.code
      );
    } else {
      const { tableNonSqlFields } = getDatasetSchemaTableFields(referencedTable, tabs);
      referencedFields = tableNonSqlFields;
    }
    creationFormDispatch({
      type: 'SET_REFERENCED_FIELDS',
      payload: {
        referencedFields,
        referencedTable
      }
    });
  };

  const onRelationsErrors = (expression, value) => {
    setRelationsErrors({
      ...relationsErrors,
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

  const onSetSqlSentence = value => {
    creationFormDispatch({
      type: 'SET_FORM_FIELD',
      payload: {
        key: 'sqlSentence',
        value
      }
    });
  };

  const dialogLayout = children =>
    validationContext.isVisible && (
      <Dialog
        className={styles.dialog}
        header={
          validationContext.ruleEdit
            ? resourcesContext.messages.editTableConstraint
            : resourcesContext.messages.createTableConstraint
        }
        onHide={() => onHide()}
        style={{ width: '975px' }}
        visible={validationContext.isVisible}>
        {children}
      </Dialog>
    );

  const getIsCreationDisabled = () => {
    if (creationFormState.candidateRule.expressionType === 'sqlSentence') {
      return (
        creationFormState.isValidationCreationDisabled ||
        isSubmitDisabled ||
        !checkComparisonSqlSentence(creationFormState?.candidateRule?.sqlSentence)
      );
    }
    return (
      creationFormState.isValidationCreationDisabled ||
      isSubmitDisabled ||
      checkComparisonRelation(creationFormState.candidateRule.relations.links)
    );
  };
  return dialogLayout(
    <form>
      <div id={styles.QCFormWrapper}>
        <div className={styles.body}>
          <TabView
            activeIndex={tabMenuActiveItem}
            className={styles.tabView}
            name="TableValidation"
            onTabChange={e => onTabChange(e.index)}
            renderActiveOnly={false}>
            {tabContents}
          </TabView>
        </div>
        <div className={styles.footer}>
          <div className={`${styles.section} ${styles.footerToolBar}`}>
            <div className={styles.subsection}>
              {validationContext.ruleEdit ? (
                <span data-for="createTooltip" data-tip>
                  <Button
                    className="p-button-primary p-button-text-icon-left"
                    disabled={creationFormState.isValidationCreationDisabled || isSubmitDisabled}
                    icon={isSubmitDisabled ? 'spinnerAnimate' : 'check'}
                    id={`${componentName}__update`}
                    label={resourcesContext.messages['update']}
                    onClick={() => onUpdateValidationRule()}
                    type="button"
                  />
                </span>
              ) : (
                <span data-for="createTooltip" data-tip>
                  <Button
                    className={`p-button-primary p-button-text-icon-left ${
                      !creationFormState.isValidationCreationDisabled && !isSubmitDisabled
                        ? 'p-button-animated-blink'
                        : ''
                    }`}
                    disabled={getIsCreationDisabled()}
                    icon={isSubmitDisabled ? 'spinnerAnimate' : 'check'}
                    id={`${componentName}__create`}
                    label={resourcesContext.messages['create']}
                    onClick={() => onCreateValidationRule()}
                    type="button"
                  />
                </span>
              )}
              {(creationFormState.isValidationCreationDisabled || isSubmitDisabled) && (
                <ReactTooltip
                  border={true}
                  className={styles.tooltipClass}
                  effect="solid"
                  id="createTooltip"
                  place="top">
                  <span>{resourcesContext.messages['fcSubmitButtonDisabled']}</span>
                </ReactTooltip>
              )}

              <Button
                className="p-button-secondary p-button-text-icon-left p-button-animated-blink button-right-aligned"
                icon="cancel"
                id={`${componentName}__cancel`}
                label={resourcesContext.messages['cancel']}
                onClick={() => onHide()}
                type="button"
              />
            </div>
          </div>
        </div>
      </div>
    </form>
  );
};
