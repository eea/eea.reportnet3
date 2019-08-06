import React from 'react';

import { CustomButton } from 'ui/views/_components/CustomButton';
import { Toolbar } from 'primereact/toolbar';

export const ButtonsBar = React.memo(({ buttonsList }) => {
  const leftButtons = [];
  const rightButtons = [];

  const buttons = buttonsList.forEach((b, i) => {
    b.group === 'left'
      ? leftButtons.push(
          <CustomButton
            label={b.label}
            title={b.title}
            icon={b.icon}
            key={i}
            onClick={b.onClick}
            disabled={b.disabled}
            ownButtonClasses={b.ownButtonClasses}
            iconClasses={b.iconClasses}
          />
        )
      : rightButtons.push(
          <CustomButton
            label={b.label}
            title={b.title}
            icon={b.icon}
            key={i}
            onClick={b.onClick}
            disabled={b.disabled}
            ownButtonClasses={b.ownButtonClasses}
            iconClasses={b.iconClasses}
          />
        );
  });

  return (
    <Toolbar>
      {buttons}
      <div className="p-toolbar-group-left">{leftButtons}</div>
      <div className="p-toolbar-group-right">{rightButtons}</div>
    </Toolbar>
  );
});
