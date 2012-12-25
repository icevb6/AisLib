/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.ais.message;

import dk.dma.ais.binary.BinArray;
import dk.dma.ais.binary.SixbitEncoder;
import dk.dma.ais.binary.SixbitException;
import dk.dma.ais.sentence.Vdm;

/**
 * This message should be used as a standard position report for aircraft involved 
 * in SAR operations instead of Messages 1, 2 or 3. Stations other than
 * aircraft involved in SAR operations should not use transmit this message. The 
 * default reporting interval for this message should be 10 seconds.
 */
public class AisMessage9 extends AisMessage {

	/** serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Altitude (derived from GNSS) expressed in metres (0 – 4 094 metres) 
	 * 4 095 = not available, 4 094 = 4 094 metres or higher.
	 * 
	 * Altitude is in meters. The special value 4095 indicates altitude is 
	 * not available; 4094 indicates 4094 meters or higher.
	 */
	private int altitude; //12 bits
	
	/**
	 * Speed over ground in knot steps (0-1022 knots) 
	 * 1023 = not available, 1022 = 1022 knots or higher.
	 * 
	 * Speed over ground is in knots, NOT deciknots as in the common navigation 
	 * block; planes go faster. The special value 1023 indicates speed not 
	 * available, 1022 indicates 1022 knots or higher.
	 */
	private int sog; //10 bits
	
	/**
	 * AisPosition Accuracy: The position accuracy (PA) flag should be
	 * determined in accordance with Table 47 1 = high ( =< 10 m) 0 = low (>10
	 * m) 0 = default
	 */
	private int posAcc; // 1 bit
	
	/**
	 * Stores the positions in a general manner
	 */
	private AisPosition pos; // : Lat/Long 1/10000 minute

	/**
	 * Course over Ground Course over ground in 1/10 = (0-3599). 3600 (E10h) =
	 * not available = default. 3601-4095 should not be used
	 */
	private int cog; // 12 bits
	
	/**
	 * Time stamp: UTC second when the report was generated by the EPFS (0-59 or
	 * 60 if time stamp is not available, which should also be the default value
	 * or 61 if positioning system is in manual input mode or 62 if electronic
	 * position fixing system operates in estimated (dead reckoning) mode or 63
	 * if the positioning system is inoperative) 61, 62, 63 are not used by CS
	 * AIS
	 */
	private int utcSec; // 6 bits : UTC Seconds
	
	/**
	 * Reserved for definition by a competent regional authority. Should be set 
	 * to zero, if not used for any regional application. Regional applications 
	 * should not use zero
	 */
	private int regionalReserved; // 8 bits
	
	/**
	 * DTE: Data terminal equipment (DTE) ready 0 = available 1 = not available
	 * = default see § 3.3.1
	 */
	private int dte; // 1 bit : DTE flag
	
	/**
	 * Not used. Should be set to zero. Reserved for future use.
	 * 
	 * NOTE: In ITU-R M1371-4 this field is 3 bits compared to 1 previously
	 */
	private int spare = 0; // 3 bits
	
	/**
	 * 0 = Station operating in autonomous  and continuous mode =default
	 * 1 = Station operating in assigned mode
	 */
	private int assigned; // 1 bit : Assigned Mode Flag
	
	/**
	 * RAIM-flag: Receiver autonomous integrity monitoring (RAIM) flag of
	 * electronic position fixing device; 0 = RAIM not in use = default; 1 =
	 * RAIM in use. See Table 47
	 */
	private int raim; // 1 bit : RAIM flag
	
	/**
	 * Communication state selector flag: 
	 * 0 = SOTDMA communication state follows
	 * 1 = ITDMA communication state follows
	 */
	private int commStateSelectorFlag; // 1 bit
	
	/**
	 * SOTDMA/ITDMA sync state: sync state is part of the defined communication
	 * state (19) bits. The sync state is the first 2 bits of the 19 bits in the
	 * communication state of the message.
	 * 
	 * 0 UTC direct (see § 3.1.1.1) 1 UTC indirect (see § 3.1.1.2) 2 Station is
	 * synchronized to a base station (base direct) 3 Station is synchronized to
	 * another station based on the highest number of received stations or to
	 * another mobile station, which is directly synchronized to a base station
	 */
	private int syncState; // 2 bits
	
	/**
	 * SOTDMA Slot Timeout Specifies frames remaining until a new slot is
	 * selected 0 means that this was the last transmission in this slot 1-7
	 * means that 1 to 7 frames respectively are left until slot change
	 */
	private int slotTimeout; // 3 bits

	/**
	 * SOTDMA sub-message This field has many purposes see class description for
	 * help:
	 */
	private int subMessage; // 14 bits

	public AisMessage9() {
		super(9);
	}
	
	public AisMessage9(Vdm vdm) throws AisMessageException, SixbitException {
		super(vdm);
		parse(vdm.getBinArray());
	}

