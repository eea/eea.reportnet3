/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext } from 'react';
import styles from './DataViewer.module.css';
import ButtonsBar from '../../../components/Layout/UI/ButtonsBar/ButtonsBar';
// import { MultiSelect } from 'primereact/multiselect';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import ReporterDataSetContext from '../../../components/Context/ReporterDataSetContext';

import HTTPRequester from '../../../services/HTTPRequester/HTTPRequester';

const DataViewer = (props) => {
    const contextReporterDataSet = useContext(ReporterDataSetContext);
    const [totalRecords, setTotalRecords] = useState(0);
    const [fetchedData, setFetchedData] = useState([]);    
    const [linkedErrorData, setLinkedErrorData] = useState((props.linkedErrorData.length>0) ? props.linkedErrorData : []);
    const [loading, setLoading] = useState(false);
    const [numRows, setNumRows] = useState(10);
    const [firstRow, setFirstRow] = useState(0);
    const [sortOrder, setSortOrder] = useState();   
    const [sortField,setSortField] = useState();
    const [columns, setColumns] = useState([]); 
    const [cols, setCols] = useState(props.tableSchemaColumns); 
    const [header] = useState();
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
        fetchDataHandler(null, sortOrder, firstRow, numRows);   
        
        console.log("Filtering data...");
        const inmTableSchemaColumns = [...props.tableSchemaColumns];
        setCols(inmTableSchemaColumns);

      }, []);
  
      useEffect(()=>{         
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
        
        let columnsArr = cols.map(col => <Column sortable={true} key={col.field} field={col.field} header={col.header} />);
        setColumns(columnsArr); 
  
      }, [cols, colOptions]);
      
      const onChangePageHandler = (event)=>{     
        console.log('Refetching data...');                
        setNumRows(event.rows);
        setFirstRow(event.first);        
        fetchDataHandler(sortField, sortOrder, event.first, event.rows); 
      }
  
      const onSortHandler = (event)=>{      
        console.log("Sorting...");
        setSortOrder(event.sortOrder);  
        setSortField(event.sortField);    
        fetchDataHandler(event.sortField, event.sortOrder, firstRow, numRows);       
      }
  
      const onRefreshClickHandler = () => {
        setLinkedErrorData([]);
        fetchDataHandler(null, sortOrder, firstRow, numRows);  
      }

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
          idTableSchema: props.id,
          pageNum: Math.floor(fRow / nRows),
          pageSize: nRows
        }

        if (sField !== undefined && sField !== null) {
          queryString.fields = sField;
          queryString.asc = sOrder === -1 ? 0 : 1;
        }

        // props.urlViewer
        const dataPromise = HTTPRequester.get(
          {
            url: '/jsons/response_dataset_values2.json',
            queryString: queryString
          }
        );        
        dataPromise.then(response =>{
          filterDataResponse(response.data.records);          
          if(response.data.totalRecords!==totalRecords){
            setTotalRecords(response.data.totalRecords);
          }
        
          setLoading(false);
        })
        .catch(error => {
          console.log(error);
          return error;
        });
      }

      const filterDataResponse = (data) =>{        
        
        //TODO: Refactorizar
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
        setFetchedData(auxArrayFiltered);
      }

      //TODO: Textos + iconos + ver si deben estar aquí.
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
            clickHandler: onRefreshClickHandler
        }
      ];

      let totalCount = <span>Total: {totalRecords} rows</span>;

    return (
        <div>
          <ButtonsBar buttons={(props.customButtons)?props.customButtons:customButtons} />
            {/* <Toolbar>
                <CustomButton label="Visibility" icon="6" />                
                <CustomButton label="Filter" icon="7" />   
                <CustomButton label="Group by" icon="8" />   
                <CustomButton label="Sort" icon="9" />   
            </Toolbar> */}
            <div className={styles.Table}>
                <DataTable value={(linkedErrorData.length>0) ? linkedErrorData : fetchedData} paginatorRight={totalCount}
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

export default React.memo(DataViewer);