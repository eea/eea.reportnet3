export class Integration {
  constructor({ externalParameters, integrationDescription, integrationId, integrationName, internalParameters, operation, tool } = {}) {
    this.externalParameters = externalParameters;
    this.integrationDescription = integrationDescription;
    this.integrationId = integrationId;
    this.integrationName = integrationName;
    this.internalParameters = internalParameters;
    this.operation = operation;
    this.tool = tool;
  }
}
