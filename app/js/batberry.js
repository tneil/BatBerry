
// Object that contains the application settings
settings = {

	connected : false,
	pairedDevice : localStorage.getItem('pairedDevice'),
	queue : [],
	
	// Set our current paired device and store it for next launch
	savePairedDevice: function(name) {
		localStorage.setItem('pairedDevice', name);
		settings.pairedDevice = name;
	},
	
	// Connect bluetooth to the BatBerry
	connect: function() {
		if (settings.pairedDevice) {
			if (window.webworks && window.webworks.bluetooth) {
				webworks.bluetooth.disconnect();
				webworks.bluetooth.connect(settings.pairedDevice, settings.onConnected,settings.onConnectError, settings.onData);
			} else {
				// Ripple emulation
				settings.connected = true;
				settings.sendQueueData();
			}			
		} else {
			//alert('You have not configured a Bluetooth device');
		}
	},
	// Success event when we have established a connection with the Bluetooth device
	onConnected: function(device)  {
		settings.connected = true;
		settings.sendQueueData();
	},
				
	// Error event if there was a problem connecting to the device
	onConnectError: function(msg) {
		settings.connected = false;
		alert('Connection error "' + msg + '"');
	},
	
	// Data retrieved from Bluetooth connection
	onData: function(data) {
		alert(data);
	},
	
	// Send string over the bluetooth connection 
	sendBluetoothString: function(value) {
		settings.queue.push(value);
		if(settings.connected) {
			settings.sendQueueData();
		} else {
			settings.connect();
		}
	},
	
	// Send the data in the queue
	sendQueueData: function() {
		if (window.webworks && window.webworks.bluetooth) { 
			for (var i = settings.queue.length -1; i >= 0; i--) {
				// Send the data
				webworks.bluetooth.send(settings.queue[i])
				settings.queue.pop();	
			}
		} else {
			// Ripple emulation
			if (settings.queue.length > 0) {
				settings.queue.length = 0;
				alert('data sent');
			}
		}
	}
}

/* This object manages the state of the application */
state = {
	currentTab : 'tabHome',
	
	changeTab : function(value) {

		if (value == state.currentTab) return;
		document.getElementById(state.currentTab).style.display = 'none';
		document.getElementById(value).style.display = '';
		state.currentTab = value;
		
		
	}
}

/* this object manages the systems */
system = {
	canopyOpen: false,
	afterBurnerOn: false,
	suspensionUp: false,
	weaponsUp: false,
	
	triggerCanopy : function() {
		var canopy = document.getElementById('highlightCanopy'),
			actionCanopy = document.getElementById('actionCanopy');
		
		if (system.canopyOpen) {
			// Make our highlight disappear
			canopy.style.opacity = '0';
			// Change the action caption
			actionCanopy.setCaption('Open Canopy');
			actionCanopy.setImage('images/icons/unlocked.png');	
		
		} else {
			// Make our highlight appear
			canopy.style.opacity = '1';
			canopy.style['-webkit-transition'] = 'opacity 0.5s ease-in-out';
			// Change the action caption
			actionCanopy.setCaption('Close Canopy');
			actionCanopy.setImage('images/icons/locked.png');	
		}
		system.canopyOpen = !system.canopyOpen;
	},
	
	triggerAfterburner : function() {
		var afterburner = document.getElementById('highlightAfterburner'),
			actionAfterburner = document.getElementById('actionAfterburner');
		
		if (system.afterBurnerOn) {
			// Make our highlight disappear
			afterburner.style.opacity = '0';
			// Change the action caption
			actionAfterburner.setCaption('Afterburner On');
		
		} else {
			// Make our highlight appear
			afterburner.style.opacity = '1';
			afterburner.style['-webkit-transition'] = 'opacity 0.5s ease-in-out';
			// Change the action caption
			actionAfterburner.setCaption('Afterburner Off');
		}
		system.afterBurnerOn = !system.afterBurnerOn;
	},
	
	triggerSuspension : function() {
		var suspension = document.getElementById('highlightSuspension'),
			actionSuspension = document.getElementById('actionSuspension');
		
		if (system.suspensionUp) {
			// Make our highlight disappear
			suspension.style.opacity = '0';
			// Change the action caption
			actionSuspension.setCaption('Suspension Up');
			actionSuspension.setImage('images/icons/cloudUpload.png');		
		} else {
			// Make our highlight appear
			suspension.style.opacity = '1';
			suspension.style['-webkit-transition'] = 'opacity 0.5s ease-in-out';
			// Change the action caption
			actionSuspension.setCaption('Suspension Down');
			actionSuspension.setImage('images/icons/cloudDownload.png');	
		}
		system.suspensionUp = !system.suspensionUp;
	},
	
	triggerWeapons : function() {
		var weapons = document.getElementById('highlightGuns'),
			actionWeapons = document.getElementById('actionWeapons');
		
		if (system.weaponsUp) {
			// Make our highlight disappear
			weapons.style.opacity = '0';
			// Change the action caption
			actionWeapons.setCaption('Weapons Up');	
		} else {
			// Make our highlight appear
			weapons.style.opacity = '1';
			weapons.style['-webkit-transition'] = 'opacity 0.5s ease-in-out';
			// Change the action caption
			actionWeapons.setCaption('Weapons Down');
		}
		system.weaponsUp = !system.weaponsUp;
	}
	
}