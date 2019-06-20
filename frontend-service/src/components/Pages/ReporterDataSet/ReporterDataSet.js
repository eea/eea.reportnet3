/* eslint-disable react-hooks/exhaustive-deps */
import React, {useState, useEffect, useContext, Suspense} from 'react';

import Title from '../../Layout/Title/Title';
import ButtonsBar from '../../Layout/UI/ButtonsBar/ButtonsBar';
import {BreadCrumb} from 'primereact/breadcrumb';
import {Dialog} from 'primereact/dialog';
// import {Lightbox} from 'primereact/lightbox';
//import {Loader} from '../../Layout/UI/Loader/Loader';
import TabsSchema from '../../Layout/UI/TabsSchema/TabsSchema';
import ConfirmDialog from '../../Layout/UI/ConfirmDialog/ConfirmDialog';
import ValidationViewer from '../../../containers/DataSets/ValidationViewer/ValidationViewer';
import Dashboard from '../../../containers/DashBoard/DashBoard';

import config from '../../../conf/web.config.json';
import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';
import styles from './ReporterDataSet.module.css';
import ResourcesContext from '../../Context/ResourcesContext';
import ReporterDataSetContext from '../../Context/ReporterDataSetContext';


const ReporterDataSet = () => {
  const resources = useContext(ResourcesContext);  
  const [customButtons, setCustomButtons] = useState([]);
  const [breadCrumbItems,setBreadCrumbItems] = useState([]);
  //const [validationError, setValidationError] = useState(false);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();

  const [dashDialogVisible, setDashDialogVisible] = useState(false);
  const [validationsVisible, setValidationsVisible] = useState(false);
  const [validateDialogVisible, setValidateDialogVisible] = useState(false);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [isDataDeleted, setIsDataDeleted] = useState(false);
  const [activeIndex, setActiveIndex] = useState();
  const [positionIdObject, setPositionIdObject] = useState(0);

  const home = {icon: resources.icons["home"], url: '#'};

  useEffect(()=>{
    console.log("ReporterDataSet useEffect");

    setBreadCrumbItems( [
      {label: resources.messages["newDataset"], url: '#'},
      {label: resources.messages["viewData"], url: '#'}
    ]);    

    //Fetch DataSchema(JSON)
    //fetchDataHandler(jsonDataSchema);

    //`${config.dataSchemaAPI.url}1`
    const dataPromise = HTTPRequesterAPI.get(
      {
        url:`${config.dataSchemaAPI.url}1`,
        queryString: {}
      }
    ); 
    dataPromise.then(response =>{
      //'/jsons/error-statistics.json'
      const dataPromiseError = HTTPRequesterAPI.get(
        {
          url: `${config.loadStatisticsAPI.url}1`,
          queryString: {}
        }
      );

      //Parse JSON to array statistic values
      dataPromiseError
      .then(res =>{
        setTableSchema(response.data.tableSchemas.map((item,i)=>{
          return {
              id: item["idTableSchema"],
              name : item["nameTableSchema"],
              hasErrors: {...res.data.tables.filter(t=>t["idTableSchema"]===item["idTableSchema"])[0]}.tableErrors
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

        //#region Button inicialization
              
        setCustomButtons([
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
            disabled: false,
            //!validationError,
            clickHandler: () => setVisibleHandler(setValidateDialogVisible, true),
            ownButtonClasses:null,
            iconClasses:null
          },
          {
            label: resources.messages["showValidations"],
            icon: "3",
            group: "right",
            disabled: !res.data.datasetErrors,
            clickHandler: () => setVisibleHandler(setValidationsVisible, true),
            ownButtonClasses:null,
            iconClasses:(response.data.datasetErrors)?"warning":""
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
        //#endregion Button inicialization

      });     
    })
    .catch(error => {
      console.log(error);
      return error;

    });   
    
  }, [isDataDeleted]);

  const setVisibleHandler = (fnUseState, visible) =>{
    fnUseState(visible);
  }

  const onConfirmDeleteHandler = () =>{
    let idDataSet = 1;
    setDeleteDialogVisible(false);
    HTTPRequesterAPI.delete(
      {
        url:'/dataset/'+ idDataSet + '/deleteImportData',
        queryString: {}
      }
    )
    .then(res=>{
      setIsDataDeleted(true);
    });
  }

  const onConfirmValidateHandler = () =>{
    let idDataSet = 1;
    setValidateDialogVisible(false);
    HTTPRequesterAPI.update(
      {
        url:'/validation/dataset/'+ idDataSet,
        queryString: {}
      }
    );
  }
  
  const onTabChangeHandler = (idTableSchema) =>{
    setActiveIndex(idTableSchema.index);
    setPositionIdObject(0);
  }

  
  return (
    <div className="titleDiv">
        <BreadCrumb model={breadCrumbItems} home={home}/>
        <Title title={resources.messages["titleDataset"]}/> 
        <div className={styles.ButtonsBar}>      
          <ButtonsBar buttons={customButtons} />
        </div>
        {/*TODO: Loading spinner --> En el Suspense
        ¿LinkedErrorData mejor pasar por props o tirar de context?*/}
        <ReporterDataSetContext.Provider value={
                    {
                      validationsVisibleHandler: null,
                      setTabHandler: null,
                      setPageHandler: (posIdObject)=>{ setPositionIdObject(posIdObject) }
                    }}>    
          <TabsSchema tables={tableSchema} 
                      tableSchemaColumns={tableSchemaColumns} 
                      urlViewer={`${config.dataviewerAPI.url}1`}
                      activeIndex={activeIndex}
                      positionIdObject={positionIdObject}
                      onTabChangeHandler={(idTableSchema) => onTabChangeHandler(idTableSchema)}                    
                      /> 
        </ReporterDataSetContext.Provider>               
        <Dialog visible={dashDialogVisible} 
                onHide={()=>setVisibleHandler(setDashDialogVisible,false)} 
                header={resources.messages["titleDashboard"]} 
                maximizable 
                dismissableMask={true} 
                style={{width:'80%'}}>
                <Dashboard refresh={dashDialogVisible}/>
        </Dialog>   
            {/* TODO: ¿Merece la pena utilizar ContextAPI a un único nivel? */}
        <ReporterDataSetContext.Provider value={
                    {
                      validationsVisibleHandler:()=>{setVisibleHandler(setValidationsVisible, false)},
                      setTabHandler: (idTableSchema)=>{ setActiveIndex(idTableSchema) },
                      setPageHandler: (posIdObject)=>{ setPositionIdObject(posIdObject)}
                    }}>     
          <Dialog visible={validationsVisible} 
                  onHide={()=>setVisibleHandler(setValidationsVisible, false)} 
                  header={resources.messages["titleValidations"]} 
                  maximizable 
                  dismissableMask={true} 
                  style={{width:'80%'}}>
                      <ValidationViewer idDataSet = {1}/>     
          </Dialog>
        </ReporterDataSetContext.Provider> 
          <ConfirmDialog onConfirm={onConfirmDeleteHandler} 
                         onHide={()=>setVisibleHandler(setDeleteDialogVisible,false)} 
                         visible={deleteDialogVisible} 
                         header={resources.messages["deleteDatasetHeader"]} 
                         maximizable={false} 
                         labelConfirm={resources.messages["yes"]}  
                         labelCancel={resources.messages["no"]}>
            {resources.messages["deleteDatasetConfirm"]}
          </ConfirmDialog>
          <ConfirmDialog  onConfirm={onConfirmValidateHandler} 
                          onHide={() => setVisibleHandler(setValidateDialogVisible, false)}
                          visible={validateDialogVisible} 
                          header={resources.messages["validateDataSet"]} 
                          maximizable={false}
                          labelConfirm={resources.messages["yes"]} 
                          labelCancel={resources.messages["no"]}>
                          {resources.messages["validateDataSetConfirm"]}
          </ConfirmDialog>
      </div>
    );
}
export default ReporterDataSet;
