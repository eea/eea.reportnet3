export const LevelErrorPriority = ({ datasetRepository }) => levelError =>
  datasetRepository.getLevelErrorPriorityByLevelError(levelError);
