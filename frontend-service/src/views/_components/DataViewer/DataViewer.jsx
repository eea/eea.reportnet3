/* eslint-disable react-hooks/exhaustive-deps */
import { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import { withRouter } from 'react-router-dom';

import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { config } from 'conf';
import { DatasetConfig } from 'repositories/config/DatasetConfig';

import styles from './DataViewer.module.scss';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { ActionsToolbar } from './_components/ActionsToolbar';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { Checkbox } from 'views/_components/Checkbox';
import { Chips } from 'views/_components/Chips';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { ContextMenu } from 'views/_components/ContextMenu';
import { CustomFileUpload } from 'views/_components/CustomFileUpload';
import { DataForm } from './_components/DataForm';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { DownloadFile } from 'views/_components/DownloadFile';
import { FieldEditor } from './_components/FieldEditor';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Footer } from './_components/Footer';
import { InfoTable } from './_components/InfoTable';
import { Map } from 'views/_components/Map';

import { DatasetService } from 'services/DatasetService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'views/_functions/Contexts/SnapshotContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { recordReducer } from './_functions/Reducers/recordReducer';
import { sortReducer } from './_functions/Reducers/sortReducer';

import { useContextMenu, useLoadColsSchemasAndColumnOptions, useSetColumns } from './_functions/Hooks/DataViewerHooks';

import { DataViewerUtils } from './_functions/Utils/DataViewerUtils';
import { MetadataUtils, RecordUtils } from 'views/_functions/Utils';
import { MapUtils } from 'views/_functions/Utils/MapUtils';
import { ErrorUtils } from 'views/_functions/Utils/ErrorUtils';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

