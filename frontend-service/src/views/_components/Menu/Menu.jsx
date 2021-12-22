import { Component, Fragment } from 'react';
import PropTypes from 'prop-types';

import classNames from 'classnames';

import styles from './Menu.module.scss';

import { MenuItem } from './_components/MenuItem/MenuItem';

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
    const items = submenu.items.map((item, index) => <MenuItem index={index} item={item} key={item.label} />);

    return (
      <Fragment key={submenu.label + '_' + index}>
        <li className={className} role="presentation" style={submenu.style}>
          {submenu.label}
        </li>
        {items}
      </Fragment>
    );
  }

  renderItem(item, index) {
    if (item.items) return this.renderSubMenu(item, index);
    else return <MenuItem index={index} item={item} key={item.label} />;
  }

  render() {
    return (
      <div
        className={`${styles.dropDownMenu} ${this.props.className} p-menu-overlay-visible p-menu`}
        style={this.state.style}>
        <ul className="p-menu-list p-reset">{this.props.model.map((item, index) => this.renderItem(item, index))}</ul>
      </div>
    );
  }
}

export { Menu };
