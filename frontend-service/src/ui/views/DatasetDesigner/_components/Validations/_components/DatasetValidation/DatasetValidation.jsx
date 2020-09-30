import React, { Fragment, useContext, useEffect, useReducer, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import styles from './DatasetValidation.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { ExpressionSelector } from 'ui/views/DatasetDesigner/_components/Validations/_components/ExpressionSelector';
import { InfoTab } from 'ui/views/DatasetDesigner/_components/Validations/_components/InfoTab';
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

import { checkComparisonRelation } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/checkComparisonRelation';
import { checkComparisonSQLsentence } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/checkComparisonSQLsentence';
import { checkComparisonValidation } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/checkComparisonValidation';
import { deleteLink } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/deleteLink';
import { getDatasetSchemaTableFields } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getDatasetSchemaTableFields';
import { getDatasetSchemaTableFieldsBySchema } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getDatasetSchemaTableFieldsBySchema';
import { getEmptyLink } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getEmptyLink';
import { getFieldType } from '../../_functions/utils/getFieldType';
import { getReferencedTables } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getReferencedTables';
import { getSelectedTableByTableSchemaId } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getSelectedTableByTableSchemaId';
import { initValidationRuleRelationCreation } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/initValidationRuleRelationCreation';
import { resetValidationRuleCreation } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/resetValidationRuleCreation';
import { setValidationRelation } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/setValidationRelation';

export const DatasetValidation = ({ datasetId, datasetSchema, datasetSchemas, tabs }) => {
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
    if (!isEmpty(tabs)) {
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
            onSetSQLsentence={onSetSQLsentence}
            showRequiredFields={tabsChanges.expression}
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

      const filteredOriginFields =
        rule.relations.referencedDatasetSchema.code !== rule.relations.originDatasetSchema
          ? filteredOriginTable[0].records[0].fields.map(field => {
              return { code: field.fieldId, label: field.name };
            })
          : getDatasetSchemaTableFields(rule.relations.table, tabs);

      inmRuleToEdit.relations.tableFields = filteredOriginFields;

      const filteredReferencedTable = filteredReferencedDatasetSchema[0].tables.filter(
        table => table.tableSchemaId === referencedTableSchemaId
      );

      const filteredReferencedFields =
        rule.relations.referencedDatasetSchema.code !== rule.relations.originDatasetSchema
          ? filteredReferencedTable[0].records[0].fields.map(field => {
              return { code: field.fieldId, label: field.name };
            })
          : getDatasetSchemaTableFields(rule.relations.referencedTable, tabs);

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
      const { candidateRule } = creationFormState;
      candidateRule.recordSchemaId = getRecordIdByTableSchemaId(candidateRule.table.code);

      await ValidationService.createDatasetRule(datasetId, candidateRule);
      onHide();
      notificationContext.hide({
        type: 'VALIDATED_QC_RULE_EVENT'
      });
    } catch (error) {
      notificationContext.add({
        type: 'QC_RULE_CREATION_ERROR'
      });
      console.error('onCreateValidationRule error', error);
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
      const { candidateRule } = creationFormState;
      candidateRule.recordSchemaId = getRecordIdByTableSchemaId(candidateRule.table.code);
      await ValidationService.updateDatasetRule(datasetId, candidateRule);
      onHide();
      notificationContext.hide({
        type: 'VALIDATED_QC_RULE_EVENT'
      });
    } catch (error) {
      notificationContext.add({
        type: 'QC_RULE_UPDATING_ERROR'
      });
      console.error('onUpdateValidationRule error', error);
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
    creationFormDispatch({
      type: 'SET_REFERENCED_FIELDS',
      payload: {
        referencedFields:
          creationFormState.candidateRule.relations.referencedDatasetSchema.code !==
          creationFormState.candidateRule.relations.originDatasetSchema
            ? getDatasetSchemaTableFieldsBySchema(
                referencedTable,
                datasetSchemas,
                creationFormState.candidateRule.relations.referencedDatasetSchema.code
              )
            : getDatasetSchemaTableFields(referencedTable, tabs),
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

  const onSetSQLsentence = (key, value) => {
    creationFormDispatch({
      type: 'SET_FORM_FIELD',
      payload: {
        key,
        value
      }
    });
  };

  const dialogLayout = children => (
    <Fragment>
      {validationContext.isVisible && (
        <Dialog
          className={styles.dialog}
          header={
            validationContext.ruleEdit
              ? resourcesContext.messages.editTableConstraint
              : resourcesContext.messages.createTableConstraint
          }
          visible={validationContext.isVisible}
          style={{ width: '975px' }}
          onHide={() => onHide()}>
          {children}
        </Dialog>
      )}
    </Fragment>
  );

  const getIsCreationDisabled = () => {
    if (creationFormState.candidateRule.expressionType === 'sqlSentence') {
      return (
        creationFormState.isValidationCreationDisabled ||
        isSubmitDisabled ||
        !checkComparisonSQLsentence(creationFormState?.candidateRule?.sqlSentence)
      );
    }
    return (
      creationFormState.isValidationCreationDisabled ||
      isSubmitDisabled ||
      checkComparisonRelation(creationFormState.candidateRule.relations.links)
    );
  };
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
          <div className={styles.footer}>
            <div className={`${styles.section} ${styles.footerToolBar}`}>
              <div className={styles.subsection}>
                {validationContext.ruleEdit ? (
                  <span data-tip data-for="createTooltip">
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
                  <span data-tip data-for="createTooltip">
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
                  <ReactTooltip className={styles.tooltipClass} effect="solid" id="createTooltip" place="top">
                    <span>{resourcesContext.messages['fcSubmitButtonDisabled']}</span>
                  </ReactTooltip>
                )}

                <Button
                  className="p-button-secondary p-button-text-icon-left p-button-animated-blink"
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
    </>
  );
};
