/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef, useReducer } from 'react';
import { withRouter } from 'react-router-dom';
import { capitalize, isEmpty, isUndefined, isNull, isEqual } from 'lodash';

import { DatasetConfig } from 'conf/domain/model/Dataset';
import { config } from 'conf';

import styles from './DataViewer.module.css';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { ContextMenu } from 'ui/views/_components/ContextMenu';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { IconTooltip } from './_components/IconTooltip';
import { InfoTable } from './_components/InfoTable';
import { InputText } from 'ui/views/_components/InputText';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';

import { DatasetContext } from 'ui/views/_functions/Contexts/DatasetContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';

import { recordReducer } from './_functions/Reducers/recordReducer';
import { sortReducer } from './_functions/Reducers/sortReducer';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { DatasetService } from 'core/services/Dataset';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

import { RecordUtils } from 'ui/views/_functions/Utils';

import { routes } from 'ui/routes';
import { DataViewerToolbar } from './_components/DataViewerToolbar/DataViewerToolbar';

const DataViewer = withRouter(
  ({
    buttonsList = undefined,
    correctLevelError = ['CORRECT'],
    hasWritePermissions,
    isPreviewModeOn = false,
    isWebFormMMR,
    levelErrorTypes = !isPreviewModeOn ? correctLevelError.concat(levelErrorTypes) : correctLevelError,
    levelErrorTypesWithCorrects = !isPreviewModeOn ? correctLevelError.concat(levelErrorTypes) : correctLevelError,
    onLoadTableData,
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
    const [editDialogVisible, setEditDialogVisible] = useState(false);
    const [fetchedData, setFetchedData] = useState([]);
    const [header] = useState();
    const [importDialogVisible, setImportDialogVisible] = useState(false);
    const [initialCellValue, setInitialCellValue] = useState();
    const [isEditing, setIsEditing] = useState(false);
    const [isFilterValidationsActive, setIsFilterValidationsActive] = useState(false);
    const [isNewRecord, setIsNewRecord] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [levelErrorValidations, setLevelErrorValidations] = useState(levelErrorTypesWithCorrects);
    const [menu, setMenu] = useState();
    const [originalColumns, setOriginalColumns] = useState([]);
    //const [selectedRecords, setSelectedRecords] = useState([]);
    const [selectedCellId, setSelectedCellId] = useState();
    const [invisibleColumns, setInvisibleColumns] = useState([]);

    const [records, dispatchRecords] = useReducer(recordReducer, {
      totalRecords: 0,
      totalFilteredRecords: 0,
      firstPageRecord: 0,
      recordsPerPage: 10,
      initialRecordValue: undefined,
      isRecordDeleted: false,
      editedRecord: {},
      selectedRecord: {},
      newRecord: {},
      numCopiedRecords: undefined,
      pastedRecords: undefined,
      fetchedDataFirstRecord: []
    });
    const [sort, dispatchSort] = useReducer(sortReducer, {
      sortField: undefined,
      sortOrder: undefined
    });

    const datasetContext = useContext(DatasetContext);
    const notificationContext = useContext(NotificationContext);
    const resources = useContext(ResourcesContext);
    const snapshotContext = useContext(SnapshotContext);

    let contextMenuRef = useRef();
    let datatableRef = useRef();
    let divRef = useRef();

    useEffect(() => {
      let colOptions = [];
      let dropdownFilter = [];
      for (let colSchema of colsSchema) {
        colOptions.push({ label: colSchema.header, value: colSchema });
        dropdownFilter.push({ label: colSchema.header, key: colSchema.field });
      }
      setColumnOptions(colOptions);

      const inmTableSchemaColumns = [...tableSchemaColumns];
      inmTableSchemaColumns.push({ table: inmTableSchemaColumns[0].table, field: 'id', header: '' });
      inmTableSchemaColumns.push({ table: inmTableSchemaColumns[0].table, field: 'datasetPartitionId', header: '' });
      setColsSchema(inmTableSchemaColumns);
    }, []);

    useEffect(() => {
      if (datasetContext.isValidationSelected) {
        setIsFilterValidationsActive(false);
        setLevelErrorValidations(levelErrorTypesWithCorrects);
        datasetContext.setIsValidationSelected(false);
      }
    }, [datasetContext.isValidationSelected]);

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
    }, [records.selectedRecord]);

    useEffect(() => {
      if (records.isRecordDeleted) {
        onRefresh();
        setConfirmDeleteVisible(false);
      }
    }, [records.isRecordDeleted]);

    useEffect(() => {
      dispatchRecords({ type: 'IS_RECORD_DELETED', payload: false });
    }, [confirmDeleteVisible]);

    useEffect(() => {
      if (isUndefined(recordPositionId) || recordPositionId === -1) {
        return;
      }
      dispatchRecords({
        type: 'SET_FIRST_PAGE_RECORD',
        payload: Math.floor(recordPositionId / records.recordsPerPage) * records.recordsPerPage
      });
      dispatchSort({ type: 'SORT_TABLE', payload: { order: undefined, field: undefined } });
      onFetchData(
        undefined,
        undefined,
        Math.floor(recordPositionId / records.recordsPerPage) * records.recordsPerPage,
        records.recordsPerPage,
        levelErrorTypesWithCorrects
      );
    }, [recordPositionId]);

    useEffect(() => {
      const maxWidths = [];
      // if (!isEditing) {
      //Calculate the max width of the shown data
      // colsSchema.forEach(col => {
      //   const bulkData = fetchedData.map(data => data.dataRow.map(d => d.fieldData).flat()).flat();
      //   const filteredBulkData = bulkData
      //     .filter(data => col.field === Object.keys(data)[0])
      //     .map(filteredData => Object.values(filteredData)[0]);
      //   if (filteredBulkData.length > 0) {
      //     const maxDataWidth = filteredBulkData.map(data => getTextWidth(data, '14pt Open Sans'));
      //     maxWidths.push(Math.max(...maxDataWidth) - 10 > 400 ? 400 : Math.max(...maxDataWidth) - 10);
      //   }
      // });

      //Calculate the max width of data column
      const textMaxWidth = colsSchema.map(col => RecordUtils.getTextWidth(col.header, '14pt Open Sans'));
      const maxWidth = Math.max(...textMaxWidth) + 30;
      let columnsArr = colsSchema.map((column, i) => {
        let sort = column.field === 'id' || column.field === 'datasetPartitionId' ? false : true;
        let invisibleColumn =
          column.field === 'id' || column.field === 'datasetPartitionId' ? styles.invisibleHeader : '';
        return (
          <Column
            body={dataTemplate}
            className={invisibleColumn}
            editor={hasWritePermissions && !isWebFormMMR ? row => cellDataEditor(row, records.selectedRecord) : null}
            //editorValidator={requiredValidator}
            field={column.field}
            header={column.header}
            key={column.field}
            sortable={sort}
            style={{
              width: !invisibleColumn
                ? `${!isUndefined(maxWidths[i]) ? (maxWidth > maxWidths[i] ? maxWidth : maxWidths[i]) : maxWidth}px`
                : '1px'
            }}
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
          style={{ width: '100px' }}
        />
      );
      let validationCol = (
        <Column
          body={validationsTemplate}
          header={resources.messages['errors']}
          field="validations"
          key="recordValidation"
          sortable={false}
          style={{ width: '100px' }}
        />
      );

      if (!isWebFormMMR) {
        hasWritePermissions ? columnsArr.unshift(editCol, validationCol) : columnsArr.unshift(validationCol);
      }

      if (invisibleColumns.length > 0 && columnsArr.length !== invisibleColumns.length) {
        const visibleKeys = invisibleColumns.map(column => {
          return column.key;
        });
        setColumns(columnsArr.filter(column => visibleKeys.includes(column.key)));
      } else {
        setColumns(columnsArr);
        setOriginalColumns(columnsArr);
      }
      // }
    }, [colsSchema, columnOptions, records.selectedRecord, records.editedRecord, initialCellValue]);

    const showValidationFilter = filteredKeys => {
      // length of errors in data schema rules of validation
      setIsFilterValidationsActive(filteredKeys.length !== levelErrorTypesWithCorrects.length);
      dispatchRecords({ type: 'SET_FIRST_PAGE_RECORD', payload: 0 });
      setLevelErrorValidations(filteredKeys);
    };

    useEffect(() => {
      onFetchData(sort.sortField, sort.sortOrder, 0, records.recordsPerPage, levelErrorValidations);
    }, [levelErrorValidations]);

    useEffect(() => {
      if (confirmPasteVisible) {
        if (!isUndefined(records.pastedRecords)) {
          if (records.pastedRecords.length > 0) {
            dispatchRecords({ type: 'EMPTY_PASTED_RECORDS', payload: [] });
          }
        }
        // if (confirmPasteVisible) {
        //   divRef.current.focus();
        // }
      }
    }, [confirmPasteVisible]);

    const onCancelRowEdit = () => {
      let updatedValue = changeRecordInTable(fetchedData, getRecordId(fetchedData, records.selectedRecord));
      setEditDialogVisible(false);
      if (!isUndefined(updatedValue)) {
        setFetchedData(updatedValue);
      }
    };

    const onChangePage = event => {
      dispatchRecords({ type: 'SET_RECORDS_PER_PAGE', payload: event.rows });
      dispatchRecords({ type: 'SET_FIRST_PAGE_RECORD', payload: event.first });
      onFetchData(sort.sortField, sort.sortOrder, event.first, event.rows, levelErrorValidations);
    };

    const onConfirmDeleteTable = async () => {
      setDeleteDialogVisible(false);
      const dataDeleted = await DatasetService.deleteTableDataById(datasetId, tableId);
      if (dataDeleted) {
        setFetchedData([]);
        dispatchRecords({ type: 'SET_TOTAL', payload: 0 });
        dispatchRecords({ type: 'SET_FILTERED', payload: 0 });
        snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
      }
    };

    const onConfirmDeleteRow = async () => {
      setDeleteDialogVisible(false);
      const recordDeleted = await DatasetService.deleteRecordById(datasetId, records.selectedRecord.recordId);
      if (recordDeleted) {
        snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
        const calcRecords = records.totalFilteredRecords >= 0 ? records.totalFilteredRecords : records.totalRecords;
        const page =
          (calcRecords - 1) / records.recordsPerPage === 1
            ? (Math.floor(records.firstPageRecord / records.recordsPerPage) - 1) * records.recordsPerPage
            : Math.floor(records.firstPageRecord / records.recordsPerPage) * records.recordsPerPage;
        dispatchRecords({ type: 'SET_FIRST_PAGE_RECORD', payload: page });
        dispatchRecords({ type: 'IS_RECORD_DELETED', payload: true });
      }
    };

    const onDeletePastedRecord = recordIndex => {
      dispatchRecords({ type: 'DELETE_PASTED_RECORDS', payload: { recordIndex } });
    };

    const onEditAddFormInput = (property, value) => {
      dispatchRecords({ type: !isNewRecord ? 'SET_EDITED_RECORD' : 'SET_NEW_RECORD', payload: { property, value } });
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
          record.recordId === records.selectedRecord.recordId
        ) {
          //without await. We don't have to wait for the response.
          const fieldUpdated = DatasetService.updateFieldById(datasetId, cell.field, field.id, field.type, value);
          if (!fieldUpdated) {
            console.error('Error!');
          }
          snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
        }
        if (isEditing) {
          setIsEditing(false);
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
      if (!isEditing) {
        setIsEditing(true);
      }
    };

    const onFetchData = async (sField, sOrder, fRow, nRows, levelErrorValidations) => {
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
          levelErrorValidations
        );

        if (!isEmpty(tableData.records)) {
          if (!isUndefined(onLoadTableData)) {
            onLoadTableData(true);
          }
        }
        if (!isUndefined(colsSchema) && !isUndefined(tableData)) {
          if (!isUndefined(tableData.records)) {
            if (tableData.records.length > 0) {
              dispatchRecords({
                type: 'SET_NEW_RECORD',
                payload: RecordUtils.createEmptyObject(colsSchema, tableData.records[0])
              });
            }
          } else {
            dispatchRecords({
              type: 'SET_NEW_RECORD',
              payload: RecordUtils.createEmptyObject(colsSchema, undefined)
            });
          }
        }
        if (!isUndefined(tableData.records)) {
          filterDataResponse(tableData);
        } else {
          setFetchedData([]);
        }

        if (tableData.totalRecords !== records.totalRecords) {
          dispatchRecords({ type: 'SET_TOTAL', payload: tableData.totalRecords });
        }

        if (tableData.totalFilteredRecords !== records.totalFilteredRecords) {
          dispatchRecords({ type: 'SET_FILTERED', payload: tableData.totalFilteredRecords });
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
        dispatchRecords({ type: 'COPY_RECORDS', payload: { pastedData, colsSchema } });
      }
    };

    const onPasteAsync = async () => {
      const pastedData = await navigator.clipboard.readText();
      dispatchRecords({ type: 'COPY_RECORDS', payload: { pastedData, colsSchema } });
    };

    const onPasteAccept = async () => {
      try {
        const recordsAdded = await DatasetService.addRecordsById(datasetId, tableId, records.pastedRecords);
        if (recordsAdded) {
          notificationContext.add({
            type: 'ADD_RECORDS_BY_ID_SUCCESS',
            message: resources.messages['dataPasted'],
            content: {
              dataflowId,
              datasetId
            }
          });
          onRefresh();
          snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
        } else {
          notificationContext.add({
            type: 'ADD_RECORDS_BY_ID_ERROR',
            message: resources.messages['dataPastedError'],
            content: {
              dataflowId,
              datasetId
            }
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
      onFetchData(
        sort.sortField,
        sort.sortOrder,
        records.firstPageRecord,
        records.recordsPerPage,
        levelErrorValidations
      );
    };

    const onPasteCancel = () => {
      dispatchRecords({ type: 'EMPTY_PASTED_RECORDS', payload: [] });
      setConfirmPasteVisible(false);
    };

    const onSelectRecord = val => {
      setIsNewRecord(false);
      dispatchRecords({ type: 'SET_EDITED_RECORD', payload: { record: { ...val }, colsSchema } });
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
          snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
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
          snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
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

    const onSetColumns = currentColumns => {
      setColumns(currentColumns);
    };

    const onSetInvisibleColumns = currentInvisibleColumns => {
      setInvisibleColumns(currentInvisibleColumns);
    };

    const onSetVisible = (fnUseState, visible) => {
      fnUseState(visible);
    };

    const onSort = event => {
      dispatchSort({ type: 'SORT_TABLE', payload: { order: event.sortOrder, field: event.sortField } });
      dispatchRecords({ type: 'SET_FIRST_PAGE_RECORD', payload: 0 });
      onFetchData(event.sortField, event.sortOrder, 0, records.recordsPerPage, levelErrorTypesWithCorrects);
    };

    const onUpload = () => {
      setImportDialogVisible(false);
      notificationContext.add({
        type: 'DATASET_DATA_LOADING_INIT',
        message: resources.messages['datasetDataLoadingInit'],
        content: {
          datasetLoadingMessage: resources.messages['datasetLoadingMessage'],
          title: editLargeStringWithDots(tableName, 22),
          datasetLoading: resources.messages['datasetLoading']
        }
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
            onSaveRecord(records.newRecord);
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
      const recordFiltered = RecordUtils.getInitialRecordValues(record, colsSchema);
      if (!isEqual(recordFiltered.flat(), records.initialRecordValue.flat())) {
        for (let i = 0; i < records.initialRecordValue.length; i++) {
          record = RecordUtils.changeRecordValue(
            record,
            records.initialRecordValue[i][0],
            records.initialRecordValue[i][1]
          );
        }

        tableData[rowIndex] = record;
        return tableData;
      }
    };

    const changeCellValue = (tableData, rowIndex, field, value) => {
      tableData[rowIndex].dataRow.filter(data => Object.keys(data.fieldData)[0] === field)[0].fieldData[field] = value;
      return tableData;
    };

    const orderValidationsByLevelError = validations => {
      return validations
        .sort((a, b) => {
          const levelErrorsWithPriority = [
            { id: 'INFO', index: 1 },
            { id: 'WARNING', index: 2 },
            { id: 'ERROR', index: 3 },
            { id: 'BLOCKER', index: 4 }
          ];
          let levelError = levelErrorsWithPriority.filter(priority => a.levelError === priority.id)[0].index;
          let levelError2 = levelErrorsWithPriority.filter(priority => b.levelError === priority.id)[0].index;
          return levelError < levelError2 ? -1 : levelError > levelError2 ? 1 : 0;
        })
        .reverse();
    };

    //Template for Field validation
    const dataTemplate = (rowData, column) => {
      let field = rowData.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
      if (field !== null && field && field.fieldValidations !== null && !isUndefined(field.fieldValidations)) {
        let message = [];
        const validations = orderValidationsByLevelError([...field.fieldValidations]);
        const errorValidations = [...new Set(validations.map(validation => validation.levelError))];
        validations.forEach(validation => {
          let error = '';
          if (errorValidations.length > 1) {
            error = `${capitalize(validation.levelError)}: `;
          }
          message += '- ' + error + capitalize(validation.message) + '\n';
        });

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
          if (!isUndefined(records.editedRecord.dataRow)) {
            const field = records.editedRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
            console.log('EDITANDO!');
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
      return null;
    });

    const editRowDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button
          label={resources.messages['save']}
          icon="save"
          onClick={() => {
            try {
              onSaveRecord(records.editedRecord);
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
        dispatchRecords({ type: 'FIRST_FILTERED_RECORD', payload: dataFiltered[0] });
      } else {
        setFetchedData([]);
      }
      setFetchedData(dataFiltered);
    };

    const getLevelError = validations => {
      let levelError = '';
      let lvlFlag = 0;
      const errors = [];
      validations.forEach(validation => {
        errors.push(validation.levelError);
      });
      let differentErrors = [...new Set(errors)];

      if (differentErrors.length > 1) {
        return 'MULTI';
      } else {
        validations.forEach(validation => {
          if (validation.levelError === 'INFO') {
            const iNum = 1;
            if (iNum > lvlFlag) {
              lvlFlag = iNum;
              levelError = 'INFO';
            }
          } else if (validation.levelError === 'WARNING') {
            const wNum = 2;
            if (wNum > lvlFlag) {
              lvlFlag = wNum;
              levelError = 'WARNING';
            }
          } else if (validation.levelError === 'ERROR') {
            const eNum = 3;
            if (eNum > lvlFlag) {
              lvlFlag = eNum;
              levelError = 'ERROR';
            }
          } else if (validation.levelError === 'BLOCKER') {
            const bNum = 4;
            if (bNum > lvlFlag) {
              lvlFlag = bNum;
              levelError = 'BLOCKER';
            }
          }
        });
      }
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

    const getRecordId = (tableData, record) => {
      return tableData
        .map(e => {
          return e.recordId;
        })
        .indexOf(record.recordId);
    };

    const newRecordForm = colsSchema.map((column, i) => {
      if (addDialogVisible) {
        if (i < colsSchema.length - 2) {
          if (!isUndefined(records.newRecord.dataRow)) {
            const field = records.newRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
            return (
              <React.Fragment key={column.field}>
                <div className="p-col-4" style={{ padding: '.75em' }}>
                  <label htmlFor={column.field}>{column.header}</label>
                </div>
                <div className="p-col-8" style={{ padding: '.5em' }}>
                  <InputText
                    id={column.field}
                    onChange={e => onEditAddFormInput(column.field, e.target.value, field)}
                  />
                </div>
              </React.Fragment>
            );
          }
        }
      }
    });

    // const requiredValidator = props => {
    //   let value = getCellValue(props, props.field);
    //   return value && value.length > 0;
    // };

    const getRecordValidationByErrorAndMessage = (levelError, message) => {
      return DatasetService.createValidation('RECORD', 0, levelError, message);
    };

    //Template for Record validation
    const validationsTemplate = recordData => {
      let validations = [];
      if (recordData.recordValidations && !isUndefined(recordData.recordValidations)) {
        validations = [...recordData.recordValidations];
      }

      let hasFieldErrors = false;

      const recordsWithFieldValidations = recordData.dataRow.filter(
        row => !isUndefined(row.fieldValidations) && !isNull(row.fieldValidations)
      );

      hasFieldErrors = !isEmpty(recordsWithFieldValidations);

      const filteredFieldValidations = recordsWithFieldValidations.map(record => record.fieldValidations).flat();

      if (hasFieldErrors) {
        const filteredFieldValidationsWithBlocker = filteredFieldValidations.filter(
          filteredFieldValidation => filteredFieldValidation.levelError === 'BLOCKER'
        );
        if (!isEmpty(filteredFieldValidationsWithBlocker)) {
          validations.push(getRecordValidationByErrorAndMessage('BLOCKER', resources.messages['recordBlockers']));
        }

        const filteredFieldValidationsWithError = filteredFieldValidations.filter(
          filteredFieldValidation => filteredFieldValidation.levelError === 'ERROR'
        );
        if (!isEmpty(filteredFieldValidationsWithError)) {
          validations.push(getRecordValidationByErrorAndMessage('ERROR', resources.messages['recordErrors']));
        }

        const filteredFieldValidationsWithWarning = filteredFieldValidations.filter(
          filteredFieldValidation => filteredFieldValidation.levelError === 'WARNING'
        );
        if (!isEmpty(filteredFieldValidationsWithWarning)) {
          validations.push(getRecordValidationByErrorAndMessage('WARNING', resources.messages['recordWarnings']));
        }

        const filteredFieldValidationsWithInfo = filteredFieldValidations.filter(
          filteredFieldValidation => filteredFieldValidation.levelError === 'INFO'
        );
        if (!isEmpty(filteredFieldValidationsWithInfo)) {
          validations.push(getRecordValidationByErrorAndMessage('INFO', resources.messages['recordInfos']));
        }
      }

      const blockerValidations = validations.filter(validation => validation.levelError === 'BLOCKER');
      const errorValidations = validations.filter(validation => validation.levelError === 'ERROR');
      const warningValidations = validations.filter(validation => validation.levelError === 'WARNING');
      const infoValidations = validations.filter(validation => validation.levelError === 'INFO');

      let messageBlockers = '';
      let messageErrors = '';
      let messageWarnings = '';
      let messageInfos = '';

      blockerValidations.forEach(validation =>
        validation.message ? (messageBlockers += '- ' + capitalize(validation.message) + '\n') : ''
      );

      errorValidations.forEach(validation =>
        validation.message ? (messageErrors += '- ' + capitalize(validation.message) + '\n') : ''
      );

      warningValidations.forEach(validation =>
        validation.message ? (messageWarnings += '- ' + capitalize(validation.message) + '\n') : ''
      );

      infoValidations.forEach(validation =>
        validation.message ? (messageInfos += '- ' + capitalize(validation.message) + '\n') : ''
      );

      let validationsGroup = {};
      validationsGroup.blockers = blockerValidations;
      validationsGroup.errors = errorValidations;
      validationsGroup.warnings = warningValidations;
      validationsGroup.infos = infoValidations;

      validationsGroup.messageBlockers = messageBlockers;
      validationsGroup.messageErrors = messageErrors;
      validationsGroup.messageWarnings = messageWarnings;
      validationsGroup.messageInfos = messageInfos;

      let iconValidaionsCell = getIconsValidationsErrors(validationsGroup);
      return iconValidaionsCell;
    };

    const addIconLevelError = (validation, levelError, message) => {
      let icon = [];
      if (!isEmpty(validation)) {
        icon.push(
          <IconTooltip levelError={levelError} message={message} style={{ width: '1.5em' }} key={levelError} />
        );
      }
      return icon;
    };

    const getIconsValidationsErrors = validations => {
      let icons = [];
      if (isNull(validations)) {
        return icons;
      }

      let blockerIcon = addIconLevelError(validations.blockers, 'BLOCKER', validations.messageBlockers);
      let errorIcon = addIconLevelError(validations.errors, 'ERROR', validations.messageErrors);
      let warningIcon = addIconLevelError(validations.warnings, 'WARNING', validations.messageWarnings);
      let infoIcon = addIconLevelError(validations.infos, 'INFO', validations.messageInfos);

      icons = blockerIcon.concat(errorIcon, warningIcon, infoIcon);
      return <div className={styles.iconTooltipWrapper}>{icons}</div>;
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
          {resources.messages['totalRecords']} {!isUndefined(records.totalRecords) ? records.totalRecords : 0}{' '}
          {resources.messages['records'].toLowerCase()}
        </span>
      );
    };

    const filteredCount = () => {
      return (
        <span>
          {resources.messages['filtered']}
          {':'}{' '}
          {!isNull(records.totalFilteredRecords) && !isUndefined(records.totalFilteredRecords)
            ? records.totalFilteredRecords
            : 0}
          {' | '}
          {resources.messages['totalRecords']} {!isUndefined(records.totalRecords) ? records.totalRecords : 0}{' '}
          {resources.messages['records'].toLowerCase()}
        </span>
      );
    };

    const filteredCountSameValue = () => {
      return (
        <span>
          {resources.messages['totalRecords']} {!isUndefined(records.totalRecords) ? records.totalRecords : 0}{' '}
          {resources.messages['records'].toLowerCase()} {'('}
          {resources.messages['filtered'].toLowerCase()}
          {')'}
        </span>
      );
    };

    const getPaginatorRecordsCount = () => {
      if (!isUndefined(records.totalFilteredRecords) || !isUndefined(records.totalRecords)) {
        if (!isFilterValidationsActive) {
          return totalCount();
        } else {
          return records.totalRecords == records.totalFilteredRecords ? filteredCountSameValue() : filteredCount();
        }
      }
    };

    return (
      <SnapshotContext.Provider>
        <DataViewerToolbar
          colsSchema={colsSchema}
          datasetId={datasetId}
          hasWritePermissions={hasWritePermissions}
          isFilterValidationsActive={isFilterValidationsActive}
          isWebFormMMR={isWebFormMMR}
          isLoading={isLoading}
          levelErrorTypesWithCorrects={levelErrorTypesWithCorrects}
          onRefresh={onRefresh}
          onSetColumns={onSetColumns}
          onSetInvisibleColumns={onSetInvisibleColumns}
          onSetVisible={onSetVisible}
          originalColumns={originalColumns}
          records={records}
          setDeleteDialogVisible={setDeleteDialogVisible}
          setImportDialogVisible={setImportDialogVisible}
          showValidationFilter={showValidationFilter}
          tableHasErrors={tableHasErrors}
          tableId={tableId}
          tableName={tableName}
        />
        <ContextMenu model={menu} ref={contextMenuRef} />
        <div className={styles.Table}>
          <DataTable
            // autoLayout={true}
            //columnsPreviewNumber={columnsPreviewNumber}
            contextMenuSelection={records.selectedRecord}
            editable={hasWritePermissions}
            //emptyMessage={resources.messages['noDataInDataTable']}
            id={tableId}
            first={records.firstPageRecord}
            footer={hasWritePermissions && !isWebFormMMR ? addRowFooter : null}
            header={header}
            lazy={true}
            loading={isLoading}
            onContextMenu={
              hasWritePermissions && !isEditing
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
            onRowSelect={e => {
              onSelectRecord(Object.assign({}, e.data));
            }}
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
            rows={records.recordsPerPage}
            rowsPerPageOptions={[5, 10, 20, 100]}
            //selection={selectedRecords}
            scrollable={true}
            scrollHeight="70vh"
            selectionMode="single"
            sortable={true}
            sortField={sort.sortField}
            sortOrder={sort.sortOrder}
            totalRecords={
              !isNull(records.totalFilteredRecords) &&
              !isUndefined(records.totalFilteredRecords) &&
              isFilterValidationsActive
                ? records.totalFilteredRecords
                : records.totalRecords
            }
            value={fetchedData}
            //frozenWidth="100px"
            // unfrozenWidth="600px"
          >
            {columns}
          </DataTable>
        </div>
        <Dialog
          className={styles.Dialog}
          dismissableMask={false}
          header={`${resources.messages['uploadDataset']}${tableName}`}
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
          header={`${resources.messages['deleteDatasetTableHeader']} (${tableName})`}
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
            data={records.pastedRecords}
            filteredColumns={colsSchema.filter(
              column =>
                column.field !== 'actions' &&
                column.field !== 'recordValidation' &&
                column.field !== 'id' &&
                column.field !== 'datasetPartitionId'
            )}
            numCopiedRecords={records.numCopiedRecords}
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
      </SnapshotContext.Provider>
    );
  }
);

export { DataViewer };
