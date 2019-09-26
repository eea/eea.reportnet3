import { apiDataFlow } from 'core/infrastructure/api/domain/model/DataFlow';
import { DataFlow } from 'core/domain/model/DataFlow/DataFlow';

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
    dataFlowDTO.weblinks,
    dataFlowDTO.requestId
  );
};

const parseDataFlowDTOs = dataFlowDTOs => {
  return dataFlowDTOs.map(dataFlowDTO => {
    return parseDataFlowDTO(dataFlowDTO);
  });
};

const all = async () => {
  const pendingDataflowsDTO = await apiDataFlow.all();
  return {
    pending: parseDataFlowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'PENDING')),
    accepted: parseDataFlowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'ACCEPTED')),
    completed: parseDataFlowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'COMPLETED'))
  };
};

const accepted = async () => {
  const acceptedDataflowsDTO = await apiDataFlow.accepted();
  return parseDataFlowDTOs(acceptedDataflowsDTO.filter(item => item.userRequestStatus === 'ACCEPTED'));
};

const completed = async () => {
  const completedDataflowsDTO = await apiDataFlow.completed();
  return parseDataFlowDTOs(completedDataflowsDTO);
};

const dashboards = async dataFlowId => {
  const dashboardsData = await apiDataFlow.dashboards(dataFlowId);
  return dashboardsData;
};
const dataset_status = async dataFlowId => {
  const dashboardsData = await apiDataFlow.datasetReleasedStatus(dataFlowId);

  const releasedDataObject = {
    labels: dashboardsData.map(dataset => dataset.dataSetName),
    datasets: [
      {
        label: 'Released',
        backgroundColor: 'rgb(50, 205, 50)',

        data: dashboardsData.map(dataset => dataset.isReleased)
      },
      {
        label: 'Unreleased',
        backgroundColor: 'rgb(255, 99, 132)',
        data: dashboardsData.map(dataset => !dataset.isReleased)
      }
    ]
  };
  console.log('in ApiDataFlowRepository', releasedDataObject);
  return releasedDataObject;
};

const pending = async () => {
  const pendingDataflowsDTO = await apiDataFlow.pending();
  return parseDataFlowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'PENDING'));
};

const reporting = async dataFlowId => {
  const reportingDataFlowDTO = await apiDataFlow.reporting(dataFlowId);
  return parseDataFlowDTO(reportingDataFlowDTO);
};

const accept = async dataFlowId => {
  const status = await apiDataFlow.accept(dataFlowId);
  return status;
};

const reject = async dataFlowId => {
  const status = await apiDataFlow.reject(dataFlowId);
  return status;
};

export const ApiDataFlowRepository = {
  all,
  accept,
  accepted,
  completed,
  dashboards,
  dataset_status,
  pending,
  reject,
  reporting
};
