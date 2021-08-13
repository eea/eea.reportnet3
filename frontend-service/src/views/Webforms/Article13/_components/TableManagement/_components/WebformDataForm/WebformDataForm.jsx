import { Fragment } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { WebformDataFormFieldEditor } from './_components/WebformDataFormFieldEditor';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const WebformDataForm = ({ colsSchema, datasetId, onChangeForm, selectedRecord, tableColumns }) => {
  const editWebformRecordForm = colsSchema.map(column => {
    if (!isNil(selectedRecord) && !isEmpty(selectedRecord)) {
      const field = selectedRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
      const fieldTitle = tableColumns.filter(tableColumn => tableColumn.field === column.header)[0];
      return (
        <Fragment key={column.field}>
          <div className="p-col-4" style={{ padding: '.75em' }}>
            <label htmlFor={column.field}>
              {!isNil(fieldTitle) && !isEmpty(fieldTitle) ? fieldTitle.header : column.header}
            </label>
          </div>

          <div
            className="p-col-8"
            style={{
              padding: '.5em',
              width: ['CODELIST', 'EXTERNAL_LINK', 'EXTERNAL_LINK', 'LINK'].includes(column.type) ? '30%' : ''
            }}>
            <WebformDataFormFieldEditor
              // autoFocus={i === 0}
              column={column}
              datasetId={datasetId}
              field={column.header}
              fieldValue={field.fieldData[column.field]}
              hasSingle={
                selectedRecord.dataRow
                  .map(
                    field =>
                      TextUtils.areEquals(field.fieldData.type, 'CODELIST') &&
                      Object.values(field.fieldData).includes('Single') &&
                      colsSchema.filter(
                        col => TextUtils.areEquals(col.header, 'ISGROUP') && col.field === field.fieldData.fieldSchemaId
                      ).length > 0
                  )
                  .filter(result => result).length > 0
              }
              onChangeForm={onChangeForm}
              type={column.type}
            />
          </div>
        </Fragment>
      );
    }
    return null;
  });

  return editWebformRecordForm;
};
