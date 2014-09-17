var lastco = null;
var lastclass = "";
function selectText(event, containerobj) {
	if (lastco != null) {
		lastco.className = lastclass;
	}
	if (containerobj == null) {
		lastco = null;
		mybibleinternal.setSelectedVerse(null, null);
	} else {
		if (lastco == containerobj) {
			lastco.className = lastclass;
			lastco = null;
		} else {
			lastco = containerobj;
			lastclass = containerobj.className;
			containerobj.className = "markbbl";
			mybibleinternal.setSelectedVerse(containerobj.name, 
					containerobj.innerHTML.replace("/\&lt;br\&gt;/gi","\n").replace("/(&lt;([^&gt;]+)&gt;)/gi", ""));
			if (event) {				
				event.stopPropagation();
			}
		}
	}
}

function markVerse(verse, classverse) {
	verseelem = document.getElementById(verse);
	if (verseelem) {
		if (verseelem == lastco) {
			lastclass = classverse;
		}
		verseelem.className = classverse;
	}
}

function cleanSelection() {
	if (lastco != null) {
		lastco.className = lastclass;
	}
	lastco = null;
}
