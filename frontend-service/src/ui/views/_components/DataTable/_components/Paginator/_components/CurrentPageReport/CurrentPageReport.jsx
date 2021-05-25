import { Component } from 'react';
import PropTypes from 'prop-types';

export class CurrentPageReport extends Component {
  static defaultProps = {
    first: null,
    page: null,
    pageCount: null,
    rows: null,
    template: '({currentPage} of {totalPages})',
    totalRecords: null
  };

  static propTypes = {
    first: PropTypes.number,
    page: PropTypes.number,
    pageCount: PropTypes.number,
    rows: PropTypes.number,
    template: PropTypes.string,
    totalRecords: PropTypes.number
  };

  render() {
    let text = this.props.template
      .replace('{currentPage}', this.props.page + 1)
      .replace('{totalPages}', this.props.pageCount)
      .replace('{first}', this.props.first + 1)
      .replace('{last}', Math.min(this.props.first + this.props.rows, this.props.totalRecords))
      .replace('{rows}', this.props.rows)
      .replace('{totalRecords}', this.props.totalRecords);

    return <span className="p-paginator-current">{text}</span>;
  }
}
