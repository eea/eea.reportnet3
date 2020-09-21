import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import PropTypes from 'prop-types';

import isNil from 'lodash/isNil';

import styles from './WebformContent.module.scss';

import { Button } from 'ui/views/_components/Button';
import { WebformRecord } from './_components/WebformRecord';

import { DatasetService } from 'core/services/Dataset';

export const WebformContent = ({ datasetId, webform }) => {
  console.log('webform', webform);
  useEffect(() => {
    onLoadTableData();
  }, []);

  const onAddMultipleWebform = () => {};

  const onLoadTableData = async () => {
    try {
      const tableData = await DatasetService.tableDataById(datasetId, webform.tableSchemaId, '', '', undefined, [
        'CORRECT',
        'INFO',
        'WARNING',
        'ERROR',
        'BLOCKER'
      ]);

      let filteredFields = webform.webformRecords.map(record =>
        tableData.records.filter(tableRecord => tableRecord.recordSchemaId === record.webformFields[0].recordId)
      )[0];

      webform.webformRecords.forEach(record => {
        record.webformFields.forEach((field, i) => {
          if (!isNil(filteredFields)) {
            let filteredTableField = filteredFields[0].fields.filter(
              filteredField => filteredField.fieldSchemaId === field.fieldId
            );
            if (!isNil(filteredTableField)) {
              field.recordSchemaId = filteredFields[0].recordId;
              field.fieldSchemaId = filteredTableField[0].fieldId;
              field.value = filteredFields[0].fields[i].value;
            }
          }
        });
      });
    } catch (error) {
      console.log('error', error);
    }
  };

  const renderWebformRecords = () => {
    return webform.webformRecords.map((record, i) => (
      <WebformRecord key={i} record={record} datasetId={datasetId} tableId={webform.tableSchemaId} />
    ));
  };

  return (
    <div className={styles.body}>
      <h3 className={styles.title}>
        {webform.webformTitle}
        {webform.multipleRecords ? (
          <Button label={'Add'} icon={'plus'} onClick={() => onAddMultipleWebform()} />
        ) : (
          <Fragment />
        )}
      </h3>
      {webform.description ? <h3 className={styles.description}>{webform.description}</h3> : <Fragment />}
      {renderWebformRecords()}

      {/* {webform.multiple
        ? webformState.multipleView.map(element => renderContent(webform.webformFields, webform.multiple, element.id))
        : renderContent(webform.webformFields, webform.multiple)} */}
    </div>
  );
};

WebformContent.propTypes = { webform: PropTypes.object };

WebformContent.defaultProps = { webform: {} };
