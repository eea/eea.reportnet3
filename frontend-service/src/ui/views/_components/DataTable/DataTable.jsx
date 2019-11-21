import React, { Component } from 'react';
import { isUndefined } from 'lodash';

import colors from 'conf/colors.json';

import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Paginator } from 'primereact/paginator';
import { ScrollableView } from './_components/ScrollableView';
import { TableBody } from './_components/TableBody';
import { TableFooter } from './_components/TableFooter';
import { TableHeader } from './_components/TableHeader';
import { TableLoadingBody } from './_components/TableLoadingBody';

import classNames from 'classnames';

import DomHandler from 'ui/DomHandler';
import ObjectUtils from 'ui/ObjectUtils';
import PropTypes from 'prop-types';

export class DataTable extends Component {
  static defaultProps = {
    id: null,
    value: null,
    header: null,
    footer: null,
    style: null,
    className: null,
    tableStyle: null,
    tableClassName: null,
    paginator: false,
    paginatorPosition: 'bottom',
    pasteHeader: '',
    alwaysShowPaginator: true,
    paginatorTemplate: 'FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown',
    paginatorLeft: null,
    paginatorRight: null,
    pageLinkSize: 5,
    rowsPerPageOptions: null,
    currentPageReportTemplate: '({currentPage} of {totalPages})',
    first: null,
    rows: null,
    totalRecords: null,
    lazy: false,
    sortField: null,
    sortOrder: null,
    multiSortMeta: null,
    sortMode: 'single',
    defaultSortOrder: 1,
    emptyMessage: null,
    selectionMode: null,
    selection: null,
    onSelectionChange: null,
    contextMenuSelection: null,
    onContextMenuSelectionChange: null,
    compareSelectionBy: 'deepEquals',
    dataKey: null,
    metaKeySelection: true,
    headerColumnGroup: null,
    footerColumnGroup: null,
    frozenHeaderColumnGroup: null,
    frozenFooterColumnGroup: null,
    rowExpansionTemplate: null,
    expandedRows: null,
    onRowToggle: null,
    responsive: false,
    resizableColumns: false,
    columnResizeMode: 'fit',
    reorderableColumns: false,
    filters: null,
    globalFilter: null,
    scrollable: false,
    scrollHeight: null,
    virtualScroll: false,
    virtualScrollDelay: 150,
    virtualRowHeight: 28,
    frozenWidth: null,
    frozenValue: null,
    csvSeparator: ',',
    exportFilename: 'download',
    rowGroupMode: null,
    autoLayout: false,
    rowClassName: null,
    rowGroupHeaderTemplate: null,
    rowGroupFooterTemplate: null,
    loading: false,
    loadingIcon: 'pi pi-spinner',
    tabIndex: '0',
    stateKey: null,
    stateStorage: 'session',
    onColumnResizeEnd: null,
    onSort: null,
    onPage: null,
    onFilter: null,
    onVirtualScroll: null,
    onRowClick: null,
    onRowDoubleClick: null,
    onRowSelect: null,
    onRowUnselect: null,
    onRowExpand: null,
    onRowCollapse: null,
    onContextMenu: null,
    onColReorder: null,
    onRowReorder: null,
    onValueChange: null
  };

  static propTypes = {
    id: PropTypes.string,
    value: PropTypes.array,
    header: PropTypes.any,
    footer: PropTypes.any,
    style: PropTypes.object,
    className: PropTypes.string,
    tableStyle: PropTypes.any,
    tableClassName: PropTypes.string,
    paginator: PropTypes.bool,
    paginatorPosition: PropTypes.string,
    pasteHeader: PropTypes.string,
    alwaysShowPaginator: PropTypes.bool,
    paginatorTemplate: PropTypes.string,
    paginatorLeft: PropTypes.any,
    paginatorRight: PropTypes.any,
    pageLinkSize: PropTypes.number,
    rowsPerPageOptions: PropTypes.array,
    currentPageReportTemplate: PropTypes.string,
    first: PropTypes.number,
    rows: PropTypes.number,
    totalRecords: PropTypes.number,
    lazy: PropTypes.bool,
    sortField: PropTypes.string,
    sortOrder: PropTypes.number,
    multiSortMeta: PropTypes.array,
    sortMode: PropTypes.string,
    defaultSortOrder: PropTypes.number,
    emptyMessage: PropTypes.string,
    selectionMode: PropTypes.string,
    selection: PropTypes.any,
    onSelectionChange: PropTypes.func,
    compareSelectionBy: PropTypes.string,
    dataKey: PropTypes.string,
    metaKeySelection: PropTypes.bool,
    headerColumnGroup: PropTypes.any,
    footerColumnGroup: PropTypes.any,
    frozenHeaderColumnGroup: PropTypes.any,
    frozenFooterColumnGroup: PropTypes.any,
    rowExpansionTemplate: PropTypes.func,
    expandedRows: PropTypes.array,
    onRowToggle: PropTypes.func,
    responsive: PropTypes.bool,
    resizableColumns: PropTypes.bool,
    columnResizeMode: PropTypes.string,
    reorderableColumns: PropTypes.bool,
    filters: PropTypes.object,
    globalFilter: PropTypes.any,
    scrollable: PropTypes.bool,
    scrollHeight: PropTypes.string,
    virtualScroll: PropTypes.bool,
    virtualScrollDelay: PropTypes.number,
    virtualRowHeight: PropTypes.number,
    frozenWidth: PropTypes.string,
    frozenValue: PropTypes.array,
    csvSeparator: PropTypes.string,
    exportFilename: PropTypes.string,
    rowGroupMode: PropTypes.string,
    autoLayout: PropTypes.bool,
    rowClassName: PropTypes.func,
    rowGroupHeaderTemplate: PropTypes.func,
    rowGroupFooterTemplate: PropTypes.func,
    loading: PropTypes.bool,
    loadingIcon: PropTypes.string,
    tabIndex: PropTypes.string,
    stateKey: PropTypes.string,
    stateStorage: PropTypes.string,
    onColumnResizeEnd: PropTypes.func,
    onSort: PropTypes.func,
    onPage: PropTypes.func,
    onFilter: PropTypes.func,
    onVirtualScroll: PropTypes.func,
    onRowClick: PropTypes.func,
    onRowDoubleClick: PropTypes.func,
    onRowSelect: PropTypes.func,
    onRowUnselect: PropTypes.func,
    onRowExpand: PropTypes.func,
    onRowCollapse: PropTypes.func,
    onContextMenu: PropTypes.func,
    onColReorder: PropTypes.func,
    onRowReorder: PropTypes.func,
    onValueChange: PropTypes.func
  };

