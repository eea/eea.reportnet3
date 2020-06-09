const parseIntegration = data => ({
  description: data.integrationDescription,
  externalParameters: parseIntegrationParameters(data.externalParameters),
  fileExtension: data.internalParameters.fileExtension,
  id: data.integrationId,
  isUpdatedVisible: true,
  name: data.integrationName,
  operation: data.operation,
  processName: ''
});

const parseIntegrationsList = (data = []) => data.map(integration => parseIntegration(integration))[0];

const parseIntegrationParameters = parameters => {
  return Object.keys(parameters).map((item, index) => ({
    id: index,
    isEditorView: { key: false, value: false },
    key: parameters[item],
    value: parameters[item]
  }));
};

export const IntegrationsUtils = { parseIntegrationsList };
