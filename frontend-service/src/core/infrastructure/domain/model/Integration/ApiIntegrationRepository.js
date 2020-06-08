import { apiIntegration } from 'core/infrastructure/api/domain/model/Integration/ApiIntegration';

const all = async () => apiIntegration.all();

const create = async integration => {
  const integrationDTO = {
    name: integration.name,
    description: integration.description,
    tool: integration.tool,
    operation: integration.operation,
    internalParameters: { fileExtension: integration.fileExtension, datasetSchemaId: integration.datasetSchemaId },
    externalParameters: parseExternalParameters(integration.externalParameters)
  };

  return apiIntegration.create(integrationDTO);
};

const parseExternalParameters = parameterDTO => {
  const externalParameters = {};
  for (let index = 0; index < parameterDTO.length; index++) {
    const parameter = parameterDTO[index];
    externalParameters[parameter.key] = parameter.value;
  }
  return externalParameters;
};

export const ApiIntegrationRepository = { all, create };
