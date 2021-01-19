import React, { Fragment, useContext, useEffect, useState } from 'react';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './DataForm.module.css';

import { Button } from 'ui/views/_components/Button';
import { DataFormFieldEditor } from './_components/DataFormFieldEditor';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';

const DataForm = ({
  addDialogVisible,
  colsSchema,
  datasetId,
  datasetSchemaId,
  editDialogVisible,
  formType,
  getTooltipMessage,
  hasWritePermissions,
  onChangeForm,
  onConditionalChange,
  onShowCoordinateError = () => {},
  records,
  reporting,
  onShowFieldInfo
}) => {
  const resources = useContext(ResourcesContext);

  const [fieldsWithError, setFieldsWithError] = useState([]);
  const [isConditionalChanged, setIsConditionalChanged] = useState(false);
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

  const allAttachmentsOrComplexGeom = () => {
    const notAttachmentOrComplexGeom = colsSchema.filter(
      col =>
        !['ATTACHMENT', 'POLYGON', 'LINESTRING', 'MULTIPOLYGON', 'MULTILINESTRING', 'MULTIPOINT'].includes(col.type)
    );
    return (
      notAttachmentOrComplexGeom.filter(col => col.field !== 'id' && col.field !== 'datasetPartitionId').length === 0
    );
  };

  const editRecordForm = !allAttachmentsOrComplexGeom() ? (
    colsSchema.map((column, i) => {
      //Avoid row id Field and dataSetPartitionId
      if (editDialogVisible) {
        if (i < colsSchema.length - 2) {
          if (!isUndefined(records.editedRecord.dataRow)) {
            const field = records.editedRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
            return (
              <Fragment key={column.field}>
                {!['ATTACHMENT', 'POLYGON', 'LINESTRING', 'MULTIPOLYGON', 'MULTILINESTRING', 'MULTIPOINT'].includes(
                  column.type
                ) && (
                  <div className="p-col-4" style={{ padding: '.75em' }}>
                    <label htmlFor={column.field}>{`${column.header}${
                      TextUtils.areEquals(column.type, 'DATE') ? ' (YYYY-MM-DD)' : ''
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
                    padding: ![
                      'ATTACHMENT',
                      'POLYGON',
                      'LINESTRING',
                      'MULTIPOLYGON',
                      'MULTILINESTRING',
                      'MULTIPOINT'
                    ].includes(column.type)
                      ? '.5em'
                      : '0',
                    width: ['DATE', 'CODELIST', 'MULTISELECT_CODELIST', 'LINK'].includes(column.type) ? '30%' : ''
                  }}>
                  <DataFormFieldEditor
                    autoFocus={i === 0}
                    column={column}
                    datasetId={datasetId}
                    datasetSchemaId={datasetSchemaId}
                    editing={true}
                    field={column.field}
                    fieldValue={
                      isNil(field.fieldData[column.field])
                        ? column.type === 'POINT'
                          ? `{"type": "Feature", "geometry": {"type":"Point","coordinates":[55.6811608,12.5844761]}, "properties": {"srid": "EPSG:4326"}}`
                          : ''
                        : field.fieldData[column.field]
                    }
                    hasWritePermissions={hasWritePermissions}
                    isConditional={
                      colsSchema.filter(
                        col =>
                          !isNil(col.referencedField) && col.referencedField.masterConditionalFieldId === column.field
                      ).length > 0
                    }
                    isConditionalChanged={isConditionalChanged}
                    isVisible={editDialogVisible}
                    onChangeForm={(property, value, conditionalChanged = false) => {
                      if (isConditionalChanged !== conditionalChanged) {
                        setIsConditionalChanged(conditionalChanged);
                      }
                      onChangeForm(property, value);
                    }}
                    onCheckCoordinateFieldsError={onCheckCoordinateFieldsError}
                    onConditionalChange={onConditionalChange}
                    records={records}
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
    <span className={styles.allAttachmentMessage}>{resources.messages['allAttachmentOrComplexGeom']}</span>
  );

  const newRecordForm = !allAttachmentsOrComplexGeom() ? (
    colsSchema.map((column, i) => {
      if (addDialogVisible) {
        if (i < colsSchema.length - 2) {
          if (!isUndefined(records.newRecord.dataRow)) {
            const field = records.newRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
            return (
              <Fragment key={column.field}>
                {!['ATTACHMENT', 'POLYGON', 'LINESTRING', 'MULTIPOLYGON', 'MULTILINESTRING', 'MULTIPOINT'].includes(
                  column.type
                ) && (
                  <div className="p-col-4" style={{ padding: '.75em' }}>
                    <label htmlFor={column.field}>{`${column.header}${
                      TextUtils.areEquals(column.type, 'DATE') ? ' (YYYY-MM-DD)' : ''
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
                    padding: ![
                      'ATTACHMENT',
                      'POLYGON',
                      'LINESTRING',
                      'MULTIPOLYGON',
                      'MULTILINESTRING',
                      'MULTIPOINT'
                    ].includes(column.type)
                      ? '.5em'
                      : '0',
                    width: ['DATE', 'CODELIST', 'MULTISELECT_CODELIST', 'LINK'].includes(column.type) ? '30%' : ''
                  }}>
                  <DataFormFieldEditor
                    autoFocus={i === 0}
                    column={column}
                    datasetId={datasetId}
                    datasetSchemaId={datasetSchemaId}
                    field={column.field}
                    fieldValue={isNil(field.fieldData[column.field]) ? '' : field.fieldData[column.field]}
                    hasWritePermissions={hasWritePermissions}
                    isConditional={
                      colsSchema.filter(
                        col =>
                          !isNil(col.referencedField) && col.referencedField.masterConditionalFieldId === column.field
                      ).length > 0
                    }
                    isConditionalChanged={isConditionalChanged}
                    isVisible={addDialogVisible}
                    onChangeForm={(property, value, conditionalChanged = false) => {
                      if (isConditionalChanged !== conditionalChanged) {
                        setIsConditionalChanged(conditionalChanged);
                      }
                      onChangeForm(property, value);
                    }}
                    onCheckCoordinateFieldsError={onCheckCoordinateFieldsError}
                    onConditionalChange={onConditionalChange}
                    records={records}
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
    <span className={styles.allAttachmentMessage}>{resources.messages['allAttachmentOrComplexGeom']}</span>
  );

  return formType === 'EDIT' ? editRecordForm : newRecordForm;
};

export { DataForm };
