import { isNull, isUndefined } from 'lodash';

import { apiDataflow } from 'core/infrastructure/api/domain/model/DataFlow';
import { Dataflow } from 'core/domain/model/DataFlow/DataFlow';
import { Dataset } from 'core/domain/model/DataSet/DataSet';
import { WebLink } from 'core/domain/model/WebLink/WebLink';

const parseDataflowDTO = dataflowDTO => {
  const dataflow = new Dataflow();
  dataflow.creationDate = dataflowDTO.creationDate;
  dataflow.datasets = parseDatasetListDTO(dataflowDTO.datasets);
  dataflow.deadlineDate = dataflowDTO.deadlineDate;
  dataflow.description = dataflowDTO.description;
  dataflow.documents = parseDocumentListDTO(dataflowDTO.documents);
  dataflow.id = dataflowDTO.id;
  dataflow.name = dataflowDTO.name;
  dataflow.status = dataflowDTO.status;
  dataflow.userRequestStatus = dataflowDTO.userRequestStatus;
  dataflow.weblinks = parseWebLinkListDTO(dataflowDTO.weblinks);
  dataflow.requestId = dataflowDTO.requestId;
  return dataflow;
};

const parseDatasetListDTO = datasetsDTO => {
  if (isUndefined(datasetsDTO)) {
    return;
  }
  if (!isNull(datasetsDTO)) {
    const datasets = [];
    datasetsDTO.map(datasetDTO => {
      datasets.push(parseDatasetDTO(datasetDTO));
    });
    return datasets;
  } else {
    return null;
  }
};

const parseDatasetDTO = datasetDTO => {
  return new Dataset(
    null,
    datasetDTO.id,
    null,
    datasetDTO.dataSetName,
    null,
    null,
    null,
    null,
    null,
    datasetDTO.isReleased
  );
};

const parseDocumentListDTO = documentsDTO => {
  if (isUndefined(documentsDTO)) {
    return;
  }
  if (!isNull(documentsDTO)) {
    const documents = [];
    documentsDTO.map(documentDTO => {
      documents.push(parseDocumentDTO(documentDTO));
    });
    return documents;
  } else {
    return null;
  }
};

const parseDocumentDTO = documentDTO => {
  return new Document(
    documentDTO.category,
    documentDTO.dataflowId,
    documentDTO.description,
    documentDTO.id,
    documentDTO.language,
    documentDTO.name
  );
};

const parseWebLinkListDTO = webLinksDTO => {
  if (isUndefined(webLinksDTO)) {
    return;
  }
  if (!isNull(webLinksDTO)) {
    const webLinks = [];
    webLinksDTO.map(webLinkDTO => {
      webLinks.push(parseWebLinkDTO(webLinkDTO));
    });
    return webLinks;
  } else {
    return null;
  }
};

const parseWebLinkDTO = webLinkDTO => {
  return new WebLink(webLinkDTO.description, webLinkDTO.url);
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

const datasetStatisticsStatus = async dataflowId => {
  const datasetsDashboardsData = await apiDataflow.datasetStatisticsStatus(dataflowId);

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

const datasetReleasedStatus = async dataflowId => {
  const releasedDashboardsData = await apiDataflow.datasetReleasedStatus(dataflowId);

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
  datasetStatisticsStatus,
  datasetReleasedStatus,
  pending,
  reject,
  reporting
};
