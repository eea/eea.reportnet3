import { Component } from 'react';
import uniqueId from 'lodash/uniqueId';

import PropTypes from 'prop-types';
import { Dropdown } from 'views/_components/Dropdown';

export class RowsPerPageDropdown extends Component {
  static defaultProps = {
    onChange: null,
    options: null,
    value: null
  };

  static propTypes = {
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
        <Dropdown
          appendTo={document.body}
          ariaLabel={'rowsPerPage'}
          name={uniqueId('rowsPerPage')}
          onChange={this.props.onChange}
          options={options}
          style={{ marginLeft: '0.75rem' }}
          value={this.props.value}
        />
      );
    } else {
      return null;
    }
  }
}
