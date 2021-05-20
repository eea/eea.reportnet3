import PropTypes from 'prop-types';
import classNames from 'classnames';

export const RowRadioButton = props => {
  const onClick = event => {
    if (props.onClick) {
      props.onClick({
        originalEvent: event,
        data: props.rowData
      });
    }
  };

  let className = classNames('p-radiobutton-box p-component p-radiobutton-relative', { 'p-highlight': props.selected });
  let iconClassName = classNames('p-radiobutton-icon p-clickable', { 'pi pi-circle-on': props.selected });

  return (
    <div className="p-radiobutton p-component">
      <div className="p-hidden-accessible">
        <input type="radio" />
      </div>
      <div className={className} onClick={onClick}>
        <span className={iconClassName} />
      </div>
    </div>
  );
};

RowRadioButton.defaultProps = {
  onClick: null,
  rowData: null,
  selected: false
};

RowRadioButton.propTypes = {
  rowData: PropTypes.object,
  onClick: PropTypes.func,
  selected: PropTypes.bool
};
