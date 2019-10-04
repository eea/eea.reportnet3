import { isNull, isUndefined } from 'lodash';

import { apiDataflow } from 'core/infrastructure/api/domain/model/DataFlow';
import { Dataflow } from 'core/domain/model/DataFlow/DataFlow';
import { Dataset } from 'core/domain/model/DataSet/DataSet';
import { WebLink } from 'core/domain/model/WebLink/WebLink';

import { DatasetTable } from 'core/domain/model/DataSet/DataSetTable/DataSetTable';

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
  console.log('datasetsDashboardsData', datasetsDashboardsData);

  const datasetsArray = datasetsDashboardsData.map((dataset, i) => {
    let datasetObject = new Dataset();
    datasetObject.datasetSchemaName = dataset.nameDataSetSchema;
    datasetObject.datasetErrors = dataset.datasetErrors;

    const tableStatisticValues = [];

    const datasetTables = datasetsDashboardsData.map(dataset =>
      dataset.tables.map(datasetTableDTO => {
        tableStatisticValues.push([
          datasetTableDTO.totalRecords -
            (datasetTableDTO.totalRecordsWithErrors + datasetTableDTO.totalRecordsWithWarnings),
          datasetTableDTO.totalRecordsWithWarnings,
          datasetTableDTO.totalRecordsWithErrors
        ]);

        return new DatasetTable(
          datasetTableDTO.tableErrors,
          datasetTableDTO.idTableSchema,
          datasetTableDTO.nameTableSchema
        );
      })
    );

    let transposedValues = transposeMatrix(tableStatisticValues);

    datasetObject.tableStatisticValues = transposedValues;
    datasetObject.tableStatisticPercentages = getPercentage(transposedValues);
    datasetObject.tables = datasetTables;

    return datasetObject;
  });

  console.log('datasetsArray', datasetsArray);

  const labels = datasetsArray.map(dataset => dataset.datasetSchemaName);
  const values = datasetsArray.map(dataset => dataset.tableStatisticValues);
  const percentages = datasetsArray.map(dataset => dataset.tableStatisticPercentages);
  const tablesData = datasetsArray.map(dataset => dataset.tables);

  console.log('labels', labels);
  console.log('values', values);
  console.log('percentages', percentages);
  console.log('tablesData', tablesData);

  console.log('table names', datasetsArray[0].tables.map(tab => tab.map(table => table.tableSchemaName)));

  const datasets = datasetsArray[0].tables
    .map(table => [
      ({
        label: `CORRECT`,
        tableName: table.tableSchemaName,
        tableId: table.tableSchemaId,
        backgroundColor: '#004494',
        // data: percentages[0],
        // totalData: values[0],
        stack: table.tableSchemaName
      },
      {
        label: `WARNINGS`,
        tableName: table.tableSchemaName,
        tableId: table.tableSchemaId,
        backgroundColor: '#ffd617',
        // data: percentages[1],
        // totalData: values[1],
        stack: table.tableSchemaName
      },
      {
        label: `ERRORS`,
        tableName: table.tableSchemaName,
        tableId: table.tableSchemaId,
        backgroundColor: '#DA2131',
        // data: percentages[2],
        // totalData: values[2],
        stack: table.tableSchemaName
      })
    ])
    .flat();

  console.log('datasets', datasets);

  const datasetDataObject = {
    labels: labels,
    datasets: datasets
  };

  return datasetDataObject;
};

const getPercentage = valArr => {
  let total = valArr.reduce((arr1, arr2) => arr1.map((v, i) => v + arr2[i]));
  return valArr.map(val => val.map((v, i) => ((v / total[i]) * 100).toFixed(2)));
};

const transposeMatrix = matrix => {
  return Object.keys(matrix[0]).map(c => matrix.map(r => r[c]));
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
