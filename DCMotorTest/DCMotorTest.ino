/* 
This is a test sketch for the Adafruit assembled Motor Shield for Arduino v2
It won't work with v1.x motor shields! Only for the v2's with built in PWM
control
For use with the Adafruit Motor Shield v2 
---->    http://www.adafruit.com/products/1438
*/
#include <Wire.h>
#include <Adafruit_MotorShield.h>
#include <SoftwareSerial.h>
Adafruit_MotorShield AFMS = Adafruit_MotorShield(); 
Adafruit_DCMotor *myMotor = AFMS.getMotor(1);
Adafruit_DCMotor *myMotor2 = AFMS.getMotor(2);
SoftwareSerial bt(0, 1);
char data;
String dataString = "";
int leftWheelSpeed = 0;
int rightWheelSpeed = 0;
//data read format 
// "leftWheelSpeed" + " " + "rightWheelSpeed" + " " "x" //x is terminating character
void setup() {
  Serial.begin(9600);
  bt.begin(9600);
  pinMode(0, INPUT);
  AFMS.begin();
}
void loop() {
    
  while(bt.available() > 0)
  {    
    data = bt.read(); //read in character-by-character
    
    if (data == 'x') //check for terminating character string
    {
      Serial.println(dataString);
      leftWheelSpeed = getWheelSpeeds(dataString, 0);
      rightWheelSpeed = getWheelSpeeds(dataString, 1);

      Serial.println(leftWheelSpeed);
      Serial.println(rightWheelSpeed);

      
      if (leftWheelSpeed > 0 || rightWheelSpeed > 0)
      {
        myMotor->run(FORWARD);
        myMotor2->run(FORWARD);

        myMotor->setSpeed(leftWheelSpeed);
        myMotor2->setSpeed(rightWheelSpeed);
      }
      else if (leftWheelSpeed < 0 || rightWheelSpeed < 0)
      {
        myMotor->run(BACKWARD);
        myMotor2->run(BACKWARD);

        myMotor->setSpeed(leftWheelSpeed*(-1));
        myMotor2->setSpeed(rightWheelSpeed*(-1));
      }
      else if (leftWheelSpeed == 0 && rightWheelSpeed == 0)
      {
        myMotor->run(RELEASE);
        myMotor2->run(RELEASE);
      }
      
      //clear data string after command
      dataString = "";
      
    }
    else
    {
      //add character to data string
      dataString += data;
    }
    
    
  }
  
}
int getWheelSpeeds(String command, int LorR) //if LorR is 0, left wheel speed, else, right wheel speed
{
   int lastCharacterIndex;
   String leftWheel = "";
   String rightWheel = "";
   
   for (lastCharacterIndex = 0; lastCharacterIndex < command.length(); lastCharacterIndex++)
   {
     if (command[lastCharacterIndex] == ' ')
       break;
     
     leftWheel += command[lastCharacterIndex];
   }
   
   if (LorR == 0) //left wheel
     return leftWheel.toInt();
   else
   {
     for (lastCharacterIndex += 1; lastCharacterIndex < command.length(); lastCharacterIndex++)
     {
       if (command[lastCharacterIndex] == ' ')
         break;
         
       rightWheel += command[lastCharacterIndex];
     }
     
     return rightWheel.toInt();
   }
}

