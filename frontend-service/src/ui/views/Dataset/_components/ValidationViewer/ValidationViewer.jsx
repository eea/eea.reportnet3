/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef } from 'react';
import PropTypes from 'prop-types';

import { capitalize, isNull, isUndefined } from 'lodash';

import styles from './ValidationViewer.module.css';

import { DataTable } from 'ui/views/_components/DataTable';
import { Column } from 'primereact/column';

import { Button } from 'ui/views/_components/Button';
import { DropdownFilter } from 'ui/views/Dataset/_components/DropdownFilter';
import { DatasetContext } from 'ui/views/_functions/Contexts/DatasetContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DatasetService } from 'core/services/Dataset';

const ValidationViewer = React.memo(
  ({
    visible,
    datasetId,
    datasetName,
    buttonsList = undefined,
    levelErrorTypes,
    hasWritePermissions,
    tableSchemaNames
  }) => {
    const datasetContext = useContext(DatasetContext);
    const resources = useContext(ResourcesContext);
    const [allLevelErrorsFilter, setAllLevelErrorsFilter] = useState([]);
    const [allTypeEntitiesFilter, setAllTypeEntitiesFilter] = useState([]);
    const [allOriginsFilter, setAllOriginsFilter] = useState([]);
    const [areActiveFilters, setAreActiveFilters] = useState(false);
    const [columns, setColumns] = useState([]);
    const [fetchedData, setFetchedData] = useState([]);
    const [firstRow, setFirstRow] = useState(0);
    const [isFilteredLevelErrors, setIsFilteredLevelErrors] = useState(false);
    const [isFilteredOrigins, setIsFilteredOrigins] = useState(false);
    const [isFilteredTypeEntities, setIsFilteredTypeEntities] = useState(false);
    const [levelErrorsFilter, setLevelErrorsFilter] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [numberRows, setNumberRows] = useState(10);
    const [originsFilter, setOriginsFilter] = useState([]);
    const [sortField, setSortField] = useState('');
    const [sortOrder, setSortOrder] = useState(0);
    const [totalRecords, setTotalRecords] = useState(0);
    const [totalFilteredRecords, setTotalFilteredRecords] = useState();
    const [typeEntitiesFilter, setTypeEntitiesFilter] = useState([]);

    let dropdownLevelErrorsFilterRef = useRef();
    let dropdownTypeEntitiesFilterRef = useRef();
    let dropdownOriginsFilterRef = useRef();

    useEffect(() => {
      const headers = [
        {
          id: 'tableSchemaName',
          header: resources.messages['origin']
        },
        {
          id: 'levelError',
          header: resources.messages['levelError']
        },
        {
          id: 'message',
          header: resources.messages['errorMessage']
        },
        {
          id: 'entityType',
          header: resources.messages['entityType']
        }
      ];
      let columnsArr = headers.map(col => <Column sortable={true} key={col.id} field={col.id} header={col.header} />);
      columnsArr.push(<Column key="recordId" field="recordId" header="" className={styles.invisibleHeader} />);
      columnsArr.push(
        <Column key="datasetPartitionId" field="datasetPartitionId" header="" className={styles.invisibleHeader} />
      );
      columnsArr.push(
        <Column key="tableSchemaId" field="tableSchemaId" header="" className={styles.invisibleHeader} />
      );
      setColumns(columnsArr);
    }, []);

    useEffect(() => {
      if (visible) {
        onLoadFilters();
        fetchData('', sortOrder, firstRow, numberRows, levelErrorsFilter, typeEntitiesFilter, originsFilter);
      } else {
        if (isFilteredLevelErrors || isFilteredTypeEntities || isFilteredOrigins || firstRow != 0) {
          resetFilters();
          setFirstRow(0);
          fetchData('', sortOrder, 0, numberRows, [], [], []);
        }
      }
    }, [visible]);

    const onChangePage = event => {
      setNumberRows(event.rows);
      setFirstRow(event.first);
      fetchData(sortField, sortOrder, event.first, event.rows, levelErrorsFilter, typeEntitiesFilter, originsFilter);
    };

    const onLoadErrors = async (
      firstRow,
      numberRows,
      sortField,
      sortOrder,
      levelErrorsFilter,
      typeEntitiesFilter,
      originsFilter
    ) => {
      setIsLoading(true);
      const datasetErrors = await DatasetService.errorsById(
        datasetId,
        Math.floor(firstRow / numberRows),
        numberRows,
        sortField,
        sortOrder,
        levelErrorsFilter,
        typeEntitiesFilter,
        originsFilter
      );
      setTotalRecords(datasetErrors.totalErrors);
      setTotalFilteredRecords(datasetErrors.totalFilteredErrors);
      setFetchedData(datasetErrors.errors);
      setIsLoading(false);
    };

    const onLoadFilters = async () => {
      onLoadLevelErrorsFilter();
      onLoadTypeEntitiesFilter();
      onLoadOriginsFilter();
    };

    const onLoadLevelErrorsFilter = () => {
      const allLevelErrorsFilterList = [];
      levelErrorTypes.forEach(filter => {
        allLevelErrorsFilterList.push({
          label: capitalize(filter),
          key: `${filter.toString()}_Id`
        });
      });
      setAllLevelErrorsFilter(allLevelErrorsFilterList);
    };

    const onLoadTypeEntitiesFilter = () => {
      const allTypeEntitiesFilterList = [
        { label: 'Dataset', key: 'Dataset_Id' },
        { label: 'Table', key: 'Table_Id' },
        { label: 'Record', key: 'Record_Id' },
        { label: 'Field', key: 'Field_Id' }
      ];
      setAllTypeEntitiesFilter(allTypeEntitiesFilterList);
    };

    const onLoadOriginsFilter = () => {
      const allOriginsFilterList = [];
      allOriginsFilterList.push({
        label: datasetName.toString(),
        key: `${datasetName.toString()}_Id`
      });
      tableSchemaNames.forEach(name => {
        allOriginsFilterList.push({ label: name.toString(), key: `${name.toString()}_Id` });
      });
      setAllOriginsFilter(allOriginsFilterList);
    };

    const onLoadErrorsWithErrorLevelFilter = levelErrorsDeselected => {
      levelErrorsDeselected = levelErrorsDeselected.map(filter => {
        return filter.toString().toUpperCase();
      });

      setLevelErrorsFilter(levelErrorsDeselected);

      setIsFilteredLevelErrors(levelErrorsDeselected.length > 0);

      if (levelErrorsDeselected.length <= 0) {
        checkActiveFilters(isFilteredOrigins, false, isFilteredTypeEntities);
      } else {
        setAreActiveFilters(true);
      }

      onLoadErrors(0, numberRows, sortField, sortOrder, levelErrorsDeselected, typeEntitiesFilter, originsFilter);

      setFirstRow(0);
    };

    const onLoadErrorsWithEntityFilter = typeEntitiesDeselected => {
      typeEntitiesDeselected = typeEntitiesDeselected.map(filter => {
        return filter.toString().toUpperCase();
      });
      if (typeEntitiesDeselected.length <= 0) {
        checkActiveFilters(isFilteredOrigins, isFilteredLevelErrors, false);
      } else {
        setAreActiveFilters(true);
      }
      setTypeEntitiesFilter(typeEntitiesDeselected);
      setIsFilteredTypeEntities(typeEntitiesDeselected.length > 0);
      onLoadErrors(0, numberRows, sortField, sortOrder, levelErrorsFilter, typeEntitiesDeselected, originsFilter);
      setFirstRow(0);
    };

    const onLoadErrorsWithOriginsFilter = originsDeselected => {
      setOriginsFilter(originsDeselected);
      if (originsDeselected.length <= 0) {
        checkActiveFilters(false, isFilteredLevelErrors, isFilteredTypeEntities);
      } else {
        setAreActiveFilters(true);
      }
      setIsFilteredOrigins(originsDeselected.length > 0);
      onLoadErrors(0, numberRows, sortField, sortOrder, levelErrorsFilter, typeEntitiesFilter, originsDeselected);
      setFirstRow(0);
    };

    const onLoadErrorPosition = async (objectId, datasetId, entityType) => {
      setIsLoading(true);
      const errorPosition = await DatasetService.errorPositionByObjectId(objectId, datasetId, entityType);
      setIsLoading(false);
      return errorPosition;
    };

    const onSort = event => {
      setSortOrder(event.sortOrder);
      setSortField(event.sortField);
      fetchData(
        event.sortField,
        event.sortOrder,
        firstRow,
        numberRows,
        levelErrorsFilter,
        typeEntitiesFilter,
        originsFilter
      );
    };

    const fetchData = (
      sortField,
      sortOrder,
      firstRow,
      numberRows,
      levelErrorsFilter,
      typeEntitiesFilter,
      originsFilter
    ) => {
      onLoadErrors(firstRow, numberRows, sortField, sortOrder, levelErrorsFilter, typeEntitiesFilter, originsFilter);
    };

    const checkActiveFilters = (originsFilter, levelErrorsFilter, typeEntitiesFilter) => {
      if (originsFilter || levelErrorsFilter || typeEntitiesFilter) {
        setAreActiveFilters(true);
      } else {
        setAreActiveFilters(false);
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

    const onRowSelect = async event => {
      switch (event.data.entityType) {
        case 'FIELD':
        case 'RECORD':
          const datasetError = await onLoadErrorPosition(event.data.objectId, datasetId, event.data.entityType);
          datasetContext.setIsValidationSelected(true);
          datasetContext.onSelectValidation(event.data.tableSchemaId, datasetError.position, datasetError.recordId);
          datasetContext.onValidationsVisible();
          break;
        case 'TABLE':
          datasetContext.onSelectValidation(event.data.tableSchemaId, -1, -1);
          datasetContext.onValidationsVisible();
          break;
        default:
          break;
      }
    };

    const totalCountWithoutFilters = () => {
      return (
        <span>
          {resources.messages['totalRecords']} {!isUndefined(totalRecords) ? totalRecords : 0}{' '}
          {resources.messages['records'].toLowerCase()}
        </span>
      );
    };

    const filteredCount = () => {
      return (
        <span>
          {resources.messages['filtered']}
          {':'}{' '}
          {!isNull(totalFilteredRecords) && !isUndefined(totalFilteredRecords) ? totalFilteredRecords : totalRecords}
          {' | '}
          {resources.messages['totalRecords']} {!isUndefined(totalRecords) ? totalRecords : 0}{' '}
          {resources.messages['records'].toLowerCase()}
        </span>
      );
    };

    const filteredCountWithSameAsTotalValue = () => {
      return (
        <span>
          {resources.messages['totalRecords']} {!isUndefined(totalRecords) ? totalRecords : 0}{' '}
          {resources.messages['records'].toLowerCase()} {'('}
          {resources.messages['filtered'].toLowerCase()}
          {')'}
        </span>
      );
    };

    const getPaginatorRecordsCount = () => {
      if (isNull(totalFilteredRecords) || isUndefined(totalFilteredRecords) || totalFilteredRecords == totalRecords) {
        return areActiveFilters && totalFilteredRecords !== 0
          ? filteredCountWithSameAsTotalValue()
          : totalCountWithoutFilters();
      } else {
        return filteredCount();
      }
    };

    const resetFilters = () => {
      setOriginsFilter([]);
      setTypeEntitiesFilter([]);
      setLevelErrorsFilter([]);
      setIsFilteredOrigins(false);
      setIsFilteredTypeEntities(false);
      setIsFilteredLevelErrors(false);
      setAreActiveFilters(false);
      onLoadFilters();
    };

    const refreshData = () => {
      onLoadErrors(firstRow, numberRows, sortField, sortOrder, levelErrorsFilter, typeEntitiesFilter, originsFilter);
    };

    return (
      <>
        {!isUndefined(buttonsList) ? (
          buttonsList
        ) : (
          <Toolbar className={styles.validationToolbar}>
            <div className="p-toolbar-group-left">
              <Button
                className={`${styles.origin} p-button-rounded p-button-secondary`}
                icon={'filter'}
                label={resources.messages['origin']}
                onClick={event => {
                  dropdownOriginsFilterRef.current.show(event);
                }}
                iconClasses={isFilteredOrigins ? styles.filterActive : styles.filterInactive}
              />

              <DropdownFilter
                disabled={isLoading}
                filters={allOriginsFilter}
                popup={true}
                ref={dropdownOriginsFilterRef}
                id="exportTableMenu"
                showNotCheckedFilters={onLoadErrorsWithOriginsFilter}
                onShow={e => {
                  getExportButtonPosition(e);
                }}
              />

              <Button
                className={`${styles.level} p-button-rounded p-button-secondary`}
                icon={'filter'}
                label={resources.messages['levelError']}
                onClick={event => {
                  dropdownLevelErrorsFilterRef.current.show(event);
                }}
                iconClasses={isFilteredLevelErrors ? styles.filterActive : styles.filterInactive}
              />

              <DropdownFilter
                disabled={isLoading}
                filters={allLevelErrorsFilter}
                popup={true}
                ref={dropdownLevelErrorsFilterRef}
                id="exportTableMenu"
                showNotCheckedFilters={onLoadErrorsWithErrorLevelFilter}
                onShow={e => {
                  getExportButtonPosition(e);
                }}
              />

              <Button
                className={`p-button-rounded p-button-secondary`}
                // icon={'eye'}
                icon={'filter'}
                label={resources.messages['entityType']}
                onClick={event => {
                  dropdownTypeEntitiesFilterRef.current.show(event);
                }}
                iconClasses={isFilteredTypeEntities ? styles.filterActive : styles.filterInactive}
              />

              <DropdownFilter
                disabled={isLoading}
                filters={allTypeEntitiesFilter}
                popup={true}
                ref={dropdownTypeEntitiesFilterRef}
                id="exportTableMenu"
                showNotCheckedFilters={onLoadErrorsWithEntityFilter}
                onShow={e => {
                  getExportButtonPosition(e);
                }}
              />

              <Button
                className={`p-button-rounded p-button-secondary`}
                disabled={!areActiveFilters}
                icon={'cross'}
                label={resources.messages['cleanFilters']}
                onClick={() => {
                  resetFilters();
                  fetchData('', sortOrder, 0, numberRows, [], [], []);
                  setFirstRow(0);
                  setAreActiveFilters(false);
                }}
              />
            </div>
            <div className="p-toolbar-group-right">
              <Button
                className={`p-button-rounded p-button-secondary`}
                disabled={false}
                icon={'refresh'}
                label={resources.messages['refresh']}
                onClick={refreshData}
              />
            </div>
          </Toolbar>
        )}
        <>
          <DataTable
            autoLayout={true}
            className={styles.showValidationsData}
            first={firstRow}
            lazy={true}
            loading={isLoading}
            onRowSelect={onRowSelect}
            onPage={onChangePage}
            onSort={onSort}
            paginator={true}
            paginatorRight={getPaginatorRecordsCount()}
            resizableColumns={true}
            reorderableColumns={true}
            rows={numberRows}
            rowsPerPageOptions={[5, 10, 15]}
            sortable={true}
            sortField={sortField}
            sortOrder={sortOrder}
            totalRecords={totalFilteredRecords}
            selectionMode="single"
            value={fetchedData}>
            {columns}
          </DataTable>
        </>
      </>
    );
  }
);

ValidationViewer.propTypes = {
  id: PropTypes.string,
  buttonsList: PropTypes.array
};

export { ValidationViewer };
