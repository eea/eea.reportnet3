/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef, useReducer } from 'react';
import { withRouter } from 'react-router-dom';
import { isEmpty, isUndefined, isNull } from 'lodash';

import { DatasetConfig } from 'conf/domain/model/Dataset';
import { config } from 'conf';

import styles from './DataViewer.module.css';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { ActionsToolbar } from './_components/ActionsToolbar';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { ContextMenu } from 'ui/views/_components/ContextMenu';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { DataForm } from './_components/DataForm';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { FieldEditor } from './_components/FieldEditor';
import { Footer } from './_components/Footer';
import { IconTooltip } from 'ui/views/_components/IconTooltip';
import { InfoTable } from './_components/InfoTable';

import { DatasetService } from 'core/services/Dataset';

import { DatasetContext } from 'ui/views/_functions/Contexts/DatasetContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';

import { recordReducer } from './_functions/Reducers/recordReducer';
import { sortReducer } from './_functions/Reducers/sortReducer';

import { DataViewerUtils } from './_functions/Utils/DataViewerUtils';
import { getUrl, TextUtils } from 'core/infrastructure/CoreUtils';
import { MetadataUtils } from 'ui/views/_functions/Utils/MetadataUtils';
import { RecordUtils } from 'ui/views/_functions/Utils';

