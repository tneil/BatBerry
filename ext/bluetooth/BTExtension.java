package bluetooth;

import blackberry.core.threading.DispatchableEvent;
import blackberry.core.threading.Dispatcher;

import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog; 

import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.ServerSocketConnection;

import net.rim.device.api.bluetooth.BluetoothSerialPortInfo;
import net.rim.device.api.bluetooth.BluetoothSerialPort;


import java.util.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.io.*;
import java.io.*;
import javax.bluetooth.*;


public final class BTExtension extends Scriptable
{

	// Statics ------------------------------------------------------------------     
    private static final int INSERT = 1;
    private static final int REMOVE = 2;    
    private static final int JUST_OPEN = 3;
    private static final int CONTENTS = 4;
    private static final int NO_CONTENTS = 5;
    private static final int FIRST = 0;
	private static final String FIELD_CONNECT = "connect";
	private static final String FIELD_DISCONNECT = "disconnect";
	private static final String FIELD_LIST = "listDevices";
	private static final String FIELD_SEND = "send";
	
	
	private openConnection _openConnection = new openConnection();
	private listKnownDevices _listKnownDevices = new listKnownDevices(); 
	private closeConnection _closeConnection = new closeConnection();
	private send _send = new send();
	private ScriptableFunction _onConnected = null;
	private ScriptableFunction _onConnectError = null;
	private ScriptableFunction _onData = null;
	
	
	// Bluetooth variables
	private StreamConnection _bluetoothConnection;
	private DataOutputStream _dout;
	private DataInputStream _din;
	
	
	/* Dispatcher! */
	public Object getField(String name) throws Exception {
		if( name.equals( FIELD_CONNECT ) ) {
				return _openConnection;
		}
		else if ( name.equals( FIELD_DISCONNECT ) ) {
				return _closeConnection;
		}
		else if ( name.equals( FIELD_SEND) ) {
				return _send;
		}
		else if ( name.equals( FIELD_LIST ) ) {
				return _listKnownDevices;
		}

		return super.getField(name);
	}
	
	
	
	/**
	 * Lists the devices available for serial port connections
	 */                
	private final class listKnownDevices extends ScriptableFunction {
			
		public Object invoke(Object obj,Object[] args) throws Exception {                    
						   
			try 
			{
			
				BluetoothSerialPortInfo[] info = BluetoothSerialPort.getSerialPortInfo();
				
				if (info == null)  {
					return UNDEFINED;
				} else {
					String[] values = new String[info.length];
					for (int i = 0; i < info.length; i++) {
							values[i] = info[i].getDeviceName();
					}
				
					return values;
				}
			}
			catch(Exception e) // Unable to connect
			{
				return UNDEFINED;
			} 
		}
	}

	
	/* Opens a serial connection at the given target location. */
	private final class openConnection extends ScriptableFunction {
			
		public Object invoke(Object obj,Object[] args) throws Exception {
				  
			try
			{
				String targetLocation = (String)args[0];   
				_onConnected = (ScriptableFunction)args[1];
				_onConnectError = (ScriptableFunction)args[2];
				_onData = (ScriptableFunction)args[3];
				new ConnectionThread(targetLocation).start();
			}
			catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
			return UNDEFINED;
		}
	}
	
	/* Sends data over the open connection. */
	private final class send extends ScriptableFunction {
			
		public Object invoke(Object obj,Object[] args) throws Exception {
				  
			try
			{
				final String data = (String)args[0];   
				
				new Thread () {
					public void run() {
						try
						{
							_dout.writeUTF(data);
						}
						catch (Exception e) {
							throw new RuntimeException(e.getMessage());
						}
					}
				}.start();
				
			}
			catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
			return UNDEFINED;
		}
	}
	
	/* Closes the current open serial connection. */
	private final class closeConnection extends ScriptableFunction {
			
		public Object invoke(Object obj,Object[] args) throws Exception {
				  
			try
			{
				new Thread () {
					public void run() {
						try
						{
							closePort();
						}
						catch (Exception e) {
							throw new RuntimeException(e.getMessage());
						}
					}
				}.start();
			}
			catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
			return UNDEFINED;
		}
	}
		
