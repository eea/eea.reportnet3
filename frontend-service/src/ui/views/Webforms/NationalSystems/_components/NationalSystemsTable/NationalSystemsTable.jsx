import isNil from 'lodash/isNil';
import React, { useEffect, useReducer, useState } from 'react';

import { DatasetService } from 'core/services/Dataset';
import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';
import { WebformsUtils } from 'ui/views/Webforms/_functions/Utils/WebformsUtils';
import { isEmpty } from 'lodash';

export const NationalSystemsTable = ({ data, datasetId, schemaTables, tables, tableSchemaId }) => {
  console.log('tables', tables);
  const [nationalData, setNationalData] = useState([]);
  console.log('nationalData', nationalData);

  useEffect(() => {
    onLoadTableData();
  }, []);

  const onLoadTableData = async () => {
    try {
      const response = await DatasetService.tableDataById(datasetId, tableSchemaId, '', 100, undefined, [
        'CORRECT',
        'INFO',
        'WARNING',
        'ERROR',
        'BLOCKER'
      ]);

      console.log('response', response);
      setNationalData(parseData(response.records));
    } catch (error) {
      console.log('error', error);
    }
  };

  const parseData = (dataRecords = []) => {
    const test = dataRecords.map(record => {
      const { fields, recordId, recordSchemaId, validations } = record;

      return { fields: parseFields(fields), recordId, recordSchemaId, validations };
    });

    return test.map(tes => {
      return { ...tes, elements: parseElements(tables.elements, tes.fields) };
    });
  };

  const parseElements = (elements = [], fields = []) => {
    elements.map(element => {
      Object.keys(element).forEach(key => {
        const value = fields.find(field => TextUtils.areEquals(field['name'], element[key]));
        element[key] = value ? value : element[key];
      });
    });

    return elements || [];
  };

  const parseFields = (dataFields = []) => {
    return dataFields.map(field => {
      const schemaField = schemaTables.records[0].fields.find(
        element =>
          !isNil(element['fieldId']) &&
          !isNil(field['fieldSchemaId']) &&
          TextUtils.areEquals(element['fieldId'], field['fieldSchemaId'])
      );

      return {
        codelistItems: schemaField.codelistItems,
        description: schemaField.description,
        fieldId: field.fieldId,
        fieldSchemaId: field.fieldSchemaId,
        label: schemaField.label,
        maxSize: schemaField.maxSize,
        name: schemaField.name,
        pk: schemaField.pk,
        pkHasMultipleValues: schemaField.pkHasMultipleValues,
        pkMustBeUsed: schemaField.pkMustBeUsed,
        pkReferenced: schemaField.pkReferenced,
        readOnly: schemaField.readOnly,
        recordId: field.recordId,
        referencedField: schemaField.referencedField,
        required: schemaField.required,
        type: field.type,
        unique: schemaField.unique,
        validations: schemaField.validations || field.validations,
        validExtensions: schemaField.validExtensions,
        value: field.value
      };
    });
  };

  if (isNil(nationalData.elements)) return 'haha';

  return nationalData.elements.map((dat, index) => (
    <div style={{ margin: '1rem' }}>
      TABLE: {index}
      <div> title: {dat.titleSource.value}</div>
      <div> tooltip: {dat.tooltipSource.value}</div>
      <div> data: {dat.name.value}</div>
    </div>
  ));
};
