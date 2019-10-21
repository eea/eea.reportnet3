/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef } from 'react';
import PropTypes from 'prop-types';

import isUndefined from 'lodash/isUndefined';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import styles from './ValidationViewer.module.css';

import { DataTable } from 'ui/views/_components/DataTable';
import { Column } from 'primereact/column';

import { Button } from 'ui/views/_components/Button';
import { DropdownFilter } from 'ui/views/ReporterDataSet/_components/DropdownFilter';
import { ReporterDatasetContext } from 'ui/views/ReporterDataSet/_components/_context/ReporterDataSetContext';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DatasetService } from 'core/services/DataSet';

const ValidationViewer = React.memo(
  ({ visible, datasetId, datasetName, buttonsList = undefined, hasWritePermissions, tableSchemaNames }) => {
    const contextReporterDataset = useContext(ReporterDatasetContext);
    const resources = useContext(ResourcesContext);
    const [allLevelErrorsFilter, setAllLevelErrorsFilter] = useState([]);
    const [allTypeEntitiesFilter, setAllTypeEntitiesFilter] = useState([]);
    const [allOriginsFilter, setAllOriginsFilter] = useState([]);
    const [columns, setColumns] = useState([]);
    const [fetchedData, setFetchedData] = useState([]);
    const [firstRow, setFirstRow] = useState(0);
    const [levelErrorsFilter, setLevelErrorsFilter] = useState([]);
    const [loading, setLoading] = useState(false);
    const [numberRows, setNumberRows] = useState(10);
    const [originsFilter, setOriginsFilter] = useState([]);
    const [sortField, setSortField] = useState('');
    const [sortOrder, setSortOrder] = useState(0);
    const [totalRecords, setTotalRecords] = useState(0);
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
      columnsArr.push(<Column key="recordId" field="recordId" header="" className={styles.VisibleHeader} />);
      columnsArr.push(
        <Column key="datasetPartitionId" field="datasetPartitionId" header="" className={styles.VisibleHeader} />
      );
      columnsArr.push(<Column key="tableSchemaId" field="tableSchemaId" header="" className={styles.VisibleHeader} />);
      setColumns(columnsArr);
    }, []);

    useEffect(() => {
      if (visible) {
        fetchData('', sortOrder, firstRow, numberRows, levelErrorsFilter, typeEntitiesFilter, originsFilter);
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
      setFetchedData(datasetErrors.errors);
      setLoading(false);
    };

    const onLoadFilters = async () => {
      onLoadLevelErrorsFilter();
      onLoadTypeEntitiesFilter();
      onLoadOriginsFilter();
    };

    const onLoadLevelErrorsFilter = () => {
      const allLevelErrorsFilterList = [{ label: 'Error', key: 'Error_Id' }, { label: 'Warning', key: 'Warning_Id' }];
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
      tableSchemaNames.map(name => {
        allOriginsFilterList.push({ label: name.toString(), key: `${name.toString()}_Id` });
      });
      setAllOriginsFilter(allOriginsFilterList);
    };

    const onLoadErrorsWithErrorLevelFilter = levelErrorsDeselected => {
      levelErrorsDeselected = levelErrorsDeselected.map(filter => {
        return filter.toString().toUpperCase();
      });
      setLevelErrorsFilter(levelErrorsDeselected);
      setLoading(true);
      onLoadErrors(
        firstRow,
        numberRows,
        sortField,
        sortOrder,
        levelErrorsDeselected,
        typeEntitiesFilter,
        originsFilter
      );
    };

    const onLoadErrorsWithEntityFilter = typeEntitiesDeselected => {
      typeEntitiesDeselected = typeEntitiesDeselected.map(filter => {
        return filter.toString().toUpperCase();
      });

      setTypeEntitiesFilter(typeEntitiesDeselected);
      setLoading(true);
      onLoadErrors(
        firstRow,
        numberRows,
        sortField,
        sortOrder,
        levelErrorsFilter,
        typeEntitiesDeselected,
        originsFilter
      );
    };

    const onLoadErrorsWithOriginsFilter = originsDeselected => {
      setOriginsFilter(originsDeselected);
      setLoading(true);
      onLoadErrors(
        firstRow,
        numberRows,
        sortField,
        sortOrder,
        levelErrorsFilter,
        typeEntitiesFilter,
        originsDeselected
      );
    };

    const onLoadErrorPosition = async (objectId, datasetId, entityType) => {
      const errorPosition = await DatasetService.errorPositionByObjectId(objectId, datasetId, entityType);
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
      setLoading(true);
      onLoadFilters();
      onLoadErrors(firstRow, numberRows, sortField, sortOrder, levelErrorsFilter, typeEntitiesFilter, originsFilter);
    };

    const isFiltered = (originalFilter, filter) => {
      if (filter.length < originalFilter.length) {
        return true;
      } else {
        return false;
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
          contextReporterDataset.onSelectValidation(
            event.data.tableSchemaId,
            datasetError.position,
            datasetError.recordId
          );
          contextReporterDataset.onValidationsVisible();
          break;
        case 'TABLE':
          contextReporterDataset.onSelectValidation(event.data.tableSchemaId, -1, -1);
          contextReporterDataset.onValidationsVisible();
          break;
        default:
          break;
      }
    };

    let totalCount = <span>Total: {totalRecords} rows</span>;

    const clearFilters = () => {
      setOriginsFilter([]);
      setTypeEntitiesFilter([]);
      setLevelErrorsFilter([]);
    };

    const refreshData = () => {
      setLoading(true);
      onLoadErrors(firstRow, numberRows, sortField, sortOrder, levelErrorsFilter, typeEntitiesFilter, originsFilter);
    };

    return (
      <div>
        {!isUndefined(buttonsList) ? (
          buttonsList
        ) : (
          <Toolbar className={styles.validationToolbar}>
            <div className="p-toolbar-group-left">
              <Button
                className={`p-button-rounded p-button-secondary`}
                // icon={'eye'}
                icon={'filter'}
                label={resources.messages['origin']}
                onClick={event => {
                  dropdownOriginsFilterRef.current.show(event);
                }}
              />
              <DropdownFilter
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
                className={`p-button-rounded p-button-secondary`}
                icon={'warning'}
                label={resources.messages['levelError']}
                onClick={event => {
                  dropdownLevelErrorsFilterRef.current.show(event);
                }}
              />
              <DropdownFilter
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
              />
              <DropdownFilter
                filters={allTypeEntitiesFilter}
                popup={true}
                ref={dropdownTypeEntitiesFilterRef}
                id="exportTableMenu"
                showNotCheckedFilters={onLoadErrorsWithEntityFilter}
                onShow={e => {
                  getExportButtonPosition(e);
                }}
              />
            </div>
            <div className="p-toolbar-group-right">
              <Button
                className={`p-button-rounded p-button-secondary`}
                disabled={false}
                icon={'cross'}
                label={resources.messages['clearFilters']}
                onClick={() => {
                  clearFilters();
                  fetchData('', sortOrder, firstRow, numberRows, [], [], []);
                }}
              />
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
        <div>
          <DataTable
            autoLayout={true}
            first={firstRow}
            lazy={true}
            loading={loading}
            onRowSelect={onRowSelect}
            onPage={onChangePage}
            onSort={onSort}
            paginator={true}
            paginatorRight={totalCount}
            resizableColumns={true}
            reorderableColumns={true}
            rows={numberRows}
            rowsPerPageOptions={[5, 10, 15]}
            sortable={true}
            sortField={sortField}
            sortOrder={sortOrder}
            totalRecords={totalRecords}
            selectionMode="single"
            value={fetchedData}>
            {columns}
          </DataTable>
        </div>
      </div>
    );
  }
);

ValidationViewer.propTypes = {
  id: PropTypes.string,
  buttonsList: PropTypes.array
};

export { ValidationViewer };
