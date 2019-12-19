export const OrderTableDesign = ({ datasetRepository }) => async (datasetId, position, tableSchemaId) =>
  datasetRepository.orderTableSchema(datasetId, position, tableSchemaId);
