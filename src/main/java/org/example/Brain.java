package org.example;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Brain extends ListenerAdapter implements Runnable{
    private static List<TextChannel> quizChannels;//this test channel list is used to hold all the trivia channels from every guild,used by getChannel()
    private static JDA jda;//used to store the jda reference passes from Main class we use this to access guilds and inturn channels
    private static AtomicInteger flag;//this atomic integer is used to stop onMessageEvent() from processing any answer when time is up
    private QuizBook quiz;//used to store the instace of quizbook
    private static Map<TextChannel,List<MessageReceivedEvent>> map;//used to map textchannels and the answers for a quiz from the textchannels to process them later.
    public Brain(JDA jda)
    { // initializing all the global variable s
        this.jda=jda;
        flag=new AtomicInteger(0);
        map=new HashMap<>();
        quiz=new QuizBook();
        quizChannels=new ArrayList<>();
    }
    @Override
    public void run()
    {
        System.out.println("The task is scheduled");//ignore
        //-----------------------Getting the list of Quiz channels----------------------
        this.getChannels();//this will get the channels and store it into the textchannel list global variable
        System.out.println(quizChannels.get(0).getId());//ignore

        //-------------------------------------------------------------------------------
        //-----------------------Getting the list of Quizes------------------------------
        /* this function will get the quizes from the Quizbook class instance in the form of list of string. Note that the quizes returned are in form
        "question?,answer "*/
        List<String> quizes=quiz.get();
        System.out.println(quizes.get(0));//ignore
        //-------------------------------------------------------------------------------
        //-----------------------Sending Quizes to the text channels---------------------
        /*Below function will send the quizes from the list of quizes to the trivia channel at a specific delay of 1 minuste between them*/
            this.sendQuizes(quizes);
            System.out.println("All quizes are sent.");//ignore
        //-------------------------------------------------------------------------------
        //-----------------------Sending score after the end of the trivia---------------
        /*Below function will be called after the trivia is finished and will send the leaderboard to all the given textchannels*/
        quiz.printScoreBoard(jda,quizChannels);


    }
    private void getChannels()
    {
        List<Guild> guilds=jda.getGuilds();//using the jda instance to get all the guilds in which bot is a member
        System.out.println(guilds.size());//ignore
        for(Guild g:guilds)
        {
            if(g.getName().equalsIgnoreCase("restingalienclub"))
               quizChannels.add(g.getTextChannelsByName("trivia",true).get(0));//storing all the trivia channels

        }
        for(TextChannel t:quizChannels)
        {
            map.put(t,new ArrayList<MessageReceivedEvent>());//Initializing textchannels and answers map.
        }

    }
    private synchronized void sendQuizes(List<String> quizes)
    {
        ExecutorService workers=Executors.newFixedThreadPool(10);//thread pool that will send the quiz questions to the channels.
        String qna[];//string to store question and answer size will be 2.
        for(int i=0;i<9;i++)
        {

            qna=quizes.get(i).split(",");//splitting a question and the answer from the quizes list and storing it into qna

            try {
                flag.getAndSet(0);//setting flag to zero so event listner wont process any answer
                for (TextChannel tc : quizChannels) {
                    workers.submit(new Task(qna[0], tc));//sending the question and answer to all the channels by Task class run method through different thread for every channel.
                }
                flag.getAndSet(1);//allowing the messagelistner to listen and get the answers in map.
                this.wait(1*60*1000);//waiting this thread for 1 minute to collect all the answers.
                flag.getAndSet(0);//setting flag to zero so event listner won't process any answer while the answers are checked.
                this.checkAndSend(qna[1]);//calling the checkAndSend(String) method to check the answer received and send the result to all channels.
            }
            catch(Exception e)
            {
                System.out.print(e);
                flag.getAndSet(0);//this ensures that if any error occurs during waiting the messageEvent will stop taking answer and move to next quiz.
            }
        }
        workers.shutdown();//shutting down all the worker thread that send the
        System.out.println("Exit");//ignore
    }
    public void checkAndSend(String correctAns)
    {
        String result;//used to store the result of a quiz that will be sent to the trivia channel after every quiz.
        correctAns=correctAns.toLowerCase().trim();//converting the currect answer to lower case.
        result="The correct answer is: "+correctAns; //assigning the correct answer to result. this answer wont be modifies anymore if no one gave right answer to the question.
        for (Map.Entry<TextChannel,List<MessageReceivedEvent>> entry:map.entrySet())//getting a set of textchannel and list of messsages received from that textchannel in form on event
        {
            if(entry.getValue().size()!=0) { //proceed if there is at least one answer
                for (MessageReceivedEvent event : entry.getValue()) { // getting the messageEvent(event) out the list one by one.
                    String ans = event.getMessage().getContentRaw().split("/")[1].toLowerCase().trim();//the answers are send in form of a/'ans' so splitting the msg and getting the ans.
                    if (ans.equalsIgnoreCase(correctAns)) {//checking if the answer by user(ans) is equal to correct answer in correctAns
                        quiz.setScore(entry.getKey().getGuild(), event.getAuthor());//setting the score of user through setScore() method of quiz object.
                        /*adding the mention and the score of the user to the result string*/
                        result += " Congratulation: " + event.getAuthor().getAsMention() + "Your Score: " + quiz.getScore(entry.getKey().getGuild(), event.getAuthor());
                        break;
                    }

                }
            }
            entry.getKey().sendMessageEmbeds(QuizBook.EmbedMsg(result,"answer")).queue();//sending the result string as embed Message.
            entry.getValue().clear();//clearing all the values of the list of the MessageEvents from the map.

        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        String msg=event.getMessage().getContentRaw();//extract the message in form of string and store it in msg
        if(flag.get()==1&&msg.split("/")[0].equalsIgnoreCase("a"))//check if flag is 1 meaning this function is allowed to take answers and check if the msg is send with answer command prefixed i.e./a.
        {
            if(quizChannels.contains(event.getChannel()))//checking if the answer is from the trivia channel
            {
                List<MessageReceivedEvent> answers=map.get(event.getChannel());//getting the list of messageEvent for the tria channel in which answer this msg is recieved
                answers.add(event);//adding the messageReceivedEvent to the list
            }
        }
    }

}
