import { Component, Children } from 'react';
import PropTypes from 'prop-types';

import classNames from 'classnames';

import './DataTable.css';

import { InputText } from 'views/_components/InputText';
import { Paginator } from './_components/Paginator';
import ReactTooltip from 'react-tooltip';
import { ScrollableView } from './_components/ScrollableView';
import { TableBody } from './_components/TableBody';
import { TableFooter } from './_components/TableFooter';
import { TableHeader } from './_components/TableHeader';
import { TableLoadingBody } from './_components/TableLoadingBody';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import DomHandler from 'views/_functions/PrimeReact/DomHandler';
import ObjectUtils from 'views/_functions/PrimeReact/ObjectUtils';

export class DataTable extends Component {
  static defaultProps = {
    alwaysShowPaginator: true,
    autoLayout: false,
    className: null,
    columnResizeMode: 'fit',
    compareSelectionBy: 'deepEquals',
    contextMenuSelection: null,
    csvSeparator: ',',
    currentPageReportTemplate: '({currentPage} of {totalPages})',
    dataKey: null,
    defaultSortOrder: 1,
    editMode: 'cell',
    emptyMessage: null,
    expandedRows: null,
    exportFilename: 'download',
    filters: null,
    first: null,
    footer: null,
    footerColumnGroup: null,
    frozenFooterColumnGroup: null,
    frozenHeaderColumnGroup: null,
    frozenValue: null,
    frozenWidth: null,
    getPageChange: null,
    globalFilter: null,
    hasDefaultCurrentPage: false,
    header: null,
    headerColumnGroup: null,
    id: null,
    lazy: false,
    loading: false,
    loadingIcon: 'pi pi-spinner',
    metaKeySelection: true,
    multiSortMeta: null,
    onColReorder: null,
    onColumnResizeEnd: null,
    onContextMenu: null,
    onContextMenuSelectionChange: null,
    onFilter: null,
    onPage: null,
    onRowClick: null,
    onRowCollapse: null,
    onRowDoubleClick: null,
    onRowEditCancel: null,
    onRowEditInit: null,
    onRowEditSave: null,
    onRowExpand: null,
    onRowReorder: null,
    onRowSelect: null,
    onRowToggle: null,
    onRowUnselect: null,
    onSelectionChange: null,
    onSort: null,
    onValueChange: null,
    onVirtualScroll: null,
    pageLinkSize: 5,
    paginator: false,
    paginatorLeft: null,
    paginatorPosition: 'bottom',
    paginatorRight: null,
    paginatorTemplate: 'FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown',
    reorderableColumns: false,
    resizableColumns: false,
    responsive: false,
    rowClassName: () => ({ 'p-highlight-contextmenu': '' }),
    rowEditorValidator: null,
    rowExpansionTemplate: null,
    rowGroupFooterTemplate: null,
    rowGroupHeaderTemplate: null,
    rowGroupMode: null,
    rows: null,
    rowsPerPageOptions: null,
    scrollHeight: null,
    scrollable: false,
    selection: null,
    selectionMode: null,
    sortField: null,
    sortMode: 'single',
    sortOrder: null,
    stateKey: null,
    stateStorage: 'session',
    style: null,
    summary: null,
    tabIndex: '0',
    tableClassName: null,
    tableStyle: null,
    totalRecords: null,
    value: null,
    virtualRowHeight: 28,
    virtualScroll: false,
    virtualScrollDelay: 150
  };

