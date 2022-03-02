import { Component, Fragment } from 'react';
import PropTypes from 'prop-types';

import classNames from 'classnames';

import styles from './Paginator.module.scss';

import { FirstPageLink } from './_components/FirstPageLink';
import { NextPageLink } from './_components/NextPageLink';
import { PrevPageLink } from './_components/PrevPageLink';
import { LastPageLink } from './_components/LastPageLink';
import { PageLinks } from './_components/PageLinks';
import { RowsPerPageDropdown } from './_components/RowsPerPageDropdown';
import { CurrentPageReport } from './_components/CurrentPageReport';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export class Paginator extends Component {
  static defaultProps = {
    alwaysShow: true,
    areComponentsVisible: true,
    className: null,
    currentPageReportTemplate: '({currentPage} of {totalPages})',
    disabled: false,
    first: 0,
    isDataflowsList: false,
    leftContent: null,
    onPageChange: null,
    pageLinkSize: 5,
    rightContent: null,
    rows: 0,
    rowsPerPageOptions: null,
    style: null,
    template: 'FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink',
    totalRecords: 0
  };

  static propTypes = {
    alwaysShow: PropTypes.bool,
    areComponentsVisible: PropTypes.bool,
    className: PropTypes.string,
    currentPageReportTemplate: PropTypes.any,
    disabled: PropTypes.bool,
    first: PropTypes.number,
    isDataflowsList: PropTypes.bool,
    leftContent: PropTypes.any,
    onPageChange: PropTypes.func,
    pageLinkSize: PropTypes.number,
    rightContent: PropTypes.any,
    rows: PropTypes.number,
    rowsPerPageOptions: PropTypes.array,
    style: PropTypes.object,
    template: PropTypes.any,
    totalRecords: PropTypes.number
  };

  static contextType = ResourcesContext;

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
        currentPage: p + 1,
        first: first,
        rows: rows,
        page: p,
        pageCount: pc,
        pageInputTooltip:
          p >= 0 && p < pc
            ? this.context.messages['currentPageInfoMessage']
            : `${this.context.messages['currentPageErrorMessage']} ${Math.ceil(this.props.totalRecords / rows)}`
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

  componentDidUpdate(prevProps) {
    if (
      this.getPage() > 0 &&
      prevProps.totalRecords !== this.props.totalRecords &&
      this.props.first >= this.props.totalRecords
    ) {
      this.changePage((this.getPageCount() - 1) * this.props.rows, this.props.rows);
    }
  }

  renderElement(key, template) {
    switch (key) {
      case 'FirstPageLink':
        return (
          <FirstPageLink
            disabled={this.isFirstPage() || this.props.disabled}
            key={key}
            onClick={this.changePageToFirst}
          />
        );
      case 'PrevPageLink':
        return (
          <PrevPageLink
            disabled={this.isFirstPage() || this.props.disabled}
            key={key}
            onClick={this.changePageToPrev}
          />
        );
      case 'NextPageLink':
        return (
          <NextPageLink disabled={this.isLastPage() || this.props.disabled} key={key} onClick={this.changePageToNext} />
        );
      case 'LastPageLink':
        return (
          <LastPageLink disabled={this.isLastPage() || this.props.disabled} key={key} onClick={this.changePageToLast} />
        );
      case 'PageLinks':
        return (
          <PageLinks
            disabled={this.props.disabled}
            isDataflowsList={this.props.isDataflowsList}
            key={key}
            onClick={this.onPageLinkClick}
            page={this.getPage()}
            value={this.updatePageLinks()}
          />
        );
      case 'CurrentPageReport':
        return (
          <CurrentPageReport
            first={this.props.first}
            key={key}
            page={this.getPage()}
            pageCount={this.getPageCount()}
            reportTemplate={this.props.currentPageReportTemplate}
            rows={this.props.rows}
            template={template}
            totalRecords={this.props.totalRecords}
          />
        );
      default:
        return null;
    }
  }

  renderElements() {
    const template = this.props.template;

    if (template) {
      if (typeof template === 'object') {
        return template.layout
          ? template.layout.split(' ').map(value => {
              const key = value.trim();
              return this.renderElement(key, template[key]);
            })
          : Object.entries(template).map(([key, _template]) => {
              return this.renderElement(key, _template);
            });
      }

      return template.split(' ').map(value => {
        return this.renderElement(value.trim());
      });
    }

    return null;
  }

  renderRowsPerPageDropdown() {
    return (
      <div className="p-paginator-left-content-rowsPerPage">
        <RowsPerPageDropdown
          disabled={this.props.disabled}
          key="RowsPerPageDropdown"
          label={this.context.messages['rowsPerPage']}
          onChange={this.onRowsChange}
          options={this.props.rowsPerPageOptions}
          value={this.props.rows}
        />
        {this.props.leftContent && <div className="p-paginator-left-content">{this.props.leftContent}</div>}
      </div>
    );
  }

  render() {
    if (!this.props.alwaysShow && this.getPageCount() === 1) {
      return null;
    }

    return (
      <div
        className={classNames(
          'p-paginator p-component p-unselectable-text',
          this.props.className,
          `${!this.props.areComponentsVisible && styles.paginatorContainer} `
        )}
        style={this.props.style}>
        {this.props.areComponentsVisible && (
          <Fragment>
            {this.renderRowsPerPageDropdown()}
            <div className="p-paginator-middle-content">{this.renderElements()}</div>
          </Fragment>
        )}
        <div>
          {this.props.rightContent && <div className="p-paginator-right-content">{this.props.rightContent}</div>}
        </div>
      </div>
    );
  }
}
