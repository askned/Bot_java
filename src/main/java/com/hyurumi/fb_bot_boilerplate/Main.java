package com.hyurumi.fb_bot_boilerplate;

import com.google.gson.Gson;
import com.hyurumi.fb_bot_boilerplate.models.common.Action;
import com.hyurumi.fb_bot_boilerplate.models.send.Button;
import com.hyurumi.fb_bot_boilerplate.models.send.Element;
import com.hyurumi.fb_bot_boilerplate.models.send.Mail;
import com.hyurumi.fb_bot_boilerplate.models.send.Message;
import com.hyurumi.fb_bot_boilerplate.models.webhook.Messaging;
import com.hyurumi.fb_bot_boilerplate.models.webhook.ReceivedMessage;
import okhttp3.*;

import java.util.List;
import java.util.Random;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.port;

public class Main {
    public static  String sAccessToken;
    private static String sValidationToken;
    public static final String END_POINT;
    public static final MediaType JSON;
    private static final Random sRandom;
    private static final Gson GSON;

    static {
        JSON = MediaType.parse("application/json; charset=utf-8");
        END_POINT = "https://graph.facebook.com/v2.6/me/messages";
        GSON = new Gson();
        sRandom = new Random();
        sAccessToken = System.getenv("ACCESS_TOKEN");
        sValidationToken = System.getenv("VALIDATION_TOKEN");
    }

    public static void main(String[] args) {

        port(Integer.valueOf(System.getenv("PORT")));

        get("/webhook", (request, response) -> {
            if (request.queryMap("hub.verify_token").value().equals(sValidationToken)) {
                return request.queryMap("hub.challenge").value();
            }
            return "Error, wrong validation token";
        });

        post("/webhook", (request, response) -> {
            ReceivedMessage receivedMessage = GSON.fromJson(request.body(), ReceivedMessage.class);
            List<Messaging> messagings = receivedMessage.entry.get(0).messaging;
            for (Messaging messaging : messagings) {
                String senderId = messaging.sender.id;
                if (messaging.message !=null) {
                    // Receiving text message
                    switch (messaging.message.text){
                        case "да":
                            if (messaging.message.text != null)
                                Message.Text(messaging.message.text).sendTo(senderId);
                            else
                                sendSampleGenericMessage(senderId);
                            break;
                        case "нет":
                            Message.Image("https://scontent-frt3-1.xx.fbcdn.net/v/t1.0-9/10653363_745618215533353_8151139926344498327_n.png?oh=b9a4ae206114d0996873fdc117cec7e8&oe=57DE417B").sendTo(senderId);
                            break;
                        case "не знаю":
                            sendSampleGenericMessage(senderId);
                            break;
                        default:
                         //   sendSamplePostBackMessage(senderId);
                            Message.Text("Спасибо за интерес к нашей компании!").sendTo(senderId);
                            Message.Image("https://scontent-frt3-1.xx.fbcdn.net/v/t1.0-9/10653363_745618215533353_8151139926344498327_n.png?oh=b9a4ae206114d0996873fdc117cec7e8&oe=57DE417B").sendTo(senderId);

                            firstMenu(senderId);
                             Message.Text(messagings.size()).sendTo(senderId);
                            break;
                    }

                } else if (messaging.postback != null) {
                    // Receiving postback message
                    if (messaging.postback.payload == Action.ACTION_A) {
                        Message.Text("Хорошо что вы интересуетесь СММ").sendTo(senderId);
                        Mail.sendMail();
                    }else if (messaging.postback.payload == Action.ACTION_B){
                        Message.Text("Колл центр очень хорошо").sendTo(senderId);
                    }else if (messaging.postback.payload == Action.ACTION_C){
                       // Message.Text("Колл центр оцень хорошо").sendTo(senderId);
                        suvenirMenu(senderId);
                    }else {
                        Message.Text("Будет доставлена").sendTo(senderId);
                    }
                } else if (messaging.delivery != null) {
                    // when the message is delivered, this webhook will be triggered.
                } else {
                    // sticker may not be supported for now.
                    System.out.println(request.body());
                }
            }
            return "";
        });
    }

    static private void sendSamplePostBackMessage(String senderId) throws Exception {
        Message message = Message.Button("This is a postback message; please choose the action below");
        message.addButton(Button.Postback("action A", Action.ACTION_A));
        message.addButton(Button.Postback("action B", Action.ACTION_B));
        message.addButton(Button.Url("open Google", "https://google.com"));
        message.sendTo(senderId);
    }

    static private void sendSampleGenericMessage(String senderId) throws Exception {
        Message message = Message.Generic();
        Element element = new Element("Generic Message Sample", "https://unsplash.it/764/400?image=400", "subtitle");
        message.addElement(element);
        element = new Element("Yay Yay", "https://unsplash.it/764/400?image=500", "subtitle");
        element.addButton(Button.Postback("action A", Action.ACTION_A));
        element.addButton(Button.Url("jump to FB", "https://facebook.com/"));
        message.addElement(element);
        message.sendTo(senderId);
    }
    static private void firstMenu(String senderId) throws Exception {
        Message message = Message.Button("Какие услуги нашей компании вас интересуют?");
        message.addButton(Button.Postback("СММ", Action.ACTION_A));
        message.addButton(Button.Postback("Колл-центр", Action.ACTION_B));
        message.addButton(Button.Postback("Купить сувенир", Action.ACTION_C));
        message.sendTo(senderId);
    }

    static private void suvenirMenu(String senderId) throws Exception {
        Message message = Message.Generic();
        Element element = new Element("Футболка", "http://www.dhresource.com/albu_606424795_00-1.600x600/wjx670-2014-smmmer-3d-print-pattern-t-shirt.jpg", "лучший подарок");
        message.addElement(element);
        element = new Element("Чашка", "https://content.freelancehunt.com/snippet/8e5fa/9497f/239475/%D1%81%D1%83%D0%B2%D0%B5%D0%BD%D0%B8%D1%80%D0%BD%D0%B0%D1%8F+%D1%87%D0%B0%D1%88%D0%BA%D0%B0.png", "лучший подарок");
        element.addButton(Button.Postback("Заказать", Action.ACTION_D));
        element.addButton(Button.Url("Детальнее", "https://facebook.com/"));
        message.addElement(element);
        message.sendTo(senderId);
    }
}
