import { Fragment, useContext, useEffect, useState } from 'react';
import ReactDOMServer from 'react-dom/server';

import first from 'lodash/first';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './DataForm.module.css';

import { DataFormFieldEditor } from './_components/DataFormFieldEditor';
import { TooltipButton } from 'views/_components/TooltipButton';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextUtils } from 'repositories/_utils/TextUtils';

const DataForm = ({
  addDialogVisible,
  colsSchema,
  datasetId,
  datasetSchemaId,
  editDialogVisible,
  formType,
  getTooltipMessage,
  hasWritePermissions,
  isSaving = false,
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

  const nonAttachmentsOrComplexGeomTypes = colsSchema
    .filter(
      col =>
        !['ATTACHMENT', 'POLYGON', 'LINESTRING', 'MULTIPOLYGON', 'MULTILINESTRING', 'MULTIPOINT'].includes(col.type)
    )
    .filter(col => col.field !== 'id' && col.field !== 'datasetPartitionId');

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

  const renderTooltipButton = (column, i) => (
    <TooltipButton
      getContent={() =>
        ReactDOMServer.renderToStaticMarkup(
          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'flex-start'
            }}>
            {getTooltipMessage(column)}
          </div>
        )
      }
      onClick={() => onShowFieldInfo(column.header, true)}
      uniqueIdentifier={i}
    />
  );

  const editRecordForm =
    nonAttachmentsOrComplexGeomTypes.length !== 0 ? (
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
                      <label>{`${column.header}${
                        TextUtils.areEquals(column.type, 'DATE') ? ' (YYYY-MM-DD)' : ''
                      }`}</label>
                      {renderTooltipButton(column, i)}
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
                      width: ['DATE', 'DATETIME', 'CODELIST', 'MULTISELECT_CODELIST', 'LINK', 'EXTERNAL_LINK'].includes(
                        column.type
                      )
                        ? '30%'
                        : ''
                    }}>
                    <DataFormFieldEditor
                      autoFocus={column.field === first(nonAttachmentsOrComplexGeomTypes).field}
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
                      isSaving={isSaving}
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

        return null;
      })
    ) : (
      <span className={styles.allAttachmentMessage}>{resources.messages['allAttachmentOrComplexGeom']}</span>
    );

  const newRecordForm =
    nonAttachmentsOrComplexGeomTypes.length !== 0 ? (
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
                        TextUtils.areEquals(column.type, 'DATE')
                          ? ' (YYYY-MM-DD)'
                          : TextUtils.areEquals(column.type, 'DATETIME')
                          ? ' (YYYY-MM-DD HH:mm:ss)'
                          : ''
                      }`}</label>
                      {renderTooltipButton(column, i)}
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
                      width: ['DATE', 'DATETIME', 'CODELIST', 'MULTISELECT_CODELIST', 'LINK', 'EXTERNAL_LINK'].includes(
                        column.type
                      )
                        ? '60%'
                        : ''
                    }}>
                    <DataFormFieldEditor
                      autoFocus={column.field === first(nonAttachmentsOrComplexGeomTypes).field}
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
                      isSaving={isSaving}
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

        return null;
      })
    ) : (
      <span className={styles.allAttachmentMessage}>{resources.messages['allAttachmentOrComplexGeom']}</span>
    );

  return formType === 'EDIT' ? editRecordForm : newRecordForm;
};

export { DataForm };
