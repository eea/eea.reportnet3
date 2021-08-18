import { Fragment, useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';

import styles from './ExpressionSelector.module.scss';

import { Dropdown } from 'views/_components/Dropdown';
import { ExpressionsTab } from 'views/DatasetDesigner/_components/Validations/_components/ExpressionsTab';
import { FieldComparison } from 'views/DatasetDesigner/_components/Validations/_components/FieldComparison';
import { IfThenClause } from 'views/DatasetDesigner/_components/Validations/_components/IfThenClause';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { SqlSentence } from 'views/DatasetDesigner/_components/Validations/_components/SqlSentenceAux';
import { TableRelationsSelector } from 'views/DatasetDesigner/_components/Validations/_components/TableRelationsSelector';
import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

export const ExpressionSelector = ({
  componentName,
  creationFormState,
  isBusinessDataflow,
  onAddNewExpression,
  onAddNewExpressionIf,
  onAddNewExpressionThen,
  onAddNewRelation,
  onDatasetSchemaChange,
  onDoubleReferencedChange,
  onExpressionDelete,
  onExpressionFieldUpdate,
  onExpressionGroup,
  onExpressionIfDelete,
  onExpressionIfFieldUpdate,
  onExpressionIfGroup,
  onExpressionIfMarkToGroup,
  onExpressionMarkToGroup,
  onExpressionsErrors,
  onExpressionThenDelete,
  onExpressionThenFieldUpdate,
  onExpressionThenGroup,
  onExpressionThenMarkToGroup,
  onExpressionTypeToggle,
  onGetFieldType,
  onReferencedTableChange,
  onRelationDelete,
  onRelationFieldUpdate,
  onRelationsErrors,
  onSetSqlSentence,
  tabsChanges
}) => {
  const resources = useContext(ResourcesContext);
  const validationContext = useContext(ValidationContext);

  const [expressionTypeValue, setExpressionTypeValue] = useState('');

  const getOptions = () => {
    if (validationContext.level === 'field') {
      const {
        validations: {
          bannedTypes: { nonSql }
        }
      } = config;
      const {
        candidateRule: { fieldType }
      } = creationFormState;
      if (nonSql.includes(fieldType?.toLowerCase())) {
        return [{ label: resources.messages['sqlSentence'], value: 'sqlSentence' }];
      }
      return [
        { label: resources.messages['fieldComparisonLabel'], value: 'fieldTab' },
        { label: resources.messages['sqlSentence'], value: 'sqlSentence' }
      ];
    }

    if (validationContext.level === 'row') {
      return [
        { label: resources.messages['fieldComparisonLabel'], value: 'fieldComparison' },
        { label: resources.messages['ifThenLabel'], value: 'ifThenClause' },
        { label: resources.messages['sqlSentence'], value: 'sqlSentence' }
      ];
    }

    return [
      { label: resources.messages['datasetComparison'], value: 'fieldRelations' },
      { label: resources.messages['sqlSentence'], value: 'sqlSentence' }
    ];
  };

  const {
    candidateRule: { expressionType }
  } = creationFormState;

  useEffect(() => {
    setExpressionTypeValue(expressionType);
  }, [expressionType]);

  const getSelectorValue = () => {
    const option = getOptions().filter(option => option.value === expressionTypeValue);
    const [selectedOption] = option;

    return selectedOption;
  };
  const expressionsTypeView = () => {
    if (!isEmpty(expressionType) && expressionType === 'fieldComparison') {
      return (
        <FieldComparison
          componentName={componentName}
          creationFormState={creationFormState}
          isBusinessDataflow={isBusinessDataflow}
          onAddNewExpression={onAddNewExpression}
          onExpressionDelete={onExpressionDelete}
          onExpressionFieldUpdate={onExpressionFieldUpdate}
          onExpressionGroup={onExpressionGroup}
          onExpressionMarkToGroup={onExpressionMarkToGroup}
          onExpressionsErrors={onExpressionsErrors}
          onGetFieldType={onGetFieldType}
          tabsChanges={tabsChanges}
        />
      );
    }

    if (!isEmpty(expressionType) && expressionType === 'fieldTab') {
      return (
        <ExpressionsTab
          componentName={componentName}
          creationFormState={creationFormState}
          isBusinessDataflow={isBusinessDataflow}
          onAddNewExpression={onAddNewExpression}
          onExpressionDelete={onExpressionDelete}
          onExpressionFieldUpdate={onExpressionFieldUpdate}
          onExpressionGroup={onExpressionGroup}
          onExpressionMarkToGroup={onExpressionMarkToGroup}
          onExpressionsErrors={onExpressionsErrors}
          tabsChanges={tabsChanges}
        />
      );
    }

    if (!isEmpty(expressionType) && expressionType === 'fieldRelations') {
      return (
        <TableRelationsSelector
          componentName={componentName}
          creationFormState={creationFormState}
          onAddNewRelation={onAddNewRelation}
          onDatasetSchemaChange={onDatasetSchemaChange}
          onDoubleReferencedChange={onDoubleReferencedChange}
          onExpressionTypeToggle={onExpressionTypeToggle}
          onGetFieldType={onGetFieldType}
          onReferencedTableChange={onReferencedTableChange}
          onRelationDelete={onRelationDelete}
          onRelationFieldUpdate={onRelationFieldUpdate}
          onRelationsErrors={onRelationsErrors}
          showRequiredFields={tabsChanges.expression}
          tabsChanges={tabsChanges}
        />
      );
    }

    if (!isEmpty(expressionType) && expressionType === 'ifThenClause') {
      return (
        <IfThenClause
          componentName={componentName}
          creationFormState={creationFormState}
          isBusinessDataflow={isBusinessDataflow}
          onAddNewExpressionIf={onAddNewExpressionIf}
          onAddNewExpressionThen={onAddNewExpressionThen}
          onExpressionIfDelete={onExpressionIfDelete}
          onExpressionIfFieldUpdate={onExpressionIfFieldUpdate}
          onExpressionIfGroup={onExpressionIfGroup}
          onExpressionIfMarkToGroup={onExpressionIfMarkToGroup}
          onExpressionThenDelete={onExpressionThenDelete}
          onExpressionThenFieldUpdate={onExpressionThenFieldUpdate}
          onExpressionThenGroup={onExpressionThenGroup}
          onExpressionThenMarkToGroup={onExpressionThenMarkToGroup}
          onExpressionsErrors={onExpressionsErrors}
          onGetFieldType={onGetFieldType}
          tabsChanges={tabsChanges}
        />
      );
    }

    if (!isEmpty(expressionType) && expressionType === 'sqlSentence') {
      return (
        <SqlSentence
          creationFormState={creationFormState}
          isBusinessDataflow={isBusinessDataflow}
          level={validationContext.level}
          onSetSqlSentence={onSetSqlSentence}
        />
      );
    }
    return null;
  };
  return (
    <Fragment>
      {!validationContext.ruleEdit && (
        <div className={styles.section}>
          <Dropdown
            appendTo={document.body}
            onChange={e => onExpressionTypeToggle(e.value.value)}
            optionLabel="label"
            options={getOptions()}
            placeholder={resources.messages['expressionTypeDropdownPlaceholder']}
            style={{ width: '12em' }}
            value={getSelectorValue()}
          />
        </div>
      )}

      <div className={styles.section}>{expressionsTypeView()} </div>
    </Fragment>
  );
};
