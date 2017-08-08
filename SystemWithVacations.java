package engine;
//test github
//WERSJA TYLKO CONTINOUS, bez priorytetów Z WRR
import java.util.ArrayList;
import java.util.Arrays;

public class SystemWithVacations {
	ArrayList<SourcePSN> listOfSources;
	ArrayList<Double> listOfRITimes;
	ArrayList<Integer> listOfBufforsCap;
	Node node; //only one node in the system is considered

	
	public SystemWithVacations(int startSeed_){		
		//set loging paramethers
		Helper.DEBUG=false;
		Helper.DEBUG_QUEUE=false;
		Helper.SAVE_PACKET_TRACE=true;
		Helper.DISPLAY_DETAILS_FOR_EACH_SIM=true;
		Helper.ROUND_DEC=4;
		
		//set system parameters !
		Helper.SLOTED=false;
		Helper.PRIORITIES=false;
		Helper.DEBUG_SYSTEM_STATE=false;
		Helper.GUARD_TIME=new int[]{8,8};
		double T0=10.0;
		double T1=10.0;
		double lambda0=0.5*5/10;
		double lambda1=0.5*5/10;
		int bufor0 = 200;
		int bufor1 = 200;
		
		/*//Third RI
		double T2=2.0;
		double lambda2=0.2;
		int bufor2 = 100;
		int seed2 = startSeed_+321;*/
		
		//set simulation parameters
		double SIM_SECONDS=0.8; //aproximated simulation time (in the real world... is there any ?)
		Helper.MAX_SIM_TIME=SIM_SECONDS*3000000.0;
		Helper.START_COLLECT_TIME=SIM_SECONDS*300000.0;	
		
	//	Helper.MAX_SIM_TIME=1000;
	//	Helper.START_COLLECT_TIME=0;	
		int seed0 = 111+startSeed_;
		int seed1 = 222+startSeed_+5;
		
		//add parameters to Helper.FILENAME_PATH
		String temp="_T1-T2="+T0+"-"+T1+"_L1-L2="+Helper.roundDouble(lambda0,2)+"-"+Helper.roundDouble(lambda1,2)+"_B1-B2="+bufor0+"-"+bufor1;
		if(Helper.SLOTED)
			temp=temp+"_SLOTED";
		if(Helper.PRIORITIES)
			temp=temp+"_PRIORITIES";
		Helper.FILENAME=Helper.FILENAME_DATE+temp;
			
		//create sources
		listOfSources=new ArrayList<SourcePSN>();
		SourcePSN src0=new SourcePSN(0, 	//RI
									 0, 	//SRC_ID
									 seed0, //Random Seed
									 1000,  //pSize
									 true,  //constSize
									 lambda0,	//lambda 
									 0.11);	//tStart
		
		SourcePSN src1=new SourcePSN(1, 	//RI
									 1, 	//SRC_ID
									 seed1, //Random Seed
									 1000,  //pSize
									 true,  //constSize
									 lambda1,	//lambda 
									 0.23);	//tStart
		listOfSources.add(src0);
		listOfSources.add(src1);
		listOfRITimes=new ArrayList<Double>(Arrays.asList(T0,T1));
		listOfBufforsCap=new ArrayList<Integer>(Arrays.asList(bufor0,bufor1));

		//SINGLE SYSTEM !
		//listOfRITimes=new ArrayList<Double>(Arrays.asList(T0));
		//listOfBufforsCap=new ArrayList<Integer>(Arrays.asList(bufor0));
		
		/*//ADD THIRD
		SourcePSN src2=new SourcePSN(2, 	//RI
				 2, 	//SRC_ID
				 seed2, //Random Seed
				 1000,  //pSize
				 true,  //constSize
				 lambda2,	//lambda 
				 0.43);	//tStart		listOfSources.add(src2);
		listOfRITimes.add(T2);
		listOfBufforsCap.add(bufor2);	
		////----END OF THIRD--------//
*/		
		node=new Node(0, 				//NODE_ID
					1000.0,	  				//linkSpeed
					listOfBufforsCap,		//BufforSizes
					listOfRITimes);			//RITimes
		
		//initialize Paths for files for Packet statistics
		initializePaths();
		//for some time we do not collect statistics
		//waiting for steady state
		Helper.isAfterStart=false;

	}
	
	private void initializePaths(){
		for (int i=0;i<listOfRITimes.size();i++){
			String filename="D:/wyniki/"+Helper.FILENAME+"/Packet-Trace-RI"+i+".txt";
			Helper.createPath(filename);
		}
	}
	
	//TBD OPTIMILIZATION !!!! 
	//add/remove from list
	//be sure, that node event is first on the list
	/**
	 * collects events from the node, and sources
	 * first event on the list is the node event
	 * cycle event is considered as a node event
	 * 
	 * @return list of events (node + sources)
	 */
	private ArrayList<SystemEvent> collectEvents(){
		ArrayList<SystemEvent> listOfEvents=new ArrayList<SystemEvent>();
		listOfEvents.add(node.getEvent());
		for (SourcePSN s : listOfSources)
			listOfEvents.add(s.getEvent());
		return listOfEvents;
	}
	
