import { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';

import styles from './ExpressionSelector.module.scss';

import { Dropdown } from 'ui/views/_components/Dropdown';
import { ExpressionsTab } from 'ui/views/DatasetDesigner/_components/Validations/_components/ExpressionsTab';
import { FieldComparison } from 'ui/views/DatasetDesigner/_components/Validations/_components/FieldComparison';
import { IfThenClause } from 'ui/views/DatasetDesigner/_components/Validations/_components/IfThenClause';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SQLsentence } from 'ui/views/DatasetDesigner/_components/Validations/_components/SQLsentence';
import { TableRelationsSelector } from 'ui/views/DatasetDesigner/_components/Validations/_components/TableRelationsSelector';
import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

export const ExpressionSelector = ({
  componentName,
  creationFormState,
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
  onSetSQLsentence,
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
        <SQLsentence
          creationFormState={creationFormState}
          level={validationContext.level}
          onSetSQLsentence={onSetSQLsentence}
        />
      );
    }
    return <div />;
  };
  return (
    <>
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
    </>
  );
};