  constructor(props) {
    super(props);
    let state = {
      confirmVisible: false,
      pastedData: []
    };

    if (!this.props.onPage) {
      state.first = props.first;
      state.rows = props.rows;
    }

    if (!this.props.onSort) {
      state.sortField = props.sortField;
      state.sortOrder = props.sortOrder;
      state.multiSortMeta = props.multiSortMeta;
    }

    if (!this.props.onFilter) {
      state.filters = props.filters;
    }

    if (this.isStateful()) {
      this.restoreState(state);
    }

    if (Object.keys(state).length) {
      this.state = state;
    }

    this.onPageChange = this.onPageChange.bind(this);
    this.onSort = this.onSort.bind(this);
    this.onFilter = this.onFilter.bind(this);
    this.onColumnResizeStart = this.onColumnResizeStart.bind(this);
    this.onHeaderCheckboxClick = this.onHeaderCheckboxClick.bind(this);
    this.onColumnDragStart = this.onColumnDragStart.bind(this);
    this.onColumnDragOver = this.onColumnDragOver.bind(this);
    this.onColumnDragLeave = this.onColumnDragLeave.bind(this);
    this.onColumnDrop = this.onColumnDrop.bind(this);
    this.onVirtualScroll = this.onVirtualScroll.bind(this);
    this.onPasteAccept = this.onPasteAccept.bind(this);
    this.onPasteCancel = this.onPasteCancel.bind(this);
    this.frozenSelectionMode = null;
    this.checkPastedColumnsErrors = this.checkPastedColumnsErrors.bind(this);
    this.previewPastedDataBody = this.previewPastedDataBody.bind(this);
    this.previewPastedDataHeaders = this.previewPastedDataHeaders.bind(this);
    this.previewPastedData = this.previewPastedData.bind(this);
    //document.addEventListener('paste', event => this.onPaste(event));
  }

  getFirst() {
    return this.props.onPage ? this.props.first : this.state.first;
  }

  getRows() {
    return this.props.onPage ? this.props.rows : this.state.rows;
  }

  getSortField() {
    return this.props.onSort ? this.props.sortField : this.state.sortField;
  }

  getSortOrder() {
    return this.props.onSort ? this.props.sortOrder : this.state.sortOrder;
  }

  getMultiSortMeta() {
    return this.props.onSort ? this.props.multiSortMeta : this.state.multiSortMeta;
  }

  getFilters() {
    return this.props.onFilter ? this.props.filters : this.state.filters;
  }

  getStorage() {
    switch (this.props.stateStorage) {
      case 'local':
        return window.localStorage;

      case 'session':
        return window.sessionStorage;

      default:
        throw new Error(
          this.props.stateStorage +
            ' is not a valid value for the state storage, supported values are "local" and "session".'
        );
    }
  }

  isStateful() {
    return this.props.stateKey != null;
  }

  saveState() {
    const storage = this.getStorage();
    let state = {};

    if (this.props.paginator) {
      state.first = this.getFirst();
      state.rows = this.getRows();
    }

    if (this.getSortField()) {
      state.sortField = this.getSortField();
      state.sortOrder = this.getSortOrder();
      state.multiSortMeta = this.getMultiSortMeta();
    }

    if (this.hasFilter()) {
      state.filters = this.getFilters();
    }

    if (this.props.resizableColumns) {
      this.saveColumnWidths(state);
    }

    if (this.props.reorderableColumns) {
      state.columnOrder = this.state.columnOrder;
    }

    if (this.props.expandedRows) {
      state.expandedRows = this.props.expandedRows;
    }

    if (this.props.selection && this.props.onSelectionChange) {
      state.selection = this.props.selection;
    }

    if (Object.keys(state).length) {
      storage.setItem(this.props.stateKey, JSON.stringify(state));
    }
  }

  clearState() {
    const storage = this.getStorage();

    if (this.props.stateKey) {
      storage.removeItem(this.props.stateKey);
    }
  }

  restoreState(state) {
    const storage = this.getStorage();
    const stateString = storage.getItem(this.props.stateKey);

    if (stateString) {
      let restoredState = JSON.parse(stateString);

      if (this.props.paginator) {
        if (this.props.onPage) {
          this.props.onPage({
            first: restoredState.first,
            rows: restoredState.rows
          });
        } else {
          state.first = restoredState.first;
          state.rows = restoredState.rows;
        }
      }

      if (restoredState.sortField) {
        if (this.props.onSort) {
          this.props.onSort({
            sortField: restoredState.sortField,
            sortOrder: restoredState.sortOrder,
            multiSortMeta: restoredState.multiSortMeta
          });
        } else {
          state.sortField = restoredState.sortField;
          state.sortOrder = restoredState.sortOrder;
          state.multiSortMeta = restoredState.multiSortMeta;
        }
      }

      if (restoredState.filters) {
        if (this.props.onFilter) {
          this.props.onFilter({
            filters: restoredState.filters
          });
        } else {
          state.filters = restoredState.filters;
        }
      }

      if (this.props.resizableColumns) {
        this.columnWidthsState = restoredState.columnWidths;
        this.tableWidthState = restoredState.tableWidth;
      }

      if (this.props.reorderableColumns) {
        state.columnOrder = restoredState.columnOrder;
      }

      if (restoredState.expandedRows && this.props.onRowToggle) {
        this.props.onRowToggle({
          data: restoredState.expandedRows
        });
      }

      if (restoredState.selection && this.props.onSelectionChange) {
        this.props.onSelectionChange({
          value: restoredState.selection
        });
      }
    }
  }

  saveColumnWidths(state) {
    let widths = [];
    let headers = DomHandler.find(this.container, '.p-datatable-thead > tr > th');
    headers.map(header => widths.push(DomHandler.getOuterWidth(header)));
    state.columnWidths = widths.join(',');

    if (this.props.columnResizeMode === 'expand') {
      state.tableWidth = this.props.scrollable
        ? DomHandler.findSingle(this.container, '.p-datatable-scrollable-header-table').style.width
        : DomHandler.getOuterWidth(this.table) + 'px';
    }
  }

