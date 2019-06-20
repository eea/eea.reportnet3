import React, { useContext } from 'react';
import ResourcesContext from '../../../Context/ResourcesContext';
import styles from './CustomIconToolTip.module.css';

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

  const parseTooltipMessage = () => {
    if(props.message!==null){
      let splittedMessage = props.message.split('\n');
      splittedMessage.pop();
      if(splittedMessage.length>1){
        return <ul>{splittedMessage.map((m,i)=><li key={i}>{m}</li>)}</ul>;
      }
      else{
        return props.message;
      }
    }
    return "";
  } 

  return (
    <span>
      <span className={styles.tooltip} style={{float:"right", whiteSpace: "pre-line"}}>
        <i className={validationIcon} style={{ color: iconColor, float: "right" }} />
        <span className={styles.tooltiptext}>{parseTooltipMessage()}</span>
      </span>
    </span>
  );
}
