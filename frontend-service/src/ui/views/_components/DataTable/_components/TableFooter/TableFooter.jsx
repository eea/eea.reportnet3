import { Component, Children } from 'react';
import { FooterCell } from './_components/FooterCell';

export class TableFooter extends Component {
  createFooterCells(root, column, i) {
    let children = Children.toArray(root.props.children);

    return Children.map(children, (column, i) => {
      return <FooterCell key={i} {...column.props} />;
    });
  }

  render() {
    let content;
    if (this.props.columnGroup) {
      let rows = Children.toArray(this.props.columnGroup.props.children);
      content = rows.map((row, i) => {
        return <tr key={i}>{this.createFooterCells(row)}</tr>;
      });
    } else {
      content = <tr>{this.createFooterCells(this)}</tr>;
    }

    return <tfoot className="p-datatable-tfoot">{content}</tfoot>;
  }
}
