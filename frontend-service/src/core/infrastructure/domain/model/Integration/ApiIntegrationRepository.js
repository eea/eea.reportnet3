import isNil from 'lodash/isNil';

import { apiIntegration } from 'core/infrastructure/api/domain/model/Integration/ApiIntegration';

import { Integration } from 'core/domain/model/Integration/Integration';

const all = async datasetSchemaId =>
  parseIntegrationsList(await apiIntegration.all(parseDatasetSchemaId(datasetSchemaId)));

const allExtensionsOperations = async datasetSchemaId =>
  parseIntegrationsOperationsExtensionsList(
    await apiIntegration.allExtensionsOperations(parseDatasetSchemaId(datasetSchemaId))
  );

const create = async integration => apiIntegration.create(parseManageIntegration(integration));

const deleteById = async integrationId => {
  return await apiIntegration.deleteById(integrationId);
};

const parseDatasetSchemaId = datasetSchemaId => {
  const integration = new Integration();

  integration.internalParameters = { datasetSchemaId: datasetSchemaId };

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

// const parseIntegrationId = integrationId => new Integration({ integrationId });

const parseIntegrationsList = integrationsDTO => {
  if (!isNil(integrationsDTO)) {
    const integrations = [];
    integrationsDTO.forEach(integrationDTO => integrations.push(parseIntegration(integrationDTO)));

    return integrations;
  }
  return;
};

const parseIntegrationsOperationsExtensionsList = integrationsDTO => {
  console.log({ integrationsDTO });
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
    processName: integration.processName
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

const update = async integration => apiIntegration.update(parseManageIntegration(integration));

export const ApiIntegrationRepository = { all, allExtensionsOperations, create, deleteById, update };
