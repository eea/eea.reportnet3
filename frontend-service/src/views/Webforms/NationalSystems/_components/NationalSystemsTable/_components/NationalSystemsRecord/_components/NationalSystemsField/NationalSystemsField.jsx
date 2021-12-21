import { useContext, useEffect, useReducer } from 'react';

import intersection from 'lodash/intersection';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import uniqBy from 'lodash/uniqBy';
import uniqueId from 'lodash/uniqueId';

import { config } from 'conf';
import { DatasetConfig } from 'repositories/config/DatasetConfig';

import styles from './NationalSystemsField.module.scss';

import { Button } from 'views/_components/Button';
import { Calendar } from 'views/_components/Calendar';
import { CharacterCounter } from 'views/_components/CharacterCounter';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { CustomFileUpload } from 'views/_components/CustomFileUpload';
import { DownloadFile } from 'views/_components/DownloadFile';
import { Dropdown } from 'views/_components/Dropdown';
import { IconTooltip } from 'views/_components/IconTooltip';
import { InputText } from 'views/_components/InputText';
import { InputTextarea } from 'views/_components/InputTextarea';
import { MultiSelect } from 'views/_components/MultiSelect';

import { DatasetService } from 'services/DatasetService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { nationalSystemsFieldReducer } from './_functions/Reducers/nationalSystemsFieldReducer';

import { RecordUtils } from 'views/_functions/Utils';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

