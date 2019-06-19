/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext } from 'react';
import styles from './DataViewer.module.css';
import ButtonsBar from '../../../components/Layout/UI/ButtonsBar/ButtonsBar';
// import { MultiSelect } from 'primereact/multiselect';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import {Dialog} from 'primereact/dialog';
import {CustomFileUpload} from '../../../components/Layout/UI/CustomFileUpload/CustomFileUpload';
import CustomIconToolTip from '../../../components/Layout/UI/CustomIconToolTip/CustomIconToolTip';
import ReporterDataSetContext from '../../../components/Context/ReporterDataSetContext';
import ResourcesContext from '../../../components/Context/ResourcesContext';

import HTTPRequester from '../../../services/HTTPRequester/HTTPRequester';
import config from '../../../conf/web.config.json';

const DataViewer = (props) => {
    const contextReporterDataSet = useContext(ReporterDataSetContext);
    const [importDialogVisible, setImportDialogVisible] = useState(false);    
    const [totalRecords, setTotalRecords] = useState(0);
    const [fetchedData, setFetchedData] = useState([]);    
    const [linkedErrorData, setLinkedErrorData] = useState((props.linkedErrorData && props.linkedErrorData.length>0) ? props.linkedErrorData : []);
    const [loading, setLoading] = useState(false);
    const [numRows, setNumRows] = useState(10);
    const [firstRow, setFirstRow] = useState(0);
    const [sortOrder, setSortOrder] = useState();   
    const [sortField,setSortField] = useState();
    const [columns, setColumns] = useState([]); 
    const [cols, setCols] = useState(props.tableSchemaColumns); 
    const [header] = useState();
    const [colOptions,setColOptions] = useState([{}]); 
    const resources = useContext(ResourcesContext);      

    //TODO: Render se está ejecutando dos veces. Mirar por qué.
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
        
        let columnsArr = cols.map(col => (
          <Column
            sortable={true}
            key={col.field}
            field={col.field}
            header={col.header}
            body={dataTemplate}
          />
        ));
        let validationCol = (
          <Column key="recordValidation" field="validations" header="" body={validationsTemplate} style={{width: "15px"}} />
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
  
      const onRefreshClickHandler = () => {
        contextReporterDataSet.setLinkedErrorDataHandler(setLinkedErrorData([]));
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
            url: props.urlViewer,
            queryString: queryString
          }
        );        
        dataPromise.then(response =>{
          filterDataResponse(response.data);          
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
        ? (message += validation.message + '<br/>')
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
const dataTemplate = (rowData, column) =>{
    let row = rowData.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
  if (row !== null && row.fieldValidations!==null) {
    const validations = row.fieldValidations.map(
      val => val.validation
    );
    let message = [];
    validations.forEach(validation =>
      validation.message ? (message += validation.message +"<br/>") : ""
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
  
      return <div style={{'display':'flex','alignItems':'center'}}> {row.fieldData[column.field]} <CustomIconToolTip levelError={levelError} message={message}/></div>;
    }
    else{
      return <div style={{'display':'flex','alignItems':'center'}}>{row.fieldData[column.field]}</div>;
    }
  }

      //TODO: Textos + iconos + ver si deben estar aquí.
      const customButtons = [
        {
          label: resources.messages["import"],
          icon: "0",
          group: "left",
          disabled: false,
          clickHandler: () => setImportDialogVisible(true)
      },
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
              <DataTable value={(linkedErrorData && linkedErrorData.length>0) ? linkedErrorData : fetchedData} paginatorRight={totalCount}
                       resizableColumns={true} reorderableColumns={true}
                       paginator={true} rows={numRows} first={firstRow} onPage={onChangePageHandler} 
                       rowsPerPageOptions={[5, 10, 20, 100]} lazy={true} 
                       loading={loading} totalRecords={totalRecords} sortable={true}
                       onSort={onSortHandler} header={header} sortField={sortField} sortOrder={sortOrder} autoLayout={true}>
                    {columns}
                </DataTable>
            </div>
            <Dialog header={resources.messages["uploadDataset"]} visible={importDialogVisible}
                                className={styles.Dialog} dismissableMask={false} onHide={() => setImportDialogVisible(false)} >
                            <CustomFileUpload mode="advanced" name="file" url={`${config.api.protocol}${config.api.url}${config.api.port}${config.loadDataTableAPI.url}${props.id}`}
                                                onUpload={() => setImportDialogVisible(false)} 
                                                multiple={false} chooseLabel={resources.messages["selectFile"]} //allowTypes="/(\.|\/)(csv|doc)$/"
                                                fileLimit={1} className={styles.FileUpload}  /> 
                        </Dialog>
        </div>
    );
}

export default React.memo(DataViewer);