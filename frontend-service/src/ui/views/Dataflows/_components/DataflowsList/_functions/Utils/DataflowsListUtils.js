const parseDataToFilter = (data, pinnedDataflows) => {
  return data.map(dataflow => ({
    id: dataflow.id,
    description: dataflow.description,
    expirationDate: dataflow.expirationDate,
    legalInstrument: dataflow.obligation?.legalInstruments?.alias,
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
