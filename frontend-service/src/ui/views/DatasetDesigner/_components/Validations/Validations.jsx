import React, { useContext } from 'react';

import styles from './Validations.module.scss';

import { FieldValidation } from './_components/FieldValidation';
import { RowValidation } from './_components/RowValidation';

import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

export const Validations = ({ tabs, datasetId }) => {
  const validationContext = useContext(ValidationContext);

  return validationContext.level === 'field' ? (
    <FieldValidation tabs={tabs} datasetId={datasetId} />
  ) : (
    <RowValidation tabs={tabs} datasetId={datasetId} />
  );
};
