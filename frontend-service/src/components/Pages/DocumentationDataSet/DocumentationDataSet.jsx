import React ,{ useState, useEffect, useContext}from 'react'
import styles from './DocumentationDataSet.module.scss';
import MainLayout from '../../Layout/main-layout.component';
import {BreadCrumb} from 'primereact/breadcrumb';
import ResourcesContext from '../../Context/ResourcesContext';
import uploadDummy from "../../../assets/jsons/uploadDummy";
import { TabView, TabPanel } from "primereact/tabview";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Button } from "primereact/button";
import ButtonsBar from '../../Layout/UI/ButtonsBar/ButtonsBar';
import IconComponent from '../../Layout/UI/icon-component';
import { ProgressSpinner } from 'primereact/progressspinner';
import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';

const DocumentationDataSet = ({ match, history }) => {

    const resources = useContext(ResourcesContext);  

    const [file, setFile] = useState();
    const [cols, setCols] = useState();
    const [breadCrumbItems,setBreadCrumbItems] = useState([]);
    const [customButtons, setCustomButtons] = useState([]);
    const [loading, setLoading] = useState(false);

    const home = {
		icon: resources.icons["home"],
		command: () => history.push("/")
	};

    //Bread Crumbs settings
    useEffect(() => {
        setBreadCrumbItems([
            { 
                label: resources.messages["dataFlowTask"], 
                command: () => history.push('/data-flow-task')
            },
            {
                label: resources.messages["reportingDataFlow"],
                command: () => history.push(`/reporting-data-flow/${match.params.dataFlowId}`)
            },
            { label: resources.messages["documents"] }
        ]);
    }, [history, match.params.dataFlowId, resources.messages]);

    //Data Fetching
    useEffect(() => {
        //TO DO change to real API call
        /* HTTPRequesterAPI.get({
            url: '/jsons/list-of-documents.json',
            queryString: {}
        })
        .then(response => {
            setFile(response.data);
            setLoading(false);
        })
        .catch(error => {
            setLoading(false);
            console.log("error", error);
            return error;
        }); */
        setFile(uploadDummy)

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

    const actionTemplate = (rowData, column) =>{
       
        return <a href={rowData.url} target="_blank"> <IconComponent icon="pi pi-file"/></a>     
    }
    
    const layout = children => {
        return (
            <MainLayout>
                <div className="titleDiv">
                    <BreadCrumb model={breadCrumbItems} home={home}/>
                </div>
                <div className="rep-container">{children}</div>
            </MainLayout>
        )
    }

    if (loading) {
        return layout(<ProgressSpinner/>)
    }
      

    if (file) {
    return layout (
        <TabView>
            <TabPanel header={resources.messages['documents']}>                    
                <ButtonsBar buttons={customButtons} />
                {
                <DataTable value={file} autoLayout={true}>
                    <Column
                        columnResizeMode="expand"
                        field="title"
                        header={resources.messages['title']}
                        filter={false}
                        filterMatchMode="contains"
                    />
                    <Column
                        field="description"
                        header={resources.messages['description']}
                        filter={false}
                        filterMatchMode="contains"
                    />
                    <Column
                        field="category"
                        header={resources.messages['category']}
                        filter={false}
                        filterMatchMode="contains"
                    />
                    <Column
                        field="language"
                        header={resources.messages['language']}
                        filter={false}
                        filterMatchMode="contains"
                    />
                    <Column
                        body={actionTemplate} style={{textAlign:'center', width: '8em'}}
                        field="url"
                        header={resources.messages['url']}
                        filter={false}
                        filterMatchMode="contains"
                    />
                </DataTable>                
                }

                <DataTable value={file}>{cols}</DataTable>
            </TabPanel>
            
            <TabPanel header={resources.messages['webLinks']}>
                {
                <DataTable value={file} autoLayout={true}>
                    <Column
                        columnResizeMode="expand"
                        field="title"
                        header={resources.messages['title']}
                        filter={false}
                        filterMatchMode="contains"
                    />
                    
                    <Column
                        field="url"
                        header={resources.messages['url']}
                        filter={false}
                        filterMatchMode="contains"
                    />
                </DataTable>                
                }
                <DataTable value={file}>{cols}</DataTable>
            </TabPanel>                
        </TabView>
    );
    } else {
    return <></>;
    }
}

export default DocumentationDataSet
