import React, { useContext, useEffect, useState } from 'react';

import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import styles from './FieldsDesigner.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'primereact/checkbox';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataViewer } from 'ui/views/_components/DataViewer';
import { Dialog } from 'ui/views/_components/Dialog';
import { FieldDesigner } from './_components/FieldDesigner';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { DatasetService } from 'core/services/Dataset';

import { FieldsDesignerUtils } from './_functions/Utils/FieldsDesignerUtils';

export const FieldsDesigner = ({
  datasetId,
  datasetSchemas,
  isPreviewModeOn,
  manageDialogs,
  manageUniqueConstraint,
  onChangeFields,
  onChangeTableProperties,
  onLoadTableData,
  table
}) => {
  const [toPrefill, setToPrefill] = useState(false);
  const [errorMessageAndTitle, setErrorMessageAndTitle] = useState({ title: '', message: '' });
  const [fields, setFields] = useState([]);
  const [indexToDelete, setIndexToDelete] = useState();
  const [fieldToDeleteType, setFieldToDeleteType] = useState();
  const [initialFieldIndexDragged, setInitialFieldIndexDragged] = useState();
  const [initialTableDescription, setInitialTableDescription] = useState();
  const [isCodelistOrLink, setIsCodelistOrLink] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isErrorDialogVisible, setIsErrorDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isReadOnlyTable, setIsReadOnlyTable] = useState(false);
  const [tableDescriptionValue, setTableDescriptionValue] = useState('');

  const resources = useContext(ResourcesContext);

  useEffect(() => {
    if (!isUndefined(table) && !isNil(table.records) && !isNull(table.records[0].fields)) {
      setFields(table.records[0].fields);
    }
    if (!isUndefined(table)) {
      setTableDescriptionValue(table.description || '');
      setIsReadOnlyTable(table.readOnly || false);
      setToPrefill(table.toPrefill || false);
    }
  }, []);

  useEffect(() => {
    if (!isUndefined(fields)) {
      setIsCodelistOrLink(
        fields.filter(
          field =>
            field.type.toUpperCase() === 'CODELIST' ||
            field.type.toUpperCase() === 'MULTISELECT_CODELIST' ||
            field.type.toUpperCase() === 'LINK'
        ).length > 0
      );
    }
  }, [fields]);

  const onCodelistAndLinkShow = (fieldId, selectedField) => {
    setIsCodelistOrLink(
      fields.filter(field => {
        return (
          (field.type.toUpperCase() === 'CODELIST' ||
            field.type.toUpperCase() === 'MULTISELECT_CODELIST' ||
            field.type.toUpperCase() === 'LINK') &&
          field.fieldId !== fieldId
        );
      }).length > 0 ||
        selectedField.fieldType.toUpperCase() === 'CODELIST' ||
        selectedField.fieldType.toUpperCase() === 'MULTISELECT_CODELIST' ||
        selectedField.fieldType.toUpperCase() === 'LINK'
    );
  };

  const onFieldAdd = ({
    codelistItems,
    description,
    fieldId,
    pk,
    pkMustBeUsed,
    name,
    recordId,
    referencedField,
    required,
    type
  }) => {
    const inmFields = [...fields];
    inmFields.splice(inmFields.length, 0, {
      codelistItems,
      description,
      fieldId,
      pk,
      pkMustBeUsed,
      name,
      recordId,
      referencedField,
      required,
      type
    });
    onChangeFields(inmFields, type.toUpperCase() === 'LINK', table.tableSchemaId);
    setFields(inmFields);
    window.scrollTo(0, document.body.scrollHeight);
  };

  const onFieldDelete = (deletedFieldIndex, deletedFieldType) => {
    setIndexToDelete(deletedFieldIndex);
    setFieldToDeleteType(deletedFieldType);
    setIsDeleteDialogVisible(true);
  };

  const onFieldUpdate = ({
    codelistItems,
    description,
    id,
    isLinkChange,
    pk,
    pkMustBeUsed,
    name,
    referencedField,
    required,
    type
  }) => {
    const inmFields = [...fields];
    const fieldIndex = FieldsDesignerUtils.getIndexByFieldId(id, inmFields);

    if (fieldIndex > -1) {
      inmFields[fieldIndex].name = name;
      inmFields[fieldIndex].type = type;
      inmFields[fieldIndex].description = description;
      inmFields[fieldIndex].codelistItems = codelistItems;
      inmFields[fieldIndex].referencedField = referencedField;
      inmFields[fieldIndex].required = required;
      inmFields[fieldIndex].pk = pk;
      inmFields[fieldIndex].pkMustBeUsed = pkMustBeUsed;
      onChangeFields(inmFields, isLinkChange, table.tableSchemaId);
      setFields(inmFields);
    }
  };

  const onChangeIsReadOnly = checked => {
    setIsReadOnlyTable(checked);
    if (checked) {
      setToPrefill(checked);
    }
    updateTableDesign({ readOnly: checked, toPrefill: checked === false ? toPrefill : checked });
  };

  const onChangeToPrefill = checked => {
    setToPrefill(checked);
    updateTableDesign({ readOnly: isReadOnlyTable, toPrefill: checked });
  };

  const onFieldDragAndDrop = (draggedFieldIdx, droppedFieldName) => {
    reorderField(draggedFieldIdx, droppedFieldName);
  };

  const onFieldDragAndDropStart = draggedFieldIdx => {
    setInitialFieldIndexDragged(draggedFieldIdx);
  };

  const onKeyChange = event => {
    if (event.key === 'Escape') {
      setTableDescriptionValue(initialTableDescription);
    } else if (event.key == 'Enter') {
      event.preventDefault();
      updateTableDesign(isReadOnlyTable);
    }
  };

  const onShowDialogError = (message, title) => {
    setErrorMessageAndTitle({ title, message });
    setIsErrorDialogVisible(true);
  };

  const deleteField = async (deletedFieldIndex, deletedFieldType) => {
    try {
      const fieldDeleted = await DatasetService.deleteRecordFieldDesign(datasetId, fields[deletedFieldIndex].fieldId);
      if (fieldDeleted) {
        const inmFields = [...fields];
        inmFields.splice(deletedFieldIndex, 1);
        onChangeFields(inmFields, deletedFieldType.toUpperCase() === 'LINK', table.tableSchemaId);
        setFields(inmFields);
      } else {
        console.error('Error during field delete');
      }
    } catch (error) {
      console.error('Error during field delete');
    } finally {
    }
  };

  const errorDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        label={resources.messages['ok']}
        icon="check"
        onClick={() => {
          setIsErrorDialogVisible(false);
        }}
      />
    </div>
  );

  const getReferencedFieldName = referencedField => {
    if (!isUndefined(referencedField.name)) {
      return referencedField;
    }
    const link = {};
    datasetSchemas.forEach(schema =>
      schema.tables.forEach(table => {
        if (!table.addTab) {
          table.records.forEach(record =>
            record.fields.forEach(field => {
              if (!isNil(field) && field.fieldId === referencedField.idPk) {
                link.name = `${table.tableSchemaName} - ${field.name}`;
                link.value = `${table.tableSchemaName} - ${field.fieldId}`;
                link.disabled = false;
              }
            })
          );
        }
      })
    );
    link.referencedField = { fieldSchemaId: referencedField.idPk, datasetSchemaId: referencedField.idDatasetSchema };
    return link;
  };

  const previewData = () => {
    const tableSchemaColumns = !isNil(fields)
      ? fields.map(field => {
          return {
            codelistItems: field.codelistItems,
            description: field.description,
            field: field['fieldId'],
            header: `${capitalize(field['name'])}`,
            recordId: field['recordId'],
            referencedField: field['referencedField'],
            required: field.required,
            table: table['tableSchemaName'],
            type: field['type']
          };
        })
      : [];

    if (!isUndefined(table) && !isNil(table.records)) {
      return (
        <DataViewer
          hasWritePermissions={true}
          isPreviewModeOn={isPreviewModeOn}
          isWebFormMMR={false}
          key={table.id}
          levelErrorTypes={table.levelErrorTypes}
          onLoadTableData={onLoadTableData}
          recordPositionId={-1}
          tableHasErrors={table.hasErrors}
          tableId={table.tableSchemaId}
          tableName={table.tableSchemaName}
          tableReadOnly={false}
          tableSchemaColumns={tableSchemaColumns}
        />
      );
    }
  };

  const renderConfirmDialog = () => (
    <ConfirmDialog
      classNameConfirm={'p-button-danger'}
      header={resources.messages['deleteFieldTitle']}
      labelCancel={resources.messages['no']}
      labelConfirm={resources.messages['yes']}
      onConfirm={() => {
        deleteField(indexToDelete, fieldToDeleteType);
        setIsDeleteDialogVisible(false);
      }}
      onHide={() => setIsDeleteDialogVisible(false)}
      visible={isDeleteDialogVisible}>
      {resources.messages['deleteFieldConfirm']}
    </ConfirmDialog>
  );

  const renderAllFields = () => {
    if (isLoading) {
      return <Spinner className={styles.positioning} />;
    } else {
      return (
        <>
          {isPreviewModeOn ? (!isEmpty(fields) ? previewData() : renderNoFields()) : renderFields()}
          {!isPreviewModeOn && renderNewField()}
        </>
      );
    }
  };

  const renderErrors = (errorTitle, error) => {
    return (
      <Dialog
        footer={errorDialogFooter}
        header={errorTitle}
        modal={true}
        onHide={() => setIsErrorDialogVisible(false)}
        visible={isErrorDialogVisible}>
        <div className="p-grid p-fluid">{error}</div>
      </Dialog>
    );
  };

  const renderNewField = () => {
    return (
      <div className={styles.fieldDesignerWrapper} key="0">
        <FieldDesigner
          addField={true}
          checkDuplicates={(name, fieldId) => FieldsDesignerUtils.checkDuplicates(fields, name, fieldId)}
          codelistItems={[]}
          datasetId={datasetId}
          fieldId="-1"
          fieldName=""
          fieldLink={null}
          fieldMustBeUsed={false}
          fieldRequired={false}
          fieldType=""
          fieldValue=""
          hasPK={!isNil(fields) && fields.filter(field => field.pk === true).length > 0}
          // hasPK={true}
          index="-1"
          initialFieldIndexDragged={initialFieldIndexDragged}
          isCodelistOrLink={isCodelistOrLink}
          onCodelistAndLinkShow={onCodelistAndLinkShow}
          onFieldDragAndDrop={onFieldDragAndDrop}
          onNewFieldAdd={onFieldAdd}
          onShowDialogError={onShowDialogError}
          recordSchemaId={!isUndefined(table.recordSchemaId) ? table.recordSchemaId : table.recordId}
          tableSchemaId={table.tableSchemaId}
          totalFields={!isNil(fields) ? fields.length : 0}
        />
      </div>
    );
  };

  const renderFields = () => {
    const renderedFields =
      !isNil(fields) && !isEmpty(fields) ? (
        fields.map((field, index) => {
          return (
            <div className={styles.fieldDesignerWrapper} key={field.fieldId}>
              <FieldDesigner
                checkDuplicates={(name, fieldId) => FieldsDesignerUtils.checkDuplicates(fields, name, fieldId)}
                codelistItems={!isNil(field.codelistItems) ? field.codelistItems : []}
                datasetId={datasetId}
                fieldDescription={field.description}
                fieldId={field.fieldId}
                fieldLink={!isNull(field.referencedField) ? getReferencedFieldName(field.referencedField) : null}
                fieldName={field.name}
                fieldMustBeUsed={field.pkMustBeUsed}
                fieldPK={field.pk}
                fieldPkMustBeUsed={field.pkMustBeUsed}
                fieldPKReferenced={field.pkReferenced}
                fieldRequired={Boolean(field.required)}
                fieldType={field.type}
                fieldValue={field.value}
                hasPK={fields.filter(field => field.pk === true).length > 0}
                index={index}
                initialFieldIndexDragged={initialFieldIndexDragged}
                isCodelistOrLink={isCodelistOrLink}
                key={field.fieldId}
                onCodelistAndLinkShow={onCodelistAndLinkShow}
                onFieldDelete={onFieldDelete}
                onFieldDragAndDrop={onFieldDragAndDrop}
                onFieldDragAndDropStart={onFieldDragAndDropStart}
                onFieldUpdate={onFieldUpdate}
                onShowDialogError={onShowDialogError}
                recordSchemaId={field.recordId}
                tableSchemaId={table.tableSchemaId}
                totalFields={fields.length}
              />
            </div>
          );
        })
      ) : (
        <div className={styles.fieldDesignerWrapper} key="-1"></div>
      );
    return renderedFields;
  };

  const reorderField = async (draggedFieldIdx, droppedFieldName) => {
    try {
      const inmFields = [...fields];
      const droppedFieldIdx = FieldsDesignerUtils.getIndexByFieldName(droppedFieldName, inmFields);
      const fieldOrdered = await DatasetService.orderRecordFieldDesign(
        datasetId,
        droppedFieldIdx === -1
          ? inmFields.length
          : draggedFieldIdx < droppedFieldIdx
          ? droppedFieldIdx - 1
          : droppedFieldIdx,
        inmFields[draggedFieldIdx].fieldId
      );
      if (fieldOrdered) {
        setFields([...FieldsDesignerUtils.arrayShift(inmFields, draggedFieldIdx, droppedFieldIdx)]);
      }
    } catch (error) {
      console.error(`There has been an error during the field reorder: ${error}`);
    }
  };

  const renderNoFields = () => (
    <div>
      <h3>{resources.messages['datasetDesignerNoFields']}</h3>
    </div>
  );

  const updateTableDesign = async ({ readOnly, toPrefill }) => {
    // if (isUndefined(tableDescriptionValue)) {
    //   return;
    // }
    try {
      const tableUpdated = await DatasetService.updateTableDescriptionDesign(
        toPrefill,
        table.tableSchemaId,
        tableDescriptionValue,
        readOnly,
        datasetId
      );
      if (!tableUpdated) {
        console.error('Error during table description update');
      } else {
        onChangeTableProperties(table.tableSchemaId, tableDescriptionValue, readOnly, toPrefill);
      }
    } catch (error) {
      console.error(`Error during table description update: ${error}`);
    }
  };

  return (
    <React.Fragment>
      <h4 className={styles.descriptionLabel}>{resources.messages['newTableDescriptionPlaceHolder']}</h4>
      <div className={styles.switchDivInput}>
        <InputTextarea
          className={styles.tableDescriptionInput}
          collapsedHeight={40}
          expandableOnClick={true}
          key="tableDescription"
          onChange={e => setTableDescriptionValue(e.target.value)}
          onBlur={() => updateTableDesign({ readOnly: isReadOnlyTable, toPrefill })}
          onFocus={e => {
            setInitialTableDescription(e.target.value);
          }}
          onKeyDown={e => onKeyChange(e)}
          placeholder={resources.messages['newTableDescriptionPlaceHolder']}
          // style={{ transition: '0.5s' }}
          value={!isUndefined(tableDescriptionValue) ? tableDescriptionValue : ''}
        />
        <div className={styles.constraintsButtons}>
          <Button
            className={`${`p-button-secondary p-button-animated-blink ${styles.constraintsTextButtons}`}`}
            icon={'key'}
            label={resources.messages['addUniqueConstraint']}
            onClick={() => {
              manageDialogs('isManageUniqueConstraintDialogVisible', true);
              manageUniqueConstraint({
                isTableCreationMode: true,
                tableSchemaId: table.tableSchemaId,
                tableSchemaName: table.tableSchemaName
              });
            }}
          />
        </div>
        <div className={styles.switchDiv}>
          <div>
            <span className={styles.switchTextInput}>{resources.messages['readOnlyTable']}</span>
            <Checkbox
              checked={isReadOnlyTable}
              // className={styles.checkRequired}
              inputId={`${table.tableId}_check`}
              label="Default"
              onChange={e => onChangeIsReadOnly(e.checked)}
              style={{ width: '70px' }}
            />
          </div>
          <div>
            <span className={styles.switchTextInput}>{resources.messages['prefilled']}</span>
            <Checkbox
              checked={toPrefill}
              disabled={isReadOnlyTable}
              // className={styles.checkRequired}
              inputId={`${table.tableId}_check`}
              label="Default"
              onChange={e => onChangeToPrefill(e.checked)}
              style={{ width: '70px' }}
            />
          </div>
        </div>
      </div>
      {!isPreviewModeOn && (
        <div className={styles.fieldsHeader}>
          <label></label>
          <label>{resources.messages['required']}</label>
          <label>{resources.messages['pk']}</label>
          <label>{resources.messages['newFieldPlaceHolder']}</label>
          <label>{resources.messages['newFieldDescriptionPlaceHolder']}</label>
          <label>{resources.messages['newFieldTypePlaceHolder']}</label>
        </div>
      )}
      {renderAllFields()}
      {renderErrors(errorMessageAndTitle.title, errorMessageAndTitle.message)}
      {!isErrorDialogVisible && isDeleteDialogVisible && renderConfirmDialog()}
    </React.Fragment>
  );
};
FieldsDesigner.propTypes = {};
