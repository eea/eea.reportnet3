import React, { useContext, useEffect, useState } from 'react';

import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import styles from './FieldsDesigner.module.scss';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataViewer } from 'ui/views/_components/DataViewer';
import { Dialog } from 'ui/views/_components/Dialog';
import { FieldDesigner } from './_components/FieldDesigner';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { DatasetService } from 'core/services/Dataset';

import { FieldsDesignerUtils } from './_functions/Utils/FieldsDesignerUtils';

export const FieldsDesigner = ({
  datasetId,
  datasetSchemaId,
  datasetSchemas,
  onChangeFields,
  onChangeReference,
  onChangeTableDescription,
  onLoadTableData,
  table
}) => {
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
  const [isPreviewModeOn, setIsPreviewModeOn] = useState(false);
  const [tableDescriptionValue, setTableDescriptionValue] = useState('');

  const resources = useContext(ResourcesContext);

  useEffect(() => {
    if (
      !isUndefined(table) &&
      !isNull(table.records) &&
      !isUndefined(table.records) &&
      !isNull(table.records[0].fields)
    ) {
      setFields(table.records[0].fields);
    }
    if (!isUndefined(table)) {
      setTableDescriptionValue(table.description);
    }
  }, []);

  useEffect(() => {
    if (!isUndefined(fields)) {
      setIsCodelistOrLink(
        fields.filter(field => field.type.toUpperCase() === 'CODELIST' || field.type.toUpperCase() === 'LINK').length >
          0
      );
    }
  }, [fields]);

  const onCodelistAndLinkShow = (fieldId, selectedField) => {
    setIsCodelistOrLink(
      fields.filter(field => {
        return (
          (field.type.toUpperCase() === 'CODELIST' || field.type.toUpperCase() === 'LINK') && field.fieldId !== fieldId
        );
      }).length > 0 ||
        selectedField.fieldType.toUpperCase() === 'CODELIST' ||
        selectedField.fieldType.toUpperCase() === 'LINK'
    );
  };

  const onFieldAdd = ({ codelistItems, description, fieldId, pk, name, recordId, referencedField, required, type }) => {
    const inmFields = [...fields];
    inmFields.splice(inmFields.length, 0, {
      codelistItems,
      description,
      fieldId,
      pk,
      name,
      recordId,
      referencedField,
      required,
      type
    });
    onChangeFields(inmFields, type.toUpperCase() === 'LINK', table.tableSchemaId);
    setFields(inmFields);
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
    name,
    referencedField,
    required,
    type
  }) => {
    const inmFields = [...fields];
    const fieldIndex = FieldsDesignerUtils.getIndexByFieldId(id, inmFields);
    //Buscar en los datasetSchemas si se está usando el id y actualizar el idx del field de la PK según el count

    if (fieldIndex > -1) {
      inmFields[fieldIndex].name = name;
      inmFields[fieldIndex].type = type;
      inmFields[fieldIndex].description = description;
      inmFields[fieldIndex].codelistItems = codelistItems;
      inmFields[fieldIndex].referencedField = referencedField;
      inmFields[fieldIndex].required = required;
      inmFields[fieldIndex].pk = pk;
      onChangeFields(inmFields, isLinkChange, table.tableSchemaId);
      setFields(inmFields);
    }
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
      updateTableDescriptionDesign();
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
    const tableSchemaColumns =
      !isUndefined(fields) && !isNull(fields)
        ? fields.map(field => {
            return {
              table: table['tableSchemaName'],
              field: field['fieldId'],
              header: `${capitalize(field['name'])}`,
              type: field['type'],
              recordId: field['recordId'],
              codelistItems: field.codelistItems,
              required: field.required,
              description: field.description
            };
          })
        : [];

    return !isUndefined(table) && !isUndefined(table.records) && !isNull(table.records) ? (
      <DataViewer
        hasWritePermissions={true}
        isPreviewModeOn={isPreviewModeOn}
        onLoadTableData={onLoadTableData}
        isWebFormMMR={false}
        key={table.id}
        levelErrorTypes={table.levelErrorTypes}
        recordPositionId={-1}
        tableHasErrors={table.hasErrors}
        tableId={table.tableSchemaId}
        tableName={table.tableSchemaName}
        tableSchemaColumns={tableSchemaColumns}
      />
    ) : (
      <div>
        <h3>{resources.messages['datasetDesignerNoFields']}</h3>
      </div>
    );
  };

  const renderConfirmDialog = () => {
    return (
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
  };

  const renderAllFields = () => {
    if (isLoading) {
      return <Spinner className={styles.positioning} />;
    } else {
      return (
        <>
          {isPreviewModeOn ? previewData() : renderFields()}
          {!isPreviewModeOn ? renderNewField() : null}
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
                fieldPK={field.pk}
                fieldPKReferenced={field.pkReferenced}
                fieldName={field.name}
                fieldLink={!isNull(field.referencedField) ? getReferencedFieldName(field.referencedField) : null}
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

  const updateTableDescriptionDesign = async () => {
    if (isUndefined(tableDescriptionValue)) {
      return;
    }
    try {
      const tableUpdated = await DatasetService.updateTableDescriptionDesign(
        table.tableSchemaId,
        tableDescriptionValue,
        datasetId
      );
      if (!tableUpdated) {
        console.error('Error during table description update');
      } else {
        onChangeTableDescription(table.tableSchemaId, tableDescriptionValue);
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
          onBlur={() => updateTableDescriptionDesign()}
          onFocus={e => {
            setInitialTableDescription(e.target.value);
          }}
          onKeyDown={e => onKeyChange(e)}
          placeholder={resources.messages['newTableDescriptionPlaceHolder']}
          // style={{ transition: '0.5s' }}
          value={!isUndefined(tableDescriptionValue) ? tableDescriptionValue : ''}
        />
        <div className={styles.switchDiv}>
          <span className={styles.switchTextInput}>{resources.messages['design']}</span>
          <InputSwitch
            checked={isPreviewModeOn}
            // disabled={true}
            disabled={!isUndefined(fields) ? (fields.length === 0 ? true : false) : false}
            onChange={e => {
              setIsPreviewModeOn(e.value);
            }}
          />
          <span className={styles.switchTextInput}>{resources.messages['preview']}</span>
        </div>
      </div>
      {!isPreviewModeOn ? (
        <div className={styles.fieldsHeader}>
          <label></label>
          <label>{resources.messages['required']}</label>
          <label>{resources.messages['pk']}</label>
          <label>{resources.messages['newFieldPlaceHolder']}</label>
          <label>{resources.messages['newFieldDescriptionPlaceHolder']}</label>
          <label>{resources.messages['newFieldTypePlaceHolder']}</label>
        </div>
      ) : null}
      {renderAllFields()}
      {renderErrors(errorMessageAndTitle.title, errorMessageAndTitle.message)}
      {!isErrorDialogVisible ? renderConfirmDialog() : null}
    </React.Fragment>
  );
};
FieldsDesigner.propTypes = {};
