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
  editedTables,
  hasCountryCode,
  hasWritePermissions = false,
  isDatasetReleased,
  isEditingEnabled,
  isEditor,
  isExportable = true,
  isFilterable,
  isGroupedValidationDeleted,
  isGroupedValidationSelected,
  isIcebergCreated,
  isReferenceDataset,
  isReportingWebform,
  isTableDataRestorationInProgress,
  levelErrorTypes,
  onHideSelectGroupedValidation,
  onLoadTableData,
  onRestoreData,
  onTabChange,
  preparationSetCode,
  reporting,
  selectedRuleId,
  selectedRuleLevelError,
  selectedRuleMessage,
  selectedShortCode,
  selectedTableSchemaId,
  showWriteButtons = true,
  tables,
  tableSchemaColumns,
  tableSchemaId,
  tableImportedMetadata
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const getRightIcon = tab => {
    
    // The priority for the Icon display is Blockers > Errors > Warnings > Infos, so if a tab has blockers, only the blockers tooltip will be shown, if it has errors but no blockers, only the errors tooltip will be shown and so on.
    if (tab.hasBlockers) {
      return config.icons['blocker'];
    }
    if (tab.hasErrors) {
      return config.icons['errorCircle'];
    }
    if (tab.hasWarnings) {
      return config.icons['warning'];
    }
    if (tab.hasInfos) {
      return config.icons['info'];
    }
  };

  // The priority for the tooltip is Blockers > Errors > Warnings > Infos, so if a tab has blockers, only the blockers tooltip will be shown, if it has errors but no blockers, only the errors tooltip will be shown and so on.
  const getRightIconTooltip = tab => {
    if (tab.hasBlockers) {
      return resourcesContext.messages['tableWithBlockersTooltip'];
    }
    if (tab.hasErrors) {
      return resourcesContext.messages['tableWithErrorsTooltip'];
    }
    if (tab.hasWarnings) {
      return resourcesContext.messages['tableWithWarningsTooltip'];
    }
    if (tab.hasInfos) {
      return resourcesContext.messages['tableWithInfosTooltip'];
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
                  isEditingEnabled={isEditingEnabled}
                  isEditor={isEditor}
                  isExportable={isExportable}
                  isFilterable={isFilterable}
                  isGroupedValidationDeleted={isGroupedValidationDeleted}
                  isGroupedValidationSelected={isGroupedValidationSelected}
                  isIcebergCreated={isIcebergCreated}
                  isReferenceDataset={isReferenceDataset}
                  isReportingWebform={isReportingWebform}
                  isTableDataRestorationInProgress={isTableDataRestorationInProgress}
                  isTableTop={true}
                  key={table.id}
                  levelErrorTypes={levelErrorTypes}
                  onHideSelectGroupedValidation={onHideSelectGroupedValidation}
                  onLoadTableData={onLoadTableData}
                  onRestoreData={onRestoreData}
                  preparationSetCode={preparationSetCode}
                  reporting={reporting}
                  selectedRuleId={selectedRuleId}
                  selectedRuleLevelError={selectedRuleLevelError}
                  selectedRuleMessage={selectedRuleMessage}
                  selectedShortCode={selectedShortCode}
                  selectedTableSchemaId={selectedTableSchemaId}
                  showWriteButtons={showWriteButtons}
                  tableFixedNumber={table.fixedNumber}
                  tableHasBlockers={table.hasBlockers}
                  tableHasErrors={table.hasErrors}
                  tableHasInfos={table.hasInfos}
                  tableHasWarnings={table.hasWarnings}
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
      editedTables={editedTables}
      isDatasetReleased={isDatasetReleased}
      name="TabsSchema"
      onTabChange={onTabChange}
      preventScrollLeft={true}
      tableImportedMetadata={tableImportedMetadata}
      tableSchemaId={tableSchemaId}>
      {tabs}
    </TabView>
  );
};
