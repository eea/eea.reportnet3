import { DataflowService } from 'core/services/Dataflow';

const getDataflowName = async dataflowId => {
  const { data } = await DataflowService.dataflowDetails(dataflowId);
  return data.name;
};

export const DataflowUtils = { getDataflowName };
