/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef } from 'react';
import { withRouter } from 'react-router-dom';

import isUndefined from 'lodash/isUndefined';

import styles from './DataViewer.module.css';

import { ButtonsBar } from 'ui/views/_components/ButtonsBar';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { CustomIconTooltip } from './_components/CustomIconTooltip';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Growl } from 'primereact/growl';
import { ReporterDataSetContext } from 'ui/views/ReporterDataSet/_components/_context/ReporterDataSetContext';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const DataViewer = withRouter(
  React.memo(
    ({
      positionIdRecord,
      tableSchemaColumns,
      tableId,
      idSelectedRow,
      urlViewer,
      buttonsList = undefined,
      tableName,
      match: {
        params: { dataSetId }
      }
    }) => {
      const contextReporterDataSet = useContext(ReporterDataSetContext);
      const [importDialogVisible, setImportDialogVisible] = useState(false);
      const [totalRecords, setTotalRecords] = useState(0);
      const [fetchedData, setFetchedData] = useState([]);
      const [loading, setLoading] = useState(false);
      const [numRows, setNumRows] = useState(10);
      const [firstRow, setFirstRow] = useState(
        positionIdRecord && positionIdRecord !== null ? Math.floor(positionIdRecord / numRows) * numRows : 0
      );
      const [sortOrder, setSortOrder] = useState();
      const [sortField, setSortField] = useState();
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
        if (firstRow !== positionIdRecord) {
          setFirstRow(Math.floor(positionIdRecord / numRows) * numRows);
        }

        let colOpt = [];
        for (let col of cols) {
          colOpt.push({ label: col.header, value: col });
        }
        setColOptions(colOpt);

        if (positionIdRecord !== 0 || fetchedData.length === 0) {
          fetchDataHandler(null, sortOrder, Math.floor(positionIdRecord / numRows) * numRows, numRows);
        }

        const inmTableSchemaColumns = [...tableSchemaColumns];
        inmTableSchemaColumns.push({ table: inmTableSchemaColumns[0].table, field: 'id', header: '' });
        setCols(inmTableSchemaColumns);
      }, [positionIdRecord]);

      useEffect(() => {
        // let visibilityIcon = (<div className="TableDiv">
        //     <span className="pi pi-eye" style={{zoom:2}}></span>
        //     <span className="my-multiselected-empty-token">Visibility</span>
        //   </div>
        // );
        // let headerArr = <div className="TableDiv">
        //     <i className="pi pi-eye"></i>
        //     <MultiSelect value={cols} options={colOptions} tooltip="Filter columns" onChange={onColumnToggleHandler} style={{width:'10%'}} placeholder={visibilityIcon} filter={true} fixedPlaceholder={true}/>
        // </div>;
        // setHeader(headerArr);

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

      const onChangePageHandler = event => {
        setNumRows(event.rows);
        setFirstRow(event.first);
        contextReporterDataSet.setPageHandler(event.first);
        contextReporterDataSet.setIdSelectedRowHandler(-1);
        if (event.first === 0) {
          fetchDataHandler(sortField, sortOrder, event.first, event.rows);
        }
      };

      const onConfirmDeleteHandler = () => {
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

      const setVisibleHandler = (fnUseState, visible) => {
        fnUseState(visible);
      };

      const onSortHandler = event => {
        console.log('Sorting...');
        setSortOrder(event.sortOrder);
        setSortField(event.sortField);
        contextReporterDataSet.setPageHandler(0);
        contextReporterDataSet.setIdSelectedRowHandler(-1);
        fetchDataHandler(event.sortField, event.sortOrder, firstRow, numRows);
      };

      const onRefreshClickHandler = () => {
        contextReporterDataSet.setPageHandler(0);
        contextReporterDataSet.setIdSelectedRowHandler(-1);
        fetchDataHandler(null, sortOrder, firstRow, numRows);
      };

      // const onColumnToggleHandler = (event) =>{
      //   console.log("OnColumnToggle...");
      //   setCols(event.value);
      //   setColOptions(colOptions);
      // }

      // useEffect(()=>{
      //   console.log("Fetching new data...");
      // console.log(fetchedData);
      // },[fetchedData]);

      const fetchDataHandler = (sField, sOrder, fRow, nRows) => {
        setLoading(true);

        let queryString = {
          idTableSchema: tableId,
          pageNum: Math.floor(fRow / nRows),
          pageSize: nRows
        };

        if (sField !== undefined && sField !== null) {
          queryString.fields = sField;
          queryString.asc = sOrder === -1 ? 0 : 1;
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

          return <CustomIconTooltip levelError={levelError} message={message} />;
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
              {row ? row.fieldData[column.field] : null} <CustomIconTooltip levelError={levelError} message={message} />
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
          clickHandler: () => setImportDialogVisible(true)
        },
        {
          label: resources.messages['deleteTable'],
          icon: '2',
          group: 'left',
          disabled: false,
          clickHandler: () => setVisibleHandler(setDeleteDialogVisible, true)
        },
        {
          label: resources.messages['visibility'],
          icon: '6',
          group: 'left',
          disabled: true,
          clickHandler: null
        },
        {
          label: resources.messages['filter'],
          icon: '7',
          group: 'left',
          disabled: true,
          clickHandler: null
        },
        {
          label: resources.messages['group-by'],
          icon: '8',
          group: 'left',
          disabled: true,
          clickHandler: null
        },
        {
          label: resources.messages['sort'],
          icon: '9',
          group: 'left',
          disabled: true,
          clickHandler: null
        },
        {
          label: resources.messages['refresh'],
          icon: '11',
          group: 'right',
          disabled: true,
          clickHandler: onRefreshClickHandler
        }
      ];

      const onHideHandler = () => {
        setImportDialogVisible(false);
      };

      const editLargeStringWithDots = (string, length) => {
        if (string.length > length) {
          return string.substring(0, length).concat('...');
        } else {
          return string;
        }
      };

      const onUploadHandler = () => {
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

        return { 'p-highlight': id === idSelectedRow };
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
              onPage={onChangePageHandler}
              rowsPerPageOptions={[5, 10, 20, 100]}
              lazy={true}
              loading={loading}
              totalRecords={totalRecords}
              sortable={true}
              onSort={onSortHandler}
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
            onHide={onHideHandler}>
            <CustomFileUpload
              mode="advanced"
              name="file"
              url={`${window.env.REACT_APP_BACKEND}/dataset/${dataSetId}/loadTableData/${tableId}`}
              onUpload={onUploadHandler}
              multiple={false}
              chooseLabel={resources.messages['selectFile']} //allowTypes="/(\.|\/)(csv|doc)$/"
              fileLimit={1}
              className={styles.FileUpload}
            />
          </Dialog>

          <ReporterDataSetContext.Provider>
            <ConfirmDialog
              onConfirm={onConfirmDeleteHandler}
              onHide={() => setVisibleHandler(setDeleteDialogVisible, false)}
              visible={deleteDialogVisible}
              header={resources.messages['deleteDatasetTableHeader']}
              maximizable={false}
              labelConfirm={resources.messages['yes']}
              labelCancel={resources.messages['no']}>
              {resources.messages['deleteDatasetTableConfirm']}
            </ConfirmDialog>
          </ReporterDataSetContext.Provider>
        </div>
      );
    }
  )
);

export { DataViewer };
