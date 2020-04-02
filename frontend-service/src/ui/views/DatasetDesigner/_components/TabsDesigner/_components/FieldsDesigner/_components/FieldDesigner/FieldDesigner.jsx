import React, { useContext, useEffect, useReducer, useRef } from 'react';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './FieldDesigner.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'primereact/checkbox';
import { CodelistEditor } from './_components/CodelistEditor';
import { Dialog } from 'ui/views/_components/Dialog';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { LinkSelector } from './_components/LinkSelector';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

import { fieldDesignerReducer } from './_functions/Reducers/fieldDesignerReducer';

import { DatasetService } from 'core/services/Dataset';

export const FieldDesigner = ({
  addField = false,
  checkDuplicates,
  codelistItems,
  datasetId,
  fieldId,
  fieldDescription,
  fieldName,
  fieldPK,
  fieldPKReferenced,
  fieldLink,
  fieldRequired,
  fieldType,
  hasPK,
  index,
  initialFieldIndexDragged,
  isCodelistOrLink,
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
    { fieldType: 'Number', value: 'Number', fieldTypeIcon: 'number' },
    { fieldType: 'Date', value: 'Date', fieldTypeIcon: 'calendar' },
    // { fieldType: 'Latitude', value: 'Geospatial object (Latitude)', fieldTypeIcon: 'map' },
    // { fieldType: 'Longitude', value: 'Geospatial object (Longitude)', fieldTypeIcon: 'map' },
    { fieldType: 'Text', value: 'Single line text', fieldTypeIcon: 'italic' },
    // { fieldType: 'Boolean', value: 'Boolean', fieldTypeIcon: 'boolean' },
    // { fieldType: 'Point', value: 'Point', fieldTypeIcon: 'point' },
    // { fieldType: 'Circle', value: 'Circle', fieldTypeIcon: 'circle' },
    // { fieldType: 'Polygon', value: 'Polygon', fieldTypeIcon: 'polygon' },
    { fieldType: 'Codelist', value: 'Single select', fieldTypeIcon: 'list' },
    { fieldType: 'Link', value: 'Link to another record', fieldTypeIcon: 'link' }
    // { fieldType: 'Reference', value: 'Reference', fieldTypeIcon: 'link' }
    // { fieldType: 'URL', value: 'Url', fieldTypeIcon: 'url' },
    // { fieldType: 'LongText', value: 'Long text', fieldTypeIcon: 'text' },
    // { fieldType: 'LinkData', value: 'Link to a data collection', fieldTypeIcon: 'linkData' },
    // { fieldType: 'Percentage', value: 'Percentage', fieldTypeIcon: 'percentage' },
    // { fieldType: 'Formula', value: 'Formula', fieldTypeIcon: 'formula' },
    // { fieldType: 'Fixed', value: 'Fixed select list', fieldTypeIcon: 'list' },
    // { fieldType: 'Email', value: 'Email', fieldTypeIcon: 'email' },
    // { fieldType: 'Attachement', value: 'Attachement', fieldTypeIcon: 'clip' }
  ];

  const getFieldTypeValue = value => {
    // if (value.toUpperCase() === 'COORDINATE_LONG') {
    //   value = 'Longitude';
    // }
    // if (value.toUpperCase() === 'COORDINATE_LAT') {
    //   value = 'Latitude';
    // }
    return fieldTypes.filter(field => field.fieldType.toUpperCase() === value.toUpperCase())[0];
  };
  const initialFieldDesignerState = {
    codelistItems: codelistItems,
    fieldDescriptionValue: fieldDescription,
    fieldLinkValue: fieldLink || null,
    fieldPKReferencedValue: fieldPKReferenced || false,
    fieldPKValue: fieldPK,
    fieldPreviousTypeValue: getFieldTypeValue(fieldType) || '',
    fieldRequiredValue: fieldRequired,
    fieldTypeValue: getFieldTypeValue(fieldType),
    fieldValue: fieldName,
    initialDescriptionValue: undefined,
    initialFieldValue: undefined,
    isCodelistEditorVisible: false,
    isDragging: false,
    isEditing: false,
    isLinkSelectorVisible: false,
    isQCManagerVisible: false
  };

  const [fieldDesignerState, dispatchFieldDesigner] = useReducer(fieldDesignerReducer, initialFieldDesignerState);

  const inputRef = useRef();
  const resources = useContext(ResourcesContext);
  const validationContext = useContext(ValidationContext);

  useEffect(() => {
    dispatchFieldDesigner({ type: 'SET_PK_REFERENCED', payload: fieldPKReferenced });
  }, [fieldPKReferenced]);

  useEffect(() => {
    if (totalFields > 0) {
      if (!isUndefined(inputRef.current)) {
        if (index === '-1') {
          inputRef.current.element.focus();
        }
      }
    }
  }, [totalFields]);

  useEffect(() => {
    //Set pointerEvents to auto or none depending on isDragging.
    //because appendTo in Dropdown component we need to find the p-dropdown-panel class in the document, not in the Dropdown itself
    const dropdownPanel = document.getElementsByClassName('p-dropdown-panel')[0];
    const childs = document.getElementsByClassName('fieldRow');
    if (!isUndefined(childs)) {
      for (let i = 0; i < childs.length; i++) {
        for (let j = 2; j < childs[i].childNodes.length; j++) {
          if (fieldDesignerState.isDragging) {
            childs[i].childNodes[j].style.pointerEvents = 'none';
            dropdownPanel.style.pointerEvents = 'none';
          } else {
            childs[i].childNodes[j].style.pointerEvents = 'auto';
            dropdownPanel.style.pointerEvents = 'auto';
            //Dropdown
            const dropdownChilds = document.getElementsByClassName('p-dropdown-items');
            if (!isUndefined(dropdownChilds)) {
              for (let k = 0; k < dropdownChilds.length; k++) {
                for (let l = 0; l < dropdownChilds[k].childNodes.length; l++) {
                  if (!isUndefined(dropdownChilds[k].childNodes[l])) {
                    dropdownChilds[k].childNodes[l].style.pointerEvents = 'auto';
                  }
                }
              }
            }
          }
        }
      }
    }
    const requiredAndPKCheckboxes = document.getElementsByClassName('requiredAndPKCheckboxes');
    if (!isUndefined(requiredAndPKCheckboxes)) {
      for (let i = 0; i < requiredAndPKCheckboxes.length; i++) {
        for (let j = 0; j < requiredAndPKCheckboxes[i].childNodes.length; j++) {
          if (fieldDesignerState.isDragging) {
            requiredAndPKCheckboxes[i].childNodes[j].style.pointerEvents = 'none';
          } else {
            requiredAndPKCheckboxes[i].childNodes[j].style.pointerEvents = 'auto';
          }
        }
      }
    }
  }, [fieldDesignerState.isDragging]);

  const onChangeFieldType = type => {
    dispatchFieldDesigner({ type: 'SET_TYPE', payload: { type, previousType: fieldDesignerState.fieldTypeValue } });
    if (type.fieldType.toLowerCase() === 'codelist') {
      onCodelistDropdownSelected(type);
    } else if (type.fieldType.toLowerCase() === 'link') {
      onLinkDropdownSelected(type);
    } else {
      if (fieldId === '-1') {
        if (type !== '') {
          if (!isUndefined(fieldDesignerState.fieldValue) && fieldDesignerState.fieldValue !== '') {
            onFieldAdd({ type: parseGeospatialTypes(type.fieldType) });
          }
        }
      } else {
        if (type !== '' && type !== fieldDesignerState.fieldValue) {
          fieldUpdate({ codelistItems: null, type: parseGeospatialTypes(type.fieldType) });
        } else {
          if (type !== '') {
            onShowDialogError(resources.messages['emptyFieldTypeMessage'], resources.messages['emptyFieldTypeTitle']);
          }
        }
      }
      dispatchFieldDesigner({ type: 'SET_CODELIST_ITEMS', payload: [] });
      dispatchFieldDesigner({ type: 'SET_LINK', payload: null });
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
            onShowDialogError(resources.messages['emptyFieldMessage'], resources.messages['emptyFieldTitle']);
          } else {
            if (!checkDuplicates(name, fieldId)) {
              if (!isNil(fieldDesignerState.fieldTypeValue) && fieldDesignerState.fieldTypeValue !== '') {
                onFieldAdd({ name });
              }
            } else {
              onShowDialogError(
                resources.messages['duplicatedFieldMessage'],
                resources.messages['duplicatedFieldTitle']
              );
              dispatchFieldDesigner({ type: 'SET_NAME', payload: fieldDesignerState.initialFieldValue });
            }
          }
        } else {
          if (name === '') {
            onShowDialogError(resources.messages['emptyFieldMessage'], resources.messages['emptyFieldTitle']);
            dispatchFieldDesigner({ type: 'SET_NAME', payload: fieldDesignerState.initialFieldValue });
          } else {
            if (name !== fieldDesignerState.initialFieldValue) {
              if (!checkDuplicates(name, fieldId)) {
                fieldUpdate({ name });
              } else {
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

  const onCancelSaveLink = () => {
    // onCodelistAndLinkShow(fieldId, { fieldType: 'Link', value: 'Link to another record', fieldTypeIcon: 'link' });
    dispatchFieldDesigner({ type: 'CANCEL_SELECT_LINK', payload: fieldDesignerState.fieldPreviousTypeValue });
  };

  const onCancelSaveCodelist = () => {
    // onCodelistAndLinkShow(fieldId, { fieldType: 'Codelist', value: 'Codelist', fieldTypeIcon: 'list' });
    dispatchFieldDesigner({ type: 'CANCEL_SELECT_CODELIST', payload: fieldDesignerState.fieldPreviousTypeValue });
  };

  const onCodelistDropdownSelected = fieldType => {
    if (!isUndefined(fieldType)) {
      onCodelistAndLinkShow(fieldId, fieldType);
    }
    dispatchFieldDesigner({ type: 'TOGGLE_CODELIST_EDITOR_VISIBLE', payload: true });
  };

  const onLinkDropdownSelected = fieldType => {
    if (!isUndefined(fieldType)) {
      onCodelistAndLinkShow(fieldId, fieldType);
    }
    dispatchFieldDesigner({ type: 'TOGGLE_LINK_SELECTOR_VISIBLE', payload: true });
  };

  const onFieldAdd = async ({
    codelistItems = fieldDesignerState.codelistItems,
    description = fieldDesignerState.fieldDescriptionValue,
    pk = fieldDesignerState.fieldPKValue,
    name = fieldDesignerState.fieldValue,
    recordId = recordSchemaId,
    referencedField = fieldDesignerState.fieldLinkValue,
    required = fieldDesignerState.fieldRequiredValue,
    type = parseGeospatialTypes(fieldDesignerState.fieldTypeValue.fieldType)
  }) => {
    try {
      const response = await DatasetService.addRecordFieldDesign(datasetId, {
        codelistItems,
        description,
        pk,
        name,
        recordId,
        referencedField: !isNil(referencedField)
          ? parseReferenceField(referencedField)
          : fieldDesignerState.fieldLinkValue,
        required,
        type
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
          pk,
          name,
          recordId,
          referencedField,
          required,
          type
        });
      }
    } catch (error) {
      console.error('Error during field Add: ', error);
    } finally {
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
      inputRef.current.element.focus();
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

  const onSaveCodelist = codelistItems => {
    dispatchFieldDesigner({ type: 'SET_CODELIST_ITEMS', payload: codelistItems });
    if (fieldDesignerState.fieldValue === '') {
      onShowDialogError(resources.messages['emptyFieldMessage'], resources.messages['emptyFieldTitle']);
    } else {
      if (!isUndefined(fieldId)) {
        if (fieldId.toString() === '-1') {
          onFieldAdd({ codelistItems, type: 'CODELIST' });
        } else {
          fieldUpdate({ codelistItems, type: 'CODELIST' });
        }
      }
    }
    dispatchFieldDesigner({ type: 'TOGGLE_CODELIST_EDITOR_VISIBLE', payload: false });
  };

  const onSaveLink = link => {
    dispatchFieldDesigner({ type: 'SET_LINK', payload: link });
    if (fieldDesignerState.fieldValue === '') {
      onShowDialogError(resources.messages['emptyFieldMessage'], resources.messages['emptyFieldTitle']);
    } else {
      if (!isUndefined(fieldId)) {
        if (fieldId.toString() === '-1') {
          onFieldAdd({
            codelistItems,
            type: 'LINK',
            referencedField: link
          });
        } else {
          fieldUpdate({
            codelistItems,
            isLinkChange: true,
            type: 'LINK',
            referencedField: link
          });
        }
      }
    }
    dispatchFieldDesigner({ type: 'TOGGLE_CODELIST_EDITOR_VISIBLE', payload: false });
    dispatchFieldDesigner({ type: 'TOGGLE_LINK_SELECTOR_VISIBLE', payload: false });
  };

  const parseGeospatialTypes = value => {
    // if (value.toUpperCase() === 'LONGITUDE') {
    //   return 'COORDINATE_LONG';
    // }
    // if (value.toUpperCase() === 'LATITUDE') {
    //   return 'COORDINATE_LAT';
    // }
    return value.toUpperCase();
  };

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
    pk = fieldDesignerState.fieldPKValue,
    name = fieldDesignerState.fieldValue,
    referencedField = fieldDesignerState.fieldLinkValue,
    required = fieldDesignerState.fieldRequiredValue,
    type = parseGeospatialTypes(fieldDesignerState.fieldTypeValue.fieldType)
  }) => {
    try {
      const fieldUpdated = await DatasetService.updateRecordFieldDesign(datasetId, {
        codelistItems,
        description,
        fieldSchemaId,
        pk,
        name,
        referencedField: !isNil(referencedField)
          ? parseReferenceField(referencedField)
          : fieldDesignerState.fieldLinkValue,
        required,
        type
      });
      if (!fieldUpdated) {
        console.error('Error during field Update');
        dispatchFieldDesigner({ type: 'SET_NAME', payload: fieldDesignerState.initialFieldValue });
      } else {
        onFieldUpdate({
          codelistItems,
          description,
          id: fieldId,
          isLinkChange,
          pk,
          name,
          referencedField,
          required,
          type
        });
      }
    } catch (error) {
      console.error(`Error during field Update: ${error}`);
    }
  };

  const parseReferenceField = completeReferencedField => {
    return {
      idPk: completeReferencedField.referencedField.fieldSchemaId,
      idDatasetSchema: completeReferencedField.referencedField.datasetSchemaId
    };
  };

  const qcDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        className="p-button-secondary-transparent p-button-animated-blink"
        icon="cancel"
        label={resources.messages['close']}
        onClick={() => dispatchFieldDesigner({ type: 'TOGGLE_QC_MANAGER_VISIBLE', payload: false })}
      />
    </div>
  );

  const renderCheckboxes = () => (
    <div className="requiredAndPKCheckboxes">
      {!addField ? (
        <FontAwesomeIcon icon={AwesomeIcons('move')} style={{ width: '32px' }} />
      ) : (
        <div style={{ marginLeft: '32px', display: 'inline-block' }}></div>
      )}
      <Checkbox
        checked={fieldDesignerState.fieldRequiredValue}
        className={styles.checkRequired}
        disabled={Boolean(fieldDesignerState.fieldPKValue)}
        inputId={`${fieldId}_check`}
        label="Default"
        onChange={e => {
          onRequiredChange(e.checked);
        }}
        style={{ width: '70px' }}
      />
      <Checkbox
        checked={fieldDesignerState.fieldPKValue}
        disabled={hasPK && (!fieldDesignerState.fieldPKValue || fieldDesignerState.fieldPKReferencedValue)}
        inputId={`${fieldId}_check_pk`}
        label="Default"
        onChange={e => {
          if (!(hasPK && (!fieldDesignerState.fieldPKValue || fieldDesignerState.fieldPKReferencedValue))) {
            onPKChange(e.checked);
          }
        }}
        style={{ width: '35px' }}
      />
    </div>
  );

  const renderCodelistAndLinkButtons = () => {
    return !isUndefined(fieldDesignerState.fieldTypeValue) &&
      fieldDesignerState.fieldTypeValue.fieldType === 'Codelist' ? (
      <Button
        className={`${styles.codelistButton} p-button-secondary-transparent`}
        label={
          !isUndefined(fieldDesignerState.codelistItems) && !isEmpty(fieldDesignerState.codelistItems)
            ? `${fieldDesignerState.codelistItems.join(', ')}`
            : resources.messages['codelistSelection']
        }
        onClick={() => onCodelistDropdownSelected()}
        style={{ pointerEvents: 'auto' }}
        tooltip={
          !isUndefined(fieldDesignerState.codelistItems) && !isEmpty(fieldDesignerState.codelistItems)
            ? `${fieldDesignerState.codelistItems.join(', ')}`
            : resources.messages['codelistSelection']
        }
        tooltipOptions={{ position: 'top' }}
      />
    ) : !isUndefined(fieldDesignerState.fieldTypeValue) && fieldDesignerState.fieldTypeValue.fieldType === 'Link' ? (
      <Button
        className={`${styles.codelistButton} p-button-secondary-transparent`}
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
    ) : isCodelistOrLink ? (
      <span style={{ width: '4rem', marginRight: '0.4rem' }}></span>
    ) : null;
  };

  const renderDeleteButton = () =>
    !addField ? (
      <a
        draggable={true}
        className={`${styles.button} ${styles.deleteButton} ${
          fieldDesignerState.fieldPKValue || fieldPKReferenced ? styles.disabledDeleteButton : ''
        }`}
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
      </a>
    ) : null;

  const renderInputs = () => (
    <React.Fragment>
      <InputText
        autoFocus={false}
        className={styles.inputField}
        // key={`${fieldId}_${index}`} --> Problem with DOM modification
        ref={inputRef}
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
        required={!isUndefined(fieldDesignerState.fieldValue) ? fieldDesignerState.fieldValue === '' : fieldName === ''}
        value={!isUndefined(fieldDesignerState.fieldValue) ? fieldDesignerState.fieldValue : fieldName}
      />
      <InputTextarea
        autoFocus={false}
        collapsedHeight={33}
        expandableOnClick={true}
        className={styles.inputFieldDescription}
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
        className={styles.dropdownFieldType}
        itemTemplate={fieldTypeTemplate}
        onChange={e => onChangeFieldType(e.target.value)}
        onMouseDown={event => {
          event.preventDefault();
          event.stopPropagation();
        }}
        optionLabel="value"
        options={fieldTypes}
        required={true}
        placeholder={resources.messages['newFieldTypePlaceHolder']}
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
        draggable={!addField}
        className={`${styles.draggableFieldDiv} fieldRow`}
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
        }}>
        <div
          className={`${styles.fieldSeparator} ${
            fieldDesignerState.isDragging ? styles.fieldSeparatorDragging : ''
          }`}></div>

        {renderCheckboxes()}
        {renderInputs()}
        {renderCodelistAndLinkButtons()}
        {!addField ? (
          <Button
            className={`p-button-secondary-transparent button ${styles.qcButton}`}
            icon="horizontalSliders"
            onClick={() => validationContext.onOpenModalFromField(fieldId, tableSchemaId)}
            style={{ marginLeft: '0.4rem', alignSelf: !fieldDesignerState.isEditing ? 'center' : 'baseline' }}
            tooltip={resources.messages['createFieldQC']}
            tooltipOptions={{ position: 'bottom' }}
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
        />
      ) : null}
      {fieldDesignerState.isLinkSelectorVisible ? (
        <LinkSelector
          isLinkSelectorVisible={fieldDesignerState.isLinkSelectorVisible}
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
          closeOnEscape={false}
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
