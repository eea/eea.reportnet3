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
        'inputswitch-checked': 'var(--gray-25)',
        'inputswitch-checked-before-bg': 'var(--gray-25)',
        'inputswitch-unchecked-bg': 'var(--gray-25)',
        'inputswitch-dark-theme-checked': 'inset 0px 0px var(--white)',
        'datatable-header-bg': 'var(--gray-10)',
        'datatable-header-color': 'var(--gray-110)',
        'datatable-header-border': '1px solid var(--c-custom-gray)',
        'datatable-body-bg': 'var(--white)',
        'datatable-body-even-bg': 'var(--gray-10)',
        'datatable-body-highlight-bg': 'var(--gray-25)',
        'datatable-body-border': '1px solid var(--c-custom-gray)',
        'datatable-body-color': 'var(--black)',
        'inputtext-color': 'var(--main-font-color)',
        'inputtext-bg': 'var(--bg)',
        'inputtext-border': '1px solid var(--gray-25)',
        'treeview-expandable-color': 'var(--c-corporate-blue)',
        'documenticon-color': 'var(--c-corporate-blue)',
        'dialog-header-bg': 'var(--gray-10)',
        'dialog-header-icon-color': 'var(--gray-75)',
        'dialog-header-icon-color-hover': 'var(--gray-110)',
        'dialog-content-bg': 'var(--white)',
        'dialog-footer-bg': 'var(--white)',
        'dialog-header-border': '1px solid var(--gray-25)',
        'dialog-content-border': '1px solid var(--gray-25)',
        'dialog-footer-border': '1px solid var(--gray-25)',
        'dialog-header-color': 'var(--black)',
        'dialog-content-color': 'var(--black)',
        'dialog-footer-color': 'var(--black)',
        'button-primary-bg': ' var(--c-corporate-blue)',
        'button-primary-bg-hover': ' var(--blue-120)',
        'button-primary-bg-active': ' var(--blue-140)',
        'button-primary-border': ' 1px solid var(--c-corporate-blue)',
        'button-primary-border-color-hover': ' var(--blue-120)',
        'button-primary-border-color-active': ' var(--blue-140)',
        'button-primary-color': ' var(--white)',
        'button-primary-color-hover': ' var(--white)',
        'button-primary-color-icon': ' var(--white)',
        'button-primary-color-active': ' var(--white)',
        'button-primary-box-shadow-focus': ' 0 0 0 0.2em var(--c-blue-300)',
        'button-success-bg': ' var(--success-color)',
        'button-success-bg-hover': ' var(--success-color-dark)',
        'button-success-bg-active': ' var(--success-color-darker)',
        'button-success-border': ' 1px solid var(--success-color)',
        'button-success-border-color-hover': ' var(--success-color-dark)',
        'button-success-border-color-active': ' var(--success-color-darker)',
        'button-success-color': ' var(--white)',
        'button-success-color-hover': ' var(--white)',
        'button-success-color-icon': ' var(--white)',
        'button-success-color-active': ' var(--white)',
        'button-success-box-shadow-focus': ' 0 0 0 0.2em var(--success-color-light)',
        'button-secondary-bg': ' var(--gray-10)',
        'button-secondary-bg-hover': ' var(--gray-25)',
        'button-secondary-bg-active': ' var(--gray-50)',
        'button-secondary-border': ' 1px solid var(--gray-10)',
        'button-secondary-border-color-hover': ' var(--gray-25)',
        'button-secondary-border-color-active': ' var(--gray-25)',
        'button-secondary-color': ' var(--gray-110)',
        'button-secondary-color-hover': ' var(--gray-110)',
        'button-secondary-color-icon': ' var(--gray-110)',
        'button-secondary-color-active': ' var(--gray-110)',
        'button-secondary-box-shadow-focus': ' 0 0 0 0.2em var(--gray-25)'
      },
      dark: {
        bg: 'var(--c-dark-blue)',
        'main-font-color': 'var(--white)',
        'breadCrumb-font-color': 'var(--white)',
        'leftSideBar-bg': 'var(--c-dark-blue)',
        'leftSideBar-box-shadow': '0 0 0.5rem rgba(255, 255, 255, 0.1)',
        'header-box-shadow': '0 0 0.5rem rgba(255, 255, 255, 0.2)',
        'icon-title-color': 'var(--white)',
        'title-color': 'var(--white)',
        'subtitle-color': 'var(--c-corporate-blue)',
        'secondary-button-color': ' var(--white)',
        'secondary-button-bg': 'var(--c-dark-blue)',
        'secondary-button-border': '1px solid var(--c-dark-blue)',
        'tabview-bg': 'transparent',
        'tabview-border': '1px solid var(--white)',
        'tabview-color': 'var(--white)',
        'tabview-highlight-bg': 'var(--c-corporate-blue)',
        'tabview-highlight-border': '1px solid var(--white)',
        'tabview-highlight-color': 'var(--white)',
        'inputswitch-checked': 'var(--black)',
        'inputswitch-checked-before-bg': 'var(--white)',
        'inputswitch-unchecked-bg': 'var(--black)',
        // 'inputswitch-dark-theme-checked': 'inset 4px 0px var(--white)',
        'datatable-header-bg': 'var(--c-black-rose-500)',
        'datatable-header-color': 'var(--white)',
        'datatable-header-border': '1px solid var(--white)',
        'datatable-body-bg': 'var(--c-dark-blue)',
        'datatable-body-even-bg': 'var(--c-darker-blue)',
        'datatable-body-highlight-bg': 'var(--gray-110)',
        'datatable-body-border': '1px solid var(--c-custom-gray)',
        'datatable-body-color': 'var(--white)',
        'inputtext-color': 'var(--main-font-color)',
        'inputtext-bg': 'var(--bg)',
        'inputtext-border': '1px solid var(--white)',
        'treeview-expandable-color': 'var(--white)',
        'documenticon-color': 'var(--white)',
        'dialog-header-bg': 'var(--c-black-rose-500)',
        'dialog-header-icon-color': 'var(--gray-25)',
        'dialog-header-icon-color-hover': 'var(--white)',
        'dialog-content-bg': 'var(--c-dark-blue)',
        'dialog-footer-bg': 'var(--c-dark-blue)',
        'dialog-header-border': '1px solid var(--white)',
        'dialog-content-border': '1px solid var(--white)',
        'dialog-footer-border': '1px solid var(--white)',
        'dialog-header-color': 'var(--white)',
        'dialog-content-color': 'var(--white)',
        'dialog-footer-color': 'var(--white)',
        'button-primary-bg': ' var(--gray-25)',
        'button-primary-bg-hover': ' var(--gray-75)',
        'button-primary-bg-active': ' var(--gray-110)',
        'button-primary-border': ' 1px solid var(--gray-25)',
        'button-primary-border-color-hover': ' var(--gray-75)',
        'button-primary-border-color-active': ' var(--gray-50)',
        'button-primary-color': ' var(--black)',
        'button-primary-color-hover': ' var(--white)',
        'button-primary-color-icon': ' var(--white)',
        'button-primary-color-active': ' var(--white)',
        'button-primary-box-shadow-focus': ' 0 0 0 0.2em var(--gray-75)',
        'button-success-bg': ' var(--success-color)',
        'button-success-bg-hover': ' var(--success-color-dark)',
        'button-success-bg-active': ' var(--success-color-darker)',
        'button-success-border': ' 1px solid var(--success-color)',
        'button-success-border-color-hover': ' var(--success-color-dark)',
        'button-success-border-color-active': ' var(--success-color-darker)',
        'button-success-color': ' var(--white)',
        'button-success-color-hover': ' var(--white)',
        'button-success-color-icon': ' var(--white)',
        'button-success-color-active': ' var(--white)',
        'button-success-box-shadow-focus': ' 0 0 0 0.2em var(--success-color-light)',
        'button-secondary-bg': ' var(--c-dark-blue)',
        'button-secondary-bg-hover': ' var(--gray-75)',
        'button-secondary-bg-active': ' var(--gray-110)',
        'button-secondary-border': ' none',
        'button-secondary-border-color-hover': ' var(--gray-75)',
        'button-secondary-color': ' var(--white)',
        'button-secondary-color-hover': ' var(--white)',
        'button-secondary-color-icon': ' var(--white)',
        'button-secondary-color-active': ' var(--white)',
        'button-secondary-box-shadow-focus': ' 0 0 0 0.2em var(--gray-25)',
        'button-secondary-border-color-active': 'var(--gray-25)'
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
