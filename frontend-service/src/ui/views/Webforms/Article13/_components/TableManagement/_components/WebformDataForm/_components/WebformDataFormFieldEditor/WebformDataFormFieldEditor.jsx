import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';

// import isEmpty from 'lodash/isEmpty';
import cloneDeep from 'lodash/cloneDeep';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

// import styles from './WebformDataFormFieldEditor.module.scss';

import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { MultiSelect } from 'ui/views/_components/MultiSelect';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { RecordUtils } from 'ui/views/_functions/Utils';

const WebformDataFormFieldEditor = ({
  autoFocus = false,
  column,
  datasetId,
  field,
  fieldValue = '',
  onChangeForm,
  type
}) => {
  const resources = useContext(ResourcesContext);
  const inputRef = useRef(null);

  useEffect(() => {}, []);

  useEffect(() => {
    if (inputRef.current && autoFocus) {
      inputRef.current.element.focus();
    }
  }, [inputRef.current]);

  const getMaxCharactersByType = type => {
    const longCharacters = 20;
    const decimalCharacters = 40;
    const dateCharacters = 10;
    const textCharacters = 10000;
    const richTextCharacters = 10000;
    const emailCharacters = 256;
    const phoneCharacters = 256;
    const urlCharacters = 5000;

    switch (type) {
      case 'NUMBER_INTEGER':
        return longCharacters;
      case 'NUMBER_DECIMAL':
        return decimalCharacters;
      case 'POINT':
        return textCharacters;
      case 'DATE':
        return dateCharacters;
      case 'TEXT':
      case 'TEXTAREA':
        return textCharacters;
      case 'RICH_TEXT':
        return richTextCharacters;
      case 'EMAIL':
        return emailCharacters;
      case 'PHONE':
        return phoneCharacters;
      case 'URL':
        return urlCharacters;
      default:
        return null;
    }
  };

  const renderCodelistDropdown = (field, fieldValue) => {
    return (
      <Dropdown
        appendTo={document.body}
        disabled={column.readOnly}
        onChange={e => onChangeForm(field, e.target.value.value)}
        optionLabel="itemType"
        options={RecordUtils.getCodelistItemsWithEmptyOption(column, resources.messages['noneCodelist'])}
        value={RecordUtils.getCodelistValue(RecordUtils.getCodelistItemsInSingleColumn(column), fieldValue)}
      />
    );
  };

  const renderMultiselectCodelist = (field, fieldValue) => {
    return (
      <MultiSelect
        appendTo={document.body}
        onChange={e => onChangeForm(field, e.value)}
        optionLabel="itemType"
        options={column.codelistItems.sort().map(codelistItem => ({ itemType: codelistItem, value: codelistItem }))}
        style={{ height: '34px' }}
        value={RecordUtils.getMultiselectValues(RecordUtils.getCodelistItemsInSingleColumn(column), fieldValue)}
      />
    );
  };

  const renderFieldEditor = () =>
    type === 'MULTISELECT_CODELIST' ? (
      renderMultiselectCodelist(field, fieldValue)
    ) : type === 'CODELIST' ? (
      renderCodelistDropdown(field, fieldValue)
    ) : type === 'TEXTAREA' ? (
      renderTextarea(field, fieldValue)
    ) : (
      <InputText
        disabled={column.readOnly}
        id={field}
        keyfilter={RecordUtils.getFilter(type)}
        maxLength={getMaxCharactersByType(type)}
        onChange={e => onChangeForm(field, e.target.value)}
        ref={inputRef}
        style={{ width: '35%' }}
        type="text"
        value={fieldValue}
      />
    );

  const renderTextarea = (field, fieldValue) => (
    <InputTextarea
      collapsedHeight={75}
      disabled={column.readOnly}
      id={field}
      keyfilter={RecordUtils.getFilter(type)}
      maxLength={getMaxCharactersByType(type)}
      onChange={e => onChangeForm(field, e.target.value)}
      style={{ width: '60%' }}
      value={fieldValue}
    />
  );

  return <Fragment>{renderFieldEditor()}</Fragment>;
};

export { WebformDataFormFieldEditor };