		/**
     * Close the serial port
     */
    private void closePort() 
    {       
        // Close the bluetooth connection
        if (_bluetoothConnection != null) 
        {
            try 
            {
                _bluetoothConnection.close();
            } 
            catch(IOException ioe) 
            {                
            }
        }
        
		 // Close the input stream
        if (_din != null) 
        {
            try 
            {
                _din.close();
            } 
            catch(IOException ioe) 
            {                
            }
        }
        
        // Close the output stream
        if (_dout != null) 
        {
            try 
            {
                _dout.close();
            } 
            catch(IOException ioe) 
            {                
            }
        }
        
        _bluetoothConnection = null;
        _dout = null;
		_din = null;
    }
        
		
		private class ConnectionThread extends Thread 
		{
			private String _pairedDevice;
		
			public ConnectionThread(String pairedDevice) {
				_pairedDevice = pairedDevice;
			}
			
			public void run() 
			{
				try
				{
					BluetoothSerialPortInfo[] info = BluetoothSerialPort.getSerialPortInfo();
								
					if (info != null)  {
						String[] values = new String[info.length];
						for (int i = 0; i < info.length; i++) {
							if (info[i].getDeviceName().equals( _pairedDevice)) {
								
								// Set up the bluetooth connection
								final BluetoothSerialPortInfo serialPortInfo = info[i];
								
								_bluetoothConnection = (StreamConnection)Connector.open( serialPortInfo.toString(), Connector.READ_WRITE );
								_dout = _bluetoothConnection.openDataOutputStream();
								_din = _bluetoothConnection.openDataInputStream();
								
								final Object[] result = new Object[1];
								result[0] = _pairedDevice;
								
								// Create a new thread to make sure that the invoke of the JavaScript callback
								// does not initiate from the event thread.  This can otherwise cause a deadlock scenario
								new Thread () {
									public void run() {
										try
										{
											// Pass the result of the connection back to the handle of the JavaScript callback
											_onConnected.invoke(_onConnected, result);
										}
										catch (Exception e) {
											throw new RuntimeException(e.getMessage());
										}
									}
								}.start();
							}
						}
					}
					
					
				}
				catch(IOException e) // Unable to connect
				{
					final Object[] result = new Object[1];
					result[0] = "Unable to open serial port";
					
					// Create a new thread to make sure that the invoke of the JavaScript callback
					// does not initiate from the event thread.  This can otherwise cause a deadlock scenario
					new Thread () {
						public void run() {
							try
							{
								// Pass the error back to the handle of the JavaScript callback
								_onConnectError.invoke(_onConnectError, result);
							}
							catch (Exception e) {
								throw new RuntimeException(e.getMessage());
							}
						}
					}.start();
					
				} 
				catch( UnsupportedOperationException e ) // Bluetooth not supported
				{
					final Object[] result = new Object[1];
					result[0] = "This handheld or simulator does not support bluetooth.";
					
					// Create a new thread to make sure that the invoke of the JavaScript callback
					// does not initiate from the event thread.  This can otherwise cause a deadlock scenario
					new Thread () {
						public void run() {
							try
							{
								// Pass the error back to the handle of the JavaScript callback
								_onConnectError.invoke(_onConnectError, result);
							}
							catch (Exception e) {
								throw new RuntimeException(e.getMessage());
							}
						}
					}.start();
				}
			
				// Read information from the opened bluetooth serial port and 
				// respond to the type of action requested. Note: we flush the 
				// output stream every time we want to communicate to the other
				// device to ensure the message is sent immediately.
				try 
				{
				   // int type, offset, count;
				    String value;
					_dout.flush();
						
					// Communicating with the other device indefinitely unless the
					// connection is interrupted with an exception.
				    for (;;) 
					{
						value = _din.readUTF(); 
						
						if (value != null && value.length() > 0) {
							// Set our result data
							final Object[] result = new Object[1];
							result[0] = value;
							new Thread () {
								public void run() {
									try
									{
										// Pass the result of the data back to the handle of the JavaScript callback
										_onData.invoke(_onData, result);
									}
									catch (Exception e) {
										throw new RuntimeException(e.getMessage());
									}
								}
							}.start();
						}
					}
				} 
				catch(IOException ioe) 
				{
					UiApplication.getUiApplication().invokeLater(new Runnable() 
					{
						/**
						 * @see java.lang.Runnable#run()
						 */
						public void run() 
						{
							Dialog.alert("Problems reading from or writing to serial port.");
							closePort();
						}
					});
				}
		
			}
		}

        
}

