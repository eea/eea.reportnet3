export const GenerateFile = ({ validationRepository }) => async datasetId =>
  validationRepository.generateFile(datasetId);
