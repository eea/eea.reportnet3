import {
  faAlignJustify,
  faAlignRight,
  faAngleDoubleLeft,
  faAngleDoubleRight,
  faAngleDoubleUp,
  faAngleDown,
  faAngleUp,
  faAngleRight,
  faArchive,
  faArrowDown,
  faArrowUp,
  faAt,
  faBars,
  faBroom,
  faCaretDown,
  faCaretRight,
  faChartBar,
  faCheck,
  faCircleNotch,
  faClipboard,
  faClipboardList,
  faCloudUploadAlt,
  faCog,
  faCogs,
  faCoins,
  faDatabase,
  faDice,
  faDiceFour,
  faDiceSix,
  faDraftingCompass,
  faDrawPolygon,
  faExternalLinkAlt,
  faEye,
  faFileAlt,
  faFileArchive,
  faFileAudio,
  faFileCode,
  faFileCsv,
  faFileDownload,
  faFileExcel,
  faFileImage,
  faFilePdf,
  faFilePowerpoint,
  faFileVideo,
  faFileWord,
  faFolder,
  faGlobeEurope,
  faHome,
  faIgloo,
  faInfo,
  faInfoCircle,
  faItalic,
  faLayerGroup,
  faLink,
  faList,
  faMapMarkedAlt,
  faMapPin,
  faMinus,
  faPalette,
  faPaperclip,
  faPen,
  faPencilRuler,
  faPercentage,
  faPhone,
  faPlus,
  faPowerOff,
  faQuestion,
  faShareAlt,
  faSitemap,
  faSortAlphaDown,
  faSortAlphaUp,
  faSortAlphaUpAlt,
  faSquareRootAlt,
  faTasks,
  faThList,
  faTimes,
  faToggleOff,
  faTrashAlt,
  faUserCircle,
  faUserCog,
  faUsersCog,
  faUserShield,
  faVectorSquare
} from '@fortawesome/free-solid-svg-icons';

import {
  faSquare as farSquareRegular,
  faCheckSquare as faCheckSquareRegular,
  faCalendarAlt,
  faClone,
  faComments,
  faQuestionCircle as farQuestionCircle,
  faCircle,
  faMinusSquare,
  faPlusSquare,
  faEyeSlash,
  faBell,
  faSave
} from '@fortawesome/free-regular-svg-icons';

