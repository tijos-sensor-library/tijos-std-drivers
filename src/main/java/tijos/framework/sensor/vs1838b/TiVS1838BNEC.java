package tijos.framework.sensor.vs1838b;

import java.io.IOException;

import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.eventcenter.ITiEvent;
import tijos.framework.eventcenter.ITiEventListener;
import tijos.framework.eventcenter.TiEventService;
import tijos.framework.eventcenter.TiEventType;
import tijos.framework.eventcenter.TiGPIOEvent;

public class TiVS1838BNEC implements ITiEventListener {
	/**
	 * TiVS1838B pin id
	 */
	private int vs1838bPin;
	/**
	 * TiVS1838B receive data
	 */
	private long recvData;
	/**
	 * TiVS1838B address data
	 */
	private int addrData;
	/**
	 * TiVS1838B command data
	 */
	private int cmdData;
	/**
	 * TiVS1838B data count
	 */
	private int vs1838bCount;
	/**
	 * TiVS1838B data step
	 */
	private int vs1838bStep;
	/**
	 * TiVS1838B restart flag
	 */
	private boolean vs1838bRestart;
	/**
	 * TiVS1838B data last time;
	 */
	private long vs1838bLastTime;
	/**
	 * TiVS1838B data delta time;
	 */
	private long vs1838bDelayTime;
	/**
	 * TiGPIO object
	 */
	private TiGPIO gpioObj = null;
	/**
	 * Event listener
	 */
	private ITiVS1838BNECEventListener vs1838bLc = null;

	@Override
	public TiEventType getType() {
		return TiEventType.GPIO;
	}

	@Override
	public void onEvent(ITiEvent evt) {
		synchronized (this) {
			TiGPIOEvent eventObj = (TiGPIOEvent) evt;
			if (eventObj.getPin() == vs1838bPin && eventObj.getEvent() == TiGPIO.EVT_FALLINGEDGE) {
				long time = eventObj.getTime();
				switch (vs1838bStep) {
				case 0:
					long delta = time - vs1838bLastTime;
					vs1838bLastTime = time;
					if (delta >= 80000) {
						recvData = 0;
						vs1838bStep = 1;
						vs1838bRestart = false;
					}
					if (delta >= 110000)
						vs1838bRestart = true;
					break;
				case 1:
					delta = time - vs1838bDelayTime;
					if (delta > 12000 && delta <= 15000) {
						addrData = -1;
						cmdData = -1;
						vs1838bCount = 0;
						vs1838bStep = 2;
						break;
					}
					if (delta > 9000 && delta <= 12000) {
						if (vs1838bRestart) {
							addrData = -1;
							cmdData = -1;
						} else if (addrData >= 0) {
							if (vs1838bLc != null)
								vs1838bLc.cmdRepeat(this);
						}
					}
					vs1838bStep = 0;
					break;
				default:
					recvData >>= 1;
					vs1838bCount++;
					delta = time - vs1838bDelayTime;
					if (delta > 2000 && delta <= 2500)
						recvData |= 0x80000000L;
					else if (delta >= 560 && delta <= 2000) {
						/**/} else {
						vs1838bStep = 0;
						break;
					}
					if (vs1838bCount >= 32) {
						byte addressP = (byte) (recvData & 0xff);
						byte addressN = (byte) ((recvData >> 8) & 0xff);
						byte commandP = (byte) ((recvData >> 16) & 0xff);
						byte commandN = (byte) ((recvData >> 24) & 0xff);
						if (~commandN == commandP && ~addressN == addressP) {
							addrData = addressP;
							cmdData = commandP;
							if (vs1838bLc != null)
								vs1838bLc.cmdReceived(this);
						}
						vs1838bStep = 0;
					}
					break;
				}
				vs1838bDelayTime = time;
			}
		}
	}

	/**
	 * TiVS1838B initialization
	 * 
	 * @param gpio
	 *            TiGPIO object[IN]
	 * @param dataPinID
	 *            data pin id[IN]
	 * @throws IOException
	 */
	public TiVS1838BNEC(TiGPIO gpio, int dataPinID) throws IOException {
		gpio.setWorkMode(dataPinID, TiGPIO.INPUT_FLOATING);
		gpioObj = gpio;
		vs1838bPin = dataPinID;
		addrData = -1;
		cmdData = -1;
		vs1838bStep = 0;
	}

	/**
	 * Set the TiVS1838BNEC event listener
	 * 
	 * @param lc
	 *            listener or null[IN]
	 * @throws IOException
	 */
	public void setEventListener(ITiVS1838BNECEventListener lc) throws IOException {
		synchronized (this) {
			if (vs1838bLc == null && lc != null) {
				gpioObj.setEventParameters(vs1838bPin, TiGPIO.EVT_FALLINGEDGE, 1000);// >=1ms
				TiEventService.getInstance().addListener(this);
				vs1838bLc = lc;
			} else if (vs1838bLc != null && lc == null) {
				gpioObj.setEventParameters(vs1838bPin, TiGPIO.EVT_NONE, 0);
				TiEventService.getInstance().unregisterEvent(this);
				vs1838bLc = null;
			} else {
			}
		}
		return;
	}

	/**
	 * Gets the address received
	 * 
	 * @return >=0:address, <= no address
	 */
	public int getAddress() {
		synchronized (this) {
			return addrData;
		}
	}

	/**
	 * Gets the command received
	 * 
	 * @return >=0:command, <= no command
	 */
	public int getCommand() {
		synchronized (this) {
			return cmdData;
		}
	}

	/**
	 * Gets the data pin id
	 * 
	 * @return pin id
	 */
	public int getDataPinID() {
		return vs1838bPin;
	}
}
