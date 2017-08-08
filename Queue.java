package engine;

import java.util.ArrayList;

public class Queue {
	private final int Q_ID;
	private int howManyInQueue=0; //how many packets is present in the queue
	private double fieldBelowEN=0; //time*N(time) -> time*N(time)/time = E[N]
	private double sumOfEW=0;
	private double sumOfEWSlots=0;	
	private double tLastUpdate=Helper.START_COLLECT_TIME;
	private int numberOfLosses=0;
	private int numberOfArrivals=0;
	private final int BUFOR_SIZE;
	private final int NO_SLOTS;
	private final int RI_SLOTS;
	private boolean DEBUG;
	protected ArrayList<PacketDelay> delays;
	
	private ArrayList<Packet> listOfBufferedPackets;
	/**
	 * Queue occupancy JUST BEFORE the slot ends
	 * [slot][presentQueueOccupancy]
	 * slot=[0, CycleTime-1]; presentQueueOccupancy=[0,BUFOR_SIZE]
	 */
	private int[][] occupancyDistributionBefore;
	/**
	 * Queue occupancy JUST AFTER the slot ends
	 * [slot][presentQueueOccupancy]
	 * slot=[0, CycleTime-1]; presentQueueOccupancy=[0,BUFOR_SIZE]
	 */
	private int[][] occupancyDistributionAfter;
	/**
	 * distribution seen at the moments of packets arrivals
	 */
	private int[] occupancyDistributionPacketComes;
	/**
	 * distribution of waiting times (in slots)
	 */
	private int[] waitingSlotsDistribution;
	/**
	 * distribution of waiting times for packets that came in a given slot
	 */
	private int[][] waitingSlotsDistributionPerSlot;
	/**
	 * Qystem occupancy JUST BEFORE the slot ends
	 * [slot][presentSystemOccupancy]
	 * slot=[0, CycleTime-1]; presentSystemOccupancy=[0,BUFOR_SIZE+1]
	 */	
	private int[][] systemOccupancyDistributionBefore;
	/**
	 * Qystem occupancy JUST AFTER the slot ends
	 * [slot][presentSystemOccupancy]
	 * slot=[0, CycleTime-1]; presentSystemOccupancy=[0,BUFOR_SIZE+1]
	 */	
	private int[][] systemOccupancyDistributionAfter;
	
	Queue(int Q_ID_, double cycleTime, double RITime, int buf){
		Q_ID=Q_ID_;
		DEBUG=Helper.DEBUG_QUEUE;
		NO_SLOTS=(int)cycleTime;
		RI_SLOTS=(int)RITime;
		BUFOR_SIZE=buf;
		occupancyDistributionBefore=new int[NO_SLOTS][BUFOR_SIZE+1];
		occupancyDistributionAfter=new int[NO_SLOTS][BUFOR_SIZE+1];
		occupancyDistributionPacketComes=new int[BUFOR_SIZE+1];
		waitingSlotsDistribution=new int[calculateMaxWaitingTime()+1];
		waitingSlotsDistributionPerSlot=new int[NO_SLOTS][calculateMaxWaitingTime()+1];
		listOfBufferedPackets=new ArrayList<Packet>();
		systemOccupancyDistributionAfter=new int[NO_SLOTS][BUFOR_SIZE+2];
		systemOccupancyDistributionBefore=new int[NO_SLOTS][BUFOR_SIZE+2];
		delays=new ArrayList<PacketDelay>();
	}
	protected int calculateMaxWaitingTime(){
		//amount of vacation periods
		int avp=(int)Math.ceil((double)BUFOR_SIZE/(double)RI_SLOTS);
		return (BUFOR_SIZE+avp*(NO_SLOTS-RI_SLOTS)+1);
	}
	protected void updateStatsJustBefore(int slot_no_){
		occupancyDistributionBefore[slot_no_][howManyInQueue]++;
	}
	
	protected void updateStatsJustAfter(int slot_no_){
		occupancyDistributionAfter[slot_no_][howManyInQueue]++;
	}
	protected void updateSystemStatsJustBefore(int slot_no_, boolean systemState){
		if (systemState)
			systemOccupancyDistributionBefore[slot_no_][howManyInQueue+1]++;
		else
			systemOccupancyDistributionBefore[slot_no_][howManyInQueue]++;
	}
	
	protected void updateSystemStatsJustAfter(int slot_no_, boolean systemState){
		if (systemState)
			systemOccupancyDistributionAfter[slot_no_][howManyInQueue+1]++;
		else
			systemOccupancyDistributionAfter[slot_no_][howManyInQueue]++;
	}
	
