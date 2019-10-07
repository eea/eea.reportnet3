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

const datasetsValidationStatistics = async dataflowId => {
  const datasetsDashboardsDataDTO = await apiDataflow.datasetsValidationStatistics(dataflowId);
  datasetsDashboardsDataDTO.sort(function(a, b) {
    var datasetName_A = a.nameDataSetSchema;
    var datasetName_B = b.nameDataSetSchema;
    return datasetName_A < datasetName_B ? -1 : datasetName_A > datasetName_B ? 1 : 0;
  });

  const datasetsDashboardsData = {};
  datasetsDashboardsData.datasetId = datasetsDashboardsDataDTO.idDataSetSchema;

  const datasetReporters = [];
  const tables = [];
  let tablePercentages = [];
  let tableValues = [];

  datasetsDashboardsDataDTO.map(dataset => {
    datasetsDashboardsData.datasetId = dataset.idDataSetSchema;
    datasetReporters.push({
      reporterName: dataset.nameDataSetSchema
    });

    dataset.tables.map((table, i) => {
      let index = tables.map(t => t.tableId).indexOf(table.idTableSchema);
      //Check if table has been already added
      if (index === -1) {
        tablePercentages.push(
          [
            getPercentageOfValue(
              table.totalRecords - (table.totalRecordsWithErrors + table.totalRecordsWithWarnings),
              table.totalRecords
            )
          ],
          [getPercentageOfValue(table.totalRecordsWithWarnings, table.totalRecords)],
          [getPercentageOfValue(table.totalRecordsWithErrors, table.totalRecords)]
        );

        tableValues.push(
          [table.totalRecords - (table.totalRecordsWithErrors + table.totalRecordsWithWarnings)],
          [table.totalRecordsWithWarnings],
          [table.totalRecordsWithErrors]
        );

        tables.push({
          tableId: table.idTableSchema,
          tableName: table.nameTableSchema,
          tableStatisticPercentages: tablePercentages,
          tableStatisticValues: tableValues
        });
        tablePercentages = [];
        tableValues = [];
      } else {
        const tableById = tables.filter(tab => tab.tableId === table.idTableSchema)[0];

        tableById.tableStatisticPercentages[0].push(
          getPercentageOfValue(
            table.totalRecords - (table.totalRecordsWithErrors + table.totalRecordsWithWarnings),
            table.totalRecords
          )
        );
        tableById.tableStatisticPercentages[1].push(
          getPercentageOfValue(table.totalRecordsWithWarnings, table.totalRecords)
        );
        tableById.tableStatisticPercentages[2].push(
          getPercentageOfValue(table.totalRecordsWithErrors, table.totalRecords)
        );

        tableById.tableStatisticPercentages = tableById.tableStatisticPercentages;

        tableById.tableStatisticValues[0].push(
          table.totalRecords - (table.totalRecordsWithErrors + table.totalRecordsWithWarnings)
        );
        tableById.tableStatisticValues[1].push(table.totalRecordsWithWarnings);
        tableById.tableStatisticValues[2].push(table.totalRecordsWithErrors);
        tables[index] = tableById;
      }
    });
  });

  datasetsDashboardsData.datasetReporters = datasetReporters;
  datasetsDashboardsData.tables = tables;

  const datasets = datasetsDashboardsData.tables
    .map(table => [
      {
        label: `CORRECT`,
        tableName: table.tableName,
        tableId: table.tableId,
        backgroundColor: 'rgba(153, 204, 51, 1)',
        data: table.tableStatisticPercentages[0],
        totalData: table.tableStatisticValues[0],
        stack: table.tableName
      },
      {
        label: `WARNINGS`,
        tableName: table.tableName,
        tableId: table.tableId,
        backgroundColor: 'rgba(255, 204, 0, 1)',
        data: table.tableStatisticPercentages[1],
        totalData: table.tableStatisticValues[1],
        stack: table.tableName
      },
      {
        label: `ERRORS`,
        tableName: table.tableName,
        tableId: table.tableId,
        backgroundColor: 'rgba(204, 51, 0, 1)',
        data: table.tableStatisticPercentages[2],
        totalData: table.tableStatisticValues[2],
        stack: table.tableName
      }
    ])
    .flat();

  const labels = datasetsDashboardsData.datasetReporters.map(reporterData => reporterData.reporterName);

  const datasetDataObject = {
    labels: labels,
    datasets: datasets
  };

  return datasetDataObject;
};

const datasetsReleasedStatus = async dataflowId => {
  const datasetsReleasedStatusDTO = await apiDataflow.datasetsReleasedStatus(dataflowId);
  datasetsReleasedStatusDTO.sort(function(a, b) {
    var datasetName_A = a.dataSetName;
    var datasetName_B = b.dataSetName;
    return datasetName_A < datasetName_B ? -1 : datasetName_A > datasetName_B ? 1 : 0;
  });

  const releasedDataObject = {
    labels: datasetsReleasedStatusDTO.map(dataset => dataset.dataSetName),
    datasets: [
      {
        label: 'Released',
        backgroundColor: 'rgba(51, 153, 0, 1)',

        data: datasetsReleasedStatusDTO.map(dataset => dataset.isReleased)
      },
      {
        label: 'Unreleased',
        backgroundColor: 'rgba(208, 208, 206, 1)',
        data: datasetsReleasedStatusDTO.map(dataset => !dataset.isReleased)
      }
    ]
  };
  return releasedDataObject;
};

const metadata = async dataflowId => {
  const metadataDTO = await apiDataflow.metadata(dataflowId);
  return metadataDTO;
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

const getPercentageOfValue = (val, total) => {
  return total === 0 ? '0.00' : ((val / total) * 100).toFixed(2);
};

const transposeMatrix = matrix => {
  return Object.keys(matrix[0]).map(c => matrix.map(r => r[c]));
};

export const ApiDataflowRepository = {
  all,
  accept,
  accepted,
  completed,
  datasetsValidationStatistics,
  datasetsReleasedStatus,
  metadata,
  pending,
  reject,
  reporting
};
