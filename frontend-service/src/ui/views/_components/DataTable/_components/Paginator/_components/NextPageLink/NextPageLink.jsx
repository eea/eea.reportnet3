import { Component } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';

export class NextPageLink extends Component {
  static defaultProps = {
    disabled: false,
    onClick: null
  };

  static propTypes = {
    disabled: PropTypes.bool,
    onClick: PropTypes.func
  };

  render() {
    let className = classNames('p-paginator-next p-paginator-element p-link', { 'p-disabled': this.props.disabled });

    return (
      <button className={className} disabled={this.props.disabled} onClick={this.props.onClick} type="button">
        <span className="p-paginator-icon pi pi-caret-right"></span>
        <span className="srOnly">Next page</span>
      </button>
    );
  }
}
