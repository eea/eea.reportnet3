import { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './FieldDesigner.module.scss';

import { config } from 'conf';

import { AttachmentEditor } from './_components/AttachmentEditor';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { Checkbox } from 'views/_components/Checkbox';
import { CodelistEditor } from './_components/CodelistEditor';
import { Dialog } from 'views/_components/Dialog';
import { Dropdown } from 'views/_components/Dropdown';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { InputText } from 'views/_components/InputText';
import { InputTextarea } from 'views/_components/InputTextarea';
import { LinkSelector } from './_components/LinkSelector';
import ReactTooltip from 'react-tooltip';

import { DatasetService } from 'services/DatasetService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

import { fieldDesignerReducer } from './_functions/Reducers/fieldDesignerReducer';

import { FieldsDesignerUtils } from 'views/_functions/Utils/FieldsDesignerUtils';
import { RecordUtils } from 'views/_functions/Utils/RecordUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

export const FieldDesigner = ({
  addField = false,
  bulkDelete = false,
  checkDuplicates,
  checkInvalidCharacters,
  codelistItems,
  datasetId,
  datasetSchemaId,
  fieldDescription,
  fieldFileProperties,
  fieldHasMultipleValues,
  fieldId,
  fieldLink,
  fieldLinkedTableConditional = '',
  fieldLinkedTableLabel = '',
  fieldMasterTableConditional = '',
  fieldMustBeUsed,
  fieldName,
  fieldPK,
  fieldPKReferenced,
  fieldReadOnly,
  fieldRequired,
  fieldType,
  fields,
  hasPK,
  index,
  initialFieldIndexDragged,
  isCodelistOrLink,
  isDataflowOpen,
  isDesignDatasetEditorRead,
  isLoading = false,
  isReferenceDataset,
  markedForDeletion,
  onBulkCheck,
  onCodelistAndLinkShow,
  onFieldDelete,
  onFieldDragAndDrop,
  onFieldDragAndDropStart,
  onFieldUpdate,
  onNewFieldAdd,
  onShowDialogError,
  recordSchemaId,
  setIsLoading = () => {},
  tableSchemaId,
  totalFields
}) => {
  const geometricTypes = ['POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'];
  const { areEquals } = TextUtils;

  const initialFieldDesignerState = {
    addFieldCallSent: false,
    codelistItems: codelistItems,
    fieldDescriptionValue: fieldDescription,
    fieldLinkValue: fieldLink || null,
    fieldPkHasMultipleValues: fieldHasMultipleValues || false,
    fieldPkMustBeUsed: fieldMustBeUsed || false,
    fieldPKReferencedValue: fieldPKReferenced || false,
    fieldPKValue: fieldPK,
    fieldPreviousTypeValue: RecordUtils.getFieldTypeValue(fieldType) || '',
    fieldReadOnlyValue: fieldReadOnly,
    fieldRequiredValue: fieldRequired,
    fieldTypeValue: RecordUtils.getFieldTypeValue(fieldType),
    fieldValue: fieldName,
    initialDescriptionValue: undefined,
    initialFieldValue: undefined,
    isAttachmentEditorVisible: false,
    isCodelistEditorVisible: false,
    isDragging: false,
    isEditing: false,
    isLinkSelectorVisible: false,
    isQCManagerVisible: false,
    fieldFileProperties: fieldFileProperties
  };

  const [fieldDesignerState, dispatchFieldDesigner] = useReducer(fieldDesignerReducer, initialFieldDesignerState);

  const fieldTypeRef = useRef();
  const inputRef = useRef();

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const validationContext = useContext(ValidationContext);
  const [headerHeight, setHeaderHeight] = useState(0);
  const [headerInitialHeight, setHeaderInitialHeight] = useState();

  useEffect(() => {
    const header = document.getElementById('header');
    const observer = new ResizeObserver(entries =>
      entries.forEach(entry => {
        if (headerHeight !== entry.contentRect.height) {
          setHeaderHeight(entry.contentRect.height);
        }
      })
    );

    if (!isNil(header)) {
      observer.observe(header);
    }

    return () => {
      observer.disconnect();
    };
  }, []);

  useEffect(() => {
    const dropDowns = document.querySelectorAll('.p-dropdown-panel.p-input-overlay-visible');
    dropDowns.forEach(dropDown => {
      const dropDownDisplay = dropDown.style.display;
      if (dropDownDisplay) {
        if (headerInitialHeight === 64 || headerInitialHeight === 180) {
          dropDown.style.marginTop = `${headerHeight - headerInitialHeight}px`;
        }
      }
    });
  }, [headerHeight]);

  useEffect(() => {
    if (!isNil(fieldLink)) {
      dispatchFieldDesigner({
        type: 'SET_FIELD_LINK',
        payload: {
          link: fieldLink
        }
      });
    }
  }, [fieldLink]);

  const onSetInitHeaderHeight = () => {
    const header = document.getElementById('header');
    setHeaderInitialHeight(header.offsetHeight);
  };

  useEffect(() => {
    dispatchFieldDesigner({ type: 'SET_PK_REFERENCED', payload: fieldPKReferenced });
  }, [fieldPKReferenced]);

  useEffect(() => {
    if (!isNil(totalFields)) {
      if (totalFields === 0 && !isUndefined(tableSchemaId) && tableSchemaId !== '' && !isUndefined(inputRef.current)) {
        if (index === '-1') {
          inputRef.current.element.focus();
        }
      }
    }
  }, [totalFields]);

  const getDuplicatedName = () => {
    const filteredFields = fields.filter(field => field.name.startsWith(`${fieldDesignerState.fieldValue}_`));
    return `${fieldDesignerState.fieldValue}_${filteredFields.length + 1}`;
  };

  const validField = () =>
    !isNil(fieldDesignerState.fieldTypeValue) &&
    fieldDesignerState.fieldTypeValue !== '' &&
    !isNil(fieldDesignerState.fieldValue) &&
    fieldDesignerState.fieldValue !== '';

  const onAttachmentDropdownSelected = fieldType => {
    if (!isUndefined(fieldType)) {
      onCodelistAndLinkShow(fieldId, fieldType);
    }
    if (!fieldDesignerState.addFieldCallSent) {
      dispatchFieldDesigner({ type: 'TOGGLE_ATTACHMENT_EDITOR_VISIBLE', payload: true });
    }
  };

  const onChangeFieldType = type => {
    dispatchFieldDesigner({ type: 'SET_TYPE', payload: { type, previousType: fieldDesignerState.fieldTypeValue } });
    if (areEquals(type.fieldType, 'codelist') || areEquals(type.fieldType, 'multiselect_codelist')) {
      onCodelistDropdownSelected(type);
    } else if (areEquals(type.fieldType, 'link') || areEquals(type.fieldType, 'external_link')) {
      onLinkDropdownSelected(type);
    } else if (areEquals(type.fieldType, 'attachment')) {
      onAttachmentDropdownSelected(type);
    } else {
      if (fieldId === '-1') {
        if (type !== '') {
          if (
            !isUndefined(fieldDesignerState.fieldValue) &&
            fieldDesignerState.fieldValue !== '' &&
            !fieldDesignerState.addFieldCallSent
          ) {
            dispatchFieldDesigner({ type: 'SET_ADD_FIELD_SENT', payload: true });
            onFieldAdd({
              type: parseGeospatialTypes(type.fieldType),
              pk: geometricTypes.includes(type.fieldType.toUpperCase()) ? false : fieldDesignerState.fieldPKValue
            });
          }
        }
      } else {
        if (type !== '' && type !== fieldDesignerState.fieldValue) {
          fieldUpdate({
            codelistItems: null,
            pk: geometricTypes.includes(type.fieldType.toUpperCase()) ? false : fieldDesignerState.fieldPKValue,
            type: parseGeospatialTypes(type.fieldType),
            isLinkChange: areEquals(fieldDesignerState.fieldTypeValue.fieldType, 'LINK')
          });
        } else {
          if (type !== '') {
            fieldTypeRef.current.hide();
            onShowDialogError(
              resourcesContext.messages['emptyFieldTypeMessage'],
              resourcesContext.messages['emptyFieldTypeTitle'],
              inputRef?.current?.element
            );
          }
        }
      }
      dispatchFieldDesigner({ type: 'RESET_FIELD' });
      if (geometricTypes.includes(type.fieldType.toUpperCase()))
        dispatchFieldDesigner({ type: 'SET_PK', payload: false });
    }
    onCodelistAndLinkShow(fieldId, type);
  };

  const onBlurFieldDescription = description => {
    if (!isUndefined(description)) {
      if (!fieldDesignerState.isDragging) {
        //New field
        if (fieldId === '-1') {
          if (
            !isNil(fieldDesignerState.fieldTypeValue) &&
            (fieldDesignerState.fieldTypeValue !== '') & !isNil(fieldDesignerState.fieldValue) &&
            fieldDesignerState.fieldValue !== ''
          ) {
            onFieldAdd({ description: description });
          }
        } else {
          if (description !== fieldDesignerState.initialDescriptionValue) {
            fieldUpdate({ description });
          }
        }
      }
    }
  };

  const onBlurFieldName = name => {
    if (!isUndefined(name)) {
      if (!fieldDesignerState.isDragging) {
        if (fieldId === '-1') {
          if (
            name === '' &&
            fieldDesignerState.fieldTypeValue !== '' &&
            !isUndefined(fieldDesignerState.fieldTypeValue)
          ) {
            fieldTypeRef.current.hide();
            onShowDialogError(
              resourcesContext.messages['emptyFieldMessage'],
              resourcesContext.messages['emptyFieldTitle'],
              inputRef?.current?.element
            );
          } else {
            if (checkInvalidCharacters(name)) {
              fieldTypeRef.current.hide();
              onShowDialogError(
                resourcesContext.messages['invalidCharactersFieldMessage'],
                resourcesContext.messages['invalidCharactersFieldTitle'],
                inputRef?.current?.element
              );
              dispatchFieldDesigner({ type: 'SET_NAME', payload: fieldDesignerState.initialFieldValue });
            } else {
              if (!checkDuplicates(name, fieldId)) {
                if (!isNil(fieldDesignerState.fieldTypeValue) && fieldDesignerState.fieldTypeValue !== '') {
                  onFieldAdd({ name });
                }
              } else {
                fieldTypeRef.current.hide();
                onShowDialogError(
                  resourcesContext.messages['duplicatedFieldMessage'],
                  resourcesContext.messages['duplicatedFieldTitle'],
                  inputRef?.current?.element
                );
                dispatchFieldDesigner({ type: 'SET_NAME', payload: fieldDesignerState.initialFieldValue });
              }
            }
          }
        } else {
          if (name === '') {
            fieldTypeRef.current.hide();
            onShowDialogError(
              resourcesContext.messages['emptyFieldMessage'],
              resourcesContext.messages['emptyFieldTitle'],
              inputRef?.current?.element
            );
            dispatchFieldDesigner({ type: 'SET_NAME', payload: fieldDesignerState.initialFieldValue });
          } else {
            if (name !== fieldDesignerState.initialFieldValue) {
              if (checkInvalidCharacters(name)) {
                fieldTypeRef.current.hide();
                onShowDialogError(
                  resourcesContext.messages['invalidCharactersFieldMessage'],
                  resourcesContext.messages['invalidCharactersFieldTitle'],
                  inputRef?.current?.element
                );
                dispatchFieldDesigner({ type: 'SET_NAME', payload: fieldDesignerState.initialFieldValue });
              } else {
                if (!checkDuplicates(name, fieldId)) {
                  fieldUpdate({ name });
                } else {
                  fieldTypeRef.current.hide();
                  onShowDialogError(
                    resourcesContext.messages['duplicatedFieldMessage'],
                    resourcesContext.messages['duplicatedFieldTitle'],
                    inputRef?.current?.element
                  );
                  dispatchFieldDesigner({ type: 'SET_NAME', payload: fieldDesignerState.initialFieldValue });
                }
              }
            }
          }
        }
      }
    }
  };

  const onCancelSaveAttachment = () => {
    if (!isUndefined(fieldId)) {
      if (fieldId.toString() === '-1') {
        if (!isUndefined(fieldDesignerState.fieldValue) && fieldDesignerState.fieldValue !== '') {
          onFieldAdd({ validExtensions: fieldFileProperties.validExtensions, maxSize: fieldFileProperties.maxSize });
        }
      }
    }
    dispatchFieldDesigner({ type: 'CANCEL_SELECT_ATTACHMENT' });
  };

  const onCancelSaveLink = ({
    link,
    linkedTableConditional,
    linkedTableLabel,
    masterTableConditional,
    pkHasMultipleValues,
    pkMustBeUsed
  }) => {
    const inmReferencedField = { ...link.referencedField };
    if (linkedTableConditional !== '') {
      inmReferencedField.linkedTableConditional = linkedTableConditional;
    }
    if (linkedTableLabel !== '') {
      inmReferencedField.linkedTableLabel = linkedTableLabel;
    }
    if (masterTableConditional !== '') {
      inmReferencedField.masterTableConditional = masterTableConditional;
    }
    if (!isUndefined(fieldId)) {
      if (fieldId.toString() === '-1') {
        if (!isUndefined(fieldDesignerState.fieldValue) && fieldDesignerState.fieldValue !== '') {
          onFieldAdd({
            codelistItems,
            type: areEquals(fieldDesignerState.fieldTypeValue.fieldType, 'external_link') ? 'EXTERNAL_LINK' : 'LINK',
            referencedField: {
              ...link,
              referencedField: inmReferencedField
            },
            pkMustBeUsed,
            pkHasMultipleValues
          });
        }
      }
    }
    dispatchFieldDesigner({ type: 'CANCEL_SELECT_LINK' });
  };

  const onCancelSaveCodelist = () => {
    if (!isUndefined(fieldId)) {
      if (fieldId.toString() === '-1') {
        if (!isUndefined(fieldDesignerState.fieldValue) && fieldDesignerState.fieldValue !== '') {
          onFieldAdd({ codelistItems });
        }
      }
    }
    dispatchFieldDesigner({ type: 'CANCEL_SELECT_CODELIST' });
  };

  const onCodelistDropdownSelected = fieldType => {
    if (!isUndefined(fieldType)) {
      onCodelistAndLinkShow(fieldId, fieldType);
    }
    if (!fieldDesignerState.addFieldCallSent) {
      dispatchFieldDesigner({ type: 'TOGGLE_CODELIST_EDITOR_VISIBLE', payload: true });
    }
  };

  const onLinkDropdownSelected = fieldType => {
    if (
      !isNil(fieldType) &&
      !isNil(fieldDesignerState.fieldPreviousTypeValue) &&
      !areEquals(fieldType, fieldDesignerState.fieldPreviousTypeValue.fieldType)
    ) {
      dispatchFieldDesigner({ type: 'RESET_REFERENCED_FIELD' });
    }
    if (!isUndefined(fieldType)) {
      onCodelistAndLinkShow(fieldId, fieldType);
    }
    if (!fieldDesignerState.addFieldCallSent) {
      dispatchFieldDesigner({ type: 'TOGGLE_LINK_SELECTOR_VISIBLE', payload: true });
    }
  };

  const onFieldAdd = async ({
    codelistItems = fieldDesignerState.codelistItems,
    description = fieldDesignerState.fieldDescriptionValue,
    maxSize = fieldDesignerState.fieldFileProperties.maxSize,
    pk = fieldDesignerState.fieldPKValue,
    pkHasMultipleValues = fieldDesignerState.pkHasMultipleValues,
    pkMustBeUsed = fieldDesignerState.pkMustBeUsed,
    name = fieldDesignerState.fieldValue,
    readOnly = fieldDesignerState.fieldReadOnlyValue,
    recordId = recordSchemaId,
    referencedField = fieldDesignerState.fieldLinkValue,
    required = fieldDesignerState.fieldRequiredValue,
    type = parseGeospatialTypes(fieldDesignerState.fieldTypeValue.fieldType),
    validExtensions = fieldDesignerState.fieldFileProperties.validExtensions,
    isDuplicated = false
  }) => {
    try {
      setIsLoading(true);
      const response = await DatasetService.createRecordDesign(datasetId, {
        codelistItems,
        description,
        maxSize,
        pk,
        pkHasMultipleValues,
        pkMustBeUsed,
        name,
        readOnly,
        recordId,
        referencedField: !isNil(referencedField)
          ? parseReferenceField(referencedField)
          : fieldDesignerState.fieldLinkValue,
        required,
        type,
        validExtensions
      });
      if (!isDuplicated) {
        dispatchFieldDesigner({ type: 'RESET_NEW_FIELD' });
      }
      onNewFieldAdd({
        codelistItems,
        description,
        fieldId: response.data,
        fieldLinkValue: null,
        maxSize,
        name,
        pk,
        pkHasMultipleValues,
        pkMustBeUsed,
        readOnly,
        recordId,
        referencedField: !isNil(referencedField)
          ? parseReferenceField(referencedField)
          : fieldDesignerState.fieldLinkValue,
        required,
        type,
        validExtensions
      });
    } catch (error) {
      console.error('FieldDesigner - onFieldAdd.', error);
      if (error?.response.status === 400) {
        if (error.response?.data?.message?.includes('name invalid')) {
          notificationContext.add({
            type: 'DATASET_SCHEMA_FIELD_INVALID_NAME',
            content: { fieldName: name }
          });
        }
      }
    } finally {
      if (!isNil(inputRef.current)) {
        if (index === '-1') {
          inputRef.current.element.focus();
        }
      }
      dispatchFieldDesigner({ type: 'SET_ADD_FIELD_SENT', payload: false });
      setIsLoading(false);
    }
  };

  const onFieldDragDrop = event => {
    if (!isUndefined(initialFieldIndexDragged)) {
      //Get the dragged field
      //currentTarget gets the child's target parent
      const childs = event.currentTarget.childNodes;
      for (let i = 0; i < childs.length; i++) {
        if (childs[i].nodeName === 'INPUT') {
          if (!isUndefined(onFieldDragAndDrop)) {
            onFieldDragAndDrop(initialFieldIndexDragged, childs[i].value);
            dispatchFieldDesigner({ type: 'TOGGLE_IS_DRAGGING', payload: false });
          }
        }
      }
    }
  };

  const onFieldDragEnd = () => {
    if (!isUndefined(onFieldDragAndDropStart)) {
      onFieldDragAndDropStart(undefined);
    }
    dispatchFieldDesigner({ type: 'TOGGLE_IS_DRAGGING', payload: false });
  };

  const onFieldDragEnter = event => {
    event.dataTransfer.dropEffect = 'copy';
  };

  const onFieldDragLeave = event => {
    if (!isUndefined(initialFieldIndexDragged)) {
      if (event.currentTarget.tabIndex !== initialFieldIndexDragged) {
        dispatchFieldDesigner({ type: 'TOGGLE_IS_DRAGGING', payload: false });
      }
    }
  };

  const onFieldDragOver = () => {
    if (!isUndefined(initialFieldIndexDragged)) {
      if (index !== initialFieldIndexDragged) {
        if (!fieldDesignerState.isDragging) {
          if (
            (index === '-1' && totalFields - initialFieldIndexDragged !== 1) ||
            (index !== '-1' && initialFieldIndexDragged - index !== -1)
          ) {
            dispatchFieldDesigner({ type: 'TOGGLE_IS_DRAGGING', payload: true });
          }
        }
      }
    }
  };

  const onFieldDragStart = event => {
    if (fieldDesignerState.isEditing) {
      event.preventDefault();
    }
    //Needed the setData for Firefox
    event.dataTransfer.setData('text/plain', null);
    if (!isUndefined(onFieldDragAndDropStart)) {
      onFieldDragAndDropStart(index);
    }
  };

  const onKeyChange = (event, input) => {
    if (event.key === 'Escape') {
      input === 'NAME'
        ? dispatchFieldDesigner({ type: 'SET_NAME', payload: fieldDesignerState.initialFieldValue })
        : dispatchFieldDesigner({ type: 'SET_DESCRIPTION', payload: fieldDesignerState.initialDescriptionValue });
    } else if (event.key === 'Enter') {
      if (input === 'NAME') {
        onBlurFieldName(event.target.value.trim());
      }
    }
  };

  const onPKChange = checked => {
    if (!fieldDesignerState.isDragging) {
      if (fieldId === '-1') {
        if (validField()) {
          onFieldAdd({ pk: checked });
        }
      } else {
        fieldUpdate({ pk: checked, required: checked ? true : fieldDesignerState.fieldRequiredValue });
      }
    }
    dispatchFieldDesigner({ type: 'SET_PK', payload: checked });
  };

  const onReadOnlyChange = checked => {
    if (!fieldDesignerState.isDragging) {
      if (fieldId === '-1') {
        if (validField()) {
          onFieldAdd({ readOnly: checked });
        }
      } else {
        fieldUpdate({ readOnly: checked });
      }
    }
    dispatchFieldDesigner({ type: 'SET_READONLY', payload: checked });
  };

  const onRequiredChange = checked => {
    if (!fieldDesignerState.isDragging) {
      if (fieldId === '-1') {
        if (validField()) {
          onFieldAdd({ required: checked });
        }
      } else {
        fieldUpdate({ required: checked });
      }
    }
    dispatchFieldDesigner({ type: 'SET_REQUIRED', payload: checked });
  };

  const onSaveAttachment = fileProperties => {
    dispatchFieldDesigner({
      type: 'SET_ATTACHMENT_PROPERTIES',
      payload: { validExtensions: fileProperties.validExtensions, maxSize: fileProperties.maxSize }
    });
    if (fieldDesignerState.fieldValue === '') {
      fieldTypeRef.current.hide();
      onShowDialogError(
        resourcesContext.messages['emptyFieldMessage'],
        resourcesContext.messages['emptyFieldTitle'],
        inputRef?.current?.element
      );
    } else {
      if (!isUndefined(fieldId)) {
        if (fieldId.toString() === '-1') {
          onFieldAdd({ validExtensions: fileProperties.validExtensions, maxSize: fileProperties.maxSize });
        } else {
          fieldUpdate({ validExtensions: fileProperties.validExtensions, maxSize: fileProperties.maxSize });
        }
        dispatchFieldDesigner({ type: 'TOGGLE_ATTACHMENT_EDITOR_VISIBLE', payload: false });
      }
    }
  };

  const onSaveCodelist = codelistItems => {
    dispatchFieldDesigner({ type: 'SET_CODELIST_ITEMS', payload: codelistItems });
    if (fieldDesignerState.fieldValue === '') {
      fieldTypeRef.current.hide();
      onShowDialogError(
        resourcesContext.messages['emptyFieldMessage'],
        resourcesContext.messages['emptyFieldTitle'],
        inputRef?.current?.element
      );
    } else {
      if (!isUndefined(fieldId)) {
        if (fieldId.toString() === '-1') {
          onFieldAdd({ codelistItems });
        } else {
          fieldUpdate({ codelistItems });
        }
        dispatchFieldDesigner({ type: 'TOGGLE_CODELIST_EDITOR_VISIBLE', payload: false });
      }
    }
  };

  const onSaveLink = ({
    link,
    linkedTableConditional,
    linkedTableLabel,
    masterTableConditional,
    pkHasMultipleValues,
    pkMustBeUsed
  }) => {
    const inmReferencedField = { ...link.referencedField };
    if (linkedTableConditional !== '') {
      inmReferencedField.linkedTableConditional = linkedTableConditional;
    }
    if (linkedTableLabel !== '') {
      inmReferencedField.linkedTableLabel = linkedTableLabel;
    }
    if (masterTableConditional !== '') {
      inmReferencedField.masterTableConditional = masterTableConditional;
    }
    dispatchFieldDesigner({
      type: 'SET_LINK',
      payload: {
        link: {
          ...link,
          referencedField: inmReferencedField
        },
        pkMustBeUsed,
        pkHasMultipleValues
      }
    });
    if (fieldDesignerState.fieldValue === '') {
      fieldTypeRef.current.hide();
      onShowDialogError(
        resourcesContext.messages['emptyFieldMessage'],
        resourcesContext.messages['emptyFieldTitle'],
        inputRef?.current?.element
      );
    } else {
      if (!isUndefined(fieldId)) {
        if (fieldId.toString() === '-1') {
          onFieldAdd({
            codelistItems,
            type: areEquals(fieldDesignerState.fieldTypeValue.fieldType, 'external_link') ? 'EXTERNAL_LINK' : 'LINK',
            referencedField: {
              ...link,
              referencedField: inmReferencedField
            },
            pkMustBeUsed,
            pkHasMultipleValues
          });
        } else {
          fieldUpdate({
            codelistItems,
            isLinkChange: true,
            type: areEquals(fieldDesignerState.fieldTypeValue.fieldType, 'external_link') ? 'EXTERNAL_LINK' : 'LINK',
            referencedField: {
              ...link,
              referencedField: inmReferencedField
            },
            pkMustBeUsed,
            pkHasMultipleValues
          });
        }
        dispatchFieldDesigner({ type: 'TOGGLE_LINK_SELECTOR_VISIBLE', payload: false });
      }
    }
  };

  const parseGeospatialTypes = value => value.toUpperCase();

  const fieldTypeTemplate = option => {
    if (!option.value) {
      return option.label;
    } else {
      return (
        <div className="p-clearfix">
          <FontAwesomeIcon icon={AwesomeIcons(option.fieldTypeIcon)} role="presentation" />
          <span style={{ margin: '.5em .25em 0 0.5em' }}>{option.value}</span>
        </div>
      );
    }
  };

  const fieldUpdate = async ({
    codelistItems = fieldDesignerState.codelistItems,
    description = fieldDesignerState.fieldDescriptionValue,
    fieldSchemaId = fieldId,
    isLinkChange = false,
    maxSize = fieldDesignerState.fieldFileProperties.maxSize,
    pk = fieldDesignerState.fieldPKValue,
    pkHasMultipleValues = fieldDesignerState.pkHasMultipleValues,
    pkMustBeUsed = fieldDesignerState.pkMustBeUsed,
    name = fieldDesignerState.fieldValue,
    readOnly = fieldDesignerState.fieldReadOnlyValue,
    recordId = recordSchemaId,
    referencedField = fieldDesignerState.fieldLinkValue,
    required = fieldDesignerState.fieldRequiredValue,
    type = parseGeospatialTypes(fieldDesignerState.fieldTypeValue.fieldType),
    validExtensions = fieldDesignerState.fieldFileProperties.validExtensions
  }) => {
    try {
      setIsLoading(true);
      await DatasetService.updateFieldDesign(datasetId, {
        codelistItems,
        description,
        fieldSchemaId,
        maxSize,
        pk,
        pkHasMultipleValues,
        pkMustBeUsed,
        name,
        readOnly,
        recordId,
        referencedField:
          areEquals(type, 'LINK') || areEquals(type, 'EXTERNAL_LINK')
            ? !isNil(referencedField)
              ? parseReferenceField(referencedField)
              : fieldDesignerState.fieldLinkValue
            : null,
        required,
        type,
        validExtensions
      });

      onFieldUpdate({
        codelistItems,
        description,
        id: fieldId,
        isLinkChange,
        maxSize,
        pk,
        pkHasMultipleValues,
        pkMustBeUsed,
        name,
        readOnly,
        recordId,
        referencedField:
          areEquals(type, 'LINK') || areEquals(type, 'EXTERNAL_LINK')
            ? !isNil(referencedField)
              ? parseReferenceField(referencedField)
              : fieldDesignerState.fieldLinkValue
            : null,
        required,
        type,
        validExtensions
      });
    } catch (error) {
      console.error('FieldDesigner - fieldUpdate.', error);
      if (error?.response.status === 400) {
        if (error.response?.data?.message?.includes('name invalid')) {
          notificationContext.add({
            type: 'DATASET_SCHEMA_FIELD_INVALID_NAME',
            content: { fieldName: name }
          });
        }
      }
    } finally {
      setIsLoading(false);
    }
  };

  const parseReferenceField = completeReferencedField => {
    return {
      dataflowId: completeReferencedField.referencedField.dataflowId,
      fieldSchemaName: completeReferencedField.referencedField.fieldSchemaName,
      idDatasetSchema: completeReferencedField.referencedField.datasetSchemaId,
      idPk: completeReferencedField.referencedField.fieldSchemaId,
      labelId: completeReferencedField.referencedField.linkedTableLabel,
      linkedConditionalFieldId: completeReferencedField.referencedField.linkedTableConditional,
      masterConditionalFieldId: completeReferencedField.referencedField.masterTableConditional,
      tableSchemaName: completeReferencedField.referencedField.tableSchemaName
    };
  };

  const qcDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        className="p-button-secondary-transparent p-button-animated-blink button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={() => dispatchFieldDesigner({ type: 'TOGGLE_QC_MANAGER_VISIBLE', payload: false })}
      />
    </div>
  );

  const renderAttachmentEditor = () => {
    if (fieldDesignerState.isAttachmentEditorVisible) {
      return (
        <AttachmentEditor
          isAttachmentEditorVisible={fieldDesignerState.isAttachmentEditorVisible}
          onCancelSaveAttachment={onCancelSaveAttachment}
          onSaveAttachment={onSaveAttachment}
          selectedAttachment={fieldDesignerState.fieldFileProperties}
          type={fieldDesignerState.fieldTypeValue.value}
        />
      );
    }
  };

  const renderCheckboxes = () => (
    <div>
      {!addField ? (
        <FontAwesomeIcon
          aria-label={resourcesContext.messages['moveField']}
          icon={AwesomeIcons('move')}
          style={{ width: '32px', opacity: isDataflowOpen || isDesignDatasetEditorRead ? 0.5 : 1 }}
        />
      ) : (
        <div style={{ marginLeft: '32px', display: 'inline-block' }}></div>
      )}
      <Checkbox
        ariaLabel={resourcesContext.messages['pk']}
        checked={fieldDesignerState.fieldPKValue}
        className={`${styles.checkPK} datasetSchema-pk-help-step ${
          fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
        } ${isDataflowOpen && isDesignDatasetEditorRead && styles.checkboxDisabled}`}
        disabled={
          (!isNil(fieldDesignerState.fieldTypeValue) &&
            !isNil(fieldDesignerState.fieldTypeValue.fieldType) &&
            geometricTypes.includes(fieldDesignerState.fieldTypeValue.fieldType.toUpperCase())) ||
          (hasPK && (!fieldDesignerState.fieldPKValue || fieldDesignerState.fieldPKReferencedValue)) ||
          isDataflowOpen ||
          isDesignDatasetEditorRead ||
          isLoading
        }
        id={`${fieldId}_check_pk`}
        inputId={`${fieldId}_check_pk`}
        label="Default"
        onChange={e => {
          if (!(hasPK && (!fieldDesignerState.fieldPKValue || fieldDesignerState.fieldPKReferencedValue))) {
            onPKChange(e.checked);
          }
        }}
        tooltip={renderTooltipPK()}
        tooltipOptions={{ position: 'top' }}
      />
      <Checkbox
        ariaLabel={resourcesContext.messages['required']}
        checked={fieldDesignerState.fieldRequiredValue}
        className={`${styles.checkRequired} datasetSchema-required-help-step ${
          fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
        } ${isDataflowOpen && isDesignDatasetEditorRead && styles.checkboxDisabled}`}
        disabled={Boolean(fieldDesignerState.fieldPKValue) || isDataflowOpen || isDesignDatasetEditorRead || isLoading}
        id={`${fieldId}_check_required`}
        inputId={`${fieldId}_check_required`}
        label="Default"
        onChange={e => {
          onRequiredChange(e.checked);
        }}
        tooltip={renderTooltipRequired()}
        tooltipOptions={{ position: 'top' }}
      />
      <Checkbox
        ariaLabel={resourcesContext.messages['readOnly']}
        checked={fieldDesignerState.fieldReadOnlyValue}
        className={`${styles.checkReadOnly} datasetSchema-readOnly-help-step ${
          fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
        } ${isDataflowOpen && isDesignDatasetEditorRead && styles.checkboxDisabled}`}
        disabled={isDataflowOpen || isDesignDatasetEditorRead || isLoading}
        id={`${fieldId}_check_readOnly`}
        inputId={`${fieldId}_check_readOnly`}
        label="Default"
        onChange={e => onReadOnlyChange(e.checked)}
      />
    </div>
  );

  const renderCodelistFileAndLinkButtons = () => {
    if (
      !isUndefined(fieldDesignerState.fieldTypeValue) &&
      (fieldDesignerState.fieldTypeValue.fieldType === 'Codelist' ||
        fieldDesignerState.fieldTypeValue.fieldType === 'Multiselect_Codelist')
    ) {
      return (
        <Button
          className={`${styles.codelistButton} p-button-secondary-transparent ${
            fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
          }`}
          disabled={isDataflowOpen || isDesignDatasetEditorRead}
          label={
            !isUndefined(fieldDesignerState.codelistItems) && !isEmpty(fieldDesignerState.codelistItems)
              ? `${fieldDesignerState.codelistItems.join('; ')}`
              : fieldDesignerState.fieldTypeValue.fieldType === 'Codelist'
              ? resourcesContext.messages['codelistSelection']
              : resourcesContext.messages['multiselectCodelistSelection']
          }
          onClick={onCodelistDropdownSelected}
          style={{ pointerEvents: 'auto' }}
          tooltip={renderTooltipCodelist()}
          tooltipOptions={{ position: 'top' }}
        />
      );
    }
    if (
      !isUndefined(fieldDesignerState.fieldTypeValue) &&
      (fieldDesignerState.fieldTypeValue.fieldType === 'Link' ||
        fieldDesignerState.fieldTypeValue.fieldType === 'External_link')
    ) {
      return (
        <Button
          className={`${styles.codelistButton} p-button-secondary-transparent ${
            fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
          }`}
          disabled={
            isDataflowOpen ||
            isDesignDatasetEditorRead ||
            (!isNil(fieldDesignerState.fieldLinkValue) &&
              !isEmpty(fieldDesignerState.fieldLinkValue) &&
              isNil(fieldDesignerState.fieldLinkValue.name))
          }
          icon={
            isNil(fieldDesignerState.fieldLinkValue) || isEmpty(fieldDesignerState.fieldLinkValue)
              ? null
              : isNil(fieldDesignerState.fieldLinkValue.name)
              ? 'spinnerAnimate'
              : null
          }
          label={
            isNil(fieldDesignerState.fieldLinkValue) || isEmpty(fieldDesignerState.fieldLinkValue)
              ? resourcesContext.messages['linkSelection']
              : isNil(fieldDesignerState.fieldLinkValue.name)
              ? '...'
              : `${fieldDesignerState.fieldLinkValue.name}`
          }
          onClick={onLinkDropdownSelected}
          style={{ pointerEvents: 'auto' }}
          tooltip={renderTooltipLink()}
          tooltipOptions={{ position: 'top' }}
        />
      );
    }
    if (
      !isUndefined(fieldDesignerState.fieldTypeValue) &&
      fieldDesignerState.fieldTypeValue.fieldType === 'Attachment'
    ) {
      return (
        <Button
          className={`${styles.codelistButton} p-button-secondary-transparent ${
            fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
          }`}
          disabled={isDataflowOpen || isDesignDatasetEditorRead}
          label={`${resourcesContext.messages['validExtensions']} ${
            !isUndefined(fieldDesignerState.fieldFileProperties.validExtensions) &&
            !isEmpty(fieldDesignerState.fieldFileProperties.validExtensions)
              ? fieldDesignerState.fieldFileProperties.validExtensions.join(', ')
              : '*'
          } - ${resourcesContext.messages['maxFileSize']} ${fieldDesignerState.fieldFileProperties.maxSize} ${
            resourcesContext.messages['MB']
          }`}
          onClick={onAttachmentDropdownSelected}
          style={{ pointerEvents: 'auto' }}
          tooltip={renderTooltipAttachment()}
          tooltipOptions={{ position: 'top' }}
        />
      );
    }
    if (isCodelistOrLink) {
      return (
        <span
          className={fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive}
          style={{ width: '4rem', marginRight: '0.4rem' }}></span>
      );
    }
  };

  const renderDeleteButton = () => {
    if (!addField) {
      if (!bulkDelete) {
        return (
          <div
            className={`${styles.button} ${styles.deleteButton} ${
              fieldPKReferenced ? styles.disabledDeleteButton : ''
            } ${fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive} ${
              isDataflowOpen || isDesignDatasetEditorRead ? styles.linkDisabled : ''
            }`}
            draggable={true}
            href="#"
            onClick={e => {
              e.preventDefault();
              onFieldDelete(index, fieldDesignerState.fieldTypeValue.fieldType);
            }}
            onDragStart={event => {
              event.preventDefault();
              event.stopPropagation();
            }}>
            <FontAwesomeIcon aria-label={resourcesContext.messages['deleteFieldLabel']} icon={AwesomeIcons('delete')} />
            <span className="srOnly">{resourcesContext.messages['deleteFieldLabel']}</span>
          </div>
        );
      } else {
        return (
          <Checkbox
            checked={markedForDeletion.some(markedField => markedField.fieldId === fieldId)}
            className={`${styles.checkBulkDelete} ${
              fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
            } ${isDataflowOpen && isDesignDatasetEditorRead && styles.checkboxDisabled}`}
            disabled={fieldPKReferenced || isDataflowOpen || isDesignDatasetEditorRead || isLoading}
            id={`${fieldDesignerState.fieldValue}_mark_to_delete`}
            inputId={`${fieldDesignerState.fieldValue}_mark_to_delete`}
            onChange={e => {
              if (e.originalEvent.shiftKey && markedForDeletion.length > 0) {
                const idx = FieldsDesignerUtils.getIndexByFieldId(fieldId, fields);
                const lastMarkedFieldIdx =
                  markedForDeletion.length > 0 ? markedForDeletion[markedForDeletion.length - 1].fieldIndex : -1;
                if (lastMarkedFieldIdx !== -1) {
                  const initIdx = idx > lastMarkedFieldIdx ? lastMarkedFieldIdx : idx;
                  const lastIdx = idx > lastMarkedFieldIdx ? idx : lastMarkedFieldIdx;
                  const fieldsSelected = [
                    {
                      checked: true,
                      fieldId,
                      fieldType: fieldDesignerState.fieldTypeValue,
                      fieldName: fieldDesignerState.fieldValue,
                      fieldIndex: index
                    }
                  ];
                  for (let i = initIdx; i <= lastIdx; i++) {
                    if (!fieldsSelected.some(markedField => markedField.fieldId === fields[i].fieldId)) {
                      fieldsSelected.push({
                        checked: true,
                        fieldId: fields[i].fieldId,
                        fieldType: RecordUtils.getFieldTypeValue(fields[i].type)?.value,
                        fieldName: fields[i].name,
                        fieldIndex: i
                      });
                    }
                  }
                  onBulkCheck({ fieldsSelected, multiple: true });
                }
              } else {
                onBulkCheck({
                  checked: e.checked,
                  fieldId,
                  fieldType: fieldDesignerState.fieldTypeValue,
                  fieldName: fieldDesignerState.fieldValue,
                  fieldIndex: index
                });
              }
            }}
            role="checkbox"
          />
        );
      }
    }
  };
  const renderDuplicateButton = () => {
    if (!addField) {
      return (
        <div
          className={`${styles.button} ${styles.duplicateButton} ${
            fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
          } ${isDataflowOpen || isLoading || isDesignDatasetEditorRead ? styles.linkDisabled : ''}`}
          data-for={fieldDesignerState.fieldValue}
          data-tip
          href="#"
          onClick={e => {
            e.preventDefault();
            onFieldAdd({
              codelistItems: fieldDesignerState.codelistItems,
              description: fieldDesignerState.fieldDescriptionValue,
              isDuplicated: true,
              maxSize: fieldDesignerState.fieldFileProperties.maxSize,
              pk: false,
              pkHasMultipleValues: fieldDesignerState.fieldPkHasMultipleValues,
              pkMustBeUsed: fieldDesignerState.fieldPkMustBeUsed,
              name: getDuplicatedName(),
              readOnly: fieldDesignerState.fieldReadOnlyValue,
              recordId: recordSchemaId,
              referencedField: fieldDesignerState.completeLink,
              required: fieldDesignerState.fieldRequiredValue,
              type: parseGeospatialTypes(fieldDesignerState.fieldTypeValue.fieldType),
              validExtensions: fieldDesignerState.fieldFileProperties.validExtensions
            });
          }}>
          <FontAwesomeIcon aria-label={resourcesContext.messages['duplicate']} icon={AwesomeIcons('clone')} />
          <span className="srOnly">{resourcesContext.messages['duplicate']}</span>
          <ReactTooltip border={true} effect="solid" id={fieldDesignerState.fieldValue} place="top">
            {resourcesContext.messages['duplicate']}
          </ReactTooltip>
        </div>
      );
    }
  };

  const renderInputs = () => (
    <Fragment>
      <InputText
        autoFocus={false}
        className={`${styles.inputField} ${isCodelistOrLink ? styles.withCodeListOrLink : ''} ${
          fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
        }`}
        disabled={isDataflowOpen || isDesignDatasetEditorRead || isLoading}
        id={fieldName !== '' ? fieldName : 'newField'}
        keyfilter="schemaTableFields"
        maxLength={60}
        name={resourcesContext.messages['newFieldPlaceHolder']}
        // key={`${fieldId}_${index}`} --> Problem with DOM modification
        onBlur={e => {
          dispatchFieldDesigner({ type: 'TOGGLE_IS_EDITING', payload: false });
          onBlurFieldName(e.target.value.trim());
          dispatchFieldDesigner({ type: 'SET_NAME', payload: e.target.value.trim() });
        }}
        onChange={e => dispatchFieldDesigner({ type: 'SET_NAME', payload: e.target.value })}
        onFocus={e => {
          if (
            e.target.value.trim() !== '' &&
            !checkDuplicates(e.target.value.trim(), fieldId) &&
            !checkInvalidCharacters(e.target.value.trim())
          ) {
            dispatchFieldDesigner({ type: 'SET_INITIAL_FIELD_VALUE', payload: e.target.value.trim() });
          }
          dispatchFieldDesigner({ type: 'TOGGLE_IS_EDITING', payload: true });
        }}
        onKeyDown={e => onKeyChange(e, 'NAME')}
        placeholder={resourcesContext.messages['newFieldPlaceHolder']}
        ref={inputRef}
        required={!isUndefined(fieldDesignerState.fieldValue) ? fieldDesignerState.fieldValue === '' : fieldName === ''}
        value={!isUndefined(fieldDesignerState.fieldValue) ? fieldDesignerState.fieldValue : fieldName}
      />
      <InputTextarea
        autoFocus={false}
        className={`${styles.inputFieldDescription} ${
          fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
        }`}
        collapsedHeight={33}
        disabled={isDataflowOpen || isDesignDatasetEditorRead || isLoading}
        expandableOnClick={true}
        id={`${fieldName}_description`}
        key={fieldId}
        onBlur={e => {
          dispatchFieldDesigner({ type: 'TOGGLE_IS_EDITING', payload: false });
          onBlurFieldDescription(e.target.value);
        }}
        onChange={e => dispatchFieldDesigner({ type: 'SET_DESCRIPTION', payload: e.target.value })}
        onFocus={e => {
          dispatchFieldDesigner({ type: 'SET_INITIAL_FIELD_DESCRIPTION', payload: e.target.value });
          dispatchFieldDesigner({ type: 'TOGGLE_IS_EDITING', payload: true });
        }}
        onKeyDown={e => onKeyChange(e, 'DESCRIPTION')}
        placeholder={resourcesContext.messages['newFieldDescriptionPlaceHolder']}
        value={
          !isUndefined(fieldDesignerState.fieldDescriptionValue)
            ? fieldDesignerState.fieldDescriptionValue
            : fieldDescription
        }
      />
      <Dropdown
        appendTo={document.body}
        ariaLabel={'fieldType'}
        className={`${styles.dropdownFieldType} ${isCodelistOrLink ? styles.withCodeListOrLink : ''} ${
          fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
        }`}
        disabled={isDataflowOpen || isDesignDatasetEditorRead || isLoading}
        inputId={`${fieldName}_fieldType`}
        itemTemplate={fieldTypeTemplate}
        name={resourcesContext.messages['newFieldTypePlaceHolder']}
        onChange={e => onChangeFieldType(e.target.value)}
        onMouseDown={event => {
          event.preventDefault();
          onSetInitHeaderHeight();
          event.stopPropagation();
        }}
        optionLabel="value"
        options={config.fieldType}
        placeholder={resourcesContext.messages['newFieldTypePlaceHolder']}
        ref={fieldTypeRef}
        required={true}
        scrollHeight="450px"
        style={{ alignSelf: !fieldDesignerState.isEditing ? 'center' : 'auto', display: 'block' }}
        value={
          fieldDesignerState.fieldTypeValue !== ''
            ? fieldDesignerState.fieldTypeValue
            : RecordUtils.getFieldTypeValue(fieldType)
        }
      />
    </Fragment>
  );

  const renderLinkSelector = () => {
    if (fieldDesignerState.isLinkSelectorVisible) {
      return (
        <LinkSelector
          datasetSchemaId={datasetSchemaId}
          fieldId={fieldId}
          fields={fields}
          hasMultipleValues={fieldDesignerState.fieldPkHasMultipleValues}
          isExternalLink={areEquals(fieldDesignerState.fieldTypeValue.fieldType, 'external_link') ? true : false}
          isLinkSelectorVisible={fieldDesignerState.isLinkSelectorVisible}
          isReferenceDataset={isReferenceDataset}
          linkedTableConditional={fieldLinkedTableConditional}
          linkedTableLabel={fieldLinkedTableLabel}
          masterTableConditional={fieldMasterTableConditional}
          mustBeUsed={fieldDesignerState.fieldPkMustBeUsed}
          onCancelSaveLink={onCancelSaveLink}
          onHideSelector={() => dispatchFieldDesigner({ type: 'CANCEL_SELECT_LINK' })}
          onSaveLink={onSaveLink}
          selectedLink={fieldDesignerState.fieldLinkValue}
          tableSchemaId={tableSchemaId}
        />
      );
    }
  };

  const renderQCButton = () => {
    if (!addField) {
      return (
        <Button
          className={`p-button-secondary-transparent button ${styles.qcButton} ${
            fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
          } ${
            !isUndefined(fieldDesignerState.fieldTypeValue) &&
            !config.validations.bannedFieldsNames.sqlFields.includes(
              fieldDesignerState.fieldTypeValue.value.toLowerCase()
            ) &&
            !isDesignDatasetEditorRead &&
            !(isDataflowOpen && isReferenceDataset)
              ? 'p-button-animated-blink'
              : null
          }`}
          disabled={
            (!isUndefined(fieldDesignerState.fieldTypeValue) &&
              config.validations.bannedFieldsNames.sqlFields.includes(
                fieldDesignerState.fieldTypeValue.value.toLowerCase()
              )) ||
            isDesignDatasetEditorRead ||
            (isDataflowOpen && isReferenceDataset)
          }
          icon="horizontalSliders"
          label={resourcesContext.messages['createFieldQC']}
          onClick={() => validationContext.onOpenModalFromField(fieldId, tableSchemaId)}
          style={{ marginLeft: '0.4rem', alignSelf: !fieldDesignerState.isEditing ? 'center' : 'baseline' }}
        />
      );
    }
  };

  const renderQCManager = () => {
    if (fieldDesignerState.isQCManagerVisible) {
      return (
        <Dialog
          blockScroll={false}
          contentStyle={{ overflow: 'auto' }}
          footer={qcDialogFooter}
          header={resourcesContext.messages['qcManager']}
          modal={true}
          onHide={() => dispatchFieldDesigner({ type: 'TOGGLE_QC_MANAGER_VISIBLE', payload: false })}
          style={{ width: '80%' }}
          visible={fieldDesignerState.isQCManagerVisible}
          zIndex={3003}>
          {}
        </Dialog>
      );
    }
  };

  const renderSingleMultipleSelector = () => {
    if (fieldDesignerState.isCodelistEditorVisible) {
      return (
        <CodelistEditor
          isCodelistEditorVisible={fieldDesignerState.isCodelistEditorVisible}
          onCancelSaveCodelist={onCancelSaveCodelist}
          onSaveCodelist={onSaveCodelist}
          selectedCodelist={fieldDesignerState.codelistItems}
          type={fieldDesignerState.fieldTypeValue.value}
        />
      );
    }
  };

  const renderTooltipAttachment = () => {
    return `${resourcesContext.messages['validExtensions']} ${
      !isUndefined(fieldDesignerState.fieldFileProperties.validExtensions) &&
      !isEmpty(fieldDesignerState.fieldFileProperties.validExtensions)
        ? fieldDesignerState.fieldFileProperties.validExtensions.join(', ')
        : '*'
    } - ${resourcesContext.messages['maxFileSize']} ${
      !isNil(fieldDesignerState.fieldFileProperties.maxSize) &&
      fieldDesignerState.fieldFileProperties.maxSize.toString() !== '0'
        ? `${fieldDesignerState.fieldFileProperties.maxSize} ${resourcesContext.messages['MB']}`
        : resourcesContext.messages['maxSizeNotDefined']
    }`;
  };

  const renderTooltipCodelist = () => {
    if (!isUndefined(fieldDesignerState.codelistItems) && !isEmpty(fieldDesignerState.codelistItems)) {
      return `${fieldDesignerState.codelistItems.join('; ')}`;
    }
    if (areEquals(fieldDesignerState.fieldTypeValue.fieldType, 'Codelist')) {
      return resourcesContext.messages['codelistSelection'];
    }
    return resourcesContext.messages['multiselectCodelistSelection'];
  };

  const renderTooltipPK = () => {
    if (
      !isNil(fieldDesignerState.fieldTypeValue) &&
      !isNil(fieldDesignerState.fieldTypeValue.fieldType) &&
      geometricTypes.includes(fieldDesignerState.fieldTypeValue.fieldType.toUpperCase())
    ) {
      return resourcesContext.messages['disabledPKGeom'];
    }
    if (hasPK && !fieldDesignerState.fieldPKValue) {
      return resourcesContext.messages['disabledPKHas'];
    }
    if (hasPK && fieldDesignerState.fieldPKReferencedValue) {
      return resourcesContext.messages['disabledPKLink'];
    }
    if (isDataflowOpen) {
      return resourcesContext.messages['disabledIsOpen'];
    }
    if (isDesignDatasetEditorRead) {
      return resourcesContext.messages['disabledEditorRead'];
    }
  };

  const renderTooltipLink = () => {
    if (isNil(fieldDesignerState.fieldLinkValue) || isEmpty(fieldDesignerState.fieldLinkValue)) {
      return resourcesContext.messages['linkSelection'];
    }
    if (isNil(fieldDesignerState.fieldLinkValue.name)) {
      return '...';
    }
    return `${fieldDesignerState.fieldLinkValue.name}`;
  };

  const renderTooltipRequired = () => {
    if (Boolean(fieldDesignerState.fieldPKValue)) {
      return resourcesContext.messages['disabledRequiredPK'];
    }
    if (isDataflowOpen) {
      return resourcesContext.messages['disabledIsOpen'];
    }
    if (isDesignDatasetEditorRead) {
      return resourcesContext.messages['disabledEditorRead'];
    }
  };

  return (
    <Fragment>
      <div
        className={`${styles.draggableFieldDiv} fieldRow datasetSchema-fieldDesigner-help-step`}
        draggable={isDataflowOpen || isDesignDatasetEditorRead ? false : !addField}
        onDragEnd={onFieldDragEnd}
        onDragEnter={onFieldDragEnter}
        onDragLeave={onFieldDragLeave}
        onDragOver={onFieldDragOver}
        onDragStart={onFieldDragStart}
        onDrop={onFieldDragDrop}
        style={{ cursor: isDataflowOpen || isDesignDatasetEditorRead ? 'default' : 'grab' }}>
        <div
          className={`${styles.fieldSeparator} ${
            fieldDesignerState.isDragging ? styles.fieldSeparatorDragging : ''
          }`}></div>

        {renderCheckboxes()}
        {renderInputs()}
        {renderCodelistFileAndLinkButtons()}
        {renderQCButton()}
        {renderDuplicateButton()}
        {renderDeleteButton()}
      </div>

      {renderSingleMultipleSelector()}
      {renderAttachmentEditor()}
      {renderLinkSelector()}
      {renderQCManager()}
    </Fragment>
  );
};
FieldDesigner.propTypes = {};
