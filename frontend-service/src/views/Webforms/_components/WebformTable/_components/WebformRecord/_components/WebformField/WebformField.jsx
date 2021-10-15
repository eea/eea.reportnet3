import { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

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
import { Dropdown } from 'views/_components/Dropdown';
import { InputText } from 'views/_components/InputText';
import { InputTextarea } from 'views/_components/InputTextarea';
import { MultiSelect } from 'views/_components/MultiSelect';

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
  record
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

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

  const { formatDate, getMultiselectValues } = WebformRecordUtils;

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
      await DatasetService.deleteAttachment(datasetId, selectedFieldId);
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

  const onFilter = async (filter, field) => {
    if (isNil(field) || isNil(field.referencedField)) {
      return;
    }

    const conditionalField = record.elements.find(
      element => element.fieldSchemaId === field.referencedField.masterConditionalFieldId
    );

    if (datasetSchemaId === '' || isNil(datasetSchemaId)) {
      const metadata = await DatasetService.getMetadata(datasetId);
      datasetSchemaId = metadata.datasetSchemaId;
    }

    try {
      webformFieldDispatch({ type: 'SET_IS_LOADING_DATA', payload: true });
      const referencedFieldValues = await DatasetService.getReferencedFieldValues(
        datasetId,
        field.fieldSchemaId,
        filter,
        !isNil(conditionalField)
          ? conditionalField.type === 'MULTISELECT_CODELIST'
            ? conditionalField.value?.replace('; ', ';').replace(';', '; ')
            : conditionalField.value
          : field.value,
        datasetSchemaId,
        100
      );

      const linkItems = referencedFieldValues
        .map(referencedField => {
          return {
            itemType: `${
              !isNil(referencedField.label) &&
              referencedField.label !== '' &&
              referencedField.label !== referencedField.value
                ? `${referencedField.label}`
                : referencedField.value
            }`,
            value: referencedField.value
          };
        })
        .sort((a, b) => a.value - b.value);

      if (!field.pkHasMultipleValues) {
        linkItems.unshift({
          itemType: resourcesContext.messages['noneCodelist'],
          value: ''
        });
      }

      if (referencedFieldValues.length > 99) {
        linkItems[linkItems.length - 1] = {
          disabled: true,
          itemType: resourcesContext.messages['moreElements'],
          value: ''
        };
      }

      webformFieldDispatch({ type: 'SET_LINK_ITEMS', payload: linkItems });
    } catch (error) {
      console.error('WebformField - onFilter.', error);
      notificationContext.add({
        type: 'GET_REFERENCED_LINK_VALUES_ERROR'
      });
    } finally {
      webformFieldDispatch({ type: 'SET_IS_LOADING_DATA', payload: false });
    }
  };

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

  const onEditorSubmitValue = async (field, option, value, updateInCascade = false, updatesGroupInfo = false) => {
    webformFieldDispatch({ type: 'SET_IS_SUBMITING', payload: true });
    const parsedValue =
      field.fieldType === 'MULTISELECT_CODELIST' ||
      ((field.fieldType === 'LINK' || field.fieldType === 'EXTERNAL_LINK') && Array.isArray(value))
        ? value.join(';')
        : value;

    try {
      if (!isSubmiting && initialFieldValue !== parsedValue) {
        await DatasetService.updateField(
          datasetId,
          option,
          field.fieldId,
          field.fieldType,
          parsedValue,
          updateInCascade
        );
        if (!isNil(onUpdatePamsValue) && (updateInCascade || updatesGroupInfo)) {
          onUpdatePamsValue(field.recordId, field.value, field.fieldId, updatesGroupInfo);
        }

        if (!isNil(onUpdateSinglesList) && field.updatesSingleListData) {
          onUpdateSinglesList();
        }
      }
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
      } else {
        console.error('WebformField - onEditorSubmitValue.', error);
        if (updateInCascade) {
          notificationContext.add({ type: 'UPDATE_WEBFORM_FIELD_IN_CASCADE_BY_ID_ERROR' });
        } else {
          notificationContext.add({ type: 'UPDATE_WEBFORM_FIELD_BY_ID_ERROR' });
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

  const getAttachExtensions = [{ fileExtension: element.validExtensions || [] }]
    .map(file => file.fileExtension.map(extension => (extension.indexOf('.') > -1 ? extension : `.${extension}`)))
    .flat()
    .join(', ');

  const infoAttachTooltip = `${resourcesContext.messages['supportedFileAttachmentsTooltip']} ${
    getAttachExtensions || '*'
  }
  ${resourcesContext.messages['supportedFileAttachmentsMaxSizeTooltip']} ${
    !isNil(element.maxSize) && element.maxSize.toString() !== '0'
      ? `${element.maxSize} ${resourcesContext.messages['MB']}`
      : resourcesContext.messages['maxSizeNotDefined']
  }`;

  const onUploadFileError = async ({ xhr }) => {
    if (xhr.status === 400) {
      notificationContext.add({
        type: 'UPLOAD_FILE_ERROR'
      });
    }
    if (xhr.status === 423) {
      notificationContext.add({
        type: 'GENERIC_BLOCKED_ERROR'
      });
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

  const renderTemplate = (field, option, type) => {
    switch (type) {
      case 'DATE':
        const changeDatePickerPosition = inputLeftPosition => {
          const datePickerElements = document.getElementsByClassName('p-datepicker');
          for (let index = 0; index < datePickerElements.length; index++) {
            const datePicker = datePickerElements[index];
            datePicker.style.left = `${inputLeftPosition}px`;
          }
        };
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
            showSeconds={true}
            showTime={true}
            value={field.value}
            yearNavigator={true}
            yearRange="1900:2100"
          />
        );
      case 'EXTERNAL_LINK':
      case 'LINK':
        if (field.pkHasMultipleValues) {
          return (
            <MultiSelect
              appendTo={document.body}
              clearButton={false}
              currentValue={field.value}
              disabled={isLoadingData}
              filter={true}
              filterPlaceholder={resourcesContext.messages['linkFilterPlaceholder']}
              isLoadingData={isLoadingData}
              maxSelectedLabels={10}
              onChange={event => {
                onFillField(field, option, event.target.value, isConditional);
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
            <Dropdown
              appendTo={document.body}
              currentValue={!isNil(selectedValue) ? selectedValue.value : ''}
              disabled={isLoadingData}
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
          <MultiSelect
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
          <Dropdown
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
              else onEditorSubmitValue(field, option, event.target.value, field.isPrimary, field.updatesGroupInfo);
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
                className={`p-button-animated-blink p-button-primary-transparent`}
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
              className={`p-button-animated-blink p-button-primary-transparent`}
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
          dialogClassName={styles.dialog}
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
          url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.uploadAttachment, {
            datasetId,
            fieldId: selectedFieldId
          })}`}
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
