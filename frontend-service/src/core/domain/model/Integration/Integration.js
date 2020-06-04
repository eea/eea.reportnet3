export class export class Integration {
  constructor({ datasetSchemaId, externalTool, externalUrl, fileExtension, integrationId, integrationName, operation, parameters } = {}) {
    this.datasetSchemaId = datasetSchemaId
    this.externalTool = externalTool;
    this.externalUrl = externalUrl;
    this.fileExtension = fileExtension;
    this.integrationId = integrationId;
    this.integrationName = integrationName;
    this.operation = operation;
    this.parameters = parameters;
  }
} 
