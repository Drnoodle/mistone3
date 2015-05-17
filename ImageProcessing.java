

import java.util.List;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;



public class ImageProcessing extends PApplet {

	
	private static final long serialVersionUID = 1L;

	private PImage initialImg = loadImage("board4.jpg");
	
	private QuadGraph quads;
	List<PVector> lines;
	
	private Hough hough;
	
	
	public void setup(){
		
		size(800,600);
		

		ImageFilter filter = new FilterLibrary(this).getFilterForDetection();
		
	    PImage img = filter.convertImage(initialImg);	
	
	    this.hough = new Hough(img).neighbourhoodFilter().takeBest(4);

	    lines = this.hough.vectlines();

	    quads = new QuadGraph();
	    quads.build( lines, img.width, img.height);
	    quads.findCycles(lines);
	    
	    
	    
	    noLoop();
	    
	}
	


	
	
	public void draw(){

		image(initialImg,0,0);
		
		stroke(color(255,0,0));



	    //imgFiltered.reloadEffect(); 
		
		for(int[]quad: quads.cycles){
	    	PVector l1=lines.get(quad[0]);
	    	PVector l2=lines.get(quad[1]);
	    	PVector l3=lines.get(quad[2]);
	    	PVector l4=lines.get(quad[3]);
	    // (intersection() is a simplified version of the
	    // intersections() method you wrote last week, that simply
	    // return the coordinates of the intersection between 2 lines)
	    PVector c12=Line.intersections( l1, l2);
	    PVector c23=Line.intersections(l2,l3);
	    PVector c34=Line.intersections(l3, l4);
	    PVector c41=Line.intersections(l4,l1);
	    // Choose a random, semi-transparent colour
	    Random random = new Random();
	    fill(color(min(255, random.nextInt(300))
	    		, min(255, random.nextInt(300))
	    		, min(255, random.nextInt(300)), 200));
	    
	    quad(c12.x, c12.y, c23.x, c23.y, c34.x,  c34.y,  c41.x,  c41.y);
	    }

	}
	


}




