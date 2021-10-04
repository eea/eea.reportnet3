const parseDataToFilter = (data, pinnedDataflows) => {
  return data?.map(dataflow => ({
    id: dataflow.id,
    creationDate: dataflow.creationDate,
    description: dataflow.description,
    expirationDate: dataflow.expirationDate,
    legalInstrument: dataflow.obligation?.legalInstrument?.alias,
    name: dataflow.name,
    obligationTitle: dataflow.obligation?.title,
    obligationId: dataflow.obligation?.obligationId?.toString(),
    pinned: pinnedDataflows.some(pinnedDataflow => pinnedDataflow === dataflow.id.toString()) ? 'pinned' : 'unpinned',
    reportingDatasetsStatus: dataflow.reportingDatasetsStatus,
    status: dataflow.status,
    userRole: dataflow.userRole
  }));
};

export const DataflowsListUtils = { parseDataToFilter };
