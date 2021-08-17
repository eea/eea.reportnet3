/* eslint-disable react-hooks/exhaustive-deps */
import { Fragment, memo, useContext, useEffect, useReducer, useState } from 'react';
import ReactDOMServer from 'react-dom/server';

import concat from 'lodash/concat';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import uniq from 'lodash/uniq';

import styles from './ValidationViewer.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Filters } from 'views/_components/Filters';
import { LevelError } from 'views/_components/LevelError';
import { Spinner } from 'views/_components/Spinner';
import { Toolbar } from 'views/_components/Toolbar';
import { TooltipButton } from 'views/_components/TooltipButton';

import { DatasetService } from 'services/DatasetService';
import { ValidationService } from 'services/ValidationService';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

import { validationReducer } from './_functions/Reducers/validationReducer';

import { TextUtils } from 'repositories/_utils/TextUtils';

const ValidationViewer = memo(
  ({
    datasetId,
    datasetSchemaId,
    isWebformView,
    levelErrorTypes,
    onSelectValidation,
    reporting = false,
    schemaTables,
    switchToTabularData = () => {},
    tables,
    visible
  }) => {
    const resources = useContext(ResourcesContext);
    const validationContext = useContext(ValidationContext);

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
    const [firstRow, setFirstRow] = useState(0);
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
          header: resources.messages['ruleCode'],
          template: ruleCodeTemplate
        },
        {
          id: 'levelError',
          header: resources.messages['levelError'],
          template: levelErrorTemplate
        },
        {
          id: 'message',
          header: resources.messages['errorMessage']
        }
      ];

      let columnsArr = headers.map(col => {
        return !isNil(col.template) ? (
          <Column
            body={col.template}
            field={col.id}
            header={col.header}
            key={col.id}
            sortable={true}
            style={columnStyles(col.id)}
          />
        ) : (
          <Column field={col.id} header={col.header} key={col.id} sortable={true} style={columnStyles(col.id)} />
        );
      });

      columnsArr.push(
        <Column
          className={styles.invisibleHeader}
          field="recordId"
          header={resources.messages['recordId']}
          key="recordId"
        />
      );
      columnsArr.push(
        <Column
          className={styles.invisibleHeader}
          field="datasetPartitionId"
          header={resources.messages['datasetPartitionId']}
          key="datasetPartitionId"
        />
      );
      columnsArr.push(
        <Column
          className={styles.invisibleHeader}
          field="tableSchemaId"
          header={resources.messages['tableSchemaId']}
          key="tableSchemaId"
        />
      );
      columnsArr.push(
        <Column className={styles.invisibleHeader} field="ruleId" header={resources.messages['ruleId']} key="ruleId" />
      );

      columnsArr.push(
        <Column
          field="numberOfRecords"
          header={resources.messages['numberOfRecords']}
          key="numberOfRecords"
          sortable={true}
        />
      );

      setColumns(columnsArr);
    }, [validationContext.rulesDescription]);

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
        if (isFilteredLevelErrors || isFilteredTypeEntities || isFilteredOrigins || firstRow.toString() !== '0') {
          resetFilters();
          setFirstRow(0);
          fetchData('', sortOrder, 0, numberRows, [], [], [], []);
        }
      }
    }, [visible]);

    useEffect(() => {
      onLoadRulesDescription();
    }, []);

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

    const getRuleSchema = data =>
      validationContext.rulesDescription.find(ruleDescription => ruleDescription.id === data.ruleId);

    const getTooltipMessage = column => {
      const ruleInfo = getRuleSchema(column);
      return (
        <Fragment>
          <span className={styles.tooltipInfoLabel}>{resources.messages['ruleName']}: </span>{' '}
          <span className={styles.tooltipValueLabel}>{ruleInfo?.name}</span>
          <br />
          <span className={styles.tooltipInfoLabel}>{resources.messages['description']}: </span>
          <span className={styles.tooltipValueLabel}>
            {!isNil(ruleInfo?.description) && ruleInfo?.description !== ''
              ? ruleInfo?.description
              : resources.messages['noDescription']}
          </span>
        </Fragment>
      );
    };

    const levelErrorTemplate = recordData => (
      <div className={styles.levelErrorTemplateWrapper}>
        <LevelError type={recordData.levelError} />
      </div>
    );

    const ruleCodeTemplate = recordData => {
      return (
        <div className={styles.ruleCodeTemplateWrapper}>
          <span>{recordData.shortCode}</span>
          <TooltipButton
            getContent={() =>
              ReactDOMServer.renderToStaticMarkup(
                <div
                  style={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'flex-start'
                  }}>
                  {getTooltipMessage(recordData)}
                </div>
              )
            }
            maxWidth
            uniqueIdentifier={recordData.shortCode}
          />
        </div>
      );
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

      const data = await DatasetService.getShowValidationErrors(
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

    const onLoadRulesDescription = async () => {
      const validationsServiceList = await ValidationService.getAll(datasetSchemaId, reporting);
      validationContext.onSetRulesDescription(
        validationsServiceList?.validations?.map(validation => {
          return {
            automaticType: validation.automaticType,
            id: validation.id,
            description: validation.description,
            name: validation.name
          };
        })
      );
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
      switchToTabularData();
      switch (event.data.entityType) {
        case 'FIELD':
        case 'RECORD':
          onSelectValidation(event.data.tableSchemaId, event.data.ruleId, event.data.message, event.data.levelError);
          break;
        case 'TABLE':
          const ruleSchema = getRuleSchema(event.data);
          if (TextUtils.areEquals(ruleSchema.automaticType, 'TABLE_UNIQUENESS')) {
            onSelectValidation(event.data.tableSchemaId, event.data.ruleId, event.data.message, event.data.levelError);
          } else {
            onSelectValidation(event.data.tableSchemaId);
          }
          break;

        default:
          break;
      }
    };

    const getPaginatorRecordsCount = () => (
      <Fragment>
        {filtered && totalRecords !== totalFilteredRecords
          ? `${resources.messages['filtered']}: ${totalFilteredRecords} | `
          : ''}
        {resources.messages['totalRecords']} {totalRecords}{' '}
        {`${resources.messages['records'].toLowerCase()}${` (${resources.messages[
          'totalErrors'
        ].toLowerCase()}${totalErrors})`}`}
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

    if (isLoading) {
      return (
        <div className={styles.spinner}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      );
    }

    if (isEmpty(fetchedData) && !filtered) {
      return (
        <div className={styles.emptyValidationsWrapper}>
          <h3>{resources.messages['emptyValidations']}</h3>
        </div>
      );
    }

    return (
      <div className={styles.validationWrapper}>
        <Toolbar className={styles.validationToolbar}>
          <Filters
            data={fetchedData}
            filterByList={filterBy}
            options={filterOptions}
            sendData={onLoadFilteredValidations}
            validations
            validationsAllTypesFilters={validationsAllTypesFilters}
          />
        </Toolbar>
        {!isEmpty(fetchedData) ? (
          <DataTable
            autoLayout={true}
            className={isWebformView ? styles.tableWebform : undefined}
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
            sortField={sortField}
            sortOrder={sortOrder}
            sortable={true}
            totalRecords={totalFilteredRecords}
            value={fetchedData}>
            {columns}
          </DataTable>
        ) : (
          <div className={styles.emptyFilteredData}>
            <h3>{resources.messages['noValidationsWithSelectedParameters']}</h3>
          </div>
        )}
      </div>
    );
  }
);

export { ValidationViewer };
