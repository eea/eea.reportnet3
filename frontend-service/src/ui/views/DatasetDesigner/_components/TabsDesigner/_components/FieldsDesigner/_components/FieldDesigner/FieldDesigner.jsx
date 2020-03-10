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
import { FKSelector } from './_components/FKSelector';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

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
  fieldIsPK,
  fieldRequired,
  fieldType,
  hasPK,
  index,
  initialFieldIndexDragged,
  isCodelistSelected,
  onCodelistShow,
  onFieldDelete,
  onFieldDragAndDrop,
  onFieldDragAndDropStart,
  onFieldUpdate,
  onNewFieldAdd,
  onShowDialogError,
  recordId,
  totalFields
}) => {
  const fieldTypes = [
    { fieldType: 'Number', value: 'Number', fieldTypeIcon: 'number' },
    { fieldType: 'Date', value: 'Date', fieldTypeIcon: 'calendar' },
    { fieldType: 'Latitude', value: 'Geospatial object (Latitude)', fieldTypeIcon: 'map' },
    { fieldType: 'Longitude', value: 'Geospatial object (Longitude)', fieldTypeIcon: 'map' },
    { fieldType: 'Text', value: 'Single line text', fieldTypeIcon: 'italic' },
    { fieldType: 'Boolean', value: 'Boolean', fieldTypeIcon: 'boolean' },
    { fieldType: 'Point', value: 'Point', fieldTypeIcon: 'point' },
    { fieldType: 'Circle', value: 'Circle', fieldTypeIcon: 'circle' },
    { fieldType: 'Polygon', value: 'Polygon', fieldTypeIcon: 'polygon' },
    { fieldType: 'Codelist', value: 'Codelist', fieldTypeIcon: 'list' },
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
    if (value.toUpperCase() === 'COORDINATE_LONG') {
      value = 'Longitude';
    }
    if (value.toUpperCase() === 'COORDINATE_LAT') {
      value = 'Latitude';
    }
    return fieldTypes.filter(field => field.fieldType.toUpperCase() === value.toUpperCase())[0];
  };

  const initialFieldDesignerState = {
    codelistItems: codelistItems,
    fieldDescriptionValue: fieldDescription,
    fieldIsPKValue: fieldIsPK,
    fieldPreviousTypeValue: '',
    fieldRequiredValue: fieldRequired,
    fieldTypeValue: getFieldTypeValue(fieldType),
    fieldValue: fieldName,
    initialDescriptionValue: undefined,
    initialFieldValue: undefined,
    isCodelistEditorVisible: false,
    isEditing: false,
    isDragging: false,
    isFKSelectorVisible: false,
    isQCManagerVisible: false,
    selectedFK: ''
  };

  const [fieldDesignerState, dispatchFieldDesigner] = useReducer(fieldDesignerReducer, initialFieldDesignerState);

  const fieldRef = useRef();
  const inputRef = useRef();
  const resources = useContext(ResourcesContext);

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
    const dropdownPanel = fieldRef.current.getElementsByClassName('p-dropdown-panel')[0];
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
      onFKDropdownSelected();
    } else {
      if (fieldId === '-1') {
        if (type !== '') {
          if (!isUndefined(fieldDesignerState.fieldValue) && fieldDesignerState.fieldValue !== '') {
            onFieldAdd({
              description: fieldDesignerState.fieldDescriptionValue,
              isPK: fieldDesignerState.fieldIsPKValue,
              recordId,
              required: fieldDesignerState.fieldRequiredValue,
              type: parseGeospatialTypes(type.fieldType),
              name: fieldDesignerState.fieldValue
            });
          }
        }
      } else {
        if (type !== '' && type !== fieldDesignerState.fieldValue) {
          fieldUpdate({
            description: fieldDesignerState.fieldDescriptionValue,
            fieldSchemaId: fieldId,
            isPK: fieldDesignerState.fieldIsPKValue,
            required: fieldDesignerState.fieldRequiredValue,
            type: parseGeospatialTypes(type.fieldType),
            name: fieldDesignerState.fieldValue
          });
        } else {
          if (type !== '') {
            onShowDialogError(resources.messages['emptyFieldTypeMessage'], resources.messages['emptyFieldTypeTitle']);
          }
        }
      }
      dispatchFieldDesigner({ type: 'SET_CODELIST_ITEMS', payload: [] });
    }
    onCodelistShow(fieldId, type);
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
            onFieldAdd({
              description: fieldDesignerState.fieldDescriptionValue,
              recordId,
              type: parseGeospatialTypes(fieldDesignerState.fieldTypeValue.fieldType),
              name: fieldDesignerState.fieldValue
            });
          }
        } else {
          if (description !== fieldDesignerState.initialDescriptionValue) {
            fieldUpdate({
              codelistItems: fieldDesignerState.codelistItems,
              description,
              fieldSchemaId: fieldId,
              type: parseGeospatialTypes(fieldDesignerState.fieldTypeValue.fieldType),
              name: fieldDesignerState.fieldValue
            });
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
                onFieldAdd({
                  description: fieldDesignerState.fieldDescriptionValue,
                  recordId,
                  type: parseGeospatialTypes(fieldDesignerState.fieldTypeValue.fieldType),
                  name: fieldDesignerState.fieldValue
                });
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
                fieldUpdate({
                  codelistItems: fieldDesignerState.codelistItems,
                  description: fieldDesignerState.fieldDescriptionValue,
                  fieldSchemaId: fieldId,
                  isPK: fieldDesignerState.fieldIsPKValue,
                  type: parseGeospatialTypes(fieldDesignerState.fieldTypeValue.fieldType),
                  name: fieldDesignerState.fieldValue
                });
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

  const onCancelSaveFK = () => {
    dispatchFieldDesigner({ type: 'CANCEL_SELECT_FK', payload: fieldDesignerState.fieldPreviousTypeValue });
  };

  const onCancelSaveCodelist = () => {
    dispatchFieldDesigner({ type: 'CANCEL_SELECT_CODELIST', payload: fieldDesignerState.fieldPreviousTypeValue });
  };

  const onCodelistDropdownSelected = fieldType => {
    if (!isUndefined(fieldType)) {
      onCodelistShow(fieldId, fieldType);
    }
    dispatchFieldDesigner({ type: 'TOGGLE_CODELIST_EDITOR_VISIBLE', payload: true });
  };

  const onFKDropdownSelected = () => {
    dispatchFieldDesigner({ type: 'TOGGLE_FK_SELECTOR_VISIBLE', payload: true });
  };

  const onFieldAdd = async ({ codelistItems, description, isPK, name, recordId, required, type }) => {
    try {
      const response = await DatasetService.addRecordFieldDesign(datasetId, {
        codelistItems,
        description,
        isPK,
        name,
        recordId,
        required,
        type
      });
      if (response.status < 200 || response.status > 299) {
        console.error('Error during field Add');
      } else {
        dispatchFieldDesigner({ type: 'RESET_NEW_FIELD' });
        onNewFieldAdd({
          fieldId: response.data,
          name,
          recordId,
          type,
          description,
          codelistItems,
          required
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
          onFieldAdd({
            codelistItems: fieldDesignerState.codelistItems,
            description: fieldDesignerState.fieldDescriptionValue,
            isPK: checked,
            recordId,
            required: fieldDesignerState.fieldRequiredValue,
            type: parseGeospatialTypes(fieldDesignerState.fieldTypeValue.fieldType),
            name: fieldDesignerState.fieldValue
          });
        }
      } else {
        fieldUpdate({
          codelistItems: fieldDesignerState.codelistItems,
          description: fieldDesignerState.fieldDescriptionValue,
          fieldSchemaId: fieldId,
          isPK: checked,
          required: checked ? true : fieldDesignerState.fieldRequiredValue,
          type: parseGeospatialTypes(fieldDesignerState.fieldTypeValue.fieldType),
          name: fieldDesignerState.fieldValue
        });
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
          onFieldAdd({
            codelistItems: fieldDesignerState.codelistItems,
            description: fieldDesignerState.fieldDescriptionValue,
            isPK: fieldDesignerState.fieldIsPKValue,
            recordId,
            required: checked,
            type: parseGeospatialTypes(fieldDesignerState.fieldTypeValue.fieldType),
            name: fieldDesignerState.fieldValue
          });
        }
      } else {
        fieldUpdate({
          codelistItems: fieldDesignerState.codelistItems,
          description: fieldDesignerState.fieldDescriptionValue,
          fieldSchemaId: fieldId,
          isPK: fieldDesignerState.fieldIsPKValue,
          required: checked,
          type: parseGeospatialTypes(fieldDesignerState.fieldTypeValue.fieldType),
          name: fieldDesignerState.fieldValue
        });
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
          onFieldAdd({
            codelistItems,
            description: fieldDesignerState.fieldDescriptionValue,
            isPK: fieldDesignerState.fieldIsPKValue,
            recordId,
            required: fieldDesignerState.fieldRequiredValue,
            type: 'CODELIST',
            name: fieldDesignerState.fieldValue
          });
        } else {
          fieldUpdate({
            codelistItems,
            description: fieldDesignerState.fieldDescriptionValue,
            fieldSchemaId: fieldId,
            isPK: fieldDesignerState.fieldIsPKValue,
            required: fieldDesignerState.fieldRequiredValue,
            type: 'CODELIST',
            name: fieldDesignerState.fieldValue
          });
        }
      }
    }
    dispatchFieldDesigner({ type: 'TOGGLE_CODELIST_EDITOR_VISIBLE', payload: false });
  };

  const onSaveFK = fk => {
    dispatchFieldDesigner({ type: 'TOGGLE_FK_SELECTOR_VISIBLE', payload: false });
  };

  const parseGeospatialTypes = value => {
    if (value.toUpperCase() === 'LONGITUDE') {
      return 'COORDINATE_LONG';
    }
    if (value.toUpperCase() === 'LATITUDE') {
      return 'COORDINATE_LAT';
    }
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

  const fieldUpdate = async ({ codelistItems, description, fieldSchemaId, isPK, required, type, name }) => {
    try {
      const fieldUpdated = await DatasetService.updateRecordFieldDesign(datasetId, {
        codelistItems,
        description,
        fieldSchemaId,
        isPK,
        name,
        required,
        type
      });
      if (!fieldUpdated) {
        console.error('Error during field Update');
        dispatchFieldDesigner({ type: 'SET_NAME', payload: fieldDesignerState.initialFieldValue });
      } else {
        onFieldUpdate({ id: fieldId, name, type, description, codelistItems, required });
      }
    } catch (error) {
      console.error(`Error during field Update: ${error}`);
    }
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
        }}
        ref={fieldRef}>
        {console.log(fieldDesignerState.isDragging)}
        <div
          className={`${styles.fieldSeparator} ${
            fieldDesignerState.isDragging ? styles.fieldSeparatorDragging : ''
          }`}></div>

        <div className="requiredAndPKCheckboxes">
          {!addField ? (
            <FontAwesomeIcon icon={AwesomeIcons('move')} style={{ width: '32px' }} />
          ) : (
            <div style={{ marginLeft: '32px', display: 'inline-block' }}></div>
          )}
          <Checkbox
            checked={fieldDesignerState.fieldRequiredValue}
            className={styles.checkRequired}
            inputId={`${fieldId}_check`}
            label="Default"
            onChange={e => {
              onRequiredChange(e.checked);
            }}
            style={{ width: '70px' }}
          />
          <Checkbox
            checked={fieldDesignerState.fieldIsPKValue}
            disabled={hasPK && !fieldDesignerState.fieldIsPKValue}
            inputId={`${fieldId}_check_pk`}
            label="Default"
            onChange={e => {
              onPKChange(e.checked);
            }}
            style={{ width: '35px' }}
          />
        </div>

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
          required={
            !isUndefined(fieldDesignerState.fieldValue) ? fieldDesignerState.fieldValue === '' : fieldName === ''
          }
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
          className={styles.dropdownFieldType}
          itemTemplate={fieldTypeTemplate}
          onChange={e => onChangeFieldType(e.target.value)}
          onMouseDown={event => {
            event.preventDefault();
            event.stopPropagation();
          }}
          optionLabel="fieldType"
          options={fieldTypes}
          required={true}
          placeholder={resources.messages['newFieldTypePlaceHolder']}
          scrollHeight="450px"
          style={{ alignSelf: !fieldDesignerState.isEditing ? 'center' : 'auto' }}
          value={
            fieldDesignerState.fieldTypeValue !== '' ? fieldDesignerState.fieldTypeValue : getFieldTypeValue(fieldType)
          }
        />
        {!isUndefined(fieldDesignerState.fieldTypeValue) &&
        fieldDesignerState.fieldTypeValue.fieldType === 'Codelist' ? (
          <Button
            className={`${styles.codelistButton} p-button-secondary-transparent`}
            label={
              !isUndefined(fieldDesignerState.codelistItems) && !isEmpty(fieldDesignerState.codelistItems)
                ? `${fieldDesignerState.codelistItems}`
                : resources.messages['codelistSelection']
            }
            onClick={() => onCodelistDropdownSelected()}
            style={{ pointerEvents: 'auto' }}
            tooltip={
              !isUndefined(fieldDesignerState.codelistItems) && !isEmpty(fieldDesignerState.codelistItems)
                ? `${fieldDesignerState.codelistItems}`
                : resources.messages['codelistSelection']
            }
            tooltipOptions={{ position: 'top' }}
          />
        ) : isCodelistSelected ? (
          <span style={{ width: '4rem', marginRight: '0.4rem' }}></span>
        ) : null}
        {!addField ? (
          <Button
            className={`p-button-secondary-transparent button ${styles.qcButton}`}
            icon="horizontalSliders"
            onClick={() => dispatchFieldDesigner({ type: 'TOGGLE_QC_MANAGER_VISIBLE', payload: true })}
            style={{ marginLeft: '0.4rem', alignSelf: !fieldDesignerState.isEditing ? 'center' : 'baseline' }}
            tooltip={resources.messages['createFieldQC']}
            tooltipOptions={{ position: 'bottom' }}
          />
        ) : null}
        {!addField ? (
          <a
            draggable={true}
            className={`${styles.button} ${styles.deleteButton}`}
            href="#"
            onClick={e => {
              e.preventDefault();
              onFieldDelete(index);
            }}
            onDragStart={event => {
              event.preventDefault();
              event.stopPropagation();
            }}>
            <FontAwesomeIcon icon={AwesomeIcons('delete')} />
          </a>
        ) : null}
      </div>
      {fieldDesignerState.isCodelistEditorVisible ? (
        <CodelistEditor
          isCodelistEditorVisible={fieldDesignerState.isCodelistEditorVisible}
          onCancelSaveCodelist={onCancelSaveCodelist}
          onSaveCodelist={onSaveCodelist}
          selectedCodelist={fieldDesignerState.codelistItems}
        />
      ) : null}
      {fieldDesignerState.isFKSelectorVisible ? (
        <FKSelector
          isFKSelectorVisible={fieldDesignerState.isFKSelectorVisible}
          onCancelSaveFK={onCancelSaveFK}
          onSaveFK={onSaveFK}
          selectedFK={fieldDesignerState.selectedFK}
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
