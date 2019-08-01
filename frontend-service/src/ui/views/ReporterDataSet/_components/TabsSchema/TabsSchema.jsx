import React, { useContext } from 'react';

import styles from './TabsSchema.module.css';

import { DataViewer } from './_components/DataViewer';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { TabView, TabPanel } from 'primereact/tabview';

export const TabsSchema = ({
  tables,
  tableSchemaColumns,
  buttonsList = undefined,
  urlViewer,
  positionIdRecord,
  idSelectedRow,
  activeIndex,
  onTabChangeHandler
}) => {
  const resources = useContext(ResourcesContext);

  let tabs =
    tables && tableSchemaColumns
      ? tables.map((table, i) => {
          return (
            <TabPanel
              header={table.name}
              key={table.id}
              rightIcon={table.hasErrors ? resources.icons['warning'] : null}>
              <div className={styles.TabsSchema}>
                <DataViewer
                  key={table.id}
                  tableId={table.id}
                  tableName={table.name}
                  buttonsList={buttonsList}
                  tableSchemaColumns={
                    tableSchemaColumns.map(tab => tab.filter(t => t.table === table.name)).filter(f => f.length > 0)[0]
                  }
                  urlViewer={urlViewer}
                  positionIdRecord={positionIdRecord}
                  idSelectedRow={idSelectedRow}
                />
              </div>
            </TabPanel>
          );
        })
      : null;
  const filterActiveIndex = idTableSchema => {
    //TODO: Refactorizar este apaño.
    if (Number.isInteger(idTableSchema)) {
      return tabs ? activeIndex : 0;
    } else {
      return tabs ? tabs.findIndex(t => t.key === idTableSchema) : 0;
    }
  };

  return (
    <TabView activeIndex={activeIndex ? filterActiveIndex(activeIndex) : 0} onTabChange={onTabChangeHandler}>
      {tabs}
    </TabView>
  );
};
