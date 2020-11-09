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

import { useLoadColsSchemasAndColumnOptions } from 'ui/views/_components/DataViewer/_functions/Hooks/DataViewerHooks';

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
    parentTablesWithData: [],
    records: [],
    selectedRecord: {},
    tableColumns: [],
    tableSchemaColumns: []
  });

  const {
    isDialogVisible,
    parentTablesWithData,
    selectedRecord,
    tableColumns,
    tableSchemaColumns
  } = tableManagementState;

  const { colsSchema, columnOptions } = useLoadColsSchemasAndColumnOptions(tableManagementState.tableSchemaColumns);

  useEffect(() => {
    onLoadParentTablesData();
  }, [records]);

  useEffect(() => {
    if (!isEmpty(parentTablesWithData)) {
      initialLoad();
    }
  }, [parentTablesWithData]);

  const editRowDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      {/* <Button
        className={!isSaving && !records.isSaveDisabled && 'p-button-animated-blink'}
        disabled={isSaving || records.isSaveDisabled}
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
      /> */}
    </div>
  );

  const initialLoad = () => {
    console.log({ records });
    if (!isEmpty(records)) {
      // console.log({ parsedTables, parentTablesWithData });
      const parsedTables = DataViewerUtils.parseData(parentTablesWithData[0].data);
      const tableSchemaColumns = parseTableSchemaColumns(schemaTables);
      console.log({ tableSchemaColumns });
      const parsedRecordsWithValidations = parsePamsRecordsWithParentData(
        parsedTables,
        parentTablesWithData,
        schemaTables
      );

      console.log({ schemaTables });
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

  const manageDialogs = (dialog, value) => {
    tableManagementDispatch({ type: 'MANAGE_DIALOGS', payload: { dialog, value } });
  };

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

  const onEditFormInput = (property, value) =>
    tableManagementDispatch({ type: 'EDIT_SELECTED_RECORD', payload: { property, value } });

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
      console.log({ parentTableData });
      tableManagementDispatch({ type: 'SET_PARENT_TABLES_DATA', payload: parentTableData });
    });
  };

  const onSaveRecord = async record => {
    //Delete hidden column null values (datasetPartitionId and id)
    // record.dataRow = record.dataRow.filter(
    //   field => Object.keys(field.fieldData)[0] !== 'datasetPartitionId' && Object.keys(field.fieldData)[0] !== 'id'
    // );
    // //Check invalid coordinates and replace them
    // record = MapUtils.changeIncorrectCoordinates(record);
    // if (isNewRecord) {
    //   try {
    //     setIsSaving(true);
    //     await DatasetService.addRecordsById(datasetId, tableId, [parseMultiselect(record)]);
    //     setIsTableDeleted(false);
    //     onRefresh();
    //   } catch (error) {
    //     const {
    //       dataflow: { name: dataflowName },
    //       dataset: { name: datasetName }
    //     } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
    //     notificationContext.add({
    //       type: 'ADD_RECORDS_BY_ID_ERROR',
    //       content: { dataflowId, datasetId, dataflowName, datasetName, tableName }
    //     });
    //   } finally {
    //     if (!addAnotherOne) {
    //       setAddDialogVisible(false);
    //     }
    //     setIsLoading(false);
    //     setIsSaving(false);
    //   }
    // } else {
    //   try {
    //     await DatasetService.updateRecordsById(datasetId, parseMultiselect(record));
    //     onRefresh();
    //   } catch (error) {
    //     const {
    //       dataflow: { name: dataflowName },
    //       dataset: { name: datasetName }
    //     } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
    //     notificationContext.add({
    //       type: 'UPDATE_RECORDS_BY_ID_ERROR',
    //       content: { dataflowId, datasetId, dataflowName, datasetName, tableName }
    //     });
    //   } finally {
    //     onCancelRowEdit();
    //     setIsLoading(false);
    //     setIsSaving(false);
    //   }
    // }
  };

  const addTableTemplate = (rowData, colData) => {
    let hasRecord = false;
    rowData.dataRow.forEach(row =>
      row.fieldData.tableSchemas.forEach(tableSchema => {
        if (tableSchema.tableSchemaName === colData.field) {
          hasRecord = tableSchema.hasRecord;
        }
      })
    );

    const pamsIdFieldSchemaId = getFieldSchemaColumnIdByHeader(tableSchemaColumns, 'Id');
    const pamsFieldSchemaValue = RecordUtils.getCellValue({ rowData: rowData }, pamsIdFieldSchemaId);

    return (
      <div style={{ display: 'flex', justifyContent: 'center' }}>
        <Button
          className={'p-button-secondary'}
          icon={hasRecord ? 'edit' : 'add'}
          label={hasRecord ? resources.messages['webformTableEdit'] : resources.messages['webformTableCreation']}
          onClick={() => {
            if (hasRecord) {
              onSelectEditTable(pamsFieldSchemaValue, colData.field);
            } else {
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
                pamsFieldSchemaValue
              );
            }
          }}
          // onClick={() => setTableToCreate(rowData.table3)}
        />
      </div>
    );
  };

  const dataTemplate = (rowData, column) => {
    let field = rowData.dataRow.filter(row => Object.keys(row.fieldData)[0] === column.fieldSchemaId)[0];
    // console.log({ rowData, field, column });
    if (!isNil(field) && !isNil(field.fieldData) && !isNil(field.fieldValidations)) {
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

  const getListOfSingleOptions = () => {
    const singleRecords = tableManagementState.records.filter(record => record.IsGroup === 'Single');
    return singleRecords.map(record => ({ label: record.Title, value: record.Title }));
  };

  // const renderListOfSinglePamsTemplate = rowData => {
  //   if (rowData.IsGroup === 'Group') {
  //     return (
  //       <MultiSelect
  //         appendTo={document.body}
  //         maxSelectedLabels={10}
  //         id={rowData.recordId}
  //         onChange={event => {
  //           // onFillField(field, option, event.target.value);
  //           // if (isNil(field.recordId)) onSaveField(option, event.target.value);
  //           // else onEditorSubmitValue(field, option, event.target.value);
  //         }}
  //         options={getListOfSingleOptions()}
  //         // value={getMultiselectValues(
  //         //   field.codelistItems.map(codelist => ({ label: codelist, value: codelist })),
  //         //   field.value
  //         // )}
  //       />
  //     );
  //   } else return '-';
  // };

  const renderTableColumns = () => {
    console.log({ tableColumns, tableSchemaColumns });
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

    data.push(renderActionButtonsColumn);

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
          ARE YOU SURE YOU WANT TO DELETE
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
              // onShowCoordinateError={onShowCoordinateError}
              // onShowFieldInfo={onShowFieldInfo}
              selectedRecord={selectedRecord}
              records={tableManagementState.records}
              // reporting={reporting}
            />
          </div>
        </Dialog>
      )}
    </Fragment>
  );
};
