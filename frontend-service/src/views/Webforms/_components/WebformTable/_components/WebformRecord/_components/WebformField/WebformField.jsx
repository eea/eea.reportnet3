import { Fragment, useContext, useEffect, useReducer, useRef, useCallback } from 'react';
import { useQueryClient } from 'react-query';

import isNil from 'lodash/isNil';

import { config } from 'conf';
import { DatasetConfig } from 'repositories/config/DatasetConfig';

import styles from './WebformField.module.scss';

import { Button } from 'views/_components/Button';
import { Calendar } from 'views/_components/Calendar';
import { CharacterCounter } from 'views/_components/CharacterCounter';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { CustomFileUpload } from 'views/_components/CustomFileUpload';
import { DownloadFile } from 'views/_components/DownloadFile';
import { InputText } from 'views/_components/InputText';
import { InputTextarea } from 'views/_components/InputTextarea';
import DropdownWebform from 'views/_components/Dropdown/DropdownWebform';
import MultiSelectWebform from 'views/_components/MultiSelect/MultiSelectWebform';

import { DatasetService } from 'services/DatasetService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { webformFieldReducer } from './_functions/Reducers/webformFieldReducer';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { PaMsUtils } from './_functions/Utils/PaMsUtils';
import { RecordUtils } from 'views/_functions/Utils';
import { WebformRecordUtils } from 'views/Webforms/_components/WebformTable/_components/WebformRecord/_functions/Utils/WebformRecordUtils';

import { TextUtils } from 'repositories/_utils/TextUtils';
import { isEmpty } from 'lodash';

