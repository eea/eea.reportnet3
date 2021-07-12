import { Component } from 'react';
import uniqueId from 'lodash/uniqueId';

import PropTypes from 'prop-types';
import classNames from 'classnames';

export class PrevPageLink extends Component {
  static defaultProps = {
    disabled: false,
    onClick: null
  };

  static propTypes = {
    disabled: PropTypes.bool,
    onClick: PropTypes.func
  };

  render() {
    let className = classNames('p-paginator-prev p-paginator-element p-link', { 'p-disabled': this.props.disabled });

    return (
      <button className={className} disabled={this.props.disabled} onClick={this.props.onClick} type="button">
        <span className="p-paginator-icon pi pi-caret-left" id={uniqueId('prevPage')}></span>
        <span className="srOnly" htmlFor="prevPage">
          Previous page
        </span>
      </button>
    );
  }
}
