import { Component } from 'react';
import uniqueId from 'lodash/uniqueId';

import PropTypes from 'prop-types';
import { Dropdown } from 'views/_components/Dropdown';

export class RowsPerPageDropdown extends Component {
  static defaultProps = {
    hasLabel: false,
    label: 'Rows per page',
    onChange: null,
    options: null,
    value: null
  };

  static propTypes = {
    hasLabel: PropTypes.bool,
    label: PropTypes.string,
    onChange: PropTypes.func,
    options: PropTypes.array,
    value: PropTypes.number
  };

  render() {
    if (this.props.options) {
      let options = this.props.options.map((opt, i) => {
        return { label: String(opt), value: opt };
      });
      return (
        <div>
          {this.props.hasLabel && <label>{this.props.label}</label>}
          <Dropdown
            appendTo={document.body}
            ariaLabel={'rowsPerPage'}
            name={uniqueId('rowsPerPage')}
            onChange={this.props.onChange}
            options={options}
            value={this.props.value}
          />
        </div>
      );
    } else {
      return null;
    }
  }
}
