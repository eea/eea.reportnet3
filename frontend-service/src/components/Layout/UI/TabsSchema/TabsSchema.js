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

    const newTableSchemaColumns = [...props.tableSchemaColumns];
console.log(newTableSchemaColumns);

    console.log(props.tableSchemaColumns)
    let tabs = 
        props.tables.map((table, i) => {
            return (
                <TabPanel header={table.name} key={table.name} rightIcon="pi pi-exclamation-triangle">
                    <div className={styles.TabsSchema}>
                        <DataViewer key={i} name={table.name} customButtons={customButtons} tableSchemaColumns={newTableSchemaColumns.reduce(tab=>tab.filter(t=>
                            {   
                                //console.log(t.table);
                                //console.log(table.name);
                                return t.table===table.name
                            }))}/>
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