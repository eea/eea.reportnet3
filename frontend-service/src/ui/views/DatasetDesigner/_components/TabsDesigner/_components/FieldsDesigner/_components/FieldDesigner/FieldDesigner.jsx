import React, { useContext, useEffect, useState, useRef } from 'react';
import { isUndefined, isNull } from 'lodash';

import styles from './FieldDesigner.module.css';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'primereact/checkbox';
import { CodelistsManager } from 'ui/views/_components/CodelistsManager';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Dropdown } from 'ui/views/_components/Dropdown';

import { DatasetService } from 'core/services/Dataset';

export const FieldDesigner = ({
  addField = false,
  checkDuplicates,
  codelistId,
  codelistName,
  codelistVersion,
  datasetId,
  fieldId,
  fieldDescription,
  fieldName,
  fieldRequired,
  fieldType,
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
    { fieldType: 'Codelist', value: 'Codelist', fieldTypeIcon: 'list' }
    // { fieldType: 'URL', value: 'Url', fieldTypeIcon: 'url' },
    // { fieldType: 'LongText', value: 'Long text', fieldTypeIcon: 'text' },
    // { fieldType: 'Link', value: 'Link to another record', fieldTypeIcon: 'link' },
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

  const [animation] = useState('');

  const [fieldDescriptionValue, setFieldDescriptionValue] = useState(fieldDescription);
  const [fieldPreviousTypeValue, setFieldPreviousTypeValue] = useState('');
  const [fieldRequiredValue, setFieldRequiredValue] = useState(fieldRequired);
  const [fieldTypeValue, setFieldTypeValue] = useState(getFieldTypeValue(fieldType));
  const [fieldValue, setFieldValue] = useState(fieldName);
  const [initialFieldValue, setInitialFieldValue] = useState();
  const [initialDescriptionValue, setInitialDescriptionValue] = useState();
  // const [inEffect, setInEffect] = useState();
  const [isCodelistManagerVisible, setIsCodelistManagerVisible] = useState(false);
  const [isQCManagerVisible, setIsQCManagerVisible] = useState(false);
  const [isDragging, setIsDragging] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  // const [position, setPosition] = useState({});
  const [selectedCodelist, setSelectedCodelist] = useState({
    codelistId: !isUndefined(codelistId) ? codelistId : '',
    codelistName: !isUndefined(codelistName) ? codelistName : '',
    codelistVersion: !isUndefined(codelistVersion) ? codelistVersion : ''
  });

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
    // const dropdownFilterInput = fieldRef.current.getElementsByClassName('p-dropdown-filter')[0];
    // const dropdownFilterInputButton = fieldRef.current.getElementsByClassName('p-dropdown-filter-icon')[0];
    // const dropdownFilterWrapper = fieldRef.current.getElementsByClassName('p-dropdown-items-wrapper')[0];
    // const dropdownFilterItems = fieldRef.current.getElementsByClassName('p-dropdown-items')[0];
    const dropdownPanel = fieldRef.current.getElementsByClassName('p-dropdown-panel')[0];
    const childs = document.getElementsByClassName('fieldRow');
    if (!isUndefined(childs)) {
      for (let i = 0; i < childs.length; i++) {
        for (let j = 2; j < childs[i].childNodes.length; j++) {
          if (isDragging) {
            childs[i].childNodes[j].style.pointerEvents = 'none';
            // dropdownFilterInput.style.pointerEvents = 'none';
            // dropdownFilterInputButton.style.pointerEvents = 'none';
            // dropdownFilterWrapper.style.pointerEvents = 'none';
            // dropdownFilterItems.style.pointerEvents = 'none';
            dropdownPanel.style.pointerEvents = 'none';
          } else {
            childs[i].childNodes[j].style.pointerEvents = 'auto';
            // dropdownFilterInput.style.pointerEvents = 'auto';
            // dropdownFilterInputButton.style.pointerEvents = 'auto';
            // dropdownFilterWrapper.style.pointerEvents = 'auto';
            // dropdownFilterItems.style.pointerEvents = 'auto';
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
    const requiredCheckboxes = document.getElementsByClassName('requiredCheckbox');
    if (!isUndefined(requiredCheckboxes)) {
      for (let i = 0; i < requiredCheckboxes.length; i++) {
        for (let j = 0; j < requiredCheckboxes[i].childNodes.length; j++) {
          if (isDragging) {
            requiredCheckboxes[i].childNodes[j].style.pointerEvents = 'none';
          } else {
            requiredCheckboxes[i].childNodes[j].style.pointerEvents = 'auto';
          }
        }
      }
    }
  }, [isDragging]);

  const onChangeFieldType = type => {
    setFieldPreviousTypeValue(fieldTypeValue);
    setFieldTypeValue(type);
    if (type.fieldType.toLowerCase() === 'codelist') {
      onCodelistDropdownSelected(type);
    } else {
      if (fieldId === '-1') {
        if (type !== '') {
          if (!isUndefined(fieldValue) && fieldValue !== '') {
            onFieldAdd(
              recordId,
              parseGeospatialTypes(type.fieldType),
              fieldValue,
              fieldDescriptionValue,
              null,
              null,
              null,
              null,
              fieldRequiredValue
            );
          }
        }
      } else {
        if (type !== '' && type !== fieldValue) {
          fieldUpdate(
            fieldId,
            parseGeospatialTypes(type.fieldType),
            fieldValue,
            fieldDescriptionValue,
            null,
            null,
            null,
            null,
            fieldRequiredValue
          );
        } else {
          if (type !== '') {
            onShowDialogError(resources.messages['emptyFieldTypeMessage'], resources.messages['emptyFieldTypeTitle']);
          }
        }
      }
      setSelectedCodelist({ codelistId: null, codelistName: null, codelistVersion: null });
    }
    onCodelistShow(fieldId, type);
  };

  const onBlurFieldDescription = description => {
    if (!isUndefined(description)) {
      if (!isDragging) {
        //New field
        if (fieldId === '-1') {
          if (
            !isUndefined(fieldTypeValue) &&
            !isNull(fieldTypeValue) &&
            (fieldTypeValue !== '') & !isUndefined(fieldValue) &&
            !isNull(fieldValue) &&
            fieldValue !== ''
          ) {
            onFieldAdd(
              recordId,
              parseGeospatialTypes(fieldTypeValue.fieldType),
              fieldValue,
              fieldDescriptionValue,
              selectedCodelist.codelistId,
              selectedCodelist.codelistName,
              selectedCodelist.codelistVersion
            );
          }
        } else {
          if (description !== initialDescriptionValue) {
            fieldUpdate(
              fieldId,
              parseGeospatialTypes(fieldTypeValue.fieldType),
              fieldValue,
              description,
              selectedCodelist.codelistId,
              selectedCodelist.codelistName,
              selectedCodelist.codelistVersion
            );
          }
        }
      }
    }
  };

  const onBlurFieldName = name => {
    if (!isUndefined(name)) {
      if (!isDragging) {
        if (fieldId === '-1') {
          if (name === '' && fieldTypeValue !== '' && !isUndefined(fieldTypeValue)) {
            onShowDialogError(resources.messages['emptyFieldMessage'], resources.messages['emptyFieldTitle']);
          } else {
            // if (!isUndefined(fieldTypeValue) && !isNull(fieldTypeValue) && fieldTypeValue !== '') {
            if (!checkDuplicates(name, fieldId)) {
              if (!isUndefined(fieldTypeValue) && !isNull(fieldTypeValue) && fieldTypeValue !== '') {
                onFieldAdd(
                  recordId,
                  parseGeospatialTypes(fieldTypeValue.fieldType),
                  fieldValue,
                  fieldDescriptionValue,
                  selectedCodelist.codelistId,
                  selectedCodelist.codelistName,
                  selectedCodelist.codelistVersion
                );
              }
            } else {
              onShowDialogError(
                resources.messages['duplicatedFieldMessage'],
                resources.messages['duplicatedFieldTitle']
              );
              setFieldValue(initialFieldValue);
            }
          }
        } else {
          if (name === '') {
            onShowDialogError(resources.messages['emptyFieldMessage'], resources.messages['emptyFieldTitle']);
            setFieldValue(initialFieldValue);
          } else {
            if (name !== initialFieldValue) {
              if (!checkDuplicates(name, fieldId)) {
                fieldUpdate(
                  fieldId,
                  parseGeospatialTypes(fieldTypeValue.fieldType),
                  fieldValue,
                  fieldDescriptionValue,
                  selectedCodelist.codelistId,
                  selectedCodelist.codelistName,
                  selectedCodelist.codelistVersion
                );
              } else {
                onShowDialogError(
                  resources.messages['duplicatedFieldMessage'],
                  resources.messages['duplicatedFieldTitle']
                );
                setFieldValue(initialFieldValue);
              }
            }
          }
        }
      }
    }
  };

  const onCodelistSelected = (codelistId, codelistName, codelistVersion, codelistItems) => {
    setSelectedCodelist({ codelistId: codelistId, codelistName: codelistName, codelistVersion: codelistVersion });
    if (fieldValue === '') {
      onShowDialogError(resources.messages['emptyFieldMessage'], resources.messages['emptyFieldTitle']);
    } else {
      if (fieldId.toString() === '-1') {
        onFieldAdd(
          recordId,
          'CODELIST',
          fieldValue,
          fieldDescriptionValue,
          codelistId,
          codelistName,
          codelistVersion,
          codelistItems,
          fieldRequiredValue
        );
      } else {
        fieldUpdate(
          fieldId,
          'CODELIST',
          fieldValue,
          fieldDescriptionValue,
          codelistId,
          codelistName,
          codelistVersion,
          codelistItems,
          fieldRequiredValue
        );
      }
    }
    setIsCodelistManagerVisible(false);
  };

  const onCodelistDropdownSelected = fieldType => {
    if (!isUndefined(fieldType)) {
      onCodelistShow(fieldId, fieldType);
    }
    setIsCodelistManagerVisible(true);
  };

  const onFieldAdd = async (
    recordId,
    type,
    value,
    description,
    codelistId,
    codelistName,
    codelistVersion,
    codelistItems,
    required
  ) => {
    try {
      const response = await DatasetService.addRecordFieldDesign(datasetId, {
        recordId,
        name: value,
        type,
        description,
        codelistId,
        required
      });
      if (response.status < 200 || response.status > 299) {
        console.error('Error during field Add');
      } else {
        setFieldRequiredValue(false);
        setFieldValue('');
        setFieldTypeValue('');
        setFieldDescriptionValue('');
        onNewFieldAdd(
          response.data,
          value,
          recordId,
          type,
          description,
          codelistId,
          codelistName,
          codelistVersion,
          codelistItems,
          required
        );
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
            setIsDragging(false);
          }
        }
      }
    }
  };

  const onFieldDragEnd = () => {
    // setPosition(fieldRef.current.getBoundingClientRect());
    if (!isUndefined(onFieldDragAndDropStart)) {
      onFieldDragAndDropStart(undefined);
      inputRef.current.element.focus();
    }
    //   setInEffect(`
    //   @keyframes swap {
    //     0% {
    //     }
    //     100% {
    //       transform: translate(${fieldRef.current.offsetLeft}px, ${fieldRef.current.offsetTop - position.y}px);
    //      }
    // }
    // `);
    // setAnimation(styles.flip);
    setIsDragging(false);
  };

  const onFieldDragEnter = event => {
    event.dataTransfer.dropEffect = 'copy';
  };

  const onFieldDragLeave = event => {
    if (!isUndefined(initialFieldIndexDragged)) {
      if (event.currentTarget.tabIndex !== initialFieldIndexDragged) {
        setIsDragging(false);
      }
    }
  };

  const onFieldDragOver = () => {
    if (!isUndefined(initialFieldIndexDragged)) {
      if (index !== initialFieldIndexDragged) {
        if (!isDragging) {
          if (
            (index === '-1' && totalFields - initialFieldIndexDragged !== 1) ||
            (index !== '-1' && initialFieldIndexDragged - index !== -1)
          ) {
            setIsDragging(true);
          }
        }
      }
    }
  };

  const onFieldDragStart = event => {
    if (isEditing) {
      event.preventDefault();
    }
    //Needed the setData for Firefox
    event.dataTransfer.setData('text/plain', null);
    if (!isUndefined(onFieldDragAndDropStart)) {
      onFieldDragAndDropStart(index);
    }
    // setPosition(fieldRef.current.getBoundingClientRect());
  };

  const onKeyChange = (event, input) => {
    if (event.key === 'Escape') {
      input === 'NAME' ? setFieldValue(initialFieldValue) : setFieldDescriptionValue(initialDescriptionValue);
    } else if (event.key == 'Enter') {
      if (input === 'NAME') {
        onBlurFieldName(event.target.value);
      }
    }
  };

  const onRequiredChange = checked => {
    if (!isDragging) {
      if (fieldId === '-1') {
        if (
          !isUndefined(fieldTypeValue) &&
          !isNull(fieldTypeValue) &&
          (fieldTypeValue !== '') & !isUndefined(fieldValue) &&
          !isNull(fieldValue) &&
          fieldValue !== ''
        ) {
          onFieldAdd(
            recordId,
            parseGeospatialTypes(fieldTypeValue.fieldType),
            fieldValue,
            fieldDescriptionValue,
            selectedCodelist.codelistId,
            selectedCodelist.codelistName,
            selectedCodelist.codelistVersion,
            undefined,
            checked
          );
        }
      } else {
        fieldUpdate(
          fieldId,
          parseGeospatialTypes(fieldTypeValue.fieldType),
          fieldValue,
          fieldDescriptionValue,
          selectedCodelist.codelistId,
          selectedCodelist.codelistName,
          selectedCodelist.codelistVersion,
          undefined,
          checked
        );
      }
    }
    setFieldRequiredValue(checked);
  };

  const codelistDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        label={resources.messages['cancel']}
        icon="cancel"
        onClick={() => {
          if (selectedCodelist.codelistName === '' && selectedCodelist.codelistVersion === '') {
            setFieldTypeValue(fieldPreviousTypeValue);
          }
          setIsCodelistManagerVisible(false);
        }}
      />
    </div>
  );

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

  const fieldUpdate = async (
    fieldSchemaId,
    type,
    value,
    description,
    codelistId,
    codelistName,
    codelistVersion,
    codelistItems,
    required
  ) => {
    console.log({ required, fieldSchemaId });
    try {
      const fieldUpdated = await DatasetService.updateRecordFieldDesign(datasetId, {
        fieldSchemaId,
        name: value,
        type: type,
        description,
        codelistId,
        required
      });
      if (!fieldUpdated) {
        console.error('Error during field Update');
        setFieldValue(initialFieldValue);
      } else {
        onFieldUpdate(
          fieldId,
          value,
          type,
          description,
          codelistId,
          codelistName,
          codelistVersion,
          codelistItems,
          required
        );
      }
    } catch (error) {
      console.error(`Error during field Update: ${error}`);
    }
  };

  const qcDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        className="p-button-secondary-transparent"
        icon="cancel"
        label={resources.messages['close']}
        onClick={() => setIsQCManagerVisible(false)}
      />
    </div>
  );

  return (
    <React.Fragment>
      {/* <style children={inEffect} /> */}
      <div
        draggable={!addField}
        className={`${styles.draggableFieldDiv} fieldRow ${animation}`}
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
        ref={fieldRef}
        // style={{
        //   animationDuration: '400ms',
        //   animationIterationCount: 1,
        //   animationName: 'swap',
        //   animationTimingFunction: 'ease-in'
        // }}
      >
        <div
          className={`${styles.fieldSeparator} ${isDragging ? styles.fieldSeparatorDragging : ''}`}
          // style={{
          //   display: isDragging ? 'block' : 'none',
          //   // animation: 'fadeInOpacity 1s'
          //   animationName: 'fadeInOpacity',
          //   animationIterationCount: 1,
          //   animationTimingFunction: 'ease-in',
          //   animationDuration: '400ms'
          // }}
        ></div>

        <div className="requiredCheckbox">
          {!addField ? (
            <FontAwesomeIcon icon={AwesomeIcons('move')} />
          ) : (
            <div style={{ marginLeft: '32px', display: 'inline-block' }}></div>
          )}
          <label htmlFor={`${fieldId}_check`}>{resources.messages['required']}</label>
          <Checkbox
            checked={fieldRequiredValue}
            inputId={`${fieldId}_check`}
            label="Default"
            onChange={e => {
              onRequiredChange(e.checked);
            }}
            style={{
              marginLeft: '0.4rem',
              marginRight: '0.4rem'
              // alignSelf: !isEditing ? 'center' : 'flex-end',
              // justifySelf: 'flex-end'
            }}
          />
        </div>

        <InputText
          autoFocus={false}
          className={styles.inputField}
          // key={`${fieldId}_${index}`} --> Problem with DOM modification
          ref={inputRef}
          onBlur={e => {
            setIsEditing(false);
            onBlurFieldName(e.target.value);
          }}
          onChange={e => setFieldValue(e.target.value)}
          onFocus={e => {
            setInitialFieldValue(e.target.value);
            setIsEditing(true);
          }}
          onKeyDown={e => onKeyChange(e, 'NAME')}
          placeholder={resources.messages['newFieldPlaceHolder']}
          required={!isUndefined(fieldValue) ? fieldValue === '' : fieldName === ''}
          value={!isUndefined(fieldValue) ? fieldValue : fieldName}
        />
        <InputTextarea
          autoFocus={false}
          collapsedHeight={33}
          expandableOnClick={true}
          className={styles.inputFieldDescription}
          key={fieldId}
          onBlur={e => {
            setIsEditing(false);
            onBlurFieldDescription(e.target.value);
          }}
          onChange={e => setFieldDescriptionValue(e.target.value)}
          onFocus={e => {
            setInitialDescriptionValue(e.target.value);
            setIsEditing(true);
          }}
          onKeyDown={e => onKeyChange(e, 'DESCRIPTION')}
          placeholder={resources.messages['newFieldDescriptionPlaceHolder']}
          value={!isUndefined(fieldDescriptionValue) ? fieldDescriptionValue : fieldDescription}
        />
        <Dropdown
          className={styles.dropdownFieldType}
          // filter={true}
          // filterBy="fieldType,value"
          // filterPlaceholder={resources.messages['newFieldTypePlaceHolder']}
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
          // showClear={true}
          scrollHeight="450px"
          style={{ alignSelf: !isEditing ? 'center' : 'auto' }}
          value={fieldTypeValue !== '' ? fieldTypeValue : getFieldTypeValue(fieldType)}
        />
        {!isUndefined(fieldTypeValue) && fieldTypeValue.fieldType === 'Codelist' ? (
          <Button
            className={`${styles.codelistButton} p-button-secondary-transparent`}
            label={
              !isUndefined(selectedCodelist.codelistName) && selectedCodelist.codelistName !== ''
                ? `${selectedCodelist.codelistName} (${selectedCodelist.codelistVersion})`
                : resources.messages['codelistSelection']
            }
            onClick={() => onCodelistDropdownSelected()}
            style={{ pointerEvents: 'auto' }}
            tooltip={
              !isUndefined(selectedCodelist.codelistName) && selectedCodelist.codelistName !== ''
                ? `${selectedCodelist.codelistName} (${selectedCodelist.codelistVersion})`
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
            onClick={() => setIsQCManagerVisible(true)}
            style={{ marginLeft: '0.4rem', alignSelf: !isEditing ? 'center' : 'baseline' }}
            tooltip={resources.messages['editFieldQC']}
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
      {isCodelistManagerVisible ? (
        <Dialog
          blockScroll={false}
          contentStyle={{ overflow: 'auto' }}
          closeOnEscape={false}
          footer={codelistDialogFooter}
          header={resources.messages['codelistsManager']}
          modal={true}
          onHide={() => setIsCodelistManagerVisible(false)}
          style={{ width: '80%' }}
          visible={isCodelistManagerVisible}
          zIndex={3003}>
          {<CodelistsManager isInDesign={true} onCodelistSelected={onCodelistSelected} />}
        </Dialog>
      ) : null}
      {isQCManagerVisible ? (
        <Dialog
          blockScroll={false}
          contentStyle={{ overflow: 'auto' }}
          closeOnEscape={false}
          footer={qcDialogFooter}
          header={resources.messages['qcManager']}
          modal={true}
          onHide={() => setIsQCManagerVisible(false)}
          style={{ width: '80%' }}
          visible={isQCManagerVisible}
          zIndex={3003}>
          {}
        </Dialog>
      ) : null}
    </React.Fragment>
  );
  // });
};
FieldDesigner.propTypes = {};
