/* eslint-disable react-hooks/exhaustive-deps */
import { Fragment, memo, useContext, useEffect, useReducer, useState } from 'react';

import PropTypes from 'prop-types';

import concat from 'lodash/concat';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import uniq from 'lodash/uniq';

import styles from './ValidationViewer.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { Filters } from 'ui/views/_components/Filters';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { validationReducer } from './_functions/Reducers/validationReducer';

const ValidationViewer = memo(
  ({
    buttonsList = undefined,
    datasetId,
    isWebformView,
    levelErrorTypes,
    onSelectValidation,
    schemaTables,
    tables,
    visible
  }) => {
    const resources = useContext(ResourcesContext);

    const [columns, setColumns] = useState([]);
    const [fetchedData, setFetchedData] = useState([]);
    const [fieldsTypesFilter, setFieldsTypesFilter] = useState([]);
    const [filterBy, setFilterBy] = useState({
      entityType: [],
      tableSchemaName: [],
      fieldSchemaName: [],
      levelError: []
    });
    const [filtered, setFiltered] = useState(false);
    const [filteredData, setFilteredData] = useState([]);
    const [firstRow, setFirstRow] = useState(0);
    const [grouped, setGrouped] = useState(true);
    const [isFilteredLevelErrors, setIsFilteredLevelErrors] = useState(false);
    const [isFilteredOrigins, setIsFilteredOrigins] = useState(false);
    const [isFilteredTypeEntities, setIsFilteredTypeEntities] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [fieldValueFilter, setFieldValueFilter] = useState([]);
    const [levelErrorsFilter, setLevelErrorsFilter] = useState([]);
    const [levelErrorsTypesFilter, setLevelErrorsTypesFilter] = useState([]);
    const [numberRows, setNumberRows] = useState(10);
    const [tablesFilter, setTablesFilter] = useState([]);
    const [originsTypesFilter, setOriginsTypesFilter] = useState([]);
    const [sortField, setSortField] = useState('');
    const [sortOrder, setSortOrder] = useState(0);
    const [typeEntitiesFilter, setTypeEntitiesFilter] = useState([]);
    const [typeEntitiesTypesFilter, setTypeEntitiesTypesFilter] = useState([]);
    const [validationsAllTypesFilters, setValidationsAllTypesFilters] = useState([]);

    const [validationState, validationDispatch] = useReducer(validationReducer, {
      totalErrors: 0,
      totalFilteredGroupedRecords: 0,
      totalFilteredRecords: 0,
      totalRecords: 0
    });

    const { totalErrors, totalFilteredRecords, totalRecords } = validationState;

    useEffect(() => {
      const allTypesFilter = concat(
        levelErrorsTypesFilter,
        originsTypesFilter,
        typeEntitiesTypesFilter,
        fieldsTypesFilter
      );
      setValidationsAllTypesFilters(allTypesFilter);
    }, [levelErrorsTypesFilter, originsTypesFilter, typeEntitiesTypesFilter, fieldsTypesFilter]);

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
        fetchData(
          '',
          sortOrder,
          firstRow,
          numberRows,
          fieldValueFilter,
          levelErrorsFilter,
          typeEntitiesFilter,
          tablesFilter
        );
      } else {
        if (isFilteredLevelErrors || isFilteredTypeEntities || isFilteredOrigins || firstRow != 0) {
          resetFilters();
          setFirstRow(0);
          fetchData('', sortOrder, 0, numberRows, [], [], [], []);
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
      const isChangedPage = true;
      setNumberRows(event.rows);
      setFirstRow(event.first);
      fetchData(
        sortField,
        sortOrder,
        event.first,
        event.rows,
        fieldValueFilter,
        levelErrorsFilter,
        typeEntitiesFilter,
        tablesFilter,
        isChangedPage
      );
    };

    const onLoadErrors = async (
      firstRow,
      numberRows,
      sortField,
      sortOrder,
      fieldValueFilter,
      levelErrorsFilter,
      typeEntitiesFilter,
      tablesFilter,
      isChangedPage
    ) => {
      setIsLoading(true);

      let datasetErrors = {};

      let pageNums = isChangedPage ? Math.floor(firstRow / numberRows) : 0;

      if (grouped) {
        const { data } = await DatasetService.groupedErrorsById(
          datasetId,
          pageNums,
          numberRows,
          sortField,
          sortOrder,
          fieldValueFilter,
          levelErrorsFilter,
          typeEntitiesFilter,
          tablesFilter
        );
        datasetErrors = data;
        addTableSchemaId(datasetErrors.errors);
        validationDispatch({
          type: 'SET_TOTAL_GROUPED_ERRORS',
          payload: {
            totalErrors: datasetErrors.totalErrors,
            totalFilteredGroupedRecords: datasetErrors.totalFilteredErrors
          }
        });
      } else {
        const { data } = await DatasetService.errorsById(
          datasetId,
          pageNums,
          numberRows,
          sortField,
          sortOrder,
          fieldValueFilter,
          levelErrorsFilter,
          typeEntitiesFilter,
          tablesFilter
        );
        datasetErrors = data;
      }

      validationDispatch({
        type: 'SET_TOTALS_ERRORS',
        payload: { totalFilteredRecords: datasetErrors.totalFilteredErrors, totalRecords: datasetErrors.totalRecords }
      });
      setFetchedData(datasetErrors.errors);
      setIsLoading(false);
    };

    const onLoadFilters = async () => {
      onLoadLevelErrorsTypes();
      onLoadTablesTypes();
      onLoadFieldsTypes();
      onLoadEntitiesTypes();
    };

    const onLoadFilteredData = fetchedData => {
      setFilteredData(fetchedData);
    };

    const onLoadLevelErrorsTypes = () => {
      const allLevelErrorsFilterList = [];

      levelErrorTypes.forEach(levelError => {
        if (!isNil(levelError)) {
          allLevelErrorsFilterList.push({ type: 'levelError', value: `${levelError}` });
        }
      });

      setLevelErrorsTypesFilter(allLevelErrorsFilterList);
    };

    const onLoadEntitiesTypes = () => {
      const allTypeEntitiesFilterList = [
        { type: 'entityType', value: 'DATASET' },
        { type: 'entityType', value: 'FIELD' },
        { type: 'entityType', value: 'RECORD' },
        { type: 'entityType', value: 'TABLE' }
      ];
      setTypeEntitiesTypesFilter(allTypeEntitiesFilterList);
    };

    const onLoadTablesTypes = () => {
      const allTablesFilterList = [];

      schemaTables.forEach(table => {
        if (!isNil(table.name)) {
          allTablesFilterList.push({ type: 'tableSchemaName', value: `${table.name.toString()}` });
        }
      });

      setOriginsTypesFilter(allTablesFilterList);
    };

    const onLoadFieldsTypes = () => {
      const tablesWithRecords = !isNil(tables) && tables.filter(table => !isUndefined(table.records));

      const fields = [];
      tablesWithRecords.forEach(table => {
        table.records.forEach(record => {
          record.fields.forEach(field => fields.push(field.name));
        });
      });

      const uniquesFields = uniq(fields);

      const allFieldsFilterList = [];

      uniquesFields.forEach(field => {
        if (!isNil(field)) {
          allFieldsFilterList.push({ type: 'fieldSchemaName', value: `${field.toString()}` });
        }
      });

      setFieldsTypesFilter(allFieldsFilterList);
    };

    const onLoadFilteredValidations = filterData => {
      setFirstRow(0);
      setFieldValueFilter(filterData.fieldSchemaName);
      setLevelErrorsFilter(filterData.levelError);
      setTypeEntitiesFilter(filterData.entityType);
      setTablesFilter(filterData.tableSchemaName);

      onLoadErrors(
        0,
        numberRows,
        sortField,
        sortOrder,
        filterData.fieldSchemaName,
        filterData.levelError,
        filterData.entityType,
        filterData.tableSchemaName
      );
      setFilterBy(filterData);
      if (!isNil(filterData)) {
        const filterDataValues = Object.values(filterData).map(value => value.length !== 0);
        filterDataValues.includes(true) ? setFiltered(true) : setFiltered(false);
      }
    };

    const onLoadErrorPosition = async (objectId, datasetId, entityType) => {
      setIsLoading(true);
      try {
        const errorPosition = await DatasetService.errorPositionByObjectId(objectId, datasetId, entityType);
        return errorPosition.data;
      } catch (error) {
        console.error('error', error);
      } finally {
        setIsLoading(false);
      }
    };

    const onSort = event => {
      setSortOrder(event.sortOrder);
      setSortField(event.sortField);
      fetchData(
        event.sortField,
        event.sortOrder,
        firstRow,
        numberRows,
        fieldValueFilter,
        levelErrorsFilter,
        typeEntitiesFilter,
        tablesFilter
      );
    };

    const fetchData = (
      sortField,
      sortOrder,
      firstRow,
      numberRows,
      fieldValueFilter,
      levelErrorsFilter,
      typeEntitiesFilter,
      tablesFilter,
      isChangedPage
    ) => {
      onLoadErrors(
        firstRow,
        numberRows,
        sortField,
        sortOrder,
        fieldValueFilter,
        levelErrorsFilter,
        typeEntitiesFilter,
        tablesFilter,
        isChangedPage
      );
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
        {filtered && totalRecords !== totalFilteredRecords
          ? `${resources.messages['filtered']}: ${totalFilteredRecords} | `
          : ''}
        {resources.messages['totalRecords']} {totalRecords}{' '}
        {`${resources.messages['records'].toLowerCase()}${
          grouped ? ` (${resources.messages['totalErrors'].toLowerCase()}${totalErrors})` : ''
        }`}
        {filtered && totalRecords === totalFilteredRecords ? ` (${resources.messages['filtered'].toLowerCase()})` : ''}
      </Fragment>
    );

    const resetFilters = () => {
      setTablesFilter([]);
      setTypeEntitiesFilter([]);
      setLevelErrorsFilter([]);
      setIsFilteredOrigins(false);
      setIsFilteredTypeEntities(false);
      setIsFilteredLevelErrors(false);
      onLoadFilters();
    };

    const filterOptions = [
      {
        type: 'multiselect',
        properties: [
          { name: 'entityType' },
          { name: 'tableSchemaName', showInput: true },
          { name: 'fieldSchemaName', showInput: true },
          { name: 'levelError' }
        ]
      }
    ];

    const refreshData = () => {
      onLoadErrors(
        firstRow,
        numberRows,
        sortField,
        sortOrder,
        fieldValueFilter,
        levelErrorsFilter,
        typeEntitiesFilter,
        tablesFilter
      );
    };

    if (isLoading) {
      return (
        <div className={styles.spinner}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      );
    }

    return (
      <div className={styles.validationWrapper}>
        {!isUndefined(buttonsList) ? (
          buttonsList
        ) : (
          <Toolbar className={styles.validationToolbar}>
            <Filters
              data={fetchedData}
              filterByList={filterBy}
              getFilteredData={onLoadFilteredData}
              options={filterOptions}
              sendData={onLoadFilteredValidations}
              validations
              validationsAllTypesFilters={validationsAllTypesFilters}
            />

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
              </div>
            </div>
          </Toolbar>
        )}
        <>
          {!isEmpty(fetchedData) ? (
            <DataTable
              autoLayout={true}
              className={isWebformView ? styles.tableWebform : undefined}
              first={firstRow}
              lazy={true}
              loading={isLoading}
              onPage={onChangePage}
              onRowSelect={isWebformView ? () => {} : onRowSelect}
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
          ) : (
            <div className={styles.emptyFilteredData}>{resources.messages['noValidationsWithSelectedParameters']}</div>
          )}
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