  restoreColumnWidths() {
    if (this.columnWidthsState) {
      let widths = this.columnWidthsState.split(',');

      if (this.props.columnResizeMode === 'expand' && this.tableWidthState) {
        if (this.props.scrollable) {
          this.setScrollableItemsWidthOnExpandResize(null, this.tableWidthState, 0);
        } else {
          this.tableViewChild.nativeElement.style.width = this.tableWidthState;
          this.containerViewChild.nativeElement.style.width = this.tableWidthState;
        }
      }

      if (this.props.scrollable) {
        let headerCols = DomHandler.find(this.container, '.p-datatable-scrollable-header-table > colgroup > col');
        let bodyCols = DomHandler.find(this.container, '.p-datatable-scrollable-body-table > colgroup > col');

        headerCols.map((col, index) => (col.style.width = widths[index] + 'px'));
        bodyCols.map((col, index) => (col.style.width = widths[index] + 'px'));
      } else {
        let headers = DomHandler.find(this.table, '.p-datatable-thead > tr > th');
        headers.map((header, index) => (header.style.width = widths[index] + 'px'));
      }
    }
  }

  onPaste(event) {
    //Preview data
    // this.props.pastedRecords;
    // this.props.recordsPreviewNumber;
    // this.props.columnsPreviewNumber;

    if (this.props.onPaste) {
      this.props.onPaste();
      this.setState({
        confirmVisible: true,
        pastedData: this.props.pastedData
      });
    } else {
      //Default component paste
    }
  }

  onPasteAccept() {
    if (this.props.onPasteAccept) {
      this.props.onPasteAccept();
    } else {
      //Default paste Accept
    }

    this.setState({
      confirmVisible: false
    });

    // let auxFiltered = {}
    // let auxArrayFiltered = [];
    // dataFiltered.forEach(dat => {
    //   dat.forEach(d=>auxFiltered = {...auxFiltered,...d});
    //   auxArrayFiltered.push(auxFiltered);
    //   auxFiltered={};
    // });
  }

  onPasteCancel() {
    if (this.state.confirmVisible) {
      this.setState({
        confirmVisible: false
      });
    }
  }

  onPageChange(event) {
    if (this.props.onPage) this.props.onPage(event);
    else this.setState({ first: event.first, rows: event.rows });

    if (this.props.onValueChange) {
      this.props.onValueChange();
    }
  }

  previewPastedData() {
    return (
      <div className="p-datatable-wrapper">
        <table className="p-datatable" style={{ width: '100%' }}>
          <thead className="p-datatable-thead">
            <tr>{this.previewPastedDataHeaders()}</tr>
          </thead>
          <tbody className="p-datatable-tbody">{this.previewPastedDataBody()}</tbody>
        </table>
      </div>
    );
  }

  previewPastedDataHeaders() {
    if (this.props.pastedRecords) {
      if (this.props.pastedRecords.length > 0) {
        const filteredColumns = this.props.children.filter(
          column => column.key !== 'actions' && column.key !== 'recordValidation' && column.key !== 'id'
        );
        let columns = filteredColumns.slice(0, this.props.recordsPreviewNumber);

        columns = columns.map((column, i) => {
          return (
            <th key={i} className="p-resizable-column">
              {column.props.header}
            </th>
          );
        });
        if (filteredColumns.length > this.props.columnsPreviewNumber) {
          columns.push(<th key="previewColumn">...</th>);
        }
        return columns;
      }
    }
  }

  previewPastedDataBody() {
    if (this.props.pastedRecords) {
      if (this.props.pastedRecords.length > 0) {
        const filteredColumns = this.props.children.filter(
          column => column.key !== 'actions' && column.key !== 'recordValidation' && column.key !== 'id'
        );
        let records = this.props.pastedRecords.slice(0, this.props.recordsPreviewNumber);
        if (records) {
          records = records.map((record, i) => {
            return (
              <tr key={i} className="p-datatable-row">
                {record.dataRow
                  .slice(
                    0,
                    filteredColumns.length < this.props.columnsPreviewNumber
                      ? filteredColumns.length
                      : this.props.columnsPreviewNumber
                  )
                  .map((column, j) => {
                    return <td key={j}>{Object.values(column.fieldData)[0]}</td>;
                  })}
              </tr>
            );
          });
        }
        if (this.props.pastedRecords.length > this.props.recordsPreviewNumber) {
          records.push(
            <tr key="preview">
              <td style={{ colSpan: this.props.columnsPreviewNumber }}>...</td>
            </tr>
          );
        }
        return records;
      }
    }
  }

  checkPastedColumnsErrors() {
    let correctColumns = true;
    if (this.props.pastedRecords) {
      if (this.props.pastedRecords.length > 0) {
        let i = 0;
        //Data columns minus "actions", "recordValidations" and "id" columnns
        const columns = this.props.children.filter(
          column => column.key !== 'actions' && column.key !== 'recordValidation' && column.key !== 'id'
        );
        do {
          if (!isUndefined(this.props.pastedRecords[i])) {
            const pastedColumns = this.props.pastedRecords[i].dataRow.filter(d => Object.values(d.fieldData)[0]);
            if (pastedColumns.length !== columns.length) {
              correctColumns = false;
            }
            i++;
          }
        } while (i < this.props.pastedRecords.length && correctColumns);
      }
    }

    if (!correctColumns) {
      return (
        <div>
          <p style={{ fontWeight: 'bold', color: colors.errors }}>
            Warning! There are rows with a wrong number of columns
          </p>
          <p>Do you still want to paste this data?</p>
        </div>
      );
    } else {
      return (
        <div>
          <p>Do you want to paste this data?</p>
        </div>
      );
    }
  }

  createPaginator(position, totalRecords, data) {
    let className = 'p-paginator-' + position;

    return (
      <Paginator
        first={this.getFirst()}
        rows={this.getRows()}
        pageLinkSize={this.props.pageLinkSize}
        className={className}
        onPageChange={this.onPageChange}
        template={this.props.paginatorTemplate}
        totalRecords={totalRecords}
        rowsPerPageOptions={this.props.rowsPerPageOptions}
        currentPageReportTemplate={this.props.currentPageReportTemplate}
        leftContent={this.props.paginatorLeft}
        rightContent={this.props.paginatorRight}
        alwaysShow={this.props.alwaysShowPaginator}
      />
    );
  }

