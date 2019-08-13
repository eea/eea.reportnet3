import React from 'react';
import { Button } from 'primereact/button';
function ButtonY({ label, icon, iconPos, onClick }) {
  return (
    <div>
      <Button label={label} icon={icon} iconPos={iconPos} />
    </div>
  );
}

export { ButtonY };
