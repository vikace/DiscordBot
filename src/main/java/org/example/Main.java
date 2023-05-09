/*Class Name                discription
   Main                     This class creates a jda instance which is used all over the program , adds event listener(Brain class object)
                             and schedules the trivia through ScheduledExecutorServices everyday at same time

   Brain                    This class is the brain of the bot it implements Runnable interface and extends EventListner class and does the
                            Following tasks: getting trivia channels,getting list of the quizes to be sent , sending the questions, checking for
                            answers ,sending right answers after every quiz ,updating the score and sending the leaderboard after the quiz ends.

   Task                     This class is used to send the quizes to all the channels it implements the Runnable interface so it passed to the
                             Executor services and to send the quiz to the channels on multiple threads

   QuizBook                 This class is the Record that contain all the quizes for trivia and scoreboards.It contain all the function to retrieve the
                            quizes from the file and store it in the list that can be send when asked and funtions that can set the scoreboard ,
                             maintain the leaderbord and can send the scoreboard to the trivia channel*/
package org.example;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.time.LocalTime;
import java.util.concurrent.*;
public class Main  {

   private static JDA jda;

    public static void main(String[] args) {
        //--------------------- Initialization and logging the client---------------------
        jda=JDABuilder.createDefault("MTAwMTAyMzY4MDU4OTA5MDgxOQ.GGyN4K.dFOwf2zlk7C9Ev9HxxQuv7-hh5fv06OMblmZSk", GatewayIntent.GUILD_MESSAGES,GatewayIntent.MESSAGE_CONTENT).build();
        Brain brain=new Brain(jda); //Brain class object that is used throughout the bot,Guess this bot one has one brain (:;
        jda.addEventListener(brain);//passing the brain object to eventlistner meaning the corrospondin listener methods in Brain object will be call to an event.
        try
        {
            jda.awaitReady();//waiting for the jda to login successfully.
        }
        catch(Exception e)
        {
            System.out.print(e.getStackTrace());
        }
        //-------------------------------------------------------------------------------
        //--------------------- Caluclation of Quiz schedule time------------------------
        int current= LocalTime.now().toSecondOfDay();//current time in  seconds
        int schedule=LocalTime.of(2,46).toSecondOfDay();//Quiz schedule time in seconds
        int initialD=schedule-current;//initial delay on which the quiz will start
        //-------------------------------------------------------------------------------
        //--------------------- Scheduler to schedule the task on time ------------------
        ScheduledExecutorService ses= Executors.newScheduledThreadPool(2);//creating a ScheduleThreadPool of 2 threads.
        ses.scheduleAtFixedRate(brain,0,24*60*60, TimeUnit.SECONDS);//scheduling the task every day at the same time of the day.
        //-------------------------------------------------------------------------------

    }

}