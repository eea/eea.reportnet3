/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext } from 'react';
import PropTypes from 'prop-types';

import isUndefined from 'lodash/isUndefined';

import styles from './ValidationViewer.module.css';

import { DataTable } from 'ui/views/_components/DataTable';
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
    columnsArr.push(<Column key="tableSchemaId" field="tableSchemaId" header="" className={styles.VisibleHeader} />);
    setColumns(columnsArr);

    fetchData('', sortOrder, firstRow, numRows);
  }, []);

  useEffect(() => {
    if (visible) {
      fetchData('', sortOrder, firstRow, numRows);
    }
  }, [visible]);

  const onChangePage = event => {
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

  const onLoadErrorPosition = async (objectId, dataSetId, entityType) => {
    const errorPosition = await DataSetService.errorPositionByObjectId(objectId, dataSetId, entityType);
    return errorPosition;
  };

  const onSort = event => {
    setSortOrder(event.sortOrder);
    setSortField(event.sortField);
    fetchData(event.sortField, event.sortOrder, firstRow, numRows);
  };

  const fetchData = (sField, sOrder, fRow, nRows) => {
    setLoading(true);
    onLoadErrors(fRow, nRows, sField, sOrder);
  };

  const onRowSelect = async event => {
    switch (event.data.entityType) {
      case 'FIELD':
      case 'RECORD':
        const dataSetError = await onLoadErrorPosition(event.data.objectId, dataSetId, event.data.entityType);
        contextReporterDataSet.onSelectValidation(
          event.data.tableSchemaId,
          dataSetError.position,
          dataSetError.recordId
        );
        contextReporterDataSet.onValidationsVisible();
        break;
      case 'TABLE':
        contextReporterDataSet.onSelectValidation(event.data.tableSchemaId, -1, -1);
        contextReporterDataSet.onValidationsVisible();
        break;
      default:
        break;
    }
  };

  let totalCount = <span>Total: {totalRecords} rows</span>;

  return (
    <div>
      <ButtonsBar buttonsList={!isUndefined(buttonsList) ? buttonsList : defaultButtonsList} />
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
