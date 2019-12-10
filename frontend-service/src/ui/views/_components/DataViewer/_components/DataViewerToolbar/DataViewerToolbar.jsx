import React, { useContext, useEffect, useRef, useState } from 'react';
import { isUndefined } from 'lodash';

import { config } from 'conf';

import styles from './DataViewerToolbar.module.css';

import { Button } from 'ui/views/_components/Button';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { DropdownFilter } from 'ui/views/Dataset/_components/DropdownFilter';
import { Menu } from 'primereact/menu';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DatasetService } from 'core/services/Dataset';

const DataViewerToolbar = ({
  datasetId,
  hasWritePermissions,
  isFilterValidationsActive,
  isWebFormMMR,
  isLoading,
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
  tableName,
  validationDropdownFilter,
  visibilityDropdownFilter
}) => {
  const [exportTableData, setExportTableData] = useState(undefined);
  const [exportTableDataName, setExportTableDataName] = useState('');
  const [isLoadingFile, setIsLoadingFile] = useState(false);
  const [visibilityColumnIcon, setVisibilityColumnIcon] = useState('eye');

  const resources = useContext(ResourcesContext);

  let exportMenuRef = useRef();
  let filterMenuRef = useRef();
  let dropdownFilterRef = useRef();

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

  const isFiltered = (originalFilter, filter) => {
    if (filter.length < originalFilter.length) {
      return true;
    } else {
      return false;
    }
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

    if (isFiltered(originalColumns, currentinvisibleColumns)) {
      setVisibilityColumnIcon('eye-slash');
    } else {
      setVisibilityColumnIcon('eye');
    }
  };

  return (
    <Toolbar className={styles.dataViewerToolbar}>
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
          icon={visibilityColumnIcon}
          label={resources.messages['showHideColumns']}
          onClick={event => {
            dropdownFilterRef.current.show(event);
          }}
        />
        <DropdownFilter
          filters={visibilityDropdownFilter}
          popup={true}
          ref={dropdownFilterRef}
          id="exportTableMenu"
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
          filters={validationDropdownFilter}
          popup={true}
          ref={filterMenuRef}
          id="exportTableMenu"
          showFilters={showValidationFilter}
          onShow={e => {
            getExportButtonPosition(e);
          }}
        />

        <Button
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
          onClick={() => {}}
        />
      </div>
      <div className="p-toolbar-group-right">
        <Button
          className={`p-button-rounded p-button-secondary`}
          disabled={true}
          icon={'refresh'}
          label={resources.messages['refresh']}
          onClick={() => onRefresh()}
        />
      </div>
    </Toolbar>
  );
};

export { DataViewerToolbar };
