import React ,{ useState, useEffect, useContext}from 'react'
import style from './DocumentationDataSet.module.scss';
import MainLayout from '../../Layout/main-layout.component';
import {BreadCrumb} from 'primereact/breadcrumb';
import uploadDummy from '../../../assets/jsons/uploadDummy';
import ResourcesContext from '../../Context/ResourcesContext';
import { TabView, TabPanel } from "primereact/tabview";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Button } from "primereact/button";

const DocumentationDataSet = () => {

    const resources = useContext(ResourcesContext);  

    const [file, setFile] = useState();
    const [cols, setCols] = useState();
    const [breadCrumbItems,setBreadCrumbItems] = useState([]);
    const home = {icon: resources.icons["home"], url: '#'};
    //Data Fetching
    useEffect(() => {
        setFile(uploadDummy);
    }, [file]);

    //Bread Crumbs settings
    useEffect(()=>{
        setBreadCrumbItems( [
            {label: resources.messages["newDataset"], url: '#'},
            {label: resources.messages["viewData"], url: '#'}
        ]); 
    } , [] )
    
      

    if (file) {
    return (
        <MainLayout>
         <BreadCrumb model={breadCrumbItems} home={home}/>
            <TabView>

                <TabPanel header="Documents">
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
