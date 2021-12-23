import PropTypes from 'prop-types';

export const TabPanel = props => {
  return <div {...props}>{props.children}</div>;
};

TabPanel.propTypes = {
  contentClassName: PropTypes.string,
  contentStyle: PropTypes.object,
  description: PropTypes.string,
  disabled: PropTypes.bool,
  hasInfoTooltip: PropTypes.bool,
  header: PropTypes.string,
  headerClassName: PropTypes.string,
  headerStyle: PropTypes.object,
  leftIcon: PropTypes.string,
  rightIcon: PropTypes.string,
  tableSchemaId: PropTypes.string
};
