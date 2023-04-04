package com.darianngo.discordBot.commands;

import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.services.UserService;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SetUserRankingCommand {
    public static void setUserRanking(MessageReceivedEvent event, UserService userService, String content) {
        String[] parts = content.split(" ");
        if (parts.length == 2) {
            String userId = parts[0];
            int ranking;
            try {
                ranking = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("Invalid ranking format!").queue();
                return;
            }
            event.getJDA().retrieveUserById(userId).queue(user -> {
                if (user != null) {
                    UserDTO updatedUser = userService.setRanking(userId, ranking, user.getName());
                    if (updatedUser != null) {
                        String userName = updatedUser.getName();
                        event.getChannel().sendMessage("User " + userName + " ranking updated successfully!").queue();
                    } else {
                        event.getChannel().sendMessage("User not found!").queue();
                    }
                } else {
                    event.getChannel().sendMessage("User not found!").queue();
                }
            }, throwable -> {
                event.getChannel().sendMessage("User not found!").queue();
            });
        } else {
            event.getChannel().sendMessage("Invalid command format! Usage: !setRanking <userId> <ranking>").queue();
        }
    }
}
