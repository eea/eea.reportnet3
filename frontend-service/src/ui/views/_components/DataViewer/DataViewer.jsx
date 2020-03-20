/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef, useReducer } from 'react';
import { withRouter } from 'react-router-dom';
import isEmpty from 'lodash/isEmpty';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { DatasetConfig } from 'conf/domain/model/Dataset';
import { config } from 'conf';

import styles from './DataViewer.module.css';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { ActionsToolbar } from './_components/ActionsToolbar';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'ui/views/_components/Button';
import { Chips } from 'ui/views/_components/Chips';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { ContextMenu } from 'ui/views/_components/ContextMenu';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { DataForm } from './_components/DataForm';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { FieldEditor } from './_components/FieldEditor';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Footer } from './_components/Footer';
import { IconTooltip } from 'ui/views/_components/IconTooltip';
import { InfoTable } from './_components/InfoTable';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';

import { recordReducer } from './_functions/Reducers/recordReducer';
import { sortReducer } from './_functions/Reducers/sortReducer';

import { DataViewerUtils } from './_functions/Utils/DataViewerUtils';
import { getUrl, TextUtils } from 'core/infrastructure/CoreUtils';
import { MetadataUtils } from 'ui/views/_functions/Utils/MetadataUtils';
import { RecordUtils } from 'ui/views/_functions/Utils';
import {
  useLoadColsSchemasAndColumnOptions,
  useContextMenu,
  useSetColumns,
  useRecordErrorPosition
} from './_functions/Hooks/DataViewerHooks';

