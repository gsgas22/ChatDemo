package chatDemo.bot;

import chatDemo.Authentication.entity.OlxUser;
import chatDemo.Authentication.service.OAuth2AuthenticationService;
import chatDemo.Authentication.service.OlxUserService;
import chatDemo.message.MessageManager;
import chatDemo.message.ThreadDetailFetcher;
import chatDemo.message.entity.Message;
import chatDemo.message.repository.MessageRepository;
import chatDemo.message.repository.ThreadDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import chatDemo.message.entity.ThreadDetail;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ChatDemoBot extends TelegramLongPollingBot {
    private static final long olxUserId = 100491046;
    @Autowired
    OAuth2AuthenticationService authService;
    @Autowired
    OlxUserService olxUserService;
    @Autowired
    MessageManager messageManager;
    @Autowired
    ThreadDetailFetcher threadDetailFetcher;
    @Autowired
    ThreadDetailRepository threadDetailRepository;
    @Autowired
    MessageRepository messageRepository;
    public ChatDemoBot(@Value("${bot.token}") String botToken){
        super(botToken);
    }
    @Value("${loginUrl}")
    private String loginUrl;
    private String selectedThreadId;
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            var message = update.getMessage().getText();
            var chatId = update.getMessage().getChatId();

            switch (message) {
                case "/start" -> {
                    String userName = update.getMessage().getChat().getFirstName();
                    startCommand(chatId, userName);
                }
                case "/chats" -> {
                    displayUserThreads(chatId, 1);
                }
                case "/help" -> {
                    helpCommand(chatId);
                }
                case "/about" -> {
                    aboutCommand(chatId);
                }
                case "/login" -> {
                    loginCommand(chatId);
                }
                default -> {
                    if (message.startsWith("Чат ")) {
                        try {
                            int threadIndex = Integer.parseInt(message.split(" ")[1].replace(":", ""));
                            List<ThreadDetail> threadDetails = threadDetailRepository.findByUserId(olxUserId);
                            if (threadIndex >= 1 && threadIndex <= threadDetails.size()) {
                                String threadId = threadDetails.get(threadIndex - 1).getThreadId().toString();
                                displayThreadMessages(chatId, threadId);
                                selectedThreadId = threadId;
                            } else {
                                sendMessage(chatId, "Чат не знайдено.");
                            }
                        } catch (NumberFormatException e) {
                            sendMessage(chatId, "Неправильний формат чату.");
                        }
                    }else {
                        // Відправка повідомлення клієнту, якщо вибраний чат існує
                        if (selectedThreadId != null) {

                            messageManager.postMessage(olxUserId, selectedThreadId, message);
                            Message messageToSave = new Message();
                            messageToSave.setId(UUID.randomUUID());
                            messageToSave.setThreadId(selectedThreadId);
                            messageToSave.setUserId(olxUserId);
                            messageToSave.setMarketplace("olx");
                            messageToSave.setType("sent");
                            messageToSave.setContent(message);
                            messageToSave.setTimestamp(Instant.now().toString());
                            messageRepository.save(messageToSave);
                            sendMessage(chatId, "Повідомлення відправлено.");
                        } else {
                            sendMessage(chatId, "Спочатку виберіть чат.");
                        }
                    }
                }
            }
        }
    }


    private void startCommand(Long chatId, String userName) {
        var text = "Привіт, %s! Вітаємо у нашому боті.";
        var formattedMessage = String.format(text, userName);
        sendMessageWithReplyKeyboard(chatId, formattedMessage, createMainMenuKeyboard());
    }

    private void helpCommand(Long chatId) {
        var text = "Список доступних команд:\n" +
                "/start - Почати роботу з ботом\n" +
                "/help - Показати допомогу\n" +
                "/about - Інформація про бота\n" +
                "/chats - Показати чати користувача\n" +
                "/login - Увійти в систему";
        sendMessage(chatId, text);
    }

    private void aboutCommand(Long chatId) {
        var text = "Це бот для управління повідомленнями.";
        sendMessage(chatId, text);
    }

    private void loginCommand(Long chatId) {
        var text = "Будь ласка, увійдіть за наступним посиланням: " + loginUrl;
        sendMessage(chatId, text);
    }

    private void displayUserThreads(Long chatId, int page) {
        int pageSize = 3; // Number of threads to display per page
        try {
            // Fetch and save thread details
            //threadDetailFetcher.fetchAndSaveThreadDetails(olxUserId, 1);

            // Retrieve saved thread details from the repository
            List<ThreadDetail> threadDetails = threadDetailRepository.findByUserId(olxUserId);
            //messageManager.fetchAndSaveMessages(olxUserId, threadDetails.getFirst().getThreadId());
            // Determine the start and end index for the current page
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, threadDetails.size());

            // Ensure the page is within valid range
            if (start < 0 || start >= threadDetails.size()) {
                sendMessage(chatId, "Сторінка не знайдена.");
                return;
            }

            // Prepare HTML formatted text for thread details
            StringBuilder text = new StringBuilder("<b>Ось ваші чати:</b>\n\n");
            for (int i = start; i < end; i++) {
                ThreadDetail thread = threadDetails.get(i);
                text.append("<b>Клієнт:</b> ").append(thread.getClientFullName()).append("\n")
                        .append("<b>Заголовок оголошення:</b> ").append(thread.getAdvertLogoUrl()).append("\n")
                        .append("<b>Останнє повідомлення:</b> ").append(thread.getLastMessageContent()).append("\n")
                        .append("<b>Час:</b> ").append(thread.getLastMessageTimestamp()).append("\n\n");
            }

            // Prepare inline keyboard for pagination
            InlineKeyboardMarkup inlineKeyboardMarkup = createPaginationInlineKeyboard(page, threadDetails.size(), pageSize);
            sendMessageWithKeyboards(chatId, text.toString(), inlineKeyboardMarkup);

            // Prepare reply keyboard for thread selection
            ReplyKeyboardMarkup replyKeyboardMarkup = createReplyKeyboardForThreads(threadDetails, start, end);
            sendReplyKeyboard(chatId, replyKeyboardMarkup);
        } catch (Exception e) {
            var text = "Не вдалося отримати чати: " + e.getMessage();
            sendMessage(chatId, text);
        }
    }

    private InlineKeyboardMarkup createPaginationInlineKeyboard(int page, int totalThreads, int pageSize) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        if (page > 1) {
            InlineKeyboardButton prevButton = new InlineKeyboardButton();
            prevButton.setText("Попередня");
            prevButton.setCallbackData("PAGE_" + (page - 1));
            row.add(prevButton);
        }

        if (page * pageSize < totalThreads) {
            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText("Наступна");
            nextButton.setCallbackData("PAGE_" + (page + 1));
            row.add(nextButton);
        }

        rows.add(row);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
    private ReplyKeyboardMarkup createReplyKeyboardForThreads(List<ThreadDetail> threads, int start, int end) {
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        for (int i = start; i < end; i++) {
            ThreadDetail thread = threads.get(i);
            KeyboardRow row = new KeyboardRow();
            row.add("Чат " + (i + 1) + ": " + thread.getClientFullName() + " - " + thread.getLastMessageContent());
            keyboardRows.add(row);
        }

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }
    private void sendMessageWithKeyboards(Long chatId, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("HTML");
        message.setReplyMarkup(inlineKeyboardMarkup); // Only set inline keyboard
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendReplyKeyboard(Long chatId, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Виберіть чат:"); // Optional prompt for the user
        message.setReplyMarkup(replyKeyboardMarkup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        if (callbackData.startsWith("PAGE_")) {
            int page = Integer.parseInt(callbackData.substring("PAGE_".length()));
            displayUserThreads(callbackQuery.getMessage().getChatId(), page);
        } else if (callbackData.startsWith("THREAD_")) {
            String threadId = callbackData.substring("THREAD_".length());
            displayThreadDetails(callbackQuery.getMessage().getChatId(), threadId);
        }
    }

    private void displayThreadDetails(Long chatId, String threadId) {
        ThreadDetail threadDetail = threadDetailRepository.findById(UUID.fromString(threadId)).orElse(null);
        if (threadDetail == null) {
            sendMessage(chatId, "Чат не знайдено.");
            return;
        }

        StringBuilder text = new StringBuilder();
        text.append("<b>Клієнт:</b> ").append(threadDetail.getClientFullName()).append("\n")
                .append("<b>Заголовок оголошення:</b> ").append(threadDetail.getAdvertLogoUrl()).append("\n")
                .append("<b>Останнє повідомлення:</b> ").append(threadDetail.getLastMessageContent()).append("\n")
                .append("<b>Час:</b> ").append(threadDetail.getLastMessageTimestamp()).append("\n\n");

        sendMessage(chatId, text.toString(), "HTML");
    }
    private void displayThreadMessages(Long chatId, String threadId) {
        try {
            List<Message> messages = messageRepository.findByThreadIdOrderByTimestampAsc(threadId);
            if (messages.isEmpty()) {
                sendMessage(chatId, "Повідомлення не знайдено.");
                return;
            }

            StringBuilder text = new StringBuilder("<b>Повідомлення в чаті:</b>\n\n");
            for (Message message : messages) {
                String messageType = determineMessageType(message);
                String messageEmoji = getMessageEmoji(messageType);

                text.append(messageEmoji)
                        .append("<b>Тип:</b> ").append(messageType).append("\n")
                        .append("<b>Час:</b> ").append(message.getTimestamp()).append("\n")
                        .append("<b>Зміст:</b> ").append(message.getContent()).append("\n\n");
            }

            sendMessage(chatId, text.toString(), "HTML");
        } catch (Exception e) {
            var text = "Не вдалося отримати повідомлення: " + e.getMessage();
            sendMessage(chatId, text);
        }
    }
    private String determineMessageType(Message message) {
        // Логіка для визначення типу повідомлення
        // Наприклад, якщо повідомлення відправлено користувачем
        return message.getType().startsWith("sent") ? "Sent" : "Received";
    }

    private String getMessageEmoji(String messageType) {
        switch (messageType) {
            case "Sent":
                return "🧑‍💻 ";
            case "Received":
                return "📦 ";
            default:
                return "";
        }
    }
    @Override
    public String getBotUsername(){
        return "ChatDemoBot";
    }

    private void sendMessage(Long chatId, String message) {
        var chatIdString = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdString, message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Long chatId, String text, String parseMode) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode(parseMode); // Set the parse mode to HTML
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboardMarkup createMainMenuKeyboard() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("/help"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("/about"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("/chats"));

        KeyboardRow row4 = new KeyboardRow();
        row4.add(new KeyboardButton("/login"));

        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardRows.add(row3);
        keyboardRows.add(row4);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }

    private void sendMessageWithReplyKeyboard(Long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(replyKeyboardMarkup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
