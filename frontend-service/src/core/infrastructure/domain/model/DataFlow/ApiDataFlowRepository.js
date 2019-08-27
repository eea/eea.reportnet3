import { api } from 'core/infrastructure/api';
import { DataFlow } from 'core/domain/model/DataFlow/DataFlow';

const accepted = async () => {
  const acceptedDataflowsDTO = await api.acceptedDataFlows();
  return parseDataFlowDTOs(acceptedDataflowsDTO.filter(item => item.userRequestStatus === 'ACCEPTED'));
};

const completed = async () => {
  const completedDataflowsDTO = await api.completedDataFlows();
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

const pending = async () => {
  const pendingDataflowsDTO = await api.pendingDataFlows();
  return parseDataFlowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'PENDING'));
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
  accept,
  accepted,
  completed,
  pending,
  reject,
  reporting
};
