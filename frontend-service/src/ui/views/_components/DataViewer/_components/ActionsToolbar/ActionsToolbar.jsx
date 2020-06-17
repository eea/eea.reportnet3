import React, { useContext, useEffect, useRef, useState, useReducer } from 'react';

import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';
import uniq from 'lodash/uniq';

import { config } from 'conf';

import styles from './ActionsToolbar.module.css';

import { Button } from 'ui/views/_components/Button';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { DropdownFilter } from 'ui/views/Dataset/_components/DropdownFilter';
import { Menu } from 'primereact/menu';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

import { filterReducer } from './_functions/Reducers/filterReducer';

import { DatasetService } from 'core/services/Dataset';

import { MetadataUtils } from 'ui/views/_functions/Utils';

const ActionsToolbar = ({
  colsSchema,
  dataflowId,
  datasetId,
  exportExtensionsOperationsList,
  hasWritePermissions,
  isDataCollection = false,
  isFilterValidationsActive,
  isLoading,
  isTableDeleted,
  isValidationSelected,
  isWebFormMMR,
  levelErrorTypesWithCorrects,
  onRefresh,
  onSetVisible,
  originalColumns,
  onUpdateData,
  records,
  setColumns,
  setDeleteDialogVisible,
  setImportDialogVisible,
  showValidationFilter,
  tableHasErrors,
  tableId,
  tableName,
  tableReadOnly
}) => {
  const [exportTableData, setExportTableData] = useState(undefined);
  const [exportTableDataName, setExportTableDataName] = useState('');
  const [isLoadingFile, setIsLoadingFile] = useState(false);
  const [FMEExportExtensions, setFMEExportExtensions] = useState([]);

  const [filter, dispatchFilter] = useReducer(filterReducer, {
    validationDropdown: [],
    visibilityDropdown: [],
    visibilityColumnIcon: 'eye'
  });

  const resources = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

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
    if (isValidationSelected) {
      dispatchFilter({ type: 'SET_VALIDATION_FILTER', payload: { levelErrors: getLevelErrorFilters() } });
    }
  }, [isValidationSelected]);

  useEffect(() => {
    // const mustShowColumns = ['actions', 'recordValidation', 'id', 'datasetPartitionId', 'providerCode'];
    // const dropdownFilter = colsSchema
    //   .map(colSchema => {
    //     if (!mustShowColumns.includes(colSchema.field)) {
    //       return { label: colSchema.header, key: colSchema.field };
    //     }
    //   })
    //   .filter(colSchema => !isUndefined(colSchema));
    // dispatchFilter({ type: 'SET_VALIDATION_FILTER', payload: { levelErrors: getLevelErrorFilters() } });
  }, [levelErrorTypesWithCorrects]);

  useEffect(() => {
    if (!isUndefined(exportTableData)) {
      DownloadFile(exportTableData, exportTableDataName);
    }
  }, [exportTableData]);

  useEffect(() => {
    getReportNetandFMEExportExtensions(exportExtensionsOperationsList);
  }, [exportExtensionsOperationsList]);

  const parseUniqsExportExtensions = exportExtensionsOperationsList => {
    return exportExtensionsOperationsList.map(uniqExportExtension => ({
      text: `${uniqExportExtension.toUpperCase()} (.${uniqExportExtension.toLowerCase()})`,
      code: uniqExportExtension.toLowerCase()
    }));
  };

  const getReportNetandFMEExportExtensions = exportExtensionsOperationsList => {
    const uniqsExportExtensions = uniq(exportExtensionsOperationsList.map(element => element.fileExtension));
    setFMEExportExtensions(parseUniqsExportExtensions(uniqsExportExtensions));
  };

  const reportNetExtensionsItems = config.exportTypes.map(type => ({
    label: type.text,
    icon: config.icons['archive'],
    command: () => onExportTableData(type.code)
  }));

  const FMEExtensionsItems = [
    {
      label: 'FME Extensions',
      items: FMEExportExtensions.map(type => ({
        label: type.text,
        icon: config.icons['archive'],
        command: () => onExportTableData(type.code)
      }))
    }
  ];

  const totalExtensionsItems = isEmpty(FMEExportExtensions)
    ? reportNetExtensionsItems
    : reportNetExtensionsItems.concat(FMEExtensionsItems);

  const onExportTableData = async fileType => {
    setIsLoadingFile(true);
    try {
      setExportTableDataName(createTableName(tableName, fileType));
      setExportTableData(await DatasetService.exportTableDataById(datasetId, tableId, fileType));
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
    <Toolbar className={styles.actionsToolbar}>
      <div className="p-toolbar-group-left">
        <Button
          className={`p-button-rounded p-button-secondary ${
            !hasWritePermissions || tableReadOnly || isWebFormMMR ? null : 'p-button-animated-blink'
          }`}
          disabled={!hasWritePermissions || tableReadOnly || isWebFormMMR}
          icon={'export'}
          label={resources.messages['import']}
          onClick={() => setImportDialogVisible(true)}
        />
        <Button
          id="buttonExportTable"
          className={`p-button-rounded p-button-secondary-transparent ${
            isDataCollection ? null : 'p-button-animated-blink'
          }`}
          // disabled={!hasWritePermissions}
          disabled={isDataCollection}
          icon={isLoadingFile ? 'spinnerAnimate' : 'import'}
          label={resources.messages['exportTable']}
          onClick={event => {
            onUpdateData();
            exportMenuRef.current.show(event);
          }}
        />
        <Menu
          className={styles.menu}
          id="exportTableMenu"
          model={totalExtensionsItems}
          onShow={e => getExportButtonPosition(e)}
          popup={true}
          ref={exportMenuRef}
        />

        <Button
          className={`p-button-rounded p-button-secondary-transparent ${
            !hasWritePermissions || tableReadOnly || isWebFormMMR || isUndefined(records.totalRecords) || isTableDeleted
              ? null
              : 'p-button-animated-blink'
          }`}
          disabled={
            !hasWritePermissions || tableReadOnly || isWebFormMMR || isUndefined(records.totalRecords) || isTableDeleted
          }
          icon={'trash'}
          label={resources.messages['deleteTable']}
          onClick={() => onSetVisible(setDeleteDialogVisible, true)}
        />

        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink`}
          disabled={false}
          icon={filter.visibilityColumnIcon}
          label={resources.messages['showHideColumns']}
          onClick={event => {
            dropdownFilterRef.current.show(event);
          }}
        />
        <DropdownFilter
          className={`p-button-animated-blink`}
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
          className={`p-button-rounded p-button-secondary-transparent ${
            tableHasErrors ? 'p-button-animated-blink' : null
          }`}
          disabled={!tableHasErrors}
          icon="filter"
          iconClasses={!isFilterValidationsActive ? styles.filterInactive : ''}
          label={resources.messages['validationFilter']}
          onClick={event => {
            filterMenuRef.current.show(event);
          }}
        />
        <DropdownFilter
          className={!isLoading ? 'p-button-animated-blink' : null}
          disabled={isLoading}
          filters={filter.validationDropdown}
          popup={true}
          ref={filterMenuRef}
          id="filterValidationDropdown"
          showFilters={showValidationFilter}
          onShow={e => {
            getExportButtonPosition(e);
          }}
          showLevelErrorIcons={true}
        />
        {/* <Button
          className={`p-button-rounded p-button-secondary-transparent`}
          disabled={true}
          icon={'groupBy'}
          label={resources.messages['groupBy']}
        />

        <Button
          className={`p-button-rounded p-button-secondary-transparent`}
          disabled={true}
          icon={'sort'}
          label={resources.messages['sort']}
        />

        <Button
          className={`p-button-rounded p-button-secondary-transparent`}
          disabled={true}
          icon="filter"
          label={resources.messages['filters']}
          onClick={() => {}
          /> */}
      </div>
      {/* <div className="p-toolbar-group-right">
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${
            isLoading ? 'p-button-animated-spin' : ''
          }`}
          icon={'refresh'}
          label={resources.messages['refresh']}
          onClick={() => onRefresh()}
        />
      </div> */}
    </Toolbar>
  );
};

export { ActionsToolbar };
