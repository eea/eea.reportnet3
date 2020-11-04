import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './TableManagement.module.scss';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataForm } from 'ui/views/_components/DataViewer/_components/DataForm';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { tableManagementReducer } from './_functions/Reducers/tableManagementReducer';

import { useLoadColsSchemasAndColumnOptions } from 'ui/views/_components/DataViewer/_functions/Hooks/DataViewerHooks';

import { Article15Utils } from '../../../Article15/_functions/Utils/Article15Utils';
import { MetadataUtils } from 'ui/views/_functions/Utils';
import { TableManagementUtils } from './_functions/Utils/TableManagementUtils';

export const TableManagement = ({
  dataflowId,
  datasetId,
  loading,
  onAddRecord,
  onAddTableRecord,
  onRefresh,
  records,
  schemaTables,
  tables
}) => {
  const { parsePamsRecords, parseTableSchemaColumns } = TableManagementUtils;

  const resources = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [tableManagementState, tableManagementDispatch] = useReducer(tableManagementReducer, {
    deleteId: null,
    isDialogVisible: { delete: false, manageRows: false },
    parentTablesWithData: [],
    records: [],
    tableColumns: [],
    tableSchemaColumns: []
  });

  const { deleteId, isDialogVisible, parentTablesWithData, tableColumns } = tableManagementState;

  const { colsSchema, columnOptions } = useLoadColsSchemasAndColumnOptions(tableManagementState.tableSchemaColumns);

  useEffect(() => {
    onLoadParentTablesData();
  }, [records]);

  useEffect(() => {
    if (!isEmpty(parentTablesWithData)) {
      console.log({ parentTablesWithData });
      initialLoad();
      // tableManagementDispatch({
      //   type: 'SET_COLUMNS'
      // });
    }
  }, [parentTablesWithData]);

  const initialLoad = () => {
    console.log({ records, schemaTables, parentTablesWithData });
    const parsedRecords = parsePamsRecords(records);
    const tableSchemaColumns = parseTableSchemaColumns(schemaTables);

    tableManagementDispatch({
      type: 'INITIAL_LOAD',
      payload: {
        records: parsedRecords,
        tableSchemaColumns,
        tableColumns: [
          { field: 'Id', header: 'PaM Number' },
          { field: 'Title', header: 'Name of policy or measure' },
          { field: 'IsGroup', header: 'PaM or group of PaMs' },
          {
            body: addTableTemplate,
            field: 'Table_1',
            header:
              'Table 1: Sectors and gases for reporting on policies and measures and groups of measures, and type of policy instrument'
          },
          {
            body: addTableTemplate,
            field: 'Table_2',
            header:
              'Table 2: Available results of ex-ante and ex-post assessments of the effects of individual or groups of policies and measures on mitigation of climate change'
          },
          {
            body: addTableTemplate,
            field: 'Table_3',
            header:
              'Table 3: Available projected and realised costs and benefits of individual or groups of policies and measures on mitigation of climate change'
          },
          {
            field: 'TableSchemaIds'
          },
          {
            field: 'hasRecord'
          }
        ]
      }
    });
  };

  const manageDialogs = (dialog, value) => {
    tableManagementDispatch({ type: 'MANAGE_DIALOGS', payload: { dialog, value } });
  };

  const onDeleteRow = async () => {
    try {
      const isDataDeleted = await DatasetService.deleteRecordById(datasetId, deleteId);
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

  const onLoadParentTablesData = () => {
    const configParentTables = Object.keys(
      Article15Utils.getWebformTabs(
        tables.map(table => table.name),
        schemaTables,
        tables
      )
    );
    const parentTables = schemaTables.filter(schemaTable => configParentTables.includes(schemaTable.header));
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

  const addTableTemplate = (rowData, colData) => {
    // console.log({ rowData, colData });
    // console.log(
    //   schemaTables.filter(
    //     schemaTable => schemaTable.header === `Table_${colData.field.substring(colData.field.length - 1)}`
    //   )[0]
    // );
    return (
      <div style={{ display: 'flex', justifyContent: 'center' }}>
        <Button
          className={'p-button-secondary'}
          icon={'add'}
          label={resources.messages['webformTableCreation']}
          onClick={() => {
            console.log({ parentTablesWithData, rowData, colData });

            const configParentTables = Object.keys(
              Article15Utils.getWebformTabs(
                tables.map(table => table.name),
                schemaTables,
                tables
              )
            );
            onAddTableRecord(
              schemaTables.filter(
                schemaTable =>
                  configParentTables.includes(colData.field) && schemaTable.tableSchemaName === colData.field
              )[0],
              rowData.Id
            );
          }}
          // onClick={() => setTableToCreate(rowData.table3)}
        />
      </div>
    );
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

  const renderTableColumns = () => {
    const data = tableColumns.map(col => (
      <Column
        className={col.field === 'TableSchemaIds' || col.field === 'hasRecord' ? styles.invisibleHeader : ''}
        key={col.field}
        field={col.field}
        header={col.header}
        body={col.body}
      />
    ));

    data.push(renderActionButtonsColumn);

    return data;
  };

  return (
    <Fragment>
      <DataTable
        autoLayout={true}
        loading={loading}
        onRowClick={event =>
          tableManagementDispatch({ type: 'GET_DELETE_ID', payload: { deleteId: event.data.recordId } })
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
          ARE YOU SURE YOU WANT TO DELETE
        </ConfirmDialog>
      )}

      {isDialogVisible.manageRows && (
        <Dialog
          className={`calendar-table ${styles.addEditRecordDialog}`}
          // footer={editRowDialogFooter}
          header={resources.messages['editRow']}
          modal={true}
          onHide={() => manageDialogs('manageRows', false)}
          visible={isDialogVisible.manageRows}
          zIndex={3003}>
          <div className="p-grid p-fluid">
            <DataForm
              colsSchema={colsSchema}
              datasetId={datasetId}
              editDialogVisible={isDialogVisible.manageRows}
              formType="EDIT"
              getTooltipMessage={''}
              hasWritePermissions={true}
              // onChangeForm={onEditAddFormInput}
              // onShowCoordinateError={onShowCoordinateError}
              // onShowFieldInfo={onShowFieldInfo}
              records={tableManagementState.records}
              // reporting={reporting}
            />
          </div>
        </Dialog>
      )}
    </Fragment>
  );
};
