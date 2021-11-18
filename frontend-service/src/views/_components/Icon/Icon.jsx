import { config } from 'conf';

export const Icon = ({ className, icon, style, onClick, onMouseOut, onMouseOver }) => {
  return (
    <em
      className={`${config.icons[icon]} ${className}`}
      onClick={onClick}
      onMouseOut={onMouseOut}
      onMouseOver={onMouseOver}
      style={style}
    />
  );
};
