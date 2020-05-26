import React, { useContext } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ExpressionSelector.module.scss';

import { Dropdown } from 'primereact/dropdown';
import { FieldComparison } from 'ui/views/DatasetDesigner/_components/Validations/_components/FieldComparison';
import { IfThenClause } from 'ui/views/DatasetDesigner/_components/Validations/_components/IfThenClause';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const ExpressionSelector = ({
  onExpressionTypeToggle,
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
  const resources = useContext(ResourcesContext);
  const options = [
    { label: resources.messages['fieldComparisonLabel'], value: 'fieldComparison' },
    { label: resources.messages['ifThenLabel'], value: 'ifThenClause' }
  ];
  const {
    candidateRule: { expressionType }
  } = creationFormState;
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
          />
        </>
      );
    }
    if (!isEmpty(expressionType) && expressionType === 'ifThenClause') {
      return <IfThenClause />;
    }
    return <></>;
  };
  return (
    <>
      <div className={styles.section}>
        <Dropdown
          value={expressionType}
          options={options}
          onChange={e => onExpressionTypeToggle(e.value)}
          placeholder={resources.messages['expressionTypeDropdownPlaceholder']}
          optionLabel="label"
        />
      </div>
      <div className={styles.section}>{expressionsTypeView()}</div>
    </>
  );
};
