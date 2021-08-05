import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import sortBy from 'lodash/sortBy';

import { integrationRepository } from 'repositories/IntegrationRepository';

import { Integration } from 'entities/Integration';

const all = async (dataflowId, datasetSchemaId) => {
  const integrationsDTO = await integrationRepository.all(parseDatasetSchemaId(datasetSchemaId, dataflowId));
  return parseIntegrationsList(integrationsDTO.data);
};

const allExtensionsOperations = async (dataflowId, datasetSchemaId) => {
  const integrationsDTO = await integrationRepository.allExtensionsOperations(
    parseDatasetSchemaId(datasetSchemaId, dataflowId)
  );
  return parseIntegrationsOperationsExtensionsList(integrationsDTO.data);
};
const create = async integration => integrationRepository.create(parseManageIntegration(integration));

const deleteById = async (dataflowId, integrationId) =>
  await integrationRepository.deleteById(dataflowId, integrationId);

const findEUDatasetIntegration = async datasetSchemaId => {
  const eUDatasetIntegrationsDTO = await integrationRepository.findEUDatasetIntegration(datasetSchemaId);
  return parseIntegration(eUDatasetIntegrationsDTO.data);
};
const getProcesses = async (repositoryName, datasetId) => {
  const processes = await integrationRepository.getProcesses(repositoryName, datasetId);
  return parseProcessList(processes.data);
};

const getRepositories = async datasetId => {
  const repositories = await integrationRepository.getRepositories(datasetId);
  return parseRepositoryList(repositories.data);
};

const update = async integration => integrationRepository.update(parseManageIntegration(integration));

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

const parseManageIntegration = integration => {
  const integrationToSave = {};
  integrationToSave.description = integration.description;
  integrationToSave.externalParameters = parseExternalParameters(integration.externalParameters);
  integrationToSave.id = integration.id;
  integrationToSave.internalParameters = {
    dataflowId: integration.dataflowId,
    datasetSchemaId: integration.datasetSchemaId,
    fileExtension: integration.fileExtension,
    processName: integration.processName.value,
    repository: integration.repository.value
  };
  integrationToSave.name = integration.name;
  integrationToSave.operation = integration.operation.value;
  integrationToSave.tool = integration.tool;

  if (integration.notificationRequired) {
    integrationToSave.internalParameters.notificationRequired = integration.notificationRequired;
  }

  return integrationToSave;
};

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

const runIntegration = async (integrationId, datasetId, replaceData) =>
  await integrationRepository.runIntegration(integrationId, datasetId, replaceData);

export const IntegrationService = {
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
