import { Component } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';

export class PageLinks extends Component {
  static defaultProps = {
    disabled: false,
    links: null,
    page: null,
    value: null
  };

  static propTypes = {
    disabled: PropTypes.bool,
    onClick: PropTypes.func,
    page: PropTypes.number,
    value: PropTypes.array
  };

  onPageLinkClick(event, pageLink) {
    if (this.props.onClick) {
      this.props.onClick({
        originalEvent: event,
        value: pageLink
      });
    }

    event.preventDefault();
  }

  render() {
    const elements = this.props.value.map(pageLink => (
      <button
        className={classNames('p-paginator-page p-paginator-element p-link', {
          'p-highlight': pageLink - 1 === this.props.page
        })}
        disabled={this.props.disabled}
        key={pageLink}
        onClick={e => this.onPageLinkClick(e, pageLink)}
        style={{ opacity: this.props.disabled ? '0.6' : '1', pointerEvents: this.props.disabled ? 'none' : 'auto' }}
        type="button">
        {pageLink}
      </button>
    ));

    return <span className="p-paginator-pages">{elements}</span>;
  }
}
