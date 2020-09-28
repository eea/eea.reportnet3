import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './WebformRecord.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { IconTooltip } from 'ui/views/_components/IconTooltip';
import { InputText } from 'ui/views/_components/InputText';
import { MultiSelect } from 'ui/views/_components/MultiSelect';

import { DatasetService } from 'core/services/Dataset';

import { webformRecordReducer } from './_functions/Reducers/webformRecordReducer';

import { WebformRecordUtils } from './_functions/Utils/WebformRecordUtils';
import { Article15Utils } from 'ui/views/Webform/Article15/_functions/Utils/Article15Utils';

export const WebformRecord = ({ datasetId, onRefresh, record, tableId }) => {
  const [webformRecordState, webformRecordDispatch] = useReducer(webformRecordReducer, { newRecord: {}, record });

  useEffect(() => {
    webformRecordDispatch({
      type: 'INITIAL_LOAD',
      payload: { newRecord: Article15Utils.parseNewRecordData(record.webformFields, undefined) }
    });
  }, [record]);

  const onDeleteMultipleWebform = async () => {
    try {
      const isDataDeleted = await DatasetService.deleteRecordById(datasetId, webformRecordState.record.recordId);
      if (isDataDeleted) onRefresh();
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

  const parseMultiselect = record => {
    record.dataRow.forEach(field => {
      if (
        field.fieldData.type === 'MULTISELECT_CODELIST' ||
        (field.fieldData.type === 'LINK' && Array.isArray(field.fieldData[field.fieldData.fieldSchemaId]))
      ) {
        if (
          !isNil(field.fieldData[field.fieldData.fieldSchemaId]) &&
          field.fieldData[field.fieldData.fieldSchemaId] !== ''
        ) {
          if (Array.isArray(field.fieldData[field.fieldData.fieldSchemaId])) {
            field.fieldData[field.fieldData.fieldSchemaId] = field.fieldData[field.fieldData.fieldSchemaId].join(',');
          } else {
            field.fieldData[field.fieldData.fieldSchemaId] = field.fieldData[field.fieldData.fieldSchemaId]
              .split(',')
              .map(item => item.trim())
              .join(',');
          }
        }
      }
    });
    return record;
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

  const formatDate = (date, isInvalidDate) => {
    if (isInvalidDate) return '';

    let d = new Date(date),
      month = '' + (d.getMonth() + 1),
      day = '' + d.getDate(),
      year = d.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('-');
  };

  const renderTemplate = (field, option, type) => {
    // // console.log('field.options', field.options);
    switch (type) {
      case 'DATE':
        return (
          <Calendar
            appendTo={document.body}
            dateFormat="yy-mm-dd"
            monthNavigator={true}
            onChange={event => {
              onFillField(option, formatDate(event.target.value, isNil(event.target.value)));
              if (isNil(field.recordId)) onSaveField(option, formatDate(event.target.value, isNil(event.target.value)));
              else onEditorSubmitValue(field, option, formatDate(event.target.value, isNil(event.target.value)));
            }}
            onFocus={event => {}}
            value={new Date(field.value)}
            yearNavigator={true}
            yearRange="2010:2030"
          />
        );

      case 'LINK':
        return (
          <InputText
            // keyfilter={getFilter(type)}
            // maxLength={urlCharacters}
            onBlur={event => {}}
            onChange={event => {}}
            onFocus={event => {
              event.preventDefault();
              // onEditorValueFocus(cells, event.target.value);
            }}
            onKeyDown={event => {}}
            value={field.value}
          />
        );

      case 'MULTISELECT':
        return (
          <MultiSelect
            appendTo={document.body}
            maxSelectedLabels={10}
            onChange={event => {
              onFillField(option, event.target.value);
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else onEditorSubmitValue(field, option, event.target.value);
            }}
            // onFocus={e => {
            //   e.preventDefault();
            //   if (!isUndefined(codelistItemValue)) {
            //     onEditorValueFocus(cells, codelistItemValue);
            //   }
            // }}
            options={field.options}
            // optionLabel="itemType"
            value={field.value}
          />
        );

      case 'SELECT':
        return (
          <Dropdown
            appendTo={document.body}
            // currentValue={RecordUtils.getCellValue(cells, cells.field)}
            // filter={true}
            // filterPlaceholder={resources.messages['linkFilterPlaceholder']}
            // filterBy="itemType,value"
            onChange={event => {
              onFillField(option, event.target.value);
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else onEditorSubmitValue(field, option, event.target.value);
            }}
            // onFilterInputChangeBackend={onFilter}
            // onMouseDown={e => onEditorValueFocus(cells, event.target.value)}
            // optionLabel="label"
            options={field.options}
            showFilterClear={true}
            value={field.value}
          />
        );

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
      <div className={styles.actionButtons}>
        {!isEmpty(webformRecordState.record.validations) &&
          webformRecordState.record.validations.map((validation, index) => (
            <IconTooltip key={index} levelError={validation.levelError} message={validation.message} />
          ))}
      </div>
      {webformRecordState.record.multiple && !isEmpty(webformRecordState.record.webformFields) ? (
        <div className={styles.actionButtons}>
          {/* <Button
            className={`${styles.collapse} p-button-rounded p-button-secondary p-button-animated-blink`}
            icon={'plus'}
          /> */}
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
                <div>
                  {renderTemplate(field, field.fieldSchemaId, field.fieldType)}
                  {field.validations.map((validation, index) => (
                    <IconTooltip key={index} levelError={validation.levelError} message={validation.message} />
                  ))}
                </div>
              </div>
            );
          })
        : 'There are no fields'}
    </div>
  );
};

WebformRecord.propTypes = { record: PropTypes.shape({ webformFields: PropTypes.array }) };

WebformRecord.defaultProps = { record: { webformFields: [] } };
