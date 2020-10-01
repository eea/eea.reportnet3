import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import sortBy from 'lodash/sortBy';

import { apiIntegration } from 'core/infrastructure/api/domain/model/Integration/ApiIntegration';

import { Integration } from 'core/domain/model/Integration/Integration';

const all = async (dataflowId, datasetSchemaId) => {
  return parseIntegrationsList(await apiIntegration.all(parseDatasetSchemaId(datasetSchemaId, dataflowId)));
};

const allExtensionsOperations = async (dataflowId, datasetSchemaId) =>
  parseIntegrationsOperationsExtensionsList(
    await apiIntegration.allExtensionsOperations(parseDatasetSchemaId(datasetSchemaId, dataflowId))
  );

const create = async integration => apiIntegration.create(parseManageIntegration(integration));

const deleteById = async (dataflowId, integrationId) => await apiIntegration.deleteById(dataflowId, integrationId);

const findEUDatasetIntegration = async datasetSchemaId =>
  parseIntegration(await apiIntegration.findEUDatasetIntegration(datasetSchemaId));

const getProcesses = async (repositoryName, datasetId) =>
  parseProcessList(await apiIntegration.getProcesses(repositoryName, datasetId));

const getRepositories = async datasetId => parseRepositoryList(await apiIntegration.getRepositories(datasetId));

const update = async integration => apiIntegration.update(parseManageIntegration(integration));

const parseDatasetSchemaId = (datasetSchemaId, dataflowId) => {
  const integration = new Integration();

  integration.internalParameters = { dataflowId, datasetSchemaId };

  return integration;
};

const parseExternalParameters = parameterDTO => {
  const externalParameters = {};
  for (let index = 0; index < parameterDTO.length; index++) {
    const parameter = parameterDTO[index];
    externalParameters[parameter.key] = parameter.value;
  }
  return externalParameters;
};

const parseIntegration = integration => {
  const integrationDTO = new Integration();
  integrationDTO.externalParameters = integration.externalParameters;
  integrationDTO.integrationDescription = integration.description;
  integrationDTO.integrationId = integration.id;
  integrationDTO.integrationName = integration.name;
  integrationDTO.internalParameters = integration.internalParameters;
  integrationDTO.operation = integration.operation;
  integrationDTO.operationName = integration.operation.split('_').join(' ');
  integrationDTO.tool = integration.tool;

  return integrationDTO;
};

const parseIntegrationsList = integrations => {
  if (!isNil(integrations)) {
    const integrationsDTO = [];
    integrations.forEach(integration => integrationsDTO.push(parseIntegration(integration)));
    return sortBy(integrationsDTO, ['integrationId']);
  }
  return;
};

const parseIntegrationsOperationsExtensionsList = integrations => {
  if (!isNil(integrations)) {
    const integrationsDTO = [];
    integrations.forEach(integration => integrationsDTO.push(parseIntegrationOperationExtension(integration)));
    return integrationsDTO;
  }
  return;
};

const parseManageIntegration = integration => ({
  description: integration.description,
  externalParameters: parseExternalParameters(integration.externalParameters),
  id: integration.id,
  internalParameters: {
    dataflowId: integration.dataflowId,
    datasetSchemaId: integration.datasetSchemaId,
    fileExtension: integration.fileExtension,
    processName: integration.processName.value,
    repository: integration.repository.value
  },
  name: integration.name,
  operation: integration.operation.value,
  tool: integration.tool
});

const parseIntegrationOperationExtension = integration => ({
  datasetSchemaId: integration.internalParameters.datasetSchemaId,
  fileExtension: integration.internalParameters.fileExtension,
  id: integration.id,
  name: integration.name,
  operation: integration.operation
});

const parseRepositoryList = repositoryList => parseKeyValue(repositoryList);

const parseProcessList = processList => parseKeyValue(processList);

const parseKeyValue = list => {
  const listDTO = [];

  if (!isNil(list) && !isEmpty(list.items)) {
    list.items.map(item => listDTO.push({ label: item.name, value: item.name }));
  }

  return listDTO;
};

const runIntegration = async (integrationId, datasetId, replaceData) => {
  return await apiIntegration.runIntegration(integrationId, datasetId, replaceData);
};

export const ApiIntegrationRepository = {
  all,
  allExtensionsOperations,
  create,
  deleteById,
  findEUDatasetIntegration,
  getProcesses,
  getRepositories,
  runIntegration,
  update
};