const DataViewer = withRouter(
  ({
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
    match: { params },
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
      if (!isEmpty(inmTableSchemaColumns)) {
        inmTableSchemaColumns.push({ table: inmTableSchemaColumns[0].table, field: 'id', header: '' });
        inmTableSchemaColumns.push({ table: inmTableSchemaColumns[0].table, field: 'datasetPartitionId', header: '' });
      }
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
    }, [colsSchema, columnOptions, records.selectedRecord, initialCellValue]);

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
      if (confirmPasteVisible && !isUndefined(records.pastedRecords) && records.pastedRecords.length > 0) {
        dispatchRecords({ type: 'EMPTY_PASTED_RECORDS', payload: [] });
      }
    }, [confirmPasteVisible]);

    const onCancelRowEdit = () => {
      let updatedValue = RecordUtils.changeRecordInTable(
        fetchedData,
        RecordUtils.getRecordId(fetchedData, records.selectedRecord),
        colsSchema,
        records
      );
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
      try {
        await DatasetService.deleteTableDataById(datasetId, tableId);
        setFetchedData([]);
        dispatchRecords({ type: 'SET_TOTAL', payload: 0 });
        dispatchRecords({ type: 'SET_FILTERED', payload: 0 });
        snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
      } catch (error) {
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
        notificationContext.add({
          type: 'DELETE_TABLE_DATA_BY_ID_ERROR',
          content: {
            dataflowId,
            datasetId,
            dataflowName,
            datasetName,
            tableName
          }
        });
      } finally {
        setDeleteDialogVisible(false);
      }
    };

    const onConfirmDeleteRow = async () => {
      try {
        await DatasetService.deleteRecordById(datasetId, records.selectedRecord.recordId);

        snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
        const calcRecords = records.totalFilteredRecords >= 0 ? records.totalFilteredRecords : records.totalRecords;
        const page =
          (calcRecords - 1) / records.recordsPerPage === 1
            ? (Math.floor(records.firstPageRecord / records.recordsPerPage) - 1) * records.recordsPerPage
            : Math.floor(records.firstPageRecord / records.recordsPerPage) * records.recordsPerPage;
        dispatchRecords({ type: 'SET_FIRST_PAGE_RECORD', payload: page });
        dispatchRecords({ type: 'IS_RECORD_DELETED', payload: true });
      } catch (error) {
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
        notificationContext.add({
          type: 'DELETE_RECORD_BY_ID_ERROR',
          content: {
            dataflowId,
            datasetId,
            dataflowName,
            datasetName,
            tableName
          }
        });
      } finally {
        setDeleteDialogVisible(false);
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
        let updatedData = RecordUtils.changeCellValue([...props.value], props.rowIndex, props.field, initialCellValue);
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
          selectedCellId === RecordUtils.getCellId(cell, cell.field) &&
          record.recordId === records.selectedRecord.recordId
        ) {
          try {
            //without await. We don't have to wait for the response.
            const fieldUpdated = DatasetService.updateFieldById(datasetId, cell.field, field.id, field.type, value);
            if (!fieldUpdated) {
              throw new Error('UPDATE_FIELD_BY_ID_ERROR');
            }
          } catch (error) {
            const {
              dataflow: { name: dataflowName },
              dataset: { name: datasetName }
            } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
            notificationContext.add({
              type: 'UPDATE_FIELD_BY_ID_ERROR',
              content: {
                dataflowId,
                datasetId,
                dataflowName,
                datasetName,
                tableName
              }
            });
          } finally {
            snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
          }
        }
        if (isEditing) {
          setIsEditing(false);
        }
      }
    };

    const onEditorValueChange = (props, value) => {
      const updatedData = RecordUtils.changeCellValue([...props.value], props.rowIndex, props.field, value);
      setFetchedData(updatedData);
    };

    const onEditorValueFocus = (props, value) => {
      setSelectedCellId(RecordUtils.getCellId(props, props.field));
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

        if (!isEmpty(tableData.records) && !isUndefined(onLoadTableData)) {
          onLoadTableData(true);
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
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
        notificationContext.add({
          type: 'TABLE_DATA_BY_ID_ERROR',
          content: {
            dataflowId,
            datasetId,
            dataflowName,
            datasetName
          }
        });
      } finally {
        setIsLoading(false);
      }
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
        if (!recordsAdded) {
          throw new Error('ADD_RECORDS_BY_ID_ERROR');
        }
      } catch (error) {
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
        notificationContext.add({
          type: 'ADD_RECORDS_BY_ID_ERROR',
          content: {
            dataflowId,
            datasetId,
            dataflowName,
            datasetName,
            tableName
          }
        });
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
          snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
          onRefresh();
        } catch (error) {
          const {
            dataflow: { name: dataflowName },
            dataset: { name: datasetName }
          } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
          notificationContext.add({
            type: 'ADD_RECORDS_BY_ID_ERROR',
            content: {
              dataflowId,
              datasetId,
              dataflowName,
              datasetName,
              tableName
            }
          });
        } finally {
          setAddDialogVisible(false);
          setIsLoading(false);
        }
      } else {
        try {
          await DatasetService.updateRecordsById(datasetId, record);
          onRefresh();
          snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
        } catch (error) {
          const {
            dataflow: { name: dataflowName },
            dataset: { name: datasetName }
          } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
          notificationContext.add({
            type: 'UPDATE_RECORDS_BY_ID_ERROR',
            content: {
              dataflowId,
              datasetId,
              dataflowName,
              datasetName,
              tableName
            }
          });
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
      dispatchSort({ type: 'SORT_TABLE', payload: { order: event.sortOrder, field: event.sortField } });
      dispatchRecords({ type: 'SET_FIRST_PAGE_RECORD', payload: 0 });
      onFetchData(event.sortField, event.sortOrder, 0, records.recordsPerPage, levelErrorTypesWithCorrects);
    };

    const onUpload = async () => {
      setImportDialogVisible(false);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'DATASET_DATA_LOADING_INIT',
        content: {
          datasetLoadingMessage: resources.messages['datasetLoadingMessage'],
          title: TextUtils.ellipsis(tableName, config.notifications.STRING_LENGTH_MAX),
          datasetLoading: resources.messages['datasetLoading'],
          dataflowName,
          datasetName
        }
      });
    };

    const actionTemplate = () => (
      <ActionsColumn
        onDeleteClick={() => setConfirmDeleteVisible(true)}
        onEditClick={() => setEditDialogVisible(true)}
      />
    );

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

    const cellDataEditor = (cells, record) => {
      return (
        <FieldEditor
          cells={cells}
          record={record}
          onEditorSubmitValue={onEditorSubmitValue}
          onEditorValueChange={onEditorValueChange}
          onEditorValueFocus={onEditorValueFocus}
          onEditorKeyChange={onEditorKeyChange}
        />
      );
    };

    //Template for Field validation
    const dataTemplate = (rowData, column) => {
      let field = rowData.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
      if (field !== null && field && field.fieldValidations !== null && !isUndefined(field.fieldValidations)) {
        const validations = DataViewerUtils.orderValidationsByLevelError([...field.fieldValidations]);
        const message = DataViewerUtils.formatValidations(validations);
        const levelError = DataViewerUtils.getLevelError(validations);

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
      const dataFiltered = DataViewerUtils.parseData(data);
      if (dataFiltered.length > 0) {
        dispatchRecords({ type: 'FIRST_FILTERED_RECORD', payload: dataFiltered[0] });
      } else {
        setFetchedData([]);
      }
      setFetchedData(dataFiltered);
    };

    //Template for Record validation
    const validationsTemplate = recordData => {
      const validationsGroup = DataViewerUtils.groupValidations(
        recordData,
        resources.messages['recordBlockers'],
        resources.messages['recordErrors'],
        resources.messages['recordWarnings'],
        resources.messages['recordInfos']
      );
      return getIconsValidationsErrors(validationsGroup);
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

      const blockerIcon = addIconLevelError(validations.blockers, 'BLOCKER', validations.messageBlockers);
      const errorIcon = addIconLevelError(validations.errors, 'ERROR', validations.messageErrors);
      const warningIcon = addIconLevelError(validations.warnings, 'WARNING', validations.messageWarnings);
      const infoIcon = addIconLevelError(validations.infos, 'INFO', validations.messageInfos);

      icons = blockerIcon.concat(errorIcon, warningIcon, infoIcon);
      return <div className={styles.iconTooltipWrapper}>{icons}</div>;
    };

    const rowClassName = rowData => {
      let id = rowData.dataRow.filter(record => Object.keys(record.fieldData)[0] === 'id')[0].fieldData.id;
      return {
        'p-highlight': id === selectedRecordErrorId,
        'p-highlight-contextmenu': ''
      };
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
        <ActionsToolbar
          colsSchema={colsSchema}
          datasetId={datasetId}
          dataflowId={dataflowId}
          hasWritePermissions={hasWritePermissions}
          isFilterValidationsActive={isFilterValidationsActive}
          isWebFormMMR={isWebFormMMR}
          isLoading={isLoading}
          levelErrorTypesWithCorrects={levelErrorTypesWithCorrects}
          onRefresh={onRefresh}
          onSetColumns={currentColumns => setColumns(currentColumns)}
          onSetInvisibleColumns={currentInvisibleColumns => setInvisibleColumns(currentInvisibleColumns)}
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
            contextMenuSelection={records.selectedRecord}
            editable={hasWritePermissions}
            id={tableId}
            first={records.firstPageRecord}
            footer={
              hasWritePermissions && !isWebFormMMR ? (
                <Footer
                  hasWritePermissions={hasWritePermissions}
                  onAddClick={() => {
                    setIsNewRecord(true);
                    setAddDialogVisible(true);
                  }}
                  onPasteClick={() => setConfirmPasteVisible(true)}
                />
              ) : null
            }
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
            onContextMenuSelectionChange={e => onSelectRecord(e.value)}
            onPage={onChangePage}
            onPaste={e => onPaste(e)}
            onRowSelect={e => onSelectRecord(Object.assign({}, e.data))}
            onSort={onSort}
            paginator={true}
            paginatorRight={getPaginatorRecordsCount()}
            ref={datatableRef}
            reorderableColumns={true}
            resizableColumns={true}
            rowClassName={rowClassName}
            rows={records.recordsPerPage}
            rowsPerPageOptions={[5, 10, 20, 100]}
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
            value={fetchedData}>
            {columns}
          </DataTable>
        </div>
        <Dialog
          className={styles.Dialog}
          dismissableMask={false}
          header={`${resources.messages['uploadDataset']}${tableName}`}
          onHide={() => setImportDialogVisible(false)}
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
          onHide={() => setConfirmDeleteVisible(false)}
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
          <div className="p-grid p-fluid">
            <DataForm
              colsSchema={colsSchema}
              formType="NEW"
              addDialogVisible={addDialogVisible}
              onChangeForm={onEditAddFormInput}
              records={records}
            />
          </div>
        </Dialog>
        <Dialog
          className="edit-table"
          blockScroll={false}
          closeOnEscape={false}
          contentStyle={{ height: '80%', maxHeight: '80%', overflow: 'auto' }}
          footer={editRowDialogFooter}
          header={resources.messages['editRow']}
          modal={true}
          onHide={() => setEditDialogVisible(false)}
          style={{ width: '50%', height: '80%' }}
          visible={editDialogVisible}>
          <div className="p-grid p-fluid">
            <DataForm
              colsSchema={colsSchema}
              formType="EDIT"
              editDialogVisible={editDialogVisible}
              onChangeForm={onEditAddFormInput}
              records={records}
            />
          </div>
        </Dialog>
      </SnapshotContext.Provider>
    );
  }
);

export { DataViewer };
