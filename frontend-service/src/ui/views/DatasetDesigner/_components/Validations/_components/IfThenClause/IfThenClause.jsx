import React, { useContext } from 'react';

import styles from './IfThenClause.module.scss';

import { Button } from 'ui/views/_components/Button';
import { ValidationExpressionSelector } from 'ui/views/DatasetDesigner/_components/Validations/_components/ValidationExpressionSelector';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const IfThenClause = ({
  componentName,
  creationFormState,
  onAddNewExpressionIf,
  onAddNewExpressionThen,
  onExpressionIfDelete,
  onExpressionIfFieldUpdate,
  onExpressionIfGroup,
  onExpressionIfMarkToGroup,
  onExpressionsErrors,
  onExpressionThenDelete,
  onExpressionThenFieldUpdate,
  onExpressionThenGroup,
  onExpressionThenMarkToGroup,
  onGetFieldType,
  tabsChanges
}) => {
  const resourcesContext = useContext(ResourcesContext);

  return (
    <React.Fragment>
      <div className={styles.section}>
        <h3 className="if">IF</h3>
        <ul className={styles.list}>
          {creationFormState.candidateRule.expressionsIf &&
            creationFormState.candidateRule.expressionsIf.map((expression, i) => (
              <ValidationExpressionSelector
                expressionType={creationFormState.candidateRule.expressionType}
                expressionValues={expression}
                isDisabled={creationFormState.areRulesDisabled}
                key={expression.expressionId}
                onExpressionDelete={onExpressionIfDelete}
                onExpressionFieldUpdate={onExpressionIfFieldUpdate}
                onExpressionGroup={onExpressionIfMarkToGroup}
                onExpressionsErrors={onExpressionsErrors}
                onGetFieldType={onGetFieldType}
                position={i}
                rawTableFields={creationFormState.tableFields}
                showRequiredFields={tabsChanges.expression}
              />
            ))}
        </ul>

        <div className={styles.expressionsActionsBtns}>
          <Button
            className="p-button-primary p-button-text-icon-left"
            disabled={creationFormState.isRuleAddingDisabled}
            icon="plus"
            id={`${componentName}__addExpresion`}
            label={resourcesContext.messages.addNewRule}
            onClick={() => onAddNewExpressionIf()}
            type="button"
          />
          {creationFormState.groupExpressionsActive >= 2 && (
            <Button
              className="p-button-primary p-button-text"
              icon="plus"
              id={`${componentName}__groupExpresions`}
              label="Group"
              onClick={() => onExpressionIfGroup()}
              type="button"
            />
          )}
        </div>
      </div>
      <hr></hr>
      <div className={styles.section}>
        <h3 className="then">THEN</h3>
        <ul className={styles.list}>
          {creationFormState.candidateRule.expressionsThen &&
            creationFormState.candidateRule.expressionsThen.map((expression, i) => (
              <ValidationExpressionSelector
                expressionType={creationFormState.candidateRule.expressionType}
                expressionValues={expression}
                isDisabled={creationFormState.areRulesDisabled}
                key={expression.expressionId}
                onExpressionDelete={onExpressionThenDelete}
                onExpressionFieldUpdate={onExpressionThenFieldUpdate}
                onExpressionGroup={onExpressionThenMarkToGroup}
                onExpressionsErrors={onExpressionsErrors}
                onGetFieldType={onGetFieldType}
                position={i}
                rawTableFields={creationFormState.tableFields}
                showRequiredFields={tabsChanges.expression}
              />
            ))}
        </ul>

        <div className={styles.expressionsActionsBtns}>
          <Button
            className="p-button-primary p-button-text-icon-left"
            disabled={creationFormState.isRuleAddingDisabled}
            icon="plus"
            id={`${componentName}__addExpresion`}
            label={resourcesContext.messages.addNewRule}
            onClick={() => onAddNewExpressionThen()}
            type="button"
          />
          {creationFormState.groupExpressionsActive >= 2 && (
            <Button
              className="p-button-primary p-button-text"
              icon="plus"
              id={`${componentName}__groupExpresions`}
              label="Group"
              onClick={() => onExpressionThenGroup()}
              type="button"
            />
          )}
        </div>
      </div>

      <div className={styles.section}>
        <textarea name="" id="" cols="30" readOnly rows="5" value={creationFormState.validationRuleString}></textarea>
      </div>
    </React.Fragment>
  );
};
