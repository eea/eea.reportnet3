import React, { useEffect, useReducer } from 'react';

import { capitalize, isEmpty, isEqual, isUndefined, last, pullAllWith, pull, findIndex } from 'lodash';
import uuid from 'uuid';

import styles from './CreateValidation.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
import { Dialog } from 'ui/views/_components/Dialog';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { ValidationRule } from './_components/ValidationRule';

const createValidationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_FORM_FIELD':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          [payload.key]: payload.value
        }
      };
    case 'SET_TABLES':
      return {
        ...state,
        schemaTables: payload
      };
    case 'SET_FIELDS':
      return {
        ...state,
        tableFields: payload
      };
    case 'UPDATE_RULES':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          rules: payload
        }
      };
    case 'SET_ARE_RULES_DISABLED':
      return {
        ...state,
        areRulesDisabled: payload
      };
    case 'SET_IS_VALIDATION_ADDING_DISABLED':
      return {
        ...state,
        isRuleAddingDisabled: payload
      };
    case 'SET_IS_VALIDATION_ADDING_DISABLED':
      return {
        ...state,
        isValidationCreationDisabled: payload
      };
    case 'ADD_EMPTY_RULE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          rules: [...state.candidateRule.rules, payload]
        }
      };
    case 'DELETE_RULE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          rules: payload
        }
      };
    case 'GROUP_RULES_ACTIVATOR':
      return {
        ...state,
        groupRulesActive: state.groupRulesActive + payload.groupRulesActive
      };
    case 'SET_EXPRESIONS_STRING':
      return {
        ...state,
        validationRuleString: payload
      };
    case 'INIT_FORM':
      return {
        ...state,
        schemaTables: payload.tables,
        errorLevels: payload.errorLevels,
        candidateRule: {
          rules: payload.rules
        }
      };
    default:
      return state;
  }
};

const createValidationReducerInitState = {
  candidateRule: {},
  datasetSchema: {},
  schemaTables: [],
  errorLevels: [],
  areRulesDisabled: true,
  isRuleAddingDisabled: true,
  isValidationCreationDisabled: true,
  groupRulesActive: 0,
  groupCandidate: []
};

