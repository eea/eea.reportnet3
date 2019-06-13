/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect } from 'react';
import styles from './DataViewer.module.css';
import ButtonsBar from '../../../components/Layout/UI/ButtonsBar/ButtonsBar';
// import { MultiSelect } from 'primereact/multiselect';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';

import jsonData from '../../../assets/jsons/response_dataset_values4.json';

import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';
import CustomIconToolTip from '../../../components/Layout/UI/CustomIconToolTip/CustomIconToolTip';

const DataViewer = (props) => {
  const [totalRecords, setTotalRecords] = useState(0);
  const [fetchedData, setFetchedData] = useState([]);
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
    console.log(inmTableSchemaColumns);
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
    
    let columnsArr = cols.map(col => (
      <Column
        sortable={true}
        key={col.field}
        field={col.field}
        header={col.header}
        body={columnValidationsTemplate}
      />
     
      ));
    let validationCol = (
      <Column key={'recordValidation'} field="validations" header="" body={validationsTemplate} />
    );
    let newColumnsArr = [validationCol].concat(columnsArr);
    setColumns(newColumnsArr);
   
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

  // useEffect(()=>{
  //   console.log("Fetching new data...");
  // console.log(fetchedData);
  // },[fetchedData]);

  const fetchDataHandler = (sField, sOrder, fRow, nRows) => {
    setLoading(true);
    /* 
        let queryString = {
          idTableSchema: props.id,
          pageNum: Math.floor(fRow / nRows),
          pageSize: nRows
        }

        if (sField !== undefined && sField !== null) {
          queryString.fields = sField;
          queryString.asc = sOrder === -1 ? 0 : 1;
        }

        const dataPromise = HTTPRequesterAPI.get(
          {
            url:'/dataset/TableValueDataset/1',
            queryString: queryString
          }
        );

        dataPromise.then(response =>{
          console.log(response.data);
          filterDataResponse(response.data.records);
          if(response.data.totalRecords!==totalRecords){
            setTotalRecords(response.data.totalRecords);
          }
        
          setLoading(false);
        })
        .catch(error => {
          console.log(error);
          return error;
        }); */
    if (jsonData) {
      filterDataResponse(jsonData);
    }
    console.log('total ',jsonData.totalRecords)
    setTotalRecords(jsonData.totalRecords);
    setLoading(false);
  };

  const filterDataResponse = (data) =>{

    //TODO: Refactorizar
    const dataFiltered = data.records.map(record => {
      const recordValidations = record.recordValidations;
      const arrayDataFields = record.fields.map(field => {
        
        return { 
          [field.idFieldSchema]: field.value,
          "fieldValidations"   : [field.fieldValidations]
         };
      });

      const arrayDataAndValidations = {
        ...arrayDataFields,
        recordValidations
      };

      return arrayDataAndValidations;
    });
    
    console.log(dataFiltered);
    let auxFiltered = {};

    let auxArrayFiltered = [];
/* 
    dataFiltered.forEach(dat => {

      dat.forEach(d => (auxFiltered = { ...auxFiltered, ...d }));
      
      auxArrayFiltered.push(auxFiltered);
      
      auxFiltered = {};
    }); */

    // TO DO Para que se carguen bien los datos en la tabla el componente consume un objeto especifico:
    /**
     * en el    dataFiltered   tengo todos los datos necesarios para mostrar
     * 
     * Sacar el array de validaciones por record
     * Sacar para cada campo el array de validaciones por field
     * Sacar los datos
     * 
     * Procesar los datos para que se pueda consumir por el DataViewer
     */
    dataFiltered.forEach(dat => {

      if (dat.recordValidations) {
        console.log(dat.recordValidations);
      }
    });

    setFetchedData(dataFiltered);
  };

  //Template for Record validation
  const validationsTemplate = (fetchedData, column) => {
    if (fetchedData.recordValidations) {
      const validations = fetchedData.recordValidations.map(
        val => val.validation
      );

      let message = "";
      validations.forEach(validation =>
        validation.message
          ? (message += validation.message + ' \n')
          : ""
      );

      let levelError = "";
      let lvlFlag = 0;

      validations.forEach(validation => {
        if (validation.levelError === "WARNING") {
          const wNum = 1;
          if (wNum > lvlFlag) {
            lvlFlag = wNum;
            levelError = "WARNING";
          }
        } else if (validation.levelError === "ERROR") {
          const eNum = 2;
          if (eNum > lvlFlag) {
            lvlFlag = eNum;
            levelError = "ERROR";
          }
        } else if (validation.levelError === "BLOCKER") {
          const bNum = 2;
          if (bNum > lvlFlag) {
            lvlFlag = bNum;
            levelError = "BLOCKER";
          }
        }
      });

      return <CustomIconToolTip levelError={levelError} message={message} />;
    } else {
      return <CustomIconToolTip levelError={null} message={null} />;
    }
  };
  
  //Template for Field validation
  const columnValidationsTemplate = (fetchedData, column) => {
//TO DO ADAPTAR PARA  MOSTRAR VALIDACION POR CELDA. AHORA ES UN COPIA PEGA DE LAS VALIDACIONES POR RECORD
    if (fetchedData.recordValidations) {
      const validations = fetchedData.recordValidations.map(
        val => val.validation
      );
        console.log('fetchedData', fetchedData);

      let message = "";
      validations.forEach(validation =>
        validation.message
          ? (message += validation.message + ' \n')
          : ""
      );

      let levelError = "";
      let lvlFlag = 0;

      validations.forEach(validation => {
        if (validation.levelError === "WARNING") {
          const wNum = 1;
          if (wNum > lvlFlag) {
            lvlFlag = wNum;
            levelError = "WARNING";
          }
        } else if (validation.levelError === "ERROR") {
          const eNum = 2;
          if (eNum > lvlFlag) {
            lvlFlag = eNum;
            levelError = "ERROR";
          }
        } else if (validation.levelError === "BLOCKER") {
          const bNum = 2;
          if (bNum > lvlFlag) {
            lvlFlag = bNum;
            levelError = "BLOCKER";
          }
        }
      });

      return <CustomIconToolTip levelError={levelError} message={message} />;
    } else {
      return <CustomIconToolTip levelError={null} message={null} />;
    }
  };

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
        >
          {columns}
        </DataTable>
      </div>
    </div>
  );
}

export default DataViewer;