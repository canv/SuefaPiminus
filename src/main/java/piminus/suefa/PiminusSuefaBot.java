package piminus.suefa;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PiminusSuefaBot {
    private static final String PROCESSING_LABEL = "...";

    TelegramBot bot = new TelegramBot(System.getenv("BOT_TOKEN"));

    private final static List<String> senderWins = new ArrayList<String>() {{
        add("rs");
        add("sp");
        add("pr");
    }};

    private final static Map<String, String> items = new HashMap<String, String>() {{
        put("r", "\uD83D\uDDFF");
        put("s", "✂");
        put("p", "\uD83D\uDCC4");
    }};

    public void serve() {
        bot.setUpdatesListener(updates -> {
            updates.forEach(this::process);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void process(Update update) {

        InlineQuery inlineQuery = update.inlineQuery();
        Message message = update.message();
        CallbackQuery callbackQuery = update.callbackQuery();

        BaseRequest request = null;

        if (isSenderStartGame(message)) {
            InlineKeyboardMarkup inlineKeyboardMarkup = message.replyMarkup();

            if (inlineKeyboardMarkup == null) return;

            InlineKeyboardButton[][] inlineKeyboardButtons = inlineKeyboardMarkup.inlineKeyboard();

            if (inlineKeyboardButtons == null) return;

            InlineKeyboardButton button = inlineKeyboardButtons[0][0];
            if (!button.text().equals(PROCESSING_LABEL)) return;


            Long chatId = message.chat().id();
            String senderName = message.from().username();
            String senderChoice = button.callbackData();
            Integer messageId = message.messageId();

            request = new EditMessageText(chatId, messageId, message.text())
                    .replyMarkup(
                            new InlineKeyboardMarkup(
                                    new InlineKeyboardButton("\uD83D\uDDFF").callbackData(String.format("%d %s %s %s %d", chatId, senderName, senderChoice, "r", messageId)),
                                    new InlineKeyboardButton("✂").callbackData(String.format("%d %s %s %s %d", chatId, senderName, senderChoice, "s", messageId)),
                                    new InlineKeyboardButton("\uD83D\uDCC4").callbackData(String.format("%d %s %s %s %d", chatId, senderName, senderChoice, "p", messageId))
                            )
                    );


        } else if (inlineQuery != null) {
            InlineQueryResultArticle rockQueryResult = getQueryResult("rock", "\uD83D\uDDFF Камень", "r");
            InlineQueryResultArticle scissorsQueryResult = getQueryResult("scissors", "✂ Ножницы", "s");
            InlineQueryResultArticle paperQueryResult = getQueryResult("paper", "\uD83D\uDCC4 Бумага", "p");

            request = new AnswerInlineQuery(inlineQuery.id(), rockQueryResult, scissorsQueryResult, paperQueryResult).cacheTime(5);

        } else if (callbackQuery != null) {
            String[] data = callbackQuery.data().split(" ");
            String chatId = data[0];
            String senderName = data[1];
            String senderChoice = data[2];
            String opponentChoice = data[3];
            int messageId = Integer.parseInt(data[4]);
            String opponentName = callbackQuery.from().firstName();

            if (senderChoice.equals(opponentChoice)) {
                request = new SendMessage(chatId, "Ничья!");
            } else if (senderWins.contains(senderChoice + opponentChoice)) {
                request = new EditMessageText(
                        chatId, messageId,
                        String.format(
                                "%s выбрал %s и отхватил от %s, выбравшего %s",
                                opponentName, items.get(opponentChoice),
                                senderName, items.get(senderChoice)
                                )
                );
            } else {
                request = new EditMessageText(
                        chatId, messageId,
                        String.format(
                                "%s выбрал %s и отхватил от %s, выбравшего %s",
                                senderName, items.get(senderChoice),
                                opponentName, items.get(opponentChoice)
                                )
                );
            }
        }


        if (request != null) {
            bot.execute(request);
        }

    }

    private boolean isSenderStartGame(Message message) {
        return message != null && message.viaBot() != null && message.viaBot().username().equals(System.getenv("BOT_NAME"));
    }

    private InlineQueryResultArticle getQueryResult(String id, String title, String callbackData) {
        return new InlineQueryResultArticle(id, title, "Су-е-фа!")
                .replyMarkup(
                        new InlineKeyboardMarkup(
                                new InlineKeyboardButton(PROCESSING_LABEL).callbackData(callbackData)
                        )
                );
    }
}
