import { DataflowService } from 'services/DataflowService';

const getDataflowDetails = async dataflowId => {
  const data = await DataflowService.dataflowDetails(dataflowId);
  return data;
};

export const DataflowUtils = { getDataflowDetails };
