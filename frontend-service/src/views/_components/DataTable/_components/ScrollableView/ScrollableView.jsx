import { Component } from 'react';
import isNull from 'lodash/isNull';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import DomHandler from 'views/_functions/PrimeReact/DomHandler';

export class ScrollableView extends Component {
  static defaultProps = {
    body: null,
    columns: null,
    footer: null,
    frozen: null,
    frozenBody: null,
    frozenWidth: null,
    header: null,
    loading: false,
    onVirtualScroll: null,
    rows: null,
    tableClassName: null,
    tableStyle: null,
    totalRecords: null,
    virtualRowHeight: null,
    virtualScroll: false
  };

  static propTypes = {
    body: PropTypes.any,
    columns: PropTypes.array,
    footer: PropTypes.any,
    frozen: PropTypes.bool,
    frozenBody: PropTypes.any,
    frozenWidth: PropTypes.string,
    header: PropTypes.any,
    loading: PropTypes.bool,
    onVirtualScroll: PropTypes.func,
    rows: PropTypes.number,
    tableClassName: PropTypes.string,
    tableStyle: PropTypes.any,
    totalRecords: PropTypes.number,
    virtualRowHeight: PropTypes.number,
    virtualScroll: PropTypes.bool
  };

  constructor(props) {
    super(props);
    this.onHeaderScroll = this.onHeaderScroll.bind(this);
    this.onBodyScroll = this.onBodyScroll.bind(this);
  }

  componentDidMount() {
    this.setScrollHeight();

    if (!this.props.frozen) {
      this.alignScrollBar();
    } else {
      this.scrollBody.style.paddingBottom = DomHandler.calculateScrollbarWidth() + 'px';
    }
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (this.props.scrollHeight !== prevProps.scrollHeight) {
      this.setScrollHeight();
    }

    if (!this.props.frozen) {
      this.alignScrollBar();

      if (this.props.virtualScroll) {
        this.virtualScroller.style.height = this.props.totalRecords * this.props.virtualRowHeight + 'px';
      }
    }

    if (this.virtualScrollCallback && !this.props.loading) {
      this.virtualScrollCallback();
      this.virtualScrollCallback = null;
    }
  }

  setScrollHeight() {
    if (this.props.scrollHeight) {
      if (this.props.scrollHeight.indexOf('%') !== -1) {
        let datatableContainer = this.findDataTableContainer(this.container);
        this.scrollBody.style.visibility = 'hidden';
        this.scrollBody.style.height = '100px'; //temporary height to calculate static height
        let containerHeight = DomHandler.getOuterHeight(datatableContainer);
        let relativeHeight =
          (DomHandler.getOuterHeight(datatableContainer.parentElement) * parseInt(this.props.scrollHeight, 10)) / 100;
        let staticHeight = containerHeight - 100; //total height of headers, footers, paginators
        let scrollBodyHeight = relativeHeight - staticHeight;

        this.scrollBody.style.height = 'auto';
        this.scrollBody.style.maxHeight = scrollBodyHeight + 'px';
        this.scrollBody.style.visibility = 'visible';
      } else {
        this.scrollBody.style.maxHeight = this.props.scrollHeight;
      }
    }
  }

  findDataTableContainer(element) {
    if (element) {
      let el = element;
      while (el && !DomHandler.hasClass(el, 'p-datatable')) {
        el = el.parentElement;
      }

      return el;
    } else {
      return null;
    }
  }

  onHeaderScroll() {
    this.scrollHeader.scrollLeft = 0;
  }

  onBodyScroll() {
    let frozenView = this.container.previousElementSibling;
    let frozenScrollBody;
    if (frozenView) {
      frozenScrollBody = DomHandler.findSingle(frozenView, '.p-datatable-scrollable-body');
    }

    this.scrollHeaderBox.style.marginLeft = -1 * this.scrollBody.scrollLeft + 'px';
    if (this.scrollFooterBox) {
      this.scrollFooterBox.style.marginLeft = -1 * this.scrollBody.scrollLeft + 'px';
    }

    if (frozenScrollBody) {
      frozenScrollBody.scrollTop = this.scrollBody.scrollTop;
    }

    if (this.props.virtualScroll) {
      let viewport = DomHandler.getClientHeight(this.scrollBody);
      let tableHeight = DomHandler.getOuterHeight(this.scrollTable);
      let pageHeight = this.props.virtualRowHeight * this.props.rows;
      let virtualTableHeight = DomHandler.getOuterHeight(this.virtualScroller);
      let pageCount = virtualTableHeight / pageHeight || 1;
      let scrollBodyTop = this.scrollTable.style.top || '0';

      if (
        this.scrollBody.scrollTop + viewport > parseFloat(scrollBodyTop) + tableHeight ||
        this.scrollBody.scrollTop < parseFloat(scrollBodyTop)
      ) {
        if (this.loadingTable) {
          this.loadingTable.style.display = 'table';
          this.loadingTable.style.top = this.scrollBody.scrollTop + 'px';
        }

        let page = Math.floor((this.scrollBody.scrollTop * pageCount) / this.scrollBody.scrollHeight) + 1;
        if (this.props.onVirtualScroll) {
          this.props.onVirtualScroll({
            page: page
          });

          this.virtualScrollCallback = () => {
            if (this.loadingTable) {
              this.loadingTable.style.display = 'none';
            }

            this.scrollTable.style.top = (page - 1) * pageHeight + 'px';
          };
        }
      }
    }
  }

