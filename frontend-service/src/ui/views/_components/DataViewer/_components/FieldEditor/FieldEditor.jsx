import React, { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

// import { Calendar } from 'ui/views/_components/Calendar';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { RecordUtils } from 'ui/views/_functions/Utils';

const FieldEditor = ({
  cells,
  colsSchema,
  datasetId,
  onEditorKeyChange,
  onEditorSubmitValue,
  onEditorValueChange,
  onEditorValueFocus,
  record
}) => {
  const resources = useContext(ResourcesContext);
  const [codelistItemsOptions, setCodelistItemsOptions] = useState([]);
  const [codelistItemValue, setCodelistItemValue] = useState();
  const [linkItemsOptions, setLinkItemsOptions] = useState([]);

  const [linkItemsValue, setLinkItemsValue] = useState([]);

  useEffect(() => {
    if (!isUndefined(colsSchema)) setCodelistItemsOptions(RecordUtils.getCodelistItems(colsSchema, cells.field));
    setCodelistItemValue(RecordUtils.getCellValue(cells, cells.field).toString());
    setLinkItemsValue(RecordUtils.getCellValue(cells, cells.field).toString());
  }, []);

  useEffect(() => {
    onFilter(RecordUtils.getCellValue(cells, cells.field));
  }, []);

  let fieldType = {};
  if (!isEmpty(record)) {
    fieldType = record.dataRow.filter(row => Object.keys(row.fieldData)[0] === cells.field)[0].fieldData.type;
  }

  const onFilter = async filter => {
    const colSchema = colsSchema.filter(colSchema => colSchema.field === cells.field)[0];
    if (isNil(colSchema) || isNil(colSchema.referencedField)) {
      return;
    }
    const referencedFieldValues = await DatasetService.getReferencedFieldValues(
      datasetId,
      isUndefined(colSchema.referencedField.name)
        ? colSchema.referencedField.idPk
        : colSchema.referencedField.referencedField.fieldSchemaId,
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
    setLinkItemsOptions(linkItems);
  };

  const getCodelistItemsWithEmptyOption = () => {
    const codelistsItems = RecordUtils.getCodelistItems(colsSchema, cells.field);
    codelistsItems.unshift({
      itemType: resources.messages['noneCodelist'],
      value: ''
    });
    return codelistsItems;
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

  const renderField = type => {
    const longCharacters = 20;
    const decimalCharacters = 40;
    const dateCharacters = 10;
    const textCharacters = 5000;
    const longTextCharacters = 10000;
    const emailCharacters = 256;
    const phoneCharacters = 256;
    const urlCharacters = 5000;

    switch (type) {
      case 'TEXT':
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            onKeyDown={e => onEditorKeyChange(cells, e, record)}
            type="text"
            value={RecordUtils.getCellValue(cells, cells.field)}
            maxLength={textCharacters}
          />
        );
      case 'LONG_TEXT':
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            onKeyDown={e => onEditorKeyChange(cells, e, record)}
            type="text"
            value={RecordUtils.getCellValue(cells, cells.field)}
            maxLength={longTextCharacters}
          />
        );
      case 'NUMBER_INTEGER':
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            onKeyDown={e => onEditorKeyChange(cells, e, record)}
            maxLength={longCharacters}
            value={RecordUtils.getCellValue(cells, cells.field)}
          />
        );
      case 'NUMBER_DECIMAL':
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            onKeyDown={e => onEditorKeyChange(cells, e, record)}
            maxLength={decimalCharacters}
            value={RecordUtils.getCellValue(cells, cells.field)}
          />
        );
      case 'POINT':
      case 'COORDINATE_LONG':
      case 'COORDINATE_LAT':
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            onKeyDown={e => onEditorKeyChange(cells, e, record)}
            type="text"
            value={RecordUtils.getCellValue(cells, cells.field)}
          />
        );
      case 'DATE':
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            // type="date"
            maxLength={dateCharacters}
            placeHolder="YYYY-MM-DD"
            value={RecordUtils.getCellValue(cells, cells.field)}
          />
          //   <Calendar
          //     //   onChange={e => onEditorValueChange(cells, e.value)}
          //     dateFormat="yy-mm-dd"
          //     monthNavigator={true}
          //     value={RecordUtils.getCellValue(cells, cells.field)}
          //     yearNavigator={true}
          //     yearRange="2010:2030"
          //   />
        );
      case 'EMAIL':
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            maxLength={emailCharacters}
            value={RecordUtils.getCellValue(cells, cells.field)}
          />
        );
      case 'URL':
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            maxLength={urlCharacters}
            value={RecordUtils.getCellValue(cells, cells.field)}
          />
        );
      case 'PHONE':
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            maxLength={phoneCharacters}
            value={RecordUtils.getCellValue(cells, cells.field)}
          />
        );
      case 'LINK':
        return (
          <Dropdown
            appendTo={document.body}
            currentValue={RecordUtils.getCellValue(cells, cells.field)}
            filter={true}
            filterPlaceholder={resources.messages['linkFilterPlaceholder']}
            filterBy="itemType,value"
            onChange={e => {
              setLinkItemsValue(e.target.value.value);
              onEditorValueChange(cells, e.target.value.value);
              onEditorSubmitValue(cells, e.target.value.value, record);
            }}
            onFilterInputChangeBackend={onFilter}
            onMouseDown={e => {
              onEditorValueFocus(cells, e.target.value);
            }}
            optionLabel="itemType"
            options={linkItemsOptions}
            showFilterClear={true}
            value={RecordUtils.getLinkValue(linkItemsOptions, linkItemsValue)}
          />
        );
      case 'CODELIST':
        return (
          <Dropdown
            appendTo={document.body}
            onChange={e => {
              setCodelistItemValue(e.target.value.value);
              onEditorValueChange(cells, e.target.value.value);
              onEditorSubmitValue(cells, e.target.value.value, record);
            }}
            onMouseDown={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            optionLabel="itemType"
            options={getCodelistItemsWithEmptyOption()}
            value={RecordUtils.getCodelistValue(codelistItemsOptions, codelistItemValue)}
          />
        );
      default:
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            onKeyDown={e => onEditorKeyChange(cells, e, record)}
            type="text"
            value={RecordUtils.getCellValue(cells, cells.field)}
          />
        );
    }
  };

  return !isEmpty(fieldType) ? renderField(fieldType) : null;
};

export { FieldEditor };
