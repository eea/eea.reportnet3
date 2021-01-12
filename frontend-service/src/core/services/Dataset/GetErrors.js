export const GetErrors = ({ datasetRepository }) => async (
  datasetId,
  pageNum,
  pageSize,
  sortField,
  asc,
  fieldValueFilter,
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
    fieldValueFilter,
    levelErrorsFilter,
    typeEntitiesFilter,
    tablesFilter
  );
