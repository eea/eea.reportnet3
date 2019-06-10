/* eslint-disable react-hooks/exhaustive-deps */
import React, {useState, useEffect, useContext, Suspense} from 'react';
import {BreadCrumb} from 'primereact/breadcrumb';
import {Dialog} from 'primereact/dialog';

import Title from '../../Layout/Title/Title';
import ButtonsBar from '../../Layout/UI/ButtonsBar/ButtonsBar';
import TabsSchema from '../../Layout/UI/TabsSchema/TabsSchema';
import {CustomFileUpload} from '../../Layout/UI/CustomFileUpload/CustomFileUpload';
//import ConfirmDialog from '../../Layout/UI/ConfirmDialog/ConfirmDialog';
// import {Lightbox} from 'primereact/lightbox';

//import jsonDataSchema from '../../../assets/jsons/datosDataSchema2.json';

import config from '../../../conf/web.config.json';
import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';
import styles from './ReporterDataSet.module.css';
import ResourcesContext from '../../Context/ResourcesContext';
import ReporterDataSetContext from '../../Context/ReporterDataSetContext';

const ReporterDataSet = () => {
  const resources = useContext(ResourcesContext);  
  const [customButtons, setCustomButtons] = useState([]);
  const [breadCrumbItems,setBreadCrumbItems] = useState([]);
  const [validationError] = useState(true);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();

  const [importDialogVisible, setImportDialogVisible] = useState(false);
  const [dashDialogVisible, setDashDialogVisible] = useState(false);
  const [validationsVisible, setValidationsVisible] = useState(false);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);


  const ConfirmDialog = React.lazy(() => import('../../Layout/UI/ConfirmDialog/ConfirmDialog'));
  const ValidationDataViewer = React.lazy(() => import('../../../containers/DataSets/ValidationViewer/ValidationViewer'));
  const Dashboard = React.lazy(()=> import('../../../containers/DashBoard/DashBoard'));

  console.log('ReporterDataSet Render...');   

  const home = {icon: resources.icons["home"], url: '#'};

  useEffect(()=>{
    console.log("ReporterDataSet useEffect");

    //#region Button inicialization
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
        disabled: false,
        clickHandler: () => setVisibleHandler(setDeleteDialogVisible, true)
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

      //#endregion Button inicialization
    ]);

    setBreadCrumbItems( [
      {label: resources.messages["newDataset"], url: '#'},
      {label: resources.messages["viewData"], url: '#'}
    ]);    

    //Fetch DataSchema(JSON)
    //fetchDataHandler(jsonDataSchema);
    const dataPromise = HTTPRequesterAPI.get(
      {
        url:'/dataschema/dataflow/1',
        queryString: {}
      }
    );

    dataPromise.then(response =>{
      setTableSchema(response.data.tableSchemas.map((item,i)=>{
        return {
            id: item["idTableSchema"],
            name : item["nameTableSchema"]
            }
      })); 
      
      setTableSchemaColumns(response.data.tableSchemas.map(table =>{
        return table.recordSchema.fieldSchema.map(item=>{
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
  
  
  return (
    <div className="titleDiv">
        <BreadCrumb model={breadCrumbItems} home={home}/>
        <Title title={resources.messages["titleDataset"]}/> 
        <div className={styles.ButtonsBar}>      
          <ButtonsBar buttons={customButtons} />
        </div>
        {/*TODO: Loading spinner --> En el Suspense*/}
        <TabsSchema tables={tableSchema} 
                    tableSchemaColumns={tableSchemaColumns} 
                    onRefresh={onRefreshClickHandler} 
                    urlViewer={`${config.dataviewerAPI.url}1`}/>
        <Dialog header={resources.messages["uploadDataset"]} 
                visible={importDialogVisible}
                className={styles.Dialog} 
                dismissableMask={false} 
                onHide={() => setVisibleHandler(setImportDialogVisible, false)} >
          <CustomFileUpload mode="advanced" 
                            name="file" 
                            url="http://127.0.0.1:8030/dataset/1/loadDatasetData" 
                            onUpload={() => setVisibleHandler(setImportDialogVisible, false)} 
                            multiple={false} 
                            chooseLabel={resources.messages["selectFile"]} //allowTypes="/(\.|\/)(csv|doc)$/"
                            fileLimit={1} 
                            className={styles.FileUpload}  /> 
        </Dialog>                
        <Dialog visible={dashDialogVisible} 
                onHide={()=>setVisibleHandler(setDashDialogVisible,false)} 
                header={resources.messages["titleDashboard"]} 
                maximizable 
                dismissableMask={true} 
                style={{width:'80%'}}>
                <Suspense fallback={<div>Loading...</div>}>
                  <Dashboard/>
                </Suspense>
        </Dialog>   
            {/* TODO: ¿Merece la pena utilizar ContextAPI a un único nivel? */}
        <ReporterDataSetContext.Provider value={{validationsVisibleHandler:()=>setVisibleHandler(setValidationsVisible, false)}}>     
          <Dialog visible={validationsVisible} 
                  onHide={()=>setVisibleHandler(setValidationsVisible, false)} 
                  header={resources.messages["titleValidations"]} 
                  maximizable 
                  dismissableMask={true} 
                  style={{width:'80%'}}>
                    <Suspense fallback={<div>Loading...</div>}>
                      <ValidationDataViewer/>
                    </Suspense>        
          </Dialog>
        </ReporterDataSetContext.Provider> 
        <Suspense fallback={<div>Loading...</div>}>
          <ConfirmDialog onConfirm={onConfirmDeleteHandler} 
                         onHide={()=>setVisibleHandler(setDeleteDialogVisible,false)} 
                         visible={deleteDialogVisible} 
                         header={resources.messages["deleteDatasetHeader"]} 
                         maximizable={false} 
                         labelConfirm={resources.messages["yes"]}  
                         labelCancel={resources.messages["no"]}>
            {resources.messages["deleteDatasetConfirm"]}
          </ConfirmDialog>
        </Suspense>
      </div>
  );
}

export default ReporterDataSet;
