import React, { Fragment, useEffect, useReducer } from 'react';
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

import { Article15Utils } from 'ui/views/Webform/Article15/_functions/Utils/Article15Utils';

export const WebformRecord = ({ onAddMultipleWebform, datasetId, onRefresh, onTabChange, record, tableId }) => {
  const [webformRecordState, webformRecordDispatch] = useReducer(webformRecordReducer, { newRecord: {}, record });

  useEffect(() => {
    webformRecordDispatch({
      type: 'INITIAL_LOAD',
      payload: { newRecord: Article15Utils.parseNewRecordData(record.elements, undefined) }
    });
  }, [record, onTabChange]);

  const onDeleteMultipleWebform = async () => {
    try {
      const isDataDeleted = await DatasetService.deleteRecordById(datasetId, webformRecordState.record.recordId);
      if (isDataDeleted) onRefresh();
    } catch (error) {
      console.log('error', error);
    }
  };

  const onFillField = (option, value) => {
    webformRecordState.newRecord.dataRow.filter(data => Object.keys(data.fieldData)[0] === option)[0].fieldData[
      option
    ] = value;

    webformRecordState.record.elements.filter(field => field.fieldSchemaId === option)[0].value = value;

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
    switch (type) {
      case 'DATE':
        return (
          <Calendar
            appendTo={document.body}
            dateFormat="yy-mm-dd"
            id={field.fieldId}
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
            className={'p-disabled'}
            disabled={field.isDisabled}
            id={field.fieldId}
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

      case 'MULTISELECT_CODELIST':
        return (
          <MultiSelect
            appendTo={document.body}
            maxSelectedLabels={10}
            id={field.fieldId}
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

      case 'CODELIST':
        return (
          <Dropdown
            appendTo={document.body}
            id={field.fieldId}
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
            options={field.codelistItems}
            showFilterClear={true}
            value={field.value}
          />
        );

      case 'TEXT':
      case 'URL':
      case 'EMAIL':
      case 'PHONE':
      case 'NUMBER_INTEGER':
      case 'NUMBER_DECIMAL':
        return (
          <InputText
            id={field.fieldId}
            onBlur={event => {
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else onEditorSubmitValue(field, option, event.target.value);
            }}
            onChange={event => onFillField(option, event.target.value)}
            type="text"
            value={field.value}
          />
        );

      case 'EMPTY':
        return (
          <Fragment>
            <Button
              className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
              icon="errorCircle"
            />
            <span
              style={{ color: 'red' }}>{`The field ${field.name} is not created in the design, please check it`}</span>
          </Fragment>
        );

      default:
        break;
    }
  };

  const renderFields = elements => {
    return elements.map((field, i) => {
      if (field.type === 'FIELD') {
        return (
          <div key={i} className={styles.content}>
            <div>{field.title}</div>
            <div style={{ display: 'flex' }}>
              <div style={{ width: '75%' }}>{renderTemplate(field, field.fieldSchemaId, field.fieldType)}</div>
              {field.validations &&
                field.validations.map((validation, index) => (
                  <IconTooltip key={index} levelError={validation.levelError} message={validation.message} />
                ))}
            </div>
          </div>
        );
      } else {
        return (
          <div key={i} className={styles.body}>
            <h3 className={styles.title}>
              <div>
                {field.title ? field.title : field.name}
                {field.hasErrors && <IconTooltip levelError={'ERROR'} message={'This table has errors'} />}
              </div>
              {field.multipleRecords && (
                <Button
                  disabled
                  icon={'plus'}
                  label={'Add'}
                  onClick={() => onAddMultipleWebform(field.tableSchemaId)}
                />
              )}
            </h3>
            {field.tableNotCreated && (
              <span style={{ color: 'red' }}>
                {`The table ${field.name} is not created in the design, please check it`}
              </span>
            )}
            {field.elementsRecords.map((record, i) => {
              return (
                <WebformRecord
                  datasetId={datasetId}
                  key={i}
                  onAddMultipleWebform={onAddMultipleWebform}
                  onRefresh={onRefresh}
                  onTabChange={onTabChange}
                  record={record}
                  tableId={tableId}
                />
              );
            })}
          </div>
        );
      }
    });
  };

  const renderWebformContent = record => {
    return (
      <div className={styles.contentWrap}>
        <div className={styles.actionButtons}>
          {!isEmpty(record.validations) &&
            record.validations.map((validation, index) => (
              <IconTooltip key={index} levelError={validation.levelError} message={validation.message} />
            ))}
        </div>
        {record.multiple && !isEmpty(record.elements) && (
          <div className={styles.actionButtons}>
            <Button
              className={`${styles.delete} p-button-rounded p-button-secondary p-button-animated-blink`}
              icon={'trash'}
              onClick={() => onDeleteMultipleWebform()}
            />
          </div>
        )}
        {!isEmpty(record.elements) ? renderFields(record.elements) : 'There are no fields'}
      </div>
    );
  };

  return renderWebformContent(webformRecordState.record);
};

WebformRecord.propTypes = { record: PropTypes.shape({ elements: PropTypes.array }) };

WebformRecord.defaultProps = { record: { elements: [] } };
