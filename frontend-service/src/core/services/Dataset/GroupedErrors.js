export const GroupedErrors = ({ datasetRepository }) => async (
  datasetId,
  pageNum,
  pageSize,
  sortField,
  asc,
  levelErrorsFilter,
  typeEntitiesFilter,
  originsFilter
) =>
  datasetRepository.groupedErrorsById(
    datasetId,
    pageNum,
    pageSize,
    sortField,
    asc,
    levelErrorsFilter,
    typeEntitiesFilter,
    originsFilter
  );
