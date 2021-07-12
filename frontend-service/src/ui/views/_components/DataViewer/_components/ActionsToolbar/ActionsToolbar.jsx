import { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import ReactDOMServer from 'react-dom/server';

import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { config } from 'conf';

import styles from './ActionsToolbar.module.scss';

import { Button } from 'ui/views/_components/Button';
import { ChipButton } from 'ui/views/_components/ChipButton';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { DropdownFilter } from 'ui/views/Dataset/_components/DropdownFilter';
import { InputText } from 'ui/views/_components/InputText';
import { Menu } from 'ui/views/_components/Menu';
import ReactTooltip from 'react-tooltip';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { TooltipButton } from 'ui/views/_components/TooltipButton';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

import { filterReducer } from './_functions/Reducers/filterReducer';

import { DatasetService } from 'core/services/Dataset';

import { MetadataUtils } from 'ui/views/_functions/Utils';

const ActionsToolbar = ({
  colsSchema,
  dataflowId,
  datasetId,
  hasWritePermissions,
  hideValidationFilter,
  isDataflowOpen,
  isDesignDatasetEditorRead,
  isExportable,
  isFilterable = true,
  isFilterValidationsActive,
  isGroupedValidationSelected,
  isLoading,
  isValidationSelected,
  levelErrorTypesWithCorrects,
  onHideSelectGroupedValidation,
  onSetVisible,
  onUpdateData,
  originalColumns,
  prevFilterValue,
  records,
  selectedRuleLevelError,
  selectedRuleMessage,
  selectedTableSchemaId,
  setColumns,
  setDeleteDialogVisible,
  setImportTableDialogVisible,
  showGroupedValidationFilter,
  showValidationFilter,
  showValueFilter,
  showWriteButtons,
  tableHasErrors,
  tableId,
  tableName
}) => {
  const [exportTableData, setExportTableData] = useState(undefined);
  const [exportTableDataName, setExportTableDataName] = useState('');
  const [isLoadingFile, setIsLoadingFile] = useState(false);
  const [filter, dispatchFilter] = useReducer(filterReducer, {
    groupedFilter: isGroupedValidationSelected,
    validationDropdown: [],
    valueFilter: prevFilterValue,
    visibilityDropdown: [],
    visibilityColumnIcon: 'eye'
  });

  const { groupedFilter, validationDropdown, valueFilter, visibilityDropdown, visibilityColumnIcon } = filter;

  const resources = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const exportMenuRef = useRef();
  const filterMenuRef = useRef();
  const dropdownFilterRef = useRef();

  useEffect(() => {
    const dropdownFilter = colsSchema.map(colSchema => {
      return { label: colSchema.header, key: colSchema.field };
    });

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
    if (isValidationSelected) {
      dispatchFilter({ type: 'SET_VALIDATION_FILTER', payload: { levelErrors: getLevelErrorFilters() } });
    }
  }, [isValidationSelected]);

  useEffect(() => {
    if (!isUndefined(exportTableData)) {
      DownloadFile(exportTableData, exportTableDataName);
    }
  }, [exportTableData]);

  const exportExtensionItems = config.exportTypes.exportTableTypes.map(type => ({
    label: type.text,
    icon: type.code,
    command: () => onExportTableData(type.code)
  }));

  const onExportTableData = async fileType => {
    setIsLoadingFile(true);
    try {
      setExportTableDataName(createTableName(tableName, fileType));
      const { data } = await DatasetService.exportTableDataById(datasetId, tableId, fileType);
      setExportTableData(data);
    } catch (error) {
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'EXPORT_TABLE_DATA_BY_ID_ERROR',
        content: {
          dataflowId,
          datasetId,
          dataflowName,
          datasetName,
          tableName
        }
      });
    } finally {
      setIsLoadingFile(false);
    }
  };

  const onSearchKeyEvent = event => {
    if (event.key === 'Enter') {
      showValueFilter(encodeURIComponent(valueFilter));
    }
  };

  const createTableName = (tableName, fileType) => {
    return `${tableName}.${fileType}`;
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
    let filters = [];
    if (!isUndefined(levelErrorTypesWithCorrects)) {
      levelErrorTypesWithCorrects.forEach(value => {
        if (!isUndefined(value) && !isNull(value)) {
          let filter = {
            label: capitalize(value),
            key: capitalize(value)
          };
          filters.push(filter);
        }
      });
    }
    return filters;
  };

  const getTooltipMessage = () => {
    return (
      <>
        <span style={{ fontWeight: 'bold' }}>{resources.messages['valueFilterTooltip']}</span> <br />
        <span style={{ fontStyle: 'italic' }}>{resources.messages['valueFilterTooltipGeometryNote']}</span>
        <br />
        <span style={{ fontStyle: 'italic' }}>{resources.messages['valueFilterTooltipCaseSensitiveNote']}</span>
      </>
    );
  };

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

  return (
    <Toolbar className={`${styles.actionsToolbar} datasetSchema-table-toolbar-help-step`}>
      <div className="p-toolbar-group-left">
        {(hasWritePermissions || showWriteButtons) && (
          <Button
            className={`p-button-rounded p-button-secondary datasetSchema-import-table-help-step ${
              !hasWritePermissions || isDataflowOpen || isDesignDatasetEditorRead ? null : 'p-button-animated-blink'
            }`}
            disabled={!hasWritePermissions || isDataflowOpen || isDesignDatasetEditorRead}
            icon={'import'}
            label={resources.messages['importTable']}
            onClick={() => setImportTableDialogVisible(true)}
          />
        )}
        {isExportable && (
          <Button
            className={`p-button-rounded p-button-secondary-transparent datasetSchema-export-table-help-step ${
              isDataflowOpen || isDesignDatasetEditorRead ? null : 'p-button-animated-blink'
            }`}
            disabled={isDataflowOpen || isDesignDatasetEditorRead}
            icon={isLoadingFile ? 'spinnerAnimate' : 'export'}
            id="buttonExportTable"
            label={resources.messages['exportTable']}
            onClick={event => {
              onUpdateData();
              exportMenuRef.current.show(event);
            }}
          />
        )}
        <Menu
          className={styles.menu}
          id="exportTableMenu"
          model={exportExtensionItems}
          popup={true}
          ref={exportMenuRef}
        />

        {(hasWritePermissions || showWriteButtons) && (
          <Button
            className={`p-button-rounded p-button-secondary-transparent datasetSchema-delete-table-help-step ${
              !hasWritePermissions || isUndefined(records.totalRecords) || isDataflowOpen || isDesignDatasetEditorRead
                ? null
                : 'p-button-animated-blink'
            }`}
            disabled={
              !hasWritePermissions || isUndefined(records.totalRecords) || isDataflowOpen || isDesignDatasetEditorRead
            }
            icon={'trash'}
            label={resources.messages['deleteTable']}
            onClick={() => onSetVisible(setDeleteDialogVisible, true)}
          />
        )}

        <Button
          className={`p-button-rounded p-button-secondary-transparent datasetSchema-showColumn-help-step ${
            isDataflowOpen || isDesignDatasetEditorRead ? null : 'p-button-animated-blink'
          }`}
          disabled={isDataflowOpen || isDesignDatasetEditorRead}
          icon={'eye'}
          iconClasses={visibilityColumnIcon === 'eye' ? styles.filterInactive : styles.filterActive}
          label={resources.messages['showHideColumns']}
          onClick={event => {
            dropdownFilterRef.current.show(event);
          }}
        />
        <DropdownFilter
          className={`p-button-animated-blink`}
          filters={visibilityDropdown}
          id="dropdownFilterMenu"
          onShow={event => getExportButtonPosition(event)}
          popup={true}
          ref={dropdownFilterRef}
          showFilters={showFilters}
        />

        {isFilterable && (
          <Fragment>
            <Button
              className={`p-button-rounded p-button-secondary-transparent datasetSchema-validationFilter-help-step ${
                tableHasErrors ? 'p-button-animated-blink' : null
              }`}
              disabled={!tableHasErrors}
              icon={'filter'}
              iconClasses={!isFilterValidationsActive ? styles.filterInactive : styles.filterActive}
              label={resources.messages['validationFilter']}
              onClick={event => filterMenuRef.current.show(event)}
            />
            <DropdownFilter
              className={!isLoading ? 'p-button-animated-blink' : null}
              disabled={isLoading}
              filters={validationDropdown}
              hide={hideValidationFilter}
              id="filterValidationDropdown"
              onShow={e => {
                getExportButtonPosition(e);
              }}
              popup={true}
              ref={filterMenuRef}
              showFilters={showValidationFilter}
              showLevelErrorIcons={true}
            />
            {groupedFilter && selectedRuleMessage !== '' && tableId === selectedTableSchemaId && (
              <ChipButton
                hasLevelErrorIcon={true}
                labelClassName={styles.groupFilter}
                levelError={selectedRuleLevelError}
                onClick={() => {
                  onHideSelectGroupedValidation();
                  showGroupedValidationFilter(false);
                  dispatchFilter({
                    type: 'SET_VALIDATION_GROUPED_FILTER',
                    payload: { groupedFilter: false }
                  });
                }}
                tooltip={selectedRuleMessage}
                tooltipOptions={{ position: 'top' }}
                value={selectedRuleMessage}
              />
            )}
          </Fragment>
        )}
        {prevFilterValue !== '' && (
          <Fragment>
            <span data-for="valueFilterTooltip" data-tip>
              <ChipButton
                icon="search"
                labelClassName={styles.groupFilter}
                levelError={selectedRuleLevelError}
                onClick={() => showValueFilter('')}
                value={decodeURIComponent(prevFilterValue)}
              />
            </span>
            <ReactTooltip border={true} effect="solid" id="valueFilterTooltip" place="top">
              {decodeURIComponent(prevFilterValue)}
            </ReactTooltip>
          </Fragment>
        )}
      </div>
      <div className={`p-toolbar-group-right ${styles.valueFilterWrapper}`}>
        <span className={styles.input}>
          <span className={`p-float-label ${styles.label}`}>
            <InputText
              className={styles.inputFilter}
              id="value_filter_input"
              name={resources.messages['valueFilter']}
              onChange={event => dispatchFilter({ type: 'SET_VALUE_FILTER', payload: event.target.value })}
              onKeyDown={onSearchKeyEvent}
              value={valueFilter}
            />
            {!isEmpty(valueFilter) && (
              <Button
                className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
                icon="cancel"
                onClick={() => dispatchFilter({ type: 'SET_VALUE_FILTER', payload: '' })}
              />
            )}
            <Button
              className="p-button-secondary-transparent"
              icon="search"
              onClick={() => showValueFilter(encodeURIComponent(valueFilter))}
            />
            <label
              className={`${styles.label} ${valueFilter !== '' && styles.labelFilled}`}
              htmlFor="value_filter_input">
              {resources.messages['valueFilter']}
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

export { ActionsToolbar };
