import React, { Component } from 'react';
import PropTypes from 'prop-types';

import './GrowlMessage.scss';

import classNames from 'classnames';

import DomHandler from 'ui/views/_functions/PrimeReact/DomHandler';

export class GrowlMessage extends Component {
  static defaultProps = {
    closableOnClick: false,
    message: null,
    onClick: null,
    onClose: null
  };

  static propTypes = {
    closableOnClick: PropTypes.bool,
    message: PropTypes.object,
    onClose: PropTypes.func,
    onClick: PropTypes.func
  };

  constructor(props) {
    super(props);

    this.onClick = this.onClick.bind(this);
    this.onClose = this.onClose.bind(this);
  }

  componentWillUnmount() {
    if (this.timeout) {
      clearTimeout(this.timeout);
    }
  }

  componentDidMount() {
    if (!this.props.message.sticky) {
      this.timeout = setTimeout(() => {
        this.onClose(null);
      }, this.props.message.life || 3000);
    }
  }

  onClose() {
    if (this.timeout) {
      clearTimeout(this.timeout);
    }

    if (this.props.onClose) {
      this.props.onClose(this.props.message);
    }
  }

  onClick(event) {
    if (
      this.props.onClick &&
      !(
        DomHandler.hasClass(event.target, 'p-growl-icon-close') ||
        DomHandler.hasClass(event.target, 'p-growl-icon-close-icon')
      )
    ) {
      this.props.onClick(this.props.message);
    }
    if (this.props.closableOnClick) {
      this.onClose();
    }
  }

  renderCloseIcon() {
    if (this.props.message.closable !== false) {
      return (
        <button className="p-growl-icon-close p-link" onClick={this.onClose} type="button">
          <span className="p-growl-icon-close-icon pi pi-times"></span>
        </button>
      );
    } else {
      return null;
    }
  }

  render() {
    let className = classNames('p-growl-item-container p-highlight', {
      'p-growl-message-info': this.props.message.severity === 'info',
      'p-growl-message-warn': this.props.message.severity === 'warn',
      'p-growl-message-error': this.props.message.severity === 'error',
      'p-growl-message-success': this.props.message.severity === 'success'
    });

    let iconClassName = classNames('p-growl-image pi', {
      'pi-info-circle': this.props.message.severity === 'info',
      'pi-exclamation-triangle': this.props.message.severity === 'warn',
      'pi-times-circle': this.props.message.severity === 'error',
      'pi-check': this.props.message.severity === 'success'
    });

    let titleClassName = classNames('p-growl-title', {
      'p-growl-title-info': this.props.message.severity === 'info',
      'p-growl-title-warn': this.props.message.severity === 'warn',
      'p-growl-title-error': this.props.message.severity === 'error',
      'p-growl-title-success': this.props.message.severity === 'success'
    });

    let closeIcon = this.renderCloseIcon();

    return (
      <div
        aria-live="polite"
        className={className}
        onClick={this.onClick}
        ref={el => {
          this.element = el;
        }}>
        <div aria-atomic="true" aria-live="assertive" className="p-growl-item" role="alert">
          {closeIcon}
          <span className={iconClassName} onClick={this.onClose}></span>
          <div className="p-growl-message">
            <span className={titleClassName}>{this.props.message.summary}</span>
            {this.props.message.detail && <div className="p-growl-details">{this.props.message.detail}</div>}
          </div>
        </div>
      </div>
    );
  }
}
