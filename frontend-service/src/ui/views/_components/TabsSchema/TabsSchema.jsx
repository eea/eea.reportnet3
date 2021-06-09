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
  dataProviderId,
  hasWritePermissions = false,
  hasCountryCode,
  isExportable = true,
  isFilterable,
  isGroupedValidationDeleted,
  isGroupedValidationSelected,
  isReportingWebform,
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
  let tabs =
    tables && tableSchemaColumns
      ? tables.map(table => {
          return (
            <TabPanel header={table.name} key={table.id} rightIcon={table.hasErrors ? config.icons['warning'] : null}>
              <div className={styles.tabsSchema}>
                <DataViewer
                  buttonsList={buttonsList}
                  dataProviderId={dataProviderId}
                  hasCountryCode={hasCountryCode}
                  hasWritePermissions={hasWritePermissions}
                  isExportable={isExportable}
                  isFilterable={isFilterable}
                  isGroupedValidationDeleted={isGroupedValidationDeleted}
                  isGroupedValidationSelected={isGroupedValidationSelected}
                  isReportingWebform={isReportingWebform}
                  isValidationSelected={isValidationSelected}
                  key={table.id}
                  levelErrorTypes={levelErrorTypes}
                  onChangeIsValidationSelected={onChangeIsValidationSelected}
                  onHideSelectGroupedValidation={onHideSelectGroupedValidation}
                  onLoadTableData={onLoadTableData}
                  recordPositionId={table.id === tableSchemaId ? recordPositionId : -1}
                  reporting={reporting}
                  selectedRecordErrorId={table.id === tableSchemaId ? selectedRecordErrorId : -1}
                  selectedRuleId={selectedRuleId}
                  selectedRuleLevelError={selectedRuleLevelError}
                  selectedRuleMessage={selectedRuleMessage}
                  showWriteButtons={showWriteButtons}
                  tableFixedNumber={table.fixedNumber}
                  tableHasErrors={table.hasErrors}
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
                />
              </div>
            </TabPanel>
          );
        })
      : null;

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
