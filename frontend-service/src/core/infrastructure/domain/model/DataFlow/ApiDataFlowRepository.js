import { api } from 'core/infrastructure/api';
import { DataFlow } from 'core/domain/model/DataFlow/DataFlow';

const accepted = async userId => {
  const acceptedDataflowsDTO = await api.acceptedDataFlows(userId);
  return parseDataFlowDTOs(acceptedDataflowsDTO.filter(item => item.userRequestStatus === 'ACCEPTED'));
};

const completed = async userId => {
  const completedDataflowsDTO = await api.completedDataFlows(userId);
  return parseDataFlowDTOs(completedDataflowsDTO);
};

const parseDataFlowDTO = dataFlowDTO => {
  return new DataFlow(
    dataFlowDTO.id,
    dataFlowDTO.datasets,
    dataFlowDTO.description,
    dataFlowDTO.name,
    dataFlowDTO.deadlineDate,
    dataFlowDTO.creationDate,
    dataFlowDTO.userRequestStatus,
    dataFlowDTO.status,
    dataFlowDTO.documents,
    dataFlowDTO.weblinks
  );
};

const parseDataFlowDTOs = dataFlowDTOs => {
  return dataFlowDTOs.map(dataFlowDTO => {
    return parseDataFlowDTO(dataFlowDTO);
  });
};

const pending = async userId => {
  const pendingDataflowsDTO = await api.pendingDataFlows(userId);
  return parseDataFlowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'PENDING'));
};

const reporting = async dataFlowId => {
  const reportingDataFlowDTO = await api.reportingDataFlow(dataFlowId);
  return parseDataFlowDTO(reportingDataFlowDTO);
};

export const ApiDataFlowRepository = {
  pending,
  accepted,
  completed,
  reporting
};
