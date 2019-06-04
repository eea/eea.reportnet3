/* eslint-disable react-hooks/exhaustive-deps */
import React, {useState, useEffect, useContext} from 'react';
import {BreadCrumb} from 'primereact/breadcrumb';
import Title from '../../Layout/Title/Title';
import ButtonsBar from '../../Layout/UI/ButtonsBar/ButtonsBar';
import TabsSchema from '../../Layout/UI/TabsSchema/TabsSchema';
import {Dialog} from 'primereact/dialog';
import {Chart} from 'primereact/chart';
import {Card} from 'primereact/card';
import {CustomFileUpload} from '../../Layout/UI/CustomFileUpload/CustomFileUpload';
import ConfirmDialog from '../../Layout/UI/ConfirmDialog/ConfirmDialog';
// import {Lightbox} from 'primereact/lightbox';

//import jsonDataSchema from '../../../assets/jsons/datosDataSchema2.json';
import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';
import styles from './ReporterDataSet.module.css';
import ResourcesContext from '../../Context/ResourcesContext';

import validationImage from '../../../assets/images/dataset_icon.png';

const ReporterDataSet = () => {
  const resources = useContext(ResourcesContext);  
  const [customButtons, setCustomButtons] = useState([]);
  const [breadCrumbItems,setBreadCrumbItems] = useState([]);
  const [validationError] = useState(true);
  const [dashBoardData, setDashBoardData] = useState({});
  const [dashBoardOptions, setDashBoardOptions] = useState({});
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();
  const [importDialogVisible, setImportDialogVisible] = useState(false);
  const [dashDialogVisible, setDashDialogVisible] = useState(false);
  const [validationsVisible, setValidationsVisible] = useState(false);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);

  console.log('ReporterDataSet Render...');   

  const home = {icon: resources.icons["home"], url: '#'};

  useEffect(()=>{
    console.log("ReporterDataSet useEffect");
    setCustomButtons([
      {
        label: resources.messages["import"],
        icon: "0",
        group: "left",
        disabled: false,
        clickHandler: () => setVisibleHandler(setImportDialogVisible, true)
      },
      {
        label: resources.messages["export"],
        icon: "1",
        group: "left",
        disabled: true,
        clickHandler: null
      },
      {
        label: resources.messages["delete"],
        icon: "2",
        group: "left",
        disabled: true,
        clickHandler: null
        //() => setVisibleHandler(setDeleteDialogVisible, true)
      },
      {
        label: resources.messages["events"],
        icon: "4",
        group: "right",
        disabled: true,
        clickHandler: null
      },
      {
        label: resources.messages["validate"],
        icon: "10",
        group: "right",
        disabled: true,
        //!validationError,
        clickHandler: null,
        ownButtonClasses:null,
        iconClasses:null
      },
      {
        label: resources.messages["showValidations"],
        icon: "3",
        group: "right",
        disabled: !validationError,
        clickHandler: () => setVisibleHandler(setValidationsVisible, true),
        ownButtonClasses:null,
        iconClasses:(validationError)?"warning":""
      },
      {
        //title: "Dashboards",
        label: resources.messages["dashboards"],
        icon: "5",
        group: "right",
        disabled: false,
        clickHandler: () => setVisibleHandler(setDashDialogVisible, true)
      }
    ]);

    setBreadCrumbItems( [
      {label: resources.messages["newDataset"], url: '#'},
      {label: resources.messages["viewData"], url: '#'}
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
                callback: (value, index, values) => `${value} %`
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
  }, []);

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
        <Title title={resources.messages["titleDataset"]}/> 
        <div className={styles.ButtonsBar}>      
          <ButtonsBar buttons={customButtons} />
        </div>
        {/*TODO: Loading spinner*/}
        <TabsSchema tables={tableSchema} tableSchemaColumns={tableSchemaColumns} onRefresh={onRefreshClickHandler}/>
          <Dialog header={resources.messages["uploadDataset"]} visible={importDialogVisible}
                  className={styles.Dialog} dismissableMask={false} onHide={() => setVisibleHandler(setImportDialogVisible, false)} >
              <CustomFileUpload mode="advanced" name="file" url="http://127.0.0.1:8030/dataset/1/loadDatasetData" 
                                onUpload={() => setVisibleHandler(setImportDialogVisible, false)} 
                                multiple={false} chooseLabel={resources.messages["selectFile"]} //allowTypes="/(\.|\/)(csv|doc)$/"
                                fileLimit={1} className={styles.FileUpload}  /> 
          </Dialog>                
        <Dialog visible={dashDialogVisible} onHide={()=>setVisibleHandler(setDashDialogVisible,false)} 
                header={resources.messages["titleDashboard"]} maximizable dismissableMask={true} style={{width:'80%'}}>
          <h1>US-STP6-DSM-VIS-01-List of Visualizations (next sprint)</h1>
          <Chart type="bar" data={dashBoardData} options={dashBoardOptions} />
        </Dialog>             
        <Dialog visible={validationsVisible} onHide={()=>setVisibleHandler(setValidationsVisible, false)} 
                header={resources.messages["titleValidations"]} maximizable dismissableMask={true} style={{width:'80%'}}>
          <Card title="US-STP6-DSM-QC-01-List of Validations (next sprint)">
            <div style={{textAlign: 'center'}}>
              <img alt="Validations" src={validationImage} />;
            </div>
          </Card>
        </Dialog>
        <ConfirmDialog onConfirm={onConfirmDeleteHandler} onHide={()=>setVisibleHandler(setDeleteDialogVisible,false)} 
                       visible={deleteDialogVisible} header={resources.messages["deleteDatasetHeader"]} maximizable={false} 
                       labelConfirm={resources.messages["yes"]}  labelCancel={resources.messages["no"]}>
          {resources.messages["deleteDatasetConfirm"]}
        </ConfirmDialog>
      </div>
  );
}

export default ReporterDataSet;
