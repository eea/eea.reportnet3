import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './WebformContent.module.scss';

import { Button } from 'ui/views/_components/Button';
import { WebformRecord } from './_components/WebformRecord';

import { DatasetService } from 'core/services/Dataset';

import { Article15Utils } from 'ui/views/Webform/Article15/_functions/Utils/Article15Utils';

export const WebformContent = ({ datasetId, webform }) => {
  const [refresh, setRefresh] = useState(false);
  const [webformData, setWebformData] = useState({});

  useEffect(() => {
    onLoadTableData();
  }, [refresh]);

  const onAddMultipleWebform = async () => {
    if (!isEmpty(webformData.webformRecords)) {
      const newEmptyRecord = Article15Utils.parseNewRecordData(webformData.webformRecords[0].webformFields);
      console.log('newEmptyRecord', newEmptyRecord);

      try {
        await DatasetService.addRecordsById(datasetId, webformData.tableSchemaId, [newEmptyRecord]);
      } catch (error) {}
    }
  };

  const onLoadTableData = async () => {
    try {
      const tableData = await DatasetService.tableDataById(datasetId, webform.tableSchemaId, '', '', undefined, [
        'CORRECT',
        'INFO',
        'WARNING',
        'ERROR',
        'BLOCKER'
      ]);

      // let filteredFields = webform.webformRecords.map(record =>
      //   tableData.records.filter(tableRecord => tableRecord.recordSchemaId === record.webformFields[0].recordId)
      // )[0];

      if (!isNil(tableData.records)) {
        webform.webformRecords = tableData.records.map(record => {
          const records = { ...webform.webformRecords[0], ...record };
          records['webformFields'] = record.fields.map((field, i) => {
            const webformField = getFieldIndexById(field, webform.webformRecords[0].webformFields);
            return {
              fieldId: field.fieldId,
              fieldName: webformField.fieldName,
              fieldSchemaId: field.fieldSchemaId,
              fieldType: field.type || webformField.fieldType,
              recordId: record.recordId,
              recordSchemaId: field.recordId,
              validations: field.validations || [],
              value: field.value || ''
            };
          });
          return records;
        });
      } else {
        webform.webformRecords.map(record => {
          record['webformFields'] = record.webformFields.map((field, i) => {
            return {
              fieldId: null,
              fieldName: field.fieldName,
              fieldSchemaId: field.fieldId,
              fieldType: field.type,
              recordSchemaId: field.recordId,
              validations: field.validations || [],
              value: field.value || ''
            };
          });
        });
      }

      // webform.webformRecords.forEach(record => {
      //   record.webformFields.forEach((field, i) => {
      //     if (!isNil(filteredFields) && !isEmpty(filteredFields)) {
      //       let filteredTableField = filteredFields[0].fields.filter(
      //         filteredField => filteredField.fieldSchemaId === field.fieldId
      //       );
      //       if (!isNil(filteredTableField)) {
      //         field.recordSchemaId = filteredFields[0].recordId;
      //         field.fieldSchemaId = filteredTableField[0].fieldId;
      //         field.value = filteredFields[0].fields[i].value;
      //       }
      //     }
      //   });
      // });
      setWebformData(webform);
    } catch (error) {
      console.log('error', error);
    }
  };

  const onRefresh = () => setRefresh(!refresh);

  const getFieldIndexById = (field, allFields) =>
    allFields.filter(completeField => completeField.fieldId === field.fieldSchemaId)[0];

  const renderWebformRecords = multiple => {
    return multiple ? (
      webformData.webformRecords.map((record, i) => {
        return (
          <WebformRecord
            datasetId={datasetId}
            key={i}
            onRefresh={onRefresh}
            record={record}
            tableId={webformData.tableSchemaId}
          />
        );
      })
    ) : (
      <WebformRecord
        datasetId={datasetId}
        onRefresh={onRefresh}
        record={webformData.webformRecords[0]}
        tableId={webformData.tableSchemaId}
      />
    );
  };

  return !isEmpty(webformData) ? (
    <div className={styles.body}>
      <h3 className={styles.title}>
        {webformData.webformTitle}
        {webformData.multipleRecords ? (
          <Button label={'Add'} icon={'plus'} onClick={() => onAddMultipleWebform()} />
        ) : (
          <Fragment />
        )}
      </h3>
      {webformData.description ? <h3 className={styles.description}>{webformData.description}</h3> : <Fragment />}
      {renderWebformRecords(webformData.multipleRecords)}
    </div>
  ) : (
    'hey'
  );
};

WebformContent.propTypes = { webform: PropTypes.object };

WebformContent.defaultProps = { webform: {} };
