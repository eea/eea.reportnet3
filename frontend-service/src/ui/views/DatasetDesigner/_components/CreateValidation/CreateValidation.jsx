import React, { useEffect, useReducer, useContext } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import pull from 'lodash/pull';

import styles from './CreateValidation.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
import { Dialog } from 'ui/views/_components/Dialog';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { ValidationExpresion } from './_components/ValidationExpresion';

import { ValidationService } from 'core/services/Validation';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import {
  createValidationReducerInitState,
  createValidationReducer
} from './_functions/reducers/CreateValidationReducer';

import { checkExpresions } from './_functions/utils/checkExpresions';
import { checkValidation } from './_functions/utils/checkValidation';
import { deleteExpresion } from './_functions/utils/deleteExpresion';
import { getEmptyExpresion } from './_functions/utils/getEmptyExpresion';
import { getExpresionString } from './_functions/utils/getExpresionString';
import { groupExpresions } from './_functions/utils/groupExpresions';
import { setFormField } from './_functions/utils/setFormField';
import { setValidationExpresion } from './_functions/utils/setValidationExpresion';

const CreateValidation = ({ isVisible, datasetSchema, table, field, toggleVisibility }) => {
  const resourcesContext = useContext(ResourcesContext);
  const [creationFormState, creationFormDispatch] = useReducer(
    createValidationReducer,
    createValidationReducerInitState
  );

  const ruleDisablingCheckListener = [creationFormState.candidateRule.table, creationFormState.candidateRule.field];
  const ruleAdditionCheckListener = [creationFormState.areRulesDisabled, creationFormState.candidateRule];
  const validationCreationCheckListener = [ruleAdditionCheckListener, creationFormState.candidateRule];
  const onExpresionGroup = (expresionId, field) => {
    const {
      candidateRule: { expresions },
      groupCandidate
    } = creationFormState;
    if (field.key == 'group') {
      if (field.value.value) {
        groupCandidate.push(expresionId);
      } else {
        pull(groupCandidate, expresionId);
      }
      creationFormDispatch({
        type: 'GROUP_RULES_ACTIVATOR',
        payload: {
          groupRulesActive: field.value.value ? 1 : -1
        }
      });
    }
  };
  const onExpresionFieldUpdate = (expresionId, field) => {
    const {
      candidateRule: { expresions }
    } = creationFormState;
    creationFormDispatch({
      type: 'UPDATE_RULES',
      payload: setValidationExpresion(expresionId, field, expresions)
    });
  };
  const onExpresionDelete = expresionId => {
    const {
      candidateRule: { expresions }
    } = creationFormState;
    creationFormDispatch({
      type: 'UPDATE_RULES',
      payload: deleteExpresion(expresionId, expresions)
    });
  };
  //set table tada
  const initValidationRuleCreation = () => {
    const { tables: rawTables } = datasetSchema;
    rawTables.pop();
    const tables = rawTables.map(table => {
      return { label: table.tableSchemaName, code: table.recordSchemaId };
    });
    const errorLevels = [
      { label: 'INFO', value: 'INFO' },
      { label: 'WARNING', value: 'WARNING' },
      { label: 'ERROR', value: 'ERROR' },
      { label: 'BLOCKER', value: 'BLOCKER' }
    ];
    return {
      tables,
      errorLevels,
      candidateRule: {
        table: undefined,
        field: undefined,
        shortCode: '',
        description: '',
        errorMessage: '',
        errorLevel: undefined,
        active: false,
        expresions: [getEmptyExpresion()]
      }
    };
  };
  useEffect(() => {
    const { tables: rawTables } = datasetSchema;
    rawTables.pop();
    const tables = rawTables.map(table => {
      return { label: table.tableSchemaName, code: table.recordSchemaId };
    });
    const errorLevels = [
      { label: 'INFO', value: 'INFO' },
      { label: 'WARNING', value: 'WARNING' },
      { label: 'ERROR', value: 'ERROR' },
      { label: 'BLOCKER', value: 'BLOCKER' }
    ];
    creationFormDispatch({ type: 'INIT_FORM', payload: initValidationRuleCreation() });
  }, []);

  //set field data
  useEffect(() => {
    const { table: tableInContext } = creationFormState.candidateRule;
    if (!isEmpty(tableInContext)) {
      const [selectedTable] = datasetSchema.tables.filter(table => table.recordSchemaId === tableInContext.code);
      const fields =
        !isNil(selectedTable) && !isEmpty(selectedTable)
          ? selectedTable.records[0].fields.map(field => ({
              label: field.name,
              code: field.fieldId
            }))
          : [];
      creationFormDispatch({
        type: 'SET_FIELDS',
        payload: fields
      });
    }
  }, [creationFormState.candidateRule.table]);

  useEffect(() => {
    const {
      candidateRule: { field, expresions }
    } = creationFormState;

    creationFormDispatch({
      type: 'SET_EXPRESIONS_STRING',
      payload: getExpresionString(expresions, field)
    });
  }, [creationFormState.candidateRule]);

  //disable manager
  const checkActivateRules = () => {
    return creationFormState.candidateRule.table && creationFormState.candidateRule.field;
  };
  const checkDesactivateRules = () => {
    return (
      (!creationFormState.candidateRule.table || !creationFormState.candidateRule.field) &&
      !creationFormState.areRulesDisabled
    );
  };
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
      candidateRule: { expresions }
    } = creationFormState;
    creationFormDispatch({
      type: 'SET_IS_VALIDATION_ADDING_DISABLED',
      payload: checkExpresions(expresions)
    });
  }, [...ruleAdditionCheckListener]);

  const createValidationRule = async () => {
    try {
      console.log('creationFormState', creationFormState, datasetSchema);
      const { candidateRule } = creationFormState;
      const { datasetSchemaId } = datasetSchema;
      await ValidationService.create(datasetSchemaId, candidateRule);
    } catch (error) {
      console.log('createValidationRule error', error);
    }
  };

  const dialogLayout = children => (
    <Dialog
      header="Create Field Validation rule"
      visible={isVisible}
      style={{ width: '70%' }}
      onHide={e => {
        toggleVisibility(false);
      }}>
      {children}
    </Dialog>
  );

  useEffect(() => {
    creationFormDispatch({
      type: 'SET_IS_VALIDATION_CREATION_DISABLED',
      payload: !checkValidation(creationFormState.candidateRule)
    });
  }, [creationFormState.candidateRule]);

  return dialogLayout(
    <div>
      <form action="">
        <div id={styles.QCFormWrapper}>
          <div className={styles.section}>
            <div>
              <label htmlFor="table">
                {resourcesContext.messages.table}
                <Dropdown
                  appendTo={document.body}
                  filterPlaceholder={resourcesContext.messages.table}
                  placeholder={resourcesContext.messages.table}
                  optionLabel="label"
                  options={creationFormState.schemaTables}
                  onChange={e => {
                    setFormField(
                      {
                        key: 'table',
                        value: e.target.value
                      },
                      creationFormDispatch
                    );
                  }}
                  value={creationFormState.candidateRule.table}
                />
              </label>
              <label htmlFor="field">
                {resourcesContext.messages.field}
                <Dropdown
                  appendTo={document.body}
                  filterPlaceholder={resourcesContext.messages.field}
                  placeholder={resourcesContext.messages.field}
                  optionLabel="label"
                  options={creationFormState.tableFields}
                  onChange={e => {
                    setFormField(
                      {
                        key: 'field',
                        value: e.target.value
                      },
                      creationFormDispatch
                    );
                  }}
                  value={creationFormState.candidateRule.field}
                />
              </label>
              <label htmlFor="shortCode">
                {resourcesContext.messages.ruleShortCode}
                <InputText
                  placeholder={resourcesContext.messages.ruleShortCode}
                  value={creationFormState.candidateRule.shortCode}
                  onChange={e => {
                    setFormField(
                      {
                        key: 'shortCode',
                        value: e.target.value
                      },
                      creationFormDispatch
                    );
                  }}
                />
              </label>
              <label htmlFor="description">
                {resourcesContext.messages.description}
                <InputText
                  placeholder={resourcesContext.messages.description}
                  value={creationFormState.candidateRule.description}
                  onChange={e => {
                    setFormField(
                      {
                        key: 'description',
                        value: e.target.value
                      },
                      creationFormDispatch
                    );
                  }}
                />
              </label>
              <label htmlFor="errorMessage" className={styles.errorMessage}>
                {resourcesContext.messages.errorMessage}
                <InputText
                  placeholder={resourcesContext.messages.errorMessage}
                  value={creationFormState.candidateRule.errorMessage}
                  onChange={e => {
                    setFormField(
                      {
                        key: 'errorMessage',
                        value: e.target.value
                      },
                      creationFormDispatch
                    );
                  }}
                />
              </label>
              <label htmlFor="description">
                {resourcesContext.messages.errorType}
                <Dropdown
                  filterPlaceholder={resourcesContext.messages.errorType}
                  placeholder={resourcesContext.messages.errorType}
                  appendTo={document.body}
                  optionLabel="label"
                  options={creationFormState.errorLevels}
                  onChange={e => {
                    setFormField(
                      {
                        key: 'errorLevel',
                        value: e.target.value
                      },
                      creationFormDispatch
                    );
                  }}
                  value={creationFormState.candidateRule.errorLevel}
                />
              </label>
            </div>
            <div>
              <label htmlFor="QcActive">
                {resourcesContext.messages.active}
                <Checkbox
                  onChange={e => {
                    setFormField({ key: 'active', value: e.checked }, creationFormDispatch);
                  }}
                  isChecked={creationFormState.candidateRule.active}
                />
              </label>
            </div>
          </div>
          <div className={styles.section}>
            <table>
              <thead>
                <tr>
                  <th>{resourcesContext.messages.group}</th>
                  <th>{resourcesContext.messages.andor}</th>
                  <th>{resourcesContext.messages.operatorType}</th>
                  <th>{resourcesContext.messages.operator}</th>
                  <th>{resourcesContext.messages.value}</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {creationFormState.candidateRule.expresions &&
                  creationFormState.candidateRule.expresions.map(expresion => (
                    <ValidationExpresion
                      isDisabled={creationFormState.areRulesDisabled}
                      expresionValues={expresion}
                      onExpresionFieldUpdate={onExpresionFieldUpdate}
                      onExpresionDelete={onExpresionDelete}
                      onExpresionGroup={onExpresionGroup}
                    />
                  ))}
              </tbody>
            </table>
          </div>

          {creationFormState.groupRulesActive >= 2 && (
            <div className={styles.section}>
              <Button
                className="p-button-primary p-button-text"
                type="button"
                label="Group"
                icon="plus"
                onClick={e => {
                  groupExpresions(
                    creationFormState.candidateRule.expresions,
                    creationFormState.groupRulesActive,
                    creationFormState.groupCandidate,
                    creationFormDispatch
                  );
                }}
              />
            </div>
          )}

          <div className={styles.section}>
            <textarea name="" id="" cols="30" rows="5" value={creationFormState.validationRuleString}></textarea>
          </div>
          <div className={`${styles.section} ${styles.footerToolBar}`}>
            <div>
              <Button
                disabled={creationFormState.isRuleAddingDisabled}
                className="p-button-primary p-button-text-icon-left"
                type="button"
                label={resourcesContext.messages.addNewRule}
                icon="plus"
                onClick={e =>
                  creationFormDispatch({
                    type: 'ADD_EMPTY_RULE',
                    payload: getEmptyExpresion()
                  })
                }
              />
            </div>
            <div>
              <Button
                disabled={creationFormState.isValidationCreationDisabled}
                className="p-button-primary p-button-text-icon-left"
                type="button"
                label={resourcesContext.messages.create}
                icon="check"
                onClick={e => createValidationRule()}
              />
              <Button
                className="p-button-secondary p-button-text-icon-left"
                type="button"
                label={resourcesContext.messages.cancel}
                icon="cancel"
                onClick={e => {
                  creationFormDispatch({ type: 'INIT_FORM', payload: initValidationRuleCreation() });
                  toggleVisibility(false);
                }}
              />
            </div>
          </div>
        </div>
      </form>
    </div>
  );
};

export { CreateValidation };
