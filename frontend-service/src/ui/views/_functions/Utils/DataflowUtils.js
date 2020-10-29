import { DataflowService } from 'core/services/Dataflow';

const getDataflowName = async dataflowId => {
  const dataflowData = await DataflowService.dataflowDetails(dataflowId);
  return dataflowData.name;
};

export const DataflowUtils = { getDataflowName };
