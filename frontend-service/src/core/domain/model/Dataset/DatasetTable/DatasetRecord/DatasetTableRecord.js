export class DatasetTableRecord {
  constructor(datasetPartitionId, recordId, recordSchemaId, fields, validations, providerCode) {
    this.datasetPartitionId = datasetPartitionId;
    this.fields = fields;
    this.providerCode = providerCode;
    this.recordId = recordId;
    this.recordSchemaId = recordSchemaId;
    this.validations = validations;
  }
}
