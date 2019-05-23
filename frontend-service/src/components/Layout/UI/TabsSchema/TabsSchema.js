import React from 'react';
import DataViewer from '../../../../containers/DataSets/DataViewer/DataViewer';
import { TabView, TabPanel } from 'primereact/tabview';
import styles from './TabsSchema.module.css';

const TabsSchema = (props) => {

    const clickTest = () =>{
        console.log("Click on button");
      }
      const customButtons = [
        {
          label: "Visibility",
          icon: "6",
          group: "left",
          disabled: false,
          clickHandler: clickTest
        },
        {
            label: "Filter",
            icon: "7",
            group: "left",
            disabled: false,
            clickHandler: clickTest
        },
        {
            label: "Group by",
            icon: "8",
            group: "left",
            disabled: false,
            clickHandler: clickTest
        },
        {
            label: "Sort",
            icon: "9",
            group: "left",
            disabled: false,
            clickHandler: clickTest
        }
    ];

    


    let tabs = 
        props.tables.map((table, i) => {
            return (
                <TabPanel header={table.name} key={table.name} rightIcon="pi pi-exclamation-triangle">
                    <div className={styles.TabsSchema}>
                        <DataViewer key={i} name={table.name} customButtons={customButtons} tableSchemaColumns={props.tableSchemaColumns.map(tab => tab.filter(t=>t.table===table.name)).filter(f=>f.length>0)[0]}/>
                    </div>
                </TabPanel>
            );
        });
    return (
        <TabView>
            {tabs}
        </TabView>
    );
}

export default TabsSchema;