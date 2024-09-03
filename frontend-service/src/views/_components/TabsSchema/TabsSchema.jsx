import { useContext } from 'react';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './TabsSchema.module.css';

import { config } from 'conf';

import { DataViewer } from 'views/_components/DataViewer';
import { TabView } from 'views/_components/TabView';
import { TabPanel } from 'views/_components/TabView/_components/TabPanel';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { QuerystringUtils } from 'views/_functions/Utils/QuerystringUtils';
import { TabsUtils } from 'views/_functions/Utils/TabsUtils';

export const TabsSchema = ({
  bigData,
  dataflowType,
  dataProviderId,
  datasetSchemaId,
  datasetType,
  hasCountryCode,
  hasWritePermissions = false,
  isExportable = true,
  isFilterable,
  isGroupedValidationDeleted,
  isGroupedValidationSelected,
  isReferenceDataset,
  isReportingWebform,
  isTableDataRestorationInProgress,
  levelErrorTypes,
  onHideSelectGroupedValidation,
  onLoadTableData,
  onRestoreData,
  onTabChange,
  reporting,
  selectedRuleId,
  selectedRuleLevelError,
  selectedRuleMessage,
  selectedShortCode,
  selectedTableSchemaId,
  showWriteButtons = true,
  tables,
  tableSchemaColumns,
  tableSchemaId
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const getRightIcon = tab => {
    if (tab.hasErrors) {
      return config.icons['warning'];
    }
  };

  const getRightIconTooltip = tab => {
    if (tab.hasErrors) {
      return resourcesContext.messages['tableWithErrorsTooltip'];
    }
  };

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
              numberOfFields={table.numberOfFields}
              readOnly={table.readOnly}
              rightIcon={getRightIcon(table)}
              rightIconTooltip={getRightIconTooltip(table)}
              toPrefill={table.toPrefill}>
              <div className={styles.tabsSchema}>
                <DataViewer
                  bigData={bigData}
                  dataAreManuallyEditable={table.dataAreManuallyEditable}
                  dataflowType={dataflowType}
                  dataProviderId={dataProviderId}
                  datasetSchemaId={datasetSchemaId}
                  datasetType={datasetType}
                  hasCountryCode={hasCountryCode}
                  hasWritePermissions={hasWritePermissions}
                  isExportable={isExportable}
                  isFilterable={isFilterable}
                  isGroupedValidationDeleted={isGroupedValidationDeleted}
                  isGroupedValidationSelected={isGroupedValidationSelected}
                  isReferenceDataset={isReferenceDataset}
                  isReportingWebform={isReportingWebform}
                  isTableDataRestorationInProgress={isTableDataRestorationInProgress}
                  key={table.id}
                  levelErrorTypes={levelErrorTypes}
                  onHideSelectGroupedValidation={onHideSelectGroupedValidation}
                  onLoadTableData={onLoadTableData}
                  onRestoreData={onRestoreData}
                  reporting={reporting}
                  selectedRuleId={selectedRuleId}
                  selectedRuleLevelError={selectedRuleLevelError}
                  selectedRuleMessage={selectedRuleMessage}
                  selectedShortCode={selectedShortCode}
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
                  toPrefill={table.toPrefill}
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
