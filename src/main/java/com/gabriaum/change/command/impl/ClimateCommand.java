package com.gabriaum.change.command.impl;

import com.gabriaum.change.ChangeMain;
import com.gabriaum.change.command.data.Command;
import com.gabriaum.change.command.data.CommandArgs;
import com.gabriaum.change.type.ChangeType;
import com.gabriaum.change.vote.Vote;
import com.gabriaum.change.vote.scheduler.AutomaticVoteScheduler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClimateCommand {

    @Command(name = "climatepool", aliases = {"cp"}, permission = "climatepool.vote")
    public void climateCommand(CommandArgs commandArgs) {

        String[] args = commandArgs.getArgs();

        CommandSender sender = commandArgs.getSender();

        if (args.length < 1) {

            sender.sendMessage("§6Usage of §a/" + commandArgs.getLabel() + "§6:");
            sender.sendMessage("§e/" + commandArgs.getLabel() + " vote yes - §bvote yes");
            sender.sendMessage("§e/" + commandArgs.getLabel() + " vote no - §bvote no");

            if (commandArgs.isPlayer() && (sender.hasPermission("climatepool.vote.call") || sender.hasPermission("climatepool.admin"))) {

                sender.sendMessage("§e/" + commandArgs.getLabel() + " call <change> - §bto start a vote");

                if (sender.hasPermission("climatepool.admin")) {
                    sender.sendMessage("§e/" + commandArgs.getLabel() + " set <change> - §bto set the climate");
                    sender.sendMessage("§e/" + commandArgs.getLabel() + " automatic <true:false> - §bdisable automatic voting (Scheduler)");
                }
            }

            return;
        }

        ArgumentType argumentType = ArgumentType.getByName(args[0]);

        if (argumentType == null) {
            sender.sendMessage("§cInvalid argument.");
            return;
        }

        argumentType.getArgument().execute(args, commandArgs);
    }

    @Getter
    @RequiredArgsConstructor
    public enum ArgumentType {

        VOTE("vote", (args, commandArgs) -> {

            if (!commandArgs.isPlayer()) {
                commandArgs.getSender().sendMessage("§cYou must be a player to vote.");
                return;
            }

            Player player = commandArgs.getPlayer();

            Vote vote = ChangeMain.getInstance().getVote();

            if (!vote.isVoting()) {
                player.sendMessage("§cThere is no vote in progress.");
                return;
            }

            if (args.length < 2) {

                player.sendMessage("§6Usage of §a/" + commandArgs.getLabel() + "§6:");
                player.sendMessage("§e/" + commandArgs.getLabel() + " vote yes - §bvote yes");
                player.sendMessage("§e/" + commandArgs.getLabel() + " vote no - §bvote no");

                return;
            }

            boolean isYesVote = args[1].equalsIgnoreCase("yes");

            if (isYesVote) {

                if (vote.getAccepted().contains(player.getUniqueId())) {
                    player.sendMessage("§cYou have already voted!");
                    return;
                }

                vote.removeDenied(player.getUniqueId());
                vote.addAccepted(player.getUniqueId());

                player.sendMessage("§aYou voted yes!");
                return;
            }

            if (vote.getDenied().contains(player.getUniqueId())) {
                player.sendMessage("§cYou have already voted!");
                return;
            }

            vote.removeAccepted(player.getUniqueId());
            vote.addDenied(player.getUniqueId());

            player.sendMessage("§cYou voted no!");
        }),

        CALL("call", (args, commandArgs) -> {

            CommandSender sender = commandArgs.getSender();

            if (commandArgs.isPlayer() && !sender.hasPermission("climatepool.vote.call") && !sender.hasPermission("climatepool.admin")) {
                sender.sendMessage("§cYou do not have permission to call a vote.");
                return;
            }

            Vote vote = ChangeMain.getInstance().getVote();

            if (vote.isVoting()) {
                sender.sendMessage("§cThere is already a vote in progress.");
                return;
            }

            if (args.length < 2) {
                sender.sendMessage("§6Usage of §a/" + commandArgs.getLabel() + "§6:");
                sender.sendMessage("§e/" + commandArgs.getLabel() + " call <change> - §bto start a vote");
                return;
            }

            ChangeType changeType = ChangeType.getByName(args[1]);

            if (changeType == null) {
                sender.sendMessage("§cInvalid change type.");
                return;
            }

            sender.sendMessage("§aYou called a vote for " + changeType.name() + "!");

            vote.setExpectedType(changeType);
            vote.setVoting(true);
            vote.setDeadlineCount(ChangeMain.getInstance().getConfig().getInt("vote.scheduler.deadline"));
        }),

        SET("set", (args, commandArgs) -> {

            CommandSender sender = commandArgs.getSender();

            Vote vote = ChangeMain.getInstance().getVote();

            if (vote.isVoting()) {
                sender.sendMessage("§cThere is already a vote in progress.");
                return;
            }

            if (commandArgs.isPlayer() && !sender.hasPermission("climatepool.admin")) {
                sender.sendMessage("§cYou do not have permission to set the climate.");
                return;
            }

            if (args.length < 2) {
                sender.sendMessage("§6Usage of §a/" + commandArgs.getLabel() + "§6:");
                sender.sendMessage("§e/" + commandArgs.getLabel() + " set <change> - §bto set the climate");
                return;
            }

            ChangeType changeType = ChangeType.getByName(args[1]);

            if (changeType == null) {
                sender.sendMessage("§cInvalid change type.");
                return;
            }

            vote.setActualType(changeType);

            changeType.getExecutor().execute(AutomaticVoteScheduler.getWorlds());

            sender.sendMessage("§aYou set the climate to " + changeType.name() + "!");
        }),

        AUTOMATIC("automatic", ((args, commandArgs) -> {

            CommandSender sender = commandArgs.getSender();

            if (commandArgs.isPlayer() && !sender.hasPermission("climatepool.admin")) {
                sender.sendMessage("§cYou do not have permission to set the climate.");
                return;
            }

            if (args.length < 2) {
                sender.sendMessage("§6Usage of §a/" + commandArgs.getLabel() + "§6:");
                sender.sendMessage("§e/" + commandArgs.getLabel() + " automatic <true:false> - §bdisable automatic voting (Scheduler)");
                return;
            }

            boolean isAutomatic = Boolean.parseBoolean(args[1]);

            Vote vote = ChangeMain.getInstance().getVote();

            if (vote.isAutomatic() == isAutomatic) {
                sender.sendMessage("§cThe automatic voting is already set to " + isAutomatic + "!");
                return;
            }

            vote.setAutomatic(isAutomatic);

            sender.sendMessage("§aYou set the automatic voting to " + isAutomatic + "!");
        }))
        ;

        private final String name;

        private final Argument argument;

        public static ArgumentType getByName(String name) {
            for (ArgumentType argumentType : values()) {
                if (argumentType.getName().equalsIgnoreCase(name)) {
                    return argumentType;
                }
            }
            return null;
        }
    }

    public interface Argument {

        void execute(String[] args, CommandArgs commandArgs);
    }
}
