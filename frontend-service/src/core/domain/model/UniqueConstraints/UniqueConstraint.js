export class UniqueConstraint {
  constructor({ datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId } = {}) {
    this.datasetSchemaId = datasetSchemaId;
    this.fieldSchemaIds = fieldSchemaIds;
    this.tableSchemaId = tableSchemaId;
    this.uniqueId = uniqueId;
  }
}
