import {
  faFilePdf,
  faFileExcel,
  faFileCsv,
  faFileWord,
  faFileAlt,
  faFileAudio,
  faFileArchive,
  faFileCode,
  faDatabase,
  faFileImage,
  faFilePowerpoint,
  faFileVideo,
  faTrashAlt,
  faCaretDown,
  faChartBar,
  faPlus,
  faArchive,
  faBroom,
  faPencilRuler,
  faUserCircle,
  faItalic,
  faSquareRootAlt,
  faPercentage,
  faDiceSix,
  faGlobeEurope,
  faMapMarkedAlt,
  faLink,
  faVectorSquare,
  faAt,
  faPaperclip,
  faAlignJustify,
  faList,
  faShareAlt
} from '@fortawesome/free-solid-svg-icons';

import {
  faSquare as farSquareRegular,
  faCheckSquare as faCheckSquareRegular,
  faCalendarAlt,
  faClone,
  faComments
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
    case 'delete':
      return faTrashAlt;
    case 'dropDown':
      return faCaretDown;
    case 'barChart':
      return faChartBar;
    case 'dataset':
      return faDatabase;
    case 'plus':
      return faPlus;
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
    case 'email':
      return faAt;
    case 'calendar':
      return faCalendarAlt;
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
    case 'map':
      return faMapMarkedAlt;
    case 'number':
      return faDiceSix;
    case 'percentage':
      return faPercentage;
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
    default:
      return faFileAlt;
  }
};
