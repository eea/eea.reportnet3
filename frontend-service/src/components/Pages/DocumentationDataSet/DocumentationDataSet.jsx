import React ,{ useState, useEffect}from 'react'
import style from './DocumentationDataSet.module.scss';
import MainLayout from '../../Layout/main-layout.component';

import uploadDummy from '../../../assets/jsons/uploadDummy';
import { TabView, TabPanel } from "primereact/tabview";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Button } from "primereact/button";

const DocumentationDataSet = () => {
    const [file, setFile] = useState();
    const [cols, setCols] = useState();

    useEffect(() => {
        setFile(uploadDummy);
    }, [file]);

    if (file) {
    return (
        <MainLayout>
       
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
