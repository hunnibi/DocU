#include <Servo.h>

Servo myservo; 
int x=1;
unsigned long currentTime;
unsigned long startTime;
int v=0;
int t;
int y=0;
unsigned long currentTime2;
unsigned long startTime2;
const long interval = 1200;
int times=1;
int pos=0;
char message1;
char message2;
int dragTimes=0;
void setup() {
   Serial.begin (9600);
 pinMode (2,INPUT);
 startTime=millis();
 myservo.attach(9);
 
}

void loop() 
{   
    
   
    if(isNearMagnet())//if the hall sensor is near the magnet
    {
   if(x==1){
    currentTime=millis();
    x=x+1;
    
   
   t= currentTime-startTime;
   v=100/t;
    startTime=currentTime;
    Serial.println(t);
    }
    }
    
   else
   {x=1;}


   
// when value is bigger than 100,it's draging.
// when value is smaller than 100,it's retracting. 

   if(t<100){
    
     if(times==1){
      if(y<=90){
        if(y=0){
          message1="retract1";//frist activity ends
          }
       if(y=60){
          message1="retract2";//second activity ends
          }
         if(y=90){
          message1="retract3";//third activity ends
          }
      myservo.write(y);
      
      y=y+30;
      }
      else{y=0;}

      times=times+1;
      }

      
      
   }
   if(t>100){
 
    dragTimes=dragTimes+1;
    if(dragTimes=3){dragTimes=1;}
    if(dragTimes=1){
          message2="draging1";//frist evaluation beginns
          }
       if(dragTimes=2){
          message2="draging2";//second evaluation beginns
          }
         if(dragTimes=3){
          message2="draging3";//third evaluation beginns
          }
    times=1;
   }
}

boolean isNearMagnet()
{
    int sensorValue = digitalRead(2);
    if(sensorValue == LOW)//if the sensor value is LOW
    {
        return true;//yes,return ture
    }
    else
    {
        return false;//no,return false
    }
}
