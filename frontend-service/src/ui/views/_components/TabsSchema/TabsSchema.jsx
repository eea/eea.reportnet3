import React from 'react';

import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './TabsSchema.module.css';

import { config } from 'conf';

import { DataViewer } from 'ui/views/_components/DataViewer';
import { TabView } from 'ui/views/_components/TabView';
import { TabPanel } from 'ui/views/_components/TabView/_components/TabPanel';

import { QuerystringUtils } from 'ui/views/_functions/Utils/QuerystringUtils';
import { TabsUtils } from 'ui/views/_functions/Utils/TabsUtils';

export const TabsSchema = ({
  activeIndex = 0,
  buttonsList = undefined,
  hasWritePermissions = false,
  hasCountryCode,
  isDatasetDeleted,
  isExportable = true,
  isFilterable,
  isGroupedValidationDeleted,
  isGroupedValidationSelected,
  isValidationSelected,
  levelErrorTypes,
  onChangeIsValidationSelected,
  onHideSelectGroupedValidation,
  onLoadTableData,
  onTabChange,
  recordPositionId,
  reporting,
  selectedRecordErrorId,
  selectedRuleId,
  selectedRuleLevelError,
  selectedRuleMessage,
  showWriteButtons = true,
  tableSchemaId,
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
                  hasCountryCode={hasCountryCode}
                  hasWritePermissions={hasWritePermissions}
                  isDatasetDeleted={isDatasetDeleted}
                  isExportable={isExportable}
                  isFilterable={isFilterable}
                  isGroupedValidationDeleted={isGroupedValidationDeleted}
                  isGroupedValidationSelected={isGroupedValidationSelected}
                  isValidationSelected={isValidationSelected}
                  key={table.id}
                  levelErrorTypes={levelErrorTypes}
                  onChangeIsValidationSelected={onChangeIsValidationSelected}
                  onHideSelectGroupedValidation={onHideSelectGroupedValidation}
                  onLoadTableData={onLoadTableData}
                  reporting={reporting}
                  showWriteButtons={showWriteButtons}
                  tableFixedNumber={table.fixedNumber}
                  tableHasErrors={tableHasErrors}
                  tableId={table.id}
                  tableName={table.name}
                  tableReadOnly={table.readOnly}
                  tableSchemaColumns={
                    !isUndefined(tableSchemaColumns)
                      ? tableSchemaColumns
                          .map(tab => tab.filter(t => t.table === table.name))
                          .filter(f => f.length > 0)[0]
                      : []
                  }
                  recordPositionId={table.id === activeIndex ? recordPositionId : -1}
                  selectedRecordErrorId={table.id === tableSchemaId ? selectedRecordErrorId : -1}
                  selectedRuleId={selectedRuleId}
                  selectedRuleLevelError={selectedRuleLevelError}
                  selectedRuleMessage={selectedRuleMessage}
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
      activeIndex={
        !isNil(tables)
          ? TabsUtils.getIndexByTableProperty(
              !isNil(tableSchemaId)
                ? tableSchemaId
                : QuerystringUtils.getUrlParamValue('tab') !== ''
                ? QuerystringUtils.getUrlParamValue('tab')
                : '',
              tables,
              'id'
            )
          : 0
      }
      onTabChange={onTabChange}
      renderActiveOnly={false}
      tableSchemaId={tableSchemaId}>
      {tabs}
    </TabView>
  );
};
