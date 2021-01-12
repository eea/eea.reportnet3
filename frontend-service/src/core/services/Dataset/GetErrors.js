export const GetErrors = ({ datasetRepository }) => async (
  datasetId,
  pageNum,
  pageSize,
  sortField,
  asc,
  levelErrorsFilter,
  typeEntitiesFilter,
  tablesFilter
) =>
  datasetRepository.errorsById(
    datasetId,
    pageNum,
    pageSize,
    sortField,
    asc,
    levelErrorsFilter,
    typeEntitiesFilter,
    tablesFilter
  );
