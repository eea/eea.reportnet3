import { apiDataflow } from 'core/infrastructure/api/domain/model/DataFlow';
import { Dataflow } from 'core/domain/model/DataFlow/DataFlow';

const parseDataflowDTO = dataflowDTO => {
  return new Dataflow(
    dataflowDTO.id,
    dataflowDTO.datasets,
    dataflowDTO.description,
    dataflowDTO.name,
    dataflowDTO.deadlineDate,
    dataflowDTO.creationDate,
    dataflowDTO.userRequestStatus,
    dataflowDTO.status,
    dataflowDTO.documents,
    dataflowDTO.weblinks,
    dataflowDTO.requestId
  );
};

const parseDataflowDTOs = dataflowDTOs => {
  return dataflowDTOs.map(dataflowDTO => {
    return parseDataflowDTO(dataflowDTO);
  });
};

const all = async () => {
  const pendingDataflowsDTO = await apiDataflow.all();
  return {
    pending: parseDataflowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'PENDING')),
    accepted: parseDataflowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'ACCEPTED')),
    completed: parseDataflowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'COMPLETED'))
  };
};

const accepted = async () => {
  const acceptedDataflowsDTO = await apiDataflow.accepted();
  return parseDataflowDTOs(acceptedDataflowsDTO.filter(item => item.userRequestStatus === 'ACCEPTED'));
};

const completed = async () => {
  const completedDataflowsDTO = await apiDataflow.completed();
  return parseDataflowDTOs(completedDataflowsDTO);
};

const dashboards = async dataflowId => {
  const dashboardsData = await apiDataflow.dashboards(dataflowId);
  return dashboardsData;
};

const pending = async () => {
  const pendingDataflowsDTO = await apiDataflow.pending();
  return parseDataflowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'PENDING'));
};

const reporting = async dataflowId => {
  const reportingDataflowDTO = await apiDataflow.reporting(dataflowId);
  return parseDataflowDTO(reportingDataflowDTO);
};

const accept = async dataflowId => {
  const status = await apiDataflow.accept(dataflowId);
  return status;
};

const reject = async dataflowId => {
  const status = await apiDataflow.reject(dataflowId);
  return status;
};

export const ApiDataflowRepository = {
  all,
  accept,
  accepted,
  completed,
  dashboards,
  pending,
  reject,
  reporting
};
