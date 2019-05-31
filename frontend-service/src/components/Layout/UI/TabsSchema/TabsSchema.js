import React from 'react';
import DataViewer from '../../../../containers/DataSets/DataViewer/DataViewer';
import { TabView, TabPanel } from 'primereact/tabview';
import styles from './TabsSchema.module.css';

const TabsSchema = (props) => {

      const customButtons = [
        {
          label: "Visibility",
          icon: "6",
          group: "left",
          disabled: false,
          clickHandler: null
        },
        {
            label: "Filter",
            icon: "7",
            group: "left",
            disabled: false,
            clickHandler: null
        },
        {
            label: "Group by",
            icon: "8",
            group: "left",
            disabled: false,
            clickHandler: null
        },
        {
            label: "Sort",
            icon: "9",
            group: "left",
            disabled: false,
            clickHandler: null
        },
        {
            label: "Refresh",
            icon: "11",
            group: "right",
            disabled: false,
            clickHandler: props.onRefresh
        }
    ];

    
    let tabs = (props.tables && props.tableSchemaColumns)?
        props.tables.map((table, i) => {
            return (
                <TabPanel header={table.name} key={table.name} rightIcon="pi pi-exclamation-triangle">
                    <div className={styles.TabsSchema}>
                        <DataViewer key={table.id} id={table.id} name={table.name} customButtons={customButtons} tableSchemaColumns={props.tableSchemaColumns.map(tab => tab.filter(t=>t.table===table.name)).filter(f=>f.length>0)[0]}/>
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