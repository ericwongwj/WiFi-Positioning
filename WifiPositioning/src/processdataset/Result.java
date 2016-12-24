package processdataset;

import java.util.ArrayList;
import java.util.List;

public class Result {
	public List<Point> points=new ArrayList<>();
	public List<Double> deviations=new ArrayList<>();
	public double averageDeviation;
	public double variance;
	
	void addResult(Point point, double deviation){
		points.add(point);
		deviations.add(deviation);
	}
	
	/**计算总体的误差和方差*/
	void calculateOverAllDeviationAndVariance(){
		double deviationsum=0.0;
		for(double d: deviations){
			deviationsum+=d;
		}
		double avgdeviation=deviationsum/points.size();
		
		double variancesum=0.0;
		for(double d:deviations){
			variancesum+=(d-avgdeviation)*(d-avgdeviation);
		}
		double variance=Math.sqrt(variancesum);
		System.out.println("average deviation:"+avgdeviation+"  variance:"+variance);
	}
	
	public Result(){
		
	}
	
	public static void main(String[] args) {
		int a=7,b=17;
		double c=11;
		System.out.println(a/c);
		System.out.println(b/a);System.out.println(c/a);
	}
	
}