	protected boolean isQueueFull(){
		return howManyInQueue==BUFOR_SIZE;
	}
	protected boolean isQueueEmpty(){
		return howManyInQueue==0;
	}
	protected int getQID(){
		return Q_ID;
	}
	protected int howManyInQueue(){
		return howManyInQueue;
	}
	/**
	 * Tries to add Packet p to the queue.
	 * 
	 * Update Stats of the queue (field below N(t))
	 * Update distribution seen at the moments of packets arrivals
	 * Increment number of arrivals
	 * 
	 * If there is a place in buffor -> 
	 * 		add a packet to the queue
	 * 		increment howManyInQueue counter, 
	 * If no place in bufor -> 
	 * 		increment losses
	 * 		print statistics of lossed packet		
	 * 
	 * @param t simtime
	 * @param p Packet
	 */
	protected void addToQueue(double t, Packet p){
		if(Helper.isAfterStart) occupancyDistributionPacketComes[howManyInQueue]++;
		if(Helper.isAfterStart) updateFieldBelowNT(t);
		if(Helper.isAfterStart) numberOfArrivals++;
		if (!isQueueFull()){
			listOfBufferedPackets.add(p);
			howManyInQueue++;		
		}
		else{
			if(Helper.isAfterStart) numberOfLosses++;
			//if(Helper.isAfterStart && Helper.SAVE_PACKET_TRACE) Helper.printPacketStatistic(t, p);
			if(Helper.isAfterStart && Helper.SAVE_PACKET_TRACE) delays.add(new PacketDelay(p.getPcktNr(),t, p.tCreation, p.getTTaken()));
		}
	}
	public void printDelays(String filename){
		System.out.println("liczba opoznien: " +delays.size());
		String data="";
		int j=0;
		for (int i=0; i<delays.size(); i++){
			j++;
			PacketDelay pd=delays.get(i);
		//	data=data+""+pd.number+"\t"+pd.timeIn+"\t"+pd.delay+"\t"+pd.timeOut+"\t "+pd.simTime+"\n";
			if(j!=1000)
				data=data+""+pd.number+"\t "+pd.delay+"\n";			
			if(j==1000)
				data=data+""+pd.number+"\t "+pd.delay;		
			if (j==1000){
				Helper.writeToFile(filename, data.replace('.', ','));
				j=0;
				data="";
			}
		}
	//	Helper.writeToFile(filename, data.replace('.', ','));
	}
	/**
	 * take first queued packet to service
	 * if there is no packets in the queue - returns false
	 * update field below N(t)
	 * add waiting time of the packet to the sum of waiting times
	 * update waiting slots distribution
	 * 
	 * @param t simtime
	 * @return does a queue contains at least one packet
	 */
	protected Packet takeFromQueue(double t){
		if(Helper.isAfterStart) updateFieldBelowNT(t);
		if (!isQueueEmpty()){
			Packet p=listOfBufferedPackets.get(0);
			howManyInQueue--;
			listOfBufferedPackets.remove(0);
			p.setTaken(t);
			if(Helper.isAfterStart) {
				double waitingTime=t-p.getTCreation();
				sumOfEW+=waitingTime;
				int waitingSlots=0;
				/* when continous system (without slots) is considered - waitingSlots==0 is fine.
				 * In general, when waitingTime is an integer, do not increment number of waited slots
				 * eg. 1.23 rounds to 2 slots, but 2 stays 2.
				 */
				if (waitingTime%1!=0) {waitingSlots=(int)waitingTime+1;}
				else if (waitingTime%1==0) {waitingSlots=(int)waitingTime;}				
				sumOfEWSlots+=waitingSlots;
				waitingSlotsDistribution[waitingSlots]++;
				waitingSlotsDistributionPerSlot[p.getArrivingSlot()][waitingSlots]++;
			}
			return p;
		}
		else{
			System.out.println("ERR: Queue "+Q_ID+" / "+"function: takeFromQueue / QueueEmpty==true - you shouldn't see this...");
			return null;
		}
	}
	
	/**
	 * when Queue size changes - update field N(t)
	 * @param t
	 */
	protected void updateFieldBelowNT(double t){
		double interval=t-tLastUpdate;
		fieldBelowEN=fieldBelowEN+interval*howManyInQueue;
		if (Helper.DEBUG_QUEUE)
			System.out.println("Q_ID: "+Q_ID +" updateFieldBelowNT; tLastUpdate: "+tLastUpdate +" t " + t + " inQueue: " + howManyInQueue );
		tLastUpdate=t;
	}
			
