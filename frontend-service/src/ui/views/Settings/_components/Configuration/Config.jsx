import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';
import React , {useState,useContext} from 'react';
import styles from './Config.module.scss';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const Config = (props)=>{
    const [isEditionModeOn, setIsEditionModeOn] = useState(false);
    const themeContext = useContext(ThemeContext);
    const resources = useContext(ResourcesContext);
    return(
        
    <div className = {props.shouldHide}>
        <h1>User Configuration</h1>
        <div>
        <h2>Confirmation Logout</h2>
            
      <InputSwitch 
      checked={themeContext.currentTheme === 'dark'}
      onChange={e => themeContext.onToggleTheme(e.value ? 'dark' : 'light')}
      sliderCheckedClassName={styles.themeSwitcherInputSwitch}
      style={{ marginRight: '1rem' }}
      tooltip={
        themeContext.currentTheme === 'light'
          ? resources.messages['toggleDarkTheme']
          : resources.messages['toggleLightTheme']
      }/>
      </div>

      <h2>Number of lines per page</h2>

    </div>
    );}

    // checked={isEditionModeOn}
    //   onChange={e => {
    //     setIsEditionModeOn(e.value);
    //   }}
    //   /></div>


   