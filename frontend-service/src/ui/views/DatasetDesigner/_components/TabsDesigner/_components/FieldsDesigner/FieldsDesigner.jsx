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
import { Spinner } from 'ui/views/_components/Spinner';

import { DatasetService } from 'core/services/Dataset';

import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { FieldsDesignerUtils } from './_functions/Utils/FieldsDesignerUtils';

export const FieldsDesigner = ({
  //activeIndex,
  datasetId,
  datasetSchemas,
  isPreviewModeOn,
  isValidationSelected,
  manageDialogs,
  manageUniqueConstraint,
  onChangeFields,
  onChangeTableProperties,
  onLoadTableData,
  recordPositionId,
  selectedRecordErrorId,
  setIsValidationSelected,
  table
}) => {
  const validationContext = useContext(ValidationContext);
  const resources = useContext(ResourcesContext);

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
  const [notEmpty, setNotEmpty] = useState(true);
  const [fixedNumber, setFixedNumber] = useState(false);
  const [isReadOnlyTable, setIsReadOnlyTable] = useState(false);
  const [tableDescriptionValue, setTableDescriptionValue] = useState('');

  useEffect(() => {
    if (!isUndefined(table) && !isNil(table.records) && !isNull(table.records[0].fields)) {
      setFields(table.records[0].fields);
    }
    if (!isUndefined(table)) {
      setTableDescriptionValue(table.description || '');
      setIsReadOnlyTable(table.readOnly || false);
      setToPrefill(table.toPrefill || false);
      table.notEmpty === false ? setNotEmpty(false) : setNotEmpty(true);
      setFixedNumber(table.fixedNumber || false);
    }
  }, []);

  useEffect(() => {
    if (!isUndefined(fields)) {
      setIsCodelistOrLink(
        fields.filter(
          field =>
            field.type.toUpperCase() === 'CODELIST' ||
            field.type.toUpperCase() === 'MULTISELECT_CODELIST' ||
            field.type.toUpperCase() === 'LINK' ||
            field.type.toUpperCase() === 'ATTACHMENT'
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
            field.type.toUpperCase() === 'LINK' ||
            field.type.toUpperCase() === 'ATTACHMENT') &&
          field.fieldId !== fieldId
        );
      }).length > 0 ||
        selectedField.fieldType.toUpperCase() === 'CODELIST' ||
        selectedField.fieldType.toUpperCase() === 'MULTISELECT_CODELIST' ||
        selectedField.fieldType.toUpperCase() === 'LINK' ||
        selectedField.fieldType.toUpperCase() === 'ATTACHMENT'
    );
  };

  const onFieldAdd = ({
    codelistItems,
    description,
    fieldId,
    maxSize,
    pk,
    pkHasMultipleValues,
    pkMustBeUsed,
    name,
    recordId,
    referencedField,
    required,
    type,
    validExtensions
  }) => {
    const inmFields = [...fields];
    inmFields.splice(inmFields.length, 0, {
      codelistItems,
      description,
      fieldId,
      maxSize,
      pk,
      pkHasMultipleValues,
      pkMustBeUsed,
      name,
      recordId,
      referencedField,
      required,
      type,
      validExtensions
    });
    onChangeFields(inmFields, type.toUpperCase() === 'LINK', table.tableSchemaId);
    setFields(inmFields);
    // window.scrollTo(0, document.body.scrollHeight);
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
    maxSize,
    pk,
    pkHasMultipleValues,
    pkMustBeUsed,
    name,
    referencedField,
    required,
    type,
    validExtensions
  }) => {
    const inmFields = [...fields];
    const fieldIndex = FieldsDesignerUtils.getIndexByFieldId(id, inmFields);

    if (fieldIndex > -1) {
      inmFields[fieldIndex].codelistItems = codelistItems;
      inmFields[fieldIndex].description = description;
      inmFields[fieldIndex].maxSize = maxSize;
      inmFields[fieldIndex].name = name;
      inmFields[fieldIndex].pk = pk;
      inmFields[fieldIndex].pkHasMultipleValues = pkHasMultipleValues;
      inmFields[fieldIndex].pkMustBeUsed = pkMustBeUsed;
      inmFields[fieldIndex].referencedField = referencedField;
      inmFields[fieldIndex].required = required;
      inmFields[fieldIndex].type = type;
      inmFields[fieldIndex].validExtensions = validExtensions;
      onChangeFields(inmFields, isLinkChange, table.tableSchemaId);
      setFields(inmFields);
    }
  };

  const onChangeIsReadOnly = checked => {
    setIsReadOnlyTable(checked);
    if (checked) {
      setToPrefill(checked);
    }
    updateTableDesign({
      readOnly: checked,
      toPrefill: checked === false ? toPrefill : checked,
      notEmpty: checked === false ? notEmpty : checked
    });
  };

  const onChangeToPrefill = checked => {
    setToPrefill(checked);
    updateTableDesign({ readOnly: isReadOnlyTable, toPrefill: checked, fixedNumber: checked });
  };

  const onChangeFixedNumber = checked => {
    setFixedNumber(checked);
    updateTableDesign({ toPrefill: checked, fixedNumber: checked });
  };

  const onChangeNotEmpty = checked => {
    setNotEmpty(checked);
    updateTableDesign({ readOnly: isReadOnlyTable, notEmpty: checked });
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
            header: field['name'],
            pk: field['pk'],
            pkHasMultipleValues: field['pkHasMultipleValues'],
            readOnly: field['readOnly'],
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
          isExportable={true}
          isValidationSelected={isValidationSelected}
          key={table.id}
          levelErrorTypes={table.levelErrorTypes}
          onLoadTableData={onLoadTableData}
          recordPositionId={-1}
          recordPositionId={recordPositionId}
          reporting={false}
          selectedRecordErrorId={selectedRecordErrorId}
          setIsValidationSelected={setIsValidationSelected}
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
          fieldFileProperties={{}}
          fieldId="-1"
          fieldName=""
          fieldLink={null}
          fieldHasMultipleValues={false}
          fieldMustBeUsed={false}
          fieldReadOnly={false}
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
                fieldFileProperties={{ validExtensions: field.validExtensions, maxSize: field.maxSize }}
                fieldId={field.fieldId}
                fieldLink={!isNull(field.referencedField) ? getReferencedFieldName(field.referencedField) : null}
                fieldName={field.name}
                fieldHasMultipleValues={field.pkHasMultipleValues}
                fieldMustBeUsed={field.pkMustBeUsed}
                fieldPK={field.pk}
                fieldPKReferenced={field.pkReferenced}
                fieldReadOnly={Boolean(field.readOnly)}
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

  const updateTableDesign = async ({ fixedNumber, notEmpty, readOnly, toPrefill }) => {
    // if (isUndefined(tableDescriptionValue)) {
    //   return;
    // }
    try {
      const tableUpdated = await DatasetService.updateTableDescriptionDesign(
        toPrefill,
        table.tableSchemaId,
        tableDescriptionValue,
        readOnly,
        datasetId,
        notEmpty,
        fixedNumber
      );
      if (!tableUpdated) {
        console.error('Error during table description update');
      } else {
        onChangeTableProperties(table.tableSchemaId, tableDescriptionValue, readOnly, toPrefill, notEmpty, fixedNumber);
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
          collapsedHeight={55}
          expandableOnClick={true}
          id="tableDescription"
          key="tableDescription"
          onChange={e => setTableDescriptionValue(e.target.value)}
          onBlur={() => updateTableDesign({ readOnly: isReadOnlyTable, toPrefill, notEmpty, fixedNumber })}
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
            className={`p-button-secondary p-button-animated-blink datasetSchema-uniques-help-step`}
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

          <Button
            className="p-button-secondary p-button-animated-blink datasetSchema-rowConstraint-help-step"
            icon={'horizontalSliders'}
            label={resources.messages['addRowConstraint']}
            onClick={() => validationContext.onOpenModalFromRow(table.recordSchemaId)}
          />
        </div>
        <div className={`${styles.switchDiv} datasetSchema-readOnlyAndPrefill-help-step`}>
          <div>
            <span className={styles.switchTextInput}>{resources.messages['readOnlyTable']}</span>
            <Checkbox
              checked={isReadOnlyTable}
              // className={styles.checkRequired}
              id={`${table.tableSchemaId}_check_readOnly`}
              inputId={`${table.tableSchemaId}_check_readOnly`}
              label="Default"
              onChange={e => onChangeIsReadOnly(e.checked)}
              style={{ width: '70px' }}
            />
            <label htmlFor={`${table.tableSchemaId}_check_readOnly`} className="srOnly">
              {resources.messages['readOnlyTable']}
            </label>
          </div>
          <div>
            <span className={styles.switchTextInput}>{resources.messages['prefilled']}</span>
            <Checkbox
              checked={toPrefill || fixedNumber}
              disabled={isReadOnlyTable || fixedNumber}
              // className={styles.checkRequired}
              id={`${table.tableSchemaId}_check_to_prefill`}
              inputId={`${table.tableSchemaId}_check_to_prefill`}
              label="Default"
              onChange={e => onChangeToPrefill(e.checked)}
              style={{ width: '70px' }}
            />
            <label htmlFor={`${table.tableSchemaId}_check_to_prefill`} className="srOnly">
              {resources.messages['prefilled']}
            </label>
          </div>
          <div>
            <span className={styles.switchTextInput}>{resources.messages['fixedNumber']}</span>
            <Checkbox
              checked={fixedNumber}
              // className={styles.checkRequired}
              id={`${table.tableSchemaId}_check_fixed_number`}
              inputId={`${table.tableSchemaId}_check_fixed_number`}
              label="Default"
              onChange={e => onChangeFixedNumber(e.checked)}
              style={{ width: '70px' }}
            />
            <label htmlFor={`${table.tableSchemaId}_check_fixed_number`} className="srOnly">
              {resources.messages['fixedNumber']}
            </label>
          </div>
          <div>
            <span className={styles.switchTextInput}>{resources.messages['notEmpty']}</span>
            <Checkbox
              checked={notEmpty}
              // className={styles.checkRequired}
              id={`${table.tableSchemaId}_check_not_empty`}
              inputId={`${table.tableSchemaId}_check_not_empty`}
              label="Default"
              onChange={e => onChangeNotEmpty(e.checked)}
              style={{ width: '70px' }}
            />
            <label htmlFor={`${table.tableSchemaId}_check_not_empty`} className="srOnly">
              {resources.messages['notEmpty']}
            </label>
          </div>
        </div>
      </div>
      {!isPreviewModeOn && (
        <div className={styles.fieldsHeader}>
          <label className={styles.readOnlyWrap}>{resources.messages['readOnly']}</label>
          <label className={styles.requiredWrap}>{resources.messages['required']}</label>
          <span className={styles.PKWrap}>
            <label>{resources.messages['pk']}</label>
            <Button
              className={`${styles.PKInfoButton} p-button-rounded p-button-secondary-transparent`}
              icon="infoCircle"
              id="infoPk"
              title={resources.messages['PKTooltip']}
              tooltip={resources.messages['PKTooltip']}
              tooltipOptions={{ position: 'top' }}
            />
          </span>

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