  onSort(event) {
    let sortField = event.sortField;
    let sortOrder = this.getSortField() === event.sortField ? this.getSortOrder() * -1 : this.props.defaultSortOrder;
    let multiSortMeta;

    this.columnSortable = event.sortable;
    this.columnSortFunction = event.sortFunction;

    if (this.props.sortMode === 'multiple') {
      let metaKey = event.originalEvent.metaKey || event.originalEvent.ctrlKey;
      multiSortMeta = this.getMultiSortMeta();
      if (!multiSortMeta || !metaKey) {
        multiSortMeta = [];
      }

      this.addSortMeta({ field: sortField, order: sortOrder }, multiSortMeta);
    }

    if (this.props.onSort) {
      this.props.onSort({
        sortField: sortField,
        sortOrder: sortOrder,
        multiSortMeta: multiSortMeta
      });
    } else {
      this.setState({
        sortField: sortField,
        sortOrder: sortOrder,
        first: 0,
        multiSortMeta: multiSortMeta
      });
    }

    if (this.props.onValueChange) {
      this.props.onValueChange(
        this.processData({
          sortField: sortField,
          sortOrder: sortOrder,
          multiSortMeta: multiSortMeta
        })
      );
    }
  }

  addSortMeta(meta, multiSortMeta) {
    let index = -1;
    for (let i = 0; i < multiSortMeta.length; i++) {
      if (multiSortMeta[i].field === meta.field) {
        index = i;
        break;
      }
    }

    if (index >= 0) multiSortMeta[index] = meta;
    else multiSortMeta.push(meta);
  }

  sortSingle(data, sortField, sortOrder) {
    let value = [...data];

    if (this.columnSortable && this.columnSortFunction) {
      value = this.columnSortFunction({
        field: this.getSortField(),
        order: this.getSortOrder()
      });
    } else {
      value.sort((data1, data2) => {
        const value1 = ObjectUtils.resolveFieldData(data1, sortField);
        const value2 = ObjectUtils.resolveFieldData(data2, sortField);
        let result = null;

        if (value1 == null && value2 != null) result = -1;
        else if (value1 != null && value2 == null) result = 1;
        else if (value1 == null && value2 == null) result = 0;
        else if (typeof value1 === 'string' && typeof value2 === 'string')
          result = value1.localeCompare(value2, undefined, { numeric: true });
        else result = value1 < value2 ? -1 : value1 > value2 ? 1 : 0;

        return sortOrder * result;
      });
    }

    return value;
  }

  sortMultiple(data, multiSortMeta) {
    let value = [...data];
    value.sort((data1, data2) => {
      return this.multisortField(data1, data2, multiSortMeta, 0);
    });

    return value;
  }

  multisortField(data1, data2, multiSortMeta, index) {
    const value1 = ObjectUtils.resolveFieldData(data1, multiSortMeta[index].field);
    const value2 = ObjectUtils.resolveFieldData(data2, multiSortMeta[index].field);
    let result = null;

    if (typeof value1 === 'string' || value1 instanceof String) {
      if (value1.localeCompare && value1 !== value2) {
        return multiSortMeta[index].order * value1.localeCompare(value2, undefined, { numeric: true });
      }
    } else {
      result = value1 < value2 ? -1 : 1;
    }

    if (value1 === value2) {
      return multiSortMeta.length - 1 > index ? this.multisortField(data1, data2, multiSortMeta, index + 1) : 0;
    }

    return multiSortMeta[index].order * result;
  }

  filter(value, field, mode) {
    this.onFilter({
      value: value,
      field: field,
      matchMode: mode
    });
  }

  onFilter(event) {
    let currentFilters = this.getFilters();
    let newFilters = currentFilters ? { ...currentFilters } : {};

    if (!this.isFilterBlank(event.value))
      newFilters[event.field] = {
        value: event.value,
        matchMode: event.matchMode
      };
    else if (newFilters[event.field]) delete newFilters[event.field];

    if (this.props.onFilter) {
      this.props.onFilter({
        filters: newFilters
      });
    } else {
      this.setState({
        first: 0,
        filters: newFilters
      });
    }

    if (this.props.onValueChange) {
      this.props.onValueChange(
        this.processData({
          filters: newFilters
        })
      );
    }
  }

  hasFilter() {
    let filters = this.getFilters();

    return filters && Object.keys(filters).length > 0;
  }

  isFilterBlank(filter) {
    if (filter !== null && filter !== undefined) {
      if (
        (typeof filter === 'string' && filter.trim().length === 0) ||
        (filter instanceof Array && filter.length === 0)
      )
        return true;
      else return false;
    }
    return true;
  }

  hasFooter() {
    if (this.props.children) {
      if (this.props.footerColumnGroup) {
        return true;
      } else {
        if (this.props.children instanceof Array) {
          for (let i = 0; i < this.props.children.length; i++) {
            if (this.props.children[i].props.footer) {
              return true;
            }
          }
        } else {
          return this.props.children.props.footer !== null;
        }
      }
    } else {
      return false;
    }
  }

  onColumnResizeStart(event) {
    let containerLeft = DomHandler.getOffset(this.container).left;
    this.resizeColumn = event.columnEl;
    this.resizeColumnProps = event.columnProps;
    this.columnResizing = true;
    this.lastResizerHelperX = event.originalEvent.pageX - containerLeft + this.container.scrollLeft;

    this.bindColumnResizeEvents();
  }

  onColumnResize(event) {
    let containerLeft = DomHandler.getOffset(this.container).left;
    DomHandler.addClass(this.container, 'p-unselectable-text');
    this.resizerHelper.style.height = this.container.offsetHeight + 'px';
    this.resizerHelper.style.top = 0 + 'px';
    this.resizerHelper.style.left = event.pageX - containerLeft + this.container.scrollLeft + 'px';

    this.resizerHelper.style.display = 'block';
  }