  static propTypes = {
    alwaysShowPaginator: PropTypes.bool,
    autoLayout: PropTypes.bool,
    className: PropTypes.string,
    columnResizeMode: PropTypes.string,
    compareSelectionBy: PropTypes.string,
    csvSeparator: PropTypes.string,
    currentPageReportTemplate: PropTypes.string,
    dataKey: PropTypes.string,
    defaultSortOrder: PropTypes.number,
    editMode: PropTypes.string,
    emptyMessage: PropTypes.string,
    expandedRows: PropTypes.oneOfType([PropTypes.array, PropTypes.object]),
    exportFilename: PropTypes.string,
    filters: PropTypes.object,
    first: PropTypes.number,
    footer: PropTypes.any,
    footerColumnGroup: PropTypes.any,
    frozenFooterColumnGroup: PropTypes.any,
    frozenHeaderColumnGroup: PropTypes.any,
    frozenValue: PropTypes.array,
    frozenWidth: PropTypes.string,
    getPageChange: PropTypes.func,
    globalFilter: PropTypes.any,
    hasDefaultCurrentPage: PropTypes.bool,
    header: PropTypes.any,
    headerColumnGroup: PropTypes.any,
    id: PropTypes.string,
    lazy: PropTypes.bool,
    loading: PropTypes.bool,
    loadingIcon: PropTypes.string,
    metaKeySelection: PropTypes.bool,
    multiSortMeta: PropTypes.array,
    onColReorder: PropTypes.func,
    onColumnResizeEnd: PropTypes.func,
    onContextMenu: PropTypes.func,
    onFilter: PropTypes.func,
    onPage: PropTypes.func,
    onRowClick: PropTypes.func,
    onRowCollapse: PropTypes.func,
    onRowDoubleClick: PropTypes.func,
    onRowEditCancel: PropTypes.func,
    onRowEditInit: PropTypes.func,
    onRowEditSave: PropTypes.func,
    onRowExpand: PropTypes.func,
    onRowReorder: PropTypes.func,
    onRowSelect: PropTypes.func,
    onRowToggle: PropTypes.func,
    onRowUnselect: PropTypes.func,
    onSelectionChange: PropTypes.func,
    onSort: PropTypes.func,
    onValueChange: PropTypes.func,
    onVirtualScroll: PropTypes.func,
    pageLinkSize: PropTypes.number,
    paginator: PropTypes.bool,
    paginatorLeft: PropTypes.any,
    paginatorPosition: PropTypes.string,
    paginatorRight: PropTypes.any,
    paginatorTemplate: PropTypes.any,
    reorderableColumns: PropTypes.bool,
    resizableColumns: PropTypes.bool,
    responsive: PropTypes.bool,
    rowClassName: PropTypes.func,
    rowEditorValidator: PropTypes.func,
    rowExpansionTemplate: PropTypes.func,
    rowGroupFooterTemplate: PropTypes.func,
    rowGroupHeaderTemplate: PropTypes.func,
    rowGroupMode: PropTypes.string,
    rows: PropTypes.number,
    rowsPerPageOptions: PropTypes.array,
    scrollable: PropTypes.bool,
    scrollHeight: PropTypes.string,
    selection: PropTypes.any,
    selectionMode: PropTypes.string,
    sortField: PropTypes.string,
    sortMode: PropTypes.string,
    sortOrder: PropTypes.number,
    stateKey: PropTypes.string,
    stateStorage: PropTypes.string,
    style: PropTypes.object,
    summary: PropTypes.string,
    tabIndex: PropTypes.string,
    tableClassName: PropTypes.string,
    tableStyle: PropTypes.any,
    totalRecords: PropTypes.number,
    value: PropTypes.array,
    virtualRowHeight: PropTypes.number,
    virtualScroll: PropTypes.bool,
    virtualScrollDelay: PropTypes.number
  };

  static contextType = ResourcesContext;

