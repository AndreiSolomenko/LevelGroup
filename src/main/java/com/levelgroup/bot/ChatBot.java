//package com.levelgroup.bot;
//
//import com.levelgroup.model.ChatMessage;
//import com.levelgroup.services.ChatMessageService;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.context.event.ContextRefreshedEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//import org.telegram.telegrambots.meta.TelegramBotsApi;
//import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
//import org.telegram.telegrambots.meta.api.objects.Message;
//import org.telegram.telegrambots.meta.api.objects.Update;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//@PropertySource("classpath:telegram.properties")
//public class ChatBot extends TelegramLongPollingBot {
//
//    @Value("${bot.name}")
//    private String botName;
//
//    @Value("${bot.token}")
//    private String botToken;
//
//    private final ChatMessageService chatMessageService;
//
//    public ChatBot(ChatMessageService chatMessageService) {
//        this.chatMessageService = chatMessageService;
//    }
//
//    @EventListener({ContextRefreshedEvent.class})
//    public void init() throws TelegramApiException {
//        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
//        try {
//            telegramBotsApi.registerBot(this);
//        } catch (TelegramApiException e) {
//            System.out.println("Error occurred: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public String getBotUsername() {
//        return botName;
//    }
//
//    @Override
//    public String getBotToken() {
//        return botToken;
//    }
//
//    @Override
//    public void onUpdateReceived(Update update) {
//        try {
//            if (update.hasMessage() && update.getMessage().hasText()) {
//                long chatId = update.getMessage().getChatId();
//
//                if (update.getMessage().isReply()) {
//                    String receivedMessage = update.getMessage().getText();
//
//                    Message repliedMessage = update.getMessage().getReplyToMessage();
//                    String repliedText = repliedMessage.getText();
//                    String userName = getUserName(repliedText);
//                    long repliedChatId = repliedMessage.getChatId();
//
//                    ChatMessage chatMessage = new ChatMessage();
//                    chatMessage.setMessageText(receivedMessage);
//                    chatMessage.setUserName(userName);
//                    chatMessage.setMessageType("outgoing");
//
//                    chatMessageService.add(chatMessage);
//
//                    sendMessage(repliedChatId, "Message: " + receivedMessage + " sent to: " + userName);
//                } else {
//                    sendMessage(chatId, "Reply to an individual user's message!");
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void saveIncomingMessage(String text, String userName, String massageType) {
//        ChatMessage chatMessage = new ChatMessage();
//        chatMessage.setMessageText(text);
//        chatMessage.setUserName(userName);
//        chatMessage.setMessageType(massageType);
//        chatMessageService.add(chatMessage);
//    }
//
//    public void sendMessage(long chatId, String text) {
//        SendMessage message = new SendMessage();
//        message.setChatId(Long.toString(chatId));
//        message.setText(text);
//        try {
//            execute(message);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static String getUserName(String message) {
//        int colonIndex = message.indexOf(":");
//        if (colonIndex != -1) {
//            return message.substring(0, colonIndex).trim();
//        } else {
//            return message.trim();
//        }
//    }
//
//}