export const NationalSystemsField = ({
  dataProviderId,
  dataflowId,
  datasetId,
  getTableErrors,
  nationalField,
  recordValidations,
  title,
  tooltip
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [nationalSystemsFieldState, nationalSystemsFieldDispatch] = useReducer(nationalSystemsFieldReducer, {
    field: nationalField,
    isDialogVisible: { deleteAttachment: false, uploadFile: false }
  });

  const { field, isDialogVisible } = nationalSystemsFieldState;

  useEffect(() => {
    getTableErrors(!isEmpty(recordValidations) || !isEmpty(field.validations));
  }, []);

  const getAttachExtensions = [{ fileExtension: field.validExtensions || [] }]
    .map(file => file.fileExtension.map(extension => (extension.indexOf('.') > -1 ? extension : `.${extension}`)))
    .flat()
    .join(', ');

  const infoAttachTooltip = `${resourcesContext.messages['supportedFileAttachmentsTooltip']} ${
    getAttachExtensions || '*'
  }
  ${resourcesContext.messages['supportedFileAttachmentsMaxSizeTooltip']} ${
    !isNil(field.maxSize) && field.maxSize.toString() !== '0'
      ? `${field.maxSize} ${resourcesContext.messages['MB']}`
      : resourcesContext.messages['maxSizeNotDefined']
  }`;

  const getMultiselectValues = (multiselectItemsOptions, value) => {
    if (!isUndefined(value) && !isUndefined(value[0]) && !isUndefined(multiselectItemsOptions)) {
      const splittedValue = !Array.isArray(value) ? TextUtils.splitByChar(value, ';') : value;
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
    try {
      await DatasetService.deleteAttachment(dataflowId, datasetId, field.fieldId, dataProviderId);
      onFillField(field, field.fieldSchemaId, '');
      handleDialogs('deleteAttachment', false);
    } catch (error) {
      console.error('NationalSystemsField - onConfirmDeleteAttachment.', error);
    }
  };

  const onEditorKeyChange = (event, field, option) => {
    if (event.key === 'Enter' || event.key === 'Tab') onEditorSubmitValue(field, option, event.target.value);
  };

  const onEditorSubmitValue = async (field, option, value) => {
    const parsedValue =
      field.fieldType === 'MULTISELECT_CODELIST' ||
      ((field.fieldType === 'LINK' || field.fieldType === 'EXTERNAL_LINK') && Array.isArray(value))
        ? value.join(';')
        : value;

    try {
      await DatasetService.updateField(datasetId, option, field.fieldId, field.fieldType, parsedValue);
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        console.error('NationalSystemsField - onEditorSubmitValue.', error);
        notificationContext.add({ type: 'UPDATE_WEBFORM_FIELD_BY_ID_ERROR' }, true);
      }
    }
  };

  const onFileDownload = async (fileName, fieldId) => {
    try {
      const { data } = await DatasetService.downloadFileData(dataflowId, datasetId, fieldId, dataProviderId);
      DownloadFile(data, fileName);
    } catch (error) {
      console.error('NationalSystemsField - onFileDownload.', error);
    }
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

  const onUploadFileError = async ({ xhr }) => {
    if (xhr.status === 400) {
      notificationContext.add({ type: 'UPLOAD_FILE_ERROR' }, true);
    }
    if (xhr.status === 423) {
      notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
    }
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
            hasMaxCharCounter
            id={field.fieldId}
            keyfilter={RecordUtils.getFilter(type)}
            onBlur={event => onEditorSubmitValue(field, fieldSchemaId, event.target.value)}
            onChange={event => onFillField(field, fieldSchemaId, event.target.value)}
            onKeyDown={event => onEditorKeyChange(event, field, fieldSchemaId)}
            value={field.value}
          />
        );
      case 'TEXTAREA':
        return (
          <div>
            <InputTextarea
              className={field.required ? styles.required : undefined}
              collapsedHeight={150}
              hasMaxCharCounter
              id={field.fieldId}
              onBlur={event => onEditorSubmitValue(field, fieldSchemaId, event.target.value)}
              onChange={event => onFillField(field, fieldSchemaId, event.target.value)}
              onKeyDown={event => onEditorKeyChange(event, field, fieldSchemaId)}
              value={field.value}
            />
            <CharacterCounter currentLength={field.value.length} style={{ position: 'relative', top: '0.25rem' }} />
          </div>
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
            onBlur={event =>
              onEditorSubmitValue(field, fieldSchemaId, onFormatDate(event.target.value, isNil(event.target.value)))
            }
            onChange={event =>
              onFillField(field, fieldSchemaId, onFormatDate(event.target.value, isNil(event.target.value)))
            }
            value={new Date(field.value)}
            yearNavigator={true}
            yearRange="1900:2100"
          />
        );
      case 'DATETIME':
        return (
          <Calendar
            appendTo={document.body}
            dateFormat="yy-mm-dd"
            id={field.fieldId}
            monthNavigator={true}
            onBlur={event => onEditorSubmitValue(field, fieldSchemaId, event.target.value)}
            onChange={event => onFillField(field, fieldSchemaId, event.target.value)}
            showSeconds={true}
            showTime={true}
            value={field.value}
            yearNavigator={true}
            yearRange="1900:2100"
          />
        );
      case 'MULTISELECT_CODELIST':
        return (
          <MultiSelect
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
            valuesSeparator=";"
          />
        );
      case 'ATTACHMENT':
        return (
          <div className={styles.attachmentWrapper}>
            {!isNil(field.value) && field.value !== '' && (
              <Button
                className={`${field.value === '' && 'p-button-animated-blink'} p-button-primary-transparent`}
                icon="export"
                iconPos={'right'}
                label={field.value}
                onClick={() => onFileDownload(field.value, field.fieldId)}
              />
            )}
            {
              <Button
                className={`p-button-animated-blink p-button-primary-transparent`}
                icon="import"
                label={
                  resourcesContext.messages[
                    !isNil(field.value) && field.value !== '' ? 'uploadReplaceAttachment' : 'uploadAttachment'
                  ]
                }
                onClick={() => {
                  handleDialogs('uploadFile', true);
                }}
              />
            }

            <Button
              className={`p-button-animated-blink p-button-primary-transparent`}
              icon="trash"
              onClick={() => {
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
    uniqBy(validations, element => [element.message, element.errorLevel].join()).map(validation => (
      <IconTooltip
        className={`webform-validationErrors ${styles.validation}`}
        key={uniqueId()}
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
                  icon="infoCircle"
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
          chooseLabel={resourcesContext.messages['selectFile']}
          className={styles.fileUpload}
          dialogClassName={styles.dialog}
          dialogHeader={resourcesContext.messages['uploadAttachment']}
          dialogOnHide={() => handleDialogs('uploadFile', false)}
          dialogVisible={isDialogVisible.uploadFile}
          infoTooltip={infoAttachTooltip}
          invalidExtensionMessage={resourcesContext.messages['invalidExtensionFile']}
          isDialog={true}
          maxFileSize={
            !isNil(field.maxSize) && field.maxSize.toString() !== '0'
              ? field.maxSize * config.MB_SIZE
              : config.MAX_ATTACHMENT_SIZE
          }
          mode={'advanced'}
          name={'file'}
          onError={onUploadFileError}
          onUpload={onAttachFile}
          operation={'PUT'}
          url={`${window.env.REACT_APP_BACKEND}${
            isNil(dataProviderId)
              ? getUrl(DatasetConfig.uploadAttachment, {
                  dataflowId,
                  datasetId,
                  fieldId: field.fieldId
                })
              : getUrl(DatasetConfig.uploadAttachmentWithProviderId, {
                  dataflowId,
                  datasetId,
                  fieldId: field.fieldId,
                  providerId: dataProviderId
                })
          }`}
        />
      )}
      {isDialogVisible.deleteAttachment && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resourcesContext.messages['deleteAttachmentHeader']}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onConfirmDeleteAttachment}
          onHide={() => handleDialogs('deleteAttachment', false)}
          visible={isDialogVisible.deleteAttachment}>
          {resourcesContext.messages['deleteAttachmentConfirm']}
        </ConfirmDialog>
      )}
    </div>
  );
};
