export const ReferenceDataflowConfig = {
  getAll: '/dataflow/referenceDataflows?asc={:isAsc}&numPage={:pageNum}&orderHeader={:sortBy}&sizePage={:numberRows}',
  getReferencingDataflows: '/referenceDataset/referenced/dataflow/{:referenceDataflowId}'
};
