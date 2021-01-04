import isNil from 'lodash/isNil';
import React, { useEffect, useReducer, useState } from 'react';

import { DatasetService } from 'core/services/Dataset';
import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';
import { WebformsUtils } from 'ui/views/Webforms/_functions/Utils/WebformsUtils';
import { isEmpty } from 'lodash';
import { Fragment } from 'react';

export const NationalSystemsTable = ({ data, datasetId, schemaTables, tables, tableSchemaId }) => {
  const [nationalData, setNationalData] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

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

      setNationalData(parseData(response.records));
    } catch (error) {
      console.log('error', error);
    } finally {
      setIsLoading(false);
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

  if (isLoading) return 'haha';

  return nationalData.map((dat, index) => (
    <div style={{ margin: '1rem' }}>
      TABLE: {index}
      {dat.elements.map(element => {
        console.log('element', element);
        const { titleSource, tooltipSource, name } = element;
        return (
          <Fragment>
            <div> title: {titleSource ? titleSource.value : ''}</div>
            <div> tooltip: {tooltipSource ? tooltipSource.value : ''}</div>
            <div> data: {name ? name.value : ''}</div>
          </Fragment>
        );
      })}
    </div>
  ));
};
