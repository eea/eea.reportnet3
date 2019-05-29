/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect } from 'react';
import styles from './DataViewer.module.css';
import ButtonsBar from '../../../components/Layout/UI/ButtonsBar/ButtonsBar';
import { MultiSelect } from 'primereact/multiselect';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';

import jsonData from '../../../assets/jsons/response_dataset_values.json';
import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';

const DataViewer = (props) => {
    const [totalRecords, setTotalRecords] = useState(0);
    const [fetchedData, setFetchedData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [numRows, setNumRows] = useState(10);
    const [firstRow, setFirstRow] = useState(0);
    const [sortOrder, setSortOrder] = useState(0);   
    const [sortField,setSortField] = useState();
    const [columns, setColumns] = useState([]); 
    const [cols, setCols] = useState(props.tableSchemaColumns); 
    const [header, setHeader] = useState();
    const [colOptions,setColOptions] = useState([{}]);    

    //TODO: Render se está ejecutando dos veces. Mirar por qué.
    console.log("DataViewer Render..." + props.name);
    useEffect(() =>{            
        console.log("Setting column options...");      
        let colOpt = [];
        for(let col of cols) {  
          colOpt.push({label: col.header, value: col});
        }              
        setColOptions(colOpt);
  
        console.log('Fetching data...');
        //fetchDataHandler("default", sortOrder, firstRow, numRows);   
        
        const dataPromise = HTTPRequesterAPI.get(
          {
            url:'/dataset/TableValueDataset/1',
            queryString: {
              MongoID: "prueba",
              asc:true,
              fields:"id",
              pageNum:0,
              pageSize:20
            }
          }
        );

        dataPromise.then(response =>{
          console.log(response.data);
          filterDataResponse(response.data.tableVO[0].records);
          setTotalRecords(response.data.tableVO[0].totalRecords);
        })
        .catch(error => {
          console.log(error);
          return error;
        });

        //filterDataResponse(jsonData.tableVO[0].records);        
        console.log("Filtering data...");
        const inmTableSchemaColumns = [...props.tableSchemaColumns];
        console.log(inmTableSchemaColumns);
        setCols(inmTableSchemaColumns);

      }, []);
  
      useEffect(()=>{         
        // let visibilityIcon = (<div className="TableDiv">
        //     <span className="pi pi-eye" style={{zoom:2}}></span> 
        //     <span className="my-multiselected-empty-token">Visibility</span>
        //   </div>
        // );
        let headerArr = <div className="TableDiv">
            <i className="pi pi-eye"></i>
            <MultiSelect value={cols} options={colOptions} tooltip="Filter columns" onChange={onColumnToggleHandler} style={{width:'10%'}} placeholder="Visibility" filter={true} fixedPlaceholder={true}/>
        </div>;
        setHeader(headerArr);
        
        let columnsArr = cols.map(col => <Column sortable={true} key={col.field} field={col.field} header={col.header} />);
        setColumns(columnsArr); 
  
      }, [cols, colOptions]);
      
      const onChangePageHandler = (event)=>{     
        console.log('Refetching data...');
        fetchDataHandler(event.sortField, sortOrder, event.first, event.rows);         
        setNumRows(event.rows);
        setFirstRow(event.first);        
      }
  
      const onSortHandler = (event)=>{      
        console.log("Sorting...");
        fetchDataHandler(event.sortField, sortOrder, firstRow, numRows);     
        setSortField(event.sortField);
        setSortOrder((sortOrder === 1)?-1:1);        
      }
  
      const onColumnToggleHandler = (event) =>{
        console.log("OnColumnToggle...");
        setCols(event.value);
        setColOptions(colOptions);
      }
  
      useEffect(()=>{
        console.log("Fetching new data...");
      console.log(fetchedData);
      },[fetchedData]);

      const fetchDataHandler = (sField, sOrder, fRow, nRows) => {
        setLoading(true);
        fetch(`http://pmpwvsig69.tcsa.local/Dev/ProduccionSIUN/api/Instrumentos/${sField}/${sOrder === 1}/${fRow}/${nRows}`)
        .then(response => response.json())
        .then(json => {           
          const rows = json.currentPage.map(item=>{
            return {
                    idInstrumento : item["idInstrumento"], 
                    denominacion : item["denominacion"], 
                    fechaInicial : item["fechaInicial"], 
                    tieneDocumentos : item["tieneDocumentos"], 
                    anulado : item["anulado"]
                  }
          }); 
          setFetchedData(rows);
          if(json.pagedInfo.totalElements!==totalRecords){
            setTotalRecords(json.pagedInfo.totalElements);
          }
          setLoading(false);
        })
        .catch(error => console.log("ERROR!!!!!!! - " + error));
      }

      const filterDataResponse = (data) =>{        
        
        //TODO: Refactorizar
        const dataFiltered = data.map(record => record.fields.map(f =>{
          return {[f.idFieldSchema]: f.value}
        }));
        console.log(data)
        let auxFiltered = {}
        let auxArrayFiltered = [];
        dataFiltered.forEach(dat => {
          dat.forEach(d=>auxFiltered = {...auxFiltered,...d});
          auxArrayFiltered.push(auxFiltered);
          auxFiltered={};
        });

        setFetchedData(auxArrayFiltered);
      }

      let totalCount = <span>Total: {totalRecords} rows</span>;

    return (
        <div>
          <ButtonsBar buttons={props.customButtons} />
            {/* <Toolbar>
                <CustomButton label="Visibility" icon="6" />                
                <CustomButton label="Filter" icon="7" />   
                <CustomButton label="Group by" icon="8" />   
                <CustomButton label="Sort" icon="9" />   
            </Toolbar> */}
            <div className={styles.Table}>
                <DataTable value={fetchedData} paginatorRight={totalCount}
                       resizableColumns={true} reorderableColumns={true}
                       paginator={true} rows={numRows} first={firstRow} onPage={onChangePageHandler} 
                       rowsPerPageOptions={[5, 10, 20, 100]} lazy={true} 
                       loading={loading} totalRecords={totalRecords} sortable={true}
                       onSort={onSortHandler} header={header} sortField={sortField} sortOrder={sortOrder} autoLayout={true}>
                    {columns}
                </DataTable>
            </div>
        </div>
    );
}

export default DataViewer;