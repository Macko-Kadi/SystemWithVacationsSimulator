package engine;

import java.util.ArrayList;

public class Node {
	public final int NODE_ID;
	protected boolean isServerBusy=false;
	private double linkSpeed;
	private int[][][] queuesStates; //[slot][Q0][Q1] - for evaluation of 2D Markov chain analysis
	protected ArrayList<Queue> listOfQueues; //1 Queue for 1 RI !
	//the only event a node generates = endOfService !
	private double tNextEndOfService=Double.MAX_VALUE;
	private Packet pcktCurrServed=null;
	protected final int NUMBER_OF_RIS;
	//To be enhanced - different packet sizes...
	protected final int PCKT_TIME=1; //service time of a packet
	protected int preferedRI=0; //z którego RI chcê wziaæ pakiet
	private int[] weights={10,10};
	private int[] utilizedSlots={0,0};

	void setPreferedRI(){
		
	}
	
	Node(int NODE_ID_, double linkSpeed_, ArrayList<Integer> buforSizes_, ArrayList<Double> RITimes_){
		NODE_ID=NODE_ID_;
		linkSpeed=linkSpeed_;
		NUMBER_OF_RIS=RITimes_.size();	

		listOfQueues=new ArrayList<Queue>();
		for (int i=0;i<NUMBER_OF_RIS;i++){
			Queue q=new Queue(i, CycleGeneric.getCycleTime(RITimes_),RITimes_.get(i), buforSizes_.get(i));
			listOfQueues.add(q);
		}
		//cant be done, when single system
		if(NUMBER_OF_RIS>1)
			queuesStates = new int[2][buforSizes_.get(0)+1][buforSizes_.get(1)+1];
		
	}
	
	/*tu zmienione WRR - nie ma zdarzeñ dla cyklu*/
	protected SystemEvent getEvent(){
		
	/*	SystemEvent e=cycle.getEvent();
		if (e.eventTime==tNextEndOfService)
			return new SystemEvent(tNextEndOfService, 4+e.eventType, 1, NODE_ID);
		else if (e.eventTime>tNextEndOfService){
			if (Helper.SLOTED) System.out.println("ERR: Node / getEvent - system sloted and tNextEndOfService<tSlotChange - you should never see this...");
		*/	return new SystemEvent(tNextEndOfService, 4, 1, NODE_ID);
	//	}
	//	else return e;
	}
	/**
	 * changes a slot
	 * @param t
	 */
	
		/**
	 * Be careful!
	 * don't use getCurrSlot() inside the updateQueuesStatsJustAfter() 
	 * because the slot we want to refer is not the currentSlot!
	 * The moment just before n-th slot end is in the n-th   slot
	 * The moment just after  n-th slot end is in the n+1-st slot
	 * 
	 * @return
	 */

	protected void updateStatsForMarkov(int slot_no_){
		int q0=listOfQueues.get(0).howManyInQueue();
		int q1=listOfQueues.get(1).howManyInQueue();
		queuesStates[slot_no_][q0][q1]++;
	}
	protected double[][][] getMarkovDistribution(){
		return Helper.normalizeDistr3D(queuesStates);
	}
	protected void printQueueLength(){
		for (Queue q : listOfQueues)
			System.out.println("Queue"+q.getQID()+" "+q.howManyInQueue());
	}
	/**
	 * Updates stats (occupancy) for each queue and for Markov chain - S(Q1, Q2, Slot) 
	 * @param slot_no_
	 */
	protected void updateQueuesStatsJustBefore(int slot_no_){
		if (NUMBER_OF_RIS>1) updateStatsForMarkov(slot_no_);
		for (Queue q : listOfQueues){
			q.updateStatsJustBefore(slot_no_);
			q.updateSystemStatsJustBefore(slot_no_, isServerBusy);
		}	
	}
	protected void updateQueuesStatsJustAfter(int slot_no_){
		for (Queue q : listOfQueues){
			q.updateStatsJustAfter(slot_no_);
			q.updateSystemStatsJustAfter(slot_no_, isServerBusy);
		}
	}
	/**
	 * 
	 * called at the end of each simulation
	 * prints the Markov states
	 * 
	 * @param t
	 * @param syso_details true- queues stats will be printed for each simulations
	 * @return StatsRecord for the first queue
	 */
	protected StatsRecord getStats(double t, boolean syso_details){
		System.out.println("Markov states");
		if (NUMBER_OF_RIS>1){
			System.out.println(Helper.print3D(queuesStates)); 
			System.out.println(Helper.print3D(getMarkovDistribution()));
		}
		
		StatsRecord srs=null;
		StatsRecord theSrs=null;
		for (Queue q : listOfQueues){
			srs=q.getStats(t,syso_details);
			if (q.getQID()==0)
				theSrs=srs;
		}
		return theSrs;
	}
	
