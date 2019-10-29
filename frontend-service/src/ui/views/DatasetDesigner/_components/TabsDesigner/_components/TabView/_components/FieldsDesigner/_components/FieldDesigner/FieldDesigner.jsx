import React, { useEffect, useState, useContext } from 'react';
import { isUndefined, isNull } from 'lodash';
import PropTypes from 'prop-types';

import styles from './FieldDesigner.module.css';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { InputText } from 'ui/views/_components/InputText';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Dropdown } from 'ui/views/_components/Dropdown';

export const FieldDesigner = ({ fieldId, fieldName, fieldType }) => {
  const [fieldValue, setFieldValue] = useState();
  const [fieldTypeValue, setFieldTypeValue] = useState();

  const resources = useContext(ResourcesContext);

  const fieldTypes = [
    { fieldType: 'Number', value: 'Number', fieldTypeIcon: 'number' },
    { fieldType: 'Date', value: 'Date', fieldTypeIcon: 'calendar' },
    { fieldType: 'URL', value: 'Url', fieldTypeIcon: 'url' },
    { fieldType: 'Geospatial', value: 'Geospatial object', fieldTypeIcon: 'map' },
    { fieldType: 'Text', value: 'Single line text', fieldTypeIcon: 'italic' },
    { fieldType: 'LongText', value: 'Long text', fieldTypeIcon: 'text' },
    { fieldType: 'Link', value: 'Link to another record', fieldTypeIcon: 'link' },
    { fieldType: 'LinkData', value: 'Link to a data collection', fieldTypeIcon: 'linkData' },
    { fieldType: 'Percentage', value: 'Percentage', fieldTypeIcon: 'percentage' },
    { fieldType: 'Formula', value: 'Formula', fieldTypeIcon: 'formula' },
    { fieldType: 'Fixed', value: 'Fixed select list', fieldTypeIcon: 'list' },
    { fieldType: 'Email', value: 'Email', fieldTypeIcon: 'email' },
    { fieldType: 'Attachement', value: 'Attachement', fieldTypeIcon: 'clip' }
  ];

  const onChangeFieldType = type => {
    if (type !== '') {
      setFieldTypeValue(type);
    }
  };

  const onBlurFieldName = name => {
    if (name !== '') {
    }
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

  const getFieldTypeValue = value => {
    if (value.toUpperCase() === 'COORDINATE_LONG' || value.toUpperCase() === 'COORDINATE_LAT') {
      value = 'Geospatial';
    }
    return fieldTypes.filter(field => field.fieldType.toUpperCase() === value.toUpperCase())[0];
  };

  return (
    <React.Fragment>
      <InputText
        autoFocus={false}
        className={styles.inputField}
        key={fieldId}
        onBlur={e => {
          onBlurFieldName(e.target.value);
          // if (titleHeader !== '') {
          //   setTitleHeader(onTabBlur(e.target.value, index));
          //   setEditingHeader(false);
          // } else {
          //   if (!isUndefined(onTabNameError)) {
          //     onTabNameError();
          //     //Set focus on input if the name is empty
          //     document.getElementsByClassName('p-inputtext p-component')[0].focus();
          //   }
          // }
        }}
        onChange={e => setFieldValue(e.target.value)}
        //onKeyDown={e => onKeyChange(e, index)}
        placeholder={resources.messages['newFieldPlaceHolder']}
        value={!isUndefined(fieldValue) ? fieldValue : fieldName}
      />
      <Dropdown
        className={styles.dropdownFieldType}
        filter={true}
        filterBy="fieldType,value"
        filterPlaceholder={resources.messages['newFieldTypePlaceHolder']}
        itemTemplate={fieldTypeTemplate}
        onChange={e => onChangeFieldType(e.target.value)}
        optionLabel="fieldType"
        options={fieldTypes}
        placeholder={resources.messages['newFieldTypePlaceHolder']}
        // showClear={true}
        scrollHeight="450px"
        value={!isUndefined(fieldTypeValue) ? fieldTypeValue : getFieldTypeValue(fieldType)}
      />
    </React.Fragment>
  );
  // });
};
FieldDesigner.propTypes = {};
