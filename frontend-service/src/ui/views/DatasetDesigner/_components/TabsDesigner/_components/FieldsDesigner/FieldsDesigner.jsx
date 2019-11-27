import React, { useContext, useEffect, useState } from 'react';
import { capitalize, isUndefined, isNull } from 'lodash';

import styles from './FieldsDesigner.module.css';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataViewer } from 'ui/views/ReporterDataSet/_components/TabsSchema/_components/DataViewer';
import { Dialog } from 'ui/views/_components/Dialog';
import { FieldDesigner } from './_components/FieldDesigner';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { DatasetService } from 'core/services/DataSet';

export const FieldsDesigner = ({ datasetId, table, onChangeFields }) => {
  const [errorMessageAndTitle, setErrorMessageAndTitle] = useState({ title: '', message: '' });
  const [fields, setFields] = useState([]);
  const [initialFieldIndexDragged, setinitialFieldIndexDragged] = useState();
  const [indexToDelete, setIndexToDelete] = useState();
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isErrorDialogVisible, setIsErrorDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isPreviewModeOn, setIsPreviewModeOn] = useState(false);
  const [levelErrorTypes, setLevelErrorTypes] = useState([]);

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
  }, []);

  useEffect(() => {
    if (isPreviewModeOn) {
      setLevelErrorTypes(onLoadErrorTypes());
    }
  }, [isPreviewModeOn]);

  const onLoadErrorTypes = async () => {
    const datasetSchema = await DatasetService.schemaById(datasetId);
    return datasetSchema.levelErrorTypes;
  };

  const onFieldAdd = (fieldId, fieldName, recordId, fieldType) => {
    const inmFields = [...fields];
    inmFields.splice(inmFields.length, 0, { fieldId, name: fieldName, recordId, type: fieldType });
    onChangeFields(inmFields, table.tableSchemaId);
    setFields(inmFields);
  };

  const onFieldDelete = deletedFieldIndx => {
    setIndexToDelete(deletedFieldIndx);
    setIsDeleteDialogVisible(true);
  };

  const onFieldUpdate = (fieldId, fieldName, fieldType) => {
    const inmFields = [...fields];
    const fieldIndex = getIndexByFieldId(fieldId, inmFields);
    if (fieldIndex > -1) {
      inmFields[fieldIndex].name = fieldName;
      inmFields[fieldIndex].type = fieldType;
      setFields(inmFields);
    }
  };

  const onFieldDragAndDrop = (draggedFieldIdx, droppedFieldName) => {
    reorderField(draggedFieldIdx, droppedFieldName);
  };

  const onFieldDragAndDropStart = draggedFieldIdx => {
    setinitialFieldIndexDragged(draggedFieldIdx);
  };

  const onShowDialogError = (message, title) => {
    setErrorMessageAndTitle({ title, message });
    setIsErrorDialogVisible(true);
  };

  const arrayShift = (arr, initialIdx, endIdx) => {
    const element = arr[initialIdx];
    if (endIdx === -1) {
      arr.splice(initialIdx, 1);
      arr.splice(arr.length, 0, element);
    } else {
      if (Math.abs(endIdx - initialIdx) > 1) {
        arr.splice(initialIdx, 1);
        if (initialIdx < endIdx) {
          arr.splice(endIdx - 1, 0, element);
        } else {
          arr.splice(endIdx, 0, element);
        }
      } else {
        if (endIdx === 0) {
          arr.splice(initialIdx, 1);
          arr.splice(0, 0, element);
        } else {
          arr.splice(initialIdx, 1);
          if (initialIdx < endIdx) {
            arr.splice(endIdx - 1, 0, element);
          } else {
            arr.splice(endIdx, 0, element);
          }
        }
      }
    }
    return arr;
  };

  const checkDuplicates = (name, fieldId) => {
    if (!isUndefined(fields) && !isNull(fields)) {
      const inmFields = [...fields];
      const repeteadElements = inmFields.filter(field => name.toLowerCase() === field.name.toLowerCase());
      return repeteadElements.length > 0 && fieldId !== repeteadElements[0].fieldId;
    } else {
      return false;
    }
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

  const getIndexByFieldName = (fieldName, fieldsArray) => {
    return fieldsArray
      .map(field => {
        return field.name;
      })
      .indexOf(fieldName);
  };

  const getIndexByFieldId = (fieldId, fieldsArray) => {
    return fieldsArray
      .map(field => {
        return field.fieldId;
      })
      .indexOf(fieldId);
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
              recordId: field['recordId']
            };
          })
        : [];

    return !isUndefined(table) && !isUndefined(table.records) && !isNull(table.records) ? (
      <DataViewer
        hasWritePermissions={true}
        isWebFormMMR={false}
        // buttonsList={[]}
        key={table.id}
        levelErrorTypes={levelErrorTypes}
        tableId={table.tableSchemaId}
        tableName={table}
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
          checkDuplicates={checkDuplicates}
          datasetId={datasetId}
          fieldId="-1"
          fieldName=""
          fieldType=""
          fieldValue=""
          index="-1"
          initialFieldIndexDragged={initialFieldIndexDragged}
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
    const renderedFields =
      !isUndefined(fields) && !isNull(fields) ? (
        fields.map((field, index) => (
          <div className={styles.fieldDesignerWrapper} key={field.fieldId}>
            <FieldDesigner
              checkDuplicates={checkDuplicates}
              datasetId={datasetId}
              fieldId={field.fieldId}
              fieldName={field.name}
              fieldType={field.type}
              fieldValue={field.value}
              index={index}
              initialFieldIndexDragged={initialFieldIndexDragged}
              key={field.fieldId}
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
      const droppedFieldIdx = getIndexByFieldName(droppedFieldName, inmFields);
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
        setFields([...arrayShift(inmFields, draggedFieldIdx, droppedFieldIdx)]);
      }
    } catch (error) {
      console.error(`There has been an error during the field reorder: ${error}`);
    }
  };

  //return fieldsSchema.map(field => {
  return (
    <React.Fragment>
      <div className={styles.InputSwitchContainer}>
        <div className={styles.InputSwitchDiv}>
          <span className={styles.InputSwitchText}>{resources.messages['design']}</span>
          <InputSwitch
            checked={isPreviewModeOn}
            // disabled={true}
            disabled={!isUndefined(fields) ? (fields.length === 0 ? true : false) : false}
            onChange={e => {
              setIsPreviewModeOn(e.value);
            }}
          />
          <span className={styles.InputSwitchText}>{resources.messages['preview']}</span>
        </div>
      </div>
      <div className={styles.fieldsWrapper}>{renderAllFields()}</div>
      {renderErrors(errorMessageAndTitle.title, errorMessageAndTitle.message)}
      {!isErrorDialogVisible ? renderConfirmDialog() : null}
    </React.Fragment>
  );
  // });
};
FieldsDesigner.propTypes = {};
