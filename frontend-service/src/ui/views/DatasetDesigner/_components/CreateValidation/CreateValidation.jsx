import React, { useEffect, useReducer, useContext } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import pull from 'lodash/pull';

import styles from './CreateValidation.module.scss';

import { config } from 'conf/';

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
import { getDatasetSchemaTableFields } from './_functions/utils/getDatasetSchemaTableFields';
import { getEmptyExpresion } from './_functions/utils/getEmptyExpresion';
import { getExpresionString } from './_functions/utils/getExpresionString';
import { groupExpresions } from './_functions/utils/groupExpresions';
import { initValidationRuleCreation } from './_functions/utils/initValidationRuleCreation';
import { resetValidationRuleCreation } from './_functions/utils/resetValidationRuleCreation';
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

  useEffect(() => {
    creationFormDispatch({ type: 'INIT_FORM', payload: initValidationRuleCreation(datasetSchema.tables) });
  }, []);

  useEffect(() => {
    const { table } = creationFormState.candidateRule;
    if (!isEmpty(table)) {
      creationFormDispatch({
        type: 'SET_FIELDS',
        payload: getDatasetSchemaTableFields(table, datasetSchema.tables)
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

  useEffect(() => {
    creationFormDispatch({
      type: 'SET_IS_VALIDATION_CREATION_DISABLED',
      payload: !checkValidation(creationFormState.candidateRule)
    });
  }, [creationFormState.candidateRule]);

  const checkActivateRules = () => {
    return creationFormState.candidateRule.table && creationFormState.candidateRule.field;
  };

  const checkDesactivateRules = () => {
    return (
      (!creationFormState.candidateRule.table || !creationFormState.candidateRule.field) &&
      !creationFormState.areRulesDisabled
    );
  };

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

  const onExpresionDelete = expresionId => {
    const {
      candidateRule: { expresions }
    } = creationFormState;
    creationFormDispatch({
      type: 'UPDATE_RULES',
      payload: deleteExpresion(expresionId, expresions)
    });
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

  const onHide = () => {
    creationFormDispatch({ type: 'RESET_CREATION_FORM', payload: resetValidationRuleCreation() });
    toggleVisibility(false);
  };

  const dialogLayout = children => (
    <Dialog header="Create Field Validation rule" visible={isVisible} style={{ width: '90%' }} onHide={e => onHide()}>
      {children}
    </Dialog>
  );

  return dialogLayout(
    <form action="">
      <div id={styles.QCFormWrapper}>
        <div className={styles.section}>
          <div className={styles.subsection}>
            <div className={styles.field}>
              <label htmlFor="table">{resourcesContext.messages.table}</label>
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
            </div>
            <div className={styles.field}>
              <label htmlFor="field">{resourcesContext.messages.field}</label>
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
            </div>
            <div className={styles.field}>
              <label htmlFor="shortCode">{resourcesContext.messages.ruleShortCode}</label>
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
            </div>
            <div className={styles.field}>
              <label htmlFor="description">{resourcesContext.messages.description}</label>
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
            </div>
            <div className={`${styles.field} ${styles.errorMessage}`}>
              <label htmlFor="errorMessage">{resourcesContext.messages.errorMessage}</label>
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
            </div>
            <div className={styles.field}>
              <label htmlFor="description">{resourcesContext.messages.errorType}</label>
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
            </div>
          </div>
          <div className={styles.subsection}>
            <div className={`${styles.field} ${styles.qcActive}`}>
              <label htmlFor="QcActive">{resourcesContext.messages.active}</label>
              <Checkbox
                onChange={e => {
                  setFormField({ key: 'active', value: e.checked }, creationFormDispatch);
                }}
                isChecked={creationFormState.candidateRule.active}
              />
            </div>
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
          <div className={styles.subsection}>
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
          <div className={styles.subsection}>
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
              onClick={e => onHide()}
            />
          </div>
        </div>
      </div>
    </form>
  );
};

export { CreateValidation };