const CreateValidation = ({ isVisible, datasetSchema, table, field }) => {
  const [creationFormState, creationFormDispatch] = useReducer(
    createValidationReducer,
    createValidationReducerInitState
  );

  const ruleDisablingCheckListener = [creationFormState.candidateRule.table, creationFormState.candidateRule.field];
  const ruleAdditionCheckListener = [creationFormState.areRulesDisabled, creationFormState.candidateRule];
  const validationCreationCheckListener = [ruleAdditionCheckListener, creationFormState.candidateRule];

  const setFormField = field => {
    creationFormDispatch({
      type: 'SET_FORM_FIELD',
      payload: field
    });
  };

  const setValidationRule = (ruleId, ruleProperty) => {
    if (ruleProperty.key == 'group') {
      const { groupCandidate } = creationFormState;
      if (ruleProperty.value.value) {
        groupCandidate.push(ruleId);
      } else {
        pull(groupCandidate, ruleId);
      }
      creationFormDispatch({
        type: 'GROUP_RULES_ACTIVATOR',
        payload: {
          groupRulesActive: ruleProperty.value.value ? 1 : -1
        }
      });
    }
    const {
      candidateRule: { rules }
    } = creationFormState;
    const [targetRule] = rules.filter(fRule => ruleId === fRule.ruleId);
    targetRule[ruleProperty.key] = ruleProperty.value.value;
    creationFormDispatch({
      type: 'UPDATE_RULES',
      payload: rules
    });
  };

  const getEmptyRule = () => {
    const ruleId = uuid.v4();
    return {
      ruleId,
      group: false,
      union: '',
      operatorType: '',
      operatorValue: '',
      ruleValue: '',
      rules: []
    };
  };

  const addEmptyRule = () => {
    creationFormDispatch({
      type: 'ADD_EMPTY_RULE',
      payload: getEmptyRule()
    });
  };

  const deleteRule = ruleId => {
    const {
      candidateRule: { rules }
    } = creationFormState;
    const [deleteCandidate] = rules.filter(rule => rule.ruleId == ruleId);
    if (rules.length > 1) {
      if (deleteCandidate.rules.length === 0) {
        const remainRules = pullAllWith(rules, [deleteCandidate], isEqual);
        creationFormDispatch({
          type: 'UPDATE_RULES',
          payload: remainRules
        });
      }
    } else {
      const rulesKey = Object.keys(deleteCandidate);
      rulesKey.forEach(ruleKey => {
        if (ruleKey != 'ruleId') {
          if (ruleKey == 'rules') {
            deleteCandidate[ruleKey] = [];
          } else {
            deleteCandidate[ruleKey] = '';
          }
        }
      });
      creationFormDispatch({
        type: 'UPDATE_RULES',
        payload: [deleteCandidate]
      });
    }
  };
  const groupRules = () => {
    if (creationFormState.groupRulesActive >= 2) {
      //take firs rule to group position in array
      const {
        candidateRule: { rules }
      } = creationFormState;
      const [firstId, restIds] = creationFormState.groupCandidate;
      const firstRulePosition = findIndex(rules, rule => rule.ruleId == firstId);

      //get to rules and remove from rules
      const rulesToGroup = rules.filter(rule => creationFormState.groupCandidate.includes(rule.ruleId));

      // compose group rule
      const newGroup = getEmptyRule();
      const [firstGroupRule] = rulesToGroup;
      newGroup.union = firstGroupRule.union;
      newGroup.rules = rulesToGroup;

      // add to rules in first rule to group position
      rules.splice(firstRulePosition, 0, newGroup);

      //remove groupedElements from array
      pullAllWith(rules, rulesToGroup, isEqual);

      creationFormDispatch({
        type: 'UPDATE_RULES',
        payload: rules
      });
    }
  };

  //set table tada
  useEffect(() => {
    console.log('*'.repeat(60));
    console.log('datasetSchema', datasetSchema);
    console.log();
    console.log();
    const { tables: rawTables, levelErrorTypes } = datasetSchema;
    rawTables.pop();
    const tables = rawTables.map(table => {
      return { label: table.tableSchemaName, code: table.recordSchemaId };
    });
    const errorLevels = levelErrorTypes.map(levelErrorType => ({
      label: capitalize(levelErrorType.toLowerCase()),
      value: levelErrorType
    }));
    creationFormDispatch({ type: 'INIT_FORM', payload: { tables, errorLevels, rules: [getEmptyRule()] } });
  }, []);

  //set field data
  useEffect(() => {
    const { table: tableInContext } = creationFormState.candidateRule;
    if (!isEmpty(tableInContext)) {
      const [selectedTable] = datasetSchema.tables.filter(table => table.recordSchemaId === tableInContext.code);
      const fields =
        !isUndefined(selectedTable) && !isEmpty(selectedTable)
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

  const getRuleString = (rules, field) => {
    let ruleString = '';
    if (!isUndefined(field) && rules.length > 0) {
      const { label: fieldLabel } = field;
      rules.forEach((rule, i) => {
        const { union: unionValue, operatorValue: operator, ruleValue, rules } = rule;
        if (!isUndefined(operator) && !isUndefined(ruleValue)) {
          const ruleLeft = `${fieldLabel} ${operator} ${ruleValue}`;
          if (i == 0) {
            ruleString = `${ruleString} ${ruleLeft}`;
          } else {
            if (!isUndefined(unionValue)) {
              ruleString = `${ruleString} ${unionValue} ${ruleLeft}`;
            }
          }
        }
      });
    }
    return ruleString;
  };

  //create result rule
  const createResultString = () => {
    const {
      candidateRule: { field, rules }
    } = creationFormState;

    creationFormDispatch({
      type: 'SET_EXPRESIONS_STRING',
      payload: getRuleString(rules, field)
    });
  };
  useEffect(() => {
    creationFormDispatch({
      type: 'UPDATE_RESULT_RULE_STRING',
      payload: createResultString(creationFormState.candidateRule.rules)
    });
  }, [creationFormState.candidateRule]);

  //create exchange structure

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
  const checkRulefilled = () => {
    const {
      candidateRule: { rules }
    } = creationFormState;
    if (!isUndefined(rules)) {
      const lastRule = last(rules);
      if (lastRule.rules && lastRule.rules.length > 0) {
        return true;
      } else {
        const deactivate =
          isEmpty(lastRule.union) ||
          isEmpty(lastRule.operatorType) ||
          isEmpty(lastRule.operatorValue) ||
          isEmpty(lastRule.ruleValue);
        return deactivate;
      }
    }
    return true;
  };
  useEffect(() => {
    creationFormDispatch({
      type: 'SET_IS_VALIDATION_ADDING_DISABLED',
      payload: checkRulefilled()
    });
  }, [...ruleAdditionCheckListener]);

  const dialogLayout = children => (
    <Dialog header="Create Field Validation rule" visible={isVisible} style={{ width: '70%' }}>
      {children}
    </Dialog>
  );

  return dialogLayout(
    <div>
      <form action="">
        <div id={styles.QCFormWrapper}>
          <div className={styles.section}>
            <div>
              <label htmlFor="table">
                Table
                <Dropdown
                  appendTo={document.body}
                  placeholder="Table"
                  optionLabel="label"
                  options={creationFormState.schemaTables}
                  onChange={e => {
                    setFormField({
                      key: 'table',
                      value: e.target.value
                    });
                  }}
                  value={creationFormState.candidateRule.table}
                />
              </label>
              <label htmlFor="field">
                Field
                <Dropdown
                  appendTo={document.body}
                  placeholder="field"
                  optionLabel="label"
                  options={creationFormState.tableFields}
                  onChange={e => {
                    setFormField({
                      key: 'field',
                      value: e.target.value
                    });
                  }}
                  value={creationFormState.candidateRule.field}
                />
              </label>
              <label htmlFor="shortCode">
                Short code
                <InputText
                  placeholder="short code"
                  value={creationFormState.candidateRule.shortCode}
                  onChange={e => {
                    setFormField({
                      key: 'shortCode',
                      value: e.target.value
                    });
                  }}
                />
              </label>
              <label htmlFor="description">
                Description
                <InputText
                  placeholder="Description"
                  value={creationFormState.candidateRule.description}
                  onChange={e => {
                    setFormField({
                      key: 'description',
                      value: e.target.value
                    });
                  }}
                />
              </label>
              <label htmlFor="errorMessage" className={styles.errorMessage}>
                Error message
                <InputText
                  placeholder="errorMessage"
                  value={creationFormState.candidateRule.errorMessage}
                  onChange={e => {
                    setFormField({
                      key: 'errorMessage',
                      value: e.target.value
                    });
                  }}
                />
              </label>
              <label htmlFor="description">
                Error type
                <Dropdown
                  placeholder="Error type"
                  appendTo={document.body}
                  optionLabel="label"
                  options={creationFormState.errorLevels}
                  onChange={e => {
                    setFormField({
                      key: 'errorType',
                      value: e.target.value
                    });
                  }}
                  value={creationFormState.candidateRule.errorLevel}
                />
              </label>
            </div>
            <div>
              <label htmlFor="QcActive">
                Active
                <Checkbox
                  onChange={e => {
                    setFormField({ key: 'active', value: e.checked });
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
                  <th>Group</th>
                  <th>AND / OR</th>
                  <th>Opertators type</th>
                  <th>Operator</th>
                  <th>Value</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {creationFormState.candidateRule.rules &&
                  creationFormState.candidateRule.rules.map(rule => (
                    <ValidationRule
                      isDisabled={creationFormState.areRulesDisabled}
                      setValidationRule={setValidationRule}
                      deleteRule={deleteRule}
                      ruleValues={rule}
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
                  groupRules();
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
                label="Add new rule"
                icon="plus"
                onClick={e => addEmptyRule()}
              />
            </div>
            <div>
              <Button
                disabled={creationFormState.isValidationCreationDisabled}
                className="p-button-primary p-button-text-icon-left"
                type="button"
                label="create"
                icon="check"
              />
              <Button
                className="p-button-secondary p-button-text-icon-left"
                type="button"
                label="cancel"
                icon="cancel"
              />
            </div>
          </div>
        </div>
      </form>
    </div>
  );
};

export { CreateValidation };
