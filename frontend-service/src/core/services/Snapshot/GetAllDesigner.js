export const GetAllDesigner = ({ snapshotRepository }) => async datasetSchemaId =>
  snapshotRepository.allDesigner(datasetSchemaId);
