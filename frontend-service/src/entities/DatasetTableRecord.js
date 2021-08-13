export class DatasetTableRecord {
  constructor({ datasetPartitionId, fields, providerCode, recordId, recordSchemaId, validations } = {}) {
    this.datasetPartitionId = datasetPartitionId;
    this.fields = fields;
    this.providerCode = providerCode;
    this.recordId = recordId;
    this.recordSchemaId = recordSchemaId;
    this.validations = validations;
  }
}
