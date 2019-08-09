/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, Suspense, useContext } from 'react';
import PropTypes from 'prop-types';

import isUndefined from 'lodash/isUndefined';

import styles from './ValidationViewer.module.css';

import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';

import { ButtonsBar } from 'ui/views/_components/ButtonsBar';
import { ReporterDataSetContext } from 'ui/views/ReporterDataSet/_components/_context/ReporterDataSetContext';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { DataSetService } from 'core/services/DataSet';

const ValidationViewer = React.memo(({ visible, dataSetId, buttonsList = undefined }) => {
  const contextReporterDataSet = useContext(ReporterDataSetContext);
  const resources = useContext(ResourcesContext);
  const [columns, setColumns] = useState([]);
  const [fetchedData, setFetchedData] = useState([]);
  const [firstRow, setFirstRow] = useState(0);
  const [loading, setLoading] = useState(false);
  const [numRows, setNumRows] = useState(10);
  const [sortField, setSortField] = useState('');
  const [sortOrder, setSortOrder] = useState(0);
  const [totalRecords, setTotalRecords] = useState(0);
  const [defaultButtonsList] = useState([
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
      disabled: false,
      onClick: () => fetchData('', sortOrder, firstRow, numRows)
    }
  ]);

  useEffect(() => {
    const headers = [
      {
        id: 'tableSchemaName',
        header: resources.messages['origin']
      },
      {
        id: 'levelError',
        header: resources.messages['levelError']
      },
      {
        id: 'message',
        header: resources.messages['errorMessage']
      },
      {
        id: 'entityType',
        header: resources.messages['entityType']
      }
    ];
    let columnsArr = headers.map(col => <Column sortable={true} key={col.id} field={col.id} header={col.header} />);
    columnsArr.push(<Column key="recordId" field="recordId" header="" className={styles.VisibleHeader} />);
    columnsArr.push(<Column key="idTableSchema" field="idTableSchema" header="" className={styles.VisibleHeader} />);
    setColumns(columnsArr);

    fetchData('', sortOrder, firstRow, numRows);
  }, []);

  useEffect(() => {
    if (visible) {
      fetchData('', sortOrder, firstRow, numRows);
    }
  }, [visible]);

  const onChangePage = event => {
    console.log('Refetching data ValidationViewer...');
    setNumRows(event.rows);
    setFirstRow(event.first);
    fetchData(sortField, sortOrder, event.first, event.rows);
  };

  const onLoadErrors = async (fRow, nRows, sField, sOrder) => {
    const dataSetErrors = await DataSetService.errorsById(dataSetId, Math.floor(fRow / nRows), nRows, sField, sOrder);
    setTotalRecords(dataSetErrors.totalErrors);
    setFetchedData(dataSetErrors.errors);
    setLoading(false);
  };

  const onSelectError = async (objectId, entityType) => {
    const dataSetError = await DataSetService.errorPositionByObjectId(objectId, dataSetId, entityType);
    contextReporterDataSet.onSetTab(dataSetError.tableSchemaId);
    contextReporterDataSet.onSetPage(dataSetError.position);
    contextReporterDataSet.onSetSelectedRowId(dataSetError.recordId);
    contextReporterDataSet.onValidationsVisible();
  };

  const onSort = event => {
    console.log('Sorting ValidationViewer...');
    setSortOrder(event.sortOrder);
    setSortField(event.sortField);
    fetchData(event.sortField, event.sortOrder, firstRow, numRows);
  };

  const fetchData = (sField, sOrder, fRow, nRows) => {
    setLoading(true);
    onLoadErrors(fRow, nRows, sField, sOrder);
  };

  const onRowSelect = event => {
    switch (event.data.entityType) {
      case 'FIELD':
      case 'RECORD':
        onSelectError(event.data.objectId, event.data.entityType);
        break;
      case 'TABLE':
        contextReporterDataSet.onSetTab(event.data.tableSchemaId);
        contextReporterDataSet.onSetPage(0);
        contextReporterDataSet.onValidationsVisible();
        break;
      default:
        break;
    }
  };

  let totalCount = <span>Total: {totalRecords} rows</span>;

  return (
    <div>
      <Suspense fallback={<div>Loading...</div>}>
        <ButtonsBar buttonsList={!isUndefined(buttonsList) ? buttonsList : defaultButtonsList} />
      </Suspense>
      <div>
        <DataTable
          autoLayout={true}
          first={firstRow}
          lazy={true}
          loading={loading}
          onRowSelect={onRowSelect}
          onPage={onChangePage}
          onSort={onSort}
          paginator={true}
          paginatorRight={totalCount}
          resizableColumns={true}
          reorderableColumns={true}
          rows={numRows}
          rowsPerPageOptions={[5, 10, 15]}
          sortable={true}
          sortField={sortField}
          sortOrder={sortOrder}
          totalRecords={totalRecords}
          selectionMode="single"
          value={fetchedData}>
          {columns}
        </DataTable>
      </div>
    </div>
  );
});

ValidationViewer.propTypes = {
  id: PropTypes.string,
  buttonsList: PropTypes.array
};

export { ValidationViewer };
