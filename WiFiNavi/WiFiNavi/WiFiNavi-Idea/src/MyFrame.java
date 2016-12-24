import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;

public class MyFrame extends Frame{
	
	double [][] estimated_pos;
	double [][] pos;
	int r;
	
	public MyFrame(double [][] _estimated_pos, double [][] _pos, int r) {
		super("Display "+r);
		setLayout(new FlowLayout());
		this.estimated_pos = _estimated_pos;
		this.pos = _pos;
		this.r = r;
	}
	
	public void display() {
		this.setTitle("PosFrame "+r);
		this.setSize(800,600);
		this.setLocation (80,60);
		this.setBackground (Color.white);

		this.setVisible(true);
	}
	 
	public void paint(Graphics g) {
		double minx = 10000.0, maxx = -10000.0, miny = 10000.0, maxy = -10000.0;
		for(int i = 0; i < r; i ++) {
			if (this.estimated_pos[i][0] < minx)
				minx = this.estimated_pos[i][0];
			if (this.estimated_pos[i][0] > maxx)
				maxx = this.estimated_pos[i][0];
			if (this.estimated_pos[i][1] < miny)
				miny = this.estimated_pos[i][1];
			if (this.estimated_pos[i][0] > maxy)
				maxy = this.estimated_pos[i][1];
			if (this.estimated_pos[i][0] < minx)
				minx = this.estimated_pos[i][0];
		}
		double mulx = 400.0 / (maxx - minx);
		double muly = 300.0 / (maxy - miny);
		double mul = (mulx < muly)? mulx : muly;
		double disx = 400.0 - (maxx + minx) / 2.0 * mul;
		double disy = 300.0 - (maxy + miny) / 2.0 * mul;
		
		
		super.paint(g);
		for(int i = 0; i < r; i ++) {
			double x = mul * this.estimated_pos[i][0] + disx;
			double y = mul * this.estimated_pos[i][1] + disy;
			double red = this.pos[i][0]/50.0;
			double blue = this.pos[i][1]/20.0;
			paint_node(x, y, red, blue, g);
		}
	}
	
	public void paint_node(double x, double y, double red, double blue, Graphics g) {
		int d = 10;
		
		g.setColor(new Color(1-(float)red, (float)0.8, 1-(float)blue));
		g.fillOval((int)x, (int)y, d, d);
	}

}
