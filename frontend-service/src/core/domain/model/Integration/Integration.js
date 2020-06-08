export class Integration {
  constructor({ id, name, description, tool, operation, internalParameters, externalParameters } = {}) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.tool = tool;
    this.operation = operation;
    this.internalParameters = internalParameters;
    this.externalParameters = externalParameters;
  }
}
