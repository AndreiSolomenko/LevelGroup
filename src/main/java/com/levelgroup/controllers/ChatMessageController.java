//package com.levelgroup.controllers;
//
//import com.levelgroup.model.ChatMessage;
//import com.levelgroup.services.ChatMessageService;
//import org.springframework.web.bind.annotation.*;
//import com.levelgroup.bot.ChatBot;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@CrossOrigin(origins = "http://localhost:3000")
//@RestController
//@RequestMapping("/api")
//public class ChatMessageController {
//
//    private final ChatBot chatBot;
//    private final ChatMessageService chatMessageService;
//
//    public ChatMessageController(ChatBot chatBot, ChatMessageService chatMessageService) {
//        this.chatBot = chatBot;
//        this.chatMessageService = chatMessageService;
//    }
//
//    @GetMapping("/send-message")
//    public void sendMessage(
//            @RequestParam(required = false) String userName,
//            @RequestParam(required = false) String messageText
//    ) {
//        String welcomeMessage = userName + ": " + messageText;
//        long chatId = 00000000;
//
//        String massageType = "incoming";
//        chatBot.saveIncomingMessage(messageText, userName, massageType);
//        chatBot.sendMessage(chatId, welcomeMessage);
//
//        chatMessageService.cleanupOldMessages();
//
//    }
//
//    @GetMapping("/get-messages")
//    public List<String> getMessages() {
//        List<String> messages = new ArrayList<>();
//
//        List<ChatMessage> chatMessages = chatMessageService.get();
//        for (ChatMessage message : chatMessages) {
//            String messageString = "{\"userName\":\"" + message.getUserName() + "\",\"text\":\"" + message.getMessageText() + "\",\"messageType\":\"" + message.getMessageType() + "\"}";
//            messages.add(messageString);
//        }
//
//        System.out.println("Fetched messages: " + messages);
//        return messages;
//    }
//
//}


//        exampleforstudy280623

//        levelgroup_test
//        https://api.telegram.org/bot6895841707:AAEWTxVbJUUMeVCAPBwXWf9UxhHfvhLhOx0/getUpdates
