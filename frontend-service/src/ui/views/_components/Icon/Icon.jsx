import { config } from 'conf';

export const Icon = ({ className, icon, style, onClick, onMouseOver }) => {
  return (
    <i className={`${config.icons[icon]} ${className}`} onClick={onClick} onMouseOver={onMouseOver} style={style} />
  );
};
