import { Fragment, useContext } from 'react';

import styles from './FieldComparison.module.scss';

import { Button } from 'views/_components/Button';
import { ValidationExpressionSelector } from 'views/DatasetDesigner/_components/Validations/_components/ValidationExpressionSelector';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const FieldComparison = ({
  componentName,
  creationFormState,
  isBusinessDataflow,
  onAddNewExpression,
  onExpressionDelete,
  onExpressionFieldUpdate,
  onExpressionGroup,
  onExpressionMarkToGroup,
  onExpressionsErrors,
  onGetFieldType,
  tabsChanges
}) => {
  const resourcesContext = useContext(ResourcesContext);

  return (
    <Fragment>
      <div className={styles.section}>
        <ul className={styles.list}>
          {creationFormState.candidateRule.expressions &&
            creationFormState.candidateRule.expressions.map((expression, i) => (
              <ValidationExpressionSelector
                expressionType={creationFormState.candidateRule.expressionType}
                expressionValues={expression}
                fieldType={creationFormState.candidateRule.fieldType}
                isBusinessDataflow={isBusinessDataflow}
                isDisabled={creationFormState.areRulesDisabled}
                key={expression.expressionId}
                onExpressionDelete={onExpressionDelete}
                onExpressionFieldUpdate={onExpressionFieldUpdate}
                onExpressionGroup={onExpressionMarkToGroup}
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
            onClick={() => onAddNewExpression()}
            type="button"
          />
          {creationFormState.groupExpressionsActive >= 2 && (
            <Button
              className="p-button-primary p-button-text"
              icon="plus"
              id={`${componentName}__groupExpresions`}
              label="Group"
              onClick={() => onExpressionGroup()}
              type="button"
            />
          )}
        </div>
      </div>
      <div className={styles.section}>
        <textarea
          cols="30"
          id="fieldComparisonTextarea"
          name=""
          readOnly
          rows="5"
          value={creationFormState.validationRuleString}></textarea>
      </div>
    </Fragment>
  );
};
