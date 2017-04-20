package engine;

public class SystemEvent {
	double 	eventTime;
	int 	eventType; 
	int 	objectType; //-0-source, -1- node, -2- cycle
	int 	objectID;
	
	/**
	 * Class-----EventType----Action\n
	 * Node/Cycle---1------changePhase
	 * Node/Cycle---2------changeSlot
	 * Node---------4------endOfService
	 * Source-------8------generatePacket
	 * When many events in parallel - priorities correspond to Type (0-the highest)
	 * 
	 * @param eventTime_ 	tNext
	 * @param eventType_ 	1-changePhase, 2-changeSlot, 4-endOfService, 8-generatePacket
	 * @param objectType_ 	0-source, 1- node, 2- cycle
	 * @param objectID_		
	 */
	public SystemEvent(double eventTime_, int eventType_, int objectType_, int objectID_){
		eventTime=eventTime_;
		eventType=eventType_;
		objectType=objectType_;
		objectID=objectID_;
	}
}
