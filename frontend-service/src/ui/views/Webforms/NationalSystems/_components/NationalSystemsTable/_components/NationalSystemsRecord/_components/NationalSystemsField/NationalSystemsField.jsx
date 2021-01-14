import React, { useContext, useEffect, useReducer } from 'react';

import intersection from 'lodash/intersection';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import uniqBy from 'lodash/uniqBy';

import { DatasetConfig } from 'conf/domain/model/Dataset';

import styles from './NationalSystemsField.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { IconTooltip } from 'ui/views/_components/IconTooltip';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { MultiSelect } from 'ui/views/_components/MultiSelect';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { nationalSystemsFieldReducer } from './_functions/Reducers/nationalSystemsFieldReducer';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { RecordUtils, TextUtils } from 'ui/views/_functions/Utils';

export const NationalSystemsField = ({
  datasetId,
  getTableErrors,
  nationalField,
  recordValidations,
  title,
  tooltip
}) => {
  const getInputMaxLength = { TEXT: 10000, RICH_TEXT: 10000, EMAIL: 256, NUMBER_INTEGER: 20, NUMBER_DECIMAL: 40 };

  const resources = useContext(ResourcesContext);

  const [nationalSystemsFieldState, nationalSystemsFieldDispatch] = useReducer(nationalSystemsFieldReducer, {
    field: nationalField,
    isDialogVisible: { deleteAttachment: false, uploadFile: false },
    selectedValidExtensions: []
  });

  const { field, isDialogVisible, selectedValidExtensions } = nationalSystemsFieldState;

  useEffect(() => {
    getTableErrors(!isEmpty(recordValidations) || !isEmpty(field.validations));
  }, []);

  const getAttachExtensions = [{ fileExtension: selectedValidExtensions || [] }]
    .map(file => file.fileExtension.map(extension => (extension.indexOf('.') > -1 ? extension : `.${extension}`)))
    .flat()
    .join(', ');

  const getMultiselectValues = (multiselectItemsOptions, value) => {
    if (!isUndefined(value) && !isUndefined(value[0]) && !isUndefined(multiselectItemsOptions)) {
      const splittedValue = !Array.isArray(value) ? TextUtils.splitByComma(value) : value;
      return intersection(
        splittedValue,
        multiselectItemsOptions.map(item => item.value)
      ).sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' }));
    }
  };

  const handleDialogs = (dialog, value) => {
    nationalSystemsFieldDispatch({ type: 'HANDLE_DIALOGS', payload: { dialog, value } });
  };

  const onAttachFile = async value => {
    onFillField(field, field.fieldSchemaId, `${value.files[0].name}`);
    handleDialogs('uploadFile', false);
  };

  const onConfirmDeleteAttachment = async () => {
    const isFileDeleted = await DatasetService.deleteFileData(datasetId, field.fieldId);

    if (isFileDeleted) {
      onFillField(field, field.fieldSchemaId, '');
      handleDialogs('deleteAttachment', false);
    }
  };

  const onEditorKeyChange = (event, field, option) => {
    if (event.key === 'Enter') onEditorSubmitValue(field, option, event.target.value);

    if (event.key === 'Tab') onEditorSubmitValue(field, option, event.target.value);
  };

  const onEditorSubmitValue = async (field, option, value) => {
    const parsedValue =
      field.fieldType === 'MULTISELECT_CODELIST' || (field.fieldType === 'LINK' && Array.isArray(value))
        ? value.join(',')
        : value;

    try {
      await DatasetService.updateFieldById(datasetId, option, field.fieldId, field.fieldType, parsedValue);
    } catch (error) {
      console.error('error', error);
    }
  };

  const onFileDownload = async (fileName, fieldId) => {
    const fileContent = await DatasetService.downloadFileData(datasetId, fieldId);

    DownloadFile(fileContent, fileName);
  };

  const onFillField = (field, option, value) => {
    nationalSystemsFieldDispatch({ type: 'ON_FILL_FIELD', payload: { field, option, value } });
  };

  const onFormatDate = (date, isInvalidDate) => {
    if (isInvalidDate) return '';

    let d = new Date(date),
      month = '' + (d.getMonth() + 1),
      day = '' + d.getDate(),
      year = d.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('-');
  };

  const renderTemplate = () => {
    const { fieldSchemaId, type } = field;

    switch (type) {
      case 'EMAIL':
      case 'NUMBER_DECIMAL':
      case 'NUMBER_INTEGER':
      case 'PHONE':
      case 'RICH_TEXT':
      case 'TEXT':
      case 'URL':
        return (
          <InputText
            id={field.fieldId}
            keyfilter={RecordUtils.getFilter(type)}
            maxLength={getInputMaxLength[type]}
            onBlur={event => onEditorSubmitValue(field, fieldSchemaId, event.target.value)}
            onChange={event => onFillField(field, fieldSchemaId, event.target.value)}
            onKeyDown={event => onEditorKeyChange(event, field, fieldSchemaId)}
            value={field.value}
          />
        );

      case 'TEXTAREA':
        return (
          <InputTextarea
            className={field.required ? styles.required : undefined}
            collapsedHeight={150}
            id={field.fieldId}
            maxLength={getInputMaxLength[type]}
            onBlur={event => onEditorSubmitValue(field, fieldSchemaId, event.target.value)}
            onChange={event => onFillField(field, fieldSchemaId, event.target.value)}
            onKeyDown={event => onEditorKeyChange(event, field, fieldSchemaId)}
            value={field.value}
          />
        );

      case 'CODELIST':
        return (
          <Dropdown
            appendTo={document.body}
            className={styles.dropdown}
            id={field.fieldId}
            onChange={event => {
              onFillField(field, fieldSchemaId, event.target.value);
              onEditorSubmitValue(field, fieldSchemaId, event.target.value);
            }}
            options={field.codelistItems.map(codelist => ({ label: codelist, value: codelist }))}
            showFilterClear={true}
            value={field.value}
          />
        );

      case 'DATE':
        return (
          <Calendar
            appendTo={document.body}
            dateFormat="yy-mm-dd"
            id={field.fieldId}
            monthNavigator={true}
            onChange={event => {
              onFillField(field, fieldSchemaId, onFormatDate(event.target.value, isNil(event.target.value)));
              onEditorSubmitValue(field, fieldSchemaId, onFormatDate(event.target.value, isNil(event.target.value)));
            }}
            value={new Date(field.value)}
            yearNavigator={true}
            yearRange="2010:2030"
          />
        );

      case 'MULTISELECT_CODELIST':
        return (
          <MultiSelect
            addSpaceCommaSeparator={true}
            appendTo={document.body}
            id={field.fieldId}
            maxSelectedLabels={10}
            onChange={event => {
              onFillField(field, fieldSchemaId, event.target.value);
              onEditorSubmitValue(field, fieldSchemaId, event.target.value);
            }}
            options={field.codelistItems.map(codelist => ({ label: codelist, value: codelist }))}
            value={getMultiselectValues(
              field.codelistItems.map(codelist => ({ label: codelist, value: codelist })),
              field.value
            )}
          />
        );

      case 'ATTACHMENT':
        // const colSchema = columnsSchema.filter(colSchema => colSchema.fieldSchemaId === field.fieldSchemaId)[0];

        return (
          <div className={styles.attachmentWrapper}>
            {!isNil(field.value) && field.value !== '' && (
              <Button
                className={`${field.value === '' && 'p-button-animated-blink'} p-button-primary-transparent`}
                icon={'export'}
                iconPos={'right'}
                label={field.value}
                onClick={() => onFileDownload(field.value, field.fieldId)}
              />
            )}
            {
              <Button
                className={`p-button-animated-blink p-button-primary-transparent`}
                icon={'import'}
                label={
                  resources.messages[
                    !isNil(field.value) && field.value !== '' ? 'uploadReplaceAttachment' : 'uploadAttachment'
                  ]
                }
                onClick={() => {
                  handleDialogs('uploadFile', true);
                  // onFileUploadVisible(
                  //   field.fieldId,
                  //   field.fieldSchemaId,
                  //   !isNil(colSchema) ? colSchema.validExtensions : [],
                  //   !isNil(colSchema) ? colSchema.maxSize : 20
                  // );
                }}
              />
            }

            <Button
              className={`p-button-animated-blink p-button-primary-transparent`}
              icon={'trash'}
              onClick={() => {
                // onFileDeleteVisible(field.fieldId, field.fieldSchemaId);
                handleDialogs('deleteAttachment', true);
              }}
            />
          </div>
        );

      default:
        break;
    }
  };

  const renderValidations = validations =>
    validations &&
    uniqBy(validations, element => [element.message, element.errorLevel].join()).map((validation, index) => (
      <IconTooltip
        className={`webform-validationErrors ${styles.validation}`}
        key={index}
        levelError={validation.levelError}
        message={validation.message}
      />
    ));

  return (
    <div className={styles.content}>
      <div className={styles.titleWrapper}>
        {title && (
          <span className={styles.sectionTitle}>
            <h4 className={styles.title}>
              {!isNil(title?.value) ? title.value : title}
              <span className={styles.requiredMark}>{field.required ? '*' : ''}</span>
              {!isNil(tooltip) && (
                <Button
                  className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
                  icon={'infoCircle'}
                  tooltip={tooltip.value || tooltip}
                  tooltipOptions={{ position: 'top' }}
                />
              )}
            </h4>
          </span>
        )}
        {renderValidations(recordValidations)}
      </div>
      <div className={styles.fieldWrapper}>
        <div className={styles.template}>{renderTemplate()}</div>
        {renderValidations(field.validations)}
      </div>
      {isDialogVisible.uploadFile && (
        <CustomFileUpload
          accept={getAttachExtensions || '*'}
          chooseLabel={resources.messages['selectFile']}
          className={styles.fileUpload}
          dialogClassName={styles.dialog}
          dialogHeader={resources.messages['uploadAttachment']}
          dialogOnHide={() => handleDialogs('uploadFile', false)}
          dialogVisible={isDialogVisible.uploadFile}
          fileLimit={1}
          invalidExtensionMessage={resources.messages['invalidExtensionFile']}
          isDialog={true}
          maxFileSize={
            !isNil(field.maxSize) && field.maxSize.toString() !== '0' ? field.maxSize * 1000 * 1024 : 20 * 1000 * 1024
          }
          mode={'advanced'}
          multiple={false}
          name={'file'}
          onUpload={onAttachFile}
          operation={'PUT'}
          url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.addAttachment, {
            datasetId,
            fieldId: field.fieldId
          })}`}
        />
      )}
      {isDialogVisible.deleteAttachment && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resources.messages['deleteAttachmentHeader']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={onConfirmDeleteAttachment}
          onHide={() => handleDialogs('deleteAttachment', false)}
          visible={isDialogVisible.deleteAttachment}>
          {resources.messages['deleteAttachmentConfirm']}
        </ConfirmDialog>
      )}
    </div>
  );
};
