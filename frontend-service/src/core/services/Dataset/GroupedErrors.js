export const GroupedErrors = ({ datasetRepository }) => async (
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
  datasetRepository.groupedErrorsById(
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
