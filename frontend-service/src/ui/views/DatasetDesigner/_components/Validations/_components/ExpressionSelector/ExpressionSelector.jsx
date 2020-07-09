import React, { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ExpressionSelector.module.scss';

import { Dropdown } from 'primereact/dropdown';
import { FieldComparison } from 'ui/views/DatasetDesigner/_components/Validations/_components/FieldComparison';
import { IfThenClause } from 'ui/views/DatasetDesigner/_components/Validations/_components/IfThenClause';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

export const ExpressionSelector = ({
  componentName,
  creationFormState,
  onAddNewExpression,
  onAddNewExpressionIf,
  onAddNewExpressionThen,
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
  tabsChanges
}) => {
  const resources = useContext(ResourcesContext);
  const validationContext = useContext(ValidationContext);

  const [expressionTypeValue, setExpressionTypeValue] = useState('');

  const options = [
    { label: resources.messages['fieldComparisonLabel'], value: 'fieldComparison' },
    { label: resources.messages['ifThenLabel'], value: 'ifThenClause' }
  ];
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
            tabsChanges={tabsChanges}
            onGetFieldType={onGetFieldType}
          />
        </>
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
    return <></>;
  };
  return (
    <>
      <div className={styles.section} style={validationContext.ruleEdit ? { display: 'none' } : {}}>
        <Dropdown
          onChange={e => onExpressionTypeToggle(e.value)}
          optionLabel="label"
          options={options}
          placeholder={resources.messages['expressionTypeDropdownPlaceholder']}
          style={{ width: '12em' }}
          value={expressionTypeValue}
        />
      </div>

      <div className={styles.section}>{expressionsTypeView()} </div>
    </>
  );
};
