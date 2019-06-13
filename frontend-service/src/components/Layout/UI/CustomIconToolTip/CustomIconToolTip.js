import React, { useContext } from 'react';
import ResourcesContext from '../../../Context/ResourcesContext';
import ReactTooltip from 'react-tooltip';

//import './CustomIconToolTip.css';

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

  return (
    <div className='tooltip '>
      <span data-tip={props.message}>
        <i className={validationIcon} style={{ color: iconColor }} />
      </span>

      <ReactTooltip place='right' type='dark' effect='solid' />
    </div>
  );
}