  constructor(props) {
    super(props);
    this.state = {
      currentPage: 1
    };

    if (!this.props.onPage) {
      this.state.first = props.first;
      this.state.rows = props.rows;
    }

    if (!this.props.onSort) {
      this.state.sortField = props.sortField;
      this.state.sortOrder = props.sortOrder;
      this.state.multiSortMeta = props.multiSortMeta;
    }

    if (!this.props.onFilter) {
      this.state.filters = props.filters;
    }

    if (this.isStateful()) {
      this.restoreState(this.state);
    }

    this.onChangeCurrentPage = this.onChangeCurrentPage.bind(this);
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
    this.frozenSelectionMode = null;
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
          this.table.style.width = this.tableWidthState;
          this.container.style.width = this.tableWidthState;
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

  onChangeCurrentPage(event) {
    if (event.key === 'Enter' && this.state.currentPage !== '' && this.state.currentPage !== this.props.first) {
      var pc = Math.ceil(this.props.totalRecords / this.getRows()) || 1;
      var p = Math.floor(event.target.value - 1);

      if (p >= 0 && p < pc) {
        var newPageState = {
          currentPage: p + 1,
          first: (event.target.value - 1) * this.getRows(),
          rows: this.getRows(),
          page: p,
          pageCount: pc,
          pageInputTooltip:
            p >= 0 && p < pc
              ? this.context.messages['currentPageInfoMessage']
              : `${this.context.messages['currentPageErrorMessage']} ${Math.ceil(
                  this.props.totalRecords / this.getRows()
                )}`
        };
        this.onPageChange(newPageState);
      }
    } else {
      this.setState({
        currentPage: event.target.value
      });
      if (event.target.value <= 0 || event.target.value > Math.ceil(this.props.totalRecords / this.getRows())) {
        this.setState({
          pageInputTooltip: `${this.context.messages['currentPageErrorMessage']} ${Math.ceil(
            this.props.totalRecords / this.getRows()
          )}`
        });
      } else {
        this.setState({ pageInputTooltip: this.context.messages['currentPageInfoMessage'] });
      }
    }
  }

  onPageChange(event) {
    this.setState({ currentPage: event.currentPage, pageInputTooltip: event.pageInputTooltip });

    if (this.props.onPage) this.props.onPage(event);
    else this.setState({ currentPage: event.currentPage, first: event.first, rows: event.rows });

    if (this.props.getPageChange) this.props.getPageChange(event);

    if (this.props.onValueChange) {
      this.props.onValueChange();
    }
  }

  createPaginator(position, totalRecords, data) {
    let className = 'p-paginator-' + position;

    return (
      <Paginator
        alwaysShow={this.props.alwaysShowPaginator}
        className={className}
        currentPageReportTemplate={this.props.currentPageReportTemplate}
        first={this.getFirst()}
        leftContent={this.props.paginatorLeft}
        onPageChange={this.onPageChange}
        pageLinkSize={this.props.pageLinkSize}
        rightContent={this.props.paginatorRight}
        rows={this.getRows()}
        rowsPerPageOptions={this.props.rowsPerPageOptions}
        template={
          !this.props.hasDefaultCurrentPage
            ? this.props.paginatorTemplate
            : {
                layout: `PrevPageLink PageLinks NextPageLink RowsPerPageDropdown CurrentPageReport`,
                CurrentPageReport: options => {
                  return (
                    <span style={{ color: 'var(--white)', userSelect: 'none' }}>
                      <label style={{ margin: '0 0.5rem' }}>{this.context.messages['goTo']}</label>
                      <InputText
                        data-for="pageInputTooltip"
                        data-tip
                        id="currentPageInput"
                        keyfilter="pint"
                        onChange={this.onChangeCurrentPage}
                        onKeyDown={this.onChangeCurrentPage}
                        style={{
                          border:
                            (this.state.currentPage <= 0 || this.state.currentPage > options.totalPages) &&
                            '1px solid var(--errors)',
                          boxShadow:
                            this.state.currentPage <= 0 || this.state.currentPage > options.totalPages
                              ? 'var(--inputtext-box-shadow-focus-error)'
                              : 'none',
                          display: 'inline',
                          height: '1.75rem',
                          width: '2.5rem'
                        }}
                        value={this.state.currentPage}
                      />
                      <ReactTooltip border={true} effect="solid" id="pageInputTooltip" place="bottom">
                        {this.state.pageInputTooltip}
                      </ReactTooltip>
                      <label style={{ fontWeight: 'bold', margin: '0 0 0 0.5rem' }}>
                        {this.props.totalRecords > 0
                          ? `${this.context.messages['of']} ${Math.ceil(this.props.totalRecords / this.getRows())}`
                          : 1}
                      </label>
                    </span>
                  );
                }
              }
        }
        totalRecords={totalRecords}
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

    if (!this.isFilterBlank(event.value)) newFilters[event.field] = { value: event.value, matchMode: event.matchMode };
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
    let filters = this.getFilters() || this.props.globalFilter;

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
        return this.hasChildrenFooter(this.props.children);
      }
    } else {
      return false;
    }
  }

