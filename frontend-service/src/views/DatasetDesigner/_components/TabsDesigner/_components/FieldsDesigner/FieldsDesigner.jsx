import { Fragment, useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { DatasetConfig } from 'repositories/config/DatasetConfig';

import styles from './FieldsDesigner.module.scss';

import { config } from 'conf';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { CharacterCounter } from 'views/_components/CharacterCounter';
import { Checkbox } from 'views/_components/Checkbox';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { CustomFileUpload } from 'views/_components/CustomFileUpload';
import { DataViewer } from 'views/_components/DataViewer';
import { Dialog } from 'views/_components/Dialog';
import { DownloadFile } from 'views/_components/DownloadFile';
import { FieldDesigner } from './_components/FieldDesigner';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { InputTextarea } from 'views/_components/InputTextarea';
import ReactTooltip from 'react-tooltip';
import { Spinner } from 'views/_components/Spinner';
import { Toolbar } from 'views/_components/Toolbar';

import { DatasetService } from 'services/DatasetService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

import { FieldsDesignerUtils } from 'views/_functions/Utils/FieldsDesignerUtils';
import { MetadataUtils, RecordUtils } from 'views/_functions/Utils';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';
import { uniqueId } from 'lodash';

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
  manageDialogs,
  manageUniqueConstraint,
  onChangeFields,
  onChangeTableProperties,
  onHideSelectGroupedValidation,
  onLoadTableData,
  selectedRuleId,
  selectedRuleLevelError,
  selectedRuleMessage,
  selectedTableSchemaId,
  table,
  viewType
}) => {
  const notificationContext = useContext(NotificationContext);
  const validationContext = useContext(ValidationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [bulkDelete, setBulkDelete] = useState(false);
  const [errorMessageAndTitle, setErrorMessageAndTitle] = useState({ title: '', message: '' });
  const [exportTableSchema, setExportTableSchema] = useState(undefined);
  const [exportTableSchemaName, setExportTableSchemaName] = useState('');
  const [fields, setFields] = useState();
  const [fieldToDeleteType, setFieldToDeleteType] = useState();
  const [fixedNumber, setFixedNumber] = useState(false);
  const [indexToDelete, setIndexToDelete] = useState();
  const [initialFieldIndexDragged, setInitialFieldIndexDragged] = useState();
  const [initialTableDescription, setInitialTableDescription] = useState();
  const [isCodelistOrLink, setIsCodelistOrLink] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isErrorDialogVisible, setIsErrorDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isReadOnlyTable, setIsReadOnlyTable] = useState(false);
  const [markedForDeletion, setMarkedForDeletion] = useState([]);
  const [notEmpty, setNotEmpty] = useState(true);
  const [refElement, setRefElement] = useState();
  const [tableDescriptionValue, setTableDescriptionValue] = useState('');
  const [toPrefill, setToPrefill] = useState(false);

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
    if (!isLoading && !isNil(refElement)) {
      refElement.focus();
    }
  }, [isLoading]);

  useEffect(() => {
    if (!isUndefined(fields)) {
      setIsCodelistOrLink(
        fields.filter(field =>
          ['CODELIST', 'MULTISELECT_CODELIST', 'EXTERNAL_LINK', 'LINK', 'ATTACHMENT'].includes(field.type.toUpperCase())
        ).length > 0
      );
      if (markedForDeletion.length > 0) {
        const inmMarkedForDeletion = [...markedForDeletion];
        inmMarkedForDeletion.forEach(markedField => {
          const field = fields.find(field => field.fieldId === markedField.fieldId);
          if (!isNil(field)) {
            markedField.fieldType = RecordUtils.getFieldTypeValue(field.type)?.value;
            markedField.fieldName = field.name;
          }
        });
        setMarkedForDeletion(inmMarkedForDeletion);
      }
    }
  }, [fields]);

  useEffect(() => {
    if (!isUndefined(exportTableSchema)) {
      DownloadFile(exportTableSchema, exportTableSchemaName);
    }
  }, [exportTableSchema]);

  useEffect(() => {
    ReactTooltip.rebuild();
  }, [bulkDelete]);

  const onBulkCheck = ({
    checked,
    fieldId,
    fieldIndex,
    fieldName,
    fieldsSelected = [],
    fieldType,
    multiple = false
  }) => {
    if (multiple) {
      const inmMarkedForDeletion = [...markedForDeletion];
      fieldsSelected.forEach(field => {
        if (!markedForDeletion.some(markedField => markedField.fieldId === field.fieldId)) {
          inmMarkedForDeletion.push(field);
        }
      });
      setMarkedForDeletion(inmMarkedForDeletion);
    } else if (checked) {
      setMarkedForDeletion([...markedForDeletion, { fieldId, fieldType, fieldName, fieldIndex }]);
    } else {
      setMarkedForDeletion(markedForDeletion.filter(markedField => markedField.fieldId !== fieldId));
    }
  };

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
      setIsLoading(true);
      await DatasetService.deleteFieldDesign(datasetId, fields[deletedFieldIndex].fieldId);
      const inmFields = [...fields];
      inmFields.splice(deletedFieldIndex, 1);
      onChangeFields(
        inmFields,
        TextUtils.areEquals(deletedFieldType, 'LINK') || TextUtils.areEquals(deletedFieldType, 'EXTERNAL_LINK'),
        table.tableSchemaId
      );
      setFields(inmFields);
    } catch (error) {
      console.error('FieldsDesigner - deleteField.', error);
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const deleteFields = async () => {
    setIsLoading(true);

    try {
      const deletionPromises = markedForDeletion.map(async markedField => {
        return await DatasetService.deleteFieldDesign(datasetId, markedField.fieldId);
      });

      Promise.all(deletionPromises)
        .then(() => {
          const filteredFields = fields.filter(
            inmField => !markedForDeletion.some(markedField => markedField.fieldId === inmField.fieldId)
          );
          onChangeFields(
            filteredFields,
            markedForDeletion.some(markedField => TextUtils.areEquals(markedField.fieldType.fieldType, 'LINK')) ||
              markedForDeletion.some(markedField =>
                TextUtils.areEquals(markedField.fieldType.fieldType, 'EXTERNAL_LINK')
              ),
            table.tableSchemaId
          );
          setFields(filteredFields);
          setIsDeleteDialogVisible(false);
          setMarkedForDeletion([]);
        })
        .catch(error => {
          console.error(`FieldsDesigner - deleteFields.`, error);
        })
        .finally(() => {
          setBulkDelete(false);
          setIsLoading(false);
        });
    } catch (error) {
      console.error('FieldsDesigner - deleteFields.', error);
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      }
      setIsLoading(false);
    }
  };

  const errorDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        icon="check"
        label={resourcesContext.messages['ok']}
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
          datasetSchemaId={datasetSchemaId}
          hasWritePermissions={true}
          isDataflowOpen={isDataflowOpen}
          isDesignDatasetEditorRead={isDesignDatasetEditorRead}
          isExportable={true}
          isGroupedValidationDeleted={isGroupedValidationDeleted}
          isGroupedValidationSelected={isGroupedValidationSelected}
          key={table.id}
          levelErrorTypes={table.levelErrorTypes}
          onHideSelectGroupedValidation={onHideSelectGroupedValidation}
          onLoadTableData={onLoadTableData}
          reporting={false}
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
      header={
        markedForDeletion.length === 0
          ? resourcesContext.messages['deleteFieldTitle']
          : resourcesContext.messages['deleteFieldBulkTitle']
      }
      labelCancel={resourcesContext.messages['no']}
      labelConfirm={resourcesContext.messages['yes']}
      onConfirm={() => {
        if (markedForDeletion.length === 0) {
          deleteField(indexToDelete, fieldToDeleteType);
        } else {
          deleteFields();
        }
        setIsDeleteDialogVisible(false);
      }}
      onHide={() => setIsDeleteDialogVisible(false)}
      visible={isDeleteDialogVisible}>
      {markedForDeletion.length === 0
        ? resourcesContext.messages['deleteFieldConfirm']
        : resourcesContext.messages['deleteFieldBulkConfirm']}
      {markedForDeletion.length > 0 ? (
        <ul className={styles.markedForDeletionList}>
          {markedForDeletion.map(markedField => (
            <li key={uniqueId('markedField_')}>
              <div>
                <span data-for={markedField.fieldName} data-tip>
                  {TextUtils.ellipsis(markedField.fieldName, 25)}
                </span>
              </div>
              <div>
                <span>
                  {markedField.fieldType.fieldType}
                  <FontAwesomeIcon icon={AwesomeIcons(markedField.fieldType.fieldTypeIcon)} role="presentation" />
                </span>
              </div>
              <ReactTooltip
                border={true}
                className={styles.tooltip}
                effect="solid"
                id={markedField.fieldName}
                place="top">
                {markedField.fieldName}
              </ReactTooltip>
            </li>
          ))}
        </ul>
      ) : (
        ''
      )}
    </ConfirmDialog>
  );

  const renderAllFields = () => (
    <div className={styles.fieldsWrapper}>
      {isLoading && (
        <div className={styles.overlay}>
          <Spinner className={styles.spinner} />
        </div>
      )}
      {viewType['tabularData'] ? (!isEmpty(fields) ? previewData() : renderNoFields()) : renderFields()}
      {!viewType['tabularData'] && renderNewField()}
    </div>
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
          isLoading={isLoading}
          isReferenceDataset={isReferenceDataset}
          onCodelistAndLinkShow={onCodelistAndLinkShow}
          onFieldDragAndDrop={onFieldDragAndDrop}
          onNewFieldAdd={onFieldAdd}
          onShowDialogError={onShowDialogError}
          recordSchemaId={!isUndefined(table.recordSchemaId) ? table.recordSchemaId : table.recordId}
          setIsLoading={(loading, ref) => {
            setRefElement(ref.element);
            setIsLoading(loading);
          }}
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
                bulkDelete={bulkDelete}
                checkDuplicates={(name, fieldId) => FieldsDesignerUtils.checkDuplicates(fields, name, fieldId)}
                checkInvalidCharacters={name => FieldsDesignerUtils.checkInvalidCharacters(name)}
                codelistItems={!isNil(field.codelistItems) ? field.codelistItems : []}
                datasetId={datasetId}
                datasetSchemaId={datasetSchemaId}
                fieldDescription={field.description || ''}
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
                isLoading={isLoading}
                isReferenceDataset={isReferenceDataset}
                key={field.fieldId}
                markedForDeletion={markedForDeletion}
                onBulkCheck={onBulkCheck}
                onCodelistAndLinkShow={onCodelistAndLinkShow}
                onFieldDelete={onFieldDelete}
                onFieldDragAndDrop={onFieldDragAndDrop}
                onFieldDragAndDropStart={onFieldDragAndDropStart}
                onFieldUpdate={onFieldUpdate}
                onNewFieldAdd={onFieldAdd}
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
      setIsLoading(true);
      const inmFields = [...fields];
      const droppedFieldIdx = FieldsDesignerUtils.getIndexByFieldName(droppedFieldName, inmFields);
      await DatasetService.updateFieldOrder(
        datasetId,
        droppedFieldIdx === -1
          ? inmFields.length
          : draggedFieldIdx < droppedFieldIdx
          ? droppedFieldIdx - 1
          : droppedFieldIdx,
        inmFields[draggedFieldIdx].fieldId
      );
      setFields([...FieldsDesignerUtils.arrayShift(inmFields, draggedFieldIdx, droppedFieldIdx)]);
      onChangeFields(inmFields, false, table.tableSchemaId);
    } catch (error) {
      console.error('FieldsDesigner - reorderField.', error);
    } finally {
      setIsLoading(false);
    }
  };

  const renderNoFields = () => (
    <div>
      <h3>{resourcesContext.messages['datasetDesignerNoFields']}</h3>
    </div>
  );

  const updateTableDesign = async ({ fixedNumber, notEmpty, readOnly, toPrefill }) => {
    try {
      if (initialTableDescription !== tableDescriptionValue) {
        await DatasetService.updateTableDesign(
          toPrefill,
          table.tableSchemaId,
          tableDescriptionValue,
          readOnly,
          datasetId,
          notEmpty,
          fixedNumber
        );
        onChangeTableProperties(table.tableSchemaId, tableDescriptionValue, readOnly, toPrefill, notEmpty, fixedNumber);
      }
    } catch (error) {
      console.error('FieldsDesigner - updateTableDesign.', error);
    }
  };

  const onUpload = async () => {
    notificationContext.add({ type: 'IMPORT_TABLE_SCHEMA_INIT' });
    manageDialogs('isImportTableSchemaDialogVisible', false);
  };

  const createTableName = (tableName, fileType) => `${tableName}.${fileType}`;

  const onExportTableSchema = async fileType => {
    try {
      setExportTableSchemaName(createTableName(table.tableSchemaName, fileType));
      const { data } = await DatasetService.exportTableSchema(
        datasetId,
        designerState.datasetSchemaId,
        table.tableSchemaId,
        fileType
      );
      setExportTableSchema(data);
    } catch (error) {
      console.error('FieldsDesigner - onExportTableSchema.', error);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
      notificationContext.add(
        {
          type: 'EXPORT_TABLE_DATA_BY_ID_ERROR',
          content: {
            dataflowId,
            datasetId,
            dataflowName,
            datasetName,
            customContent: { tableName: designerState.tableName }
          }
        },
        true
      );
    }
  };

  const onImportTableSchemaError = async ({ xhr }) => {
    if (xhr.status === 423) {
      notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
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
            icon="import"
            label={resourcesContext.messages['importTableSchema']}
            onClick={() => manageDialogs('isImportTableSchemaDialogVisible', true)}
          />
          <Button
            className={`p-button-rounded p-button-secondary-transparent ${
              !isDataflowOpen && !isDesignDatasetEditorRead ? 'p-button-animated-blink' : null
            }`}
            disabled={isDataflowOpen || isDesignDatasetEditorRead}
            icon="export"
            label={resourcesContext.messages['exportTableSchema']}
            onClick={() => onExportTableSchema('csv', true)}
          />
          <Button
            className={`p-button-secondary-transparent ${
              !isDesignDatasetEditorRead && (!isDataflowOpen || !isReferenceDataset) ? 'p-button-animated-blink' : null
            } datasetSchema-uniques-help-step`}
            disabled={isDesignDatasetEditorRead || (isDataflowOpen && isReferenceDataset)}
            icon="key"
            label={resourcesContext.messages['addUniqueConstraint']}
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
            icon="horizontalSliders"
            label={resourcesContext.messages['addRowConstraint']}
            onClick={() => validationContext.onOpenModalFromRow(table.recordSchemaId)}
          />
        </div>
      </Toolbar>
      <h4 className={styles.descriptionLabel}>{resourcesContext.messages['newTableDescriptionPlaceHolder']}</h4>
      <div className={styles.tableDescriptionRow}>
        <div>
          <InputTextarea
            className={styles.tableDescriptionInput}
            collapsedHeight={75}
            disabled={isDataflowOpen || isDesignDatasetEditorRead}
            id="tableDescription"
            key="tableDescription"
            onBlur={() => updateTableDesign({ readOnly: isReadOnlyTable, toPrefill, notEmpty, fixedNumber })}
            onChange={e => setTableDescriptionValue(e.target.value)}
            onFocus={e => {
              setInitialTableDescription(e.target.value);
            }}
            onKeyDown={e => onKeyChange(e)}
            placeholder={resourcesContext.messages['newTableDescriptionPlaceHolder']}
            value={tableDescriptionValue}
          />
          <CharacterCounter
            currentLength={tableDescriptionValue.length}
            maxLength={config.DESCRIPTION_MAX_LENGTH}
            style={{ position: 'relative', top: '4px' }}
          />
        </div>
        <div className={`${styles.switchDiv} datasetSchema-readOnlyAndPrefill-help-step`}>
          <div>
            <span
              className={styles.switchTextInput}
              id={`${table.tableSchemaId}_check_readOnly_label`}
              style={{ opacity: isDesignDatasetEditorRead || isDataflowOpen ? 0.5 : 1 }}>
              {resourcesContext.messages['readOnlyTable']}
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
              {resourcesContext.messages['prefilled']}
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
              {resourcesContext.messages['fixedNumber']}
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
              {resourcesContext.messages['fixedNumber']}
            </label>
          </div>
          <div>
            <span
              className={styles.switchTextInput}
              id={`${table.tableSchemaId}_check_not_empty_label`}
              style={{ opacity: isDesignDatasetEditorRead || isDataflowOpen ? 0.5 : 1 }}>
              {resourcesContext.messages['notEmpty']}
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
              {resourcesContext.messages['notEmpty']}
            </label>
          </div>
        </div>
      </div>
      {!viewType['tabularData'] && (
        <div className={styles.fieldsHeader}>
          <span className={styles.PKWrap}>
            <label>{resourcesContext.messages['pk']}</label>
            <Button
              className={`${styles.PKInfoButton} p-button-rounded p-button-secondary-transparent`}
              icon="infoCircle"
              id="infoPk"
              title={resourcesContext.messages['PKTooltip']}
              tooltip={resourcesContext.messages['PKTooltip']}
              tooltipOptions={{ position: 'top' }}
            />
          </span>
          <label>{resourcesContext.messages['required']}</label>
          <label>{resourcesContext.messages['readOnly']}</label>
          <label className={isCodelistOrLink ? styles.withCodelistOrLink : ''}>
            {resourcesContext.messages['newFieldPlaceHolder']}
          </label>
          <label>{resourcesContext.messages['newFieldDescriptionPlaceHolder']}</label>
          <label>{resourcesContext.messages['newFieldTypePlaceHolder']}</label>
          <label className={isCodelistOrLink ? styles.withCodelistOrLink : ''}></label>
          <label></label>
          <label></label>
          {/* <label>
            <div
              className={`${styles.bulkDeleteButton} ${
                markedForDeletion.length === 0 && bulkDelete ? styles.disabledButton : ''
              } ${bulkDelete ? styles.bulkConfirmDeleteButton : ''}`}
              data-for="bulkDeleteTooltip"
              data-tip
              onClick={e => {
                e.preventDefault();
                if (markedForDeletion.length > 0) {
                  setIsDeleteDialogVisible(true);
                } else {
                  setBulkDelete(!bulkDelete);
                }
              }}>
              <FontAwesomeIcon
                aria-label={resourcesContext.messages['deleteFieldLabel']}
                icon={AwesomeIcons(!bulkDelete ? 'check' : 'delete')}
              />
              <span className="srOnly">{resourcesContext.messages['deleteFieldLabel']}</span>
            </div>
            {bulkDelete && (
              <div
                className={`${styles.bulkDeleteButton} ${styles.bulkCancelDeleteButton}`}
                data-for="bulkDeleteCancelTooltip"
                data-tip
                onClick={e => {
                  e.preventDefault();
                  setMarkedForDeletion([]);
                  setBulkDelete(false);
                }}>
                <FontAwesomeIcon aria-label={resourcesContext.messages['cancel']} icon={AwesomeIcons('cross')} />
                <span className="srOnly">{resourcesContext.messages['cancel']}</span>
              </div>
            )}
            <ReactTooltip border={true} effect="solid" id="bulkDeleteTooltip" place="top">
              {!bulkDelete ? (
                <div>
                  <p>{resourcesContext.messages['bulkDeleteCheckTooltip']}</p>
                  <p className={styles.bulkCancelDeleteButtonTooltip}>
                    {resourcesContext.messages['bulkDeleteCheckTooltipShift']}
                  </p>
                </div>
              ) : (
                resourcesContext.messages['bulkDeleteConfirmTooltip']
              )}
            </ReactTooltip>
            <ReactTooltip border={true} effect="solid" id="bulkDeleteCancelTooltip" place="top">
              {resourcesContext.messages['cancel']}
            </ReactTooltip>
          </label> */}
        </div>
      )}
      {renderAllFields()}
      {renderErrors(errorMessageAndTitle.title, errorMessageAndTitle.message, errorMessageAndTitle.focusElement)}
      {!isErrorDialogVisible && isDeleteDialogVisible && renderConfirmDialog()}
      {designerState.isImportTableSchemaDialogVisible && (
        <CustomFileUpload
          accept=".csv"
          chooseLabel={resourcesContext.messages['selectFile']}
          className={styles.FileUpload}
          dialogClassName={styles.Dialog}
          dialogHeader={`${resourcesContext.messages['importTableSchemaDialogHeader']} ${table.tableSchemaName}`}
          dialogOnHide={() => manageDialogs('isImportTableSchemaDialogVisible', false)}
          dialogVisible={designerState.isImportTableSchemaDialogVisible}
          infoTooltip={`${resourcesContext.messages['supportedFileExtensionsTooltip']} .csv`}
          invalidExtensionMessage={resourcesContext.messages['invalidExtensionFile']}
          isDialog={true}
          mode="advanced"
          name="file"
          onError={onImportTableSchemaError}
          onUpload={onUpload}
          replaceCheck={true}
          replaceCheckDisabled={hasPKReferenced}
          replaceCheckLabelMessage={resourcesContext.messages['replaceDataPKInUse']}
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
