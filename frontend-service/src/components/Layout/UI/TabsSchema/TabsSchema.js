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
          clickHandler: clickTest
        },
        {
            label: "Filter",
            icon: "7",
            group: "left",
            clickHandler: clickTest
        },
        {
            label: "Group by",
            icon: "8",
            group: "left",
            clickHandler: clickTest
        },
        {
            label: "Sort",
            icon: "9",
            group: "left",
            clickHandler: clickTest
        }
    ];

    let tabs = 
        props.tables.map((table, i) => {
            return (
                <TabPanel header={table.name} key={table.name} rightIcon="pi pi-exclamation-triangle">
                    <div className={styles.TabsSchema}>
                        <DataViewer name={table.name} data={table.data} customButtons={customButtons}/>
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