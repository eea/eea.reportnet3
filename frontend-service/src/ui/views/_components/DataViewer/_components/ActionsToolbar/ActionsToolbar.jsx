import React, { useContext, useEffect, useRef, useState, useReducer } from 'react';
import { isUndefined, isNull, capitalize } from 'lodash';

import { config } from 'conf';

import styles from './ActionsToolbar.module.css';

import { Button } from 'ui/views/_components/Button';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { DropdownFilter } from 'ui/views/Dataset/_components/DropdownFilter';
import { Menu } from 'primereact/menu';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DatasetContext } from 'ui/views/_functions/Contexts/DatasetContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { filterReducer } from './_functions/Reducers/filterReducer';

import { DatasetService } from 'core/services/Dataset';

const ActionsToolbar = ({
  colsSchema,
  datasetId,
  hasWritePermissions,
  isFilterValidationsActive,
  isWebFormMMR,
  isLoading,
  levelErrorTypesWithCorrects,
  onRefresh,
  onSetColumns,
  onSetInvisibleColumns,
  onSetVisible,
  originalColumns,
  records,
  setDeleteDialogVisible,
  setImportDialogVisible,
  showValidationFilter,
  tableHasErrors,
  tableId,
  tableName
}) => {
  const [exportTableData, setExportTableData] = useState(undefined);
  const [exportTableDataName, setExportTableDataName] = useState('');
  const [isLoadingFile, setIsLoadingFile] = useState(false);

  const [filter, dispatchFilter] = useReducer(filterReducer, {
    validationDropdown: [],
    visibilityDropdown: [],
    visibilityColumnIcon: 'eye'
  });

  const datasetContext = useContext(DatasetContext);
  const resources = useContext(ResourcesContext);

  let exportMenuRef = useRef();
  let filterMenuRef = useRef();
  let dropdownFilterRef = useRef();

  useEffect(() => {
    const dropdownFilter = colsSchema.map(colSchema => {
      return { label: colSchema.header, key: colSchema.field };
    });
    dispatchFilter({ type: 'INIT_FILTERS', payload: { dropdownFilter, levelErrors: getLevelErrorFilters() } });
  }, []);

  useEffect(() => {
    if (datasetContext.isValidationSelected) {
      dispatchFilter({ type: 'SET_VALIDATION_FILTER', payload: { levelErrors: getLevelErrorFilters() } });
    }
  }, [datasetContext.isValidationSelected]);

  useEffect(() => {
    if (!isUndefined(exportTableData)) {
      DownloadFile(exportTableData, exportTableDataName);
    }
  }, [exportTableData]);

  const onExportTableData = async fileType => {
    setIsLoadingFile(true);
    try {
      setExportTableDataName(createTableName(tableName, fileType));
      setExportTableData(await DatasetService.exportTableDataById(datasetId, tableId, fileType));
    } catch (error) {
      console.error(error);
    } finally {
      setIsLoadingFile(false);
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
    levelErrorTypesWithCorrects.forEach(value => {
      if (!isUndefined(value) && !isNull(value)) {
        let filter = {
          label: capitalize(value),
          key: capitalize(value)
        };
        filters.push(filter);
      }
    });
    return filters;
  };

  const showFilters = columnKeys => {
    const mustShowColumns = ['actions', 'recordValidation', 'id', 'datasetPartitionId'];
    const currentinvisibleColumns = originalColumns.filter(
      column => columnKeys.includes(column.key) || mustShowColumns.includes(column.key)
    );
    if (!isUndefined(onSetColumns)) {
      onSetColumns(currentinvisibleColumns);
    }
    if (!isUndefined(onSetColumns)) {
      onSetInvisibleColumns(currentinvisibleColumns);
    }
    dispatchFilter({ type: 'SET_FILTER_ICON', payload: { originalColumns, currentinvisibleColumns } });
  };

  return (
    <Toolbar className={styles.actionsToolbar}>
      <div className="p-toolbar-group-left">
        <Button
          className={`p-button-rounded p-button-secondary`}
          disabled={!hasWritePermissions || isWebFormMMR}
          icon={'export'}
          label={resources.messages['import']}
          onClick={() => setImportDialogVisible(true)}
        />

        <Button
          disabled={!hasWritePermissions}
          id="buttonExportTable"
          className={`p-button-rounded p-button-secondary`}
          icon={isLoadingFile ? 'spinnerAnimate' : 'import'}
          label={resources.messages['exportTable']}
          onClick={event => {
            exportMenuRef.current.show(event);
          }}
        />
        <Menu
          model={config.exportTypes.map(type => ({
            label: type.text,
            icon: config.icons['archive'],
            command: () => onExportTableData(type.code)
          }))}
          popup={true}
          ref={exportMenuRef}
          id="exportTableMenu"
          onShow={e => getExportButtonPosition(e)}
        />

        <Button
          className={`p-button-rounded p-button-secondary`}
          disabled={!hasWritePermissions || isWebFormMMR || isUndefined(records.totalRecords)}
          icon={'trash'}
          label={resources.messages['deleteTable']}
          onClick={() => onSetVisible(setDeleteDialogVisible, true)}
        />

        <Button
          className={`p-button-rounded p-button-secondary`}
          disabled={false}
          icon={filter.visibilityColumnIcon}
          label={resources.messages['showHideColumns']}
          onClick={event => {
            dropdownFilterRef.current.show(event);
          }}
        />
        <DropdownFilter
          filters={filter.visibilityDropdown}
          popup={true}
          ref={dropdownFilterRef}
          id="dropdownFilterMenu"
          showFilters={showFilters}
          onShow={e => {
            getExportButtonPosition(e);
          }}
        />

        <Button
          className={'p-button-rounded p-button-secondary'}
          disabled={!tableHasErrors}
          icon="filter"
          iconClasses={!isFilterValidationsActive ? styles.filterInactive : ''}
          label={resources.messages['validationFilter']}
          onClick={event => {
            filterMenuRef.current.show(event);
          }}
        />
        <DropdownFilter
          disabled={isLoading}
          filters={filter.validationDropdown}
          popup={true}
          ref={filterMenuRef}
          id="filterValidationDropdown"
          showFilters={showValidationFilter}
          onShow={e => {
            getExportButtonPosition(e);
          }}
        />
        {/* <Button
          className={`p-button-rounded p-button-secondary`}
          disabled={true}
          icon={'groupBy'}
          label={resources.messages['groupBy']}
        />

        <Button
          className={`p-button-rounded p-button-secondary`}
          disabled={true}
          icon={'sort'}
          label={resources.messages['sort']}
        />

        <Button
          className={`p-button-rounded p-button-secondary`}
          disabled={true}
          icon="filter"
          label={resources.messages['filters']}
          onClick={() => {}
          /> */}
      </div>
      <div className="p-toolbar-group-right">
        {/* <Button
          className={`p-button-rounded p-button-secondary`}
          disabled={true}
          icon={'refresh'}
          label={resources.messages['refresh']}
          onClick={() => onRefresh()}
        /> */}
      </div>
    </Toolbar>
  );
};

export { ActionsToolbar };