export const WebformField = ({
  bigData = false,
  columnsSchema,
  dataProviderId,
  dataflowId,
  datasetId,
  datasetSchemaId,
  element,
  hasErrors,
  isConditional,
  isConditionalChanged,
  isSubTableCreated,
  newRecord,
  onFillField,
  onSaveField,
  record,
  referencedTableSchemaId,
  rootPkFieldId,
  tableSchemaId,
  webformType
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const queryClient = useQueryClient();

  const inputRef = useRef(null);

  const [webformFieldState, webformFieldDispatch] = useReducer(webformFieldReducer, {
    initialFieldValue: '',
    isDeleteAttachmentVisible: false,
    isDeleteRowVisible: false,
    isDeletingRow: false,
    isDialogVisible: { deleteRow: false, uploadFile: false },
    isFileDialogVisible: false,
    isLoadingData: false,
    isSubmiting: false,
    linkItemsOptions: [],
    record: record,
    sectorAffectedValue: null,
    selectedFieldId: '',
    selectedFieldSchemaId: '',
    selectedMaxSize: '',
    selectedRecordId: '',
    selectedFieldName: '',
    selectedFileName: ''
  });

  const {
    initialFieldValue,
    isDeleteAttachmentVisible,
    isFileDialogVisible,
    isLoadingData,
    isSubmiting,
    linkItemsOptions,
    sectorAffectedValue,
    selectedFieldId,
    selectedFieldSchemaId,
    selectedRecordId,
    selectedFieldName,
    selectedFileName
  } = webformFieldState;

  const { formatDate, formatDateTime, getMultiselectValues } = WebformRecordUtils;

  const { getObjectiveOptions } = PaMsUtils;

  useEffect(() => {
    if (element.fieldType === 'LINK' || element.fieldType === 'EXTERNAL_LINK') onFilter('', element);
  }, [newRecord, isConditionalChanged]);


  const onAttach = async value => {
    onFillField(record, selectedFieldSchemaId, `${value.files[0].name}`);
    onToggleDialogVisible(false);
  };

  const onConfirmDeleteAttachment = async () => {
    try {
      await DatasetService.deleteAttachment({
        dataflowId,
        datasetId,
        fieldId: selectedFieldId,
        dataProviderId,
        tableSchemaName: undefined,
        fieldName: selectedFieldName,
        fileName: selectedFileName,
        recordId: selectedRecordId
      });
      onFillField(record, selectedFieldSchemaId, '');
      onToggleDeleteAttachmentDialogVisible(false);
    } catch (error) {
      console.error('WebformField - onConfirmDeleteAttachment.', error);
    }
  };

  const onFileDownload = async (fileName, fieldId, recordId, fieldName) => {
    try {
      const { data } = await DatasetService.downloadFileData({
        dataflowId,
        datasetId,
        fieldId,
        providerId: dataProviderId,
        fileName,
        recordId,
        tableSchemaName: undefined,
        fieldName
      });
      DownloadFile(data, fileName);
    } catch (error) {
      console.error('WebformField - onFileDownload.', error);
    }
  };

  const onFilter = useCallback(
    async (filter, element) => {
      if (isNil(element) || isNil(element.referencedField)) {
        return;
      }

      let localDatasetSchemaId = datasetSchemaId;

      if (localDatasetSchemaId === '' || isNil(localDatasetSchemaId)) {
        try {
          const metadata = await DatasetService.getMetadata(datasetId);
          localDatasetSchemaId = metadata.datasetSchemaId;
        } catch (error) {
          console.error('Failed to fetch dataset schema ID:', error);
          // Handle error (e.g., setting an error state or showing a notification)
          return; // Exit if unable to fetch the metadata
        }
      }

      const conditionalField = record.elements.find(
        el => el.fieldSchemaId === element.referencedField.masterConditionalFieldId
      );
      queryClient
        .fetchQuery(
          ['referencedFieldValues', datasetSchemaId, conditionalField, element, filter],
          async () => {
            const referencedFieldValues = await DatasetService.getReferencedFieldValues(
              datasetId,
              element.fieldSchemaId,
              filter,
              !isNil(conditionalField)
                ? conditionalField.type === 'MULTISELECT_CODELIST'
                  ? conditionalField.value?.replace('; ', ';').replace(';', '; ')
                  : conditionalField.value
                : encodeURIComponent(element.value),
              localDatasetSchemaId,
              400
            );
            return referencedFieldValues
              .map(referencedField => ({
                itemType:
                  !isNil(referencedField.label) &&
                  referencedField.label !== '' &&
                  referencedField.label !== referencedField.value
                    ? `${referencedField.label}`
                    : referencedField.value,
                value: referencedField.value
              }))
              .sort((a, b) => a.value.localeCompare(b.value));
          },
          {
            staleTime: 5 * 60 * 1000 // Example stale time
          }
        )
        .then(linkItems => {
          webformFieldDispatch({ type: 'SET_LINK_ITEMS', payload: linkItems });
        })
        .catch(error => {
          console.error('WebformField - onFilter.', error);
          notificationContext.add({ type: 'GET_REFERENCED_LINK_VALUES_ERROR' }, true);
        });
    },
    [
      datasetId,
      element,
      record,
      queryClient,
      datasetSchemaId,
      resourcesContext,
      webformFieldDispatch,
      notificationContext
    ]
  );

  const onFocusField = value => {
    webformFieldDispatch({ type: 'SET_INITIAL_FIELD_VALUE', payload: value });
  };

  const onEditorKeyChange = (event, field, option) => {
    if (event.key === 'Escape') {
    } else if (event.key === 'Enter') {
      onEditorSubmitValue(field, option, event.target.value);
    } else if (event.key === 'Tab') {
      onEditorSubmitValue(field, option, event.target.value);
    }
  };

  const onEditorSubmitValue = async (field, option, value, updateInCascade = false) => {
    let conditionalFields;
    let parsedValues;

    if (isConditional && field.fieldType === 'LINK') {
      conditionalFields = record.elements.map(element =>
        !(element.fieldSchema === option || element.fieldSchemaId === option)
          ? { ...element, value: '' }
          : { ...element, value: value }
      );

      parsedValues = conditionalFields.map(conditionalField =>
        conditionalField.fieldType === 'MULTISELECT_CODELIST' ||
        ((conditionalField.fieldType === 'LINK' || conditionalField.fieldType === 'EXTERNAL_LINK') &&
          Array.isArray(conditionalField.value))
          ? conditionalField.value.join(';')
          : conditionalField.value
      );
    }

    const parsedValue =
      field.fieldType === 'MULTISELECT_CODELIST' ||
      ((field.fieldType === 'LINK' || field.fieldType === 'EXTERNAL_LINK') && Array.isArray(value))
        ? value.join(';')
        : value;

    try {
      if ((!isSubmiting && initialFieldValue !== parsedValue) || parsedValue === '') {
        if (!isNil(conditionalFields) && !isNil(parsedValues)) {
          await DatasetService.updateConditionalFieldsWebform(
            datasetId,
            conditionalFields,
            record.recordId,
            bigData ? (referencedTableSchemaId ? referencedTableSchemaId : tableSchemaId) : tableSchemaId
          );
        } else {
          await DatasetService.updateFieldWebform(
            datasetId,
            field,
            parsedValue,
            bigData ? (referencedTableSchemaId ? referencedTableSchemaId : tableSchemaId) : tableSchemaId
          );
        }
      }
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        if (field?.fieldType !== 'DATETIME') {
          console.error('WebformField - onEditorSubmitValue.', error);
          if (updateInCascade) {
            notificationContext.add({ type: 'UPDATE_WEBFORM_FIELD_IN_CASCADE_BY_ID_ERROR' }, true);
          } else {
            notificationContext.add({ type: 'UPDATE_WEBFORM_FIELD_BY_ID_ERROR' }, true);
          }
        }
      }
    } finally {
      webformFieldDispatch({ type: 'SET_IS_SUBMITING', payload: false });
    }
  };

  const onFileDeleteVisible = (fileName, fieldId, fieldSchemaId) => {
    webformFieldDispatch({ type: 'ON_FILE_DELETE_OPENED', payload: { fileName, fieldId, fieldSchemaId } });
  };

  const onFileUploadVisible = (fieldName, recordId, fieldId, fieldSchemaId, validExtensions, maxSize) => {
    webformFieldDispatch({
      type: 'ON_FILE_UPLOAD_SET_FIELDS',
      payload: { fieldName, recordId, fieldId, fieldSchemaId, validExtensions, maxSize }
    });
  };

  const onToggleDeleteAttachmentDialogVisible = value =>
    webformFieldDispatch({ type: 'ON_TOGGLE_DELETE_DIALOG', payload: { value } });

  const onToggleDialogVisible = value => webformFieldDispatch({ type: 'ON_TOGGLE_DIALOG', payload: { value } });

  const getAttachExtensions = [
    { fileExtension: isNil(element) || isNil(element.validExtensions) ? [] : element.validExtensions }
  ]
    .map(file => file.fileExtension.map(extension => (extension.indexOf('.') > -1 ? extension : `.${extension}`)))
    .flat()
    .join(', ');

  const infoAttachTooltip = `<span style="font-weight: bold">${
    resourcesContext.messages['supportedFileAttachmentsTooltip']
  } </span><span style="color: var(--success-color-lighter); fontWeight: 600">${getAttachExtensions || '*'}</span>
    <span style="font-weight: bold">${
      resourcesContext.messages['supportedFileAttachmentsMaxSizeTooltip']
    } </span><span style="color: var(--success-color-lighter); fontWeight: 600">${
    !isNil(element) && !isNil(element.maxSize) && element.maxSize.toString() !== '0'
      ? `${element.maxSize} ${resourcesContext.messages['MB']}`
      : resourcesContext.messages['maxSizeNotDefined']
  }`;

  const onUploadFileError = async ({ xhr }) => {
    if (xhr.status === 400) {
      notificationContext.add({ type: 'UPLOAD_FILE_ERROR' }, true);
    }
    if (xhr.status === 423) {
      notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
    }
  };

  const changeDatePickerPosition = inputLeftPosition => {
    const datePickerElements = document.getElementsByClassName('p-datepicker');
    for (let index = 0; index < datePickerElements.length; index++) {
      const datePicker = datePickerElements[index];
      datePicker.style.left = `${inputLeftPosition}px`;
    }
  };

  const renderTemplate = (field, option, type) => {
    switch (type) {
      case 'DATE':
        return (
          <Calendar
            appendTo={document.body}
            dateFormat="yy-mm-dd"
            disabled={field?.readOnly}
            id={field.fieldId || field.fieldSchemaId}
            monthNavigator={true}
            onBlur={event => {
              if (isNil(field.recordId)) onSaveField(option, formatDate(event.target.value, isNil(event.target.value)));
              else onEditorSubmitValue(field, option, formatDate(event.target.value, isNil(event.target.value)));
            }}
            onChange={event => onFillField(field, option, formatDate(event.target.value, isNil(event.target.value)))}
            onFocus={event => {
              changeDatePickerPosition(event.target.getBoundingClientRect().left);
              onFocusField(event.target.value);
            }}
            onSelect={event => {
              onFillField(field, option, formatDate(event.value, isNil(event.value)));
              onEditorSubmitValue(field, option, formatDate(event.value, isNil(event.value)));
            }}
            readOnlyInput={true}
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
            disabled={field?.readOnly}
            id={field.fieldId || field.fieldSchemaId}
            monthNavigator={true}
            onBlur={e => {
              if (isNil(field.recordId)) onSaveField(option, formatDate(e.value, isNil(e.value)));
            }}
            onChange={e => {
              onFillField(field, option, formatDateTime(e.value, isNil(e.value)));
            }}
            onSelect={e => {
              onFillField(field, option, formatDateTime(e.value, isNil(e.value)));
              onEditorSubmitValue(field, option, formatDateTime(e.value, isNil(e.value)));
            }}
            readOnlyInput={true}
            selectableYears={100}
            showSeconds={true}
            showTime={true}
            value={!isEmpty(field.value) ? new Date(field.value) : null}
            yearNavigator={true}
          />
        );
      case 'EXTERNAL_LINK':
      case 'LINK':
        if (field.pkHasMultipleValues) {
          return (
            <MultiSelectWebform
              appendTo={document.body}
              clearButton={false}
              currentValue={field.value}
              disabled={field?.readOnly || isLoadingData}
              filter={true}
              filterPlaceholder={resourcesContext.messages['linkFilterPlaceholder']}
              isLoadingData={isLoadingData}
              maxSelectedLabels={10}
              onChange={() => {
                if (isNil(field.recordId)) onSaveField(option, field.value);
                else onEditorSubmitValue(field, option, field.value);
              }}
              onFilterInputChangeBackend={filter => onFilter(filter, field)}
              onUpdate={event => {
                onFillField(field, option, event.target.value, isConditional);
              }}
              optionLabel="itemType"
              options={linkItemsOptions}
              style={hasErrors ? { border: '2px solid #b90202' } : null}
              value={RecordUtils.getMultiselectValues(linkItemsOptions, field.value)}
              valuesSeparator=";"
            />
          );
        } else {
          const selectedValue = RecordUtils.getLinkValue(linkItemsOptions, field.value);
          return (
            <DropdownWebform
              appendTo={document.body}
              currentValue={!isNil(selectedValue) ? selectedValue.value : ''}
              disabled={field?.readOnly || isLoadingData}
              filter={true}
              filterPlaceholder={resourcesContext.messages['linkFilterPlaceholder']}
              isLoadingData={isLoadingData}
              onChange={event => {
                const value =
                  typeof event.target.value === 'object' && !Array.isArray(event.target.value)
                    ? event.target.value.value
                    : event.target.value;
                onFillField(field, option, value, isConditional);
                webformFieldDispatch({ type: 'SET_SECTOR_AFFECTED', payload: { value } });
                if (isNil(field.recordId)) onSaveField(option, value);
                else if (!(event.target.action === 'arrowKeys')) onEditorSubmitValue(field, option, value);
              }}
              onFilterInputChangeBackend={filter => onFilter(filter, field)}
              optionLabel="itemType"
              options={linkItemsOptions}
              showFilterClear={true}
              style={hasErrors ? { border: '2px solid #b90202' } : null}
              value={RecordUtils.getLinkValue(linkItemsOptions, field.value)}
            />
          );
        }
      case 'MULTISELECT_CODELIST':
        return (
          <MultiSelectWebform
            appendTo={document.body}
            disabled={field?.readOnly}
            id={field.fieldId}
            maxSelectedLabels={10}
            onChange={() => {
              if (isNil(field.recordId)) onSaveField(option, field.value);
              else onEditorSubmitValue(field, option, field.value);
            }}
            onUpdate={event => {
              onFillField(field, option, event.target.value);
            }}
            options={
              field.name === 'Objective'
                ? getObjectiveOptions(sectorAffectedValue)
                : field.codelistItems.map(codelist => ({ label: codelist, value: codelist }))
            }
            style={hasErrors ? { border: '2px solid #b90202' } : null}
            value={getMultiselectValues(
              field.codelistItems.map(codelist => ({ label: codelist, value: codelist })),
              field.value
            )}
            valuesSeparator=";"
          />
        );
      case 'CODELIST':
        const codelistOptions = field.codelistItems.map(codelist => ({ itemType: codelist, value: codelist }));
        const selectedValue = RecordUtils.getLinkValue(codelistOptions, field.value);
        return (
          <DropdownWebform
            appendTo={document.body}
            currentValue={!isNil(selectedValue) ? selectedValue.value : ''}
            disabled={field?.readOnly || isLoadingData}
            id={field.fieldId}
            isLoadingData={isLoadingData}
            onChange={event => {
              const value =
                typeof event.target.value === 'object' && !Array.isArray(event.target.value)
                  ? event.target.value.value
                  : event.target.value;
              onFillField(field, option, value, isConditional);
              webformFieldDispatch({ type: 'SET_SECTOR_AFFECTED', payload: { value } });
              if (isNil(field.recordId)) onSaveField(option, value);
              else if (!(event.target.action === 'arrowKeys')) onEditorSubmitValue(field, option, value);
            }}
            onFilterInputChangeBackend={filter => onFilter(filter, field)}
            optionLabel="itemType"
            options={codelistOptions}
            showFilterClear={true}
            singleCodelist={true}
            style={hasErrors ? { border: '2px solid #b90202' } : null}
            value={RecordUtils.getLinkValue(codelistOptions, field.value)}
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
            characterCounterStyles={{ marginBottom: 0 }}
            disabled={
              field?.readOnly ||
              isSubTableCreated ||
              field.fieldSchema === rootPkFieldId ||
              field.fieldSchemaId === rootPkFieldId ||
              field.autoIncrement
            }
            hasErrors={hasErrors}
            hasMaxCharCounter
            id={field.fieldId || field.fieldSchemaId}
            keyfilter={RecordUtils.getFilter(type)}
            onBlur={event => {
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else onEditorSubmitValue(field, option, event.target.value, field.isPrimary || false);
            }}
            onChange={event => onFillField(field, option, event.target.value)}
            onFocus={event => onFocusField(event.target.value)}
            onKeyDown={event => onEditorKeyChange(event, field, option)}
            ref={inputRef}
            value={field.value}
          />
        );
      case 'TEXTAREA':
        return (
          <Fragment>
            <InputTextarea
              className={`${field.required ? styles.required : undefined} ${webformType==='ENTITIES' ? `resizable` : ""}`}
              collapsedHeight={150}
              disabled={field?.readOnly}
              hasErrors={hasErrors}
              id={field.fieldId || field.fieldSchemaId}
              onBlur={event => {
                if (isNil(field.recordId)) onSaveField(option, event.target.value);
                else onEditorSubmitValue(field, option, event.target.value);
              }}
              onChange={event => onFillField(field, option, event.target.value)}
              onFocus={event => onFocusField(event.target.value)}
              onKeyDown={event => onEditorKeyChange(event, field, option)}
              value={field.value}
            />
            <CharacterCounter
              currentLength={field.value.length}
              style={{ position: 'relative', right: '0', top: '0.25rem' }}
            />
          </Fragment>
        );
      case 'EMPTY':
        return (
          <div className={styles.infoButtonWrapper}>
            <Button
              className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
              icon="errorCircle"
            />
            <span
              className={styles.nonExistField}
              dangerouslySetInnerHTML={{
                __html: TextUtils.parseText(resourcesContext.messages['fieldIsNotCreated'], { fieldName: field.name })
              }}
            />
          </div>
        );
      case 'READ_ONLY':
        return (
          <Fragment>
            {field.title}: <strong>{field.value}</strong>
          </Fragment>
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
                onClick={() => onFileDownload(field.value, field.fieldId, field.recordId, field.name)}
              />
            )}
            {
              <Button
                className="p-button-animated-blink p-button-primary-transparent"
                icon="import"
                label={
                  !isNil(field.value) && field.value !== ''
                    ? resourcesContext.messages['uploadReplaceAttachment']
                    : resourcesContext.messages['uploadAttachment']
                }
                onClick={() => {
                  onToggleDialogVisible(true);
                  onFileUploadVisible(
                    field.name,
                    field.recordId,
                    field.fieldId,
                    field.fieldSchemaId,
                    !isNil(colSchema) ? colSchema.validExtensions : [],
                    !isNil(colSchema) ? colSchema.maxSize : 20
                  );
                }}
              />
            }

            <Button
              className="p-button-animated-blink p-button-primary-transparent"
              icon="trash"
              onClick={() => onFileDeleteVisible(field.value, field.fieldId, field.fieldSchemaId)}
            />
          </div>
        );
      default:
        break;
    }
  };

  return (
    <Fragment>
      {renderTemplate(element, element.fieldSchemaId, element.customType ? element.customType : element.fieldType)}
      {isFileDialogVisible && (
        <CustomFileUpload
          accept={getAttachExtensions || '*'}
          chooseLabel={resourcesContext.messages['selectFile']}
          className={styles.fileUpload}
          dialogHeader={resourcesContext.messages['uploadAttachment']}
          dialogOnHide={() => onToggleDialogVisible(false)}
          dialogVisible={isFileDialogVisible}
          infoTooltip={infoAttachTooltip}
          invalidExtensionMessage={resourcesContext.messages['invalidExtensionFile']}
          isDialog={true}
          maxFileSize={
            !isNil(element.maxSize) && element.maxSize.toString() !== '0'
              ? element.maxSize * config.MB_SIZE
              : config.MAX_ATTACHMENT_SIZE
          }
          name="file"
          onError={onUploadFileError}
          onUpload={onAttach}
          operation="PUT"
          url={`${window.env.REACT_APP_BACKEND}${
            isNil(dataProviderId)
              ? getUrl(DatasetConfig.uploadAttachment, {
                  dataflowId,
                  datasetId,
                  fieldId: selectedFieldId,
                  tableSchemaName: undefined,
                  fieldName: selectedFieldName,
                  recordId: selectedRecordId,
                  previousFileName: undefined
                })
              : getUrl(DatasetConfig.uploadAttachmentWithProviderId, {
                  dataflowId,
                  datasetId,
                  fieldId: selectedFieldId,
                  tableSchemaName: undefined,
                  fieldName: selectedFieldName,
                  recordId: selectedRecordId,
                  previousFileName: undefined,
                  providerId: dataProviderId
                })
          }`}
        />
      )}
      {isDeleteAttachmentVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={`${resourcesContext.messages['deleteAttachmentHeader']}`}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onConfirmDeleteAttachment}
          onHide={() => onToggleDeleteAttachmentDialogVisible(false)}
          visible={isDeleteAttachmentVisible}>
          {resourcesContext.messages['deleteAttachmentConfirm']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
