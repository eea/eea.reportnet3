/* eslint-disable react-hooks/exhaustive-deps */
import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import { withRouter } from 'react-router-dom';

import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { config } from 'conf';
import { DatasetConfig } from 'conf/domain/model/Dataset';

import styles from './DataViewer.module.scss';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { ActionsToolbar } from './_components/ActionsToolbar';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox';
import { Chips } from 'ui/views/_components/Chips';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { ContextMenu } from 'ui/views/_components/ContextMenu';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { DataForm } from './_components/DataForm';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { FieldEditor } from './_components/FieldEditor';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Footer } from './_components/Footer';
import { IconTooltip } from 'ui/views/_components/IconTooltip';
import { InfoTable } from './_components/InfoTable';
import { Map } from 'ui/views/_components/Map';

import { DatasetService } from 'core/services/Dataset';
import { IntegrationService } from 'core/services/Integration';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { recordReducer } from './_functions/Reducers/recordReducer';
import { sortReducer } from './_functions/Reducers/sortReducer';

import { DataViewerUtils } from './_functions/Utils/DataViewerUtils';
import { ExtensionUtils, MetadataUtils, RecordUtils } from 'ui/views/_functions/Utils';
import { getUrl, TextUtils } from 'core/infrastructure/CoreUtils';
import {
  useContextMenu,
  useLoadColsSchemasAndColumnOptions,
  useRecordErrorPosition,
  useSetColumns
} from './_functions/Hooks/DataViewerHooks';
import { MapUtils } from 'ui/views/_functions/Utils/MapUtils';

