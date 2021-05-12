package corbaFilter;

import FilterApp.*;

import java.awt.Color;
import java.awt.Graphics;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;

import java.awt.image.BufferedImage;
import java.awt.image.PackedColorModel;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import javax.imageio.ImageIO;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

class FilterImpl extends FilterPOA {
	private ORB orb;

    public void setORB(ORB orb_val) {
        orb = orb_val; 
      }

    @Override
    public String getFilterImage(String path, int filter) {
        try {
            if(filter==0) {
                BufferedImage middleImage = ImageIO.read(new File(path));
                int color;
                for (int j = 0; j < middleImage.getHeight(); j = j + 2) {
                    for (int i = 0; i < middleImage.getWidth(); i++) {
                        color = middleImage.getRGB(i, j);
                        middleImage.setRGB(i, j, color - 150);
                    }
                }
                path = path.substring(0, path.lastIndexOf('/'));
                path = path + "/filter.jpg";
                ImageIO.write(middleImage, "jpg", new File(path));
                
            }else if(filter==1) {
            	BufferedImage grayImage = ImageIO.read(new File(path));
                int average;
                for(int i = 0; i < grayImage.getWidth(); i++) {
                    for(int j = 0; j < grayImage.getHeight(); j++) {
                        Color newColor = new Color(grayImage.getRGB(i, j));
                        average = (newColor.getRed() + newColor.getGreen() + newColor.getBlue())/3;
                        Color convertedColor = new Color(average, average, average);
                        grayImage.setRGB(i, j, convertedColor.getRGB());
                    }
                }
                path = path.substring(0, path.lastIndexOf('/'));
                path = path + "/filter.jpg";
                ImageIO.write(grayImage, "jpg", new File(path));
            }else if(filter == 2) {
            	BufferedImage oppositeImage = ImageIO.read(new File(path));
                for(int i = 0; i < oppositeImage.getWidth(); i++) {
                    for(int j = 0; j < oppositeImage.getHeight(); j++) {
                        oppositeImage.setRGB(i, j, 255 - oppositeImage.getRGB(i, j));
                    }
                }
                path = path.substring(0, path.lastIndexOf('/'));
                path = path + "/filter.jpg";
                ImageIO.write(oppositeImage, "jpg", new File(path));
            }else if(filter==3) {
            	BufferedImage flipedImage = ImageIO.read(new File(path));
                for(int i = 0; i < flipedImage.getWidth(); i++) {
                    for(int j = 0; j < flipedImage.getHeight(); j++) {
                        if((flipedImage.getWidth() % 2 == 1) && (Math.floor(flipedImage.getWidth()/2) == i)) {
                            int aux = flipedImage.getRGB(i, j);
                            flipedImage.setRGB(i, j, flipedImage.getRGB(flipedImage.getWidth()-i-1, j));
                            flipedImage.setRGB(flipedImage.getWidth()-i-1, j, aux);
                        }else if((flipedImage.getWidth() % 2 == 0) && (Math.ceil(flipedImage.getWidth()/2) <= i)) {
                            int aux = flipedImage.getRGB(i, j);
                            flipedImage.setRGB(i, j, flipedImage.getRGB(flipedImage.getWidth()-i-1, j));
                            flipedImage.setRGB(flipedImage.getWidth()-i-1, j, aux);
                        }
                    }
                }
                path = path.substring(0, path.lastIndexOf('/'));
                path = path + "/filter.jpg";
                ImageIO.write(flipedImage, "jpg", new File(path));
            }else if (filter ==4) {
            	Process process = Runtime.getRuntime().exec("python C:/Users/fer27/OneDrive/Escritorio/Distribuidos/filterDark.py");
            	path = path.substring(0, path.lastIndexOf('/'));
                path = path + "/result.jpg";
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return path;
    }

    @Override
    public void shutdown() {
        orb.shutdown(false);
    }
    
}

public class ServerFilterCorba{
	 public static void main(String args[]) {
	      try{


	        // create and initialize the ORB
	        ORB orb = ORB.init(args, null);

	        // get reference to rootpoa & activate the POAManager
	        POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	        rootpoa.the_POAManager().activate();

	        // create servant and register it with the ORB
	        FilterImpl filterImpl = new FilterImpl();
	        filterImpl.setORB(orb); 

	        // get object reference from the servant
	        org.omg.CORBA.Object ref = rootpoa.servant_to_reference(filterImpl);
	        Filter href = FilterHelper.narrow(ref);

	        // get the root naming context
	        // NameService invokes the name service
	        org.omg.CORBA.Object objRef =
	            orb.resolve_initial_references("NameService");
	 // Use NamingContextExt which is part of the Interoperable
	        // Naming Service (INS) specification.
	        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);  
	        // bind the Object Reference in Naming
	        String name = "Filter";
	        NameComponent path[] = ncRef.to_name( name );
	        ncRef.rebind(path, href);
	  
	        // wait for invocations from clients
	        	System.out.println("Filter Server ready and waiting ...");
	        	orb.run();
	      } 
	          
	        catch (Exception e) {
	          System.err.println("ERROR: " + e);
	        }
	            
	        System.out.println("HelloServer Exiting ...");
	          
	    }
}

