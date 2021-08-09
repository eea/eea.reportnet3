import { Component } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import ReactDOM from 'react-dom';
import DomHandler from 'views/_functions/PrimeReact/DomHandler';

class ContextMenuSub extends Component {
  static defaultProps = {
    model: null,
    onLeafClick: null,
    resetMenu: false,
    root: false
  };

  static propTypes = {
    model: PropTypes.any,
    onLeafClick: PropTypes.func,
    resetMenu: PropTypes.bool,
    root: PropTypes.bool
  };

  constructor(props) {
    super(props);
    this.state = {
      activeItem: null
    };
  }

  static getDerivedStateFromProps(nextProps, prevState) {
    if (nextProps.resetMenu === true) {
      return {
        activeItem: null
      };
    }

    return null;
  }

  onItemMouseEnter(event, item) {
    if (item.disabled) {
      event.preventDefault();
      return;
    }

    this.setState({
      activeItem: item
    });
  }

  onItemClick(event, item) {
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

    if (!item.items) {
      this.props.onLeafClick(event);
    }
  }

  componentDidUpdate() {
    if (this.element.offsetParent) {
      this.position();
    }
  }

  position() {
    const parentItem = this.element.parentElement;
    const containerOffset = DomHandler.getOffset(this.element.parentElement);
    const viewport = DomHandler.getViewport();
    const sublistWidth = this.element.offsetParent
      ? this.element.offsetWidth
      : DomHandler.getHiddenElementOuterWidth(this.element);
    const itemOuterWidth = DomHandler.getOuterWidth(parentItem.children[0]);
    this.element.style.top = '0px';

    if (
      parseInt(containerOffset.left, 10) + itemOuterWidth + sublistWidth >
      viewport.width - DomHandler.calculateScrollbarWidth()
    ) {
      this.element.style.left = -1 * sublistWidth + 'px';
    } else {
      this.element.style.left = itemOuterWidth + 'px';
    }
  }

  renderSeparator(index) {
    return <li className="p-menu-separator" key={'separator_' + index}></li>;
  }

  renderIcon(item) {
    const className = classNames('p-menuitem-icon', item.icon);
    if (item.icon) {
      return <span className={className}></span>;
    } else {
      return null;
    }
  }

  renderSubmenuIcon(item) {
    if (item.items) {
      return <span className="p-submenu-icon pi pi-fw pi-caret-right"></span>;
    } else {
      return null;
    }
  }

  renderSubmenu(item) {
    if (item.items) {
      return (
        <ContextMenuSub
          model={item.items}
          onLeafClick={this.props.onLeafClick}
          resetMenu={item !== this.state.activeItem}
        />
      );
    } else {
      return null;
    }
  }

  renderMenuitem(item, index) {
    const className = classNames(
      'p-menuitem',
      { 'p-menuitem-active': this.state.activeItem === item, 'p-disabled': item.disabled },
      item.className
    );
    const icon = this.renderIcon(item);
    const submenuIcon = this.renderSubmenuIcon(item);
    const submenu = this.renderSubmenu(item);

    return (
      <li
        className={className}
        key={item.label + '_' + index}
        onMouseEnter={event => this.onItemMouseEnter(event, item)}
        style={item.style}>
        <a
          className="p-menuitem-link"
          href={item.url || '#'}
          onClick={event => this.onItemClick(event, item, index)}
          target={item.target}>
          {icon}
          <span className="p-menuitem-text">{item.label}</span>
          {submenuIcon}
        </a>
        {submenu}
      </li>
    );
  }

  renderItem(item, index) {
    if (item.separator) return this.renderSeparator(index);
    else return this.renderMenuitem(item, index);
  }

  renderMenu() {
    if (this.props.model) {
      return this.props.model.map((item, index) => {
        return this.renderItem(item, index);
      });
    } else {
      return null;
    }
  }

  render() {
    const className = classNames({ 'p-submenu-list': !this.props.root });
    const submenu = this.renderMenu();

    return (
      <ul className={className} ref={el => (this.element = el)}>
        {submenu}
      </ul>
    );
  }
}

// eslint-disable-next-line react/no-multi-comp
export class ContextMenu extends Component {
  static defaultProps = {
    appendTo: null,
    autoZIndex: true,
    baseZIndex: 0,
    className: null,
    global: false,
    id: null,
    model: null,
    onHide: null,
    onShow: null,
    style: null
  };

  static propTypes = {
    appendTo: PropTypes.any,
    autoZIndex: PropTypes.bool,
    baseZIndex: PropTypes.number,
    className: PropTypes.string,
    global: PropTypes.bool,
    id: PropTypes.string,
    model: PropTypes.array,
    onHide: PropTypes.func,
    onShow: PropTypes.func,
    style: PropTypes.object
  };

