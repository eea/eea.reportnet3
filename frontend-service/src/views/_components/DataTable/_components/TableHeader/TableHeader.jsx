import { Component, Children } from 'react';
import { HeaderCell } from './_components/HeaderCell';

export class TableHeader extends Component {
  createHeaderCells(root) {
    let children = Children.toArray(root.props.children);

    return Children.map(children, column => (
      <HeaderCell
        key={column.key}
        {...column.props}
        columnSortField={column.props.sortField}
        filters={this.props.filters}
        headerCheckboxSelected={this.props.headerCheckboxSelected}
        multiSortMeta={this.props.multiSortMeta}
        onColumnResizeStart={this.props.onColumnResizeStart}
        onDragLeave={this.props.onColumnDragLeave}
        onDragOver={this.props.onColumnDragOver}
        onDragStart={this.props.onColumnDragStart}
        onDrop={this.props.onColumnDrop}
        onFilter={this.props.onFilter}
        onHeaderCheckboxClick={this.props.onHeaderCheckboxClick}
        onSort={this.props.onSort}
        reorderableColumns={this.props.reorderableColumns}
        resizableColumns={this.props.resizableColumns}
        sortField={this.props.sortField}
        sortOrder={this.props.sortOrder}
        tabIndex={this.props.tabIndex}
        value={this.props.value}
      />
    ));
  }

  render() {
    let content;
    if (this.props.columnGroup) {
      const rows = Children.toArray(this.props.columnGroup.props.children);
      content = rows.map((row, i) => {
        // eslint-disable-next-line react/no-array-index-key
        return <tr key={i}>{this.createHeaderCells(row)}</tr>;
      });
    } else {
      content = <tr>{this.createHeaderCells(this)}</tr>;
    }

    return <thead className="p-datatable-thead">{content}</thead>;
  }
}