	public void receive(Packet p, double t){
		//set whether a packet came in proper RI or not
		int currRI=0;
		p.properRI=(p.getPcktRI()==currRI) ? true : false;
		//RI the packet came in
		p.setActRI(currRI);
		//try to add the packet to a proper queue (can be lost)
		addToQueue(t, p);
	}
	
	/**
	 * evaluate an endOfService event:
	 * 	print served Packet statistics
	 * 	set isServerBusy=true
	 *  set pcktCurrServed=null
	 * 	next end of service - never
	 * 
	 * @param t simtime
	 * @return Served Packet RI
	 */
	protected int endOfService(double t){
	//if(Helper.isAfterStart && Helper.SAVE_PACKET_TRACE) Helper.printPacketStatistic(t, pcktCurrServed);	
		int RI=pcktCurrServed.getPcktRI();
		if(Helper.isAfterStart && Helper.SAVE_PACKET_TRACE) listOfQueues.get(RI).delays.add(new PacketDelay(pcktCurrServed.getPcktNr(),t, pcktCurrServed.tCreation, pcktCurrServed.getTTaken()));
		isServerBusy=false;
		pcktCurrServed=null;
		tNextEndOfService=Double.MAX_VALUE;
		return RI;
	}
	

	/**
	 * Takes to service first packet from the proper queue
	 * @param t simtime
	 * @return is something taken
	 */
	protected void takeToServiceFromQueue(double t, int RI_){
		Packet p=listOfQueues.get(RI_).takeFromQueue(t);
		utilizedSlots[RI_]++;
		tNextEndOfService=Helper.roundDouble(t+p.getSize()/linkSpeed,3);
		isServerBusy=true;
		pcktCurrServed=p;
		if (Helper.DEBUG) System.out.println("node.takeToService - queue: " + RI_);
		if (Helper.DEBUG) System.out.println("node.utilizedSlots: " + utilizedSlots[0] + " " + utilizedSlots[1]);
	}

	protected boolean isItPossibleToTakeFromQueue(double t){
		if (isServerBusy) return false;		

		//jesli wzi¹³em ju¿ tyle ile powinienem to resetuje i zmieniam preferencje 
		if (utilizedSlots[preferedRI]==weights[preferedRI]){
			utilizedSlots[preferedRI]=0;
			preferedRI=(preferedRI+1)%2;
		}
		//sprawdzam czy cos jest w kolejce preferowanego
		boolean queueEmpty=listOfQueues.get(preferedRI).isQueueEmpty();
		//jesli nie ma to ustawiam utilizedSlots na 0 i prze³¹czam na poprzedni¹,
		//jesli jest to zwracam true (preferedRI jest widziane globalnie)
		if (queueEmpty) {
			utilizedSlots[preferedRI]=0;
			preferedRI=(preferedRI+1)%2;
		}
		else return true;
		queueEmpty=listOfQueues.get(preferedRI).isQueueEmpty();
		if (queueEmpty) {
			utilizedSlots[preferedRI]=0;
			preferedRI=(preferedRI+1)%2;
		}
		else return true;
		return false;	
	}

	/**
	 * Checks whether Server is idle, 
	 * if it is, then check if next RI has something in queue,
	 * if it doesn't, check next queue - but its packet must be served before current phase change
	 * 
	 * 	 RI1   RI2   RI3   RI4  RI1
	 * -|----|-----|------|---|----
	 *     |<- moment when a packet left service  
	 *     |---| <-RI2 Packet service time - when taken, service will be finished in RI2 slot - it's OK.
	 *     |-| <-RI3 Packet service time and RI2 has nothing to send - can be taken as well (finished before phase changes)
	 *     |---|<-RI3 Packet service time and RI2 has nothing to send - can't be taken (finished after phase changes)
	 *     
	 *  NIEAKTUALNE !!!!!!!
	 *  TERAZ NAWET Z PRIO PAKIET NIE MO¯E PRZEJŒÆ DO SWOJEJ FAZY !
	 *  NA POCZ¥TKU FAZY SERWER MA BYÆ PUSTY
	 *  
	 *  
	 * @param t simTime
	 * @return ID of first of the nexts RIs, that has a packet to service
	 */
		
	protected void addToQueue(double t, Packet p){
		p.setArrivingSlot(0);
		listOfQueues.get(p.getPcktRI()).addToQueue(t, p);
	}
	
}
