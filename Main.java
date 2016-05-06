import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;


public class Main {
	
	OutputStream out;
	InputStream in;
	
	byte[] readInput1 = {(byte)254, (byte)150};

	public static void main(String[] args) {
		new Main();

	}
	
	public Main(){
		//Create a new socket object
		Socket clientSocket = new Socket();

		//Create inetSocketAddress object for controller ip address and listening port number
		InetSocketAddress address = new InetSocketAddress("192.168.2.54", 2101);		//Set IP address to the IP of your controller

		try {
			//Set timeout for connect, read, and write to controller
			clientSocket.setSoTimeout(3000);

			//Connect to the controller
			clientSocket.connect(address);

		} catch (SocketException e1) {
			e1.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		//Create output stream to controller for writing commands to it
		try {
			out = clientSocket.getOutputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}

		//Create input stream to controller for reading data back from it
		try {
			in = clientSocket.getInputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		}
		
		try {
			//Wait 1 second after connecting to board
			Thread.sleep(1000);
			
			//Send command to controller to read input 1
			out.write(readInput1);
			
			//Wait for return from controller
			while(in.available() == 0){
				Thread.sleep(100);
			}
			
			//Check to make sure we got data back from controller then print it to the console				
			if(in.available()> 0){
				//First wait for all data to be returned from controller
				Thread.sleep(50);
				byte[] returnData = new byte[in.available()];
				in.read(returnData);
				int reading = returnData[0];
				//Java inverts readings greater than 128 so we need to compensate for that
				if(reading < 0){
					reading = reading + 255;
				}
				//Print out the temperature to the console.
				int tempC = temperature(reading);
				System.out.println("Temperature Celsius: "+tempC);
				System.out.println("Temperature Fahrenheit "+convertToF(tempC));
				clientSocket.close();
				System.exit(1);
			}else{
				System.out.println("Controller failed to respond.  Killing application");
				//Stop application
				clientSocket.close();
				System.exit(1);
			}
			
			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
	}
	
	public int temperature(int reading){
		
		System.out.println("Reading: "+reading);
		
		//Convert reading to voltage
		double voltsPerStep = 5.000000/255.000000;
		double voltage = voltsPerStep*reading;
		System.out.println("Voltage: "+voltage);
		
		//Use voltage on input to calculate resistance
		double resistance = (10000*(voltage/5))/(1 - (voltage/5));
		System.out.println("Resistance: "+resistance);

		double lookupValue = resistance;

		double[] lookupTable={111000.3, 86000.39, 67000.74, 53000.39, 42000.45, 33000.89, 27000.28, 22000.05, 17000.96, 14000.68, 12000.09, 10000.00, 8000.313, 6000.941, 5000.828, 4000.912, 4000.161, 3000.537, 3000.021,
				2000.589, 2000.229, 1000.924, 1000.669, 1000.451, 1000.266, 1000.108, 973.5, 857.4, 757.9};
		int[] temps={-30, -25, -20, -15, -10, -5, 0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 105, 110};

		int tablePossition = 0;

		for(int i = 0; i < lookupTable.length; i++){
			if(lookupValue>lookupTable[i]){
				tablePossition = i;
				break;
			}
		}
		int lowtemp = 0;
		if(tablePossition != 0){
			lowtemp = temps[tablePossition-1];
		}

		if(tablePossition !=0){
			int hightemp = temps[tablePossition];
		}

		double tableValLow = lookupTable[tablePossition];
		double tableValHigh = 0;
		if(tablePossition !=0){
			tableValHigh = lookupTable[tablePossition-1];
		}


		double difference = tableValHigh-tableValLow;

		double stepVal = difference/5;

		double remainder = lookupValue - tableValLow;

		double tempDifference = (difference - remainder) / stepVal;

		int temperature = (int) (lowtemp + tempDifference);

		return temperature;
	}
	
	public double convertToF(int tempC){
		int tempF = (tempC*9/5) +32;
		
		return tempF;
	}

}
