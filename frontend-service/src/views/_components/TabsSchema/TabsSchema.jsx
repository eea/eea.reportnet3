import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './TabsSchema.module.css';

import { config } from 'conf';

import { DataViewer } from 'views/_components/DataViewer';
import { TabView } from 'views/_components/TabView';
import { TabPanel } from 'views/_components/TabView/_components/TabPanel';

import { QuerystringUtils } from 'views/_functions/Utils/QuerystringUtils';
import { TabsUtils } from 'views/_functions/Utils/TabsUtils';

export const TabsSchema = ({
  dataflowType,
  dataProviderId,
  datasetSchemaId,
  hasCountryCode,
  hasWritePermissions = false,
  isExportable = true,
  isFilterable,
  isGroupedValidationDeleted,
  isGroupedValidationSelected,
  isReferenceDataset,
  isReportingWebform,
  levelErrorTypes,
  onHideSelectGroupedValidation,
  onLoadTableData,
  onTabChange,
  reporting,
  selectedRuleId,
  selectedRuleLevelError,
  selectedRuleMessage,
  selectedTableSchemaId,
  showWriteButtons = true,
  tables,
  tableSchemaColumns,
  tableSchemaId
}) => {
  let tabs =
    tables && tableSchemaColumns
      ? tables.map(table => {
          return (
            <TabPanel
              description={table.description}
              fixedNumber={table.fixedNumber}
              hasInfoTooltip={table.hasInfoTooltip}
              header={table.name}
              key={table.id}
              notEmpty={table.notEmpty}
              readOnly={table.readOnly}
              rightIcon={table.hasErrors ? config.icons['warning'] : null}
              toPrefill={table.toPrefill}>
              <div className={styles.tabsSchema}>
                <DataViewer
                  dataflowType={dataflowType}
                  dataProviderId={dataProviderId}
                  datasetSchemaId={datasetSchemaId}
                  hasCountryCode={hasCountryCode}
                  hasWritePermissions={hasWritePermissions}
                  isExportable={isExportable}
                  isFilterable={isFilterable}
                  isGroupedValidationDeleted={isGroupedValidationDeleted}
                  isGroupedValidationSelected={isGroupedValidationSelected}
                  isReferenceDataset={isReferenceDataset}
                  isReportingWebform={isReportingWebform}
                  key={table.id}
                  levelErrorTypes={levelErrorTypes}
                  onHideSelectGroupedValidation={onHideSelectGroupedValidation}
                  onLoadTableData={onLoadTableData}
                  reporting={reporting}
                  selectedRuleId={selectedRuleId}
                  selectedRuleLevelError={selectedRuleLevelError}
                  selectedRuleMessage={selectedRuleMessage}
                  selectedTableSchemaId={selectedTableSchemaId}
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
      name="TabsSchema"
      onTabChange={onTabChange}
      tableSchemaId={tableSchemaId}>
      {tabs}
    </TabView>
  );
};
