package engine;

public class PacketDelay {
	int number;
	double delay;
	double simTime;
	double timeIn;
	double timeOut;
	PacketDelay(int nr, double tSim, double tIn, double tOut )
	{
		number=nr;
		simTime=Helper.roundDouble(tSim,2);
		timeIn=Helper.roundDouble(tIn,2);
		timeOut=Helper.roundDouble(tOut,2);
		delay=Helper.roundDouble(timeOut-timeIn,2);
	}
}
