import { Component } from 'react';
import uniqueId from 'lodash/uniqueId';

import PropTypes from 'prop-types';
import classNames from 'classnames';

export class LastPageLink extends Component {
  static defaultProps = {
    disabled: false,
    onClick: null
  };

  static propTypes = {
    disabled: PropTypes.bool,
    onClick: PropTypes.func
  };

  render() {
    const className = classNames('p-paginator-last p-paginator-element p-link', { 'p-disabled': this.props.disabled });

    return (
      <button className={className} disabled={this.props.disabled} onClick={this.props.onClick} type="button">
        <span className="p-paginator-icon pi pi-step-forward" id={uniqueId('lastPage')}></span>
        <span className="srOnly" htmlFor="lastPage">
          Last page
        </span>
      </button>
    );
  }
}