const DataViewer = withRouter(
  ({
    filterRuleId,
    hasCountryCode,
    hasWritePermissions,
    isDatasetDeleted = false,
    isExportable,
    isGroupedValidationSelected,
    isValidationSelected,
    match: {
      params: { datasetId, dataflowId }
    },
    onLoadTableData,
    recordPositionId,
    reporting,
    selectedRecordErrorId,
    selectedRuleId,
    setIsValidationSelected,
    showWriteButtons,
    tableFixedNumber,
    tableHasErrors,
    tableId,
    tableName,
    tableReadOnly,
    tableSchemaColumns
  }) => {
    const userContext = useContext(UserContext);

    const [addAnotherOne, setAddAnotherOne] = useState(false);
    const [addDialogVisible, setAddDialogVisible] = useState(false);
    const [isAttachFileVisible, setIsAttachFileVisible] = useState(false);
    const [isDeleteAttachmentVisible, setIsDeleteAttachmentVisible] = useState(false);
    const [confirmDeleteVisible, setConfirmDeleteVisible] = useState(false);
    const [confirmPasteVisible, setConfirmPasteVisible] = useState(false);
    const [datasetSchemaId, setDatasetSchemaId] = useState(null);
    const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
    const [editDialogVisible, setEditDialogVisible] = useState(false);
    const [extensionsOperationsList, setExtensionsOperationsList] = useState({ export: [], import: [] });
    const [fetchedData, setFetchedData] = useState([]);
    const [importTableDialogVisible, setImportTableDialogVisible] = useState(false);
    const [initialCellValue, setInitialCellValue] = useState();
    const [isColumnInfoVisible, setIsColumnInfoVisible] = useState(false);
    const [isDataUpdated, setIsDataUpdated] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [isFilterValidationsActive, setIsFilterValidationsActive] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [isNewRecord, setIsNewRecord] = useState(false);
    const [isPasting, setIsPasting] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [isTableDeleted, setIsTableDeleted] = useState(false);
    const [isValidationShown, setIsValidationShown] = useState(false);
    const [levelErrorTypesWithCorrects, setLevelErrorTypesWithCorrects] = useState([
      'CORRECT',
      'INFO',
      'WARNING',
      'ERROR',
      'BLOCKER'
    ]);
    const [levelErrorValidations, setLevelErrorValidations] = useState([]);
    const [recordErrorPositionId, setRecordErrorPositionId] = useState(recordPositionId);

    const [records, dispatchRecords] = useReducer(recordReducer, {
      crs: 'EPSG:4326',
      editedRecord: {},
      fetchedDataFirstRecord: [],
      firstPageRecord: 0,
      initialRecordValue: undefined,
      isAllDataDeleted: isDatasetDeleted,
      isMapOpen: false,
      isRecordAdded: false,
      isRecordDeleted: false,
      mapGeoJson: '',
      newPoint: '',
      newPointCRS: 'EPSG:4326',
      newRecord: {},
      numCopiedRecords: undefined,
      pastedRecords: undefined,
      recordsPerPage: userContext.userProps.rowsPerPage,
      selectedFieldId: '',
      selectedFieldSchemaId: '',
      selectedMapCells: {},
      selectedMaxSize: '',
      selectedRecord: {},
      selectedValidExtensions: [],
      totalFilteredRecords: 0,
      totalRecords: 0
    });

    const [sort, dispatchSort] = useReducer(sortReducer, {
      sortField: undefined,
      sortOrder: undefined
    });

    const notificationContext = useContext(NotificationContext);
    const resources = useContext(ResourcesContext);

    let contextMenuRef = useRef();
    let datatableRef = useRef();
    let divRef = useRef();

    const { colsSchema, columnOptions } = useLoadColsSchemasAndColumnOptions(tableSchemaColumns);
    const { menu } = useContextMenu(
      resources,
      records,
      RecordUtils.allAttachments(colsSchema),
      tableFixedNumber,
      setEditDialogVisible,
      setConfirmDeleteVisible
    );

    const cellDataEditor = (cells, record) => {
      return (
        <FieldEditor
          cells={cells}
          colsSchema={colsSchema}
          datasetId={datasetId}
          hasWritePermissions={hasWritePermissions}
          onChangePointCRS={onChangePointCRS}
          onEditorKeyChange={onEditorKeyChange}
          onEditorSubmitValue={onEditorSubmitValue}
          onEditorValueChange={onEditorValueChange}
          onEditorValueFocus={onEditorValueFocus}
          onMapOpen={onMapOpen}
          record={record}
          reporting={reporting}
        />
      );
    };

    const actionTemplate = () => (
      <ActionsColumn
        hideDeletion={tableFixedNumber}
        hideEdition={RecordUtils.allAttachments(colsSchema)}
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

    const onChangePointCRS = crs => dispatchRecords({ type: 'SET_MAP_CRS', payload: crs });

    const onFileDownload = async (fileName, fieldId) => {
      const fileContent = await DatasetService.downloadFileData(datasetId, fieldId);

      DownloadFile(fileContent, fileName);
    };

    const onFileUploadVisible = (fieldId, fieldSchemaId, validExtensions, maxSize) => {
      dispatchRecords({ type: 'SET_FIELD_IDS', payload: { fieldId, fieldSchemaId, validExtensions, maxSize } });
    };

    const onFileDeleteVisible = (fieldId, fieldSchemaId) => {
      dispatchRecords({ type: 'SET_FIELD_IDS', payload: { fieldId, fieldSchemaId } });
      setIsDeleteAttachmentVisible(true);
    };

    const { columns, getTooltipMessage, onShowFieldInfo, originalColumns, selectedHeader, setColumns } = useSetColumns(
      actionTemplate,
      cellDataEditor,
      colsSchema,
      columnOptions,
      hasCountryCode,
      hasWritePermissions && !tableReadOnly,
      initialCellValue,
      onFileDeleteVisible,
      onFileDownload,
      onFileUploadVisible,
      records,
      resources,
      setIsAttachFileVisible,
      setIsColumnInfoVisible,
      validationsTemplate,
      reporting
    );

    // useEffect(() => {
    //   let inmLevelErrorTypesWithCorrects = [...levelErrorTypesWithCorrects];
    //   inmLevelErrorTypesWithCorrects = inmLevelErrorTypesWithCorrects.concat(levelErrorTypes);
    //   setLevelErrorTypesWithCorrects(inmLevelErrorTypesWithCorrects);
    // }, [levelErrorTypes]);

    useEffect(() => {
      if (!addDialogVisible) setAddAnotherOne(false);
    }, [addDialogVisible]);

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
      if (records.isMapOpen) datatableRef.current.closeEditingCell();
    }, [records.isMapOpen]);

    useEffect(() => {
      if (isDatasetDeleted) {
        dispatchRecords({ type: 'IS_ALL_DATA_DELETED', payload: true });
      }
    }, [isDatasetDeleted]);

    useEffect(() => {
      dispatchRecords({ type: 'IS_RECORD_DELETED', payload: false });
    }, [confirmDeleteVisible]);

    useEffect(() => {
      getMetadata();
    }, []);

    useEffect(() => {
      if (records.mapGeoJson !== '') {
        onEditorValueChange(records.selectedMapCells, records.mapGeoJson);
        const inmMapGeoJson = cloneDeep(records.mapGeoJson);
        const parsedInmMapGeoJson = typeof inmMapGeoJson === 'object' ? inmMapGeoJson : JSON.parse(inmMapGeoJson);
        onEditorSubmitValue(records.selectedMapCells, JSON.stringify(parsedInmMapGeoJson), records.selectedRecord);
      }
    }, [records.mapGeoJson]);

    useEffect(() => {
      if (datasetSchemaId) getFileExtensions();
    }, [datasetSchemaId, isDataUpdated, importTableDialogVisible]);

    const getMetadata = async () => {
      try {
        const metadata = await MetadataUtils.getDatasetMetadata(datasetId);
        setDatasetSchemaId(metadata.datasetSchemaId);
      } catch (error) {
        notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId, datasetId } });
      }
    };

    const getFileExtensions = async () => {
      try {
        const response = await IntegrationService.allExtensionsOperations(dataflowId, datasetSchemaId);
        setExtensionsOperationsList(ExtensionUtils.groupOperations('operation', response));
      } catch (error) {
        notificationContext.add({ type: 'LOADING_FILE_EXTENSIONS_ERROR' });
      }
    };

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
          levelErrorValidations,
          filterRuleId
        );
        if (!isEmpty(tableData.records) && !isUndefined(onLoadTableData)) onLoadTableData(true);
        if (!isUndefined(colsSchema) && !isEmpty(colsSchema) && !isUndefined(tableData)) {
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
      filterRuleId,
      recordErrorPositionId,
      dispatchRecords,
      records,
      dispatchSort,
      onFetchData,
      levelErrorTypesWithCorrects
    );

    useEffect(() => {
      if (recordErrorPositionId === -1) {
        if (!isValidationShown && levelErrorValidations.length > 0) {
          onFetchData(sort.sortField, sort.sortOrder, 0, records.recordsPerPage, levelErrorValidations);
        } else {
          if (isValidationShown) {
            onFetchData(sort.sortField, sort.sortOrder, 0, records.recordsPerPage, levelErrorValidations);
          }
        }
      }
    }, [levelErrorValidations]);

    useEffect(() => {
      if (confirmPasteVisible && !isUndefined(records.pastedRecords) && records.pastedRecords.length > 0) {
        dispatchRecords({ type: 'EMPTY_PASTED_RECORDS', payload: [] });
      }
    }, [confirmPasteVisible]);

    const parseMultiselect = record => {
      record.dataRow.forEach(field => {
        if (
          field.fieldData.type === 'MULTISELECT_CODELIST' ||
          (field.fieldData.type === 'LINK' && Array.isArray(field.fieldData[field.fieldData.fieldSchemaId]))
        ) {
          if (
            !isNil(field.fieldData[field.fieldData.fieldSchemaId]) &&
            field.fieldData[field.fieldData.fieldSchemaId] !== ''
          ) {
            if (Array.isArray(field.fieldData[field.fieldData.fieldSchemaId])) {
              field.fieldData[field.fieldData.fieldSchemaId] = field.fieldData[field.fieldData.fieldSchemaId].join(',');
            } else {
              field.fieldData[field.fieldData.fieldSchemaId] = field.fieldData[field.fieldData.fieldSchemaId]
                .split(',')
                .map(item => item.trim())
                .join(',');
            }
          }
        }
      });
      return record;
    };

    const hideValidationFilter = () => {
      setIsValidationShown(false);
    };

    const showValidationFilter = filteredKeys => {
      // length of errors in data schema rules of validation
      const filteredKeysWithoutSelectAll = filteredKeys.filter(key => key !== 'selectAll');

      setIsFilterValidationsActive(filteredKeysWithoutSelectAll.length !== levelErrorTypesWithCorrects.length);
      dispatchRecords({ type: 'SET_FIRST_PAGE_RECORD', payload: 0 });
      setLevelErrorValidations(filteredKeysWithoutSelectAll);

      if (recordErrorPositionId !== -1) {
        setRecordErrorPositionId(-1);
      }
      setIsValidationShown(true);
    };

    const onAttach = async value => {
      RecordUtils.changeRecordValue(records.selectedRecord, records.selectedFieldSchemaId, `${value.files[0].name}`);
      setIsAttachFileVisible(false);
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

    const onConfirmDeleteAttachment = async () => {
      const fileDeleted = await DatasetService.deleteFileData(datasetId, records.selectedFieldId);
      if (fileDeleted) {
        RecordUtils.changeRecordValue(records.selectedRecord, records.selectedFieldSchemaId, '');
        setIsDeleteAttachmentVisible(false);
      }
    };

    const onConfirmDeleteRow = async () => {
      try {
        await DatasetService.deleteRecordById(datasetId, records.selectedRecord.recordId);

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

    const onDeletePastedRecord = recordIndex =>
      dispatchRecords({ type: 'DELETE_PASTED_RECORDS', payload: { recordIndex } });

    const onEditAddFormInput = (property, value) => {
      dispatchRecords({ type: !isNewRecord ? 'SET_EDITED_RECORD' : 'SET_NEW_RECORD', payload: { property, value } });
    };

    //When pressing "Escape" cell data resets to initial value
    //on "Enter" and "Tab" the value submits
    const onEditorKeyChange = (props, event, record, isGeometry = false, geoJson = '') => {
      if (event.key === 'Escape') {
        let updatedData = RecordUtils.changeCellValue([...props.value], props.rowIndex, props.field, initialCellValue);
        datatableRef.current.closeEditingCell();
        setFetchedData(updatedData);
      } else if (event.key === 'Enter') {
        if (!isGeometry) {
          onEditorSubmitValue(props, event.target.value, record);
        } else {
          onEditorSubmitValue(props, geoJson, record);
        }
      } else if (event.key === 'Tab') {
        event.preventDefault();
        if (!isGeometry) {
          onEditorSubmitValue(props, event.target.value, record);
        } else {
          onEditorSubmitValue(props, geoJson, record);
        }
      }
    };

    const onEditorSubmitValue = async (cell, value, record) => {
      if (!isEmpty(record)) {
        let field = record.dataRow.filter(row => Object.keys(row.fieldData)[0] === cell.field)[0].fieldData;
        if (value !== initialCellValue && record.recordId === records.selectedRecord.recordId) {
          try {
            //without await. We don't have to wait for the response.
            const fieldUpdated = DatasetService.updateFieldById(
              datasetId,
              cell.field,
              field.id,
              field.type,
              field.type === 'MULTISELECT_CODELIST' || (field.type === 'LINK' && Array.isArray(value))
                ? value.join(',')
                : value
            );
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
      setInitialCellValue(value);
      if (!isEditing) {
        setIsEditing(true);
      }
    };

    const onMapOpen = (coordinates, mapCells) =>
      dispatchRecords({ type: 'OPEN_MAP', payload: { coordinates, mapCells } });

    const onPaste = event => {
      if (event) {
        const clipboardData = event.clipboardData;
        const pastedData = clipboardData.getData('Text');
        dispatchRecords({ type: 'COPY_RECORDS', payload: { pastedData, colsSchema, reporting } });
      }
    };

    const onPasteAsync = async () => {
      const pastedData = await navigator.clipboard.readText();
      dispatchRecords({ type: 'COPY_RECORDS', payload: { pastedData, colsSchema, reporting } });
    };

    const onPasteAccept = async () => {
      try {
        setIsPasting(true);
        const recordsAdded = await DatasetService.addRecordsById(
          datasetId,
          tableId,
          MapUtils.parseGeometryData(records.pastedRecords)
        );
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
          await DatasetService.addRecordsById(datasetId, tableId, [parseMultiselect(record)]);
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
          if (!addAnotherOne) {
            setAddDialogVisible(false);
          }
          setIsLoading(false);
          setIsSaving(false);
        }
      } else {
        try {
          await DatasetService.updateRecordsById(datasetId, parseMultiselect(record));
          onRefresh();
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

    const onSavePoint = coordinates => {
      if (coordinates !== '') {
        dispatchRecords({ type: 'SAVE_MAP_COORDINATES', payload: coordinates });
      } else {
        dispatchRecords({ type: 'TOGGLE_MAP_VISIBILITY', payload: false });
      }
    };

    const onSelectPoint = (coordinates, crs) =>
      dispatchRecords({ type: 'SET_MAP_NEW_POINT', payload: { coordinates, crs } });

    const onSetVisible = (fnUseState, visible) => {
      fnUseState(visible);
    };

    const onSort = event => {
      dispatchSort({ type: 'SORT_TABLE', payload: { order: event.sortOrder, field: event.sortField } });
      dispatchRecords({ type: 'SET_FIRST_PAGE_RECORD', payload: 0 });
      onFetchData(event.sortField, event.sortOrder, 0, records.recordsPerPage, levelErrorValidations);
    };

    const onUpdateData = () => setIsDataUpdated(!isDataUpdated);

    const onUpload = async () => {
      setImportTableDialogVisible(false);
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
      setIsTableDeleted(false);
    };

    const addRowDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        {isNewRecord && (
          <div className={styles.addAnotherOneWrapper}>
            <Checkbox
              id={`addAnother`}
              isChecked={addAnotherOne}
              onChange={() => setAddAnotherOne(!addAnotherOne)}
              role="checkbox"
            />
            <span className={styles.addAnotherOne} onClick={() => setAddAnotherOne(!addAnotherOne)}>
              {resources.messages['addAnotherOne']}
            </span>
          </div>
        )}
        <Button
          className="p-button-animated-blink"
          disabled={isSaving}
          label={resources.messages['save']}
          icon={!isSaving ? 'check' : 'spinnerAnimate'}
          onClick={() => {
            onSaveRecord(records.newRecord);
          }}
        />
        <Button
          className="p-button-secondary"
          icon="cancel"
          label={resources.messages['cancel']}
          onClick={() => {
            dispatchRecords({
              type: 'SET_NEW_RECORD',
              payload: RecordUtils.createEmptyObject(colsSchema, undefined)
            });
            setAddDialogVisible(false);
          }}
        />
      </div>
    );

    const columnInfoDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button
          icon="check"
          label={resources.messages['ok']}
          onClick={() => {
            setIsColumnInfoVisible(false);
          }}
        />
      </div>
    );

    const editRowDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button
          className="p-button-animated-blink"
          icon={isSaving === true ? 'spinnerAnimate' : 'check'}
          label={resources.messages['save']}
          onClick={() => {
            try {
              onSaveRecord(records.editedRecord);
            } catch (error) {
              console.error(error);
            }
          }}
        />
        <Button
          className="p-button-secondary p-button-animated-blink"
          icon={'cancel'}
          label={resources.messages['cancel']}
          onClick={onCancelRowEdit}
        />
      </div>
    );

    const saveMapGeoJsonDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button
          className={`p-button-animated-blink ${styles.saveButton}`}
          // disabled={isSaving}
          label={resources.messages['save']}
          icon={'check'}
          onClick={() => onSavePoint(records.newPoint)}
        />
        <Button
          className="p-button-secondary"
          icon="cancel"
          label={resources.messages['cancel']}
          onClick={() => {
            // dispatchRecords({
            //   type: 'SET_NEW_RECORD',
            //   payload: RecordUtils.createEmptyObject(colsSchema, undefined)
            // });
            dispatchRecords({ type: 'CANCEL_SAVE_MAP_NEW_POINT', payload: {} });
          }}
        />
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

    const mapRender = () => (
      <Map hasLegend={true} geoJson={records.mapGeoJson} onSelectPoint={onSelectPoint} selectedCRS={records.crs}></Map>
    );

    const rowClassName = rowData => {
      let id = rowData.dataRow.filter(record => Object.keys(record.fieldData)[0] === 'id')[0].fieldData.id;
      return {
        'p-highlight': id === selectedRecordErrorId,
        'p-highlight-contextmenu': ''
      };
    };

    const requiredTemplate = rowData => {
      return (
        <div style={{ display: 'flex', justifyContent: 'center' }}>
          {rowData.field === 'Required' || rowData.field === 'Read only' ? (
            <FontAwesomeIcon
              icon={AwesomeIcons('check')}
              style={{ float: 'center', color: 'var(--treeview-table-icon-color)' }}
            />
          ) : rowData.field === 'Single select items' ||
            rowData.field === 'Multiple select items' ||
            rowData.field === 'Valid extensions' ? (
            <Chips disabled={true} value={rowData.value.split(',')} className={styles.chips}></Chips>
          ) : rowData.field === 'Maximum file size' ? (
            `${rowData.value} ${resources.messages['MB']}`
          ) : (
            rowData.value
          )}
        </div>
      );
    };

    const getPaginatorRecordsCount = () => (
      <Fragment>
        {isFilterValidationsActive && records.totalRecords !== records.totalFilteredRecords
          ? `${resources.messages['filtered']}: ${records.totalFilteredRecords} | `
          : ''}
        {resources.messages['totalRecords']} {!isUndefined(records.totalRecords) ? records.totalRecords : 0}{' '}
        {records.totalRecords === 1
          ? resources.messages['record'].toLowerCase()
          : resources.messages['records'].toLowerCase()}
        {isFilterValidationsActive && records.totalRecords === records.totalFilteredRecords
          ? `(${resources.messages['filtered'].toLowerCase()})`
          : ''}
      </Fragment>
    );

    const onKeyPress = event => {
      if (event.key === 'Enter' && !isSaving) {
        event.preventDefault();
        onSaveRecord(records.newRecord);
      }
    };
    const getAttachExtensions = [{ datasetSchemaId, fileExtension: records.selectedValidExtensions || [] }]
      .map(file => file.fileExtension.map(extension => (extension.indexOf('.') > -1 ? extension : `.${extension}`)))
      .flat()
      .join(', ');

    const infoAttachTooltip = `${resources.messages['supportedFileAttachmentsTooltip']} ${getAttachExtensions || '*'}
    ${resources.messages['supportedFileAttachmentsMaxSizeTooltip']} ${
      !isNil(records.selectedMaxSize) && records.selectedMaxSize.toString() !== '0'
        ? `${records.selectedMaxSize} ${resources.messages['MB']}`
        : resources.messages['maxSizeNotDefined']
    }`;

    const onImportTableError = async ({ xhr, files }) => {
      if (xhr.status === 423) {
        notificationContext.add({ type: 'FILE_UPLOAD_BLOCKED_ERROR' });
      }
    };

    return (
      <SnapshotContext.Provider>
        <ActionsToolbar
          colsSchema={colsSchema}
          dataflowId={dataflowId}
          datasetId={datasetId}
          fileExtensions={extensionsOperationsList.export}
          hasCountryCode={hasCountryCode}
          hasWritePermissions={hasWritePermissions && !tableFixedNumber && !tableReadOnly}
          hideValidationFilter={hideValidationFilter}
          isExportable={isExportable}
          isFilterValidationsActive={isFilterValidationsActive}
          isLoading={isLoading}
          isTableDeleted={isTableDeleted}
          isGroupedValidationSelected={isGroupedValidationSelected}
          isValidationSelected={isValidationSelected}
          levelErrorTypesWithCorrects={levelErrorTypesWithCorrects}
          onRefresh={onRefresh}
          onSetVisible={onSetVisible}
          onUpdateData={onUpdateData}
          originalColumns={originalColumns}
          records={records}
          setColumns={setColumns}
          setDeleteDialogVisible={setDeleteDialogVisible}
          setImportTableDialogVisible={setImportTableDialogVisible}
          setRecordErrorPositionId={setRecordErrorPositionId}
          showValidationFilter={showValidationFilter}
          showWriteButtons={showWriteButtons && !tableFixedNumber && !tableReadOnly}
          tableHasErrors={tableHasErrors}
          tableId={tableId}
          tableName={tableName}
        />
        <ContextMenu model={menu} ref={contextMenuRef} />
        <div className={styles.Table}>
          <DataTable
            // autoLayout={true}
            contextMenuSelection={records.selectedRecord}
            editable={hasWritePermissions && !tableReadOnly}
            id={tableId}
            first={records.firstPageRecord}
            footer={
              hasWritePermissions && !tableReadOnly && !tableFixedNumber ? (
                <Footer
                  hasWritePermissions={hasWritePermissions && !tableReadOnly}
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
              hasWritePermissions && !tableReadOnly && !isEditing
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
            footer={columnInfoDialogFooter}
            header={resources.messages['columnInfo']}
            onHide={() => setIsColumnInfoVisible(false)}
            style={{ minWidth: '40vw', maxWidth: '80vw', maxHeight: '80vh' }}
            visible={isColumnInfoVisible}>
            <DataTable
              autoLayout={true}
              className={styles.itemTable}
              value={DataViewerUtils.getFieldValues(colsSchema, selectedHeader, [
                'header',
                'description',
                'type',
                ...(!isNull(DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader).codelistItems) &&
                !isEmpty(DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader).codelistItems)
                  ? ['codelistItems']
                  : []),
                ...(!isNull(DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader).validExtensions) &&
                !isEmpty(DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader).validExtensions)
                  ? ['validExtensions']
                  : []),
                ...(!isNull(DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader).validExtensions) &&
                !isEmpty(DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader).validExtensions)
                  ? ['maxSize']
                  : []),
                !isNil(DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader)) &&
                DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader).readOnly
                  ? 'readOnly'
                  : ''
              ])}>
              {['field', 'value'].map((column, i) => (
                <Column
                  body={column === 'value' ? requiredTemplate : null}
                  className={column === 'field' ? styles.fieldColumn : ''}
                  field={column}
                  headerStyle={{ display: 'none' }}
                  key={i}
                />
              ))}
            </DataTable>
          </Dialog>
        )}

        {importTableDialogVisible && (
          <CustomFileUpload
            dialogClassName={styles.Dialog}
            dialogHeader={`${resources.messages['uploadTable']}${tableName}`}
            dialogOnHide={() => setImportTableDialogVisible(false)}
            dialogVisible={importTableDialogVisible}
            accept=".csv"
            chooseLabel={resources.messages['selectFile']} //allowTypes="/(\.|\/)(csv)$/"
            className={styles.FileUpload}
            isDialog={true}
            fileLimit={1}
            infoTooltip={`${resources.messages['supportedFileExtensionsTooltip']} .csv`}
            invalidExtensionMessage={resources.messages['invalidExtensionFile']}
            mode="advanced"
            multiple={false}
            name="file"
            onError={onImportTableError}
            onUpload={onUpload}
            replaceCheck={true}
            url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.importTableData, {
              datasetId: datasetId,
              tableId: tableId
            })}`}
          />
        )}

        {isAttachFileVisible && (
          <CustomFileUpload
            dialogClassName={styles.Dialog}
            dialogHeader={`${resources.messages['uploadAttachment']}`}
            dialogOnHide={() => setIsAttachFileVisible(false)}
            dialogVisible={isAttachFileVisible}
            accept={getAttachExtensions || '*'}
            chooseLabel={resources.messages['selectFile']}
            className={styles.FileUpload}
            fileLimit={1}
            isDialog={true}
            infoTooltip={infoAttachTooltip}
            mode="advanced"
            multiple={false}
            invalidExtensionMessage={resources.messages['invalidExtensionFile']}
            maxFileSize={
              !isNil(records.selectedMaxSize) && records.selectedMaxSize.toString() !== '0'
                ? records.selectedMaxSize * 1000 * 1024
                : 20 * 1000 * 1024
            }
            name="file"
            onUpload={onAttach}
            operation="PUT"
            url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.importFileData, {
              datasetId,
              fieldId: records.selectedFieldId
            })}`}
          />
        )}

        {addDialogVisible && (
          <div onKeyPress={onKeyPress}>
            <Dialog
              blockScroll={false}
              className={'edit-table calendar-table'}
              footer={addRowDialogFooter}
              header={resources.messages['addRecord']}
              modal={true}
              onHide={() => setAddDialogVisible(false)}
              style={{ width: '50%' }}
              visible={addDialogVisible}
              zIndex={3003}>
              <div className="p-grid p-fluid">
                <DataForm
                  addDialogVisible={addDialogVisible}
                  colsSchema={colsSchema}
                  datasetId={datasetId}
                  formType="NEW"
                  getTooltipMessage={getTooltipMessage}
                  hasWritePermissions={hasWritePermissions}
                  onChangeForm={onEditAddFormInput}
                  onShowFieldInfo={onShowFieldInfo}
                  records={records}
                  reporting={reporting}
                />
              </div>
            </Dialog>
          </div>
        )}

        {editDialogVisible && (
          <Dialog
            blockScroll={false}
            className="calendar-table"
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
                datasetId={datasetId}
                editDialogVisible={editDialogVisible}
                formType="EDIT"
                getTooltipMessage={getTooltipMessage}
                hasWritePermissions={hasWritePermissions}
                onChangeForm={onEditAddFormInput}
                onShowFieldInfo={onShowFieldInfo}
                records={records}
                reporting={reporting}
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

        {isDeleteAttachmentVisible && (
          <ConfirmDialog
            classNameConfirm={'p-button-danger'}
            header={`${resources.messages['deleteAttachmentHeader']}`}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            onConfirm={onConfirmDeleteAttachment}
            onHide={() => setIsDeleteAttachmentVisible(false)}
            visible={isDeleteAttachmentVisible}>
            {resources.messages['deleteAttachmentConfirm']}
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
            disabledConfirm={isEmpty(records.pastedRecords)}
            divRef={divRef}
            header={resources.messages['pasteRecords']}
            hasPasteOption={true}
            isPasting={isPasting}
            labelCancel={resources.messages['cancel']}
            labelConfirm={resources.messages['save']}
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
        {records.isMapOpen && (
          <Dialog
            className={'map-data'}
            blockScroll={false}
            footer={saveMapGeoJsonDialogFooter}
            header={resources.messages['geospatialData']}
            modal={true}
            onHide={() => dispatchRecords({ type: 'TOGGLE_MAP_VISIBILITY', payload: false })}
            visible={records.isMapOpen}>
            <div className="p-grid p-fluid">{mapRender()}</div>
          </Dialog>
        )}
      </SnapshotContext.Provider>
    );
  }
);

export { DataViewer };
