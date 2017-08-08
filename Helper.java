package engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A Class with handy functions
 * @author macso
 * 
 */
public final class Helper {
	public static boolean DEBUG; 		//I set this value when SystemWithVacations object is created
	public static boolean DEBUG_QUEUE;	//I set this value when SystemWithVacations object is created
	public static boolean SLOTED;		//is the system sloted/continous - I set this value when SystemWithVacations object is created
	public static boolean PRIORITIES;
	public static boolean isAfterStart;
	public static boolean SAVE_PACKET_TRACE;
	public static boolean DISPLAY_DETAILS_FOR_EACH_SIM;
	public static boolean DEBUG_SYSTEM_STATE;
	public static double MAX_SIM_TIME;
	public static double START_COLLECT_TIME;
	public static String FILENAME_DATE=getCurrDate();
	public static String FILENAME;
	public static int ROUND_DEC=4;
	private Helper(){}
	
	public static double roundDouble(double x, int decimalPlaces){
		double result=x*Math.pow(10, decimalPlaces);
		long l=(long)Math.round(result);
		result=l/Math.pow(10, decimalPlaces);
		return result;
	}
	/**
	 * creates path to a file
	 * @param filename  path to the file e.g "D:/data/result.txt"
	 */
	public static void createPath(String filename){
		File targetFile = new File(filename);
		File parent = targetFile.getParentFile();
		if(!parent.exists() && !parent.mkdirs()){
		    throw new IllegalStateException("Couldn't create dir: " + parent);
		}
	}
	public int[] resetValues(int[] a){
		return new int[a.length];
	}
	public int[][] resetValues(int[][] a){
		return new int[a.length][a[0].length];
	}
	
	public static void printPacketStatistic(double t, Packet p){
		if(isAfterStart){
			//You should create a File at first.
			String filename="D:/wyniki/"+FILENAME+"/Packet-Trace-RI"+p.getPcktRI()+".txt";
			double tCreation=Helper.roundDouble(p.getTCreation(),ROUND_DEC);
			String data=""
					+p.getPcktNr()+"\t"
					+p.getPcktRI()+"\t"
					+tCreation+"\t"+
					+p.getTTaken()+"\t";
			//if not taken <-> packet loss <-> tTaken==-1
			String tInQueue = (p.getTTaken()!=-1) ? ""+Helper.roundDouble(p.getTTaken()-tCreation,ROUND_DEC)+"\t" : "***\t";
			data=data+tInQueue;		
			String tInQueueFullSlots = (p.getTTaken()!=-1) ? ""+(int)(p.getTTaken()-tCreation+1) : "***";
			data=data+tInQueueFullSlots;	
			writeToFile(filename,data);
		}
	}
	/**
	 * Normalizes values of amount to probability
	 * -tab------tabNorm
	 * [0]=2-----[0]=0.1
	 * [1]=4-----[1]=0.2
	 * [2]=6-----[2]=0.3
	 * [3]=8-----[3]=0.4
	 * 
	 * @param tab array with amounts
	 * @return	array with normalized probabilities
	 */
	public static double[] normalizeDistr1D(int[] tab){
		int size=tab.length;
		double[] tabNorm= new double[size];
		int noProbes=0;
		for (int i=0;i<size;i++)
			noProbes=noProbes+tab[i];
		for (int i=0;i<size;i++)
			tabNorm[i]=Helper.roundDouble((double)tab[i]/(double)noProbes,ROUND_DEC);
		return tabNorm;
	}
	/**
	 * Normalizes values of amount to probability per each row
	 * ---tab--------tabNorm
	 * [0][0]=2-----[0][0]=0.1
	 * [0][1]=4-----[0][1]=0.2
	 * [0][2]=6-----[0][2]=0.3
	 * [0][3]=8-----[0][3]=0.4
	 * 
	 * @param tab 2D array with amounts 
	 * @return	array with normalized probabilities
	 */
	public static double[][] normalizeDistr2D(int[][] tab){
		int size1=tab.length;
		int size2=tab[0].length;
		double[][] tabNorm= new double[size1][size2];
		//per row
		for (int i=0;i<size1;i++){
			tabNorm[i]=normalizeDistr1D(tab[i]);
		}
		return tabNorm;
	}
	public static double[][][] normalizeDistr3D(int[][][] tab){
		int size1=tab.length;
		int size2=tab[0].length;
		int size3=tab[0][0].length;
		double[][][] tabNorm= new double[size1][size2][size3];
		//per row
		for (int i=0;i<size1;i++){
				tabNorm[i]=normalizeDistr2D(tab[i]);		
		}
		return tabNorm;
	}
	public static String print1D(int[] array){
		String row="[";
		for (int i : array)
			row=row+i+ " ";
		row=row+"]";
		return row;
	}
	public static String print1D(double[] array){
		String row="[";
		for (double d : array)
			row=row+d+ " ";
		row=row+"]";
		return row;
	}
	//OLD ONES - now I use print1D function within print2D
	/*public static String print2D(int[][] array){
		String row="[";
		for (int j=0;j<array.length;j++){
			row=row+"[";
			for (int i : array[j])
				row=row+i+ " ";
			row=row+"];";
		}
		row=row.substring(0, row.length()-1)+"]";
		return row;
	}
	public static String print2D(double[][] array){
		String row="[";
		for (int j=0;j<array.length;j++){
			row=row+"[";
			for (double d : array[j])
				row=row+d+ " ";
			row=row+"];";
		}
		row=row.substring(0, row.length()-1)+"]";
		return row;
	}*/
	public static String print2D(int[][] array){
		String row="[";
		for (int j=0;j<array.length;j++){
			row=row+print1D(array[j])+";";
		}
		row=row.substring(0, row.length()-1)+"]";
		return row;
	}
	public static String print2D(double[][] array){
		String row="[";
		for (int j=0;j<array.length;j++){
			row=row+print1D(array[j])+";";
		}
		row=row.substring(0, row.length()-1)+"]";
		return row;
	}
	
	public static String print3D(double[][][] array){
		String row="[";
			for (int j=0;j<array.length;j++){
			row=row+print2D(array[j])+";";			
		}
		row=row.substring(0, row.length()-1)+"]";
		return row;
	}
	public static String print3D(int[][][] array){
		String row="[";
			for (int j=0;j<array.length;j++){
				row=row+print2D(array[j])+";";			
			}
			row=row.substring(0, row.length()-1)+"]";
			return row;
	}
	/**
	 * @return current date (Timezone UTC+1) in specified format
	 */
	public static String getCurrDate(){
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
		Date date=new Date();
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
		System.out.println(dt.format(date).toString());
		return dt.format(date).toString();
	}
	
	public static void writeToFile(String filename, String data){
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter out = null;
		try {
		    fw = new FileWriter(filename, true);
		    bw = new BufferedWriter(fw);
		    out = new PrintWriter(bw);
		    out.println(data);
		    out.close();
		} catch (IOException e) {
		    System.out.println("IOException - write to file 1"+ e);
		}
		finally {
		    if(out != null)
			    out.close();
		    try {
		        if(bw != null)
		            bw.close();
		    } catch (IOException e) {
		    	System.out.println("IOException - write to file 2"+ e);
		    }
		    try {
		        if(fw != null)
		            fw.close();
		    } catch (IOException e) {
		    	System.out.println("IOException - write to file 3"+ e);
		    }
		}
	}
}
