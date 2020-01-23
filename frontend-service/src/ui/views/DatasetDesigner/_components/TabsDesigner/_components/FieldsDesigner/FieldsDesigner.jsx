import React, { useContext, useEffect, useState } from 'react';
import { capitalize, isUndefined, isNull } from 'lodash';

import styles from './FieldsDesigner.module.css';

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

export const FieldsDesigner = ({ datasetId, table, onChangeFields, onChangeTableDescription }) => {
  const [errorMessageAndTitle, setErrorMessageAndTitle] = useState({ title: '', message: '' });
  const [fields, setFields] = useState([]);
  const [initialFieldIndexDragged, setinitialFieldIndexDragged] = useState();
  const [initialTableDescription, setInitialTableDescription] = useState();
  const [indexToDelete, setIndexToDelete] = useState();
  const [isCodelistSelected, setIsCodelistSelected] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isErrorDialogVisible, setIsErrorDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isPreviewModeOn, setIsPreviewModeOn] = useState(false);
  const [levelErrorTypes, setLevelErrorTypes] = useState([]);
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
    if (isPreviewModeOn) {
      setLevelErrorTypes(onLoadErrorTypes());
    }
  }, [isPreviewModeOn]);

  const onCodelistShow = (fieldId, selectedField) => {
    console.log(fieldId, fields, selectedField);
    console.log(fields.filter(field => field.type.toUpperCase() === 'CODELIST' && field.fieldId !== fieldId));
    console.log(selectedField.fieldType.toUpperCase() === 'CODELIST');
    // console.log(selectedField.fieldType.toUpperCase() === 'CODELIST', selectedField);
    setIsCodelistSelected(
      fields.filter(field => field.type.toUpperCase() === 'CODELIST' && field.fieldId !== fieldId).length > 0 ||
        selectedField.fieldType.toUpperCase() === 'CODELIST'
    );
  };

  const onFieldAdd = (fieldId, fieldName, recordId, fieldType, fieldDescription) => {
    const inmFields = [...fields];
    inmFields.splice(inmFields.length, 0, {
      fieldId,
      name: fieldName,
      recordId,
      type: fieldType,
      description: fieldDescription
    });
    onChangeFields(inmFields, table.tableSchemaId);
    setFields(inmFields);
  };

  const onFieldDelete = deletedFieldIndx => {
    setIndexToDelete(deletedFieldIndx);
    setIsDeleteDialogVisible(true);
  };

  const onFieldUpdate = (fieldId, fieldName, fieldType, fieldDescription) => {
    const inmFields = [...fields];
    const fieldIndex = FieldsDesignerUtils.getIndexByFieldId(fieldId, inmFields);
    if (fieldIndex > -1) {
      inmFields[fieldIndex].name = fieldName;
      inmFields[fieldIndex].type = fieldType;
      inmFields[fieldIndex].description = fieldDescription;
      setFields(inmFields);
    }
  };

  const onFieldDragAndDrop = (draggedFieldIdx, droppedFieldName) => {
    reorderField(draggedFieldIdx, droppedFieldName);
  };

  const onFieldDragAndDropStart = draggedFieldIdx => {
    setinitialFieldIndexDragged(draggedFieldIdx);
  };

  const onKeyChange = event => {
    if (event.key === 'Escape') {
      setTableDescriptionValue(initialTableDescription);
    } else if (event.key == 'Enter') {
      event.preventDefault();
      //API CALL
      updateTableDescriptionDesign();
    }
  };

  const onLoadErrorTypes = async () => {
    const datasetSchema = await DatasetService.schemaById(datasetId);
    return datasetSchema.levelErrorTypes;
  };

  const onShowDialogError = (message, title) => {
    setErrorMessageAndTitle({ title, message });
    setIsErrorDialogVisible(true);
  };

  const deleteField = async deletedFieldIndx => {
    // setIsLoading(true);
    try {
      const fieldDeleted = await DatasetService.deleteRecordFieldDesign(datasetId, fields[deletedFieldIndx].fieldId);
      if (fieldDeleted) {
        const inmFields = [...fields];
        inmFields.splice(deletedFieldIndx, 1);
        onChangeFields(inmFields, table.tableSchemaId);
        setFields(inmFields);
      } else {
        console.error('Error during field delete');
      }
    } catch (error) {
      console.error('Error during field delete');
    } finally {
      // setIsLoading(false);
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

  const previewData = () => {
    const tableSchemaColumns =
      !isUndefined(fields) && !isNull(fields)
        ? fields.map(field => {
            return {
              table: table['tableSchemaName'],
              field: field['fieldId'],
              header: `${capitalize(field['name'])}`,
              type: field['type'],
              recordId: field['recordId']
            };
          })
        : [];

    return !isUndefined(table) && !isUndefined(table.records) && !isNull(table.records) ? (
      <DataViewer
        hasWritePermissions={true}
        isPreviewModeOn={isPreviewModeOn}
        isWebFormMMR={false}
        key={table.id}
        levelErrorTypes={levelErrorTypes}
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
        header={resources.messages['deleteFieldTitle']}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        onConfirm={() => {
          deleteField(indexToDelete);
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
        <React.Fragment>
          {isPreviewModeOn ? previewData() : renderFields()}
          {!isPreviewModeOn ? renderNewField() : null}
        </React.Fragment>
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
          datasetId={datasetId}
          fieldId="-1"
          fieldName=""
          fieldType=""
          fieldValue=""
          index="-1"
          initialFieldIndexDragged={initialFieldIndexDragged}
          isCodelistSelected={isCodelistSelected}
          onCodelistShow={onCodelistShow}
          onFieldDragAndDrop={onFieldDragAndDrop}
          onNewFieldAdd={onFieldAdd}
          onShowDialogError={onShowDialogError}
          recordId={!isUndefined(table.recordSchemaId) ? table.recordSchemaId : table.recordId}
          totalFields={!isUndefined(fields) && !isNull(fields) ? fields.length : 0}
        />
      </div>
    );
  };

  const renderFields = () => {
    console.log(
      fields.filter(field => field.type === 'CODELIST').length,
      fields.filter(field => field.type === 'CODELIST')
    );
    const renderedFields =
      !isUndefined(fields) && !isNull(fields) ? (
        fields.map((field, index) => (
          <div className={styles.fieldDesignerWrapper} key={field.fieldId}>
            <FieldDesigner
              checkDuplicates={(name, fieldId) => FieldsDesignerUtils.checkDuplicates(fields, name, fieldId)}
              datasetId={datasetId}
              fieldId={field.fieldId}
              fieldDescription={field.description}
              fieldName={field.name}
              fieldType={field.type}
              fieldValue={field.value}
              index={index}
              initialFieldIndexDragged={initialFieldIndexDragged}
              isCodelistSelected={isCodelistSelected}
              key={field.fieldId}
              onCodelistShow={onCodelistShow}
              onFieldDelete={onFieldDelete}
              onFieldDragAndDrop={onFieldDragAndDrop}
              onFieldDragAndDropStart={onFieldDragAndDropStart}
              onFieldUpdate={onFieldUpdate}
              onShowDialogError={onShowDialogError}
              recordId={field.recordId}
              totalFields={fields.length}
            />
          </div>
        ))
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
      <div className={styles.fieldsWrapper}>{renderAllFields()}</div>
      {renderErrors(errorMessageAndTitle.title, errorMessageAndTitle.message)}
      {!isErrorDialogVisible ? renderConfirmDialog() : null}
    </React.Fragment>
  );
};
FieldsDesigner.propTypes = {};
