import React from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ExpressionSelector.module.scss';

import { Dropdown } from 'primereact/dropdown';
import { FieldComparison } from 'ui/views/DatasetDesigner/_components/Validations/_components/FieldComparison';
import { IfThenClause } from 'ui/views/DatasetDesigner/_components/Validations/_components/IfThenClause';

export const ExpressionSelector = ({ onExpressionTypeToggle, creationFormState }) => {
  const options = [
    { label: 'Field comparison', value: 'fieldComparison' },
    { label: 'If-then clause', value: 'ifThenClause' }
  ];
  const {
    candidateRule: { expressionType }
  } = creationFormState;
  const expressionsTypeView = () => {
    if (!isEmpty(expressionType) && expressionType === 'fieldComparison') {
      return <FieldComparison />;
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
          placeholder={'Select Expression type'}
          optionLabel="label"
        />
      </div>
      <div className={styles.section}>{expressionsTypeView()}</div>
    </>
  );
};
