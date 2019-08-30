/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef } from 'react';
import { withRouter } from 'react-router-dom';
import { isEmpty } from 'lodash';

import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { routes } from 'ui/routes';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';

import { isUndefined } from 'lodash';

import { config } from 'conf';

import styles from './DataViewer.module.css';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { IconTooltip } from './_components/IconTooltip';
import { InputText } from 'ui/views/_components/InputText';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { Growl } from 'primereact/growl';
import { Menu } from 'primereact/menu';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { getUrl } from 'core/infrastructure/api/getUrl';
import { DataSetService } from 'core/services/DataSet';

const DataViewer = withRouter(
  ({
    buttonsList = undefined,
    recordPositionId,
    selectedRecordErrorId,
    tableId,
    tableName,
    tableSchemaColumns,
    match: {
      params: { dataSetId, dataFlowId }
    },
    history
  }) => {
    //const contextReporterDataSet = useContext(ReporterDataSetContext);
    const [addDialogVisible, setAddDialogVisible] = useState(false);
    const [columnOptions, setColumnOptions] = useState([{}]);
    const [colsSchema, setColsSchema] = useState(tableSchemaColumns);
    const [columns, setColumns] = useState([]);
    const [confirmDeleteVisible, setConfirmDeleteVisible] = useState(false);
    const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
    const [editedRecord, setEditedRecord] = useState({});
    const [editDialogVisible, setEditDialogVisible] = useState(false);
    const [exportButtonsList, setExportButtonsList] = useState([]);
    const [exportTableData, setExportTableData] = useState(undefined);
    const [exportTableDataName, setExportTableDataName] = useState('');
    const [fetchedData, setFetchedData] = useState([]);
    const [firstRow, setFirstRow] = useState(0);
    const [header] = useState();
    const [importDialogVisible, setImportDialogVisible] = useState(false);
    const [isDataDeleted, setIsDataDeleted] = useState(false);
    const [isNewRecord, setIsNewRecord] = useState(false);
    const [isRecordDeleted, setIsRecordDeleted] = useState(false);
    const [loading, setLoading] = useState(false);
    const [loadingFile, setLoadingFile] = useState(false);
    const [newRecord, setNewRecord] = useState({});
    const [numRows, setNumRows] = useState(10);
    const [selectedRecord, setSelectedRecord] = useState({});
    const [sortField, setSortField] = useState(undefined);
    const [sortOrder, setSortOrder] = useState(undefined);
    const [totalRecords, setTotalRecords] = useState(0);

    const resources = useContext(ResourcesContext);

    let growlRef = useRef();
    let exportMenuRef = useRef();

    useEffect(() => {
      setExportButtonsList(
        config.exportTypes.map(type => ({
          label: type.text,
          icon: config.icons['archive'],
          command: () => onExportTableData(type.code)
        }))
      );

      let colOptions = [];
      for (let colSchema of colsSchema) {
        colOptions.push({ label: colSchema.header, value: colSchema });
      }
      setColumnOptions(colOptions);

      const inmTableSchemaColumns = [...tableSchemaColumns];
      inmTableSchemaColumns.push({ table: inmTableSchemaColumns[0].table, field: 'id', header: '' });
      setColsSchema(inmTableSchemaColumns);

      onFetchData(undefined, undefined, 0, numRows);
    }, []);

    useEffect(() => {
      setFetchedData([]);
    }, [isDataDeleted]);

    useEffect(() => {
      onRefresh();
    }, [isRecordDeleted]);

    useEffect(() => {
      if (isUndefined(recordPositionId) || recordPositionId === -1) {
        return;
      }

      setFirstRow(Math.floor(recordPositionId / numRows) * numRows);
      setSortField(undefined);
      setSortOrder(undefined);
      onFetchData(undefined, undefined, Math.floor(recordPositionId / numRows) * numRows, numRows);
    }, [recordPositionId]);

    useEffect(() => {
      let columnsArr = colsSchema.map(column => {
        let sort = column.field === 'id' ? false : true;
        let visibleColumn = column.field === 'id' ? styles.VisibleHeader : '';
        return (
          <Column
            body={dataTemplate}
            className={visibleColumn}
            editor={row => cellDataEditor(row, selectedRecord)}
            //editorValidator={requiredValidator}
            field={column.field}
            header={column.header}
            key={column.field}
            sortable={sort}
          />
        );
      });
      let editCol = (
        <Column key="actions" body={row => actionTemplate(row)} style={{ width: '100px', height: '45px' }} />
      );

      let validationCol = (
        <Column
          body={validationsTemplate}
          field="validations"
          header=""
          key="recordValidation"
          style={{ width: '15px' }}
        />
      );
      let newColumnsArr = [validationCol].concat(columnsArr);
      let newColumnsArr2 = [editCol].concat(newColumnsArr);
      setColumns(newColumnsArr2);
    }, [colsSchema, columnOptions, selectedRecord]);

    useEffect(() => {
      if (!isUndefined(exportTableData)) {
        DownloadFile(exportTableData, exportTableDataName);
      }
    }, [exportTableData]);

    const onChangePage = event => {
      setNumRows(event.rows);
      setFirstRow(event.first);
      onFetchData(sortField, sortOrder, event.first, event.rows);
    };

    const onConfirmDelete = async () => {
      setDeleteDialogVisible(false);
      const dataDeleted = await DataSetService.deleteTableDataById(dataSetId);
      if (dataDeleted) {
        setIsDataDeleted(true);
      }
    };

    const onConfirmDeleteRow = async () => {
      console.log(selectedRecord);
      setDeleteDialogVisible(false);
      let field = selectedRecord.dataRow.filter(row => Object.keys(row.fieldData)[0] === 'id')[0];
      const recordDeleted = await DataSetService.deleteRecordByIds(dataSetId, field.fieldData['id']);
      if (recordDeleted) {
        setIsRecordDeleted(true);
      }
    };

    const onEditAddFormInput = (property, value, field) => {
      let record = {};
      if (!isNewRecord) {
        record = { ...editedRecord };
        field.fieldData[property] = value;
        setEditedRecord(record);
      } else {
        record = { ...newRecord };
        field.fieldData[property] = value;
        setNewRecord(record);
      }
    };

    const onEditorSubmitValue = async (cell, value, record) => {
      if (!isEmpty(record)) {
        let field = record.dataRow.filter(row => Object.keys(row.fieldData)[0] === cell.field)[0].fieldData;
        const fieldUpdated = await DataSetService.updateFieldById(dataSetId, cell.field, field.id, field.type, value);
        if (!fieldUpdated) {
          console.error('Error!');
        }
      }
    };

    const onEditorValueChange = (props, value) => {
      let updatedData = changeCellValue([...props.value], props.rowIndex, props.field, value);
      setFetchedData(updatedData);
    };

    const onExportTableData = async fileType => {
      setLoadingFile(true);
      try {
        setExportTableDataName(createTableName(tableName, fileType));
        setExportTableData(await DataSetService.exportTableDataById(dataSetId, tableId, fileType));
      } catch (error) {
        console.error(error);
      } finally {
        setLoadingFile(false);
      }
    };

    const onFetchData = async (sField, sOrder, fRow, nRows) => {
      setLoading(true);
      try {
        let fields;
        if (!isUndefined(sField) && sField !== null) {
          fields = `${sField}:${sOrder}`;
        }

        const tableData = await DataSetService.tableDataById(
          dataSetId,
          tableId,
          Math.floor(fRow / nRows),
          nRows,
          fields
        );

        if (!isUndefined(colsSchema)) {
          setNewRecord(createEmptyObject(colsSchema));
        }
        if (!isUndefined(tableData.records)) {
          filterDataResponse(tableData);
        }
        if (tableData.totalRecords !== totalRecords) {
          setTotalRecords(tableData.totalRecords);
        }
        setLoading(false);
      } catch (error) {
        console.error('DataViewer error: ', error);
        const errorResponse = error.response;
        console.error('DataViewer errorResponse: ', errorResponse);
        if (!isUndefined(errorResponse) && (errorResponse.status === 401 || errorResponse.status === 403)) {
          history.push(getUrl(config.REPORTING_DATAFLOW.url, { dataFlowId }));
        }
      } finally {
        setLoading(false);
      }
    };

    const onHide = () => {
      setImportDialogVisible(false);
    };

    const onHideConfirmDeleteDialog = () => {
      setConfirmDeleteVisible(false);
    };

    const onRefresh = () => {
      onFetchData(sortField, sortOrder, firstRow, numRows);
    };

    const onSelectRecord = (event, value) => {
      setIsNewRecord(false);
      setSelectedRecord(value);
      setEditedRecord(value);
    };

    const onSaveRow = async record => {
      console.log(isNewRecord, record);
      if (isNewRecord) {
        try {
          await DataSetService.addRecordById(dataSetId, tableId, record);
        } catch (error) {
          console.error('DataViewer error: ', error);
          const errorResponse = error.response;
          console.error('DataViewer errorResponse: ', errorResponse);
          if (!isUndefined(errorResponse) && (errorResponse.status === 401 || errorResponse.status === 403)) {
            history.push(getUrl(config.REPORTING_DATAFLOW.url, { dataFlowId }));
          }
        } finally {
          setLoading(false);
        }
      } else {
        await DataSetService.updateRecordById(dataSetId, tableId, record);
      }
    };

    const onSetVisible = (fnUseState, visible) => {
      fnUseState(visible);
    };

    const onSort = event => {
      setSortOrder(event.sortOrder);
      setSortField(event.sortField);
      setFirstRow(0);
      onFetchData(event.sortField, event.sortOrder, 0, numRows);
    };

    const onUpload = () => {
      setImportDialogVisible(false);

      const detailContent = (
        <span>
          {resources.messages['datasetLoadingMessage']}
          <strong>{editLargeStringWithDots(tableName, 22)}</strong>
          {resources.messages['datasetLoading']}
        </span>
      );

      growlRef.current.show({
        severity: 'info',
        summary: resources.messages['datasetLoadingTitle'],
        detail: detailContent,
        life: '5000'
      });
    };

    const actionTemplate = () => {
      return (
        <div className={styles.actionTemplate}>
          <Button
            type="button"
            icon="edit"
            className={`${`p-button-rounded p-button-secondary ${styles.editRowButton}`}`}
            onClick={() => {
              setEditDialogVisible(true);
            }}
          />
          <Button
            type="button"
            icon="trash"
            className={`${`p-button-rounded p-button-secondary ${styles.deleteRowButton}`}`}
            onClick={() => {
              setConfirmDeleteVisible(true);
            }}
          />
        </div>
      );
    };

    const addRowDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button
          label={resources.messages['cancel']}
          icon="cancel"
          onClick={() => {
            setAddDialogVisible(false);
            setNewRecord({});
          }}
        />
        <Button
          label={resources.messages['save']}
          icon="save"
          onClick={e => {
            onSaveRow(newRecord);
          }}
        />
      </div>
    );

    const addRowFooter = (
      <div className="p-clearfix" style={{ width: '100%' }}>
        <Button
          style={{ float: 'left' }}
          label={resources.messages['add']}
          icon="add"
          onClick={() => {
            setIsNewRecord(true);
            setAddDialogVisible(true);
          }}
        />
      </div>
    );

    const cellDataEditor = (cell, record) => {
      return (
        <InputText
          type="text"
          value={getCellValue(cell, cell.field)}
          onChange={e => onEditorValueChange(cell, e.target.value)}
          onBlur={e => onEditorSubmitValue(cell, e.target.value, record)}
        />
      );
    };

    const changeCellValue = (tableData, rowIndex, field, value) => {
      tableData[rowIndex].dataRow.filter(data => Object.keys(data.fieldData)[0] === field)[0].fieldData[field] = value;
      return tableData;
    };

    const createEmptyObject = columnsSchema => {
      let fields;
      if (!isUndefined(columnsSchema)) {
        fields = columnsSchema.map(column => {
          return {
            fieldData: { [column.field]: null, type: column.type }
          };
        });
      }

      const obj = {
        dataRow: fields,
        recordId: columnsSchema[0].recordId
      };
      return obj;
    };

    const createTableName = (tableName, fileType) => {
      return `${tableName}.${fileType}`;
    };

    const editRowDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button
          label={resources.messages['cancel']}
          icon="cancel"
          onClick={() => {
            console.log(fetchedData);
            setEditedRecord(selectedRecord);
            setEditDialogVisible(false);
          }}
        />
        <Button label={resources.messages['save']} icon="save" onClick={''} />
      </div>
    );

    const editRecordForm = colsSchema.map((column, i) => {
      //Avoid row id Field
      if (editDialogVisible) {
        if (i < colsSchema.length - 1) {
          if (!isUndefined(editedRecord.dataRow)) {
            let field = editedRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
            return (
              <React.Fragment key={column.field}>
                <div className="p-col-4" style={{ padding: '.75em' }}>
                  <label htmlFor={column.field}>{column.header}</label>
                </div>
                <div className="p-col-8" style={{ padding: '.5em' }}>
                  <InputText
                    id={column.field}
                    value={field.fieldData[column.field]}
                    onChange={e => onEditAddFormInput(column.field, e.target.value, field)}
                  />
                </div>
              </React.Fragment>
            );
          }
        }
      }
    });

    const filterDataResponse = data => {
      const dataFiltered = data.records.map(record => {
        const recordValidations = record.validations;
        const arrayDataFields = record.fields.map(field => {
          return {
            fieldData: { [field.fieldSchemaId]: field.value, type: field.type, id: field.fieldId },
            fieldValidations: field.validations
          };
        });
        arrayDataFields.push({ fieldData: { id: record.recordId }, fieldValidations: null });
        const arrayDataAndValidations = {
          dataRow: arrayDataFields,
          recordValidations
        };

        return arrayDataAndValidations;
      });

      setFetchedData(dataFiltered);
    };

    const getCellValue = (tableData, field) => {
      const value = tableData.rowData.dataRow.filter(data => data.fieldData[field]);
      return value.length > 0 ? value[0].fieldData[field] : '';
    };

    const getExportButtonPosition = button => {
      const buttonLeftPosition = document.getElementById('buttonExportTable').offsetLeft;
      const buttonTopPosition = button.style.top;

      const exportTableMenu = document.getElementById('exportTableMenu');
      exportTableMenu.style.top = buttonTopPosition;
      exportTableMenu.style.left = `${buttonLeftPosition}px`;
    };

    const newRecordForm = colsSchema.map((column, i) => {
      if (addDialogVisible) {
        if (i < colsSchema.length - 1) {
          let field = newRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
          return (
            <React.Fragment key={column.field}>
              <div className="p-col-4" style={{ padding: '.75em' }}>
                <label htmlFor={column.field}>{column.header}</label>
              </div>
              <div className="p-col-8" style={{ padding: '.5em' }}>
                <InputText id={column.field} onChange={e => onEditAddFormInput(column.field, e.target.value, field)} />
              </div>
            </React.Fragment>
          );
        }
      }
    });

    const requiredValidator = props => {
      let value = getCellValue(props, props.field);
      return value && value.length > 0;
    };

    //Template for Record validation
    const validationsTemplate = (recordData, column) => {
      if (recordData.recordValidations && !isUndefined(recordData.recordValidations)) {
        const validations = recordData.recordValidations;

        let message = '';
        validations.forEach(validation => (validation.message ? (message += '- ' + validation.message + '\n') : ''));

        let levelError = '';
        let lvlFlag = 0;

        validations.forEach(validation => {
          if (validation.levelError === 'WARNING') {
            const wNum = 1;
            if (wNum > lvlFlag) {
              lvlFlag = wNum;
              levelError = 'WARNING';
            }
          } else if (validation.levelError === 'ERROR') {
            const eNum = 2;
            if (eNum > lvlFlag) {
              lvlFlag = eNum;
              levelError = 'ERROR';
            }
          } else if (validation.levelError === 'BLOCKER') {
            const bNum = 2;
            if (bNum > lvlFlag) {
              lvlFlag = bNum;
              levelError = 'BLOCKER';
            }
          }
        });

        return <IconTooltip levelError={levelError} message={message} />;
      } else {
        return;
      }
    };

    //Template for Field validation
    const dataTemplate = (rowData, column) => {
      let field = rowData.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
      if (field !== null && field && field.fieldValidations !== null && !isUndefined(field.fieldValidations)) {
        const validations = field.fieldValidations;
        let message = [];
        validations.forEach(validation => (validation.message ? (message += '- ' + validation.message + '\n') : ''));
        let levelError = '';
        let lvlFlag = 0;
        validations.forEach(validation => {
          if (validation.levelError === 'WARNING') {
            const wNum = 1;
            if (wNum > lvlFlag) {
              lvlFlag = wNum;
              levelError = 'WARNING';
            }
          } else if (validation.levelError === 'ERROR') {
            const eNum = 2;
            if (eNum > lvlFlag) {
              lvlFlag = eNum;
              levelError = 'ERROR';
            }
          } else if (validation.levelError === 'BLOCKER') {
            const bNum = 2;
            if (bNum > lvlFlag) {
              lvlFlag = bNum;
              levelError = 'BLOCKER';
            }
          }
        });

        return (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}>
            {' '}
            {field ? field.fieldData[column.field] : null} <IconTooltip levelError={levelError} message={message} />
          </div>
        );
      } else {
        return (
          <div style={{ display: 'flex', alignItems: 'center' }}>{field ? field.fieldData[column.field] : null}</div>
        );
      }
    };

    const editLargeStringWithDots = (string, length) => {
      if (string.length > length) {
        return string.substring(0, length).concat('...');
      } else {
        return string;
      }
    };

    const rowClassName = rowData => {
      let id = rowData.dataRow.filter(r => Object.keys(r.fieldData)[0] === 'id')[0].fieldData.id;

      return { 'p-highlight': id === selectedRecordErrorId };
    };

    const totalCount = <span>Total: {totalRecords} rows</span>;

    return (
      <div>
        <Toolbar>
          <div className="p-toolbar-group-left">
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={false}
              icon={'export'}
              label={resources.messages['import']}
              onClick={() => setImportDialogVisible(true)}
            />
            <Button
              id="buttonExportTable"
              className={`p-button-rounded p-button-secondary`}
              icon={loadingFile ? 'spinnerAnimate' : 'import'}
              label={resources.messages['exportTable']}
              onClick={event => exportMenuRef.current.show(event)}
            />
            <Menu
              model={exportButtonsList}
              popup={true}
              ref={exportMenuRef}
              id="exportTableMenu"
              onShow={e => {
                getExportButtonPosition(e.target);
              }}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={false}
              icon={'trash'}
              label={resources.messages['deleteTable']}
              onClick={() => onSetVisible(setDeleteDialogVisible, true)}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'eye'}
              label={resources.messages['visibility']}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'filter'}
              label={resources.messages['filter']}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'groupBy'}
              label={resources.messages['groupBy']}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'sort'}
              label={resources.messages['sort']}
            />
          </div>
          <div className="p-toolbar-group-right">
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'refresh'}
              label={resources.messages['refresh']}
              onClick={() => onRefresh()}
            />
          </div>
        </Toolbar>
        <div className={styles.Table}>
          <DataTable
            autoLayout={true}
            editable={true}
            first={firstRow}
            footer={addRowFooter}
            header={header}
            lazy={true}
            loading={loading}
            onPage={onChangePage}
            onPaste={() => console.log('Paste')}
            onRowSelect={e => onSelectRecord(e, e.data)}
            onSort={onSort}
            paginator={true}
            paginatorRight={totalCount}
            reorderableColumns={true}
            resizableColumns={true}
            rowClassName={rowClassName}
            rows={numRows}
            rowsPerPageOptions={[5, 10, 20, 100]}
            selectionMode="single"
            sortable={true}
            sortField={sortField}
            sortOrder={sortOrder}
            totalRecords={totalRecords}
            value={fetchedData}
            //scrollable={true}
            //frozenWidth="100px"
            // unfrozenWidth="600px"
          >
            {columns}
          </DataTable>
        </div>
        <Growl ref={growlRef} />
        <Dialog
          className={styles.Dialog}
          dismissableMask={false}
          header={resources.messages['uploadDataset']}
          onHide={onHide}
          visible={importDialogVisible}>
          <CustomFileUpload
            chooseLabel={resources.messages['selectFile']} //allowTypes="/(\.|\/)(csv|doc)$/"
            className={styles.FileUpload}
            fileLimit={1}
            mode="advanced"
            multiple={false}
            name="file"
            onUpload={onUpload}
            url={`${window.env.REACT_APP_BACKEND}${getUrl(config.loadDataTableAPI.url, {
              dataSetId: dataSetId,
              tableId: tableId
            })}`}
          />
        </Dialog>

        <ConfirmDialog
          header={resources.messages['deleteDatasetTableHeader']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          maximizable={false}
          onConfirm={onConfirmDelete}
          onHide={() => onSetVisible(setDeleteDialogVisible, false)}
          visible={deleteDialogVisible}>
          {resources.messages['deleteDatasetTableConfirm']}
        </ConfirmDialog>
        <ConfirmDialog
          onConfirm={onConfirmDeleteRow}
          onHide={onHideConfirmDeleteDialog}
          visible={confirmDeleteVisible}
          header={resources.messages['deleteRow']}
          maximizable={false}
          labelConfirm="Yes"
          labelCancel="No">
          {resources.messages['confirmDeleteRow']}
        </ConfirmDialog>
        <Dialog
          blockScroll={false}
          contentStyle={{ maxHeight: '80%', overflow: 'auto' }}
          footer={addRowDialogFooter}
          header={resources.messages['addNewRow']}
          maximizable={true}
          modal={true}
          onHide={() => setAddDialogVisible(false)}
          style={{ width: '50%', height: '80%', overflow: 'auto' }}
          visible={addDialogVisible}>
          <div className="p-grid p-fluid">{newRecordForm}</div>
        </Dialog>
        <Dialog
          blockScroll={false}
          contentStyle={{ maxHeight: '80%', overflow: 'auto' }}
          footer={editRowDialogFooter}
          header={resources.messages['editRow']}
          maximizable={true}
          modal={true}
          onHide={() => setEditDialogVisible(false)}
          style={{ width: '50%', height: '80%', overflow: 'auto' }}
          visible={editDialogVisible}>
          <div className="p-grid p-fluid">{editRecordForm}</div>
        </Dialog>
      </div>
    );
  }
);

export { DataViewer };
