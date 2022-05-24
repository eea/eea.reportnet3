import { Fragment, memo, useContext, useEffect, useReducer, useState } from 'react';
import ReactDOMServer from 'react-dom/server';

import concat from 'lodash/concat';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import sortBy from 'lodash/sortBy';
import uniq from 'lodash/uniq';

import styles from './ShowValidationsList.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Filters } from 'views/_components/Filters';
import { LevelError } from 'views/_components/LevelError';
import { Spinner } from 'views/_components/Spinner';
import { TooltipButton } from 'views/_components/TooltipButton';

import { DatasetService } from 'services/DatasetService';
import { ValidationService } from 'services/ValidationService';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

import { showValidationsReducer } from './_functions/Reducers/showValidationsReducer';

import { useApplyFilters } from 'views/_functions/Hooks/useApplyFilters';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const ShowValidationsList = memo(
  ({
    dataflowId,
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
    const resourcesContext = useContext(ResourcesContext);
    const validationContext = useContext(ValidationContext);

    const [columns, setColumns] = useState([]);
    const [fetchedData, setFetchedData] = useState([]);
    const [fieldsTypesFilter, setFieldsTypesFilter] = useState([]);
    const [fieldValueFilter, setFieldValueFilter] = useState([]);
    const [firstRow, setFirstRow] = useState(0);
    const [isFilteredLevelErrors, setIsFilteredLevelErrors] = useState(false);
    const [isFilteredOrigins, setIsFilteredOrigins] = useState(false);
    const [isFilteredTypeEntities, setIsFilteredTypeEntities] = useState(false);
    const [isLoadingModal, setIsLoadingModal] = useState(true);
    const [isLoadingTable, setIsLoadingTable] = useState(false);
    const [isRulesDescriptionLoaded, setIsRulesDescriptionLoaded] = useState(false);
    const [levelErrorsFilter, setLevelErrorsFilter] = useState([]);
    const [levelErrorsTypesFilter, setLevelErrorsTypesFilter] = useState([]);
    const [numberRows, setNumberRows] = useState(10);
    const [originsTypesFilter, setOriginsTypesFilter] = useState([]);
    const [sortField, setSortField] = useState('');
    const [sortOrder, setSortOrder] = useState(0);
    const [tablesFilter, setTablesFilter] = useState([]);
    const [typeEntitiesFilter, setTypeEntitiesFilter] = useState([]);
    const [typeEntitiesTypesFilter, setTypeEntitiesTypesFilter] = useState([]);
    const [filterOptions, setFilterOptions] = useState([]);

    const [validationState, validationDispatch] = useReducer(showValidationsReducer, {
      totalErrors: 0,
      totalFilteredGroupedRecords: 0,
      totalFilteredRecords: 0,
      totalRecords: 0
    });

    const { totalErrors, totalFilteredRecords, totalRecords } = validationState;

    const isFiltered = totalRecords !== totalFilteredRecords;

    const { getFilterBy, resetFilterState, setData } = useApplyFilters('showValidations');

    useEffect(() => {
      const allTypesFilter = concat(
        levelErrorsTypesFilter,
        originsTypesFilter,
        typeEntitiesTypesFilter,
        fieldsTypesFilter
      );

      const nestedOptions = [];
      filterOptionsInitial[0].nestedOptions.forEach(optionMultiSelect => {
        nestedOptions.push({
          ...optionMultiSelect,
          multiSelectOptions: getValidationsOptionTypes(allTypesFilter, optionMultiSelect.key)
        });
      });

      const filterOptions = [{ type: 'MULTI_SELECT', nestedOptions }];

      setFilterOptions(filterOptions);
    }, [levelErrorsTypesFilter, originsTypesFilter, typeEntitiesTypesFilter, fieldsTypesFilter]);

    useEffect(() => {
      onLoadRulesDescription();
    }, []);

    useEffect(() => {
      const columns = [
        {
          key: 'entityType',
          header: resourcesContext.messages['entity'],
          style: columnStyles('entityType'),
          sortable: true
        },
        {
          key: 'tableSchemaName',
          header: resourcesContext.messages['table'],
          style: columnStyles('tableSchemaName'),
          sortable: true
        },
        {
          key: 'fieldSchemaName',
          header: resourcesContext.messages['field'],
          style: columnStyles('fieldSchemaName'),
          sortable: true
        },
        {
          key: 'shortCode',
          header: resourcesContext.messages['ruleCode'],
          template: ruleCodeTemplate,
          style: columnStyles('shortCode'),
          sortable: true
        },
        {
          key: 'levelError',
          header: resourcesContext.messages['levelError'],
          template: levelErrorTemplate,
          style: columnStyles('levelError'),
          sortable: true
        },
        {
          key: 'message',
          header: resourcesContext.messages['errorMessage'],
          style: columnStyles('message'),
          sortable: true
        },
        {
          key: 'recordId',
          header: resourcesContext.messages['recordId'],
          className: styles.invisibleHeader
        },
        {
          key: 'datasetPartitionId',
          header: resourcesContext.messages['datasetPartitionId'],
          className: styles.invisibleHeader
        },
        {
          key: 'tableSchemaId',
          header: resourcesContext.messages['tableSchemaId'],
          className: styles.invisibleHeader
        },
        {
          key: 'ruleId',
          header: resourcesContext.messages['id'],
          className: styles.invisibleHeader
        },
        {
          key: 'numberOfRecords',
          header: resourcesContext.messages['numberOfRecords'],
          sortable: true
        }
      ];

      const columnsArr = columns.map(column => (
        <Column
          body={column?.template}
          className={column?.className}
          field={column.key}
          header={column.header}
          key={column.key}
          sortable={column?.sortable}
          style={column?.style}
        />
      ));

      setColumns(columnsArr);
    }, [validationContext.rulesDescription]);

    useEffect(() => {
      if (isRulesDescriptionLoaded) {
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
      }

      resetFilterState();
    }, [visible, isRulesDescriptionLoaded]);

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

    const getRuleSchema = ruleId =>
      validationContext.rulesDescription.find(ruleDescription => ruleDescription.id === ruleId);

    const getValidationsOptionTypes = (data, option) => {
      const optionsItems = data
        .filter(filterType => filterType.type === option)
        .map(optionItem => ({ type: optionItem.value, value: optionItem.value }));
      return sortBy(optionsItems, 'type');
    };

    const levelErrorTemplate = recordData => (
      <div className={styles.levelErrorTemplateWrapper}>
        <LevelError type={recordData.levelError} />
      </div>
    );

    const ruleCodeTemplate = recordData => {
      const ruleInfo = getRuleSchema(recordData.ruleId);
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
                  <span className={styles.tooltipInfoLabel}>{resourcesContext.messages['ruleName']}: </span>{' '}
                  <span className={styles.tooltipValueLabel}>{ruleInfo?.name}</span>
                  <br />
                  <span className={styles.tooltipInfoLabel}>{resourcesContext.messages['description']}: </span>
                  <span className={styles.tooltipValueLabel}>
                    {!isNil(ruleInfo?.description) && ruleInfo?.description !== ''
                      ? ruleInfo?.description
                      : resourcesContext.messages['noDescription']}
                  </span>
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
      setIsLoadingTable(true);

      try {
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

        addTableSchemaId(data.errors);
        validationDispatch({
          type: 'SET_TOTAL_GROUPED_ERRORS',
          payload: {
            totalErrors: data.totalErrors,
            totalFilteredGroupedRecords: data.totalFilteredErrors
          }
        });

        validationDispatch({
          type: 'SET_TOTALS_ERRORS',
          payload: { totalFilteredRecords: data.totalFilteredErrors, totalRecords: data.totalRecords }
        });

        setData(data.errors);
        setFetchedData(data.errors);
      } catch (error) {
        console.error('ShowValidationsList - onLoadErrors.', error);
      } finally {
        setIsLoadingTable(false);
        setIsLoadingModal(false);
      }
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

    const onLoadFilteredValidations = async () => {
      const filterData = await getFilterBy();

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
    };

    const onLoadRulesDescription = async () => {
      const validationsServiceList = await ValidationService.getAll(dataflowId, datasetSchemaId, reporting);
      validationContext.onSetRulesDescription(
        validationsServiceList?.validations?.map(validation => {
          return {
            automaticType: validation.automaticType,
            description: validation.description,
            id: validation.id,
            message: validation.message,
            name: validation.name,
            shortCode: validation.shortCode
          };
        })
      );
      setIsRulesDescriptionLoaded(true);
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
          const ruleSchema = getRuleSchema(event.data.ruleId);
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
        {resourcesContext.messages['totalRecords']} {totalRecords}{' '}
        {`${resourcesContext.messages['records'].toLowerCase()}${` (${resourcesContext.messages[
          'totalErrors'
        ].toLowerCase()}${totalErrors})`}`}
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

    const filterOptionsInitial = [
      {
        type: 'MULTI_SELECT',
        nestedOptions: [
          {
            key: 'entityType',
            label: resourcesContext.messages['entityType'],
            multiSelectOptions: []
          },
          {
            key: 'tableSchemaName',
            label: resourcesContext.messages['table'],
            showInput: true,
            multiSelectOptions: []
          },
          {
            key: 'fieldSchemaName',
            label: resourcesContext.messages['fieldSchemaName'],
            showInput: true,
            multiSelectOptions: []
          },
          {
            key: 'levelError',
            label: resourcesContext.messages['levelError'],
            multiSelectOptions: [],
            template: 'LevelError'
          }
        ]
      }
    ];

    const renderShowValidations = () => {
      if (isLoadingModal) {
        return (
          <div className={styles.validationsWithoutTable}>
            <div className={styles.loadingSpinner}>
              <Spinner className={styles.spinnerPosition} />
            </div>
          </div>
        );
      }

      if (isEmpty(fetchedData) && !isFiltered) {
        if (!isLoadingTable) {
          return (
            <div className={styles.validationsWithoutTable}>
              <div className={styles.noValidations}>{resourcesContext.messages['noValidations']}</div>
            </div>
          );
        } else {
          return (
            <div className={styles.validationsWithoutTable}>
              <div className={styles.loadingSpinner}>
                <Spinner className={styles.spinnerPosition} />
              </div>
            </div>
          );
        }
      }

      return (
        <div className={styles.validations}>
          <div className={styles.searchInput}>
            <Filters
              className="showValidations"
              onFilter={onLoadFilteredValidations}
              onReset={onLoadFilteredValidations}
              options={filterOptions}
              recoilId="showValidations"
            />
          </div>

          <div className={styles.validationsWrapper}>
            {!isEmpty(fetchedData) ? (
              <DataTable
                autoLayout={true}
                className={isWebformView ? styles.tableWebform : undefined}
                first={firstRow}
                hasDefaultCurrentPage={true}
                lazy={true}
                loading={isLoadingTable}
                onPage={onChangePage}
                onRowSelect={onRowSelect}
                onSort={onSort}
                paginator={true}
                paginatorRight={isLoadingTable ? <Spinner className={styles.loading} /> : getPaginatorRecordsCount()}
                reorderableColumns={true}
                resizableColumns={true}
                rows={numberRows}
                rowsPerPageOptions={[5, 10, 15]}
                selectionMode="single"
                sortable={true}
                sortField={sortField}
                sortOrder={sortOrder}
                summary={resourcesContext.messages['noNotifications']}
                totalRecords={totalFilteredRecords}
                value={fetchedData}>
                {columns}
              </DataTable>
            ) : (
              <div className={styles.emptyFilteredData}>
                {resourcesContext.messages['noValidationsWithSelectedParameters']}
              </div>
            )}
          </div>
        </div>
      );
    };

    return renderShowValidations();
  }
);