const DataViewer = withRouter(
  ({
    hasWritePermissions,
    isDatasetDeleted = false,
    isDataCollection,
    isValidationSelected,
    isWebFormMMR,
    levelErrorTypes,
    match: {
      params: { datasetId, dataflowId }
    },
    onLoadTableData,
    recordPositionId,
    selectedRecordErrorId,
    setIsValidationSelected,
    tableHasErrors,
    tableId,
    tableName,
    tableSchemaColumns
  }) => {
    const [addDialogVisible, setAddDialogVisible] = useState(false);
    const [confirmDeleteVisible, setConfirmDeleteVisible] = useState(false);
    const [confirmPasteVisible, setConfirmPasteVisible] = useState(false);
    const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
    const [editDialogVisible, setEditDialogVisible] = useState(false);
    const [fetchedData, setFetchedData] = useState([]);
    const [importDialogVisible, setImportDialogVisible] = useState(false);
    const [initialCellValue, setInitialCellValue] = useState();
    const [isColumnInfoVisible, setIsColumnInfoVisible] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [isFilterValidationsActive, setIsFilterValidationsActive] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [isNewRecord, setIsNewRecord] = useState(false);
    const [isPasting, setIsPasting] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [isTableDeleted, setIsTableDeleted] = useState(false);
    const [levelErrorTypesWithCorrects, setLevelErrorTypesWithCorrects] = useState([
      'CORRECT',
      'INFO',
      'WARNING',
      'ERROR',
      'BLOCKER'
    ]);
    const [levelErrorValidations, setLevelErrorValidations] = useState([]);
    const [recordErrorPositionId, setRecordErrorPositionId] = useState(recordPositionId);
    const [selectedCellId, setSelectedCellId] = useState();

    const [records, dispatchRecords] = useReducer(recordReducer, {
      editedRecord: {},
      fetchedDataFirstRecord: [],
      firstPageRecord: 0,
      initialRecordValue: undefined,
      isAllDataDeleted: isDatasetDeleted,
      isRecordAdded: false,
      isRecordDeleted: false,
      newRecord: {},
      numCopiedRecords: undefined,
      pastedRecords: undefined,
      recordsPerPage: 10,
      selectedRecord: {},
      totalFilteredRecords: 0,
      totalRecords: 0
    });
    const [sort, dispatchSort] = useReducer(sortReducer, {
      sortField: undefined,
      sortOrder: undefined
    });

    const notificationContext = useContext(NotificationContext);
    const resources = useContext(ResourcesContext);
    const snapshotContext = useContext(SnapshotContext);

    let contextMenuRef = useRef();
    let datatableRef = useRef();
    let divRef = useRef();

    const { colsSchema, columnOptions } = useLoadColsSchemasAndColumnOptions(tableSchemaColumns);
    const { menu } = useContextMenu(resources, records, setEditDialogVisible, setConfirmDeleteVisible);

    const cellDataEditor = (cells, record) => {
      return (
        <FieldEditor
          cells={cells}
          colsSchema={colsSchema}
          onEditorKeyChange={onEditorKeyChange}
          onEditorSubmitValue={onEditorSubmitValue}
          onEditorValueChange={onEditorValueChange}
          onEditorValueFocus={onEditorValueFocus}
          record={record}
        />
      );
    };

    const actionTemplate = () => (
      <ActionsColumn
        onDeleteClick={() => setConfirmDeleteVisible(true)}
        onEditClick={() => setEditDialogVisible(true)}
      />
    );

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

    const { columns, setColumns, originalColumns, selectedHeader } = useSetColumns(
      actionTemplate,
      cellDataEditor,
      colsSchema,
      columnOptions,
      hasWritePermissions,
      initialCellValue,
      isDataCollection,
      isWebFormMMR,
      records,
      resources,
      setIsColumnInfoVisible,
      validationsTemplate
    );

    // useEffect(() => {
    //   let inmLevelErrorTypesWithCorrects = [...levelErrorTypesWithCorrects];
    //   inmLevelErrorTypesWithCorrects = inmLevelErrorTypesWithCorrects.concat(levelErrorTypes);
    //   setLevelErrorTypesWithCorrects(inmLevelErrorTypesWithCorrects);
    // }, [levelErrorTypes]);

    useEffect(() => {
      setLevelErrorValidations(levelErrorTypesWithCorrects);
    }, [levelErrorTypesWithCorrects]);

    useEffect(() => {
      setRecordErrorPositionId(recordPositionId);
    }, [recordPositionId]);

    useEffect(() => {
      if (isValidationSelected) {
        setIsFilterValidationsActive(false);
        setLevelErrorValidations(levelErrorTypesWithCorrects);
        setIsValidationSelected(false);
      }
    }, [isValidationSelected]);

    useEffect(() => {
      if (records.isRecordDeleted) {
        onRefresh();
        setConfirmDeleteVisible(false);
      }
    }, [records.isRecordDeleted]);

    useEffect(() => {
      console.log('');
      if (isDatasetDeleted) {
        dispatchRecords({ type: 'IS_ALL_DATA_DELETED', payload: true });
      }
    }, [isDatasetDeleted]);

    useEffect(() => {
      dispatchRecords({ type: 'IS_RECORD_DELETED', payload: false });
    }, [confirmDeleteVisible]);

    const onFetchData = async (sField, sOrder, fRow, nRows, levelErrorValidations) => {
      const removeSelectAllFromList = levelErrorValidations => {
        levelErrorValidations = levelErrorValidations
          .map(error => error.toUpperCase())
          .filter(error => error !== 'SELECTALL')
          .join(',');
        return levelErrorValidations;
      };

      const filterDataResponse = data => {
        const dataFiltered = DataViewerUtils.parseData(data);
        if (dataFiltered.length > 0) {
          dispatchRecords({ type: 'FIRST_FILTERED_RECORD', payload: dataFiltered[0] });
        } else {
          setFetchedData([]);
        }
        setFetchedData(dataFiltered);
      };
      levelErrorValidations = removeSelectAllFromList(levelErrorValidations);

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
        console.error({ error });
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

    useRecordErrorPosition(
      recordErrorPositionId,
      dispatchRecords,
      records,
      dispatchSort,
      onFetchData,
      levelErrorTypesWithCorrects
    );

    useEffect(() => {
      if (recordErrorPositionId === -1) {
        onFetchData(sort.sortField, sort.sortOrder, 0, records.recordsPerPage, levelErrorValidations);
      }
    }, [levelErrorValidations]);

    useEffect(() => {
      if (confirmPasteVisible && !isUndefined(records.pastedRecords) && records.pastedRecords.length > 0) {
        dispatchRecords({ type: 'EMPTY_PASTED_RECORDS', payload: [] });
      }
    }, [confirmPasteVisible]);

    const showValidationFilter = filteredKeys => {
      // length of errors in data schema rules of validation
      const filteredKeysWithoutSelectAll = filteredKeys.filter(key => key !== 'selectAll');

      setIsFilterValidationsActive(filteredKeysWithoutSelectAll.length !== levelErrorTypesWithCorrects.length);
      dispatchRecords({ type: 'SET_FIRST_PAGE_RECORD', payload: 0 });
      setLevelErrorValidations(filteredKeysWithoutSelectAll);

      if (recordErrorPositionId !== -1) {
        setRecordErrorPositionId(-1);
      }
    };

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
        setIsTableDeleted(true);
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
        setIsPasting(true);
        const recordsAdded = await DatasetService.addRecordsById(datasetId, tableId, records.pastedRecords);
        if (!recordsAdded) {
          throw new Error('ADD_RECORDS_BY_ID_ERROR');
        } else {
          onRefresh();
          setIsPasting(false);
          setIsTableDeleted(false);
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
        setIsPasting(false);
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
          setIsSaving(true);
          await DatasetService.addRecordsById(datasetId, tableId, [record]);
          snapshotContext.snapshotDispatch({ type: 'clear_restored', payload: {} });
          setIsTableDeleted(false);
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
          setIsSaving(false);
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
          setIsSaving(false);
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

    const addRowDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button
          disabled={isSaving}
          label={resources.messages['save']}
          icon={!isSaving ? 'save' : 'spinnerAnimate'}
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

    const columnInfoDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button
          label={resources.messages['ok']}
          icon="check"
          onClick={() => {
            setIsColumnInfoVisible(false);
          }}
        />
      </div>
    );

    const editRowDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button
          label={resources.messages['save']}
          icon={isSaving === true ? 'spinnerAnimate' : 'save'}
          onClick={() => {
            try {
              onSaveRecord(records.editedRecord);
            } catch (error) {
              console.error(error);
            }
          }}
        />
        <Button label={resources.messages['cancel']} icon={'cancel'} onClick={onCancelRowEdit} />
      </div>
    );

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

    const requiredTemplate = rowData => {
      return (
        <div style={{ display: 'flex', justifyContent: 'center' }}>
          {rowData.field === 'Required' ? (
            <FontAwesomeIcon
              icon={AwesomeIcons('check')}
              style={{ float: 'center', color: 'var(--treeview-table-icon-color)' }}
            />
          ) : rowData.field === 'Codelist items' ? (
            <Chips disabled={true} value={rowData.value}></Chips>
          ) : (
            rowData.value
          )}
        </div>
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
    const onKeyPress = event => {
      if (event.key === 'Enter' && !isSaving) {
        event.preventDefault();
        onSaveRecord(records.newRecord);
      }
    };

    return (
      <SnapshotContext.Provider>
        <ActionsToolbar
          colsSchema={colsSchema}
          dataflowId={dataflowId}
          datasetId={datasetId}
          hasWritePermissions={hasWritePermissions}
          isFilterValidationsActive={isFilterValidationsActive}
          isTableDeleted={isTableDeleted}
          isLoading={isLoading}
          isValidationSelected={isValidationSelected}
          isWebFormMMR={isWebFormMMR}
          levelErrorTypesWithCorrects={levelErrorTypesWithCorrects}
          onRefresh={onRefresh}
          onSetVisible={onSetVisible}
          originalColumns={originalColumns}
          records={records}
          setColumns={setColumns}
          setDeleteDialogVisible={setDeleteDialogVisible}
          setImportDialogVisible={setImportDialogVisible}
          setRecordErrorPositionId={setRecordErrorPositionId}
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

        {isColumnInfoVisible && (
          <Dialog
            className={styles.Dialog}
            dismissableMask={false}
            footer={columnInfoDialogFooter}
            header={resources.messages['columnInfo']}
            onHide={() => setIsColumnInfoVisible(false)}
            visible={isColumnInfoVisible}>
            <DataTable
              autoLayout={true}
              className={styles.itemTable}
              value={DataViewerUtils.getFieldValues(colsSchema, selectedHeader, [
                'header',
                'description',
                'type',
                ...(!isNull(DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader).codelistItems)
                  ? ['codelistItems']
                  : [])
              ])}>
              {['field', 'value'].map((column, i) => (
                <Column body={column === 'value' ? requiredTemplate : null} field={column} header={''} key={i} />
              ))}
            </DataTable>
          </Dialog>
        )}

        {importDialogVisible && (
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
        )}

        {addDialogVisible && (
          <div onKeyPress={onKeyPress}>
            <Dialog
              className="edit-table"
              blockScroll={false}
              footer={addRowDialogFooter}
              header={resources.messages['addRecord']}
              modal={true}
              onHide={() => setAddDialogVisible(false)}
              style={{ width: '50%' }}
              visible={addDialogVisible}
              zIndex={3003}>
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
          </div>
        )}

        {editDialogVisible && (
          <Dialog
            blockScroll={false}
            className="edit-table"
            closeOnEscape={false}
            footer={editRowDialogFooter}
            header={resources.messages['editRow']}
            modal={true}
            onHide={() => setEditDialogVisible(false)}
            style={{ width: '50%' }}
            visible={editDialogVisible}
            zIndex={3003}>
            <div className="p-grid p-fluid">
              <DataForm
                colsSchema={colsSchema}
                editDialogVisible={editDialogVisible}
                formType="EDIT"
                onChangeForm={onEditAddFormInput}
                records={records}
              />
            </div>
          </Dialog>
        )}

        {deleteDialogVisible && (
          <ConfirmDialog
            classNameConfirm={'p-button-danger'}
            header={`${resources.messages['deleteDatasetTableHeader']} (${tableName})`}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            onConfirm={onConfirmDeleteTable}
            onHide={() => onSetVisible(setDeleteDialogVisible, false)}
            visible={deleteDialogVisible}>
            {resources.messages['deleteDatasetTableConfirm']}
          </ConfirmDialog>
        )}

        {confirmDeleteVisible && (
          <ConfirmDialog
            classNameConfirm={'p-button-danger'}
            header={resources.messages['deleteRow']}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            onConfirm={onConfirmDeleteRow}
            onHide={() => setConfirmDeleteVisible(false)}
            visible={confirmDeleteVisible}>
            {resources.messages['confirmDeleteRow']}
          </ConfirmDialog>
        )}

        {confirmPasteVisible && (
          <ConfirmDialog
            className="edit-table"
            divRef={divRef}
            header={resources.messages['pasteRecords']}
            hasPasteOption={true}
            isPasting={isPasting}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            onConfirm={onPasteAccept}
            onHide={onPasteCancel}
            onPaste={onPaste}
            onPasteAsync={onPasteAsync}
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
              isPasting={isPasting}
              numCopiedRecords={records.numCopiedRecords}
              onDeletePastedRecord={onDeletePastedRecord}
            />
          </ConfirmDialog>
        )}
      </SnapshotContext.Provider>
    );
  }
);

export { DataViewer };
