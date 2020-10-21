/* eslint-disable react-hooks/exhaustive-deps */
import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import PropTypes from 'prop-types';

import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './ValidationViewer.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { DropdownFilter } from 'ui/views/Dataset/_components/DropdownFilter';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { validationReducer } from './_functions/Reducers/validationReducer';

const ValidationViewer = React.memo(
  ({
    buttonsList = undefined,
    datasetId,
    datasetName,
    hasWritePermissions,
    levelErrorTypes,
    onSelectValidation,
    schemaTables,
    visible
  }) => {
    const resources = useContext(ResourcesContext);
    const [allLevelErrorsFilter, setAllLevelErrorsFilter] = useState([]);
    const [allOriginsFilter, setAllOriginsFilter] = useState([]);
    const [allTypeEntitiesFilter, setAllTypeEntitiesFilter] = useState([]);
    const [areActiveFilters, setAreActiveFilters] = useState(false);
    const [columns, setColumns] = useState([]);
    const [fetchedData, setFetchedData] = useState([]);
    const [firstRow, setFirstRow] = useState(0);
    const [grouped, setGrouped] = useState(true);
    const [isFilteredLevelErrors, setIsFilteredLevelErrors] = useState(false);
    const [isFilteredOrigins, setIsFilteredOrigins] = useState(false);
    const [isFilteredTypeEntities, setIsFilteredTypeEntities] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [levelErrorsFilter, setLevelErrorsFilter] = useState([]);
    const [numberRows, setNumberRows] = useState(10);
    const [originsFilter, setOriginsFilter] = useState([]);
    const [sortField, setSortField] = useState('');
    const [sortOrder, setSortOrder] = useState(0);
    const [typeEntitiesFilter, setTypeEntitiesFilter] = useState([]);

    const [validationState, validationDispatch] = useReducer(validationReducer, {
      totalErrors: 0,
      totalFilteredGroupedRecords: 0,
      totalFilteredRecords: 0,
      totalRecords: 0
    });

    const { totalErrors, totalFilteredGroupedRecords, totalFilteredRecords, totalRecords } = validationState;

    let dropdownLevelErrorsFilterRef = useRef();
    let dropdownTypeEntitiesFilterRef = useRef();
    let dropdownOriginsFilterRef = useRef();

    useEffect(() => {
      const headers = [
        {
          id: 'entityType',
          header: resources.messages['entity']
        },
        {
          id: 'tableSchemaName',
          header: resources.messages['table']
        },
        {
          id: 'fieldSchemaName',
          header: resources.messages['field']
        },
        {
          id: 'shortCode',
          header: resources.messages['ruleCode']
        },
        {
          id: 'levelError',
          header: resources.messages['levelError']
        },
        {
          id: 'message',
          header: resources.messages['errorMessage']
        }
      ];

      let columnsArr = headers.map(col => (
        <Column sortable={true} key={col.id} field={col.id} header={col.header} style={columnStyles(col.id)} />
      ));

      columnsArr.push(
        <Column
          key="recordId"
          field="recordId"
          header={resources.messages['recordId']}
          className={styles.invisibleHeader}
        />
      );
      columnsArr.push(
        <Column
          key="datasetPartitionId"
          field="datasetPartitionId"
          header={resources.messages['datasetPartitionId']}
          className={styles.invisibleHeader}
        />
      );
      columnsArr.push(
        <Column
          key="tableSchemaId"
          field="tableSchemaId"
          className={styles.invisibleHeader}
          header={resources.messages['tableSchemaId']}
        />
      );
      columnsArr.push(
        <Column key="ruleId" field="ruleId" className={styles.invisibleHeader} header={resources.messages['ruleId']} />
      );
      if (grouped) {
        columnsArr.push(
          <Column
            key="numberOfRecords"
            field="numberOfRecords"
            header={resources.messages['numberOfRecords']}
            sortable={true}
          />
        );
      }

      setColumns(columnsArr);
    }, [grouped]);

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
    }, [visible, grouped]);

    const addTableSchemaId = tableErrors => {
      tableErrors.forEach(tableError => {
        const filteredTable = schemaTables.filter(schemaTable => schemaTable.name === tableError.tableSchemaName);
        if (!isEmpty(filteredTable)) tableError.tableSchemaId = filteredTable[0].id;
      });
      return tableErrors;
    };

    const columnStyles = columnId => {
      const style = {};
      if (columnId === 'levelError') {
        style.minWidth = '6rem';
      }
      return style;
    };

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

      let datasetErrors = {};

      if (grouped) {
        datasetErrors = await DatasetService.groupedErrorsById(
          datasetId,
          Math.floor(firstRow / numberRows),
          numberRows,
          sortField,
          sortOrder,
          levelErrorsFilter,
          typeEntitiesFilter,
          originsFilter
        );
        addTableSchemaId(datasetErrors.errors);
        validationDispatch({
          type: 'SET_TOTAL_GROUPED_ERRORS',
          payload: {
            totalErrors: datasetErrors.totalErrors,
            totalFilteredGroupedRecords: datasetErrors.totalFilteredErrors
          }
        });
      } else {
        datasetErrors = await DatasetService.errorsById(
          datasetId,
          Math.floor(firstRow / numberRows),
          numberRows,
          sortField,
          sortOrder,
          levelErrorsFilter,
          typeEntitiesFilter,
          originsFilter
        );
      }

      validationDispatch({
        type: 'SET_TOTALS_ERRORS',
        payload: {
          totalFilteredRecords: datasetErrors.totalFilteredErrors,
          totalRecords: datasetErrors.totalRecords
        }
      });
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
          key: `${filter.toString()}`
        });
      });

      setAllLevelErrorsFilter(allLevelErrorsFilterList);
    };

    const onLoadTypeEntitiesFilter = () => {
      const allTypeEntitiesFilterList = [
        { label: resources.messages['dataset'], key: 'Dataset' },
        { label: resources.messages['validationViewerTable'], key: 'Table' },
        { label: resources.messages['record'], key: 'Record' },
        { label: resources.messages['validationViewerField'], key: 'Field' }
      ];
      setAllTypeEntitiesFilter(allTypeEntitiesFilterList);
    };

    const onLoadOriginsFilter = () => {
      const allOriginsFilterList = [];

      allOriginsFilterList.push({
        label: datasetName.toString(),
        key: `${datasetName.toString()}`
      });
      schemaTables.forEach(table => {
        if (!isNil(table.name)) {
          allOriginsFilterList.push({ label: table.name.toString(), key: `${table.name.toString()}` });
        }
      });

      setAllOriginsFilter(allOriginsFilterList);
    };

    const removeSelectAllFromList = filters => {
      if (!isEmpty(filters)) {
        filters.shift();
      }
    };

    const onLoadErrorsWithErrorLevelFilter = levelErrorsDeselected => {
      levelErrorsDeselected = levelErrorsDeselected.map(filter => filter.toString().toUpperCase());

      removeSelectAllFromList(levelErrorsDeselected);

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
      typeEntitiesDeselected = typeEntitiesDeselected.map(filter => filter.toString().toUpperCase());

      removeSelectAllFromList(typeEntitiesDeselected);

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
      if (!grouped) {
        switch (event.data.entityType) {
          case 'FIELD':
          case 'RECORD':
            const datasetError = await onLoadErrorPosition(event.data.objectId, datasetId, event.data.entityType);
            onSelectValidation(event.data.tableSchemaId, datasetError.position, datasetError.recordId, '', false);
            break;

          case 'TABLE':
            onSelectValidation(event.data.tableSchemaId, -1, -1, '', false);
            break;

          default:
            break;
        }
      } else {
        switch (event.data.entityType) {
          case 'FIELD':
          case 'RECORD':
            onSelectValidation(
              event.data.tableSchemaId,
              -1,
              -1,
              event.data.ruleId,
              true,
              event.data.message,
              event.data.levelError
            );
            break;
          case 'TABLE':
            if (event.data.shortCode.substring(0, 2) === 'TU' && event.data.message.startsWith('Uniqueness')) {
              onSelectValidation(
                event.data.tableSchemaId,
                -1,
                -1,
                event.data.ruleId,
                true,
                event.data.message,
                event.data.levelError
              );
            } else {
              onSelectValidation(event.data.tableSchemaId, -1, -1, '', false);
            }
            break;

          default:
            break;
        }
      }
    };
    const getPaginatorRecordsCount = () => (
      <Fragment>
        {areActiveFilters && totalRecords !== totalFilteredRecords
          ? `${resources.messages['filtered']}: ${!grouped ? totalFilteredRecords : totalFilteredGroupedRecords} | `
          : ''}
        {resources.messages['totalRecords']} {totalRecords}{' '}
        {`${resources.messages['records'].toLowerCase()}${
          grouped ? ` (${resources.messages['totalErrors'].toLowerCase()}${totalErrors})` : ''
        }`}
        {areActiveFilters && totalRecords === totalFilteredRecords
          ? ` (${resources.messages['filtered'].toLowerCase()})`
          : ''}
      </Fragment>
    );

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
      <div className={styles.validationWrapper}>
        {!isUndefined(buttonsList) ? (
          buttonsList
        ) : (
          <Toolbar className={styles.validationToolbar}>
            <div className="p-toolbar-group-left">
              <Button
                className={`${styles.origin} p-button-rounded p-button-secondary-transparent`}
                icon={'filter'}
                label={resources.messages['table']}
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
                className={`${styles.level} p-button-rounded p-button-secondary-transparent`}
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
                showLevelErrorIcons={true}
              />

              <Button
                className={`p-button-rounded p-button-secondary-transparent`}
                icon={'filter'}
                label={resources.messages['entity']}
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
                className={`p-button-rounded p-button-secondary-transparent`}
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
              <div className={styles.switchDivInput}>
                <div className={styles.switchDiv}>
                  <span className={styles.switchTextInput}>{resources.messages['ungrouped']}</span>
                  <InputSwitch
                    checked={grouped}
                    onChange={e => setGrouped(e.value)}
                    tooltip={resources.messages['toggleGroup']}
                    tooltipOptions={{ position: 'bottom' }}
                  />
                  <span className={styles.switchTextInput}>{resources.messages['grouped']}</span>
                </div>
              </div>
              <Button
                className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${
                  isLoading ? 'p-button-animated-spin' : ''
                }`}
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
            first={firstRow}
            lazy={true}
            loading={isLoading}
            onPage={onChangePage}
            onRowSelect={onRowSelect}
            onSort={onSort}
            paginator={true}
            paginatorRight={isLoading ? <Spinner className={styles.loading} /> : getPaginatorRecordsCount()}
            reorderableColumns={true}
            resizableColumns={true}
            rows={numberRows}
            rowsPerPageOptions={[5, 10, 15]}
            selectionMode="single"
            sortable={true}
            sortField={sortField}
            sortOrder={sortOrder}
            totalRecords={totalFilteredRecords}
            value={fetchedData}>
            {columns}
          </DataTable>
        </>
      </div>
    );
  }
);

ValidationViewer.propTypes = {
  id: PropTypes.string,
  buttonsList: PropTypes.array
};

export { ValidationViewer };
