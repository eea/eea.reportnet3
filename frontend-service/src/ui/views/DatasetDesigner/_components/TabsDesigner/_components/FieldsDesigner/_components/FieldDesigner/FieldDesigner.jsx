import React, { useContext, useEffect, useReducer, useRef, useState } from 'react';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './FieldDesigner.module.scss';

import { config } from 'conf';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { AttachmentEditor } from './_components/AttachmentEditor';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'primereact/checkbox';
import { CodelistEditor } from './_components/CodelistEditor';
import { Dialog } from 'ui/views/_components/Dialog';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { LinkSelector } from './_components/LinkSelector';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

import { fieldDesignerReducer } from './_functions/Reducers/fieldDesignerReducer';

import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';

export const FieldDesigner = ({
  addField = false,
  checkDuplicates,
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
  hasPK,
  index,
  initialFieldIndexDragged,
  isCodelistOrLink,
  isDataflowOpen,
  isDesignDatasetEditorRead,
  onCodelistAndLinkShow,
  onFieldDelete,
  onFieldDragAndDrop,
  onFieldDragAndDropStart,
  onFieldUpdate,
  onNewFieldAdd,
  onShowDialogError,
  recordSchemaId,
  tableSchemaId,
  totalFields
}) => {
  const fieldTypes = [
    { fieldType: 'Number_Integer', value: 'Number - Integer', fieldTypeIcon: 'number-integer' },
    { fieldType: 'Number_Decimal', value: 'Number - Decimal', fieldTypeIcon: 'number-decimal' },
    { fieldType: 'Date', value: 'Date', fieldTypeIcon: 'calendar' },
    { fieldType: 'Text', value: 'Text', fieldTypeIcon: 'italic' },
    { fieldType: 'Textarea', value: 'Multiline text', fieldTypeIcon: 'align-right' },
    { fieldType: 'Email', value: 'Email', fieldTypeIcon: 'email' },
    { fieldType: 'URL', value: 'URL', fieldTypeIcon: 'url' },
    { fieldType: 'Phone', value: 'Phone number', fieldTypeIcon: 'mobile' },
    // { fieldType: 'Boolean', value: 'Boolean', fieldTypeIcon: 'boolean' },
    { fieldType: 'Point', value: 'Point', fieldTypeIcon: 'point' },
    { fieldType: 'MultiPoint', value: 'Multiple points', fieldTypeIcon: 'multiPoint' },
    { fieldType: 'Linestring', value: 'Line', fieldTypeIcon: 'line' },
    { fieldType: 'MultiLineString', value: 'Multiple lines', fieldTypeIcon: 'multiLineString' },
    { fieldType: 'Polygon', value: 'Polygon', fieldTypeIcon: 'polygon' },
    { fieldType: 'MultiPolygon', value: 'Multiple polygons', fieldTypeIcon: 'multiPolygon' },
    // { fieldType: 'Circle', value: 'Circle', fieldTypeIcon: 'circle' },
    { fieldType: 'Codelist', value: 'Single select', fieldTypeIcon: 'list' },
    { fieldType: 'Multiselect_Codelist', value: 'Multiple select', fieldTypeIcon: 'multiselect' },
    { fieldType: 'Link', value: 'Link', fieldTypeIcon: 'link' },
    // { fieldType: 'Reference', value: 'Reference', fieldTypeIcon: 'link' }
    // { fieldType: 'URL', value: 'Url', fieldTypeIcon: 'url' },
    // { fieldType: 'RichText', value: 'Rich text', fieldTypeIcon: 'text' },
    // { fieldType: 'LinkData', value: 'Link to a data collection', fieldTypeIcon: 'linkData' },
    // { fieldType: 'Percentage', value: 'Percentage', fieldTypeIcon: 'percentage' },
    // { fieldType: 'Formula', value: 'Formula', fieldTypeIcon: 'formula' },
    // { fieldType: 'Fixed', value: 'Fixed select list', fieldTypeIcon: 'list' },
    { fieldType: 'Attachment', value: 'Attachment', fieldTypeIcon: 'clip' }
  ];

  const getFieldTypeValue = value => {
    return fieldTypes.filter(field => TextUtils.areEquals(field.fieldType, value))[0];
  };
  
  const initialFieldDesignerState = {
    addFieldCallSent: false,
    codelistItems: codelistItems,
    fieldDescriptionValue: fieldDescription,
    fieldLinkValue: fieldLink || null,
    fieldPkHasMultipleValues: fieldHasMultipleValues || false,
    fieldPkMustBeUsed: fieldMustBeUsed || false,
    fieldPKReferencedValue: fieldPKReferenced || false,
    fieldPKValue: fieldPK,
    fieldPreviousTypeValue: getFieldTypeValue(fieldType) || '',
    fieldReadOnlyValue: fieldReadOnly,
    fieldRequiredValue: fieldRequired,
    fieldTypeValue: getFieldTypeValue(fieldType),
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

  const resources = useContext(ResourcesContext);
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
    if (
      TextUtils.areEquals(type.fieldType, 'codelist') ||
      TextUtils.areEquals(type.fieldType, 'multiselect_codelist')
    ) {
      onCodelistDropdownSelected(type);
    } else if (TextUtils.areEquals(type.fieldType, 'link')) {
      onLinkDropdownSelected(type);
    } else if (TextUtils.areEquals(type.fieldType, 'attachment')) {
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
              pk: ['POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(
                type.fieldType.toUpperCase()
              )
                ? false
                : fieldDesignerState.fieldPKValue
            });
          }
        }
      } else {
        if (type !== '' && type !== fieldDesignerState.fieldValue) {
          fieldUpdate({
            codelistItems: null,
            pk: ['POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(
              type.fieldType.toUpperCase()
            )
              ? false
              : fieldDesignerState.fieldPKValue,
            type: parseGeospatialTypes(type.fieldType),
            isLinkChange: TextUtils.areEquals(fieldDesignerState.fieldTypeValue.fieldType, 'LINK')
          });
        } else {
          if (type !== '') {
            fieldTypeRef.current.hide();
            onShowDialogError(resources.messages['emptyFieldTypeMessage'], resources.messages['emptyFieldTypeTitle']);
          }
        }
      }
      dispatchFieldDesigner({ type: 'RESET_FIELD' });
      if (
        ['POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(
          type.fieldType.toUpperCase()
        )
      )
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
            onShowDialogError(resources.messages['emptyFieldMessage'], resources.messages['emptyFieldTitle']);
          } else {
            if (!checkDuplicates(name, fieldId)) {
              if (!isNil(fieldDesignerState.fieldTypeValue) && fieldDesignerState.fieldTypeValue !== '') {
                onFieldAdd({ name });
              }
            } else {
              fieldTypeRef.current.hide();
              onShowDialogError(
                resources.messages['duplicatedFieldMessage'],
                resources.messages['duplicatedFieldTitle']
              );
              dispatchFieldDesigner({ type: 'SET_NAME', payload: fieldDesignerState.initialFieldValue });
            }
          }
        } else {
          if (name === '') {
            fieldTypeRef.current.hide();
            onShowDialogError(resources.messages['emptyFieldMessage'], resources.messages['emptyFieldTitle']);
            dispatchFieldDesigner({ type: 'SET_NAME', payload: fieldDesignerState.initialFieldValue });
          } else {
            if (name !== fieldDesignerState.initialFieldValue) {
              if (!checkDuplicates(name, fieldId)) {
                fieldUpdate({ name });
              } else {
                fieldTypeRef.current.hide();
                onShowDialogError(
                  resources.messages['duplicatedFieldMessage'],
                  resources.messages['duplicatedFieldTitle']
                );
                dispatchFieldDesigner({ type: 'SET_NAME', payload: fieldDesignerState.initialFieldValue });
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
    // onCodelistAndLinkShow(fieldId, { fieldType: 'Link', value: 'Link to another record', fieldTypeIcon: 'link' });
    if (!isUndefined(fieldId)) {
      if (fieldId.toString() === '-1') {
        if (!isUndefined(fieldDesignerState.fieldValue) && fieldDesignerState.fieldValue !== '') {
          onFieldAdd({
            codelistItems,
            type: 'LINK',
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
    // onCodelistAndLinkShow(fieldId, { fieldType: 'Codelist', value: 'Codelist', fieldTypeIcon: 'list' });

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
    validExtensions = fieldDesignerState.fieldFileProperties.validExtensions
  }) => {
    try {
      const response = await DatasetService.addRecordFieldDesign(datasetId, {
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
      if (response.status < 200 || response.status > 299) {
        console.error('Error during field Add');
      } else {
        dispatchFieldDesigner({ type: 'RESET_NEW_FIELD' });
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
      }
    } catch (error) {
      console.error('Error during field Add: ', error);
    } finally {
      if (!isNil(inputRef.current)) {
        if (index === '-1') {
          inputRef.current.element.focus();
        }
      }
      dispatchFieldDesigner({ type: 'SET_ADD_FIELD_SENT', payload: false });
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
    } else if (event.key == 'Enter') {
      if (input === 'NAME') {
        onBlurFieldName(event.target.value);
      }
    }
  };

  const onPKChange = checked => {
    if (!fieldDesignerState.isDragging) {
      if (fieldId === '-1') {
        if (
          !isNil(fieldDesignerState.fieldTypeValue) &&
          fieldDesignerState.fieldTypeValue !== '' &&
          !isNil(fieldDesignerState.fieldValue) &&
          fieldDesignerState.fieldValue !== ''
        ) {
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
        if (
          !isNil(fieldDesignerState.fieldTypeValue) &&
          fieldDesignerState.fieldTypeValue !== '' &&
          !isNil(fieldDesignerState.fieldValue) &&
          fieldDesignerState.fieldValue !== ''
        ) {
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
        if (
          !isNil(fieldDesignerState.fieldTypeValue) &&
          fieldDesignerState.fieldTypeValue !== '' &&
          !isNil(fieldDesignerState.fieldValue) &&
          fieldDesignerState.fieldValue !== ''
        ) {
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
      onShowDialogError(resources.messages['emptyFieldMessage'], resources.messages['emptyFieldTitle']);
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
      onShowDialogError(resources.messages['emptyFieldMessage'], resources.messages['emptyFieldTitle']);
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
      onShowDialogError(resources.messages['emptyFieldMessage'], resources.messages['emptyFieldTitle']);
    } else {
      if (!isUndefined(fieldId)) {
        if (fieldId.toString() === '-1') {
          onFieldAdd({
            codelistItems,
            type: 'LINK',
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
            type: 'LINK',
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
          <FontAwesomeIcon icon={AwesomeIcons(option.fieldTypeIcon)} />
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
      const { status } = await DatasetService.updateRecordFieldDesign(datasetId, {
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
        referencedField: TextUtils.areEquals(type, 'LINK')
          ? !isNil(referencedField)
            ? parseReferenceField(referencedField)
            : fieldDesignerState.fieldLinkValue
          : null,
        required,
        type,
        validExtensions
      });

      if (status >= 200 && status <= 299) {
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
          referencedField: TextUtils.areEquals(type, 'LINK')
            ? !isNil(referencedField)
              ? parseReferenceField(referencedField)
              : fieldDesignerState.fieldLinkValue
            : null,
          required,
          type,
          validExtensions
        });
      } else {
        console.error('Error during field Update');
        dispatchFieldDesigner({ type: 'SET_NAME', payload: fieldDesignerState.initialFieldValue });
      }
    } catch (error) {
      console.error(`Error during field Update: ${error}`);
    }
  };

  const parseReferenceField = completeReferencedField => {
    return {
      idDatasetSchema: completeReferencedField.referencedField.datasetSchemaId,
      idPk: completeReferencedField.referencedField.fieldSchemaId,
      linkedConditionalFieldId: completeReferencedField.referencedField.linkedTableConditional,
      labelId: completeReferencedField.referencedField.linkedTableLabel,
      masterConditionalFieldId: completeReferencedField.referencedField.masterTableConditional
    };
  };

  const qcDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        className="p-button-secondary-transparent p-button-animated-blink button-right-aligned"
        icon="cancel"
        label={resources.messages['close']}
        onClick={() => dispatchFieldDesigner({ type: 'TOGGLE_QC_MANAGER_VISIBLE', payload: false })}
      />
    </div>
  );

  const renderCheckboxes = () => (
    <div className="requiredAndPKCheckboxes">
      {!addField ? (
        <FontAwesomeIcon
          icon={AwesomeIcons('move')}
          style={{ width: '32px', opacity: isDataflowOpen || isDesignDatasetEditorRead ? 0.5 : 1 }}
        />
      ) : (
        <div style={{ marginLeft: '32px', display: 'inline-block' }}></div>
      )}
      <Checkbox
        checked={fieldDesignerState.fieldPKValue}
        className={`${styles.checkPK} datasetSchema-pk-help-step ${
          fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
        } ${isDataflowOpen && isDesignDatasetEditorRead && styles.checkboxDisabled}`}
        disabled={
          (!isNil(fieldDesignerState.fieldTypeValue) &&
            !isNil(fieldDesignerState.fieldTypeValue.fieldType) &&
            ['POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(
              fieldDesignerState.fieldTypeValue.fieldType.toUpperCase()
            )) ||
          (hasPK && (!fieldDesignerState.fieldPKValue || fieldDesignerState.fieldPKReferencedValue)) ||
          isDataflowOpen ||
          isDesignDatasetEditorRead
        }
        id={`${fieldId}_check_pk`}
        inputId={`${fieldId}_check_pk`}
        label="Default"
        onChange={e => {
          if (!(hasPK && (!fieldDesignerState.fieldPKValue || fieldDesignerState.fieldPKReferencedValue))) {
            onPKChange(e.checked);
          }
        }}
        style={{ width: '35px' }}
      />
      <label htmlFor={`${fieldId}_check_pk`} className="srOnly">
        {resources.messages['pk']}
      </label>
      <Checkbox
        checked={fieldDesignerState.fieldRequiredValue}
        className={`${styles.checkRequired} datasetSchema-required-help-step ${
          fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
        } ${isDataflowOpen && isDesignDatasetEditorRead && styles.checkboxDisabled}`}
        disabled={Boolean(fieldDesignerState.fieldPKValue) || isDataflowOpen || isDesignDatasetEditorRead}
        id={`${fieldId}_check_required`}
        inputId={`${fieldId}_check_required`}
        label="Default"
        onChange={e => {
          onRequiredChange(e.checked);
        }}
        style={{ width: '70px' }}
      />
      <label htmlFor={`${fieldId}_check_required`} className="srOnly">
        {resources.messages['required']}
      </label>
      <Checkbox
        checked={fieldDesignerState.fieldReadOnlyValue}
        className={`${styles.checkReadOnly} datasetSchema-readOnly-help-step ${
          fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
        } ${isDataflowOpen && isDesignDatasetEditorRead && styles.checkboxDisabled}`}
        disabled={isDataflowOpen || isDesignDatasetEditorRead}
        id={`${fieldId}_check_readOnly`}
        inputId={`${fieldId}_check_readOnly`}
        label="Default"
        onChange={e => onReadOnlyChange(e.checked)}
        style={{ width: '90px' }}
      />
      <label htmlFor={`${fieldId}_check_required`} className="srOnly">
        {resources.messages['readOnly']}
      </label>
    </div>
  );

  const renderCodelistFileAndLinkButtons = () =>
    !isUndefined(fieldDesignerState.fieldTypeValue) &&
    (fieldDesignerState.fieldTypeValue.fieldType === 'Codelist' ||
      fieldDesignerState.fieldTypeValue.fieldType === 'Multiselect_Codelist') ? (
      <Button
        className={`${styles.codelistButton} p-button-secondary-transparent ${
          fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
        }`}
        disabled={isDataflowOpen || isDesignDatasetEditorRead}
        label={
          !isUndefined(fieldDesignerState.codelistItems) && !isEmpty(fieldDesignerState.codelistItems)
            ? `${fieldDesignerState.codelistItems.join('; ')}`
            : fieldDesignerState.fieldTypeValue.fieldType === 'Codelist'
            ? resources.messages['codelistSelection']
            : resources.messages['multiselectCodelistSelection']
        }
        onClick={() => onCodelistDropdownSelected()}
        style={{ pointerEvents: 'auto' }}
        tooltip={
          !isUndefined(fieldDesignerState.codelistItems) && !isEmpty(fieldDesignerState.codelistItems)
            ? `${fieldDesignerState.codelistItems.join('; ')}`
            : fieldDesignerState.fieldTypeValue.fieldType === 'Codelist'
            ? resources.messages['codelistSelection']
            : resources.messages['multiselectCodelistSelection']
        }
        tooltipOptions={{ position: 'top' }}
      />
    ) : !isUndefined(fieldDesignerState.fieldTypeValue) && fieldDesignerState.fieldTypeValue.fieldType === 'Link' ? (
      <Button
        className={`${styles.codelistButton} p-button-secondary-transparent ${
          fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
        }`}
        disabled={isDataflowOpen || isDesignDatasetEditorRead}
        label={
          !isUndefined(fieldDesignerState.fieldLinkValue) && !isEmpty(fieldDesignerState.fieldLinkValue)
            ? `${fieldDesignerState.fieldLinkValue.name}`
            : resources.messages['linkSelection']
        }
        onClick={() => onLinkDropdownSelected()}
        style={{ pointerEvents: 'auto' }}
        tooltip={
          !isUndefined(fieldDesignerState.fieldLinkValue) && !isEmpty(fieldDesignerState.fieldLinkValue)
            ? `${fieldDesignerState.fieldLinkValue.name}`
            : resources.messages['linkSelection']
        }
        tooltipOptions={{ position: 'top' }}
      />
    ) : !isUndefined(fieldDesignerState.fieldTypeValue) &&
      fieldDesignerState.fieldTypeValue.fieldType === 'Attachment' ? (
      <Button
        className={`${styles.codelistButton} p-button-secondary-transparent ${
          fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
        }`}
        disabled={isDataflowOpen || isDesignDatasetEditorRead}
        label={`${resources.messages['validExtensions']} ${
          !isUndefined(fieldDesignerState.fieldFileProperties.validExtensions) &&
          !isEmpty(fieldDesignerState.fieldFileProperties.validExtensions)
            ? fieldDesignerState.fieldFileProperties.validExtensions.join(', ')
            : '*'
        } - ${resources.messages['maxFileSize']} ${fieldDesignerState.fieldFileProperties.maxSize} ${
          resources.messages['MB']
        }`}
        onClick={() => onAttachmentDropdownSelected()}
        style={{ pointerEvents: 'auto' }}
        tooltip={`${resources.messages['validExtensions']} ${
          !isUndefined(fieldDesignerState.fieldFileProperties.validExtensions) &&
          !isEmpty(fieldDesignerState.fieldFileProperties.validExtensions)
            ? fieldDesignerState.fieldFileProperties.validExtensions.join(', ')
            : '*'
        } - ${resources.messages['maxFileSize']} ${
          !isNil(fieldDesignerState.fieldFileProperties.maxSize) &&
          fieldDesignerState.fieldFileProperties.maxSize.toString() !== '0'
            ? `${fieldDesignerState.fieldFileProperties.maxSize} ${resources.messages['MB']}`
            : resources.messages['maxSizeNotDefined']
        }`}
        tooltipOptions={{ position: 'top' }}
      />
    ) : isCodelistOrLink ? (
      <span
        className={fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive}
        style={{ width: '4rem', marginRight: '0.4rem' }}></span>
    ) : null;

  const renderDeleteButton = () =>
    !addField ? (
      <a
        draggable={true}
        className={`${styles.button} ${styles.deleteButton} ${fieldPKReferenced ? styles.disabledDeleteButton : ''} ${
          fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
        } ${isDataflowOpen || isDesignDatasetEditorRead ? styles.linkDisabled : ''}`}
        href="#"
        onClick={e => {
          e.preventDefault();
          onFieldDelete(index, fieldDesignerState.fieldTypeValue.fieldType);
        }}
        onDragStart={event => {
          event.preventDefault();
          event.stopPropagation();
        }}>
        <FontAwesomeIcon icon={AwesomeIcons('delete')} />
        <span className="srOnly">{resources.messages['deleteFieldLabel']}</span>
      </a>
    ) : null;

  const renderInputs = () => (
    <React.Fragment>
      <InputText
        autoFocus={false}
        className={`${styles.inputField} ${
          fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
        }`}
        disabled={isDataflowOpen || isDesignDatasetEditorRead}
        id={fieldName}
        keyfilter="schemaTableFields"
        maxLength={60}
        // key={`${fieldId}_${index}`} --> Problem with DOM modification
        onBlur={e => {
          dispatchFieldDesigner({ type: 'TOGGLE_IS_EDITING', payload: false });
          onBlurFieldName(e.target.value);
        }}
        onChange={e => dispatchFieldDesigner({ type: 'SET_NAME', payload: e.target.value })}
        onFocus={e => {
          dispatchFieldDesigner({ type: 'SET_INITIAL_FIELD_VALUE', payload: e.target.value });
          dispatchFieldDesigner({ type: 'TOGGLE_IS_EDITING', payload: true });
        }}
        onKeyDown={e => onKeyChange(e, 'NAME')}
        placeholder={resources.messages['newFieldPlaceHolder']}
        ref={inputRef}
        required={!isUndefined(fieldDesignerState.fieldValue) ? fieldDesignerState.fieldValue === '' : fieldName === ''}
        value={!isUndefined(fieldDesignerState.fieldValue) ? fieldDesignerState.fieldValue : fieldName}
      />
      <label htmlFor={fieldName} className="srOnly">
        {resources.messages['newFieldPlaceHolder']}
      </label>
      <InputTextarea
        autoFocus={false}
        collapsedHeight={33}
        disabled={isDataflowOpen || isDesignDatasetEditorRead}
        expandableOnClick={true}
        className={`${styles.inputFieldDescription} ${
          fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
        }`}
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
        placeholder={resources.messages['newFieldDescriptionPlaceHolder']}
        value={
          !isUndefined(fieldDesignerState.fieldDescriptionValue)
            ? fieldDesignerState.fieldDescriptionValue
            : fieldDescription
        }
      />
      <Dropdown
        appendTo={document.body}
        ariaLabel={'fieldType'}
        className={`${styles.dropdownFieldType} ${
          fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
        }`}
        disabled={isDataflowOpen || isDesignDatasetEditorRead}
        inputId={`${fieldName}_fieldType`}
        itemTemplate={fieldTypeTemplate}
        name={resources.messages['newFieldTypePlaceHolder']}
        onChange={e => onChangeFieldType(e.target.value)}
        onMouseDown={event => {
          event.preventDefault();
          onSetInitHeaderHeight();
          event.stopPropagation();
        }}
        optionLabel="value"
        options={fieldTypes}
        placeholder={resources.messages['newFieldTypePlaceHolder']}
        ref={fieldTypeRef}
        required={true}
        scrollHeight="450px"
        style={{ alignSelf: !fieldDesignerState.isEditing ? 'center' : 'auto', display: 'block' }}
        value={
          fieldDesignerState.fieldTypeValue !== '' ? fieldDesignerState.fieldTypeValue : getFieldTypeValue(fieldType)
        }
      />
    </React.Fragment>
  );

  return (
    <React.Fragment>
      <div
        draggable={isDataflowOpen || isDesignDatasetEditorRead ? false : !addField}
        className={`${styles.draggableFieldDiv} fieldRow datasetSchema-fieldDesigner-help-step`}
        onDragEnd={e => {
          onFieldDragEnd(e);
        }}
        onDragEnter={e => {
          onFieldDragEnter(e);
        }}
        onDragOver={onFieldDragOver}
        onDragLeave={onFieldDragLeave}
        onDragStart={e => {
          onFieldDragStart(e);
        }}
        onDrop={e => {
          onFieldDragDrop(e);
        }}
        style={{ cursor: isDataflowOpen || isDesignDatasetEditorRead ? 'default' : 'grab' }}>
        <div
          className={`${styles.fieldSeparator} ${
            fieldDesignerState.isDragging ? styles.fieldSeparatorDragging : ''
          }`}></div>

        {renderCheckboxes()}
        {renderInputs()}
        {renderCodelistFileAndLinkButtons()}
        {!addField ? (
          <Button
            className={`p-button-secondary-transparent button ${styles.qcButton} ${
              fieldDesignerState.isDragging ? styles.dragAndDropActive : styles.dragAndDropInactive
            }`}
            disabled={
              (!isUndefined(fieldDesignerState.fieldTypeValue) &&
                config.validations.bannedFieldsNames.sqlFields.includes(
                  fieldDesignerState.fieldTypeValue.value.toLowerCase()
                )) ||
              isDesignDatasetEditorRead
            }
            icon="horizontalSliders"
            label={resources.messages['createFieldQC']}
            onClick={() => validationContext.onOpenModalFromField(fieldId, tableSchemaId)}
            style={{ marginLeft: '0.4rem', alignSelf: !fieldDesignerState.isEditing ? 'center' : 'baseline' }}
          />
        ) : null}
        {renderDeleteButton()}
      </div>
      {fieldDesignerState.isCodelistEditorVisible ? (
        <CodelistEditor
          isCodelistEditorVisible={fieldDesignerState.isCodelistEditorVisible}
          onCancelSaveCodelist={onCancelSaveCodelist}
          onSaveCodelist={onSaveCodelist}
          selectedCodelist={fieldDesignerState.codelistItems}
          type={fieldDesignerState.fieldTypeValue.value}
        />
      ) : null}
      {fieldDesignerState.isAttachmentEditorVisible ? (
        <AttachmentEditor
          isAttachmentEditorVisible={fieldDesignerState.isAttachmentEditorVisible}
          onCancelSaveAttachment={onCancelSaveAttachment}
          onSaveAttachment={onSaveAttachment}
          selectedAttachment={fieldDesignerState.fieldFileProperties}
          type={fieldDesignerState.fieldTypeValue.value}
        />
      ) : null}
      {fieldDesignerState.isLinkSelectorVisible ? (
        <LinkSelector
          datasetSchemaId={datasetSchemaId}
          hasMultipleValues={fieldDesignerState.fieldPkHasMultipleValues}
          isLinkSelectorVisible={fieldDesignerState.isLinkSelectorVisible}
          linkedTableConditional={fieldLinkedTableConditional}
          linkedTableLabel={fieldLinkedTableLabel}
          masterTableConditional={fieldMasterTableConditional}
          mustBeUsed={fieldDesignerState.fieldPkMustBeUsed}
          onCancelSaveLink={onCancelSaveLink}
          onSaveLink={onSaveLink}
          selectedLink={fieldDesignerState.fieldLinkValue}
          tableSchemaId={tableSchemaId}
        />
      ) : null}
      {fieldDesignerState.isQCManagerVisible ? (
        <Dialog
          blockScroll={false}
          contentStyle={{ overflow: 'auto' }}
          footer={qcDialogFooter}
          header={resources.messages['qcManager']}
          modal={true}
          onHide={() => dispatchFieldDesigner({ type: 'TOGGLE_QC_MANAGER_VISIBLE', payload: false })}
          style={{ width: '80%' }}
          visible={fieldDesignerState.isQCManagerVisible}
          zIndex={3003}>
          {}
        </Dialog>
      ) : null}
    </React.Fragment>
  );
};
FieldDesigner.propTypes = {};
