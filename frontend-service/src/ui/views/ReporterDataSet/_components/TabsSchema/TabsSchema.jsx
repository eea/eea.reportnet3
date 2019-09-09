import React, { useContext } from 'react';

import styles from './TabsSchema.module.css';

import { config } from 'conf';

import { DataViewer } from './_components/DataViewer';

import { TabView, TabPanel } from 'primereact/tabview';

export const TabsSchema = ({
  activeIndex,
  buttonsList = undefined,
  onTabChange,
  recordPositionId,
  selectedRecordErrorId,
  tables,
  tableSchemaColumns,
  hasWritePermissions
}) => {
  let tabs =
    tables && tableSchemaColumns
      ? tables.map((table, i) => {
          return (
            <TabPanel header={table.name} key={table.id} rightIcon={table.hasErrors ? config.icons['warning'] : null}>
              <div className={styles.TabsSchema}>
                <DataViewer
                  hasWritePermissions={hasWritePermissions}
                  buttonsList={buttonsList}
                  key={table.id}
                  tableId={table.id}
                  tableName={table.name}
                  tableSchemaColumns={
                    tableSchemaColumns.map(tab => tab.filter(t => t.table === table.name)).filter(f => f.length > 0)[0]
                  }
                  recordPositionId={table.id === activeIndex ? recordPositionId : -1}
                  selectedRecordErrorId={table.id === activeIndex ? selectedRecordErrorId : -1}
                />
              </div>
            </TabPanel>
          );
        })
      : null;
  const filterActiveIndex = tableSchemaId => {
    //TODO: Refactorizar este apaño y CUIDADO con activeIndex (integer cuando es manual, idTable cuando es por validación).
    if (Number.isInteger(tableSchemaId)) {
      return tabs ? activeIndex : 0;
    } else {
      return tabs ? tabs.findIndex(t => t.key === tableSchemaId) : 0;
    }
  };

  return (
    <TabView
      renderActiveOnly={false}
      activeIndex={activeIndex ? filterActiveIndex(activeIndex) : 0}
      onTabChange={onTabChange}>
      {tabs}
    </TabView>
  );
};
