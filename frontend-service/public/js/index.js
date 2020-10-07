var isIE = /*@cc_on!@*/ false || !!document.documentMode;

// Edge 20+ no chromium
var isEdge = !isIE && !!window.StyleMedia;
if (isIE || isEdge) {
  var notice = document.getElementById('browserNotice');
  notice.style.display = 'block';
}
