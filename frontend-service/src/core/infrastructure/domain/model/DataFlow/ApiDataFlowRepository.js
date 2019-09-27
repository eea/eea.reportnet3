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

const datasetStatisticsStatus = async dataFlowId => {
  const datasetsDashboardsData = await apiDataFlow.datasetStatisticsStatus(dataFlowId);

  const datasets = datasetsDashboardsData.tables
    .map(table => [
      {
        label: `CORRECT`,
        tableName: table.tableName,
        tableId: table.tableId,
        backgroundColor: '#004494',
        data: table.tableStatisticPercentages[0],
        totalData: table.tableStatisticValues[0],
        stack: table.tableName
      },
      {
        label: `WARNINGS`,
        tableName: table.tableName,
        tableId: table.tableId,
        backgroundColor: '#ffd617',
        data: table.tableStatisticPercentages[1],
        totalData: table.tableStatisticValues[1],
        stack: table.tableName
      },
      {
        label: `ERRORS`,
        tableName: table.tableName,
        tableId: table.tableId,
        backgroundColor: '#DA2131',
        data: table.tableStatisticPercentages[2],
        totalData: table.tableStatisticPercentages[2],
        stack: table.tableName
      }
    ])
    .flat();

  const labels = datasetsDashboardsData.dataSetReporters.map(reporterData => reporterData.reporterName);

  const datasetDataObject = {
    labels: labels,
    datasets: datasets
  };

  return datasetDataObject;
};

const datasetReleasedStatus = async dataFlowId => {
  const releasedDashboardsData = await apiDataFlow.datasetReleasedStatus(dataFlowId);

  const releasedDataObject = {
    labels: releasedDashboardsData.map(dataset => dataset.dataSetName),
    datasets: [
      {
        label: 'Released',
        backgroundColor: 'rgb(50, 205, 50)',

        data: releasedDashboardsData.map(dataset => dataset.isReleased)
      },
      {
        label: 'Unreleased',
        backgroundColor: 'rgb(255, 99, 132)',
        data: releasedDashboardsData.map(dataset => !dataset.isReleased)
      }
    ]
  };
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
  datasetStatisticsStatus,
  datasetReleasedStatus,
  pending,
  reject,
  reporting
};
