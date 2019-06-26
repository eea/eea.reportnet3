import React ,{ useState, useEffect, useContext}from 'react'
import styles from './DocumentationDataSet.module.scss';
import MainLayout from '../../Layout/main-layout.component';
import {BreadCrumb} from 'primereact/breadcrumb';
import uploadDummy from '../../../assets/jsons/uploadDummy';
import ResourcesContext from '../../Context/ResourcesContext';
import { TabView, TabPanel } from "primereact/tabview";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Button } from "primereact/button";
import ButtonsBar from '../../Layout/UI/ButtonsBar/ButtonsBar';
import IconComponent from '../../Layout/UI/icon-component';

const DocumentationDataSet = () => {

    const resources = useContext(ResourcesContext);  

    const [file, setFile] = useState();
    const [cols, setCols] = useState();
    const [breadCrumbItems,setBreadCrumbItems] = useState([]);
    const [customButtons, setCustomButtons] = useState([]);

    const home = {icon: resources.icons["home"], url: '#'};

    //Data Fetching
    useEffect(() => {
        //TO DO change to real API call
        setFile(uploadDummy);

        //#region Button inicialization              
        setCustomButtons([
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
                label: resources.messages["export"],
                icon: "1",
                group: "left",
                disabled: true,
                clickHandler: null
            }
           
          ]);
        //#end region Button inicialization

    }, [file]);

    //Bread Crumbs settings
    useEffect(()=>{
        setBreadCrumbItems( [
            {label: resources.messages["newDataset"], url: '#'},
            {label: resources.messages["viewData"], url: '#'}
        ]); 
    } , [] );

    const actionTemplate = (rowData, column) =>{
       
        return <a href={rowData.url}> <IconComponent icon="pi pi-file"/></a>     
    }
    
      

    if (file) {
    return (
        <MainLayout>
         <BreadCrumb model={breadCrumbItems} home={home}/>
           
            <TabView>
                
                <TabPanel header="Documents">
                      
                    <ButtonsBar buttons={customButtons} />
                
                    {

                    <DataTable value={file} autoLayout={true}>
                        
                        <Column
                        columnResizeMode="expand"
                        field="title"
                        header="Title"
                        filter={false}
                        filterMatchMode="contains"
                        />
                        <Column
                        field="description"
                        header="Description"
                        filter={false}
                        filterMatchMode="contains"
                        />

                        <Column
                        field="category"
                        header="Category"
                        filter={false}
                        filterMatchMode="contains"
                        />
                        <Column
                        body={actionTemplate} style={{textAlign:'center', width: '8em'}}
                        field="url"
                        header="Url"
                        filter={false}
                        filterMatchMode="contains"
                        />
                    </DataTable>
                    
                    }

                    <DataTable value={file}>{cols}</DataTable>
                </TabPanel>
                
                <TabPanel header="Web links">
                    {

                    <DataTable value={file} autoLayout={true}>
                        
                        <Column
                        columnResizeMode="expand"
                        field="title"
                        header="Title"
                        filter={false}
                        filterMatchMode="contains"
                        />
                       
                        <Column
                        field="url"
                        header="Url"
                        filter={false}
                        filterMatchMode="contains"
                        />
                    </DataTable>
                    
                    }

                    <DataTable value={file}>{cols}</DataTable>
                </TabPanel>
                
            </TabView>
        
        </MainLayout>
    );
    } else {
    return <></>;
    }
    
}

export default DocumentationDataSet