const DataViewer = withRouter(
  ({
    dataProviderId,
    datasetSchemaId,
    hasCountryCode,
    hasWritePermissions,
    isBusinessDataflow,
    isDataflowOpen,
    isDesignDatasetEditorRead,
    isExportable,
    isFilterable,
    isGroupedValidationDeleted,
    isGroupedValidationSelected,
    isReferenceDataset,
    isReportingWebform,
    match: {
      params: { datasetId, dataflowId }
    },
    onHideSelectGroupedValidation,
    onLoadTableData,
    reporting,
    selectedRuleId,
    selectedRuleLevelError,
    selectedRuleMessage,
    selectedTableSchemaId,
    showWriteButtons,
    tableFixedNumber,
    tableHasErrors,
    tableId,
    tableName,
    tableReadOnly,
    tableSchemaColumns
  }) => {
    const levelErrorAllTypes = ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER'];

    const userContext = useContext(UserContext);
    const [addAnotherOne, setAddAnotherOne] = useState(false);
    const [addDialogVisible, setAddDialogVisible] = useState(false);
    const [isAttachFileVisible, setIsAttachFileVisible] = useState(false);
    const [isDeleteAttachmentVisible, setIsDeleteAttachmentVisible] = useState(false);
    const [confirmDeleteVisible, setConfirmDeleteVisible] = useState(false);
    const [confirmPasteVisible, setConfirmPasteVisible] = useState(false);
    const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
    const [editDialogVisible, setEditDialogVisible] = useState(false);
    const [fetchedData, setFetchedData] = useState([]);
    const [hasWebformWritePermissions, setHasWebformWritePermissions] = useState(true);
    const [importTableDialogVisible, setImportTableDialogVisible] = useState(false);
    const [initialCellValue, setInitialCellValue] = useState();
    const [isColumnInfoVisible, setIsColumnInfoVisible] = useState(false);
    const [isDataUpdated, setIsDataUpdated] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [prevFilterValue, setPrevFilterValue] = useState('');
    const [isFilterValidationsActive, setIsFilterValidationsActive] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [isNewRecord, setIsNewRecord] = useState(false);
    const [isPasting, setIsPasting] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [levelErrorValidations, setLevelErrorValidations] = useState(levelErrorAllTypes);
    const [valueFilter, setValueFilter] = useState();

    const [records, dispatchRecords] = useReducer(recordReducer, {
      crs: 'EPSG:4326',
      drawElements: {
        circle: false,
        circlemarker: false,
        polyline: false,
        marker: false,
        point: false,
        polygon: false,
        rectangle: false
      },
      editedRecord: {},
      fetchedDataFirstRecord: [],
      firstPageRecord: 0,
      geometryType: '',
      initialRecordValue: undefined,
      isMapOpen: false,
      isRecordAdded: false,
      isRecordDeleted: false,
      isSaveDisabled: false,
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
    const resourcesContext = useContext(ResourcesContext);

    let contextMenuRef = useRef();
    let datatableRef = useRef();
    let divRef = useRef();

    const { areEquals, removeSemicolonSeparatedWhiteSpaces } = TextUtils;

    const { colsSchema, columnOptions } = useLoadColsSchemasAndColumnOptions(tableSchemaColumns);

    const { menu } = useContextMenu(
      resourcesContext,
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
          datasetSchemaId={datasetSchemaId}
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
        disabledButtons={isDataflowOpen || isDesignDatasetEditorRead}
        hideDeletion={tableFixedNumber}
        hideEdition={RecordUtils.allAttachments(colsSchema)}
        onDeleteClick={() => setConfirmDeleteVisible(true)}
        onEditClick={() => setEditDialogVisible(true)}
      />
    );

    const validationsTemplate = recordData => {
      return (
        <div className={styles.iconTooltipWrapper}>
          {ErrorUtils.getValidationsTemplate(recordData, {
            blockers: resourcesContext.messages['recordBlockers'],
            errors: resourcesContext.messages['recordErrors'],
            warnings: resourcesContext.messages['recordWarnings'],
            infos: resourcesContext.messages['recordInfos']
          })}
        </div>
      );
    };

    const onChangePointCRS = crs => dispatchRecords({ type: 'SET_MAP_CRS', payload: crs });

    const onFileDownload = async (fileName, fieldId) => {
      try {
        const { data } = await DatasetService.downloadFileData(dataflowId, datasetId, fieldId, dataProviderId);
        DownloadFile(data, fileName);
      } catch (error) {
        console.error('DataViewer - onFileDownload.', error);
      }
    };

    const onFileUploadVisible = (fieldId, fieldSchemaId, validExtensions, maxSize) => {
      dispatchRecords({ type: 'SET_FIELD_IDS', payload: { fieldId, fieldSchemaId, validExtensions, maxSize } });
    };

    const onFileDeleteVisible = (fieldId, fieldSchemaId) => {
      dispatchRecords({ type: 'SET_FIELD_IDS', payload: { fieldId, fieldSchemaId } });
      setIsDeleteAttachmentVisible(true);
    };

    const onShowCoordinateError = errorCount =>
      dispatchRecords({ type: 'DISABLE_SAVE_BUTTON', payload: { disable: errorCount > 0 } });

    const { columns, getTooltipMessage, onShowFieldInfo, originalColumns, selectedHeader, setColumns } = useSetColumns(
      actionTemplate,
      cellDataEditor,
      colsSchema,
      columnOptions,
      hasCountryCode,
      hasWebformWritePermissions,
      (hasWritePermissions && !tableReadOnly) || (hasWritePermissions && isReferenceDataset),
      initialCellValue,
      isBusinessDataflow,
      isDataflowOpen,
      isDesignDatasetEditorRead,
      onFileDeleteVisible,
      onFileDownload,
      onFileUploadVisible,
      records,
      resourcesContext,
      setIsAttachFileVisible,
      setIsColumnInfoVisible,
      validationsTemplate,
      reporting
    );

    useEffect(() => {
      if (isGroupedValidationSelected) {
        dispatchRecords({ type: 'SET_FIRST_PAGE_RECORD', payload: 0 });
      }
    }, [isGroupedValidationSelected]);

    useEffect(() => {
      if (!addDialogVisible) setAddAnotherOne(false);
    }, [addDialogVisible]);

    useEffect(() => {
      if (records.isRecordDeleted) {
        onRefresh();
        setConfirmDeleteVisible(false);
      }
    }, [records.isRecordDeleted]);

    useEffect(() => {
      if (records.isMapOpen) {
        datatableRef.current.closeEditingCell();
      }
    }, [records.isMapOpen]);

    useEffect(() => {
      dispatchRecords({ type: 'IS_RECORD_DELETED', payload: false });
    }, [confirmDeleteVisible]);

    useEffect(() => {
      if (records.mapGeoJson !== '' && areEquals(records.geometryType, 'POINT')) {
        onEditorValueChange(records.selectedMapCells, records.mapGeoJson);
        const inmMapGeoJson = cloneDeep(records.mapGeoJson);
        const parsedInmMapGeoJson = typeof inmMapGeoJson === 'object' ? inmMapGeoJson : JSON.parse(inmMapGeoJson);
        onEditorSubmitValue(records.selectedMapCells, JSON.stringify(parsedInmMapGeoJson), records.selectedRecord);
      }
    }, [records.mapGeoJson]);

    useEffect(() => {
      if (isReportingWebform) {
        setHasWebformWritePermissions(false);
      }
    }, [isReportingWebform]);

    const filterDataResponse = data => {
      const dataFiltered = DataViewerUtils.parseData(data);
      if (dataFiltered.length > 0) {
        dispatchRecords({ type: 'FIRST_FILTERED_RECORD', payload: dataFiltered[0] });
      } else {
        setFetchedData([]);
      }
      setFetchedData(dataFiltered);
    };

    const onFetchData = async (
      sField,
      sOrder,
      fRow,
      nRows,
      levelErrorValidationsItems,
      groupedRules,
      valueFilter = ''
    ) => {
      levelErrorValidationsItems = levelErrorValidationsItems
        .map(error => error.toUpperCase())
        .filter(error => error !== 'SELECTALL')
        .join(',');
      setIsLoading(true);
      try {
        let fields;
        if (!isUndefined(sField) && sField !== null) {
          fields = `${sField}:${sOrder}`;
        }
        const data = await DatasetService.getTableData({
          datasetId,
          tableSchemaId: tableId,
          pageNum: Math.floor(fRow / nRows),
          pageSize: nRows,
          fields,
          levelError: levelErrorValidationsItems,
          ruleId: tableId === selectedTableSchemaId ? groupedRules : undefined,
          value: valueFilter
        });

        if (!isEmpty(data.records) && !isUndefined(onLoadTableData)) onLoadTableData(true);
        if (!isUndefined(colsSchema) && !isEmpty(colsSchema) && !isUndefined(data)) {
          if (!isUndefined(data.records) && data.records.length > 0) {
            dispatchRecords({
              type: 'SET_NEW_RECORD',
              payload: RecordUtils.createEmptyObject(colsSchema, data.records[0])
            });
          } else {
            dispatchRecords({ type: 'SET_NEW_RECORD', payload: RecordUtils.createEmptyObject(colsSchema, undefined) });
          }
        }
        if (!isUndefined(data.records)) {
          filterDataResponse(data);
        } else {
          setFetchedData([]);
        }

        if (data.totalRecords !== records.totalRecords) {
          dispatchRecords({ type: 'SET_TOTAL', payload: data.totalRecords });
        }
        if (data.totalFilteredRecords !== records.totalFilteredRecords) {
          dispatchRecords({ type: 'SET_FILTERED', payload: data.totalFilteredRecords });
        }

        setIsLoading(false);
      } catch (error) {
        console.error('DataViewer - onFetchData.', error);
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
        notificationContext.add({
          type: 'TABLE_DATA_BY_ID_ERROR',
          content: { dataflowId, datasetId, dataflowName, datasetName }
        });
      } finally {
        setIsLoading(false);
      }
    };

    useEffect(() => {
      onFetchData(
        sort.sortField,
        sort.sortOrder,
        0,
        records.recordsPerPage,
        levelErrorValidations,
        selectedRuleId,
        valueFilter
      );
      if (!isNil(valueFilter)) {
        setPrevFilterValue(valueFilter);
      }
    }, [levelErrorValidations, valueFilter]);

    useEffect(() => {
      if (selectedRuleId !== '' || isGroupedValidationDeleted) {
        onFetchData(
          sort.sortField,
          sort.sortOrder,
          0,
          records.recordsPerPage,
          levelErrorValidations,
          selectedRuleId,
          valueFilter
        );
      }
    }, [selectedRuleId]);

    useEffect(() => {
      if (confirmPasteVisible && !isUndefined(records.pastedRecords) && records.pastedRecords.length > 0) {
        dispatchRecords({ type: 'EMPTY_PASTED_RECORDS', payload: [] });
      }
    }, [confirmPasteVisible]);

    const parseMultiselect = record => {
      record.dataRow.forEach(field => {
        if (
          field.fieldData.type === 'MULTISELECT_CODELIST' ||
          ((field.fieldData.type === 'LINK' || field.fieldData.type === 'EXTERNAL_LINK') &&
            Array.isArray(field.fieldData[field.fieldData.fieldSchemaId]))
        ) {
          if (
            !isNil(field.fieldData[field.fieldData.fieldSchemaId]) &&
            field.fieldData[field.fieldData.fieldSchemaId] !== ''
          ) {
            if (Array.isArray(field.fieldData[field.fieldData.fieldSchemaId])) {
              field.fieldData[field.fieldData.fieldSchemaId] = field.fieldData[field.fieldData.fieldSchemaId].join(';');
            } else {
              field.fieldData[field.fieldData.fieldSchemaId] = removeSemicolonSeparatedWhiteSpaces(
                field.fieldData[field.fieldData.fieldSchemaId]
              );
            }
          }
        }
      });
      return record;
    };

    const hasTextareas = () => {
      if (!isNil(records) && !isEmpty(records.newRecord) && !isEmpty(records.newRecord.dataRow)) {
        const filtered = records.newRecord.dataRow.filter(row => row.fieldData.type === 'TEXTAREA');
        return filtered.length > 0;
      }
      return false;
    };

    const showGroupedValidationFilter = groupedBy => {
      setIsFilterValidationsActive(groupedBy);
      dispatchRecords({ type: 'SET_FIRST_PAGE_RECORD', payload: 0 });
    };

    const showValueFilter = value => {
      setValueFilter(value);
      dispatchRecords({ type: 'SET_FIRST_PAGE_RECORD', payload: 0 });
    };

    const showValidationFilter = filteredKeys => {
      // length of errors in data schema rules of validation
      const filteredKeysWithoutSelectAll = filteredKeys.filter(key => key !== 'selectAll');

      setIsFilterValidationsActive(filteredKeysWithoutSelectAll.length !== levelErrorAllTypes.length);
      dispatchRecords({ type: 'SET_FIRST_PAGE_RECORD', payload: 0 });
      setLevelErrorValidations(filteredKeysWithoutSelectAll);
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
      dispatchRecords({ type: 'ON_CHANGE_PAGE', payload: event });
      onFetchData(
        sort.sortField,
        sort.sortOrder,
        event.first,
        event.rows,
        levelErrorValidations,
        selectedRuleId,
        valueFilter
      );
    };

    const onConditionalChange = field => {
      dispatchRecords({
        type: 'RESET_CONDITIONAL_FIELDS',
        payload: {
          field,
          isNewRecord,
          referencedFields: colsSchema.filter(
            col => !isNil(col.referencedField) && col.referencedField.masterConditionalFieldId === field
          )
        }
      });
    };

    const onConfirmDeleteTable = async () => {
      try {
        notificationContext.add({ type: 'DELETE_TABLE_DATA_INIT' });
        await DatasetService.deleteTableData(datasetId, tableId);
        setFetchedData([]);
        dispatchRecords({ type: 'SET_TOTAL', payload: 0 });
        dispatchRecords({ type: 'SET_FILTERED', payload: 0 });
      } catch (error) {
        if (error.response.status === 423) {
          notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
        } else {
          console.error('DataViewer - onConfirmDeleteTable.', error);
          const {
            dataflow: { name: dataflowName },
            dataset: { name: datasetName }
          } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
          notificationContext.add({
            type: 'DELETE_TABLE_DATA_BY_ID_ERROR',
            content: { dataflowId, datasetId, dataflowName, datasetName, tableName }
          });
        }
      } finally {
        setDeleteDialogVisible(false);
      }
    };

    const onConfirmDeleteAttachment = async () => {
      try {
        await DatasetService.deleteAttachment(datasetId, records.selectedFieldId);
        RecordUtils.changeRecordValue(records.selectedRecord, records.selectedFieldSchemaId, '');
        setIsDeleteAttachmentVisible(false);
      } catch (error) {
        console.error('DataViewer - onConfirmDeleteAttachment.', error);
      }
    };

    const onConfirmDeleteRow = async () => {
      try {
        await DatasetService.deleteRecord(datasetId, records.selectedRecord.recordId);
        const calcRecords = records.totalFilteredRecords >= 0 ? records.totalFilteredRecords : records.totalRecords;
        const page =
          (calcRecords - 1) / records.recordsPerPage === 1
            ? (Math.floor(records.firstPageRecord / records.recordsPerPage) - 1) * records.recordsPerPage
            : Math.floor(records.firstPageRecord / records.recordsPerPage) * records.recordsPerPage;
        dispatchRecords({ type: 'SET_FIRST_PAGE_RECORD', payload: page });
        dispatchRecords({ type: 'IS_RECORD_DELETED', payload: true });
      } catch (error) {
        if (error.response.status === 423) {
          notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
        } else {
          console.error('DataViewer - onConfirmDeleteRow.', error);
          const {
            dataflow: { name: dataflowName },
            dataset: { name: datasetName }
          } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
          notificationContext.add({
            type: 'DELETE_RECORD_BY_ID_ERROR',
            content: { dataflowId, datasetId, dataflowName, datasetName, tableName }
          });
        }
      } finally {
        setDeleteDialogVisible(false);
      }
    };

    const onDeletePastedRecord = recordIndex =>
      dispatchRecords({ type: 'DELETE_PASTED_RECORDS', payload: { recordIndex } });

    const onEditAddFormInput = (property, value) =>
      dispatchRecords({ type: !isNewRecord ? 'SET_EDITED_RECORD' : 'SET_NEW_RECORD', payload: { property, value } });

    //When pressing "Escape" cell data resets to initial value
    //on "Enter" and "Tab" the value submits
    const onEditorKeyChange = (props, event, record, isGeometry = false, geoJson = '', type = '') => {
      if (event.key === 'Escape') {
        let updatedData = RecordUtils.changeCellValue([...props.value], props.rowIndex, props.field, initialCellValue);
        datatableRef.current.closeEditingCell();
        setFetchedData(updatedData);
      } else if (event.key === 'Enter') {
        if (!isGeometry) {
          if (!areEquals(type, 'TEXTAREA')) {
            datatableRef.current.closeEditingCell();
            onEditorSubmitValue(props, event.target.value, record);
          }
        } else {
          datatableRef.current.closeEditingCell();
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
            await DatasetService.updateField(
              datasetId,
              cell.field,
              field.id,
              field.type,
              field.type === 'MULTISELECT_CODELIST' ||
                ((field.type === 'LINK' || field.type === 'EXTERNAL_LINK') && Array.isArray(value))
                ? value.join(';')
                : value
            );
          } catch (error) {
            if (error.response.status === 423) {
              notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
            } else {
              console.error('DataViewer - onEditorSubmitValue.', error);
              const {
                dataflow: { name: dataflowName },
                dataset: { name: datasetName }
              } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
              notificationContext.add({
                type: 'UPDATE_FIELD_BY_ID_ERROR',
                content: { dataflowId, datasetId, dataflowName, datasetName, tableName }
              });
            }
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

    const onMapOpen = (coordinates, mapCells, fieldType) =>
      dispatchRecords({ type: 'OPEN_MAP', payload: { coordinates, fieldType, mapCells } });

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
        const recordsAdded = await DatasetService.createRecord(
          datasetId,
          tableId,
          MapUtils.parseGeometryData(records.pastedRecords)
        );
        if (!recordsAdded) {
          throw new Error('ADD_RECORDS_PASTING_ERROR');
        } else {
          onRefresh();
          setIsPasting(false);
        }
      } catch (error) {
        if (error.response.status === 423) {
          notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
        } else {
          console.error('DataViewer - onPasteAccept.', error);
          const {
            dataflow: { name: dataflowName },
            dataset: { name: datasetName }
          } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
          notificationContext.add({
            type: 'ADD_RECORDS_PASTING_ERROR',
            content: {
              dataflowId,
              datasetId,
              dataflowName,
              datasetName,
              tableName
            }
          });
        }
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
        levelErrorValidations,
        selectedRuleId,
        valueFilter
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
      //Check invalid coordinates and replace them
      record = MapUtils.changeIncorrectCoordinates(record);
      if (isNewRecord) {
        try {
          setIsSaving(true);
          await DatasetService.createRecord(datasetId, tableId, [parseMultiselect(record)]);
          onRefresh();
        } catch (error) {
          if (error.response.status === 423) {
            notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
          } else {
            console.error('DataViewer - onSaveRecord - add.', error);
            const {
              dataflow: { name: dataflowName },
              dataset: { name: datasetName }
            } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
            notificationContext.add({
              type: 'ADD_RECORDS_BY_ID_ERROR',
              content: { dataflowId, datasetId, dataflowName, datasetName, tableName }
            });
          }
        } finally {
          if (!addAnotherOne) {
            setAddDialogVisible(false);
          }
          setIsLoading(false);
          setIsSaving(false);
        }
      } else {
        try {
          setIsSaving(true);
          await DatasetService.updateRecord(datasetId, parseMultiselect(record));
          onRefresh();
        } catch (error) {
          if (error.response.status === 423) {
            notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
          } else {
            console.error('DataViewer - onSaveRecord - update.', error);
            const {
              dataflow: { name: dataflowName },
              dataset: { name: datasetName }
            } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
            notificationContext.add({
              type: 'UPDATE_RECORDS_BY_ID_ERROR',
              content: { dataflowId, datasetId, dataflowName, datasetName, tableName }
            });
          }
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
      onFetchData(
        event.sortField,
        event.sortOrder,
        0,
        records.recordsPerPage,
        levelErrorValidations,
        selectedRuleId,
        valueFilter
      );
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
          datasetLoadingMessage: resourcesContext.messages['datasetLoadingMessage'],
          title: TextUtils.ellipsis(tableName, config.notifications.STRING_LENGTH_MAX),
          datasetLoading: resourcesContext.messages['datasetLoading'],
          dataflowName,
          datasetName
        }
      });
    };

    const addRowDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        {isNewRecord && (
          <div className={styles.addAnotherOneWrapper}>
            <Checkbox
              checked={addAnotherOne}
              id="addAnother"
              inputId="addAnother"
              onChange={() => setAddAnotherOne(!addAnotherOne)}
              role="checkbox"
            />
            <span className={styles.addAnotherOne} onClick={() => setAddAnotherOne(!addAnotherOne)}>
              {resourcesContext.messages['addAnotherOne']}
            </span>
          </div>
        )}
        <Button
          className={!isSaving && !records.isSaveDisabled && 'p-button-animated-blink'}
          disabled={isSaving || records.isSaveDisabled}
          icon={!isSaving ? 'check' : 'spinnerAnimate'}
          label={resourcesContext.messages['save']}
          onClick={() => onSaveRecord(records.newRecord)}
        />
        <Button
          className="p-button-secondary button-right-aligned p-button-animated-blink"
          icon="cancel"
          label={resourcesContext.messages['cancel']}
          onClick={() => {
            dispatchRecords({ type: 'SET_NEW_RECORD', payload: RecordUtils.createEmptyObject(colsSchema, undefined) });
            setAddDialogVisible(false);
          }}
        />
      </div>
    );

    const columnInfoDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button icon="check" label={resourcesContext.messages['ok']} onClick={() => setIsColumnInfoVisible(false)} />
      </div>
    );

    const editRowDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button
          className={!isSaving && !records.isSaveDisabled && 'p-button-animated-blink'}
          disabled={isSaving || records.isSaveDisabled}
          icon={isSaving === true ? 'spinnerAnimate' : 'check'}
          label={resourcesContext.messages['save']}
          onClick={() => {
            try {
              onSaveRecord(records.editedRecord);
            } catch (error) {
              console.error('DataViewer - editRowDialogFooter.', error);
            }
          }}
        />
        <Button
          className="p-button-secondary p-button-animated-blink p-button-right-aligned"
          icon={'cancel'}
          label={resourcesContext.messages['cancel']}
          onClick={onCancelRowEdit}
        />
      </div>
    );

    const saveMapGeoJsonDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button
          className={`p-button-animated-blink ${styles.saveButton}`}
          icon={'check'}
          label={
            areEquals(records.geometryType, 'POINT')
              ? resourcesContext.messages['save']
              : resourcesContext.messages['ok']
          }
          onClick={
            areEquals(records.geometryType, 'POINT')
              ? () => onSavePoint(records.newPoint)
              : () => dispatchRecords({ type: 'TOGGLE_MAP_VISIBILITY', payload: false })
          }
        />
        {areEquals(records.geometryType, 'POINT') && (
          <Button
            className="p-button-secondary button-right-aligned"
            icon="cancel"
            label={resourcesContext.messages['cancel']}
            onClick={() => {
              dispatchRecords({ type: 'CANCEL_SAVE_MAP_NEW_POINT', payload: {} });
            }}
          />
        )}
      </div>
    );

    const mapRender = () => (
      <Map
        enabledDrawElements={records.drawElements}
        geoJson={records.mapGeoJson}
        geometryType={records.geometryType}
        hasLegend={true}
        onSelectPoint={onSelectPoint}
        selectedCRS={records.crs}></Map>
    );

    const requiredTemplate = rowData => {
      return (
        <div className={styles.requiredTemplateWrapper}>
          {rowData.field === 'Required' || rowData.field === 'Read only' ? (
            <FontAwesomeIcon className={styles.requiredTemplateCheck} icon={AwesomeIcons('check')} />
          ) : rowData.field === 'Single select items' || rowData.field === 'Multiple select items' ? (
            <Chips
              className={styles.chips}
              disabled={true}
              name={resourcesContext.messages['multipleSingleMessage']}
              pasteSeparator=";"
              value={rowData.value.split(';')}></Chips>
          ) : rowData.field === 'Valid extensions' ? (
            <Chips
              className={styles.chips}
              disabled={true}
              name={resourcesContext.messages['validExtensionsShort']}
              value={rowData.value.split(',')}></Chips>
          ) : rowData.field === 'Maximum file size' ? (
            `${rowData.value} ${resourcesContext.messages['MB']}`
          ) : (
            rowData.value
          )}
        </div>
      );
    };

    const getPaginatorRecordsCount = () => (
      <Fragment>
        {(isGroupedValidationSelected || isFilterValidationsActive || (!isNil(valueFilter) && valueFilter !== '')) &&
        records.totalRecords !== records.totalFilteredRecords
          ? `${resourcesContext.messages['filtered']}: ${records.totalFilteredRecords} | `
          : ''}
        {resourcesContext.messages['totalRecords']} {!isUndefined(records.totalRecords) ? records.totalRecords : 0}{' '}
        {records.totalRecords === 1
          ? resourcesContext.messages['record'].toLowerCase()
          : resourcesContext.messages['records'].toLowerCase()}
        {(isGroupedValidationSelected || isFilterValidationsActive || (!isNil(valueFilter) && valueFilter !== '')) &&
        records.totalRecords === records.totalFilteredRecords
          ? ` (${resourcesContext.messages['filtered'].toLowerCase()})`
          : ''}
      </Fragment>
    );

    const onKeyPress = event => {
      if (event.key === 'Enter' && !isSaving && !records.isSaveDisabled) {
        event.preventDefault();
        onSaveRecord(records.newRecord);
      }
    };
    const getAttachExtensions = [{ datasetSchemaId, fileExtension: records.selectedValidExtensions || [] }]
      .map(file => file.fileExtension.map(extension => (extension.indexOf('.') > -1 ? extension : `.${extension}`)))
      .flat()
      .join(', ');

    const infoAttachTooltip = `<span style="font-weight: bold">${
      resourcesContext.messages['supportedFileAttachmentsTooltip']
    } </span><span style="color: var(--success-color-lighter); fontWeight: 600">${getAttachExtensions || '*'}</span>
    <span style="font-weight: bold">${
      resourcesContext.messages['supportedFileAttachmentsMaxSizeTooltip']
    } </span><span style="color: var(--success-color-lighter); fontWeight: 600">${
      !isNil(records.selectedMaxSize) && records.selectedMaxSize.toString() !== '0'
        ? `${records.selectedMaxSize} ${resourcesContext.messages['MB']}`
        : resourcesContext.messages['maxSizeNotDefined']
    }`;

    const onImportTableError = async ({ xhr }) => {
      if (xhr.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
      }
    };

    return (
      <SnapshotContext.Provider>
        <ActionsToolbar
          colsSchema={colsSchema}
          dataflowId={dataflowId}
          datasetId={datasetId}
          hasCountryCode={hasCountryCode}
          hasWritePermissions={
            (hasWritePermissions && !tableFixedNumber && !tableReadOnly) || (hasWritePermissions && isReferenceDataset)
          }
          isDataflowOpen={isDataflowOpen}
          isDesignDatasetEditorRead={isDesignDatasetEditorRead}
          isExportable={isExportable}
          isFilterValidationsActive={isFilterValidationsActive}
          isFilterable={isFilterable}
          isGroupedValidationSelected={isGroupedValidationSelected}
          isLoading={isLoading}
          levelErrorTypesWithCorrects={levelErrorAllTypes}
          onHideSelectGroupedValidation={onHideSelectGroupedValidation}
          onRefresh={onRefresh}
          onSetVisible={onSetVisible}
          onUpdateData={onUpdateData}
          originalColumns={originalColumns}
          prevFilterValue={prevFilterValue}
          records={records}
          selectedRuleLevelError={selectedRuleLevelError}
          selectedRuleMessage={selectedRuleMessage}
          selectedTableSchemaId={selectedTableSchemaId}
          setColumns={setColumns}
          setDeleteDialogVisible={setDeleteDialogVisible}
          setImportTableDialogVisible={setImportTableDialogVisible}
          showGroupedValidationFilter={showGroupedValidationFilter}
          showValidationFilter={showValidationFilter}
          showValueFilter={showValueFilter}
          showWriteButtons={showWriteButtons && !tableFixedNumber && !tableReadOnly}
          tableHasErrors={tableHasErrors}
          tableId={tableId}
          tableName={tableName}
        />
        <ContextMenu model={menu} ref={contextMenuRef} />
        <div className={styles.Table}>
          <DataTable
            contextMenuSelection={records.selectedRecord}
            editable={(hasWritePermissions && !tableReadOnly) || (hasWritePermissions && isReferenceDataset)}
            first={records.firstPageRecord}
            footer={
              (hasWebformWritePermissions && hasWritePermissions && !tableReadOnly && !tableFixedNumber) ||
              (hasWritePermissions && isReferenceDataset) ? (
                <Footer
                  hasWritePermissions={
                    (hasWritePermissions && !tableReadOnly) || (hasWritePermissions && isReferenceDataset)
                  }
                  isDataflowOpen={isDataflowOpen}
                  isDesignDatasetEditorRead={isDesignDatasetEditorRead}
                  onAddClick={() => {
                    setIsNewRecord(true);
                    setAddDialogVisible(true);
                  }}
                  onPasteClick={() => setConfirmPasteVisible(true)}
                />
              ) : null
            }
            hasDefaultCurrentPage={true}
            id={tableId}
            lazy={true}
            loading={isLoading}
            onContextMenu={
              (hasWebformWritePermissions &&
                hasWritePermissions &&
                !tableReadOnly &&
                !isEditing &&
                !isDataflowOpen &&
                !isDesignDatasetEditorRead) ||
              (hasWritePermissions && isReferenceDataset)
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
            rows={records.recordsPerPage}
            rowsPerPageOptions={[5, 10, 20, 100]}
            scrollHeight="70vh"
            scrollable={true}
            selectionMode="single"
            sortField={sort.sortField}
            sortOrder={sort.sortOrder}
            sortable={true}
            totalRecords={
              !isNil(records.totalFilteredRecords) &&
              (isGroupedValidationSelected || isFilterValidationsActive || (!isNil(valueFilter) && valueFilter !== ''))
                ? records.totalFilteredRecords
                : records.totalRecords
            }
            value={fetchedData}>
            {columns}
          </DataTable>
        </div>

        {isColumnInfoVisible && (
          <Dialog
            className={styles.fieldInfoDialogWrapper}
            footer={columnInfoDialogFooter}
            header={resourcesContext.messages['columnInfo']}
            onHide={() => setIsColumnInfoVisible(false)}
            visible={isColumnInfoVisible}>
            <DataTable
              autoLayout={true}
              className={styles.itemTable}
              value={DataViewerUtils.getFieldValues(colsSchema, selectedHeader, [
                'header',
                'description',
                'type',
                ...(!isNil(DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader)) &&
                !isEmpty(DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader).codelistItems)
                  ? ['codelistItems']
                  : []),
                ...(!isNil(DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader)) &&
                !isEmpty(DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader).validExtensions)
                  ? ['validExtensions']
                  : []),
                ...(!isNil(DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader)) &&
                !isEmpty(DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader).validExtensions)
                  ? ['maxSize']
                  : []),
                !isNil(DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader)) &&
                DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader).readOnly
                  ? 'readOnly'
                  : '',
                !isNil(DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader)) &&
                DataViewerUtils.getColumnByHeader(colsSchema, selectedHeader).required
                  ? 'required'
                  : ''
              ])}>
              {['field', 'value'].map((column, i) => (
                <Column
                  body={column === 'value' ? requiredTemplate : null}
                  className={column === 'field' ? styles.fieldColumn : ''}
                  field={column}
                  headerStyle={{ display: 'none' }}
                  key={column}
                />
              ))}
            </DataTable>
          </Dialog>
        )}

        {importTableDialogVisible && (
          <CustomFileUpload
            accept=".csv"
            chooseLabel={resourcesContext.messages['selectFile']}
            className={styles.FileUpload}
            dialogClassName={styles.Dialog}
            dialogHeader={`${resourcesContext.messages['uploadTable']}${tableName}`}
            dialogOnHide={() => setImportTableDialogVisible(false)} //allowTypes="/(\.|\/)(csv)$/"
            dialogVisible={importTableDialogVisible}
            infoTooltip={`${resourcesContext.messages['supportedFileExtensionsTooltip']} .csv`}
            invalidExtensionMessage={resourcesContext.messages['invalidExtensionFile']}
            isDialog={true}
            name="file"
            onError={onImportTableError}
            onUpload={onUpload}
            replaceCheck={true}
            url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.importFileTable, {
              datasetId: datasetId,
              tableSchemaId: tableId,
              delimiter: encodeURIComponent(config.IMPORT_FILE_DELIMITER)
            })}`}
          />
        )}

        {isAttachFileVisible && (
          <CustomFileUpload
            accept={getAttachExtensions || '*'}
            chooseLabel={resourcesContext.messages['selectFile']}
            className={styles.FileUpload}
            dialogClassName={styles.Dialog}
            dialogHeader={`${resourcesContext.messages['uploadAttachment']}`}
            dialogOnHide={() => setIsAttachFileVisible(false)}
            dialogVisible={isAttachFileVisible}
            infoTooltip={infoAttachTooltip}
            invalidExtensionMessage={resourcesContext.messages['invalidExtensionFile']}
            isDialog={true}
            maxFileSize={
              !isNil(records.selectedMaxSize) && records.selectedMaxSize.toString() !== '0'
                ? records.selectedMaxSize * config.MB_SIZE
                : config.MAX_ATTACHMENT_SIZE
            }
            name="file"
            onUpload={onAttach}
            operation="PUT"
            url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.uploadAttachment, {
              datasetId,
              fieldId: records.selectedFieldId
            })}`}
          />
        )}

        {addDialogVisible && (
          <div onKeyPress={!hasTextareas() ? onKeyPress : undefined}>
            <Dialog
              blockScroll={false}
              className={`edit-table calendar-table ${styles.addEditRecordDialog}`}
              footer={addRowDialogFooter}
              header={resourcesContext.messages['addRecord']}
              modal={true}
              onHide={() => setAddDialogVisible(false)}
              visible={addDialogVisible}
              zIndex={3003}>
              <div className="p-grid p-fluid">
                <DataForm
                  addDialogVisible={addDialogVisible}
                  colsSchema={colsSchema}
                  datasetId={datasetId}
                  datasetSchemaId={datasetSchemaId}
                  formType="NEW"
                  getTooltipMessage={getTooltipMessage}
                  hasWritePermissions={hasWritePermissions}
                  isSaving={isSaving}
                  onChangeForm={onEditAddFormInput}
                  onConditionalChange={onConditionalChange}
                  onShowCoordinateError={onShowCoordinateError}
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
            className={`calendar-table ${styles.addEditRecordDialog}`}
            footer={editRowDialogFooter}
            header={resourcesContext.messages['editRow']}
            modal={true}
            onHide={() => setEditDialogVisible(false)}
            visible={editDialogVisible}
            zIndex={3003}>
            <div className="p-grid p-fluid">
              <DataForm
                colsSchema={colsSchema}
                datasetId={datasetId}
                datasetSchemaId={datasetSchemaId}
                editDialogVisible={editDialogVisible}
                formType="EDIT"
                getTooltipMessage={getTooltipMessage}
                hasWritePermissions={hasWritePermissions}
                isSaving={isSaving}
                onChangeForm={onEditAddFormInput}
                onConditionalChange={onConditionalChange}
                onShowCoordinateError={onShowCoordinateError}
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
            header={`${resourcesContext.messages['deleteDatasetTableHeader']} (${tableName})`}
            labelCancel={resourcesContext.messages['no']}
            labelConfirm={resourcesContext.messages['yes']}
            onConfirm={onConfirmDeleteTable}
            onHide={() => onSetVisible(setDeleteDialogVisible, false)}
            visible={deleteDialogVisible}>
            {resourcesContext.messages['deleteDatasetTableConfirm']}
          </ConfirmDialog>
        )}

        {isDeleteAttachmentVisible && (
          <ConfirmDialog
            classNameConfirm={'p-button-danger'}
            header={`${resourcesContext.messages['deleteAttachmentHeader']}`}
            labelCancel={resourcesContext.messages['no']}
            labelConfirm={resourcesContext.messages['yes']}
            onConfirm={onConfirmDeleteAttachment}
            onHide={() => setIsDeleteAttachmentVisible(false)}
            visible={isDeleteAttachmentVisible}>
            {resourcesContext.messages['deleteAttachmentConfirm']}
          </ConfirmDialog>
        )}

        {confirmDeleteVisible && (
          <ConfirmDialog
            classNameConfirm={'p-button-danger'}
            header={resourcesContext.messages['deleteRow']}
            labelCancel={resourcesContext.messages['no']}
            labelConfirm={resourcesContext.messages['yes']}
            onConfirm={onConfirmDeleteRow}
            onHide={() => setConfirmDeleteVisible(false)}
            visible={confirmDeleteVisible}>
            {resourcesContext.messages['confirmDeleteRow']}
          </ConfirmDialog>
        )}
        {confirmPasteVisible && (
          <ConfirmDialog
            className="edit-table"
            disabledConfirm={isEmpty(records.pastedRecords)}
            divRef={divRef}
            hasPasteOption={true}
            header={resourcesContext.messages['pasteRecords']}
            isPasting={isPasting}
            labelCancel={resourcesContext.messages['cancel']}
            labelConfirm={resourcesContext.messages['save']}
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
            blockScroll={false}
            className={'map-data'}
            footer={saveMapGeoJsonDialogFooter}
            header={resourcesContext.messages['geospatialData']}
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
