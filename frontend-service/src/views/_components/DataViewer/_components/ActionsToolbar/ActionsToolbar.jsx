import { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import ReactDOMServer from 'react-dom/server';

import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';
import ReactTooltip from 'react-tooltip';

import { config } from 'conf';

import styles from './ActionsToolbar.module.scss';

import { Button } from 'views/_components/Button';
import { ChipButton } from 'views/_components/ChipButton';
import { DeleteDialog } from './_components/DeleteDialog';
import { DropdownFilter } from 'views/Dataset/_components/DropdownFilter';
import { ImportTableDataDialog } from './_components/ImportTableDataDialog';
import { InputText } from 'views/_components/InputText';
import { Menu } from 'views/_components/Menu';
import { Toolbar } from 'views/_components/Toolbar';
import { TooltipButton } from 'views/_components/TooltipButton';

import { DatasetService } from 'services/DatasetService';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';

import { filterReducer } from './_functions/Reducers/filterReducer';

import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';

import { MetadataUtils } from 'views/_functions/Utils';
import { TextUtils } from 'repositories/_utils/TextUtils';

export const ActionsToolbar = ({
  colsSchema,
  dataflowId,
  datasetId,
  hasWritePermissions,
  isDataflowOpen,
  isDesignDatasetEditorRead,
  isExportable,
  isFilterable = true,
  isFilterValidationsActive,
  isGroupedValidationSelected,
  isLoading,
  levelErrorTypesWithCorrects,
  levelErrorValidations,
  onHideSelectGroupedValidation,
  onConfirmDeleteTable,
  onUpdateData,
  originalColumns,
  prevFilterValue,
  records,
  selectedRuleId,
  selectedRuleLevelError,
  selectedRuleMessage,
  selectedTableSchemaId,
  setColumns,
  showGroupedValidationFilter,
  showValidationFilter,
  showValueFilter,
  showWriteButtons,
  tableHasErrors,
  tableId,
  tableName
}) => {
  const [isFilteredByValue, setIsFilteredByValue] = useState(false);
  const [isLoadingFile, setIsLoadingFile] = useState(false);
  const [filter, dispatchFilter] = useReducer(filterReducer, {
    groupedFilter: isGroupedValidationSelected,
    validationDropdown: [],
    valueFilter: prevFilterValue,
    visibilityDropdown: [],
    visibilityColumnIcon: 'eye'
  });

  const { groupedFilter, validationDropdown, valueFilter, visibilityDropdown, visibilityColumnIcon } = filter;

  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const exportMenuRef = useRef();
  const filterMenuRef = useRef();
  const dropdownFilterRef = useRef();

  useEffect(() => {
    const dropdownFilter = colsSchema.map(colSchema => ({ label: colSchema.header, key: colSchema.field }));
    dispatchFilter({ type: 'INIT_FILTERS', payload: { dropdownFilter, levelErrors: getLevelErrorFilters() } });
  }, []);

  useEffect(() => {
    if (isGroupedValidationSelected) {
      dispatchFilter({
        type: 'SET_VALIDATION_GROUPED_FILTER',
        payload: { groupedFilter: isGroupedValidationSelected }
      });
    }
  }, [isGroupedValidationSelected]);

  useEffect(() => {
    if (notificationContext.hidden.some(notification => notification.key === 'EXPORT_TABLE_DATA_COMPLETED_EVENT')) {
      setIsLoadingFile(false);
    }
  }, [notificationContext.hidden]);

  useCheckNotifications(['EXPORT_TABLE_DATA_FAILED_EVENT'], setIsLoadingFile, false);

  const exportExtensionItems = config.exportTypes.exportTableTypes.map(type => ({
    command: () => onExportTableData(type),
    icon: type.code,
    label: resourcesContext.messages[type.key]
  }));

  const onExportTableData = async type => {
    setIsLoadingFile(true);
    notificationContext.add({ type: 'EXPORT_TABLE_DATA_START' }, true);
    try {
      const isExportFilteredCsv = TextUtils.areEquals(type.key, 'exportFilteredCsv');
      await DatasetService.exportTableData(
        datasetId,
        tableId,
        type.code,
        isFilteredByValue ? filter.valueFilter : '',
        levelErrorValidations.map(levelError => levelError.toUpperCase()),
        selectedRuleId,
        isExportFilteredCsv,
        isFilterValidationsActive
      );
    } catch (error) {
      console.error('ActionsToolbar - onExportTableData.', error);
      setIsLoadingFile(false);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
      notificationContext.add(
        {
          type: 'EXPORT_TABLE_DATA_BY_ID_ERROR',
          content: {
            dataflowId,
            datasetId,
            dataflowName,
            datasetName,
            customContent: { tableName }
          }
        },
        true
      );
    }
  };

  const onSearchKeyEvent = event => {
    if (event.key === 'Enter') {
      setIsFilteredByValue(true);
      showValueFilter(encodeURIComponent(valueFilter));
    }
  };

  const getExportButtonPosition = e => {
    const exportButton = e.currentTarget;
    const left = `${exportButton.offsetLeft}px`;
    const topValue = exportButton.offsetHeight + exportButton.offsetTop + 3;
    const top = `${topValue}px `;
    const menu = exportButton.nextElementSibling;
    menu.style.top = top;
    menu.style.left = left;
  };

  const getLevelErrorFilters = () => {
    const filters = [];

    if (!isUndefined(levelErrorTypesWithCorrects)) {
      levelErrorTypesWithCorrects.forEach(value => {
        if (!isUndefined(value) && !isNull(value)) {
          filters.push({
            label: capitalize(value),
            key: capitalize(value)
          });
        }
      });
    }

    return filters;
  };

  const getTooltipMessage = () => (
    <Fragment>
      <span style={{ fontStyle: 'italic' }}>{resourcesContext.messages['valueFilterTooltipGeometryNote']}</span>
      <span style={{ fontStyle: 'italic' }}>{resourcesContext.messages['valueFilterTooltipCaseSensitiveNote']}</span>
    </Fragment>
  );

  const showFilters = columnKeys => {
    const mustShowColumns = ['actions', 'recordValidation', 'id', 'datasetPartitionId', 'providerCode'];
    const currentVisibleColumns = originalColumns.filter(
      column => columnKeys.includes(column.key) || mustShowColumns.includes(column.key)
    );

    if (!isUndefined(setColumns)) {
      setColumns(currentVisibleColumns);
    }

    dispatchFilter({ type: 'SET_FILTER_ICON', payload: { originalColumns, currentVisibleColumns } });
  };

  const renderExportableButton = () => {
    if (isExportable) {
      return (
        <Button
          className={`p-button-rounded p-button-secondary-transparent datasetSchema-export-table-help-step ${
            isDataflowOpen || isDesignDatasetEditorRead ? null : 'p-button-animated-blink'
          }`}
          disabled={isDataflowOpen || isDesignDatasetEditorRead}
          icon={isLoadingFile ? 'spinnerAnimate' : 'export'}
          id="buttonExportTable"
          label={resourcesContext.messages['exportTable']}
          onClick={event => {
            onUpdateData();
            exportMenuRef.current.show(event);
          }}
        />
      );
    }
  };

  const renderFilterableButton = () => {
    const renderChipButton = () => {
      if (groupedFilter && selectedRuleMessage !== '' && tableId === selectedTableSchemaId) {
        return (
          <Fragment>
            <span data-for="groupedFilterTooltip" data-tip>
              <ChipButton
                className={styles.chipButton}
                hasLevelErrorIcon={true}
                labelClassName={styles.groupFilter}
                levelError={selectedRuleLevelError}
                onClick={() => {
                  onHideSelectGroupedValidation();
                  showGroupedValidationFilter();
                  dispatchFilter({
                    type: 'SET_VALIDATION_GROUPED_FILTER',
                    payload: { groupedFilter: false }
                  });
                }}
                value={selectedRuleMessage}
              />
            </span>
            <ReactTooltip border={true} effect="solid" id="groupedFilterTooltip" place="top">
              {selectedRuleMessage}
            </ReactTooltip>
          </Fragment>
        );
      }
    };

    if (isFilterable) {
      return (
        <Fragment>
          <Button
            className={`p-button-rounded p-button-secondary-transparent datasetSchema-validationFilter-help-step ${
              tableHasErrors ? 'p-button-animated-blink' : null
            }`}
            disabled={!tableHasErrors}
            icon="filter"
            iconClasses={!isFilterValidationsActive ? styles.filterInactive : styles.filterActive}
            label={resourcesContext.messages['validationFilter']}
            onClick={event => filterMenuRef.current.show(event)}
          />
          <DropdownFilter
            className={!isLoading ? 'p-button-animated-blink' : null}
            disabled={isLoading}
            filters={validationDropdown}
            id="filterValidationDropdown"
            onShow={e => {
              getExportButtonPosition(e);
            }}
            popup={true}
            ref={filterMenuRef}
            showFilters={showValidationFilter}
            showLevelErrorIcons={true}
          />
          {renderChipButton()}
        </Fragment>
      );
    }
  };

  const renderFilterSearch = () => {
    if (prevFilterValue !== '') {
      return (
        <Fragment>
          <span data-for="valueFilterTooltip" data-tip>
            <ChipButton
              className={styles.chipButton}
              icon="search"
              labelClassName={styles.groupFilter}
              levelError={selectedRuleLevelError}
              onClick={() => {
                showValueFilter('');
                setIsFilteredByValue(false);
              }}
              value={decodeURIComponent(prevFilterValue)}
            />
          </span>
          <ReactTooltip border={true} effect="solid" id="valueFilterTooltip" place="top">
            {decodeURIComponent(prevFilterValue)}
          </ReactTooltip>
        </Fragment>
      );
    }
  };

  const renderImportTableDataButton = () => (
    <ImportTableDataDialog
      colsSchema={colsSchema}
      dataflowId={dataflowId}
      datasetId={datasetId}
      hasWritePermissions={hasWritePermissions}
      isDataflowOpen={isDataflowOpen}
      isDesignDatasetEditorRead={isDesignDatasetEditorRead}
      showWriteButtons={showWriteButtons}
      tableId={tableId}
      tableName={tableName}
    />
  );

  const renderValueFilter = () => {
    if (isEmpty(valueFilter)) {
      return <span style={{ width: '2.357em' }} />;
    }

    return (
      <Button
        className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
        icon="cancel"
        onClick={() => dispatchFilter({ type: 'SET_VALUE_FILTER', payload: '' })}
      />
    );
  };

  return (
    <Toolbar className={`${styles.actionsToolbar} datasetSchema-table-toolbar-help-step`}>
      <div className={`${styles.toolbarLeftContent} p-toolbar-group-left`}>
        {renderImportTableDataButton()}
        {renderExportableButton()}
        <Menu
          className={styles.menu}
          id="exportTableMenu"
          model={exportExtensionItems}
          popup={true}
          ref={exportMenuRef}
        />
        <DeleteDialog
          disabled={
            !hasWritePermissions || isUndefined(records.totalRecords) || isDataflowOpen || isDesignDatasetEditorRead
          }
          hasWritePermissions={hasWritePermissions}
          onConfirmDeleteTable={onConfirmDeleteTable}
          showWriteButtons={showWriteButtons}
          tableName={tableName}
        />
        <Button
          className={`p-button-rounded p-button-secondary-transparent datasetSchema-showColumn-help-step ${
            isDataflowOpen || isDesignDatasetEditorRead ? null : 'p-button-animated-blink'
          }`}
          disabled={isDataflowOpen || isDesignDatasetEditorRead}
          icon="eye"
          iconClasses={visibilityColumnIcon === 'eye' ? styles.filterInactive : styles.filterActive}
          label={resourcesContext.messages['showHideColumns']}
          onClick={event => {
            dropdownFilterRef.current.show(event);
          }}
        />
        <DropdownFilter
          className="p-button-animated-blink"
          filters={visibilityDropdown}
          id="dropdownFilterMenu"
          onShow={event => getExportButtonPosition(event)}
          popup={true}
          ref={dropdownFilterRef}
          showFilters={showFilters}
        />
        {renderFilterableButton()}
        {renderFilterSearch()}
      </div>
      <div className={`p-toolbar-group-right ${styles.valueFilterWrapper}`}>
        <span className={styles.input}>
          <span className={`p-float-label ${styles.label}`}>
            <InputText
              className={styles.inputFilter}
              id={`value_filter_input_${tableId}`}
              name={resourcesContext.messages['valueFilter']}
              onChange={event => dispatchFilter({ type: 'SET_VALUE_FILTER', payload: event.target.value })}
              onKeyDown={onSearchKeyEvent}
              value={valueFilter}
            />
            {renderValueFilter()}
            <Button
              className="p-button-secondary"
              icon="search"
              onClick={() => {
                showValueFilter(encodeURIComponent(valueFilter));
                setIsFilteredByValue(true);
              }}
            />
            <label
              className={`${styles.label} ${valueFilter !== '' && styles.labelFilled}`}
              htmlFor={`value_filter_input_${tableId}`}>
              {resourcesContext.messages['valueFilter']}
            </label>
          </span>
        </span>
        <TooltipButton
          getContent={() =>
            ReactDOMServer.renderToStaticMarkup(
              <div
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'flex-start'
                }}>
                {getTooltipMessage()}
              </div>
            )
          }
          uniqueIdentifier="valueFilter"
        />
      </div>
    </Toolbar>
  );
};
