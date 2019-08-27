/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, Suspense, useContext } from 'react';
import PropTypes from 'prop-types';

import isUndefined from 'lodash/isUndefined';

import styles from './ValidationViewer.module.css';

import { config } from 'assets/conf';

import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';

import { ButtonsBar } from 'ui/views/_components/ButtonsBar';
import { ReporterDataSetContext } from 'ui/views/ReporterDataSet/_components/_context/ReporterDataSetContext';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const ValidationViewer = React.memo(({ visible, dataSetId, buttonsList = undefined }) => {
  const resources = useContext(ResourcesContext);
  const contextReporterDataSet = useContext(ReporterDataSetContext);
  const [totalRecords, setTotalRecords] = useState(0);
  const [fetchedData, setFetchedData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [numRows, setNumRows] = useState(10);
  const [firstRow, setFirstRow] = useState(0);
  const [sortOrder, setSortOrder] = useState();
  const [sortField, setSortField] = useState();
  const [columns, setColumns] = useState([]);
  const [header] = useState();

  useEffect(() => {
    const headers = [
      {
        id: 'nameTableSchema',
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
        id: 'typeEntity',
        header: resources.messages['typeEntity']
      }
    ];
    let columnsArr = headers.map(col => <Column sortable={true} key={col.id} field={col.id} header={col.header} />);
    columnsArr.push(<Column key="idRecord" field="idRecord" header="" className={styles.VisibleHeader} />);
    columnsArr.push(<Column key="idTableSchema" field="idTableSchema" header="" className={styles.VisibleHeader} />);
    setColumns(columnsArr);

    fetchDataHandler(null, sortOrder, firstRow, numRows);
  }, []);

  useEffect(() => {
    if (visible) {
      fetchDataHandler(null, sortOrder, firstRow, numRows);
    }
  }, [visible]);

  const onChangePageHandler = event => {
    setNumRows(event.rows);
    setFirstRow(event.first);
    fetchDataHandler(sortField, sortOrder, event.first, event.rows);
  };

  const onSortHandler = event => {
    setSortOrder(event.sortOrder);
    setSortField(event.sortField);
    fetchDataHandler(event.sortField, event.sortOrder, firstRow, numRows);
  };

  // const onColumnToggleHandler = (event) =>{
  //   console.log("OnColumnToggle...");
  //   setCols(event.value);
  //   setColOptions(colOptions);
  // }

  const fetchDataHandler = (sField, sOrder, fRow, nRows) => {
    setLoading(true);

    let queryString = {
      dataSetId: dataSetId,
      pageNum: Math.floor(fRow / nRows),
      pageSize: nRows
    };

    if (sField !== undefined && sField !== null) {
      queryString.fields = sField;
      queryString.asc = sOrder === -1 ? 0 : 1;
    }

    const dataPromise = HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? '/jsons/list-of-errors.json' : `${config.listValidationsAPI.url}${dataSetId}`,
      queryString: queryString
    });
    dataPromise
      .then(res => {
        setTotalRecords(res.data.totalErrors);
        filterDataResponse(res.data);
        setLoading(false);
      })
      .catch(error => {
        console.log(error);
        return error;
      });
  };

  const filterDataResponse = data => {
    setFetchedData(data.errors);
  };

  const onRowSelectHandler = event => {
    switch (event.data.typeEntity) {
      case 'FIELD':
      case 'RECORD':
        let queryString = {
          datasetId: dataSetId,
          type: event.data.typeEntity
        };
        const dataPromise = HTTPRequester.get({
          url: window.env.REACT_APP_JSON
            ? `${config.validationViewerAPI.url}${event.data.idObject}`
            : `${config.validationViewerAPI.url}${event.data.idObject}`,
          queryString: queryString
        });

        dataPromise
          .then(res => {
            contextReporterDataSet.setValidationHandler(event.data.idTableSchema, res.data.position, res.data.idRecord);
            //contextReporterDataSet.setPageHandler(res.data.position);
            //contextReporterDataSet.setIdSelectedRowHandler(res.data.idRecord);
            contextReporterDataSet.validationsVisibleHandler();
          })
          .catch(error => {
            console.log(error);
            return error;
          });
        break;
      case 'TABLE':
        contextReporterDataSet.setValidationHandler(event.data.idTableSchema, -1, -1);
        //contextReporterDataSet.setPageHandler(0);
        contextReporterDataSet.validationsVisibleHandler();
        break;
      default:
        //contextReporterDataSet.validationsVisibleHandler();
        break;
    }
  };

  const defaultButtonsList = [
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
      disabled: false,
      clickHandler: () => fetchDataHandler(null, sortOrder, firstRow, numRows)
    }
  ];

  let totalCount = <span>Total: {totalRecords} rows</span>;

  return (
    <div>
      <Suspense fallback={<div>Loading...</div>}>
        <ButtonsBar buttonsList={!isUndefined(buttonsList) ? buttonsList : defaultButtonsList} />
      </Suspense>
      <div>
        <DataTable
          value={fetchedData}
          paginatorRight={totalCount}
          resizableColumns={true}
          reorderableColumns={true}
          paginator={true}
          rows={numRows}
          first={firstRow}
          onPage={onChangePageHandler}
          rowsPerPageOptions={[5, 10, 15]}
          lazy={true}
          loading={loading}
          totalRecords={totalRecords}
          sortable={true}
          onSort={onSortHandler}
          header={header}
          sortField={sortField}
          sortOrder={sortOrder}
          autoLayout={true}
          selectionMode="single"
          onRowSelect={onRowSelectHandler}>
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
