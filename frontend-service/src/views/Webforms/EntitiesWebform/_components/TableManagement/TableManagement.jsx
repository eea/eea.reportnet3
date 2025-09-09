import { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import isArray from 'lodash/isArray';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import { InputText } from 'views/_components/InputText';
import styles from './TableManagement.module.scss';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { IconTooltip } from 'views/_components/IconTooltip';
import { WebformDataForm } from './_components/WebformDataForm';

import { DatasetService } from 'services/DatasetService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { tableManagementReducer } from './_functions/Reducers/tableManagementReducer';

import { DataViewerUtils } from 'views/_components/DataViewer/_functions/Utils/DataViewerUtils';
import { MetadataUtils, RecordUtils } from 'views/_functions/Utils';
import { TableManagementUtils } from './_functions/Utils/TableManagementUtils';
import { WebformsUtils } from 'views/Webforms/_functions/Utils/WebformsUtils';
import { useState } from 'react';
import { sortReducer } from './_functions/Reducers/sortReducer';
import { TooltipButton } from 'views/_components/TooltipButton';
import ReactDOMServer from 'react-dom/server';

export const TableManagement = ({
  bigData,
  dataflowId,
  datasetId,
  disableActionButtons = false,
  isAddingRootTableId = false,
  isIcebergCreated,
  loading,
  onRefresh,
  onSelectEditTable,
  overview,
  refreshTrigger,
  rootPkFieldId,
  rootTableId,
  rootTableName,
  schemaTables,
  tables
}) => {
  const { getFieldSchemaColumnIdByHeader, parseEntitiesRecordsWithParentData, parseTableSchemaColumns } =
    TableManagementUtils;

  const { getWebformTabs } = WebformsUtils;
  const didInitialParentFetch = useRef({
    hasLoaded: false,
    initialTableName: null
  });
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [valueFilter, setValueFilter] = useState('');
  const [fetchFilter, setFetchFilter] = useState('');

  const [records] = useReducer(tableManagementReducer, {
    totalRecords: 0,
    totalFilteredRecords: 0,
    recordsPerPage: 10,
    firstPageRecord: 0
  });

  const [sort, dispatchSort] = useReducer(sortReducer, {
    sortField: undefined,
    sortOrder: undefined
  });

  const [tableManagementState, tableManagementDispatch] = useReducer(tableManagementReducer, {
    initialSelectedRecord: {},
    isDeletingRow: false,
    isDialogVisible: { delete: false, doubleDelete: false, manageRows: false },
    isLoading: true,
    isSaving: false,
    parentTablesWithData: [],
    records: [],
    selectedRecord: {},
    tableColumns: [],
    tableSchemaColumns: [],
    totalRecords: 0,
    totalFilteredRecords: 0,
    recordsPerPage: 10,
    firstPageRecord: 0
  });

  const {
    initialSelectedRecord,
    isDialogVisible,
    isLoading,
    isSaving,
    parentTablesWithData,
    selectedRecord,
    tableColumns,
    tableSchemaColumns
  } = tableManagementState;

  useEffect(() => {
    onLoadParentTablesData(fetchFilter);
  }, [
    records,
    fetchFilter,
    refreshTrigger,
    sort.sortField,
    sort.sortOrder,
    tableManagementState.firstPageRecord,
    tableManagementState.recordsPerPage
  ]);

  useEffect(() => {
    if (!isEmpty(parentTablesWithData)) {
      initialLoad();
    }
  }, [parentTablesWithData]);

  const onCancelRowEdit = () => {
    const inmRecords = [...tableManagementState.records];
    const recordIndex = RecordUtils.getRecordId(tableManagementState.records, selectedRecord);
    inmRecords[recordIndex] = initialSelectedRecord;
    tableManagementDispatch({ type: 'RESET_SELECTED_RECORD', payload: { records: inmRecords } });
  };

  const editRowDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        className={!isSaving && 'p-button-animated-blink'}
        disabled={isSaving}
        icon={isSaving ? 'spinnerAnimate' : 'check'}
        label={resourcesContext.messages['save']}
        onClick={() => onSaveRecord(selectedRecord)}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['cancel']}
        onClick={onCancelRowEdit}
      />
    </div>
  );

  const initialLoad = () => {
    const parseOverview = tableSchemaColumnsAux =>
      overview.map(item => {
        item.fieldSchemaId = getFieldSchemaColumnIdByHeader(tableSchemaColumnsAux, item.field);
        return item;
      });

    if (!isEmpty(records)) {
      const parsedTables = DataViewerUtils.parseData(parentTablesWithData[0].data);
      const tableSchemaColumnsAux = parseTableSchemaColumns(schemaTables, rootTableName);
      const parsedRecordsWithValidations = parseEntitiesRecordsWithParentData(
        parsedTables,
        parentTablesWithData,
        schemaTables,
        rootPkFieldId,
        rootTableName
      );
      tableManagementDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          records: parsedRecordsWithValidations,
          tableSchemaColumns: tableSchemaColumnsAux,
          tableColumns: parseOverview(tableSchemaColumnsAux)
        }
      });
    }
  };

  const manageDialogs = (dialog, value) =>
    tableManagementDispatch({ type: 'MANAGE_DIALOGS', payload: { dialog, value } });

  const setIsLoading = value => tableManagementDispatch({ type: 'IS_LOADING', payload: { value } });

  const onDeleteRow = async () => {
    tableManagementDispatch({ type: 'DELETE_ROW', payload: true });
    try {
      await DatasetService.deleteRecord({
        datasetId,
        selectedRecordId: selectedRecord.recordId,
        tableId: rootTableId,
        updateInCascade: true
      });
      onRefresh();
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        console.error('TableManagement - onDeleteRow.', error);
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
        notificationContext.add(
          {
            type: 'DELETE_RECORD_BY_ID_ERROR',
            content: { dataflowId, dataflowName, datasetId, datasetName }
          },
          true
        );
      }
    } finally {
      tableManagementDispatch({ type: 'DELETE_ROW', payload: false });
      manageDialogs('doubleDelete', false);
      onLoadParentTablesData();
    }
  };

  const onEditFormInput = (property, value) => {
    if (property === 'ListOfSingleEntities' && isArray(value)) {
      value = value.sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' })).join('; ');
    }

    let updatedRecord = RecordUtils.changeRecordValue(
      { ...selectedRecord },
      getFieldSchemaColumnIdByHeader(tableSchemaColumns, property),
      value
    );

    tableManagementDispatch({ type: 'EDIT_SELECTED_RECORD', payload: updatedRecord });
  };

  const onLoadParentTablesData = async (filterValue = '') => {
    setIsLoading(true);
    const configParentTables = Object.keys(
      getWebformTabs(
        tables.map(table => table.name.toUpperCase()),
        schemaTables,
        tables
      )
    );
    const parentTables = schemaTables.filter(schemaTable => {
      return (
        configParentTables.includes(
          !isNil(schemaTable['tableSchemaName']) && schemaTable['tableSchemaName'].toUpperCase()
        ) || configParentTables.includes(!isNil(schemaTable['header']) && schemaTable['header'].toUpperCase())
      );
    });

    const parentTablesDataPromises = parentTables.map(async parentTable => {
      const sortFieldSchemaId = sort.sortField
        ? getFieldSchemaColumnIdByHeader(tableSchemaColumns, sort.sortField)
        : undefined;
      const sortField = sortFieldSchemaId;

      const sortOrder = sort.sortOrder === 1 ? '1' : '-1';

      let data;
      let fields;
      const fRow = tableManagementState.firstPageRecord;
      const nRows = tableManagementState.recordsPerPage;

      if (!isUndefined(sortField) && sortField !== null) {
        fields = `${sortField}:${sortOrder}`;
      }

      if (bigData) {
        data = await DatasetService.getTableDataDL({
          datasetId,
          tableSchemaId: parentTable.tableSchemaId,
          pageSize: nRows,
          pageNum: Math.floor(fRow / nRows),
          fields,
          levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER'],
          value: filterValue
        });
      } else {
        data = await DatasetService.getTableData({
          datasetId,
          tableSchemaId: parentTable.tableSchemaId,
          pageSize: nRows,
          pageNum: Math.floor(fRow / nRows),
          value: filterValue,
          fields,
          levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER']
        });
      }
      return { data, tableSchemaId: parentTable.tableSchemaId, tableSchemaName: parentTable.tableSchemaName };
    });
    Promise.all(parentTablesDataPromises)
      .then(parentTableData => {
        tableManagementDispatch({ type: 'SET_PARENT_TABLES_DATA', payload: parentTableData });

        const mainTableData = parentTableData.find(t => t.tableSchemaId === rootTableId);

        if (mainTableData?.data) {
          tableManagementDispatch({
            type: 'SET_TABLE_DATA',
            payload: {
              records: DataViewerUtils.parseData(mainTableData.data),
              totalRecords: mainTableData.data.totalRecords || 0,
              totalFilteredRecords: mainTableData.data.totalFilteredRecords || mainTableData.data.totalRecords || 0
            }
          });
        }
      })
      .finally(() => {
        didInitialParentFetch.current.hasLoaded = true;
        setIsLoading(false);
      });
  };

  const onSaveRecord = async record => {
    const updateInCascade = true;
    record.dataRow = record.dataRow.filter(
      field => !['datasetPartitionId', 'id'].includes(Object.keys(field.fieldData)[0])
    );
    try {
      tableManagementDispatch({ type: 'SET_IS_SAVING', payload: true });
      await DatasetService.updateRecord({ datasetId, record, updateInCascade });
    } catch (error) {
      console.error('TableManagement - onSaveRecord.', error);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
      if (updateInCascade) {
        notificationContext.add({ type: 'UPDATE_RECORDS_IN_CASCADE_BY_ID_ERROR' }, true);
      } else {
        notificationContext.add(
          {
            type: 'UPDATE_RECORDS_BY_ID_ERROR',
            content: { dataflowId, datasetId, dataflowName, datasetName }
          },
          true
        );
      }
    } finally {
      tableManagementDispatch({ type: 'ON_SAVE_RECORD' });
      onRefresh();
    }
  };

  const dataTemplate = (rowData, column) => {
    if (!rowData || !Array.isArray(rowData.dataRow)) return null;
    let field = rowData.dataRow.filter(row => Object.keys(row.fieldData)[0] === column.fieldSchemaId)[0];
    if (!isNil(field) && !isNil(field.fieldData)) {
      if (!isNil(field.fieldValidations)) {
        const validations = DataViewerUtils.orderValidationsByLevelError([...field.fieldValidations]);
        const message = DataViewerUtils.formatValidations(validations);
        const levelError = DataViewerUtils.getLevelError(validations);
        return (
          <div style={{ alignItems: 'center', display: 'flex', justifyContent: 'space-between' }}>
            {field.fieldData[column.fieldSchemaId]}
            <IconTooltip levelError={levelError} message={message} />
          </div>
        );
      } else {
        return (
          <div style={{ alignItems: 'center', display: 'flex', justifyContent: 'space-between' }}>
            {field.fieldData[column.fieldSchemaId]}
          </div>
        );
      }
    }
  };

  const renderActionButtonsColumn = (
    <Column
      body={row => renderActionsTemplate(row)}
      header={resourcesContext.messages['actions']}
      key="actions"
      style={{ width: '100px' }}
    />
  );
  const renderActionsTemplate = rowData => {
    const entitiesIdFieldSchemaId = rootPkFieldId;

    const entitiesFieldSchemaValue =
      rowData && rowData.dataRow ? RecordUtils.getCellValue({ rowData }, entitiesIdFieldSchemaId) : undefined;

    let tableName = rootTableName;

    if (rowData && rowData.dataRow) {
      rowData.dataRow.forEach(row =>
        row.fieldData.tableSchemas?.forEach((tableSchema, index) => {
          if (index === 0) {
            tableName = tableSchema.tableSchemaName;
            if (!didInitialParentFetch.current.hasLoaded) {
              didInitialParentFetch.current.initialTableName = tableSchema.tableSchemaName;
            }
          }
        })
      );
    }

    if (isUndefined(tableName)) {
      tableName = didInitialParentFetch.current.initialTableName || rootTableName;
    }

    return (
      <ActionsColumn
        bigData={bigData}
        disabledButtons={disableActionButtons}
        isIcebergCreated={isIcebergCreated}
        onDeleteClick={() => {
          tableManagementDispatch({ type: 'SET_SELECTED_RECORD', payload: rowData });
          manageDialogs('delete', true);
        }}
        onEditClick={() => {
          tableManagementDispatch({ type: 'SET_SELECTED_RECORD', payload: rowData });
          onSelectEditTable(entitiesFieldSchemaValue, tableName, rowData.recordId);
        }}
      />
    );
  };

  const renderTableColumns = () => {
    if (isNil(overview)) {
      const data = tableColumns.map(col => (
        <Column
          body={dataTemplate}
          className={col.field === 'TableSchemas' ? styles.invisibleHeader : ''}
          field={col.field}
          fieldSchemaId={col.fieldSchemaId}
          header={col.header}
          key={col.field}
        />
      ));

      data.unshift(renderActionButtonsColumn);

      return data;
    }

    const data = tableColumns.map(col => (
      <Column
        body={dataTemplate}
        className={col.type === 'TABLE' ? styles.tableColumn : ''}
        field={col.field}
        fieldSchemaId={col.fieldSchemaId}
        header={col.header}
        key={col.field}
        sortable={col.isSortable}
      />
    ));

    data.unshift(renderActionButtonsColumn);

    return data;
  };

  const onChangePage = event => {
    tableManagementDispatch({
      type: 'SET_PAGINATION',
      payload: { firstPageRecord: event.first, recordsPerPage: event.rows }
    });
  };

  const onSort = event => {
    dispatchSort({
      type: 'SORT_TABLE',
      payload: { order: event.sortOrder, field: event.sortField }
    });

    tableManagementDispatch({
      type: 'SET_PAGINATION',
      payload: { firstPageRecord: 0, recordsPerPage: tableManagementState.recordsPerPage }
    });
  };

  const onSelectRecord = record => {
    tableManagementDispatch({ type: 'SET_SELECTED_RECORD', payload: record });
  };
  const getTooltipMessage = () => (
    <span style={{ fontStyle: 'italic' }}>{resourcesContext.messages['valueFilterTooltipCaseSensitiveNote']}</span>
  );

  const renderPaginatorRecordsCount = () => {
    const renderFilteredRowsLabel = () => {
      if (
        !isNil(valueFilter) &&
        valueFilter !== '' &&
        tableManagementState.totalRecords !== tableManagementState.totalFilteredRecords
      ) {
        return `${resourcesContext.messages['filtered']}: ${tableManagementState.totalFilteredRecords} | `;
      }
    };

    const renderTotalRowsLabel = () =>
      `${resourcesContext.messages['totalRecords']} ${
        !isUndefined(tableManagementState.totalRecords) ? tableManagementState.totalRecords : 0
      } `;
    const renderRowsLabel = () =>
      tableManagementState.totalRecords === 1
        ? resourcesContext.messages['record'].toLowerCase()
        : resourcesContext.messages['records'].toLowerCase();

    const renderFilteredLabel = () => {
      if (
        !isNil(valueFilter) &&
        valueFilter !== '' &&
        tableManagementState.totalRecords === tableManagementState.totalFilteredRecords
      ) {
        return ` (${resourcesContext.messages['filtered'].toLowerCase()})`;
      }
    };
    return (
      <Fragment>
        {renderFilteredRowsLabel()}
        {renderTotalRowsLabel()}
        {renderRowsLabel()}
        {renderFilteredLabel()}
      </Fragment>
    );
  };

  const renderFilters = () => {
    const onFilterSubmit = () => {
      tableManagementDispatch({
        type: 'SET_PAGINATION',
        payload: {
          firstPageRecord: 0,
          recordsPerPage: tableManagementState.recordsPerPage
        }
      });
      setFetchFilter(valueFilter);
    };

    const onReset = () => {
      setValueFilter('');
      setFetchFilter('');
      tableManagementDispatch({
        type: 'SET_PAGINATION',
        payload: {
          firstPageRecord: 0,
          recordsPerPage: tableManagementState.recordsPerPage
        }
      });
    };

    return (
      <div className={styles.filtersContainer}>
        <InputText
          className={styles.filterInput}
          onChange={e => {
            setValueFilter(e.target.value);
            if (e.target.value === '') {
              setFetchFilter('');
              tableManagementDispatch({
                type: 'SET_PAGINATION',
                payload: {
                  firstPageRecord: 0,
                  recordsPerPage: tableManagementState.recordsPerPage
                }
              });
            }
          }}
          onKeyDown={e => {
            if (e.key === 'Enter') {
              onFilterSubmit();
            }
          }}
          placeholder={resourcesContext.messages['valueFilter']}
          value={valueFilter}
        />
        <TooltipButton
          className={styles.tooltipButton}
          getContent={() => ReactDOMServer.renderToStaticMarkup(<div>{getTooltipMessage()}</div>)}
          uniqueIdentifier="valueFilter"
        />

        <Button
          disabled={isLoading}
          icon="filter"
          label={resourcesContext.messages['filter']}
          onClick={onFilterSubmit}
        />
        <Button
          disabled={isLoading || !valueFilter}
          icon="refresh"
          label={resourcesContext.messages['reset']}
          onClick={onReset}
        />
      </div>
    );
  };

  const renderTable = () => {
    return (
      <Fragment>
        {renderFilters()}
        <DataTable
          autoLayout={true}
          className={styles.table}
          first={tableManagementState.firstPageRecord}
          lazy={true}
          loading={isLoading}
          onPage={onChangePage}
          onRowSelect={e => onSelectRecord(Object.assign({}, e.data))}
          onSort={onSort}
          paginator={true}
          paginatorRight={renderPaginatorRecordsCount()}
          reorderableColumns={true}
          resizableColumns={true}
          rows={tableManagementState.recordsPerPage}
          rowsPerPageOptions={[10, 20, 50, 100, 300]}
          scrollable={true}
          sortField={sort.sortField}
          sortOrder={sort.sortOrder}
          summary={resourcesContext.messages['webformEntitiesTitle']}
          totalRecords={
            valueFilter && valueFilter !== ''
              ? tableManagementState.totalFilteredRecords
              : tableManagementState.totalRecords
          }
          value={tableManagementState.records}>
          {renderTableColumns()}
        </DataTable>
      </Fragment>
    );
  };

  return (
    <Fragment>
      {renderTable()}
      {isDialogVisible.delete && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resourcesContext.messages['deleteTabHeader']}
          iconConfirm={tableManagementState.isDeletingRow ? 'spinnerAnimate' : 'check'}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={() => {
            manageDialogs('delete', false);
            manageDialogs('doubleDelete', true);
          }}
          onHide={() => manageDialogs('delete', false)}
          visible={isDialogVisible.delete}>
          {resourcesContext.messages['confirmDeleteRow']}
        </ConfirmDialog>
      )}

      {isDialogVisible.doubleDelete && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resourcesContext.messages['deleteTabHeaderConfirm']}
          iconConfirm={tableManagementState.isDeletingRow ? 'spinnerAnimate' : 'check'}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onDeleteRow}
          onHide={() => manageDialogs('doubleDelete', false)}
          visible={isDialogVisible.doubleDelete}>
          <strong>{resourcesContext.messages['doubleConfirmDeleteRow']}</strong>
        </ConfirmDialog>
      )}

      {isDialogVisible.manageRows && (
        <Dialog
          footer={editRowDialogFooter}
          header={resourcesContext.messages['editRow']}
          modal={true}
          onHide={() => manageDialogs('manageRows', false)}
          style={{ width: '30%' }}
          visible={isDialogVisible.manageRows}
          zIndex={3003}>
          <div className="p-grid p-fluid">
            <WebformDataForm
              colsSchema={tableSchemaColumns}
              datasetId={datasetId}
              editDialogVisible={isDialogVisible.manageRows}
              onChangeForm={onEditFormInput}
              selectedRecord={selectedRecord}
              tableColumns={tableColumns}
            />
          </div>
        </Dialog>
      )}
    </Fragment>
  );
};