  onColumnResizeEnd(event) {
    let delta = this.resizerHelper.offsetLeft - this.lastResizerHelperX;
    let columnWidth = this.resizeColumn.offsetWidth;
    let newColumnWidth = columnWidth + delta;
    let minWidth = this.resizeColumn.style.minWidth || 15;

    if (columnWidth + delta > parseInt(minWidth, 10)) {
      if (this.props.columnResizeMode === 'fit') {
        let nextColumn = this.resizeColumn.nextElementSibling;
        let nextColumnWidth = nextColumn.offsetWidth - delta;

        if (newColumnWidth > 15 && nextColumnWidth > 15) {
          if (this.props.scrollable) {
            let scrollableView = this.findParentScrollableView(this.resizeColumn);
            let scrollableBodyTable = DomHandler.findSingle(scrollableView, 'table.p-datatable-scrollable-body-table');
            let scrollableHeaderTable = DomHandler.findSingle(
              scrollableView,
              'table.p-datatable-scrollable-header-table'
            );
            let scrollableFooterTable = DomHandler.findSingle(
              scrollableView,
              'table.p-datatable-scrollable-footer-table'
            );
            let resizeColumnIndex = DomHandler.index(this.resizeColumn);

            this.resizeColGroup(scrollableHeaderTable, resizeColumnIndex, newColumnWidth, nextColumnWidth);
            this.resizeColGroup(scrollableBodyTable, resizeColumnIndex, newColumnWidth, nextColumnWidth);
            this.resizeColGroup(scrollableFooterTable, resizeColumnIndex, newColumnWidth, nextColumnWidth);
          } else {
            this.resizeColumn.style.width = newColumnWidth + 'px';
            if (nextColumn) {
              nextColumn.style.width = nextColumnWidth + 'px';
            }
          }
        }
      } else if (this.props.columnResizeMode === 'expand') {
        if (this.props.scrollable) {
          this.setScrollableItemsWidthOnExpandResize(this.resizeColumn, newColumnWidth, delta);
        } else {
          this.table.style.width = this.table.offsetWidth + delta + 'px';
          this.resizeColumn.style.width = newColumnWidth + 'px';
        }
      }

      if (this.props.onColumnResizeEnd) {
        this.props.onColumnResizeEnd({
          element: this.resizeColumn,
          column: this.resizeColumnProps,
          delta: delta
        });
      }

      if (this.isStateful()) {
        this.saveState();
      }
    }

    this.resizerHelper.style.display = 'none';
    this.resizeColumn = null;
    this.resizeColumnProps = null;
    DomHandler.removeClass(this.container, 'p-unselectable-text');

    this.unbindColumnResizeEvents();
  }

  setScrollableItemsWidthOnExpandResize(column, newColumnWidth, delta) {
    let scrollableView = column ? this.findParentScrollableView(column) : this.container;
    let scrollableBody = DomHandler.findSingle(scrollableView, '.p-datatable-scrollable-body');
    let scrollableHeader = DomHandler.findSingle(scrollableView, '.p-datatable-scrollable-header');
    let scrollableFooter = DomHandler.findSingle(scrollableView, '.p-datatable-scrollable-footer');
    let scrollableBodyTable = DomHandler.findSingle(scrollableBody, 'table.p-datatable-scrollable-body-table');
    let scrollableHeaderTable = DomHandler.findSingle(scrollableHeader, 'table.p-datatable-scrollable-header-table');
    let scrollableFooterTable = DomHandler.findSingle(scrollableFooter, 'table.p-datatable-scrollable-footer-table');

    const scrollableBodyTableWidth = column ? scrollableBodyTable.offsetWidth + delta : newColumnWidth;
    const scrollableHeaderTableWidth = column ? scrollableHeaderTable.offsetWidth + delta : newColumnWidth;
    const isContainerInViewport = this.container.offsetWidth >= scrollableBodyTableWidth;

    let setWidth = (container, table, width, isContainerInViewport) => {
      if (container && table) {
        container.style.width = isContainerInViewport
          ? width + DomHandler.calculateScrollbarWidth(scrollableBody) + 'px'
          : 'auto';
        table.style.width = width + 'px';
      }
    };

    setWidth(scrollableBody, scrollableBodyTable, scrollableBodyTableWidth, isContainerInViewport);
    setWidth(scrollableHeader, scrollableHeaderTable, scrollableHeaderTableWidth, isContainerInViewport);
    setWidth(scrollableFooter, scrollableFooterTable, scrollableHeaderTableWidth, isContainerInViewport);

    if (column) {
      let resizeColumnIndex = DomHandler.index(column);

      this.resizeColGroup(scrollableHeaderTable, resizeColumnIndex, newColumnWidth, null);
      this.resizeColGroup(scrollableBodyTable, resizeColumnIndex, newColumnWidth, null);
      this.resizeColGroup(scrollableFooterTable, resizeColumnIndex, newColumnWidth, null);
    }
  }

  findParentScrollableView(column) {
    if (column) {
      let parent = column.parentElement;
      while (parent && !DomHandler.hasClass(parent, 'p-datatable-scrollable-view')) {
        parent = parent.parentElement;
      }

      return parent;
    } else {
      return null;
    }
  }

  resizeColGroup(table, resizeColumnIndex, newColumnWidth, nextColumnWidth) {
    if (table) {
      let colGroup = table.children[0].nodeName === 'COLGROUP' ? table.children[0] : null;

      if (colGroup) {
        let col = colGroup.children[resizeColumnIndex];
        let nextCol = col.nextElementSibling;
        col.style.width = newColumnWidth + 'px';

        if (nextCol && nextColumnWidth) {
          nextCol.style.width = nextColumnWidth + 'px';
        }
      } else {
        throw new Error('Scrollable tables require a colgroup to support resizable columns');
      }
    }
  }

  bindColumnResizeEvents() {
    this.documentColumnResizeListener = document.addEventListener('mousemove', event => {
      if (this.columnResizing) {
        this.onColumnResize(event);
      }
    });

    this.documentColumnResizeEndListener = document.addEventListener('mouseup', event => {
      if (this.columnResizing) {
        this.columnResizing = false;
        this.onColumnResizeEnd(event);
      }
    });
  }

  unbindColumnResizeEvents() {
    document.removeEventListener('document', this.documentColumnResizeListener);
    document.removeEventListener('document', this.documentColumnResizeEndListener);
  }

  findParentHeader(element) {
    if (element.nodeName === 'TH') {
      return element;
    } else {
      let parent = element.parentElement;
      while (parent.nodeName !== 'TH') {
        parent = parent.parentElement;
        if (!parent) break;
      }
      return parent;
    }
  }

  onColumnDragStart(event) {
    if (this.columnResizing) {
      event.preventDefault();
      return;
    }

    this.iconWidth = DomHandler.getHiddenElementOuterWidth(this.reorderIndicatorUp);
    this.iconHeight = DomHandler.getHiddenElementOuterHeight(this.reorderIndicatorUp);

    this.draggedColumn = this.findParentHeader(event.target);
    event.dataTransfer.setData('text', 'b'); // Firefox requires this to make dragging possible
  }

