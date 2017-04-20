package engine;

public class Packet {
	private final int PCKT_RI;
	private final int PCKT_SRC_ID; //which source generated the packet
	private double tCreation=-1; //when packet was generated
	private double tTaken=-1; //when packet was taken to server
	protected boolean properRI=false; //whether packet came to the system within its RI
	private int actualRI=-1; //RI that packet came to the system within
	private int sizeInBits;
	private int pcktNr=-1; //Packet number (assigned by the source)
	private int arrivingSlot=-1; //number of slot, when packet arrives
	
	public Packet(int PCKT_RI_, int PCKT_SRC_ID_,  int pcktNr_, int sizeInBits_, double tCreation_){
		PCKT_RI=PCKT_RI_;
		PCKT_SRC_ID=PCKT_SRC_ID_;
		pcktNr=pcktNr_;
		sizeInBits=sizeInBits_;
		tCreation=tCreation_;
	}
	public double getTCreation(){
		return tCreation;
	}
	public double getTTaken(){
		return tTaken;
	}
	public int getSize(){
		return sizeInBits;
	}
	public int getActRI(){
		return actualRI;
	}
	public int getPcktNr(){
		return pcktNr;
	}
	public int getPcktRI(){
		return PCKT_RI;
	}
	public int getPcktSrcID(){
		return PCKT_SRC_ID;
	}
	public void setTaken(double t){
		tTaken=t;
	}
	public void setActRI(int RI_){
		actualRI=RI_;
	}
	public void setArrivingSlot(int slot_){
		arrivingSlot=slot_;
	}
	public int getArrivingSlot(){
		return arrivingSlot;
	}
}
