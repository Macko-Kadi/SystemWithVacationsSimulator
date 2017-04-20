package engine;

import java.util.ArrayList;

public class Node {
	public final int NODE_ID;
	private CycleGeneric cycle;
	private boolean isServerBusy=false;
	private double linkSpeed;
	private int[][][] queuesStates; //[slot][Q0][Q1] - for evaluation of 2D Markov chain analysis
	private ArrayList<Queue> listOfQueues; //1 Queue for 1 RI !
	//the only event a node generates = endOfService !
	private double tNextEndOfService=Double.MAX_VALUE;
	private Packet pcktCurrServed=null;
	protected final int NUMBER_OF_RIS;
	//To be enhanced - different packet sizes...
	protected final int PCKT_TIME=1; //service time of a packet
	Node(int NODE_ID_, double linkSpeed_, ArrayList<Integer> buforSizes_, ArrayList<Double> RITimes_){
		NODE_ID=NODE_ID_;
		linkSpeed=linkSpeed_;
		NUMBER_OF_RIS=RITimes_.size();	
		//initialize cycle 
		cycle=new CycleGeneric(NODE_ID, RITimes_);
		//initialize Queues
		listOfQueues=new ArrayList<Queue>();
		for (int i=0;i<NUMBER_OF_RIS;i++){
			Queue q=new Queue(i, CycleGeneric.getCycleTime(RITimes_),RITimes_.get(i), buforSizes_.get(i));
			listOfQueues.add(q);
		}
		//cant be done, when single system
		if(NUMBER_OF_RIS>1)
			queuesStates = new int[(int)getCycleTime()][buforSizes_.get(0)+1][buforSizes_.get(1)+1];
		
	}
	
	protected SystemEvent getEvent(){
		SystemEvent e=cycle.getEvent();
		if (e.eventTime==tNextEndOfService)
			return new SystemEvent(tNextEndOfService, 4+e.eventType, 1, NODE_ID);
		else if (e.eventTime>tNextEndOfService){
			if (Helper.SLOTED) System.out.println("ERR: Node / getEvent - system sloted and tNextEndOfService<tSlotChange - you should never see this...");
			return new SystemEvent(tNextEndOfService, 4, 1, NODE_ID);
		}
		else return e;
	}
	/**
	 * changes a slot
	 * @param t
	 */
	protected void slotChange(double t){
		cycle.slotChange(t);
	}
	protected void phaseChange(double t){
		cycle.phaseChange(t);
	}
	protected double getCycleTime(){
		return cycle.getCycleTime();
	}
	/**
	 * Be careful!
	 * don't use getCurrSlot() inside the updateQueuesStatsJustAfter() 
	 * because the slot we want to refer is not the currentSlot!
	 * The moment just before n-th slot end is in the n-th   slot
	 * The moment just after  n-th slot end is in the n+1-st slot
	 * 
	 * @return
	 */
	protected int getCurrSlot(){
		return cycle.getCurrSlot();
	}
	protected int getCurrRI(){
		return cycle.getCurrRI();
	}
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
		int currRI=cycle.getCurrRI();
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
		if(Helper.isAfterStart && Helper.SAVE_PACKET_TRACE) Helper.printPacketStatistic(t, pcktCurrServed);
		int RI=pcktCurrServed.getPcktRI();
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
		tNextEndOfService=Helper.roundDouble(t+p.getSize()/linkSpeed,3);
		isServerBusy=true;
		pcktCurrServed=p;
	}
	

	protected boolean isItPossibleToTakeFromProperQueue(double t){
		return (!isServerBusy && 						    		    //server is not busy and
				cycle.getCurrentPhaseRemainTime(t)>=PCKT_TIME &&				//remain time is enough, and
				!listOfQueues.get(cycle.getCurrRI()).isQueueEmpty());  //something in the queue
	}
	/**
	 * Checks whether Server is idle, and returns the first of next queues 
	 * that has a packet in bufor
	 *  
	 * @param t simTime
	 * @return ID of first of the nexts RIs, that has a packet to service
	 */
	protected int fromWhichQueueCanITakeAPacketSloted(double t){
		if(!isServerBusy){
			for (int i=1;i<NUMBER_OF_RIS;i++){
				boolean answer=(!listOfQueues.get((getCurrRI()+i)%NUMBER_OF_RIS).isQueueEmpty());	
				if (answer) return  ((getCurrRI()+i)%NUMBER_OF_RIS);
			}
		}
		return -1;				
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
	 *  
	 * @param t simTime
	 * @return ID of first of the nexts RIs, that has a packet to service
	 */
	protected int fromWhichQueueCanITakeAPacketContinous(double t){
		if(!isServerBusy){
			for (int i=1;i<NUMBER_OF_RIS;i++){
				boolean answer=(!listOfQueues.get((getCurrRI()+i)%NUMBER_OF_RIS).isQueueEmpty());
				if(i>1)answer=(answer && cycle.getCurrentPhaseRemainTime(t)>=PCKT_TIME);
				if (answer) return  ((getCurrRI()+i)%NUMBER_OF_RIS);
			}
		}
		return -1;				
	}
	
	
	protected void addToQueue(double t, Packet p){
		p.setArrivingSlot(getCurrSlot());
		listOfQueues.get(p.getPcktRI()).addToQueue(t, p);
	}
	
}