	protected StatsRecord getStats(double t, boolean syso){
		//You should create a File at first.
		String filename="D:/wyniki/"+Helper.FILENAME+"/Queue-"+Q_ID+"-EN-Ploss.txt";
		double EN=Helper.roundDouble(fieldBelowEN/(t-Helper.START_COLLECT_TIME),2);
		double Ploss=Helper.roundDouble((double)numberOfLosses/(double)numberOfArrivals,5);
		double EW=Helper.roundDouble(sumOfEW/(double)(numberOfArrivals-numberOfLosses),2);
		double EWSlot=Helper.roundDouble(sumOfEWSlots/(double)(numberOfArrivals-numberOfLosses),3);
		
		String data="==================QUEUE:"+Q_ID+"================"
				+ "\nE[N]=\t"+EN+
					"\nPloss=\t"+Ploss+
					"\nLosses/Arrivals="+numberOfLosses+"/"+ numberOfArrivals+
					"\nE[W]=\t"+EW+
					"\nE[W_SLOT]=\t"+EWSlot;
		if (syso) System.out.println(data);
		Helper.writeToFile(filename,data);
		//Distribution when packet comes
		filename="D:/wyniki/"+Helper.FILENAME+"/Queue-"+Q_ID+"-DistrCome.txt";
		double[] distrCome=Helper.normalizeDistr1D(occupancyDistributionPacketComes);
		data=Helper.print1D(distrCome);
		if (syso)System.out.println("-----------distrPcktComes----------");
		if (syso)System.out.println(data);
		Helper.writeToFile(filename,data);
		//Distribution just before	
		filename="D:/wyniki/"+Helper.FILENAME+"/Queue-"+Q_ID+"-DistrBefore.txt";
		double[][] distrBefore=Helper.normalizeDistr2D(occupancyDistributionBefore);
		data=Helper.print2D(distrBefore);
		if (syso)System.out.println("-------------distrBefore-----------");
		if (syso)System.out.println(data);
		Helper.writeToFile(filename,data);
		//Distribution just after
		filename="D:/wyniki/"+Helper.FILENAME+"/Queue-"+Q_ID+"-DistrAfter.txt";
		double[][] distrAfter=Helper.normalizeDistr2D(occupancyDistributionAfter);
		data=Helper.print2D(distrAfter);
		if (syso)System.out.println("-------------distrAfter-----------");
		if (syso)System.out.println(data);	
		Helper.writeToFile(filename,data);
		filename="D:/wyniki/"+Helper.FILENAME+"/Queue-"+Q_ID+"-waitingSlotsDistr.txt";
		double[] distrWaitingSlots=Helper.normalizeDistr1D(waitingSlotsDistribution);
		data=Helper.print1D(distrWaitingSlots);
		if (syso)System.out.println("-------------waitingSlotsDistr-----------");
		if (syso)System.out.println(data);	
		Helper.writeToFile(filename,data);
		filename="D:/wyniki/"+Helper.FILENAME+"/Queue-"+Q_ID+"-waitingSlotsDistrPerSlot.txt";
		double[][] distrWaitingSlotsPerSlot=Helper.normalizeDistr2D(waitingSlotsDistributionPerSlot);
		data=Helper.print2D(distrWaitingSlotsPerSlot);
		if (syso)System.out.println("----------waitingSlotsDistrPerSlot---------");
		if (syso)System.out.println(data);	
		Helper.writeToFile(filename,data);
		filename="D:/wyniki/"+Helper.FILENAME+"/Queue-"+Q_ID+"-systemOccupancyBefore.txt";
		double[][] distrSystemOccupancyDistributionBefore=Helper.normalizeDistr2D(systemOccupancyDistributionBefore);
		data=Helper.print2D(distrSystemOccupancyDistributionBefore);
		if (syso)System.out.println("----------SystemOccupancyDistributionBefore---------");
		if (syso)System.out.println(data);	
		Helper.writeToFile(filename,data);
		filename="D:/wyniki/"+Helper.FILENAME+"/Queue-"+Q_ID+"-systemOccupancyAfter.txt";
		double[][] distrSystemOccupancyDistributionAfter=Helper.normalizeDistr2D(systemOccupancyDistributionAfter);
		data=Helper.print2D(distrSystemOccupancyDistributionAfter);
		if (syso)System.out.println("----------SystemOccupancyDistributionAfter---------");
		if (syso)System.out.println(data);	
		Helper.writeToFile(filename,data);
		if(DEBUG){
			 System.out.println("-----------distrPcktComes (amounts)----------");
			filename="D:/wyniki/"+Helper.FILENAME+"/Queue-"+Q_ID+"-DistrComeDEBUG.txt";
			data=Helper.print1D(occupancyDistributionPacketComes);
			System.out.println(data);
			Helper.writeToFile(filename,data);
			System.out.println("--------------distrBefore (amounts)----------");
			filename="D:/wyniki/"+Helper.FILENAME+"/Queue-"+Q_ID+"-DistrBeforeDEBUG.txt";
			data=Helper.print2D(occupancyDistributionBefore);
			System.out.println(data);
			Helper.writeToFile(filename,data);
			System.out.println("--------------distrAfter (amounts)-----------");
			filename="D:/wyniki/"+Helper.FILENAME+"/Queue-"+Q_ID+"-DistrAfterDEBUG.txt";
			data=Helper.print2D(occupancyDistributionAfter);
			System.out.println(data);
			Helper.writeToFile(filename,data);
		}		
		StatsRecord sr=new StatsRecord(EN,EW,EWSlot,Ploss,distrCome,distrBefore,distrAfter);
		return sr;		
	}
}
