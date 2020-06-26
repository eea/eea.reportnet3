import React from 'react';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import styles from './DataForm.module.css';

import { Button } from 'ui/views/_components/Button';
import { DataFormFieldEditor } from './_components/DataFormFieldEditor';

const DataForm = ({
  addDialogVisible,
  colsSchema,
  datasetId,
  editDialogVisible,
  formType,
  getTooltipMessage,
  onChangeForm,
  records,
  onShowFieldInfo
}) => {
  const editRecordForm = colsSchema.map((column, i) => {
    //Avoid row id Field and dataSetPartitionId
    if (editDialogVisible) {
      if (i < colsSchema.length - 2) {
        if (!isUndefined(records.editedRecord.dataRow)) {
          const field = records.editedRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
          return (
            <React.Fragment key={column.field}>
              <div className="p-col-4" style={{ padding: '.75em' }}>
                <label htmlFor={column.field}>{`${column.header}${
                  column.type.toUpperCase() === 'DATE' ? ' (YYYY-MM-DD)' : ''
                }`}</label>
                <Button
                  className={`${styles.columnInfoButton} p-button-rounded p-button-secondary-transparent`}
                  icon="infoCircle"
                  onClick={() => {
                    onShowFieldInfo(column.header, true);
                  }}
                  tooltip={getTooltipMessage(column)}
                  tooltipOptions={{ position: 'top' }}
                />
              </div>
              <div
                className="p-col-8"
                style={{
                  padding: '.5em',
                  width:
                    column.type === 'DATE' ||
                    column.type === 'CODELIST' ||
                    column.type === 'MULTISELECT_CODELIST' ||
                    column.type === 'LINK'
                      ? '30%'
                      : ''
                }}>
                <DataFormFieldEditor
                  column={column}
                  datasetId={datasetId}
                  field={column.field}
                  fieldValue={
                    isNull(field.fieldData[column.field]) || isUndefined(field.fieldData[column.field])
                      ? ''
                      : field.fieldData[column.field]
                  }
                  onChangeForm={onChangeForm}
                  type={column.type}></DataFormFieldEditor>
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
                <label htmlFor={column.field}>{`${column.header}${
                  column.type.toUpperCase() === 'DATE' ? ' (YYYY-MM-DD)' : ''
                }`}</label>
                <Button
                  className={`${styles.columnInfoButton} p-button-rounded p-button-secondary-transparent`}
                  icon="infoCircle"
                  onClick={() => {
                    onShowFieldInfo(column.header, true);
                  }}
                  tooltip={getTooltipMessage(column)}
                  tooltipOptions={{ position: 'top' }}
                />
              </div>
              <div
                className="p-col-8"
                style={{
                  padding: '.5em',
                  width:
                    column.type === 'DATE' || column.type === 'CODELIST' || column.type === 'MULTISELECT_CODELIST'
                      ? '30%'
                      : ''
                }}>
                <DataFormFieldEditor
                  column={column}
                  datasetId={datasetId}
                  field={column.field}
                  fieldValue={
                    isNull(field.fieldData[column.field]) || isUndefined(field.fieldData[column.field])
                      ? ''
                      : field.fieldData[column.field]
                  }
                  onChangeForm={onChangeForm}
                  type={column.type}></DataFormFieldEditor>
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
