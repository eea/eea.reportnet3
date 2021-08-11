import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import sortBy from 'lodash/sortBy';

import { Integration } from 'entities/Integration';

const parseExternalParameters = parameterDTO => {
  const externalParameters = {};
  for (let index = 0; index < parameterDTO.length; index++) {
    const parameter = parameterDTO[index];
    externalParameters[parameter.key] = parameter.value;
  }
  return externalParameters;
};

const parseDatasetSchemaId = (datasetSchemaId, dataflowId) =>
  new Integration({
    internalParameters: { dataflowId, datasetSchemaId }
  });

const parseIntegration = integration =>
  new Integration({
    externalParameters: integration.externalParameters,
    integrationDescription: integration.description,
    integrationId: integration.id,
    integrationName: integration.name,
    internalParameters: integration.internalParameters,
    operation: integration.operation,
    operationName: integration.operation.split('_').join(' '),
    tool: integration.tool
  });

const parseIntegrationsList = (integrations = []) => {
  const integrationsDTO = integrations.map(integration => parseIntegration(integration));
  return sortBy(integrationsDTO, ['integrationId']);
};

const parseIntegrationsOperationsExtensionsList = (integrations = []) =>
  integrations.map(integration => parseIntegrationOperationExtension(integration));

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

export const IntegrationUtils = {
  parseDatasetSchemaId,
  parseIntegrationsList,
  parseIntegrationsOperationsExtensionsList,
  parseIntegration,
  parseProcessList,
  parseRepositoryList,
  parseManageIntegration
};
