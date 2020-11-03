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

const parsePamsRecords = records =>
  records.map(record => {
    const { recordId, recordSchemaId } = record;
    let data = {};

    record.elements.forEach(
      element => (data = { ...data, [element.name]: element.value, recordId: recordId, recordSchemaId: recordSchemaId })
    );

    return data;
  });

export const TableManagementUtils = { parsePamsRecords, parseTableSchemaColumns };
