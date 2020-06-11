const parseIntegration = data => ({
  description: data.integrationDescription,
  externalParameters: parseIntegrationParameters(data.externalParameters),
  fileExtension: data.internalParameters.fileExtension,
  id: data.integrationId,
  isUpdatedVisible: true,
  name: data.integrationName,
  operation: { label: data.operation, value: data.operation },
  processName: data.internalParameters.processName
});

const parseIntegrationsList = (data = []) => data.map(integration => parseIntegration(integration))[0];

const parseIntegrationParameters = parameters => {
  return Object.keys(parameters).map((item, index) => ({
    id: index,
    isEditorView: { key: false, value: false },
    key: item,
    prevValue: { key: '', value: '' },
    value: parameters[item]
  }));
};

export const IntegrationsUtils = { parseIntegrationsList };
