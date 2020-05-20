import React, { useContext, useEffect, useState } from 'react';

import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { Calendar } from 'ui/views/_components/Calendar';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { MultiSelect } from 'primereact/multiselect';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { RecordUtils } from 'ui/views/_functions/Utils';

const DataFormFieldEditor = ({ column, datasetId, field, fieldValue = '', onChangeForm, type }) => {
  const resources = useContext(ResourcesContext);
  const [columnWithLinks, setColumnWithLinks] = useState([]);

  useEffect(() => {
    if (!isUndefined(fieldValue)) {
      if (type === 'LINK') onLoadColsSchema(fieldValue);
    }
  }, []);

  const onFilter = async filter => {
    onLoadColsSchema(filter);
  };

  const onLoadColsSchema = async filter => {
    const inmColumn = { ...column };
    const linkItems = await getLinkItemsWithEmptyOption(filter, type, column.referencedField);
    inmColumn.linkItems = linkItems;
    setColumnWithLinks(inmColumn);
  };

  const formatDate = date => {
    let d = new Date(date),
      month = '' + (d.getMonth() + 1),
      day = '' + d.getDate(),
      year = d.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('-');
  };

  const getFilter = type => {
    switch (type) {
      case 'NUMBER_INTEGER':
        return 'int';
      case 'NUMBER_DECIMAL':
      case 'POINT':
      case 'COORDINATE_LONG':
      case 'COORDINATE_LAT':
        return 'num';
      case 'DATE':
        return 'date';
      case 'TEXT':
      case 'LONG_TEXT':
        return 'any';
      case 'EMAIL':
        return 'email';
      case 'PHONE':
        return 'phone';
      // case 'URL':
      //   return 'url';
      default:
        return 'any';
    }
  };

  const getLinkItemsWithEmptyOption = async (filter, type, referencedField) => {
    if (isNil(type) || type.toUpperCase() !== 'LINK' || isNil(referencedField)) {
      return [];
    }
    const referencedFieldValues = await DatasetService.getReferencedFieldValues(
      datasetId,
      isUndefined(referencedField.name) ? referencedField.idPk : referencedField.referencedField.fieldSchemaId,
      filter
    );
    const linkItems = referencedFieldValues
      .map(referencedField => {
        return {
          itemType: referencedField.value,
          value: referencedField.value
        };
      })
      .sort((a, b) => a.value - b.value);
    linkItems.unshift({
      itemType: resources.messages['noneCodelist'],
      value: ''
    });
    return linkItems;
  };

  const getCodelistItemsWithEmptyOption = () => {
    const codelistItems = column.codelistItems.map(codelistItem => {
      return { itemType: codelistItem, value: codelistItem };
    });

    codelistItems.unshift({
      itemType: resources.messages['noneCodelist'],
      value: ''
    });
    return codelistItems;
  };

  const renderCodelistDropdown = (field, fieldValue) => (
    <Dropdown
      appendTo={document.body}
      onChange={e => {
        onChangeForm(field, e.target.value.value);
      }}
      optionLabel="itemType"
      options={getCodelistItemsWithEmptyOption()}
      value={RecordUtils.getCodelistValue(RecordUtils.getCodelistItemsInSingleColumn(column), fieldValue)}
    />
  );

  const renderMultiselectCodelist = (field, fieldValue) => {
    return (
      <MultiSelect
        maxSelectedLabels={10}
        onChange={e => onChangeForm(field, e.value)}
        options={column.codelistItems.map(codelistItem => {
          return { itemType: codelistItem, value: codelistItem };
        })}
        optionLabel="itemType"
        styles={{ border: 'var(--dropdown-border)', borderColor: 'red' }}
        value={RecordUtils.getMultiselectValues(RecordUtils.getCodelistItemsInSingleColumn(column), fieldValue)}
        // hasSelectedItemsLabel={false}
      />
    );
  };

  const getMaxCharactersByType = type => {
    const longCharacters = 20;
    const decimalCharacters = 40;
    const dateCharacters = 10;
    const textCharacters = 5000;
    const longTextCharacters = 10000;
    const emailCharacters = 256;
    const phoneCharacters = 256;
    const urlCharacters = 5000;

    switch (type) {
      case 'NUMBER_INTEGER':
        return longCharacters;
      case 'NUMBER_DECIMAL':
        return decimalCharacters;
      case 'POINT':
      case 'COORDINATE_LONG':
      case 'COORDINATE_LAT':
        return textCharacters;
      case 'DATE':
        return dateCharacters;
      case 'TEXT':
        return textCharacters;
      case 'LONG_TEXT':
        return longTextCharacters;
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

  const renderFieldEditor = () =>
    type === 'CODELIST' ? (
      renderCodelistDropdown(field, fieldValue)
    ) : type === 'MULTISELECT_CODELIST' ? (
      renderMultiselectCodelist(field, fieldValue)
    ) : type === 'LINK' ? (
      renderLinkDropdown(field, fieldValue)
    ) : type === 'DATE' ? (
      renderCalendar(field, fieldValue)
    ) : (
      <InputText
        id={field}
        keyfilter={getFilter(type)}
        maxLength={getMaxCharactersByType(type)}
        onChange={e => onChangeForm(field, e.target.value)}
        value={fieldValue}
        // type={type === 'DATE' ? 'date' : 'text'}
        placeHolder={type === 'DATE' ? 'YYYY-MM-DD' : ''}
        type="text"
      />
    );

  const renderCalendar = (field, fieldValue) => {
    return (
      <Calendar
        onChange={e => onChangeForm(field, formatDate(e.target.value))}
        appendTo={document.getElementById('pr_id_11')}
        dateFormat="yy-mm-dd"
        monthNavigator={true}
        style={{ width: '60px' }}
        value={new Date(formatDate(fieldValue))}
        yearNavigator={true}
        yearRange="2010:2030"
      />
    );
  };

  const renderLinkDropdown = (field, fieldValue) => (
    <Dropdown
      appendTo={document.body}
      currentValue={fieldValue}
      filter={true}
      filterPlaceholder={resources.messages['linkFilterPlaceholder']}
      filterBy="itemType,value"
      onChange={e => {
        onChangeForm(field, e.target.value.value);
      }}
      onFilterInputChangeBackend={onFilter}
      optionLabel="itemType"
      options={columnWithLinks.linkItems}
      showFilterClear={true}
      value={RecordUtils.getLinkValue(columnWithLinks.linkItems, fieldValue)}
    />
  );

  return <React.Fragment>{renderFieldEditor()}</React.Fragment>;
};

export { DataFormFieldEditor };
