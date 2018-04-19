package tijos.framework.sensor.hcsr;

import java.io.IOException;

import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.eventcenter.ITiEvent;
import tijos.framework.eventcenter.ITiEventListener;
import tijos.framework.eventcenter.TiEventService;
import tijos.framework.eventcenter.TiEventType;
import tijos.framework.eventcenter.TiGPIOEvent;
import tijos.framework.util.Delay;

public class TiHCSR04 implements ITiEventListener {
	/**
	 * TiHCSR04 trig pin id
	 */
	private int trigPin;
	/**
	 * TiHCSR04 echo pin id
	 */
	private int echoPin;
	/**
	 * TiGPIO object
	 */
	private TiGPIO gpioObj;
	/**
	 * TiHCSR04 echo delta
	 */
	private long echoDelta;
	/**
	 * TiHCSR04 speed default, 340m/s
	 */
	private double speedDefault;
	/**
	 * TiHCSR04 echo time1
	 */
	private long echoTime1;
	/**
	 * TiHCSR04 echo time2
	 */
	private long echoTime2;
	/**
	 * TiHCSR04 enable flag
	 */
	private volatile boolean sr04Enable;

	@Override
	public TiEventType getType() {
		return TiEventType.GPIO;
	}

	@Override
	public void onEvent(ITiEvent evt) {
		if (!sr04Enable)
			return;
		TiGPIOEvent eventObj = (TiGPIOEvent) evt;
		if (eventObj.getPin() == echoPin) {
			switch (eventObj.getEvent()) {
			case TiGPIO.EVT_FALLINGEDGE:
				echoTime2 = eventObj.getTime();
				sr04Enable = false;
				break;
			case TiGPIO.EVT_RISINGEDGE:
				echoTime1 = eventObj.getTime();
				break;
			default:
				break;
			}
		}
	}

	/**
	 * TiHCSR04 initialization
	 * 
	 * @param gpio
	 *            TiGPIO object[IN]
	 * @param trigPinID
	 *            trig pin id[IN]
	 * @param echoPinID
	 *            echo id[IN]
	 * @throws IOException
	 */
	public TiHCSR04(TiGPIO gpio, int trigPinID, int echoPinID) throws IOException {
		gpio.setWorkMode(trigPinID, TiGPIO.OUTPUT_PP);
		gpio.writePin(trigPinID, 0);
		gpio.setWorkMode(echoPinID, TiGPIO.INPUT_FLOATING);
		gpio.setEventParameters(echoPinID, TiGPIO.EVT_BOTHEDGE, 0);
		TiEventService.getInstance().addListener(this);
		gpioObj = gpio;
		trigPin = trigPinID;
		echoPin = echoPinID;
		echoDelta = -1;
		speedDefault = 0.00034;
		sr04Enable = false;
	}

	/**
	 * Startup distance measurement
	 * 
	 * @throws IOException
	 */
	public void measure() throws IOException {
		int currTime = (int) System.currentTimeMillis();
		synchronized (this) {
			sr04Enable = true;
			gpioObj.writePin(trigPin, 1);
			Delay.msDelay(1);
			gpioObj.writePin(trigPin, 0);
			for (;;) {
				if ((int) System.currentTimeMillis() - currTime > 500) {
					throw new IOException("time out.");
				}
				if (sr04Enable) {
					Thread.yield();
					continue;
				}
				if (echoTime2 <= echoTime1) {
					throw new IOException("data error.");
				}
				echoDelta = echoTime2 - echoTime1;
				break;
			}
		}
	}

	/**
	 * Gets the distance, unit:m
	 * 
	 * @return distance
	 */
	public double getDistance() {
		synchronized (this) {
			if (echoDelta < 0)
				return Double.NaN;
			double distance = (echoDelta * speedDefault) / 2;
			if (distance > 4.0)
				return Double.NaN;
			return distance;
		}
	}

	/**
	 * Set the sound speed
	 * 
	 * @param speed
	 *            current speed,default:340
	 */
	public void setSpeed(double speed) {
		synchronized (this) {
			speedDefault = speed / 1000000;
		}
	}

	/**
	 * Gets the trig pin id
	 * 
	 * @return pin id
	 */
	public int getTrigPinID() {
		return trigPin;
	}

	/**
	 * Gets the echo pin id
	 * 
	 * @return pin id
	 */
	public int getEchoPinID() {
		return echoPin;
	}
}
