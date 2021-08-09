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
  const resources = useContext(ResourcesContext);

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
        label={resources.messages['save']}
        onClick={() => onSaveRecord(selectedRecord)}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon={'cancel'}
        label={resources.messages['cancel']}
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
        notificationContext.add({
          type: 'GENERIC_BLOCKED_ERROR'
        });
      } else {
        console.error('TableManagement - onDeleteRow.', error);
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
        notificationContext.add({
          type: 'DELETE_RECORD_BY_ID_ERROR',
          content: { dataflowId, dataflowName, datasetId, datasetName, tableName: 'tableName' }
        });
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
        notificationContext.add({
          type: 'UPDATE_RECORDS_IN_CASCADE_BY_ID_ERROR'
        });
      } else {
        notificationContext.add({
          type: 'UPDATE_RECORDS_BY_ID_ERROR',
          content: { dataflowId, datasetId, dataflowName, datasetName }
        });
      }
    } finally {
      tableManagementDispatch({ type: 'ON_SAVE_RECORD' });
      onRefresh();
    }
  };

  const addIconLevelError = (validation, levelError, message) => {
    let icon = [];
    if (!isEmpty(validation)) {
      icon.push(
        <IconTooltip
          className={styles.iconTooltipLevelError}
          key={levelError}
          levelError={levelError}
          message={message}
        />
      );
    }
    return icon;
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
          label={hasRecord ? resources.messages['webformTableEdit'] : resources.messages['webformTableCreation']}
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

  const getIconsValidationsErrors = validations => {
    let icons = [];
    if (isNil(validations)) {
      return icons;
    }

    const blockerIcon = addIconLevelError(validations.blockers, 'BLOCKER', validations.messageBlockers);
    const errorIcon = addIconLevelError(validations.errors, 'ERROR', validations.messageErrors);
    const warningIcon = addIconLevelError(validations.warnings, 'WARNING', validations.messageWarnings);
    const infoIcon = addIconLevelError(validations.infos, 'INFO', validations.messageInfos);

    icons = blockerIcon.concat(errorIcon, warningIcon, infoIcon);
    return <div className={styles.iconTooltipWrapper}>{icons}</div>;
  };

  const renderActionButtonsColumn = (
    <Column
      body={row => renderActionsTemplate(row)}
      header={resources.messages['actions']}
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
    const validationsGroup = DataViewerUtils.groupValidations(
      recordData,
      resources.messages['recordBlockers'],
      resources.messages['recordErrors'],
      resources.messages['recordWarnings'],
      resources.messages['recordInfos']
    );
    return getIconsValidationsErrors(validationsGroup);
  };

  const renderValidationColumn = (
    <Column
      body={validationsTemplate}
      field="validations"
      header={resources.messages['validationsDataColumn']}
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
    <DataTable className={styles.table} value={[{ emptyContent: resources.messages['noDataInDataTable'] }]}>
      <Column field={'emptyContent'} header={resources.messages['overviewEmptyTableHeader']} />
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
          value={tableManagementState.records}>
          {renderTableColumns()}
        </DataTable>
      )}

      {isDialogVisible.delete && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resources.messages['deleteTabHeader']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteRow()}
          onHide={() => manageDialogs('delete', false)}
          visible={isDialogVisible.delete}>
          {resources.messages['confirmDeleteRow']}
        </ConfirmDialog>
      )}

      {isDialogVisible.manageRows && (
        <Dialog
          className={`calendar-table ${styles.addEditRecordDialog}`}
          footer={editRowDialogFooter}
          header={resources.messages['editRow']}
          modal={true}
          onHide={() => manageDialogs('manageRows', false)}
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
