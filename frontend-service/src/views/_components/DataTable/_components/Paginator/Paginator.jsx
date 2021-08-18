import { Component } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { FirstPageLink } from './_components/FirstPageLink';
import { NextPageLink } from './_components/NextPageLink';
import { PrevPageLink } from './_components/PrevPageLink';
import { LastPageLink } from './_components/LastPageLink';
import { PageLinks } from './_components/PageLinks';
import { RowsPerPageDropdown } from './_components/RowsPerPageDropdown';
import { CurrentPageReport } from './_components/CurrentPageReport';

export class Paginator extends Component {
  static defaultProps = {
    alwaysShow: true,
    className: null,
    currentPageReportTemplate: '({currentPage} of {totalPages})',
    first: 0,
    leftContent: null,
    onPageChange: null,
    pageLinkSize: 5,
    rightContent: null,
    rows: 0,
    rowsPerPageOptions: null,
    style: null,
    template: 'FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown',
    totalRecords: 0
  };

  static propTypes = {
    alwaysShow: PropTypes.bool,
    className: PropTypes.string,
    currentPageReportTemplate: PropTypes.any,
    first: PropTypes.number,
    leftContent: PropTypes.any,
    onPageChange: PropTypes.func,
    pageLinkSize: PropTypes.number,
    rightContent: PropTypes.any,
    rows: PropTypes.number,
    rowsPerPageOptions: PropTypes.array,
    style: PropTypes.object,
    template: PropTypes.string,
    totalRecords: PropTypes.number
  };

  constructor(props) {
    super(props);
    this.changePageToFirst = this.changePageToFirst.bind(this);
    this.changePageToPrev = this.changePageToPrev.bind(this);
    this.changePageToNext = this.changePageToNext.bind(this);
    this.changePageToLast = this.changePageToLast.bind(this);
    this.onRowsChange = this.onRowsChange.bind(this);
    this.onPageLinkClick = this.onPageLinkClick.bind(this);
  }

  isFirstPage() {
    return this.getPage() === 0;
  }

  isLastPage() {
    return this.getPage() === this.getPageCount() - 1;
  }

  getPageCount() {
    return Math.ceil(this.props.totalRecords / this.props.rows) || 1;
  }

  calculatePageLinkBoundaries() {
    var numberOfPages = this.getPageCount();
    var visiblePages = Math.min(this.props.pageLinkSize, numberOfPages);

    //calculate range, keep current in middle if necessary
    var start = Math.max(0, Math.ceil(this.getPage() - visiblePages / 2));
    var end = Math.min(numberOfPages - 1, start + visiblePages - 1);

    //check when approaching to last page
    var delta = this.props.pageLinkSize - (end - start + 1);
    start = Math.max(0, start - delta);

    return [start, end];
  }

  updatePageLinks() {
    var pageLinks = [];
    var boundaries = this.calculatePageLinkBoundaries();
    var start = boundaries[0];
    var end = boundaries[1];

    for (var i = start; i <= end; i++) {
      pageLinks.push(i + 1);
    }

    return pageLinks;
  }

  changePage(first, rows) {
    var pc = this.getPageCount();
    var p = Math.floor(first / rows);

    if (p >= 0 && p < pc) {
      var newPageState = {
        first: first,
        rows: rows,
        page: p,
        pageCount: pc
      };

      if (this.props.onPageChange) {
        this.props.onPageChange(newPageState);
      }
    }
  }

  getPage() {
    return Math.floor(this.props.first / this.props.rows);
  }

  changePageToFirst(event) {
    this.changePage(0, this.props.rows);
    event.preventDefault();
  }

  changePageToPrev(event) {
    this.changePage(this.props.first - this.props.rows, this.props.rows);
    event.preventDefault();
  }

  onPageLinkClick(event) {
    this.changePage((event.value - 1) * this.props.rows, this.props.rows);
  }

  changePageToNext(event) {
    this.changePage(this.props.first + this.props.rows, this.props.rows);
    event.preventDefault();
  }

  changePageToLast(event) {
    this.changePage((this.getPageCount() - 1) * this.props.rows, this.props.rows);
    event.preventDefault();
  }

  onRowsChange(event) {
    this.changePage(0, event.value);
  }

  componentDidUpdate(prevProps, prevState) {
    if (
      this.getPage() > 0 &&
      prevProps.totalRecords !== this.props.totalRecords &&
      this.props.first >= this.props.totalRecords
    ) {
      this.changePage((this.getPageCount() - 1) * this.props.rows, this.props.rows);
    }
  }

  render() {
    if (!this.props.alwaysShow && this.getPageCount() === 1) {
      return null;
    } else {
      let className = classNames('p-paginator p-component p-unselectable-text', this.props.className);

      let paginatorElements = this.props.template.split(' ').map(value => {
        let key = value.trim();
        let element;

        switch (key) {
          case 'FirstPageLink':
            element = <FirstPageLink disabled={this.isFirstPage()} key={key} onClick={this.changePageToFirst} />;
            break;

          case 'PrevPageLink':
            element = <PrevPageLink disabled={this.isFirstPage()} key={key} onClick={this.changePageToPrev} />;
            break;

          case 'NextPageLink':
            element = <NextPageLink disabled={this.isLastPage()} key={key} onClick={this.changePageToNext} />;
            break;

          case 'LastPageLink':
            element = <LastPageLink disabled={this.isLastPage()} key={key} onClick={this.changePageToLast} />;
            break;

          case 'PageLinks':
            element = (
              <PageLinks
                key={key}
                onClick={this.onPageLinkClick}
                page={this.getPage()}
                value={this.updatePageLinks()}
              />
            );
            break;

          case 'RowsPerPageDropdown':
            element = (
              <RowsPerPageDropdown
                key={key}
                onChange={this.onRowsChange}
                options={this.props.rowsPerPageOptions}
                value={this.props.rows}
              />
            );
            break;

          case 'CurrentPageReport':
            element = (
              <CurrentPageReport
                first={this.props.first}
                key={key}
                page={this.getPage()}
                pageCount={this.getPageCount()}
                rows={this.props.rows}
                template={this.props.currentPageReportTemplate}
                totalRecords={this.props.totalRecords}
              />
            );
            break;

          default:
            element = null;
            break;
        }

        return element;
      });

      let leftContent = this.props.leftContent && (
        <div className="p-paginator-left-content">{this.props.leftContent}</div>
      );
      let rightContent = this.props.rightContent && (
        <div className="p-paginator-right-content">{this.props.rightContent}</div>
      );

      return (
        <div className={className} style={this.props.style}>
          {leftContent}
          {paginatorElements}
          {rightContent}
        </div>
      );
    }
  }
}
