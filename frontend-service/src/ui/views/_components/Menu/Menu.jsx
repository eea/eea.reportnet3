import { Component, Fragment } from 'react';
import PropTypes from 'prop-types';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import classNames from 'classnames';
import isNil from 'lodash/isNil';

import styles from './Menu.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

class Menu extends Component {
  static defaultProps = {
    className: '',
    model: []
  };

  static propTypes = {
    className: PropTypes.string,
    model: PropTypes.array
  };

  constructor(props) {
    super(props);
    this.state = { style: { display: 'none' }, menuClick: false };

    ['show', 'hide'].map(item => (this[item] = this[item].bind(this)));
  }

  getMenuPosition(event) {
    const menu = event.currentTarget.nextElementSibling;
    const button = event.currentTarget;
    const left = `${button.offsetLeft}px`;
    const topValue = button.offsetHeight + button.offsetTop + 3;
    const top = `${topValue}px `;
    menu.style.left = left;
    menu.style.top = top;
    const menuLeft = left;
    const menuTop = top;

    return { menuLeft, menuTop };
  }

  hide() {
    if (!this.state.menuClick) {
      this.setState(
        state => ({ ...state, style: { ...state.style, display: 'none' } }),
        () => {
          document.removeEventListener('click', this.hide, false);
        }
      );
    } else {
      this.setState(state => ({ ...state, menuClick: false }));
    }
  }

  show(event) {
    const { menuLeft, menuTop } = this.getMenuPosition(event);

    this.setState(
      state => ({ ...state, style: { ...state.style, display: 'block' } }),
      () => {
        document.addEventListener('click', this.hide);
        this.setState(
          state => ({ ...state, style: { ...state.style, bottom: `-${menuTop}px`, left: `${menuLeft}px` } }),
          () => {
            setTimeout(() => {
              this.setState(state => ({ ...state, style: { ...state.style, opacity: 1 } }));
            }, 50);
          }
        );
      }
    );
  }

  renderSubMenu(submenu, index) {
    const className = classNames('p-submenu-header', { 'p-disabled': submenu.disabled }, submenu.className);
    const items = submenu.items.map((item, index) => this.renderMenuitem(item, index));

    return (
      <Fragment key={submenu.label + '_' + index}>
        <li className={className} role="presentation" style={submenu.style}>
          {submenu.label}
        </li>
        {items}
      </Fragment>
    );
  }

  renderMenuitem(item, index) {
    return (
      <li className={'p-menuitem'} key={index}>
        <span
          className={`p-menuitem-link ${item.disabled ? styles.menuItemDisabled : null}`}
          disabled={item.disabled}
          onClick={e => {
            e.preventDefault();
            if (!item.disabled) {
              item.command();
            } else {
              this.setState(state => ({ ...state, menuClick: true }));
            }
          }}>
          {!isNil(item.icon) && <FontAwesomeIcon icon={AwesomeIcons(item.icon)} role="presentation" />}
          <span>{item.label}</span>
        </span>
      </li>
    );
  }

  renderItem(item, index) {
    if (item.items) return this.renderSubMenu(item, index);
    else return this.renderMenuitem(item, index);
  }

  render() {
    return (
      <div
        className={`${styles.dropDownMenu} ${this.props.className} p-menu-overlay-visible p-menu`}
        style={this.state.style}>
        <ul className={'p-menu-list p-reset'}>{this.props.model.map((item, index) => this.renderItem(item, index))}</ul>
      </div>
    );
  }
}

export { Menu };
