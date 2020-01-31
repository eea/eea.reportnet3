import React, { useReducer } from 'react';
import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

const themeReducer = (state, { type, payload }) => {
  switch (type) {
    case 'TOGGLE_THEME':
      //   document.body.style.setProperty('--bg', '#282c35');
      return {
        ...state,
        currentTheme: payload.newTheme
      };
    default:
      return state;
  }
};

export const ThemeProvider = ({ children }) => {
  const [state, dispatch] = useReducer(themeReducer, {
    currentTheme: 'light',
    themes: {
      light: {
        bg: 'var(--white)',
        'main-font-color': 'var(--gray-110)',
        'breadCrumb-font-color': 'var(--c-corporate-blue)',
        'leftSideBar-bg': 'var(--c-corporate-blue)',
        'leftSideBar-box-shadow': '0 0 0 rgba(255, 255, 255, 0.1)',
        'header-box-shadow': '0 0 0.5rem rgba(0, 0, 0, 0.1)',
        'icon-title-color': 'var(--gray-110)',
        'title-color': 'var(--gray-110)',
        'subtitle-color': 'var(--c-corporate-blue)',
        'secondary-button-color': ' var(--gray-110)',
        'secondary-button-bg': 'var(--white)',
        'secondary-button-border': '1px solid var(--white)',
        'tabview-bg': 'var(--white)',
        'tabview-border': '1px solid var(--c-custom-gray)',
        'tabview-color': 'var(--gray-110)',
        'tabview-highlight-bg': 'var(--c-corporate-blue)',
        'tabview-highlight-border': '1px solid var(--c-corporate-blue)',
        'tabview-highlight-color': 'var(--white)',
        'inputswitch-checked': 'var(--black)',
        'datatable-header-bg': 'var(--gray-10)',
        'datatable-header-color': 'var(--gray-110)',
        'datatable-header-border': '1px solid var(--c-custom-gray)',
        'datatable-body-border': '1px solid var(--white)',
        'datatable-body-color': 'var(--c-custom-gray)',
        'datatable-body-bg': 'var(--white)'
      },
      dark: {
        bg: 'var(--c-darkest-blue)',
        'main-font-color': 'var(--white)',
        'breadCrumb-font-color': 'var(--white)',
        'leftSideBar-bg': 'var(--c-darkest-blue)',
        'leftSideBar-box-shadow': '0 0 0.5rem rgba(255, 255, 255, 0.1)',
        'header-box-shadow': '0 0 0.5rem rgba(255, 255, 255, 0.2)',
        'icon-title-color': 'var(--white)',
        'title-color': 'var(--white)',
        'subtitle-color': 'var(--c-corporate-blue)',
        'secondary-button-color': ' var(--white)',
        'secondary-button-bg': 'var(--c-darkest-blue)',
        'secondary-button-border': '1px solid var(--c-darkest-blue)',
        'tabview-bg': 'transparent',
        'tabview-border': '1px solid var(--white)',
        'tabview-color': 'var(--white)',
        'tabview-highlight-bg': 'var(--c-corporate-blue)',
        'tabview-highlight-border': '1px solid var(--white)',
        'tabview-highlight-color': 'var(--white)',
        'inputswitch-checked': 'var(--black)',
        'datatable-header-bg': 'var(--c-black-rose-500)',
        'datatable-header-color': 'var(--white)',
        'datatable-header-border': '1px solid var(--white)',
        'datatable-body-border': '1px solid var(--c-custom-gray)',
        'datatable-body-color': 'var(--white)',
        'datatable-body-bg': 'var(--c-darkest-blue)'
      }
    }
  });

  return (
    <ThemeContext.Provider
      value={{
        ...state,
        onToggleTheme: newTheme => {
          console.log({ newTheme });
          dispatch({
            type: 'TOGGLE_THEME',
            payload: {
              newTheme
            }
          });
          const theme = state.themes[newTheme];
          console.log({ theme });
          Object.keys(theme).forEach(key => {
            const cssKey = `--${key}`;
            const cssValue = theme[key];
            document.body.style.setProperty(cssKey, cssValue);
          });
        }
      }}>
      {children}
    </ThemeContext.Provider>
  );
};
