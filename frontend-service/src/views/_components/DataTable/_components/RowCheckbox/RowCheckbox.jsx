import PropTypes from 'prop-types';
import classNames from 'classnames';

export const RowCheckbox = props => {
  const onClick = event => {
    if (props.onClick && !props.disabled) {
      props.onClick({
        originalEvent: event,
        data: props.rowData,
        checked: props.selected
      });
    }
  };

  const className = classNames('p-checkbox-box p-component', {
    'p-highlight': props.selected,
    'p-disabled': props.disabled
  });
  const iconClassName = classNames('p-checkbox-icon p-clickable', { 'pi pi-check': props.selected });

  return (
    <div className="p-checkbox p-component">
      <div className="p-hidden-accessible">
        <input aria-label="Select all" type="checkbox" />
      </div>
      <div className={className} onClick={onClick}>
        <span className={iconClassName} />
      </div>
    </div>
  );
};

RowCheckbox.defaultProps = {
  disabled: false,
  onClick: null,
  rowData: null
};

RowCheckbox.propTypes = {
  disabled: PropTypes.bool,
  onClick: PropTypes.func,
  rowData: PropTypes.object
};
