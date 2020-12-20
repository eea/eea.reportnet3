import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isNil from 'lodash/isNil';

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
  onUpdatePamsId,
  record
}) => {
  const resources = useContext(ResourcesContext);

  const [webformFieldState, webformFieldDispatch] = useReducer(webformFieldReducer, {
    isDeleteAttachmentVisible: false,
    isDeleteRowVisible: false,
    isDeletingRow: false,
    isDialogVisible: { deleteRow: false, uploadFile: false },
    isFileDialogVisible: false,
    linkItemsOptions: [],
    record,
    sectorAffectedValue: null,
    selectedFieldId: '',
    selectedFieldSchemaId: '',
    selectedMaxSize: '',
    selectedValidExtensions: []
  });

  const {
    isDeleteAttachmentVisible,
    isFileDialogVisible,
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
    const fileDeleted = await DatasetService.deleteFileData(datasetId, selectedFieldId);
    if (fileDeleted) {
      onFillField(record, selectedFieldSchemaId, '');
      onToggleDeleteAttachmentDialogVisible(false);
    }
  };
  const onFileDownload = async (fileName, fieldId) => {
    const fileContent = await DatasetService.downloadFileData(datasetId, fieldId);

    DownloadFile(fileContent, fileName);
  };

  const onFilter = async (filter, field) => {
    if (isNil(field) || isNil(field.referencedField)) {
      return;
    }
    const conditionalField = webformFieldState.record.elements.find(
      element => element.fieldSchemaId === field.referencedField.masterConditionalFieldId
    );

    if (datasetSchemaId === '' || isNil(datasetSchemaId)) {
      const metadata = await MetadataUtils.getDatasetMetadata(datasetId);
      datasetSchemaId = metadata.datasetSchemaId;
    }

    const referencedFieldValues = await DatasetService.getReferencedFieldValues(
      datasetId,
      field.fieldSchemaId,
      filter,
      !isNil(conditionalField) ? conditionalField.value : field.value,
      datasetSchemaId,
      50
    );

    const linkItems = referencedFieldValues
      .map(referencedField => {
        return {
          itemType: `${referencedField.value}${
            !isNil(referencedField.label) &&
            referencedField.label !== '' &&
            referencedField.label !== referencedField.value
              ? ` - ${referencedField.label}`
              : ''
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
    webformFieldDispatch({ type: 'SET_LINK_ITEMS', payload: linkItems });
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
    const parsedValue =
      field.fieldType === 'MULTISELECT_CODELIST' || (field.fieldType === 'LINK' && Array.isArray(value))
        ? value.join(',')
        : value;

    try {
      await DatasetService.updateFieldById(
        datasetId,
        option,
        field.fieldId,
        field.fieldType,
        parsedValue,
        updateInCascade
      );
      if (!isNil(onUpdatePamsId) && updateInCascade) {
        onUpdatePamsId(field.recordId, field.value, field.fieldId);
      }
      if (!isNil(onUpdateSinglesList) && field.updatesSingleListData) {
        onUpdateSinglesList();
      }
    } catch (error) {
      console.error('error', error);
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

  const getAttachExtensions = [{ fileExtension: selectedValidExtensions || [] }]
    .map(file => file.fileExtension.map(extension => (extension.indexOf('.') > -1 ? extension : `.${extension}`)))
    .flat()
    .join(', ');

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
            value={new Date(field.value)}
            yearNavigator={true}
            yearRange="2010:2030"
          />
        );

      case 'LINK':
        if (field.pkHasMultipleValues) {
          return (
            <MultiSelect
              appendTo={document.body}
              clearButton={false}
              currentValue={field.value}
              filter={true}
              filterPlaceholder={resources.messages['linkFilterPlaceholder']}
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
            />
          );
        } else {
          const selectedValue = RecordUtils.getLinkValue(linkItemsOptions, field.value);
          return (
            <Dropdown
              appendTo={document.body}
              currentValue={!isNil(selectedValue) ? selectedValue.value : ''}
              filter={true}
              filterPlaceholder={resources.messages['linkFilterPlaceholder']}
              filterBy="itemType,value"
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
              else onEditorSubmitValue(field, option, event.target.value, field.isPrimary);
            }}
            onChange={event => onFillField(field, option, event.target.value)}
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
          invalidExtensionMessage={resources.messages['invalidExtensionFile']}
          isDialog={true}
          mode="advanced"
          multiple={false}
          name="file"
          onUpload={onAttach}
          operation="PUT"
          url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.importFileData, {
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
