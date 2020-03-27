export class DatasetTable {
  constructor({
    hasErrors,
    hasPKReferenced,
    recordSchemaId,
    records,
    recordsWithErrorsPercentage,
    recordsWithWarningsPercentage,
    recordsWithoutErrorsPercentage,
    tableSchemaId,
    tableSchemaName,
    totalRecords
  } = {}) {
    this.hasErrors = hasErrors;
    this.hasPKReferenced = hasPKReferenced;
    this.records = records;
    this.recordSchemaId = recordSchemaId;
    this.recordsWithoutErrorsPercentage = recordsWithoutErrorsPercentage;
    this.recordsWithErrorsPercentage = recordsWithErrorsPercentage;
    this.recordsWithWarningsPercentage = recordsWithWarningsPercentage;
    this.tableSchemaId = tableSchemaId;
    this.tableSchemaName = tableSchemaName;
    this.totalRecords = totalRecords;
  }
}
