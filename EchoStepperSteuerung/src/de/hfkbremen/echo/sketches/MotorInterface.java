

package de.hfkbremen.echo.sketches;


import processing.core.PApplet;
import controlP5.*;
import oscP5.*;
import netP5.*;
import processing.opengl.*;

public class MotorInterface
        extends PApplet {

   private Serial mSerial;
   final int numberOfLeinwaende = 17;
   Leinwand[] leinwaende;
   ControlP5 controlP5;
   int margin = 10;
   int breiteSingleView;
   int hoeheSingleView;
   boolean drawLeinwaende;
   OscP5 oscObjekt;
   NetAddress oscEmpfaenger;
  
    public void setup() {
        size(1920,1200,OPENGL);//beim skalieren verhältnis beachten ca 16/10
        background(0);
        ellipseMode(CENTER);
        smooth();
        frameRate(25);
        //Serial.listPorts();
        drawLeinwaende=true;
        breiteSingleView=(width-9*margin)/8;
        hoeheSingleView=breiteSingleView+breiteSingleView/3;
        //mSerial = Serial.open("/dev/tty.SLAB_USBtoUART");
        //mSerial.write("#*@A\r");
        //mSerial.write("#*p2\r");
        //mSerial.write("#*o60\r");
        controlP5 = new ControlP5(this);
        leinwaende = new Leinwand[ numberOfLeinwaende ];
        for ( int i = 0; i < numberOfLeinwaende; i++ ) {
            if (i<=7){
                leinwaende[ i ] = new Leinwand(margin+i*(breiteSingleView+margin),margin,i);
                controlP5.addSlider("steps"+i,0,400,0,margin+i*(breiteSingleView+margin),margin+hoeheSingleView+margin,hoeheSingleView/2+margin,margin).setId(i);
            }

            
            if (i>7 && i<13){
                leinwaende[ i ] = new Leinwand(margin+(i-8)*(breiteSingleView+margin),margin+hoeheSingleView+margin+margin +margin,i);
                controlP5.addSlider("steps"+i,0,400,0,margin+(i-8)*(breiteSingleView+margin),(margin+hoeheSingleView+margin)*2+margin,hoeheSingleView/2+margin,margin).setId(i);
            }
            
            if (i>12){
                leinwaende[ i ] = new Leinwand(margin+(i-13)*(breiteSingleView+margin),(margin+hoeheSingleView+margin+margin)*2+margin,i);
                controlP5.addSlider("steps"+i,0,400,0,margin+(i-13)*(breiteSingleView+margin),(margin+hoeheSingleView+margin)*3+margin+margin,hoeheSingleView/2+margin,margin).setId(i);
            }

                
            
        }
        //oscObjekt = new OscP5(this,3000);
        //oscEmpfaenger = new NetAddress("David.local",3000);
        controlP5.addButton("Start",0,1200,10,50,20).setId(20);
        controlP5.controller("Start").moveTo("global");
        controlP5.tab("extra").activateEvent(true);
        controlP5.tab("extra").setId(18);
        controlP5.tab("default").activateEvent(true);
        controlP5.tab("default").setLabel("something");
        controlP5.tab("default").setId(19);
        
    }

    public void draw() {
        background(0);
        if (drawLeinwaende){
            for ( int i = 0; i < numberOfLeinwaende; i++ ) {
                leinwaende[ i ].display();  
            }
        }
        
        
   
    }
    
    public void keyPressed(){
        switch(key) {
            case('r'):
                for ( int i = 0; i < numberOfLeinwaende; i++ ) {
                controlP5.controller("steps"+i).setValue(0);
                }
                break;
            case('1'):
                controlP5.controller("steps"+0).setValue(100);
                controlP5.controller("steps"+1).setValue(0);
                controlP5.controller("steps"+2).setValue(100);
                controlP5.controller("steps"+3).setValue(0);
                controlP5.controller("steps"+4).setValue(100);
                controlP5.controller("steps"+5).setValue(0);
                controlP5.controller("steps"+6).setValue(100);
                controlP5.controller("steps"+7).setValue(0);
                controlP5.controller("steps"+8).setValue(100);
                controlP5.controller("steps"+9).setValue(0);
                controlP5.controller("steps"+10).setValue(100);
                controlP5.controller("steps"+11).setValue(0);
                controlP5.controller("steps"+12).setValue(100);
                controlP5.controller("steps"+13).setValue(100);
                controlP5.controller("steps"+14).setValue(0);
                controlP5.controller("steps"+15).setValue(100);
                controlP5.controller("steps"+16).setValue(0);
                
                break;
            case('2'):
                controlP5.controller("steps"+0).setValue(50);
                controlP5.controller("steps"+1).setValue(50);
                controlP5.controller("steps"+2).setValue(50);
                controlP5.controller("steps"+3).setValue(50);
                controlP5.controller("steps"+4).setValue(100);
                controlP5.controller("steps"+5).setValue(100);
                controlP5.controller("steps"+6).setValue(100);
                controlP5.controller("steps"+7).setValue(100);
                controlP5.controller("steps"+8).setValue(50);
                controlP5.controller("steps"+9).setValue(50);
                controlP5.controller("steps"+10).setValue(50);
                controlP5.controller("steps"+11).setValue(50);
                controlP5.controller("steps"+12).setValue(50);
                controlP5.controller("steps"+13).setValue(50);
                controlP5.controller("steps"+14).setValue(50);
                controlP5.controller("steps"+15).setValue(50);
                controlP5.controller("steps"+16).setValue(50);
                
                break;
            case('3'):
                for ( int i = 0; i < numberOfLeinwaende; i++ ) {
                controlP5.controller("steps"+i).setValue((int)random(400));
                }
                break;
            case('4'):
                if(drawLeinwaende==true){
                    drawLeinwaende=false;
                }else if (drawLeinwaende==false){
                    drawLeinwaende=true;
                }
                break;
        }        
            
        
        
        
        
    }
    
    public class Leinwand{
        
        int steps;
        float currentAngle;
      
        int id;
        int positionX;
        int positionY;
        int startTime;
        Leinwand(int X, int Y,int i) {
        
        steps = 0;
        currentAngle =0;
        positionX = X;
        positionY = Y;
        
        id=i;
        
        
        
        
        }
        
        void display(){
            pushMatrix();
            translate(positionX, positionY);
            fill(255);
            text("Motor "+id, margin/2, margin+margin/2); 
            text("Angle: "+(steps*0.9f)+"°", margin/2, (margin+margin/2)*2);
            strokeWeight(1);
            stroke(50);
            noFill();
            rect(0,0,breiteSingleView,hoeheSingleView);
            strokeWeight(3);
            stroke(255);
            ellipse(breiteSingleView/2,hoeheSingleView/2+hoeheSingleView/8, breiteSingleView-margin,breiteSingleView-margin);
            pushMatrix();
            translate(breiteSingleView/2,hoeheSingleView/2+hoeheSingleView/8);
            pushMatrix();
            noStroke();
            fill(0,54,82);
            arc(0,0,breiteSingleView/2,breiteSingleView/2,radians(currentAngle),radians((steps*0.9f)));
            strokeWeight(2);
            stroke(0,105,140);
            actualAngle();
            rotate(radians(currentAngle));
            triangle(breiteSingleView/3,0,
                    (breiteSingleView/3-margin/3),margin/3,
                    (breiteSingleView/3-margin/3),-margin/3);
            line(-breiteSingleView/3,0,breiteSingleView/3,0);
            popMatrix();
            rotate(radians(steps*0.9f));
            strokeWeight(3);
            stroke(255);
            triangle(breiteSingleView/3,0,
                    (breiteSingleView/3-margin/3),margin/3,
                    (breiteSingleView/3-margin/3),-margin/3);
            line(-breiteSingleView/3,0,breiteSingleView/3,0);
            popMatrix();
            ellipse(breiteSingleView/2,hoeheSingleView/2+hoeheSingleView/8,margin/2,margin/2);
            popMatrix();
        }
        
        float actualAngle(){
            float angle=currentAngle;
            if (millis()<=startTime+((steps*0.9)*(6000/360))){
               angle=angle+2.4f;
               currentAngle=angle;
            } else if (millis()>startTime+((steps*0.9)*(6000/360))&&currentAngle!=angle){
               currentAngle=(steps*0.9f);
            }
            return angle;
        }
           
           
             
            
        
        float angle(int schritte) {
            float angle=0;
            if (schritte*0.9f<=360){
                angle=schritte*0.9f;
            }else if (schritte*0.9f>360){
                angle=(schritte*0.9f)%360;
            }
            return angle;
        }
        
        
        
        
       
    }
    
    void controlEvent(ControlEvent theEvent) {    
            if (theEvent.isController()) {
                if(theEvent.controller().id()==20){
                    GO();
                }else{
                leinwaende[theEvent.controller().id()].steps=(int)theEvent.controller().value();
                }
            } else if (theEvent.isTab()) {
                if (theEvent.tab().id()==18){
                    drawLeinwaende=false;
                    println("tab : "+theEvent.tab().id()+" / "+theEvent.tab().name());
                }else if (theEvent.tab().id()==19){
                    drawLeinwaende=true;
                    println("tab : "+theEvent.tab().id()+" / "+theEvent.tab().name());
                }
            }
                  
            
        }
    
    public void GO(){
        //mSerial.write("#*A\r");
        //OscMessage myMessage = new OscMessage("Motorposition");
        //myMessage.add(180.8); /* add an int to the osc message */
        //oscObjekt.send(myMessage, oscEmpfaenger);
        /*for (int i=0; i<=numberOfLeinwaende;i++){ 
                leinwaende[i].startTime = millis();
                
            
        }*/
        println("und Go!");
    }
    
    

    public static void main(String[] args) {
        PApplet.main(new String[] {MotorInterface.class.getName()});
    }
}
