import React from 'react'; //, { useContext }
import DataViewer from '../../../../containers/DataSets/DataViewer/DataViewer';
import { TabView, TabPanel } from 'primereact/tabview';
import styles from './TabsSchema.module.css';
// import ResourcesContext from '../../../Context/ResourcesContext';

const TabsSchema = (props) => {
    // const resources = useContext(ResourcesContext);    
    
    let tabs = (props.tables && props.tableSchemaColumns)?
        props.tables.map((table, i) => {
            return (
                // rightIcon={resources.icons["warning"]}
                //TODO: Refactorizar para no renderizar siempre DataViewer sino pasárselo como composición de componentes. Así se podrá reutilizar
                //TabsSchema para cualquier visualización de datos
                <TabPanel header={table.name} key={table.id}>
                    <div className={styles.TabsSchema}>
                        <DataViewer key={table.id} id={table.id} name={table.name} customButtons={(props.customButtons)?props.customButtons:null} 
                                    tableSchemaColumns={props.tableSchemaColumns.map(tab => tab.filter(t=>t.table===table.name)).filter(f=>f.length>0)[0]}
                                    urlViewer={props.urlViewer}
                                    linkedErrorData={props.linkedErrorData}/>
                    </div>
                </TabPanel>
            );
        })
        : null;
    const filterActiveIndex = (idTableSchema) =>{
        //TODO: Refactorizar este apaño.
        if (Number.isInteger(idTableSchema)){
            return (tabs) ? props.activeIndex : 0;
        }
        else{
            return (tabs) ?  tabs.findIndex(t => t.key === idTableSchema) : 0;
        }
    }

    return (
        <TabView activeIndex={(props.activeIndex) ? filterActiveIndex(props.activeIndex) : 0} onTabChange={props.onTabChangeHandler}>
            {tabs}
        </TabView>
    );
}

export default TabsSchema;