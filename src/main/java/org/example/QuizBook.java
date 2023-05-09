package org.example;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class QuizBook {
   private FileInputStream fis;
   private static Map<String,Integer> map;
   private static int length=0;

    public QuizBook()
    {

        try {
            fis = new FileInputStream(new File("E:\\CapnCook\\src\\main\\resources\\quizes.txt"));


            }
            catch(Exception e)
            {
                System.out.println(e.getStackTrace());
            }
        map=this.getObject(new File("E:\\CapnCook\\src\\main\\resources\\score.txt"));
    }
    public List<String> get(){
        List<String> quizes=new ArrayList<>();
        String line="";
        char ch;
        try {
            while (fis.available() > 0) {
                ch=(char)fis.read();
                if(ch!='\n')
                {
                    line+=ch;
                }
                else{
                    quizes.add(line);
                    line="";
                }

            }
        }
        catch(Exception e)
        {
            System.out.println(e.getStackTrace());

        }
        return quizes;

    }
    private Map<String,Integer> getObject(File file)
    {
        ObjectInputStream ois;
        Map<String,Integer> map;
        try
        {
             ois=new ObjectInputStream(new FileInputStream(file));
             map=(Map<String,Integer>)ois.readObject();
            return map;

        }
        catch (Exception e)
        {

            System.out.println(e.getStackTrace());
            return null;
        }

    }
    private void setObject(File file)
    {
        ObjectOutputStream ois;
        try
        {
            ois=new ObjectOutputStream(new FileOutputStream(file));
            ois.writeObject(map);
            ois.close();

        }
        catch (Exception e)
        {

            System.out.println(e.getStackTrace());
        }


    }

    public void setScore(Guild server,User user){
        if(map.containsKey(server.getName()+"/"+user.getId()))
        {
            map.put(server.getName()+"/"+user.getId(),map.get(server.getName()+"/"+user.getId())+1);
            System.out.println(map.get(server.getName()+"/"+user.getId()));
        }
        else{
            System.out.println(map.get(server.getName()+"/"+user.getId()));
            map.put(server.getName()+"/"+user.getId(),1);
            System.out.println(map.get(server.getName()+"/"+user.getId()));
        }
        this.setObject((new File("E:\\CapnCook\\src\\main\\resources\\score.txt")));
    }
    public int getScore(Guild server, User user)
    {

        return map.get(server.getName()+"/"+user.getId());
    }
    public static MessageEmbed EmbedMsg(String message,String type
    )
    {
        EmbedBuilder builder=new EmbedBuilder();
        builder.setTitle(type.toUpperCase()+" :").setDescription(message).setColor(type.equalsIgnoreCase("question")? Color.BLUE:Color.MAGENTA);
        return builder.build();

    }
    public  Map<String,Integer> getScoreBoard(JDA jda)
    {
        Map<String,Integer> scoreBoard=new HashMap<>();
        String serverAndId[];
        String serverAndName;

        for(Map.Entry<String,Integer> entry:map.entrySet())
        {
            serverAndId=entry.getKey().split("/");
            serverAndName=serverAndId[0]+"->"+jda.getUserById(serverAndId[1]).getName();
            if(serverAndName.length()>length)
            {
                length=serverAndName.length();
            }
            scoreBoard.put(serverAndName,entry.getValue());


        }
        return scoreBoard;
    }
    public void printScoreBoard(JDA jda,List<TextChannel> tc)
    {
        System.out.println("Enter the printScoreBoard");
        Map<String,Integer> scoreBoard=this.getScoreBoard(jda);
        String sb="";

        for(Map.Entry<String, Integer> entry: scoreBoard.entrySet())
        {
            sb+=entry.getKey();
            for(int i=0;i<=(length+2-entry.getKey().length());i++)
            {
                sb+=" ";
            }
            sb+="|  "+entry.getValue();
            sb+="\n";
        }
        System.out.println(sb);
        for (TextChannel c: tc)
        {
            c.sendMessageEmbeds(EmbedMsg(sb,"Scoreboard")).addEmbeds().queue();
        }
    }


}
