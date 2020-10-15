import React, { Fragment, useContext, useEffect, useReducer } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { DatasetConfig } from 'conf/domain/model/Dataset';

import styles from './WebformRecord.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { IconTooltip } from 'ui/views/_components/IconTooltip';
import { InputText } from 'ui/views/_components/InputText';
import { MultiSelect } from 'ui/views/_components/MultiSelect';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { webformRecordReducer } from './_functions/Reducers/webformRecordReducer';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { WebformRecordUtils } from './_functions/Utils/WebformRecordUtils';

export const WebformRecord = ({
  columnsSchema,
  onAddMultipleWebform,
  datasetId,
  onRefresh,
  onTabChange,
  record,
  tableId
}) => {
  const resources = useContext(ResourcesContext);

  const [webformRecordState, webformRecordDispatch] = useReducer(webformRecordReducer, {
    isDeleteAttachmentVisible: false,
    isDeleteRowVisible: false,
    isDialogVisible: { deleteRow: false, uploadFile: false },
    isFileDialogVisible: false,
    newRecord: {},
    record,
    selectedField: {},
    selectedFieldId: '',
    selectedFieldSchemaId: '',
    selectedMaxSize: '',
    selectedRecordId: null,
    selectedValidExtensions: []
  });

  const {
    isDeleteAttachmentVisible,
    isDialogVisible,
    isFileDialogVisible,
    selectedField,
    selectedFieldId,
    selectedFieldSchemaId,
    selectedRecordId,
    selectedValidExtensions
  } = webformRecordState;

  const {
    formatDate,
    getInputMaxLength,
    getInputType,
    getMultiselectValues,
    parseMultiselect,
    parseNewRecordData
  } = WebformRecordUtils;

  useEffect(() => {
    webformRecordDispatch({
      type: 'INITIAL_LOAD',
      payload: { newRecord: parseNewRecordData(record.elements, undefined) }
    });
  }, [record, onTabChange]);

  const getAttachExtensions = [{ fileExtension: selectedValidExtensions || [] }]
    .map(file => file.fileExtension.map(extension => (extension.indexOf('.') > -1 ? extension : `.${extension}`)))
    .flat()
    .join(', ');

  const onAttach = async value => {
    onFillField(record, selectedFieldSchemaId, `${value.files[0].name}`);
    onToggleDialogVisible(false);
  };

  const onConfirmDeleteAttachment = async () => {
    const fileDeleted = await DatasetService.deleteFileData(datasetId, selectedFieldId);
    if (fileDeleted) {
      onFillField(record, selectedFieldSchemaId, '');
      onToggleDeleteAttachmentDialogVisible(false);
    }
  };

  const onDeleteMultipleWebform = async () => {
    try {
      const isDataDeleted = await DatasetService.deleteRecordById(datasetId, selectedRecordId);
      if (isDataDeleted) onRefresh();
    } catch (error) {
      console.error('error', error);
    }
  };

  const onFileDownload = async (fileName, fieldId) => {
    const fileContent = await DatasetService.downloadFileData(datasetId, fieldId);

    DownloadFile(fileContent, fileName);
  };

  const onEditorKeyChange = (event, field, option) => {
    if (event.key === 'Escape') {
    } else if (event.key === 'Enter') {
      onEditorSubmitValue(field, option, event.target.value);
    } else if (event.key === 'Tab') {
      onEditorSubmitValue(field, option, event.target.value);
    }
  };

  const onEditorSubmitValue = async (field, option, value) => {
    const parsedValue =
      field.type === 'MULTISELECT_CODELIST' || (field.type === 'LINK' && Array.isArray(value))
        ? value.join(',')
        : value;

    try {
      DatasetService.updateFieldById(datasetId, option, field.fieldId, field.fieldType, parsedValue);
    } catch (error) {
      console.error('error', error);
    }
  };

  const onFileDeleteVisible = (fieldId, fieldSchemaId) =>
    webformRecordDispatch({ type: 'ON_FILE_DELETE_OPENED', payload: { fieldId, fieldSchemaId } });

  const onFileUploadVisible = (fieldId, fieldSchemaId, validExtensions, maxSize) => {
    webformRecordDispatch({
      type: 'ON_FILE_UPLOAD_SET_FIELDS',
      payload: { fieldId, fieldSchemaId, validExtensions, maxSize }
    });
  };

  const onFillField = (field, option, value) => {
    webformRecordState.newRecord.dataRow.filter(data => Object.keys(data.fieldData)[0] === option)[0].fieldData[
      option
    ] = value;

    webformRecordState.record.elements.filter(field => field.fieldSchemaId === option)[0].value = value;

    webformRecordDispatch({ type: 'ON_FILL_FIELD', payload: { field, option, value } });
  };

  const onSaveField = async (option, value, recordId) => {
    try {
      await DatasetService.addRecordsById(datasetId, tableId, [parseMultiselect(webformRecordState.newRecord)]);
    } catch (error) {
      console.error('error', error);
    }
  };

  const onSelectField = field => webformRecordDispatch({ type: 'ON_SELECT_FIELD', payload: { field } });

  const onToggleDeleteAttachmentDialogVisible = value =>
    webformRecordDispatch({ type: 'ON_TOGGLE_DELETE_DIALOG', payload: { value } });

  const onToggleDialogVisible = value => webformRecordDispatch({ type: 'ON_TOGGLE_DIALOG', payload: { value } });

  const handleDialogs = (dialog, value) => {
    webformRecordDispatch({ type: 'HANDLE_DIALOGS', payload: { dialog, value } });
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
              onFillField(field, option, formatDate(event.target.value, isNil(event.target.value)));
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
              onFillField(field, option, event.target.value);
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else onEditorSubmitValue(field, option, event.target.value);
            }}
            // onFocus={e => {
            //   e.preventDefault();
            //   if (!isUndefined(codelistItemValue)) {
            //     onEditorValueFocus(cells, codelistItemValue);
            //   }
            // }}
            options={field.codelistItems.map(codelist => ({ label: codelist, value: codelist }))}
            // optionLabel="itemType"
            value={getMultiselectValues(
              field.codelistItems.map(codelist => ({ label: codelist, value: codelist })),
              field.value
            )}
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
              onFillField(field, option, event.target.value);
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else onEditorSubmitValue(field, option, event.target.value);
            }}
            // onFilterInputChangeBackend={onFilter}
            // onMouseDown={e => onEditorValueFocus(cells, event.target.value)}
            // optionLabel="label"
            options={field.codelistItems.map(codelist => ({ label: codelist, value: codelist }))}
            showFilterClear={true}
            value={field.value}
          />
        );

      case 'TEXT':
      case 'RICH_TEXT':
      case 'URL':
      case 'EMAIL':
      case 'PHONE':
      case 'NUMBER_INTEGER':
      case 'NUMBER_DECIMAL':
        return (
          <InputText
            id={field.fieldId}
            // keyfilter={getInputType[type]}
            maxLength={getInputMaxLength[type]}
            onBlur={event => {
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else onEditorSubmitValue(field, option, event.target.value);
            }}
            onChange={event => onFillField(field, option, event.target.value)}
            onKeyDown={event => onEditorKeyChange(event, field, option)}
            type="text"
            value={field.value}
          />
        );

      case 'EMPTY':
        return (
          <div className={styles.infoButtonWrapper}>
            <Button
              className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
              icon="errorCircle"
            />
            <span className={styles.nonExistField}>
              {`The field ${field.name} is not created in the design, please check it`}
            </span>
          </div>
        );

      case 'ATTACHMENT':
        const colSchema = columnsSchema.filter(colSchema => colSchema.fieldSchemaId === field.fieldSchemaId)[0];
        return (
          <div className={styles.attachmentWrapper}>
            {!isNil(field.value) && field.value !== '' && (
              <Button
                className={`${field.value === '' && 'p-button-animated-blink'} p-button-primary-transparent`}
                icon="export"
                iconPos="right"
                label={field.value}
                onClick={() => onFileDownload(field.value, field.fieldId)}
              />
            )}
            {
              <Button
                className={`p-button-animated-blink p-button-primary-transparent`}
                icon="import"
                label={
                  !isNil(field.value) && field.value !== ''
                    ? resources.messages['uploadReplaceAttachment']
                    : resources.messages['uploadAttachment']
                }
                onClick={() => {
                  onToggleDialogVisible(true);
                  onFileUploadVisible(
                    field.fieldId,
                    field.fieldSchemaId,
                    !isNil(colSchema) ? colSchema.validExtensions : [],
                    !isNil(colSchema) ? colSchema.maxSize : 20
                  );
                }}
              />
            }

            <Button
              className={`p-button-animated-blink p-button-primary-transparent`}
              icon="trash"
              onClick={() => onFileDeleteVisible(field.fieldId, field.fieldSchemaId)}
            />
          </div>
        );
      // return (
      //   <Button
      //     className={`p-button-animated-blink p-button-primary-transparent`}
      //     // disabled={true}
      //     icon={'import'}
      //     label={resources.messages['uploadAttachment']}
      //     onClick={() => {
      //       onToggleDialogVisible(true);
      //       onSelectField(field);
      //       // setIsAttachFileVisible(true);
      //       // onFileUploadVisible(
      //       //   fieldId,
      //       //   fieldSchemaId,
      //       //   !isNil(colSchema) ? colSchema.validExtensions : [],
      //       //   colSchema.maxSize
      //       // );
      //     }}
      //   />
      //);

      default:
        break;
    }
  };

  const renderFields = elements => {
    return elements.map((field, i) => {
      if (field.type === 'FIELD') {
        return (
          <div key={i} className={styles.field}>
            <label>{field.title}</label>
            <div className={styles.content}>
              <div className={styles.template}>{renderTemplate(field, field.fieldSchemaId, field.fieldType)}</div>
              {field.validations &&
                field.validations.map((validation, index) => (
                  <IconTooltip
                    className={'webform-validationErrors'}
                    key={index}
                    levelError={validation.levelError}
                    message={validation.message}
                  />
                ))}
            </div>
          </div>
        );
      } else {
        return (
          <div key={i} className={styles.subTable}>
            <h3 className={styles.title}>
              <div>
                {field.title ? field.title : field.name}
                {field.hasErrors && <IconTooltip levelError={'ERROR'} message={'This table has errors'} />}
              </div>
              {field.multipleRecords && (
                <Button icon={'plus'} label={'Add'} onClick={() => onAddMultipleWebform(field.tableSchemaId)} />
              )}
            </h3>
            {field.tableNotCreated && (
              <span className={styles.nonExistTable}>
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
      <div className={styles.content}>
        <div className={styles.actionButtons}>
          {!isEmpty(record.validations) &&
            record.validations.map((validation, index) => (
              <IconTooltip key={index} levelError={validation.levelError} message={validation.message} />
            ))}
        </div>
        {/* record.multiple && */ !isEmpty(record.elements) && (
          <div className={styles.actionButtons}>
            <Button
              className={`${styles.delete} p-button-rounded p-button-secondary p-button-animated-blink`}
              icon={'trash'}
              // onClick={() => onDeleteMultipleWebform(record.recordId)}
              onClick={() => {
                handleDialogs('deleteRow', true);
                webformRecordDispatch({ type: 'GET_DELETE_ROW_ID', payload: { recordId: record.recordId } });
              }}
            />
          </div>
        )}
        {!isEmpty(record.elements) ? renderFields(record.elements) : 'There are no fields'}
      </div>
    );
  };

  return (
    <Fragment>
      {renderWebformContent(webformRecordState.record)}
      {isFileDialogVisible && (
        <CustomFileUpload
          dialogClassName={styles.dialog}
          dialogHeader={resources.messages['uploadAttachment']}
          dialogOnHide={() => onToggleDialogVisible(false)}
          dialogVisible={isFileDialogVisible}
          accept={getAttachExtensions || '*'}
          chooseLabel={resources.messages['selectFile']}
          className={styles.fileUpload}
          fileLimit={1}
          isDialog={true}
          mode="advanced"
          multiple={false}
          invalidExtensionMessage={resources.messages['invalidExtensionFile']}
          name="file"
          onUpload={onAttach}
          operation="PUT"
          url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.importFileData, {
            datasetId,
            fieldId: selectedFieldId
          })}`}
        />
      )}

      {isDialogVisible.deleteRow && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resources.messages['deleteRow']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteMultipleWebform(selectedRecordId)}
          onHide={() => handleDialogs('deleteRow', false)}
          visible={isDialogVisible.deleteRow}>
          {resources.messages['confirmDeleteRow']}
        </ConfirmDialog>
      )}
      {isDeleteAttachmentVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={`${resources.messages['deleteAttachmentHeader']}`}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={onConfirmDeleteAttachment}
          onHide={() => onToggleDeleteAttachmentDialogVisible(false)}
          visible={isDeleteAttachmentVisible}>
          {resources.messages['deleteAttachmentConfirm']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};

WebformRecord.propTypes = { record: PropTypes.shape({ elements: PropTypes.array }) };

WebformRecord.defaultProps = { record: { elements: [] } };