	private SystemEvent getSoonestEvent(){
		ArrayList<SystemEvent> listOfEvents=collectEvents();
		double soonestTime=Double.MAX_VALUE;
		SystemEvent theE=listOfEvents.get(0);
		for (SystemEvent e : listOfEvents)
			if (e.eventTime<soonestTime){
				theE=e;
				soonestTime=e.eventTime;
			}
		return theE;
	}

	private double evaluateEvent(){
		SystemEvent e=getSoonestEvent();
		double simTime=e.eventTime;
	/*	if (Helper.DEBUG) {
			System.out.println("-------------");
			System.out.println(e.eventTime);
			System.out.println(e.eventType);
			System.out.println(e.objectID);
			System.out.println(e.objectType);
		}*/
		/**
		 * System is CONTINOUS, without PRIORITIES
		 * possible types:
		 * 1 - change phase 					--SHOULDN'T_SEE_IT_WHEN_CYCLE_TIME_IS_INT
		 * 2 - change slot
		 * 3 - change phase and slot
		 * 4 - end of service 					
		 * 5 - end of service and change phase 	--SHOULDN'T_SEE_IT_WHEN_CYCLE_TIME_IS_INT
		 * 6 - end of service and change slot
		 * 7 - end of service and change phase and slot
		 * 8 - generate Packet
		 */
		if(!Helper.SLOTED && !Helper.PRIORITIES){
			int currSlot=-1;
			int RI=-1;
			switch(e.eventType){
			//1 - change phase 		--SHOULDN'T_SEE_IT_WHEN_CYCLE_TIME_IS_INT
			case 1: 
				System.out.println("CASE 1 - phase change - nie powinno tego byc");
					break;
			case 2: 
				System.out.println("CASE 2 - slot change - nie powinno tego byc");
					break;
			case 3: 
				System.out.println("CASE 3 - slot i phase change - nie powinno tego byc");
					break;
			case 4: 
				//4 - end of service
					RI=node.endOfService(simTime);
					if (node.isItPossibleToTakeFromQueue(simTime))
						node.takeToServiceFromQueue(simTime,node.preferedRI);
					if (Helper.DEBUG) System.out.println(""+simTime+ " end of service RI: "+RI+" | eventType=4");
					break;		
			case 5: 
				System.out.println("CASE 5 - slot i obsluga - nie powinno tego byc");
					break;
			case 6: 
				System.out.println("CASE 6 - faza i obsluga - nie powinno tego byc");
					break;
			case 7: 
				System.out.println("CASE 7 - slot, faza i obsluga - nie powinno tego byc");
				break;
			case 8:
				//8 - generate Packet 
				//in case of simultanous source and node event
				//node events are first
					if (Helper.DEBUG) System.out.println(""+simTime+ " generation for RI: "+e.objectID+" | eventType=8");
					Packet p=listOfSources.get(e.objectID).genPacket(simTime);
					node.addToQueue(simTime, p);
					if (Helper.DEBUG) node.printQueueLength();
					if (node.isItPossibleToTakeFromQueue(simTime))
						node.takeToServiceFromQueue(simTime,node.preferedRI);
					break;
				
			}
		}
		return simTime;
	}
	
	private StatsRecord getStats(double t, boolean displayDetailsForEachSim){
		return node.getStats(t,displayDetailsForEachSim);
	}
	
	
	public static void main(String[] args) {
		SystemWithVacations SWV;
		ArrayList<StatsRecord> srs=new ArrayList<StatsRecord>();
		int startSeed=101;
		int amountOfSim=1;
		
		for (int i=0;i<amountOfSim;i++){
			System.out.println("===SIMULATION: "+i+" :STATISTICS=====");
			long tStart=System.currentTimeMillis();
			SWV=new SystemWithVacations(startSeed+i);		
			double simTime=0;
			while(simTime<Helper.MAX_SIM_TIME){
				if (simTime>=Helper.START_COLLECT_TIME && !Helper.isAfterStart){
					Helper.isAfterStart=true;
				}
				simTime=SWV.evaluateEvent();	
			}
			long tSim=System.currentTimeMillis();
			srs.add(SWV.getStats(simTime,Helper.DISPLAY_DETAILS_FOR_EACH_SIM));
			System.out.println("Simulation  Time [ms]: "+(tSim-tStart));
			SWV.node.listOfQueues.get(0).printDelays("D:/wyniki/queue-delays-Q0-"+Helper.getCurrDate()+"GT"+Helper.GUARD_TIME[0]+".txt");
			SWV.node.listOfQueues.get(1).printDelays("D:/wyniki/queue-delays-Q1-"+Helper.getCurrDate()+"GT"+Helper.GUARD_TIME[1]+".txt");
		}
		if (amountOfSim>1){
			StatsRecord[] res=StatsRecord.computeMeanAndConfidenceInterval(srs);
			StatsRecord.printMeanStats(res);
		}
	}
}
