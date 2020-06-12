import React, { useEffect, useReducer, useContext, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import styles from './DatasetValidation.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { TableRelationsSelector } from 'ui/views/DatasetDesigner/_components/Validations/_components/TableRelationsSelector';
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

import { checkComparisonRelation } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/checkComparisonRelation';
import { checkComparisonValidation } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/checkComparisonValidation';
import { deleteLink } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/deleteLink';
import { getDatasetSchemaTableFields } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getDatasetSchemaTableFields';
import { getDatasetSchemaTableFieldsBySchema } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getDatasetSchemaTableFieldsBySchema';
import { getEmptyLink } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getEmptyLink';
import { getFieldType } from '../../_functions/utils/getFieldType';
import { getReferencedTables } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getReferencedTables';
import { getSelectedTableByRecordId } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getSelectedTableByRecordId';
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
      console.log('INIT');
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
          leftIcon={showErrorOnRelationsTab ? 'pi pi-exclamation-circle' : ''}>
          <TableRelationsSelector
            componentName={componentName}
            creationFormState={creationFormState}
            onAddNewRelation={onAddNewRelation}
            onDatasetSchemaChange={onDatasetSchemaChange}
            onDoubleReferencedChange={onDoubleReferencedChange}
            onInfoFieldChange={onInfoFieldChange}
            onReferencedTableChange={onReferencedTableChange}
            onRelationDelete={onRelationDelete}
            onRelationFieldUpdate={onRelationFieldUpdate}
            onRelationsErrors={onRelationsErrors}
            onExpressionTypeToggle={onExpressionTypeToggle}
            onGetFieldType={onGetFieldType}
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
      console.log('FIELDS');
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
      console.log({ creationFormState }, creationFormState.table);
      creationFormDispatch({
        type: 'POPULATE_CREATE_FORM',
        payload: parseRuleToEdit(validationContext.ruleToEdit)
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
    return (
      (!creationFormState.candidateRule.table || creationFormState.candidateRule.relations.links.length > 1) &&
      !creationFormState.areRulesDisabled
    );
  };

  const getRecordIdByTableSchemaId = tableSchemaId => {
    const filteredTables = tabs.filter(tab => tab.tableSchemaId === tableSchemaId);
    const [filteredTable] = filteredTables;
    return filteredTable.recordSchemaId;
  };

  const parseRuleToEdit = rule => {
    console.log({ rule, datasetSchemas });
    const inmRuleToEdit = { ...rule };

    const filteredReferencedDatasetSchema = datasetSchemas.filter(
      dataset => dataset.datasetSchemaId === rule.relations.referencedDatasetSchema.code
    );

    let originTableSchemaId = '';
    let referencedTableSchemaId = '';
    // if (rule.originDatasetSchemaId !== rule.relations.referencedDatasetSchema.code) {
    const filteredOriginDatasetSchema = datasetSchemas.filter(
      dataset => dataset.datasetSchemaId === rule.relations.originDatasetSchema
    );

    if (!isNil(filteredOriginDatasetSchema[0])) {
      filteredOriginDatasetSchema[0].tables.forEach(table => {
        table.records[0].fields.forEach(field => {
          if (field.fieldId === rule.relations.links[0].originField.code) {
            originTableSchemaId = table.tableSchemaId;
            inmRuleToEdit.relations.table = { label: table.tableSchemaName, code: table.tableSchemaId };
          }
        });
      });
    }

    console.log({ filteredReferencedDatasetSchema });
    if (!isNil(filteredReferencedDatasetSchema[0])) {
      inmRuleToEdit.relations.referencedTables = filteredReferencedDatasetSchema[0].tables.map(table => {
        return { code: table.tableSchemaId, label: table.tableSchemaName };
      });

      filteredReferencedDatasetSchema[0].tables.forEach(table => {
        console.log(table.records[0].fields, rule.relations.links[0].referencedField.code);
        table.records[0].fields.forEach(field => {
          if (field.fieldId === rule.relations.links[0].referencedField.code) {
            referencedTableSchemaId = table.tableSchemaId;
            inmRuleToEdit.relations.referencedTable = { label: table.tableSchemaName, code: table.tableSchemaId };
          }
        });
      });

      const filteredOriginTable = filteredOriginDatasetSchema[0].tables.filter(
        table => table.tableSchemaId === originTableSchemaId
      );

      const filteredOriginFields = filteredOriginTable[0].records[0].fields.map(field => {
        return { code: field.fieldId, label: field.name };
      });

      inmRuleToEdit.relations.tableFields = filteredOriginFields;

      const filteredReferencedTable = filteredReferencedDatasetSchema[0].tables.filter(
        table => table.tableSchemaId === referencedTableSchemaId
      );

      const filteredReferencedFields = filteredReferencedTable[0].records[0].fields.map(field => {
        return { code: field.fieldId, label: field.name };
      });

      inmRuleToEdit.relations.referencedFields = filteredReferencedFields;

      inmRuleToEdit.relations.links.forEach(link => {
        link.referencedField.label = filteredReferencedFields.filter(
          filteredField => filteredField.code === link.referencedField.code
        )[0].label;
        link.originField.label = filteredOriginFields.filter(
          originField => originField.code === link.originField.code
        )[0].label;
      });

      console.log(inmRuleToEdit.relations.referencedTable);
    }
    // }
    if (!isNil(filteredReferencedDatasetSchema[0])) {
      inmRuleToEdit.relations.referencedDatasetSchema.label = filteredReferencedDatasetSchema[0].datasetSchemaName;
    }

    return inmRuleToEdit;

    //   {
    //     "isDoubleReferenced":false,
    //     "referencedDatasetSchema":{
    //        "label":"sdafasdf",
    //        "code":"5ed5f1e07999710001366b03"
    //     },
    //     "referencedFields":[
    //        {
    //           "label":"Countries",
    //           "code":"5ed7b4c1cee26900014c2601"
    //        },
    //        {
    //           "label":"Linked field",
    //           "code":"5ed89a74cee26900014c261b"
    //        },
    //        {
    //           "label":"poinnnt",
    //           "code":"5edf991036c5a50001723220"
    //        }
    //     ],
    //     "referencedTable":{
    //        "label":"5",
    //        "code":"5ed7a282cee26900014c25ed"
    //     },
    //     "referencedTables":[
    //        {
    //           "label":"1",
    //           "code":"5ed7adf3cee26900014c25f7"
    //        },
    //        {
    //           "label":"2",
    //           "code":"5ed5f1f17999710001366b07"
    //        },
    //        null
    //     ],
    //     "originDatasetSchema":"5ed5f1e07999710001366b03",
    //     "links":[
    //        {
    //           "linkId":"8dcd07e2-1024-45de-aa91-e9b1f91f58f2",
    //           "originField":"",
    //           "referencedField":""
    //        }
    //     ]
    //  }
  };

  const onCreateValidationRule = async () => {
    try {
      setIsSubmitDisabled(true);
      const { candidateRule } = creationFormState;
      candidateRule.recordSchemaId = getRecordIdByTableSchemaId(candidateRule.table.code);

      await ValidationService.createDatasetRule(datasetId, candidateRule);
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
        referencedFields: getDatasetSchemaTableFieldsBySchema(
          referencedTable,
          datasetSchemas,
          datasetSchema.datasetSchemaId
        ),
        referencedTable
      }
    });
    // onInfoFieldChange('referencedTable',referencedDatasetSchema)
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
    console.log(creationFormState.candidateRule.table);
    return getFieldType(creationFormState.candidateRule.table, { code: field }, tabs);
  };

  const dialogLayout = children => (
    <Dialog
      className={styles.dialog}
      header={
        validationContext.ruleEdit
          ? resourcesContext.messages.editDatasetConstraint
          : resourcesContext.messages.createDatasetConstraint
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
                      label={resourcesContext.messages.update}
                      onClick={() => onUpdateValidationRule()}
                      type="button"
                    />
                  </span>
                ) : (
                  <span data-tip data-for="createTooltip">
                    <Button
                      className="p-button-primary p-button-text-icon-left"
                      disabled={
                        creationFormState.isValidationCreationDisabled ||
                        isSubmitDisabled ||
                        checkComparisonRelation(creationFormState.candidateRule.relations.links)
                      }
                      icon={isSubmitDisabled ? 'spinnerAnimate' : 'check'}
                      id={`${componentName}__create`}
                      label={resourcesContext.messages.create}
                      onClick={() => onCreateValidationRule()}
                      type="button"
                    />
                  </span>
                )}
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
        </div>
      </form>
    </>
  );
};
