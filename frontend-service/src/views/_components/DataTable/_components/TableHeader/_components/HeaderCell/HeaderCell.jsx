import { Component } from 'react';

import uniqueId from 'lodash/uniqueId';

import { InputText } from 'views/_components/InputText';

import classNames from 'classnames';
import { RowCheckbox } from 'views/_components/DataTable/_components/RowCheckbox';
import DomHandler from 'views/_functions/PrimeReact/DomHandler';

export class HeaderCell extends Component {
  constructor(props) {
    super(props);
    this.onClick = this.onClick.bind(this);
    this.onFilterInput = this.onFilterInput.bind(this);
    this.onMouseDown = this.onMouseDown.bind(this);
    this.onResizerMouseDown = this.onResizerMouseDown.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);
  }

  onClick(event) {
    if (this.props.sortable) {
      let targetNode = event.target;
      if (
        DomHandler.hasClass(targetNode, 'p-sortable-column') ||
        DomHandler.hasClass(targetNode, 'p-column-title') ||
        DomHandler.hasClass(targetNode, 'p-sortable-column-icon') ||
        DomHandler.hasClass(targetNode.parentElement, 'p-sortable-column-icon')
      ) {
        this.props.onSort({
          originalEvent: event,
          sortField: this.props.columnSortField || this.props.field,
          sortFunction: this.props.sortFunction,
          sortable: this.props.sortable
        });

        DomHandler.clearSelection();
      }
    }
  }

  onFilterInput(e) {
    if (this.props.filter && this.props.onFilter) {
      if (this.filterTimeout) {
        clearTimeout(this.filterTimeout);
      }

      let filterValue = e.target.value;
      this.filterTimeout = setTimeout(() => {
        this.props.onFilter({
          value: filterValue,
          field: this.props.field,
          matchMode: this.props.filterMatchMode
        });
        this.filterTimeout = null;
      }, this.filterDelay);
    }
  }

  onResizerMouseDown(event) {
    if (this.props.resizableColumns && this.props.onColumnResizeStart) {
      this.props.onColumnResizeStart({
        originalEvent: event,
        columnEl: event.target.parentElement,
        columnProps: this.props
      });
    }
  }

  onMouseDown(event) {
    if (this.props.reorderableColumns) {
      if (event.target.nodeName !== 'INPUT') this.el.draggable = true;
      else if (event.target.nodeName === 'INPUT') this.el.draggable = false;
    }
  }

  onKeyDown(event) {
    if (event.key === 'Enter' && event.currentTarget === this.el) {
      this.onClick(event);
      event.preventDefault();
    }
  }

  getMultiSortMetaData() {
    if (this.props.multiSortMeta) {
      for (let i = 0; i < this.props.multiSortMeta.length; i++) {
        if (this.props.multiSortMeta[i].field === this.props.field) {
          return this.props.multiSortMeta[i];
        }
      }
    }

    return null;
  }

  renderSortIcon(sorted, sortOrder) {
    if (this.props.sortable) {
      let sortIcon = sorted ? (sortOrder < 0 ? 'pi-sort-down' : 'pi-sort-up') : 'pi-sort';
      let sortIconClassName = classNames('p-sortable-column-icon pi pi-fw', sortIcon);

      return <span className={sortIconClassName} />;
    } else {
      return null;
    }
  }

  render() {
    let multiSortMetaData = this.getMultiSortMetaData();
    let singleSorted =
      this.props.field === this.props.sortField ||
      (this.props.columnSortField != null && this.props.columnSortField === this.props.sortField);
    let multipleSorted = multiSortMetaData !== null;
    let sortOrder = 0;
    let resizer = this.props.resizableColumns && (
      <span className="p-column-resizer p-clickable" onMouseDown={this.onResizerMouseDown} />
    );
    let filterElement, headerCheckbox;

    if (singleSorted) sortOrder = this.props.sortOrder;
    else if (multipleSorted) sortOrder = multiSortMetaData.order;

    let sorted = this.props.sortable && (singleSorted || multipleSorted);
    let className = classNames(
      {
        'p-sortable-column': this.props.sortable,
        'p-highlight': sorted,
        'p-resizable-column': this.props.resizableColumns,
        'p-selection-column': this.props.selectionMode
      },
      this.props.headerClassName || this.props.className
    );

    let sortIconElement = this.renderSortIcon(sorted, sortOrder);
    if (this.props.filter) {
      const id = uniqueId();
      filterElement = this.props.filterElement || (
        <InputText
          className="p-column-filter"
          defaultValue={
            this.props.filters && this.props.filters[this.props.field]
              ? this.props.filters[this.props.field].value
              : null
          }
          id={`${this.props.field}_${id}_filter`}
          maxLength={this.props.filterMaxLength}
          name={`${this.props.field}_filter`}
          onInput={this.onFilterInput}
          placeholder={this.props.filterPlaceholder}
          type={this.props.filterType}
        />
      );
    }

    if (this.props.selectionMode === 'multiple') {
      headerCheckbox = (
        <RowCheckbox
          disabled={!this.props.value || this.props.value.length === 0}
          onClick={this.props.onHeaderCheckboxClick}
          selected={this.props.headerCheckboxSelected}
        />
      );
    }

    return (
      <th
        className={className}
        colSpan={this.props.colSpan}
        onClick={this.onClick}
        onDragLeave={this.props.onDragLeave}
        onDragOver={this.props.onDragOver}
        onDragStart={this.props.onDragStart}
        onDrop={this.props.onDrop}
        onKeyDown={this.onKeyDown}
        onMouseDown={this.onMouseDown}
        ref={el => (this.el = el)}
        rowSpan={this.props.rowSpan}
        style={this.props.headerStyle || this.props.style}
        tabIndex={this.props.sortable ? this.props.tabIndex : null}>
        {resizer}
        <span className="p-column-title">{this.props.header}</span>
        {sortIconElement}
        {filterElement}
        {headerCheckbox}
      </th>
    );
  }
}
