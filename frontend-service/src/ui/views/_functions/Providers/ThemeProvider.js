import React, { useReducer } from 'react';
import { isNull } from 'lodash';
import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

const themeReducer = (state, { type, payload }) => {
  switch (type) {
    case 'TOGGLE_THEME':
      if (typeof Storage !== 'undefined') {
        localStorage.setItem('theme', payload.newTheme);
      }
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
    currentTheme: !isNull(window.localStorage.getItem('theme'))
      ? window.localStorage.getItem('theme')
      : window.localStorage.setItem('theme', 'light'),
    themes: {
      light: {
        bg: 'var(--white)',
        'main-font-color': 'var(--gray-110)',
        'breadCrumb-font-color': 'var(--c-corporate-blue)',
        'breadCrumb-icon-color': 'var(--c-corporate-blue)',
        'breadCrumb-chevron-color': 'var(--c-corporate-blue-lighter)',
        'leftSideBar-bg': 'var(--c-corporate-blue)',
        'leftSideBar-box-shadow': '0 5px 0.5rem rgba(0, 0, 0, 0.6)',
        'header-box-shadow': '0 0 0.5rem rgba(0, 0, 0, 0.1)',
        'title-icon-color': 'var(--gray-110)',
        'title-color': 'var(--gray-110)',
        'subtitle-color': 'var(--c-corporate-blue)',
        'secondary-button-color': 'var(--gray-110)',
        'secondary-button-bg': 'var(--white)',
        'secondary-button-border': '1px solid var(--white)',
        'tabview-bg': 'var(--gray-5)',
        'tabview-border': '1px solid var(--gray-25)',
        'tabview-color': 'var(--gray-110)',
        'tabview-highlight-bg': 'var(--bg)',
        'tabview-highlight-border': '1px solid var(--gray-25)',
        'tabview-highlight-border-bottom': '1px solid var(--bg)',
        'tabview-highlight-color': 'var(--c-corporate-blue)',
        'tabview-highlight-icon-color': 'var(--errors)',
        'tabview-highlight-bg-hover': 'var(--c-corporate-yellow)',
        'tabview-highlight-border-hover': '1px solid var(--gray-25)',
        'tabview-highlight-color-hover': 'var(--gray-110)',
        'tabmenu-highlight-bg': 'var(--bg)',
        'tabmenu-highlight-border': '1px solid var(--gray-25)',
        'tabmenu-highlight-border-bottom': '1px solid var(--bg)',
        'tabmenu-highlight-color': 'var(--c-corporate-blue)',
        'inputswitch-checked': 'var(--gray-25)',
        'inputswitch-checked-before-bg': 'var(--white)',
        'inputswitch-unchecked-bg': 'var(--gray-25)',
        'inputswitch-dark-theme-checked': 'inset 0px 0px var(--white)',
        'inputswitch-slider-bg': 'var(--white)',
        'datatable-header-bg': 'var(--gray-5)',
        'datatable-header-color': 'var(--gray-110)',
        'datatable-header-border': ' 1px solid var(--c-custom-gray)',
        'datatable-body-border': ' 1px solid var(--c-custom-gray)',
        'datatable-body-color': 'var(--black)',
        'datatable-body-bg': 'var(--white)',
        'datatable-body-even-bg': 'var(--gray-5)',
        'datatable-body-highlight-bg': 'var(--gray-10)',
        'datatable-column-sortable-hightlight-bg': 'var(--c-corporate-blue)',
        'datatable-column-sortable-hightlight-color': 'var(--white)',
        'datatable-column-sortable-hightlight-icon-color': 'var(--white)',
        'datatable-column-sortable-hightlight-box-shadow-focus': ' inset 0 0 0 0.1em var(--gray-25)',
        'datatable-header-footer-bg': 'var(--gray-10)',
        'datatable-header-footer-color': 'var(--gray-110)',
        'datatable-header-footer-border': ' 1px solid var(--gray-25)',
        'datatable-column-sortable-icon-color': 'var(--gray-110)',
        'datatable-column-sortable-hightlight-bg-hover': 'var(--c-corporate-yellow)',
        'datatable-column-sortable-hightlight-color-hover': 'var(--gray-110)',
        'datatable-column-sortable-icon-hightlight-color-hover': 'var(--gray-110)',
        'datatable-body-row-toggler-infotable-bg':
          ' repeating-linear-gradient(45deg,rgba(0, 0, 0, 0),rgba(0, 0, 0, 0) 10px,rgba(218, 33, 49, 0.3) 10px,rgba(218, 33, 49, 0.3) 15px) !important',
        'datatable-body-row-toggler-infotable-border': ' 1px solid var(--gray-10)',
        'datatable-body-row-toggler-color': 'var(--gray-10)',
        'datatable-body-row-toggler-color-hover': 'var(--gray-110)',
        'datatable-body-row-border': ' 1px solid rgba(0, 0, 0, 0)',
        'datatable-body-row-highlight-bg': 'rgba(240, 233, 145, 0.3)',
        'datatable-body-row-highlight-color': 'var(gray-110)',
        'datatable-body-row-toggler-hightlight-color': 'var(--c-corporate-blue)',
        'datatable-body-row-toggler-hightlight-color-hover': 'var(--white)',
        'datatable-body-row-contextmenu-hightlight-bg': 'var(--c-corporate-blue)',
        'datatable-body-row-contextmenu-hightlight-color': 'var(--white)',
        'datatable-column-resizer-bg': 'var(--c-corporate-blue)',
        'paginator-bg': 'var(--c-corporate-blue)',
        'paginator-border': ' 1px solid var(--blue-75)',
        'paginator-color': 'var(--white)',
        'paginator-bg-hover': 'var(--gray-10)',
        'paginator-color-hover': 'var(--gray-110)',
        'paginator-dropdown-label-color': 'var(--gray-110)',
        'paginator-dropdown-label-color-hover': 'var(--gray-110)',
        'paginator-pages-not-highlight-hover-bg': 'var(--white)',
        'paginator-pages-not-highlight-hover-color': 'var(--gray-110)',
        'paginator-pages-not-highlight-hover-border': ' 1px solid var(--c-corporate-blue)',
        'paginator-pages-highlight-box-shadow': ' inset 0 0 0 0.1em var(--gray-75)',
        'paginator-pages-highlight-bg': 'var(--white)',
        'paginator-pages-highlight-color': 'var(--black)',
        'paginator-pages-color': 'var(--white)',
        'dropdown-bg': 'var(--bg)',
        'dropdown-border': '1px solid var(--gray-50)',
        'dropdown-border-color-hover': 'var(--gray-75)',
        'dropdown-bg-hover': 'var(--white)',
        'dropdown-bg-focus': 'var(--c-corporate-blue)',
        'dropdown-trigger-bg': 'var(--white)',
        'dropdown-trigger-color': 'var(--gray-50)',
        'dropdown-trigger-clear-icon-color': 'var(--gray-50)',
        'dropdown-panel-border': '1px solid var(--gray-25)',
        'dropdown-panel-bg': 'var(--white)',
        'dropdown-panel-box-shadow': '0 3px 6px 0 rgba(0, 0, 0, 0.16)',
        'dropdown-panel-filter-border': '1px solid var(--gray-10)',
        'dropdown-panel-filter-color': 'var(--gray-110)',
        'dropdown-panel-filter-bg': 'var(--white)',
        'dropdown-panel-filter-icon-color': 'var(--c-corporate-blue)',
        'dropdown-panel-items-group-color': 'var(--gray-110)',
        'dropdown-panel-items-group-highlight-color': 'var(--white)',
        'dropdown-panel-items-group-highlight-bg': 'var(--c-corporate-blue)',
        'dropdown-panel-items-group-not-highlight-color-hover': 'var(--gray-110)',
        'dropdown-panel-items-group-not-highlight-bg-hover': 'var(--gray-10)',
        'floating-label-color': 'var(--gray-65)',
        'growl-box-shadow': '0 3px 6px 0 rgba(0, 0, 0, 0.3)',
        'growl-info-bg': 'var(--c-growl-info-blue)',
        'growl-info-color': 'var(--black)',
        'growl-info-icon-color': 'var(--black)',
        'growl-success-bg': 'var(--success-color-lighter)',
        'growl-success-color': 'var(--black)',
        'growl-success-icon-color': 'var(--black)',
        'growl-warning-bg': 'var(--c-corporate-yellow)',
        'growl-warning-color': 'var(--black)',
        'growl-warning-icon-color': 'var(--black)',
        'growl-error-bg': 'var(--c-pink)',
        'growl-error-color': 'var(--black)',
        'growl-error-icon-color': 'var(--black)',
        'inputtext-color': 'var(--main-font-color)',
        'inputtext-bg': 'var(--bg)',
        'inputtext-border': '1px solid var(--gray-25)',
        'inputtext-border-color-hover': 'var(--gray-75)',
        'inputtext-border-color-focus': 'var(--c-corporate-blue)',
        'inputtext-box-shadow-focus': '0 0 0 0.1em var(--c-corporate-blue)',
        'inputtext-placeholder': 'var(--black)',
        'treeview-expandable-color': 'var(--c-corporate-blue)',
        'treeview-property-title-color': 'var(--black)',
        'treeview-property-value-color': 'var(--black)',
        'treeview-empty-property-color': 'var(--black)',
        'treeview-table-icon-color': 'var(--black)',
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
        'button-primary-bg': 'var(--c-corporate-blue)',
        'button-primary-bg-hover': 'var(--blue-120)',
        'button-primary-bg-active': 'var(--blue-140)',
        'button-primary-border': ' 1px solid var(--c-corporate-blue)',
        'button-primary-border-color-hover': 'var(--blue-120)',
        'button-primary-border-color-active': 'var(--blue-140)',
        'button-primary-color': 'var(--white)',
        'button-primary-color-hover': 'var(--white)',
        'button-primary-color-icon': 'var(--white)',
        'button-primary-color-active': 'var(--white)',
        'button-primary-box-shadow-focus': ' 0 0 0 0.2em var(--c-blue-300)',
        'button-success-bg': 'var(--success-color)',
        'button-success-bg-hover': 'var(--success-color-dark)',
        'button-success-bg-active': 'var(--success-color-darker)',
        'button-success-border': ' 1px solid var(--success-color)',
        'button-success-border-color-hover': 'var(--success-color-dark)',
        'button-success-border-color-active': 'var(--success-color-darker)',
        'button-success-color': 'var(--white)',
        'button-success-color-hover': 'var(--white)',
        'button-success-color-icon': 'var(--white)',
        'button-success-color-active': 'var(--white)',
        'button-success-box-shadow-focus': ' 0 0 0 0.2em var(--success-color-light)',
        'button-secondary-bg': 'var(--gray-10)',
        'button-secondary-bg-hover': 'var(--gray-25)',
        'button-secondary-bg-active': 'var(--gray-50)',
        'button-secondary-border': ' 1px solid var(--gray-10)',
        'button-secondary-border-color-hover': 'var(--gray-25)',
        'button-secondary-border-color-active': 'var(--gray-25)',
        'button-secondary-color': 'var(--gray-110)',
        'button-secondary-color-hover': 'var(--gray-110)',
        'button-secondary-color-icon': 'var(--gray-110)',
        'button-secondary-color-active': 'var(--gray-110)',
        'button-secondary-box-shadow-focus': ' 0 0 0 0.2em var(--gray-25)',
        'button-secondary-transparent-bg': 'transparent',
        'button-secondary-transparent-bg-hover': 'var(--gray-25)',
        'button-secondary-transparent-bg-active': 'var(--gray-50)',
        'button-secondary-transparent-border': 'none',
        'button-secondary-transparent-border-color-hover': 'var(--gray-25)',
        'button-secondary-transparent-border-color-active': 'var(--gray-25)',
        'button-secondary-transparent-color': 'var(--gray-110)',
        'button-secondary-transparent-color-hover': 'var(--gray-110)',
        'button-secondary-transparent-color-icon': 'var(--gray-110)',
        'button-secondary-transparent-color-active': 'var(--gray-110)',
        'button-secondary-transparent-box-shadow-focus': ' 0 0 0 0.2em var(--gray-25)',
        'toolbar-border': '1px solid var(--gray-10)',
        'chart-bg': 'var(--bg)',
        'chart-color': 'var(--main-font-color)',
        'category-expandable-treeview-color': 'var(--main-font-color)',
        'codelist-expandable-treeview-color': 'var(--main-font-color)',
        'multiselect-bg': 'var(--white)',
        'multiselect-border': '1px solid var(--gray-15)',
        'multiselect-bg-hover': 'var(--gray-110)',
        'multiselect-label-color': 'var(--gray-110)',
        'multiselect-trigger-bg': 'var(--white)',
        'multiselect-trigger-color': 'var(--gray-50)',
        'multiselect-panel-border': '1px solid var(--gray-25)',
        'multiselect-panel-bg': 'var(--white)',
        'multiselect-panel-box-shadow': '0 3px 6px 0 rgba(0, 0, 0, 0.16)',
        'multiselect-panel-header-border-bottom': '1px solid var(--gray-10)',
        'multiselect-panel-header-color': 'var(--gray-110)',
        'multiselect-panel-header-bg': 'var(--white)',
        'multiselect-panel-header-filter-icon-color': 'var(--c-corporate-blue)',
        'multiselect-panel-header-close-color': 'var(--gray-25)',
        'multiselect-panel-item-color': 'var(--gray-110)',
        'multiselect-panel-item-color-hover': 'var(--gray-110)',
        'multiselect-panel-item-bg-hover': 'var(--gray-5)',
        'checkbox-box-border': '1px solid var(--gray-50)',
        'checkbox-box-bg': 'var(--white)',
        'checkbox-box-bg-hover': 'var(--black)',
        'checkbox-box-box-shadow-focus': '0 0 0 0.2em var(--c-corporate-blue)',
        'checkbox-box-bg-focus': 'var(--c-corporate-blue)',
        'checkbox-box-highlight-border-color': 'var(--c-corporate-blue)',
        'checkbox-box-highlight-bg': 'var(--c-corporate-blue)',
        'checkbox-box-highlight-color': 'var(--white)',
        'checkbox-box-highlight-border-color-hover': 'var(--c-corporate-blue-lighter)',
        'checkbox-box-highlight-bg-hover': 'var(--c-corporate-blue-lighter)',
        'checkbox-box-highlight-color-hover': 'var(--white)',
        'inputtextarea-box-shadow': '0 10px 6px -6px rgba(var(--c-corporate-blue-rgb), 0.2)',
        'field-designer-separator-box-shadow':
          '1px 1px rgba(var(--c-corporate-blue-rgb), 0.2), 2px 2px rgba(var(--c-corporate-blue-rgb), 0.2), 3px 3px rgba(var(--c-corporate-blue-rgb), 0.2)',
        'field-designer-separator-bg': 'rgba(var(--c-corporate-blue-rgb), 0.2)',
        'sidebar-bg': 'var(--white)',
        'sidebar-color': 'var(--gray-110)',
        'sidebar-border': '1px solid var(--gray-50)',
        'sidebar-boxshadow': '0 0 6px 0 rgba(0, 0, 0, 0.16)',
        'sidebar-scrollbar-color': 'var(--gray-75) var(--c-custom-gray)',
        'sidebar-scrollbar-bg': 'var(--c-custom-gray)',
        'sidebar-scrollbar-thumb-bg': 'var(--gray-50)',
        'sidebar-scrollbar-thumb-bg-hover': 'var(--gray-75)',
        'sidebar-close-icon-color': 'var(--gray-50)',
        'sidebar-close-icon-color-hover': 'var(--gray-110)',
        'sidebar-title-color': 'var(--c-corporate-blue)',
        'form-field-bg': 'var(--white)',
        'form-field-color': 'var(--black)',
        'hyperlink-color': 'var(--c-corporate-blue)',
        'hyperlink-color-hover': 'var(--c-corporate-blue-lighter)',
        'hyperlink-color-active': 'var(--c-corporate-blue)',
        'hyperlink-color-visited': 'var(--c-corporate-blue)',
        'drag-and-drop-arrow-color': 'var(--c-corporate-blue)',
        'drag-and-drop-arrow-opacity': '0.6',
        'drag-and-drop-div-border': '1px dashed var(--c-corporate-blue)',
        'drag-and-drop-div-opacity': '0.7',
        'chips-input-box-shadow': '0 0 0 0.2em var(--c-corporate-blue)',
        'chips-input-box-shadow-error': '0 0 0 0.2em var(--errors)',
        'chips-input-border-color': 'var(--c-corporate-blue)',
        'chips-input-border-color-hover': 'var(--c-corporate-blue)',
        'chips-input-color': 'var(--black)',
        'chips-token-bg': 'var(--c-corporate-blue)',
        'chips-token-color': 'var(--white)'
      },
      dark: {
        bg: 'var(--c-dark-blue)',
        'main-font-color': 'var(--white)',
        'breadCrumb-font-color': 'var(--white)',
        'breadCrumb-icon-color': 'var(--c-corporate-yellow)',
        'breadCrumb-chevron-color': 'var(--c-corporate-blue-lighter)',
        'leftSideBar-bg': 'var(--c-dark-blue)',
        'leftSideBar-box-shadow': '0 5px 0.5rem rgba(255, 255, 255, 0.1)',
        'header-box-shadow': '0 0 0.5rem rgba(255, 255, 255, 0.2)',
        'title-icon-color': 'var(--white)',
        'title-color': 'var(--white)',
        'subtitle-color': 'var(--c-corporate-yellow)',
        'secondary-button-color': 'var(--white)',
        'secondary-button-bg': 'var(--c-dark-blue)',
        'secondary-button-border': '1px solid var(--c-dark-blue)',
        'tabview-bg': 'var(--c-black-rose-500)',
        'tabview-border': '1px solid var(--gray-25)',
        'tabview-color': 'var(--white)',
        'tabview-highlight-bg': 'var(--bg)',
        'tabview-highlight-border': '1px solid var(--gray-25)',
        'tabview-highlight-border-bottom': '1px solid var(--bg)',
        'tabview-highlight-color': 'var(--c-corporate-yellow)',
        'tabview-highlight-icon-color': 'var(--errors)',
        'tabview-highlight-bg-hover': 'var(--c-corporate-yellow)',
        'tabview-highlight-border-hover': '1px solid var(--gray-25)',
        'tabview-highlight-color-hover': 'var(--gray-110)',
        'tabmenu-highlight-bg': 'var(--bg)',
        'tabmenu-highlight-border': '1px solid var(--gray-25)',
        'tabmenu-highlight-border-bottom': '1px solid var(--bg)',
        'tabmenu-highlight-color': 'var(--c-corporate-yellow)',
        'inputswitch-checked': 'var(--black)',
        'inputswitch-checked-before-bg': 'var(--white)',
        'inputswitch-unchecked-bg': 'var(--black)',
        'inputswitch-slider-bg': 'var(--white)',
        // 'inputswitch-dark-theme-checked': 'inset 4px 0px var(--white)',
        'datatable-header-bg': 'var(--c-black-rose-500)',
        'datatable-header-color': 'var(--white)',
        'datatable-header-border': '1px solid var(--white)',
        'datatable-body-bg': 'var(--c-dark-blue)',
        'datatable-body-even-bg': 'var(--c-darker-blue)',
        'datatable-body-highlight-bg': 'var(--gray-110)',
        'datatable-body-border': '1px solid var(--c-custom-gray)',
        'datatable-body-color': 'var(--white)',
        'datatable-column-sortable-hightlight-bg': 'var(--gray-50)',
        'datatable-column-sortable-hightlight-color': 'var(--white)',
        'datatable-column-sortable-hightlight-icon-color': 'var(--white)',
        'datatable-column-sortable-hightlight-box-shadow-focus': 'inset 0 0 0 0.1em var(--gray-25)',
        'datatable-header-footer-bg': 'var(--c-black-rose-500)',
        'datatable-header-footer-color': 'var(--gray-110)',
        'datatable-header-footer-border': '1px solid var(--gray-10)',
        'datatable-column-sortable-icon-color': 'var(--white)',
        'datatable-column-sortable-hightlight-bg-hover': 'var(--c-corporate-yellow)',
        'datatable-column-sortable-hightlight-color-hover': 'var(--gray-110)',
        'datatable-column-sortable-icon-hightlight-color-hover': 'var(--gray-110)',
        'datatable-body-row-toggler-infotable-bg':
          'repeating-linear-gradient(45deg, rgba(0, 0, 0, 0), rgba(0, 0, 0, 0) 10px, rgba(218, 33, 49, 0.3) 10px, rgba(218, 33, 49, 0.3) 15px) !important',
        'datatable-body-row-toggler-infotable-border': '1px solid var(--gray-10)',
        'datatable-body-row-toggler-color': 'var(--gray-10)',
        'datatable-body-row-toggler-color-hover': 'var(--gray-110)',
        'datatable-body-row-border': '1px solid rgba(0, 0, 0, 0)',
        'datatable-body-row-highlight-bg': 'rgba(0, 83, 132, 0.3)',
        'datatable-body-row-highlight-color': 'var(gray-110)',
        'datatable-body-row-toggler-hightlight-color': 'var(--c-corporate-blue)',
        'datatable-body-row-toggler-hightlight-color-hover': 'var(--white)',
        'datatable-body-row-contextmenu-hightlight-bg': 'var(--c-corporate-blue)',
        'datatable-body-row-contextmenu-hightlight-color': 'var(--white)',
        'datatable-column-resizer-bg': 'var(--c-corporate-blue)',
        'paginator-bg': 'var(--c-dark-blue)',
        'paginator-border': '1px solid var(--white)',
        'paginator-color': 'var(--white)',
        'paginator-bg-hover': 'var(--white)',
        'paginator-color-hover': 'var(--black)',
        'paginator-dropdown-label-color': 'var(--gray-10)',
        'paginator-dropdown-label-color-hover': 'var(--gray-10)',
        'paginator-pages-not-highlight-hover-bg': 'var(--white)',
        'paginator-pages-not-highlight-hover-color': 'var(--black)',
        'paginator-pages-not-highlight-hover-border': '1px solid var(--gray-75)',
        'paginator-pages-highlight-box-shadow': 'inset 0 0 0 0.1em var(--c-corporate-blue)',
        'paginator-pages-highlight-bg': 'var(--c-darkest-blue)',
        'paginator-pages-highlight-color': 'var(--white)',
        'paginator-pages-color': 'var(--white)',
        'paginator-right-content': 'var(--white)',
        'dropdown-bg': 'var(--bg)',
        'dropdown-border': '1px solid var(--gray-25)',
        'dropdown-border-color-hover': 'var(--white)',
        'dropdown-bg-hover': 'var(--white)',
        'dropdown-bg-focus': 'var(--white)',
        'dropdown-trigger-bg': 'var(--c-dark-blue)',
        'dropdown-trigger-color': 'var(--white)',
        'dropdown-trigger-clear-icon-color': 'var(--white)',
        'dropdown-panel-border': '1px solid var(--gray-10)',
        'dropdown-panel-bg': 'var(--c-dark-blue)',
        'dropdown-panel-box-shadow': '0 3px 6px 0 rgba(0, 0, 0, 0.16)',
        'dropdown-panel-filter-border': '1px solid var(--gray-10)',
        'dropdown-panel-filter-color': 'var(--gray-110)',
        'dropdown-panel-filter-bg': 'var(--white)',
        'dropdown-panel-filter-icon-color': 'var(--c-corporate-blue)',
        'dropdown-panel-items-group-color': 'var(--white)',
        'dropdown-panel-items-group-highlight-color': 'var(--white)',
        'dropdown-panel-items-group-highlight-bg': 'var(--c-corporate-blue)',
        'dropdown-panel-items-group-not-highlight-color-hover': 'var(--black)',
        'dropdown-panel-items-group-not-highlight-bg-hover': 'var(--white)',
        'floating-label-color': 'var(--white)',
        'growl-box-shadow': '0 3px 6px 0 rgba(255, 255, 255, 0.3)',
        'growl-info-bg': 'var(--c-growl-info-blue)',
        'growl-info-color': 'var(--black)',
        'growl-info-icon-color': 'var(--black)',
        'growl-success-bg': 'var(--success-color-lighter)',
        'growl-success-color': 'var(--black)',
        'growl-success-icon-color': 'var(--black)',
        'growl-warning-bg': 'var(--c-corporate-yellow)',
        'growl-warning-color': 'var(--black)',
        'growl-warning-icon-color': 'var(--black)',
        'growl-error-bg': 'var(--c-pink)',
        'growl-error-color': 'var(--black)',
        'growl-error-icon-color': 'var(--black)',
        'inputtext-color': 'var(--main-font-color)',
        'inputtext-bg': 'var(--bg)',
        'inputtext-border': '1px solid var(--gray-25)',
        'inputtext-border-color-hover': 'var(--white)',
        'inputtext-border-color-focus': 'var(--c-corporate-blue-lighter)',
        'inputtext-box-shadow-focus': '0 0 0 0.1em var(--c-corporate-blue-lighter)',
        'inputtext-placeholder': 'var(--white)',
        'treeview-expandable-color': 'var(--white)',
        'treeview-property-title-color': 'var(--c-corporate-yellow)',
        'treeview-property-value-color': 'var(--c-corporate-yellow)',
        'treeview-empty-property-color': 'var(--c-corporate-yellow)',
        'treeview-table-icon-color': 'var(--c-corporate-yellow)',
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
        'button-primary-bg': 'var(--gray-25)',
        'button-primary-bg-hover': 'var(--gray-75)',
        'button-primary-bg-active': 'var(--gray-110)',
        'button-primary-border': ' 1px solid var(--gray-25)',
        'button-primary-border-color-hover': 'var(--gray-75)',
        'button-primary-border-color-active': 'var(--gray-50)',
        'button-primary-color': 'var(--black)',
        'button-primary-color-hover': 'var(--white)',
        'button-primary-color-icon': 'var(--white)',
        'button-primary-color-active': 'var(--white)',
        'button-primary-box-shadow-focus': ' 0 0 0 0.2em var(--gray-75)',
        'button-success-bg': 'var(--success-color)',
        'button-success-bg-hover': 'var(--success-color-dark)',
        'button-success-bg-active': 'var(--success-color-darker)',
        'button-success-border': ' 1px solid var(--success-color)',
        'button-success-border-color-hover': 'var(--success-color-dark)',
        'button-success-border-color-active': 'var(--success-color-darker)',
        'button-success-color': 'var(--white)',
        'button-success-color-hover': 'var(--white)',
        'button-success-color-icon': 'var(--white)',
        'button-success-color-active': 'var(--white)',
        'button-success-box-shadow-focus': ' 0 0 0 0.2em var(--success-color-light)',
        'button-secondary-bg': 'var(--c-dark-blue)',
        'button-secondary-bg-hover': 'var(--gray-75)',
        'button-secondary-bg-active': 'var(--gray-110)',
        'button-secondary-border': ' none',
        'button-secondary-border-color-hover': 'var(--gray-75)',
        'button-secondary-color': 'var(--white)',
        'button-secondary-color-hover': 'var(--white)',
        'button-secondary-color-icon': 'var(--white)',
        'button-secondary-color-active': 'var(--white)',
        'button-secondary-box-shadow-focus': ' 0 0 0 0.2em var(--gray-25)',
        'button-secondary-border-color-active': 'var(--gray-25)',
        'button-secondary-transparent-bg': 'transparent',
        'button-secondary-transparent-bg-hover': 'var(--gray-75)',
        'button-secondary-transparent-bg-active': 'var(--gray-110)',
        'button-secondary-transparent-border': 'none',
        'button-secondary-transparent-border-color-hover': 'var(--gray-75)',
        'button-secondary-transparent-color': 'var(--white)',
        'button-secondary-transparent-color-hover': 'var(--white)',
        'button-secondary-transparent-color-icon': 'var(--white)',
        'button-secondary-transparent-color-active': 'var(--white)',
        'button-secondary-transparent-box-shadow-focus': ' 0 0 0 0.2em var(--gray-25)',
        'button-secondary-transparent-border-color-active': 'var(--gray-25)',
        'toolbar-border': '1px solid var(--c-dark-blue)',
        'chart-bg': 'var(--bg)',
        'chart-color': 'var(--main-font-color)',
        'category-expandable-treeview-color': 'var(--main-font-color)',
        'codelist-expandable-treeview-color': 'var(--main-font-color)',
        'multiselect-bg': 'var(--c-dark-blue)',
        'multiselect-border': '1px solid var(--gray-25)',
        'multiselect-bg-hover': 'var(--gray-15)',
        'multiselect-label-color': 'var(--white)',
        'multiselect-trigger-bg': 'var(--c-dark-blue)',
        'multiselect-trigger-color': 'var(--gray-50)',
        'multiselect-panel-border': '1px solid var(--gray-25)',
        'multiselect-panel-bg': 'var(--c-dark-blue)',
        'multiselect-panel-box-shadow': '0 3px 6px 0 rgba(0, 0, 0, 0.16)',
        'multiselect-panel-header-border-bottom': '1px solid var(--white)',
        'multiselect-panel-header-color': 'var(--gray-110)',
        'multiselect-panel-header-bg': 'var(--c-dark-blue)',
        'multiselect-panel-header-filter-icon-color': 'var(--white)',
        'multiselect-panel-header-close-color': 'var(--gray-50)',
        'multiselect-panel-header-close-color-hover': 'var(--gray-75)',
        'multiselect-panel-item-color': 'var(--white)',
        'multiselect-panel-item-color-hover': 'var(--white)',
        'multiselect-panel-item-bg-hover': 'var(--gray-110)',
        'checkbox-box-border': '1px solid var(--gray-50)',
        'checkbox-box-bg': 'var(--c-dark-blue)',
        'checkbox-box-bg-hover': 'var(--white)',
        'checkbox-box-box-shadow-focus': '0 0 0 0.1em var(--gray-75)',
        'checkbox-box-bg-focus': 'var(--gray-110)',
        'checkbox-box-highlight-border-color': 'var(--gray-75)',
        'checkbox-box-highlight-bg': 'var(--gray-75)',
        'checkbox-box-highlight-color': 'var(--white)',
        'checkbox-box-highlight-border-color-hover': 'var(--gray-110)',
        'checkbox-box-highlight-bg-hover': 'var(--gray-110)',
        'checkbox-box-highlight-color-hover': 'var(--white)',
        'inputtextarea-box-shadow': '0 10px 6px -6px rgba(255, 255, 255, 0.1)',
        'field-designer-separator-box-shadow':
          '1px 1px rgba(255, 255, 255, 0.2), 2px 2px rgba(255, 255, 255, 0.2), 3px 3px rgba(255, 255, 255, 0.2)',
        'field-designer-separator-bg': 'rgba(255, 255, 255, 0.4)',
        'sidebar-bg': 'var(--c-dark-blue)',
        'sidebar-color': 'var(--white)',
        'sidebar-border': '1px solid var(--gray-50)',
        'sidebar-boxshadow': '0 0 6px 0 rgba(0, 0, 0, 0.16)',
        'sidebar-scrollbar-color': 'var(--gray-75) var(--c-custom-gray)',
        'sidebar-scrollbar-bg': 'var(--c-custom-gray)',
        'sidebar-scrollbar-thumb-bg': 'var(--gray-50)',
        'sidebar-scrollbar-thumb-bg-hover': 'var(--gray-75)',
        'sidebar-close-icon-color': 'var(--gray-50)',
        'sidebar-close-icon-color-hover': 'var(--gray-110)',
        'sidebar-title-color': 'var(--c-corporate-yellow)',
        'form-field-bg': 'var(--c-dark-blue)',
        'form-field-color': 'var(--white)',
        'hyperlink-color': 'var(--c-corporate-yellow)',
        'hyperlink-color-hover': 'var(--c-corporate-yellow-lighter)',
        'hyperlink-color-active': 'var(--c-corporate-yellow)',
        'hyperlink-color-visited': 'var(--c-corporate-yellow)',
        'drag-and-drop-arrow-color': 'var(--c-corporate-yellow)',
        'drag-and-drop-arrow-opacity': '1',
        'drag-and-drop-div-border': '1px dashed var(--c-corporate-yellow)',
        'drag-and-drop-div-opacity': '0.7',
        'chips-input-box-shadow': '0 0 0 0.2em var(--c-corporate--lighter)',
        'chips-input-box-shadow-error': '0 0 0 0.2em var(--errors)',
        'chips-input-border-color': 'var(--c-corporate-blue-lighter)',
        'chips-input-border-color-hover': 'var(--c-corporate-blue-lighter)',
        'chips-input-color': 'var(--white)',
        'chips-token-bg': 'var(--c-corporate-blue-lighter)',
        'chips-token-color': 'var(--white)'
      }
    }
  });

  return (
    <ThemeContext.Provider
      value={{
        ...state,
        onToggleTheme: newTheme => {
          dispatch({
            type: 'TOGGLE_THEME',
            payload: {
              newTheme
            }
          });
          const theme = state.themes[newTheme];
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
