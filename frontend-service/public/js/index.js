var isIE = /*@cc_on!@*/ false || !!document.documentMode;

// Edge 20+ no chromium
var isEdge = !isIE && !!window.StyleMedia;
if (isIE || isEdge) {
  var notice = document.getElementById('browserNotice');
  notice.style.display = 'block';
}

window.addEventListener(
  'dragover',
  function (e) {
    // e.dataTransfer.effectAllowed = 'none';
    e.preventDefault();
    e = e || event;
  },
  false
);
window.addEventListener(
  'drop',
  function (e) {
    e = e || event;
    e.preventDefault();
  },
  false
);
