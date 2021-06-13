package com.github.hectorhw.tglinkshortener.bot;

import com.github.hectorhw.tglinkshortener.database.DatabaseController;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
public class Bot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    @Getter
    @Value("${bot.name}")
    private String botUsername;

    @Getter
    @Value("${bot.token}")
    private String botToken;

    @Getter
    @Value("${domain}")
    String domain;

    @Autowired
    private DatabaseController db;

    @Override
    public void onUpdateReceived(Update update){
        if(!update.hasMessage() || !update.getMessage().hasText()) return;

        Message message = update.getMessage();
        handleTextMessage(message);
    }

    private void handleTextMessage(Message message){
        if(message.getText().startsWith("/")){
            String[] parts = message.getText().split(" ", 2);
            if(commandHandlers.containsKey(parts[0])){
                Method m = commandHandlers.get(parts[0]);
                try {
                    m.invoke(this, message);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @TgCommand("/start")
    private void startCommand(Message message){
        String helpString = Strings.join(Arrays.asList(
            "type /list to display registered links",
            "type /add followed by link to shorten it",
            "type /remove followed by id to remove link",
            "type /start to display this message"
        ), '\n');
        replyTo(message, helpString);
    }

    @TgCommand("/list")
    private void listCommand(Message message){
        Long id = message.getChatId();
        List<String> descriptions = db.getUserRecords(id).map(
            k_v_pair -> {
                String link_id = k_v_pair.key();
                String url = k_v_pair.value().getTargetUrl();
                return link_id + ": " + url;
            }
        ).materialize();

        String text = descriptions.stream().reduce((a,b) -> a+"\n"+b).orElse("you have no links");
        replyTo(message, text);

    }

    @TgCommand("/add")
    private void addCommand(Message message){

        String arg = getCommandArgument(message.getText());
        if(arg.isEmpty()){
            replyTo(message, "usage: /add <link>");
            return;
        }

        String link_short = db.insertData(message.getChatId(), arg);
        link_short = this.getDomain() + "/" + link_short;
        replyTo(message, link_short);

    }

    @TgCommand("/remove")
    private void removeCommand(Message message){
        String arg = getCommandArgument(message.getText());
        if(arg.isEmpty()){
            replyTo(message, "usage: /remove <name>");
            return;
        }

        db.getData(arg).ifPresentOrElse(
            dbRecord -> {
            if(dbRecord.getOwnerId()==message.getChatId()){
                db.remove(arg);
                replyTo(message, "Ok.");
            }else{
                replyTo(message, "you do not control such link");
            }
        },
            () -> {
                replyTo(message, "you do not control such link");
        });

    }

    private void replyTo(Message message, String text){
        Long chatId = message.getChatId();

        SendMessage response = new SendMessage();
        response.setChatId(chatId.toString());

        response.setText(text);

        try{
            execute(response);
            logger.info("Sent message \"{}\" to {}", text, chatId);
        }catch (TelegramApiException e){
            logger.error("Failed to send message \"{}\" to {}, error: {}", text, chatId, e.getMessage());
        }
    }

    private String getCommandArgument(String value){
        String[] splits = value.strip().split(" ", 2);
        if(splits.length==2) return splits[1];
        else return "";
    }

    private final HashMap<String, Method> commandHandlers = new HashMap<>();

    @PostConstruct
    public void registerHandlers(){
        //add handlers marked with annotation
        Arrays.stream(Bot.class.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(TgCommand.class))
            .forEach(method -> {
                String command = method.getAnnotation(TgCommand.class).value();
                commandHandlers.put(command, method);
                logger.info("registered {} for command {}", method.getName(), command);
            });
        logger.info("Injected handlers for telegram chat commands");
    }

    @PostConstruct
    public void start(){
        logger.info("username: {}, token: {}", botUsername, botToken);
    }
}

