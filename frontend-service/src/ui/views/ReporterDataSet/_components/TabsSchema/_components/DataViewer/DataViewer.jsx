/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef } from 'react';
import { withRouter } from 'react-router-dom';
import { isEmpty, isUndefined, isNull, isString, differenceBy } from 'lodash';

import { DownloadFile } from 'ui/views/_components/DownloadFile';

import { DatasetConfig } from 'conf/domain/model/DataSet';
import { config } from 'conf';

import styles from './DataViewer.module.css';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { ContextMenu } from 'ui/views/_components/ContextMenu';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { DropdownFilter } from 'ui/views/ReporterDataSet/_components/DropdownFilter';
import { IconTooltip } from './_components/IconTooltip';
import { InfoTable } from './_components/InfoTable';
import { InputText } from 'ui/views/_components/InputText';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { Growl } from 'primereact/growl';
import { Menu } from 'primereact/menu';
import { ReporterDatasetContext } from 'ui/views/ReporterDataSet/_components/_context/ReporterDataSetContext';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { SnapshotContext } from 'ui/views/_components/_context/SnapshotContext';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { getUrl } from 'core/infrastructure/api/getUrl';
import { DatasetService } from 'core/services/DataSet';
import { Object } from 'es6-shim';
import { routes } from 'ui/routes';

