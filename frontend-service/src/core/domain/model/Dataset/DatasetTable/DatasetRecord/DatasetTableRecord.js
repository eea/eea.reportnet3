export class DatasetTableRecord {
  constructor(datasetPartitionId, recordId, recordSchemaId, fields, validations) {
    this.datasetPartitionId = datasetPartitionId;
    this.fields = fields;
    this.recordId = recordId;
    this.recordSchemaId = recordSchemaId;
    this.validations = validations;
  }
}
