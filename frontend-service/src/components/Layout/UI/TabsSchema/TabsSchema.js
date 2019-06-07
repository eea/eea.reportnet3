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
                <TabPanel header={table.name} key={table.name}>
                    <div className={styles.TabsSchema}>
                        <DataViewer key={table.id} id={table.id} name={table.name} customButtons={(props.customButtons)?props.customButtons:null} 
                                    tableSchemaColumns={props.tableSchemaColumns.map(tab => tab.filter(t=>t.table===table.name)).filter(f=>f.length>0)[0]}
                                    urlViewer={props.urlViewer}/>
                    </div>
                </TabPanel>
            );
        })
        : null;
    return (
        <TabView>
            {tabs}
        </TabView>
    );
}

export default TabsSchema;