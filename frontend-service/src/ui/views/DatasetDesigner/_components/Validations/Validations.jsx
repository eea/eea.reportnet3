import React, { useContext } from 'react';

import styles from './Validations.module.scss';

import { DatasetValidation } from './_components/DatasetValidation';
import { FieldValidation } from './_components/FieldValidation';
import { RowValidation } from './_components/RowValidation';

import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

export const Validations = ({ datasetSchema, datasetSchemas, tabs, datasetId }) => {
  const validationContext = useContext(ValidationContext);

  return validationContext.level === 'field' ? (
    <FieldValidation tabs={tabs} datasetId={datasetId} />
  ) : validationContext.level === 'row' ? (
    <RowValidation tabs={tabs} datasetId={datasetId} />
  ) : (
    <DatasetValidation
      tabs={tabs}
      datasetId={datasetId}
      datasetSchema={datasetSchema}
      datasetSchemas={datasetSchemas}
    />
  );
};
