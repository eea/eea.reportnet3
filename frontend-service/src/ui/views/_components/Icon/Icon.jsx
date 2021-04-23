import { config } from 'conf';

export const Icon = ({ className, icon, style, onClick, onMouseOver }) => {
  return (
    <i className={`${config.icons[icon]} ${className}`} style={style} onClick={onClick} onMouseOver={onMouseOver} />
  );
};
