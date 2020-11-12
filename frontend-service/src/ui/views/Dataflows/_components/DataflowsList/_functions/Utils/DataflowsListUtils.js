import isNil from 'lodash/isNil';

const parseDataToFilter = data => {
  return data.map(dataflow => ({
    id: dataflow.id,
    description: dataflow.description,
    expirationDate: dataflow.expirationDate,
    legalInstrument: !isNil(dataflow.obligation) ? dataflow.obligation.legalInstruments.alias : null,
    name: dataflow.name,
    obligationTitle: !isNil(dataflow.obligation) ? dataflow.obligation.title : null,
    reportingDatasetsStatus: dataflow.reportingDatasetsStatus,
    status: dataflow.status,
    userRole: dataflow.userRole
  }));
};

export const DataflowsListUtils = { parseDataToFilter };
