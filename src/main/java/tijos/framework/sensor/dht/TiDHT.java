package tijos.framework.sensor.dht;

import java.io.IOException;

import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.eventcenter.ITiEvent;
import tijos.framework.eventcenter.ITiEventListener;
import tijos.framework.eventcenter.TiEventService;
import tijos.framework.eventcenter.TiEventType;
import tijos.framework.eventcenter.TiGPIOEvent;
import tijos.framework.util.Delay;

public class TiDHT implements ITiEventListener {
	/**
	 * TiDHT pin id
	 */
	private int dhtPin;
	/**
	 * TiDHT dht22 flag
	 */
	private boolean dht22;
	/**
	 * TiGPIO object
	 */
	private TiGPIO gpioObj;
	/**
	 * TiDHT temperature integer
	 */
	private short dhtTemperature;
	/**
	 * TiDHT humidity integer
	 */
	private short dhtHumidity;
	/**
	 * TiDHT data buffer
	 */
	private int[] dhtDelta;
	/**
	 * TiDHT data last time;
	 */
	private long dhtLastTime;
	/**
	 * TiDHT data count
	 */
	private volatile int dhtCount;
	/**
	 * TiDHT enable flag
	 */
	private volatile boolean dhtEnable;

	@Override
	public TiEventType getType() {
		return TiEventType.GPIO;
	}

	@Override
	public void onEvent(ITiEvent evt) {
		if (!dhtEnable)
			return;
		TiGPIOEvent eventObj = (TiGPIOEvent) evt;
		if (eventObj.getPin() == dhtPin && eventObj.getEvent() == TiGPIO.EVT_FALLINGEDGE) {
			long time = eventObj.getTime();
			if (dhtCount <= 0)
				dhtLastTime = eventObj.getTime();
			dhtDelta[dhtCount++] = (int) (time - dhtLastTime);
			dhtLastTime = time;
			if (dhtCount >= dhtDelta.length) {
				dhtCount = 0;
				dhtEnable = false;
			}
		}
	}

	/**
	 * TiDHT initialization, default:model=DHT11
	 * 
	 * @param gpio
	 *            TiGPIO object[IN]
	 * @param dataPinID
	 *            data pin id[IN]
	 * @throws IOException
	 */
	public TiDHT(TiGPIO gpio, int dataPinID) throws IOException {
		this(gpio, dataPinID, false);
	}

	/**
	 * TiDHT initialization
	 * 
	 * @param gpio
	 *            TiGPIO object[IN]
	 * @param dataPinID
	 *            data pin id[IN]
	 * @param model22
	 *            true:DHT22, false:DHT11
	 * @throws IOException
	 */
	public TiDHT(TiGPIO gpio, int dataPinID, boolean model22) throws IOException {
		gpio.setWorkMode(dataPinID, TiGPIO.IO_FLOATING_OD);
		gpio.writePin(dataPinID, 1);
		gpio.setEventParameters(dataPinID, TiGPIO.EVT_FALLINGEDGE, 45);// >=45us
		TiEventService.getInstance().addListener(this);
		gpioObj = gpio;
		dhtPin = dataPinID;
		dhtDelta = new int[43];
		dhtCount = 0;
		dhtTemperature = -1;
		dhtHumidity = -1;
		dht22 = model22;
		dhtEnable = false;
	}

	/**
	 * Startup humiture measurement
	 * 
	 * @throws IOException
	 */
	public void measure() throws IOException {
		short temperature = 0;
		short humidity = 0;
		int currTime = (int) System.currentTimeMillis();
		synchronized (this) {
			dhtCount = 0;
			dhtEnable = true;
			gpioObj.writePin(dhtPin, 0);
			Delay.msDelay(18);
			gpioObj.writePin(dhtPin, 1);
			for (;;) {
				if ((int) System.currentTimeMillis() - currTime > 500)
				{
					throw new IOException("time out.");
				}
				
				if (dhtEnable) {
					Thread.yield();
					continue;
				}
				for (int i = 0; i < 16; i++) {
					humidity <<= 1;
					temperature <<= 1;
					int t = dhtDelta[3 + i];
					if (t >= 110)
						humidity |= 1;
					t = dhtDelta[3 + 16 + i];
					if (t >= 110)
						temperature |= 1;
				}
				int dhtChecksum = 0;
				for (int i = 0; i < 8; i++) {
					dhtChecksum <<= 1;
					int t = dhtDelta[3 + 32 + i];
					if (t >= 110)
						dhtChecksum |= 1;
				}
				int dhtSum = (temperature & 0xff) + (temperature >> 8) + (humidity & 0xff) + (humidity >> 8);
				if(dhtChecksum != dhtSum)
				{
					throw new IOException("checksum error.");
				}
				dhtTemperature = temperature;
				dhtHumidity = humidity;
				break;
			}
		}
	}

	/**
	 * Get the temperature
	 * 
	 * @return temperature value
	 */
	public double getTemperature() {
		synchronized (this) {
			if (dhtTemperature < 0)
				return Double.NaN;
			return (dht22) ? dhtTemperature * 0.1 : (dhtTemperature >> 8) * 1.0 + (dhtTemperature & 0xff) * 0.1;
		}
	}

	/**
	 * Get the humidity
	 * 
	 * @return humidity value
	 */
	public double getHumidity() {
		synchronized (this) {
			if (dhtHumidity < 0)
				return Double.NaN;
			return (dht22) ? dhtHumidity * 0.1 : (dhtHumidity >> 8) * 1.0 + (dhtHumidity & 0xff) * 0.1;
		}
	}

	/**
	 * Gets the data pin id
	 * 
	 * @return pin id
	 */
	public int getDataPinID() {
		return dhtPin;
	}
}