  hasChildrenFooter(children) {
    let hasFooter = false;

    if (children) {
      if (children instanceof Array) {
        for (let i = 0; i < children.length; i++) {
          hasFooter = hasFooter || this.hasChildrenFooter(children[i]);
        }
      } else {
        return children.props && children.props.footer !== null;
      }
    }

    return hasFooter;
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
    if (this.columnResizing || !this.reorderIndicatorUp) {
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
        let columns = this.state.columnOrder ? this.getColumns() : Children.toArray(this.props.children);
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
            originalEvent: event,
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
    let columns = Children.toArray(this.props.children);

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
    if (this.props.editMode !== 'row') {
      document.body.click();
    }
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
      const { originalEvent, ...rest } = event;

      this.props.onSelectionChange({
        originalEvent,
        value: selection,
        ...rest
      });
    }
  }

  filterLocal(value, localFilters) {
    let filteredValue = [];
    let filters = localFilters || this.getFilters();
    let columns = Children.toArray(this.props.children);

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

    for (let col of columns) {
      if (col.props.frozen) {
        frozenColumns = frozenColumns || [];
        frozenColumns.push(col);
      }
    }

    return frozenColumns;
  }

  getScrollableColumns(columns) {
    let scrollableColumns = null;

    for (let col of columns) {
      if (!col.props.frozen) {
        scrollableColumns = scrollableColumns || [];
        scrollableColumns.push(col);
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
        columnGroup={columnGroup}
        filters={this.getFilters()}
        headerCheckboxSelected={this.isAllSelected()}
        multiSortMeta={this.getMultiSortMeta()}
        onColumnDragLeave={this.onColumnDragLeave}
        onColumnDragOver={this.onColumnDragOver}
        onColumnDragStart={this.onColumnDragStart}
        onColumnDrop={this.onColumnDrop}
        onColumnResizeStart={this.onColumnResizeStart}
        onFilter={this.onFilter}
        onHeaderCheckboxClick={this.onHeaderCheckboxClick}
        onSort={this.onSort}
        reorderableColumns={this.props.reorderableColumns}
        resizableColumns={this.props.resizableColumns}
        sortField={this.getSortField()}
        sortOrder={this.getSortOrder()}
        tabIndex={this.props.tabIndex}
        value={value}>
        {columns}
      </TableHeader>
    );
  }

  createTableBody(value, columns) {
    return (
      <TableBody
        compareSelectionBy={this.props.compareSelectionBy}
        contextMenuSelection={this.props.contextMenuSelection}
        dataKey={this.props.dataKey}
        editMode={this.props.editMode}
        emptyMessage={this.props.emptyMessage}
        expandedRows={this.props.expandedRows}
        first={this.getFirst()}
        frozenSelectionMode={this.frozenSelectionMode}
        groupField={this.props.groupField}
        lazy={this.props.lazy}
        loading={this.props.loading}
        metaKeySelection={this.props.metaKeySelection}
        onContextMenu={this.props.onContextMenu}
        onContextMenuSelectionChange={this.props.onContextMenuSelectionChange}
        onRowClick={this.props.onRowClick}
        onRowCollapse={this.props.onRowCollapse}
        onRowDoubleClick={this.props.onRowDoubleClick}
        onRowEditCancel={this.props.onRowEditCancel}
        onRowEditInit={this.props.onRowEditInit}
        onRowEditSave={this.props.onRowEditSave}
        onRowExpand={this.props.onRowExpand}
        onRowReorder={this.props.onRowReorder}
        onRowSelect={this.props.onRowSelect}
        onRowToggle={this.props.onRowToggle}
        onRowUnselect={this.props.onRowUnselect}
        onSelectionChange={this.props.onSelectionChange}
        paginator={this.props.paginator}
        responsive={this.props.responsive}
        rowClassName={this.props.rowClassName}
        rowEditorValidator={this.props.rowEditorValidator}
        rowExpansionTemplate={this.props.rowExpansionTemplate}
        rowGroupFooterTemplate={this.props.rowGroupFooterTemplate}
        rowGroupHeaderTemplate={this.props.rowGroupHeaderTemplate}
        rowGroupMode={this.props.rowGroupMode}
        rows={this.getRows()}
        selection={this.props.selection}
        selectionMode={this.props.selectionMode}
        sortField={this.getSortField()}
        value={value}
        virtualRowHeight={this.props.virtualRowHeight}
        virtualScroll={this.props.virtualScroll}>
        {columns}
      </TableBody>
    );
  }

  createTableLoadingBody(columns) {
    if (this.props.virtualScroll) {
      return <TableLoadingBody columns={columns} rows={this.getRows()}></TableLoadingBody>;
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
        body={this.createTableBody(value, columns)}
        columns={columns}
        footer={this.createTableFooter(columns, footerColumnGroup)}
        frozen={frozen}
        frozenBody={this.props.frozenValue ? this.createTableBody(this.props.frozenValue, columns) : null}
        frozenWidth={this.props.frozenWidth}
        header={this.createTableHeader(value, columns, headerColumnGroup)}
        loading={this.props.loading}
        loadingBody={this.createTableLoadingBody(columns)}
        onVirtualScroll={this.onVirtualScroll}
        rows={this.props.rows}
        scrollHeight={this.props.scrollHeight}
        tableClassName={this.props.tableClassName}
        tableStyle={this.props.tableStyle}
        totalRecords={totalRecords}
        virtualRowHeight={this.props.virtualRowHeight}
        virtualScroll={this.props.virtualScroll}></ScrollableView>
    );
  }

  getColumns() {
    let columns = Children.toArray(this.props.children);

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
    let columns = Children.toArray(this.props.children);
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
        <div className="p-datatable-loading-overlay p-component-overlay"></div>
        <div className="p-datatable-loading-content">
          <i className={iconClassName}></i>
        </div>
      </div>
    );
  }

