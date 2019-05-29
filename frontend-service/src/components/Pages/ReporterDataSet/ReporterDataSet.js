import React, {useState, useEffect} from 'react';
import {BreadCrumb} from 'primereact/breadcrumb';
import Title from '../../Layout/Title/Title';
import ButtonsBar from '../../Layout/UI/ButtonsBar/ButtonsBar';
import TabsSchema from '../../Layout/UI/TabsSchema/TabsSchema';
import {Dialog} from 'primereact/dialog';
import {Chart} from 'primereact/chart';
import {CustomFileUpload} from '../../Layout/UI/CustomFileUpload/CustomFileUpload';
// import {Lightbox} from 'primereact/lightbox';

import jsonDataSchema from '../../../assets/jsons/datosDataSchema.json';
import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';
import styles from './ReporterDataSet.module.css';


const ReporterDataSet = () => {
  const [dashDialogVisible, setDashDialogVisible] = useState(false);
  const [customButtons, setCustomButtons] = useState([]);
  const [validationError, setValidationError] = useState(true);
  const [dashBoardData, setDashBoardData] = useState({});
  const [dashBoardOptions, setDashBoardOptions] = useState({});
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();
  const [visible, setVisibility] = useState(false);

  console.log('ReporterDataSet Render...');
  
  const onDashBoardClickHandler = () =>{
    setDashDialogVisible(true);
  } 
  const onHideDialogHandler = () =>{
    setDashDialogVisible(false);
  } 
 

  const showFileUploadDialog = () => {
      console.log('showFileUploadDialog onClick');
      setVisibility(true);
  }

  const onUploadFile = () => {
      console.log('onUploadFile');
  }

  const onHide = () => {
      console.log('onClick');
      setVisibility(false);
  }

  //TODO:Change + Error/warning treatment
    
  useEffect(()=>{
    console.log("ReporterDataSet useEffect");
    setCustomButtons([
      {
        label: "Import",
        icon: "0",
        group: "left",
        disabled: false,
        clickHandler: showFileUploadDialog
      },
      {
        label: "Export",
        icon: "1",
        group: "left",
        disabled: false,
        clickHandler: null
      },
      {
        label: "Delete",
        icon: "2",
        group: "left",
        disabled: false,
        clickHandler: null
      },
      {
        label: "Events",
        icon: "4",
        group: "right",
        disabled: false,
        clickHandler: null
      },
      {
        label: "Validate",
        icon: "10",
        group: "right",
        disabled: !validationError,
        clickHandler: null,
        ownButtonClasses:null,
        iconClasses:null
      },
      {
        label: "Show Validations",
        icon: "3",
        group: "right",
        disabled: !validationError,
        clickHandler: null,
        ownButtonClasses:null,
        iconClasses:(validationError)?"warning":""
      },
      {
        //title: "Dashboards",
        label: "Dashboards",
        icon: "5",
        group: "right",
        disabled: false,
        clickHandler: onDashBoardClickHandler
      }
    ]);
    //TODO:Change + Error/warning treatment

    setDashBoardData({
      labels: ['Table 1', 'Table 2', 'Table 3', 'Table 4'],
            datasets: [
                {
                    label: 'Info',
                    backgroundColor: '#004494',
                    data: [65, 50, 80, 11]
                },
                {
                    label: 'Warning',
                    backgroundColor: '#ffd617',
                    data: [15, 48, 5, 19]
                },
                {
                  label: 'Error',
                  backgroundColor: '#DA2131',
                  data: [10, 2, 15, 70]
              }
            ]});

    setDashBoardOptions({tooltips: {
      mode: 'index',
      intersect: false
      },
      responsive: true,
      scales: {
          xAxes: [{
              stacked: true,
          }],
          yAxes: [{
              stacked: true
          }]
      }});

//Fetch data (JSON)
    //fetchDataHandler(jsonDataSchema);
    const dataPromise = HTTPRequesterAPI.get(
      {
        url:'/dataSchema/1',
        queryString: {}
      }
    );

    dataPromise.then(response =>{
      console.log(response.data);
      setTableSchema(response.data.tableSchemas.map((item,i)=>{
        return {
            id: i,
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

  const items = [
    {label:'New Dataset', url: '#'},
    {label:'Edit data', url: '#'}
  ];
  const home = {icon: 'pi pi-home', url: '#'};
  return (
    <div className="titleDiv">

        <BreadCrumb model={items} home={home}/>
        <Title title="Data Set: R3 Demo Dataflow"/> 
        <div className={styles.ButtonsBar}>      
          <ButtonsBar buttons={customButtons} />
        </div>
        {/*TODO: Loading spinner*/}
        {(tableSchema)?<TabsSchema tables={tableSchema} tableSchemaColumns={tableSchemaColumns}/> : null}
          <Dialog header="Upload your Dataset" visible={visible}
                  className={styles.Dialog} dismissableMask={false} onHide={onHide} >
              <CustomFileUpload mode="advanced" name="demo[]" url="." onUpload={onUploadFile} 
                          multiple={false} chooseLabel="Select or drag here your dataset (.csv)" //allowTypes="/(\.|\/)(csv|doc)$/"
                          fileLimit={1} className={styles.FileUpload}  /> 
          </Dialog>                
        <Dialog visible={dashDialogVisible} onHide={onHideDialogHandler} header="Error/Warning dashboard" maximizable dismissableMask={true} style={{width:'80%'}}>
          <Chart type="bar" data={dashBoardData} options={dashBoardOptions} />
        </Dialog>
      </div>
  );
}

export default ReporterDataSet;
