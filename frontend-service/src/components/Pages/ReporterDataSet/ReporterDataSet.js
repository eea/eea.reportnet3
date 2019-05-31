import React, {useState, useEffect, useContext} from 'react';
import {BreadCrumb} from 'primereact/breadcrumb';
import Title from '../../Layout/Title/Title';
import ButtonsBar from '../../Layout/UI/ButtonsBar/ButtonsBar';
import TabsSchema from '../../Layout/UI/TabsSchema/TabsSchema';
import {Dialog} from 'primereact/dialog';
import {Chart} from 'primereact/chart';
import {CustomFileUpload} from '../../Layout/UI/CustomFileUpload/CustomFileUpload';
import ConfirmDialog from '../../Layout/UI/ConfirmDialog/ConfirmDialog';
// import {Lightbox} from 'primereact/lightbox';

//import jsonDataSchema from '../../../assets/jsons/datosDataSchema2.json';
import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';
import styles from './ReporterDataSet.module.css';
import LangContext from '../../Context/LanguageContext';


const ReporterDataSet = () => {
  const messages = useContext(LangContext);  
  const [customButtons, setCustomButtons] = useState([]);
  const [breadCrumbItems,setBreadCrumbItems] = useState([]);
  const [validationError, setValidationError] = useState(true);
  const [dashBoardData, setDashBoardData] = useState({});
  const [dashBoardOptions, setDashBoardOptions] = useState({});
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();
  const [importDialogvisible, setImportDialogVisible] = useState(false);
  const [dashDialogVisible, setDashDialogVisible] = useState(false);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);

  console.log('ReporterDataSet Render...');   

  const home = {icon: 'pi pi-home', url: '#'};

  //TODO:Change + Error/warning treatment
    
  useEffect(()=>{
    console.log("ReporterDataSet useEffect");
    setCustomButtons([
      {
        label: messages["import"],
        icon: "0",
        group: "left",
        disabled: false,
        clickHandler: () => setVisibleHandler(setImportDialogVisible, true)
      },
      {
        label: messages["export"],
        icon: "1",
        group: "left",
        disabled: false,
        clickHandler: null
      },
      {
        label: messages["delete"],
        icon: "2",
        group: "left",
        disabled: false,
        clickHandler: () => setVisibleHandler(setDeleteDialogVisible, true)
      },
      {
        label: messages["events"],
        icon: "4",
        group: "right",
        disabled: false,
        clickHandler: null
      },
      {
        label: messages["validate"],
        icon: "10",
        group: "right",
        disabled: !validationError,
        clickHandler: null,
        ownButtonClasses:null,
        iconClasses:null
      },
      {
        label: messages["showValidations"],
        icon: "3",
        group: "right",
        disabled: !validationError,
        clickHandler: null,
        ownButtonClasses:null,
        iconClasses:(validationError)?"warning":""
      },
      {
        //title: "Dashboards",
        label: messages["dashboards"],
        icon: "5",
        group: "right",
        disabled: false,
        clickHandler: null
        //() => setVisibleHandler(setDashDialogVisible, true)
      }
    ]);

    setBreadCrumbItems( [
      {label: messages["newDataset"], url: '#'},
      {label: messages["editData"], url: '#'}
    ]);

    //TODO:Change + Error/warning treatment

    setDashBoardData({
      labels: ['Table 1', 'Table 2', 'Table 3', 'Table 4'],
      datasets: [
          {
              label: 'Info',
              backgroundColor: '#004494',
              data: getPercentage([105, 50, 80, 11])
          },
          {
              label: 'Warning',
              backgroundColor: '#ffd617',
              data: getPercentage([15, 48, 58, 19])
          },
          {
            label: 'Error',
            backgroundColor: '#DA2131',
            data: getPercentage([10, 2, 15, 85])
        }
      ]});

    setDashBoardOptions({
      tooltips: {
        mode: 'index',
        intersect: false,
        callbacks: {
            label: (tooltipItems, data) => `(${tooltipItems.yLabel} %)`
        }
      },
      responsive: true,
      scales: {
          xAxes: [{
              stacked: true,
          }],
          yAxes: [{
              stacked: true,
              scaleLabel: {
								display: true
								//labelString: 'Value'
              },
              ticks: {
                // Include a % sign in the ticks
                callback: function(value, index, values) {
                    return value +' %';
                }
            }
          }]
      }});

    //Fetch data (JSON)
    //fetchDataHandler(jsonDataSchema);
    const dataPromise = HTTPRequesterAPI.get(
      {
        url:'/dataschema/dataflow/1',
        queryString: {}
      }
    );

    dataPromise.then(response =>{
      console.log(response.data);
      setTableSchema(response.data.tableSchemas.map((item,i)=>{
        return {
            id: item["idTableSchema"],
            name : item["nameTableSchema"]
            }
      })); 
      setTableSchemaColumns(response.data.tableSchemas.map(table =>{
        return table.recordSchema.fieldSchema.map((item,i)=>{
          return {
              table: table["nameTableSchema"], 
              field: item["id"], 
              header: `${item["name"].charAt(0).toUpperCase()}${item["name"].slice(1)}`
            }
        });        
      }));
    })
    .catch(error => {
      console.log(error);
      return error;
    });    
  },[]);

  const setVisibleHandler = (fnUseState, visible) =>{
    fnUseState(visible);
  }
  
  const onRefreshClickHandler = () => {
    console.log("Refresh Clicked!");
  }

  const onConfirmDeleteHandler = () =>{
    console.log("Data deleted!");
    setDeleteDialogVisible(false);
    /*TODO: API Call delete
    HTTPRequesterAPI.delete(
      {
        url:'/dataset/deleteImportData/1',
        queryString: {}
      }
    );*/
  }

  const getPercentage = (tableValues) =>{
     let valArr = [[105, 50, 80, 11],[15, 48, 58, 19],[10, 2, 15, 85]];
     let total = valArr.reduce((arr1, arr2) =>
          arr1.map((v, i) => v + arr2[i]));
      
    return tableValues.map((v,i)=>((v/total[i])*100).toFixed(2));
  }

  return (
    <div className="titleDiv">
        <BreadCrumb model={breadCrumbItems} home={home}/>
        <Title title={messages["titleDataset"]}/> 
        <div className={styles.ButtonsBar}>      
          <ButtonsBar buttons={customButtons} />
        </div>
        {/*TODO: Loading spinner*/}
        <TabsSchema tables={tableSchema} tableSchemaColumns={tableSchemaColumns} onRefresh={onRefreshClickHandler}/>
          <Dialog header={messages["uploadDataset"]} visible={importDialogvisible}
                  className={styles.Dialog} dismissableMask={false} onHide={() => setVisibleHandler(setImportDialogVisible, false)} >
              <CustomFileUpload mode="advanced" name="file" url="http://127.0.0.1:8030/dataset/1/loadDatasetData" 
                                onUpload={() => setVisibleHandler(setImportDialogVisible,true)} 
                                multiple={false} chooseLabel={messages["selectFile"]} //allowTypes="/(\.|\/)(csv|doc)$/"
                                fileLimit={1} className={styles.FileUpload}  /> 
          </Dialog>                
        <Dialog visible={dashDialogVisible} onHide={()=>setVisibleHandler(setDashDialogVisible,false)} header={messages["titleDashboard"]} maximizable dismissableMask={true} style={{width:'80%'}}>
          <Chart type="bar" data={dashBoardData} options={dashBoardOptions} />
        </Dialog>
        <ConfirmDialog onConfirm={onConfirmDeleteHandler} onHide={()=>setVisibleHandler(setDeleteDialogVisible,false)} visible={deleteDialogVisible} header={messages["deleteDatasetHeader"]} maximizable={false} labelConfirm={messages["yes"]}  labelCancel={messages["no"]}>
          {messages["deleteDatasetConfirm"]}
        </ConfirmDialog>
      </div>
  );
}

export default ReporterDataSet;