  componentDidMount() {
    this.setState({ pageInputTooltip: this.context.messages['currentPageInfoMessage'] });
    if (this.isStateful() && this.props.resizableColumns) {
      this.restoreColumnWidths();
    }
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
        className="p-column-resizer-helper p-highlight"
        ref={el => {
          this.resizerHelper = el;
        }}
        style={{ display: 'none' }}></div>
    );
    let tableContent = null;
    let resizeIndicatorUp = this.props.reorderableColumns && (
      <span
        className="pi pi-arrow-down p-datatable-reorder-indicator-up"
        ref={el => {
          this.reorderIndicatorUp = el;
        }}
        style={{ position: 'absolute', display: 'none' }}
      />
    );
    let resizeIndicatorDown = this.props.reorderableColumns && (
      <span
        className="pi pi-arrow-up p-datatable-reorder-indicator-down"
        ref={el => {
          this.reorderIndicatorDown = el;
        }}
        style={{ position: 'absolute', display: 'none' }}
      />
    );
    let loader;

    if (this.props.loading) {
      loader = this.renderLoader();
    }

    if (Array.isArray(columns)) {
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
              className={this.props.tableClassName}
              ref={el => {
                this.table = el;
              }}
              style={this.props.tableStyle}
              summary={this.props.summary}>
              {tableHeader}
              {tableFooter}
              {tableBody}
            </table>
          </div>
        );
      }
    }

    return (
      <div
        className={className}
        id={this.props.id}
        ref={el => {
          this.container = el;
        }}
        style={this.props.style}>
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
    );
  }
}
