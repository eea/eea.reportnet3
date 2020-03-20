import React, { useContext, useEffect, useState } from 'react';

import { isEmpty, isUndefined } from 'lodash';

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
  fieldPKId,
  fieldSchemaId,
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
  }, []);

  let fieldType = {};
  if (!isEmpty(record)) {
    fieldType = record.dataRow.filter(row => Object.keys(row.fieldData)[0] === cells.field)[0].fieldData.type;
  }

  const onFilter = async filter => {
    console.log({ filter });
    const referencedFieldValues = await DatasetService.getReferencedFieldValues(
      datasetId,
      fieldPKId,
      fieldSchemaId,
      filter
    );
    setLinkItemsOptions(
      referencedFieldValues.map(referencedField => {
        return {
          label: referencedField.value,
          value: referencedField.value
        };
      })
    );
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
      case 'NUMBER':
      case 'POINT':
      case 'COORDINATE_LONG':
      case 'COORDINATE_LAT':
        return 'num';
      case 'TEXT':
        return 'alphanum';
      default:
        return 'alphanum';
    }
  };

  const renderField = type => {
    switch (type) {
      case 'TEXT':
      case 'NUMBER':
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
            type="date"
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
      case 'LINK':
        return (
          <Dropdown
            // className={!isEmbedded ? styles.dropdownFieldType : styles.dropdownFieldTypeDialog}
            // disabled={initialStatus !== 'design'}
            appendTo={document.body}
            filter={true}
            filterPlaceholder={resources.messages['linkFilterPlaceholder']}
            filterBy="label,value"
            onChange={e => {
              setCodelistItemValue(e.target.value.value);
              onEditorValueChange(cells, e.target.value.value);
              onEditorSubmitValue(cells, e.target.value.value, record);
            }}
            onFilterInputChangeBackend={onFilter}
            onMouseDown={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            // optionLabel="itemType"
            // getCodelistItemsWithEmptyOption()
            options={linkItemsOptions}
            // required={true}
            // placeholder={resources.messages['category']}
            value={linkItemsValue}
          />
        );
      case 'CODELIST':
        return (
          <Dropdown
            // className={!isEmbedded ? styles.dropdownFieldType : styles.dropdownFieldTypeDialog}
            // disabled={initialStatus !== 'design'}
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
            // required={true}
            // placeholder={resources.messages['category']}
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
