import React from 'react';

import { isEmpty } from 'lodash';

import { RecordUtils } from 'ui/views/_functions/Utils';

// import { Calendar } from 'ui/views/_components/Calendar';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';

const FieldEditor = ({
  cells,
  record,
  onEditorValueChange,
  onEditorSubmitValue,
  onEditorValueFocus,
  onEditorKeyChange
}) => {
  let fieldType = {};
  if (!isEmpty(record)) {
    fieldType = record.dataRow.filter(row => Object.keys(row.fieldData)[0] === cells.field)[0].fieldData.type;
  }

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
      case 'CODELIST':
        return (
          <Dropdown
            // className={!isEmbedded ? styles.dropdownFieldType : styles.dropdownFieldTypeDialog}
            // disabled={initialStatus !== 'design'}
            appendTo={document.body}
            onChange={e => onEditorSubmitValue(cells, e.target.value.value, record)}
            onMouseDown={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            optionLabel="itemType"
            options={[{ itemType: '0', value: '0' }, { itemType: '1', value: '1' }, { itemType: '2', value: '2' }]}
            // required={true}
            // placeholder={resources.messages['category']}
            value={RecordUtils.getCellValue(cells, cells.field)}
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