  onColumnDragOver(event) {
    let dropHeader = this.findParentHeader(event.target);
    if (this.props.reorderableColumns && this.draggedColumn && dropHeader) {
      event.preventDefault();
      let containerOffset = DomHandler.getOffset(this.container);
      let dropHeaderOffset = DomHandler.getOffset(dropHeader);

      if (this.draggedColumn !== dropHeader) {
        let targetLeft = dropHeaderOffset.left - containerOffset.left;
        //let targetTop =  containerOffset.top - dropHeaderOffset.top;
        let columnCenter = dropHeaderOffset.left + dropHeader.offsetWidth / 2;

        this.reorderIndicatorUp.style.top = dropHeaderOffset.top - containerOffset.top - (this.iconHeight - 1) + 'px';
        this.reorderIndicatorDown.style.top =
          dropHeaderOffset.top - containerOffset.top + dropHeader.offsetHeight + 'px';

        if (event.pageX > columnCenter) {
          this.reorderIndicatorUp.style.left =
            targetLeft + dropHeader.offsetWidth - Math.ceil(this.iconWidth / 2) + 'px';
          this.reorderIndicatorDown.style.left =
            targetLeft + dropHeader.offsetWidth - Math.ceil(this.iconWidth / 2) + 'px';
          this.dropPosition = 1;
        } else {
          this.reorderIndicatorUp.style.left = targetLeft - Math.ceil(this.iconWidth / 2) + 'px';
          this.reorderIndicatorDown.style.left = targetLeft - Math.ceil(this.iconWidth / 2) + 'px';
          this.dropPosition = -1;
        }

        this.reorderIndicatorUp.style.display = 'block';
        this.reorderIndicatorDown.style.display = 'block';
      }
    }
  }

  onColumnDragLeave(event) {
    if (this.props.reorderableColumns && this.draggedColumn) {
      event.preventDefault();
      this.reorderIndicatorUp.style.display = 'none';
      this.reorderIndicatorDown.style.display = 'none';
    }
  }

  onColumnDrop(event) {
    event.preventDefault();
    if (this.draggedColumn) {
      let dragIndex = DomHandler.index(this.draggedColumn);
      let dropIndex = DomHandler.index(this.findParentHeader(event.target));
      let allowDrop = dragIndex !== dropIndex;
      if (
        allowDrop &&
        ((dropIndex - dragIndex === 1 && this.dropPosition === -1) ||
          (dragIndex - dropIndex === 1 && this.dropPosition === 1))
      ) {
        allowDrop = false;
      }

      if (allowDrop) {
        let columns = this.state.columnOrder ? this.getColumns() : React.Children.toArray(this.props.children);
        ObjectUtils.reorderArray(columns, dragIndex, dropIndex);
        let columnOrder = [];
        for (let column of columns) {
          columnOrder.push(column.props.columnKey || column.props.field);
        }

        this.setState({
          columnOrder: columnOrder
        });

        if (this.props.onColReorder) {
          this.props.onColReorder({
            dragIndex: dragIndex,
            dropIndex: dropIndex,
            columns: columns
          });
        }
      }

      this.reorderIndicatorUp.style.display = 'none';
      this.reorderIndicatorDown.style.display = 'none';
      this.draggedColumn.draggable = false;
      this.draggedColumn = null;
      this.dropPosition = null;
    }
  }

  onVirtualScroll(event) {
    if (this.virtualScrollTimer) {
      clearTimeout(this.virtualScrollTimer);
    }

    this.virtualScrollTimer = setTimeout(() => {
      if (this.props.onVirtualScroll) {
        this.props.onVirtualScroll({
          first: (event.page - 1) * this.props.rows,
          rows: this.props.virtualScroll ? this.props.rows * 2 : this.props.rows
        });
      }
    }, this.props.virtualScrollDelay);
  }

  exportCSV() {
    let data = this.processData();
    let csv = '\ufeff';
    let columns = React.Children.toArray(this.props.children);

    //headers
    for (let i = 0; i < columns.length; i++) {
      if (columns[i].props.field) {
        csv += '"' + (columns[i].props.header || columns[i].props.field) + '"';

        if (i < columns.length - 1) {
          csv += this.props.csvSeparator;
        }
      }
    }

    //body
    data.forEach((record, i) => {
      csv += '\n';
      for (let i = 0; i < columns.length; i++) {
        if (columns[i].props.field) {
          csv += '"' + ObjectUtils.resolveFieldData(record, columns[i].props.field) + '"';

          if (i < columns.length - 1) {
            csv += this.props.csvSeparator;
          }
        }
      }
    });

    let blob = new Blob([csv], {
      type: 'text/csv;charset=utf-8;'
    });

    if (window.navigator.msSaveOrOpenBlob) {
      navigator.msSaveOrOpenBlob(blob, this.props.exportFilename + '.csv');
    } else {
      let link = document.createElement('a');
      link.style.display = 'none';
      document.body.appendChild(link);
      if (link.download !== undefined) {
        link.setAttribute('href', URL.createObjectURL(blob));
        link.setAttribute('download', this.props.exportFilename + '.csv');
        link.click();
      } else {
        csv = 'data:text/csv;charset=utf-8,' + csv;
        window.open(encodeURI(csv));
      }
      document.body.removeChild(link);
    }
  }

  closeEditingCell() {
    document.body.click();
  }

  onHeaderCheckboxClick(event) {
    let selection;

    if (!event.checked) {
      let visibleData = this.hasFilter() ? this.processData() : this.props.value;
      selection = [...visibleData];
    } else {
      selection = [];
    }

    if (this.props.onSelectionChange) {
      this.props.onSelectionChange({
        originalEvent: event,
        value: selection
      });
    }
  }

  filterLocal(value, localFilters) {
    let filteredValue = [];
    let filters = localFilters || this.getFilters();
    let columns = React.Children.toArray(this.props.children);

    for (let i = 0; i < value.length; i++) {
      let localMatch = true;
      let globalMatch = false;

      for (let j = 0; j < columns.length; j++) {
        let col = columns[j];
        let filterMeta = filters ? filters[col.props.field] : null;

        //local
        if (filterMeta) {
          let filterValue = filterMeta.value;
          let filterField = col.props.field;
          let filterMatchMode = filterMeta.matchMode || col.props.filterMatchMode;
          let dataFieldValue = ObjectUtils.resolveFieldData(value[i], filterField);
          let filterConstraint =
            filterMatchMode === 'custom' ? col.props.filterFunction : ObjectUtils.filterConstraints[filterMatchMode];

          if (!filterConstraint(dataFieldValue, filterValue)) {
            localMatch = false;
          }

          if (!localMatch) {
            break;
          }
        }

        //global
        if (!col.props.excludeGlobalFilter && this.props.globalFilter && !globalMatch) {
          globalMatch = ObjectUtils.filterConstraints['contains'](
            ObjectUtils.resolveFieldData(value[i], col.props.field),
            this.props.globalFilter
          );
        }
      }

      let matches = localMatch;
      if (this.props.globalFilter) {
        matches = localMatch && globalMatch;
      }

      if (matches) {
        filteredValue.push(value[i]);
      }
    }

    if (filteredValue.length === value.length) {
      filteredValue = value;
    }

    return filteredValue;
  }

