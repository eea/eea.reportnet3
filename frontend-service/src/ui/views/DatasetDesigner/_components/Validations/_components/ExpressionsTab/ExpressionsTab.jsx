import React, { useContext } from 'react';

import isNil from 'lodash/isNil';

import styles from './ExpressionsTab.module.scss';

import { Button } from 'ui/views/_components/Button';
import { ValidationExpressionSelector } from 'ui/views/DatasetDesigner/_components/Validations/_components/ValidationExpressionSelector';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const ExpressionsTab = ({
  componentName,
  creationFormState,
  onExpressionDelete,
  onExpressionFieldUpdate,
  onExpressionMarkToGroup,
  tabsChanges,
  onAddNewExpression,
  onExpressionGroup,
  onExpressionsErrors
}) => {
  const resourcesContext = useContext(ResourcesContext);
  return (
    <React.Fragment>
      <div className={styles.section}>
        <ul>
          {creationFormState.candidateRule.expressions &&
            creationFormState.candidateRule.expressions.map((expression, i) => (
              <ValidationExpressionSelector
                expressionValues={expression}
                isDisabled={creationFormState.areRulesDisabled}
                key={expression.expressionId}
                onExpressionDelete={onExpressionDelete}
                onExpressionFieldUpdate={onExpressionFieldUpdate}
                onExpressionGroup={onExpressionMarkToGroup}
                onExpressionsErrors={onExpressionsErrors}
                position={i}
                showRequiredFields={tabsChanges.expression}
              />
            ))}
        </ul>
        <div className={styles.expressionsActionsBtns}>
          <Button
            id={`${componentName}__addExpresion`}
            disabled={creationFormState.isRuleAddingDisabled}
            className="p-button-primary p-button-text-icon-left"
            type="button"
            label={resourcesContext.messages.addNewRule}
            icon="plus"
            onClick={e => onAddNewExpression()}
          />
          {creationFormState.groupExpressionsActive >= 2 && (
            <Button
              id={`${componentName}__groupExpresions`}
              className="p-button-primary p-button-text"
              type="button"
              label="Group"
              icon="plus"
              onClick={e => onExpressionGroup()}
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
