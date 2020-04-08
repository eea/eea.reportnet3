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
    tableSchemaReadOnly,
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
    this.tableSchemaReadOnly = tableSchemaReadOnly;
    this.totalRecords = totalRecords;
  }
}
