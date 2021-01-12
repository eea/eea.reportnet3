export const GroupedErrors = ({ datasetRepository }) => async (
  datasetId,
  pageNum,
  pageSize,
  sortField,
  asc,
  levelErrorsFilter,
  typeEntitiesFilter,
  tablesFilter
) =>
  datasetRepository.groupedErrorsById(
    datasetId,
    pageNum,
    pageSize,
    sortField,
    asc,
    levelErrorsFilter,
    typeEntitiesFilter,
    tablesFilter
  );
