

settingsScreen = {

	load: function(element) {
		// show our currently paired device
		if (settings.pairedDevice) {
			element.getElementById('currentlySelected').innerHTML = settings.pairedDevice;
		}
		
		if (window.webworks && window.webworks.bluetooth) {
			// Populate the screen with the available serial Bluetooth devices
			var devices = webworks.bluetooth.listDevices();
			if (devices) {
				for (var i = 0; i < devices.length; i++) {
					settingsScreen.createListEntry(element, devices[i]);							
				}
			}
		} else {
			settingsScreen.createListEntry(element, 'BatBerry');
			settingsScreen.createListEntry(element, 'PlayBook');
		}
		var list = element.getElementById('itemlist');
		bb.doLoad(list);
	},
	
	createListEntry: function(element, name) {
		var entry = document.createElement('div'),
			list = element.getElementById('itemlist');
			
		entry.setAttribute('data-bb-type','item');
		entry.setAttribute('data-bb-img','images/bluetooth.png');
		entry.setAttribute('data-bb-title', name);
		entry.setAttribute('data-bb-value', name);
		if (name == settings.pairedDevice) {
			entry.innerHTML = 'Selected'
		} else {
			entry.innerHTML = 'Enabled Device';
		}
		entry.onclick = settingsScreen.confirmChoice;
		list.appendChild(entry);	
	},
	
	confirmChoice: function() {
		var value = this.getAttribute('data-bb-value');
		if (value != settings.pairedDevice) {
			var	answer = confirm('Connect to ' + value);
			if (answer){
				// Reset the old selected item description
				if (settings.pairedDevice) {
					var item = this.parentNode.querySelectorAll('[data-bb-value='+ settings.pairedDevice +']')[0];
					item.querySelectorAll('[class=description]')[0].innerHTML = 'Enabled Device';
				}
				// Set our new item
				document.getElementById('currentlySelected').innerHTML = value;	
				this.querySelectorAll('[class=description]')[0].innerHTML = 'Selected';
				settings.savePairedDevice(value);
				setTimeout('settings.connect()', 20);
			}
		}
		
		
	}
}

