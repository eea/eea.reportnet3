import React from 'react';
import { isUndefined, isNull } from 'lodash';

import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';

const DataForm = ({ colsSchema, formType, editDialogVisible, addDialogVisible, onChangeForm, records }) => {
  const editRecordForm = colsSchema.map((column, i) => {
    console.log({ column });
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
                  <Dropdown
                    // className={!isEmbedded ? styles.dropdownFieldType : styles.dropdownFieldTypeDialog}
                    // disabled={initialStatus !== 'design'}
                    // onChange={e => onEditorPropertiesInputChange(e.target.value.value, 'codelistCategoryId')}
                    appendTo={document.body}
                    optionLabel="itemType"
                    options={[
                      { itemType: '0', value: '0' },
                      { itemType: '1', value: '1' },
                      { itemType: '2', value: '2' }
                    ]}
                    // required={true}
                    // placeholder={resources.messages['category']}
                    value={
                      isNull(field.fieldData[column.field]) || isUndefined(field.fieldData[column.field])
                        ? ''
                        : field.fieldData[column.field]
                    }
                  />
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
                  <Dropdown
                    // className={!isEmbedded ? styles.dropdownFieldType : styles.dropdownFieldTypeDialog}
                    // disabled={initialStatus !== 'design'}
                    // onChange={e => onEditorPropertiesInputChange(e.target.value.value, 'codelistCategoryId')}
                    appendTo={document.body}
                    optionLabel="itemType"
                    options={[
                      { itemType: '0', value: '0' },
                      { itemType: '1', value: '1' },
                      { itemType: '2', value: '2' }
                    ]}
                    // required={true}
                    // placeholder={resources.messages['category']}
                    value={{ itemType: '1', value: '1' }}
                  />
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
