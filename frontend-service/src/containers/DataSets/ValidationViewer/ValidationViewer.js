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
import config from '../../../conf/web.config.json';


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
        let columnsArr = headers.map(col => <Column sortable={true} key={col.id} field={col.id} header={col.header} />);
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
            url:`${config.listValidationsAPI.url}1`,
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

        const dataFiltered = data.records.map(record => {
          const recordValidations = record.recordValidations;
          const arrayDataFields = record.fields.map(field => {
            return { 
              fieldData: {[field.idFieldSchema]: field.value},
              fieldValidations : field.fieldValidations
             };
          });
          const arrayDataAndValidations = {
            dataRow: arrayDataFields,
            recordValidations
          };
    
          return arrayDataAndValidations;
        });    

        return dataFiltered;
      }



      const onRowSelectHandler = (event) =>{      
        //http://localhost:8030/dataset/loadTableFromAnyObject/901?datasetId=1&pageSize=2&type=FIELD
        switch (event.data.typeEntity) {
          case "FIELD":
          case "RECORD":
              let queryString = {
                datasetId: props.idDataSet,                
                type: event.data.typeEntity
              }
              const dataPromise = HTTPRequester.get(
                {
                  url: `${config.validationViewerAPI.url}${event.data.idObject}`,
                  queryString: queryString
                }
              );
    
              dataPromise
              .then(res => {                
                contextReporterDataSet.setTabHandler(event.data.idTableSchema);
                contextReporterDataSet.setPageHandler(res.data.position); 
                contextReporterDataSet.validationsVisibleHandler();
              })
              .catch(error => {
                console.log(error);
                return error;
              });   
            break;
          case "TABLE":     
              contextReporterDataSet.setTabHandler(event.data.idTableSchema);
              contextReporterDataSet.setPageHandler(0);    
              contextReporterDataSet.validationsVisibleHandler();
              break;              
          default:
              //contextReporterDataSet.validationsVisibleHandler();
            break;
        }        
      }      

      const customButtons = [
        {
          label: resources.messages["visibility"],
          icon: "6",
          group: "left",
          disabled: true,
          clickHandler: null
        },
        {
            label: resources.messages["filter"],
            icon: "7",
            group: "left",
            disabled: true,
            clickHandler: null
        },
        {
            label: resources.messages["group-by"],
            icon: "8",
            group: "left",
            disabled: true,
            clickHandler: null
        },
        {
            label: resources.messages["sort"],
            icon: "9",
            group: "left",
            disabled: true,
            clickHandler: null
        },
        {
            label: resources.messages["refresh"],
            icon: "11",
            group: "right",
            disabled: false,
            clickHandler: () => fetchDataHandler(null, sortOrder, firstRow, numRows)
        }
      ];

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
                       rowsPerPageOptions={[5, 10, 15]} lazy={true} 
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