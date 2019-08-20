/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef } from 'react';
import { withRouter } from 'react-router-dom';

import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';

import isUndefined from 'lodash/isUndefined';

import { config } from 'conf';

import styles from './DataViewer.module.css';

import { Button } from 'ui/views/_components/Button';
import { ButtonsBar } from 'ui/views/_components/ButtonsBar';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { IconTooltip } from './_components/IconTooltip';
import { InputText } from 'ui/views/_components/InputText';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { Growl } from 'primereact/growl';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { getUrl } from 'core/infrastructure/getUrl';
import { DataSetService } from 'core/services/DataSet';

const DataViewer = withRouter(
  React.memo(
    ({
      buttonsList = undefined,
      recordPositionId,
      selectedRowId,
      tableId,
      tableName,
      tableSchemaColumns,
      match: {
        params: { dataSetId }
      }
    }) => {
      //const contextReporterDataSet = useContext(ReporterDataSetContext);
      const [addDialogVisible, setAddDialogVisible] = useState(false);
      const [columnOptions, setColumnOptions] = useState([{}]);
      const [colsSchema, setColsSchema] = useState(tableSchemaColumns);
      const [columns, setColumns] = useState([]);
      const [confirmDeleteVisible, setConfirmDeleteVisible] = useState(false);
      const [defaultButtonsList, setDefaultButtonsList] = useState([]);
      const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
      const [fetchedData, setFetchedData] = useState([]);
      const [firstRow, setFirstRow] = useState(0);
      const [header] = useState();
      const [importDialogVisible, setImportDialogVisible] = useState(false);
      const [isDataDeleted, setIsDataDeleted] = useState(false);
      const [loading, setLoading] = useState(false);
      const [numRows, setNumRows] = useState(10);
      const [selectedRow, setSelectedRow] = useState({});
      const [sortField, setSortField] = useState(undefined);
      const [sortOrder, setSortOrder] = useState(undefined);
      const [totalRecords, setTotalRecords] = useState(0);

      const resources = useContext(ResourcesContext);

      let growlRef = useRef();

      useEffect(() => {
        setDefaultButtonsList([
          {
            label: resources.messages['import'],
            icon: 'export',
            group: 'left',
            disabled: false,
            onClick: () => setImportDialogVisible(true)
          },
          {
            label: resources.messages['deleteTable'],
            icon: 'trash',
            group: 'left',
            disabled: false,
            onClick: () => onSetVisible(setDeleteDialogVisible, true)
          },
          {
            label: resources.messages['visibility'],
            icon: 'eye',
            group: 'left',
            disabled: true,
            onClick: null
          },
          {
            label: resources.messages['filter'],
            icon: 'filter',
            group: 'left',
            disabled: true,
            onClick: null
          },
          {
            label: resources.messages['groupBy'],
            icon: 'groupBy',
            group: 'left',
            disabled: true,
            onClick: null
          },
          {
            label: resources.messages['sort'],
            icon: 'sort',
            group: 'left',
            disabled: true,
            onClick: null
          },
          {
            label: resources.messages['refresh'],
            icon: 'refresh',
            group: 'right',
            disabled: true,
            onClick: onRefresh
          }
        ]);

        let colOptions = [];
        for (let colSchema of colsSchema) {
          colOptions.push({ label: colSchema.header, value: colSchema });
        }
        setColumnOptions(colOptions);

        const inmTableSchemaColumns = [...tableSchemaColumns];
        inmTableSchemaColumns.push({ table: inmTableSchemaColumns[0].table, field: 'id', header: '' });
        setColsSchema(inmTableSchemaColumns);

        onFetchData(undefined, undefined, 0, numRows);
      }, []);

      useEffect(() => {
        setFetchedData([]);
      }, [isDataDeleted]);

      useEffect(() => {
        if (isUndefined(recordPositionId) || recordPositionId === -1) {
          return;
        }

        setFirstRow(Math.floor(recordPositionId / numRows) * numRows);
        setSortField(undefined);
        setSortOrder(undefined);
        onFetchData(undefined, undefined, Math.floor(recordPositionId / numRows) * numRows, numRows);
      }, [recordPositionId]);

      useEffect(() => {
        let columnsArr = colsSchema.map(column => {
          let sort = column.field === 'id' ? false : true;
          let visibleColumn = column.field === 'id' ? styles.VisibleHeader : '';
          return (
            <Column
              body={dataTemplate}
              className={visibleColumn}
              editor={row => cellDataEditor(row, column.field)}
              editorValidator={requiredValidator}
              field={column.field}
              header={column.header}
              key={column.field}
              sortable={sort}
            />
          );
        });
        let editCol = (
          <Column key="actions" body={row => actionTemplate(row)} style={{ width: '100px', height: '45px' }} />
        );

        let validationCol = (
          <Column
            body={validationsTemplate}
            field="validations"
            header=""
            key="recordValidation"
            style={{ width: '15px' }}
          />
        );
        let newColumnsArr = [validationCol].concat(columnsArr);
        let newColumnsArr2 = [editCol].concat(newColumnsArr);
        setColumns(newColumnsArr2);
      }, [colsSchema, columnOptions]);

      const onChangePage = event => {
        setNumRows(event.rows);
        setFirstRow(event.first);
        onFetchData(sortField, sortOrder, event.first, event.rows);
      };

      const onConfirmDelete = async () => {
        setDeleteDialogVisible(false);
        const dataDeleted = await DataSetService.deleteTableDataById(dataSetId);
        if (dataDeleted) {
          setIsDataDeleted(true);
        }
      };

      const onConfirmDeleteRow = () => {
        let inmFetchedData = [...fetchedData];
        inmFetchedData = inmFetchedData.filter(d => d.id !== selectedRow.id);
        setFetchedData(inmFetchedData);
      };

      const onDeleteRow = () => {
        setConfirmDeleteVisible(true);
      };

      const onFetchData = async (sField, sOrder, fRow, nRows) => {
        setLoading(true);

        let fields;
        if (!isUndefined(sField) && sField !== null) {
          fields = `${sField}:${sOrder}`;
        }

        const tableData = await DataSetService.tableDataById(
          dataSetId,
          tableId,
          Math.floor(fRow / nRows),
          nRows,
          fields
        );

        if (!isUndefined(tableData.records)) {
          filterDataResponse(tableData);
        }
        if (tableData.totalRecords !== totalRecords) {
          setTotalRecords(tableData.totalRecords);
        }

        setLoading(false);
      };

      const onHide = () => {
        setImportDialogVisible(false);
      };

      const onHideConfirmDeleteDialog = () => {
        setConfirmDeleteVisible(false);
      };

      const onRefresh = () => {
        onFetchData(sortField, sortOrder, firstRow, numRows);
      };

      const onRowSelect = (event, value) => {
        console.log(value, event);

        setSelectedRow(value);
      };

      const onSaveRow = () => {};

      const onSetVisible = (fnUseState, visible) => {
        fnUseState(visible);
      };

      const onSort = event => {
        setSortOrder(event.sortOrder);
        setSortField(event.sortField);
        setFirstRow(0);
        onFetchData(event.sortField, event.sortOrder, 0, numRows);
      };

      const onUpload = () => {
        setImportDialogVisible(false);

        const detailContent = (
          <span>
            {resources.messages['datasetLoadingMessage']}
            <strong>{editLargeStringWithDots(tableName, 22)}</strong>
            {resources.messages['datasetLoading']}
          </span>
        );

        growlRef.current.show({
          severity: 'info',
          summary: resources.messages['datasetLoadingTitle'],
          detail: detailContent,
          life: '5000'
        });
      };

      const cellDataEditor = props => {
        return (
          <InputText
            type="text"
            value={getCellValue(props, props.field)}
            onChange={e => onEditorValueChange(props, e.target.value)}
          />
        );
      };

      const requiredValidator = props => {
        let value = getCellValue(props, props.field);
        return value && value.length > 0;
      };

      const onEditorValueChange = (props, value) => {
        let updatedData = setCellValue([...props.value], props.rowIndex, props.field, value);
        setFetchedData(updatedData);
      };

      const getCellValue = (tableData, field) => {
        const value = tableData.rowData.dataRow.filter(data => data.fieldData[field]);
        return value.length > 0 ? value[0].fieldData[field] : '';
      };

      const setCellValue = (tableData, rowIndex, field, value) => {
        tableData[rowIndex].dataRow.filter(data => Object.keys(data.fieldData)[0] === field)[0].fieldData[
          field
        ] = value;
        return tableData;
      };

      const actionTemplate = () => {
        return (
          <div className={styles.actionTemplate}>
            <Button type="button" icon="edit" className={`${styles.editRowButton}`} />
            <Button type="button" icon="trash" className={styles.deleteRowButton} onClick={onDeleteRow} />
          </div>
        );
      };

      const addRowDialogFooter = (
        <div className="ui-dialog-buttonpane p-clearfix">
          <Button label={resources.messages['cancel']} icon="cancel" onClick={() => setAddDialogVisible(false)} />
          <Button label={resources.messages['save']} icon="save" onClick={onSaveRow} />
        </div>
      );

      const addRowFooter = (
        <div className="p-clearfix" style={{ width: '100%' }}>
          <Button
            style={{ float: 'left' }}
            label={resources.messages['add']}
            icon="add"
            onClick={() => {
              setAddDialogVisible(true);
            }}
          />
        </div>
      );

      const newRowForm = colsSchema.map((column, i) => {
        if (i < colsSchema.length - 1) {
          return (
            <React.Fragment key={column.field}>
              <div className="p-col-4" style={{ padding: '.75em' }}>
                <label htmlFor={column.field}>{column.header}</label>
              </div>
              <div className="p-col-8" style={{ padding: '.5em' }}>
                <InputText id={column.field} />
              </div>
            </React.Fragment>
          );
        }
      });

      const filterDataResponse = data => {
        const dataFiltered = data.records.map(record => {
          const recordValidations = record.validations;
          const arrayDataFields = record.fields.map(field => {
            return {
              fieldData: { [field.fieldSchemaId]: field.value },
              fieldValidations: field.validations
            };
          });
          arrayDataFields.push({ fieldData: { id: record.recordId }, fieldValidations: null });
          const arrayDataAndValidations = {
            dataRow: arrayDataFields,
            recordValidations
          };

          return arrayDataAndValidations;
        });

        setFetchedData(dataFiltered);
      };

      //Template for Record validation
      const validationsTemplate = (recordData, column) => {
        if (recordData.recordValidations && !isUndefined(recordData.recordValidations)) {
          const validations = recordData.recordValidations;

          let message = '';
          validations.forEach(validation => (validation.message ? (message += '- ' + validation.message + '\n') : ''));

          let levelError = '';
          let lvlFlag = 0;

          validations.forEach(validation => {
            if (validation.levelError === 'WARNING') {
              const wNum = 1;
              if (wNum > lvlFlag) {
                lvlFlag = wNum;
                levelError = 'WARNING';
              }
            } else if (validation.levelError === 'ERROR') {
              const eNum = 2;
              if (eNum > lvlFlag) {
                lvlFlag = eNum;
                levelError = 'ERROR';
              }
            } else if (validation.levelError === 'BLOCKER') {
              const bNum = 2;
              if (bNum > lvlFlag) {
                lvlFlag = bNum;
                levelError = 'BLOCKER';
              }
            }
          });

          return <IconTooltip levelError={levelError} message={message} />;
        } else {
          return;
        }
      };

      //Template for Field validation
      const dataTemplate = (rowData, column) => {
        let row = rowData.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
        if (row !== null && row && row.fieldValidations !== null && !isUndefined(row.fieldValidations)) {
          const validations = row.fieldValidations;
          let message = [];
          validations.forEach(validation => (validation.message ? (message += '- ' + validation.message + '\n') : ''));
          let levelError = '';
          let lvlFlag = 0;
          validations.forEach(validation => {
            if (validation.levelError === 'WARNING') {
              const wNum = 1;
              if (wNum > lvlFlag) {
                lvlFlag = wNum;
                levelError = 'WARNING';
              }
            } else if (validation.levelError === 'ERROR') {
              const eNum = 2;
              if (eNum > lvlFlag) {
                lvlFlag = eNum;
                levelError = 'ERROR';
              }
            } else if (validation.levelError === 'BLOCKER') {
              const bNum = 2;
              if (bNum > lvlFlag) {
                lvlFlag = bNum;
                levelError = 'BLOCKER';
              }
            }
          });

          return (
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
              }}>
              {' '}
              {row ? row.fieldData[column.field] : null} <IconTooltip levelError={levelError} message={message} />
            </div>
          );
        } else {
          return (
            <div style={{ display: 'flex', alignItems: 'center' }}>{row ? row.fieldData[column.field] : null}</div>
          );
        }
      };

      const editLargeStringWithDots = (string, length) => {
        if (string.length > length) {
          return string.substring(0, length).concat('...');
        } else {
          return string;
        }
      };

      const rowClassName = rowData => {
        let id = rowData.dataRow.filter(r => Object.keys(r.fieldData)[0] === 'id')[0].fieldData.id;

        return { 'p-highlight': id === selectedRowId };
      };

      const totalCount = <span>Total: {totalRecords} rows</span>;

      return (
        <div>
          <ButtonsBar buttonsList={!isUndefined(buttonsList) ? buttonsList : defaultButtonsList} />
          <div className={styles.Table}>
            <DataTable
              autoLayout={true}
              editable={true}
              first={firstRow}
              footer={addRowFooter}
              header={header}
              lazy={true}
              loading={loading}
              onPage={onChangePage}
              onRowSelect={e => onRowSelect(e, e.data)}
              onSort={onSort}
              paginator={true}
              paginatorRight={totalCount}
              reorderableColumns={true}
              resizableColumns={true}
              rowClassName={rowClassName}
              rows={numRows}
              rowsPerPageOptions={[5, 10, 20, 100]}
              selectionMode="single"
              sortable={true}
              sortField={sortField}
              sortOrder={sortOrder}
              totalRecords={totalRecords}
              value={fetchedData}
              //scrollable={true}
              //frozenWidth="100px"
              // unfrozenWidth="600px"
            >
              {columns}
            </DataTable>
          </div>
          <Growl ref={growlRef} />
          <Dialog
            className={styles.Dialog}
            dismissableMask={false}
            header={resources.messages['uploadDataset']}
            onHide={onHide}
            visible={importDialogVisible}>
            <CustomFileUpload
              chooseLabel={resources.messages['selectFile']} //allowTypes="/(\.|\/)(csv|doc)$/"
              className={styles.FileUpload}
              fileLimit={1}
              mode="advanced"
              multiple={false}
              name="file"
              onUpload={onUpload}
              url={`${window.env.REACT_APP_BACKEND}${getUrl(config.loadDataTableAPI.url, {
                dataSetId: dataSetId,
                tableId: tableId
              })}`}
            />
          </Dialog>

          <ConfirmDialog
            header={resources.messages['deleteDatasetTableHeader']}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            maximizable={false}
            onConfirm={onConfirmDelete}
            onHide={() => onSetVisible(setDeleteDialogVisible, false)}
            visible={deleteDialogVisible}>
            {resources.messages['deleteDatasetTableConfirm']}
          </ConfirmDialog>
          <ConfirmDialog
            onConfirm={onConfirmDeleteRow}
            onHide={onHideConfirmDeleteDialog}
            visible={confirmDeleteVisible}
            header={resources.messages['deleteRow']}
            maximizable={false}
            labelConfirm="Yes"
            labelCancel="No">
            {resources.messages['confirmDeleteRow']}
            <hr />
            <table className={styles.Table}>
              <thead>
                {colsSchema.map((column, i) => {
                  return <th key={i}>{column.header}</th>;
                })}
              </thead>
              <tbody>
                <tr>
                  {Object.values(selectedRow)
                    .slice(1)
                    .map((row, i) => {
                      return <td key={i}>{row[row.fieldData]}</td>;
                    })}
                </tr>
              </tbody>
            </table>
          </ConfirmDialog>
          <Dialog
            blockScroll={false}
            contentStyle={{ maxHeight: '80%', overflow: 'auto' }}
            footer={addRowDialogFooter}
            header={resources.messages['addNewRow']}
            maximizable={true}
            modal={true}
            onHide={() => setAddDialogVisible(false)}
            style={{ width: '50%', height: '80%', overflow: 'auto' }}
            visible={addDialogVisible}>
            <div className="p-grid p-fluid">{newRowForm}</div>
          </Dialog>
        </div>
      );
    }
  )
);

export { DataViewer };
