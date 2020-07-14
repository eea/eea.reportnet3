import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import sortBy from 'lodash/sortBy';

import { apiIntegration } from 'core/infrastructure/api/domain/model/Integration/ApiIntegration';

import { Integration } from 'core/domain/model/Integration/Integration';

const all = async (dataflowId, datasetSchemaId) => {
  return parseIntegrationsList(await apiIntegration.all(parseDatasetSchemaId(datasetSchemaId, dataflowId)));
};

const allExtensionsOperations = async datasetSchemaId =>
  parseIntegrationsOperationsExtensionsList(
    await apiIntegration.allExtensionsOperations(parseDatasetSchemaId(datasetSchemaId))
  );

const create = async integration => apiIntegration.create(parseManageIntegration(integration));

const deleteById = async (dataflowId, integrationId) => await apiIntegration.deleteById(dataflowId, integrationId);

const getProcesses = async repositoryName => parseProcessList(await apiIntegration.getProcesses(repositoryName));

const getRepositories = async () => parseRepositoryList(await apiIntegration.getRepositories());

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

const parseIntegration = integrationDTO => {
  const integration = new Integration();
  integration.externalParameters = integrationDTO.externalParameters;
  integration.integrationDescription = integrationDTO.description;
  integration.integrationId = integrationDTO.id;
  integration.integrationName = integrationDTO.name;
  integration.internalParameters = integrationDTO.internalParameters;
  integration.operation = integrationDTO.operation;
  integration.tool = integrationDTO.tool;

  return integration;
};

const parseIntegrationsList = integrationsDTO => {
  if (!isNil(integrationsDTO)) {
    const integrations = [];
    integrationsDTO.forEach(integrationDTO => integrations.push(parseIntegration(integrationDTO)));
    return sortBy(integrations, ['integrationId']);
  }
  return;
};

const parseIntegrationsOperationsExtensionsList = integrationsDTO => {
  if (!isNil(integrationsDTO)) {
    const integrations = [];
    integrationsDTO.forEach(integrationDTO => integrations.push(parseIntegrationOperationExtension(integrationDTO)));

    return integrations;
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

export const ApiIntegrationRepository = {
  all,
  allExtensionsOperations,
  create,
  deleteById,
  getProcesses,
  getRepositories,
  update
};
