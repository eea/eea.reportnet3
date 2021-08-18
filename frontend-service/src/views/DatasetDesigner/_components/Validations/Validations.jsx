import { useContext } from 'react';

import { TableValidation } from './_components/TableValidation';
import { FieldValidation } from './_components/FieldValidation';
import { RowValidation } from './_components/RowValidation';

import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

export const Validations = ({ datasetSchema, datasetSchemas, isBusinessDataflow, tabs, datasetId }) => {
  const validationContext = useContext(ValidationContext);

  if (validationContext.level === 'field') {
    return <FieldValidation datasetId={datasetId} isBusinessDataflow={isBusinessDataflow} tabs={tabs} />;
  }

  if (validationContext.level === 'row') {
    return <RowValidation datasetId={datasetId} isBusinessDataflow={isBusinessDataflow} tabs={tabs} />;
  }

  return (
    <TableValidation
      datasetId={datasetId}
      datasetSchema={datasetSchema}
      datasetSchemas={datasetSchemas}
      isBusinessDataflow={isBusinessDataflow}
      tabs={tabs}
    />
  );
};
