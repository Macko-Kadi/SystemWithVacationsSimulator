package engine;

import java.util.ArrayList;

public class StatsRecord {
 protected double EN;
 protected double EW;
 protected double EWSlot;
 protected double PLoss;
 protected double[] distrPcktComes;
 protected double[][] distrPcktBefore;
 protected double[][] distrPcktAfter;
 
 public StatsRecord(double EN_, double EW_, double EWSlot_, double PLoss_, 
		 			double[] distrPcktComes_, double[][] distrPcktBefore_,  double[][] distrPcktAfter_){
	 EN=EN_;
	 EW=EW_;
	 EWSlot=EWSlot_;
	 PLoss=PLoss_;
	 distrPcktComes=distrPcktComes_;
	 distrPcktBefore=distrPcktBefore_;
	 distrPcktAfter=distrPcktAfter_;
}

 public static void printMeanStats(StatsRecord[] srs){
			String filename="D:/wyniki/"+Helper.FILENAME+"/_STATISTICAL_.txt";
			double EN=srs[0].EN;
			double ENInt=srs[1].EN;
			double EW=srs[0].EW;
			double EWInt=srs[1].EW;
			double EWSlot=srs[0].EWSlot;
			double EWSlotInt=srs[1].EWSlot;
			double Ploss=srs[0].PLoss;
			double PlossInt=srs[1].PLoss;
			double[] distrCome=srs[0].distrPcktComes;
			double[] distrComeInt=srs[1].distrPcktComes;
			
			//TBD - reszta parametrów
			String data="========STATISITCAL_QUEUE:0================"
					+ "\nE[N]=\t"+EN+" +- "+ENInt+
						"\nPloss=\t"+Ploss+"+- "+PlossInt+
						"\nE[W]=\t"+EW+"+- "+EWInt+
						"\nE[W_SLOT]=\t"+EWSlot+"+- "+EWSlotInt;
			System.out.println(data);
			Helper.writeToFile(filename,data);
			//Distribution when packet comes
			filename="D:/wyniki/"+Helper.FILENAME+"/_STATISTICAL_-DistrCome.txt";	
			System.out.println("-----------distrPcktComes----------");
			data=Helper.print1D(distrCome);
			System.out.println(data);
			Helper.writeToFile(filename,data);
			data=Helper.print1D(distrComeInt);
			System.out.println(data);
			Helper.writeToFile(filename,data);
			
			//TBD - jak mi siê zachce.

			/*double[] distrCome=Helper.normalizeDistr1D(occupancyDistributionPacketComes);
			data=Helper.print1D(distrCome);
			System.out.println("-----------distrPcktComes----------");
			System.out.println(data);
			Helper.writeToFile(filename,data);
			//Distribution just before
			
			filename="D:/wyniki/"+Helper.FILENAME+"/Queue-"+Q_ID+"-DistrBefore.txt";
			double[][] distrBefore=Helper.normalizeDistr2D(occupancyDistributionBefore);
			data=Helper.print2D(distrBefore);
			System.out.println("-------------distrBefore-----------");
			System.out.println(data);
			
			Helper.writeToFile(filename,data);
			//Distribution just after
			filename="D:/wyniki/"+Helper.FILENAME+"/Queue-"+Q_ID+"-DistrAfter.txt";
			double[][] distrAfter=Helper.normalizeDistr2D(occupancyDistributionAfter);
			data=Helper.print2D(distrAfter);
			System.out.println("-------------distrAfter-----------");
			System.out.println(data);
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
			StatsRecord sr=new StatsRecord(EN,EW,EWSlot,Ploss,numberOfLosses,numberOfArrivals,distrCome,distrBefore,distrAfter);
			return sr;		
		}*/
 }
 //Do sprawdzenia compute2D
 public static StatsRecord[] computeMeanAndConfidenceInterval(ArrayList<StatsRecord> listStats_){
	 StatsRecord[] expectedValueAndConfIntervalStatsRecord= new StatsRecord[2];
	 ArrayList<Double> lEN=new ArrayList<Double>();
	 ArrayList<Double> lEW=new ArrayList<Double>();
	 ArrayList<Double> lEWSlot=new ArrayList<Double>();
	 ArrayList<Double> lPLoss=new ArrayList<Double>();
	 ArrayList<double[]> ldistrPcktComes=new ArrayList<double[]>();
	 ArrayList<double[][]> ldistrPcktBefore=new ArrayList<double[][]>();
	 ArrayList<double[][]> ldistrPcktAfter=new ArrayList<double[][]>();
	 
	 //add values from each simulation
	 for (StatsRecord sr : listStats_){
		 lEN.add(sr.EN);
		 lEW.add(sr.EW);
		 lEWSlot.add(sr.EWSlot);
		 lPLoss.add(sr.PLoss);
		 ldistrPcktComes.add(sr.distrPcktComes);
		 ldistrPcktBefore.add(sr.distrPcktBefore);
		 ldistrPcktAfter.add(sr.distrPcktAfter);
	 }
	 double[] valEN=computeSingle(lEN,2);
	 double[] valEW=computeSingle(lEW,2);
	 double[] valEWSlot=computeSingle(lEWSlot,2);
	 double[] valPLoss=computeSingle(lPLoss,5);
	 double[][] valComes=compute1D(ldistrPcktComes,4);
	 double[][][] valBefore=compute2D(ldistrPcktBefore,4);
	 double[][][] valAfter=compute2D(ldistrPcktAfter,4);
	 
	 expectedValueAndConfIntervalStatsRecord[0]=new StatsRecord(valEN[0], valEW[0], valEWSlot[0], valPLoss[0],
			 valComes[0], valBefore[0], 
			 valAfter[0]);
	 
	 expectedValueAndConfIntervalStatsRecord[1]=new StatsRecord(valEN[1], valEW[1], valEWSlot[1], valPLoss[1],
			 valComes[1], valBefore[1], 
			 valAfter[1]);
	 
	 return expectedValueAndConfIntervalStatsRecord;
 }
 /**
  * 
  * @param values ArrayList of single values (like EN, EW, distrPcktComes[1], distrPcktBeforeAmount[2][4];)
  * @return expected value (expectedValueAndConfInterval[0]) and 95% confidence interval (expectedValueAndConfInterval[1])
  */
 public static double[] computeSingle(ArrayList<Double> values, int dec){
	 double[] expectedValueAndConfInterval=new double[2];
	 double sum=0;
	 double std=0;
	 double stdSqTemp=0;
	 double amount=(double)values.size();
	 for (double v : values){
		 sum=sum+v;				  //sum of values
		 stdSqTemp=stdSqTemp+v*v; //sum of squares of the value
		//the sums divided by amount will estimate expected values
	 }
	 double expectedValue=(sum/amount);
	 std=Math.sqrt((stdSqTemp)/amount-(expectedValue)*(expectedValue)); //standard deviation D[X]=sqrt(E[X^2]-(E[X])^2)
	 double confInterv=std/Math.sqrt(amount)*(get95TStudentParamether((int)amount));
	 expectedValueAndConfInterval[0]=Helper.roundDouble(expectedValue,dec);
	 expectedValueAndConfInterval[1]=Helper.roundDouble(confInterv,dec);
	 return expectedValueAndConfInterval;
 }