  constructor(props) {
    super();
    this.state = {
      resetMenu: false
    };
    this.onMenuClick = this.onMenuClick.bind(this);
    this.onLeafClick = this.onLeafClick.bind(this);
    this.onMenuMouseEnter = this.onMenuMouseEnter.bind(this);
  }

  componentDidMount() {
    this.bindDocumentClickListener();

    if (this.props.global) {
      this.bindDocumentContextMenuListener();
    }
  }

  onMenuClick() {
    this.selfClick = true;

    this.setState({
      resetMenu: false
    });
  }

  onMenuMouseEnter() {
    this.setState({
      resetMenu: false
    });
  }

  show(event) {
    this.container.style.display = 'block';
    this.position(event);
    if (this.props.autoZIndex) {
      this.container.style.zIndex = String(this.props.baseZIndex + DomHandler.generateZIndex());
    }
    DomHandler.fadeIn(this.container, 250);

    this.bindDocumentResizeListener();

    if (this.props.onShow) {
      this.props.onShow(event);
    }

    event.stopPropagation();
    event.preventDefault();
  }

  hide(event) {
    if (this.container) {
      this.container.style.display = 'none';
    }

    if (this.props.onHide) {
      this.props.onHide(event);
    }

    this.unbindDocumentResizeListener();
  }

  position(event) {
    if (event) {
      let left = event.pageX + 1;
      let top = event.pageY + 1;
      let width = this.container.offsetParent
        ? this.container.offsetWidth
        : DomHandler.getHiddenElementOuterWidth(this.container);
      let height = this.container.offsetParent
        ? this.container.offsetHeight
        : DomHandler.getHiddenElementOuterHeight(this.container);
      let viewport = DomHandler.getViewport();

      //flip
      if (left + width - document.body.scrollLeft > viewport.width) {
        left -= width;
      }

      if (top + height - document.body.scrollTop > viewport.height) {
        top -= height;
      }

      //fit
      if (left < document.body.scrollLeft) {
        left = document.body.scrollLeft;
      }

      if (top < document.body.scrollTop) {
        top = document.body.scrollTop;
      }

      this.container.style.left = left - 50 + 'px';
      this.container.style.top = top - 183 + 'px';
    }
  }

  onLeafClick(event) {
    this.setState({
      resetMenu: true
    });

    event.stopPropagation();
  }

  bindDocumentClickListener() {
    if (!this.documentClickListener) {
      this.documentClickListener = event => {
        if (!this.selfClick && event.button !== 2) {
          this.hide(event);

          this.setState({
            resetMenu: true
          });
        }

        this.selfClick = false;
      };

      document.addEventListener('click', this.documentClickListener);
    }
  }

  bindDocumentContextMenuListener() {
    if (!this.documentContextMenuListener) {
      this.documentContextMenuListener = event => {
        this.show(event);
      };

      document.addEventListener('contextmenu', this.documentContextMenuListener);
    }
  }

  bindDocumentResizeListener() {
    if (!this.documentResizeListener) {
      this.documentResizeListener = event => {
        if (this.container.offsetParent) {
          this.hide(event);
        }
      };

      window.addEventListener('resize', this.documentResizeListener);
    }
  }

  unbindDocumentClickListener() {
    if (this.documentClickListener) {
      document.removeEventListener('click', this.documentClickListener);
      this.documentClickListener = null;
    }
  }

  unbindDocumentContextMenuListener() {
    if (this.documentContextMenuListener) {
      document.removeEventListener('contextmenu', this.documentContextMenuListener);
      this.documentContextMenuListener = null;
    }
  }

  unbindDocumentResizeListener() {
    if (this.documentResizeListener) {
      window.removeEventListener('resize', this.documentResizeListener);
      this.documentResizeListener = null;
    }
  }

  componentWillUnmount() {
    this.unbindDocumentClickListener();
    this.unbindDocumentResizeListener();
    this.unbindDocumentContextMenuListener();
  }

  renderContextMenu() {
    const className = classNames('p-contextmenu p-component', this.props.className);

    return (
      <div
        className={className}
        id={this.props.id}
        onClick={this.onMenuClick}
        onMouseEnter={this.onMenuMouseEnter}
        ref={el => (this.container = el)}
        style={this.props.style}>
        <ContextMenuSub
          model={this.props.model}
          onLeafClick={this.onLeafClick}
          resetMenu={this.state.resetMenu}
          root={true}
        />
      </div>
    );
  }

  render() {
    const element = this.renderContextMenu();

    if (this.props.appendTo) return ReactDOM.createPortal(element, this.props.appendTo);
    else return element;
  }
}
