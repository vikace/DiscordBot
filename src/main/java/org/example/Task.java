package org.example;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.concurrent.Callable;

public class Task implements Runnable {
    String question;
    TextChannel channel;
    public Task(String question, TextChannel channel)
    {
        this.question=question;
        this.channel=channel;
    }

    @Override
    public void run()
    {
        System.out.println("The Quiz is: "+question);
        channel.sendMessageEmbeds(QuizBook.EmbedMsg(question,"question")).addEmbeds().queue();
    }
}
