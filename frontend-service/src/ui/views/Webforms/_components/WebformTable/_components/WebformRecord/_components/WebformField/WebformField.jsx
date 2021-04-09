import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isNil from 'lodash/isNil';

import { config } from 'conf';
import { DatasetConfig } from 'conf/domain/model/Dataset';

import styles from './WebformField.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { MultiSelect } from 'ui/views/_components/MultiSelect';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { webformFieldReducer } from './_functions/Reducers/webformFieldReducer';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { MetadataUtils } from 'ui/views/_functions/Utils';
import { PaMsUtils } from './_functions/Utils/PaMsUtils';
import { RecordUtils, TextUtils } from 'ui/views/_functions/Utils';
import { WebformRecordUtils } from 'ui/views/Webforms/_components/WebformTable/_components/WebformRecord/_functions/Utils/WebformRecordUtils';

export const WebformField = ({
  columnsSchema,
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
  const resources = useContext(ResourcesContext);

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
    selectedValidExtensions: []
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
    selectedValidExtensions
  } = webformFieldState;

  const { formatDate, getInputMaxLength, getMultiselectValues } = WebformRecordUtils;

  const { getObjectiveOptions } = PaMsUtils;

  useEffect(() => {
    if (element.fieldType === 'LINK') onFilter('', element);
  }, [newRecord, isConditionalChanged]);

  const onAttach = async value => {
    onFillField(record, selectedFieldSchemaId, `${value.files[0].name}`);
    onToggleDialogVisible(false);
  };

  const onConfirmDeleteAttachment = async () => {
    try {
      const { status } = await DatasetService.deleteFileData(datasetId, selectedFieldId);

      if (status >= 200 && status <= 299) {
        onFillField(record, selectedFieldSchemaId, '');
        onToggleDeleteAttachmentDialogVisible(false);
      }
    } catch (error) {
      console.error('error', error);
    }
  };

  const onFileDownload = async (fileName, fieldId) => {
    try {
      const { data } = await DatasetService.downloadFileData(datasetId, fieldId);

      DownloadFile(data, fileName);
    } catch (error) {
      console.error('error', error);
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
      const metadata = await MetadataUtils.getDatasetMetadata(datasetId);
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

      const linkItems = referencedFieldValues.data
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
          itemType: resources.messages['noneCodelist'],
          value: ''
        });
      }

      if (referencedFieldValues.data.length > 99) {
        linkItems[linkItems.length - 1] = {
          disabled: true,
          itemType: resources.messages['moreElements'],
          value: ''
        };
      }

      webformFieldDispatch({ type: 'SET_LINK_ITEMS', payload: linkItems });
    } catch (error) {
      console.error(`Error getting referenced link values: ${error}`);
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
      field.fieldType === 'MULTISELECT_CODELIST' || (field.fieldType === 'LINK' && Array.isArray(value))
        ? value.join(';')
        : value;

    try {
      if (!isSubmiting && initialFieldValue !== parsedValue) {
        await DatasetService.updateFieldById(
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

  // const onSelectField = field => webformFieldDispatch({ type: 'ON_SELECT_FIELD', payload: { field } });

  const onToggleDeleteAttachmentDialogVisible = value =>
    webformFieldDispatch({ type: 'ON_TOGGLE_DELETE_DIALOG', payload: { value } });

  const onToggleDialogVisible = value => webformFieldDispatch({ type: 'ON_TOGGLE_DIALOG', payload: { value } });

  const getAttachExtensions = [{ fileExtension: element.validExtensions || [] }]
    .map(file => file.fileExtension.map(extension => (extension.indexOf('.') > -1 ? extension : `.${extension}`)))
    .flat()
    .join(', ');

  const infoAttachTooltip = `${resources.messages['supportedFileAttachmentsTooltip']} ${getAttachExtensions || '*'}
  ${resources.messages['supportedFileAttachmentsMaxSizeTooltip']} ${
    !isNil(element.maxSize) && element.maxSize.toString() !== '0'
      ? `${element.maxSize} ${resources.messages['MB']}`
      : resources.messages['maxSizeNotDefined']
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

      case 'LINK':
        if (field.pkHasMultipleValues) {
          return (
            <MultiSelect
              appendTo={document.body}
              clearButton={false}
              currentValue={field.value}
              disabled={isLoadingData}
              filter={true}
              filterPlaceholder={resources.messages['linkFilterPlaceholder']}
              isLoadingData={isLoadingData}
              maxSelectedLabels={10}
              onChange={event => {
                onFillField(field, option, event.target.value, isConditional);
                if (isNil(field.recordId)) onSaveField(option, event.target.value);
                else onEditorSubmitValue(field, option, event.target.value);
              }}
              onFilterInputChangeBackend={filter => onFilter(filter, field)}
              options={linkItemsOptions}
              optionLabel="itemType"
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
              filterPlaceholder={resources.messages['linkFilterPlaceholder']}
              filterBy="itemType,value"
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
              // onFocus={() => {
              //   onFilter('', field);
              // }}
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
            maxSelectedLabels={10}
            id={field.fieldId}
            itemTemplate={TextUtils.areEquals(field.name, 'ListOfSinglePams') ? renderSinglePamsTemplate : null}
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
            options={
              field.name === 'Objective'
                ? getObjectiveOptions(sectorAffectedValue)
                : field.codelistItems.map(codelist => ({ label: codelist, value: codelist }))
            }
            // optionLabel="itemType"
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
            // currentValue={RecordUtils.getCellValue(cells, cells.field)}
            // filter={true}
            // filterPlaceholder={resources.messages['linkFilterPlaceholder']}
            // filterBy="itemType,value"
            onChange={event => {
              onFillField(field, option, event.target.value);
              webformFieldDispatch({ type: 'SET_SECTOR_AFFECTED', payload: { value: event.target.value } });
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
            keyfilter={RecordUtils.getFilter(type)}
            id={field.fieldId}
            maxLength={getInputMaxLength[type]}
            onBlur={event => {
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else onEditorSubmitValue(field, option, event.target.value, field.isPrimary, field.updatesGroupInfo);
            }}
            onChange={event => onFillField(field, option, event.target.value)}
            onFocus={event => {
              onFocusField(event.target.value);
            }}
            onKeyDown={event => onEditorKeyChange(event, field, option)}
            value={field.value}
          />
        );
      case 'TEXTAREA':
        return (
          <InputTextarea
            className={field.required ? styles.required : undefined}
            id={field.fieldId}
            maxLength={getInputMaxLength[type]}
            collapsedHeight={150}
            onBlur={event => {
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else onEditorSubmitValue(field, option, event.target.value);
            }}
            onChange={event => onFillField(field, option, event.target.value)}
            onFocus={event => {
              onFocusField(event.target.value);
            }}
            onKeyDown={event => onEditorKeyChange(event, field, option)}
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
            <span
              className={styles.nonExistField}
              dangerouslySetInnerHTML={{
                __html: TextUtils.parseText(resources.messages['fieldIsNotCreated'], { fieldName: field.name })
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

  return (
    <Fragment>
      {renderTemplate(element, element.fieldSchemaId, element.customType ? element.customType : element.fieldType)}
      {isFileDialogVisible && (
        <CustomFileUpload
          accept={getAttachExtensions || '*'}
          chooseLabel={resources.messages['selectFile']}
          className={styles.fileUpload}
          dialogClassName={styles.dialog}
          dialogHeader={resources.messages['uploadAttachment']}
          dialogOnHide={() => onToggleDialogVisible(false)}
          dialogVisible={isFileDialogVisible}
          fileLimit={1}
          infoTooltip={infoAttachTooltip}
          invalidExtensionMessage={resources.messages['invalidExtensionFile']}
          isDialog={true}
          maxFileSize={
            !isNil(element.maxSize) && element.maxSize.toString() !== '0'
              ? element.maxSize * 1000 * 1024
              : config.MAX_ATTACHMENT_SIZE
          }
          mode="advanced"
          multiple={false}
          name="file"
          onError={onUploadFileError}
          onUpload={onAttach}
          operation="PUT"
          url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.addAttachment, {
            datasetId,
            fieldId: selectedFieldId
          })}`}
        />
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
