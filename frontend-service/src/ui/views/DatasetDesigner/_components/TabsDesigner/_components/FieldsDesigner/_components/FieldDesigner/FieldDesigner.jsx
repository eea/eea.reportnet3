import React, { useContext, useEffect, useState, useRef } from 'react';
import { isUndefined, isNull } from 'lodash';

import styles from './FieldDesigner.module.css';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { InputText } from 'ui/views/_components/InputText';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Dropdown } from 'ui/views/_components/Dropdown';

import { DatasetService } from 'core/services/Dataset';

export const FieldDesigner = ({
  addField = false,
  checkDuplicates,
  datasetId,
  fieldId,
  fieldName,
  fieldType,
  index,
  initialFieldIndexDragged,
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
    { fieldType: 'Polygon', value: 'Polygon', fieldTypeIcon: 'polygon' }
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
  const [fieldValue, setFieldValue] = useState(fieldName);
  const [fieldTypeValue, setFieldTypeValue] = useState(getFieldTypeValue(fieldType));
  const [initialFieldValue, setInitialFieldValue] = useState();
  // const [inEffect, setInEffect] = useState();
  const [isDragging, setIsDragging] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  // const [position, setPosition] = useState({});

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
                for (let l = 0; l < dropdownChilds[i].childNodes.length; l++) {
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
  }, [isDragging]);

  const onChangeFieldType = type => {
    setFieldTypeValue(type);
    if (fieldId === '-1') {
      if (type !== '') {
        if (!isUndefined(fieldValue) && fieldValue !== '') {
          onFieldAdd(recordId, parseGeospatialTypes(type.fieldType), fieldValue);
        }
      }
    } else {
      if (type !== '' && type !== fieldValue) {
        fieldUpdate(recordId, fieldId, parseGeospatialTypes(type.fieldType), fieldValue);
      } else {
        if (type !== '') {
          onShowDialogError(resources.messages['emptyFieldTypeMessage'], resources.messages['emptyFieldTypeTitle']);
        }
      }
    }
  };

  const onBlurFieldName = name => {
    if (!isUndefined(name)) {
      if (!isDragging) {
        //New field
        if (fieldId === '-1') {
          if (name === '' && fieldTypeValue !== '' && !isUndefined(fieldTypeValue)) {
            onShowDialogError(resources.messages['emptyFieldMessage'], resources.messages['emptyFieldTitle']);
          } else {
            // if (!isUndefined(fieldTypeValue) && !isNull(fieldTypeValue) && fieldTypeValue !== '') {
            if (!checkDuplicates(name, fieldId)) {
              if (!isUndefined(fieldTypeValue) && !isNull(fieldTypeValue) && fieldTypeValue !== '') {
                onFieldAdd(recordId, parseGeospatialTypes(fieldTypeValue.fieldType), fieldValue);
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
                fieldUpdate(recordId, fieldId, parseGeospatialTypes(fieldTypeValue.fieldType), fieldValue);
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

  const onFieldAdd = async (recordId, type, value) => {
    try {
      const response = await DatasetService.addRecordFieldDesign(datasetId, {
        recordId,
        name: value,
        type: type
      });
      if (response.status < 200 || response.status > 299) {
        console.error('Error during field Add');
      } else {
        setFieldValue('');
        setFieldTypeValue('');
        onNewFieldAdd(response.data, value, recordId, type);
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

  const onKeyChange = event => {
    if (event.key === 'Escape') {
      setFieldValue(initialFieldValue);
    } else if (event.key == 'Enter') {
      event.preventDefault();
      onBlurFieldName(event.target.value);
    }
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

  const fieldUpdate = async (recordId, fieldSchemaId, type, value) => {
    try {
      const fieldUpdated = await DatasetService.updateRecordFieldDesign(datasetId, {
        recordId,
        fieldSchemaId,
        name: value,
        type: type
      });
      if (!fieldUpdated) {
        console.error('Error during field Update');
        setFieldValue(initialFieldValue);
      } else {
        onFieldUpdate(fieldId, value, type);
      }
    } catch (error) {
      console.error(`Error during field Update: ${error}`);
    }
  };

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
        {!addField ? <FontAwesomeIcon icon={AwesomeIcons('move')} /> : <div style={{ width: '32px' }}></div>}
        <InputText
          autoFocus={false}
          className={styles.inputField}
          key={fieldId}
          onBlur={e => {
            setIsEditing(false);
            onBlurFieldName(e.target.value);
          }}
          onChange={e => setFieldValue(e.target.value)}
          onFocus={e => {
            setInitialFieldValue(e.target.value);
            setIsEditing(true);
          }}
          onKeyDown={e => onKeyChange(e, index)}
          placeholder={resources.messages['newFieldPlaceHolder']}
          inputRef={inputRef}
          value={!isUndefined(fieldValue) ? fieldValue : fieldName}
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
          placeholder={resources.messages['newFieldTypePlaceHolder']}
          // showClear={true}
          scrollHeight="450px"
          value={fieldTypeValue !== '' ? fieldTypeValue : getFieldTypeValue(fieldType)}
        />
        {!addField ? (
          <a
            draggable={true}
            className={styles.deleteButton}
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
    </React.Fragment>
  );
  // });
};
FieldDesigner.propTypes = {};