  hasVerticalOverflow() {
    return DomHandler.getOuterHeight(this.scrollTable) > DomHandler.getOuterHeight(this.scrollBody);
  }

  alignScrollBar() {
    let scrollBarWidth = this.hasVerticalOverflow() ? DomHandler.calculateScrollbarWidth() : 0;

    this.scrollHeaderBox.style.marginRight = scrollBarWidth + 'px';
    if (this.scrollFooterBox) {
      this.scrollFooterBox.style.marginRight = scrollBarWidth + 'px';
    }
  }

  renderColGroup() {
    if (this.props.columns && this.props.columns.length) {
      return (
        <colgroup className="p-datatable-scrollable-colgroup">
          {this.props.columns.map(col => (
            <col key={col.props.field} style={col.props.headerStyle || col.props.style} />
          ))}
        </colgroup>
      );
    } else {
      return null;
    }
  }

  renderLoadingTable(colGroup) {
    if (this.props.virtualScroll) {
      return (
        <table
          className="p-datatable-scrollable-body-table p-datatable-loading-virtual-table p-datatable-virtual-table"
          ref={el => (this.loadingTable = el)}
          style={{ top: '0', display: 'none' }}>
          {colGroup}
          {this.props.loadingBody}
        </table>
      );
    } else {
      return null;
    }
  }

  render() {
    let className = classNames('p-datatable-scrollable-view', {
      'p-datatable-frozen-view': this.props.frozen,
      'p-datatable-unfrozen-view': !this.props.frozen && this.props.frozenWidth
    });
    let tableBodyClassName = classNames('p-datatable-scrollable-body-table', this.props.tableClassName, {
      'p-datatable-virtual-table': this.props.virtualScroll
    });
    let tableHeaderClassName = classNames('p-datatable-scrollable-header-table', this.props.tableClassName);
    let tableFooterClassName = classNames('p-datatable-scrollable-footer-table', this.props.tableClassName);
    let tableBodyStyle = Object.assign({ top: '0' }, this.props.tableStyle);
    let width = this.props.frozen ? this.props.frozenWidth : 'calc(100% - ' + this.props.frozenWidth + ')';
    let left = this.props.frozen ? null : this.props.frozenWidth;
    let colGroup = this.renderColGroup();
    let loadingTable = this.renderLoadingTable(colGroup);

    return (
      <div
        className={className}
        ref={el => {
          this.container = el;
        }}
        style={{ width: width, left: left }}>
        <div
          className="p-datatable-scrollable-header"
          onScroll={this.onHeaderScroll}
          ref={el => {
            this.scrollHeader = el;
          }}>
          <div
            className="p-datatable-scrollable-header-box"
            ref={el => {
              this.scrollHeaderBox = el;
            }}
            style={{
              overflow: this.props.totalRecords === 0 || isNull(this.props.totalRecords) ? 'auto' : 'hidden'
            }}>
            <table className={tableHeaderClassName} style={this.props.tableStyle} summary={this.props.header}>
              {colGroup}
              {this.props.header}
              {this.props.frozenBody}
            </table>
          </div>
        </div>
        <div
          className="p-datatable-scrollable-body"
          onScroll={this.onBodyScroll}
          ref={el => {
            this.scrollBody = el;
          }}>
          <table className={tableBodyClassName} ref={el => (this.scrollTable = el)} style={tableBodyStyle}>
            {colGroup}
            {this.props.body}
          </table>
          {loadingTable}
          <div
            className="p-datatable-virtual-scroller"
            ref={el => {
              this.virtualScroller = el;
            }}
          />
        </div>
        <div
          className="p-datatable-scrollable-footer"
          ref={el => {
            this.scrollFooter = el;
          }}>
          <div
            className="p-datatable-scrollable-footer-box"
            ref={el => {
              this.scrollFooterBox = el;
            }}>
            <table className={tableFooterClassName} style={this.props.tableStyle}>
              {colGroup}
              {this.props.footer}
            </table>
          </div>
        </div>
      </div>
    );
  }
}
