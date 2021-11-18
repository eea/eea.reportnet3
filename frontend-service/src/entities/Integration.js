export class Integration {
  constructor({
    externalParameters,
    integrationDescription,
    integrationId,
    integrationName,
    internalParameters,
    operation,
    operationName,
    tool
  } = {}) {
    this.externalParameters = externalParameters;
    this.integrationDescription = integrationDescription;
    this.integrationId = integrationId;
    this.integrationName = integrationName;
    this.internalParameters = internalParameters;
    this.operation = operation;
    this.operationName = operationName;
    this.tool = tool;
  }
}
