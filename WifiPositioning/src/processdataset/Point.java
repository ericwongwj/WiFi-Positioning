package processdataset;

public class Point{
	public double x;
	public double y;
	
	@Override
	public String toString() {
		return "("+x+","+y+")";
	}
	
	public Point(double a,double b) {
		x=a;
		y=b;
	}
	
	public double distance(Point p){
		return Math.sqrt((p.x-x)*(p.x-x)+(p.y-y)*(p.y-y));
	}
	
	public double Xdistance(Point p){
		return (p.x-x);
	}
	
	public double Ydistance(Point p){
		return (p.y-y);
	}
	
	public static void main(String[] args) {
		Point p1=new Point(0.0, 0.0);
		Point p2=new Point(1.0, 1.0);
		System.out.println(p1);
		System.out.println(p1.distance(p2));
	}
}
