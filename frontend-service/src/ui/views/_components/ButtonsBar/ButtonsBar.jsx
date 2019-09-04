import React from 'react';

import { Button } from 'ui/views/_components/Button';
import { Toolbar } from 'ui/views/_components/Toolbar';

export const ButtonsBar = React.memo(({ buttonsList }) => {
  const leftButtons = [];
  const rightButtons = [];

  const buttons = buttonsList.forEach((button, i) => {
    button.group === 'left'
      ? leftButtons.push(
          <Button
            className={`p-button-rounded p-button-secondary ${button.ownButtonClasses}`}
            disabled={button.disabled}
            icon={button.icon}
            iconClasses={button.iconClasses}
            key={i}
            label={button.label}
            onClick={button.onClick}
            title={button.title}
          />
        )
      : rightButtons.push(
          <Button
            className={`p-button-rounded p-button-secondary ${button.ownButtonClasses}`}
            disabled={button.disabled}
            icon={button.icon}
            iconClasses={button.iconClasses}
            key={i}
            label={button.label}
            onClick={button.onClick}
            title={button.title}
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
