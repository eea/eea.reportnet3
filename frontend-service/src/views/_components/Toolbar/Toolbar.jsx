import { memo, Children } from 'react';
import { Toolbar as PrimeToolbar } from 'primereact/toolbar';

export const Toolbar = memo(function Toolbar({className = '', id, style, children}) {
  const [left, right] = Children.toArray(children);

  return (
    <PrimeToolbar
      className={`${className} datasetSchema-toolbar-dataset-data-help-step`}
      id={id}
      style={style}
      left={left}
      right={right}
    />
  );
});