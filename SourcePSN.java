package engine;

import java.util.Random;

public class SourcePSN extends SourceGeneric{
	private Random randomIntervalGenerator;
	private Random randomSizeGenerator=null;
	private boolean constSize; //is size of packets constant ?
	
	
	public SourcePSN(int RI_, int SRC_ID_, int randomSeed, int pSize_, boolean constSize_, double lambda_, double tStart_) {
		super(RI_, SRC_ID_, pSize_, lambda_, tStart_);
		constSize=constSize_;
		randomIntervalGenerator=new Random(randomSeed);
		if (!constSize){
			randomSizeGenerator=new Random(randomSeed+100);
		}
	}

	@Override
	protected double calculateInterval() {
		double randValue=randomIntervalGenerator.nextDouble();
	    double interval = (-1) /lambda * Math.log(1-randValue);	    
	    return Helper.roundDouble(interval, 4);
	}
	
	/**
	 * 
	 * @return a new random value according to exponential distribution with mean value = pSize
	 */
	protected int calculatePacketSize() { 
	    int pSize = (int)((-1) *(double)meanPacketSize * Math.log(1-randomSizeGenerator.nextDouble()));
	    return (pSize!=0) ? pSize : pSize+1;
	}

	@Override
	/**
	 * returns generated packet size
	 * if constSize=true - returns pSize
	 * if constSize=false - calculates a new value 
	 */
	protected int getPacketSize() {
		return (constSize) ? meanPacketSize : calculatePacketSize();
	}
	
}