export const ReferenceDataflowConfig = {
  getAll: '/dataflow/referenceDataflows?asc={:isAsc}&pageNum={:pageNum}&orderHeader={:sortBy}&pageSize={:numberRows}',
  getReferencingDataflows: '/referenceDataset/referenced/dataflow/{:referenceDataflowId}'
};
