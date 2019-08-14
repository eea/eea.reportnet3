import React from 'react';

import { Button } from 'ui/views/_components/Button';
import { Toolbar } from 'primereact/toolbar';

export const ButtonsBar = React.memo(({ buttonsList }) => {
  const leftButtons = [];
  const rightButtons = [];

  const buttons = buttonsList.forEach((button, i) => {
    button.group === 'left'
      ? leftButtons.push(
          <Button
            label={button.label}
            title={button.title}
            icon={button.icon}
            key={i}
            onClick={button.onClick}
            disabled={button.disabled}
            className={`p-button-rounded p-button-secondary ${button.ownButtonClasses}`}
            iconClasses={button.iconClasses}
          />
        )
      : rightButtons.push(
          <Button
            label={button.label}
            title={button.title}
            icon={button.icon}
            key={i}
            onClick={button.onClick}
            disabled={button.disabled}
            className={`p-button-rounded p-button-secondary ${button.ownButtonClasses}`}
            iconClasses={button.iconClasses}
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
