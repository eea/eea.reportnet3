import PropTypes from 'prop-types';
import classNames from 'classnames';
import DomHandler from 'views/_functions/PrimeReact/DomHandler';

export const ListBoxItem = ({
  disabled = false,
  option = null,
  label = null,
  selected = false,
  tabIndex = null,
  onClick = null,
  onTouchEnd = null,
  template = null
}) => {
  const onClickListBoxItem = event => {
    if (onClick) {
      onClick({
        originalEvent: event,
        option
      });
    }

    event.preventDefault();
  };

  const onTouchEndListBoxItem = event => {
    if (onTouchEnd) {
      onTouchEnd({
        originalEvent: event,
        option
      });
    }
  };

  const onKeyDownListBoxItem = event => {
    const item = event.currentTarget;

    switch (event.which) {
      //down
      case 40:
        var nextItem = findNextItem(item);
        if (nextItem) {
          nextItem.focus();
        }
        event.preventDefault();
        break;
      //up
      case 38:
        var prevItem = findPrevItem(item);
        if (prevItem) {
          prevItem.focus();
        }
        event.preventDefault();
        break;
      //enter
      case 13:
        onClickListBoxItem(event);
        event.preventDefault();
        break;
      default:
        break;
    }
  };

  const findNextItem = item => {
    const nextItem = item.nextElementSibling;
    if (nextItem) {
      return DomHandler.hasClass(nextItem, 'p-disabled') ? findNextItem(nextItem) : nextItem;
    } else {
      return null;
    }
  };

  const findPrevItem = item => {
    const prevItem = item.previousElementSibling;
    if (prevItem) {
      return DomHandler.hasClass(prevItem, 'p-disabled') ? findPrevItem(prevItem) : prevItem;
    } else {
      return null;
    }
  };

  const renderListBoxItem = () => {
    const className = classNames('p-listbox-item', { 'p-highlight': selected }, { 'p-disabled': disabled });
    const content = template ? template(option) : label;

    return (
      <li
        aria-label={label}
        aria-selected={selected}
        className={className}
        key={label}
        onClick={onClickListBoxItem}
        onKeyDown={onKeyDownListBoxItem}
        onTouchEnd={onTouchEndListBoxItem}
        role="option"
        tabIndex={tabIndex}>
        {content}
      </li>
    );
  };

  return renderListBoxItem();
};

ListBoxItem.propTypes = {
  option: PropTypes.any,
  label: PropTypes.string,
  selected: PropTypes.bool,
  tabIndex: PropTypes.string,
  onClick: PropTypes.func,
  onTouchEnd: PropTypes.func,
  template: PropTypes.func
};
