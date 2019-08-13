import { api } from 'core/infrastructure/api';
import { DataFlow } from 'core/domain/model/DataFlow/DataFlow';

const parseDataFlowDTOs = dataFlowDTOs => {
  return dataFlowDTOs.map(dataFlowDTO => {
    const datasets = [];
    const documents = [];
    const weblinks = [];
    return new DataFlow(
      dataFlowDTO.id,
      datasets,
      dataFlowDTO.description,
      dataFlowDTO.name,
      dataFlowDTO.deadlineDate,
      dataFlowDTO.creationDate,
      dataFlowDTO.userRequestStatus,
      dataFlowDTO.status,
      documents,
      weblinks
    );
  });
};

const pending = async userId => {
  const pendingDataflowsDTO = await api.pendingDataFlows(userId);
  return parseDataFlowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'PENDING'));
};

const accepted = async userId => {
  const acceptedDataflowsDTO = await api.acceptedDataFlows(userId);
  return parseDataFlowDTOs(acceptedDataflowsDTO.filter(item => item.userRequestStatus === 'ACCEPTED'));
};

const completed = async userId => {
  const completedDataflowsDTO = await api.completedDataFlows(userId);
  return parseDataFlowDTOs(completedDataflowsDTO);
};

export const ApiDataFlowRepository = {
  pending,
  accepted,
  completed
};
