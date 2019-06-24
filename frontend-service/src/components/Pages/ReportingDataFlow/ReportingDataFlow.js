import React, {useState, useEffect, useContext} from 'react';
import {BreadCrumb} from 'primereact/breadcrumb';
import {Button} from 'primereact/button';
import {SplitButton} from 'primereact/splitbutton';
import { SplitButtonNE } from '../../Layout/UI/SplitButtonNE/SplitButtonNE';
import Title from '../../Layout/Title/Title';
import jsonDataSchema from '../../../assets/jsons/datosDataSchema3.json';
//import jsonDataSchemaErrors from '../../../assets/jsons/errorsDataSchema.json';
//import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';
import styles from './ReportingDataFlow.module.css';
import ResourcesContext from '../../Context/ResourcesContext';
//import MainLayout from '../../Layout/MainLayout/MainLayout';
import DataFlowColumn from '../../Layout/UI/DataFlowColumn/DataFlowColumn';


const ReportingDataFlow = () => {
  const resources = useContext(ResourcesContext);  
  const [breadCrumbItems,setBreadCrumbItems] = useState([]);

  console.log('ReportingDataFlow Render...');   

  const home = {icon: resources.icons["home"], url: '#'};

  useEffect(()=>{
    console.log("ReportingDataFlow useEffect");

    setBreadCrumbItems( [
      {label: resources.messages["AcceptedDF"], url: '#'},
      {label: resources.messages["DFReporting"], url: '#'}
    ]);

  }, []);

 let items = [
    {label: 'New', icon: 'pi pi-fw pi-plus'},
    {label: 'Delete', icon: 'pi pi-fw pi-trash'}
];


  return (
    <div>
        {/* <MainLayout> */}
            <div className="titleDiv">
                <BreadCrumb model={breadCrumbItems} home={home}/>
                <Title title={resources.messages["titleDFReporting"]}/> 
            </div>
            <div className="rep-row">
                <div className="rep-col-12 rep-col-sm-2">
                    <DataFlowColumn />
                </div>
                <div className="rep-col-12 rep-col-sm-8">
                    <div className="ecl-row">
                        <p className={styles.title}>{jsonDataSchema.nameDataSetSchema}</p>
                    </div>
                    <div className="ecl-row">
                        <Button label="DO" className="p-button-warning" />
                        <SplitButtonNE label="NE" className="p-button-primary" />
                    </div>
                </div>
            </div>
        {/* </MainLayout> */}
    </div>
  );
}

export default ReportingDataFlow;