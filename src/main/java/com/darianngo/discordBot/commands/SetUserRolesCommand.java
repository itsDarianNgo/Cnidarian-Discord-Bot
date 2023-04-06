package com.darianngo.discordBot.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.services.UserService;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SetUserRolesCommand {
    private static final List<String> validRoles = Arrays.asList("top", "mid", "jungle", "adc", "support");

    public static void setUserRoles(MessageReceivedEvent event, UserService userService, String content) {
        String[] parts = content.split(" ");
        if (parts.length == 4) {
            String userId = parts[0];
            String primaryRole = parts[1].toLowerCase(Locale.ROOT);
            String secondaryRole = parts[2].toLowerCase(Locale.ROOT);
            String tertiaryRole = parts[3].toLowerCase(Locale.ROOT);

            if (validRoles.contains(primaryRole) && validRoles.contains(secondaryRole) && validRoles.contains(tertiaryRole)) {
                event.getJDA().retrieveUserById(userId).queue(user -> {
                    if (user != null) {
                        UserDTO updatedUser = userService.setRoles(userId, primaryRole, secondaryRole, tertiaryRole, user.getName());
                        if (updatedUser != null) {
                            String userName = updatedUser.getName();
                            event.getChannel().sendMessage("User " + userName + " roles updated successfully!").queue();
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
                event.getChannel().sendMessage("Invalid role(s)! Valid roles: Top, Mid, Jungle, Adc, Support").queue();
            }
        } else {
            event.getChannel().sendMessage("Invalid command format! Usage: !setRoles <userId> <primaryRole> <secondaryRole> <tertiaryRole>").queue();
        }
    }
}
