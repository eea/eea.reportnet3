import isNil from 'lodash/isNil';

const parseDataToFilter = data => {
  return data.map(dataflow => ({
    id: dataflow.id,
    description: dataflow.description,
    expirationDate: dataflow.expirationDate,
    legalInstrument: !isNil(dataflow.obligation.legalInstruments) ? dataflow.obligation.legalInstruments.alias : null,
    name: dataflow.name,
    obligationTitle: dataflow.obligation.title,
    status: dataflow.status,
    userRole: dataflow.userRole
  }));
};

export const DataflowsListUtils = { parseDataToFilter };
