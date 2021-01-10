import React, { Fragment, useEffect, useState } from 'react';

import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './NationalSystemsTable.module.scss';

import { NationalSystemsRecord } from './_components/NationalSystemsRecord';
import { Spinner } from 'ui/views/_components/Spinner';

import { DatasetService } from 'core/services/Dataset';

import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';

export const NationalSystemsTable = ({ datasetId, errorMessages, schemaTables, tables, tableSchemaId }) => {
  const [data, setData] = useState([]);
  const [schemaData, setSchemaData] = useState({});
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
      setSchemaData(response);
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

    return records.map(rec => {
      return { ...rec, elements: parseElements(tables.elements, rec.fields) };
    });
  };

  const parseElements = (elements = [], fields = []) => {
    const result = cloneDeep(elements);

    elements.map((element, index) => {
      Object.keys(element).forEach(key => {
        const value = fields.find(field => TextUtils.areEquals(field['name'], element[key]));

        result[index][key] = value ? value : element[key];
      });
    });

    return result || [];
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

  const renderErrors = () => {
    const errors = errorMessages(schemaData, tables.name);

    return (
      <ul>
        {errors.map((error, index) => (
          <li key={index}>{error}</li>
        ))}
      </ul>
    );
  };

  const renderRecords = () => {
    if (!isEmpty(errorMessages(schemaData, tables.name))) return renderErrors();

    return data.map((record, index) => (
      <Fragment key={index}>
        <NationalSystemsRecord datasetId={datasetId} record={record} />
      </Fragment>
    ));
  };

  if (isLoading) return <Spinner style={{ top: 0 }} />;

  return (
    <div className={styles.content}>
      <h2>{tables.title}</h2>

      {renderRecords()}
    </div>
  );
};
