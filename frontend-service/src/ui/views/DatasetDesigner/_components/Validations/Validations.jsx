import { useContext } from 'react';

import { TableValidation } from './_components/TableValidation';
import { FieldValidation } from './_components/FieldValidation';
import { RowValidation } from './_components/RowValidation';

import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

export const Validations = ({ datasetSchema, datasetSchemas, tabs, datasetId }) => {
  const validationContext = useContext(ValidationContext);

  if (validationContext.level === 'field') {
    return <FieldValidation datasetId={datasetId} tabs={tabs} />;
  }

  if (validationContext.level === 'row') {
    return <RowValidation datasetId={datasetId} tabs={tabs} />;
  }

  return (
    <TableValidation datasetId={datasetId} datasetSchema={datasetSchema} datasetSchemas={datasetSchemas} tabs={tabs} />
  );
};
