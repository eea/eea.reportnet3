import {
  faAlignJustify,
  faAlignRight,
  faAngleDoubleLeft,
  faAngleDoubleRight,
  faAngleDoubleUp,
  faAngleDown,
  faAngleRight,
  faAngleUp,
  faArchive,
  faArrowDown,
  faArrowUp,
  faAt,
  faBars,
  faBookOpen,
  faBraille,
  faBroom,
  faCaretDown,
  faCaretRight,
  faChartBar,
  faCheck,
  faCircleNotch,
  faClipboard,
  faClipboardList,
  faClock,
  faCloudUploadAlt,
  faCog,
  faCogs,
  faCoins,
  faCommentMedical,
  faCube,
  faDatabase,
  faDice,
  faDiceFour,
  faDownload,
  faDraftingCompass,
  faDrawPolygon,
  faEdit,
  faExclamationCircle,
  faExternalLinkAlt,
  faEye,
  faFile,
  faFileAlt,
  faFileArchive,
  faFileAudio,
  faFileCode,
  faFileCsv,
  faFileDownload,
  faFileExcel,
  faFileExport,
  faFileImage,
  faFilePdf,
  faFilePowerpoint,
  faFileVideo,
  faFileWord,
  faFlagCheckered,
  faFolder,
  faGlobeEurope,
  faGripLines,
  faHome,
  faIgloo,
  faInfo,
  faInfoCircle,
  faItalic,
  faLayerGroup,
  faLink,
  faList,
  faListOl,
  faLock,
  faMapMarkedAlt,
  faMapPin,
  faMeteor,
  faPalette,
  faPaperclip,
  faPen,
  faPencilRuler,
  faPercentage,
  faPhone,
  faPlus,
  faPowerOff,
  faQuestion,
  faReply,
  faSearch,
  faShareAlt,
  faSitemap,
  faSortAlphaDown,
  faSortAlphaUpAlt,
  faSquareRootAlt,
  faTable,
  faTasks,
  faThList,
  faThumbtack,
  faTimes,
  faTimesCircle,
  faToggleOff,
  faTrashAlt,
  faUpload,
  faUserCheck,
  faUserCircle,
  faUserCog,
  faUserLock,
  faUsers,
  faUsersCog,
  faUserShield,
  faUserTimes,
  faVectorSquare,
  faVolumeUp,
} from '@fortawesome/free-solid-svg-icons';

import {
  faBell,
  faCalendarAlt,
  faCheckSquare as faCheckSquareRegular,
  faCircle,
  faClone,
  faComments,
  faEnvelope,
  faEyeSlash,
  faMinusSquare,
  faPlusSquare,
  faQuestionCircle as farQuestionCircle,
  faSave,
  faSquare as farSquareRegular,
  faFilePdf as farFilePdf,
  faTimesCircle as farTimesCircle
} from '@fortawesome/free-regular-svg-icons';
import { faBuffer, faConnectdevelop, faStaylinked } from '@fortawesome/free-brands-svg-icons';

