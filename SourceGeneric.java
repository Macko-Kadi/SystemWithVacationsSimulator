package engine;

public abstract class SourceGeneric {
	protected final int SRC_ID; //ID of the source
	protected final int RI; 
	protected double tNextGen; //time of the next packet generation	
	protected double tStart; //first packet will be send at tStart
	protected int nrPkt; //current Packet number
	protected int meanPacketSize; //mean Packet size 
	protected double lambda; //arrival intensity
	
	public SourceGeneric(int RI_, int srcID_, int meanPacketSize_, double lambda_, double tStart_) {
		RI=RI_;
		SRC_ID=srcID_;
		meanPacketSize=meanPacketSize_;
		lambda=lambda_;
		tNextGen=tStart_;
		nrPkt=1;
	};
	protected SystemEvent getEvent(){
		return new SystemEvent(tNextGen, 8, 0, SRC_ID);
	}
	protected Packet create(int pSize_, double t){ //creates a packet to send
		Packet pkt=new Packet(RI, SRC_ID, nrPkt, pSize_, t);	
		nrPkt++;
		return pkt;
	};
	/**
	 * @return interval to next Packet generation
	 */
	protected abstract double calculateInterval();
	/**
	 * 
	 * @return generated packet's size
	 */
	protected abstract int getPacketSize();
	/**
	 * creates a packet and sets time of a new event
	 * @param t current time
	 * @return created packet
	 */
	public Packet genPacket(double t, int currRI){
		int pSize=getPacketSize();
		Packet p=null;
		if (Helper.DEBUG) System.out.println("currRI "+ currRI+ ", RI " + RI);
		if (currRI==RI)
			p=create(pSize,t);
		else
			if (Helper.DEBUG) System.out.println("ACTIVE - don't create!");
		tNextGen=Helper.roundDouble(t+calculateInterval(),4);	
		return p;		
	}
	public double getTNextGen(){
		return tNextGen;
	}

}
