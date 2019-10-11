/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext } from 'react';
import PropTypes from 'prop-types';

import isUndefined from 'lodash/isUndefined';

import styles from './ValidationViewer.module.css';

import { DataTable } from 'ui/views/_components/DataTable';
import { Column } from 'primereact/column';

import { Button } from 'ui/views/_components/Button';
import { ReporterDatasetContext } from 'ui/views/ReporterDataSet/_components/_context/ReporterDataSetContext';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DatasetService } from 'core/services/DataSet';

const ValidationViewer = React.memo(({ visible, datasetId, buttonsList = undefined, hasWritePermissions }) => {
  const contextReporterDataset = useContext(ReporterDatasetContext);
  const resources = useContext(ResourcesContext);
  const [columns, setColumns] = useState([]);
  const [fetchedData, setFetchedData] = useState([]);
  const [firstRow, setFirstRow] = useState(0);
  const [loading, setLoading] = useState(false);
  const [numRows, setNumRows] = useState(10);
  const [sortField, setSortField] = useState('');
  const [sortOrder, setSortOrder] = useState(0);
  const [totalRecords, setTotalRecords] = useState(0);

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
    columnsArr.push(
      <Column key="datasetPartitionId" field="datasetPartitionId" header="" className={styles.VisibleHeader} />
    );
    columnsArr.push(<Column key="tableSchemaId" field="tableSchemaId" header="" className={styles.VisibleHeader} />);
    setColumns(columnsArr);
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
    const datasetErrors = await DatasetService.errorsById(datasetId, Math.floor(fRow / nRows), nRows, sField, sOrder);
    setTotalRecords(datasetErrors.totalErrors);
    setFetchedData(datasetErrors.errors);
    setLoading(false);
  };

  const onLoadErrorPosition = async (objectId, datasetId, entityType) => {
    const errorPosition = await DatasetService.errorPositionByObjectId(objectId, datasetId, entityType);
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
        const datasetError = await onLoadErrorPosition(event.data.objectId, datasetId, event.data.entityType);
        contextReporterDataset.onSelectValidation(
          event.data.tableSchemaId,
          datasetError.position,
          datasetError.recordId
        );
        contextReporterDataset.onValidationsVisible();
        break;
      case 'TABLE':
        contextReporterDataset.onSelectValidation(event.data.tableSchemaId, -1, -1);
        contextReporterDataset.onValidationsVisible();
        break;
      default:
        break;
    }
  };

  let totalCount = <span>Total: {totalRecords} rows</span>;

  return (
    <div>
      {!isUndefined(buttonsList) ? (
        buttonsList
      ) : (
        <Toolbar className={styles.validationToolbar}>
          <div className="p-toolbar-group-left">
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'eye'}
              label={resources.messages['visibility']}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'filter'}
              label={resources.messages['filter']}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'groupBy'}
              label={resources.messages['groupBy']}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'sort'}
              label={resources.messages['sort']}
            />
          </div>
          <div className="p-toolbar-group-right">
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={false}
              icon={'refresh'}
              label={resources.messages['refresh']}
              onClick={() => fetchData('', sortOrder, firstRow, numRows)}
            />
          </div>
        </Toolbar>
      )}
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
