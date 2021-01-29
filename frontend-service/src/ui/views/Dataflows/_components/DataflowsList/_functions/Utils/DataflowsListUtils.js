import isNil from 'lodash/isNil';

const parseDataToFilter = (data, pinnedDataflows) => {
  return data.map(dataflow => ({
    id: dataflow.id,
    description: dataflow.description,
    expirationDate: dataflow.expirationDate,
    legalInstrument: !isNil(dataflow.obligation) ? dataflow.obligation.legalInstruments.alias : null,
    name: dataflow.name,
    obligationTitle: !isNil(dataflow.obligation) ? dataflow.obligation.title : null,
    pinned: pinnedDataflows.some(pinnedDataflow => pinnedDataflow === dataflow.id.toString()) ? 'pinned' : 'unpinned',
    reportingDatasetsStatus: dataflow.reportingDatasetsStatus,
    status: dataflow.status,
    userRole: dataflow.userRole
  }));
};

export const DataflowsListUtils = { parseDataToFilter };