export const AwesomeIcons = icon => {
  switch (icon) {
    case '7z':
      return faFileArchive;
    case 'ai':
      return faFileImage;
    case 'aif':
      return faFileAudio;
    case 'align-right':
      return faAlignRight;
    case 'alphabeticOrderDown':
      return faSortAlphaDown;
    case 'alphabeticOrderUp':
      return faSortAlphaUpAlt;
    case 'angleDoubleLeft':
      return faAngleDoubleLeft;
    case 'angleDoubleRight':
      return faAngleDoubleRight;
    case 'angleDown':
      return faAngleDown;
    case 'angleRight':
      return faAngleRight;
    case 'angleSingleUp':
      return faAngleUp;
    case 'angleUp':
      return faAngleDoubleUp;
    case 'archive':
      return faArchive;
    case 'arrowDown':
      return faArrowDown;
    case 'arrowUp':
      return faArrowUp;
    case 'avi':
      return faFileVideo;
    case 'barChart':
      return faChartBar;
    case 'bmp':
      return faFileImage;
    case 'boolean':
      return faToggleOff;
    case 'broom':
      return faBroom;
    case 'calendar':
      return faCalendarAlt;
    case 'check':
      return faCheck;
    case 'checkedSquare':
      return faCheckSquareRegular;
    case 'circle':
      return faCircle;
    case 'clip':
      return faPaperclip;
    case 'clipboard':
      return faClipboard;
    case 'clock':
      return faClock;
    case 'clone':
      return faClone;
    case 'cofings':
      return faCogs;
    case 'collapsed':
      return faCaretRight;
    case 'comment':
      return faCommentMedical;
    case 'comments':
      return faComments;
    case 'compass':
      return faDraftingCompass;
    case 'cross':
      return faTimes;
    case 'csv':
      return faFileCsv;
    case 'dataCollection':
      return faLayerGroup;
    case 'dataset':
      return faDatabase;
    case 'delete':
      return faTrashAlt;
    case 'deleteCircle':
      return farTimesCircle;
    case 'disk':
      return faSave;
    case 'doc':
      return faFileWord;
    case 'docx':
      return faFileWord;
    case 'download':
      return faDownload;
    case 'dropDown':
      return faCaretDown;
    case 'edit':
      return faPen;
    case 'email':
      return faAt;
    case 'envelope':
      return faEnvelope;
    case 'euDataset':
      return faCube;
    case 'exclamationCircle':
      return faExclamationCircle;
    case 'expanded':
      return faCaretDown;
    case 'externalLink':
      return faStaylinked;
    case 'externalUrl':
      return faExternalLinkAlt;
    case 'eye':
      return faEye;
    case 'eyeSlash':
      return faEyeSlash;
    case 'file':
      return faFile;
    case 'fileDownload':
      return faFileDownload;
    case 'fileExport':
      return faFileExport;
    case 'flag':
      return faFlagCheckered;
    case 'folder':
      return faFolder;
    case 'formula':
      return faSquareRootAlt;
    case 'gif':
      return faFileImage;
    case 'home':
      return faHome;
    case 'howTo':
      return faBookOpen;
    case 'ico':
      return faFileImage;
    case 'info':
      return faInfo;
    case 'infoCircle':
      return faInfoCircle;
    case 'italic':
      return faItalic;
    case 'jpeg':
      return faFileImage;
    case 'jpg':
      return faFileImage;
    case 'json':
      return faFileCode;
    case 'key':
      return faFilePowerpoint;
    case 'lightPdf':
      return farFilePdf;
    case 'line':
      return faGripLines;
    case 'link':
      return faLink;
    case 'linkData':
      return faVectorSquare;
    case 'list-ol':
      return faListOl;
    case 'list':
      return faList;
    case 'listClipboard':
      return faClipboardList;
    case 'localhostAlert':
      return faIgloo;
    case 'lock':
      return faLock;
    case 'logout':
      return faPowerOff;
    case 'manageReporters':
      return faUsersCog;
    case 'map':
      return faMapMarkedAlt;
    case 'mdb':
      return faDatabase;
    case 'meteor':
      return faMeteor;
    case 'minusSquare':
      return faMinusSquare;
    case 'mobile':
      return faPhone;
    case 'mov':
      return faFileVideo;
    case 'move':
      return faBars;
    case 'mp3':
      return faFileAudio;
    case 'mp4':
      return faFileVideo;
    case 'mpa':
      return faFileAudio;
    case 'mpeg':
      return faFileVideo;
    case 'mpg':
      return faFileVideo;
    case 'multiLineString':
      return faConnectdevelop;
    case 'multiPoint':
      return faBraille;
    case 'multiPolygon':
      return faBuffer;
    case 'multiselect':
      return faTasks;
    case 'notifications':
      return faBell;
    case 'number-decimal':
      return faDice;
    case 'number-integer':
      return faDiceFour;
    case 'odp':
      return faFilePowerpoint;
    case 'ods':
      return faFileExcel;
    case 'ogg':
      return faFileAudio;
    case 'palette':
      return faPalette;
    case 'pdf':
      return faFilePdf;
    case 'pencilRuler':
      return faPencilRuler;
    case 'percentage':
      return faPercentage;
    case 'pin':
      return faThumbtack;
    case 'pkg':
      return faFileArchive;
    case 'plus':
      return faPlus;
    case 'plusSquare':
      return faPlusSquare;
    case 'png':
      return faFileImage;
    case 'point':
      return faMapPin;
    case 'polygon':
      return faDrawPolygon;
    case 'power-off':
      return faPowerOff;
    case 'pps':
      return faFilePowerpoint;
    case 'ppt':
      return faFilePowerpoint;
    case 'pptx':
      return faFilePowerpoint;
    case 'psd':
      return faFileImage;
    case 'question':
      return faQuestion;
    case 'questionCircle':
      return farQuestionCircle;
    case 'quickEdit':
      return faEdit;
    case 'rar':
      return faFileArchive;
    case 'released':
      return faCloudUploadAlt;
    case 'reply':
      return faReply;
    case 'representative':
      return faCoins;
    case 'search':
      return faSearch;
    case 'settings':
      return faCog;
    case 'share':
      return faShareAlt;
    case 'siteMap':
      return faSitemap;
    case 'solidDeleteCircle':
      return faTimesCircle;
    case 'sound':
      return faVolumeUp;
    case 'spinner':
      return faCircleNotch;
    case 'sql':
      return faDatabase;
    case 'square':
      return farSquareRegular;
    case 'table':
      return faTable;
    case 'tar.gz':
      return faFileArchive;
    case 'text':
      return faAlignJustify;
    case 'thList':
      return faThList;
    case 'tif':
      return faFileImage;
    case 'tiff':
      return faFileImage;
    case 'upload':
      return faUpload;
    case 'url':
      return faGlobeEurope;
    case 'user-profile':
      return faUserCircle;
    case 'userCheck':
      return faUserCheck;
    case 'userConfig':
      return faUserCog;
    case 'userLock':
      return faUserLock;
    case 'users':
      return faUsers;
    case 'userShield':
      return faUserShield;
    case 'userTimes':
      return faUserTimes;
    case 'wav':
      return faFileAudio;
    case 'wma':
      return faFileAudio;
    case 'wmv':
      return faFileVideo;
    case 'xls':
      return faFileExcel;
    case 'xlsx':
      return faFileExcel;
    case 'xml':
      return faFileCode;
    case 'zip':
      return faFileArchive;
    default:
      return faFileAlt;
  }
};
