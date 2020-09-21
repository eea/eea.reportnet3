import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import PropTypes from 'prop-types';

import styles from './WebformRecord.module.scss';

import { Button } from 'ui/views/_components/Button';
import { InputText } from 'ui/views/_components/InputText';

import { DatasetService } from 'core/services/Dataset';

import { webformRecordReducer } from './_functions/Reducers/webformRecordReducer';

import { WebformRecordUtils } from './_functions/Utils/WebformRecordUtils';

export const WebformRecord = ({ datasetId, record, tableId }) => {
  const [webformRecordState, webformRecordDispatch] = useReducer(webformRecordReducer, {
    fields: WebformRecordUtils.getFormInitialValues(record.webformFields),
    isNewRecord: false,
    newRecord: WebformRecordUtils.parseNewRecordData(record.webformFields, undefined)
  });

  // console.log('record WEBFORM', record);

  // console.log('webformRecordState', webformRecordState);

  const onDeleteMultipleWebform = () => {};

  const onFillField = (option, value) => {
    // TODO: Move to webformRecordReducer.js
    webformRecordState.newRecord.dataRow.filter(data => Object.keys(data.fieldData)[0] === option)[0].fieldData[
      option
    ] = value;

    webformRecordDispatch({ type: 'ON_FILL_FIELD', payload: { option, value } });
  };

  const onSaveField = async (option, value) => {
    try {
      if (webformRecordState.isNewRecord) {
        await DatasetService.addRecordsById(datasetId, tableId, [webformRecordState.newRecord]);
      } else {
        await DatasetService.updateRecordsById(datasetId);
      }
    } catch (error) {
      console.log('error', error);
    }
  };

  const renderTemplate = (option, type) => {
    switch (type) {
      case 'TEXT':
        return (
          <InputText
            // keyfilter={getFilter(type)}
            // maxLength={textCharacters}
            // onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            // onFocus={e => {
            //   e.preventDefault();
            //   onEditorValueFocus(cells, e.target.value);
            // }}
            // onKeyDown={e => onEditorKeyChange(cells, e, record)}
            onBlur={event => onSaveField(option, event.target.value)}
            onChange={event => onFillField(option, event.target.value)}
            type="text"
            // value={RecordUtils.getCellValue(cells, cells.field)}
          />
        );

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

      {record.webformFields.map((field, i) => {
        return (
          <div key={i} className={styles.content}>
            <p>{field.fieldName}</p>
            <div>{renderTemplate(field.fieldId, field.fieldType)}</div>
          </div>
        );
      })}
    </div>
  );
};

WebformRecord.propTypes = { record: PropTypes.shape({ webformFields: PropTypes.array }) };

WebformRecord.defaultProps = { record: { webformFields: [] } };
