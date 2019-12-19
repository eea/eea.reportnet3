export const UpdateDatasetDescriptionDesign = ({ datasetRepository }) => async (datasetId, datasetSchemaDescription) =>
  datasetRepository.updateDatasetDescriptionDesign(datasetId, datasetSchemaDescription);
