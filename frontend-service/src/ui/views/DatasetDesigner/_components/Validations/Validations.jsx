import React from 'react';

import styles from './Validations.module.scss';

import { FieldValidation } from './_components/FieldValidation';

export const Validations = ({ tabs, datasetId }) => {
  return <FieldValidation tabs={tabs} datasetId={datasetId} />;
};
