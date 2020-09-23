import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './WebformRecord.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { MultiSelect } from 'ui/views/_components/MultiSelect';

import { DatasetService } from 'core/services/Dataset';

import { webformRecordReducer } from './_functions/Reducers/webformRecordReducer';

import { WebformRecordUtils } from './_functions/Utils/WebformRecordUtils';

export const WebformRecord = ({ datasetId, record, tableId }) => {
  const [webformRecordState, webformRecordDispatch] = useReducer(webformRecordReducer, { newRecord: {}, record });

  useEffect(() => {
    webformRecordDispatch({
      type: 'INITIAL_LOAD',
      payload: { newRecord: WebformRecordUtils.parseNewRecordData(record.webformFields, undefined) }
    });
  }, [record]);

  const onDeleteMultipleWebform = async () => {
    try {
      await DatasetService.deleteRecordById(datasetId, webformRecordState.record.recordId);
    } catch (error) {}
  };

  const onFillField = (option, value) => {
    webformRecordState.newRecord.dataRow.filter(data => Object.keys(data.fieldData)[0] === option)[0].fieldData[
      option
    ] = value;

    webformRecordState.record.webformFields.filter(field => field.fieldSchemaId === option)[0].value = value;

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
      case 'DATE':
        return <Calendar />;

      case 'LINK':
        return <a href=""></a>;

      case 'MULTISELECT':
        return <MultiSelect />;

      case 'SELECT':
        return <Dropdown />;

      case 'TEXT':
        return (
          <InputText
            onBlur={event => {
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else onEditorSubmitValue(field, option, event.target.value);
            }}
            onChange={event => onFillField(option, event.target.value)}
            type="text"
            value={field.value}
          />
        );

      default:
        break;
    }
  };

  return (
    <div className={styles.contentWrap}>
      {webformRecordState.record.multiple && !isEmpty(webformRecordState.record.webformFields) ? (
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
      {!isEmpty(webformRecordState.record.webformFields)
        ? webformRecordState.record.webformFields.map((field, i) => {
            return (
              <div key={i} className={styles.content}>
                <p>{field.fieldName}</p>
                <div>{renderTemplate(field, field.fieldSchemaId, field.fieldType)}</div>
              </div>
            );
          })
        : 'There are no fields'}
    </div>
  );
};

WebformRecord.propTypes = { record: PropTypes.shape({ webformFields: PropTypes.array }) };

WebformRecord.defaultProps = { record: { webformFields: [] } };
