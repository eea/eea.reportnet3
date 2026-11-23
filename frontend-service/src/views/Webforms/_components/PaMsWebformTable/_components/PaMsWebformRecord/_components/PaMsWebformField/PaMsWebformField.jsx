import { Fragment, useContext, useEffect, useReducer, useRef, useCallback } from 'react';
import { useQueryClient } from 'react-query';

import isNil from 'lodash/isNil';

import { config } from 'conf';
import { DatasetConfig } from 'repositories/config/DatasetConfig';

import styles from './PaMsWebformField.module.scss';

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

import { pamsWebformFieldReducer } from './_functions/Reducers/pamsWebformFieldReducer';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { WebformsUtils } from 'views/Webforms/_functions/Utils/WebformsUtils';
import { PaMsUtils } from './_functions/Utils/PaMsUtils';
import { RecordUtils } from 'views/_functions/Utils';
import { PaMsWebformRecordUtils } from 'views/Webforms/_components/PaMsWebformTable/_components/PaMsWebformRecord/_functions/Utils/PaMsWebformRecordUtils';

import { TextUtils } from 'repositories/_utils/TextUtils';
import { isEmpty } from 'lodash';

export const PaMsWebformField = ({
  bigData = false,
  changedConditionalFieldData,
  columnsSchema,
  conditionalFieldChange,
  dataProviderId,
  dataflowId,
  datasetId,
  datasetSchemaId,
  dependantConditionalFieldId,
  element,
  hasErrors,
  isConditional,
  isConditionalChanged,
  isDependantConditionalField,
  newRecord,
  onFieldUpdate = () => {},
  onFillField,
  onSaveField,
  onUpdateSinglesList,
  onUpdatePamsValue,
  pamsRecords,
  preparationCode,
  record,
  referencedTableSchemaId,
  tableSchemaId,
  tableSchemaName,
  updatingField = { field: null, isUpdating: false }
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const queryClient = useQueryClient();

  const inputRef = useRef(null);
  const isMountedRef = useRef(true);
  // Big data only: value just sent by Tab/Enter or a date selection, so the blur that follows does not send it again.
  const sentBeforeBlurRef = useRef(null);
  // Big data only: true once an option is clicked in the multiselect list, so closing the list without a change sends nothing.
  const multiSelectChangedRef = useRef(false);
  // Big data only: where Tab should put the cursor once the save it started has finished.
  const tabFocusPlanRef = useRef(null);

  const [pamsWebformFieldState, pamsWebformFieldDispatch] = useReducer(pamsWebformFieldReducer, {
    initialFieldValue: '',
    isDeleteAttachmentVisible: false,
    isDeleteRowVisible: false,
    isDeletingAttachment: false,
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
    isDeletingAttachment,
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
  } = pamsWebformFieldState;

  const { formatDate, formatDateTime, getMultiselectValues } = PaMsWebformRecordUtils;

  const { getObjectiveOptions } = PaMsUtils;

  // Same check as the ENTITIES webform: is this the field whose save is running?
  const isFieldBeingSaved = field =>
    updatingField.isUpdating &&
    field.recordId === updatingField.field?.recordId &&
    [field.fieldSchemaId, field.fieldSchema, field.fieldId].includes(
      updatingField.field?.fieldSchemaId ?? updatingField.field?.fieldSchema ?? updatingField.field?.fieldId
    );

  useEffect(() => {
    return () => {
      isMountedRef.current = false;
    };
  }, []);

  useEffect(() => {
    if (element.fieldType === 'LINK' || element.fieldType === 'EXTERNAL_LINK') onFilter('', element);
  }, [newRecord, conditionalFieldChange]);

  const onAttach = async value => {
    onFillField(record, selectedFieldSchemaId, `${value.files[0].name}`);
    onToggleDialogVisible(false);
  };

  const onConfirmDeleteAttachment = async () => {
    pamsWebformFieldDispatch({ type: 'SET_IS_DELETING_ATTACHMENT', payload: true });
    try {
      await DatasetService.deleteAttachment({
        dataflowId,
        datasetId,
        fieldId: selectedFieldId,
        dataProviderId,
        tableSchemaName,
        fieldName: selectedFieldName,
        fileName: selectedFileName,
        recordId: selectedRecordId
      });
      onFillField(record, selectedFieldSchemaId, '');
      onToggleDeleteAttachmentDialogVisible(false);
    } catch (error) {
      console.error('PaMsWebformField - onConfirmDeleteAttachment.', error);
    } finally {
      pamsWebformFieldDispatch({ type: 'SET_IS_DELETING_ATTACHMENT', payload: false });
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
        tableSchemaName,
        fieldName
      });
      DownloadFile(data, fileName);
    } catch (error) {
      console.error('PaMsWebformField - onFileDownload.', error);
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

      const masterConditionalFieldId = element.referencedField.masterConditionalFieldId;

      const fieldMatch = el =>
        !isNil(masterConditionalFieldId) &&
        [el.fieldSchemaId, el.fieldId, el.fieldSchema].includes(masterConditionalFieldId);

      const conditionalField =
        // 1. Try to find a matching field directly in the top-level elements
        record.elements.find(el => fieldMatch(el)) ||
        // 2. Otherwise, look inside BLOCKs:
        //    - pick the block records matching the current recordId
        //    - search their elements for the matching field
        record.elements
          .filter(el => el.type === 'BLOCK')
          .flatMap(block => block.elementsRecords?.filter(er => er.recordId === record.recordId) || [])
          .flatMap(elementRecord => elementRecord.elements || [])
          .find(recordElement => fieldMatch(recordElement));

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
                  : (conditionalField.type === 'LINK' || conditionalField.fieldType === 'LINK') &&
                    Array.isArray(conditionalField.value) &&
                    conditionalField.value?.length > 1
                  ? conditionalField.value?.join(';')
                  : conditionalField.value
                : isNil(masterConditionalFieldId)
                ? ''
                : encodeURIComponent(element.value),
              localDatasetSchemaId,
              400
            );
            return referencedFieldValues.map(referencedField => ({
              itemType:
                !isNil(referencedField.label) &&
                referencedField.label !== '' &&
                referencedField.label !== referencedField.value
                  ? `${referencedField.label}`
                  : referencedField.value,
              value: referencedField.value
            }));
          },
          {
            staleTime: 5 * 60 * 1000 // Example stale time
          }
        )
        .then(linkItems => {
          if (isMountedRef.current) {
            pamsWebformFieldDispatch({ type: 'SET_LINK_ITEMS', payload: linkItems });
          }
        })
        .catch(error => {
          console.error('PaMsWebformField - onFilter.', error);
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
      pamsWebformFieldDispatch,
      notificationContext
    ]
  );

  const onFocusField = value => {
    sentBeforeBlurRef.current = null;
    pamsWebformFieldDispatch({ type: 'SET_INITIAL_FIELD_VALUE', payload: value });
  };

  const rememberSentBeforeBlur = value => {
    if (bigData) sentBeforeBlurRef.current = value;
  };

  // True when the blur would send exactly what Tab/Enter or the date selection has just sent.
  const isAlreadySentBeforeBlur = value => {
    const isAlreadySent = bigData && !isNil(sentBeforeBlurRef.current) && sentBeforeBlurRef.current === value;
    sentBeforeBlurRef.current = null;
    return isAlreadySent;
  };

  // True when the multiselect list closes on big data without any option clicked since the last save.
  const isMultiSelectUnchanged = () => {
    const isUnchanged = bigData && !multiSelectChangedRef.current;
    multiSelectChangedRef.current = false;
    return isUnchanged;
  };

  // True when the field was empty when it got focus and is still empty (big data only), so nothing is sent.
  const isStillEmpty = value => bigData && value === '' && initialFieldValue === '';

  // Big data: while a save runs, text fields are read-only instead of disabled, so the cursor can stay in them or move
  // with Tab as usual; nothing new is saved from them until the save ends.
  const isFormLocked = () => bigData && updatingField.isUpdating;

  const onEditorKeyChange = (event, field, option) => {
    if (isFormLocked()) return;
    if (event.key === 'Escape') {
    } else if (event.key === 'Enter') {
      if (isStillEmpty(event.target.value)) return;
      rememberSentBeforeBlur(event.target.value);
      onEditorSubmitValue(field, option, event.target.value);
    } else if (event.key === 'Tab') {
      if (isStillEmpty(event.target.value)) return;
      rememberSentBeforeBlur(event.target.value);
      const tabFocusPlan = bigData ? WebformsUtils.createTabFocusPlan(event.target, event.shiftKey) : null;
      tabFocusPlanRef.current = tabFocusPlan;
      onEditorSubmitValue(field, option, event.target.value);
      // Big data: when this Tab starts a save, the cursor stays here until it ends, then moves on.
      if (tabFocusPlan?.isSaveSent) event.preventDefault();
    }
  };

  const onEditorSubmitValue = async (field, option, value, updateInCascade = false, updatesGroupInfo = false) => {
    let conditionalFields;
    let parsedValues;

    if (isConditional && (field.fieldType === 'LINK' || field.fieldType === 'CODELIST')) {
      const changedElementIndex = record.elements.indexOf(field);

      conditionalFields = record.elements
        .map(element =>
          !(element.fieldSchema === option || element.fieldSchemaId === option)
            ? !isEmpty(field?.dependency)
              ? element?.referencedField?.masterConditionalFieldId === field.fieldSchema ||
                element?.referencedField?.masterConditionalFieldId === field.fieldSchemaId
                ? { ...element, value: '' }
                : { ...element }
              : {
                  ...element,
                  value:
                    record.elements.indexOf(element) > changedElementIndex &&
                    (element?.referencedField?.masterConditionalFieldId === field.fieldSchema ||
                      element?.referencedField?.masterConditionalFieldId === field.fieldSchemaId)
                      ? ''
                      : element.value
                }
            : { ...element, value: value }
        )
        .filter(conditionalField => conditionalField.type === 'FIELD' && conditionalField.pk !== true);

      parsedValues = conditionalFields.map(conditionalField =>
        (conditionalField.fieldType === 'MULTISELECT_CODELIST' ||
          conditionalField.fieldType === 'LINK' ||
          conditionalField.fieldType === 'EXTERNAL_LINK') &&
        Array.isArray(conditionalField.value)
          ? { ...conditionalField, value: conditionalField.value.join(';') }
          : { ...conditionalField }
      );
    }

    const parsedValue =
      field.fieldType === 'MULTISELECT_CODELIST' ||
      ((field.fieldType === 'LINK' || field.fieldType === 'EXTERNAL_LINK') && Array.isArray(value))
        ? value.join(';')
        : value;

    const tabFocusPlan = tabFocusPlanRef.current;
    tabFocusPlanRef.current = null;
    let isSaveSent = false;

    try {
      if ((!isSubmiting && initialFieldValue !== parsedValue) || parsedValue === '') {
        bigData && onFieldUpdate(true, field);
        isSaveSent = true;
        if (tabFocusPlan) tabFocusPlan.isSaveSent = true;
        if (!isNil(conditionalFields) && !isNil(parsedValues)) {
          await DatasetService.updateConditionalFieldsWebform(
            datasetId,
            parsedValues,
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
        // Big data: the cursor can stay in the field after a save, so the saved value is the new "unchanged" baseline.
        bigData && pamsWebformFieldDispatch({ type: 'SET_INITIAL_FIELD_VALUE', payload: parsedValue });

        if (!isNil(onUpdateSinglesList) && field?.updatesSingleListData) {
          onUpdateSinglesList();
        }
      }
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        if (field?.fieldType !== 'DATETIME') {
          console.error('PaMsWebformField - onEditorSubmitValue.', error);
          if (updateInCascade) {
            notificationContext.add({ type: 'UPDATE_WEBFORM_FIELD_IN_CASCADE_BY_ID_ERROR' }, true);
          } else {
            notificationContext.add({ type: 'UPDATE_WEBFORM_FIELD_BY_ID_ERROR' }, true);
          }
        }
      }
    } finally {
      bigData && onFieldUpdate(false, field);
      if (isSaveSent) WebformsUtils.applyTabFocusPlan(tabFocusPlan);
      else WebformsUtils.cancelTabFocusPlan(tabFocusPlan);
      pamsWebformFieldDispatch({ type: 'SET_IS_SUBMITING', payload: false });
    }
  };

  const onFileDeleteVisible = (fileName, fieldName, recordId, fieldId, fieldSchemaId) => {
    pamsWebformFieldDispatch({
      type: 'ON_FILE_DELETE_OPENED',
      payload: { fileName, fieldName, recordId, fieldId, fieldSchemaId }
    });
  };

  const onFileUploadVisible = (fieldName, recordId, fieldId, fieldSchemaId, validExtensions, maxSize) => {
    pamsWebformFieldDispatch({
      type: 'ON_FILE_UPLOAD_SET_FIELDS',
      payload: { fieldName, recordId, fieldId, fieldSchemaId, validExtensions, maxSize }
    });
  };

  const onToggleDeleteAttachmentDialogVisible = value =>
    pamsWebformFieldDispatch({ type: 'ON_TOGGLE_DELETE_DIALOG', payload: { value } });

  const onToggleDialogVisible = value => pamsWebformFieldDispatch({ type: 'ON_TOGGLE_DIALOG', payload: { value } });

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
            disabled={field?.readOnly || updatingField.isUpdating}
            id={field.fieldId || field.fieldSchemaId}
            isLoadingData={!isEmpty(field.value) && isFieldBeingSaved(field)}
            monthNavigator={true}
            onBlur={event => {
              const blurValue = formatDate(event.target.value, isNil(event.target.value));
              if (isNil(field.recordId)) onSaveField(option, blurValue);
              else if (!isAlreadySentBeforeBlur(blurValue) && !isStillEmpty(blurValue))
                onEditorSubmitValue(field, option, blurValue);
            }}
            onChange={event => onFillField(field, option, formatDate(event.target.value, isNil(event.target.value)))}
            onFocus={event => {
              changeDatePickerPosition(event.target.getBoundingClientRect().left);
              onFocusField(event.target.value);
            }}
            onSelect={event => {
              onFillField(field, option, formatDate(event.value, isNil(event.value)));
              rememberSentBeforeBlur(formatDate(event.value, isNil(event.value)));
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
            disabled={field?.readOnly || updatingField.isUpdating}
            id={field.fieldId || field.fieldSchemaId}
            isLoadingData={!isEmpty(field.value) && isFieldBeingSaved(field)}
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
          if (
            isConditionalChanged &&
            !isEmpty(field.value) &&
            (!isEmpty(field?.dependency) || !isEmpty(field.referencedField?.masterConditionalFieldId))
          ) {
            const emptyValue = [];
            if (
              (isDependantConditionalField && !isEmpty(dependantConditionalFieldId)) ||
              !isEmpty(field.referencedField?.masterConditionalFieldId)
            ) {
              field.referencedField?.masterConditionalFieldId === dependantConditionalFieldId ||
                (record.elements.indexOf(field) > record.elements.indexOf(changedConditionalFieldData) &&
                  record.elements.indexOf(field) > 0 &&
                  record.elements.indexOf(changedConditionalFieldData) > 0 &&
                  onFillField(field, option, emptyValue, isConditional));
            } else {
              onFillField(field, option, emptyValue, isConditional);
            }
          }

          return (
            <MultiSelectWebform
              appendTo={document.body}
              clearButton={false}
              currentValue={field.value}
              disabled={field?.readOnly || isLoadingData || updatingField.isUpdating}
              filter={true}
              filterPlaceholder={resourcesContext.messages['linkFilterPlaceholder']}
              isLoadingData={isLoadingData || isFieldBeingSaved(field)}
              maxSelectedLabels={10}
              onChange={() => {
                if (isMultiSelectUnchanged()) return;
                if (isNil(field.recordId)) onSaveField(option, field.value);
                else onEditorSubmitValue(field, option, field.value);
              }}
              onFilterInputChangeBackend={filter => onFilter(filter, field)}
              onUpdate={event => {
                multiSelectChangedRef.current = true;
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

          if (
            isConditionalChanged &&
            !isEmpty(field.value) &&
            (!isEmpty(field?.dependency) || !isEmpty(field.referencedField?.masterConditionalFieldId))
          ) {
            const emptyValue = '';
            if (
              (isDependantConditionalField && !isEmpty(dependantConditionalFieldId)) ||
              !isEmpty(field.referencedField?.masterConditionalFieldId)
            ) {
              field.referencedField?.masterConditionalFieldId === dependantConditionalFieldId ||
                (record.elements.indexOf(field) > record.elements.indexOf(changedConditionalFieldData) &&
                  record.elements.indexOf(field) > 0 &&
                  record.elements.indexOf(changedConditionalFieldData) > 0 &&
                  onFillField(field, option, emptyValue, isConditional));
            } else {
              onFillField(field, option, emptyValue, isConditional);
            }
          }

          return (
            <DropdownWebform
              appendTo={document.body}
              currentValue={!isNil(selectedValue) ? selectedValue.value : ''}
              disabled={field?.readOnly || isLoadingData || updatingField.isUpdating}
              filter={true}
              filterPlaceholder={resourcesContext.messages['linkFilterPlaceholder']}
              isLoadingData={isLoadingData || isFieldBeingSaved(field)}
              onChange={event => {
                const value =
                  typeof event.target?.value === 'object' && !Array.isArray(event.target.value)
                    ? event.target?.value?.value
                    : event.target?.value;

                if (value !== field.value) {
                  onFillField(field, option, value, isConditional);
                  pamsWebformFieldDispatch({ type: 'SET_SECTOR_AFFECTED', payload: { value } });
                  if (isNil(field.recordId)) onSaveField(option, value);
                  else if (!(event.target.action === 'arrowKeys')) onEditorSubmitValue(field, option, value);
                }
              }}
              onFilterInputChangeBackend={filter => onFilter(filter, field)}
              optionLabel="itemType"
              options={linkItemsOptions}
              showClear={true}
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
            disabled={field?.readOnly || updatingField.isUpdating}
            filter={true}
            filterPlaceholder={resourcesContext.messages['linkFilterPlaceholder']}
            id={field.fieldId || field.fieldSchemaId}
            isLoadingData={isFieldBeingSaved(field)}
            itemTemplate={TextUtils.areEquals(field.name, 'ListOfSinglePams') ? renderSinglePamsTemplate : null}
            maxSelectedLabels={10}
            onChange={() => {
              if (isMultiSelectUnchanged()) return;
              if (isNil(field.recordId)) onSaveField(option, field.value);
              else onEditorSubmitValue(field, option, field.value);
            }}
            onUpdate={event => {
              multiSelectChangedRef.current = true;
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
            disabled={field?.readOnly || isLoadingData || updatingField.isUpdating}
            id={field.fieldId}
            isLoadingData={isLoadingData || isFieldBeingSaved(field)}
            onChange={event => {
              const value =
                typeof event.target?.value === 'object' && !Array.isArray(event.target.value)
                  ? event.target?.value?.value
                  : event.target?.value;
              if (value !== field.value) {
                onFillField(field, option, value, isConditional);
                pamsWebformFieldDispatch({ type: 'SET_SECTOR_AFFECTED', payload: { value } });
                if (isNil(field.recordId)) onSaveField(option, value);
                else if (!(event.target.action === 'arrowKeys')) onEditorSubmitValue(field, option, value);
              }
            }}
            onFilterInputChangeBackend={filter => onFilter(filter, field)}
            optionLabel="itemType"
            options={codelistOptions}
            showClear={true}
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
        if (
          isConditionalChanged &&
          !isEmpty(field.value) &&
          (!isEmpty(field?.dependency) || !isEmpty(field.referencedField?.masterConditionalFieldId))
        ) {
          const emptyValue = '';
          if (
            (isDependantConditionalField && !isEmpty(dependantConditionalFieldId)) ||
            !isEmpty(field.referencedField?.masterConditionalFieldId)
          ) {
            field.referencedField?.masterConditionalFieldId === dependantConditionalFieldId ||
              (record.elements.indexOf(field) > record.elements.indexOf(changedConditionalFieldData) &&
                record.elements.indexOf(field) > 0 &&
                record.elements.indexOf(changedConditionalFieldData) > 0 &&
                onFillField(field, option, emptyValue, isConditional));
          } else {
            changedConditionalFieldData?.name === field?.dependency?.field &&
              onFillField(field, option, emptyValue, isConditional);
          }
        }
        return (
          <InputText
            characterCounterStyles={{ marginBottom: 0 }}
            className={isFormLocked() ? 'p-disabled' : undefined}
            disabled={field?.readOnly}
            hasErrors={hasErrors}
            hasMaxCharCounter
            id={field.fieldId || field.fieldSchemaId}
            isLoadingData={isFieldBeingSaved(field)}
            keyfilter={RecordUtils.getFilter(type)}
            onBlur={event => {
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else if (
                !isAlreadySentBeforeBlur(event.target.value) &&
                !isStillEmpty(event.target.value) &&
                !isFormLocked()
              )
                onEditorSubmitValue(
                  field,
                  option,
                  event.target.value,
                  field.isPrimary || false,
                  field.updatesGroupInfo
                );
            }}
            onChange={event => {
              const editedField = { ...field, value: event.target.value };
              onFillField(editedField, option, event.target.value);
            }}
            onFocus={event => onFocusField(event.target.value)}
            onKeyDown={event => onEditorKeyChange(event, field, option)}
            readOnly={isFormLocked()}
            ref={inputRef}
            value={field.value}
          />
        );
      case 'TEXTAREA':
        return (
          <Fragment>
            <InputTextarea
              className={
                [field.required ? styles.required : undefined, isFormLocked() ? 'p-disabled' : undefined]
                  .filter(Boolean)
                  .join(' ') || undefined
              }
              collapsedHeight={150}
              disabled={field?.readOnly}
              hasErrors={hasErrors}
              id={field.fieldId || field.fieldSchemaId}
              isLoadingData={isFieldBeingSaved(field)}
              onBlur={event => {
                if (isNil(field.recordId)) onSaveField(option, event.target.value);
                else if (
                  !isAlreadySentBeforeBlur(event.target.value) &&
                  !isStillEmpty(event.target.value) &&
                  !isFormLocked()
                )
                  onEditorSubmitValue(field, option, event.target.value);
              }}
              onChange={event => onFillField(field, option, event.target.value)}
              onFocus={event => onFocusField(event.target.value)}
              onKeyDown={event => {
                // Big data: Enter only adds a line break, as before the grey-out; the text is saved on Tab or blur.
                if (bigData && event.key === 'Enter') return;
                onEditorKeyChange(event, field, option);
              }}
              readOnly={isFormLocked()}
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
                onClick={() =>
                  onFileDownload(field.value, field.fieldId || field.fieldSchemaId, field.recordId, field.name)
                }
              />
            )}
            {
              <Button
                className="p-button-animated-blink p-button-primary-transparent"
                disabled={updatingField.isUpdating}
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
              disabled={updatingField.isUpdating}
              icon="trash"
              onClick={() =>
                onFileDeleteVisible(field.value, field.name, field.recordId, field.fieldId, field.fieldSchemaId)
              }
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
                  tableSchemaName,
                  fieldName: selectedFieldName,
                  recordId: selectedRecordId,
                  previousFileName: undefined,
                  preparationCode
                })
              : getUrl(DatasetConfig.uploadAttachmentWithProviderId, {
                  dataflowId,
                  datasetId,
                  fieldId: selectedFieldId,
                  tableSchemaName,
                  fieldName: selectedFieldName,
                  recordId: selectedRecordId,
                  previousFileName: undefined,
                  providerId: dataProviderId,
                  preparationCode
                })
          }`}
        />
      )}
      {isDeleteAttachmentVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={isDeletingAttachment}
          header={`${resourcesContext.messages['deleteAttachmentHeader']}`}
          iconConfirm={isDeletingAttachment ? 'spinnerAnimate' : 'check'}
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
