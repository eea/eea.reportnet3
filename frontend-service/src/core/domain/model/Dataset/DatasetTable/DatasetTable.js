export class DatasetTable {
  constructor(
    hasErrors,
    tableSchemaId,
    tableSchemaName,
    recordsWithoutErrorsPercentage,
    recordsWithErrorsPercentage,
    recordsWithWarningsPercentage,
    records,
    totalRecords,
    recordSchemaId
  ) {
    this.hasErrors = hasErrors;
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
