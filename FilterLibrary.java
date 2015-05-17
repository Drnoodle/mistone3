
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;



public class FilterLibrary {
	
	
	private PApplet applet;
	
	private ImageFilter hueLowPass; 
	private ImageFilter hueHighPass; 
	
	private ImageFilter greenDetect; 
	private ImageFilter binary;
	private ImageFilter blur;
	private ImageFilter sobel;
	
	
	public FilterLibrary(PApplet applet){
		this.applet = applet;
		
		greenDetect =  new ColorDetect(applet.color(6,95,95), 100);
		binary = new FilterLibrary.Binary(30);
		blur  = new FilterLibrary.Kernel( FilterLibrary.blurMatrix, 90);
		sobel = new  FilterLibrary.Sobel();
		hueLowPass = new HueLowerBound(105);
		hueHighPass = new HueUpperBound(140);
	}
	
	
	

	public ImageFilter getFilterForDetection(){
		
		List<ImageFilter> filter = new ArrayList<>();
		filter.add(hueLowPass);
		filter.add(hueHighPass);
		filter.add(greenDetect);
		for(int i = 0; i<20; i++)filter.add(blur);
		filter.add(binary);
		filter.add(sobel);

		
		return new Accumulator(filter);
	}

	
	

	public class Accumulator implements ImageFilter{
		
		List<ImageFilter> filters;
		
		public Accumulator( List<ImageFilter> filters ){		
		this.filters = filters;
		}
		

		@Override
		public PImage convertImage(PImage img) {
			
			PImage newImg = img.get(); 
	       
			for(ImageFilter filter : filters) newImg = filter.convertImage(newImg);
			
			return newImg;
		}
		
	}
	
	
	
	
	
	
	
	
	public class Binary implements ImageFilter{
		
		private float threshold; 
		
		public Binary(float threshold){	
			this.threshold = threshold;
		}
		

		@Override
		public PImage convertImage(PImage img) {
			
			PImage newImg = applet.createImage(img.width,  img.height, PImage.ALPHA );
			
			for(int i = 0; i < img.height*img.width; i++){
				newImg.pixels[i] = applet.brightness(img.pixels[i])  >  threshold   ? 
								   applet.color(255) : applet.color(0);
			}
			
			return newImg;
		}
		
	}
	

	
	
	
	
	public class BinaryInverted implements ImageFilter{
		
		private float threshold;
		
		public BinaryInverted(float threshold){		
			this.threshold = threshold;
		}
		
		
		@Override
		public PImage convertImage(PImage img) {
			
			PImage newImg = applet.createImage(img.width,  img.height, PImage.ALPHA );
			
			for(int i = 0; i < img.height*img.width; i++){
				newImg.pixels[i] = applet.brightness(img.pixels[i]) > threshold ? 
						           applet.color(0) : applet.color(255);
			}
			
			return newImg;
		}
		
		
	}
	
	
	
	
	
	
	
	
	public class HueLowerBound implements ImageFilter{
		
		private float threshold;
		public HueLowerBound(float threshold){	
			this.threshold = threshold;
		}
		
		
		@Override
		public PImage convertImage(PImage img) {
			
			PImage newImg = applet.createImage(img.width,  img.height, PImage.ALPHA );
			
			for(int i = 0; i < img.height*img.width; i++){
				newImg.pixels[i] = applet.hue(img.pixels[i]) < threshold ? 
								   applet.color(0) : img.pixels[i];
			}
			
			return newImg;
		}
		
	}
	
	
	
	
	
	
	
	public class HueUpperBound implements ImageFilter{
		
		private float threshold;
		
		public HueUpperBound(float threshold){	
			this.threshold = threshold;
		}
		
		
		@Override
		public PImage convertImage(PImage img) {
			
			PImage newImg = applet.createImage(img.width,  img.height, PImage.ALPHA );
			
			for(int i = 0; i < img.height*img.width; i++){
				newImg.pixels[i] = applet.hue(img.pixels[i]) > threshold ? 
								   applet.color(0) : img.pixels[i];
			}
			
			return newImg;
		}
		
	}	
	
	
	
	
	
	
	public class ColorDetect implements ImageFilter{
		
		private PVector color;
		private int threshold;
		
		public ColorDetect(int color, int distance){	
			this.color = new PVector(applet.red(color), applet.green(color), applet.blue(color));
			this.threshold = distance;
		}
		
		private int convertPix(int pix){
			
			PVector pixVect = new PVector(applet.red(pix), applet.green(pix), applet.blue(pix));
			
			float dist = PVector.dist(color, pixVect);
			
			if(dist < threshold) return pix;
			else return applet.color(0);
		}
		
