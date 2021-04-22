import { Component } from 'react';
import PropTypes from 'prop-types';
import { Dropdown } from 'ui/views/_components/Dropdown';

export class RowsPerPageDropdown extends Component {
  static defaultProps = {
    options: null,
    value: null,
    onChange: null
  };

  static propTypes = {
    options: PropTypes.array,
    value: PropTypes.number,
    onChange: PropTypes.func
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
          onChange={this.props.onChange}
          options={options}
          value={this.props.value}
        />
      );
    } else {
      return null;
    }
  }
}
