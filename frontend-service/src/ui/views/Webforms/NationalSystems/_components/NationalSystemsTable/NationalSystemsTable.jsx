import React, { useEffect, useState } from 'react';

import isNil from 'lodash/isNil';

import styles from './NationalSystemsTable.module.scss';

import { NationalSystemsRecord } from './_components/NationalSystemsRecord';

import { DatasetService } from 'core/services/Dataset';

import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';

export const NationalSystemsTable = ({ datasetId, schemaTables, tables, tableSchemaId }) => {
  const [data, setData] = useState([]);
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

      setData(parseData(response.records));
    } catch (error) {
      console.log('error', error);
    } finally {
      setIsLoading(false);
    }
  };

  const parseData = (dataRecords = []) => {
    const records = dataRecords.map(record => {
      const { fields, recordId, recordSchemaId, validations } = record;

      return { fields: parseFields(fields), recordId, recordSchemaId, validations };
    });

    return records.map(tes => {
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

  if (isLoading) return 'SPINNER';

  return (
    <div className={styles.content}>
      {data.map((record, index) => (
        <NationalSystemsRecord record={record} index={index} datasetId={datasetId} />
      ))}
    </div>
  );
};
