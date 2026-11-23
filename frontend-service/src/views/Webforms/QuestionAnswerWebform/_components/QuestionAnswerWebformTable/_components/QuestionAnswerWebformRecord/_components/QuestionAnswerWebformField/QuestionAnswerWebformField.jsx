import { useContext, useEffect, useReducer, useRef, useState } from 'react';

import intersection from 'lodash/intersection';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import uniqBy from 'lodash/uniqBy';
import uniqueId from 'lodash/uniqueId';

import { config } from 'conf';
import { DatasetConfig } from 'repositories/config/DatasetConfig';

import styles from './QuestionAnswerWebformField.module.scss';

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

import { qaWebformFieldReducer } from './_functions/Reducers/qaWebformFieldReducer';

import { RecordUtils } from 'views/_functions/Utils';
import { WebformsUtils } from 'views/Webforms/_functions/Utils/WebformsUtils';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

export const QuestionAnswerWebformField = ({
  bigData = false,
  dataProviderId,
  dataflowId,
  datasetId,
  getTableErrors,
  nationalField,
  preparationCode,
  recordId,
  recordValidations,
  tableSchemaId,
  tableSchemaName,
  title,
  tooltip,
  updatingFieldStore = null
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [questionAnswerWebformFieldState, questionAnswerWebformFieldDispatch] = useReducer(qaWebformFieldReducer, {
    field: nationalField,
    isDialogVisible: { isDeletingAttachment: false, deleteAttachment: false, uploadFile: false }
  });

  const { field, isDialogVisible } = questionAnswerWebformFieldState;

  // Big data only: the save running in this form (shared by all its fields).
  const [updatingField, setUpdatingField] = useState(
    updatingFieldStore?.getState() ?? { field: null, isUpdating: false }
  );
  // Big data only: value at focus or after the last save, so an unchanged value is not sent again.
  const initialFieldValueRef = useRef(null);
  // Big data only: value just sent by Tab/Enter, so the blur that follows does not send it again.
  const sentBeforeBlurRef = useRef(null);
  // Big data only: where Tab should put the cursor once the save it started has finished.
  const tabFocusPlanRef = useRef(null);

  useEffect(() => {
    getTableErrors(!isEmpty(recordValidations) || !isEmpty(field?.validations));
  }, []);

  useEffect(() => {
    if (isNil(updatingFieldStore)) return;
    setUpdatingField(updatingFieldStore.getState());
    return updatingFieldStore.subscribe(setUpdatingField);
  }, [updatingFieldStore]);

  // Big data: while a save runs, text fields are read-only and the other controls disabled.
  const isFormLocked = () => bigData && updatingField.isUpdating;

  const isFieldBeingSaved = () =>
    isFormLocked() &&
    updatingField.field?.recordId === recordId &&
    updatingField.field?.fieldSchemaId === field?.fieldSchemaId;

  const onFocusField = value => {
    sentBeforeBlurRef.current = null;
    initialFieldValueRef.current = value;
  };

  const rememberSentBeforeBlur = value => {
    if (bigData) sentBeforeBlurRef.current = value;
  };

  // True when the blur would send exactly what Tab/Enter has just sent.
  const isAlreadySentBeforeBlur = value => {
    const isAlreadySent = bigData && !isNil(sentBeforeBlurRef.current) && sentBeforeBlurRef.current === value;
    sentBeforeBlurRef.current = null;
    return isAlreadySent;
  };

  // True when the value has not changed since focus or the last save (big data only), so nothing is sent.
  const isUnchanged = value => bigData && value === initialFieldValueRef.current;

  const getAttachExtensions = [
    { fileExtension: isNil(field) || isNil(field.validExtensions) ? [] : field.validExtensions }
  ]
    .map(file => file.fileExtension.map(extension => (extension.indexOf('.') > -1 ? extension : `.${extension}`)))
    .flat()
    .join(', ');

  const infoAttachTooltip = `${resourcesContext.messages['supportedFileAttachmentsTooltip']} ${
    getAttachExtensions || '*'
  }
  ${resourcesContext.messages['supportedFileAttachmentsMaxSizeTooltip']} ${
    !isNil(field) && !isNil(field.maxSize) && field.maxSize.toString() !== '0'
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
    questionAnswerWebformFieldDispatch({ type: 'HANDLE_DIALOGS', payload: { dialog, value } });
  };

  const onAttachFile = async value => {
    onFillField(field, field.fieldSchemaId, `${value.files[0].name}`);
    handleDialogs('uploadFile', false);
  };

  const onConfirmDeleteAttachment = async () => {
    try {
      handleDialogs('isDeletingAttachment', true);
      await DatasetService.deleteAttachment({
        dataflowId,
        datasetId,
        fieldId: field.fieldId || field.fieldSchemaId,
        dataProviderId,
        tableSchemaName: tableSchemaName,
        fieldName: field.name,
        fileName: field.value,
        recordId
      });
      onFillField(field, field.fieldSchemaId, '');
      handleDialogs('deleteAttachment', false);
      handleDialogs('isDeletingAttachment', false);
    } catch (error) {
      handleDialogs('isDeletingAttachment', false);
      console.error('QuestionAnswerWebformField - onConfirmDeleteAttachment.', error);
    }
  };

  const onEditorKeyChange = (event, field, option) => {
    if (isFormLocked()) return;
    if (event.key === 'Enter' || event.key === 'Tab') {
      if (isUnchanged(event.target.value)) return;
      rememberSentBeforeBlur(event.target.value);
      const tabFocusPlan =
        bigData && event.key === 'Tab' ? WebformsUtils.createTabFocusPlan(event.target, event.shiftKey) : null;
      tabFocusPlanRef.current = tabFocusPlan;
      onEditorSubmitValue(field, option, event.target.value);
      // Big data: when this Tab starts a save, the cursor stays here until it ends, then moves on.
      if (tabFocusPlan?.isSaveSent) event.preventDefault();
    }
  };

  const onEditorSubmitValue = async (field, option, value) => {
    const parsedValue =
      field.fieldType === 'MULTISELECT_CODELIST' ||
      ((field.fieldType === 'LINK' || field.fieldType === 'EXTERNAL_LINK') && Array.isArray(value))
        ? value.join(';')
        : value;

    const tabFocusPlan = tabFocusPlanRef.current;
    tabFocusPlanRef.current = null;
    // Big data: this save holds the lock; only this save can release it.
    const savingField = { fieldSchemaId: field.fieldSchemaId, recordId };
    let isSaveSent = false;

    try {
      bigData && updatingFieldStore?.lock(savingField);
      isSaveSent = true;
      if (tabFocusPlan) tabFocusPlan.isSaveSent = true;
      await DatasetService.updateField(
        datasetId,
        option,
        field.fieldId,
        field.fieldType,
        parsedValue,
        field.name,
        recordId,
        tableSchemaId
      );
      // Big data: the cursor can stay in the field after a save, so the saved value is the new "unchanged" baseline.
      if (bigData) initialFieldValueRef.current = parsedValue;
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        console.error('QuestionAnswerWebformField - onEditorSubmitValue.', error);
        notificationContext.add({ type: 'UPDATE_WEBFORM_FIELD_BY_ID_ERROR' }, true);
      }
    } finally {
      bigData && updatingFieldStore?.unlock(savingField);
      if (isSaveSent) WebformsUtils.applyTabFocusPlan(tabFocusPlan);
      else WebformsUtils.cancelTabFocusPlan(tabFocusPlan);
    }
  };

  const onFileDownload = async (fileName, fieldId) => {
    try {
      const { data } = await DatasetService.downloadFileData({
        dataflowId,
        datasetId,
        fieldId,
        dataProviderId,
        fileName,
        recordId,
        tableSchemaName: tableSchemaName,
        fieldName: field.name
      });
      DownloadFile(data, fileName);
    } catch (error) {
      console.error('QuestionAnswerWebformField - onFileDownload.', error);
    }
  };

  const onFillField = (field, option, value) => {
    questionAnswerWebformFieldDispatch({ type: 'ON_FILL_FIELD', payload: { field, option, value } });
  };

  const onFormatDate = (date, isInvalidDate) => {
    if (isInvalidDate) {
      return '';
    }

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
    if (isNil(field)) {
      return null;
    }

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
            className={isFormLocked() ? 'p-disabled' : undefined}
            hasMaxCharCounter
            id={field.fieldId}
            isLoadingData={isFieldBeingSaved()}
            keyfilter={RecordUtils.getFilter(type)}
            onBlur={event => {
              if (!isAlreadySentBeforeBlur(event.target.value) && !isUnchanged(event.target.value) && !isFormLocked())
                onEditorSubmitValue(field, fieldSchemaId, event.target.value);
            }}
            onChange={event => onFillField(field, fieldSchemaId, event.target.value)}
            onFocus={event => onFocusField(event.target.value)}
            onKeyDown={event => onEditorKeyChange(event, field, fieldSchemaId)}
            readOnly={isFormLocked()}
            value={field.value}
          />
        );
      case 'TEXTAREA':
        return (
          <div>
            <InputTextarea
              className={
                [field.required ? styles.required : undefined, isFormLocked() ? 'p-disabled' : undefined]
                  .filter(Boolean)
                  .join(' ') || undefined
              }
              collapsedHeight={150}
              id={field.fieldId}
              isLoadingData={isFieldBeingSaved()}
              onBlur={event => {
                if (!isAlreadySentBeforeBlur(event.target.value) && !isUnchanged(event.target.value) && !isFormLocked())
                  onEditorSubmitValue(field, fieldSchemaId, event.target.value);
              }}
              onChange={event => onFillField(field, fieldSchemaId, event.target.value)}
              onFocus={event => onFocusField(event.target.value)}
              onKeyDown={event => {
                // Big data: Enter only adds a line break, as in the other webform types; the text is saved on Tab or blur.
                if (bigData && event.key === 'Enter') return;
                onEditorKeyChange(event, field, fieldSchemaId);
              }}
              readOnly={isFormLocked()}
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
            disabled={isFormLocked()}
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
            disabled={isFormLocked()}
            id={field.fieldId}
            monthNavigator={true}
            onBlur={event =>
              onEditorSubmitValue(field, fieldSchemaId, onFormatDate(event.target.value, isNil(event.target.value)))
            }
            onChange={event =>
              onFillField(field, fieldSchemaId, onFormatDate(event.target.value, isNil(event.target.value)))
            }
            selectableYears={100}
            value={new Date(field.value)}
            yearNavigator={true}
          />
        );
      case 'DATETIME':
        return (
          <Calendar
            appendTo={document.body}
            dateFormat="yy-mm-dd"
            disabled={isFormLocked()}
            id={field.fieldId}
            monthNavigator={true}
            onBlur={event => onEditorSubmitValue(field, fieldSchemaId, event.target.value)}
            onChange={event => onFillField(field, fieldSchemaId, event.target.value)}
            selectableYears={100}
            showSeconds={true}
            showTime={true}
            value={field.value}
            yearNavigator={true}
          />
        );
      case 'MULTISELECT_CODELIST':
        return (
          <MultiSelect
            appendTo={document.body}
            disabled={isFormLocked()}
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
                onClick={() => onFileDownload(field.value, field.fieldId || field.fieldSchemaId)}
              />
            )}
            {
              <Button
                className="p-button-animated-blink p-button-primary-transparent"
                disabled={isFormLocked()}
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
              className="p-button-animated-blink p-button-primary-transparent"
              disabled={isFormLocked()}
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
              <span className={styles.requiredMark}>{field?.required ? '*' : ''}</span>
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
        {renderValidations(field?.validations)}
      </div>
      {isDialogVisible.uploadFile && (
        <CustomFileUpload
          accept={getAttachExtensions || '*'}
          chooseLabel={resourcesContext.messages['selectFile']}
          className={styles.fileUpload}
          dialogHeader={resourcesContext.messages['uploadAttachment']}
          dialogOnHide={() => handleDialogs('uploadFile', false)}
          dialogVisible={isDialogVisible.uploadFile}
          infoTooltip={infoAttachTooltip}
          invalidExtensionMessage={resourcesContext.messages['invalidExtensionFile']}
          isDialog={true}
          maxFileSize={
            !isNil(field) && !isNil(field.maxSize) && field.maxSize.toString() !== '0'
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
                  fieldId: field.fieldId || field.fieldSchemaId,
                  tableSchemaName: tableSchemaName,
                  fieldName: field.name,
                  recordId,
                  previousFileName: field.value,
                  preparationCode
                })
              : getUrl(DatasetConfig.uploadAttachmentWithProviderId, {
                  dataflowId,
                  datasetId,
                  fieldId: field.fieldId || field.fieldSchemaId,
                  tableSchemaName: tableSchemaName,
                  fieldName: field.name,
                  recordId,
                  previousFileName: field.value,
                  providerId: dataProviderId,
                  preparationCode
                })
          }`}
        />
      )}
      {isDialogVisible.deleteAttachment && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resourcesContext.messages['deleteAttachmentHeader']}
          iconConfirm={isDialogVisible.isDeletingAttachment && 'spinnerAnimate'}
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
