import React, { useContext } from 'react';
import { isUndefined, isNull } from 'lodash';

import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { RecordUtils } from 'ui/views/_functions/Utils';

const DataForm = ({ colsSchema, formType, editDialogVisible, addDialogVisible, onChangeForm, records }) => {
  const resources = useContext(ResourcesContext);

  const getCodelistItemsWithEmptyOption = (colsSchema, field) => {
    const codelistsItems = RecordUtils.getCodelistItems(colsSchema, field);
    codelistsItems.unshift({
      itemType: resources.messages['noneCodelist'],
      value: ''
    });
    return codelistsItems;
  };

  const renderDropdown = (field, fieldValue) => {
    return (
      <Dropdown
        appendTo={document.body}
        onChange={e => {
          onChangeForm(field, e.target.value.value);
        }}
        optionLabel="itemType"
        options={getCodelistItemsWithEmptyOption(colsSchema, field)}
        value={RecordUtils.getCodelistValue(RecordUtils.getCodelistItems(colsSchema, field), fieldValue)}
      />
    );
  };

  const editRecordForm = colsSchema.map((column, i) => {
    //Avoid row id Field and dataSetPartitionId
    if (editDialogVisible) {
      if (i < colsSchema.length - 2) {
        if (!isUndefined(records.editedRecord.dataRow)) {
          const field = records.editedRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
          return (
            <React.Fragment key={column.field}>
              <div className="p-col-4" style={{ padding: '.75em' }}>
                <label htmlFor={column.field}>{column.header}</label>
              </div>
              <div className="p-col-8" style={{ padding: '.5em' }}>
                {column.type === 'CODELIST' ? (
                  renderDropdown(
                    column.field,
                    isNull(field.fieldData[column.field]) || isUndefined(field.fieldData[column.field])
                      ? ''
                      : field.fieldData[column.field]
                  )
                ) : (
                  <InputText
                    id={column.field}
                    value={
                      isNull(field.fieldData[column.field]) || isUndefined(field.fieldData[column.field])
                        ? ''
                        : field.fieldData[column.field]
                    }
                    onChange={e => onChangeForm(column.field, e.target.value)}
                  />
                )}
              </div>
            </React.Fragment>
          );
        }
      }
    }
  });

  const newRecordForm = colsSchema.map((column, i) => {
    if (addDialogVisible) {
      if (i < colsSchema.length - 2) {
        if (!isUndefined(records.newRecord.dataRow)) {
          const field = records.newRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
          return (
            <React.Fragment key={column.field}>
              <div className="p-col-4" style={{ padding: '.75em' }}>
                <label htmlFor={column.field}>{column.header}</label>
              </div>
              <div className="p-col-8" style={{ padding: '.5em' }}>
                {column.type === 'CODELIST' ? (
                  renderDropdown(
                    column.field,
                    isNull(field.fieldData[column.field]) || isUndefined(field.fieldData[column.field])
                      ? ''
                      : field.fieldData[column.field]
                  )
                ) : (
                  <InputText id={column.field} onChange={e => onChangeForm(column.field, e.target.value, field)} />
                )}
              </div>
            </React.Fragment>
          );
        }
      }
    }
  });

  return formType === 'EDIT' ? editRecordForm : newRecordForm;
};

export { DataForm };
