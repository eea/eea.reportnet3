import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './TableManagement.module.scss';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { WebformDataForm } from './_components/WebformDataForm';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { IconTooltip } from 'ui/views/_components/IconTooltip';
import { MultiSelect } from 'ui/views/_components/MultiSelect';
import { InputTextarea } from 'ui/views/_components/InputTextarea';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { tableManagementReducer } from './_functions/Reducers/tableManagementReducer';

import { Article15Utils } from '../../../Article15/_functions/Utils/Article15Utils';
import { DataViewerUtils } from 'ui/views/_components/DataViewer/_functions/Utils/DataViewerUtils';
import { MetadataUtils } from 'ui/views/_functions/Utils/MetadataUtils';
import { RecordUtils } from 'ui/views/_functions/Utils/RecordUtils';
import { TableManagementUtils } from './_functions/Utils/TableManagementUtils';

export const TableManagement = ({
  dataflowId,
  datasetId,
  loading,
  onAddRecord,
  onAddTableRecord,
  onRefresh,
  onSelectEditTable,
  records,
  schemaTables,
  tables
}) => {
  const {
    getFieldSchemaColumnIdByHeader,
    parsePamsRecordsWithParentData,
    parseTableSchemaColumns
  } = TableManagementUtils;

  const resources = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [tableManagementState, tableManagementDispatch] = useReducer(tableManagementReducer, {
    initialSelectedRecord: {},
    isDialogVisible: { delete: false, manageRows: false },
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
        onClick={() => {
          try {
            onSaveRecord(selectedRecord);
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

  const initialLoad = () => {
    if (!isEmpty(records)) {
      const parsedTables = DataViewerUtils.parseData(parentTablesWithData[0].data);
      const tableSchemaColumns = parseTableSchemaColumns(schemaTables);
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
              header: 'Name of policy or measure'
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

  const onDeleteRow = async () => {
    try {
      const isDataDeleted = await DatasetService.deleteRecordById(datasetId, selectedRecord.recordId);
      if (isDataDeleted) {
        onRefresh();
      }
    } catch (error) {
      console.error('error', error);

      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'DELETE_RECORD_BY_ID_ERROR',
        content: { dataflowId, dataflowName, datasetId, datasetName, tableName: 'tableName' }
      });
    } finally {
      manageDialogs('delete', false);
    }
  };

  const onEditFormInput = (property, value) => {
    let updatedRecord = RecordUtils.changeRecordValue(
      { ...selectedRecord },
      getFieldSchemaColumnIdByHeader(tableSchemaColumns, property),
      value
    );

    tableManagementDispatch({
      type: 'EDIT_SELECTED_RECORD',
      payload: updatedRecord
    });
  };

  const onLoadParentTablesData = () => {
    const configParentTables = Object.keys(
      Article15Utils.getWebformTabs(
        tables.map(table => table.name.toUpperCase()),
        schemaTables,
        tables
      )
    );
    const parentTables = schemaTables.filter(schemaTable =>
      configParentTables.includes(schemaTable.header.toUpperCase())
    );
    const parentTablesDataPromises = parentTables.map(async parentTable => {
      return {
        tableSchemaId: parentTable.tableSchemaId,
        tableSchemaName: parentTable.tableSchemaName,
        data: await DatasetService.tableDataById(datasetId, parentTable.tableSchemaId, '', 100, undefined, [
          'CORRECT',
          'INFO',
          'WARNING',
          'ERROR',
          'BLOCKER'
        ])
      };
    });
    Promise.all(parentTablesDataPromises).then(parentTableData => {
      tableManagementDispatch({ type: 'SET_PARENT_TABLES_DATA', payload: parentTableData });
    });
  };

  const onSaveRecord = async record => {
    record.dataRow = record.dataRow.filter(
      field => !['datasetPartitionId', 'id'].includes(Object.keys(field.fieldData)[0])
    );
    try {
      tableManagementDispatch({ type: 'SET_IS_SAVING', payload: true });
      await DatasetService.updateRecordsById(datasetId, record);
    } catch (error) {
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'UPDATE_RECORDS_BY_ID_ERROR',
        content: { dataflowId, datasetId, dataflowName, datasetName }
      });
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
          icon={isSaving ? 'spinnerAnimate' : hasRecord ? 'edit' : 'add'}
          label={hasRecord ? resources.messages['webformTableEdit'] : resources.messages['webformTableCreation']}
          onClick={async () => {
            if (hasRecord) {
              onSelectEditTable(pamsFieldSchemaValue, colData.field);
            } else {
              tableManagementDispatch({ type: 'SET_IS_SAVING', payload: true });
              const configParentTables = Object.keys(
                Article15Utils.getWebformTabs(
                  tables.map(table => table.name),
                  schemaTables,
                  tables
                )
              );
              await onAddTableRecord(
                schemaTables.filter(
                  schemaTable =>
                    configParentTables.includes(colData.field) &&
                    schemaTable.tableSchemaName.toUpperCase() === colData.field.toUpperCase()
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
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}>
            {field.fieldData[column.fieldSchemaId]}
            <IconTooltip levelError={levelError} message={message} />
          </div>
        );
      } else {
        return (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}>
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
      header={resources.messages['validationsDataColumn']}
      field="validations"
      key="recordValidation"
      sortable={false}
      style={{ width: '100px' }}
    />
  );

  const renderTableColumns = () => {
    const data = tableColumns.map(col => (
      <Column
        className={col.field === 'TableSchemas' ? styles.invisibleHeader : ''}
        key={col.field}
        field={col.field}
        fieldSchemaId={col.fieldSchemaId}
        header={col.header}
        body={!['TableSchemas', 'Table_1', 'Table_2', 'Table_3'].includes(col.field) ? dataTemplate : addTableTemplate}
      />
    ));

    data.unshift(renderValidationColumn);
    data.unshift(renderActionButtonsColumn);

    return data;
  };

  return (
    <Fragment>
      <DataTable
        autoLayout={true}
        loading={loading}
        onRowClick={event =>
          tableManagementDispatch({ type: 'SET_SELECTED_RECORD', payload: { selectedRecord: event.data } })
        }
        value={tableManagementState.records}>
        {renderTableColumns()}
      </DataTable>

      <div className={styles.addButtons}>
        <Button label={'Add Single'} icon={'add'} onClick={() => onAddRecord('Single')} />
        <Button label={'Add Group'} icon={'add'} onClick={() => onAddRecord('Group')} />
      </div>

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
              records={tableManagementState.records}
            />
          </div>
        </Dialog>
      )}
    </Fragment>
  );
};
