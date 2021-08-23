import { Fragment, useContext } from 'react';

import styles from './IfThenClause.module.scss';

import { Button } from 'views/_components/Button';
import { ValidationExpressionSelector } from 'views/DatasetDesigner/_components/Validations/_components/ValidationExpressionSelector';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const IfThenClause = ({
  componentName,
  creationFormState,
  isBusinessDataflow,
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
    <Fragment>
      <div className={styles.section}>
        <h3 className="if">{resourcesContext.messages['if']}</h3>
        <ul className={styles.list}>
          {creationFormState.candidateRule.expressionsIf &&
            creationFormState.candidateRule.expressionsIf.map((expression, i) => (
              <ValidationExpressionSelector
                expressionType={creationFormState.candidateRule.expressionType}
                expressionValues={expression}
                isBusinessDataflow={isBusinessDataflow}
                isDisabled={creationFormState.areRulesDisabledIf}
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
            disabled={creationFormState.isRuleAddingDisabledIf}
            icon="plus"
            id={`${componentName}__addExpresionIf`}
            label={resourcesContext.messages.addNewRule}
            onClick={() => onAddNewExpressionIf()}
            type="button"
          />
          {creationFormState.groupExpressionsIfActive >= 2 && (
            <Button
              className="p-button-primary p-button-text"
              icon="plus"
              id={`${componentName}__groupExpresionsIf`}
              label="Group"
              onClick={() => onExpressionIfGroup()}
              type="button"
            />
          )}
        </div>
      </div>
      <hr></hr>
      <div className={styles.section}>
        <h3 className="then">{resourcesContext.messages['then']}</h3>
        <ul className={styles.list}>
          {creationFormState.candidateRule.expressionsThen &&
            creationFormState.candidateRule.expressionsThen.map((expression, i) => (
              <ValidationExpressionSelector
                expressionType={creationFormState.candidateRule.expressionType}
                expressionValues={expression}
                isBusinessDataflow={isBusinessDataflow}
                isDisabled={creationFormState.areRulesDisabledThen}
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
            disabled={creationFormState.isRuleAddingDisabledThen}
            icon="plus"
            id={`${componentName}__addExpresionThen`}
            label={resourcesContext.messages.addNewRule}
            onClick={() => onAddNewExpressionThen()}
            type="button"
          />
          {creationFormState.groupExpressionsThenActive >= 2 && (
            <Button
              className="p-button-primary p-button-text"
              icon="plus"
              id={`${componentName}__groupExpresionsThen`}
              label="Group"
              onClick={() => onExpressionThenGroup()}
              type="button"
            />
          )}
        </div>
      </div>

      <div className={styles.section}>
        <textarea
          cols="30"
          id="ifThenClauseTextarea"
          name=""
          readOnly
          rows="5"
          value={creationFormState.expressionText}></textarea>
      </div>
    </Fragment>
  );
};
