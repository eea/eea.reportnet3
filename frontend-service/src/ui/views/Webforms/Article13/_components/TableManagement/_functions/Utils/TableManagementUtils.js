import isNil from 'lodash/isNil';

const parseTableSchemaColumns = schemaTables => {
  schemaTables.map(table => {
    if (!isNil(table.records)) {
      return table.records[0].fields.map(field => {
        return {
          codelistItems: field['codelistItems'],
          description: field['description'],
          field: field['fieldId'],
          header: field['name'],
          pk: field['pk'],
          maxSize: field['maxSize'],
          pkHasMultipleValues: field['pkHasMultipleValues'],
          readOnly: field['readOnly'],
          recordId: field['recordId'],
          referencedField: field['referencedField'],
          table: table['tableSchemaName'],
          type: field['type'],
          validExtensions: field['validExtensions']
        };
      });
    }
  });
};

const parsePamsRecords = (records, parentTablesWithData) => {
  const getHasRecordWithPamId = record => {
    if (isNil(parentTablesWithData)) {
      return '';
    }
    const filteredparentTablesWithData = parentTablesWithData.filter(
      parentTableWithData => parentTableWithData.tableSchemaName !== 'PaMs'
    );
  };

  return records.map(record => {
    const { recordId, recordSchemaId } = record;
    let data = {};

    record.elements.forEach(
      element =>
        (data = {
          ...data,
          [element.name]: element.value,
          recordId: recordId,
          recordSchemaId: recordSchemaId,
          hasRecord: getHasRecordWithPamId(record)
        })
    );

    return data;
  });
};

export const TableManagementUtils = { parsePamsRecords, parseTableSchemaColumns };
