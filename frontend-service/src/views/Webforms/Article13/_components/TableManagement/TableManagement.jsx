import { Fragment, useContext, useEffect, useReducer } from 'react';

import isArray from 'lodash/isArray';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './TableManagement.module.scss';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { IconTooltip } from 'views/_components/IconTooltip';
import { Spinner } from 'views/_components/Spinner';
import { WebformDataForm } from './_components/WebformDataForm';

import { DatasetService } from 'services/DatasetService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { tableManagementReducer } from './_functions/Reducers/tableManagementReducer';

import { DataViewerUtils } from 'views/_components/DataViewer/_functions/Utils/DataViewerUtils';
import { MetadataUtils, RecordUtils } from 'views/_functions/Utils';
import { TableManagementUtils } from './_functions/Utils/TableManagementUtils';
import { WebformsUtils } from 'views/Webforms/_functions/Utils/WebformsUtils';
import { ErrorUtils } from 'views/_functions/Utils/ErrorUtils';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const TableManagement = ({
  dataflowId,
  datasetId,
  isAddingPamsId = false,
  loading,
  onAddTableRecord,
  onRefresh,
  onSelectEditTable,
  records,
  schemaTables,
  tables
}) => {
  const { getFieldSchemaColumnIdByHeader, parsePamsRecordsWithParentData, parseTableSchemaColumns } =
    TableManagementUtils;

  const { getWebformTabs, parsePamsRecords } = WebformsUtils;

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [tableManagementState, tableManagementDispatch] = useReducer(tableManagementReducer, {
    initialSelectedRecord: {},
    isDialogVisible: { delete: false, manageRows: false },
    isLoading: true,
    isSaving: false,
    parentTablesWithData: [],
    records: [],
    selectedRecord: {},
    tableColumns: [],
    tableSchemaColumns: []
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
    onLoadParentTablesData();
  }, [records]);

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
        icon={isSaving === true ? 'spinnerAnimate' : 'check'}
        label={resourcesContext.messages['save']}
        onClick={() => onSaveRecord(selectedRecord)}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon={'cancel'}
        label={resourcesContext.messages['cancel']}
        onClick={onCancelRowEdit}
      />
    </div>
  );

  const initialLoad = () => {
    if (!isEmpty(records)) {
      const parsedTables = DataViewerUtils.parseData(parentTablesWithData[0].data);
      const tableSchemaColumns = parseTableSchemaColumns(schemaTables, parsePamsRecords(records));
      const parsedRecordsWithValidations = parsePamsRecordsWithParentData(
        parsedTables,
        parentTablesWithData,
        schemaTables
      );

      tableManagementDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          records: parsedRecordsWithValidations,
          tableSchemaColumns,
          tableColumns: [
            {
              field: 'Id',
              fieldSchemaId: getFieldSchemaColumnIdByHeader(tableSchemaColumns, 'Id'),
              header: 'PaM Number'
            },
            {
              field: 'Title',
              fieldSchemaId: getFieldSchemaColumnIdByHeader(tableSchemaColumns, 'Title'),
              header: 'Name of PaM or group of PaMs'
            },
            {
              field: 'TitleNational',
              fieldSchemaId: getFieldSchemaColumnIdByHeader(tableSchemaColumns, 'TitleNational'),
              header: 'Name of PaM or group of PaMs in national language'
            },
            {
              field: 'IsGroup',
              fieldSchemaId: getFieldSchemaColumnIdByHeader(tableSchemaColumns, 'IsGroup'),
              header: 'PaM or group of PaMs'
            },
            {
              field: 'ListOfSinglePams',
              fieldSchemaId: getFieldSchemaColumnIdByHeader(tableSchemaColumns, 'ListOfSinglePams'),
              header: 'Which policies or measures does it cover?'
            },
            {
              field: 'ShortDescription',
              fieldSchemaId: getFieldSchemaColumnIdByHeader(tableSchemaColumns, 'ShortDescription'),
              header: 'Short description'
            },
            {
              field: 'Table_1',
              fieldSchemaId: getFieldSchemaColumnIdByHeader(tableSchemaColumns, 'Table_1'),
              header:
                'Table 1: Sectors and gases for reporting on policies and measures and groups of measures, and type of policy instrument'
            },
            {
              field: 'Table_2',
              fieldSchemaId: getFieldSchemaColumnIdByHeader(tableSchemaColumns, 'Table_2'),
              header:
                'Table 2: Available results of ex-ante and ex-post assessments of the effects of individual or groups of policies and measures on mitigation of climate change'
            },
            {
              field: 'Table_3',
              fieldSchemaId: getFieldSchemaColumnIdByHeader(tableSchemaColumns, 'Table_3'),
              header:
                'Table 3: Available projected and realised costs and benefits of individual or groups of policies and measures on mitigation of climate change'
            },
            {
              field: 'TableSchemas'
            }
          ]
        }
      });
    }
  };

  const manageDialogs = (dialog, value) =>
    tableManagementDispatch({ type: 'MANAGE_DIALOGS', payload: { dialog, value } });

  const setIsLoading = value => tableManagementDispatch({ type: 'IS_LOADING', payload: { value } });

  const onDeleteRow = async () => {
    const deleteCascade = true;

    try {
      await DatasetService.deleteRecord(datasetId, selectedRecord.recordId, deleteCascade);
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
      manageDialogs('delete', false);
    }
  };

  const onEditFormInput = (property, value) => {
    if (property === 'ListOfSinglePams' && isArray(value)) {
      value = value.sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' })).join('; ');
    }

    let updatedRecord = RecordUtils.changeRecordValue(
      { ...selectedRecord },
      getFieldSchemaColumnIdByHeader(tableSchemaColumns, property),
      value
    );

    tableManagementDispatch({ type: 'EDIT_SELECTED_RECORD', payload: updatedRecord });
  };

  const onLoadParentTablesData = () => {
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
      const sortFieldSchemaId = getFieldSchemaColumnIdByHeader(tableSchemaColumns, 'Id');
      const data = await DatasetService.getTableData({
        datasetId,
        tableSchemaId: parentTable.tableSchemaId,
        pageSize: 300,
        fields: sortFieldSchemaId !== '' ? `${sortFieldSchemaId}:${1}` : undefined,
        levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER']
      });

      return { data, tableSchemaId: parentTable.tableSchemaId, tableSchemaName: parentTable.tableSchemaName };
    });
    Promise.all(parentTablesDataPromises)
      .then(parentTableData => tableManagementDispatch({ type: 'SET_PARENT_TABLES_DATA', payload: parentTableData }))
      .finally(() => setIsLoading(false));
  };

  const onSaveRecord = async record => {
    const updateInCascade = true;

    record.dataRow = record.dataRow.filter(
      field => !['datasetPartitionId', 'id'].includes(Object.keys(field.fieldData)[0])
    );
    try {
      tableManagementDispatch({ type: 'SET_IS_SAVING', payload: true });
      await DatasetService.updateRecord(datasetId, record, updateInCascade);
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

  const addTableTemplate = (rowData, colData) => {
    let hasRecord = false;
    let hasTable = false;
    rowData.dataRow.forEach(row =>
      row.fieldData.tableSchemas.forEach(tableSchema => {
        if (tableSchema.tableSchemaName === colData.field) {
          hasRecord = tableSchema.hasRecord;
          hasTable = true;
        }
      })
    );

    const pamsIdFieldSchemaId = getFieldSchemaColumnIdByHeader(tableSchemaColumns, 'Id');
    const pamsFieldSchemaValue = RecordUtils.getCellValue({ rowData: rowData }, pamsIdFieldSchemaId);
    return (
      <div style={{ display: 'flex', justifyContent: 'center' }}>
        <Button
          className={'p-button-secondary'}
          disabled={!hasTable || isSaving}
          icon={hasRecord ? 'edit' : 'add'}
          label={
            hasRecord
              ? resourcesContext.messages['webformTableEdit']
              : resourcesContext.messages['webformTableCreation']
          }
          onClick={async () => {
            if (hasRecord) {
              onSelectEditTable(pamsFieldSchemaValue, colData.field);
            } else {
              tableManagementDispatch({ type: 'SET_IS_SAVING', payload: true });
              const configParentTables = Object.keys(
                getWebformTabs(
                  tables.map(table => table.name),
                  schemaTables,
                  tables
                )
              );
              await onAddTableRecord(
                schemaTables.filter(
                  schemaTable =>
                    configParentTables.includes(colData.field) &&
                    TextUtils.areEquals(schemaTable.tableSchemaName, colData.field)
                )[0],
                pamsFieldSchemaValue
              );
              tableManagementDispatch({ type: 'SET_IS_SAVING', payload: false });
            }
          }}
        />
      </div>
    );
  };

  const dataTemplate = (rowData, column) => {
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

  const renderActionsTemplate = () => (
    <ActionsColumn
      onDeleteClick={() => manageDialogs('delete', true)}
      onEditClick={() => manageDialogs('manageRows', true)}
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

  const renderValidationColumn = (
    <Column
      body={validationsTemplate}
      field="validations"
      header={resourcesContext.messages['validationsDataColumn']}
      key="recordValidation"
      sortable={false}
      style={{ width: '100px' }}
    />
  );

  const renderTableColumns = () => {
    const data = tableColumns.map(col => (
      <Column
        body={!['TableSchemas', 'Table_1', 'Table_2', 'Table_3'].includes(col.field) ? dataTemplate : addTableTemplate}
        className={col.field === 'TableSchemas' ? styles.invisibleHeader : ''}
        field={col.field}
        fieldSchemaId={col.fieldSchemaId}
        header={col.header}
        key={col.field}
      />
    ));

    data.unshift(renderValidationColumn);
    data.unshift(renderActionButtonsColumn);

    return data;
  };

  const renderEmptyTable = () => (
    <DataTable
      className={styles.table}
      summary={resourcesContext.messages['overviewEmptyTableHeader']}
      value={[{ emptyContent: resourcesContext.messages['noDataInDataTable'] }]}>
      <Column field={'emptyContent'} header={resourcesContext.messages['overviewEmptyTableHeader']} />
    </DataTable>
  );

  if (isLoading || isAddingPamsId) return <Spinner style={{ top: 0, marginBottom: '2rem' }} />;

  return (
    <Fragment>
      {isEmpty(records) ? (
        renderEmptyTable()
      ) : (
        <DataTable
          autoLayout={true}
          className={styles.table}
          loading={loading}
          onRowClick={event =>
            tableManagementDispatch({ type: 'SET_SELECTED_RECORD', payload: { selectedRecord: event.data } })
          }
          summary={resourcesContext.messages['webformArticle13Title']}
          value={tableManagementState.records}>
          {renderTableColumns()}
        </DataTable>
      )}

      {isDialogVisible.delete && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resourcesContext.messages['deleteTabHeader']}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={() => onDeleteRow()}
          onHide={() => manageDialogs('delete', false)}
          visible={isDialogVisible.delete}>
          {resourcesContext.messages['confirmDeleteRow']}
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
