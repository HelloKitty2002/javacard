package project;

iimport javacard.framework.*;
import javacard.framework.OwnerPIN;

public class mapin extends Applet
{
	// CLA
    final static byte project9_CLA =(byte)0xB0;
    /** test v TESTCHECK  kim tra pin sau khi thay i*/
	private final static byte[] test = new byte[8];
	private final static byte[] TEST_CHECK = new byte[]{(byte)0x01,(byte)0x02,(byte)0x03,(byte)0x04,(byte)0x05};
    //Kch thc m pin
	private final static byte PIN_MIN_SIZE = (byte) 4;
	private final static byte PIN_MAX_SIZE = (byte) 16;
	private final static byte[] PIN_INIT_VALUE={(byte)'S',(byte)'p',(byte)'a',(byte)'r',(byte)'k',(byte)'9',(byte)'9'};
	//INS-PIN
	private final static byte INS_CREATE_PIN = (byte) 0x40;
	private final static byte INS_VERIFY_PIN = (byte) 0x42;
	private final static byte INS_CHANGE_PIN = (byte) 0x44;
	private final static byte INS_UNBLOCK_PIN = (byte) 0x46;
	
	private final static byte INS_CREATE_INFORMATION = (byte)0x50;
	private final static byte INS_LOGOUT_ALL = (byte) 0x60;
	/* Kim tra trng thi setup */
	private boolean setupDone = false;
	// INS - Khi to
	private final static byte INS_SETUP = (byte) 0x2A;
	//PIN - m s nhn dng c nhn lu trong pins
	//PUK - m m kho c nhn lu trong unlk_pins
	private OwnerPIN pin, ublk_pin;
	/** ghi trng thi ng nhp*/
	private short logged_ids;
	
	/**
	* ERROR CONTROL*
	**/
	/** parameter passed invalid */
	private final static short SW_INVALID_PARAMETER = (short) 0x9C0F;
	/** returns error 9c0c when card is locked */
	private final static short SW_IDENTITY_BLOCKED = (short) 0x9C0C;
	/** tr li 9c02 khi nhp m pin sai */
	private final static short SW_AUTH_FAILED = (short) 0x9C02;
	/** tr li khi pin khng b kho*/
	private final static short SW_OPERATION_NOT_ALLOWED = (short) 0x9C03;
	/** Kim sot li */
	private final static short SW_INTERNAL_ERROR = (short) 0x9CFF;
	/** tr li 9c04 khi th cha c setup */
	private final static short SW_SETUP_NOT_DONE = (short) 0x9C04;
	/** Li P1*/
	private final static short SW_INCORRECT_P1 = (short) 0x9C10;
	/** Li P2*/
	private final static short SW_INCORRECT_P2 = (short) 0x9C11;
	private project9(byte[] bArray, short bOffset, byte bLength){
		if (!CheckPINPolicy(PIN_INIT_VALUE, (short) 0, (byte) PIN_INIT_VALUE.length))
		    ISOException.throwIt(SW_INTERNAL_ERROR);

		/* Ci gi tr pin khi to*/
		pin = new OwnerPIN((byte) 3, (byte) PIN_INIT_VALUE.length);
		pin.update(PIN_INIT_VALUE, (short) 0, (byte) PIN_INIT_VALUE.length);
		
		register();
	}
	public boolean select() {
		LogOut();
		return true;
	}

