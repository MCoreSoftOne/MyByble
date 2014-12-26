var memdata = [];
function selectText(event, dataId) {
	var containerobj = null;
	if (dataId) {
		containerobj = document.getElementById(dataId);
	}
	if (containerobj == null) {
		mybibleinternal.selectVerse(null, null);
	} else {
		var cverse = findObjectInMem(dataId);
		if (cverse) {
			internalSetClassName(dataId, cverse.lastclass);
			removeObjectInMem(memdata, cverse);
			mybibleinternal.unSelectVerse(containerobj.name);
		} else {
			var versec = {};
			versec.id = dataId;
			versec.lastclass = containerobj.className;
			memdata.push(versec);
			internalSetClassName(dataId, "markbbl");
			mybibleinternal.selectVerse(containerobj.name, 
					containerobj.innerHTML.replace("/\&lt;br\&gt;/gi","\n").replace("/(&lt;([^&gt;]+)&gt;)/gi", ""));
		}
		if (event) {				
			event.stopPropagation();
		}
	}
}

function removeObjectInMem(arr, item) {
	 for(var i = arr.length; i--;) {
          if(arr[i] === item) {
              arr.splice(i, 1);
          }
      }
}

function findObjectInMem(id) {
	for (var i = 0; i < memdata.length; i++) {
	    if (memdata[i]) {
	    	if (memdata[i].id == id) {
	    		return memdata[i];
	    	}
	    }
	}
	return null;
}

function internalSetClassName(id, className) {
	if (_internalSetClassName(id, className)) {
		var i = 0;
		while (_internalSetClassName(id + "_" + ++i, className)) {
			//Do
		}
	}
}

function _internalSetClassName(id, className) {
	var element = document.getElementById(id);
	if (element) {
		if (!(typeof element === "undefined")) {
			element.className = className;
			return true;
		}
	}
	return false;
}

function markVerse(verse, classverse) {
	internalSetClassName(verse, classverse);
	var cverse = findObjectInMem(verse);
	removeObjectInMem(memdata, cverse);
}

function cleanSelection() {
	for (var i = 0; i < memdata.length; i++) {
	    if (memdata[i]) {
	    	if (memdata[i].id) {
	    		internalSetClassName(memdata[i].id, memdata[i].lastclass);
	    	}
	    }
	}
	memdata = [];
}
