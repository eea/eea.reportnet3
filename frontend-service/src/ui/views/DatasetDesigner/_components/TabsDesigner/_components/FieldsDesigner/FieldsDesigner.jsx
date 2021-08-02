import { Fragment, useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { DatasetConfig } from 'conf/domain/model/Dataset';

import styles from './FieldsDesigner.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { DataViewer } from 'ui/views/_components/DataViewer';
import { Dialog } from 'ui/views/_components/Dialog';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { FieldDesigner } from './_components/FieldDesigner';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

import { FieldsDesignerUtils } from './_functions/Utils/FieldsDesignerUtils';
import { MetadataUtils, TextUtils } from 'ui/views/_functions/Utils';
import { getUrl } from 'core/infrastructure/CoreUtils';

export const FieldsDesigner = ({
  dataflowId,
  datasetId,
  datasetSchemaId,
  datasetSchemas,
  designerState,
  hasPKReferenced,
  isDataflowOpen,
  isDesignDatasetEditorRead,
  isGroupedValidationDeleted,
  isGroupedValidationSelected,
  isReferenceDataset,
  isValidationSelected,
  manageDialogs,
  manageUniqueConstraint,
  onChangeFields,
  onChangeIsValidationSelected,
  onChangeTableProperties,
  onHideSelectGroupedValidation,
  onLoadTableData,
  recordPositionId,
  selectedRecordErrorId,
  selectedRuleId,
  selectedRuleLevelError,
  selectedRuleMessage,
  selectedTableSchemaId,
  table,
  viewType
}) => {
  const notificationContext = useContext(NotificationContext);
  const validationContext = useContext(ValidationContext);
  const resources = useContext(ResourcesContext);

  const [toPrefill, setToPrefill] = useState(false);
  const [errorMessageAndTitle, setErrorMessageAndTitle] = useState({ title: '', message: '' });
  const [fields, setFields] = useState();
  const [fieldToDeleteType, setFieldToDeleteType] = useState();
  const [exportTableSchema, setExportTableSchema] = useState(undefined);
  const [exportTableSchemaName, setExportTableSchemaName] = useState('');
  const [indexToDelete, setIndexToDelete] = useState();
  const [initialFieldIndexDragged, setInitialFieldIndexDragged] = useState();
  const [initialTableDescription, setInitialTableDescription] = useState();
  const [isCodelistOrLink, setIsCodelistOrLink] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isErrorDialogVisible, setIsErrorDialogVisible] = useState(false);
  const [isLoadingFile, setIsLoadingFile] = useState(false);
  const [notEmpty, setNotEmpty] = useState(true);
  const [fixedNumber, setFixedNumber] = useState(false);
  const [isReadOnlyTable, setIsReadOnlyTable] = useState(false);
  const [tableDescriptionValue, setTableDescriptionValue] = useState('');

  useEffect(() => {
    if (!isUndefined(table) && !isNil(table.records) && !isNull(table.records[0].fields)) {
      setFields(table.records[0].fields);
    } else {
      setFields([]);
    }
    if (!isUndefined(table)) {
      setTableDescriptionValue(table.description || '');
      setIsReadOnlyTable(table.readOnly || false);
      setToPrefill(table.toPrefill || false);
      table.notEmpty === false ? setNotEmpty(false) : setNotEmpty(true);
      setFixedNumber(table.fixedNumber || false);
    }
  }, [table]);

  useEffect(() => {
    if (!isUndefined(fields)) {
      setIsCodelistOrLink(
        fields.filter(field =>
          ['CODELIST', 'MULTISELECT_CODELIST', 'EXTERNAL_LINK', 'LINK', 'ATTACHMENT'].includes(field.type.toUpperCase())
        ).length > 0
      );
    }
  }, [fields]);

  useEffect(() => {
    if (!isUndefined(exportTableSchema)) {
      DownloadFile(exportTableSchema, exportTableSchemaName);
    }
  }, [exportTableSchema]);

  const onCodelistAndLinkShow = (fieldId, selectedField) => {
    setIsCodelistOrLink(
      fields.filter(field => {
        return (
          ['CODELIST', 'MULTISELECT_CODELIST', 'EXTERNAL_LINK', 'LINK', 'ATTACHMENT'].includes(
            field.type.toUpperCase()
          ) && field.fieldId !== fieldId
        );
      }).length > 0 ||
        ['CODELIST', 'MULTISELECT_CODELIST', 'EXTERNAL_LINK', 'LINK', 'ATTACHMENT'].includes(
          selectedField.fieldType.toUpperCase()
        )
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
    readOnly,
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
      readOnly,
      recordId,
      referencedField,
      required,
      type,
      validExtensions
    });
    onChangeFields(
      inmFields,
      TextUtils.areEquals(type, 'LINK') || TextUtils.areEquals(type, 'EXTERNAL_LINK'),
      table.tableSchemaId
    );
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
    maxSize,
    pk,
    pkHasMultipleValues,
    pkMustBeUsed,
    name,
    readOnly,
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
      inmFields[fieldIndex].fieldType = type;
      inmFields[fieldIndex].maxSize = maxSize;
      inmFields[fieldIndex].name = name;
      inmFields[fieldIndex].pk = pk;
      inmFields[fieldIndex].pkHasMultipleValues = pkHasMultipleValues;
      inmFields[fieldIndex].pkMustBeUsed = pkMustBeUsed;
      inmFields[fieldIndex].referencedField = referencedField;
      inmFields[fieldIndex].required = required;
      inmFields[fieldIndex].readOnly = readOnly;
      inmFields[fieldIndex].type = type;
      inmFields[fieldIndex].validExtensions = validExtensions;
      onChangeFields(inmFields, isLinkChange, table.tableSchemaId);
      setFields(inmFields);
    }
  };

  const onChangeIsReadOnly = checked => {
    setIsReadOnlyTable(checked);
    if (checked) {
      setToPrefill(true);
    }
    updateTableDesign({ fixedNumber, notEmpty, readOnly: checked, toPrefill: checked === false ? toPrefill : true });
  };

  const onChangeToPrefill = checked => {
    setToPrefill(checked);
    updateTableDesign({ readOnly: isReadOnlyTable, toPrefill: checked, fixedNumber, notEmpty });
  };

  const onChangeFixedNumber = checked => {
    setFixedNumber(checked);
    if (checked) {
      setToPrefill(true);
    }
    updateTableDesign({
      fixedNumber: checked,
      notEmpty,
      readOnly: isReadOnlyTable,
      toPrefill: checked === false ? toPrefill : true
    });
  };

  const onChangeNotEmpty = checked => {
    setNotEmpty(checked);
    updateTableDesign({ readOnly: isReadOnlyTable, toPrefill, fixedNumber, notEmpty: checked });
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
    } else if (event.key === 'Enter') {
      event.preventDefault();
      updateTableDesign(isReadOnlyTable);
    }
  };

  const onShowDialogError = (message, title, focusElement) => {
    setErrorMessageAndTitle({ title, message, focusElement });
    setIsErrorDialogVisible(true);
  };

  const deleteField = async (deletedFieldIndex, deletedFieldType) => {
    try {
      const { status } = await DatasetService.deleteRecordFieldDesign(datasetId, fields[deletedFieldIndex].fieldId);

      if (status >= 200 && status <= 299) {
        const inmFields = [...fields];
        inmFields.splice(deletedFieldIndex, 1);
        onChangeFields(
          inmFields,
          TextUtils.areEquals(deletedFieldType, 'LINK') || TextUtils.areEquals(deletedFieldType, 'EXTERNAL_LINK'),
          table.tableSchemaId
        );
        setFields(inmFields);
      }
    } catch (error) {
      console.error('Error during field delete');
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
      }
    }
  };

  const errorDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        icon="check"
        label={resources.messages['ok']}
        onClick={() => {
          setIsErrorDialogVisible(false);
          errorMessageAndTitle?.focusElement?.focus();
        }}
      />
    </div>
  );

  const getReferencedFieldName = referencedField => {
    if (!isUndefined(referencedField.name)) {
      return referencedField;
    }
    const link = {};
    let tableSchema = '';

    datasetSchemas?.forEach(schema => {
      schema.tables?.forEach(table => {
        !table.addTab &&
          table.records?.forEach(record => {
            record.fields?.forEach(field => {
              if (field?.fieldId === referencedField.idPk) {
                link.name = `${table.tableSchemaName} - ${field.name}`;
                link.value = `${table.tableSchemaName} - ${field.fieldId}`;
                link.disabled = false;
                tableSchema = table.tableSchemaId;
              }
            });
          });
      });
    });

    link.referencedField = {
      datasetSchemaId: referencedField.idDatasetSchema,
      fieldSchemaId: referencedField.idPk,
      tableSchemaId: tableSchema
    };
    return link;
  };

  const getExternalReferencedFieldName = referencedField => {
    const link = {};
    link.name = `${referencedField.tableSchemaName} - ${referencedField.fieldSchemaName}`;
    link.value = `${referencedField.tableSchemaName} - ${referencedField.idPk}`;
    link.disabled = false;

    link.referencedField = {
      dataflowId: referencedField.dataflowId,
      datasetSchemaId: referencedField.idDatasetSchema,
      fieldSchemaId: referencedField.idPk,
      fieldSchemaName: referencedField.fieldSchemaName,
      tableSchemaName: referencedField.tableSchemaName
    };
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
            maxSize: field['maxSize'],
            pk: field['pk'],
            pkHasMultipleValues: field['pkHasMultipleValues'],
            readOnly: field['readOnly'],
            recordId: field['recordId'],
            referencedField: field['referencedField'],
            required: field.required,
            table: table['tableSchemaName'],
            type: field['type'],
            validExtensions: field['validExtensions']
          };
        })
      : [];

    if (!isUndefined(table) && !isNil(table.records)) {
      return (
        <DataViewer
          hasWritePermissions={true}
          isDataflowOpen={isDataflowOpen}
          isDesignDatasetEditorRead={isDesignDatasetEditorRead}
          isExportable={true}
          isGroupedValidationDeleted={isGroupedValidationDeleted}
          isGroupedValidationSelected={isGroupedValidationSelected}
          isValidationSelected={isValidationSelected}
          key={table.id}
          levelErrorTypes={table.levelErrorTypes}
          onChangeIsValidationSelected={onChangeIsValidationSelected}
          onHideSelectGroupedValidation={onHideSelectGroupedValidation}
          onLoadTableData={onLoadTableData}
          recordPositionId={recordPositionId}
          reporting={false}
          selectedRecordErrorId={selectedRecordErrorId}
          selectedRuleId={selectedRuleId}
          selectedRuleLevelError={selectedRuleLevelError}
          selectedRuleMessage={selectedRuleMessage}
          selectedTableSchemaId={selectedTableSchemaId}
          tableHasErrors={table.hasErrors}
          tableId={table.tableSchemaId}
          tableName={table.tableSchemaName}
          tableReadOnly={false}
          tableSchemaColumns={tableSchemaColumns}
          viewType={viewType}
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

  const renderAllFields = () => (
    <Fragment>
      {viewType['tabularData'] ? (!isEmpty(fields) ? previewData() : renderNoFields()) : renderFields()}
      {!viewType['tabularData'] && renderNewField()}
    </Fragment>
  );

  const renderErrors = (errorTitle, error) => {
    return (
      isErrorDialogVisible && (
        <Dialog
          footer={errorDialogFooter}
          header={errorTitle}
          modal={true}
          onHide={() => {
            setIsErrorDialogVisible(false);
            errorMessageAndTitle?.focusElement?.focus();
          }}
          visible={isErrorDialogVisible}>
          <div className="p-grid p-fluid">{error}</div>
        </Dialog>
      )
    );
  };

  const renderNewField = () => {
    return (
      <div className={styles.fieldDesignerWrapper} key="0">
        <FieldDesigner
          addField={true}
          checkDuplicates={(name, fieldId) => FieldsDesignerUtils.checkDuplicates(fields, name, fieldId)}
          checkInvalidCharacters={name => FieldsDesignerUtils.checkInvalidCharacters(name)}
          codelistItems={[]}
          datasetId={datasetId}
          datasetSchemaId={datasetSchemaId}
          fieldFileProperties={{}}
          fieldHasMultipleValues={false}
          fieldId="-1"
          fieldLink={null}
          fieldMustBeUsed={false}
          fieldName=""
          fieldReadOnly={false}
          fieldRequired={false}
          fieldType=""
          fieldValue=""
          fields={fields}
          hasPK={!isNil(fields) && fields.filter(field => field.pk).length > 0}
          index="-1"
          initialFieldIndexDragged={initialFieldIndexDragged}
          isCodelistOrLink={isCodelistOrLink}
          isDataflowOpen={isDataflowOpen}
          isDesignDatasetEditorRead={isDesignDatasetEditorRead}
          isReferenceDataset={isReferenceDataset}
          onCodelistAndLinkShow={onCodelistAndLinkShow}
          onFieldDragAndDrop={onFieldDragAndDrop}
          onNewFieldAdd={onFieldAdd}
          onShowDialogError={onShowDialogError}
          recordSchemaId={!isUndefined(table.recordSchemaId) ? table.recordSchemaId : table.recordId}
          tableSchemaId={table.tableSchemaId}
          totalFields={!isNil(fields) ? fields.length : undefined}
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
                checkInvalidCharacters={name => FieldsDesignerUtils.checkInvalidCharacters(name)}
                codelistItems={!isNil(field.codelistItems) ? field.codelistItems : []}
                datasetId={datasetId}
                datasetSchemaId={datasetSchemaId}
                fieldDescription={field.description}
                fieldFileProperties={{ validExtensions: field.validExtensions, maxSize: field.maxSize }}
                fieldHasMultipleValues={field.pkHasMultipleValues}
                fieldId={field.fieldId}
                fieldLink={
                  !isNull(field.referencedField)
                    ? !TextUtils.areEquals(field.type, 'external_link')
                      ? getReferencedFieldName(field.referencedField)
                      : getExternalReferencedFieldName(field.referencedField)
                    : null
                }
                fieldLinkedTableConditional={
                  !isNil(field.referencedField) ? field.referencedField.linkedConditionalFieldId : ''
                }
                fieldLinkedTableLabel={!isNil(field.referencedField) ? field.referencedField.labelId : ''}
                fieldMasterTableConditional={
                  !isNil(field.referencedField) ? field.referencedField.masterConditionalFieldId : ''
                }
                fieldMustBeUsed={field.pkMustBeUsed}
                fieldName={field.name}
                fieldPK={field.pk}
                fieldPKReferenced={field.pkReferenced}
                fieldReadOnly={Boolean(field.readOnly)}
                fieldRequired={Boolean(field.required)}
                fieldType={field.type}
                fieldValue={field.value}
                fields={fields}
                hasPK={fields.filter(field => field.pk).length > 0}
                index={index}
                initialFieldIndexDragged={initialFieldIndexDragged}
                isCodelistOrLink={isCodelistOrLink}
                isDataflowOpen={isDataflowOpen}
                isDesignDatasetEditorRead={isDesignDatasetEditorRead}
                isReferenceDataset={isReferenceDataset}
                key={field.fieldId}
                onCodelistAndLinkShow={onCodelistAndLinkShow}
                onFieldDelete={onFieldDelete}
                onFieldDragAndDrop={onFieldDragAndDrop}
                onFieldDragAndDropStart={onFieldDragAndDropStart}
                onFieldUpdate={onFieldUpdate}
                onShowDialogError={onShowDialogError}
                recordSchemaId={field.recordId}
                tableSchemaId={table.tableSchemaId}
                totalFields={!isNil(fields) ? fields.length : undefined}
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
      if (fieldOrdered.status >= 200 && fieldOrdered.status <= 299) {
        setFields([...FieldsDesignerUtils.arrayShift(inmFields, draggedFieldIdx, droppedFieldIdx)]);
        onChangeFields(inmFields, false, table.tableSchemaId);
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
    try {
      const { status } = await DatasetService.updateTableDescriptionDesign(
        toPrefill,
        table.tableSchemaId,
        tableDescriptionValue,
        readOnly,
        datasetId,
        notEmpty,
        fixedNumber
      );
      if (status >= 200 && status <= 299) {
        onChangeTableProperties(table.tableSchemaId, tableDescriptionValue, readOnly, toPrefill, notEmpty, fixedNumber);
      }
    } catch (error) {
      console.error(`Error during table description update: ${error}`);
    }
  };

  const onUpload = async () => {
    notificationContext.add({
      type: 'IMPORT_TABLE_SCHEMA_INIT'
    });
    manageDialogs('isImportTableSchemaDialogVisible', false);
  };

  const createTableName = (tableName, fileType) => `${tableName}.${fileType}`;

  const onExportTableSchema = async fileType => {
    setIsLoadingFile(true);
    try {
      setExportTableSchemaName(createTableName(table.tableSchemaName, fileType));
      const { data } = await DatasetService.exportTableSchemaById(
        datasetId,
        designerState.datasetSchemaId,
        table.tableSchemaId,
        fileType
      );
      setExportTableSchema(data);
    } catch (error) {
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'EXPORT_TABLE_DATA_BY_ID_ERROR',
        content: {
          dataflowId,
          datasetId,
          dataflowName,
          datasetName,
          tableName: designerState.tableName
        }
      });
    } finally {
      setIsLoadingFile(false);
    }
  };

  const onImportTableSchemaError = async ({ xhr }) => {
    if (xhr.status === 423) {
      notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
    }
  };

  return (
    <Fragment>
      <Toolbar>
        <div className="p-toolbar-group-left">
          <Button
            className={`p-button-rounded p-button-secondary-transparent ${
              !isDataflowOpen && !isDesignDatasetEditorRead ? 'p-button-animated-blink' : null
            }`}
            disabled={isDataflowOpen || isDesignDatasetEditorRead}
            icon={'import'}
            label={resources.messages['importTableSchema']}
            onClick={() => manageDialogs('isImportTableSchemaDialogVisible', true)}
          />
          <Button
            className={`p-button-rounded p-button-secondary-transparent ${
              !isDataflowOpen && !isDesignDatasetEditorRead ? 'p-button-animated-blink' : null
            }`}
            disabled={isDataflowOpen || isDesignDatasetEditorRead}
            icon={'export'}
            label={resources.messages['exportTableSchema']}
            onClick={() => onExportTableSchema('csv', true)}
          />
          <Button
            className={`p-button-secondary-transparent ${
              !isDesignDatasetEditorRead && (!isDataflowOpen || !isReferenceDataset) ? 'p-button-animated-blink' : null
            } datasetSchema-uniques-help-step`}
            disabled={isDesignDatasetEditorRead || (isDataflowOpen && isReferenceDataset)}
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
            className={`p-button-secondary-transparent ${
              !isDesignDatasetEditorRead && (!isDataflowOpen || !isReferenceDataset) ? 'p-button-animated-blink' : null
            } datasetSchema-rowConstraint-help-step`}
            disabled={isDesignDatasetEditorRead || (isDataflowOpen && isReferenceDataset)}
            icon={'horizontalSliders'}
            label={resources.messages['addRowConstraint']}
            onClick={() => validationContext.onOpenModalFromRow(table.recordSchemaId)}
          />
        </div>
      </Toolbar>
      <h4 className={styles.descriptionLabel}>{resources.messages['newTableDescriptionPlaceHolder']}</h4>
      <div className={styles.tableDescriptionRow}>
        <InputTextarea
          className={styles.tableDescriptionInput}
          collapsedHeight={55}
          disabled={isDataflowOpen || isDesignDatasetEditorRead}
          expandableOnClick={true}
          id="tableDescription"
          key="tableDescription"
          onBlur={() => updateTableDesign({ readOnly: isReadOnlyTable, toPrefill, notEmpty, fixedNumber })}
          onChange={e => setTableDescriptionValue(e.target.value)}
          onFocus={e => {
            setInitialTableDescription(e.target.value);
          }}
          onKeyDown={e => onKeyChange(e)}
          placeholder={resources.messages['newTableDescriptionPlaceHolder']}
          value={!isUndefined(tableDescriptionValue) ? tableDescriptionValue : ''}
        />

        <div className={`${styles.switchDiv} datasetSchema-readOnlyAndPrefill-help-step`}>
          <div>
            <span
              className={styles.switchTextInput}
              id={`${table.tableSchemaId}_check_readOnly_label`}
              style={{ opacity: isDesignDatasetEditorRead || isDataflowOpen ? 0.5 : 1 }}>
              {resources.messages['readOnlyTable']}
            </span>
            <Checkbox
              ariaLabelledBy={`${table.tableSchemaId}_check_readOnly_label`}
              checked={isReadOnlyTable || isReferenceDataset}
              className={styles.fieldDesignerItem}
              disabled={isDataflowOpen || isDesignDatasetEditorRead || isReferenceDataset}
              id={`${table.tableSchemaId}_check_readOnly`}
              inputId={`${table.tableSchemaId}_check_readOnly`}
              label="Default"
              onChange={e => onChangeIsReadOnly(e.checked)}
            />
          </div>
          <div>
            <span
              className={styles.switchTextInput}
              id={`${table.tableSchemaId}_check_to_prefill_label`}
              style={{ opacity: isDesignDatasetEditorRead || isDataflowOpen ? 0.5 : 1 }}>
              {resources.messages['prefilled']}
            </span>
            <Checkbox
              ariaLabelledBy={`${table.tableSchemaId}_check_to_prefill_label`}
              checked={toPrefill || fixedNumber || isReferenceDataset}
              className={styles.fieldDesignerItem}
              disabled={
                isReadOnlyTable || fixedNumber || isDataflowOpen || isDesignDatasetEditorRead || isReferenceDataset
              }
              id={`${table.tableSchemaId}_check_to_prefill`}
              inputId={`${table.tableSchemaId}_check_to_prefill`}
              label="Default"
              onChange={e => onChangeToPrefill(e.checked)}
            />
          </div>
          <div>
            <span
              className={styles.switchTextInput}
              id={`${table.tableSchemaId}_check_fixed_number_label`}
              style={{ opacity: isDesignDatasetEditorRead || isDataflowOpen ? 0.5 : 1 }}>
              {resources.messages['fixedNumber']}
            </span>
            <Checkbox
              ariaLabelledBy={`${table.tableSchemaId}_check_fixed_number_label`}
              checked={fixedNumber}
              className={styles.fieldDesignerItem}
              disabled={isDataflowOpen || isDesignDatasetEditorRead || isReferenceDataset}
              id={`${table.tableSchemaId}_check_fixed_number`}
              inputId={`${table.tableSchemaId}_check_fixed_number`}
              label="Default"
              onChange={e => onChangeFixedNumber(e.checked)}
            />
            <label className="srOnly" htmlFor={`${table.tableSchemaId}_check_fixed_number`}>
              {resources.messages['fixedNumber']}
            </label>
          </div>
          <div>
            <span
              className={styles.switchTextInput}
              id={`${table.tableSchemaId}_check_not_empty_label`}
              style={{ opacity: isDesignDatasetEditorRead || isDataflowOpen ? 0.5 : 1 }}>
              {resources.messages['notEmpty']}
            </span>
            <Checkbox
              ariaLabelledBy={`${table.tableSchemaId}_check_not_empty_label`}
              checked={notEmpty}
              className={styles.fieldDesignerItem}
              disabled={isDataflowOpen || isDesignDatasetEditorRead || isReferenceDataset}
              id={`${table.tableSchemaId}_check_not_empty`}
              inputId={`${table.tableSchemaId}_check_not_empty`}
              label="Default"
              onChange={e => onChangeNotEmpty(e.checked)}
            />
            <label className="srOnly" htmlFor={`${table.tableSchemaId}_check_not_empty`}>
              {resources.messages['notEmpty']}
            </label>
          </div>
        </div>
      </div>
      {!viewType['tabularData'] && (
        <div className={styles.fieldsHeader}>
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
          <label className={styles.requiredWrap}>{resources.messages['required']}</label>
          <label className={styles.readOnlyWrap}>{resources.messages['readOnly']}</label>
          <label className={isCodelistOrLink ? styles.withCodelistOrLink : ''}>
            {resources.messages['newFieldPlaceHolder']}
          </label>
          <label>{resources.messages['newFieldDescriptionPlaceHolder']}</label>
          <label>{resources.messages['newFieldTypePlaceHolder']}</label>
        </div>
      )}
      {renderAllFields()}
      {renderErrors(errorMessageAndTitle.title, errorMessageAndTitle.message, errorMessageAndTitle.focusElement)}
      {!isErrorDialogVisible && isDeleteDialogVisible && renderConfirmDialog()}
      {designerState.isImportTableSchemaDialogVisible && (
        <CustomFileUpload
          accept=".csv"
          chooseLabel={resources.messages['selectFile']}
          className={styles.FileUpload}
          dialogClassName={styles.Dialog}
          dialogHeader={`${resources.messages['importTableSchemaDialogHeader']} ${table.tableSchemaName}`}
          dialogOnHide={() => manageDialogs('isImportTableSchemaDialogVisible', false)}
          dialogVisible={designerState.isImportTableSchemaDialogVisible}
          fileLimit={1}
          infoTooltip={`${resources.messages['supportedFileExtensionsTooltip']} .csv`}
          invalidExtensionMessage={resources.messages['invalidExtensionFile']}
          isDialog={true}
          mode="advanced"
          multiple={false}
          name="file"
          onError={onImportTableSchemaError}
          onUpload={onUpload}
          replaceCheck={true}
          replaceCheckDisabled={hasPKReferenced}
          replaceCheckLabelMessage={resources.messages['replaceDataPKInUse']}
          url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.importTableSchema, {
            datasetSchemaId: designerState.datasetSchemaId,
            datasetId: datasetId,
            tableSchemaId: table.tableSchemaId
          })}`}
        />
      )}
    </Fragment>
  );
};
FieldsDesigner.propTypes = {};
