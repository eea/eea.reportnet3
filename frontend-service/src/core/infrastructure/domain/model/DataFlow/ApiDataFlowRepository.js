import { api } from 'core/infrastructure/api';
import { DataFlow } from 'core/domain/model/DataFlow/DataFlow';

const parseDataFlowDTOs = dataFlowDTOs => {
  return dataFlowDTOs.map(dataFlowDTO => {
    return parseDataFlowDTO(dataFlowDTO);
  });
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

const reporting = async dataFlowId => {
  const reportingDataFlowDTO = await api.reportingDataFlow(dataFlowId);
  return parseDataFlowDTO(reportingDataFlowDTO);
};

const accept = async dataFlowId => {
  const status = await api.acceptDataFlow(dataFlowId);
  return status;
};

const reject = async dataFlowId => {
  const status = await api.rejectDataFlow(dataFlowId);
  return status;
};

export const ApiDataFlowRepository = {
  pending,
  accepted,
  completed,
  reporting,
  accept,
  reject
};
