import isNil from 'lodash/isNil';

import { apiIntegration } from 'core/infrastructure/api/domain/model/Integration/ApiIntegration';

import { Integration } from 'core/domain/model/Integration/Integration';

const all = async () => apiIntegration.all();
// const all = async integration => parseIntegrationsList(await apiIntegration.all(integration));

const create = async integration => apiIntegration.create(parseManageIntegration(integration));

const deleteById = async integrationId => {
  return await apiIntegration.deleteById(integrationId);
};

const parseExternalParameters = parameterDTO => {
  const externalParameters = {};
  for (let index = 0; index < parameterDTO.length; index++) {
    const parameter = parameterDTO[index];
    externalParameters[parameter.key] = parameter.value;
  }
  return externalParameters;
};

const parseIntegration = integrationDTO => new Integration(integrationDTO);

const parseIntegrationsList = integrationsDTO => {
  if (!isNil(integrationsDTO)) {
    const integrations = [];
    integrationsDTO.forEach(integrationDTO => integrations.push(parseIntegration(integrationDTO)));
    return integrations;
  }
  return;
};

const parseManageIntegration = integration => ({
  description: integration.description,
  externalParameters: parseExternalParameters(integration.externalParameters),
  id: integration.id,
  internalParameters: { fileExtension: integration.fileExtension, datasetSchemaId: integration.datasetSchemaId },
  name: integration.name,
  operation: integration.operation,
  tool: integration.tool
});

const update = async integration => apiIntegration.update(parseManageIntegration(integration));

export const ApiIntegrationRepository = { all, create, deleteById, update };
