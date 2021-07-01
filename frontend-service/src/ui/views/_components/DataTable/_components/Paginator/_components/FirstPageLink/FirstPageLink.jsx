import { Component } from 'react';
import uniqueId from 'lodash/uniqueId';

import PropTypes from 'prop-types';
import classNames from 'classnames';

export class FirstPageLink extends Component {
  static defaultProps = {
    disabled: false,
    onClick: null
  };

  static propTypes = {
    disabled: PropTypes.bool,
    onClick: PropTypes.func
  };

  render() {
    let className = classNames('p-paginator-first p-paginator-element p-link', { 'p-disabled': this.props.disabled });

    return (
      <button className={className} disabled={this.props.disabled} onClick={this.props.onClick} type="button">
        <span className="p-paginator-icon pi pi-step-backward" id={uniqueId('firstPage')}></span>
        <span className="srOnly" htmlFor="firstPage">
          First page
        </span>
      </button>
    );
  }
}