 /**
  *
  * DistrCome - distribution of buffor occupancy seen by a coming packet
  * 
  * @param arrValues ArrayList of arrays, like distrPcktComes[], distrPcktBeforeAmount[2][];)
  * @return array of expected values (expectedValueAndConfInterval[][0]) and 95% confidence intervals (expectedValueAndConfInterval[][1])
  * 
  */
 public static double[][] compute1D(ArrayList<double[]> arrValues, int dec){
	 double[][] expectedValueAndConfInterval=new double[2][arrValues.get(0).length];
	 double[][] temp=new double[arrValues.get(0).length][2];
	 for (int i=0; i<arrValues.get(0).length;i++){				//for each value of n (distrCome[n])
		 ArrayList<Double> values=new ArrayList<Double>();		
		 for (int k=0; k<arrValues.size();k++){					//for each simulation = list length
			 values.add(arrValues.get(k)[i]);					//take value from i-th place of distrCome from each sim 
		 }
	 		temp[i]=computeSingle(values,dec);  //expected number (confInterv) of i-th value
	 		expectedValueAndConfInterval[0][i]=temp[i][0];
	 		expectedValueAndConfInterval[1][i]=temp[i][1];
	 }
	 for (int i=0; i<arrValues.get(0).length;i++){	 
	 }
	 return expectedValueAndConfInterval;
 }
 /**
  * 
  * @param arrValues ArrayList of 2D array, like distrPcktBeforeAmount[][];)
  * @return array of expected values (expectedValueAndConfInterval[][0]) and 95% confidence intervals (expectedValueAndConfInterval[][1])
  */
 public static double[][][] compute2D(ArrayList<double[][]> arrArrValues,int dec){
	 double[][][] expectedValueAndConfInterval=new double[arrArrValues.get(0).length][arrArrValues.get(0)[0].length][2];
	 /**
	  * ArrayList of Distr[slot][n]
	  * 
	  */
	 for (int i=0; i<arrArrValues.get(0)[0].length;i++){ 	//for each n
		 for (int j=0; j<arrArrValues.get(0).length;j++){ 	//for each slot
			 ArrayList<Double> values=new ArrayList<Double>();
			 for (int k=0; k<arrArrValues.size();k++){ 		//for each sim
				 values.add(arrArrValues.get(k)[j][i]);
			 }
			 expectedValueAndConfInterval[j][i]=computeSingle(values,dec);
		 }
	 }
	 return expectedValueAndConfInterval;
 }

 /**
  * 
  * 
  * @param amountOfTests
  * @return value of TStudent paramether for given amount of tests
  */
 public static double get95TStudentParamether(int amountOfTests){
	 if (amountOfTests==0)
		 System.out.println("ERR: StatsRecord / get95TStudent - amountOfTests=0 - You should never see this...");
	 if (amountOfTests>20)
		 return 2.04;
	 /**
	  * Values for first 20 amounts of tests
	  */	
	 double[] TS={
			 12.7062,
	         4.3029,
	         3.1824,
			 2.7764,
			 2.5706,
			 2.4469,
			 2.3646,
			 2.306,
			 2.2622,
			 2.2281,
			 2.201,
			 2.1788,
			 2.1604,
			 2.1448,
			 2.1314,
			 2.1199,
			 2.1098,
			 2.1009,
			 2.093,
			 2.086,
			 2.0796,
			 2.0739,
			 2.0687,
			 2.0639,
			 2.0595,
			 2.0555,
			 2.0518,
			 2.0484,
			 2.0452,
			 2.0423,
			 2.04
			 };
	 return TS[amountOfTests-1];
 }
}
