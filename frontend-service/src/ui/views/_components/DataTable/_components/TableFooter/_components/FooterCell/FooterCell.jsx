export const FooterCell = props => {
  let className = props.footerClassName || props.className;

  return (
    <td className={className} style={props.footerStyle || props.style} colSpan={props.colSpan} rowSpan={props.rowSpan}>
      {this.props.footer}
    </td>
  );
};