	public void deselect() {
		LogOut();
	}
	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		project9 wal = new project9(bArray, bOffset, bLength);
	}

	public void process(APDU apdu)
	{
		if (selectingApplet())
			ISOException.throwIt(ISO7816.SW_NO_ERROR);

		byte[] buffer = apdu.getBuffer();
		if ((buffer[ISO7816.OFFSET_CLA] == 0) && (buffer[ISO7816.OFFSET_INS] == (byte) 0xA4))
			return;
		if (buffer[ISO7816.OFFSET_CLA] != project9_CLA)
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		byte ins = buffer[ISO7816.OFFSET_INS];
		if (!setupDone && (ins != (byte) INS_SETUP))
			ISOException.throwIt(SW_SETUP_NOT_DONE);

		if (setupDone && (ins == (byte) INS_SETUP))
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		
		switch (ins)
		{
		case INS_SETUP:
			setup(apdu, buffer);
			break;
		case INS_CREATE_PIN:
			CreatePIN(apdu, buffer);
			break;
		case INS_VERIFY_PIN:
			VerifyPIN(apdu, buffer);
			break;
		case INS_CHANGE_PIN:
			ChangePIN(apdu, buffer);
			break;
		case INS_UNBLOCK_PIN:
			UnblockPIN(apdu, buffer);
			break;
		case INS_LOGOUT_ALL:
			LogOut();
			break;
		case INS_CREATE_INFORMATION:
			SetupInformation(apdu,buffer);
			break;
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
	/*SETUP*/
	private void setup(APDU apdu, byte[] buffer) {
		// short bytesLeft = Util.makeShort((byte) 0x00, buffer[ISO7816.OFFSET_LC]);
		// if (bytesLeft != apdu.setIncomingAndReceive())
			// ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

		// short base = (short) (ISO7816.OFFSET_CDATA);

		// byte numBytes = buffer[base++];
		// bytesLeft--;

		// if (!CheckPINPolicy(buffer, base, numBytes))
			// ISOException.throwIt(SW_INVALID_PARAMETER);

		// if (pin.getTriesRemaining() == (byte) 0x00)
			// ISOException.throwIt(SW_IDENTITY_BLOCKED);

		// if (!pin.check(buffer, base, numBytes))
			// ISOException.throwIt(SW_AUTH_FAILED);
			
		// base += numBytes;
		// bytesLeft-=numBytes;
		
		setupDone = true;
	}
	private void CreatePIN(APDU apdu, byte[] buffer) {
		//send B0400003050409090909
		//send CLA:B0 INS:40 P1:00 P2:*max-tries-03* LC:*05* DATA:*pinsize-04|pincode-09090909*
		byte num_tries = buffer[ISO7816.OFFSET_P2];
		/* Kim tra ng nhp */
		// if ((create_pin_ACL == (byte) 0xFF)
				// || (((logged_ids & create_pin_ACL) == (short) 0x0000) && (create_pin_ACL != (byte) 0x00)))
			// ISOException.throwIt(SW_UNAUTHORIZED);
		short avail = Util.makeShort((byte) 0x00, buffer[ISO7816.OFFSET_LC]); // 05
		if (apdu.setIncomingAndReceive() != avail)
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		// ti thiu 1 byte s size pin v 1 byte pin code
		if (avail < (short)2)
			ISOException.throwIt(SW_INVALID_PARAMETER);
		byte pin_size = buffer[ISO7816.OFFSET_CDATA]; // 04
		if (avail < (short) (1 + pin_size))
			ISOException.throwIt(SW_INVALID_PARAMETER);
		if (!CheckPINPolicy(buffer, (short) (ISO7816.OFFSET_CDATA + 1), pin_size))
			ISOException.throwIt(SW_INVALID_PARAMETER);
			
		////unblock
		// byte ucode_size = buffer[(short) (ISO7816.OFFSET_CDATA + 1 + pin_size)];
		// if (avail != (short) (1 + pin_size + 1 + ucode_size))
			// ISOException.throwIt(SW_INVALID_PARAMETER);
		// if (!CheckPINPolicy(buffer, (short) (ISO7816.OFFSET_CDATA + 1 + pin_size + 1), ucode_size))
			// ISOException.throwIt(SW_INVALID_PARAMETER);
			
		pin = new OwnerPIN(num_tries, PIN_MAX_SIZE);
		pin.update(buffer, (short) (ISO7816.OFFSET_CDATA + 1), pin_size);
		
		/*===CHECK PIN===*/
		if(pin.check(TEST_CHECK,(short)0,(byte)TEST_CHECK.length)){
			test[1] = (byte)0x02;
		}
		else{
			test[1] = (byte)0x03;
		}
		apdu.setOutgoing();
		apdu.setOutgoingLength((short)test.length);
		Util.arrayCopy(test,(short)0,buffer,(short)0,(short)test.length);
		apdu.sendBytes((short)0,(short)test.length);
		/*===END CHECK PIN===*/
		
		////unblock
		//ublk_pins[pin_nb] = new OwnerPIN((byte) 3, PIN_MAX_SIZE);
		// Recycle variable pin_size
		//pin_size = (byte) (ISO7816.OFFSET_CDATA + 1 + pin_size + 1);
		//ublk_pins[pin_nb].update(buffer, pin_size, ucode_size);
	}
	private void VerifyPIN(APDU apdu, byte[] buffer) {
		//pin_nb: th t ca pin trong mng cc pin
		if (pin == null)
			ISOException.throwIt(SW_INCORRECT_P1);
		if (buffer[ISO7816.OFFSET_P2] != 0x00)
			ISOException.throwIt(SW_INCORRECT_P2);
		short numBytes = Util.makeShort((byte) 0x00, buffer[ISO7816.OFFSET_LC]);
		
		if (numBytes != apdu.setIncomingAndReceive())
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		if (!CheckPINPolicy(buffer, ISO7816.OFFSET_CDATA, (byte) numBytes))
			ISOException.throwIt(SW_INVALID_PARAMETER);
		if (pin.getTriesRemaining() == (byte) 0x00)
			ISOException.throwIt(SW_IDENTITY_BLOCKED);
		if (!pin.check(buffer, (short) ISO7816.OFFSET_CDATA, (byte) numBytes)) {
			LogOut();
			ISOException.throwIt(SW_AUTH_FAILED);
		}
		logged_ids = (short) (0x0010);
	}
	private void ChangePIN(APDU apdu, byte[] buffer) {
		//send 
		//send CLA:B0 INS:44 P1:00 P2:00 LC:*0B* DATA:*pinsize-04|pincode-09090909|pinsize-05|pincode-0102030405*
		//byte pin_nb = buffer[ISO7816.OFFSET_P1];
		if (pin == null)
			ISOException.throwIt(SW_INCORRECT_P1);
		if (buffer[ISO7816.OFFSET_P2] != (byte) 0x00)
			ISOException.throwIt(SW_INCORRECT_P2);
		short avail = Util.makeShort((byte) 0x00, buffer[ISO7816.OFFSET_LC]);
		if (apdu.setIncomingAndReceive() != avail)
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		// c ti thiu 1 byte pin-size 1 byte pin-code
		if (avail < (short)4)
			ISOException.throwIt(SW_INVALID_PARAMETER);
		byte pin_size = buffer[ISO7816.OFFSET_CDATA];
		if (avail < (short) (1 + pin_size))
			ISOException.throwIt(SW_INVALID_PARAMETER);
		if (!CheckPINPolicy(buffer, (short) (ISO7816.OFFSET_CDATA + 1), pin_size))
			ISOException.throwIt(SW_INVALID_PARAMETER);
		byte new_pin_size = buffer[(short) (ISO7816.OFFSET_CDATA + 1 + pin_size)];
		if (avail < (short) (1 + pin_size + new_pin_size))
			ISOException.throwIt(SW_INVALID_PARAMETER);
		if (!CheckPINPolicy(buffer, (short) (ISO7816.OFFSET_CDATA + 1 + pin_size + 1), new_pin_size))
			ISOException.throwIt(SW_INVALID_PARAMETER);
		if (pin.getTriesRemaining() == (byte) 0x00)
			ISOException.throwIt(SW_IDENTITY_BLOCKED);
		if (!pin.check(buffer, (short) (ISO7816.OFFSET_CDATA + 1), pin_size)) {
			LogOut();
			ISOException.throwIt(SW_AUTH_FAILED);
		}
		pin.update(buffer, (short) (ISO7816.OFFSET_CDATA + 1 + pin_size + 1), new_pin_size);
		/*===CHECK PIN===*/
		if(pin.check(TEST_CHECK,(short)0,(byte)TEST_CHECK.length)){
			test[1] = (byte)0x02;
		}
		else{
			test[1] = (byte)0x03;
		}
		apdu.setOutgoing();
		apdu.setOutgoingLength((short)test.length);
		Util.arrayCopy(test,(short)0,buffer,(short)0,(short)test.length);
		apdu.sendBytes((short)0,(short)test.length);
		/*===END CHECK PIN===*/
		logged_ids = (short) (0x0010);
	}
	private void UnblockPIN(APDU apdu, byte[] buffer) {
		//byte pin_nb = buffer[ISO7816.OFFSET_P1];
		//OwnerPIN ublk_pin = ublk_pins[pin_nb];
		if (pin == null)
			ISOException.throwIt(SW_INCORRECT_P1);
		//if (ublk_pin == null)
			//ISOException.throwIt(SW_INTERNAL_ERROR);
		// Nu m PIN khng b chn, khng hp l
		if (pin.getTriesRemaining() != 0)
			ISOException.throwIt(SW_OPERATION_NOT_ALLOWED);
		if (buffer[ISO7816.OFFSET_P2] != 0x00)
			ISOException.throwIt(SW_INCORRECT_P2);
		short numBytes = Util.makeShort((byte) 0x00, buffer[ISO7816.OFFSET_LC]);
		
		if (numBytes != apdu.setIncomingAndReceive())
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		if (!CheckPINPolicy(buffer, ISO7816.OFFSET_CDATA, (byte) numBytes))
			ISOException.throwIt(SW_INVALID_PARAMETER);
		// if (!ublk_pin.check(buffer, ISO7816.OFFSET_CDATA, (byte) numBytes))
			// ISOException.throwIt(SW_AUTH_FAILED);
		//
		pin.resetAndUnblock();
	}
	private void SetupInformation(APDU apdu, byte[] buffer){
		
	}
	/*CHECK PIN POLICY*/
	private boolean CheckPINPolicy(byte[] pin_buffer, short pin_offset, byte pin_size) {
		if ((pin_size < PIN_MIN_SIZE) || (pin_size > PIN_MAX_SIZE))
			return false;
		return true;
	}
	private void LogOut() {
		logged_ids = (short) 0x0000; 
		pin.reset();
	}
}
