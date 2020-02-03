import React from 'react';

import { isUndefined } from 'lodash';

import styles from './TabsSchema.module.css';

import { config } from 'conf';

import { DataViewer } from 'ui/views/_components/DataViewer';
import { TabView } from 'ui/views/_components/TabView';
import { TabPanel } from 'ui/views/_components/TabView/_components/TabPanel';

export const TabsSchema = ({
  activeIndex = 0,
  buttonsList = undefined,
  levelErrorTypes,
  hasWritePermissions,
  isDataCollection,
  isValidationSelected,
  isWebFormMMR,
  onLoadTableData,
  onTabChange,
  recordPositionId,
  selectedRecordErrorId,
  setIsValidationSelected,
  tables,
  tableSchemaColumns
}) => {
  let tableHasErrors = true;
  if (!isUndefined(tables) && !isUndefined(tables[activeIndex])) {
    tableHasErrors = tables[activeIndex].hasErrors;
  }
  let tabs =
    tables && tableSchemaColumns
      ? tables.map(table => {
          return (
            <TabPanel header={table.name} key={table.id} rightIcon={table.hasErrors ? config.icons['warning'] : null}>
              <div className={styles.tabsSchema}>
                <DataViewer
                  buttonsList={buttonsList}
                  levelErrorTypes={levelErrorTypes}
                  hasWritePermissions={hasWritePermissions}
                  isDataCollection={isDataCollection}
                  isWebFormMMR={isWebFormMMR}
                  key={table.id}
                  isValidationSelected={isValidationSelected}
                  onLoadTableData={onLoadTableData}
                  tableHasErrors={tableHasErrors}
                  tableId={table.id}
                  tableName={table.name}
                  tableSchemaColumns={
                    !isUndefined(tableSchemaColumns)
                      ? tableSchemaColumns
                          .map(tab => tab.filter(t => t.table === table.name))
                          .filter(f => f.length > 0)[0]
                      : []
                  }
                  recordPositionId={table.id === activeIndex ? recordPositionId : -1}
                  selectedRecordErrorId={table.id === activeIndex ? selectedRecordErrorId : -1}
                  setIsValidationSelected={setIsValidationSelected}
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
      activeIndex={activeIndex ? filterActiveIndex(activeIndex) : 0}
      onTabChange={onTabChange}
      renderActiveOnly={false}>
      {tabs}
    </TabView>
  );
};
