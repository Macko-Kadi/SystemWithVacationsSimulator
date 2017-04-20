package engine;

import java.util.ArrayList;

public class CycleGeneric {
	private final int CYCLE_ID;
	private double tNextPhase;			//when next phase starts
	private double tNextSlot; 				//when next event occurs
	private int numberOfRIs;			//how many RIs	in the cycle
	private int currRI;					//current active RI phase 
	private int currSlot;				//current time slot
	ArrayList<Double> RITimes;			//list of RI phases times
	final private double CYCLE_TIME;
	
	public CycleGeneric(int CYCLE_ID_, ArrayList<Double> RITimes_){	
		CYCLE_ID=CYCLE_ID_;
		RITimes=RITimes_;
		numberOfRIs=RITimes.size();
		//Phase will never change, when there is only one RI (continous system)
		//if more then one RI - phase will change after first phase ends
		tNextPhase= (numberOfRIs==1 || RITimes.get(1)==0) ? Double.MAX_VALUE : RITimes.get(0);
		tNextSlot= 1;
		currRI=0;
		//tLastChange=-1;
		CYCLE_TIME=getCycleTime();
	} 
	
	protected SystemEvent getEvent(){
		//if only 1 RI - phase will never change
		if (numberOfRIs==1 || tNextSlot<tNextPhase) 
			return new SystemEvent(tNextSlot, 2, 2, CYCLE_ID);
		else if (tNextPhase==tNextSlot)
			//in sloted only ==, when cycle time is not int, also < possible
			return new SystemEvent(tNextPhase, 3, 2, CYCLE_ID);
		else{
			if (Helper.SLOTED) System.out.println("ERR: CycleGeneric / getEvent - system sloted and tNextPhase!=tNextSlot - you should never see this...");
			return new SystemEvent(tNextPhase, 1, 2, CYCLE_ID);
		}
	}
	/**
	 * Switches the cycle phase to the next RI
	 * Sets tNextPhase (t+currRI.phaseTime)
	 * Sets tNextSlot (t+1)
	 * 
	 */
	protected void phaseChange(double t){
		currRI=(currRI+1)%numberOfRIs;
		tNextPhase+=RITimes.get(currRI);
		slotChange(t);
	};
	/**
	 * changes current slot counter- first slot is numered 0 - the last one TA+TV-1
	 * 
	 * @param t
	 */
	protected void slotChange(double t){
		currSlot=(currSlot+1)%(int)CYCLE_TIME;
		tNextSlot=t+1;
	}
	
	protected int getCurrSlot(){
		return currSlot;
	}
	protected double getCurrentPhaseRemainTime(double simTime){
		double cycleTime=simTime%CYCLE_TIME; //time counted from the beginning of current cycle   
		//sum of phases times
		//                rTime
		// 0             |----|
		//-|-----|-----|-*----|-----|---
		//   RI0   RI1   ^ RI2  RI3   RI0
		//				cycleTime
		//rTime= RI0+RI1+RI2-cycleTime
		double sumOfPhases=0;
		for (int i=0;i<currRI+1;i++)
			 sumOfPhases=sumOfPhases+RITimes.get(i);
		return Helper.roundDouble(sumOfPhases-cycleTime,3);
	};
	/**
	 * BE CAREFUL - use it only to check BEFORE taking to server 
	 * remain time==1 -> just before serving last but one and just after taking to the service the last one
	 */
	protected boolean isThisTheLastSlot(double simTime){
		return (getCurrentPhaseRemainTime(simTime)<=1.0) ? true : false;
	};
	/**
	 * @return ID of the RI whose phase is currently active
	 */
	protected int getCurrRI(){
		return currRI;
	}
	/**
	 * 
	 * @return ID of the next RI
	 */
	protected int getNextRI(){
		return (currRI+1)%(int)CYCLE_TIME;
	}
	
	/**
	 * 
	 * @return cycle time (the sum of RIs' phases)
	 */
	protected double getCycleTime(){
		double cycleTime=0;
		for (double phaseTime : RITimes){
			cycleTime=cycleTime+phaseTime;
		}
		return cycleTime;
	};
	protected static double getCycleTime(ArrayList<Double> RITimes_){
		double cycleTime=0;
		for (double phaseTime : RITimes_){
			cycleTime=cycleTime+phaseTime;
		}
		return cycleTime;
	};
	
	protected double getTNextPhase(){
		return tNextPhase;
	}	
}