  processData(localState) {
    let data = this.props.value;

    if (!this.props.lazy) {
      if (data && data.length) {
        let sortField = (localState && localState.sortField) || this.getSortField();
        let sortOrder = (localState && localState.sortOrder) || this.getSortOrder();
        let multiSortMeta = (localState && localState.multiSortMeta) || this.getMultiSortMeta();

        if (sortField || multiSortMeta) {
          if (this.props.sortMode === 'single') data = this.sortSingle(data, sortField, sortOrder);
          else if (this.props.sortMode === 'multiple') data = this.sortMultiple(data, multiSortMeta);
        }

        let localFilters = (localState && localState.filters) || this.getFilters();
        if (localFilters || this.props.globalFilter) {
          data = this.filterLocal(data, localFilters);
        }
      }
    }

    return data;
  }

  isAllSelected() {
    let visibleData = this.hasFilter() ? this.processData() : this.props.value;

    return (
      this.props.selection && visibleData && visibleData.length && this.props.selection.length === visibleData.length
    );
  }

  getFrozenColumns(columns) {
    let frozenColumns = null;
    if (Array.isArray(columns)) {
      for (let col of columns) {
        if (col.props.frozen) {
          frozenColumns = frozenColumns || [];
          frozenColumns.push(col);
        }
      }
    }
    return frozenColumns;
  }

  getScrollableColumns(columns) {
    let scrollableColumns = null;
    if (Array.isArray(columns)) {
      for (let col of columns) {
        if (!col.props.frozen) {
          scrollableColumns = scrollableColumns || [];
          scrollableColumns.push(col);
        }
      }
    }
    return scrollableColumns;
  }

  getFrozenSelectionModeInColumn(columns) {
    if (Array.isArray(columns)) {
      for (let col of columns) {
        if (col.props.selectionMode) return col.props.selectionMode;
      }
    }

    return null;
  }

  createTableHeader(value, columns, columnGroup) {
    return (
      <TableHeader
        value={value}
        onSort={this.onSort}
        sortField={this.getSortField()}
        sortOrder={this.getSortOrder()}
        multiSortMeta={this.getMultiSortMeta()}
        columnGroup={columnGroup}
        resizableColumns={this.props.resizableColumns}
        onColumnResizeStart={this.onColumnResizeStart}
        onFilter={this.onFilter}
        onHeaderCheckboxClick={this.onHeaderCheckboxClick}
        headerCheckboxSelected={this.isAllSelected()}
        reorderableColumns={this.props.reorderableColumns}
        onColumnDragStart={this.onColumnDragStart}
        filters={this.getFilters()}
        onColumnDragOver={this.onColumnDragOver}
        onColumnDragLeave={this.onColumnDragLeave}
        onColumnDrop={this.onColumnDrop}
        tabIndex={this.props.tabIndex}>
        {columns}
      </TableHeader>
    );
  }

  createTableBody(value, columns) {
    return (
      <TableBody
        value={value}
        first={this.getFirst()}
        rows={this.getRows()}
        lazy={this.props.lazy}
        dataKey={this.props.dataKey}
        compareSelectionBy={this.props.compareSelectionBy}
        selectionMode={this.props.selectionMode}
        selection={this.props.selection}
        metaKeySelection={this.props.metaKeySelection}
        frozenSelectionMode={this.frozenSelectionMode}
        onEditingToggle={this.props.onEditingToggle}
        onSelectionChange={this.props.onSelectionChange}
        onRowClick={this.props.onRowClick}
        onRowDoubleClick={this.props.onRowDoubleClick}
        onRowSelect={this.props.onRowSelect}
        onRowUnselect={this.props.onRowUnselect}
        contextMenuSelection={this.props.contextMenuSelection}
        onContextMenuSelectionChange={this.props.onContextMenuSelectionChange}
        onContextMenu={this.props.onContextMenu}
        expandedRows={this.props.expandedRows}
        onRowToggle={this.props.onRowToggle}
        rowExpansionTemplate={this.props.rowExpansionTemplate}
        onRowExpand={this.props.onRowExpand}
        onRowCollapse={this.props.onRowCollapse}
        responsive={this.props.responsive}
        emptyMessage={this.props.emptyMessage}
        virtualScroll={this.props.virtualScroll}
        virtualRowHeight={this.props.virtualRowHeight}
        loading={this.props.loading}
        groupField={this.props.groupField}
        rowGroupMode={this.props.rowGroupMode}
        rowGroupHeaderTemplate={this.props.rowGroupHeaderTemplate}
        rowGroupFooterTemplate={this.props.rowGroupFooterTemplate}
        sortField={this.getSortField()}
        rowClassName={this.props.rowClassName}
        onRowReorder={this.props.onRowReorder}
        onPaste={this.props.onPaste}>
        {columns}
      </TableBody>
    );
  }

  createTableLoadingBody(columns) {
    if (this.props.virtualScroll) {
      return <TableLoadingBody columns={columns} rows={this.getRows()} />;
    } else {
      return null;
    }
  }

  createTableFooter(columns, columnGroup) {
    if (this.hasFooter()) return <TableFooter columnGroup={columnGroup}>{columns}</TableFooter>;
    else return null;
  }

  createScrollableView(value, columns, frozen, headerColumnGroup, footerColumnGroup, totalRecords) {
    return (
      <ScrollableView
        columns={columns}
        header={this.createTableHeader(value, columns, headerColumnGroup)}
        body={this.createTableBody(value, columns)}
        loadingBody={this.createTableLoadingBody(columns)}
        frozenBody={this.props.frozenValue ? this.createTableBody(this.props.frozenValue, columns) : null}
        footer={this.createTableFooter(columns, footerColumnGroup)}
        tableStyle={this.props.tableStyle}
        tableClassName={this.props.tableClassName}
        scrollHeight={this.props.scrollHeight}
        frozen={frozen}
        frozenWidth={this.props.frozenWidth}
        virtualScroll={this.props.virtualScroll}
        virtualRowHeight={this.props.virtualRowHeight}
        rows={this.props.rows}
        totalRecords={totalRecords}
        onVirtualScroll={this.onVirtualScroll}
        loading={this.props.loading}
      />
    );
  }

