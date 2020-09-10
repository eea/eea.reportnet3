import React, { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ExpressionSelector.module.scss';

import { Dropdown } from 'primereact/dropdown';
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
  onInfoFieldChange,
  onReferencedTableChange,
  onRelationDelete,
  onRelationFieldUpdate,
  onRelationsErrors,
  tabsChanges
}) => {
  const resources = useContext(ResourcesContext);
  const validationContext = useContext(ValidationContext);

  const [expressionTypeValue, setExpressionTypeValue] = useState('');

  const getOptions = () => {
    if (validationContext.level === 'field') {
      return [
        { label: resources.messages['fieldComparisonLabel'], value: 'fieldTab' },
        { label: resources.messages['SQLsentence'], value: 'SQLsentence' }
      ];
    }

    if (validationContext.level === 'row') {
      return [
        { label: resources.messages['fieldComparisonLabel'], value: 'fieldComparison' },
        { label: resources.messages['ifThenLabel'], value: 'ifThenClause' },
        { label: resources.messages['SQLsentence'], value: 'SQLsentence' }
      ];
    }

    return [
      { label: resources.messages['datasetComparison'], value: 'fieldRelations' },
      { label: resources.messages['SQLsentence'], value: 'SQLsentence' }
    ];
  };

  const {
    candidateRule: { expressionType }
  } = creationFormState;

  useEffect(() => {
    setExpressionTypeValue(expressionType);
  }, [expressionType]);

  const expressionsTypeView = () => {
    if (!isEmpty(expressionType) && expressionType === 'fieldComparison') {
      return (
        <>
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
        </>
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
          onInfoFieldChange={onInfoFieldChange}
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
          onExpressionThenDelete={onExpressionThenDelete}
          onExpressionIfFieldUpdate={onExpressionIfFieldUpdate}
          onExpressionThenFieldUpdate={onExpressionThenFieldUpdate}
          onExpressionIfGroup={onExpressionIfGroup}
          onExpressionThenGroup={onExpressionThenGroup}
          onExpressionIfMarkToGroup={onExpressionIfMarkToGroup}
          onExpressionThenMarkToGroup={onExpressionThenMarkToGroup}
          onExpressionsErrors={onExpressionsErrors}
          tabsChanges={tabsChanges}
          onGetFieldType={onGetFieldType}
        />
      );
    }

    if (!isEmpty(expressionType) && expressionType === 'SQLsentence') {
      return <SQLsentence creationFormState={creationFormState} />;
    }
    return <></>;
  };
  return (
    <>
      <div className={styles.section} style={validationContext.ruleEdit ? { display: 'none' } : {}}>
        <Dropdown
          onChange={e => onExpressionTypeToggle(e.value)}
          optionLabel="label"
          options={getOptions()}
          placeholder={resources.messages['expressionTypeDropdownPlaceholder']}
          style={{ width: '12em' }}
          value={expressionTypeValue}
        />
      </div>

      <div className={styles.section}>{expressionsTypeView()} </div>
    </>
  );
};
