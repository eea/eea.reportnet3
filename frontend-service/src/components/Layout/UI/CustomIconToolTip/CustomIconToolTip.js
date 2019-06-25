import React, { useContext } from 'react';
import ResourcesContext from '../../../Context/ResourcesContext';

import {Button} from 'primereact/button';
import style from './CustomIconToolTip.module.css';

export default function CustomIconToolTip(props) {
  let validationIcon = '';
  let iconColor = '';

  const resources = useContext(ResourcesContext);


  switch (props.levelError) {
    case 'WARNING':
      validationIcon = resources.icons['warning'];
      iconColor = '#ffd617';
      break;
    case 'ERROR':
      validationIcon = resources.icons['warning'];
      iconColor = '#da2131';
      break;
    case 'BLOCKER':
      validationIcon = resources.icons['banned'];
      iconColor = '#da2131';
      break;
    case '':
      validationIcon = '';
      iconColor = '';
      break;
    default:
      break;
  }
  const iconColorStyle = {  
    color: iconColor
  }
  

  return (
    <Button type="button" icon={validationIcon} tooltip={props.message} style={`${style.buttonCustom} ${iconColorStyle}`} /> 
  );
}