  getColumns() {
    let columns = React.Children.toArray(this.props.children);

    if (columns && columns.length) {
      if (this.props.reorderableColumns && this.state.columnOrder) {
        let orderedColumns = [];
        for (let columnKey of this.state.columnOrder) {
          let column = this.findColumnByKey(columns, columnKey);
          if (column) {
            orderedColumns.push(column);
          }
        }

        return [
          ...orderedColumns,
          ...columns.filter(item => {
            return orderedColumns.indexOf(item) < 0;
          })
        ];
      } else {
        return columns;
      }
    }

    return null;
  }

  findColumnByKey(columns, key) {
    if (columns && columns.length) {
      for (let i = 0; i < columns.length; i++) {
        let child = columns[i];
        if (child.props.columnKey === key || child.props.field === key) {
          return child;
        }
      }
    }

    return null;
  }

  getTotalRecords(data) {
    return this.props.lazy ? this.props.totalRecords : data ? data.length : 0;
  }

  resetColumnOrder() {
    let columns = React.Children.toArray(this.props.children);
    let columnOrder = [];

    for (let column of columns) {
      columnOrder.push(column.props.columnKey || column.props.field);
    }

    this.setState({
      columnOrder
    });
  }

  renderLoader() {
    let iconClassName = classNames('p-datatable-loading-icon pi-spin', this.props.loadingIcon);

    return (
      <div className="p-datatable-loading">
        <div className="p-datatable-loading-overlay p-component-overlay" />
        <div className="p-datatable-loading-content">
          <i className={iconClassName} />
        </div>
      </div>
    );
  }

  componentDidMount() {
    if (this.isStateful() && this.props.resizableColumns) {
      this.restoreColumnWidths();
    }
    //this.container.addEventListener('paste', event => this.onPaste(event));
  }

  componentDidUpdate() {
    if (this.isStateful()) {
      this.saveState();
    }
  }

  render() {
    let value = this.processData();
    let columns = this.getColumns();
    let totalRecords = this.getTotalRecords(value);
    let className = classNames(
      'p-datatable p-component',
      {
        'p-datatable-responsive': this.props.responsive,
        'p-datatable-resizable': this.props.resizableColumns,
        'p-datatable-resizable-fit': this.props.resizableColumns && this.props.columnResizeMode === 'fit',
        'p-datatable-scrollable': this.props.scrollable,
        'p-datatable-virtual-scrollable': this.props.virtualScroll,
        'p-datatable-auto-layout': this.props.autoLayout,
        'p-datatable-hoverable-rows': this.props.selectionMode
      },
      this.props.className
    );
    let paginatorTop =
      this.props.paginator && this.props.paginatorPosition !== 'bottom' && this.createPaginator('top', totalRecords);
    let paginatorBottom =
      this.props.paginator && this.props.paginatorPosition !== 'top' && this.createPaginator('bottom', totalRecords);
    let headerFacet = this.props.header && <div className="p-datatable-header">{this.props.header}</div>;
    let footerFacet = this.props.footer && <div className="p-datatable-footer">{this.props.footer}</div>;
    let resizeHelper = this.props.resizableColumns && (
      <div
        ref={el => {
          this.resizerHelper = el;
        }}
        className="p-column-resizer-helper p-highlight"
        style={{ display: 'none' }}
      />
    );
    let tableContent = null;
    let resizeIndicatorUp = this.props.reorderableColumns && (
      <span
        ref={el => {
          this.reorderIndicatorUp = el;
        }}
        className="pi pi-arrow-down p-datatable-reorder-indicator-up"
        style={{ position: 'absolute', display: 'none' }}
      />
    );
    let resizeIndicatorDown = this.props.reorderableColumns && (
      <span
        ref={el => {
          this.reorderIndicatorDown = el;
        }}
        className="pi pi-arrow-up p-datatable-reorder-indicator-down"
        style={{ position: 'absolute', display: 'none' }}
      />
    );
    let loader;

    if (this.props.loading) {
      loader = this.renderLoader();
    }

    if (this.props.scrollable) {
      this.frozenSelectionMode = this.frozenSelectionMode || this.getFrozenSelectionModeInColumn(columns);
      let frozenColumns = this.getFrozenColumns(columns);
      let scrollableColumns = frozenColumns ? this.getScrollableColumns(columns) : columns;
      let frozenView, scrollableView;
      if (frozenColumns) {
        frozenView = this.createScrollableView(
          value,
          frozenColumns,
          true,
          this.props.frozenHeaderColumnGroup,
          this.props.frozenFooterColumnGroup,
          totalRecords
        );
      }

      scrollableView = this.createScrollableView(
        value,
        scrollableColumns,
        false,
        this.props.headerColumnGroup,
        this.props.footerColumnGroup,
        totalRecords
      );

      tableContent = (
        <div className="p-datatable-scrollable-wrapper">
          {frozenView}
          {scrollableView}
        </div>
      );
    } else {
      let tableHeader = this.createTableHeader(value, columns, this.props.headerColumnGroup);
      let tableBody = this.createTableBody(value, columns);
      let tableFooter = this.createTableFooter(columns, this.props.footerColumnGroup);

      tableContent = (
        <div className="p-datatable-wrapper">
          <table
            style={this.props.tableStyle}
            className={this.props.tableClassName}
            ref={el => {
              this.table = el;
            }}>
            {tableHeader}
            {tableFooter}
            {tableBody}
          </table>
        </div>
      );
    }

    return (
      <React.Fragment>
        <div
          id={this.props.id}
          className={className}
          style={this.props.style}
          ref={el => {
            this.container = el;
          }}>
          {loader}
          {headerFacet}
          {paginatorTop}
          {tableContent}
          {paginatorBottom}
          {footerFacet}
          {resizeHelper}
          {resizeIndicatorUp}
          {resizeIndicatorDown}
        </div>
        <ConfirmDialog
          onConfirm={this.onPasteAccept}
          onHide={this.onPasteCancel}
          visible={this.state.confirmVisible}
          header={this.props.pasteHeader}
          maximizable={false}
          labelConfirm="Yes"
          labelCancel="No">
          <p>Copied records: {this.props.pastedRecords ? this.props.pastedRecords.length : null}</p>
          {this.checkPastedColumnsErrors()}
          <hr />
          {this.previewPastedData()}
        </ConfirmDialog>
      </React.Fragment>
    );
  }
}