	public void parse(BinArray binArray) throws AisMessageException, SixbitException {
		BinArray sixbit = vdm.getBinArray();
        if (sixbit.getLength() < 168) {
            throw new AisMessageException("Message 9 wrong length " + sixbit.getLength());
        }

		super.parse(binArray);

		this.altitude = (int) binArray.getVal(12);
		this.sog = (int) binArray.getVal(10);
		this.posAcc = (int) binArray.getVal(1);

		this.pos = new AisPosition();
		this.pos.setRawLongitude(binArray.getVal(28));
		this.pos.setRawLatitude(binArray.getVal(27));

		this.cog = (int) binArray.getVal(12);
		this.utcSec = (int) binArray.getVal(6);
		this.regionalReserved = (int) binArray.getVal(8);
		this.dte = (int) binArray.getVal(1);
		this.spare = (int) binArray.getVal(3);

		this.assigned = (int) binArray.getVal(1);
		this.raim = (int) binArray.getVal(1);
		this.commStateSelectorFlag = (int) binArray.getVal(1);
		this.syncState = (int) binArray.getVal(2);
		this.slotTimeout = (int) binArray.getVal(3);
		this.subMessage = (int) binArray.getVal(14);
	}

	@Override
	public SixbitEncoder getEncoded() {
		SixbitEncoder encoder = super.encode();
		encoder.addVal(this.altitude, 12);
		encoder.addVal(this.sog, 10);
		encoder.addVal(this.posAcc, 1);
		encoder.addVal(this.pos.getRawLongitude(), 28);
		encoder.addVal(this.pos.getRawLatitude(), 27);
		encoder.addVal(this.cog, 12);
		encoder.addVal(this.utcSec, 6);
		encoder.addVal(this.regionalReserved, 8);
		encoder.addVal(this.dte, 1);
		encoder.addVal(this.spare, 3);
		encoder.addVal(this.assigned, 1);
		encoder.addVal(this.raim, 1);
		encoder.addVal(this.commStateSelectorFlag, 1);
		encoder.addVal(this.syncState, 2);
		encoder.addVal(this.slotTimeout, 3);
		encoder.addVal(this.subMessage, 14);
		return encoder;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString());
		builder.append(", altitude=");
		builder.append(altitude);
		builder.append(", sog=");
		builder.append(sog);
		builder.append(", posAcc=");
		builder.append(posAcc);
		builder.append(", pos=");
		builder.append(pos);
		builder.append(", cog=");
		builder.append(cog);
		builder.append(", utcSec=");
		builder.append(utcSec);
		builder.append(", regionalReserved=");
		builder.append(regionalReserved);
		builder.append(", dte=");
		builder.append(dte);
		builder.append(", spare=");
		builder.append(spare);
		builder.append(", assigned=");
		builder.append(assigned);
		builder.append(", raim=");
		builder.append(raim);
		builder.append(", commStateSelectorFlag=");
		builder.append(commStateSelectorFlag);
		builder.append(", syncState=");
		builder.append(syncState);
		builder.append(", slotTimeout=");
		builder.append(slotTimeout);
		builder.append(", subMessage=");
		builder.append(subMessage);
		builder.append("]");
		return builder.toString();
	}

	public int getAltitude() {
		return altitude;
	}

	public void setAltitude(int altitude) {
		this.altitude = altitude;
	}

	public int getSog() {
		return sog;
	}

	public void setSog(int sog) {
		this.sog = sog;
	}

	public int getPosAcc() {
		return posAcc;
	}

	public void setPosAcc(int posAcc) {
		this.posAcc = posAcc;
	}

	public AisPosition getPos() {
		return pos;
	}

	public void setPos(AisPosition pos) {
		this.pos = pos;
	}

	public int getCog() {
		return cog;
	}

	public void setCog(int cog) {
		this.cog = cog;
	}

	public int getUtcSec() {
		return utcSec;
	}

	public void setUtcSec(int utcSec) {
		this.utcSec = utcSec;
	}

	public int getRegionalReserved() {
		return regionalReserved;
	}
	
	public void setRegionalReserved(int regionalReserved) {
		this.regionalReserved = regionalReserved;
	}

	public int getDte() {
		return dte;
	}

	public void setDte(int dte) {
		this.dte = dte;
	}

	public int getSpare() {
		return spare;
	}

	public void setSpare(int spare) {
		this.spare = spare;
	}

	public int getAssigned() {
		return assigned;
	}

	public void setAssigned(int assigned) {
		this.assigned = assigned;
	}

	public int getRaim() {
		return raim;
	}

	public void setRaim(int raim) {
		this.raim = raim;
	}

	public int getCommStateSelectorFlag() {
		return commStateSelectorFlag;
	}

	public void setCommStateSelectorFlag(int commStateSelectorFlag) {
		this.commStateSelectorFlag = commStateSelectorFlag;
	}

	public int getSyncState() {
		return syncState;
	}

	public void setSyncState(int syncState) {
		this.syncState = syncState;
	}

	public int getSlotTimeout() {
		return slotTimeout;
	}

	public void setSlotTimeout(int slotTimeout) {
		this.slotTimeout = slotTimeout;
	}

	public int getSubMessage() {
		return subMessage;
	}

	public void setSubMessage(int subMessage) {
		this.subMessage = subMessage;
	}

}
