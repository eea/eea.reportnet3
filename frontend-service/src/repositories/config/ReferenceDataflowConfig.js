export const ReferenceDataflowConfig = {
  getAll:
    '/dataflow/referenceDataflows?asc={:isAscending}&numPage={:pageNumber}&orderHeader={:sortBy}&sizePage={:pageSize}',
  getReferencingDataflows: '/referenceDataset/referenced/dataflow/{:referenceDataflowId}'
};
