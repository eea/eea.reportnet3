import { Fragment, useContext, useEffect, useRef } from 'react';

import isEmpty from 'lodash/isEmpty';

import { CharacterCounter } from 'views/_components/CharacterCounter';
import { Dropdown } from 'views/_components/Dropdown';
import { InputText } from 'views/_components/InputText';
import { InputTextarea } from 'views/_components/InputTextarea';
import { MultiSelect } from 'views/_components/MultiSelect';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { RecordUtils } from 'views/_functions/Utils';

import { TextUtils } from 'repositories/_utils/TextUtils';

const WebformDataFormFieldEditor = ({
  autoFocus = false,
  column,
  field,
  fieldValue = '',
  hasSingle = false,
  onChangeForm,
  type
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const inputRef = useRef(null);

  useEffect(() => {
    if (inputRef.current && autoFocus) {
      inputRef.current.element.focus();
    }
  }, [inputRef.current]);

  const getId = item =>
    item.substring(item.indexOf('#') + 1, item.indexOf(' ') !== -1 ? item.indexOf(' ') : item.length);

  const getMaxCharactersByType = type => {
    const longCharacters = 20;
    const decimalCharacters = 40;
    const dateCharacters = 10;
    const datetimeCharacters = 20;
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
      case 'DATETIME':
        return datetimeCharacters;
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
        options={RecordUtils.getCodelistItemsWithEmptyOption(column, resourcesContext.messages['noneCodelist'])}
        value={RecordUtils.getCodelistValue(
          RecordUtils.getCodelistItemsWithEmptyOption(column, resourcesContext.messages['noneCodelist']),
          fieldValue
        )}
      />
    );
  };

  const renderMultiselectCodelist = (field, fieldValue) => {
    if (TextUtils.areEquals(field, 'listofsinglepams') && hasSingle && !isEmpty(fieldValue)) {
      onChangeForm(field, []);
    }

    const options = column.codelistItems.map(codelistItem => ({
      itemType: codelistItem,
      value: TextUtils.areEquals(field, 'listofsinglepams') ? getId(codelistItem) : codelistItem
    }));
    return (
      <MultiSelect
        appendTo={document.body}
        disabled={TextUtils.areEquals(field, 'listofsinglepams') && hasSingle}
        onChange={e => onChangeForm(field, e.value)}
        optionLabel="itemType"
        options={options}
        style={{ height: '34px' }}
        value={RecordUtils.getMultiselectValues(options, fieldValue)}
        valuesSeparator=";"
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
    <div style={{ paddingBottom: '2rem' }}>
      <InputTextarea
        collapsedHeight={75}
        disabled={column.readOnly}
        id={field}
        keyfilter={RecordUtils.getFilter(type)}
        onChange={e => onChangeForm(field, e.target.value)}
        style={{ width: '60%' }}
        value={fieldValue}
      />
      <CharacterCounter
        currentLength={fieldValue.length}
        style={{ position: 'relative', right: '40%', top: '0.25rem' }}
      />
    </div>
  );

  return <Fragment>{renderFieldEditor()}</Fragment>;
};

export { WebformDataFormFieldEditor };
