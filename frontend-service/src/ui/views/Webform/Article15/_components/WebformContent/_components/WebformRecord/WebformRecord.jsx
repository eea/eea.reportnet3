import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './WebformRecord.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { InputText } from 'ui/views/_components/InputText';

import { DatasetService } from 'core/services/Dataset';

import { webformRecordReducer } from './_functions/Reducers/webformRecordReducer';

import { WebformRecordUtils } from './_functions/Utils/WebformRecordUtils';

export const WebformRecord = ({ datasetId, record, tableId }) => {
  const [webformRecordState, webformRecordDispatch] = useReducer(webformRecordReducer, {
    fields: {},
    isNewRecord: false,
    newRecord: {},
    record: {}
  });

  console.log('webformRecordState', webformRecordState);

  useEffect(() => {
    console.log('column record.webformFields', record.webformFields);
    webformRecordDispatch({
      type: 'INITIAL_LOAD',
      payload: {
        fields: WebformRecordUtils.getFormInitialValues(record.webformFields),
        newRecord: WebformRecordUtils.parseNewRecordData(record.webformFields, undefined),
        record: WebformRecordUtils.getRecordsInitialValues(record)
      }
    });
  }, [record]);

  const onDeleteMultipleWebform = () => {};

  const onFillField = (option, value) => {
    webformRecordState.newRecord.dataRow.filter(data => Object.keys(data.fieldData)[0] === option)[0].fieldData[
      option
    ] = value;

    webformRecordDispatch({ type: 'ON_FILL_FIELD', payload: { option, value } });
  };

  const onSaveField = async (option, value, recordId) => {
    try {
      await DatasetService.addRecordsById(datasetId, tableId, [webformRecordState.newRecord]);
    } catch (error) {
      console.log('error', error);
    }
  };

  const onEditorSubmitValue = async (field, option, value) => {
    try {
      DatasetService.updateFieldById(
        datasetId,
        option,
        field.fieldId,
        field.fieldType,
        field.type === 'MULTISELECT_CODELIST' || (field.type === 'LINK' && Array.isArray(value))
          ? value.join(',')
          : value
      );
    } catch (error) {
      console.log('error', error);
    }
  };

  const renderTemplate = (field, option, type) => {
    switch (type) {
      case 'TEXT':
        return (
          <InputText
            onBlur={event => {
              console.log('field.recordId', field.recordId);
              if (isNil(field.recordId)) {
                onSaveField(option, event.target.value);
              } else {
                onEditorSubmitValue(field, option, event.target.value);
              }
            }}
            onChange={event => onFillField(option, event.target.value)}
            type="text"
            value={field.value}
          />
        );

      case 'DATE':
        return <Calendar />;

      default:
        break;
    }
  };

  return (
    <div className={styles.contentWrap}>
      {record.multiple ? (
        <div className={styles.actionButtons}>
          <Button
            className={`${styles.collapse} p-button-rounded p-button-secondary p-button-animated-blink`}
            icon={'plus'}
          />
          <Button
            className={`${styles.delete} p-button-rounded p-button-secondary p-button-animated-blink`}
            icon={'trash'}
            onClick={() => onDeleteMultipleWebform()}
          />
        </div>
      ) : (
        <Fragment />
      )}

      {!isEmpty(webformRecordState.record) &&
        webformRecordState.record.webformFields.map((field, i) => {
          return (
            <div key={i} className={styles.content}>
              <p>{field.fieldName}</p>
              <div>{renderTemplate(field, field.fieldSchemaId, field.fieldType)}</div>
            </div>
          );
        })}
    </div>
  );
};

WebformRecord.propTypes = { record: PropTypes.shape({ webformFields: PropTypes.array }) };

WebformRecord.defaultProps = { record: { webformFields: [] } };