export const AwesomeIcons = icon => {
  switch (icon) {
    case 'pdf':
      return faFilePdf;
    case 'xls':
      return faFileExcel;
    case 'xlsx':
      return faFileExcel;
    case 'ods':
      return faFileExcel;
    case 'csv':
      return faFileCsv;
    case 'doc':
      return faFileWord;
    case 'docx':
      return faFileWord;
    case 'aif':
      return faFileAudio;
    case 'mp3':
      return faFileAudio;
    case 'ogg':
      return faFileAudio;
    case 'wav':
      return faFileAudio;
    case 'wma':
      return faFileAudio;
    case 'mpa':
      return faFileAudio;
    case '7z':
      return faFileArchive;
    case 'zip':
      return faFileArchive;
    case 'tar.gz':
      return faFileArchive;
    case 'rar':
      return faFileArchive;
    case 'pkg':
      return faFileArchive;
    case 'json':
      return faFileCode;
    case 'xml':
      return faFileCode;
    case 'mdb':
      return faDatabase;
    case 'sql':
      return faDatabase;
    case 'ai':
      return faFileImage;
    case 'bmp':
      return faFileImage;
    case 'gif':
      return faFileImage;
    case 'ico':
      return faFileImage;
    case 'png':
      return faFileImage;
    case 'psd':
      return faFileImage;
    case 'jpg':
      return faFileImage;
    case 'jpeg':
      return faFileImage;
    case 'tif':
      return faFileImage;
    case 'tiff':
      return faFileImage;
    case 'pps':
      return faFilePowerpoint;
    case 'ppt':
      return faFilePowerpoint;
    case 'pptx':
      return faFilePowerpoint;
    case 'odp':
      return faFilePowerpoint;
    case 'key':
      return faFilePowerpoint;
    case 'mov':
      return faFileVideo;
    case 'mp4':
      return faFileVideo;
    case 'avi':
      return faFileVideo;
    case 'mpg':
      return faFileVideo;
    case 'mpeg':
      return faFileVideo;
    case 'wmv':
      return faFileVideo;
    case 'alphabeticOrderDown':
      return faSortAlphaDown;
    case 'alphabeticOrderUp':
      return faSortAlphaUpAlt;
    case 'angleUp':
      return faAngleDoubleUp;
    case 'angleSingleUp':
      return faAngleUp;
    case 'angleDown':
      return faAngleDown;
    case 'angleRight':
      return faAngleRight;
    case 'arrowDown':
      return faArrowDown;
    case 'arrowUp':
      return faArrowUp;
    case 'boolean':
      return faToggleOff;
    case 'check':
      return faCheck;
    case 'delete':
      return faTrashAlt;
    case 'disk':
      return faSave;
    case 'dropDown':
      return faCaretDown;
    case 'eye':
      return faEye;
    case 'eyeSlash':
      return faEyeSlash;
    case 'barChart':
      return faChartBar;
    case 'dataset':
      return faDatabase;
    case 'plus':
      return faPlus;
    case 'minusSquare':
      return faMinusSquare;
    case 'plusSquare':
      return faPlusSquare;
    case 'square':
      return farSquareRegular;
    case 'checkedSquare':
      return faCheckSquareRegular;
    case 'archive':
      return faArchive;
    case 'broom':
      return faBroom;
    case 'pencilRuler':
      return faPencilRuler;
    case 'user-profile':
      return faUserCircle;
    case 'clip':
      return faPaperclip;
    case 'clipboard':
      return faClipboard;
    case 'email':
      return faAt;
    case 'calendar':
      return faCalendarAlt;
    case 'circle':
      return faCircle;
    case 'compass':
      return faDraftingCompass;
    case 'formula':
      return faSquareRootAlt;
    case 'italic':
      return faItalic;
    case 'link':
      return faLink;
    case 'linkData':
      return faVectorSquare;
    case 'list':
      return faList;
    case 'listClipboard':
      return faClipboardList;
    case 'map':
      return faMapMarkedAlt;
    case 'move':
      return faBars;
    case 'notifications':
      return faBell;
    case 'number-decimal':
      return faDice;
    case 'percentage':
      return faPercentage;
    case 'point':
      return faMapPin;
    case 'polygon':
      return faDrawPolygon;
    case 'text':
      return faAlignJustify;
    case 'url':
      return faGlobeEurope;
    case 'clone':
      return faClone;
    case 'share':
      return faShareAlt;
    case 'comments':
      return faComments;
    case 'info':
      return faInfo;
    case 'infoCircle':
      return faInfoCircle;
    case 'list':
      return faThList;
    case 'question':
      return faQuestion;
    case 'questionCircle':
      return farQuestionCircle;
    case 'home':
      return faHome;
    case 'released':
      return faCloudUploadAlt;
    case 'dataCollection':
      return faLayerGroup;
    case 'representative':
      return faCoins;
    case 'siteMap':
      return faSitemap;
    case 'spinner':
      return faCircleNotch;
    case 'angleDoubleLeft':
      return faAngleDoubleLeft;
    case 'angleDoubleRight':
      return faAngleDoubleRight;
    case 'logout':
      return faPowerOff;
    case 'settings':
      return faCog;
    case 'edit':
      return faPen;
    case 'manageReporters':
      return faUsersCog;
    case 'userConfig':
      return faUserCog;
    case 'localhostAlert':
      return faIgloo;
    case 'fileDownload':
      return faFileDownload;
    case 'cross':
      return faTimes;
    case 'palette':
      return faPalette;
    case 'cofings':
      return faCogs;
    case 'folder':
      return faFolder;
    case 'expanded':
      return faCaretDown;
    case 'collapsed':
      return faCaretRight;
    case 'userShield':
      return faUserShield;
    case 'externalLink':
      return faExternalLinkAlt;
    case 'power-off':
      return faPowerOff;
    case 'align-right':
      return faAlignRight;
    case 'number-integer':
      return faDiceFour;
    case 'mobile':
      return faPhone;
    case 'multiselect':
      return faTasks;
    default:
      return faFileAlt;
  }
};
