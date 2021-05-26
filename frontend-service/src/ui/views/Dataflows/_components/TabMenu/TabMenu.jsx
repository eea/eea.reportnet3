import React, { Component } from 'react';
import PropTypes from 'prop-types';

import classNames from 'classnames';

// import { Ripple } from '../ripple/Ripple';
import DomHandler from 'ui/views/_functions/PrimeReact/DomHandler';
import ObjectUtils from 'ui/views/_functions/PrimeReact/ObjectUtils';

export class TabMenu extends Component {
  static defaultProps = {
    activeIndex: 0,
    className: null,
    id: null,
    model: null,
    onTabChange: null,
    style: null
  };

  static propTypes = {
    activeIndex: PropTypes.number,
    className: PropTypes.string,
    id: PropTypes.string,
    model: PropTypes.array,
    onTabChange: PropTypes.func,
    style: PropTypes.any
  };

  constructor(props) {
    super(props);

    if (!this.props.onTabChange) {
      this.state = {
        activeIndex: props.activeIndex
      };
    }
  }

  itemClick(event, item, index) {
    if (item.disabled) {
      event.preventDefault();
      return;
    }

    if (!item.url) {
      event.preventDefault();
    }

    if (item.command) {
      item.command({
        originalEvent: event,
        item: item
      });
    }

    if (this.props.onTabChange) {
      this.props.onTabChange({
        originalEvent: event,
        value: item,
        index
      });
    } else {
      this.setState({
        activeIndex: index
      });
    }
  }

  getActiveIndex() {
    return this.props.onTabChange ? this.props.activeIndex : this.state.activeIndex;
  }

  isSelected(index) {
    return index === (this.getActiveIndex() || 0);
  }

  updateInkBar() {
    const activeIndex = this.getActiveIndex();
    const tabHeader = this[`tab_${activeIndex}`];

    this.inkbar.style.width = DomHandler.getWidth(tabHeader) + 'px';
    this.inkbar.style.left = DomHandler.getOffset(tabHeader).left - DomHandler.getOffset(this.nav).left + 'px';
  }

  componentDidMount() {
    this.updateInkBar();
  }

  componentDidUpdate() {
    this.updateInkBar();
  }

  renderMenuItem(item, index) {
    const active = this.isSelected(index);
    const className = classNames(
      'p-tabmenuitem',
      { 'p-highlight': active, 'p-disabled': item.disabled },
      item.className
    );
    const iconClassName = classNames('p-menuitem-icon', item.icon);
    const icon = item.icon && <span className={iconClassName}></span>;
    const label = item.label && <span className="p-menuitem-text">{item.label}</span>;
    let content = (
      <a
        className="p-menuitem-link"
        href={item.url || '#'}
        onClick={event => this.itemClick(event, item, index)}
        role="presentation"
        target={item.target}>
        {icon}
        {label}
        {/* <Ripple /> */}
      </a>
    );

    if (item.template) {
      const defaultContentOptions = {
        onClick: event => this.itemClick(event, item),
        className: 'p-menuitem-link',
        labelClassName: 'p-menuitem-text',
        iconClassName,
        element: content,
        props: this.props,
        active,
        index
      };

      content = ObjectUtils.getJSXElement(item.template, item, defaultContentOptions);
    }

    return (
      <li
        ref={el => (this[`tab_${index}`] = el)}
        key={item.label + '_' + index}
        className={className}
        style={item.style}
        role="tab"
        aria-selected={active}
        aria-expanded={active}
        aria-disabled={item.disabled}>
        {content}
      </li>
    );
  }

  renderItems() {
    return this.props.model.map((item, index) => {
      return this.renderMenuItem(item, index);
    });
  }

  render() {
    if (this.props.model) {
      const className = classNames('p-tabmenu p-component', this.props.className);
      const items = this.renderItems();

      return (
        <div id={this.props.id} className={className} style={this.props.style}>
          <ul ref={el => (this.nav = el)} className="p-tabmenu-nav p-reset" role="tablist">
            {items}
            <li ref={el => (this.inkbar = el)} className="p-tabmenu-ink-bar"></li>
          </ul>
        </div>
      );
    }

    return null;
  }
}
