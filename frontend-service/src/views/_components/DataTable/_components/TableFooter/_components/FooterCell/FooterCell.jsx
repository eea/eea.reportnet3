export const FooterCell = props => {
  const className = props.footerClassName || props.className;

  return (
    <td className={className} colSpan={props.colSpan} rowSpan={props.rowSpan} style={props.footerStyle || props.style}>
      {this.props.footer}
    </td>
  );
};
