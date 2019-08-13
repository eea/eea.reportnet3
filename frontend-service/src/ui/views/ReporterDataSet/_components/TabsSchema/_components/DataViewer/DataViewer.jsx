/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef } from 'react';
import { withRouter } from 'react-router-dom';

import isUndefined from 'lodash/isUndefined';

import styles from './DataViewer.module.css';

import { ButtonsBar } from 'ui/views/_components/ButtonsBar';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { IconTooltip } from './_components/IconTooltip';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { Growl } from 'primereact/growl';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { DataSetService } from 'core/services/DataSet';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const DataViewer = withRouter(
  React.memo(
    ({
      buttonsList = undefined,
      recordPositionId,
      selectedRowId,
      tableId,
      tableName,
      tableSchemaColumns,
      urlViewer,
      match: {
        params: { dataSetId }
      }
    }) => {
      //const contextReporterDataSet = useContext(ReporterDataSetContext);
      const [colOptions, setColOptions] = useState([{}]);
      const [cols, setCols] = useState(tableSchemaColumns);
      const [columns, setColumns] = useState([]);
      const [defaultButtonsList, setDefaultButtonsList] = useState([]);
      const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
      const [fetchedData, setFetchedData] = useState([]);
      const [firstRow, setFirstRow] = useState(0);
      const [header] = useState();
      const [importDialogVisible, setImportDialogVisible] = useState(false);
      const [isDataDeleted, setIsDataDeleted] = useState(false);
      const [loading, setLoading] = useState(false);
      const [numRows, setNumRows] = useState(10);
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
            label: resources.messages['group-by'],
            icon: 'group-by',
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

        let colOpt = [];
        for (let col of cols) {
          colOpt.push({ label: col.header, value: col });
        }
        setColOptions(colOpt);

        const inmTableSchemaColumns = [...tableSchemaColumns];
        inmTableSchemaColumns.push({ table: inmTableSchemaColumns[0].table, field: 'id', header: '' });
        setCols(inmTableSchemaColumns);

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
        let columnsArr = cols.map(col => {
          let sort = col.field === 'id' ? false : true;
          let visibleColumn = col.field === 'id' ? styles.VisibleHeader : '';
          return (
            <Column
              body={dataTemplate}
              className={visibleColumn}
              field={col.field}
              header={col.header}
              key={col.field}
              sortable={sort}
            />
          );
        });
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
        setColumns(newColumnsArr);
      }, [cols, colOptions]);

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

      const onFetchData = (sField, sOrder, fRow, nRows) => {
        setLoading(true);

        let queryString = {
          idTableSchema: tableId,
          pageNum: Math.floor(fRow / nRows),
          pageSize: nRows
        };

        if (!isUndefined(sField) && sField !== null) {
          queryString.fields = `${sField}:${sOrder}`;
        }

        const dataPromise = HTTPRequester.get({
          url: window.env.REACT_APP_JSON ? '/jsons/response_dataset_values2.json' : urlViewer,
          queryString: queryString
        });
        dataPromise
          .then(response => {
            filterDataResponse(response.data);
            if (response.data.totalRecords !== totalRecords) {
              setTotalRecords(response.data.totalRecords);
            }

            setLoading(false);
          })
          .catch(error => {
            console.log(error);
            return error;
          });
      };

      const onHide = () => {
        setImportDialogVisible(false);
      };

      const onRefresh = () => {
        onFetchData(sortField, sortOrder, firstRow, numRows);
      };

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

      const filterDataResponse = data => {
        const dataFiltered = data.records.map(record => {
          const recordValidations = record.recordValidations;
          const arrayDataFields = record.fields.map(field => {
            return {
              fieldData: { [field.idFieldSchema]: field.value },
              fieldValidations: field.fieldValidations
            };
          });
          arrayDataFields.push({ fieldData: { id: record.id }, fieldValidations: null });
          const arrayDataAndValidations = {
            dataRow: arrayDataFields,
            recordValidations
          };

          return arrayDataAndValidations;
        });

        setFetchedData(dataFiltered);
      };

      //Template for Record validation
      const validationsTemplate = (fetchedData, column) => {
        if (fetchedData.recordValidations) {
          const validations = fetchedData.recordValidations.map(val => val.validation);

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
        if (row !== null && row && row.fieldValidations !== null) {
          const validations = row.fieldValidations.map(val => val.validation);
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
              header={header}
              first={firstRow}
              lazy={true}
              loading={loading}
              onPage={onChangePage}
              onSort={onSort}
              paginator={true}
              paginatorRight={totalCount}
              reorderableColumns={true}
              resizableColumns={true}
              rowClassName={rowClassName}
              rows={numRows}
              rowsPerPageOptions={[5, 10, 20, 100]}
              sortable={true}
              sortField={sortField}
              sortOrder={sortOrder}
              totalRecords={totalRecords}
              value={fetchedData}>
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
              url={`${window.env.REACT_APP_BACKEND}/dataset/${dataSetId}/loadTableData/${tableId}`}
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
        </div>
      );
    }
  )
);

export { DataViewer };
