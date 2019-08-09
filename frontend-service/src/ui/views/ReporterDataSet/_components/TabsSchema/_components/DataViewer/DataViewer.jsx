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
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Growl } from 'primereact/growl';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const DataViewer = withRouter(
  React.memo(
    ({
      recordPositionId,
      tableSchemaColumns,
      tableId,
      selectedRowId,
      urlViewer,
      buttonsList = undefined,
      tableName,
      match: {
        params: { dataSetId }
      }
    }) => {
      //const contextReporterDataSet = useContext(ReporterDataSetContext);
      const [importDialogVisible, setImportDialogVisible] = useState(false);
      const [totalRecords, setTotalRecords] = useState(0);
      const [fetchedData, setFetchedData] = useState([]);
      const [loading, setLoading] = useState(false);
      const [numRows, setNumRows] = useState(10);
      const [firstRow, setFirstRow] = useState(0);
      const [sortOrder, setSortOrder] = useState(undefined);
      const [sortField, setSortField] = useState(undefined);
      const [columns, setColumns] = useState([]);
      const [cols, setCols] = useState(tableSchemaColumns);
      const [header] = useState();
      const [colOptions, setColOptions] = useState([{}]);
      const resources = useContext(ResourcesContext);
      const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
      const [isDataDeleted, setIsDataDeleted] = useState(false);

      let growlRef = useRef();

      useEffect(() => {
        setFetchedData([]);
      }, [isDataDeleted]);

      useEffect(() => {
        let colOpt = [];
        for (let col of cols) {
          colOpt.push({ label: col.header, value: col });
        }
        setColOptions(colOpt);

        const inmTableSchemaColumns = [...tableSchemaColumns];
        inmTableSchemaColumns.push({ table: inmTableSchemaColumns[0].table, field: 'id', header: '' });
        setCols(inmTableSchemaColumns);

        fetchData(undefined, undefined, 0, numRows);
      }, []);

      useEffect(() => {
        if (isUndefined(recordPositionId) || recordPositionId === -1) {
          return;
        }

        setFirstRow(Math.floor(recordPositionId / numRows) * numRows);
        setSortField(undefined);
        setSortOrder(undefined);
        fetchData(undefined, undefined, Math.floor(recordPositionId / numRows) * numRows, numRows);
      }, [recordPositionId]);

      useEffect(() => {
        let columnsArr = cols.map(col => {
          let sort = col.field === 'id' ? false : true;
          let visibleColumn = col.field === 'id' ? styles.VisibleHeader : '';
          return (
            <Column
              sortable={sort}
              key={col.field}
              field={col.field}
              header={col.header}
              body={dataTemplate}
              className={visibleColumn}
            />
          );
        });
        let validationCol = (
          <Column
            key="recordValidation"
            field="validations"
            header=""
            body={validationsTemplate}
            style={{ width: '15px' }}
          />
        );
        let newColumnsArr = [validationCol].concat(columnsArr);
        setColumns(newColumnsArr);
      }, [cols, colOptions]);

      const onChangePage = event => {
        setNumRows(event.rows);
        setFirstRow(event.first);
        fetchData(sortField, sortOrder, event.first, event.rows);
      };

      const onConfirmDelete = () => {
        setDeleteDialogVisible(false);
        HTTPRequester.delete({
          url: window.env.REACT_APP_JSON
            ? `/dataset/${dataSetId}/deleteImportTable/${tableId}`
            : `/dataset/${dataSetId}/deleteImportTable/${tableId}`,
          queryString: {}
        }).then(res => {
          setIsDataDeleted(true);
        });
      };

      const setVisible = (fnUseState, visible) => {
        fnUseState(visible);
      };

      const onSort = event => {
        setSortOrder(event.sortOrder);
        setSortField(event.sortField);
        setFirstRow(0);
        fetchData(event.sortField, event.sortOrder, 0, numRows);
      };

      const onRefresh = () => {
        fetchData(sortField, sortOrder, firstRow, numRows);
      };

      const fetchData = (sField, sOrder, fRow, nRows) => {
        setLoading(true);

        let queryString = {
          idTableSchema: tableId,
          pageNum: Math.floor(fRow / nRows),
          pageSize: nRows
        };

        if (!isUndefined(sField) && sField !== null) {
          queryString.fields = sField + ':' + sOrder;
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

      const filterDataResponse = data => {
        //TODO: Refactorizar
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

      const defaultButtonsList = [
        {
          label: resources.messages['import'],
          icon: '0',
          group: 'left',
          disabled: false,
          onClick: () => setImportDialogVisible(true)
        },
        {
          label: resources.messages['deleteTable'],
          icon: '2',
          group: 'left',
          disabled: false,
          onClick: () => setVisible(setDeleteDialogVisible, true)
        },
        {
          label: resources.messages['visibility'],
          icon: '6',
          group: 'left',
          disabled: true,
          onClick: null
        },
        {
          label: resources.messages['filter'],
          icon: '7',
          group: 'left',
          disabled: true,
          onClick: null
        },
        {
          label: resources.messages['group-by'],
          icon: '8',
          group: 'left',
          disabled: true,
          onClick: null
        },
        {
          label: resources.messages['sort'],
          icon: '9',
          group: 'left',
          disabled: true,
          onClick: null
        },
        {
          label: resources.messages['refresh'],
          icon: '11',
          group: 'right',
          disabled: true,
          onClick: onRefresh
        }
      ];

      const onHide = () => {
        setImportDialogVisible(false);
      };

      const editLargeStringWithDots = (string, length) => {
        if (string.length > length) {
          return string.substring(0, length).concat('...');
        } else {
          return string;
        }
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

      const rowClassName = rowData => {
        let id = rowData.dataRow.filter(r => Object.keys(r.fieldData)[0] === 'id')[0].fieldData.id;

        return { 'p-highlight': id === selectedRowId };
      };

      let totalCount = <span>Total: {totalRecords} rows</span>;

      return (
        <div>
          <ButtonsBar buttonsList={!isUndefined(buttonsList) ? buttonsList : defaultButtonsList} />
          <div className={styles.Table}>
            <DataTable
              value={fetchedData}
              paginatorRight={totalCount}
              resizableColumns={true}
              reorderableColumns={true}
              paginator={true}
              rows={numRows}
              first={firstRow}
              onPage={onChangePage}
              rowsPerPageOptions={[5, 10, 20, 100]}
              lazy={true}
              loading={loading}
              totalRecords={totalRecords}
              sortable={true}
              onSort={onSort}
              header={header}
              sortField={sortField}
              sortOrder={sortOrder}
              autoLayout={true}
              rowClassName={rowClassName}>
              {columns}
            </DataTable>
          </div>
          <Growl ref={growlRef} />
          <Dialog
            header={resources.messages['uploadDataset']}
            visible={importDialogVisible}
            className={styles.Dialog}
            dismissableMask={false}
            onHide={onHide}>
            <CustomFileUpload
              mode="advanced"
              name="file"
              url={`${window.env.REACT_APP_BACKEND}/dataset/${dataSetId}/loadTableData/${tableId}`}
              onUpload={onUpload}
              multiple={false}
              chooseLabel={resources.messages['selectFile']} //allowTypes="/(\.|\/)(csv|doc)$/"
              fileLimit={1}
              className={styles.FileUpload}
            />
          </Dialog>

          <ConfirmDialog
            onConfirm={onConfirmDelete}
            onHide={() => setVisible(setDeleteDialogVisible, false)}
            visible={deleteDialogVisible}
            header={resources.messages['deleteDatasetTableHeader']}
            maximizable={false}
            labelConfirm={resources.messages['yes']}
            labelCancel={resources.messages['no']}>
            {resources.messages['deleteDatasetTableConfirm']}
          </ConfirmDialog>
        </div>
      );
    }
  )
);

export { DataViewer };
