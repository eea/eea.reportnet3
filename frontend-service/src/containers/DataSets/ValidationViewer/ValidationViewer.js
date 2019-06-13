/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, Suspense, useContext } from 'react';
//import ButtonsBar from '../../../components/Layout/UI/ButtonsBar/ButtonsBar';
// import { MultiSelect } from 'primereact/multiselect';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';

import jsonData from '../../../assets/jsons/list-of-errors.json';
import ReporterDataSetContext from '../../../components/Context/ReporterDataSetContext';
//import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';

import styles from './ValidationViewer.module.css';
import ResourcesContext from '../../../components/Context/ResourcesContext';

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
    const [cols, setCols] = useState([]); 
    const [header] = useState();
    const [colOptions, setColOptions] = useState([{}]);    

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
          clickHandler: props.onRefresh
      }
  ];

    //TODO: Render se está ejecutando dos veces. Mirar por qué.
    // console.log("ValidationViewer Render..." + props.name);
    // useEffect(() =>{            
    //     console.log("Setting column options...");      
    //     let colOpt = [];
    //     for(let col of cols) {  
    //       colOpt.push({label: col.header, value: col});
    //     }              
    //     setColOptions(colOpt);
  
    //     console.log('Fetching data...');
    //     fetchDataHandler(null, sortOrder, firstRow, numRows);   
        
    //     console.log("Filtering data...");
    //     const inmTableSchemaColumns = [...props.tableSchemaColumns];
    //     console.log(inmTableSchemaColumns);
    //     setCols(inmTableSchemaColumns);

    //   }, []);
  
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

        //jsonData.errors
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
        columnsArr.push(<Column sortable={true} key="idObject" field="idObject" header="ID Object" className={styles.VisibleHeader} />)
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
  
      // const onColumnToggleHandler = (event) =>{
      //   console.log("OnColumnToggle...");
      //   setCols(event.value);
      //   setColOptions(colOptions);
      // }
  
      useEffect(()=>{
        setTotalRecords(jsonData.totalErrors);
        filterDataResponse(jsonData);
      },[]);

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

        //jsonData

        // const dataPromise = HTTPRequesterAPI.get(
        //   {
        //     url: props.urlViewer,
        //     queryString: queryString
        //   }
        // );

        // dataPromise.then(response =>{
        //   console.log(response.data);
        //   filterDataResponse(response.data.records);
        //   if(response.data.totalRecords!==totalRecords){
        //     setTotalRecords(response.data.totalRecords);
        //   }
        
        //   setLoading(false);
        // })
        // .catch(error => {
        //   console.log(error);
        //   return error;
        // });

        

      }

      const filterDataResponse = (data) =>{        
        
        //const values = [...data.errors];

        console.log(data.nameDataSetSchema);

        // const values = data.errors.map(e => {
        //   return { nameTableSchema: e.nameTableSchema, levelError: e.levelError, message: e.message, typeEntity: e.typeEntity, idObject: e.idObject  }
        // });

        //TODO: Refactorizar
        // const dataFiltered = data.map(record => record.fields.map(f =>{
        //   return {[f.idFieldSchema]: f.value}
        // }));
        // console.log(data)
        // let auxFiltered = {}
        // let auxArrayFiltered = [];
        // dataFiltered.forEach(dat => {
        //   dat.forEach(d=>auxFiltered = {...auxFiltered,...d});
        //   auxArrayFiltered.push(auxFiltered);
        //   auxFiltered={};
        // });
        setFetchedData(data.errors);
      }

      const onRowSelectHandler = (event) =>{

        // let queryString = {
        //   idTableSchema: props.id,
        //   pageNum: Math.floor(fRow / nRows),
        //   pageSize: nRows
        // }

        // const dataPromise = HTTPRequesterAPI.get(
        //   {
        //     url: props.urlViewer,
        //     queryString: queryString
        //   }
        // );
        console.log(event.data);
        //http://localhost:8030/dataset/loadTableFromAnyObject/1629858?datasetId=1&pageSize=20&type=RECORD
        contextReporterDataSet.validationsVisibleHandler();
        contextReporterDataSet.setTabHandler("5cffc519903713258408d56d");
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

export default React.memo(ValidationViewer);