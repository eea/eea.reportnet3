import React, { useState, useContext } from 'react'; //, { useContext }
import DataViewer from '../../../../containers/DataSets/DataViewer/DataViewer';
import { TabView, TabPanel } from 'primereact/tabview';
import {Dialog} from 'primereact/dialog';
import {CustomFileUpload} from '../CustomFileUpload/CustomFileUpload';

import styles from './TabsSchema.module.css';
import ResourcesContext from '../../../Context/ResourcesContext';

const TabsSchema = (props) => {
    const [importDialogVisible, setImportDialogVisible] = useState(false);
    const resources = useContext(ResourcesContext);

      const customButtons = [
        {
            label: resources.messages["import"],
            icon: "0",
            group: "left",
            disabled: false,
            clickHandler: () => setImportDialogVisible(true)
        },{
          label: resources.messages["visibility"],
          icon: "6",
          group: "left",
          disabled: true,
          clickHandler: null
        },{
            label: resources.messages["filter"],
            icon: "7",
            group: "left",
            disabled: true,
            clickHandler: null
        },{
            label: resources.messages["groupBy"],
            icon: "8",
            group: "left",
            disabled: true,
            clickHandler: null
        },{
            label: resources.messages["sort"],
            icon: "9",
            group: "left",
            disabled: true,
            clickHandler: null
        },{
            label: resources.messages["refresh"],
            icon: "11",
            group: "right",
            disabled: false,
            clickHandler: props.onRefresh
        }
    ];

    
    let tabs = (props.tables && props.tableSchemaColumns)?
        props.tables.map((table, i) => {
            return (
                // rightIcon={resources.icons["warning"]}
                <TabPanel header={table.name} key={table.name}>
                    <div className={styles.TabsSchema}>
                        <DataViewer key={table.id} id={table.id} name={table.name} customButtons={customButtons} 
                                    tableSchemaColumns={props.tableSchemaColumns.map(tab => tab.filter(t=>t.table===table.name)).filter(f=>f.length>0)[0]}/>
                        <Dialog header={resources.messages["uploadDataset"]} visible={importDialogVisible}
                                className={styles.Dialog} dismissableMask={false} onHide={() => setImportDialogVisible(false)} >
                            <CustomFileUpload mode="advanced" name="file" url={`http://127.0.0.1:8030/dataset/1/loadTableData/${table.id}`}
                                                onUpload={() => setImportDialogVisible(false)} 
                                                multiple={false} chooseLabel={resources.messages["selectFile"]} //allowTypes="/(\.|\/)(csv|doc)$/"
                                                fileLimit={1} className={styles.FileUpload}  /> 
                        </Dialog>  
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