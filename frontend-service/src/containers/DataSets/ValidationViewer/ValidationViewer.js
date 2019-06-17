/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, Suspense, useContext } from 'react';
// import { MultiSelect } from 'primereact/multiselect';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';

import ReporterDataSetContext from '../../../components/Context/ReporterDataSetContext';

import styles from './ValidationViewer.module.css';
import ResourcesContext from '../../../components/Context/ResourcesContext';

import PropTypes from 'prop-types';
import HTTPRequester from '../../../services/HTTPRequester/HTTPRequester.js';

const ValidationViewer = (props) => {
  const resources = useContext(ResourcesContext);
    const contextReporterDataSet = useContext(ReporterDataSetContext);
    const [totalRecords, setTotalRecords] = useState(0);
    const [fetchedData, setFetchedData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [numRows, setNumRows] = useState(10);
    const [firstRow, setFirstRow] = useState(0);
    const [sortOrder, setSortOrder] = useState();   
    const [sortField,setSortField] = useState();
    const [columns, setColumns] = useState([]); 
    //const [cols, setCols] = useState([]); 
    const [header] = useState();
    //const [colOptions, setColOptions] = useState([{}]);    

    const ButtonsBar = React.lazy(() => import('../../../components/Layout/UI/ButtonsBar/ButtonsBar'));
    //TODO: Refactorizar porque estamos duplicando lógica con DataViewer (Seguramente haya que cargarse el TabsSchema)
    const customButtons = [
      {
          label: "Visibility",
          icon: "6",
          group: "left",
          disabled: true,
          clickHandler: null
      },
      {
          label: "Filter",
          icon: "7",
          group: "left",
          disabled: true,
          clickHandler: null
      },
      {
          label: "Group by",
          icon: "8",
          group: "left",
          disabled: true,
          clickHandler: null
      },
      {
          label: "Sort",
          icon: "9",
          group: "left",
          disabled: true,
          clickHandler: null
      },
      {
          label: "Refresh",
          icon: "11",
          group: "right",
          disabled: false,
          clickHandler: null
      }
  ];
  
      useEffect(()=>{         
        const headers = [{
              id: "nameTableSchema",
              header: resources.messages["origin"]
            },
            {
              id: "levelError",
              header: resources.messages["levelError"]
            },
            {
              id: "message",
              header: resources.messages["errorMessage"]
            },
            {
              id: "typeEntity",
              header: resources.messages["typeEntity"]
            }];
        let columnsArr = headers.map(col => <Column sortable={true} key={col.id} field={col.id} header={`${col.header}`} />);
        columnsArr.push(<Column key="idObject" field="idObject" header="" className={styles.VisibleHeader} />)
        columnsArr.push(<Column key="idTableSchema" field="idTableSchema" header="" className={styles.VisibleHeader} />)
        setColumns(columnsArr);   
        
        fetchDataHandler(null, sortOrder, firstRow, numRows);      

      }, []);
      
      const onChangePageHandler = (event)=>{     
        console.log('Refetching data ValidationViewer...');                       
        setNumRows(event.rows);
        setFirstRow(event.first);        
        fetchDataHandler(sortField, sortOrder, event.first, event.rows); 
      }
  
      const onSortHandler = (event)=>{      
        console.log("Sorting ValidationViewer...");
        setSortOrder(event.sortOrder);  
        setSortField(event.sortField);    
        fetchDataHandler(event.sortField, event.sortOrder, firstRow, numRows);       
      }
  
      // const onColumnToggleHandler = (event) =>{
      //   console.log("OnColumnToggle...");
      //   setCols(event.value);
      //   setColOptions(colOptions);
      // }
  

      const fetchDataHandler = (sField, sOrder, fRow, nRows) => {
        setLoading(true);

        //http://localhost:8030/dataset/listValidations/1?asc=true&fields=typeEntity&pageNum=0&pageSize=20
        
        let queryString = {
          idDataSet: props.idDataSet,
          pageNum: Math.floor(fRow / nRows),
          pageSize: nRows
        }

        if (sField !== undefined && sField !== null) {
          queryString.fields = sField;
          queryString.asc = sOrder === -1 ? 0 : 1;
        }       

        const dataPromise = HTTPRequester.get(
          {
            url:'/jsons/list-of-errors.json',
            queryString: queryString
          }
        );
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

      }

      const filterDataResponse = (data) =>{                
        setFetchedData(data.errors);
      }

      const filterLinkedDataResponse = (data) =>{  
        const dataFiltered = data.map(record => record.fields.map(f =>{
          return {[f.idFieldSchema]: f.value}
        }));
        let auxFiltered = {}
        let auxArrayFiltered = [];
        dataFiltered.forEach(dat => {
          dat.forEach(d=>auxFiltered = {...auxFiltered,...d});
          auxArrayFiltered.push(auxFiltered);
          auxFiltered={};
        });
        return auxArrayFiltered;
      }

      const onRowSelectHandler = (event) =>{      
        

        //http://localhost:8030/dataset/loadTableFromAnyObject/901?datasetId=1&pageSize=2&type=FIELD

        //`${config.validationViewerAPI.url}event.data.idObject`
        let queryString = {
          datasetId: props.idDataSet,
          pageSize: numRows,
          type: event.typeEntity
        }

        const dataPromise = HTTPRequester.get(
          {
            url: '/jsons/response_getTableFromAnyObjectId.json',
            queryString: queryString
          }
        );

        dataPromise
        .then(res => {
          contextReporterDataSet.validationsVisibleHandler();
          contextReporterDataSet.setTabHandler(event.data.idTableSchema);
          contextReporterDataSet.setLinkedErrorDataHandler(filterLinkedDataResponse(res.data.page.table.records));
        })
        .catch(error => {
          console.log(error);
          return error;
        });         
      }

      let totalCount = <span>Total: {totalRecords} rows</span>;

    return (
        <div>
            <Suspense fallback={<div>Loading...</div>}>
                <ButtonsBar buttons={(props.customButtons)?props.customButtons:customButtons} />
            </Suspense>
            <div>
                <DataTable value={fetchedData} paginatorRight={totalCount}
                       resizableColumns={true} reorderableColumns={true}
                       paginator={true} rows={numRows} first={firstRow} onPage={onChangePageHandler} 
                       rowsPerPageOptions={[5, 10, 20, 100]} lazy={true} 
                       loading={loading} totalRecords={totalRecords} sortable={true}
                       onSort={onSortHandler} header={header} sortField={sortField} sortOrder={sortOrder} autoLayout={true}
                       selectionMode="single" onRowSelect={onRowSelectHandler}>
                    {columns}
                </DataTable>
            </div>
        </div>
    );
}

ValidationViewer.propTypes = {
  id: PropTypes.string,
  customButtons: PropTypes.array
};

export default React.memo(ValidationViewer);