/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef } from 'react';
import { withRouter } from 'react-router-dom';
import { isEmpty, isUndefined } from 'lodash';

import { DownloadFile } from 'ui/views/_components/DownloadFile';

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
import { Object } from 'es6-shim';

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
    const [initialCellValue, setInitialCellValue] = useState();
    const [initialRecordValue, setInitialRecordValue] = useState();
    const [isDataDeleted, setIsDataDeleted] = useState(false);
    const [isNewRecord, setIsNewRecord] = useState(false);
    const [isRecordDeleted, setIsRecordDeleted] = useState(false);
    const [loading, setLoading] = useState(false);
    const [loadingFile, setLoadingFile] = useState(false);
    const [newRecord, setNewRecord] = useState({});
    const [numRows, setNumRows] = useState(10);
    const [pastedRecords, setPastedRecords] = useState();
    const [selectedRecord, setSelectedRecord] = useState({});
    const [selectedCellId, setSelectedCellId] = useState();
    const [sortField, setSortField] = useState(undefined);
    const [sortOrder, setSortOrder] = useState(undefined);
    const [totalRecords, setTotalRecords] = useState(0);

    const resources = useContext(ResourcesContext);

    let growlRef = useRef();
    let exportMenuRef = useRef();
    let datatableRef = useRef();

    useEffect(() => {
      //document.addEventListener('paste', event => onPaste(event));

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
      setConfirmDeleteVisible(false);
    }, [isRecordDeleted]);

    useEffect(() => {
      setIsRecordDeleted(false);
    }, [confirmDeleteVisible]);

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
      columnsArr.unshift(editCol, validationCol);
      setColumns(columnsArr);
    }, [colsSchema, columnOptions, selectedRecord, editedRecord, initialCellValue]);

    useEffect(() => {
      if (!isUndefined(exportTableData)) {
        DownloadFile(exportTableData, exportTableDataName);
      }
    }, [exportTableData]);

    const onCancelRowEdit = () => {
      let updatedValue = changeRecordInTable(fetchedData, getRecordId(fetchedData, selectedRecord));
      setEditDialogVisible(false);
      setFetchedData(updatedValue);
    };

    const onChangePage = event => {
      setNumRows(event.rows);
      setFirstRow(event.first);
      onFetchData(sortField, sortOrder, event.first, event.rows);
    };

    const onConfirmDeleteTable = async () => {
      setDeleteDialogVisible(false);
      const dataDeleted = await DataSetService.deleteTableDataById(dataSetId, tableId);
      if (dataDeleted) {
        setIsDataDeleted(true);
      }
    };

    const onConfirmDeleteRow = async () => {
      setDeleteDialogVisible(false);
      const recordDeleted = await DataSetService.deleteRecordById(dataSetId, selectedRecord.recordId);
      if (recordDeleted) {
        setIsRecordDeleted(true);
      }
    };

    const onEditAddFormInput = (property, value) => {
      let record = {};
      if (!isNewRecord) {
        record = Object.create(editedRecord);
        let updatedRecord = changeRecordValue(record, property, value);
        setEditedRecord(updatedRecord);
      } else {
        record = { ...newRecord };
        let updatedRecord = changeRecordValue(record, property, value);
        setNewRecord(updatedRecord);
      }
    };

    //When pressing "Escape" cell data resets to initial value
    const onEditorEscapeChange = (props, event) => {
      if (event.key === 'Escape') {
        let updatedData = changeCellValue([...props.value], props.rowIndex, props.field, initialCellValue);
        datatableRef.current.closeEditingCell();
        setFetchedData(updatedData);
      }
    };

    const onEditorSubmitValue = async (cell, value, record) => {
      if (!isEmpty(record)) {
        let field = record.dataRow.filter(row => Object.keys(row.fieldData)[0] === cell.field)[0].fieldData;
        if (value !== initialCellValue && selectedCellId === getCellId(cell, cell.field)) {
          const fieldUpdated = await DataSetService.updateFieldById(dataSetId, cell.field, field.id, field.type, value);
          if (!fieldUpdated) {
            console.error('Error!');
          }
        }
      }
    };

    const onEditorValueChange = (props, value) => {
      let updatedData = changeCellValue([...props.value], props.rowIndex, props.field, value);
      setFetchedData(updatedData);
    };

    const onEditorValueFocus = (props, value) => {
      setSelectedCellId(getCellId(props, props.field));
      setInitialCellValue(value);
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
          setNewRecord(createEmptyObject(colsSchema, tableData));
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

    const onPaste = async () => {
      const pastedData = await navigator.clipboard.readText();
      //event.clipboardData || window.clipboardData;
      //let pastedData = clipboardData.getData('Text');
      const copiedClipboardRecords = pastedData.split('\n').filter(l => l.length > 0);
      const copiedRecords = [];
      copiedClipboardRecords.forEach(row => {
        let emptyRecord = createEmptyObject(colsSchema, fetchedData);
        const copiedCols = row.split('\t');
        //copiedCols.unshift(Math.floor(Math.random() * (999999 - 500) + 500));

        emptyRecord.dataRow.forEach((record, i) => {
          emptyRecord = changeRecordValue(emptyRecord, record.fieldData.fieldSchemaId, copiedCols[i]);
        });

        copiedRecords.push(emptyRecord);
      });
      console.log(copiedRecords);
      setPastedRecords(copiedRecords);
    };
    const onPasteAccept = async () => {
      try {
        console.log(pastedRecords);
        await DataSetService.addRecordsById(dataSetId, tableId, pastedRecords);
        growlRef.current.show({
          severity: 'success',
          summary: resources.messages['dataPasted'],
          life: '3000'
        });
      } catch (error) {
        console.error('DataViewer error: ', error);
        const errorResponse = error.response;
        console.error('DataViewer errorResponse: ', errorResponse);
        if (!isUndefined(errorResponse) && (errorResponse.status === 401 || errorResponse.status === 403)) {
          history.push(getUrl(config.REPORTING_DATAFLOW.url, { dataFlowId }));
        }
      } finally {
      }
    };
    const onRefresh = () => {
      onFetchData(sortField, sortOrder, firstRow, numRows);
    };

    const onSelectRecord = val => {
      setIsNewRecord(false);
      setSelectedRecord({ ...val });
      setInitialRecordValue(getInitialRecordValues(val));
      setEditedRecord({ ...val });
    };

    const onSaveRecord = async record => {
      if (isNewRecord) {
        try {
          await DataSetService.addRecordsById(dataSetId, tableId, [record]);
          setAddDialogVisible(false);
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
        try {
          await DataSetService.updateRecordById(dataSetId, tableId, record);
          setEditDialogVisible(false);
        } catch (error) {
          console.error('DataViewer error: ', error);
          const errorResponse = error.response;
          console.error('DataViewer errorResponse: ', errorResponse);
          if (!isUndefined(errorResponse) && (errorResponse.status === 401 || errorResponse.status === 403)) {
            history.push(getUrl(config.REPORTING_DATAFLOW.url, { dataFlowId }));
          }
        } finally {
          onCancelRowEdit();
          setLoading(false);
        }
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
          {resources.messages['dataSetLoadingMessage']}
          <strong>{editLargeStringWithDots(tableName, 22)}</strong>
          {resources.messages['dataSetLoading']}
        </span>
      );

      growlRef.current.show({
        severity: 'info',
        summary: resources.messages['dataSetLoadingTitle'],
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
          }}
        />
        <Button
          label={resources.messages['save']}
          icon="save"
          onClick={e => {
            onSaveRecord(newRecord);
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
        <Button
          style={{ float: 'right' }}
          label={resources.messages['paste']}
          icon="clipboard"
          onClick={() => {
            datatableRef.current.onPaste();
          }}
        />
      </div>
    );

    const cellDataEditor = (cells, record) => {
      return (
        <InputText
          type="text"
          value={getCellValue(cells, cells.field)}
          onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
          onChange={e => onEditorValueChange(cells, e.target.value)}
          onFocus={e => onEditorValueFocus(cells, e.target.value)}
          onKeyDown={e => onEditorEscapeChange(cells, e)}
        />
      );
    };

    const changeRecordInTable = (tableData, rowIndex) => {
      let record = tableData[rowIndex];

      for (let i = 0; i < initialRecordValue.length; i++) {
        record = changeRecordValue(record, initialRecordValue[i][0], initialRecordValue[i][1]);
      }

      tableData[rowIndex] = record;
      return tableData;
    };

    const changeCellValue = (tableData, rowIndex, field, value) => {
      tableData[rowIndex].dataRow.filter(data => Object.keys(data.fieldData)[0] === field)[0].fieldData[field] = value;
      return tableData;
    };

    const changeRecordValue = (recordData, field, value) => {
      recordData.dataRow.filter(data => Object.keys(data.fieldData)[0] === field)[0].fieldData[field] = value;
      return recordData;
    };

    const createEmptyObject = (columnsSchema, data) => {
      let fields;
      if (!isUndefined(columnsSchema)) {
        fields = columnsSchema.map(column => {
          return {
            fieldData: { [column.field]: null, type: column.type, fieldSchemaId: column.field }
          };
        });
      }

      const obj = {
        dataRow: fields,
        recordSchemaId: columnsSchema[0].recordId
      };

      //dataSetPartitionId is needed for checking the rows owned by delegated contributors
      if (!isUndefined(data.records) && data.records.length > 0) {
        obj.dataSetPartitionId = data.records[0].dataSetPartitionId;
      } else {
        obj.dataSetPartitionId = null;
      }
      return obj;
    };

    const createTableName = (tableName, fileType) => {
      return `${tableName}.${fileType}`;
    };

    const editRowDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button label={resources.messages['cancel']} icon="cancel" onClick={onCancelRowEdit} />
        <Button
          label={resources.messages['save']}
          icon="save"
          onClick={() => {
            try {
              onSaveRecord(editedRecord);
            } catch (error) {
              console.error(error);
            }
          }}
        />
      </div>
    );

    const editRecordForm = colsSchema.map((column, i) => {
      const arr = [];
      //Avoid row id Field
      if (editDialogVisible) {
        if (i < colsSchema.length - 1) {
          if (!isUndefined(editedRecord.dataRow)) {
            const field = editedRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
            arr.push([column.field, field.fieldData[column.field]]);
            return (
              <React.Fragment key={column.field}>
                <div className="p-col-4" style={{ padding: '.75em' }}>
                  <label htmlFor={column.field}>{column.header}</label>
                </div>
                <div className="p-col-8" style={{ padding: '.5em' }}>
                  <InputText
                    id={column.field}
                    value={field.fieldData[column.field]}
                    onChange={e => onEditAddFormInput(column.field, e.target.value)}
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
        const recordId = record.recordId;
        const recordSchemaId = record.recordSchemaId;
        const arrayDataFields = record.fields.map(field => {
          return {
            fieldData: {
              [field.fieldSchemaId]: field.value,
              type: field.type,
              id: field.fieldId,
              fieldSchemaId: field.fieldSchemaId
            },
            fieldValidations: field.validations
          };
        });
        arrayDataFields.push({ fieldData: { id: record.recordId }, fieldValidations: null });
        const arrayDataAndValidations = {
          dataRow: arrayDataFields,
          recordValidations,
          recordId,
          recordSchemaId
        };
        return arrayDataAndValidations;
      });
      setFetchedData(dataFiltered);
    };

    const getCellId = (tableData, field) => {
      const value = tableData.rowData.dataRow.filter(data => data.fieldData[field]);
      return value.length > 0 ? value[0].fieldData.id : undefined;
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

    const getInitialRecordValues = record => {
      const arr = [];
      colsSchema.map((column, i) => {
        if (i < colsSchema.length - 1) {
          if (!isUndefined(record.dataRow)) {
            const field = record.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
            arr.push([column.field, field.fieldData[column.field]]);
          }
        }
      });
      return arr;
    };

    const getRecordId = (tableData, record) => {
      return tableData
        .map(e => {
          return e.recordId;
        })
        .indexOf(record.recordId);
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
            columnsPreviewNumber={5}
            editable={true}
            //emptyMessage={resources.messages['noDataInDataTable']}
            id={tableId}
            first={firstRow}
            footer={addRowFooter}
            header={header}
            lazy={true}
            loading={loading}
            onPage={onChangePage}
            onPaste={onPaste}
            onPasteAccept={onPasteAccept}
            onRowSelect={e => onSelectRecord(Object.assign({}, e.data))}
            onSort={onSort}
            paginator={true}
            paginatorRight={totalCount}
            pastedRecords={pastedRecords}
            recordsPreviewNumber={5}
            ref={datatableRef}
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
          onConfirm={onConfirmDeleteTable}
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
