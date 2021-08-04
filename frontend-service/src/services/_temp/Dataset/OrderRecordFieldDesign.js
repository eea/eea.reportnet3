export const OrderRecordFieldDesign = ({ datasetRepository }) => async (datasetId, position, fieldSchemaId) =>
  datasetRepository.orderFieldSchema(datasetId, position, fieldSchemaId);