const DataViewer = withRouter(
  ({
    hasWritePermissions,
    isWebFormMMR,
    buttonsList = undefined,
    recordPositionId,
    selectedRecordErrorId,
    tableHasErrors,
    tableId,
    tableName,
    tableSchemaColumns,
    match: {
      params: { datasetId, dataflowId }
    },
    history
  }) => {
    const [addDialogVisible, setAddDialogVisible] = useState(false);
    const [columnOptions, setColumnOptions] = useState([{}]);
    const [colsSchema, setColsSchema] = useState(tableSchemaColumns);
    const [columns, setColumns] = useState([]);
    const [confirmDeleteVisible, setConfirmDeleteVisible] = useState(false);
    const [confirmPasteVisible, setConfirmPasteVisible] = useState(false);
    const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
    const [editedRecord, setEditedRecord] = useState({});
    const [editDialogVisible, setEditDialogVisible] = useState(false);
    const [exportButtonsList, setExportButtonsList] = useState([]);
    const [exportTableData, setExportTableData] = useState(undefined);
    const [exportTableDataName, setExportTableDataName] = useState('');
    const [fetchedData, setFetchedData] = useState([]);
    const [fetchedDataFirstRow, setFetchedDataFirstRow] = useState([]);
    const [filterLevelError, setFilterLevelError] = useState(['CORRECT', 'WARNING', 'ERROR']);
    const [firstRow, setFirstRow] = useState(0);
    const [header] = useState();
    const [importDialogVisible, setImportDialogVisible] = useState(false);
    const [initialCellValue, setInitialCellValue] = useState();
    const [initialRecordValue, setInitialRecordValue] = useState();
    const [isFilterValidationsActive, setIsFilterValidationsActive] = useState(false);
    const [isNewRecord, setIsNewRecord] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [isLoadingFile, setIsLoadingFile] = useState(false);
    const [isRecordDeleted, setIsRecordDeleted] = useState(false);
    const [menu, setMenu] = useState();
    const [newRecord, setNewRecord] = useState({});
    const [numCopiedRecords, setNumCopiedRecords] = useState();
    const [numRows, setNumRows] = useState(10);
    const [originalColumns, setOriginalColumns] = useState([]);
    const [pastedRecords, setPastedRecords] = useState();
    const [selectedRecord, setSelectedRecord] = useState({});
    //const [selectedRecords, setSelectedRecords] = useState([]);
    const [selectedCellId, setSelectedCellId] = useState();
    const [sortField, setSortField] = useState(undefined);
    const [sortOrder, setSortOrder] = useState(undefined);
    const [totalRecords, setTotalRecords] = useState(0);
    const [totalFilteredRecords, setTotalFilteredRecords] = useState();
    const [validationDropdownFilter, setValidationDropdownFilter] = useState([]);
    const [visibilityColumnIcon, setVisibleColumnIcon] = useState('eye');
    const [visibilityDropdownFilter, setVisibilityDropdownFilter] = useState([]);
    const [visibleColumns, setVisibleColumns] = useState([]);

    const contextReporterDataset = useContext(ReporterDatasetContext);
    const resources = useContext(ResourcesContext);
    const snapshotContext = useContext(SnapshotContext);

    let contextMenuRef = useRef();
    let datatableRef = useRef();
    let exportMenuRef = useRef();
    let divRef = useRef();
    let dropdownFilterRef = useRef();
    let filterMenuRef = useRef();
    let growlRef = useRef();

    useEffect(() => {
      if (contextReporterDataset.isValidationSelected) {
        setFilterLevelError(['ERROR', 'WARNING', 'CORRECT']);

        setValidationDropdownFilter([
          { label: resources.messages['error'], key: 'ERROR' },
          { label: resources.messages['warning'], key: 'WARNING' },
          { label: resources.messages['correct'], key: 'CORRECT' }
        ]);
        setIsFilterValidationsActive(false);
        contextReporterDataset.setIsValidationSelected(false);
      }
    }, [contextReporterDataset.isValidationSelected]);

    useEffect(() => {
      setExportButtonsList(
        config.exportTypes.map(type => ({
          label: type.text,
          icon: config.icons['archive'],
          command: () => onExportTableData(type.code)
        }))
      );

      let colOptions = [];
      let dropdownFilter = [];
      for (let colSchema of colsSchema) {
        colOptions.push({ label: colSchema.header, value: colSchema });
        dropdownFilter.push({ label: colSchema.header, key: colSchema.field });
      }
      setColumnOptions(colOptions);
      setVisibilityDropdownFilter(dropdownFilter);

      setValidationDropdownFilter([
        { label: resources.messages['error'], key: 'ERROR' },
        { label: resources.messages['warning'], key: 'WARNING' },
        { label: resources.messages['correct'], key: 'CORRECT' }
      ]);

      const inmTableSchemaColumns = [...tableSchemaColumns];
      inmTableSchemaColumns.push({ table: inmTableSchemaColumns[0].table, field: 'id', header: '' });
      inmTableSchemaColumns.push({ table: inmTableSchemaColumns[0].table, field: 'datasetPartitionId', header: '' });
      setColsSchema(inmTableSchemaColumns);
      onFetchData(undefined, undefined, 0, numRows, filterLevelError);
    }, []);

    useEffect(() => {
      setMenu([
        {
          label: resources.messages['edit'],
          icon: config.icons['edit'],
          command: () => {
            setEditDialogVisible(true);
          }
        },
        {
          label: resources.messages['delete'],
          icon: config.icons['trash'],
          command: () => setConfirmDeleteVisible(true)
        }
      ]);
    }, [selectedRecord]);

    useEffect(() => {
      if (isRecordDeleted) {
        onRefresh();
        setConfirmDeleteVisible(false);
      }
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
      onFetchData(undefined, undefined, Math.floor(recordPositionId / numRows) * numRows, numRows, filterLevelError);
    }, [recordPositionId]);

    useEffect(() => {
      let columnsArr = colsSchema.map(column => {
        let sort = column.field === 'id' || column.field === 'datasetPartitionId' ? false : true;
        let visibleColumn = column.field === 'id' || column.field === 'datasetPartitionId' ? styles.VisibleHeader : '';
        return (
          <Column
            body={dataTemplate}
            className={visibleColumn}
            editor={hasWritePermissions && !isWebFormMMR ? row => cellDataEditor(row, selectedRecord) : null}
            //editorValidator={requiredValidator}
            field={column.field}
            header={column.header}
            key={column.field}
            sortable={sort}
          />
        );
      });
      let editCol = (
        <Column
          className={styles.validationCol}
          header={resources.messages['actions']}
          key="actions"
          body={row => actionTemplate(row)}
          sortable={false}
          style={{ width: '100px', height: '45px' }}
        />
      );

      let validationCol = (
        <Column body={validationsTemplate} field="validations" header="" key="recordValidation" sortable={false} />
      );

      if (!isWebFormMMR) {
        hasWritePermissions ? columnsArr.unshift(editCol, validationCol) : columnsArr.unshift(validationCol);
      }

      if (visibleColumns.length > 0 && columnsArr.length !== visibleColumns.length) {
        const visibleKeys = visibleColumns.map(column => {
          return column.key;
        });
        setColumns(columnsArr.filter(column => visibleKeys.includes(column.key)));
      } else {
        setColumns(columnsArr);
        setOriginalColumns(columnsArr);
      }
    }, [colsSchema, columnOptions, selectedRecord, editedRecord, initialCellValue]);

    const showFilters = columnKeys => {
      const mustShowColumns = ['actions', 'recordValidation', 'id', 'datasetPartitionId'];
      const currentVisibleColumns = originalColumns.filter(
        column => columnKeys.includes(column.key) || mustShowColumns.includes(column.key)
      );
      setColumns(currentVisibleColumns);
      setVisibleColumns(currentVisibleColumns);

      if (isFiltered(originalColumns, currentVisibleColumns)) {
        setVisibleColumnIcon('eye-slash');
      } else {
        setVisibleColumnIcon('eye');
      }
    };

    const showValidationFilter = filteredKeys => {
      setIsFilterValidationsActive(filteredKeys.length !== 3);
      setFirstRow(0);
      setFilterLevelError(filteredKeys);
    };

    useEffect(() => {
      onFetchData(sortField, sortOrder, 0, numRows, filterLevelError);
    }, [filterLevelError]);

    useEffect(() => {
      if (!isUndefined(exportTableData)) {
        DownloadFile(exportTableData, exportTableDataName);
      }
    }, [exportTableData]);

    useEffect(() => {
      if (confirmPasteVisible) {
        if (!isUndefined(pastedRecords)) {
          if (pastedRecords.length > 0) {
            setPastedRecords([]);
          }
        }
        // if (confirmPasteVisible) {
        //   divRef.current.focus();
        // }
      }
    }, [confirmPasteVisible]);

    const isFiltered = (originalFilter, filter) => {
      if (filter.length < originalFilter.length) {
        return true;
      } else {
        return false;
      }
    };

    const onCancelRowEdit = () => {
      let updatedValue = changeRecordInTable(fetchedData, getRecordId(fetchedData, selectedRecord));
      setEditDialogVisible(false);
      setFetchedData(updatedValue);
    };

    const onChangePage = event => {
      setNumRows(event.rows);
      setFirstRow(event.first);
      onFetchData(sortField, sortOrder, event.first, event.rows, filterLevelError);
    };

    const onConfirmDeleteTable = async () => {
      setDeleteDialogVisible(false);
      const dataDeleted = await DatasetService.deleteTableDataById(datasetId, tableId);
      if (dataDeleted) {
        setFetchedData([]);
        setTotalRecords(0);
        setTotalFilteredRecords(0);
        //  snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
      }
    };

    const onConfirmDeleteRow = async () => {
      setDeleteDialogVisible(false);
      const recordDeleted = await DatasetService.deleteRecordById(datasetId, selectedRecord.recordId);
      if (recordDeleted) {
        //  snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
        setIsRecordDeleted(true);
      }
    };

    const onDeletePastedRecord = recordIndex => {
      const inmPastedRecords = [...pastedRecords];
      inmPastedRecords.splice(getRecordIdByIndex(inmPastedRecords, recordIndex), 1);
      setPastedRecords(inmPastedRecords);
    };

    const onEditAddFormInput = (property, value) => {
      let record = {};
      if (!isNewRecord) {
        record = { ...editedRecord };
        let updatedRecord = changeRecordValue(record, property, value);
        setEditedRecord(updatedRecord);
      } else {
        record = { ...newRecord };
        let updatedRecord = changeRecordValue(record, property, value);
        setNewRecord(updatedRecord);
      }
    };

    //When pressing "Escape" cell data resets to initial value
    //on "Enter" and "Tab" the value submits
    const onEditorKeyChange = (props, event, record) => {
      if (event.key === 'Escape') {
        let updatedData = changeCellValue([...props.value], props.rowIndex, props.field, initialCellValue);
        datatableRef.current.closeEditingCell();
        setFetchedData(updatedData);
      } else if (event.key === 'Enter') {
        onEditorSubmitValue(props, event.target.value, record);
      } else if (event.key === 'Tab') {
        event.preventDefault();
      }
    };

    const onEditorSubmitValue = async (cell, value, record) => {
      if (!isEmpty(record)) {
        let field = record.dataRow.filter(row => Object.keys(row.fieldData)[0] === cell.field)[0].fieldData;
        if (
          value !== initialCellValue &&
          selectedCellId === getCellId(cell, cell.field) &&
          record.recordId === selectedRecord.recordId
        ) {
          //without await. We don't have to wait for the response.
          const fieldUpdated = DatasetService.updateFieldById(datasetId, cell.field, field.id, field.type, value);
          if (!fieldUpdated) {
            console.error('Error!');
          }
          // snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
        }
      }
    };

    const onEditorValueChange = (props, value) => {
      const updatedData = changeCellValue([...props.value], props.rowIndex, props.field, value);
      setFetchedData(updatedData);
    };

    const onEditorValueFocus = (props, value) => {
      setSelectedCellId(getCellId(props, props.field));
      setInitialCellValue(value);
    };

    const onExportTableData = async fileType => {
      setIsLoadingFile(true);
      try {
        setExportTableDataName(createTableName(tableName, fileType));
        setExportTableData(await DatasetService.exportTableDataById(datasetId, tableId, fileType));
      } catch (error) {
        console.error(error);
      } finally {
        setIsLoadingFile(false);
      }
    };

    const onFetchData = async (sField, sOrder, fRow, nRows, filterLevelError) => {
      setIsLoading(true);
      try {
        let fields;

        if (!isUndefined(sField) && sField !== null) {
          fields = `${sField}:${sOrder}`;
        }

        const tableData = await DatasetService.tableDataById(
          datasetId,
          tableId,
          Math.floor(fRow / nRows),
          nRows,
          fields,
          filterLevelError
        );

        if (!isUndefined(colsSchema)) {
          if (!isUndefined(tableData)) {
            if (!isUndefined(tableData.records)) {
              if (tableData.records.length > 0) {
                setNewRecord(createEmptyObject(colsSchema, tableData.records[0]));
              }
            } else {
              setNewRecord(createEmptyObject(colsSchema, undefined));
            }
          }
        }
        if (!isUndefined(tableData.records)) {
          filterDataResponse(tableData);
        } else {
          setFetchedData([]);
        }

        if (tableData.totalRecords !== totalRecords) {
          setTotalRecords(tableData.totalRecords);
        }

        if (tableData.totalFilteredRecords !== totalFilteredRecords) {
          setTotalFilteredRecords(tableData.totalFilteredRecords);
        }

        setIsLoading(false);
      } catch (error) {
        console.error('DataViewer error: ', error);
        const errorResponse = error.response;
        console.error('DataViewer errorResponse: ', errorResponse);
        if (!isUndefined(errorResponse) && (errorResponse.status === 401 || errorResponse.status === 403)) {
          history.push(getUrl(routes.DATAFLOW, { dataflowId }));
        }
      } finally {
        setIsLoading(false);
      }
    };

    const onHide = () => {
      setImportDialogVisible(false);
    };

    const onHideConfirmDeleteDialog = () => {
      setConfirmDeleteVisible(false);
    };

    const onPaste = event => {
      if (event) {
        const clipboardData = event.clipboardData;
        const pastedData = clipboardData.getData('Text');
        setPastedRecords(getClipboardData(pastedData));
      }
    };

    const onPasteAsync = async () => {
      const pastedData = await navigator.clipboard.readText();
      setPastedRecords(getClipboardData(pastedData));
    };

    const onPasteAccept = async () => {
      try {
        const recordsAdded = await DatasetService.addRecordsById(datasetId, tableId, pastedRecords);
        if (recordsAdded) {
          growlRef.current.show({
            severity: 'success',
            summary: resources.messages['dataPasted'],
            life: '3000'
          });
          onRefresh();
          // snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
        } else {
          growlRef.current.show({
            severity: 'error',
            summary: resources.messages['dataPastedError'],
            life: '3000'
          });
        }
      } catch (error) {
        console.error('DataViewer error: ', error);
        const errorResponse = error.response;
        console.error('DataViewer errorResponse: ', errorResponse);
        if (!isUndefined(errorResponse) && (errorResponse.status === 401 || errorResponse.status === 403)) {
          history.push(getUrl(routes.DATAFLOW, { dataflowId }));
        }
      } finally {
        setConfirmPasteVisible(false);
      }
    };

    const onRefresh = () => {
      onFetchData(sortField, sortOrder, firstRow, numRows, filterLevelError);
    };

    const onPasteCancel = () => {
      setPastedRecords([]);
      setConfirmPasteVisible(false);
    };

    const onSelectRecord = val => {
      setIsNewRecord(false);
      setSelectedRecord({ ...val });
      setEditedRecord({ ...val });
      setInitialRecordValue(getInitialRecordValues(val));
    };

    const onSaveRecord = async record => {
      //Delete hidden column null values (datasetPartitionId and id)
      record.dataRow = record.dataRow.filter(
        field => Object.keys(field.fieldData)[0] !== 'datasetPartitionId' && Object.keys(field.fieldData)[0] !== 'id'
      );
      if (isNewRecord) {
        try {
          await DatasetService.addRecordsById(datasetId, tableId, [record]);
          setAddDialogVisible(false);
          // snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
          onRefresh();
        } catch (error) {
          console.error('DataViewer error: ', error);
          const errorResponse = error.response;
          console.error('DataViewer errorResponse: ', errorResponse);
          if (!isUndefined(errorResponse) && (errorResponse.status === 401 || errorResponse.status === 403)) {
            history.push(getUrl(routes.DATAFLOW, { dataflowId }));
          }
        } finally {
          setIsLoading(false);
        }
      } else {
        try {
          await DatasetService.updateRecordsById(datasetId, record);
          onRefresh();
          setEditDialogVisible(false);
          // snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
        } catch (error) {
          console.error('DataViewer error: ', error);
          const errorResponse = error.response;
          console.error('DataViewer errorResponse: ', errorResponse);
          if (!isUndefined(errorResponse) && (errorResponse.status === 401 || errorResponse.status === 403)) {
            history.push(getUrl(routes.DATAFLOW, { dataflowId }));
          }
        } finally {
          onCancelRowEdit();
          setIsLoading(false);
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
      onFetchData(event.sortField, event.sortOrder, 0, numRows, filterLevelError);
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
          label={resources.messages['save']}
          icon="save"
          onClick={() => {
            onSaveRecord(newRecord);
          }}
        />
        <Button
          label={resources.messages['cancel']}
          icon="cancel"
          onClick={() => {
            setAddDialogVisible(false);
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
          disabled={!hasWritePermissions}
          onClick={() => {
            setIsNewRecord(true);
            setAddDialogVisible(true);
          }}
        />
        <Button
          style={{ float: 'right' }}
          label={resources.messages['pasteRecords']}
          icon="clipboard"
          onClick={async () => {
            setConfirmPasteVisible(true);
          }}
        />
      </div>
    );

    const capitalizeFirstLetterAndToLowerCase = string => {
      return (
        string
          .trim()
          .charAt(0)
          .toUpperCase() +
        string
          .trim()
          .slice(1)
          .toLowerCase()
      );
    };

    const cellDataEditor = (cells, record) => {
      return (
        <InputText
          type="text"
          value={getCellValue(cells, cells.field)}
          onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
          onChange={e => onEditorValueChange(cells, e.target.value)}
          onFocus={e => {
            e.preventDefault();
            onEditorValueFocus(cells, e.target.value);
          }}
          onKeyDown={e => onEditorKeyChange(cells, e, record)}
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
      //Delete \r and \n values for tabular paste
      if (!isUndefined(value) && !isNull(value) && isString(value)) {
        value = value.replace(`\r`, '').replace(`\n`, '');
      }
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

      obj.datasetPartitionId = null;
      //dataSetPartitionId is needed for checking the rows owned by delegated contributors
      if (!isUndefined(data) && data.length > 0) {
        obj.datasetPartitionId = data.datasetPartitionId;
      }
      return obj;
    };

    const createTableName = (tableName, fileType) => {
      return `${tableName}.${fileType}`;
    };

    //Template for Field validation
    const dataTemplate = (rowData, column) => {
      let field = rowData.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
      if (field !== null && field && field.fieldValidations !== null && !isUndefined(field.fieldValidations)) {
        const validations = [...field.fieldValidations];
        let message = [];
        validations.forEach(validation =>
          validation.message ? (message += '- ' + capitalizeFirstLetterAndToLowerCase(validation.message) + '\n') : ''
        );
        const levelError = getLevelError(validations);

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

    const editRecordForm = colsSchema.map((column, i) => {
      //Avoid row id Field and dataSetPartitionId
      if (editDialogVisible) {
        if (i < colsSchema.length - 2) {
          if (!isUndefined(editedRecord.dataRow)) {
            const field = editedRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
            return (
              <React.Fragment key={column.field}>
                <div className="p-col-4" style={{ padding: '.75em' }}>
                  <label htmlFor={column.field}>{column.header}</label>
                </div>
                <div className="p-col-8" style={{ padding: '.5em' }}>
                  <InputText
                    id={column.field}
                    value={
                      isNull(field.fieldData[column.field]) || isUndefined(field.fieldData[column.field])
                        ? ''
                        : field.fieldData[column.field]
                    }
                    onChange={e => onEditAddFormInput(column.field, e.target.value)}
                  />
                </div>
              </React.Fragment>
            );
          }
        }
      }
    });

    const editRowDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
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
        <Button label={resources.messages['cancel']} icon="cancel" onClick={onCancelRowEdit} />
      </div>
    );

    const filterDataResponse = data => {
      const dataFiltered = data.records.map(record => {
        const datasetPartitionId = record.datasetPartitionId;
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
        arrayDataFields.push({ fieldData: { datasetPartitionId: record.datasetPartitionId }, fieldValidations: null });
        const arrayDataAndValidations = {
          dataRow: arrayDataFields,
          recordValidations,
          recordId,
          datasetPartitionId,
          recordSchemaId
        };
        return arrayDataAndValidations;
      });
      if (dataFiltered.length > 0) {
        setFetchedDataFirstRow(dataFiltered[0]);
      } else {
        setFetchedData([]);
      }
      setFetchedData(dataFiltered);
    };

    const getLevelError = validations => {
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
      return levelError;
    };

    const getCellId = (tableData, field) => {
      const completeField = tableData.rowData.dataRow.filter(data => Object.keys(data.fieldData)[0] === field)[0];
      return !isUndefined(completeField) ? completeField.fieldData.id : undefined;
    };

    const getCellValue = (tableData, field) => {
      const value = tableData.rowData.dataRow.filter(data => data.fieldData[field]);
      return value.length > 0 ? value[0].fieldData[field] : '';
    };

    const getClipboardData = pastedData => {
      //Delete double quotes from strings
      const copiedClipboardRecords = pastedData
        .split('\r\n')
        .filter(l => l.length > 0)
        .map(d => d.replace(/["]+/g, '').replace('\n', ' '));
      //Maximum number of records to paste should be 500
      setNumCopiedRecords(copiedClipboardRecords.length);
      const copiedBulkRecords = !isUndefined(pastedRecords) ? [...pastedRecords].slice(0, 500) : [];
      copiedClipboardRecords.forEach(row => {
        let emptyRecord = createEmptyObject(colsSchema, fetchedDataFirstRow);
        const copiedCols = row.split('\t');
        emptyRecord.dataRow.forEach((record, i) => {
          emptyRecord = changeRecordValue(emptyRecord, record.fieldData.fieldSchemaId, copiedCols[i]);
        });

        emptyRecord.dataRow = emptyRecord.dataRow.filter(
          column =>
            Object.keys(column.fieldData)[0] !== 'id' && Object.keys(column.fieldData)[0] !== 'datasetPartitionId'
        );
        emptyRecord.copiedCols = copiedCols.length;
        copiedBulkRecords.push(emptyRecord);
      });
      //Slice to 500 records and renumber de records for delete button
      return copiedBulkRecords.slice(0, 500).map((record, i) => {
        return { ...record, recordId: i };
      });
    };

    const getExportButtonPosition = e => {
      const exportButton = e.currentTarget;
      const left = `${exportButton.offsetLeft}px`;
      const topValue = exportButton.offsetHeight + exportButton.offsetTop + 3;
      const top = `${topValue}px `;
      const menu = exportButton.nextElementSibling;
      menu.style.top = top;
      menu.style.left = left;
    };

    const getInitialRecordValues = record => {
      const initialValues = [];
      const filteredColumns = colsSchema.filter(
        column =>
          column.key !== 'actions' &&
          column.key !== 'recordValidation' &&
          column.key !== 'id' &&
          column.key !== 'datasetPartitionId'
      );
      filteredColumns.map(column => {
        if (!isUndefined(record.dataRow)) {
          const field = record.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
          initialValues.push([column.field, field.fieldData[column.field]]);
        }
      });
      return initialValues;
    };

    const getRecordId = (tableData, record) => {
      return tableData
        .map(e => {
          return e.recordId;
        })
        .indexOf(record.recordId);
    };

    const getRecordIdByIndex = (tableData, recordIdx) => {
      return tableData
        .map(e => {
          return e.recordId;
        })
        .indexOf(recordIdx);
    };

    const newRecordForm = colsSchema.map((column, i) => {
      if (addDialogVisible) {
        if (i < colsSchema.length - 2) {
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
    const validationsTemplate = recordData => {
      let validations = [];
      if (recordData.recordValidations && !isUndefined(recordData.recordValidations)) {
        validations = [...recordData.recordValidations];
      }

      let messageErrors = '';
      let messageWarnings = '';
      let hasFieldErrors = false;
      const recordsWithFieldValidations = recordData.dataRow.filter(
        row => !isUndefined(row.fieldValidations) && !isNull(row.fieldValidations)
      );
      hasFieldErrors = recordsWithFieldValidations.length > 0;

      const filteredFieldValidations = recordsWithFieldValidations.map(record => record.fieldValidations).flat();

      if (hasFieldErrors) {
        const filteredFieldValidationsWithError = filteredFieldValidations.filter(
          filteredFieldValidation => filteredFieldValidation.levelError === 'ERROR'
        );
        //There are warnings in fields
        if (filteredFieldValidations.length - filteredFieldValidationsWithError.length > 0) {
          validations.push(
            DatasetService.createValidation('RECORD', 0, 'WARNING', resources.messages['recordWarnings'])
          );
        } else {
          validations.push(DatasetService.createValidation('RECORD', 0, 'ERROR', resources.messages['recordErrors']));
        }
      }
      const errorValidations = validations.filter(validation => validation.levelError === 'ERROR');
      const warningValidations = validations.filter(validation => validation.levelError === 'WARNING');

      errorValidations.forEach(validation =>
        validation.message
          ? (messageErrors += '- ' + capitalizeFirstLetterAndToLowerCase(validation.message) + '\n')
          : ''
      );

      warningValidations.forEach(validation =>
        validation.message
          ? (messageWarnings += '- ' + capitalizeFirstLetterAndToLowerCase(validation.message) + '\n')
          : ''
      );
      return errorValidations.length > 0 && warningValidations.length > 0 ? (
        <div className={styles.iconTooltipWrapper}>
          <IconTooltip levelError="WARNING" message={messageWarnings} style={{ width: '1.5em' }} />
          <IconTooltip levelError="ERROR" message={messageErrors} style={{ width: '1.5em' }} />
        </div>
      ) : errorValidations.length > 0 ? (
        <IconTooltip levelError="ERROR" message={messageErrors} />
      ) : warningValidations.length > 0 ? (
        <IconTooltip levelError="WARNING" message={messageWarnings} />
      ) : null;
    };

    const rowClassName = rowData => {
      let id = rowData.dataRow.filter(record => Object.keys(record.fieldData)[0] === 'id')[0].fieldData.id;
      return {
        'p-highlight': id === selectedRecordErrorId,
        'p-highlight-contextmenu': ''
      };
      // let selected = selectedRecords.filter(record => record.recordId === id);
      // if (!isUndefined(selected[0])) {
      //   return {
      //     'p-highlight': id === selectedRecordErrorId || rowData.recordId === selected[0].recordId
      //   };
      // } else {
      //   return {
      //     'p-highlight': id === selectedRecordErrorId
      //   };
      // }
    };

    const totalCount = () => {
      return (
        <span>
          {resources.messages['totalRecords']} {!isUndefined(totalRecords) ? totalRecords : 0}{' '}
          {resources.messages['records'].toLowerCase()}
        </span>
      );
    };

    const filteredCount = () => {
      return (
        <span>
          {resources.messages['totalRecords']}{' '}
          {!isNull(totalFilteredRecords) && !isUndefined(totalFilteredRecords) ? totalFilteredRecords : totalRecords}{' '}
          {'of'} {!isUndefined(totalRecords) ? totalRecords : 0} {resources.messages['records'].toLowerCase()}
        </span>
      );
    };

    const getPaginatorRecordsCount = () => {
      if (isNull(totalFilteredRecords) || isUndefined(totalFilteredRecords) || totalFilteredRecords == totalRecords) {
        return totalCount();
      } else {
        return filteredCount();
      }
    };

    // const totalCount = (
    //   <span>
    //     {resources.messages['totalRecords']}{' '}
    //     {!isNull(totalFilteredRecords) && !isUndefined(totalFilteredRecords) ? totalFilteredRecords : totalRecords}{' '}
    //     {'of'} {!isUndefined(totalRecords) ? totalRecords : 0}{' '}
    //     {/* {!isNull(totalFilteredRecords) ? totalFilteredRecords : totalRecords} */}
    //     {resources.messages['rows']}
    //   </span>
    // );

    return (
      <>
        <Toolbar className={styles.dataViewerToolbar}>
          <div className="p-toolbar-group-left">
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={!hasWritePermissions || isWebFormMMR}
              icon={'export'}
              label={resources.messages['import']}
              onClick={() => setImportDialogVisible(true)}
            />

            <Button
              disabled={!hasWritePermissions}
              id="buttonExportTable"
              className={`p-button-rounded p-button-secondary`}
              icon={isLoadingFile ? 'spinnerAnimate' : 'import'}
              label={resources.messages['exportTable']}
              onClick={event => {
                exportMenuRef.current.show(event);
              }}
            />
            <Menu
              model={exportButtonsList}
              popup={true}
              ref={exportMenuRef}
              id="exportTableMenu"
              onShow={e => getExportButtonPosition(e)}
            />

            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={!hasWritePermissions || isWebFormMMR || isUndefined(totalRecords)}
              icon={'trash'}
              label={resources.messages['deleteTable']}
              onClick={() => onSetVisible(setDeleteDialogVisible, true)}
            />

            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={false}
              icon={visibilityColumnIcon}
              label={resources.messages['visibility']}
              onClick={event => {
                dropdownFilterRef.current.show(event);
              }}
            />
            <DropdownFilter
              filters={visibilityDropdownFilter}
              popup={true}
              ref={dropdownFilterRef}
              id="exportTableMenu"
              showFilters={showFilters}
              onShow={e => {
                getExportButtonPosition(e);
              }}
            />

            <Button
              className={'p-button-rounded p-button-secondary'}
              disabled={!tableHasErrors}
              icon="filter"
              iconClasses={!isFilterValidationsActive ? styles.filterInactive : ''}
              label={resources.messages['validationFilter']}
              onClick={event => {
                filterMenuRef.current.show(event);
              }}
            />
            <DropdownFilter
              disabled={isLoading}
              filters={validationDropdownFilter}
              popup={true}
              ref={filterMenuRef}
              id="exportTableMenu"
              showFilters={showValidationFilter}
              onShow={e => {
                getExportButtonPosition(e);
              }}
            />

            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon="filter"
              label={resources.messages['filters']}
              onClick={() => {}}
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
        <ContextMenu model={menu} ref={contextMenuRef} />
        <div className={styles.Table}>
          <DataTable
            autoLayout={true}
            //columnsPreviewNumber={columnsPreviewNumber}
            contextMenuSelection={selectedRecord}
            editable={hasWritePermissions}
            //emptyMessage={resources.messages['noDataInDataTable']}
            id={tableId}
            first={firstRow}
            footer={hasWritePermissions && !isWebFormMMR ? addRowFooter : null}
            header={header}
            lazy={true}
            loading={isLoading}
            onContextMenu={
              hasWritePermissions
                ? e => {
                    datatableRef.current.closeEditingCell();
                    contextMenuRef.current.show(e.originalEvent);
                  }
                : null
            }
            onContextMenuSelectionChange={e => {
              onSelectRecord(e.value);
            }}
            onPage={onChangePage}
            onPaste={e => {
              onPaste(e);
            }}
            //onPasteAccept={onPasteAccept}
            onRowSelect={e => onSelectRecord(Object.assign({}, e.data))}
            // onSelectionChange={e => {
            //   setSelectedRecords(e.value);
            // }}
            onSort={onSort}
            paginator={true}
            paginatorRight={getPaginatorRecordsCount()}
            //pasteHeader={resources.messages['pasteRecords']}
            //pastedRecords={pastedRecords}
            //recordsPreviewNumber={recordsPreviewNumber}
            ref={datatableRef}
            reorderableColumns={true}
            resizableColumns={true}
            rowClassName={rowClassName}
            rows={numRows}
            rowsPerPageOptions={[5, 10, 20, 100]}
            //selection={selectedRecords}
            selectionMode="single"
            sortable={true}
            sortField={sortField}
            sortOrder={sortOrder}
            totalRecords={!isNull(totalFilteredRecords) ? totalFilteredRecords : totalRecords}
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
            url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.loadDataTable, {
              datasetId: datasetId,
              tableId: tableId
            })}`}
          />
        </Dialog>

        <ConfirmDialog
          header={resources.messages['deleteDatasetTableHeader']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
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
          labelConfirm={resources.messages['yes']}
          labelCancel={resources.messages['no']}>
          {resources.messages['confirmDeleteRow']}
        </ConfirmDialog>

        <ConfirmDialog
          className="edit-table"
          header={resources.messages['pasteRecords']}
          hasPasteOption={true}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={onPasteAccept}
          onHide={onPasteCancel}
          onPaste={onPaste}
          onPasteAsync={onPasteAsync}
          divRef={divRef}
          visible={confirmPasteVisible}>
          <InfoTable
            data={pastedRecords}
            filteredColumns={colsSchema.filter(
              column =>
                column.field !== 'actions' &&
                column.field !== 'recordValidation' &&
                column.field !== 'id' &&
                column.field !== 'datasetPartitionId'
            )}
            numRecords={numCopiedRecords}
            onDeletePastedRecord={onDeletePastedRecord}></InfoTable>
          <br />
          <br />
          <hr />
        </ConfirmDialog>

        <Dialog
          className="edit-table"
          blockScroll={false}
          contentStyle={{ height: '80%', maxHeight: '80%', overflow: 'auto' }}
          footer={addRowDialogFooter}
          header={resources.messages['addNewRow']}
          modal={true}
          onHide={() => setAddDialogVisible(false)}
          style={{ width: '50%', height: '80%' }}
          visible={addDialogVisible}>
          <div className="p-grid p-fluid">{newRecordForm}</div>
        </Dialog>
        <Dialog
          className="edit-table"
          blockScroll={false}
          contentStyle={{ height: '80%', maxHeight: '80%', overflow: 'auto' }}
          footer={editRowDialogFooter}
          header={resources.messages['editRow']}
          modal={true}
          onHide={() => setEditDialogVisible(false)}
          style={{ width: '50%', height: '80%' }}
          visible={editDialogVisible}>
          <div className="p-grid p-fluid">{editRecordForm}</div>
        </Dialog>
      </>
    );
  }
);

export { DataViewer };
