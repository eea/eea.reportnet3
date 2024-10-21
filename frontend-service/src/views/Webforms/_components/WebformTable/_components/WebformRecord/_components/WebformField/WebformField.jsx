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

export const WebformField = ({
  columnsSchema,
  dataProviderId,
  dataflowId,
  datasetId,
  datasetSchemaId,
  element,
  isConditional,
  isConditionalChanged,
  newRecord,
  onFillField,
  onSaveField,
  onUpdateSinglesList,
  onUpdatePamsValue,
  pamsRecords,
  record,
  tableSchemaId
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
    selectedMaxSize: ''
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
    selectedFieldSchemaId
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
      await DatasetService.deleteAttachment(dataflowId, datasetId, selectedFieldId, dataProviderId);
      onFillField(record, selectedFieldSchemaId, '');
      onToggleDeleteAttachmentDialogVisible(false);
    } catch (error) {
      console.error('WebformField - onConfirmDeleteAttachment.', error);
    }
  };

  const onFileDownload = async (fileName, fieldId) => {
    try {
      const { data } = await DatasetService.downloadFileData(dataflowId, datasetId, fieldId, dataProviderId);
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

  // const onEditorSubmitValue = async (field, option, value, updateInCascade = false, updatesGroupInfo = false) => {
  //   webformFieldDispatch({ type: 'SET_IS_SUBMITING', payload: true });
  //   const parsedValue =
  //     field.fieldType === 'MULTISELECT_CODELIST' ||
  //     ((field.fieldType === 'LINK' || field.fieldType === 'EXTERNAL_LINK') && Array.isArray(value))
  //       ? value.join(';')
  //       : value;

  //       try {
  //     if (!isSubmiting && initialFieldValue !== parsedValue) {
  //       await DatasetService.updateField(
  //         datasetId,
  //         option,
  //         field.fieldId,
  //         field.fieldType,
  //         parsedValue,
  //         updateInCascade
  //       );
  //       if (!isNil(onUpdatePamsValue) && (updateInCascade || updatesGroupInfo)) {
  //         onUpdatePamsValue(field.recordId, field.value, field.fieldId, updatesGroupInfo);
  //       }

  //       if (!isNil(onUpdateSinglesList) && field.updatesSingleListData) {
  //         onUpdateSinglesList();
  //       }
  //     }
  //   } catch (error) {
  //     if (error.response.status === 423) {
  //       notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
  //     } else {
  //       if (field.fieldType !== 'DATETIME') {
  //         console.error('WebformField - onEditorSubmitValue.', error);
  //         if (updateInCascade) {
  //           notificationContext.add({ type: 'UPDATE_WEBFORM_FIELD_IN_CASCADE_BY_ID_ERROR' }, true);
  //         } else {
  //           notificationContext.add({ type: 'UPDATE_WEBFORM_FIELD_BY_ID_ERROR' }, true);
  //         }
  //       }
  //     }
  //   } finally {
  //     webformFieldDispatch({ type: 'SET_IS_SUBMITING', payload: false });
  //   }
  // };

  const onEditorSubmitValue = async (field, option, value, updateInCascade = false, updatesGroupInfo = false) => {
    const parsedValue =
      field.fieldType === 'MULTISELECT_CODELIST' ||
      ((field.fieldType === 'LINK' || field.fieldType === 'EXTERNAL_LINK') && Array.isArray(value))
        ? value.join(';')
        : value;
  
    const encodedValue = encodeURIComponent(parsedValue);
  
    try {
      if ((!isSubmiting && initialFieldValue !== encodedValue) || encodedValue === '') {
        await DatasetService.updateFieldWebform(datasetId, field, encodedValue, tableSchemaId);
        
        if (!isNil(onUpdatePamsValue) && (updateInCascade || updatesGroupInfo)) {
          onUpdatePamsValue(field?.recordId, field?.value, field?.fieldId, updatesGroupInfo);
        }
  
        if (!isNil(onUpdateSinglesList) && field?.updatesSingleListData) {
          onUpdateSinglesList();
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
  

  const onFileDeleteVisible = (fieldId, fieldSchemaId) =>
    webformFieldDispatch({ type: 'ON_FILE_DELETE_OPENED', payload: { fieldId, fieldSchemaId } });

  const onFileUploadVisible = (fieldId, fieldSchemaId, validExtensions, maxSize) => {
    webformFieldDispatch({
      type: 'ON_FILE_UPLOAD_SET_FIELDS',
      payload: { fieldId, fieldSchemaId, validExtensions, maxSize }
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

  const infoAttachTooltip = `${resourcesContext.messages['supportedFileAttachmentsTooltip']} ${
    getAttachExtensions || '*'
  }
  ${resourcesContext.messages['supportedFileAttachmentsMaxSizeTooltip']} ${
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

  const renderSinglePamsTemplate = option => {
    const pams = pamsRecords.find(pamRecord => pamRecord.elements.find(element => element.value === option.value));

    if (!isNil(pams)) {
      return `#${option.label} - ${pams.elements.find(element => TextUtils.areEquals(element.name, 'Title')).value}`;
    } else {
      return option.label;
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
            id={field.fieldId}
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
            id={field.fieldId}
            monthNavigator={true}
            onBlur={e => {
              if (isNil(field.recordId)) onSaveField(option, formatDate(e.value, isNil(e.value)));
              else onEditorSubmitValue(field, option, formatDateTime(e.value, isNil(e.value)));
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
            value={new Date(field.value)}
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
              disabled={isLoadingData}
              filter={true}
              filterPlaceholder={resourcesContext.messages['linkFilterPlaceholder']}
              isLoadingData={isLoadingData}
              maxSelectedLabels={10}
              onUpdate={(event)=>{
                onFillField(field, option, event.target.value, isConditional);
              }}
              onChange={event => {
                if (isNil(field.recordId)) onSaveField(option, event.target.value);
                else onEditorSubmitValue(field, option, event.target.value);
              }}
              onFilterInputChangeBackend={filter => onFilter(filter, field)}
              optionLabel="itemType"
              options={linkItemsOptions}
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
              disabled={isLoadingData}
              filter={true}
              filterPlaceholder={resourcesContext.messages['linkFilterPlaceholder']}
              isLoadingData={isLoadingData}
              onUpdate={(event)=>{
                onFillField(field, option, event.target.value, isConditional);
              }}
              onChange={event => {
                const value =
                  typeof event.target.value === 'object' && !Array.isArray(event.target.value)
                    ? event.target.value.value
                    : event.target.value;
                webformFieldDispatch({ type: 'SET_SECTOR_AFFECTED', payload: { value } });
                if (isNil(field.recordId)) onSaveField(option, value);
                else onEditorSubmitValue(field, option, value);
              }}
              onFilterInputChangeBackend={filter => onFilter(filter, field)}
              optionLabel="itemType"
              options={linkItemsOptions}
              showFilterClear={true}
              value={RecordUtils.getLinkValue(linkItemsOptions, field.value)}
            />
          );
        }
      case 'MULTISELECT_CODELIST':
        return (
          <MultiSelectWebform
            appendTo={document.body}
            id={field.fieldId}
            itemTemplate={TextUtils.areEquals(field.name, 'ListOfSinglePams') ? renderSinglePamsTemplate : null}
            maxSelectedLabels={10}
            onChange={event => {
              onFillField(field, option, event.target.value);
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else onEditorSubmitValue(field, option, event.target.value);
            }}
            options={
              field.name === 'Objective'
                ? getObjectiveOptions(sectorAffectedValue)
                : field.codelistItems.map(codelist => ({ label: codelist, value: codelist }))
            }
            value={getMultiselectValues(
              field.codelistItems.map(codelist => ({ label: codelist, value: codelist })),
              field.value
            )}
            valuesSeparator=";"
          />
        );
      case 'CODELIST':
        return (
          <DropdownWebform
            appendTo={document.body}
            id={field.fieldId}
            onChange={event => {
              onFillField(field, option, event.target.value);
              webformFieldDispatch({ type: 'SET_SECTOR_AFFECTED', payload: { value: event.target.value } });
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else onEditorSubmitValue(field, option, event.target.value);
            }}
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
            characterCounterStyles={{ marginBottom: 0 }}
            hasMaxCharCounter
            id={field.fieldId}
            keyfilter={RecordUtils.getFilter(type)}
            onBlur={event => {
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else
                onEditorSubmitValue(
                  field,
                  option,
                  event.target.value,
                  field.isPrimary || false,
                  field.updatesGroupInfo
                );
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
              className={field.required ? styles.required : undefined}
              collapsedHeight={150}
              id={field.fieldId}
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
                onClick={() => onFileDownload(field.value, field.fieldId)}
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
              onClick={() => onFileDeleteVisible(field.fieldId, field.fieldSchemaId)}
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
                  fieldId: selectedFieldId
                })
              : getUrl(DatasetConfig.uploadAttachmentWithProviderId, {
                  dataflowId,
                  datasetId,
                  fieldId: selectedFieldId,
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