		@Override
		public PImage convertImage(PImage img) {
			
			PImage newImg = applet.createImage(img.width,  img.height, PImage.ALPHA );
			
			for(int i = 0; i < img.height*img.width; i++){
				newImg.pixels[i] = convertPix(img.pixels[i]);
			}
			
			return newImg;
		}
		
	}	
	

	

	
	
	public static float[][] brightnessMatrix = {
			{0,0,0},
			{0,2,0},
			{0,0,0}
		};
	
	public static float[][] blurMatrix = {
			{9,12,9},
			{12,15,12},
			{9,12,9}
		};
	

public class Kernel implements ImageFilter{

	private float[][] matrix;
	private int middleX, middleY;
	private float weight;
	
	
	public Kernel(float[][] matrix, float weight){
		
		if(matrix.length == 0 || matrix.length % 2 == 0 || 
		 matrix[0].length == 0 || matrix[0].length % 2 == 0)
		throw new IllegalArgumentException();
		
		this.matrix = matrix.clone();
		
		this.middleX = matrix[0].length/2;
		this.middleY = matrix.length/2;
		
		this.weight= weight;
	}
	
	
	
	
	@Override
	public PImage convertImage(PImage image) {
		
		if(image.width< matrix.length || image.height < matrix[0].length) throw new IllegalArgumentException();

		image.loadPixels();
		
		PImage result = new PImage(image.width, image.height);
		
		
		for(  int x = middleX;     x < image.width - middleX;     x++   )  
		for(  int y = middleY;     y < image.height - middleY;    y++   ){
			// ----------------------------- compute pixel : x,y ---------------------------------	

		    int red = 0, blue = 0, green = 0;
		    
		    
		    // genere barycenter
		    
		    for( int i =  - middleX;  i<= middleX;  i++ ) for( int j = -middleY;  j <=  middleY;  j++ ) {
		    	
		    	float coeff = matrix[ middleX + i ][ middleY + j ];
		    	
		    	int value = image.pixels[(x+i)+(y+j)*image.width]; 

		        red   += (int)( coeff/weight * applet.red(value));
		        green += (int)( coeff/weight * applet.green(value));
		        blue  += (int)( coeff/weight * applet.blue(value));


		    }

		    result.pixels[x+y*image.width] = applet.color((int)red, (int)green, (int)blue) ; 

		    
		    //------------------------------------------------------------------------------------
		}
		
		result.loadPixels();
		
		return result;

	}
	

	
	
	
	
}
	
	
	








public class Sobel implements ImageFilter {


	private float[][] hKernel = { { 0, 1, 0 },
								  { 0, 0, 0 },
			                      { 0,-1, 0 }  };
	
	private float[][] vKernel = { { 0, 0, 0 },
			  					  { 1, 0,-1 },
			  					  { 0, 0, 0 }  };

	
	
	@Override
	public PImage convertImage(PImage img) {
		

		PImage result = applet.createImage(img.width, img.height, PImage.ALPHA);
		

		for (int i = 0; i < img.width * img.height; i++) 
		result.pixels[i] = applet.color(0);
		
		
		float max=0;
		float[] buffer = new float[img.width * img.height];
		
		
		for(int y =0; y< img.height; y++)  for(int x =0; x < img.width ; x++){
				
				int sumh=0, sumv=0;
				
				
				for(int i=0; i<3; i++) for(int j=0; j<3; j++){
						
					int clampedX =  (x+i-1 < 0)? 0 : ( (x+i-1 > img.width-1)? img.width-1 : x+i-1);
					int clampedY = (y+j-1 < 0)? 0 :  ( (y+j-1 > img.height-1)? img.height-1 : y+j-1);
						
					sumh += applet.brightness(img.pixels[clampedX + clampedY*img.width])*hKernel[i][j];
					sumv += applet.brightness(img.pixels[clampedX + clampedY*img.width])*vKernel[i][j];
						
					}
				
				
				buffer[y*img.width + x] = (float)Math.sqrt(sumh*sumh + sumv*sumv);
				
				if( buffer[y*img.width + x] > max ) max = buffer[y*img.width + x];
		}
		
		
			
		for (int y = 2; y < img.height - 2; y++)  
		for (int x = 2; x < img.width - 2; x++)  
		if (buffer[y * img.width + x] > (int)(max * 0.3f)) {
			    result.pixels[y * img.width + x] = applet.color(255);
		}
	    else 	result.pixels[y * img.width + x] = applet.color(0);
				
			
		
		return result;
	}	




	


	
}











}





