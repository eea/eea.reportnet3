import { api } from 'core/infrastructure/api';
//import { DataFlow } from 'core/domain/model/DataFlow/DataFlow'

const all = async () => {
  const dataflowsDTO = await api.dataflows();

  return dataflowsDTO;
};

export const ApiDataFlowRepository = {
  all
};
