import React, { Fragment, useContext, useEffect, useState } from 'react';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './DataForm.module.css';

import { Button } from 'ui/views/_components/Button';
import { DataFormFieldEditor } from './_components/DataFormFieldEditor';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const DataForm = ({
  addDialogVisible,
  colsSchema,
  datasetId,
  editDialogVisible,
  formType,
  getTooltipMessage,
  hasWritePermissions,
  onChangeForm,
  onShowCoordinateError = () => {},
  records,
  reporting,
  onShowFieldInfo
}) => {
  const resources = useContext(ResourcesContext);

  const [fieldsWithError, setFieldsWithError] = useState([]);
  useEffect(() => {
    onShowCoordinateError(fieldsWithError.length);
  }, [fieldsWithError]);

  const onCheckCoordinateFieldsError = (id, hasError) => {
    let inmFieldsWithError = [...fieldsWithError];
    if (hasError) {
      if (!inmFieldsWithError.includes(id)) {
        inmFieldsWithError.push(id);
      }
    } else {
      if (inmFieldsWithError.includes(id)) {
        inmFieldsWithError = inmFieldsWithError.filter(error => error !== id);
      }
    }
    setFieldsWithError(inmFieldsWithError);
  };

  const allAttachments = () => {
    const notAttachment = colsSchema.filter(col => col.type && col.type.toUpperCase() !== 'ATTACHMENT');
    return notAttachment.length === 0;
  };

  const editRecordForm = colsSchema.map((column, i) => {
    //Avoid row id Field and dataSetPartitionId
    if (editDialogVisible) {
      if (i < colsSchema.length - 2) {
        if (!isUndefined(records.editedRecord.dataRow)) {
          const field = records.editedRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
          return (
            <Fragment key={column.field}>
              {column.type.toUpperCase() !== 'ATTACHMENT' && (
                <div className="p-col-4" style={{ padding: '.75em' }}>
                  <label htmlFor={column.field}>{`${column.header}${
                    column.type.toUpperCase() === 'DATE' ? ' (YYYY-MM-DD)' : ''
                  }`}</label>
                  <Button
                    className={`${styles.columnInfoButton} p-button-rounded p-button-secondary-transparent`}
                    icon="infoCircle"
                    onClick={() => onShowFieldInfo(column.header, true)}
                    tabIndex="-1"
                    tooltip={getTooltipMessage(column)}
                    tooltipOptions={{ position: 'top' }}
                  />
                </div>
              )}
              <div
                className="p-col-8"
                style={{
                  padding: column.type.toUpperCase() !== 'ATTACHMENT' ? '.5em' : '0',
                  width:
                    column.type === 'DATE' ||
                    column.type === 'CODELIST' ||
                    column.type === 'MULTISELECT_CODELIST' ||
                    column.type === 'LINK'
                      ? '30%'
                      : ''
                }}>
                <DataFormFieldEditor
                  autoFocus={i === 0}
                  column={column}
                  datasetId={datasetId}
                  field={column.field}
                  fieldValue={
                    isNil(field.fieldData[column.field])
                      ? column.type === 'POINT'
                        ? `{"type": "Feature", "geometry": {"type":"Point","coordinates":[55.6811608,12.5844761]}, "properties": {"rsid": "EPSG:4326"}}`
                        : ''
                      : field.fieldData[column.field]
                  }
                  hasWritePermissions={hasWritePermissions}
                  isVisible={editDialogVisible}
                  onChangeForm={onChangeForm}
                  onCheckCoordinateFieldsError={onCheckCoordinateFieldsError}
                  reporting={reporting}
                  type={column.type}
                />
              </div>
            </Fragment>
          );
        }
      }
    }
  });

  const newRecordForm = !allAttachments() ? (
    colsSchema.map((column, i) => {
      if (addDialogVisible) {
        if (i < colsSchema.length - 2) {
          if (!isUndefined(records.newRecord.dataRow)) {
            const field = records.newRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
            return (
              <Fragment key={column.field}>
                {column.type.toUpperCase() !== 'ATTACHMENT' && (
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
                      tabIndex="-1"
                      tooltip={getTooltipMessage(column)}
                      tooltipOptions={{ position: 'top' }}
                    />
                  </div>
                )}
                <div
                  className="p-col-8"
                  style={{
                    padding: column.type.toUpperCase() !== 'ATTACHMENT' ? '.5em' : '0',
                    width:
                      column.type === 'DATE' || column.type === 'CODELIST' || column.type === 'MULTISELECT_CODELIST'
                        ? '30%'
                        : ''
                  }}>
                  <DataFormFieldEditor
                    autoFocus={i === 0}
                    column={column}
                    datasetId={datasetId}
                    field={column.field}
                    fieldValue={isNil(field.fieldData[column.field]) ? '' : field.fieldData[column.field]}
                    hasWritePermissions={hasWritePermissions}
                    isVisible={addDialogVisible}
                    onChangeForm={onChangeForm}
                    onCheckCoordinateFieldsError={onCheckCoordinateFieldsError}
                    reporting={reporting}
                    type={column.type}
                  />
                </div>
              </Fragment>
            );
          }
        }
      }
    })
  ) : (
    <span className={styles.allAttachmentMessage}>{resources.messages['allAttachment']}</span>
  );

  return formType === 'EDIT' ? editRecordForm : newRecordForm;
};

export { DataForm };
