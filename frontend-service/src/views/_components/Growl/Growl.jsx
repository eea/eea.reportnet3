import { Component } from 'react';

import PropTypes from 'prop-types';

import './Growl.scss';

import classNames from 'classnames';

import { CSSTransition, TransitionGroup } from 'react-transition-group';
import { GrowlMessage } from './_components/GrowlMessage';

import DomHandler from 'views/_functions/PrimeReact/DomHandler';

var messageIdx = 0;

export class Growl extends Component {
  static defaultProps = {
    baseZIndex: 0,
    className: null,
    closableOnClick: false,
    id: null,
    onClick: null,
    onRemove: null,
    position: 'topright',
    style: null
  };

  static propTypes = {
    baseZIndex: PropTypes.number,
    className: PropTypes.string,
    closableOnClick: PropTypes.bool,
    id: PropTypes.string,
    onClick: PropTypes.func,
    onRemove: PropTypes.func,
    position: PropTypes.string,
    style: PropTypes.object
  };

  constructor(props) {
    super(props);
    this.state = {
      messages: []
    };

    this.onClose = this.onClose.bind(this);
  }

  show(value) {
    if (value) {
      let newMessages;

      if (Array.isArray(value)) {
        for (let i = 0; i < value.length; i++) {
          value[i].id = messageIdx++;
          newMessages = [...this.state.messages, ...value];
        }
      } else {
        value.id = messageIdx++;
        newMessages = this.state.messages ? [...this.state.messages, value] : [value];
      }

      this.setState({
        messages: newMessages
      });

      this.container.style.zIndex = String(this.props.baseZIndex + DomHandler.generateZIndex());
    }
  }

  clear() {
    this.setState({
      messages: []
    });
  }

  onClose(message) {
    let newMessages = this.state.messages.filter(msg => msg.id !== message.id);
    this.setState({
      messages: newMessages
    });

    if (this.props.onRemove) {
      this.props.onRemove(message);
    }
  }

  render() {
    let className = classNames('p-growl p-component p-growl-' + this.props.position, this.props.className);

    return (
      <div
        className={className}
        id={this.props.id}
        ref={el => {
          this.container = el;
        }}
        style={this.props.style}>
        <TransitionGroup>
          {this.state.messages.map(message => (
            <CSSTransition classNames="p-growl" key={message.id} timeout={{ enter: 250, exit: 500 }}>
              <GrowlMessage
                closableOnClick={this.props.closableOnClick}
                message={message}
                onClick={this.props.onClick}
                onClose={this.onClose}
              />
            </CSSTransition>
          ))}
        </TransitionGroup>
      </div>
    );
  }
}
