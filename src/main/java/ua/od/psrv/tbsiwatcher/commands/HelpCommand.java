/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher.commands;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;
import ua.od.psrv.tbsiwatcher.Application;

/**
 *
 * @author Prk
 */
public class HelpCommand extends BotCommand {

    private static final String LOGTAG = "HELPCOMMAND";

    public HelpCommand() {
        super("/help", "Справка по работе с ботом");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        try {
            SendMessage answer = new SendMessage();
            answer.setChatId(chat.getId().toString());
            answer.enableMarkdown(false);
            String message = "Бот мониторинга поступлений по подписке на сайте https://siwatcher.ru/ \n";
            message += "Ветка форума для обсуждений http://forum.siwatcher.ru/thread-17.html";
            answer.setText(message);
            absSender.sendMessage(answer);

            answer = new SendMessage();
            answer.setChatId(chat.getId().toString());
            answer.enableMarkdown(true);
            message = "Версия: "+Application.databaseManager.getVersion()+"\n";
            message += "`Для получения ленты обновлений необходим ID пользователя. Его можно узнать при редактирование профиля аккаунта на вышеуказанном сайте.\n";
            message += "Командой:` */settings user ID*\n";
            message += "Другие комманды:\n";
            message += " */help* - `Эта справка`\n";
            message += " */settings* - `Управление настройками с помощью клавиатуры`\n";
            message += " */settings version* - `Номер версии бота`\n";
            message += " */settings subscribe on | off* - `Включает или отключает автоматическую подписку`\n";
            message += " */settings set subscribe.type.text_deleted on | off* - `Включает или отключает Информирование об удалении текста`\n";
            message += " */settings set subscribe.type.text_updated on | off* - `Включает или отключает Информирование об изменении текста`\n";
            message += " */settings set subscribe.type.text_update.size_incremented on | off* -`Включает или отключает Информирование только об увеличении объема текста`\n";
            message += " */settings set subscribe.type.author_typed on | off* - `Включает или отключает Информирование о появлении новых книг у авторов в подписке`\n";
            message += " */settings set location* - `Позволяет указать Ваше местоположение. Необходимо для определения часового пояса. (Работает пока только на мобильных телефонах)`\n";
            message += " */settings get location* - `Возвращает ссылку на карту с местоположением`\n";
            message += " */settings set timeout* - `Позволяет указать Ваш часовоq пояс. Указывается в секундах, например часовой пояс +3 часа, тогда необходимо указать 10800 (3*60*60)`\n";
            message += " */settings get timeout* - `Возвращает установленый часовой пояс в секундах`\n";
            message += " */list* - `Список поступивших произведений в ленту обновлений`\n";
            message += " */list [[page|pages|paginate] [#]]* - `Разбивка списка обновлений по странично`\n";
            message += " */list [autofiltering=yes|autofiltering=no]* - `Принудительное включение/отключение автоматической фильтрации`\n";
            message += " */list [lastday]* - `Вывод всех обновлений за последние сутки`\n";
            message += " */list [[fromdate|fromday] [dd.mm.yyyy]]* - `Вывод всех обновлений с указаной даты`\n";
            message += " */list [[random] [#]]* - `Вывод указаного количества случайных обновлений`\n";
            message += " `Ключи команды /list можно комбинировать`\n";
            message += " */countusers* - `Количество зарегистрированных пользователей`\n";
            answer.setText(message);
            absSender.sendMessage(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

}
