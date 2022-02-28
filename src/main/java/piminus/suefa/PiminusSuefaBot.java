package piminus.suefa;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageText;

public class PiminusSuefaBot {
    private static final String PRCESSING_LABLE = "...";
    // Create your bot passing the token received from @BotFather
    TelegramBot bot = new TelegramBot(System.getenv("BOT_TOKEN"));

    public void serve() {

// Register for updates
        bot.setUpdatesListener(updates -> {

            updates.forEach(this::process);
            // ... process updates
            // return id of last processed update or confirm them all
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void process(Update update) {

        InlineQuery inlineQuery = update.inlineQuery();
        Message message = update.message();

        BaseRequest request = null;


        if (message != null && message.viaBot() != null && message.viaBot().username().equals(System.getenv("BOT_NAME"))) {
            InlineKeyboardMarkup inlineKeyboardMarkup = message.replyMarkup();

            if (inlineKeyboardMarkup == null) return;

            InlineKeyboardButton[][] inlineKeyboardButtons = inlineKeyboardMarkup.inlineKeyboard();

            if (inlineKeyboardButtons == null) return;

            InlineKeyboardButton button = inlineKeyboardButtons[0][0];
            if (!button.text().equals(PRCESSING_LABLE)) return;


            Long chatId = message.chat().id();
            String senderName = message.from().username();
            String senderChoice = button.callbackData();
            Integer messageId = message.messageId();

            request = new EditMessageText(chatId, messageId, message.text())
                    .replyMarkup(
                            new InlineKeyboardMarkup (
                                    new InlineKeyboardButton("\uD83D\uDDFF").callbackData(String.format("%d %s %s %s", chatId, senderName, senderChoice, "r")),
                                    new InlineKeyboardButton("✂").callbackData(String.format("%d %s %s %s", chatId, senderName, senderChoice, "s")),
                                    new InlineKeyboardButton("\uD83D\uDCC4").callbackData(String.format("%d %s %s %s", chatId, senderName, senderChoice, "p"))
                            )
                    );



        } else if (inlineQuery != null) {
            InlineQueryResultArticle rockQueryResult = getQueryResult("rock", "\uD83D\uDDFF Камень", "r");
            InlineQueryResultArticle scissorsQueryResult = getQueryResult("scissors", "✂ Ножницы", "s");
            InlineQueryResultArticle paperQueryResult = getQueryResult("paper", "\uD83D\uDCC4 Бумага", "p");

            request = new AnswerInlineQuery(inlineQuery.id(), rockQueryResult, scissorsQueryResult, paperQueryResult).cacheTime(5);

        }


        if (request != null) {
            bot.execute(request);
        }

    }

    private InlineQueryResultArticle getQueryResult(String id, String title, String callbackData) {
        return new InlineQueryResultArticle(id, title, "Су-е-фа!")
                .replyMarkup(
                        new InlineKeyboardMarkup(
                                new InlineKeyboardButton(PRCESSING_LABLE).callbackData(callbackData)
                        )
                );
    }
}
