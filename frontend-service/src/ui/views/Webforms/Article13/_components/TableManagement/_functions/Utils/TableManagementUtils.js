import { isEmpty } from 'lodash';
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

const parsePamsRecords = (records, parentTablesWithData, schemaTables) => {
  const getFilteredData = () => {
    if (isNil(parentTablesWithData) || isNil(schemaTables)) {
      return '';
    }
    const filteredParentTablesWithData = parentTablesWithData.filter(
      parentTableWithData => parentTableWithData.tableSchemaName !== 'PaMs'
    );

    const filteredparentTablesNames = filteredParentTablesWithData.map(
      filteredparentTable => filteredparentTable.tableSchemaName
    );

    const filteredSchemaTables = schemaTables
      .filter(filteredSchemaTable => filteredparentTablesNames.includes(filteredSchemaTable.header))
      .map(table => {
        return {
          tableSchemaId: table.tableSchemaId,
          tableSchemaName: table.tableSchemaName,
          pamField: table.records[0].fields.filter(field => field.name === 'Fk_PaMs')[0]
        };
      });
    return { filteredParentTablesWithData, filteredSchemaTables };
  };

  const { filteredParentTablesWithData, filteredSchemaTables } = getFilteredData();

  const getAdditionalInfo = record => {
    if (isNil(parentTablesWithData) || isNil(schemaTables)) {
      return '';
    }

    const getHasRecordByPaMId = table => {
      const fkPamId = filteredSchemaTables.filter(
        filteredSchemaTable => filteredSchemaTable.tableSchemaId === table.tableSchemaId
      )[0].pamField.fieldSchema;
      const pamValue = record.elements.filter(element => element.name === 'Id')[0].value;
      let hasRecord = false;
      table.data.records.forEach(record => {
        if (!isEmpty(record.fields)) {
          record.fields.forEach(field => {
            if (field.fieldSchemaId === fkPamId && parseInt(field.value) === parseInt(pamValue)) {
              hasRecord = true;
            }
          });
        }
      });
      return hasRecord;
    };

    const parentTablesIds = filteredParentTablesWithData.map(table => {
      return {
        tableSchemaId: table.tableSchemaId,
        tableSchemaName: table.tableSchemaName,
        hasRecord: getHasRecordByPaMId(table)
      };
    });
    return { tableSchemas: parentTablesIds };
  };

  return records.map(record => {
    const { recordId, recordSchemaId } = record;
    const additionalInfo = getAdditionalInfo(record);
    let data = {};

    record.elements.forEach(
      element =>
        (data = {
          ...data,
          [element.name]: element.value,
          recordId: recordId,
          recordSchemaId: recordSchemaId,
          tableSchemas: additionalInfo.tableSchemas
        })
    );

    return data;
  });
};

export const TableManagementUtils = { parsePamsRecords, parseTableSchemaColumns };
