
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