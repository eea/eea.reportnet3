import { useContext } from 'react';

import { TableValidation } from './_components/TableValidation';
import { FieldValidation } from './_components/FieldValidation';
import { RowValidation } from './_components/RowValidation';

import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

export const Validations = ({ dataflowType, datasetSchema, datasetSchemas, tabs, datasetId }) => {
  const validationContext = useContext(ValidationContext);

  if (validationContext.level === 'field') {
    return <FieldValidation dataflowType={dataflowType} datasetId={datasetId} tabs={tabs} />;
  }

  if (validationContext.level === 'row') {
    return <RowValidation dataflowType={dataflowType} datasetId={datasetId} tabs={tabs} />;
  }

  return (
    <TableValidation
      dataflowType={dataflowType}
      datasetId={datasetId}
      datasetSchema={datasetSchema}
      datasetSchemas={datasetSchemas}
      tabs={tabs}
    />
  );
};
